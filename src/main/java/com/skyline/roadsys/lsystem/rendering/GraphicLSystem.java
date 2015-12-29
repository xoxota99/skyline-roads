package com.skyline.roadsys.lsystem.rendering;

import java.util.*;

import javax.vecmath.*;

import com.skyline.roadsys.geometry.*;
import com.skyline.roadsys.lsystem.*;

/**
 * To the normal functionality of an LSystem this class adds
 * <ul>
 * <li>a drawing cursor and a cursor stack</li>
 * <li>possibility of area constraints</li>
 * <li>defines some basic symbols of the drawing alphabet
 * <ul>
 * <li>draw line of a certain length</li>
 * <li>turn the cursor a certain angle</li>
 * <li>push the cursor's position on stack</li>
 * <li>pop the cursor's position from stack</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author philippd
 * 
 */
/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */

public class GraphicLSystem extends LSystem {
	private Map<Symbol, Cursor> graphicCursors;
	/** < Stack for pushing cursors */
	private Deque<Cursor> cursorStack = new LinkedList<Cursor>();
	protected Cursor cursor = new Cursor();
	protected int currentlyInterpretedSymbol;

	public GraphicLSystem() {

		graphicCursors = new HashMap<Symbol, Cursor>();

		/*
		 * Symbols: [ - push current position ] - pop current position . - draw
		 * point _ - draw line forward
		 */
		setAlphabet("[]._");

		// FIXME set axiom and copy it to producedString
		setAxiom("."); // clears producedString.
		// producedString.add(new Symbol('.')); //add to producedString? (based
		// on original comment).
	}

	protected void pushCursor() {
		cursorStack.push(cursor);
	}

	/** < Does nothing when the stack is empty */
	protected void popCursor() {
		// FIXME throw exception
		cursor = cursorStack.pop();
	}

	public void setInitialDirection(Vector3d direction)
	{
		cursor.setDirection(direction);
	}

	protected void restoreGraphicCursor(Symbol symbol)
	{
		if (!graphicCursors.containsKey(symbol))
		{
			graphicCursors.put(symbol, new Cursor());
		}

		cursor = graphicCursors.get(symbol);
//		logger.info("Restored cursor for "+symbol.getSymbol()+", at "+cursor.getPosition().toString()+", pointing "+cursor.getDirection().toString());
	}

	protected void saveCursorPositionForSymbol(Symbol symbol)
	{
//		if (!graphicCursors.containsKey(symbol))
//		{
//			graphicCursors.put(symbol, new Cursor());
//		}

		graphicCursors.put(symbol,new Cursor(cursor));	//copy?
	}

	protected void removeSymbol(int position) {
		// uhh... Not sure this is going to work. Here we store symbols in the
		// keySet for a map. In the super class we store them in an ArrayList.
		// This is all trivial in C++ using pointers, less obvious in Java.
		Symbol sym = super.producedString.get(position);
		graphicCursors.remove(sym);

		super.removeSymbol(position);
	}

	private String printString(List<Symbol> str){
		StringBuffer sb = new StringBuffer();
		for(Symbol s : str){
			sb.append(s.getSymbol());
		}
		return sb.toString();
	}
	public char readNextSymbol()
	{
		logger.debug("readNextSymbol from '"+printString(producedString)+"'");
		if (producedString.isEmpty())
		/* Should not happen */
		{
			return 0;
		}
		Symbol currentSymbol = null;
		int pos = 0;
		for (Symbol symbol : producedString) {
			pos++;
			currentSymbol = symbol;
			if (!currentSymbol.isProcessed()) {	//skip ahead to the earliest unprocessed symbol.
				break;
			}
			restoreGraphicCursor(currentSymbol);
		}
		pos--;
		if (currentSymbol != null) {
			if (currentSymbol.isProcessed())
			/* If all symbols have been already read, generate some more. */
			{
				int rewritesMade = doIterations(1);

				if (rewritesMade > 0)
				{
					return readNextSymbol();
				}

				return '\0';
			}
			else
			{
				 logger.debug("Interpreting: " + currentSymbol.getSymbol());
				 logger.debug("  Position before: " +
				 cursor.getPosition().toString());
				 logger.debug("  Direction before: " +
				 cursor.getDirection().toString());
				currentlyInterpretedSymbol = pos;
				interpretSymbol(currentSymbol.getSymbol());
				currentSymbol.setProcessed(true);
				logger.debug("  Position after: " +
				 cursor.getPosition().toString());
				logger.debug("  Direction after: " +
				 cursor.getDirection().toString());
				saveCursorPositionForSymbol(currentSymbol);

				return currentSymbol.getSymbol();
			}
		}
		return 0;
	}

	protected void interpretSymbol(char symbol) {
		switch (symbol)
		{
			case '[':
				pushCursor();
				break;
			case ']':
				popCursor();
				break;
			default:
				/* do nothing */
				break;
		}
	}

	public void setInitialPosition(Point position) {
		cursor.setPosition(position);
	}

}
