package org.terifan.sourcecodeeditor.parsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.terifan.sourcecodeeditor.Document;
import org.terifan.sourcecodeeditor.SyntaxParser;
import org.terifan.sourcecodeeditor.Token;


/**
 * This class is used to split source code into tokens.
 */
public class JavaSyntaxParser extends SyntaxParser
{
	/**
	 * A style identifier which identifies the style used for block comments.
	 */
	public final static String COMMENT_BLOCK = "COMMENT_BLOCK";
	/**
	 * A style identifier which identifies the style used for parentheses,
	 * brackets and curly brackets.
	 */
	public final static String BRACKETS = "BRACKETS";
	/**
	 * A style identifier which identifies the style used for character
	 * literals such as 'a'.
	 */
	public final static String LITERAL_CHARACTER = "LITERAL_CHARACTER";
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
	public final static String METHOD_USE = "METHOD_USE";
	/**
	 * A style identifier which identifies the style used for numeric literals
	 * such as 1.23e+45d.
	 */
	public final static String LITERAL_NUMERIC = "LITERAL_NUMERIC";
	/**
	 * A style identifier which identifies the style used for object types.
	 */
	public final static String OBJECT_TYPE = "OBJECT_TYPE";
	/**
	 * A style identifier which identifies the style used for operators such as +/-*.
	 */
	public final static String OPERATOR = "OPERATOR";
	/**
	 * A style identifier which identifies the style used for single line comments.
	 */
	public final static String COMMENT_LINE = "COMMENT_LINE";
	/**
	 * A style identifier which identifies the style used for string literals.
	 */
	public final static String LITERAL_STRING = "LITERAL_STRING";
	/**
	 * A style identifier which identifies the style used for syntax errors.
	 */
	public final static String SYNTAX_ERROR = "SYNTAX_ERROR";
	/**
	 * A style identifier which identifies the style used for primitives such as int, long.
	 */
	public final static String PRIMITIVE = "PRIMITIVE";
	/**
	 * A style identifier which identifies the style used for annotations.
	 */
	public final static String ANNOTATION = "ANNOTATION";
	public final static String METHOD_DECLARATION = "METHOD_DECLARATION";

	private final static HashSet<String> mKeywords;
	private final static HashSet<String> mPrimitives;
	private final HashSet<String> mObjectTypes;
	private String mToken;
	private int mTokenOffset;
	private String mSourceLine;
	private String mTokenStyle;
	private String mCommentState;
	private String mPendingMethodName;
	private int mStringLiteralState;
	private boolean mOptimizeTokens;
	private boolean mOptimizeWhitespace;
	private int mInitializedRow;

	static
	{
		mKeywords = new HashSet<>(Arrays.asList("abstract", "assert", "break", "case", "catch", "class", "const", "continue", "default", "do", "else", "extends", "final", "finally", "for", "goto", "if", "implements", "import", "instanceof", "interface", "native", "new", "package", "private", "protected", "public", "return", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null"));
		mPrimitives = new HashSet<>(Arrays.asList("boolean", "byte", "short", "char", "int", "long", "float", "double"));
	}


	public JavaSyntaxParser()
	{
		mObjectTypes = new HashSet<>();
	}


	protected String getTokenStyle()
	{
		if (mTokenStyle == null)
		{
			throw new IllegalStateException("Style is null. You must call the iterate method before calling this method.");
		}

		if (mCommentState == null)
		{
			return mTokenStyle;
		}

		return mCommentState;
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
		if (COMMENT_LINE.equals(mCommentState))
		{
			mCommentState = null;
		}
	}


	protected boolean iterate()
	{
		if (mPendingMethodName != null)
		{
			mToken = mPendingMethodName;
			mTokenStyle = METHOD_USE;
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
				mTokenStyle = SYNTAX_ERROR;
			}
			else if (mCommentState == null)
			{
				mTokenStyle = WHITESPACE;
			}

			if (c == ' ')
			{
				mTokenOffset++;
				return " ";
			}

			String s = "";
			if (mOptimizeWhitespace)
			{
				for (;;)
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

		if (mStringLiteralState > 0)
		{
			return scanStringLiteral();
		}

		if (COMMENT_LINE.equals(mCommentState))
		{
			return scanSingleLineComment();
		}
		if (mCommentState != null)
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
					mTokenStyle = OPERATOR;
					mTokenOffset+=3;
					return "...";
				}
				else if(c >= '0' && c <= '9')
				{
					return scanNumericLiteral();
				}
				if (Character.isWhitespace(c) || mTokenOffset + 1 == mSourceLine.length() - 1)
				{
					mTokenStyle = IDENTIFIER;
					mTokenOffset++;
					return ".";
				}
				if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '$' || c == '_'))
				{
					return scanNumericLiteral();
				}
				mTokenStyle = OPERATOR;
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
						mTokenStyle = COMMENT_LINE;
						mCommentState = COMMENT_LINE;
						if (mOptimizeTokens)
						{
							return scanSingleLineComment();
						}
						mTokenOffset += 2;
						return "//";
					case '*':
						if (mSourceLine.charAt(mTokenOffset + 2) == '*' && mSourceLine.charAt(mTokenOffset + 3) != '/')
						{
							mCommentState = DOCUMENTATION;
							mTokenStyle = DOCUMENTATION;
							mTokenOffset += 3;
							return "/**";
						}
						else
						{
							mCommentState = COMMENT_BLOCK;
							mTokenStyle = COMMENT_BLOCK;
							mTokenOffset += 2;
							return "/*";
						}
				}
			case '*':
				if (mSourceLine.charAt(mTokenOffset + 1) == '/')
				{
					mTokenStyle = SYNTAX_ERROR;
					mTokenOffset+=2;
					return "*/";
				}
				if (mSourceLine.charAt(mTokenOffset + 1) == ';')
				{
					if (mTokenOffset > 0 && mSourceLine.charAt(mTokenOffset-1) == '.')
					{
						mTokenStyle = IDENTIFIER;
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
				return scanNumericLiteral();
			case '1': case '2': case '3': case '4': case '5': case '6': case '7':
			case '8': case '9':
				return scanNumericLiteral();
			default:
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
			mTokenStyle = LITERAL_STRING;
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
				mTokenStyle = LITERAL_STRING;
			}
			else if (mStringLiteralState == 2)
			{
				mTokenStyle = SYNTAX_ERROR;
			}
		}
		else
		{
			mTokenStyle = SYNTAX_ERROR;
			mStringLiteralState = 0;
		}

