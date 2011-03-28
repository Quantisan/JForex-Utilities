package com.quantisan.JFUtil;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.Instrument;

public class RoundingTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPip() {
		assertEquals(1.37983d, Rounding.pip(Instrument.EURUSD, 1.3798312368), 0d);
	}

	@Test
	public void testLot() {
		assertEquals(2.152d, Rounding.lot(2.15234568d), 0d);
	}

}
