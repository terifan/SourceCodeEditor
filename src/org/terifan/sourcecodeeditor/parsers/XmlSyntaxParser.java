package org.terifan.sourcecodeeditor.parsers;

import java.util.ArrayList;
import java.util.List;
import org.terifan.sourcecodeeditor.Document;
import org.terifan.sourcecodeeditor.SyntaxParser;
import org.terifan.sourcecodeeditor.Token;


public class XmlSyntaxParser extends SyntaxParser
{
	public final static String TAG = "TAG";
	public final static String NAMESPACE = "NAMESPACE";
	public final static String COMMENT_BLOCK = "COMMENT_BLOCK";
	public final static String LITERAL_STRING = "LITERAL_STRING";
	public final static String ATTRIBUTE = "ATTRIBUTE";
	public final static String ELEMENT = "ELEMENT";
	public final static String TEXT = "TEXT";
	public final static String SYNTAX_ERROR = "SYNTAX_ERROR";
	public final static String OPERATOR = "OPERATOR";
	public final static String AMP = "AMP";

	private String mToken;
	private int mTokenOffset;
	private String mSourceLine;
	private String mTokenStyle;
	private String mCommentState;
	private boolean mInsideTag;
	private boolean mOptimizeTokens;
	private boolean mOptimizeWhitespace;
	private int mInitializedRow;


	public XmlSyntaxParser()
	{
	}


	protected void prepare(String aLine, boolean aOptimizeTokens, boolean aOptimizeWhitespace)
	{
		mSourceLine = aLine + "\u00B6";
		mTokenOffset = 0;
		mToken = null;
		mOptimizeTokens = aOptimizeTokens;
		mOptimizeWhitespace = aOptimizeWhitespace;
	}


