package org.terifan.ui.sourceeditor;

import java.util.List;


/**
 * This class is used to split source code into tokens.
 */
public abstract class SyntaxParser
{
	/**
	 * A style identifier which identifies the style used for selections.
	 * Only the backgound color of this style is used.
	 */
	public final static String SELECTION = "SELECTION";
	/**
	 * A style identifier which identifies the style used for search results.
	 */
	public final static String SEARCHRESULT = "SEARCHRESULT";
	/**
	 * A style identifier which identifies the style used for white space.
	 */
	public final static String WHITESPACE = "WHITESPACE";
	/**
	 * A style identifier which identifies the style used for line breaks (paragrah symbols).
	 */
	public final static String LINEBREAK = "LINEBREAK";
	/**
	 * A style identifier which identifies the style used for highlighted text. 
	 * Only the backgound color of this style is used.
	 */
	public final static String HIGHLIGHT = "HIGHLIGHT";

	/**
	 * Return the style matching to a style identifier.
	 */
	public abstract Style getStyle(String aIdentifier);

	/**
	 * Return an array with all style keys.
	 */
	public abstract String [] getStyleKeys();

	/**
	 * Initializes the parser and resets any internal state. This method is 
	 * called once before any calls are made to parse(). This
	 * method traces the source code to figure out the style and comment state 
	 * for the source line 'aRow'.
	 *
	 * @param aDocument
	 *    The Document being tokenized.
	 * @param aRow
	 *    First line tokenized.
	 */
	public abstract void initialize(Document aDocument, int aRow);


	public abstract List<Token> parse(Document aDocument, int aRow, boolean aOptimizeTokens, boolean aOptimizeWhitespace);


	public SyntaxParser newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch (IllegalAccessException e)
		{
			throw new IllegalStateException(e);
		}
		catch (InstantiationException e)
		{
			throw new IllegalStateException(e);
		}
	}
}