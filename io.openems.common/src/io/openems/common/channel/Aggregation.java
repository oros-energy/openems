package io.openems.common.channel;

import io.openems.common.types.OpenemsType;

/**
 * The {@link Aggregation} combines the values of a Channel within a time window
 * into one value, for example for the aggregated values that
 * Controller.Api.Backend sends to the OpenEMS Backend.
 *
 * <p>
 * The {@link Aggregation} of a Channel may be set via
 * AbstractDoc::aggregation(). Defaults are:
 * <ul>
 * <li>{@link #MOST_FREQUENT} for an EnumDoc
 * <li>{@link #MAX} for a cumulated Unit
 * <li>{@link #FIRST} for a String
 * <li>{@link #MEAN} for everything else
 * </ul>
 */
public enum Aggregation {

	/**
	 * The average value, rounded for integer types. A Boolean is true if it was
	 * true most of the time.
	 */
	MEAN, //

	/**
	 * The highest value. A Boolean is true if it was true at any time.
	 */
	MAX, //

	/**
	 * The lowest value. A Boolean is false if it was false at any time.
	 */
	MIN, //

	/**
	 * The first value.
	 */
	FIRST, //

	/**
	 * The last value.
	 */
	LATEST, //

	/**
	 * The value that occurred most often. On a tie the one that occurred last.
	 */
	MOST_FREQUENT, //

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
		case MEAN, MAX, MIN -> type != OpenemsType.STRING;
		case FIRST, LATEST, MOST_FREQUENT -> true;
		case BITWISE_OR -> type == OpenemsType.SHORT || type == OpenemsType.INTEGER || type == OpenemsType.LONG;
		};
	}

}
