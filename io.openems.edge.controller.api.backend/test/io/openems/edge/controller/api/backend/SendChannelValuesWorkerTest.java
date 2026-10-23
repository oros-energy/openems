package io.openems.edge.controller.api.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;

import io.openems.common.channel.Aggregation;
import io.openems.common.channel.Unit;
import io.openems.common.function.ThrowingFunction;
import io.openems.common.types.OpenemsType;
import io.openems.common.types.OptionsEnum;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.backend.SendChannelValuesWorkerTest.DummyComponent.DummyEnum;

public class SendChannelValuesWorkerTest {

	@Test
	public void testAggregateNaturalCumulated() {
		final var value = SendChannelValuesWorker.aggregate(Aggregation.MAX, OpenemsType.LONG, //
				Lists.newArrayList(2, 4));
		assertEquals(4, value.getAsLong());
	}

	@Test
	public void testAggregateNaturalNotCumulated() {
		final var value = SendChannelValuesWorker.aggregate(Aggregation.MEAN, OpenemsType.LONG, //
				Lists.newArrayList(2, 7));
		assertEquals(5, value.getAsLong());
	}

	@Test
	public void testAggregateFloatingCumulated() {
		final var value = SendChannelValuesWorker.aggregate(Aggregation.MAX, OpenemsType.DOUBLE, //
				Lists.newArrayList(2.23, 4.75));
		assertEquals(4.75, value.getAsDouble(), 0);
	}

	@Test
	public void testAggregateFloatingNotCumulated() {
		final var value = SendChannelValuesWorker.aggregate(Aggregation.MEAN, OpenemsType.DOUBLE, //
				Lists.newArrayList(2.9, 7.1));
		assertEquals(5, value.getAsDouble(), 0);
	}

	@Test
	public void testAggregateString() {
		final var value = SendChannelValuesWorker.aggregate(Aggregation.FIRST, OpenemsType.STRING, //
				Lists.newArrayList("a", "b", "c", "d", "e"));
		assertEquals("a", value.getAsString());
	}

	@Test
	public void testAggregateBooleanMeanIsTheMajority() {
		assertEquals(1, SendChannelValuesWorker.aggregate(Aggregation.MEAN, OpenemsType.BOOLEAN, //
				Lists.newArrayList(true, true, false)).getAsLong());
		assertEquals(0, SendChannelValuesWorker.aggregate(Aggregation.MEAN, OpenemsType.BOOLEAN, //
				Lists.newArrayList(true, false, false)).getAsLong());
	}

	@Test
	public void testAggregateWithMostFrequent() {
		assertEquals(1, SendChannelValuesWorker.aggregate(Aggregation.MOST_FREQUENT, OpenemsType.INTEGER, //
				Lists.newArrayList(1, 1, 3)).getAsInt());
		assertEquals(1, SendChannelValuesWorker.aggregate(Aggregation.MOST_FREQUENT, OpenemsType.INTEGER, //
				Lists.newArrayList(1, 3, 3, 1)).getAsInt());
		assertEquals("b", SendChannelValuesWorker.aggregate(Aggregation.MOST_FREQUENT, OpenemsType.STRING, //
				Lists.newArrayList("a", "b", "b")).getAsString());
	}

	@Test
	public void testAggregateEnumDocExactValueInsteadOfRounded() throws Exception {
		testAggregateEnumChannel(4, channel -> {
			writeValue(channel, DummyEnum.VALUE_1);
			writeValue(channel, DummyEnum.VALUE_3);
			writeValue(channel, DummyEnum.VALUE_3);

			return DummyEnum.VALUE_3;
		});
	}

	@Test
	public void testAggregateEnumChannelHandleNulls() throws Exception {
		testAggregateEnumChannel(5, channel -> {
			writeValue(channel, DummyEnum.VALUE_1);
			writeValue(channel, null);
			writeValue(channel, DummyEnum.VALUE_3);
			writeValue(channel, DummyEnum.VALUE_3);

			return DummyEnum.VALUE_3;
		});
	}

