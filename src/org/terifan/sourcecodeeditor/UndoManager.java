package org.terifan.sourcecodeeditor;

import java.util.Stack;


public class UndoManager
{
	private final Stack<UndoableEdit> mUndoableEdits;
	private final Stack<UndoableEdit> mRedoableEdits;


	public UndoManager()
	{
		mRedoableEdits = new Stack<>();
		mUndoableEdits = new Stack<>();
	}


	public void addEdit(UndoableEdit aUndoableEdit)
	{
		mUndoableEdits.push(aUndoableEdit);
		mRedoableEdits.clear();
	}


	public boolean canRedo()
	{
		return !mRedoableEdits.isEmpty();
	}


	public boolean canUndo()
	{
		return !mUndoableEdits.isEmpty();
	}


	public String getRedoPresentationName()
	{
		if (mRedoableEdits.isEmpty())
		{
			throw new IllegalStateException("no redo edits exists");
		}
		return mRedoableEdits.peek().getPresentationName();
	}


	public String getUndoPresentationName()
	{
		if (mUndoableEdits.isEmpty())
		{
			throw new IllegalStateException("no undo edits exists");
		}
		return mUndoableEdits.peek().getPresentationName();
	}


	public void undo()
	{
		if (mUndoableEdits.isEmpty())
		{
			throw new IllegalStateException("no undo edits exists");
		}
		UndoableEdit edit = mUndoableEdits.pop();
		edit.undo();
		mRedoableEdits.push(edit);
	}


	public void redo()
	{
		if (mRedoableEdits.isEmpty())
		{
			throw new IllegalStateException("no redo edits exists");
		}
		UndoableEdit edit = mRedoableEdits.pop();
		edit.redo();
		mUndoableEdits.push(edit);
	}


	public void discardAllEdits()
	{
		mUndoableEdits.clear();
		mRedoableEdits.clear();
	}
}
