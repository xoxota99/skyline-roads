package com.skyline.roadsys.lsystem.rendering;

import org.junit.*;

public class TestGraphicLSystem {

	@Test
	public void testAccess()
	{
		GraphicLSystem gls = new GraphicLSystem();

		gls.setAxiom(".");
		gls.addRule('.', "-.");

		Assert.assertEquals(gls.getProducedString(), ".");

		// No iterations done yet
		Assert.assertEquals('.', gls.readNextSymbol());

		// First iteration
		Assert.assertEquals('-', gls.readNextSymbol());
		Assert.assertEquals('.', gls.readNextSymbol());

		// Second iteration
		Assert.assertEquals('-', gls.readNextSymbol());
		Assert.assertEquals('.', gls.readNextSymbol());

		// Third iteration
		Assert.assertEquals('-', gls.readNextSymbol());
		Assert.assertEquals('.', gls.readNextSymbol());

	}

	@Test
	public void testStack()
	{
		GraphicLSystem gls = new GraphicLSystem();

		gls.setAxiom(".");
		gls.addRule('.', "[[-].]");

		Assert.assertEquals(gls.getProducedString(), ".");

		// No iterations done yet
		Assert.assertEquals('.', gls.readNextSymbol());

		// First
		Assert.assertEquals('[', gls.readNextSymbol());
		Assert.assertEquals('[', gls.readNextSymbol());
		Assert.assertEquals('-', gls.readNextSymbol());
		Assert.assertEquals(']', gls.readNextSymbol());
		Assert.assertEquals('.', gls.readNextSymbol());
		Assert.assertEquals(']', gls.readNextSymbol());

		// Second
		Assert.assertEquals('[', gls.readNextSymbol());
		Assert.assertEquals('[', gls.readNextSymbol());
		Assert.assertEquals('-', gls.readNextSymbol());
		Assert.assertEquals(']', gls.readNextSymbol());
		Assert.assertEquals('.', gls.readNextSymbol());
		Assert.assertEquals(']', gls.readNextSymbol());
	}
}