	@Test
	public void testAggregateEnumChannelSameAmountButLatests() throws Exception {
		testAggregateEnumChannel(5, channel -> {
			writeValue(channel, DummyEnum.VALUE_1);
			writeValue(channel, DummyEnum.VALUE_1);
			writeValue(channel, DummyEnum.VALUE_3);
			writeValue(channel, DummyEnum.VALUE_3);

			return DummyEnum.VALUE_3;
		});
	}

	@Test
	public void testAggregateEnumChannelSameAmountButLatestsWithAll() throws Exception {
		testAggregateEnumChannel(4, channel -> {
			writeValue(channel, DummyEnum.VALUE_1);
			writeValue(channel, DummyEnum.VALUE_2);
			writeValue(channel, DummyEnum.VALUE_3);

			return DummyEnum.VALUE_3;
		});
	}

	@Test
	public void testAggregateEnumChannelSameAmountButLatestsWithAllViceVersa() throws Exception {
		testAggregateEnumChannel(4, channel -> {
			writeValue(channel, DummyEnum.VALUE_3);
			writeValue(channel, DummyEnum.VALUE_2);
			writeValue(channel, DummyEnum.VALUE_1);

			return DummyEnum.VALUE_1;
		});
	}

	@Test
	public void testGetAggregationWindowEnd() {
		final var now = ZonedDateTime.of(2026, 9, 28, 10, 3, 30, 0, ZoneOffset.UTC);
		assertEquals(ZonedDateTime.of(2026, 9, 28, 10, 3, 0, 0, ZoneOffset.UTC), //
				SendChannelValuesWorker.getAggregationWindowEnd(now, AggregationInterval.ONE_MINUTE.getDuration()));
		assertEquals(ZonedDateTime.of(2026, 9, 28, 10, 0, 0, 0, ZoneOffset.UTC), //
				SendChannelValuesWorker.getAggregationWindowEnd(now, AggregationInterval.FIVE_MINUTES.getDuration()));
	}

	@Test
	public void testEveryAggregationIntervalFitsIntoChannelHistory() {
		final var now = ZonedDateTime.of(2026, 9, 28, 10, 3, 30, 0, ZoneOffset.UTC);
		final var history = Duration.from(Channel.MAX_AGE_OF_PAST_VALUES);
		for (var interval : AggregationInterval.values()) {
			final var duration = interval.getDuration();
			assertTrue(duration.compareTo(history) < 0);

			final var end = SendChannelValuesWorker.getAggregationWindowEnd(now, duration);
			assertFalse(end.isAfter(now));
			assertTrue(Duration.between(end, now).compareTo(duration) < 0);
		}
	}

	@Test
	public void testGetAggregatedDataSendDelay() {
		final var fiveMinutes = AggregationInterval.FIVE_MINUTES.getDuration();
		final var oneMinute = AggregationInterval.ONE_MINUTE.getDuration();
		final var highestFactor = Math.nextDown(1.0);

		assertEquals(Duration.ZERO, SendChannelValuesWorker.getAggregatedDataSendDelay(fiveMinutes, 0));
		assertEquals(Duration.ofSeconds(135), SendChannelValuesWorker.getAggregatedDataSendDelay(fiveMinutes, 0.5));
		assertEquals(Duration.ofSeconds(27), SendChannelValuesWorker.getAggregatedDataSendDelay(oneMinute, 0.5));
		assertTrue(SendChannelValuesWorker.getAggregatedDataSendDelay(fiveMinutes, highestFactor)
				.compareTo(Duration.ofSeconds(270)) < 0);
		assertTrue(SendChannelValuesWorker.getAggregatedDataSendDelay(oneMinute, highestFactor)
				.compareTo(Duration.ofSeconds(54)) < 0);
	}

