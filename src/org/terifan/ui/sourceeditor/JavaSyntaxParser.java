package org.terifan.ui.sourceeditor;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/**
 * This class is used to split source code into tokens.
 */
public class JavaSyntaxParser extends SyntaxParser
{
	/**
	 * A style identifier which identifies the style used for block comments.
	 */
	public final static String BLOCKCOMMENT = "BLOCKCOMMENT";
	/**
	 * A style identifier which identifies the style used for parantheses, 
	 * brackets and curly brackets.
	 */
	public final static String BRACKETS = "BRACKETS";
	/**
	 * A style identifier which identifies the style used for character 
	 * literals such as 'a'.
	 */
	public final static String CHARACTERLITERAL = "CHARACTERLITERAL";
	/**
	 * A style identifier which identifies the style used for block 
	 * documentation comments.
	 */
	public final static String DOCUMENTATION = "DOCUMENTATION";
	/**
	 * A style identifier which identifies the style used for block 
	 * identifiers (package/class/variable names)
	 */
	public final static String IDENTIFIER = "IDENTIFIER";
	/**
	 * A style identifier which identifies the style used for Java keywords.
	 */
	public final static String KEYWORD = "KEYWORD";
	/**
	 * A style identifier which identifies the style used for method names.
	 */
	public final static String METHODNAME = "METHODNAME";
	/**
	 * A style identifier which identifies the style used for numeric literals 
	 * such as 1.23e+45d.
	 */
	public final static String NUMERICLITERAL = "NUMERICLITERAL";
	/**
	 * A style identifier which identifies the style used for object types.
	 */
	public final static String OBJECTTYPE = "OBJECTTYPE";
	/**
	 * A style identifier which identifies the style used for operators such as +/-*.
	 */
	public final static String OPERATOR = "OPERATOR";
	/**
	 * A style identifier which identifies the style used for single line comments.
	 */
	public final static String SINGLELINECOMMENT = "SINGLELINECOMMENT";
	/**
	 * A style identifier which identifies the style used for string literals.
	 */
	public final static String STRINGLITERAL = "STRINGLITERAL";
	/**
	 * A style identifier which identifies the style used for syntax errors.
	 */
	public final static String SYNTAXERROR = "SYNTAXERROR";
	/**
	 * A style identifier which identifies the style used for primitives such as int, long.
	 */
	public final static String PRIMITIVE = "PRIMITIVE";
	/**
	 * A style identifier which identifies the style used for annotations.
	 */
	public final static String ANNOTATION = "ANNOTATION";

	private static HashSet<String> mKeywords;
	private static HashSet<String> mPrimitives;
	private static HashSet<String> mObjectTypes;
	private static HashMap<String,Style> mStyles;
	private String mToken;
	private int mTokenOffset;
	private String mSourceLine;
	private Style mTokenStyle;
	private String mCommentState;
	private String mPendingMethodName;
	private int mStringLiteralState;
	private boolean mOptimizeTokens;
	private boolean mOptimizeWhitespace;
	private int mInitializedRow;


