package io.openems.edge.controller.api.backend;

import java.time.Duration;

/**
 * Interval over which Channel values are aggregated before they are sent to the
 * OpenEMS Backend. The longest interval is five minutes, because a Channel
 * keeps its past values only for
 * {@link io.openems.edge.common.channel.Channel#MAX_AGE_OF_PAST_VALUES}.
 */
public enum AggregationInterval {

	ONE_MINUTE(Duration.ofMinutes(1)), //
	TWO_MINUTES(Duration.ofMinutes(2)), //
	THREE_MINUTES(Duration.ofMinutes(3)), //
	FOUR_MINUTES(Duration.ofMinutes(4)), //
	FIVE_MINUTES(Duration.ofMinutes(5)), //
	;

	private final Duration duration;

	private AggregationInterval(Duration duration) {
		this.duration = duration;
	}

	/**
	 * Gets the length of this interval.
	 *
	 * @return the {@link Duration}
	 */
	public Duration getDuration() {
		return this.duration;
	}

}
