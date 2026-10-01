package io.openems.edge.sungrow.pvinverter;

import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_2;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SET_NULL_FOR_DEFAULT;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.chain;
import static io.openems.edge.common.event.EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE;
import static io.openems.edge.common.event.EdgeEventConstants.TOPIC_CYCLE_BEFORE_WRITE;
import static org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY;
import static org.osgi.service.component.annotations.ReferenceCardinality.OPTIONAL;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;
import static org.osgi.service.component.annotations.ReferencePolicy.STATIC;
import static org.osgi.service.component.annotations.ReferencePolicyOption.GREEDY;

import java.util.Optional;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.referencetarget.GenerateTargetsFromReferences;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.SignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.element.StringWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.element.WordOrder;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC4ReadInputRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC6WriteRegisterTask;
import io.openems.edge.common.channel.IntegerReadChannel;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.pvinverter.api.ManagedSymmetricPvInverter;
import io.openems.edge.timedata.api.Timedata;
import io.openems.edge.timedata.api.TimedataProvider;
import io.openems.edge.timedata.api.utils.CalculateEnergyFromPower;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "PV-Inverter.Sungrow", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		TOPIC_CYCLE_AFTER_PROCESS_IMAGE, //
		TOPIC_CYCLE_BEFORE_WRITE //
})
@GenerateTargetsFromReferences({ "Modbus" })
public class PvInverterSungrowImpl extends AbstractOpenemsModbusComponent implements ManagedSymmetricPvInverter,
		ElectricityMeter, PvInverterSungrow, ModbusComponent, TimedataProvider, OpenemsComponent, EventHandler {

	private static final ElementToChannelConverter ON_OFF_CONVERTER = new ElementToChannelConverter(//
			// Element to Channel
			value -> //
			value == null ? null : (Integer) value == 0xAA, //
			// Channel to Element
			value -> { //
				if (value == null) {
					return null;
				}
				if ((Boolean) value) {
					return 0xAA;
				}
				return 0x55;
			});

	/**
	 * Resolution of the power limitation adjustment register in [W].
	 */
	private static final int POWER_LIMIT_RESOLUTION = 100;

	private final CalculateEnergyFromPower calculateEnergy = new CalculateEnergyFromPower(this,
			ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY);

	@Reference(policy = DYNAMIC, policyOption = GREEDY, cardinality = OPTIONAL)
	private volatile Timedata timedata = null;

	@Override
	@Reference(//
			policy = STATIC, policyOption = GREEDY, cardinality = MANDATORY, //
			target = "(&(id=${config.modbus_id})(enabled=true))")
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	private boolean readOnly = true; // oEMS add
	private PhaseWiring phaseWiring = PhaseWiring.THREE_PHASE_FOUR_WIRE;

	public PvInverterSungrowImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				ManagedSymmetricPvInverter.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //
				PvInverterSungrow.ChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsException {
		this.readOnly = config.readOnly(); // oEMS add
		this.phaseWiring = config.phaseWiring();
		super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId());

		// The inverter reports only its nominal active power, which is used as rated
		// apparent power as well.
		this.<IntegerReadChannel>channel(PvInverterSungrow.ChannelId.NOMINAL_OUTPUT_POWER).onSetNextValue(value -> {
			this._setMaxActivePower(value.get());
			this._setMaxApparentPower(value.get());
		});
		this.getOutputTypeChannel().onSetNextValue(value -> this.detectWrongPhaseWiring(value.asEnum()));
		if (this.phaseWiring == PhaseWiring.THREE_PHASE_THREE_WIRE) {
			this.calculatePhaseVoltagesFromLineVoltages();
		}
		ElectricityMeter.calculatePhasesFromActivePower(this);
		ElectricityMeter.calculateSumCurrentFromPhases(this);
		ElectricityMeter.calculateAverageVoltageFromPhases(this);
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case TOPIC_CYCLE_AFTER_PROCESS_IMAGE //
			-> this.calculateEnergy.update(this.getActivePower().get());
		case TOPIC_CYCLE_BEFORE_WRITE //
			-> this.applyActivePowerLimit();
		}
	}

	/**
	 * Prepares the write of the power limitation adjustment register.
	 *
	 * <p>
	 * Without a requested limit the nominal power is written, so that a previous
	 * limit does not stay active on the inverter. The register is only written
	 * when the limit reported by the inverter differs from the target.
	 */
	private void applyActivePowerLimit() {
		var channel = this.getActivePowerLimitChannel();
		if (this.readOnly) {
			this._setReadOnlyModePvLimitFailed(channel.getNextWriteValueAndReset().isPresent());
			return;
		}

		var target = this.calculateActivePowerLimit(channel.getNextWriteValue());
		try {
			if (target == null || target.equals(channel.value().get())) {
				channel.getNextWriteValueAndReset();
			} else {
				channel.setNextWriteValue(target);
			}
			this._setPvLimitFailed(false);
		} catch (OpenemsNamedException e) {
			this._setPvLimitFailed(true);
		}
	}

	private Integer calculateActivePowerLimit(Optional<Integer> requestedLimit) {
		var maxActivePower = this.getMaxActivePower().get();
		var limit = requestedLimit.orElse(maxActivePower);
		if (limit == null) {
			return null;
		}
		if (maxActivePower != null) {
			limit = Math.min(limit, maxActivePower);
		}
		limit = Math.max(0, limit);
		return Math.round(limit / (float) POWER_LIMIT_RESOLUTION) * POWER_LIMIT_RESOLUTION;
	}

	private void detectWrongPhaseWiring(OutputType outputType) {
		var wrongPhaseWiring = switch (outputType) {
		case THREE_PHASE_FOUR_LINE -> this.phaseWiring != PhaseWiring.THREE_PHASE_FOUR_WIRE;
		case THREE_PHASE_THREE_LINE -> this.phaseWiring != PhaseWiring.THREE_PHASE_THREE_WIRE;
		case TWO_PHASE, UNDEFINED -> false;
		};
		this._setWrongPhaseWiringConfigured(wrongPhaseWiring);
	}

	/**
	 * Derives the phase voltages from the line voltages of an inverter with
	 * three-phase three-wire output. The phase voltage is the line voltage divided
	 * by the square root of three.
	 */
	private void calculatePhaseVoltagesFromLineVoltages() {
		this.getVoltageL1L2Channel().onSetNextValue(value -> this._setVoltageL1(toPhaseVoltage(value.get())));
		this.getVoltageL2L3Channel().onSetNextValue(value -> this._setVoltageL2(toPhaseVoltage(value.get())));
		this.getVoltageL3L1Channel().onSetNextValue(value -> this._setVoltageL3(toPhaseVoltage(value.get())));
	}

	private static Integer toPhaseVoltage(Integer lineVoltage) {
		if (lineVoltage == null) {
			return null;
		}
		return (int) Math.round(lineVoltage / Math.sqrt(3));
	}

	@Override
	protected ModbusProtocol defineModbusProtocol() {
		final io.openems.edge.common.channel.ChannelId voltage1;
		final io.openems.edge.common.channel.ChannelId voltage2;
		final io.openems.edge.common.channel.ChannelId voltage3;
		if (this.phaseWiring == PhaseWiring.THREE_PHASE_THREE_WIRE) {
			voltage1 = PvInverterSungrow.ChannelId.VOLTAGE_L1_L2;
			voltage2 = PvInverterSungrow.ChannelId.VOLTAGE_L2_L3;
			voltage3 = PvInverterSungrow.ChannelId.VOLTAGE_L3_L1;
		} else {
			voltage1 = ElectricityMeter.ChannelId.VOLTAGE_L1;
			voltage2 = ElectricityMeter.ChannelId.VOLTAGE_L2;
			voltage3 = ElectricityMeter.ChannelId.VOLTAGE_L3;
		}

		var protocol = new ModbusProtocol(this);
		protocol.addTasks(//
				new FC4ReadInputRegistersTask(4989, Priority.HIGH, //
						m(PvInverterSungrow.ChannelId.SERIAL_NUMBER, new StringWordElement(4989, 10)), //
						new DummyRegisterElement(4999), // Device Type Code
						m(PvInverterSungrow.ChannelId.NOMINAL_OUTPUT_POWER, new UnsignedWordElement(5000), //
								SCALE_FACTOR_2), //
						m(PvInverterSungrow.ChannelId.OUTPUT_TYPE, new UnsignedWordElement(5001)), //
						m(PvInverterSungrow.ChannelId.DAILY_ENERGY, new UnsignedWordElement(5002), //
								SCALE_FACTOR_2), //
						new DummyRegisterElement(5003, 5004), //
						m(PvInverterSungrow.ChannelId.TOTAL_RUNNING_TIME,
								new UnsignedDoublewordElement(5005).wordOrder(WordOrder.LSWMSW)), //
						m(PvInverterSungrow.ChannelId.INTERNAL_TEMPERATURE, new SignedWordElement(5007)), //
						m(PvInverterSungrow.ChannelId.APPARENT_POWER,
								new UnsignedDoublewordElement(5008).wordOrder(WordOrder.LSWMSW)), //
						m(PvInverterSungrow.ChannelId.DC_VOLTAGE_1, new UnsignedWordElement(5010), //
								SCALE_FACTOR_MINUS_1), //
						m(PvInverterSungrow.ChannelId.DC_CURRENT_1, new UnsignedWordElement(5011), //
								SCALE_FACTOR_2), //
						m(PvInverterSungrow.ChannelId.DC_VOLTAGE_2, new UnsignedWordElement(5012), //
								SCALE_FACTOR_MINUS_1), //
						m(PvInverterSungrow.ChannelId.DC_CURRENT_2, new UnsignedWordElement(5013), //
								SCALE_FACTOR_2), //
						m(PvInverterSungrow.ChannelId.DC_VOLTAGE_3, new UnsignedWordElement(5014), //
								SCALE_FACTOR_MINUS_1), //
						m(PvInverterSungrow.ChannelId.DC_CURRENT_3, new UnsignedWordElement(5015), //
								SCALE_FACTOR_2), //
						m(PvInverterSungrow.ChannelId.DC_POWER,
								new UnsignedDoublewordElement(5016).wordOrder(WordOrder.LSWMSW)), //
						m(voltage1, new UnsignedWordElement(5018), //
								SCALE_FACTOR_2), //
						m(voltage2, new UnsignedWordElement(5019), //
								SCALE_FACTOR_2), //
						m(voltage3, new UnsignedWordElement(5020), //
								SCALE_FACTOR_2), //
						m(ElectricityMeter.ChannelId.CURRENT_L1, new UnsignedWordElement(5021), //
								SCALE_FACTOR_2), //
						m(ElectricityMeter.ChannelId.CURRENT_L2, new UnsignedWordElement(5022), //
								SCALE_FACTOR_2), //
						m(ElectricityMeter.ChannelId.CURRENT_L3, new UnsignedWordElement(5023), //
								SCALE_FACTOR_2), //
						new DummyRegisterElement(5024, 5029), // Reserved
						m(ElectricityMeter.ChannelId.ACTIVE_POWER,
								new UnsignedDoublewordElement(5030).wordOrder(WordOrder.LSWMSW)), //
						m(ElectricityMeter.ChannelId.REACTIVE_POWER,
								new SignedDoublewordElement(5032).wordOrder(WordOrder.LSWMSW)), //
						m(PvInverterSungrow.ChannelId.POWER_FACTOR, new SignedWordElement(5034)), //
						new DummyRegisterElement(5035, 5036), // 5035: Frequency, read from another register
						m(PvInverterSungrow.ChannelId.WORK_STATE, new UnsignedWordElement(5037)) //
				),

				new FC4ReadInputRegistersTask(5048, Priority.LOW, //
						m(ManagedSymmetricPvInverter.ChannelId.MAX_REACTIVE_POWER, new UnsignedWordElement(5048), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_2)) //
				),

				new FC4ReadInputRegistersTask(5114, Priority.LOW, //
						m(PvInverterSungrow.ChannelId.DC_VOLTAGE_4, new UnsignedWordElement(5114), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_MINUS_1)), //
						m(PvInverterSungrow.ChannelId.DC_CURRENT_4, new UnsignedWordElement(5115), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_2)), //
						m(PvInverterSungrow.ChannelId.DC_VOLTAGE_5, new UnsignedWordElement(5116), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_MINUS_1)), //
						m(PvInverterSungrow.ChannelId.DC_CURRENT_5, new UnsignedWordElement(5117), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_2)), //
						m(PvInverterSungrow.ChannelId.DC_VOLTAGE_6, new UnsignedWordElement(5118), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_MINUS_1)), //
						m(PvInverterSungrow.ChannelId.DC_CURRENT_6, new UnsignedWordElement(5119), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_2)), //
						m(PvInverterSungrow.ChannelId.DC_VOLTAGE_7, new UnsignedWordElement(5120), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_MINUS_1)), //
						m(PvInverterSungrow.ChannelId.DC_CURRENT_7, new UnsignedWordElement(5121), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_2)), //
						m(PvInverterSungrow.ChannelId.DC_VOLTAGE_8, new UnsignedWordElement(5122), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_MINUS_1)), //
						m(PvInverterSungrow.ChannelId.DC_CURRENT_8, new UnsignedWordElement(5123), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_2)), //
						new DummyRegisterElement(5124, 5128), // Reserved and monthly power yields
						m(PvInverterSungrow.ChannelId.DC_VOLTAGE_9, new UnsignedWordElement(5129), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_MINUS_1)), //
						m(PvInverterSungrow.ChannelId.DC_CURRENT_9, new UnsignedWordElement(5130), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_2)), //
						m(PvInverterSungrow.ChannelId.DC_VOLTAGE_10, new UnsignedWordElement(5131), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_MINUS_1)), //
						m(PvInverterSungrow.ChannelId.DC_CURRENT_10, new UnsignedWordElement(5132), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_2)), //
						m(PvInverterSungrow.ChannelId.DC_VOLTAGE_11, new UnsignedWordElement(5133), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_MINUS_1)), //
						m(PvInverterSungrow.ChannelId.DC_CURRENT_11, new UnsignedWordElement(5134), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_2)), //
						m(PvInverterSungrow.ChannelId.DC_VOLTAGE_12, new UnsignedWordElement(5135), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_MINUS_1)), //
						m(PvInverterSungrow.ChannelId.DC_CURRENT_12, new UnsignedWordElement(5136), //
								chain(SET_NULL_FOR_DEFAULT(0xFFFF), SCALE_FACTOR_2)) //
				),

				new FC4ReadInputRegistersTask(5145, Priority.LOW, //
						m(PvInverterSungrow.ChannelId.NEGATIVE_VOLTAGE_TO_THE_GROUND, new SignedWordElement(5145), //
								SCALE_FACTOR_MINUS_1), //
						m(PvInverterSungrow.ChannelId.BUS_VOLTAGE, new UnsignedWordElement(5146), //
								SCALE_FACTOR_MINUS_1), //
						m(ElectricityMeter.ChannelId.FREQUENCY, new UnsignedWordElement(5147), //
								SCALE_FACTOR_1) //
				),

				new FC3ReadRegistersTask(5006, Priority.LOW, //
						m(PvInverterSungrow.ChannelId.POWER_LIMITATION_SWITCH, new UnsignedWordElement(5006), //
								ON_OFF_CONVERTER), //
						m(PvInverterSungrow.ChannelId.POWER_LIMITATION_SETTING, new UnsignedWordElement(5007), //
								SCALE_FACTOR_MINUS_1)), //

				new FC3ReadRegistersTask(5038, Priority.HIGH, //
						m(ManagedSymmetricPvInverter.ChannelId.ACTIVE_POWER_LIMIT, new UnsignedWordElement(5038), //
								SCALE_FACTOR_2))); //
		if (!this.readOnly) { // oEMS add
			protocol.addTask(//
					new FC6WriteRegisterTask(5038, //
							m(ManagedSymmetricPvInverter.ChannelId.ACTIVE_POWER_LIMIT, //
									new UnsignedWordElement(5038), SCALE_FACTOR_2)));
		}
		return protocol;
	}

	@Override
	public String debugLog() {
		return "L:" + this.getActivePower().asString();
	}

	@Override
	public Timedata getTimedata() {
		return this.timedata;
	}

}
