package org.terifan.ui.sourceeditor;

import java.util.Stack;



@SuppressWarnings("unchecked")
public class UndoManager
{
	private Stack<UndoableEdit> mUndoableEdits = new Stack<UndoableEdit>();
	private Stack<UndoableEdit> mRedoableEdits = new Stack<UndoableEdit>();

	public UndoManager()
	{
	}

	public void addEdit(UndoableEdit aUndoableEdit)
	{
		mUndoableEdits.push(aUndoableEdit);
		mRedoableEdits.clear();
	}

	public boolean canRedo()
	{
		return mRedoableEdits.size() > 0;
	}

	public boolean canUndo()
	{
		return mUndoableEdits.size() > 0;
	}

	public String getRedoPresentationName()
	{
		if (mRedoableEdits.size() == 0) throw new IllegalStateException("no redo edits exists");
		return mRedoableEdits.peek().getPresentationName();
	}

	public String getUndoPresentationName()
	{
		if (mUndoableEdits.size() == 0) throw new IllegalStateException("no undo edits exists");
		return mUndoableEdits.peek().getPresentationName();
	}

	public void undo()
	{
		if (mUndoableEdits.size() == 0) throw new IllegalStateException("no undo edits exists");
		UndoableEdit edit = mUndoableEdits.pop();
		edit.undo();
		mRedoableEdits.push(edit);
	}

	public void redo()
	{
		if (mRedoableEdits.size() == 0) throw new IllegalStateException("no redo edits exists");
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