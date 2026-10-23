package io.openems.edge.common.channel.internal;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Aggregation;
import io.openems.common.channel.ChannelCategory;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.BooleanDoc;
import io.openems.edge.common.channel.DoubleDoc;
import io.openems.edge.common.channel.FloatDoc;
import io.openems.edge.common.channel.IntegerDoc;
import io.openems.edge.common.channel.LongDoc;
import io.openems.edge.common.channel.ShortDoc;
import io.openems.edge.common.channel.StringDoc;

public abstract class OpenemsTypeDoc<T> extends AbstractDoc<T> {

	/**
	 * Gets the {@link OpenemsTypeDoc} for the given {@link OpenemsType}.
	 * 
	 * @param type the {@link OpenemsType}
	 * @return the {@link OpenemsTypeDoc}
	 */
	public static OpenemsTypeDoc<?> of(OpenemsType type) {
		return switch (type) {
		case BOOLEAN -> new BooleanDoc();
		case DOUBLE -> new DoubleDoc();
		case FLOAT -> new FloatDoc();
		case INTEGER -> new IntegerDoc();
		case LONG -> new LongDoc();
		case SHORT -> new ShortDoc();
		case STRING -> new StringDoc();
		};
	}

	protected OpenemsTypeDoc(OpenemsType type) {
		super(type);
	}

	@Override
	public ChannelCategory getChannelCategory() {
		return ChannelCategory.OPENEMS_TYPE;
	}

	/**
	 * Sets the Access-Mode for the Channel.
	 *
	 * <p>
	 * This is validated on construction of the Channel by
	 * {@link AbstractReadChannel}
	 *
	 * @return myself
	 */
	@Override
	public OpenemsTypeDoc<T> accessMode(AccessMode accessMode) {
		super.accessMode(accessMode);
		return this;
	}

	@Override
	public OpenemsTypeDoc<T> aggregation(Aggregation aggregation) {
		super.aggregation(aggregation);
		return this;
	}

	/*
	 * Unit
	 */
	private Unit unit = Unit.NONE;

	/**
	 * Unit. Default: none
	 *
	 * <p>
	 * A cumulated Unit changes the default {@link Aggregation#MEAN} to
	 * {@link Aggregation#MAX}, because a cumulated value only grows.
	 *
	 * @param unit the Unit
	 * @return myself
	 */
	public OpenemsTypeDoc<T> unit(Unit unit) {
		this.unit = unit;
		if (unit.isCumulated() && this.getAggregation() == Aggregation.MEAN) {
			this.aggregation(Aggregation.MAX);
		}
		return this;
	}

	/**
	 * Gets the Unit.
	 *
	 * @return the unit
	 */
	@Override
	public Unit getUnit() {
		return this.unit;
	}

}
