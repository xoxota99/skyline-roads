package com.skyline.roadsys.lsystem;

import org.junit.*;

public class TestLSystem {

	@Test
	public void testAlgae()
	{
		LSystem lsystem = new LSystem();
		lsystem.setAlphabet("AB");
		lsystem.setAxiom("A");

		lsystem.addRule('A', "AB");
		lsystem.addRule('B', "A");

		lsystem.doIterations(1);
		Assert.assertEquals("AB", lsystem.getProducedString());

		lsystem.doIterations(1);
		Assert.assertEquals("ABA", lsystem.getProducedString());

		lsystem.doIterations(1);
		Assert.assertEquals("ABAAB", lsystem.getProducedString());

		lsystem.doIterations(1);
		Assert.assertEquals("ABAABABA", lsystem.getProducedString());

		lsystem.doIterations(1);
		Assert.assertEquals("ABAABABAABAAB", lsystem.getProducedString());

		lsystem.doIterations(1);
		Assert.assertEquals("ABAABABAABAABABAABABA", lsystem.getProducedString());

		lsystem.doIterations(1);
		Assert.assertEquals("ABAABABAABAABABAABABAABAABABAABAAB", lsystem.getProducedString());
	}

	@Test
	public void testFibonacci()
	{
		LSystem lsystem = new LSystem();
		lsystem.setAlphabet("AB");
		lsystem.setAxiom("A");

		lsystem.addRule('A', "B");
		lsystem.addRule('B', "AB");

		lsystem.doIterations(7);
		Assert.assertEquals("BABABBABABBABBABABBAB", lsystem.getProducedString());
	}

	@Test	//(timeout = 1000)
	public void testMisc()
	{
		LSystem lsystem = new LSystem();
		Assert.assertEquals("", lsystem.getProducedString());

		lsystem.addToAlphabet("AB");
		lsystem.setAxiom("A");
		Assert.assertEquals("A", lsystem.getProducedString());

		/* Is already in alphabet. Should do nothing. */
		lsystem.addToAlphabet("A");
		Assert.assertTrue("Alphabet contains duplicates!",lsystem.alphabet.size()==2);

		/* No rules set, should do nothing */
		lsystem.setAxiom("AB");
		lsystem.doIterations(10);
		Assert.assertEquals("AB", lsystem.getProducedString());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testIllegalAxiom() {
		/* Alphabet not set */
		LSystem lsystem = new LSystem();
		lsystem.setAxiom("A"); // Should throw IllegalArgumentException
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalRule() {
		LSystem lsystem = new LSystem();
		lsystem.setAlphabet("AB");
		lsystem.setAxiom("A");
		
		lsystem.addRule('Z', "XZ");
	}
}
