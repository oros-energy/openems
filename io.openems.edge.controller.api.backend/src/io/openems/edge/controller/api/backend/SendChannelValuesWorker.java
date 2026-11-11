package io.openems.edge.controller.api.backend;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Aggregation;
import io.openems.common.jsonrpc.notification.AggregatedDataNotification;
import io.openems.common.jsonrpc.notification.TimestampedDataNotification;
import io.openems.common.timedata.DurationUnit;
import io.openems.common.types.DataTable;
import io.openems.common.types.OpenemsType;
import io.openems.common.utils.ThreadPoolUtils;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.type.TypeUtils;

/**
 * Method {@link #collectData()} is called Synchronously with the Core.Cycle to
 * collect values of Channels. Sending of values is then delegated to an
 * asynchronous task.
 *
 * <p>
 * The logic tries to send changed values once per Cycle and all values once
 * every {@link #SEND_VALUES_OF_ALL_CHANNELS_AFTER_SECONDS}.
 */
public class SendChannelValuesWorker {

	private static final int SEND_VALUES_OF_ALL_CHANNELS_AFTER_SECONDS = 300; /* 5 minutes */

	private final Logger log = LoggerFactory.getLogger(SendChannelValuesWorker.class);

	private final ControllerApiBackendImpl parent;
	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS,
			new ArrayBlockingQueue<>(1), //
			new ThreadFactoryBuilder().setNameFormat(ControllerApiBackendImpl.COMPONENT_NAME + ":SendWorker-%d")
					.build(), //
			new ThreadPoolExecutor.DiscardOldestPolicy());

	private final ScheduledExecutorService aggregatedExecutor = Executors.newScheduledThreadPool(1,
			new ThreadFactoryBuilder()
					.setNameFormat(ControllerApiBackendImpl.COMPONENT_NAME + ":SendAggregatedWorker-%d").build());
	private final double randomDelayFactor = new Random().nextDouble();

	/**
	 * If true: next 'send' sends all channel values.
	 */
	private final AtomicBoolean sendValuesOfAllChannels = new AtomicBoolean(true);
	private final AtomicBoolean sendValuesOfAllChannelsAggregated = new AtomicBoolean(true);

	/**
	 * Keeps the last timestamp when all channel values were sent.
	 */
	private Instant lastSendValuesOfAllChannels = Instant.MIN;

	/**
	 * Keeps the values of last successful send.
	 */
	private Map<String, JsonElement> lastAllValues = ImmutableMap.of();

	private Instant lastSendAggregatedDataTimestamp;

	protected SendChannelValuesWorker(ControllerApiBackendImpl parent) {
		this.parent = parent;
	}

	/**
	 * Triggers sending all Channel values once.
	 */
	public synchronized void sendValuesOfAllChannelsOnce() {
		this.sendValuesOfAllChannels.set(true);
		this.sendValuesOfAllChannelsAggregated.set(true);
	}

	/**
	 * Stops the {@link SendChannelValuesWorker}.
	 */
	public void deactivate() {
		// Shutdown executor
		ThreadPoolUtils.shutdownAndAwaitTermination(this.executor, 5);
		ThreadPoolUtils.shutdownAndAwaitTermination(this.aggregatedExecutor, 5);
	}

	/**
	 * Called synchronously on AFTER_PROCESS_IMAGE event. Collects all the data and
	 * triggers asynchronous sending.
	 */
	public synchronized void collectData() {
		final var now = ZonedDateTime.now(this.parent.componentManager.getClock());
		final var interval = this.parent.config.aggregationInterval().getDuration();

		// Update the values of all channels
		final var enabledComponents = this.parent.componentManager.getEnabledComponents();
		final var allValues = this.collectData(enabledComponents);
		final var aggregatedValues = this.collectAggregatedData(now, interval, enabledComponents);

		// Add to send Queue
		this.executor.execute(new SendTask(this, now.toInstant(), allValues));
		if (aggregatedValues != null && !aggregatedValues.isEmpty()) {
			final var delay = getAggregatedDataSendDelay(interval, this.randomDelayFactor);
			aggregatedValues.rowMap().forEach((timestamp, data) -> {
				this.aggregatedExecutor.schedule(
						new SendAggregatedDataTask(this, Instant.ofEpochMilli(timestamp), data), delay.toMillis(),
						TimeUnit.MILLISECONDS);
			});
		}
	}

	/**
	 * Cycles through all Channels and collects the value.
	 *
	 * @param enabledComponents the enabled components
	 * @return collected data
	 */
	private ImmutableMap<String, JsonElement> collectData(List<OpenemsComponent> enabledComponents) {
		try {
			return enabledComponents.parallelStream() //
					.flatMap(component -> component.channels().parallelStream()) //
					.filter(channel -> // Ignore WRITE_ONLY Channels
					channel.channelDoc().getAccessMode() != AccessMode.WRITE_ONLY //
							// Ignore Low-Priority Channels
							&& channel.channelDoc().getRemotePersistencePriority()
									.isAtLeast(this.parent.config.persistencePriority()))
					.collect(//
							ImmutableMap.toImmutableMap(//
									c -> c.address().toString(), //
									c -> c.value().asJson(), //
									// simple/stupid merge function to avoid
									// 'java.lang.IllegalArgumentException Duplicate Key'
									(t, u) -> {
										this.parent.logWarn(this.log, "Duplicate Key [" + t.toString() + "]");
										return t;
									}));
		} catch (Exception e) {
			// ConcurrentModificationException can happen if Channels are dynamically added
			// or removed
			this.parent.logWarn(this.log, "Unable to collect date: " + e.getMessage());
			return ImmutableMap.of();
		}
	}

	private DataTable collectAggregatedData(ZonedDateTime now, Duration interval,
			List<OpenemsComponent> enabledComponents) {
		final var endTime = getAggregationWindowEnd(now, interval);
		final var startTime = endTime.minus(interval);

		final var timestamp = startTime.toInstant();
		if (this.lastSendAggregatedDataTimestamp == null) {
			this.lastSendAggregatedDataTimestamp = timestamp;
			return null;
		}
		if (timestamp.equals(this.lastSendAggregatedDataTimestamp)) {
			return null;
		}
		this.lastSendAggregatedDataTimestamp = timestamp;
		final var timestampMillis = timestamp.toEpochMilli();

		final var sendAllChannels = this.sendValuesOfAllChannelsAggregated.getAndSet(false);

		final var table = new DataTable();
		enabledComponents.stream() //
				.flatMap(component -> component.channels().stream()) //
				.filter(channel -> // Ignore WRITE_ONLY Channels
				channel.channelDoc().getAccessMode() != AccessMode.WRITE_ONLY //
						// Ignore Low-Priority Channels
						&& channel.channelDoc().getRemotePersistencePriority()
								.isAtLeast(this.parent.config.aggregationPriority()))
				.forEach(channel -> {
					try {
						final var value = aggregateChannel(channel, startTime.toLocalDateTime(),
								endTime.toLocalDateTime());
						if (!sendAllChannels && value.isJsonNull()) {
							return;
						}
						table.put(timestampMillis, channel.address().toString(), value);
					} catch (IllegalArgumentException e) {
						// unable to collect data because types are not matching the expected one
						e.printStackTrace();
					}
				});
		return table;
	}

	/**
	 * Gets the end of the latest completed aggregation window, which is also the
	 * start of the current one.
	 *
	 * @param now      the current time
	 * @param interval the aggregation interval
	 * @return the end of the window
	 */
	protected static ZonedDateTime getAggregationWindowEnd(ZonedDateTime now, Duration interval) {
		return now.truncatedTo(DurationUnit.of(interval));
	}

	/**
	 * Gets the delay before the aggregated values of a window are sent. The delay
	 * is a random share of up to 90 % of the interval, which spreads the load of
	 * many Edges on the Backend and keeps the delay shorter than the interval.
	 *
	 * @param interval the aggregation interval
	 * @param factor   a random factor from 0 inclusive to 1 exclusive
	 * @return the delay
	 */
	protected static Duration getAggregatedDataSendDelay(Duration interval, double factor) {
		return Duration.ofMillis((long) (interval.toMillis() * 0.9 * factor));
	}

	/**
	 * Aggregates the values of a Channel within a time window with the
	 * {@link Aggregation} of its Doc.
	 *
	 * @param channel   the {@link Channel}
	 * @param startTime the start of the window
	 * @param endTime   the end of the window, exclusive
	 * @return the aggregated value
	 * @throws IllegalArgumentException if the values do not match the type of the
	 *                                  Channel
	 */
	protected static JsonElement aggregateChannel(Channel<?> channel, LocalDateTime startTime,
			LocalDateTime endTime) throws IllegalArgumentException {
		// This is the highest timestamp before `startTime`. If existing it is used for
		// the tailMap to make sure we get a Value even for Channels where the value has
		// not changed within the interval.
		final var channelStartTime = Optional //
				.ofNullable(channel.getPastValues().floorKey(startTime)) //
				.orElse(startTime);

		final var values = channel.getPastValues() //
				.tailMap(channelStartTime, true) //
				.entrySet() //
				.stream() //
				.filter(e -> e.getKey().isBefore(endTime)) //
				.filter(e -> e.getValue().isDefined()) //
				.<Object>map(e -> e.getValue().get()) //
				.toList();
		return aggregate(channel.channelDoc().getAggregation(), channel.getType(), values);
	}

	/**
	 * Aggregates values with the given {@link Aggregation}.
	 *
	 * @param aggregation the {@link Aggregation}
	 * @param type        the {@link OpenemsType} of the values
	 * @param values      the values, oldest first
	 * @return the aggregated value, or {@link JsonNull} if there are no values
	 * @throws IllegalArgumentException if the {@link Aggregation} cannot be
	 *                                  applied to the {@link OpenemsType}
	 */
	protected static JsonElement aggregate(Aggregation aggregation, OpenemsType type, List<Object> values)
			throws IllegalArgumentException {
		if (!aggregation.isApplicableTo(type)) {
			throw new IllegalArgumentException(
					"Aggregation [" + aggregation + "] is not applicable to type [" + type + "]");
		}
		if (values.isEmpty()) {
			return JsonNull.INSTANCE;
		}
		final var isFloatingPoint = type == OpenemsType.FLOAT || type == OpenemsType.DOUBLE;
		return switch (aggregation) {
		case MEAN -> {
			if (isFloatingPoint) {
				yield new JsonPrimitive(values.stream() //
						.mapToDouble(item -> TypeUtils.getAsType(OpenemsType.DOUBLE, item)) //
						.average().getAsDouble());
			}
			// round averages to their type
			yield new JsonPrimitive(Math.round(values.stream() //
					.mapToLong(item -> TypeUtils.getAsType(OpenemsType.LONG, item)) //
					.average().getAsDouble()));
		}
		case MAX, MIN -> {
			if (isFloatingPoint) {
				final var stream = values.stream() //
						.mapToDouble(item -> TypeUtils.getAsType(OpenemsType.DOUBLE, item));
				yield new JsonPrimitive(aggregation == Aggregation.MAX //
						? stream.max().getAsDouble() //
						: stream.min().getAsDouble());
			}
			final var stream = values.stream() //
					.mapToLong(item -> TypeUtils.getAsType(OpenemsType.LONG, item));
			yield new JsonPrimitive(aggregation == Aggregation.MAX //
					? stream.max().getAsLong() //
					: stream.min().getAsLong());
		}
		case FIRST -> toJson(type, values.get(0));
		case LATEST -> toJson(type, values.get(values.size() - 1));
		case MOST_FREQUENT -> toJson(type, mostFrequent(values));
		case BITWISE_OR -> new JsonPrimitive(values.stream() //
				.mapToLong(item -> TypeUtils.getAsType(OpenemsType.LONG, item)) //
				.reduce(0L, (a, b) -> a | b));
		};
	}

	/**
	 * Gets the value that occurs most often. On a tie it is the one that occurs
	 * last.
	 *
	 * @param values the values, oldest first
	 * @return the most frequent value
	 */
	private static Object mostFrequent(List<Object> values) {
		final var counts = new HashMap<Object, Integer>();
		Object result = null;
		var maxCount = 0;
		for (var value : values) {
			final var count = counts.merge(value, 1, Integer::sum);
			if (count >= maxCount) {
				maxCount = count;
				result = value;
			}
		}
		return result;
	}

	private static JsonElement toJson(OpenemsType type, Object value) {
		return switch (type) {
		case BOOLEAN, SHORT, INTEGER, LONG -> new JsonPrimitive(TypeUtils.<Long>getAsType(OpenemsType.LONG, value));
		case FLOAT, DOUBLE -> new JsonPrimitive(TypeUtils.<Double>getAsType(OpenemsType.DOUBLE, value));
		case STRING -> new JsonPrimitive(TypeUtils.<String>getAsType(OpenemsType.STRING, value));
		};
	}

	/*
	 * From here things run asynchronously.
	 */
	private static class SendTask implements Runnable {

		private final SendChannelValuesWorker parent;
		private final Instant timestamp;
		private final Map<String, JsonElement> allValues;

		public SendTask(SendChannelValuesWorker parent, Instant timestamp, Map<String, JsonElement> allValues) {
			this.parent = parent;
			this.timestamp = timestamp;
			this.allValues = allValues;
		}

		@Override
		public void run() {
			// Holds the data of the last successful send. If the table is empty, it is also
			// used as a marker to send all data.
			final Map<String, JsonElement> lastAllValues;

			if (this.parent.sendValuesOfAllChannels.getAndSet(false)) {
				// Send values of all Channels once in a while
				lastAllValues = ImmutableMap.of();

			} else if (Duration.between(this.parent.lastSendValuesOfAllChannels, this.timestamp)
					.getSeconds() > SEND_VALUES_OF_ALL_CHANNELS_AFTER_SECONDS) {
				// Send values of all Channels if explicitly asked for
				lastAllValues = ImmutableMap.of();

			} else {
				// Actually use the kept 'lastSentValues'
				// CHECKSTYLE:OFF
				lastAllValues = this.parent.lastAllValues;
				// CHECKSTYLE:ON
			}

			// Round timestamp to Global Cycle-Time
			final var cycleTime = this.parent.parent.cycle.getCycleTime();
			final var timestampMillis = this.timestamp.toEpochMilli() / cycleTime * cycleTime;

			// Prepare message values
			var sendValuesData = new DataTable();
			sendValuesData.reserveRow(timestampMillis, this.allValues.size());

			// Collect Changed values
			for (var entry : this.allValues.entrySet()) {
				var channelAddress = entry.getKey();
				var value = entry.getValue();
				if (!Objects.equals(value, lastAllValues.get(channelAddress))) {
					sendValuesData.put(timestampMillis, channelAddress, value);
				}
			}

			// Create JSON-RPC notification
			var message = new TimestampedDataNotification(sendValuesData);

			// Debug-Log
			if (this.parent.parent.config.debugMode()) {
				this.parent.parent.logInfo(this.parent.log,
						"Sending [" + sendValuesData.size() + " values]: " + sendValuesData);
			}

			// Try to send
			var wasSent = this.parent.parent.websocket.sendMessage(message);

			if (wasSent) {
				// Successfully sent: update information for next runs
				this.parent.lastAllValues = this.allValues;
				if (lastAllValues.isEmpty()) {
					// 'lastSentValues' was empty, i.e. all values were sent
					this.parent.lastSendValuesOfAllChannels = this.timestamp;
				}
			}

		}

	}

	private static final class SendAggregatedDataTask implements Runnable {

		private final SendChannelValuesWorker parent;
		private final Instant timestamp;
		private final Map<String, JsonElement> allValues;

		public SendAggregatedDataTask(SendChannelValuesWorker parent, Instant timestamp,
				Map<String, JsonElement> allValues) {
			super();
			this.parent = parent;
			this.timestamp = timestamp;
			this.allValues = allValues;
		}

		@Override
		public void run() {
			final var data = new DataTable();
			data.putRow(this.timestamp.toEpochMilli(), this.allValues);
			final var message = new AggregatedDataNotification(data);

			final var wasSent = this.parent.parent.websocket.sendMessage(message);

			// Set the UNABLE_TO_SEND channel
			this.parent.parent.getUnableToSendChannel().setNextValue(!wasSent);
		}

	}

}