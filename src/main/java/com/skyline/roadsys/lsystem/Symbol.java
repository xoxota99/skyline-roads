package com.skyline.roadsys.lsystem;

/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */

public class Symbol {
	protected char symbol;
	protected boolean processed;

	public Symbol(char character) {
		symbol = character;
		processed = false;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	public boolean isProcessed() {
		return processed;
	}

	public char getSymbol() {
		return symbol;
	}

	public boolean equals(char c) {
		return symbol == c;
	}

	public boolean equals(Symbol other) {
		return symbol == other.symbol;
	}

}
