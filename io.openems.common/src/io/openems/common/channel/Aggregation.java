package io.openems.common.channel;

import io.openems.common.types.OpenemsType;

/**
 * The {@link Aggregation} combines the values of a Channel within a time window
 * into one value, for example for the aggregated values that
 * Controller.Api.Backend sends to the OpenEMS Backend.
 *
 * <p>
 * The {@link Aggregation} of a Channel may be set via
 * AbstractDoc::aggregation(). Without it the values are aggregated to their
 * average, to their maximum for cumulated units, to the most frequent value for
 * an EnumDoc and to the first value for a String.
 */
public enum Aggregation {

	/**
	 * The highest value. A Boolean is true if it was true at any time.
	 */
	MAX, //

	/**
	 * The lowest value. A Boolean is false if it was false at any time.
	 */
	MIN, //

	/**
	 * The last value.
	 */
	LATEST, //

	/**
	 * Every bit that was set at any time.
	 */
	BITWISE_OR, //
	;

	/**
	 * Checks whether this {@link Aggregation} can be applied to values of the given
	 * {@link OpenemsType}.
	 *
	 * @param type the {@link OpenemsType}
	 * @return true if it can be applied
	 */
	public boolean isApplicableTo(OpenemsType type) {
		return switch (this) {
		case MAX, MIN -> type != OpenemsType.STRING;
		case LATEST -> true;
		case BITWISE_OR -> type == OpenemsType.SHORT || type == OpenemsType.INTEGER || type == OpenemsType.LONG;
		};
	}

}
