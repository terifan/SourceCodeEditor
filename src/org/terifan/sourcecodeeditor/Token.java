package org.terifan.sourcecodeeditor;


public class Token
{
	private String mToken;
	private Style mStyle;
	private int mOffset;
	private boolean mComment;


	public Token()
	{
	}


	public Token(String aToken, Style aStyle, int aOffset, boolean aComment)
	{
		mToken = aToken;
		mStyle = aStyle;
		mOffset = aOffset;
		mComment = aComment;
	}


	public Style getStyle()
	{
		return mStyle;
	}


	public String getToken()
	{
		return mToken;
	}


	public int getOffset()
	{
		return mOffset;
	}


	public int length()
	{
		return mToken.length();
	}


	public boolean isComment()
	{
		return mComment;
	}


	public void append(String aToken)
	{
		mToken += aToken;
	}
}