		String s = mSourceLine.substring(mTokenOffset, o);
		mTokenOffset = o;
		return s;
	}


	protected String scanCharacterLiteral()
	{
		int len;
		String t = mSourceLine.substring(mTokenOffset + 1);
		boolean b = t.startsWith("\\");
		if (!b && t.matches(".{1}\\'.*"))
		{
			len = 3;
			mTokenStyle = LITERAL_CHARACTER;
		}
		else if (b && t.matches("\\\\u[0-9]{4}\\'.*"))
		{
			len = 8;
			mTokenStyle = LITERAL_CHARACTER;
		}
		else if (b && t.matches("\\\\[0-9]{3}\\'.*"))
		{
			len = 6;
			mTokenStyle = LITERAL_CHARACTER;
		}
		else
		{
			len = t.contains("\'") ? t.indexOf("\'") + 1 : 1;
			mTokenStyle = SYNTAX_ERROR;
		}

		String s = mSourceLine.substring(mTokenOffset, mTokenOffset + len);
		mTokenOffset += len;
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

		boolean letterPrev = false;
		for (int i = mTokenOffset; --i >= 0;)
		{
			char c = mSourceLine.charAt(i);
			if (!Character.isWhitespace(c))
			{
				letterPrev = c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c == '$';
				break;
			}
		}

		boolean paransNext = false;
		for (int i = mTokenOffset + len; i < mSourceLine.length(); i++)
		{
			char c = mSourceLine.charAt(i);
			if (!Character.isWhitespace(c))
			{
				paransNext = c == '(';
				break;
			}
		}

		String s = mSourceLine.substring(mTokenOffset, mTokenOffset + len);
		mTokenOffset += len;

		if (mKeywords.contains(s))
		{
			mTokenStyle = KEYWORD;
		}
		else if (mPrimitives.contains(s))
		{
			mTokenStyle = PRIMITIVE;
		}
		else if (letterPrev && paransNext)
		{
			mTokenStyle = METHOD_DECLARATION;
		}
		else if (!letterPrev && paransNext)
		{
			mTokenStyle = METHOD_USE;
		}
		else if (mObjectTypes.contains(s))
		{
			mTokenStyle = OBJECT_TYPE;
		}
		else
		{
			mTokenStyle = IDENTIFIER;
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
			mTokenStyle = SYNTAX_ERROR;
		}
		else
		{
			mTokenStyle = LITERAL_NUMERIC;
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

		mTokenStyle = LITERAL_NUMERIC;
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

			if (temp == null)
			{
				break;
			}

			s += temp;
			mTokenStyle = SYNTAX_ERROR;
		}

		if (mTokenStyle == null)
		{
			mTokenStyle = OPERATOR;
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
				if (c == '>') return "->";
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
				if (c == '>') return "<>";
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

		mTokenStyle = BRACKETS;
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

			if (s.contains("/*") || s.contains("*/"))
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
							mTokenStyle = DOCUMENTATION;
							mCommentState = DOCUMENTATION;
						}
						else
						{
							mTokenStyle = COMMENT_BLOCK;
							mCommentState = COMMENT_BLOCK;
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
		mTokenStyle = ANNOTATION;
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

			prevToken = new Token(mToken, style, mTokenOffset-mToken.length(), mCommentState != null);
			tokens.add(prevToken);
		}
		return tokens;
	}
}