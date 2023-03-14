package org.terifan.sourcecodeeditor;


public class Token
{
	private String mText;
	private String mStyle;
	private int mOffset;
	private boolean mComment;


	public Token()
	{
	}


	public Token(String aText, String aStyle, int aOffset, boolean aComment)
	{
		mText = aText;
		mStyle = aStyle;
		mOffset = aOffset;
		mComment = aComment;
	}


	public String getStyle()
	{
		return mStyle;
	}


	public String getText()
	{
		return mText;
	}


	public int getOffset()
	{
		return mOffset;
	}


	public int length()
	{
		return mText.length();
	}


	public boolean isComment()
	{
		return mComment;
	}


	public void append(String aToken)
	{
		mText += aToken;
	}
}