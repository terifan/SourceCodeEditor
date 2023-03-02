package org.terifan.sourcecodeeditor;

import java.awt.Point;
import java.util.ArrayList;


public class UndoableEdit
{
	private final SourceEditor mSourceEditor;
	private final ArrayList<UndoableAction> mUndoableActions;
	private final String mPresentationName;
	private int mCaretStartPositionX;
	private int mCaretStartPositionY;
	private int mCaretEndPositionX;
	private int mCaretEndPositionY;


	public UndoableEdit(SourceEditor aSourceEditor, String aPresentationName)
	{
		this.mUndoableActions = new ArrayList();
		mSourceEditor = aSourceEditor;
		mPresentationName = aPresentationName;
	}


	public void setCaretStartPosition(Point aCaretPosition)
	{
		mCaretStartPositionX = aCaretPosition.x;
		mCaretStartPositionY = aCaretPosition.y;
	}


	public void setCaretEndPosition(Point aCaretPosition)
	{
		mCaretEndPositionX = aCaretPosition.x;
		mCaretEndPositionY = aCaretPosition.y;
	}


	public void addAction(UndoableAction aUndoableAction)
	{
		mUndoableActions.add(aUndoableAction);
	}


	public boolean isEmpty()
	{
		return mUndoableActions.isEmpty();
	}


	public String getPresentationName()
	{
		return mPresentationName;
	}


	public void undo()
	{
		for (int i = mUndoableActions.size(); --i >= 0; )
		{
			mUndoableActions.get(i).undo();
		}

		mSourceEditor.getCaret().moveAbsolute(mCaretStartPositionX, mCaretStartPositionY, false, false, true);
	}


	public void redo()
	{
		for (int i = 0, sz = mUndoableActions.size(); i < sz; i++)
		{
			mUndoableActions.get(i).redo();
		}

		mSourceEditor.getCaret().moveAbsolute(mCaretEndPositionX, mCaretEndPositionY, false, false, true);
	}
}
