package io.openems.edge.sungrow.pvinverter;

import org.junit.jupiter.api.Test;

import io.openems.edge.bridge.modbus.test.DummyModbusBridge;
import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.edge.common.test.ComponentTest;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.pvinverter.api.ManagedSymmetricPvInverter;

public class PvInverterSungrowImplTest {

	@Test
	public void test() throws Exception {
		new ComponentTest(new PvInverterSungrowImpl()) //
				.addReference("setModbus", new DummyModbusBridge("modbus0")) //
				.activate(MyConfig.create() //
						.setId("pvInverter0") //
						.setModbusId("modbus0") //
						.setModbusUnitId(1) //
						.build()) //
				.next(new TestCase()) //
				.deactivate();
	}

	/**
	 * Encodes a 32-bit value as two Modbus registers with the low word first.
	 *
	 * @param value the value
	 * @return the two registers
	 */
	private static int[] u32(long value) {
		return new int[] { (int) (value & 0xFFFF), (int) (value >>> 16 & 0xFFFF) };
	}

	/**
	 * Simulates a Sungrow SG125CX-P2 with twelve MPPT inputs.
	 *
	 * @return the {@link DummyModbusBridge}
	 */
	private static DummyModbusBridge modbusBridge() {
		return new DummyModbusBridge("modbus0") //
				.withInputRegisters(4989, //
						new int[10], // Serial number
						new int[] { 0x2C2D }, // Device type code SG125CX-P2
						new int[] { 1250 }, // Nominal active power 125.0 kW
						new int[] { 1 }, // Output type 3P4L
						new int[] { 1234 }, // Daily power yields 123.4 kWh
						u32(56789), // Total power yields 56789 kWh
						u32(4321), // Total running time 4321 h
						new int[] { 355 }, // Internal temperature 35.5 degree Celsius
						u32(60300), // Total apparent power 60300 VA
						new int[] { 6000, 100 }, // MPPT 1 600.0 V, 10.0 A
						new int[] { 6100, 110 }, // MPPT 2 610.0 V, 11.0 A
						new int[] { 6200, 120 }, // MPPT 3 620.0 V, 12.0 A
						u32(61000), // Total DC power 61000 W
						new int[] { 2300, 2310, 2320 }, // Voltages 230.0 V, 231.0 V, 232.0 V
						new int[] { 870, 880, 890 }, // Phase currents 87.0 A, 88.0 A, 89.0 A
						new int[6], // Reserved
						u32(60000), // Total active power 60000 W
						u32(-1500), // Total reactive power -1500 var
						new int[] { 950 }, // Power factor 0.950
						new int[] { 4999 }, // Grid frequency 49.99 Hz
						new int[] { 0 }, // Reserved
						new int[] { 0x8100 }) // Work state Derating run
				.withInputRegisters(5048, 750) // Nominal reactive power 75.0 kvar
				.withInputRegisters(5114, //
						new int[] { 6300, 130 }, // MPPT 4 630.0 V, 13.0 A
						new int[] { 6400, 140 }, // MPPT 5 640.0 V, 14.0 A
						new int[] { 6500, 150 }, // MPPT 6 650.0 V, 15.0 A
						new int[] { 6600, 160 }, // MPPT 7 660.0 V, 16.0 A
						new int[] { 6700, 170 }, // MPPT 8 670.0 V, 17.0 A
						new int[3], // Reserved
						u32(98765), // Monthly power yields 9876.5 kWh
						new int[] { 6800, 180 }, // MPPT 9 680.0 V, 18.0 A
						new int[] { 6900, 190 }, // MPPT 10 690.0 V, 19.0 A
						new int[] { 7000, 200 }, // MPPT 11 700.0 V, 20.0 A
						new int[] { 0xFFFF, 0xFFFF }) // MPPT 12 not available
				.withInputRegisters(5145, //
						new int[] { 100 }, // Negative voltage to the ground 10.0 V
						new int[] { 7500 }, // Bus voltage 750.0 V
						new int[] { 4999 }) // Grid frequency 49.99 Hz
				.withRegisters(5006, 0xAA, 400) // Power limitation enabled at 40.0 %
				.withRegisters(5038, 400); // Power limitation adjustment 40.0 kW
	}

