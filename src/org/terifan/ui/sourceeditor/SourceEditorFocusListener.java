package org.terifan.ui.sourceeditor;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;


public class SourceEditorFocusListener implements FocusListener
{
	protected SourceEditor mSourceEditor;

	protected SourceEditorFocusListener(SourceEditor aSourceEditor)
	{
		mSourceEditor = aSourceEditor;
	}


	@Override
	public void focusGained(FocusEvent aEvent)
	{
		mSourceEditor.getCaret().setEnabled(true);
		mSourceEditor.repaint();
		mSourceEditor.getCaret().paintImmediately();
	}


	@Override
	public void focusLost(FocusEvent aEvent)
	{
		mSourceEditor.getCaret().hideImmediately();
		mSourceEditor.getCaret().setEnabled(false);
	}
}