	protected boolean iterate()
	{
		mToken = scanToken();

		if (mToken == null || mToken.length() == 0)
		{
			return false;
		}

		if (mOptimizeTokens && mToken.charAt(0) != '\t')
		{
			while (mSourceLine.charAt(mTokenOffset) == ' ')
			{
				mToken += " ";
				mTokenOffset++;
			}
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

		if (Character.isWhitespace(c))
		{
			if (mCommentState == null)
			{
				mTokenStyle = WHITESPACE;
			}

			if (c == ' ')
			{
				mTokenOffset++;
				return " ";
			}
			else
			{
				String s = "";

				if (mOptimizeWhitespace)
				{
					while (true)
					{
						char d = mSourceLine.charAt(mTokenOffset);
						if (!Character.isWhitespace(d))
						{
							break;
						}
						mTokenOffset++;
						s += d;
					}
				}
				else
				{
					mTokenOffset++;
					s = "\t";
				}

				return s;
			}
		}

		if (mCommentState == null && c == ' ')
		{
			if (mCommentState == null)
			{
				mTokenStyle = WHITESPACE;
			}
			mTokenOffset++;
			return " ";
		}
		else if (c == '\t')
		{
			if (mCommentState == null)
			{
				mTokenStyle = WHITESPACE;
			}
			mTokenOffset++;
			return "\t";
		}

		if (mCommentState != null)
		{
			return scanBlockComment();
		}

		String s;
		char d = mSourceLine.charAt(mTokenOffset+1);

		switch (c)
		{
			case '<':
				if (d == '/' || d == '?')
				{
					mTokenStyle = TAG;
					mTokenOffset+=2;
					mInsideTag = true;
					return "<" + mSourceLine.charAt(mTokenOffset-1);
				}
				else if (d == '!' && mSourceLine.charAt(mTokenOffset+2) == '-' && mSourceLine.charAt(mTokenOffset+3) == '-')
				{
					mCommentState = COMMENT_BLOCK;
					mTokenStyle = COMMENT_BLOCK;
					mTokenOffset+=4;
					mInsideTag = true;
					return "<!--";
				}
				else if (d == '!')
				{
					mTokenStyle = TAG;
					mTokenOffset+=2;
					mInsideTag = true;
					return "<" + mSourceLine.charAt(mTokenOffset-1);
				}
				else
				{
					mTokenStyle = TAG;
					mTokenOffset++;
					mInsideTag = true;
					return "<";
				}
			case '>':
				if (!mInsideTag)
				{
					mTokenStyle = SYNTAX_ERROR;
					mTokenOffset++;
					return Character.toString(c);
				}
				mTokenStyle = TAG;
				mTokenOffset++;
				mInsideTag = false;
				return ">";
			case '=':
				mTokenStyle = OPERATOR;
				mTokenOffset++;
				return "=";
			case '/':
				if (d == '>' && mInsideTag)
				{
					mTokenStyle = TAG;
					mTokenOffset+=2;
					mInsideTag = false;
					return "/>";
				}
				mTokenStyle = TEXT;
				mTokenOffset++;
				return "/";
			case '&':
				if (d == '#')
				{
					for (int i=mTokenOffset+2; i<mSourceLine.length(); i++)
					{
						if (mSourceLine.charAt(i) == ';' && i > mTokenOffset+2)
						{
							mTokenStyle = AMP;
							s = mSourceLine.substring(mTokenOffset, i+1);
							mTokenOffset=i+1;
							return s;
						}
						else if (mSourceLine.charAt(i) < '0' || mSourceLine.charAt(i) > '9')
						{
							break;
						}
					}
					mTokenStyle = SYNTAX_ERROR;
					s = mSourceLine.substring(mTokenOffset, mSourceLine.length()-1);
					mTokenOffset=mSourceLine.length()-1;
					return s;
				}
				else if ((d >= 'a' && d <= 'z') || (d >= 'A' && d <= 'Z'))
				{
					for (int i=mTokenOffset+2; i<mSourceLine.length(); i++)
					{
						if (mSourceLine.charAt(i) == ';' && i > mTokenOffset+2)
						{
							mTokenStyle = AMP;
							s = mSourceLine.substring(mTokenOffset, i+1);
							mTokenOffset=i+1;
							return s;
						}
					}
					mTokenStyle = SYNTAX_ERROR;
					s = mSourceLine.substring(mTokenOffset, mSourceLine.length()-1);
					mTokenOffset=mSourceLine.length()-1;
					return s;
				}
				mTokenStyle = SYNTAX_ERROR;
				mTokenOffset++;
				return Character.toString(c);
			case '?':
				if (d == '>')
				{
					mTokenStyle = TAG;
					mTokenOffset+=2;
					mInsideTag = false;
					return "?>";
				}
			default:
				if (mInsideTag)
				{
					if (c == '\'' || c == '\"')
					{
						return scanStringLiteral();
					}
					else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-' || c == '.' || c == '_')
					{
						return scanElement();
					}
				}
				else
				{
					if (c >= 32 && c <= 127)
					{
						return scanText();
					}
				}

				mTokenStyle = SYNTAX_ERROR;
				mTokenOffset++;
				return Character.toString(c);
		}
	}


	protected String scanBlockComment()
	{
		int nextOffset = -1;
		outer: for (int o = mTokenOffset; o < mSourceLine.length(); o++)
		{
			switch (mSourceLine.charAt(o))
			{
				case '\t':
					nextOffset = o;
					break outer;
				case '-':
					if (mSourceLine.charAt(o + 1) == '-' && mSourceLine.charAt(o + 2) == '>')
					{
						mCommentState = null;
						nextOffset = o + 3;
						break outer;
					}
					break;
			}
		}
		int o = mTokenOffset;
		if (nextOffset == -1)
		{
			mTokenOffset = mSourceLine.length() - 1;
		}
		else
		{
			mTokenOffset = nextOffset;
		}
		mInsideTag = false;
		return mSourceLine.substring(o, mTokenOffset);
	}


	protected String scanStringLiteral()
	{
		int o = mTokenOffset + 1;
		boolean foundTerminator = false;
		char terminator = mSourceLine.charAt(mTokenOffset);

		for (; o < mSourceLine.length()-1; o++)
		{
			if (mSourceLine.charAt(o) == terminator)
			{
				o++;
				foundTerminator = true;
				break;
			}
		}

		if (foundTerminator)
		{
			mTokenStyle = LITERAL_STRING;
		}
		else
		{
			mTokenStyle = SYNTAX_ERROR;
		}
		String s = mSourceLine.substring(mTokenOffset, o);
		mTokenOffset = o;
		return s;
	}


	protected String scanElement()
	{
		int len = 1;
		boolean isError = false;
		boolean isNamespace = false;
		boolean isAttribute = false;
		boolean errorPending = false;

		outer: while (mTokenOffset + len < mSourceLine.length() - 1)
		{
			switch (mSourceLine.charAt(mTokenOffset + len))
			{
				case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G':
				case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N':
				case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U':
				case 'V': case 'W': case 'X': case 'Y': case 'Z':
				case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g':
				case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n':
				case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u':
				case 'v': case 'w': case 'x': case 'y': case 'z':
				case '_': case '-': case '.':
				case '0': case '1': case '2': case '3': case '4': case '5': case '6':
				case '7': case '8': case '9':
					if (errorPending)
					{
						break outer;
					}
					len++;
					break;
				case ':':
					len++;
					if (errorPending)
					{
						break outer;
					}
					isNamespace = true;
					break outer;
				case '=':
					len++;
					isAttribute = true;
					errorPending = false;
					break outer;
				case ' ': case '\t': case '>': case '/':
					break outer;
				default:
					len++;
					isError = true;
					break outer;
			}
		}

		String s = mSourceLine.substring(mTokenOffset, mTokenOffset + len);
		mTokenOffset += len;

		if (isError || errorPending)
		{
			mTokenStyle = SYNTAX_ERROR;
		}
		else if (isNamespace)
		{
			mTokenStyle = NAMESPACE;
		}
		else if (isAttribute)
		{
			mTokenStyle = ATTRIBUTE;
		}
		else
		{
			mTokenStyle = ELEMENT;
		}

		return s;
	}


	protected String scanText()
	{
		int o = mTokenOffset;

		outer: for (; o < mSourceLine.length()-1; o++)
		{
			switch (mSourceLine.charAt(o))
			{
				case '<': case '&': case '>': case '\t':
					break outer;
			}
		}

		String s = mSourceLine.substring(mTokenOffset, o);
		mTokenOffset = o;
		mTokenStyle = TEXT;

		return s;
	}


	@Override
	public void initialize(Document aDocument, int aRow)
	{
		mInitializedRow = aRow;
		mCommentState = null;
		mInsideTag = false;
		for (int rowIndex=0; rowIndex<aRow; rowIndex++)
		{
			String s = aDocument.getLine(rowIndex) + "\u00B6";
			int length = s.length();
			if (mCommentState != null && s.contains(">"))
			{
				for (int i = 0; i < length; i++)
				{
					if (s.charAt(i) == '-' && s.charAt(i + 1) == '-' && s.charAt(i + 2) == '>')
					{
						mCommentState = null;
						mInsideTag = false;
						i+=2;
					}
					else if (s.charAt(i) == '>')
					{
						mInsideTag = false;
					}
				}
			}
			if (mCommentState == null)// && s.indexOf("<") != -1)
			{
				for (int i = 0; i < length; i++)
				{
					switch (s.charAt(i))
					{
						case '\'':
							for (i++; i < length; i++)
							{
								if (s.charAt(i) == '\'')
								{
									break;
								}
							}
							break;
						case '\"':
							for (i++; i < length; i++)
							{
								if (s.charAt(i) == '\"')
								{
									break;
								}
							}
							break;
						case '<':
							mInsideTag = true;

							if (s.charAt(i + 1) == '!' && s.charAt(i + 2) == '-' && s.charAt(i + 3) == '-')
							{
								mTokenStyle = COMMENT_BLOCK;
								mCommentState = COMMENT_BLOCK;

								i+=3;
								for (; i < length; i++)
								{
									if (s.charAt(i) == '-' && s.charAt(i + 1) == '-' && s.charAt(i + 2) == '>')
									{
										mCommentState = null;
										mInsideTag = false;
										i += 2;
										break;
									}
								}
							}

							break;
						case '>':
							mInsideTag = false;
							break;
					}
				}
			}
		}
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
			String style;
			if (mCommentState == null)
			{
				style = mTokenStyle;
			}
			else
			{
				style = mCommentState;
			}

//			if (aOptimizeTokens && prevToken != null && prevToken.getStyle().similar(mTokenStyle, aOptimizeWhitespace))
//			{
//				prevToken.append(mToken);
//			}
//			else
//			{
				prevToken = new Token(mToken, style, mTokenOffset-mToken.length(), mCommentState != null);
				tokens.add(prevToken);
//			}
		}
		return tokens;
	}
}