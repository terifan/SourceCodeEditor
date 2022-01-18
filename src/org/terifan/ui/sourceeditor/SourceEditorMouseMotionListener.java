package org.terifan.ui.sourceeditor;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.SwingUtilities;


public class SourceEditorMouseMotionListener implements MouseMotionListener
{
	protected SourceEditor mSourceEditor;

	protected SourceEditorMouseMotionListener(SourceEditor aSourceEditor)
	{
		mSourceEditor = aSourceEditor;
	}


	@Override
	public void mouseDragged(MouseEvent aEvent)
	{
		if (SwingUtilities.isLeftMouseButton(aEvent))
		{
			Caret caret = mSourceEditor.getCaret();

			mSourceEditor.setRectangularSelection(aEvent.isAltDown());

			Point vp = caret.getVirtualPosition();

			if (mSourceEditor.getSelectionStartUnmodified() == null)
			{
				mSourceEditor.setSelectionStartUnmodified(new Point(vp));
			}

			Point p = mSourceEditor.getSourceOffset(aEvent.getPoint());

			caret.moveAbsolute(p.x, p.y, false, true, true);
		}
	}


	@Override
	public void mouseMoved(MouseEvent e)
	{
	}
}
