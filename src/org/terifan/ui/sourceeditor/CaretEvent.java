package org.terifan.ui.sourceeditor;

import java.awt.Point;


public class CaretEvent
{
	private SourceEditor mSourceEditor;
	private Caret mCaret;
	private Point mCharacterPosition;
	private Point mVirtualPosition;


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