	static
	{
		mKeywords = new HashSet<String>();
		mKeywords.add("abstract");		mKeywords.add("assert");		mKeywords.add("break");
		mKeywords.add("case");			mKeywords.add("catch");			mKeywords.add("class");
		mKeywords.add("const");			mKeywords.add("continue");		mKeywords.add("default");
		mKeywords.add("do");			mKeywords.add("else");			mKeywords.add("extends");
		mKeywords.add("final");			mKeywords.add("finally");		mKeywords.add("for");
		mKeywords.add("goto");			mKeywords.add("if");			mKeywords.add("implements");
		mKeywords.add("import");		mKeywords.add("instanceof");	mKeywords.add("interface");
		mKeywords.add("native");		mKeywords.add("new");			mKeywords.add("package");
		mKeywords.add("private");		mKeywords.add("protected");		mKeywords.add("public");
		mKeywords.add("return");		mKeywords.add("static");		mKeywords.add("strictfp");
		mKeywords.add("super");			mKeywords.add("switch");		mKeywords.add("synchronized");
		mKeywords.add("this");			mKeywords.add("throw");			mKeywords.add("throws");
		mKeywords.add("transient");		mKeywords.add("try");			mKeywords.add("void");
		mKeywords.add("volatile");		mKeywords.add("while");			mKeywords.add("true");
		mKeywords.add("false");			mKeywords.add("null");

		mPrimitives = new HashSet<String>();
		mPrimitives.add("int");			mPrimitives.add("double");		mPrimitives.add("long");
		mPrimitives.add("float");		mPrimitives.add("boolean");		mPrimitives.add("short");
		mPrimitives.add("byte");		mPrimitives.add("char");

		Font plain = new Font("monospaced", Font.PLAIN, 14);
		Font bold = new Font("monospaced", Font.BOLD, 14);
		Font italic = new Font("monospaced", Font.ITALIC, 14);
		Font bolditalic = new Font("monospaced", Font.BOLD | Font.ITALIC, 14);
		Color bg = Color.WHITE;

		mStyles = new HashMap<String,Style>();
		mStyles.put(BLOCKCOMMENT, new Style(BLOCKCOMMENT, italic, new Color(160,160,160), bg, false, false, true, false));
		mStyles.put(BRACKETS, new Style(BRACKETS, plain, Color.BLACK, bg, false, false, true, false));
		mStyles.put(CHARACTERLITERAL, new Style(CHARACTERLITERAL, plain, new Color(0,111,0), bg, false, false, true, false));
		mStyles.put(DOCUMENTATION, new Style(DOCUMENTATION, plain, new Color(160,160,160), bg, false, false, true, false));
		mStyles.put(IDENTIFIER, new Style(IDENTIFIER, plain, Color.BLACK, bg, false, false, true, true));
		mStyles.put(KEYWORD, new Style(KEYWORD, plain, new Color(0,0,220), bg, false, false, true, true));
		mStyles.put(LINEBREAK, new Style(LINEBREAK, plain, new Color(0,0,153), bg, false, false, true, true));
		mStyles.put(METHODNAME, new Style(METHODNAME, plain, new Color(0,153,153), bg, false, false, true, true));
		mStyles.put(NUMERICLITERAL, new Style(NUMERICLITERAL, plain, new Color(200,0,200), bg, false, false, true, true));
		mStyles.put(OBJECTTYPE, new Style(OBJECTTYPE, plain, Color.BLACK, bg, true, false, true, true));
		mStyles.put(OPERATOR, new Style(OPERATOR, plain, new Color(0,0,0), bg, false, false, true, false));
		mStyles.put(SEARCHRESULT, new Style(SEARCHRESULT, plain, Color.WHITE, new Color(255,255,128), false, false, false, true));
		mStyles.put(SELECTION, new Style(SELECTION, plain, Color.WHITE, new Color(176,197,227), false, false, false, true));
		mStyles.put(SINGLELINECOMMENT, new Style(SINGLELINECOMMENT, italic, new Color(160,160,160), bg, false, false, true, false));
		mStyles.put(STRINGLITERAL, new Style(STRINGLITERAL, plain, new Color(206,123,0), bg, false, false, true, false));
		mStyles.put(SYNTAXERROR, new Style(SYNTAXERROR, bold, new Color(0,0,0), new Color(255,200,200), false, false, false, true));
		mStyles.put(PRIMITIVE, new Style(PRIMITIVE, plain, new Color(0,0,220), bg, false, false, true, true));
		mStyles.put(WHITESPACE, new Style(WHITESPACE, plain, Color.BLACK, bg, false, false, true, false));
		mStyles.put(HIGHLIGHT, new Style(HIGHLIGHT, plain, Color.BLACK, new Color(225,236,247), false, false, true, true));
		mStyles.put(ANNOTATION, new Style(ANNOTATION, plain, new Color(153,153,0), Color.WHITE, false, false, true, true));

		mObjectTypes = new HashSet<String>();
	}


