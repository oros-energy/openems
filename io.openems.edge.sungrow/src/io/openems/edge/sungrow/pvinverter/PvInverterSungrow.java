package io.openems.edge.sungrow.pvinverter;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Level;
import io.openems.common.channel.PersistencePriority;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.EnumReadChannel;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.channel.StateChannel;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;

public interface PvInverterSungrow extends OpenemsComponent {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		SERIAL_NUMBER(Doc.of(OpenemsType.STRING)), //

		NOMINAL_OUTPUT_POWER(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.WATT)//
				.persistencePriority(PersistencePriority.VERY_LOW)), //

		DAILY_ENERGY(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.WATT_HOURS)//
				.persistencePriority(PersistencePriority.MEDIUM)),
		TOTAL_RUNNING_TIME(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.HOUR)//
				.persistencePriority(PersistencePriority.VERY_LOW)),

		INTERNAL_TEMPERATURE(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.DEZIDEGREE_CELSIUS)//
				.persistencePriority(PersistencePriority.VERY_LOW)),

		APPARENT_POWER(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.VOLT_AMPERE)//
				.persistencePriority(PersistencePriority.MEDIUM)), //

		DC_VOLTAGE_1(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.VOLT)), //

		DC_CURRENT_1(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.MILLIAMPERE)), //

		DC_VOLTAGE_2(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.VOLT)), //

		DC_CURRENT_2(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.MILLIAMPERE)), //

		DC_VOLTAGE_3(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.VOLT)), //

		DC_CURRENT_3(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.MILLIAMPERE)), //

		/**
		 * DC Voltage 4.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: V
		 * </ul>
		 */
		DC_VOLTAGE_4(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)), //

		/**
		 * DC Current 4.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: mA
		 * </ul>
		 */
		DC_CURRENT_4(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)), //

		/**
		 * DC Voltage 5.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: V
		 * </ul>
		 */
		DC_VOLTAGE_5(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)), //

		/**
		 * DC Current 5.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: mA
		 * </ul>
		 */
		DC_CURRENT_5(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)), //

		/**
		 * DC Voltage 6.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: V
		 * </ul>
		 */
		DC_VOLTAGE_6(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)), //

		/**
		 * DC Current 6.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: mA
		 * </ul>
		 */
		DC_CURRENT_6(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)), //

		/**
		 * DC Voltage 7.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: V
		 * </ul>
		 */
		DC_VOLTAGE_7(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)), //

		/**
		 * DC Current 7.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: mA
		 * </ul>
		 */
		DC_CURRENT_7(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)), //

		/**
		 * DC Voltage 8.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: V
		 * </ul>
		 */
		DC_VOLTAGE_8(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)), //

		/**
		 * DC Current 8.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: mA
		 * </ul>
		 */
		DC_CURRENT_8(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)), //

		/**
		 * DC Voltage 9.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: V
		 * </ul>
		 */
		DC_VOLTAGE_9(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)), //

		/**
		 * DC Current 9.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: mA
		 * </ul>
		 */
		DC_CURRENT_9(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)), //

		/**
		 * DC Voltage 10.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: V
		 * </ul>
		 */
		DC_VOLTAGE_10(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)), //

		/**
		 * DC Current 10.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: mA
		 * </ul>
		 */
		DC_CURRENT_10(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)), //

		/**
		 * DC Voltage 11.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: V
		 * </ul>
		 */
		DC_VOLTAGE_11(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)), //

		/**
		 * DC Current 11.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: mA
		 * </ul>
		 */
		DC_CURRENT_11(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)), //

		/**
		 * DC Voltage 12.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: V
		 * </ul>
		 */
		DC_VOLTAGE_12(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.VOLT)), //

		/**
		 * DC Current 12.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: mA
		 * </ul>
		 */
		DC_CURRENT_12(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIAMPERE)), //

		DC_POWER(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.WATT)//
				.persistencePriority(PersistencePriority.HIGH)),

		POWER_FACTOR(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.THOUSANDTH)),

		WORK_STATE(Doc.of(WorkState.values())),

		/**
		 * Output Type.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: {@link OutputType}
		 * </ul>
		 */
		OUTPUT_TYPE(Doc.of(OutputType.values())), //

		NEGATIVE_VOLTAGE_TO_THE_GROUND(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.VOLT)//
				.persistencePriority(PersistencePriority.VERY_LOW)),

		BUS_VOLTAGE(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.VOLT)),

		POWER_LIMITATION_SWITCH(Doc.of(OpenemsType.BOOLEAN)//
				.unit(Unit.ON_OFF)//
				.accessMode(AccessMode.READ_WRITE)),
		POWER_LIMITATION_SETTING(Doc.of(OpenemsType.INTEGER)//
				.unit(Unit.PERCENT)//
				.accessMode(AccessMode.READ_WRITE)), //

		/**
		 * Voltage L1-L2. Only available with three-phase three-wire output.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: mV
		 * </ul>
		 */
		VOLTAGE_L1_L2(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT)), //
		/**
		 * Voltage L2-L3. Only available with three-phase three-wire output.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: mV
		 * </ul>
		 */
		VOLTAGE_L2_L3(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT)), //
		/**
		 * Voltage L3-L1. Only available with three-phase three-wire output.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: Integer
		 * <li>Unit: mV
		 * </ul>
		 */
		VOLTAGE_L3_L1(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT)), //
		/**
		 * PV Limit Failed.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: State
		 * </ul>
		 */
		PV_LIMIT_FAILED(Doc.of(Level.FAULT) //
				.text("PV-Limit failed")), //
		/**
		 * Read-Only Mode PV Limit Failed.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: State
		 * </ul>
		 */
		READ_ONLY_MODE_PV_LIMIT_FAILED(Doc.of(Level.WARNING) //
				.text("Read-Only mode is active: PV-Limit failed")), //
		/**
		 * Wrong Phase Wiring Configured.
		 *
		 * <ul>
		 * <li>Interface: PvInverterSungrow
		 * <li>Type: State
		 * </ul>
		 */
		WRONG_PHASE_WIRING_CONFIGURED(Doc.of(Level.WARNING) //
				.text("Configured Phase Wiring does not match the Output Type reported by the inverter"));

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_VOLTAGE_4}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcVoltage4Channel() {
		return this.channel(ChannelId.DC_VOLTAGE_4);
	}

	/**
	 * Gets the DC Voltage 4 in [V]. See {@link ChannelId#DC_VOLTAGE_4}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcVoltage4() {
		return this.getDcVoltage4Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_4}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage4(Integer value) {
		this.getDcVoltage4Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_4}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage4(int value) {
		this.getDcVoltage4Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_CURRENT_4}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcCurrent4Channel() {
		return this.channel(ChannelId.DC_CURRENT_4);
	}

	/**
	 * Gets the DC Current 4 in [mA]. See {@link ChannelId#DC_CURRENT_4}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcCurrent4() {
		return this.getDcCurrent4Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_4}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent4(Integer value) {
		this.getDcCurrent4Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_4}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent4(int value) {
		this.getDcCurrent4Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_VOLTAGE_5}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcVoltage5Channel() {
		return this.channel(ChannelId.DC_VOLTAGE_5);
	}

	/**
	 * Gets the DC Voltage 5 in [V]. See {@link ChannelId#DC_VOLTAGE_5}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcVoltage5() {
		return this.getDcVoltage5Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_5}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage5(Integer value) {
		this.getDcVoltage5Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_5}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage5(int value) {
		this.getDcVoltage5Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_CURRENT_5}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcCurrent5Channel() {
		return this.channel(ChannelId.DC_CURRENT_5);
	}

	/**
	 * Gets the DC Current 5 in [mA]. See {@link ChannelId#DC_CURRENT_5}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcCurrent5() {
		return this.getDcCurrent5Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_5}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent5(Integer value) {
		this.getDcCurrent5Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_5}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent5(int value) {
		this.getDcCurrent5Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_VOLTAGE_6}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcVoltage6Channel() {
		return this.channel(ChannelId.DC_VOLTAGE_6);
	}

	/**
	 * Gets the DC Voltage 6 in [V]. See {@link ChannelId#DC_VOLTAGE_6}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcVoltage6() {
		return this.getDcVoltage6Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_6}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage6(Integer value) {
		this.getDcVoltage6Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_6}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage6(int value) {
		this.getDcVoltage6Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_CURRENT_6}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcCurrent6Channel() {
		return this.channel(ChannelId.DC_CURRENT_6);
	}

	/**
	 * Gets the DC Current 6 in [mA]. See {@link ChannelId#DC_CURRENT_6}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcCurrent6() {
		return this.getDcCurrent6Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_6}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent6(Integer value) {
		this.getDcCurrent6Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_6}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent6(int value) {
		this.getDcCurrent6Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_VOLTAGE_7}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcVoltage7Channel() {
		return this.channel(ChannelId.DC_VOLTAGE_7);
	}

	/**
	 * Gets the DC Voltage 7 in [V]. See {@link ChannelId#DC_VOLTAGE_7}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcVoltage7() {
		return this.getDcVoltage7Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_7}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage7(Integer value) {
		this.getDcVoltage7Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_7}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage7(int value) {
		this.getDcVoltage7Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_CURRENT_7}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcCurrent7Channel() {
		return this.channel(ChannelId.DC_CURRENT_7);
	}

	/**
	 * Gets the DC Current 7 in [mA]. See {@link ChannelId#DC_CURRENT_7}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcCurrent7() {
		return this.getDcCurrent7Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_7}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent7(Integer value) {
		this.getDcCurrent7Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_7}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent7(int value) {
		this.getDcCurrent7Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_VOLTAGE_8}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcVoltage8Channel() {
		return this.channel(ChannelId.DC_VOLTAGE_8);
	}

	/**
	 * Gets the DC Voltage 8 in [V]. See {@link ChannelId#DC_VOLTAGE_8}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcVoltage8() {
		return this.getDcVoltage8Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_8}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage8(Integer value) {
		this.getDcVoltage8Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_8}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage8(int value) {
		this.getDcVoltage8Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_CURRENT_8}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcCurrent8Channel() {
		return this.channel(ChannelId.DC_CURRENT_8);
	}

	/**
	 * Gets the DC Current 8 in [mA]. See {@link ChannelId#DC_CURRENT_8}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcCurrent8() {
		return this.getDcCurrent8Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_8}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent8(Integer value) {
		this.getDcCurrent8Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_8}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent8(int value) {
		this.getDcCurrent8Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_VOLTAGE_9}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcVoltage9Channel() {
		return this.channel(ChannelId.DC_VOLTAGE_9);
	}

	/**
	 * Gets the DC Voltage 9 in [V]. See {@link ChannelId#DC_VOLTAGE_9}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcVoltage9() {
		return this.getDcVoltage9Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_9}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage9(Integer value) {
		this.getDcVoltage9Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_9}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage9(int value) {
		this.getDcVoltage9Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_CURRENT_9}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcCurrent9Channel() {
		return this.channel(ChannelId.DC_CURRENT_9);
	}

	/**
	 * Gets the DC Current 9 in [mA]. See {@link ChannelId#DC_CURRENT_9}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcCurrent9() {
		return this.getDcCurrent9Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_9}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent9(Integer value) {
		this.getDcCurrent9Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_9}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent9(int value) {
		this.getDcCurrent9Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_VOLTAGE_10}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcVoltage10Channel() {
		return this.channel(ChannelId.DC_VOLTAGE_10);
	}

	/**
	 * Gets the DC Voltage 10 in [V]. See {@link ChannelId#DC_VOLTAGE_10}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcVoltage10() {
		return this.getDcVoltage10Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_10}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage10(Integer value) {
		this.getDcVoltage10Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_10}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage10(int value) {
		this.getDcVoltage10Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_CURRENT_10}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcCurrent10Channel() {
		return this.channel(ChannelId.DC_CURRENT_10);
	}

	/**
	 * Gets the DC Current 10 in [mA]. See {@link ChannelId#DC_CURRENT_10}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcCurrent10() {
		return this.getDcCurrent10Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_10}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent10(Integer value) {
		this.getDcCurrent10Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_10}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent10(int value) {
		this.getDcCurrent10Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_VOLTAGE_11}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcVoltage11Channel() {
		return this.channel(ChannelId.DC_VOLTAGE_11);
	}

	/**
	 * Gets the DC Voltage 11 in [V]. See {@link ChannelId#DC_VOLTAGE_11}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcVoltage11() {
		return this.getDcVoltage11Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_11}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage11(Integer value) {
		this.getDcVoltage11Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_11}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage11(int value) {
		this.getDcVoltage11Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_CURRENT_11}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcCurrent11Channel() {
		return this.channel(ChannelId.DC_CURRENT_11);
	}

	/**
	 * Gets the DC Current 11 in [mA]. See {@link ChannelId#DC_CURRENT_11}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcCurrent11() {
		return this.getDcCurrent11Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_11}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent11(Integer value) {
		this.getDcCurrent11Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_11}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent11(int value) {
		this.getDcCurrent11Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_VOLTAGE_12}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcVoltage12Channel() {
		return this.channel(ChannelId.DC_VOLTAGE_12);
	}

	/**
	 * Gets the DC Voltage 12 in [V]. See {@link ChannelId#DC_VOLTAGE_12}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcVoltage12() {
		return this.getDcVoltage12Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_12}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage12(Integer value) {
		this.getDcVoltage12Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_VOLTAGE_12}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcVoltage12(int value) {
		this.getDcVoltage12Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#DC_CURRENT_12}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getDcCurrent12Channel() {
		return this.channel(ChannelId.DC_CURRENT_12);
	}

	/**
	 * Gets the DC Current 12 in [mA]. See {@link ChannelId#DC_CURRENT_12}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getDcCurrent12() {
		return this.getDcCurrent12Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_12}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent12(Integer value) {
		this.getDcCurrent12Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#DC_CURRENT_12}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setDcCurrent12(int value) {
		this.getDcCurrent12Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#OUTPUT_TYPE}.
	 *
	 * @return the Channel
	 */
	public default EnumReadChannel getOutputTypeChannel() {
		return this.channel(ChannelId.OUTPUT_TYPE);
	}

	/**
	 * Gets the Output Type. See {@link ChannelId#OUTPUT_TYPE}.
	 *
	 * @return the {@link OutputType}
	 */
	public default OutputType getOutputType() {
		return this.getOutputTypeChannel().value().asEnum();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#OUTPUT_TYPE}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setOutputType(OutputType value) {
		this.getOutputTypeChannel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#VOLTAGE_L1_L2}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getVoltageL1L2Channel() {
		return this.channel(ChannelId.VOLTAGE_L1_L2);
	}

	/**
	 * Gets the Voltage L1-L2 in [mV]. See {@link ChannelId#VOLTAGE_L1_L2}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getVoltageL1L2() {
		return this.getVoltageL1L2Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#VOLTAGE_L1_L2}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setVoltageL1L2(Integer value) {
		this.getVoltageL1L2Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#VOLTAGE_L1_L2}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setVoltageL1L2(int value) {
		this.getVoltageL1L2Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#VOLTAGE_L2_L3}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getVoltageL2L3Channel() {
		return this.channel(ChannelId.VOLTAGE_L2_L3);
	}

	/**
	 * Gets the Voltage L2-L3 in [mV]. See {@link ChannelId#VOLTAGE_L2_L3}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getVoltageL2L3() {
		return this.getVoltageL2L3Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#VOLTAGE_L2_L3}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setVoltageL2L3(Integer value) {
		this.getVoltageL2L3Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#VOLTAGE_L2_L3}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setVoltageL2L3(int value) {
		this.getVoltageL2L3Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#VOLTAGE_L3_L1}.
	 *
	 * @return the Channel
	 */
	public default IntegerReadChannel getVoltageL3L1Channel() {
		return this.channel(ChannelId.VOLTAGE_L3_L1);
	}

	/**
	 * Gets the Voltage L3-L1 in [mV]. See {@link ChannelId#VOLTAGE_L3_L1}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Integer> getVoltageL3L1() {
		return this.getVoltageL3L1Channel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#VOLTAGE_L3_L1}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setVoltageL3L1(Integer value) {
		this.getVoltageL3L1Channel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#VOLTAGE_L3_L1}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setVoltageL3L1(int value) {
		this.getVoltageL3L1Channel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#PV_LIMIT_FAILED}.
	 *
	 * @return the Channel
	 */
	public default StateChannel getPvLimitFailedChannel() {
		return this.channel(ChannelId.PV_LIMIT_FAILED);
	}

	/**
	 * Gets the PV Limit Failed State. See {@link ChannelId#PV_LIMIT_FAILED}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Boolean> getPvLimitFailed() {
		return this.getPvLimitFailedChannel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#PV_LIMIT_FAILED}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setPvLimitFailed(Boolean value) {
		this.getPvLimitFailedChannel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#PV_LIMIT_FAILED}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setPvLimitFailed(boolean value) {
		this.getPvLimitFailedChannel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#READ_ONLY_MODE_PV_LIMIT_FAILED}.
	 *
	 * @return the Channel
	 */
	public default StateChannel getReadOnlyModePvLimitFailedChannel() {
		return this.channel(ChannelId.READ_ONLY_MODE_PV_LIMIT_FAILED);
	}

	/**
	 * Gets the Read-Only Mode PV Limit Failed State. See {@link ChannelId#READ_ONLY_MODE_PV_LIMIT_FAILED}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Boolean> getReadOnlyModePvLimitFailed() {
		return this.getReadOnlyModePvLimitFailedChannel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#READ_ONLY_MODE_PV_LIMIT_FAILED}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setReadOnlyModePvLimitFailed(Boolean value) {
		this.getReadOnlyModePvLimitFailedChannel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#READ_ONLY_MODE_PV_LIMIT_FAILED}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setReadOnlyModePvLimitFailed(boolean value) {
		this.getReadOnlyModePvLimitFailedChannel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#WRONG_PHASE_WIRING_CONFIGURED}.
	 *
	 * @return the Channel
	 */
	public default StateChannel getWrongPhaseWiringConfiguredChannel() {
		return this.channel(ChannelId.WRONG_PHASE_WIRING_CONFIGURED);
	}

	/**
	 * Gets the Wrong Phase Wiring Configured State. See {@link ChannelId#WRONG_PHASE_WIRING_CONFIGURED}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Boolean> getWrongPhaseWiringConfigured() {
		return this.getWrongPhaseWiringConfiguredChannel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#WRONG_PHASE_WIRING_CONFIGURED}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setWrongPhaseWiringConfigured(Boolean value) {
		this.getWrongPhaseWiringConfiguredChannel().setNextValue(value);
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#WRONG_PHASE_WIRING_CONFIGURED}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setWrongPhaseWiringConfigured(boolean value) {
		this.getWrongPhaseWiringConfiguredChannel().setNextValue(value);
	}
}