	@Test
	public void testReadOnly() throws Exception {
		new ComponentTest(new PvInverterSungrowImpl()) //
				.addReference("setModbus", modbusBridge()) //
				.activate(MyConfig.create() //
						.setId("pvInverter0") //
						.setReadOnly(true) //
						.setModbusId("modbus0") //
						.setModbusUnitId(1) //
						.setPhaseWiring(PhaseWiring.THREE_PHASE_FOUR_WIRE) //
						.build()) //
				.next(new TestCase() //
						.output(ManagedSymmetricPvInverter.ChannelId.MAX_ACTIVE_POWER, 125000) //
						.output(ManagedSymmetricPvInverter.ChannelId.MAX_APPARENT_POWER, 125000) //
						.output(PvInverterSungrow.ChannelId.OUTPUT_TYPE, OutputType.THREE_PHASE_FOUR_LINE) //
						.output(PvInverterSungrow.ChannelId.WRONG_PHASE_WIRING_CONFIGURED, false) //
						.output(PvInverterSungrow.ChannelId.DC_VOLTAGE_1, 600) //
						.output(PvInverterSungrow.ChannelId.DC_CURRENT_1, 10000) //
						.output(PvInverterSungrow.ChannelId.DC_VOLTAGE_3, 620) //
						.output(PvInverterSungrow.ChannelId.DC_CURRENT_3, 12000) //
						.output(ElectricityMeter.ChannelId.VOLTAGE_L1, 230000) //
						.output(ElectricityMeter.ChannelId.VOLTAGE_L2, 231000) //
						.output(ElectricityMeter.ChannelId.VOLTAGE_L3, 232000) //
						.output(ElectricityMeter.ChannelId.VOLTAGE, 231000) //
						.output(ElectricityMeter.ChannelId.CURRENT_L1, 87000) //
						.output(ElectricityMeter.ChannelId.CURRENT, 264000) //
						.output(ElectricityMeter.ChannelId.ACTIVE_POWER, 60000) //
						.output(ElectricityMeter.ChannelId.ACTIVE_POWER_L1, 20000) //
						.output(PvInverterSungrow.ChannelId.WORK_STATE, WorkState.DERATING_RUN)) //
				// One low priority task is executed per cycle
				.next(new TestCase()) //
				.next(new TestCase()) //
				.next(new TestCase()) //
				.next(new TestCase() //
						.output(ManagedSymmetricPvInverter.ChannelId.MAX_REACTIVE_POWER, 75000) //
						.output(PvInverterSungrow.ChannelId.DC_VOLTAGE_4, 630) //
						.output(PvInverterSungrow.ChannelId.DC_CURRENT_4, 13000) //
						.output(PvInverterSungrow.ChannelId.DC_VOLTAGE_8, 670) //
						.output(PvInverterSungrow.ChannelId.DC_VOLTAGE_9, 680) //
						.output(PvInverterSungrow.ChannelId.DC_CURRENT_11, 20000) //
						.output(PvInverterSungrow.ChannelId.DC_VOLTAGE_12, null) //
						.output(PvInverterSungrow.ChannelId.DC_CURRENT_12, null) //
						.output(ElectricityMeter.ChannelId.FREQUENCY, 49990)) //
				.next(new TestCase("Power limit is not applied in read-only mode") //
						.input(ManagedSymmetricPvInverter.ChannelId.ACTIVE_POWER_LIMIT, 62500) //
						.output(PvInverterSungrow.ChannelId.READ_ONLY_MODE_PV_LIMIT_FAILED, true) //
						.output(ManagedSymmetricPvInverter.ChannelId.ACTIVE_POWER_LIMIT, null)) //
				.next(new TestCase() //
						.output(PvInverterSungrow.ChannelId.READ_ONLY_MODE_PV_LIMIT_FAILED, false)) //
				.deactivate();
	}

