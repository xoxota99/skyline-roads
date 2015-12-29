package com.skyline.roadsys.lsystem;

import java.util.*;

import com.skyline.roadsys.util.*;
/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */

public class ProductionRule {
	private char leftSide;
	public List<String> rightSide = new ArrayList<String>();

	public ProductionRule() {
		leftSide = 0;
		rightSide = new ArrayList<String>();
	}

	public ProductionRule(char leftSide, String rightSide) {
		this.leftSide = leftSide;
		this.rightSide.add(rightSide);
	}

	public char predecessor() {
		return leftSide;
	}

	/**
	 * @return a random String from the successor. Just, like, totally fucking
	 *         Random. Very sophisticated stuff. :-(
	 */
	public String successor() {
		return rightSide.get(MathUtil.randomInt(0,rightSide.size()-1));
	}

	public void addSuccessor(String rightSideString) {
		rightSide.add(rightSideString);
	}

}
