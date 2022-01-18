package org.terifan.ui.sourceeditor;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * This class is used to split plain text into tokens.
 */
@SuppressWarnings("unchecked")
public class TextSyntaxParser extends SyntaxParser
{
	/**
	 * A style identifier which identifies the style used for plain text.
	 */
	public final static String TEXT = "TEXT";

	private static HashMap<String,Style> mStyles;
	private String mToken;
	private int mTokenOffset;
	private String mSourceLine;
	private Style mTokenStyle;
	private boolean mOptimizeWhitespace;
	private int mInitializedRow;


	static
	{
		Font plain = new Font("monospaced", Font.PLAIN, 12);
		Color bg = Color.WHITE;

		mStyles = new HashMap<String,Style>();
		mStyles.put(LINEBREAK, new Style(LINEBREAK, plain, new Color(0,0,153), bg, false, false, true, false));
		mStyles.put(SEARCHRESULT, new Style(SEARCHRESULT, plain, Color.WHITE, new Color(255,255,128), false, false, true, false));
		mStyles.put(SELECTION, new Style(SELECTION, plain, Color.WHITE, new Color(176,197,227), false, false, false, false));
		mStyles.put(TEXT, new Style(TEXT, plain, Color.BLACK, bg, false, false, true, false));
		mStyles.put(WHITESPACE, new Style(WHITESPACE, plain, Color.BLACK, bg, false, false, true, false));
		mStyles.put(HIGHLIGHT, new Style(HIGHLIGHT, plain, Color.BLACK, new Color(225,236,247), false, false, true, false));
	}


	/**
	 * Return the style matching to a style identifier.
	 */
	@Override
	public Style getStyle(String aIdentifier)
	{
		Style s = (Style)mStyles.get(aIdentifier);
		if (s == null) throw new IllegalArgumentException("Style not found: " + aIdentifier);
		return s;
	}


	/**
	 * Return an array with all style keys.
	 */
	@Override
	public String [] getStyleKeys()
	{
		String [] keys = new String[mStyles.size()];
		mStyles.keySet().toArray(keys);
		return keys;
	}


	protected void prepare(String aLine, boolean aOptimizeTokens, boolean aOptimizeWhitespace)
	{
		mSourceLine = aLine + "\u00B6";
		mTokenOffset = 0;
		mToken = null;
		mOptimizeWhitespace = aOptimizeWhitespace;
	}


	/**
	 * Scans the source code for the next token. This method set the values 
	 * returned by getCommentState(), getTokenOffset(), getTokenStyle() and 
	 * getToken().
	 *
	 * @return true if a token was found. A false value indicates that no more 
	 *         tokens exists in the current source line.
	 */
	public boolean iterate()
	{
		mToken = scanToken();

		if (mToken == null || mToken.length() == 0)
		{
			return false;
		}

		return true;
	}


	protected String scanToken()
	{
		if (mTokenOffset >= mSourceLine.length() - 1)
		{
			return null;
		}

		char c = mSourceLine.charAt(mTokenOffset);

		if (c == '\t')
		{
			mTokenStyle = (Style)mStyles.get(WHITESPACE);
			mTokenOffset++;
			return "\t";
		}
		else if (c == ' ')
		{
			mTokenStyle = (Style)mStyles.get(WHITESPACE);
			mTokenOffset++;
			return " ";
		}

		int o = mTokenOffset;

		for (; o < mSourceLine.length()-1; o++)
		{
			c = mSourceLine.charAt(o);
			if (c == '\t' || (!mOptimizeWhitespace && c == ' '))
			{
				break;
			}
		}

		mTokenStyle = (Style)mStyles.get(TEXT);
		String s = mSourceLine.substring(mTokenOffset, o);
		mTokenOffset = o;

		return s;
	}


	@Override
	public void initialize(Document aDocument, int aRow)
	{
		mInitializedRow = aRow;
	}


	@Override
	public List<Token> parse(Document aDocument, int aRow, boolean aOptimizeTokens, boolean aOptimizeWhitespace)
	{
		if (mInitializedRow == -1 || aRow < mInitializedRow)
		{
			throw new IllegalStateException("Call the initialize method with a row number less than or equal to the row to parse.");
		}

		prepare(aDocument.getLine(aRow), aOptimizeTokens, aOptimizeWhitespace);
		ArrayList<Token> tokens = new ArrayList<Token>();
		Token prevToken = null;
		while (iterate())
		{
			if (aOptimizeTokens && prevToken != null && prevToken.style.similar(mTokenStyle, aOptimizeWhitespace))
			{
				prevToken.token += mToken;
			}
			else
			{
				prevToken = new Token(mToken, mTokenStyle, mTokenOffset-mToken.length(), false);
				tokens.add(prevToken);
			}
		}
		return tokens;
	}
}