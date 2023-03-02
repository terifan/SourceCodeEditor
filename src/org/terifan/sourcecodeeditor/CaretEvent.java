package org.terifan.sourcecodeeditor;

import java.awt.Point;


public class CaretEvent
{
	private final SourceEditor mSourceEditor;
	private final Caret mCaret;
	private final Point mCharacterPosition;
	private final Point mVirtualPosition;


	public CaretEvent(SourceEditor aSourceEditor, Caret aCaret, Point aCharacterPosition, Point aVirtualPosition)
	{
		mSourceEditor = aSourceEditor;
		mCaret = aCaret;
		mCharacterPosition = aCharacterPosition;
		mVirtualPosition = aVirtualPosition;
	}


	public SourceEditor getSourceEditor()
	{
		return mSourceEditor;
	}


	public Caret getCaret()
	{
		return mCaret;
	}


	public Point getCharacterPosition()
	{
		return mCharacterPosition;
	}


	public Point getVirtualPosition()
	{
		return mVirtualPosition;
	}
}