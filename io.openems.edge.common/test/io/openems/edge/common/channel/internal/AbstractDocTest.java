package io.openems.edge.common.channel.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.openems.common.channel.Aggregation;
import io.openems.common.channel.Level;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;

public class AbstractDocTest {

	@Test
	public void testAggregationDefaultsToNull() {
		assertNull(Doc.of(OpenemsType.INTEGER).getAggregation());
	}

	@Test
	public void testAggregation() {
		assertEquals(Aggregation.BITWISE_OR,
				Doc.of(OpenemsType.LONG).aggregation(Aggregation.BITWISE_OR).getAggregation());
		assertEquals(Aggregation.MAX, Doc.of(Level.FAULT).aggregation(Aggregation.MAX).getAggregation());
		assertEquals(Aggregation.LATEST, Doc.of(OpenemsType.STRING).aggregation(Aggregation.LATEST).getAggregation());
	}

	@Test
	public void testAggregationNotApplicableToType() {
		assertThrows(IllegalArgumentException.class,
				() -> Doc.of(OpenemsType.DOUBLE).aggregation(Aggregation.BITWISE_OR));
		assertThrows(IllegalArgumentException.class, //
				() -> Doc.of(OpenemsType.STRING).aggregation(Aggregation.MAX));
	}

}
