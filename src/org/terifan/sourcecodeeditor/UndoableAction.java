package org.terifan.sourcecodeeditor;


public interface UndoableAction
{
	abstract void undo();
	abstract void redo();
}