	@Test
	public void testThreePhaseThreeWire() throws Exception {
		new ComponentTest(new PvInverterSungrowImpl()) //
				.addReference("setModbus", modbusBridge()) //
				.activate(MyConfig.create() //
						.setId("pvInverter0") //
						.setReadOnly(true) //
						.setModbusId("modbus0") //
						.setModbusUnitId(1) //
						.setPhaseWiring(PhaseWiring.THREE_PHASE_THREE_WIRE) //
						.build()) //
				.next(new TestCase() //
						.output(PvInverterSungrow.ChannelId.VOLTAGE_L1_L2, 230000) //
						.output(PvInverterSungrow.ChannelId.VOLTAGE_L2_L3, 231000) //
						.output(PvInverterSungrow.ChannelId.VOLTAGE_L3_L1, 232000) //
						.output(ElectricityMeter.ChannelId.VOLTAGE_L1, 132791) //
						.output(ElectricityMeter.ChannelId.VOLTAGE_L2, 133368) //
						.output(ElectricityMeter.ChannelId.VOLTAGE_L3, 133945) //
						.output(ElectricityMeter.ChannelId.VOLTAGE, 133368) //
						// The inverter reports a 3P4L output type
						.output(PvInverterSungrow.ChannelId.WRONG_PHASE_WIRING_CONFIGURED, true)) //
				.deactivate();
	}

	@Test
	public void testActivePowerLimit() throws Exception {
		var sut = new PvInverterSungrowImpl();
		new ComponentTest(sut) //
				.addReference("setModbus", modbusBridge()) //
				.activate(MyConfig.create() //
						.setId("pvInverter0") //
						.setReadOnly(false) //
						.setModbusId("modbus0") //
						.setModbusUnitId(1) //
						.setPhaseWiring(PhaseWiring.THREE_PHASE_FOUR_WIRE) //
						.build()) //
				.next(new TestCase("No limit requested: the nominal power releases the reported limit") //
						.output(ManagedSymmetricPvInverter.ChannelId.ACTIVE_POWER_LIMIT, 125000) //
						.output(PvInverterSungrow.ChannelId.PV_LIMIT_FAILED, false)) //
				.next(new TestCase("Requested limit is written") //
						.onExecuteControllersCallbacks(() -> sut.setActivePowerLimit(62500)) //
						.output(ManagedSymmetricPvInverter.ChannelId.ACTIVE_POWER_LIMIT, 62500)) //
				.next(new TestCase("Limit is rounded to the register resolution of 0.1 kW") //
						.onExecuteControllersCallbacks(() -> sut.setActivePowerLimit(62550)) //
						.output(ManagedSymmetricPvInverter.ChannelId.ACTIVE_POWER_LIMIT, 62600)) //
				.next(new TestCase("Limit above the nominal power is capped") //
						.onExecuteControllersCallbacks(() -> sut.setActivePowerLimit(200000)) //
						.output(ManagedSymmetricPvInverter.ChannelId.ACTIVE_POWER_LIMIT, 125000)) //
				.next(new TestCase("Negative limit is raised to zero") //
						.onExecuteControllersCallbacks(() -> sut.setActivePowerLimit(-1000)) //
						.output(ManagedSymmetricPvInverter.ChannelId.ACTIVE_POWER_LIMIT, 0)) //
				.next(new TestCase("Limit equal to the reported limit is not written again") //
						.onExecuteControllersCallbacks(() -> sut.setActivePowerLimit(40000)) //
						.output(ManagedSymmetricPvInverter.ChannelId.ACTIVE_POWER_LIMIT, null) //
						.output(PvInverterSungrow.ChannelId.PV_LIMIT_FAILED, false)) //
				.deactivate();
	}
}