	/**
	 * Return the style matching to a style identifier.
	 */
	@Override
	public Style getStyle(String aIdentifier)
	{
		Style s = mStyles.get(aIdentifier);
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


	protected  Style getTokenStyle()
	{
		if (mTokenStyle == null)
		{
			throw new IllegalStateException("Style is null. You must call the iterate method before calling this method.");
		}

		if (mCommentState == null) 
		{
			return mTokenStyle;
		}
		else
		{
			return mStyles.get(mCommentState);
		}
	}


	protected void prepare(String aLine, boolean aOptimizeTokens, boolean aOptimizeWhitespace)
	{
		mSourceLine = aLine + "\u00B6";
		mTokenOffset = 0;
		mToken = null;
		mPendingMethodName = null;
		mOptimizeTokens = aOptimizeTokens;
		mOptimizeWhitespace = aOptimizeWhitespace;
		mStringLiteralState = 0;
		if (SINGLELINECOMMENT.equals(mCommentState))
		{
			mCommentState = null;
		}
	}

	
	protected boolean iterate()
	{
		if (mPendingMethodName != null)
		{
			mToken = mPendingMethodName;
			mTokenStyle = mStyles.get(METHODNAME);
			mTokenOffset += mPendingMethodName.length();
			mPendingMethodName = null;
			return true;
		}

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
			if (mStringLiteralState == 2)
			{
				mTokenStyle = mStyles.get(SYNTAXERROR);
			}
			else if (mCommentState == null)
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

		if (mStringLiteralState > 0)
		{
			return scanStringLiteral();
		}

		if (SINGLELINECOMMENT.equals(mCommentState))
		{
			return scanSingleLineComment();
		}
		else if (mCommentState != null)
		{
			return scanBlockComment();
		}

		switch (c)
		{
			case '@':
				return scanAnnotation();
			case '.':
				c = mSourceLine.charAt(mTokenOffset + 1);
				if(c == '.' && mSourceLine.charAt(mTokenOffset + 2) == '.')
				{
					mTokenStyle = mStyles.get(OPERATOR);
					mTokenOffset+=3;
					return "...";
				}
				else if(c >= '0' && c <= '9')
				{
					return scanNumericLiteral();
				}
				if (Character.isWhitespace(c) || mTokenOffset + 1 == mSourceLine.length() - 1)
				{
					mTokenStyle = mStyles.get(IDENTIFIER);
					mTokenOffset++;
					return ".";
				}
				if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '$' || c == '_'))
				{
					return scanNumericLiteral();
				}
				mTokenStyle = mStyles.get(OPERATOR);
				mTokenOffset++;
				return ".";
			case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': 
			case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N': 
			case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U': 
			case 'V': case 'W': case 'X': case 'Y': case 'Z':
			case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': 
			case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n': 
			case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u': 
			case 'v': case 'w': case 'x': case 'y': case 'z':
			case '$': case '_':
				return scanIdentifier();
			case '/':
				switch (mSourceLine.charAt(mTokenOffset + 1))
				{
					case '/':
						mTokenStyle = mStyles.get(SINGLELINECOMMENT);
						mCommentState = SINGLELINECOMMENT;
						if (mOptimizeTokens)
						{
							return scanSingleLineComment();
						}
						else
						{
							mTokenOffset += 2;
							return "//";
						}
					case '*':
						if (mSourceLine.charAt(mTokenOffset + 2) == '*' && mSourceLine.charAt(mTokenOffset + 3) != '/')
						{
							mCommentState = DOCUMENTATION;
							mTokenStyle = mStyles.get(DOCUMENTATION);
							mTokenOffset += 3;
							return "/**";
						}
						else
						{
							mCommentState = BLOCKCOMMENT;
							mTokenStyle = mStyles.get(BLOCKCOMMENT);
							mTokenOffset += 2;
							return "/*";
						}
				}
			case '*':
				if (mSourceLine.charAt(mTokenOffset + 1) == '/')
				{
					mTokenStyle = mStyles.get(SYNTAXERROR);
					mTokenOffset+=2;
					return "*/";
				}
				if (mSourceLine.charAt(mTokenOffset + 1) == ';')
				{
					if (mTokenOffset > 0 && mSourceLine.charAt(mTokenOffset-1) == '.')
					{
						mTokenStyle = mStyles.get(IDENTIFIER);
						mTokenOffset++;
						return "*";
					}
				}
			case '+': case '-': case ';': case ',': case '?': case ':': case '<': 
			case '>': case '=': case '!': case '&': case '|': case '^': case '~': 
			case '%':
				return scanOperator();
			case '[': case ']': case '(': case ')': case '{': case '}': 
				return scanBrackets();
			case '\'':
				return scanCharacterLiteral();
			case '\"':
				return scanStringLiteral();
			case '0':
				if (mSourceLine.charAt(mTokenOffset + 1) == 'x' || mSourceLine.charAt(mTokenOffset + 1) == 'X')
				{
					return scanHexNumericLiteral();
				}
			case '1': case '2': case '3': case '4': case '5': case '6': case '7': 
			case '8': case '9': 
				return scanNumericLiteral();
			default:
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
				case ' ':
					if (mOptimizeTokens)
					{
						break;
					}
					nextOffset = o;
					break outer;
				case '\t':
					nextOffset = o;
					break outer;
				case '*':
					if (mSourceLine.charAt(o + 1) == '/')
					{
						mCommentState = null;
						nextOffset = o + 2;
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
		return mSourceLine.substring(o, mTokenOffset);
	}


	protected String scanSingleLineComment()
	{
		int offset = mTokenOffset;

		outer: for (; offset < mSourceLine.length()-1; offset++)
		{
			switch (mSourceLine.charAt(offset))
			{
				case ' ':
					if (mOptimizeTokens)
					{
						break;
					}
					break outer;
				case '\t':
					break outer;
			}
		}

		String s = mSourceLine.substring(mTokenOffset, offset);
		mTokenOffset = offset;
		return s;
	}


	protected String scanStringLiteral()
	{
		int o = mTokenOffset + (mStringLiteralState == 0 ? 1 : 0);
		boolean foundTerminator = false;
		boolean foundBreak = false;

		for (; o < mSourceLine.length()-1; o++)
		{
			char c = mSourceLine.charAt(o);
			if (c == ' ')
			{
				if (!mOptimizeTokens && mSourceLine.charAt(o + 1) != '\"')
				{
					foundBreak = true;
					break;
				}
			}
			else if (c == '\t')
			{
				foundBreak = true;
				break;
			}
			else if (c == '\"' && mSourceLine.charAt(o - 1) != '\\')
			{
				o++;
				foundTerminator = true;
				break;
			}
		}

		if (foundTerminator)
		{
			mTokenStyle = mStyles.get(STRINGLITERAL);
			mStringLiteralState = 0;
		}
		else if (foundBreak)
		{
			if (mStringLiteralState == 0)
			{
				int temp = o;
				mStringLiteralState = 2;

				for (; o < mSourceLine.length()-1; o++)
				{
					if (mSourceLine.charAt(o) == '\"' && mSourceLine.charAt(o - 1) != '\\')
					{
						o++;
						mStringLiteralState = 1;
						break;
					}
				}

				o = temp;
			}

			if (mStringLiteralState == 1)
			{
				mTokenStyle = mStyles.get(STRINGLITERAL);
			}
			else if (mStringLiteralState == 2)
			{
				mTokenStyle = mStyles.get(SYNTAXERROR);
			}
		}
		else
		{
			mTokenStyle = mStyles.get(SYNTAXERROR);
			mStringLiteralState = 0;
		}

		String s = mSourceLine.substring(mTokenOffset, o);
		mTokenOffset = o;
		return s;
	}


	protected String scanCharacterLiteral()
	{
		int o = mTokenOffset + 1;
		int len = 0;
		boolean foundTerminator = false;
		boolean foundEscape = false;
		boolean escapeConsumed = false;

		for (; o < mSourceLine.length()-1; o++)
		{
			if (mSourceLine.charAt(o) == '\'' && (escapeConsumed || mSourceLine.charAt(o - 1) != '\\'))
			{
				o++;
				foundTerminator = true;
				break;
			}
			if (!foundEscape && mSourceLine.charAt(o) == '\\')
			{
				len--;
				foundEscape = true;
			}
			else if (mSourceLine.charAt(o) == '\\')
			{
				escapeConsumed = true;
			}
			len++;
		}

		if (foundTerminator && len == 1)
		{
			mTokenStyle = mStyles.get(CHARACTERLITERAL);
		}
		else
		{
			mTokenStyle = mStyles.get(SYNTAXERROR);
		}
		String s = mSourceLine.substring(mTokenOffset, o);
		mTokenOffset = o;
		return s;
	}


	protected String scanIdentifier()
	{
		int len = 1;

		outer: for (;;)
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
				case '$': case '_':
				case '0': case '1': case '2': case '3': case '4': case '5': case '6': 
				case '7': case '8': case '9':
					len++;
					break;
				default:
					break outer;
			}
		}

