package org.terifan.sourcecodeeditor;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serializable;
import javax.swing.SwingUtilities;


public class SourceEditorMouseListener implements MouseListener, MouseMotionListener, Serializable
{
	private final static long serialVersionUID = 1L;
	protected SourceEditor mSourceEditor;


	protected SourceEditorMouseListener(SourceEditor aSourceEditor)
	{
		mSourceEditor = aSourceEditor;
	}


	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		mSourceEditor.requestFocus();

		if (SwingUtilities.isLeftMouseButton(aEvent))
		{
			Caret caret = mSourceEditor.getCaret();
			Point vp = caret.getVirtualPosition();
			Point p = mSourceEditor.getSourceOffset(aEvent.getPoint());

			if (aEvent.getClickCount() >= 2)
			{
				int x1 = mSourceEditor.getNextTokenOffset(p.x, p.y, false);
				int x0 = mSourceEditor.getPreviousTokenOffset(x1, p.y);

				mSourceEditor.setRectangularSelection(false);
				mSourceEditor.setSelectionStart(x0, p.y);
				mSourceEditor.setSelectionEnd(x1, p.y);

				mSourceEditor.setHighlightText(mSourceEditor.getSelectedText().toString().trim());

				p.x = x1;

				mSourceEditor.repaint();
			}
			else if (aEvent.isShiftDown())
			{
				mSourceEditor.setRectangularSelection(aEvent.isAltDown());
				if (mSourceEditor.getSelectionStartUnmodified() == null)
				{
					mSourceEditor.setSelectionStartUnmodified(new Point(vp));
				}
			}
			else if (mSourceEditor.getSelectionStartUnmodified() != null)
			{
				mSourceEditor.setRectangularSelection(false);
				mSourceEditor.setSelectionStartUnmodified(null);
				mSourceEditor.setSelectionEndUnmodified(null);
				mSourceEditor.repaint();
			}

			caret.moveAbsolute(p.x, p.y, false, true, true);
		}
	}


	@Override
	public void mouseReleased(MouseEvent aEvent)
	{
		Point ss = mSourceEditor.getSelectionStartUnmodified();
		if (ss != null && ss.equals(mSourceEditor.getSelectionEndUnmodified()))
		{
			mSourceEditor.setSelectionStartUnmodified(null);
			mSourceEditor.setSelectionEndUnmodified(null);
			mSourceEditor.setRectangularSelection(false);
		}
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
	public void mouseClicked(MouseEvent e)
	{
	}


	@Override
	public void mouseEntered(MouseEvent e)
	{
	}


	@Override
	public void mouseExited(MouseEvent e)
	{
	}


	@Override
	public void mouseMoved(MouseEvent e)
	{
	}
}
