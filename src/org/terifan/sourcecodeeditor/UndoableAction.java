package org.terifan.sourcecodeeditor;


public interface UndoableAction
{
	public abstract void undo();
	public abstract void redo();
}