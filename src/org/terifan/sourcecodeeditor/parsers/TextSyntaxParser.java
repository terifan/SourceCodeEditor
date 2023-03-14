package org.terifan.sourcecodeeditor.parsers;

import java.util.ArrayList;
import java.util.List;
import org.terifan.sourcecodeeditor.Document;
import org.terifan.sourcecodeeditor.SyntaxParser;
import org.terifan.sourcecodeeditor.Token;


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

	private String mToken;
	private int mTokenOffset;
	private String mSourceLine;
	private String mTokenStyle;
	private boolean mOptimizeWhitespace;
	private int mInitializedRow;


	public TextSyntaxParser()
	{
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
			mTokenStyle = WHITESPACE;
			mTokenOffset++;
			return "\t";
		}
		else if (c == ' ')
		{
			mTokenStyle = WHITESPACE;
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

		mTokenStyle = TEXT;
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
		ArrayList<Token> tokens = new ArrayList<>();
		Token prevToken = null;
		while (iterate())
		{
//			if (aOptimizeTokens && prevToken != null && prevToken.getStyle().similar(mTokenStyle, aOptimizeWhitespace))
//			{
//				prevToken.append(mToken);
//			}
//			else
//			{
				prevToken = new Token(mToken, mTokenStyle, mTokenOffset-mToken.length(), false);
				tokens.add(prevToken);
//			}
		}
		return tokens;
	}
}