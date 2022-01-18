package org.terifan.ui.sourceeditor;


public interface UndoableAction
{
	public abstract void undo();
	public abstract void redo();
}