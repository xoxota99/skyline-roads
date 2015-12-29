package com.skyline.roadsys.lsystem;

import java.util.*;

import org.apache.commons.logging.*;

/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */

public class LSystem {
	protected Log logger = LogFactory.getLog(this.getClass());
	protected Set<Character> alphabet = new HashSet<Character>();
	/** < Finite set of symbols */
	protected String axiom;
	/** < Initial string */

	/**
	 * Production rules are stored in a Map. Stochastic rules have more than one
	 * successor. Which one is used is determined randomly within the rule. @see
	 * ProductionRule
	 */
	protected Map<Character, ProductionRule> rules = new HashMap<Character, ProductionRule>();

	/** < Produced string */
	protected List<Symbol> producedString = new ArrayList<Symbol>();

	public LSystem() {
		initialize();
	}

	/** < Check if character is in this LSystem's alphabet */
	private boolean isInAlphabet(char checkedCharacter) {
		return alphabet.contains(checkedCharacter);
	}

	/** < Checks the whole string */
	private boolean isInAlphabet(String checkedString) {
		for (int i = 0; i < checkedString.length(); i++) {
			if (!alphabet.contains(checkedString.charAt(i)))
				return false;
		}
		return true;
	}

	/** Sets produced string back to axiom. */
	private void reset() {
		producedString = new ArrayList<Symbol>();
		for (int i = 0; i < axiom.length(); i++) {
			producedString.add(new Symbol(axiom.charAt(i)));
		}
	}

	/** Initialize all member variables. */
	private void initialize() {
		alphabet.clear();
		axiom = "";
		rules.clear();
		producedString = new ArrayList<Symbol>();
	}

	/**
	 * Attempts to rewrite character specified by the position iterator. return
	 * the number of characters inserted.
	 */
	protected int rewrite(int position) {
		Symbol predecessor = producedString.get(position);
		String successor = "";

		if (rules.containsKey(predecessor.symbol))
		/* Not a constant symbol */
		{
			successor = rules.get(predecessor.symbol).successor();

			/* remove the rewritten character from the string */
			removeSymbol(position);

			/* Insert the successor before the character at position */
			for (int i = 0; i < successor.length(); i++) {
				producedString.add(position + i, new Symbol(successor.charAt(i)));
			}

		}
		else
		/* Constant symbol, do nothing. */
		{
		}
		return successor.length();
	}

	/**
	 * Character must be in alphabet.
	 */
	protected boolean isTerminal(char character) {
		return !rules.containsKey(character);
	}

	protected void removeSymbol(int symbolPosition) {
		producedString.remove(symbolPosition);
	}

	/** Uses debug() for printing (debugging must be ON to see something). */
	protected void debugDumpProducedStringAddresses() {
		int counter = 0;
		logger.debug("SymbolString address dump:");
		for (Symbol s : producedString) {
			logger.debug("\t" + (counter++) + ": " + s.toString());
		}
	}

	/**
	 * \WARNING: Will delete axiom and all rules
	 */
	public void setAlphabet(String alphabetCharacters) {
		initialize();
		addToAlphabet(alphabetCharacters);
	}

	/**
	 * Inserts new symbols into alphabet.
	 */
	public void addToAlphabet(String alphabetCharacters) {
		for (int i = 0; i < alphabetCharacters.length(); i++) {
			alphabet.add(alphabetCharacters.charAt(i));
		}
	}

	/**
	 * \WARNING: Will delete producedString
	 */
	public void setAxiom(String axiom) {
		if (axiom == null || axiom.equals(""))
			throw new IllegalArgumentException("Axiom cannot be null or empty.");

		if (!isInAlphabet(axiom))
			throw new IllegalArgumentException("Axiom must appear in the Alphabet for the LSystem.");

		this.axiom = axiom;
		reset();
	}

	/**
	 * Does one rewriting iteration through the productionString. Returns number
	 * of rewrites done
	 */
	public int doIteration() {

		int position = 0;
		int nextPosition;

		int rewritesMade = 0;

		while (position < producedString.size())
		{
			/*
			 * Save the pointer for the next character, because the list can
			 * change and we don't want to expand the new parts in this
			 * iteration
			 */
			nextPosition = position;
			// nextPosition++;
			// slightly different from the original, which used pointers in an
			// elegant but confusing way.
			// BUG
			if (!isTerminal(producedString.get(position).getSymbol()))
			{
				rewritesMade++;
				nextPosition += rewrite(position);
			} else {
				nextPosition++;
			}

			position = nextPosition;
		}

		return rewritesMade;
	}

	/**
	 * Does specified number of iterations
	 */
	public int doIterations(int howManyIterations) {
		int rewritesMade = 0;
		for (int iteration = 0; iteration < howManyIterations; iteration++)
		{
			rewritesMade += doIteration();
		}

		return rewritesMade;
	}

	/**
	 * Adds a new rule to the LSystem. All the symbols in the rule must be in
	 * the LSystem's alphabet.
	 */
	public void addRule(char predecessor, String successor) {
		if (!isInAlphabet(predecessor))
			throw new IllegalArgumentException("Predecessor must be in the LSystem's alphabet!");
		// if (!isInAlphabet(successor))
		// throw new
		// IllegalArgumentException("Successor must contain only characters in the LSystem's alphabet!");

		ProductionRule existingRule = rules.get(predecessor);
		if (existingRule != null) {
			/* Rule with the same left side already exists */
			existingRule.addSuccessor(successor);
		} else {
			/* Create new rule */
			rules.put(predecessor, new ProductionRule(predecessor, successor));
		}
	}

	/** < Returns the whole produced string */
	public String getProducedString() {
		StringBuffer out = new StringBuffer();
		for (Symbol s : producedString) {
			out.append(s.symbol);
		}
		return out.toString();
	}

}
