package org.terifan.ui.sourceeditor;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class SourceEditorKeyListener implements KeyListener
{
	protected SourceEditor mSourceEditor;

	protected SourceEditorKeyListener(SourceEditor aSourceEditor)
	{
		mSourceEditor = aSourceEditor;
	}


	@Override
	public void keyTyped(KeyEvent aEvent)
	{
		if (aEvent.isAltDown())
		{
			return;
		}

		char keyChar = aEvent.getKeyChar();

		if (keyChar == '\t' && (!mSourceEditor.getAlternateMode() || aEvent.isControlDown()) && mSourceEditor.isTextSelected() && mSourceEditor.getSelectionStartUnmodified().y != mSourceEditor.getSelectionEndUnmodified().y)
		{
			if (mSourceEditor.getTabIndentsTextEnabled())
			{
				if (aEvent.isShiftDown())
				{
					mSourceEditor.outdent();
				}
				else
				{
					mSourceEditor.indent();
				}
			}
			else
			{
				mSourceEditor.replaceSelection("\t");
			}
			mSourceEditor.repaint();
		}
		else if (keyChar >= 32 && keyChar != 127 || (keyChar == '\t' && (!mSourceEditor.getAlternateMode() || aEvent.isControlDown())))
		{
			mSourceEditor.keyTyped(keyChar);
			mSourceEditor.repaint();
		}
	}


	@Override
	public void keyPressed(KeyEvent aEvent)
	{
		if (mSourceEditor.getAlternateMode() && aEvent.getKeyChar() == '\t')
		{
			keyTyped(aEvent);
			return;
		}

		boolean repaint = false;

		switch (aEvent.getKeyCode())
		{
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_PAGE_DOWN:
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
				if (aEvent.isControlDown())
				{
					break;
				}
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_HOME:
			case KeyEvent.VK_END:
				if (aEvent.isShiftDown())
				{
					mSourceEditor.setRectangularSelection(aEvent.isAltDown());
					if (!mSourceEditor.isTextSelected())
					{
						Point vp = mSourceEditor.getCaret().getVirtualPosition();
						if (mSourceEditor.getSelectionStartUnmodified() == null)
						{
							mSourceEditor.setSelectionStartUnmodified(new Point(vp));
						}
						else
						{
							mSourceEditor.getSelectionStartUnmodified().move(vp.x, vp.y);
						}
						repaint = true;
					}
				}
				else
				{
					if (mSourceEditor.getSelectionStartUnmodified() != null)
					{
						mSourceEditor.setSelectionStartUnmodified(null);
						mSourceEditor.setSelectionEndUnmodified(null);
						mSourceEditor.setRectangularSelection(false);
						mSourceEditor.repaint();
					}
				}
				break;
		}

		switch (aEvent.getKeyCode())
		{
			case KeyEvent.VK_PAGE_UP:
				if (aEvent.isControlDown())
				{
					mSourceEditor.scrollPageUp();
				}
				else
				{
					mSourceEditor.moveCaretPageUp();
				}
				break;
			case KeyEvent.VK_PAGE_DOWN:
				if (aEvent.isControlDown())
				{
					mSourceEditor.scrollPageDown();
				}
				else
				{
					mSourceEditor.moveCaretPageDown();
				}
				break;
			case KeyEvent.VK_UP:
				if (aEvent.isControlDown())
				{
					mSourceEditor.scrollLineUp();
				}
				else
				{
					mSourceEditor.moveCaretLineUp();
				}
				break;
			case KeyEvent.VK_DOWN:
				if (aEvent.isControlDown())
				{
					mSourceEditor.scrollLineDown();
				}
				else
				{
					mSourceEditor.moveCaretLineDown();
				}
				break;
			case KeyEvent.VK_LEFT:
				if (aEvent.isControlDown())
				{
					mSourceEditor.moveCaretPreviousToken();
				}
				else
				{
					mSourceEditor.moveCaretLeft();
				}
				break;
			case KeyEvent.VK_RIGHT:
				if (aEvent.isControlDown())
				{
					mSourceEditor.moveCaretNextToken();
				}
				else
				{
					mSourceEditor.moveCaretRight();
				}
				break;
			case KeyEvent.VK_HOME:
				if (aEvent.isControlDown())
				{
					mSourceEditor.moveCaretDocumentTop();
				}
				else
				{
					mSourceEditor.moveCaretLineStart();
				}
				break;
			case KeyEvent.VK_END:
				if (aEvent.isControlDown())
				{
					mSourceEditor.moveCaretDocumentEnd();
				}
				else
				{
					mSourceEditor.moveCaretLineEnd();
				}
				break;
			case KeyEvent.VK_BACK_SPACE:
				mSourceEditor.deletePreviousCharacter();
				repaint = true;
				break;
			case KeyEvent.VK_ENTER:
				if (!mSourceEditor.getAlternateMode() || aEvent.isControlDown())
				{
					mSourceEditor.insertBreak();
					repaint = true;
				}
				break;
			case KeyEvent.VK_DELETE:
				if (aEvent.isControlDown())
				{
					mSourceEditor.deleteToken();
				}
				else
				{
					mSourceEditor.deleteNextCharacter();
				}
				repaint = true;
				break;
		}

		if (repaint)
		{
			mSourceEditor.repaint();
		}
	}


	@Override
	public void keyReleased(KeyEvent e)
	{
	}
}