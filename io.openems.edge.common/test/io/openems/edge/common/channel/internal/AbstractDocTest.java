package io.openems.edge.common.channel.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.openems.common.channel.Aggregation;
import io.openems.common.channel.Level;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;

public class AbstractDocTest {

	@Test
	public void testAggregationDefaults() {
		assertEquals(Aggregation.MEAN, Doc.of(OpenemsType.INTEGER).getAggregation());
		assertEquals(Aggregation.MEAN, Doc.of(Level.FAULT).getAggregation());
		assertEquals(Aggregation.FIRST, Doc.of(OpenemsType.STRING).getAggregation());
		assertEquals(Aggregation.MOST_FREQUENT, Doc.of(Level.values()).getAggregation());
		assertEquals(Aggregation.MAX,
				Doc.of(OpenemsType.LONG).unit(Unit.CUMULATED_WATT_HOURS).getAggregation());
	}

	@Test
	public void testAggregation() {
		assertEquals(Aggregation.BITWISE_OR,
				Doc.of(OpenemsType.LONG).aggregation(Aggregation.BITWISE_OR).getAggregation());
		assertEquals(Aggregation.MAX, Doc.of(Level.FAULT).aggregation(Aggregation.MAX).getAggregation());
		assertEquals(Aggregation.LATEST, Doc.of(OpenemsType.STRING).aggregation(Aggregation.LATEST).getAggregation());
		assertEquals(Aggregation.LATEST, Doc.of(OpenemsType.LONG).aggregation(Aggregation.LATEST)
				.unit(Unit.CUMULATED_WATT_HOURS).getAggregation());
	}

	@Test
	public void testAggregationNotApplicableToType() {
		assertThrows(IllegalArgumentException.class,
				() -> Doc.of(OpenemsType.DOUBLE).aggregation(Aggregation.BITWISE_OR));
		assertThrows(IllegalArgumentException.class, //
				() -> Doc.of(OpenemsType.STRING).aggregation(Aggregation.MEAN));
	}

}
