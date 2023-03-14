package org.terifan.sourcecodeeditor;

import java.awt.Point;
import java.util.ArrayList;


public class UndoableEdit
{
	private final SourceEditor mSourceEditor;
	private final ArrayList<UndoableAction> mUndoableActions;
	private final ArrayList<String> mSubActions;
	private final String mPresentationName;
	private State mStartState;
	private State mEndState;


	public UndoableEdit(SourceEditor aSourceEditor, String aPresentationName)
	{
		mUndoableActions = new ArrayList();
		mSourceEditor = aSourceEditor;
		mPresentationName = aPresentationName;
		mSubActions = new ArrayList<>();
	}


	public void saveStartState()
	{
		mStartState = new State();
	}


	public void saveEndState()
	{
		mEndState = new State();
	}


	public void addAction(UndoableAction aUndoableAction)
	{
		mUndoableActions.add(aUndoableAction);
		mSubActions.add(mPresentationName);
	}


	public void addSubAction(String aName)
	{
		mSubActions.add(aName);
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

		mStartState.restore();
	}


	public void redo()
	{
		for (UndoableAction action : mUndoableActions)
		{
			action.redo();
		}

		mEndState.restore();
	}


	private class State
	{
		private Point mCaret;
		private Point mSelectionStart;
		private Point mSelectionEnd;
		private boolean mRectangularSelection;


		public State()
		{
			mSelectionStart = mSourceEditor.getSelectionStart();
			mSelectionEnd = mSourceEditor.getSelectionEnd();
			mRectangularSelection = mSourceEditor.isRectangularSelection();
			mCaret = mSourceEditor.getCaret().getCharacterPosition();
		}


		public void restore()
		{
			mSourceEditor.getCaret().moveAbsolute(mCaret.x, mCaret.y, false, false, true);
			if (mSelectionStart != null && mSelectionEnd != null)
			{
				mSourceEditor.setRectangularSelection(mRectangularSelection);
				mSourceEditor.setSelectionStart(mSelectionStart.x, mSelectionStart.y);
				mSourceEditor.setSelectionEnd(mSelectionEnd.x, mSelectionEnd.y);
			}
			else
			{
				mSourceEditor.clearSelection();
			}
		}
	}
}
