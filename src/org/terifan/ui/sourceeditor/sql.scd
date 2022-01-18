<?xml version="1.0"?>

<syntax-coloring-definition>
	<scd:syntax-parser class="org.terifan.apps.sourceeditor.SQLSyntaxParser"/>
	<scd:presentation-name label="SQL"/>
	<scd:file-association extension="sql" name="SQL-file"/>

	<sp:keyword name="with"/>
	<sp:keyword name="select"/>
	<sp:keyword name="delete"/>
	<sp:keyword name="insert"/>
	<sp:keyword name="to"/>
	<sp:keyword name="from"/>
	<sp:keyword name="where"/>
	<sp:keyword name="having"/>
	<sp:keyword name="order"/>
	<sp:keyword name="by"/>
	<sp:keyword name="group"/>
	<sp:keyword name="sysdate"/>
	<sp:keyword name="when"/>
	<sp:keyword name="case"/>
	<sp:keyword name="then"/>
	<sp:keyword name="else"/>
	<sp:keyword name="end"/>
	<sp:keyword name="on"/>
	<sp:keyword name="as"/>
	<sp:keyword name="distinct"/>
	<sp:keyword name="in"/>
	<sp:keyword name="desc"/>
	<sp:keyword name="not"/>
	<sp:keyword name="last"/>

	<sp:function name="to_date"/>
	<sp:function name="to_char"/>
	<sp:function name="count"/>
	<sp:function name="min"/>
	<sp:function name="max"/>
	<sp:function name="sum"/>
	<sp:function name="convert"/>
	<sp:function name="substring"/>

	<sp:join name="and"/>
	<sp:join name="or"/>
	<sp:join name="like"/>
	<sp:join name="inner"/>
	<sp:join name="join"/>
	<sp:join name="outer"/>
	<sp:join name="right"/>
	<sp:join name="left"/>

	<scd:style id="BLOCKCOMMENT" name="Block Comment" face="italic" foreground="255,0,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="BRACKETS" name="Bracket" face="plain" foreground="0,200,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="CHARACTERLITERAL" name="Character Literal" face="plain" foreground="0,111,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="DOCCOMMENT" name="Documentation Comment" face="bolditalic" foreground="255,0,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="HIGHLIGHT" name="Highlighted Text" face="plain" foreground="0,0,0" background="225,236,247" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="IDENTIFIER" name="Identifier" face="plain" foreground="0,0,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="KEYWORD" name="Keyword" face="bold" foreground="0,0,153" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="LINEBREAK" name="Line Break" face="plain" foreground="0,0,153" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="METHODNAME" name="Method Name" face="bold" foreground="0,0,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="NUMERICLITERAL" name="Numeric Literal" face="plain" foreground="255,0,255" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="OBJECTTYPE" name="Object Type" face="plain" foreground="0,0,0" background="255,255,255" underlined="true" strikethrough="false" optional-background="true"/>
	<scd:style id="OPERATOR" name="Operator" face="plain" foreground="0,102,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="SEARCHRESULT" name="Search Result" face="plain" foreground="255,255,255" background="255,255,128" underlined="false" strikethrough="false" optional-background="false"/>
	<scd:style id="SELECTION" name="Selection" face="plain" foreground="255,255,255" background="0,0,153" underlined="false" strikethrough="false" optional-background="false"/>
	<scd:style id="SINGLELINECOMMENT" name="Single-line Comment" face="italic" foreground="255,0,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="STRINGLITERAL" name="String Literal" face="plain" foreground="204,0,204" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="SYNTAXERROR" name="Syntax Error" face="bold" foreground="255,255,0" background="255,0,0" underlined="false" strikethrough="false" optional-background="false"/>
	<scd:style id="PRIMITIVE" name="Primitive Type" face="bold" foreground="0,0,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="ANNOTATION" name="Annotation" face="bold" foreground="153,153,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="WHITESPACE" name="Whitespace" face="plain" foreground="0,0,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
</syntax-coloring-definition>
