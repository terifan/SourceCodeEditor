package org.terifan.sourcecodeeditor;

import java.awt.Color;
import java.awt.Font;
import org.terifan.sourcecodeeditor.parsers.JavaSyntaxParser;
import org.terifan.sourcecodeeditor.parsers.SqlSyntaxParser;
import org.terifan.sourcecodeeditor.parsers.TextSyntaxParser;
import org.terifan.sourcecodeeditor.parsers.XmlSyntaxParser;


public class StyleSheet
{
	public static StyleMap installJava(String aFontFamily, int aFontSize, String aVariant)
	{
		if ("dark".equals(aVariant))
		{
			Font plain = new Font(aFontFamily, Font.PLAIN, aFontSize);
			Font bold = new Font(aFontFamily, Font.BOLD, aFontSize);
			Font italic = new Font(aFontFamily, Font.ITALIC, aFontSize);

			StyleMap styles = new StyleMap();

			Color bg = new Color(30, 30, 30);

			styles.put(JavaSyntaxParser.ANNOTATION, new Style(plain, new Color(198, 255, 109), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.BRACKETS, new Style(plain, new Color(200, 200, 200), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.CARET, new Style(plain, new Color(255, 255, 255), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.COMMENT_BLOCK, new Style(italic, new Color(120, 120, 120), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.COMMENT_LINE, new Style(italic, new Color(120, 120, 120), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.DOCUMENTATION, new Style(bold, new Color(120, 120, 120), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.HIGHLIGHT_CARET_ROW, new Style(plain, null, new Color(50, 50, 50), false, false, true, false));
			styles.put(JavaSyntaxParser.HIGHLIGHT_WORD, new Style(plain, new Color(255,255,255), new Color(70, 30, 30), false, false, true, true));
			styles.put(JavaSyntaxParser.IDENTIFIER, new Style(plain, new Color(170, 170, 170), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.INDENT_LINE, new Style(plain, new Color(70, 70, 70), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.KEYWORD, new Style(plain, new Color(204,120,50), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.LINE_BREAK, new Style(plain, new Color(70, 70, 70), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.LITERAL_CHARACTER, new Style(plain, new Color(106, 135, 89), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.LITERAL_NUMERIC, new Style(plain, new Color(255, 0, 255), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.LITERAL_STRING, new Style(plain, new Color(30,123,175), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.METHOD_DECLARATION, new Style(plain, new Color(255, 198, 109), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.METHOD_USE, new Style(plain, new Color(157,135,55), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.OBJECT_TYPE, new Style(plain, new Color(152, 118, 170), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.OPERATOR, new Style(plain, new Color(255, 255, 255), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.PRIMITIVE, new Style(plain, new Color(0, 200, 220), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.SEARCH_RESULT, new Style(plain, bg, new Color(255, 255, 128), false, false, false, true));
			styles.put(JavaSyntaxParser.SELECTION, new Style(plain, Color.WHITE, new Color(84, 86, 89), false, false, false, true));
			styles.put(JavaSyntaxParser.SYNTAX_ERROR, new Style(bold, new Color(255, 0, 0), new Color(30, 30, 30), false, false, false, true));
			styles.put(JavaSyntaxParser.WHITESPACE, new Style(plain, new Color(70, 70, 70), bg, false, false, true, false));

			return styles;
		}
		else
		{
			Font plain = new Font(aFontFamily, Font.PLAIN, aFontSize);
			Font bold = new Font(aFontFamily, Font.BOLD, aFontSize);
			Font italic = new Font(aFontFamily, Font.ITALIC, aFontSize);

			StyleMap styles = new StyleMap();

			Color bg = Color.WHITE;
			Color fg = Color.BLACK;

			styles.put(JavaSyntaxParser.ANNOTATION, new Style(plain, new Color(153, 153, 0), Color.WHITE, false, false, true, true));
			styles.put(JavaSyntaxParser.BRACKETS, new Style(plain, fg, bg, false, false, true, false));
			styles.put(JavaSyntaxParser.CARET, new Style(plain, Color.BLACK, Color.BLACK, false, false, true, false));
			styles.put(JavaSyntaxParser.COMMENT_BLOCK, new Style(italic, new Color(160, 160, 160), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.COMMENT_LINE, new Style(italic, new Color(160, 160, 160), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.DOCUMENTATION, new Style(plain, new Color(160, 160, 160), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.HIGHLIGHT_CARET_ROW, new Style(plain, null, new Color(240, 240, 240), false, false, true, false));
			styles.put(JavaSyntaxParser.HIGHLIGHT_WORD, new Style(plain, fg, new Color(225, 236, 247), false, false, true, true));
			styles.put(JavaSyntaxParser.IDENTIFIER, new Style(plain, fg, bg, false, false, true, true));
			styles.put(JavaSyntaxParser.INDENT_LINE, new Style(plain, new Color(200, 200, 200), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.KEYWORD, new Style(plain, new Color(0, 0, 220), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.LINE_BREAK, new Style(plain, new Color(0, 0, 153), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.LITERAL_CHARACTER, new Style(plain, new Color(0, 111, 0), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.LITERAL_NUMERIC, new Style(plain, new Color(200, 0, 200), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.LITERAL_STRING, new Style(plain, new Color(206, 123, 0), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.METHOD_DECLARATION, new Style(plain, new Color(155, 98, 109), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.METHOD_USE, new Style(plain, new Color(0, 153, 153), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.OBJECT_TYPE, new Style(plain, fg, bg, true, false, true, true));
			styles.put(JavaSyntaxParser.OPERATOR, new Style(plain, new Color(0, 0, 0), bg, false, false, true, false));
			styles.put(JavaSyntaxParser.PRIMITIVE, new Style(plain, new Color(0, 0, 220), bg, false, false, true, true));
			styles.put(JavaSyntaxParser.SEARCH_RESULT, new Style(plain, Color.WHITE, new Color(255, 255, 128), false, false, false, true));
			styles.put(JavaSyntaxParser.SELECTION, new Style(plain, Color.WHITE, new Color(173, 214, 255), false, false, false, true));
			styles.put(JavaSyntaxParser.SYNTAX_ERROR, new Style(bold, new Color(0, 0, 0), new Color(255, 200, 200), false, false, false, true));
			styles.put(JavaSyntaxParser.WHITESPACE, new Style(plain, new Color(170, 170, 170), bg, false, false, true, false));

			return styles;
		}
	}


	public static StyleMap installSql(String aFontFamily, int aFontSize, String aVariant)
	{
		Font plain = new Font(aFontFamily, Font.PLAIN, aFontSize);
		Font bold = new Font(aFontFamily, Font.BOLD, aFontSize);
		Font italic = new Font(aFontFamily, Font.ITALIC, aFontSize);
		Color bg = Color.WHITE;

		StyleMap styles = new StyleMap();

		styles.put(SqlSyntaxParser.BRACKETS, new Style(plain, new Color(0, 0, 0), bg, false, false, true, false));
		styles.put(SqlSyntaxParser.CARET, new Style(plain, Color.BLACK, Color.BLACK, false, false, true, false));
		styles.put(SqlSyntaxParser.COMMENT_BLOCK, new Style(italic, new Color(0, 128, 0), bg, false, false, true, false));
		styles.put(SqlSyntaxParser.COMMENT_LINE, new Style(italic, new Color(0, 128, 0), bg, false, false, true, false));
		styles.put(SqlSyntaxParser.FUNCTION, new Style(plain, new Color(255, 0, 255), bg, false, false, true, true));
		styles.put(SqlSyntaxParser.HIGHLIGHT_CARET_ROW, new Style(plain, new Color(200, 200, 200), null, false, false, true, false));
		styles.put(SqlSyntaxParser.HIGHLIGHT_WORD, new Style(plain, Color.BLACK, new Color(225, 236, 247), false, false, true, false));
		styles.put(SqlSyntaxParser.INDENT_LINE, new Style(plain, new Color(200, 200, 200), bg, false, false, true, false));
		styles.put(SqlSyntaxParser.JOIN, new Style(plain, new Color(127, 127, 127), bg, false, false, true, true));
		styles.put(SqlSyntaxParser.KEYWORD, new Style(plain, new Color(0, 0, 255), bg, false, false, true, true));
		styles.put(SqlSyntaxParser.LINE_BREAK, new Style(plain, new Color(170, 170, 170), bg, false, false, true, false));
		styles.put(SqlSyntaxParser.LITERAL_NUMERIC, new Style(plain, new Color(255, 0, 0), bg, false, false, true, true));
		styles.put(SqlSyntaxParser.LITERAL_STRING, new Style(plain, new Color(255, 0, 0), bg, false, false, true, false));
		styles.put(SqlSyntaxParser.OPERATOR, new Style(plain, new Color(0, 0, 0), bg, false, false, true, false));
		styles.put(SqlSyntaxParser.OTHER, new Style(plain, new Color(0, 0, 0), bg, false, false, true, true));
		styles.put(SqlSyntaxParser.SEARCH_RESULT, new Style(plain, Color.WHITE, new Color(255, 255, 128), false, false, false, false));
		styles.put(SqlSyntaxParser.SELECTION, new Style(plain, Color.WHITE, new Color(173, 214, 255), false, false, false, false));
		styles.put(SqlSyntaxParser.SYNTAX_ERROR, new Style(bold, new Color(0, 0, 0), new Color(255, 200, 200), false, false, false, false));
		styles.put(SqlSyntaxParser.WHITESPACE, new Style(plain, new Color(170, 170, 170), bg, false, false, true, false));

		return styles;
	}


	public static StyleMap installXml(String aFontFamily, int aFontSize, String aVariant)
	{
		StyleMap styles = new StyleMap();

		if ("dark".equals(aVariant))
		{
			Font plain = new Font(aFontFamily, Font.PLAIN, aFontSize);
			Font bold = new Font(aFontFamily, Font.BOLD, aFontSize);
			Font italic = new Font(aFontFamily, Font.ITALIC, aFontSize);
			Color bg = new Color(40, 40, 40);

			styles.put(XmlSyntaxParser.AMP, new Style(plain, new Color(255, 255, 255), bg, false, false, true, true));
			styles.put(XmlSyntaxParser.ATTRIBUTE, new Style(plain, new Color(156, 220, 255), bg, false, false, true, true));
			styles.put(XmlSyntaxParser.COMMENT_BLOCK, new Style(italic, new Color(136, 132, 111), bg, false, false, true, false));
			styles.put(XmlSyntaxParser.CARET, new Style(plain, Color.WHITE, Color.WHITE, false, false, true, false));
			styles.put(XmlSyntaxParser.ELEMENT, new Style(plain, new Color(86, 156, 214), bg, false, false, true, true));
			styles.put(XmlSyntaxParser.HIGHLIGHT_CARET_ROW, new Style(plain, null, new Color(50, 50, 50), false, false, true, false));
			styles.put(XmlSyntaxParser.HIGHLIGHT_WORD, new Style(plain, new Color(170, 170, 170), new Color(70, 70, 0), false, false, true, false));
			styles.put(XmlSyntaxParser.INDENT_LINE, new Style(plain, new Color(80, 80, 80), bg, false, false, true, false));
			styles.put(XmlSyntaxParser.LINE_BREAK, new Style(plain, new Color(255, 255, 255), bg, false, false, true, false));
			styles.put(XmlSyntaxParser.NAMESPACE, new Style(plain, new Color(180, 0, 0), bg, false, false, true, true));
			styles.put(XmlSyntaxParser.OPERATOR, new Style(plain, new Color(255, 255, 255), bg, false, false, true, false));
			styles.put(XmlSyntaxParser.SEARCH_RESULT, new Style(plain, Color.WHITE, new Color(255, 255, 128), false, false, true, false));
			styles.put(XmlSyntaxParser.SELECTION, new Style(plain, Color.WHITE, new Color(84, 86, 89), false, false, false, true));
			styles.put(XmlSyntaxParser.LITERAL_STRING, new Style(plain, new Color(255, 255, 255), bg, false, false, true, true));
			styles.put(XmlSyntaxParser.SYNTAX_ERROR, new Style(bold, new Color(255, 0, 0), bg, false, false, false, false));
			styles.put(XmlSyntaxParser.TAG, new Style(plain, new Color(86, 156, 214), bg, false, false, true, false));
			styles.put(XmlSyntaxParser.TEXT, new Style(plain, new Color(255, 255, 255), bg, false, false, true, true));
			styles.put(XmlSyntaxParser.WHITESPACE, new Style(plain, new Color(80, 80, 80), bg, false, false, true, false));
		}
		else
		{
			Font plain = new Font(aFontFamily, Font.PLAIN, aFontSize);
			Font bold = new Font(aFontFamily, Font.BOLD, aFontSize);
			Font italic = new Font(aFontFamily, Font.ITALIC, aFontSize);
			Color bg = Color.WHITE;

			styles.put(XmlSyntaxParser.AMP, new Style(plain, new Color(234, 202, 21), bg, false, false, true, true));
			styles.put(XmlSyntaxParser.ATTRIBUTE, new Style(plain, new Color(255, 0, 0), bg, false, false, true, true));
			styles.put(XmlSyntaxParser.COMMENT_BLOCK, new Style(italic, new Color(0, 160, 0), bg, false, false, true, false));
			styles.put(XmlSyntaxParser.CARET, new Style(plain, Color.BLACK, Color.BLACK, false, false, true, false));
			styles.put(XmlSyntaxParser.ELEMENT, new Style(plain, new Color(128, 0, 0), bg, false, false, true, true));
			styles.put(XmlSyntaxParser.HIGHLIGHT_CARET_ROW, new Style(plain, null, new Color(240, 240, 240), false, false, true, false));
			styles.put(XmlSyntaxParser.HIGHLIGHT_WORD, new Style(plain, Color.BLACK, new Color(225, 236, 247), false, false, true, false));
			styles.put(XmlSyntaxParser.INDENT_LINE, new Style(plain, new Color(200, 200, 200), bg, false, false, true, false));
			styles.put(XmlSyntaxParser.LINE_BREAK, new Style(plain, new Color(170, 170, 170), bg, false, false, true, false));
			styles.put(XmlSyntaxParser.NAMESPACE, new Style(plain, new Color(180, 0, 0), bg, false, false, true, true));
			styles.put(XmlSyntaxParser.OPERATOR, new Style(plain, new Color(0, 102, 0), bg, false, false, true, false));
			styles.put(XmlSyntaxParser.SEARCH_RESULT, new Style(plain, Color.WHITE, new Color(255, 255, 128), false, false, true, false));
			styles.put(XmlSyntaxParser.SELECTION, new Style(plain, Color.WHITE, new Color(173, 214, 255), false, false, false, true));
			styles.put(XmlSyntaxParser.LITERAL_STRING, new Style(plain, new Color(0, 0, 255), bg, false, false, true, true));
			styles.put(XmlSyntaxParser.SYNTAX_ERROR, new Style(bold, new Color(0, 0, 0), new Color(255, 200, 200), false, false, false, false));
			styles.put(XmlSyntaxParser.TAG, new Style(plain, new Color(0, 0, 220), bg, false, false, true, false));
			styles.put(XmlSyntaxParser.TEXT, new Style(plain, new Color(0, 0, 0), bg, false, false, true, true));
			styles.put(XmlSyntaxParser.WHITESPACE, new Style(plain, new Color(170, 170, 170), bg, false, false, true, false));
		}

		return styles;
	}


	public static StyleMap installText(String aFontFamily, int aFontSize, String aVariant)
	{
		Font plain = new Font("monospaced", Font.PLAIN, 12);
		Color bg = Color.WHITE;

		StyleMap styles = new StyleMap();

		styles.put(TextSyntaxParser.CARET, new Style(plain, Color.BLACK, Color.BLACK, false, false, true, false));
		styles.put(TextSyntaxParser.HIGHLIGHT_CARET_ROW, new Style(plain, null, new Color(240, 240, 240), false, false, true, false));
		styles.put(TextSyntaxParser.HIGHLIGHT_WORD, new Style(plain, Color.BLACK, new Color(225, 236, 247), false, false, true, false));
		styles.put(TextSyntaxParser.INDENT_LINE, new Style(plain, new Color(200, 200, 200), bg, false, false, true, false));
		styles.put(TextSyntaxParser.LINE_BREAK, new Style(plain, new Color(170, 170, 170), bg, false, false, true, false));
		styles.put(TextSyntaxParser.SEARCH_RESULT, new Style(plain, Color.WHITE, new Color(255, 255, 128), false, false, true, false));
		styles.put(TextSyntaxParser.SELECTION, new Style(plain, Color.WHITE, new Color(173, 214, 255), false, false, false, false));
		styles.put(TextSyntaxParser.TEXT, new Style(plain, Color.BLACK, bg, false, false, true, false));
		styles.put(TextSyntaxParser.WHITESPACE, new Style(plain, new Color(170, 170, 170), bg, false, false, true, false));

		return styles;
	}
}
