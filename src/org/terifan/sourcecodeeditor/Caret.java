package org.terifan.sourcecodeeditor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;


public class Caret extends Thread implements Serializable
{
	private final static long serialVersionUID = 1L;

	private final Point mCaretCharacterPosition; // position of caret disregarding tabs and line lengths
	private final Point mCaretPixelPosition;
	private final Point mCaretVirtualPosition; // position of caret in regard of tabs and line lengths
	private final Point mPreviousCaretPixelPosition;
	private final SourceEditor mSourceEditor;
	private final ArrayList<CaretListener> mCaretListeners;
	private int mPreferredCaretVirtualPositionX;
	private boolean mDispose;
	private boolean mEnabled;
	private boolean mIsHidden;
	private boolean mVisible;
	private boolean mWasDisabled;
	private long mResync;


	public Caret(SourceEditor aSourceEditor)
	{
		super.setDaemon(true);

		mSourceEditor = aSourceEditor;
		mCaretListeners = new ArrayList<>();
		mCaretCharacterPosition = new Point();
		mCaretVirtualPosition = new Point();
		mCaretPixelPosition = new Point();
		mPreviousCaretPixelPosition = new Point();
		mEnabled = true;
	}


	protected void dispose()
	{
		mDispose = true;
	}


	public void addCaretListener(CaretListener aCaretListener)
	{
		mCaretListeners.add(aCaretListener);
	}


	public void removeCaretListener(CaretListener aCaretListener)
	{
		mCaretListeners.remove(aCaretListener);
	}


	@Override
	public void run()
	{
		Point prevPosition = new Point();

		try
		{
			int caretBlinkRate = mSourceEditor.getCaretBlinkRate();
			long sleepTime = caretBlinkRate;
			boolean visible = true;

			sleep(sleepTime);

			while (!mDispose)
			{
				// handle token highlight
				Point p = mCaretCharacterPosition;

				if (p.equals(prevPosition))
				{
					Style style = mSourceEditor.getTokenStyleAt(p.x, p.y);

					if (style != null && style.isSupportHighlight())
					{
						int x1 = mSourceEditor.getNextTokenOffset(p.x, p.y, false);
						int x0 = mSourceEditor.getPreviousTokenOffset(x1, p.y);

						String text = mSourceEditor.getDocument().getLine(p.y).substring(x0, x1).trim();

						if (!text.equals(mSourceEditor.getHighlightText()))
						{
							mSourceEditor.setHighlightText(text);
							mSourceEditor.repaint();
						}
					}
				}
				else
				{
					prevPosition.move(p.x, p.y);
				}

				// handle caret blink
				if (mResync != 0)
				{
					visible = true;
					sleepTime = Math.max(caretBlinkRate - (System.currentTimeMillis() - mResync), 0);
					mResync = 0;
				}
				else
				{
					mVisible = visible;
					paintCaret(mSourceEditor.getGraphics());
					sleepTime = caretBlinkRate;
				}

				sleep(sleepTime);

				visible = !visible;
			}
		}
		catch (Exception | Error e)
		{
			e.printStackTrace(System.err);
		}
	}


	public void forceVisibility()
	{
		mVisible = true;
	}


	public void forceNonVisibility()
	{
		mIsHidden = false;
	}


	public void paintImmediately()
	{
		mVisible = true;
		paintCaret(mSourceEditor.getGraphics());
		mResync = System.currentTimeMillis();
	}


	public void hideImmediately()
	{
		int x = mPreviousCaretPixelPosition.x + mSourceEditor.getMargins().left;
		int y = mPreviousCaretPixelPosition.y + mSourceEditor.getMargins().top;
		mSourceEditor.repaint(x, y, 2, mSourceEditor.getFontHeight() + mSourceEditor.getLineSpacing());
	}


	public void paintCaret(Graphics aGraphics)
	{
		if (aGraphics == null)
		{
			return;
		}

		if (mVisible)
		{
			if (!mCaretPixelPosition.equals(mPreviousCaretPixelPosition))
			{
				int x = mPreviousCaretPixelPosition.x + mSourceEditor.getMargins().left;
				int y = mPreviousCaretPixelPosition.y + mSourceEditor.getMargins().top;
				mPreviousCaretPixelPosition.setLocation(mCaretPixelPosition);

				if (mEnabled || mWasDisabled)
				{
					mWasDisabled = false;
					mSourceEditor.repaint(x, y, 2, mSourceEditor.getFontHeight() + mSourceEditor.getLineSpacing());
				}
			}

			if (mEnabled)
			{
				int x = mCaretPixelPosition.x + mSourceEditor.getMargins().left;
				int y = mCaretPixelPosition.y + mSourceEditor.getMargins().top;
				if (mSourceEditor.getStyle(SyntaxParser.CARET) != null)
				{
					aGraphics.setColor(mSourceEditor.getStyle(SyntaxParser.CARET).getForeground());
				}
				else
				{
					aGraphics.setColor(Color.BLACK);
					aGraphics.setXORMode(Color.WHITE);
				}
				if (mSourceEditor.isBoldCaretEnabled())
				{
					aGraphics.drawLine(x + 1, y, x + 1, y + mSourceEditor.getFontHeight() - 1);
				}
				aGraphics.drawLine(x, y, x, y + mSourceEditor.getFontHeight() - 1);
				aGraphics.setPaintMode();
			}
			mIsHidden = false;
		}
		else if (!mIsHidden)
		{
			if (mEnabled || mWasDisabled)
			{
				mWasDisabled = false;
				int x = mPreviousCaretPixelPosition.x + mSourceEditor.getMargins().left;
				int y = mPreviousCaretPixelPosition.y + mSourceEditor.getMargins().top;
				mSourceEditor.repaint(x, y, 2, mSourceEditor.getFontHeight() + mSourceEditor.getLineSpacing());
			}
			mIsHidden = true;
		}
	}