		String s = mSourceLine.substring(mTokenOffset, mTokenOffset + len);
		mTokenOffset += len;

		if (mKeywords.contains(s))
		{
			mTokenStyle = mStyles.get(KEYWORD);
		}
		else if (mPrimitives.contains(s))
		{
			mTokenStyle = mStyles.get(PRIMITIVE);
		}
		else if (mObjectTypes.contains(s))
		{
			mTokenStyle = mStyles.get(OBJECTTYPE);
		}
		else
		{
			mTokenStyle = mStyles.get(IDENTIFIER);
		}

		return s;
	}


	protected String scanNumericLiteral()
	{
		int o = mTokenOffset;

		boolean fractionFound = false;
		boolean signFound = false;
		boolean exponentFound = false;
		boolean errorFound = false;
		boolean numberFound = false;

		outer: for (; o < mSourceLine.length(); o++)
		{
			switch (mSourceLine.charAt(o))
			{
				case '-': case '+':
					if (numberFound)
					{
						break outer;
					}
					if (signFound || !exponentFound)
					{
						o++;
						errorFound = true;
						break outer;
					}
					signFound = true;
					break;
				case '0': case '1': case '2': case '3': case '4': 
				case '5': case '6': case '7': case '8': case '9': 
					numberFound = true;
					break;
				case '.':
					if (fractionFound || exponentFound)
					{
						o++;
						errorFound = true;
						break outer;
					}
					fractionFound = true;
					break;
				case 'e':
					if (exponentFound)
					{
						o++;
						errorFound = true;
						break outer;
					}
					signFound = false;
					numberFound = false;
					exponentFound = true;
					break;
				case 'l': case 'L':
					o++;
					if (exponentFound || fractionFound)
					{
						errorFound = true;
					}
					break outer;
				case 'f': case 'F': case 'd': case 'D':
					o++;
					break outer;
				default:
					break outer;
			}
		}

		if (errorFound || !numberFound)
		{
			mTokenStyle = mStyles.get(SYNTAXERROR);
		}
		else
		{
			mTokenStyle = mStyles.get(NUMERICLITERAL);
		}

		String s = mSourceLine.substring(mTokenOffset, o);
		mTokenOffset = o;
		return s;
	}


	protected String scanHexNumericLiteral()
	{
		int len = 2;

		outer: for (;;)
		{
			switch (mSourceLine.charAt(mTokenOffset + len))
			{
				case '0': case '1': case '2': case '3': case '4': case '5': case '6': 
				case '7': case '8': case '9': case 'a': case 'b': case 'c': case 'd':
				case 'e': case 'f': case 'A': case 'B': case 'C': case 'D': case 'E':
				case 'F':
					len++;
					break;
				default:
					break outer;
			}
		}

		mTokenStyle = mStyles.get(NUMERICLITERAL);
		String s = mSourceLine.substring(mTokenOffset, mTokenOffset + len);
		mTokenOffset += len;
		return s;
	}


	protected String scanOperator()
	{
		mTokenStyle = null;

		String s = identifyOperatorAt(mTokenOffset);

		while (true)
		{
			String temp = identifyOperatorAt(mTokenOffset + s.length());

			if (temp == null) break;

			s += temp;
			mTokenStyle = mStyles.get(SYNTAXERROR);
		}

		if (mTokenStyle == null)
		{
			mTokenStyle = mStyles.get(OPERATOR);
		}

		mTokenOffset += s.length();
		return s;
	}


	protected String identifyOperatorAt(int aOffset)
	{
		char c = mSourceLine.length() - aOffset <= 1 ? '\0' : mSourceLine.charAt(aOffset+1);

		switch (mSourceLine.charAt(aOffset))
		{
			case ';':
				if (c == ';') return ";;";
				return ";";
			case ',': return ",";
			case '?': return "?";
			case ':': return ":";
			case '=':
				if (c == '=') return "==";
				return "=";
			case '!':
				if (c == '=') return "!=";
				return "!";
			case '~':
				if (c == '=') return "~=";
				return "~";
			case '*':
				if (c == '=') return "*=";
				return "*";
			case '/':
				if (c == '/') return null;
				if (c == '*') return null;
				if (c == '=') return "/=";
				return "/";
			case '^':
				if (c == '=') return "^=";
				return "^";
			case '%':
				if (c == '=') return "%=";
				return "%";
			case '&':
				if (c == '=') return "&=";
				if (c == '&') return "&&";
				return "&";
			case '|':
				if (c == '=') return "|=";
				if (c == '|') return "||";
				return "|";
			case '+':
				if (c == '=') return "+=";
				if (c == '+')
				{
					if (mSourceLine.charAt(aOffset+2) == ';') return "++;";
					return "++";
				}
				if (c == '-') return "+-";
				return "+";
			case '-':
				if (c == '=') return "-=";
				if (c == '-')
				{
					if (mSourceLine.charAt(aOffset+2) == ';') return "--;";
					return "--";
				}
				if (c == '+') return "-+";
				return "-";
			case '>':
				if (c == '=') return ">=";
				if (c == '>')
				{
					c = mSourceLine.charAt(aOffset+2);
					if (c == '>')
					{
						if (mSourceLine.charAt(aOffset+3) == '=') return ">>>=";
						return ">>>";
					}
					if (c == '=') return ">>=";
					return ">>";
				}
				return ">";
			case '<':
				if (c == '?') return "<?";
				if (c == '=') return "<=";
				if (c == '<')
				{
					if (mSourceLine.charAt(aOffset+2) == '=') return "<<=";
					return "<<";
				}
				return "<";
		}

		return null;
	}


	protected String scanBrackets()
	{
		int o = mTokenOffset;
	
		outer: while (true)
		{
			switch (mSourceLine.charAt(o))
			{
				case ' ':
					if (!mOptimizeTokens)
					{
						break outer;
					}
				case '[': case ']': case '(': case ')': case '{': case '}':  
					o++;
					break;
				default:
					break outer;
			}
		}

		mTokenStyle = mStyles.get(BRACKETS);
		String s = mSourceLine.substring(mTokenOffset, o);
		mTokenOffset = o;

		return s;
	}


	@Override
	public void initialize(Document aDocument, int aRow)
	{
		mInitializedRow = aRow;
		mCommentState = null;
		boolean finished = false;

		for (int rowIndex = aRow; --rowIndex >= 0;)
		{
			String s = aDocument.getLine(rowIndex) + " ";

			if (s.indexOf("/*") != -1 || s.indexOf("*/") != -1)
			{
				boolean singleLineComment = false;
				for (int i = 0, len = s.length(); i < len; i++)
				{
					char c = s.charAt(i);
					if (mCommentState == null && c == '/' && s.charAt(i + 1) == '/')
					{
						singleLineComment = true;
					}
					if (!singleLineComment && c == '/' && s.charAt(i + 1) == '*')
					{
						finished = true;
						if (s.charAt(i + 2) == '*')
						{
							mTokenStyle = mStyles.get(DOCUMENTATION);
							mCommentState = DOCUMENTATION;
						}
						else
						{
							mTokenStyle = mStyles.get(BLOCKCOMMENT);
							mCommentState = BLOCKCOMMENT;
						}
						i++;
					}
					if (c == '*' && s.charAt(i + 1) == '/')
					{
						finished = true;
						mCommentState = null;
						i++;
					}
					if (c == '\"')
					{
						i++;
						for (; i < len; i++)
						{
							if (s.charAt(i) == '\"' && s.charAt(i - 1) == '\"') 
							{
								i++;
								break;
							}
						}
						continue;
					}
				}

				if (finished)
				{
					break;
				}
			}
		}
	}


	protected String scanAnnotation()
	{
		mToken = scanIdentifier();
		mTokenStyle = mStyles.get(ANNOTATION);
		return mToken;
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