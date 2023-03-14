package org.terifan.sourcecodeeditor;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;


/**
 * This class is used to split source code into tokens.
 */
public abstract class SyntaxParser implements Serializable
{
	private final static long serialVersionUID = 1L;

	/**
	 * A style identifier which identifies the style used for selections.
	 * Only the background color of this style is used.
	 */
	public final static String SELECTION = "SELECTION";
	/**
	 * A style identifier which identifies the style used for search results.
	 */
	public final static String SEARCH_RESULT = "SEARCH_RESULT";
	/**
	 * A style identifier which identifies the style used for white space.
	 */
	public final static String WHITESPACE = "WHITESPACE";
	/**
	 * A style identifier which identifies the style used for line breaks (paragrah symbols).
	 */
	public final static String LINE_BREAK = "LINE_BREAK";
	/**
	 * A style identifier which identifies the style used for highlighted text.
	 * Only the background color of this style is used.
	 */
	public final static String HIGHLIGHT_WORD = "HIGHLIGHT_WORD";

	public final static String CARET = "CARET";
	public final static String HIGHLIGHT_CARET_ROW = "HIGHLIGHT_CARET_ROW";
	public final static String INDENT_LINE = "INDENT_LINE";

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
			return getClass().getConstructor().newInstance();
		}
		catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e)
		{
			throw new IllegalStateException(e);
		}
	}
}