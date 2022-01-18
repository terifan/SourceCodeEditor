package org.terifan.ui.sourceeditor;


public class Token
{
	protected String token;
	protected Style style;
	protected int offset;
	protected boolean comment;


	public Token()
	{
	}


	public Token(String token, Style style, int offset, boolean comment)
	{
		this.token = token;
		this.style = style;
		this.offset = offset;
		this.comment = comment;
	}


	public Style getStyle()
	{
		return style;
	}


	public String getToken()
	{
		return token;
	}


	public int getOffset()
	{
		return offset;
	}


	public int length()
	{
		return token.length();
	}


	public boolean isComment()
	{
		return comment;
	}
}