	@Test
	public void testAggregateWithMax() {
		assertEquals(7, SendChannelValuesWorker.aggregate(Aggregation.MAX, OpenemsType.LONG, //
				Lists.newArrayList(2, 7, 3)).getAsLong());
		assertEquals(7.5, SendChannelValuesWorker.aggregate(Aggregation.MAX, OpenemsType.DOUBLE, //
				Lists.newArrayList(2.5, 7.5, 3.5)).getAsDouble(), 0);
		assertEquals(1, SendChannelValuesWorker.aggregate(Aggregation.MAX, OpenemsType.BOOLEAN, //
				Lists.newArrayList(false, true, false)).getAsLong());
	}

	@Test
	public void testAggregateWithMin() {
		assertEquals(2, SendChannelValuesWorker.aggregate(Aggregation.MIN, OpenemsType.LONG, //
				Lists.newArrayList(2, 7, 3)).getAsLong());
		assertEquals(2.5, SendChannelValuesWorker.aggregate(Aggregation.MIN, OpenemsType.DOUBLE, //
				Lists.newArrayList(2.5, 7.5, 3.5)).getAsDouble(), 0);
		assertEquals(0, SendChannelValuesWorker.aggregate(Aggregation.MIN, OpenemsType.BOOLEAN, //
				Lists.newArrayList(true, false, true)).getAsLong());
	}

	@Test
	public void testAggregateWithLatest() {
		assertEquals(3, SendChannelValuesWorker.aggregate(Aggregation.LATEST, OpenemsType.LONG, //
				Lists.newArrayList(2, 7, 3)).getAsLong());
		assertEquals(3.5, SendChannelValuesWorker.aggregate(Aggregation.LATEST, OpenemsType.DOUBLE, //
				Lists.newArrayList(2.5, 7.5, 3.5)).getAsDouble(), 0);
		assertEquals("c", SendChannelValuesWorker.aggregate(Aggregation.LATEST, OpenemsType.STRING, //
				Lists.newArrayList("a", "b", "c")).getAsString());
	}

	@Test
	public void testAggregateWithBitwiseOr() {
		assertEquals(13, SendChannelValuesWorker.aggregate(Aggregation.BITWISE_OR, OpenemsType.LONG, //
				Lists.newArrayList(1, 4, 8, 4)).getAsLong());
	}