	protected void makePreferredPosition()
	{
		mPreferredCaretVirtualPositionX = mCaretVirtualPosition.x;
	}


	public void moveAbsolute(int aPositionX, int aPositionY, boolean aAdjustByVirtualOffset, boolean aAdjustSelection, boolean aPaintCaret)
	{
		moveRelative(aPositionX - mCaretCharacterPosition.x, aPositionY - mCaretCharacterPosition.y, aAdjustByVirtualOffset, aAdjustSelection, aPaintCaret);
	}


	public synchronized void moveRelative(int aDeltaX, int aDeltaY, boolean aAdjustByVirtualOffset, boolean aAdjustSelection, boolean aPaintCaret)
	{
		Document document = mSourceEditor.getDocument();
		int lineCount = document.getLineCount();

		if (aDeltaX != 0)
		{
			mCaretCharacterPosition.x += aDeltaX;
			mPreferredCaretVirtualPositionX += aDeltaX;
		}
		if (aDeltaY != 0)
		{
			mCaretCharacterPosition.y += aDeltaY;

			if (mCaretCharacterPosition.y < 0)
			{
				mCaretCharacterPosition.y = 0;
			}
			if (mCaretCharacterPosition.y >= lineCount)
			{
				mCaretCharacterPosition.y = lineCount - 1;
			}

			if (aAdjustByVirtualOffset)
			{
				mCaretCharacterPosition.x = mSourceEditor.findTabbedOffset(mPreferredCaretVirtualPositionX, mCaretCharacterPosition.y);
			}
		}

		if (mCaretCharacterPosition.y >= lineCount)
		{
			mCaretCharacterPosition.y = lineCount - 1;
		}
		if (mCaretCharacterPosition.x < 0)
		{
			mCaretCharacterPosition.x = 0;
		}

		mCaretVirtualPosition.x = mSourceEditor.includeTabsInOffset(mCaretCharacterPosition.x, mCaretCharacterPosition.y);
		mCaretVirtualPosition.y = mCaretCharacterPosition.y;

		if (aDeltaX != 0)
		{
			mPreferredCaretVirtualPositionX = mCaretVirtualPosition.x;
		}

		Point oldPixelPosition = new Point(mCaretPixelPosition);

		mCaretPixelPosition.x = mSourceEditor.getPixelOffset(Math.min(mCaretCharacterPosition.x, document.getLineLength(mCaretCharacterPosition.y)), mCaretCharacterPosition.y);
		mCaretPixelPosition.y = mCaretCharacterPosition.y * (mSourceEditor.getFontHeight() + mSourceEditor.getLineSpacing());

		if (!mCaretPixelPosition.equals(oldPixelPosition))
		{
			if (aAdjustSelection && mSourceEditor.getSelectionStart() != null)
			{
				mSourceEditor.setSelectionEnd(mCaretCharacterPosition.x, mCaretCharacterPosition.y);
				mSourceEditor.repaint();
			}

			if (aPaintCaret)
			{
				mVisible = true;
				paintCaret(mSourceEditor.getGraphics());
			}
			else
			{
				mVisible = false;
				mIsHidden = false;
				paintCaret(mSourceEditor.getGraphics());
			}

			mSourceEditor.scrollRectToVisible(new Rectangle(mCaretPixelPosition.x + mSourceEditor.getMargins().left, mCaretPixelPosition.y + mSourceEditor.getMargins().top, 1, mSourceEditor.getFontHeight()));

			mResync = System.currentTimeMillis();

			CaretEvent caretEvent = new CaretEvent(mSourceEditor, this, new Point(mCaretCharacterPosition), new Point(mCaretVirtualPosition));
			for (int i = mCaretListeners.size(); --i >= 0;)
			{
				mCaretListeners.get(i).caretMoved(caretEvent);
			}

			if (aDeltaY != 0 && mSourceEditor.isHighlightCaretRowEnabled())
			{
				mSourceEditor.repaint();
			}
		}
	}


	public void scrollToCaret()
	{
		moveRelative(0, 0, false, false, true);
	}


	public Point getCharacterPosition()
	{
		return mCaretCharacterPosition;
	}


	public Point getVirtualPosition()
	{
		return mCaretVirtualPosition;
	}


	public Point getPixelPosition()
	{
		return mCaretPixelPosition;
	}


	public void setEnabled(boolean aEnabled)
	{
		mEnabled = aEnabled;
		if (!mEnabled)
		{
			mWasDisabled = true;
		}
		mResync = System.currentTimeMillis();
	}


	public boolean isEnabled()
	{
		return mEnabled;
	}
}
