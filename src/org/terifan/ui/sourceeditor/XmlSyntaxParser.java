package org.terifan.ui.sourceeditor;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class XmlSyntaxParser extends SyntaxParser
{
	public final static String TAG = "TAG";
	public final static String NAMESPACE = "NAMESPACE";
	public final static String BLOCKCOMMENT = "BLOCKCOMMENT";
	public final static String STRINGLITERAL = "STRINGLITERAL";
	public final static String ATTRIBUTE = "ATTRIBUTE";
	public final static String ELEMENT = "ELEMENT";
	public final static String TEXT = "TEXT";
	public final static String SYNTAXERROR = "SYNTAXERROR";
	public final static String OPERATOR = "OPERATOR";
	public final static String AMP = "AMP";

	private static HashMap<String,Style> mStyles;
	private String mToken;
	private int mTokenOffset;
	private String mSourceLine;
	private Style mTokenStyle;
	private String mCommentState;
	private boolean mInsideTag;
	private boolean mOptimizeTokens;
	private boolean mOptimizeWhitespace;
	private int mInitializedRow;

	
	static
	{
		Font plain = new Font("monospaced", Font.PLAIN, 12);
		Font bold = new Font("monospaced", Font.BOLD, 12);
		Font italic = new Font("monospaced", Font.ITALIC, 12);
		Font bolditalic = new Font("monospaced", Font.BOLD | Font.ITALIC, 12);
		Color bg = Color.WHITE;

		mStyles = new HashMap<String,Style>();
		mStyles.put(BLOCKCOMMENT, new Style(BLOCKCOMMENT, italic, new Color(0,160,0), bg, false, false, true, false));
		mStyles.put(TAG, new Style(TAG, plain, new Color(0,0,220), bg, false, false, true, false));
		mStyles.put(NAMESPACE, new Style(NAMESPACE, plain, new Color(180,0,0), bg, false, false, true, true));
		mStyles.put(SELECTION, new Style(SELECTION, plain, Color.WHITE, new Color(176,197,227), false, false, false, true));
		mStyles.put(STRINGLITERAL, new Style(STRINGLITERAL, plain, new Color(0,0,220), bg, false, false, true, true));
		mStyles.put(TEXT, new Style(TEXT, plain, new Color(0,0,0), bg, false, false, true, true));
		mStyles.put(ELEMENT, new Style(ELEMENT, plain, new Color(180,0,0), bg, false, false, true, true));
		mStyles.put(ATTRIBUTE, new Style(ATTRIBUTE, plain, new Color(255,0,0), bg, false, false, true, true));
		mStyles.put(OPERATOR, new Style(OPERATOR, plain, new Color(0,102,0), bg, false, false, true, false));
		mStyles.put(AMP, new Style(AMP, plain, new Color(234,202,21), bg, false, false, true, true));

		mStyles.put(SEARCHRESULT, new Style(SEARCHRESULT, plain, Color.WHITE, new Color(255,255,128), false, false, true, false));
		mStyles.put(SYNTAXERROR, new Style(SYNTAXERROR, bold, new Color(0,0,0), new Color(255,200,200), false, false, false, false));
		mStyles.put(WHITESPACE, new Style(WHITESPACE, plain, Color.BLACK, bg, false, false, true, false));
		mStyles.put(HIGHLIGHT, new Style(HIGHLIGHT, plain, Color.BLACK, new Color(225,236,247), false, false, true, false));
		mStyles.put(LINEBREAK, new Style(LINEBREAK, plain, new Color(0,0,153), bg, false, false, true, false));
	}


	@Override
	public Style getStyle(String aIdentifier)
	{
		Style s = mStyles.get(aIdentifier);
		if (s == null) throw new IllegalArgumentException("Style not found: " + aIdentifier);
		return s;
	}


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
				mTokenStyle = mStyles.get(WHITESPACE);
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
				mTokenStyle = mStyles.get(WHITESPACE);
			}
			mTokenOffset++;
			return " ";
		}
		else if (c == '\t')
		{
			if (mCommentState == null)
			{
				mTokenStyle = mStyles.get(WHITESPACE);
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
					mTokenStyle = mStyles.get(TAG);
					mTokenOffset+=2;
					mInsideTag = true;
					return "<" + mSourceLine.charAt(mTokenOffset-1);
				}
				else if (d == '!' && mSourceLine.charAt(mTokenOffset+2) == '-' && mSourceLine.charAt(mTokenOffset+3) == '-')
				{
					mCommentState = BLOCKCOMMENT;
					mTokenStyle = mStyles.get(BLOCKCOMMENT);
					mTokenOffset+=4;
					mInsideTag = true;
					return "<!--";
				}
				else if (d == '!')
				{
					mTokenStyle = mStyles.get(TAG);
					mTokenOffset+=2;
					mInsideTag = true;
					return "<" + mSourceLine.charAt(mTokenOffset-1);
				}
				else
				{
					mTokenStyle = mStyles.get(TAG);
					mTokenOffset++;
					mInsideTag = true;
					return "<";
				}
			case '>':
				if (!mInsideTag)
				{
					mTokenStyle = mStyles.get(SYNTAXERROR);
					mTokenOffset++;
					return Character.toString(c);
				}
				mTokenStyle = mStyles.get(TAG);
				mTokenOffset++;
				mInsideTag = false;
				return ">";
			case '=':
				mTokenStyle = mStyles.get(OPERATOR);
				mTokenOffset++;
				return "=";
			case '/':
				if (d == '>' && mInsideTag)
				{
					mTokenStyle = mStyles.get(TAG);
					mTokenOffset+=2;
					mInsideTag = false;
					return "/>";
				}
				mTokenStyle = mStyles.get(TEXT);
				mTokenOffset++;
				return "/";
			case '&':
				if (d == '#')
				{
					for (int i=mTokenOffset+2; i<mSourceLine.length(); i++)
					{
						if (mSourceLine.charAt(i) == ';' && i > mTokenOffset+2)
						{
							mTokenStyle = mStyles.get(AMP);
							s = mSourceLine.substring(mTokenOffset, i+1);
							mTokenOffset=i+1;
							return s;
						}
						else if (mSourceLine.charAt(i) < '0' || mSourceLine.charAt(i) > '9')
						{
							break;
						}
					}
					mTokenStyle = mStyles.get(SYNTAXERROR);
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
							mTokenStyle = mStyles.get(AMP);
							s = mSourceLine.substring(mTokenOffset, i+1);
							mTokenOffset=i+1;
							return s;
						}
					}
					mTokenStyle = mStyles.get(SYNTAXERROR);
					s = mSourceLine.substring(mTokenOffset, mSourceLine.length()-1);
					mTokenOffset=mSourceLine.length()-1;
					return s;
				}
				mTokenStyle = mStyles.get(SYNTAXERROR);
				mTokenOffset++;
				return Character.toString(c);
			case '?':
				if (d == '>')
				{
					mTokenStyle = mStyles.get(TAG);
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
					if ((int)c >= 32 && (int)c <= 127)
					{
						return scanText();
					}
				}

				mTokenStyle = mStyles.get(SYNTAXERROR);
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
			mTokenStyle = mStyles.get(STRINGLITERAL);
		}
		else
		{
			mTokenStyle = mStyles.get(SYNTAXERROR);
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

		if (isError || errorPending) mTokenStyle = mStyles.get(SYNTAXERROR);
		else if (isNamespace) mTokenStyle = mStyles.get(NAMESPACE);
		else if (isAttribute) mTokenStyle = mStyles.get(ATTRIBUTE);
		else mTokenStyle = mStyles.get(ELEMENT);

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
		mTokenStyle = mStyles.get(TEXT);

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
			if (mCommentState != null && s.indexOf(">") != -1)
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
								mTokenStyle = mStyles.get(BLOCKCOMMENT);
								mCommentState = BLOCKCOMMENT;

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
		ArrayList<Token> tokens = new ArrayList<Token>();
		Token prevToken = null;
		while (iterate())
		{
			Style style;
			if (mCommentState == null)
			{
				style = mTokenStyle;
			}
			else
			{
				style = mStyles.get(mCommentState);
			}

			if (aOptimizeTokens && prevToken != null && prevToken.style.similar(mTokenStyle, aOptimizeWhitespace))
			{
				prevToken.token += mToken;
			}
			else
			{
				prevToken = new Token(mToken, style, mTokenOffset-mToken.length(), mCommentState != null);
				tokens.add(prevToken);
			}
		}
		return tokens;
	}
}