	@Test
	public void testAggregateWithoutValues() {
		for (var aggregation : Aggregation.values()) {
			assertTrue(SendChannelValuesWorker.aggregate(aggregation, OpenemsType.LONG, //
					Lists.newArrayList()).isJsonNull());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAggregateNotApplicableToType() {
		SendChannelValuesWorker.aggregate(Aggregation.BITWISE_OR, OpenemsType.DOUBLE, //
				Lists.newArrayList(1.0));
	}

	@Test
	public void testAggregateChannelKeepsMostFrequentEnumValueByDefault() throws Exception {
		final var value = aggregateWrittenValues(DummyComponent.ChannelId.DUMMY_ENUM_CHANNEL, //
				DummyEnum.VALUE_1, DummyEnum.VALUE_1, DummyEnum.VALUE_1, DummyEnum.VALUE_3);
		assertEquals(DummyEnum.VALUE_1.getValue(), value.getAsInt());
	}

	@Test
	public void testAggregateChannelTakesTheMeanByDefault() throws Exception {
		final var value = aggregateWrittenValues(DummyComponent.ChannelId.DUMMY_LONG_CHANNEL, 2L, 7L);
		assertEquals(5, value.getAsLong());
	}

	@Test
	public void testAggregateChannelTakesTheHighestValueOfACumulatedUnit() throws Exception {
		final var value = aggregateWrittenValues(DummyComponent.ChannelId.DUMMY_ENERGY_CHANNEL, 1000L, 1005L, 1010L);
		assertEquals(1010, value.getAsLong());
	}

	@Test
	public void testAggregateChannelWithMaxAggregation() throws Exception {
		final var value = aggregateWrittenValues(DummyComponent.ChannelId.DUMMY_ENUM_MAX_CHANNEL, //
				DummyEnum.VALUE_1, DummyEnum.VALUE_1, DummyEnum.VALUE_1, DummyEnum.VALUE_3);
		assertEquals(DummyEnum.VALUE_3.getValue(), value.getAsInt());
	}

	@Test
	public void testAggregateChannelWithBitwiseOrAggregation() throws Exception {
		final var value = aggregateWrittenValues(DummyComponent.ChannelId.DUMMY_ALARM_CHANNEL, 1L, 4L, 0L);
		assertEquals(5, value.getAsLong());
	}

	private static JsonElement aggregateWrittenValues(DummyComponent.ChannelId channelId, Object... values)
			throws InterruptedException {
		final var component = new DummyComponent("component0");
		final Channel<?> channel = component.channel(channelId);
		final var start = LocalDateTime.now();
		for (var value : values) {
			writeValue(channel, value);
		}
		final var end = LocalDateTime.now().plusSeconds(1);
		return SendChannelValuesWorker.aggregateChannel(channel, start, end);
	}

	private static void testAggregateEnumChannel(int numberOfValues,
			ThrowingFunction<Channel<?>, DummyEnum, Exception> test) throws Exception {
		final var component = new DummyComponent("component0");
		final var channel = component.<Channel<DummyEnum>>channel(DummyComponent.ChannelId.DUMMY_ENUM_CHANNEL);
		final var start = LocalDateTime.now();
		final var expectedValue = test.apply(channel);
		final var end = LocalDateTime.now().plusSeconds(1);

		assertEquals(numberOfValues, channel.getPastValues().size());

		final var aggregatedValue = SendChannelValuesWorker.aggregateChannel(channel, start, end);
		assertTrue(aggregatedValue.isJsonPrimitive());
		assertTrue(aggregatedValue.getAsJsonPrimitive().isNumber());
		assertEquals(expectedValue.getValue(), aggregatedValue.getAsInt());
	}

	private static void writeValue(Channel<?> channel, Object value) throws InterruptedException {
		// Needs sleep to not overwrite the latest value
		Thread.sleep(1);
		channel.setNextValue(value);
		channel.nextProcessImage();
	}

	public static class DummyComponent extends AbstractOpenemsComponent implements OpenemsComponent {

		public enum DummyEnum implements OptionsEnum {
			UNDEFINED(-1, "Undefined"), //
			VALUE_1(1, "Value 1"), //
			VALUE_2(2, "Value 2"), //
			VALUE_3(3, "Value 3"), //
			;

			private final int value;
			private final String name;

			private DummyEnum(int value, String name) {
				this.value = value;
				this.name = name;
			}

			@Override
			public int getValue() {
				return this.value;
			}

			@Override
			public String getName() {
				return this.name;
			}

			@Override
			public OptionsEnum getUndefined() {
				return DummyEnum.UNDEFINED;
			}

		}

		public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
			/**
			 * Dummy state channels for testing.
			 */
			DUMMY_ENUM_CHANNEL(Doc.of(DummyEnum.values())), //
			DUMMY_ENUM_MAX_CHANNEL(Doc.of(DummyEnum.values()).aggregation(Aggregation.MAX)), //
			DUMMY_ALARM_CHANNEL(Doc.of(OpenemsType.LONG).aggregation(Aggregation.BITWISE_OR)), //
			DUMMY_LONG_CHANNEL(Doc.of(OpenemsType.LONG)), //
			DUMMY_ENERGY_CHANNEL(Doc.of(OpenemsType.LONG).unit(Unit.CUMULATED_WATT_HOURS)), //
			;

			private final Doc doc;

			private ChannelId(Doc doc) {
				this.doc = doc;
			}

			@Override
			public Doc doc() {
				return this.doc;
			}
		}

		public DummyComponent(String id) {
			super(//
					OpenemsComponent.ChannelId.values(), //
					ChannelId.values() //
			);
			super.activate(null, id, "", true);
		}

	}

}
