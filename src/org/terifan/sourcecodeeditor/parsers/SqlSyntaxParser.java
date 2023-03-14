package org.terifan.sourcecodeeditor.parsers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.terifan.sourcecodeeditor.Document;
import org.terifan.sourcecodeeditor.SyntaxParser;
import org.terifan.sourcecodeeditor.Token;


public class SqlSyntaxParser extends SyntaxParser
{
	public final static String COMMENT_BLOCK = "COMMENT_BLOCK";
	public final static String BRACKETS = "BRACKETS";
	public final static String FUNCTION = "FUNCTION";
	public final static String OTHER = "OTHER";
	public final static String KEYWORD = "KEYWORD";
	public final static String LITERAL_NUMERIC = "LITERAL_NUMERIC";
	public final static String JOIN = "OBJECTTYPE";
	public final static String OPERATOR = "OPERATOR";
	public final static String COMMENT_LINE = "COMMENT_LINE";
	public final static String LITERAL_STRING = "LITERAL_STRING";
	public final static String SYNTAX_ERROR = "SYNTAX_ERROR";

	private final static HashSet<String> mKeywords;
	private final static HashSet<String> mFunctions;
	private final static HashSet<String> mJoins;
	private String mToken;
	private int mTokenOffset;
	private String mSourceLine;
	private String mTokenStyle;
	private String mCommentState;
	private int mStringLiteralState;
	private boolean mOptimizeTokens;
	private boolean mOptimizeWhitespace;
	private int mInitializedRow;


	static
	{
		mKeywords = new HashSet<>();
		mKeywords.add("with");
		mKeywords.add("select");
		mKeywords.add("delete");
		mKeywords.add("insert");
		mKeywords.add("to");
		mKeywords.add("from");
		mKeywords.add("where");
		mKeywords.add("having");
		mKeywords.add("order");
		mKeywords.add("by");
		mKeywords.add("group");
		mKeywords.add("sysdate");
		mKeywords.add("when");
		mKeywords.add("case");
		mKeywords.add("then");
		mKeywords.add("else");
		mKeywords.add("end");
		mKeywords.add("on");
		mKeywords.add("as");
		mKeywords.add("distinct");
		mKeywords.add("in");
		mKeywords.add("desc");
		mKeywords.add("not");
		mKeywords.add("last");

		mFunctions = new HashSet<>();
		mFunctions.add("to_date");
		mFunctions.add("to_char");
		mFunctions.add("count");
		mFunctions.add("min");
		mFunctions.add("max");
		mFunctions.add("sum");
		mFunctions.add("convert");
		mFunctions.add("substring");

		mJoins = new HashSet<>();
		mJoins.add("and");
		mJoins.add("or");
		mJoins.add("like");
		mJoins.add("inner");
		mJoins.add("join");
		mJoins.add("outer");
		mJoins.add("right");
		mJoins.add("left");
	}


	public SqlSyntaxParser()
	{
	}


	protected void prepare(String aLine, boolean aOptimizeTokens, boolean aOptimizeWhitespace)
	{
		mSourceLine = aLine + "\u00B6";
		mTokenOffset = 0;
		mToken = null;
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

		if (c == ' ' || c == '\t')
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
			else
			{
				String s = "";

				if (mOptimizeWhitespace)
				{
					while (true)
					{
						char d = mSourceLine.charAt(mTokenOffset);
						if (d != '\t' && d != ' ')
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

		if (COMMENT_LINE.equals(mCommentState))
		{
			return scanSingleLineComment();
		}
		else if (mCommentState != null)
		{
			return scanBlockComment();
		}

		switch (c)
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
				return scanIdentifier();
			case '/':
				if (mSourceLine.charAt(mTokenOffset + 1) == '*')
				{
					mCommentState = COMMENT_BLOCK;
					mTokenStyle = COMMENT_BLOCK;
					mTokenOffset += 2;
					return "/*";
				}
				return scanOperator();
			case '-':
				if (mSourceLine.charAt(mTokenOffset + 1) == '-')
				{
					mTokenStyle = COMMENT_LINE;
					mCommentState = COMMENT_LINE;
					if (mOptimizeTokens)
					{
						return scanSingleLineComment();
					}
					else
					{
						mTokenOffset += 2;
						return "--";
					}
				}
				return scanOperator();
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
						mTokenStyle = OTHER;
						mTokenOffset++;
						return "*";
					}
				}
				return scanOperator();
			case '+': case ';': case ',': case '?': case ':': case '<':
			case '>': case '=': case '!': case '&': case '|': case '^': case '~':
			case '%':
				return scanOperator();
			case '[': case ']': case '(': case ')': case '{': case '}':
				return scanBrackets();
			case '\"': case '\'':
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
		char symbol = mSourceLine.charAt(mTokenOffset);

		for (; o < mSourceLine.length()-1; o++)
		{
			char c = mSourceLine.charAt(o);
			if (c == ' ')
			{
				if (!mOptimizeTokens && mSourceLine.charAt(o + 1) != symbol)
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
			else if (c == symbol && mSourceLine.charAt(o - 1) != '\\')
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
					if (mSourceLine.charAt(o) == symbol && mSourceLine.charAt(o - 1) != '\\')
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


	protected String scanIdentifier()
	{
		int len = 1;
		boolean isError = false;

		outer: for (;;)
		{
			switch (mSourceLine.charAt(mTokenOffset + len))
			{
				case '.':
					if (!mOptimizeTokens)
					{
						len++;
						break outer;
					}
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
				case '*':
					if (mSourceLine.charAt(mTokenOffset + len - 1) == '.')
					{
						len++;
					}
					break outer;
				default:
					break outer;
			}
		}

		String s = mSourceLine.substring(mTokenOffset, mTokenOffset + len);
		String t = s.toLowerCase();
		mTokenOffset += len;

		if (isError)
		{
			mTokenStyle = SYNTAX_ERROR;
		}
		else if (mKeywords.contains(t))
		{
			mTokenStyle = KEYWORD;
		}
		else if (mFunctions.contains(t))
		{
			mTokenStyle = FUNCTION;
		}
		else if (mJoins.contains(t))
		{
			mTokenStyle = JOIN;
		}
		else
		{
//			boolean isMethod = false;
//			boolean isNotMethod = false;
//
//			outer: for (int row = mSourceLineIndex; row < mSourceEditor.getDocument().getLineCount() && !isNotMethod; row++)
//			{
//				String source = mSourceEditor.getDocument().getLine(row);
//				for (int o = row == mSourceLineIndex ? mTokenOffset : 0; o < source.length(); o++)
//				{
//					switch (source.charAt(o))
//					{
//						case ' ':
//						case '\t':
//							break;
//						case '(':
//							isMethod = true;
//							break outer;
//						default:
//							isNotMethod = true;
//							break outer;
//					}
//				}
//			}

			mTokenStyle = OTHER;
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
						mTokenStyle = COMMENT_BLOCK;
						mCommentState = COMMENT_BLOCK;
						i++;
					}
					if (c == '*' && s.charAt(i + 1) == '/')
					{
						finished = true;
						mCommentState = null;
						i++;
					}
					if (c == '\"' || c == '\'')
					{
						i++;
						for (; i < len; i++)
						{
							if ((s.charAt(i) == '\"' && s.charAt(i - 1) == '\"') || (s.charAt(i) == '\'' && s.charAt(i - 1) == '\''))
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
