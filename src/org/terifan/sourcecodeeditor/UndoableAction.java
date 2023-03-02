package org.terifan.sourcecodeeditor;


public interface UndoableAction
{
	void undo();
	void redo();
}