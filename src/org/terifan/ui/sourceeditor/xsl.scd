<?xml version="1.0"?>

<syntax-coloring-definition>
	<scd:syntax-parser class="org.terifan.apps.sourceeditor.XMLSyntaxParser"/>
	<scd:presentation-name label="Extensible Stylesheet Language"/>
	<scd:file-association extension="xsl" name="XSL-file"/>

	<sp:namespace name="xsl">
		<sp:tag name="stylesheet" strict="true">
			<sp:attribute name="version"/>
			<sp:attribute name="xmlns:xsl"/>
		</sp:tag>
		<sp:tag name="output" strict="true">
			<sp:attribute name="method"/>
			<sp:attribute name="indent"/>
		</sp:tag>
	</sp:namespace>

	<scd:style id="AMP" name="AMP" face="plain" foreground="234,202,21" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="ATTRIBUTE" name="Attribute" face="plain" foreground="111,0,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="BLOCKCOMMENT" name="Block Comment" face="italic" foreground="255,0,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="ELEMENT" name="Element" face="plain" foreground="0,0,153" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="HIGHLIGHT" name="Highlighted Text" face="plain" foreground="0,0,0" background="225,236,247" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="LINEBREAK" name="Line Break" face="plain" foreground="0,0,153" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="NAMESPACE" name="Namespace" face="plain" foreground="0,0,153" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="OPERATOR" name="Operator" face="plain" foreground="0,102,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="SEARCHRESULT" name="Search Result" face="plain" foreground="255,255,255" background="255,255,128" underlined="false" strikethrough="false" optional-background="false"/>
	<scd:style id="SELECTION" name="Selection" face="plain" foreground="255,255,255" background="0,0,153" underlined="false" strikethrough="false" optional-background="false"/>
	<scd:style id="STRINGLITERAL" name="String Literal" face="plain" foreground="204,0,204" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="TAG" name="Tag" face="bold" foreground="0,200,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="TEXT" name="Text" face="plain" foreground="0,0,255" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
	<scd:style id="WHITESPACE" name="Whitespace" face="plain" foreground="0,0,0" background="255,255,255" underlined="false" strikethrough="false" optional-background="true"/>
</syntax-coloring-definition>