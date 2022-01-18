package org.terifan.ui.sourceeditor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;


public class SourceEditor extends JComponent implements Scrollable
{
	private final static boolean DEBUG_GRAPHICS = false;

	private boolean mRectangularSelection;
	private boolean mRequestFocus;
	private Caret mCaret;
	private SyntaxParser mSyntaxParser;
	private Dimension mPreferredSize;
	private Document mDocument;
	private final Point mSelectionTempPoint = new Point();
	private Point mSelectionEnd;
	private Point mSelectionStart;
	private String mClipboardContent; // used to manage rectangular selections
	private SourceEditorMouseListener mMouseListener;
	private SourceEditorMouseMotionListener mMouseMotionListener;
	private SourceEditorKeyListener mKeyListener;
	private SourceEditorFocusListener mFocusListener;
	private JDialog mFindDialog;

	// user preferences
	private boolean mAutoIndentEnabled;
	private boolean mAutoLineCopyCutEnabled;
	private boolean mBoldCaretEnabled;
	private boolean mHighlightTextCaseSensative;
	private boolean mLineBreakSymbolEnabled;
	private boolean mOverwriteTextEnabled;
	private boolean mTabIndentsTextEnabled;
	private boolean mWhitespaceSymbolEnabled;
	private Insets mMargins;
	private int mFontSize;
	private int mLineSpacing;
	private int mTabSize;
	private String mHighlightText;
	private String mLineBreakSymbol;
	private boolean mAlternateMode;
	private Object mAntialiase;

	private SyntaxParser mInputSyntaxParser;
	private SyntaxParser mOffsetSyntaxParser;
	private SyntaxParser mPaintSyntaxParser;
	private SyntaxParser mPixelOffsetSyntaxParser;


	public SourceEditor(SyntaxParser aSyntaxParser, Document aDocument)
	{
		// Disable arrow keys in any ancestor JScrollPane
		UIManager.getDefaults().put("ScrollPane.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[] {}));

		setOpaque(true);
		setDocument(aDocument);
		setSyntaxParser(aSyntaxParser);
		setBackground(mPaintSyntaxParser.getStyle(SyntaxParser.WHITESPACE).getBackground());
		setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		setFocusTraversalKeysEnabled(false);
		setFocusable(true);

		setAutoIndentEnabled(true);
		setAutoLineCopyCutEnabled(true);
		setBoldCaretEnabled(false);
		setHighlightTextCaseSensative(false);
		setLineBreakSymbolEnabled(false);
		setOverwriteTextEnabled(false);
		setTabIndentsTextEnabled(true);
		setWhitespaceSymbolEnabled(false);
		setMargins(new Insets(0, 1, 0, 50));
		setFontPointSize(13);
		setLineSpacing(0);
		setTabSize(4);
		setLineBreakSymbol('\u00B6');
		setAlternateMode(false);
		setAntialiase(RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		mMouseListener = new SourceEditorMouseListener(this);
		mMouseMotionListener = new SourceEditorMouseMotionListener(this);
		mFocusListener = new SourceEditorFocusListener(this);
		mKeyListener = new SourceEditorKeyListener(this);

		addKeyListener(mKeyListener);
		addMouseListener(mMouseListener);
		addMouseMotionListener(mMouseMotionListener);
		addFocusListener(mFocusListener);

		mCaret = new Caret(this);
		mCaret.setEnabled(false);
		mCaret.start();

		mRequestFocus = true;

		registerDefaultKeyboardActions();
	}


	public void dispose()
	{
		mCaret.dispose();
	}


	private void registerDefaultKeyboardActions()
	{
		ActionListener actionListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent aEvent)
			{
				performAction(aEvent.getActionCommand());
			}
		};

		super.registerKeyboardAction(actionListener, "undo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "redo", KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "cut", KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "paste", KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "selectAll", KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "deleteLine", KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "upperCase", KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "lowerCase", KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "find", KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
	}


	public void performAction(String aAction)
	{
		if ("undo".equals(aAction))
		{
			undo();
		}
		else if ("redo".equals(aAction))
		{
			redo();
		}
		else if ("cut".equals(aAction))
		{
			cut();
		}
		else if ("copy".equals(aAction))
		{
			copy();
		}
		else if ("paste".equals(aAction))
		{
			paste();
		}
		else if ("deleteLine".equals(aAction))
		{
			deleteLine();
		}
		else if ("selectAll".equals(aAction))
		{
			selectAll();
		}
		else if ("upperCase".equals(aAction))
		{
			upperCase();
		}
		else if("lowerCase".equals(aAction))
		{
			lowerCase();
		}
		else if("lowerCase".equals(aAction))
		{
			deleteNextCharacter();
		}
		else if("deleteNextCharacter".equals(aAction))
		{
			deleteNextCharacter();
		}
		else if("deletePreviousCharacter".equals(aAction))
		{
			deletePreviousCharacter();
		}
		else if("deleteToken".equals(aAction))
		{
			deleteToken();
		}
		else if("moveCaretDocumentTop".equals(aAction))
		{
			moveCaretDocumentTop();
		}
		else if("moveCaretDocumentEnd".equals(aAction))
		{
			moveCaretDocumentEnd();
		}
		else if("moveCaretLineStart".equals(aAction))
		{
			moveCaretLineStart();
		}
		else if("moveCaretPageUp".equals(aAction))
		{
			moveCaretPageUp();
		}
		else if("moveCaretPageDown".equals(aAction))
		{
			moveCaretPageDown();
		}
		else if("resetSelection".equals(aAction))
		{
			resetSelection();
		}
		else if("find".equals(aAction))
		{
			if (mFindDialog == null)
			{
				mFindDialog = new FindDialog(this);
				mFindDialog.pack();
			}
			mFindDialog.setLocationRelativeTo(this);
			mFindDialog.setVisible(true);
		}
		else
		{
			throw new IllegalArgumentException("Unsupported action: " + aAction);
		}
	}


	public boolean canUndo()
	{
		return mDocument.getUndoManager().canUndo();
	}


	public void undo()
	{
		if (canUndo())
		{
			mDocument.getUndoManager().undo();
			repaint();
		}
	}


	public boolean canRedo()
	{
		return mDocument.getUndoManager().canRedo();
	}


	public void redo()
	{
		if (canRedo())
		{
			mDocument.getUndoManager().redo();
			repaint();
		}
	}


	public void dontRequestFocus()
	{
		mRequestFocus = false;
	}


	public void setSyntaxParser(SyntaxParser aSyntaxParser)
	{
		if (aSyntaxParser == null)
		{
			throw new IllegalArgumentException("Provided SyntaxParser is null");
		}

		mSyntaxParser = aSyntaxParser;

		mInputSyntaxParser = mSyntaxParser.newInstance();
		mOffsetSyntaxParser = mSyntaxParser.newInstance();
		mPaintSyntaxParser = mSyntaxParser.newInstance();
		mPixelOffsetSyntaxParser = mSyntaxParser.newInstance();

		mPreferredSize = null;
	}


	public void setHighlightText(String aHighlightText)
	{
		mHighlightText = aHighlightText;

		if (mHighlightText != null && mHighlightText.length() == 0)
		{
			mHighlightText = null;
		}
	}


	public String getHighlightText()
	{
		return mHighlightText;
	}


	public void setHighlightTextCaseSensative(boolean aState)
	{
		mHighlightTextCaseSensative = aState;
	}


	public boolean getHighlightTextCaseSensative()
	{
		return mHighlightTextCaseSensative;
	}


	public void setTabIndentsTextEnabled(boolean aTabIndentsTextEnabled)
	{
		mTabIndentsTextEnabled = aTabIndentsTextEnabled;
	}


	public boolean getAlternateMode()
	{
		return mAlternateMode;
	}


	public void setAlternateMode(boolean aAlternateMode)
	{
		mAlternateMode = aAlternateMode;
	}


	public Object getAntialiase()
	{
		return mAntialiase;
	}


	/**
	 * Enables or disables anti aliasing of the text. Use RenderingHints
	 * constants for this. Default is VALUE_TEXT_ANTIALIAS_LCD_HRGB. A
	 * null value will disable anti aliasing.
	 *
	 * @param aAntialiase
	 *   a RenderingHints.VALUE_TEXT_ANTIALIAS_* value or null.
	 */
	public void setAntialiase(Object aAntialiase)
	{
		mAntialiase = aAntialiase;
	}


	public boolean getTabIndentsTextEnabled()
	{
		return mTabIndentsTextEnabled;
	}


	public void setAutoLineCopyCutEnabled(boolean aAutoLineCopyCutEnabled)
	{
		mAutoLineCopyCutEnabled = aAutoLineCopyCutEnabled;
	}


	public boolean getAutoLineCopyCutEnabled()
	{
		return mAutoLineCopyCutEnabled;
	}


	public void setAutoIndentEnabled(boolean aAutoIndentEnabled)
	{
		mAutoIndentEnabled = aAutoIndentEnabled;
	}


	public boolean getAutoIndentEnabled()
	{
		return mAutoIndentEnabled;
	}


	public void setBoldCaretEnabled(boolean aBoldCaretEnabled)
	{
		mBoldCaretEnabled = aBoldCaretEnabled;
	}


	public boolean getBoldCaretEnabled()
	{
		return mBoldCaretEnabled;
	}


	public void setLineBreakSymbolEnabled(boolean aLineBreakSymbolEnabled)
	{
		mLineBreakSymbolEnabled = aLineBreakSymbolEnabled;
	}


	public boolean getLineBreakSymbolEnabled()
	{
		return mLineBreakSymbolEnabled;
	}


	public void setOverwriteTextEnabled(boolean aOverwriteTextEnabled)
	{
		mOverwriteTextEnabled = aOverwriteTextEnabled;
	}


	public boolean getOverwriteTextEnabled()
	{
		return mOverwriteTextEnabled;
	}


	public void setLineSpacing(int aLineSpacing)
	{
		mLineSpacing = aLineSpacing;
	}


	public int getLineSpacing()
	{
		return mLineSpacing;
	}


	public void setWhitespaceSymbolEnabled(boolean aWhitespaceSymbolEnabled)
	{
		mWhitespaceSymbolEnabled = aWhitespaceSymbolEnabled;
	}


	public boolean getWhitespaceSymbolEnabled()
	{
		return mWhitespaceSymbolEnabled;
	}


	public void setTabSize(int aTabSize)
	{
		mTabSize = aTabSize;
	}


	public int getTabSize()
	{
		return mTabSize;
	}


	public void setMargins(Insets aMargins)
	{
		mMargins = aMargins;
	}


	public Insets getMargins()
	{
		return mMargins;
	}


	public void setLineBreakSymbol(char aLineBreakSymbol)
	{
		mLineBreakSymbol = Character.toString(aLineBreakSymbol);
	}


	public char getLineBreakSymbol()
	{
		return mLineBreakSymbol.charAt(0);
	}


	public Caret getCaret()
	{
		return mCaret;
	}


	public SyntaxParser getSyntaxParser()
	{
		return mSyntaxParser;
	}


	public void setDocument(Document aDocument)
	{
		if (aDocument == null)
		{
			throw new IllegalArgumentException("Document provided is null.");
		}

		mDocument = aDocument;
		mDocument.setParent(this);
		recomputePreferredSize();
	}


	public Document getDocument()
	{
		return mDocument;
	}


	public Style getStyle(String aIdentifier)
	{
		return mPaintSyntaxParser.getStyle(aIdentifier);
	}


	public int getFontSize()
	{
		return mFontSize;
	}


	public void setFontPointSize(int aFontPointSize)
	{
		String[] keys = mPaintSyntaxParser.getStyleKeys();
		for (int i = 0; i < keys.length; i++)
		{
			mPaintSyntaxParser.getStyle(keys[i]).setFontSize(aFontPointSize);
		}
	}


	public int getFontPointSize()
	{
		return mPaintSyntaxParser.getStyle(SyntaxParser.WHITESPACE).getFont().getSize();
	}


	public void setSelectionStart(int aColumn, int aRow)
	{
		if (mSelectionStart == null)
		{
			mSelectionStart = new Point(includeTabsInOffset(aColumn, aRow), aRow);
		}
		else
		{
			mSelectionStart.move(includeTabsInOffset(aColumn, aRow), aRow);
		}
	}


	public Point getSelectionStart()
	{
		if (mSelectionStart == null)
		{
			return null;
		}
		return new Point(removeTabsFromOffset(mSelectionStart.x, mSelectionStart.y), mSelectionStart.y);
	}


	protected void setSelectionStartUnmodified(Point aPoint)
	{
		mSelectionStart = aPoint;
	}


	protected Point getSelectionStartUnmodified()
	{
		return mSelectionStart;
	}


	public void setSelectionEnd(int aColumn, int aRow)
	{
		if (mSelectionEnd == null)
		{
			mSelectionEnd = new Point(includeTabsInOffset(aColumn, aRow), aRow);
		}
		else
		{
			mSelectionEnd.move(includeTabsInOffset(aColumn, aRow), aRow);
		}
	}


	public Point getSelectionEnd()
	{
		if (mSelectionEnd == null)
		{
			return null;
		}
		return new Point(removeTabsFromOffset(mSelectionEnd.x, mSelectionEnd.y), mSelectionEnd.y);
	}


	protected void setSelectionEndUnmodified(Point aPoint)
	{
		mSelectionEnd = aPoint;
	}


	protected Point getSelectionEndUnmodified()
	{
		return mSelectionEnd;
	}


	public void replaceSelection(String aNewText)
	{
		if (!isTextSelected())
		{
			return;
		}

		mDocument.beginUndoableEdit(new UndoableEdit(this, "Replace selection"));

		Point selectionStart = mSelectionStart;
		Point selectionEnd = mSelectionEnd;

		if (selectionStart.y != selectionEnd.y)
		{
			if (selectionStart.y > selectionEnd.y)
			{
				Point temp = selectionStart;
				selectionStart = selectionEnd;
				selectionEnd = temp;
			}

			if (mRectangularSelection)
			{
				int x0 = Math.min(selectionStart.x, selectionEnd.x);
				int x1 = Math.max(selectionStart.x, selectionEnd.x);

				if (x0 == x1)
				{
					x1++;
				}

				for (int y = selectionStart.y; y <= selectionEnd.y; y++)
				{
					int tempX0 = removeTabsFromOffset(x0, y);
					int tempX1 = removeTabsFromOffset(x1, y);

					//String s = mDocument.get(y);

					int lineLength = mDocument.getLineLength(y);

					if (tempX0 == tempX1 && tempX0 < lineLength)
					{
						tempX1++;
					}

					if (lineLength >= tempX1)
					{
						//mDocument.set(y, s.substring(0, tempX0) + s.substring(tempX1));
						mDocument.removeSpan(y, tempX0, tempX1);
					}
					else if (lineLength >= tempX0)
					{
						//mDocument.set(y, s.substring(0, tempX0));
						mDocument.removeSpan(y, tempX0);
					}
				}
			}
			else
			{
				//String ss = mDocument.get(selectionStart.y).substring(0, startOffset) + mDocument.get(selectionEnd.y).substring(endOffset);
				//mDocument.set(selectionStart.y, ss);
				//for (int y = selectionStart.y + 1; y <= selectionEnd.y; y++)
				//{
				//	mDocument.remove(selectionStart.y + 1);
				//}

				int startOffset = removeTabsFromOffset(selectionStart.x, selectionStart.y);
				int endOffset = removeTabsFromOffset(selectionEnd.x, selectionEnd.y);
				//mDocument.removeRegion(selectionStart.y, startOffset, selectionEnd.y, endOffset);

				mDocument.removeSpan(selectionStart.y, startOffset);
				mDocument.removeSpan(selectionEnd.y, 0, endOffset);
				mDocument.concatLines(selectionStart.y, selectionEnd.y);

				for (int y = selectionStart.y + 1; y <= selectionEnd.y; y++)
				{
					mDocument.removeLine(selectionStart.y + 1);
				}
			}
		}
		else
		{
			if (mRectangularSelection && mSelectionStart.x == mSelectionEnd.x)
			{
				mSelectionEnd.x++;
			}

			if (selectionStart.x > selectionEnd.x)
			{
				Point temp = selectionStart;
				selectionStart = selectionEnd;
				selectionEnd = temp;
			}

			//mDocument.set(selectionStart.y, mDocument.get(selectionStart.y).substring(0, removeTabsFromOffset(selectionStart.x, selectionStart.y)) + mDocument.get(selectionEnd.y).substring(removeTabsFromOffset(selectionEnd.x, selectionEnd.y)));

			int startOffset = removeTabsFromOffset(selectionStart.x, selectionStart.y);
			int endOffset = removeTabsFromOffset(selectionEnd.x, selectionStart.y);
			mDocument.removeSpan(selectionStart.y, startOffset, endOffset);
		}

		mSelectionStart = null;
		mSelectionEnd = null;
		mRectangularSelection = false;

		mCaret.moveAbsolute(removeTabsFromOffset(selectionStart.x, selectionStart.y), selectionStart.y, false, true, true);

		insertText(aNewText);

		mDocument.commitUndoableEdit();
	}


	public boolean isTextSelected()
	{
		return mSelectionStart != null && mSelectionEnd != null && (mRectangularSelection || !mSelectionStart.equals(mSelectionEnd));
	}


	public boolean isRectangularSelection()
	{
		return mRectangularSelection;
	}


	public void setRectangularSelection(boolean aState)
	{
		mRectangularSelection = aState;
	}


	public void insertText(String aText)
	{
		insertText(aText, false);

		recomputePreferredSize();
	}


	public void insertText(String aText, boolean aRectangularText)
	{
		if (aText.length() == 0)
		{
			return;
		}

		mDocument.beginUndoableEdit(new UndoableEdit(this, "insertText"));

		StringTokenizer stringTokenizer = new StringTokenizer(new StringReader(aText), '\n');
		Point caret = new Point();

		if (isTextSelected() && mRectangularSelection)
		{
			Point selectionStart = getSelectionStart();
			Point selectionEnd = getSelectionEnd();

			if (selectionStart.y > selectionEnd.y)
			{
				Point temp = selectionStart;
				selectionStart = selectionEnd;
				selectionEnd = temp;
			}

			mSelectionStart = null;
			mSelectionEnd = null;

			mCaret.moveAbsolute(selectionStart.x, selectionStart.y, false, false, true);

			int left = selectionStart.x < selectionEnd.x ? selectionStart.x : selectionEnd.x;

			for (int y = selectionStart.y; y <= selectionEnd.y; y++)
			{
				String paste = "";

				if (stringTokenizer.hasNext())
				{
					paste = stringTokenizer.next();

					while (paste.indexOf("\r") > 0)
					{
						paste = paste.substring(0, paste.indexOf("\r")) + paste.substring(paste.indexOf("\r") + 1);
					}
					while (paste.indexOf("\n") > 0)
					{
						paste = paste.substring(0, paste.indexOf("\n")) + paste.substring(paste.indexOf("\n") + 1);
					}
				}

				if (y < mDocument.getLineCount())
				{
					caret.y = y;
				}
				else
				{
					mDocument.appendLine("");
					caret.y = y = mDocument.getLineCount() - 1;
				}

				int lineLength = mDocument.getLineLength(y);

				int right = selectionStart.x > selectionEnd.x ? selectionStart.x : selectionEnd.x;

				if (left == right && right < lineLength)
				{
					right++;
				}

				if (lineLength >= right)
				{
					mDocument.replaceSpan(y, left, right, paste);
					caret.x = left + paste.length();
				}
				else if (lineLength > left)
				{
					mDocument.replaceSpan(y, left, paste);
					caret.x = left + paste.length();
				}
				else
				{
					mDocument.replaceLine(y, paste);
					caret.x = paste.length();
				}
			}
		}
		else
		{
			if (isTextSelected())
			{
				replaceSelection("");
			}

			String paste;
			boolean firstLine = true;
			String trailer = "";

			for (int y = mCaret.getCharacterPosition().y; stringTokenizer.hasNext(); y++)
			{
				paste = stringTokenizer.next();

				while (paste.indexOf("\r") > 0)
				{
					paste = paste.substring(0, paste.indexOf("\r")) + paste.substring(paste.indexOf("\r") + 1);
				}
				while (paste.indexOf("\n") > 0)
				{
					paste = paste.substring(0, paste.indexOf("\n")) + paste.substring(paste.indexOf("\n") + 1);
				}

				if (y < mDocument.getLineCount())
				{
					caret.y = y;
				}
				else
				{
					mDocument.appendLine("");
					caret.y = y = mDocument.getLineCount() - 1;
				}

				int lineLength = mDocument.getLineLength(y);

				if (aRectangularText)
				{
					int x = mCaret.getCharacterPosition().x;
					if (x >= lineLength)
					{
						mDocument.appendSpan(y, paste);
						caret.x = mDocument.getLineLength(y);
					}
					else
					{
						mDocument.insertSpan(y, x, paste);
						caret.x = x + paste.length();
					}
				}
				else
				{
					if (firstLine)
					{
						int x = mCaret.getCharacterPosition().x;
						if (x >= lineLength)
						{
							mDocument.appendSpan(y, paste);
							caret.x = lineLength + paste.length();
						}
						else
						{
							if (stringTokenizer.hasNext())
							{
								trailer = mDocument.getSpan(y, x);
								mDocument.replaceSpan(y, x, paste);
								caret.x = x + paste.length();
							}
							else
							{
								mDocument.insertSpan(y, x, paste);
								caret.x = x + paste.length();
							}
						}
						firstLine = false;
					}
					else
					{
						if (stringTokenizer.hasNext())
						{
							mDocument.insertLine(y, paste);
						}
						else
						{
							mDocument.insertLine(y, paste + trailer);
						}
						caret.x = paste.length();
					}
					caret.y = y;
				}
			}
		}

		mDocument.commitUndoableEdit();

		recomputePreferredSize();
		mCaret.moveAbsolute(caret.x, caret.y, false, false, true);
		mCaret.makePreferredPosition();
		mRectangularSelection = false;
	}


	@Override
	public Dimension getMinimumSize()
	{
		return new Dimension(0, getFontHeight() + mLineSpacing);
	}


	@Override
	public Dimension getPreferredSize()
	{
		if (mPreferredSize != null)
		{
			return mPreferredSize;
		}

		int w = mDocument.getLongestLineLength() * getStyle(SyntaxParser.WHITESPACE).getCharWidth('m');

		mPreferredSize = new Dimension(w + mMargins.left + mMargins.right, mDocument.getLineCount() * (getFontHeight() + mLineSpacing) + mMargins.top + mMargins.bottom);

		return mPreferredSize;
	}


	@Override
	public void paintComponent(Graphics aGraphics)
	{
		Graphics2D g = (Graphics2D) aGraphics;

		if (mAntialiase != null)
		{
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, mAntialiase);
		}

		if (mRequestFocus)
		{
			requestFocusInWindow();
			mRequestFocus = false;
		}

		boolean optimizeTokens = !mWhitespaceSymbolEnabled && getStyle(SyntaxParser.WHITESPACE).getBackground().equals(getBackground());
		boolean optimizeWhitespace = !mWhitespaceSymbolEnabled;

		Rectangle clipBounds = g.getClipBounds();
		int firstRow = clipBounds.y / (getFontHeight() + mLineSpacing);
		int lastRow = (clipBounds.y + clipBounds.height) / (getFontHeight() + mLineSpacing) - 1 + 2; // TODO: m�ste rita tv� extra rader n�r sidan scrollas annars det ett h�l. scrollRectToVisible metoden verkar inte skicka r�tt clipbounds.

		int lineCount = mDocument.getLineCount();
		if (firstRow >= lineCount)
		{
			firstRow = lineCount - 1;
		}
		if (lastRow >= lineCount)
		{
			lastRow = lineCount - 1;
		}
		if (lastRow < firstRow)
		{
			lastRow = firstRow;
		}

		mPaintSyntaxParser.initialize(mDocument, firstRow);

		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());

		for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++)
		{
			boolean highlightRow = mHighlightText != null && mDocument.getLine(rowIndex).contains(mHighlightText);

			List<Token> tokens = mPaintSyntaxParser.parse(mDocument, rowIndex, optimizeTokens && !highlightRow, optimizeWhitespace);
			int positionX = 0;
			int y = getFontAscent() + rowIndex * (getFontHeight() + mLineSpacing) + mMargins.top;

			Token token = null;
			for (Token nextToken : tokens)
			{
				token = nextToken;

				if (positionX < clipBounds.x + clipBounds.width)
				{
					Point selectionIntersect = !isTextSelected() ? null : getSelectionIntersect(rowIndex, token);
					int len = token.length();

					if (selectionIntersect != null)
					{
						if (selectionIntersect.x > 0)
						{
							positionX = paintString(g, false, positionX, y, 0, selectionIntersect.x, clipBounds, highlightRow, token);
						}

						positionX = paintString(g, true, positionX, y, selectionIntersect.x, selectionIntersect.y, clipBounds, highlightRow, token);

						if (selectionIntersect.y < len)
						{
							positionX = paintString(g, false, positionX, y, selectionIntersect.y, len, clipBounds, highlightRow, token);
						}
					}
					else
					{
						positionX = paintString(g, false, positionX, y, -1, -1, clipBounds, highlightRow, token);
					}
				}
			}

			if (token != null)
			{
				boolean selectionIntersected = isTextSelected() && intersectSelection(mDocument.getLineLength(rowIndex), rowIndex);

				if (selectionIntersected || mLineBreakSymbolEnabled)
				{
					Style fontStyle = mPaintSyntaxParser.getStyle(SyntaxParser.LINEBREAK);
					Style colorStyle = token.isComment() ? token.getStyle() : fontStyle;

					int w = fontStyle.getStringWidth(mLineBreakSymbol);

					if (highlightRow || colorStyle.getBackground() != Color.WHITE)
					{
						g.setColor(colorStyle.getBackground());
						g.fillRect(positionX + mMargins.left, y - getFontAscent(), w, getFontHeight());
					}

					Color fontColor = colorStyle.getForeground();
					if (selectionIntersected)
					{
						fontColor = mPaintSyntaxParser.getStyle(SyntaxParser.SELECTION).getBackground();
					}

					g.setFont(fontStyle.getFont());
					g.setColor(fontColor);
					g.drawString(mLineBreakSymbol, positionX + mMargins.left, y);

					positionX += fontStyle.getStringWidth(mLineBreakSymbol);
				}

				if (positionX + mMargins.left + mMargins.right > getWidth())
				{
					mPreferredSize = null;
					revalidate();
				}
			}
		}

		mCaret.paintCaret(g);
	}


	private int paintString(Graphics g, boolean aIsSelection, int positionX, int y, int aTokenOffset, int aTokenLength, Rectangle aClipBounds, boolean aHighlightRow, Token aToken)
	{
		if (aTokenOffset != -1 && aTokenOffset == aTokenLength)
		{
			return positionX;
		}

		String s = aToken.getToken();

		if (aTokenOffset != -1)
		{
			s = s.substring(aTokenOffset, aTokenLength);
		}

		boolean whitespace = s.charAt(0) == '\t';
		if (!whitespace)
		{
			for (int i = 0; i < s.length(); i++)
			{
				whitespace = s.charAt(i) == ' ';
				if (!whitespace)
				{
					break;
				}
			}
		}

		if (whitespace)
		{
			int previousPositionX = positionX;
			positionX = incrementWhitespace(positionX, s);

			if (positionX >= aClipBounds.x)
			{
				Style style = aIsSelection ? mPaintSyntaxParser.getStyle(SyntaxParser.SELECTION) : aToken.getStyle();

				if (aIsSelection || aHighlightRow || style.getBackground() != Color.WHITE)
				{
					g.setColor(style.getBackground());
					g.fillRect(previousPositionX + mMargins.left, y - getFontAscent(), positionX - previousPositionX, getFontHeight());
				}

				if (DEBUG_GRAPHICS)
				{
					g.setColor(new Color(192, 192, 192));
					g.drawRect(previousPositionX + mMargins.left, y - getFontAscent(), positionX - previousPositionX, getFontHeight());
				}

				if (mWhitespaceSymbolEnabled)
				{
					g.setColor((aToken.isComment() && !aIsSelection ? aToken.getStyle() : style).getForeground());

					int h = mMargins.left + (previousPositionX + positionX) / 2;
					int v = y - getFontAscent() + getFontHeight() / 2;
					if (s.equals(" "))
					{
						g.drawLine(h, v, h, v);
					}
					else
					{
						g.drawLine(h - 2, v, h + 2, v);
						g.drawLine(h + 2, v, h, v - 2);
						g.drawLine(h + 2, v, h, v + 2);
					}
				}
			}
		}
		else
		{
			Style tokenStyle = aToken.getStyle();
			int w = tokenStyle.getStringWidth(s);

			if (positionX + w >= aClipBounds.x)
			{
				Style xxxxxxxcolorStyle = aIsSelection ? mPaintSyntaxParser.getStyle(SyntaxParser.SELECTION) : tokenStyle;
				Style colorStyle = /*aIsSelection ? mPaintSyntaxParser.getStyle(SyntaxParser.SELECTION) :*/ tokenStyle;

				if (aHighlightRow && tokenStyle.isSupportHighlight() && xxxxxxxcolorStyle.isBackgroundOptional() && s.equalsIgnoreCase(mHighlightText))
				{
					g.setColor(getStyle(SyntaxParser.HIGHLIGHT).getBackground());
					g.fillRect(positionX + mMargins.left, y - getFontAscent(), w, getFontHeight());
				}
				else if (xxxxxxxcolorStyle.getBackground() != Color.WHITE)
				{
					g.setColor(xxxxxxxcolorStyle.getBackground());
					g.fillRect(positionX + mMargins.left, y - getFontAscent(), w, getFontHeight());
				}

				if (DEBUG_GRAPHICS)
				{
					g.setColor(new Color(192, 192, 192));
					g.drawRect(positionX + mMargins.left, y - getFontAscent(), w, getFontHeight());
				}

				g.setFont(tokenStyle.getFont());
				g.setColor(colorStyle.getForeground());
				g.drawString(s, positionX + mMargins.left, y);

				if (tokenStyle.isUnderlined())
				{
					g.drawLine(positionX + mMargins.left, y + 1, positionX + mMargins.left + w, y + 1);
				}
			}

			positionX += w;
		}

		return positionX;
	}


	boolean intersectSelection(int aColumn, int aRow)
	{
		if (!isTextSelected())
		{
			return false;
		}

		Point selectionStart = mSelectionStart;
		Point selectionEnd = mSelectionEnd;

		if (selectionStart.y > selectionEnd.y || (selectionStart.y == selectionEnd.y && selectionStart.x > selectionEnd.x))
		{
			Point temp = selectionStart;
			selectionStart = selectionEnd;
			selectionEnd = temp;
		}

		if (aRow < selectionStart.y)
		{
			return false;
		}
		if (aRow > selectionEnd.y)
		{
			return false;
		}

		if (mRectangularSelection)
		{
			int adjust = selectionStart.x == selectionEnd.x ? 1 : 0;

			if (aColumn < removeTabsFromOffset(selectionStart.x, selectionStart.y))
			{
				return false;
			}
			if (aColumn >= removeTabsFromOffset(selectionEnd.x, selectionEnd.y) + adjust)
			{
				return false;
			}

			return true;
		}
		else
		{
			if (aRow == selectionStart.y && aColumn < removeTabsFromOffset(selectionStart.x, selectionStart.y))
			{
				return false;
			}
			if (aRow == selectionEnd.y && aColumn >= removeTabsFromOffset(selectionEnd.x, selectionEnd.y))
			{
				return false;
			}

			return true;
		}
	}


	private Point getSelectionIntersect(int aRow, Token aToken)
	{
		Point selectionStart = new Point(mSelectionStart);
		Point selectionEnd = new Point(mSelectionEnd);

		if (selectionStart.y > selectionEnd.y || (selectionStart.y == selectionEnd.y && selectionStart.x > selectionEnd.x))
		{
			Point temp = selectionStart;
			selectionStart = selectionEnd;
			selectionEnd = temp;
		}

		if (aRow < selectionStart.y || aRow > selectionEnd.y) // token row outside selection
		{
			return null;
		}

		int tokenStart = aToken.getOffset();
		int tokenEnd = aToken.getOffset()+aToken.length();

		int startSelection, endSelection;
		boolean startIntersect, endIntersect;

		if (mRectangularSelection)
		{
			if (selectionStart.x > selectionEnd.x)
			{
				Point temp = selectionStart;
				selectionStart = selectionEnd;
				selectionEnd = temp;
			}

			int adjust = selectionStart.x == selectionEnd.x ? 1 : 0;

			selectionStart.x = removeTabsFromOffset(selectionStart.x, aRow);
			selectionEnd.x = removeTabsFromOffset(selectionEnd.x, aRow);

			startSelection = selectionStart.x;
			endSelection = selectionEnd.x + adjust;

			if (tokenEnd < startSelection || tokenStart > endSelection) // token outside selection
			{
				return null;
			}

			startIntersect = tokenStart >= selectionStart.x && tokenStart <= selectionEnd.x;
			endIntersect = tokenEnd >= selectionStart.x && tokenEnd <= selectionEnd.x;
		}
		else
		{
			if (aRow > selectionStart.y && aRow < selectionEnd.y) // entire row inside selection
			{
				mSelectionTempPoint.move(0, tokenEnd - tokenStart);
				return mSelectionTempPoint;
			}

			selectionStart.x = removeTabsFromOffset(selectionStart.x, aRow);
			selectionEnd.x = removeTabsFromOffset(selectionEnd.x, aRow);

			startSelection = selectionStart.y == aRow ? selectionStart.x : tokenStart;
			endSelection = (selectionEnd.y == aRow ? selectionEnd.x : tokenEnd);

			startIntersect = tokenStart >= startSelection && tokenStart <= endSelection;
			endIntersect = tokenEnd >= startSelection && tokenEnd <= endSelection;
		}

		if (startIntersect && endIntersect) // token entirely inside selection
		{
			mSelectionTempPoint.move(0, tokenEnd - tokenStart);
			return mSelectionTempPoint;
		}

		if (startIntersect && !endIntersect) // begining of token inside selection
		{
			mSelectionTempPoint.move(0, endSelection - tokenStart);
			return mSelectionTempPoint;
		}

		if (!startIntersect && endIntersect) // end of token inside selection
		{
			mSelectionTempPoint.move(startSelection - tokenStart, tokenEnd - tokenStart);
			return mSelectionTempPoint;
		}

		if (startSelection > tokenStart && endSelection < tokenEnd) // selection entirely inside token
		{
			mSelectionTempPoint.move(startSelection - tokenStart, endSelection - tokenStart);
			return mSelectionTempPoint;
		}

		return null;
	}


	// TODO: possible bug
	int removeTabsFromOffset(int aTargetVirtualOffset, int aRow)
	{
		String sourceLine = mDocument.getLine(aRow);

		int virtualOffset = 0;
		int characterOffset = 0;
		int offset = aTargetVirtualOffset;
		while (aTargetVirtualOffset > virtualOffset && characterOffset < sourceLine.length())
		{
			if (sourceLine.charAt(characterOffset) == '\t')
			{
				offset += (virtualOffset % mTabSize) - mTabSize + 1;
				if (aTargetVirtualOffset < virtualOffset)
				{
					break;
				}
				virtualOffset = mTabSize * (virtualOffset / mTabSize) + mTabSize;
			}
			else
			{
				virtualOffset++;
			}
			characterOffset++;
		}

		return characterOffset;
	}


	int includeTabsInOffset(int aOffset, int aRow)
	{
		String sourceLine = mDocument.getLine(aRow);

		int x = 0;
		int a = 0;
		int o = aOffset;
		while (o > x && x < sourceLine.length())
		{
			if (sourceLine.charAt(x) == '\t')
			{
				int b = mTabSize - (a % mTabSize);
				a += b;
				aOffset += b - 1;
			}
			else
			{
				a++;
			}
			x++;
		}

		return aOffset;
	}


	int findTabbedOffset(int aDesiredVirtualOffset, int aRow)
	{
		String sourceLine = mDocument.getLine(aRow);

		int characterOffset = 0;
		int virtualOffset = 0;
		while (aDesiredVirtualOffset > virtualOffset && characterOffset < sourceLine.length())
		{
			if (sourceLine.charAt(characterOffset) == '\t')
			{
				int b = mTabSize - (virtualOffset % mTabSize);
				virtualOffset += b;
				if (virtualOffset > aDesiredVirtualOffset)
				{
					break;
				}
			}
			else
			{
				virtualOffset++;
			}
			characterOffset++;
		}

		return characterOffset;
	}


	private int incrementWhitespace(int aPositionX, String aWhitespace)
	{
		for (int i = 0, len = aWhitespace.length(); i < len; i++)
		{
			if (aWhitespace.charAt(i) == ' ')
			{
				aPositionX += getStyle(SyntaxParser.WHITESPACE).getCharWidth(' ');
			}
			else
			{
				int tabSizePixels = getStyle(SyntaxParser.WHITESPACE).getCharWidth(' ') * mTabSize;
				aPositionX = tabSizePixels * (int) Math.ceil((aPositionX + 1.0) / (double) tabSizePixels);
			}
		}

		return aPositionX;
	}


	Point getSourceOffset(Point aMousePoint)
	{
		int y = (aMousePoint.y - mMargins.top) / (getFontHeight() + mLineSpacing);

		y = Math.min(Math.max(y, 0), mDocument.getLineCount() - 1);

		String sourceLine = mDocument.getLine(y);

		if (isFontMonospaced())
		{
			int o = (int) Math.round((aMousePoint.x - mMargins.left) / (double) mPaintSyntaxParser.getStyle(SyntaxParser.SELECTION).getCharWidth('m'));

			int a = 0;
			int x = 0;
			while (o > x && x < sourceLine.length())
			{
				if (sourceLine.charAt(x) == '\t')
				{
					o += (a % mTabSize) - mTabSize + 1;
					a = mTabSize * (a / mTabSize) + mTabSize;
					if (o <= x)
					{
						break;
					}
				}
				else
				{
					a++;
				}
				x++;
			}

			return new Point(x, y);
		}

		mOffsetSyntaxParser.initialize(mDocument, y);
		int positionX = 0;
		int offsetX = -1;

		for (Token token : mOffsetSyntaxParser.parse(mDocument, y, true, false))
		{
			String s = token.getToken();

			if (Character.isWhitespace(s.charAt(0)))
			{
				positionX = incrementWhitespace(positionX, s);

				if (positionX + mMargins.left >= aMousePoint.x)
				{
					offsetX = token.getOffset()+token.length() - 1;
					break;
				}
			}
			else
			{
				Style style = token.getStyle();
				int w = style.getStringWidth(s);

				if (positionX + mMargins.left + w >= aMousePoint.x)
				{
					int min = 0;
					int max = s.length();
					int mid = max / 2;

					while (max - min > 1)
					{
						w = positionX + style.getStringWidth(s.substring(0, mid));

						if (w > aMousePoint.x)
						{
							max = mid;
						}
						else
						{
							min = mid;
						}
						mid = (min + max) / 2;
					}

					offsetX = token.getOffset() + mid;

					if (aMousePoint.x - positionX - mMargins.left - style.getStringWidth(s.substring(0, mid)) > style.getCharWidth(sourceLine.charAt(offsetX)) / 2)
					{
						offsetX++;
					}

					break;
				}

				positionX += w;
			}
		}

		if (offsetX == -1)
		{
			offsetX = sourceLine.length();
		}

		return new Point(offsetX, y);
	}


	int getPixelOffset(int aCharacterOffset, int aRow)
	{
		mPixelOffsetSyntaxParser.initialize(mDocument, aRow);
		int positionX = 0;

		for (Token token : mPixelOffsetSyntaxParser.parse(mDocument, aRow, false, false))
		{
			String s = token.getToken();

			if (Character.isWhitespace(s.charAt(0)))
			{
				if (token.getOffset() + token.length() > aCharacterOffset)
				{
					break;
				}
				else
				{
					positionX = incrementWhitespace(positionX, s);
				}
			}
			else
			{
				if (token.getOffset()+token.length() >= aCharacterOffset)
				{
					int len = s.length() - (token.getOffset()+token.length() - aCharacterOffset);
					if (len > 0)
					{
						positionX += token.getStyle().getStringWidth(s.substring(0, len));
					}
					break;
				}
				else
				{
					positionX += token.getStyle().getStringWidth(s);
				}
			}
		}

		return positionX;
	}


	private void recomputePreferredSize()
	{
		if (mPreferredSize == null || mDocument.getLineCount() * (getFontHeight() + mLineSpacing) + mMargins.top + mMargins.bottom != mPreferredSize.height)
		{
			revalidate();
		}
	}


	@Override
	public void revalidate()
	{
		mPreferredSize = null;
		super.revalidate();
	}


	public void setText(String aText)
	{
		mDocument.beginUndoableEdit(new UndoableEdit(this, "Set text"));

		try
		{
			mDocument.load(new LineNumberReader(new StringReader(aText)));
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}

		mDocument.commitUndoableEdit();

		recomputePreferredSize();
	}


	public StringBuffer getText()
	{
		return getText("\n");
	}


	StringBuffer getText(String aLineBreak)
	{
		StringBuffer buffer = new StringBuffer();

		for (int i = 0, size = mDocument.getLineCount(); i < size; i++)
		{
			if (i < size - 1)
			{
				buffer.append(mDocument.getLine(i)).append(aLineBreak);
			}
			else
			{
				buffer.append(mDocument.getLine(i));
			}
		}

		return buffer;
	}


	public StringBuffer getSelectedText()
	{
		String lineBreak = "\n";

		StringBuffer selection = new StringBuffer();

		if (isTextSelected())
		{
			Point selectionStart = getSelectionStart();
			Point selectionEnd = getSelectionEnd();

			if (selectionStart.y == selectionEnd.y)
			{
				String s = mDocument.getLine(mSelectionStart.y);
				int min = Math.min(selectionStart.x, selectionEnd.x);
				int max = Math.max(selectionStart.x, selectionEnd.x);
				selection.append(s.substring(min, max));
			}
			else
			{
				if (selectionStart.y > selectionEnd.y)
				{
					Point temp = selectionStart;
					selectionStart = selectionEnd;
					selectionEnd = temp;
				}

				if (mRectangularSelection)
				{
					int left = selectionStart.x < selectionEnd.x ? selectionStart.x : selectionEnd.x;
					int right = selectionStart.x > selectionEnd.x ? selectionStart.x : selectionEnd.x;

					for (int y = selectionStart.y; y <= selectionEnd.y; y++)
					{
						String s = mDocument.getLine(y);

						if (s.length() >= right)
						{
							s = s.substring(left, right);
						}
						else if (s.length() > left)
						{
							s = s.substring(left);
						}

						if (y == selectionEnd.y)
						{
							selection.append(s);
						}
						else
						{
							selection.append(s).append(lineBreak);
						}
					}
				}
				else
				{
					selection.append(mDocument.getSpan(selectionStart.y, selectionStart.x)).append(lineBreak);

					for (int y = selectionStart.y + 1; y < selectionEnd.y; y++)
					{
						selection.append(mDocument.getLine(y)).append(lineBreak);
					}

					selection.append(mDocument.getLine(selectionEnd.y).substring(0, selectionEnd.x));
				}
			}
		}

		return selection;
	}


	int getFontHeight()
	{
		return mPaintSyntaxParser.getStyle(SyntaxParser.WHITESPACE).getFontHeight();
	}


	int getFontAscent()
	{
		return mPaintSyntaxParser.getStyle(SyntaxParser.WHITESPACE).getFontAscent();
	}


	boolean isFontMonospaced()
	{
		return mPaintSyntaxParser.getStyle(SyntaxParser.WHITESPACE).isFontMonospaced();
	}


	void keyTyped(char aKeyChar)
	{
		if (isTextSelected())
		{
			replaceSelection(Character.toString(aKeyChar));
		}
		else
		{
			mDocument.beginUndoableEdit(new UndoableEdit(this, "Key typed"));

			//String s = mDocument.getLine(mCaret.getCharacterPosition().y);
			int x = mCaret.getCharacterPosition().x;
			int y = mCaret.getCharacterPosition().y;
			int lineLength = mDocument.getLineLength(y);

			if (mOverwriteTextEnabled)
			{
				if (mCaret.getCharacterPosition().x == lineLength)
				{
					//s += aKeyChar;
					mDocument.appendSpan(y, Character.toString(aKeyChar));
				}
				else
				{
					//s = s.substring(0, mCaret.getCharacterPosition().x) + aKeyChar + s.substring(mCaret.getCharacterPosition().x + 1);
					mDocument.replaceSpan(y, x, x + 1, Character.toString(aKeyChar));
				}
			}
			else
			{
				if (mCaret.getCharacterPosition().x == lineLength)
				{
					//s += aKeyChar;
					mDocument.appendSpan(y, Character.toString(aKeyChar));
				}
				else
				{
					//s = s.substring(0, mCaret.getCharacterPosition().x) + aKeyChar + s.substring(mCaret.getCharacterPosition().x);
					mDocument.insertSpan(y, x, Character.toString(aKeyChar));
				}
			}
			//mDocument.set(mCaret.getCharacterPosition().y, s);
			mCaret.moveRelative(1, 0, false, true, true);

			mDocument.commitUndoableEdit();
		}
	}


	private void indentOutdentSelection(boolean aOutdent)
	{
		mDocument.beginUndoableEdit(new UndoableEdit(this, "Indent/Outdent selection"));

		if (isTextSelected() && mSelectionStart.y != mSelectionEnd.y)
		{
			int topY, bottomY, topX, bottomX;
			if (mSelectionStart.y < mSelectionEnd.y)
			{
				topX = mSelectionStart.x;
				topY = mSelectionStart.y;
				bottomX = mSelectionEnd.x;
				bottomY = mSelectionEnd.y;
			}
			else
			{
				topX = mSelectionEnd.x;
				topY = mSelectionEnd.y;
				bottomX = mSelectionStart.x;
				bottomY = mSelectionStart.y;
			}

			for (int y = topY; y < bottomY; y++)
			{
				indentString(y, aOutdent);
			}

			if (mRectangularSelection)
			{
				mRectangularSelection = false;
				indentString(bottomY, aOutdent);

				if (mSelectionStart.y < mSelectionEnd.y)
				{
					mSelectionStart.x = 0;
					mSelectionEnd.x = includeTabsInOffset(mDocument.getLineLength(bottomY), bottomY);
					mCaret.moveRelative(mDocument.getLineLength(bottomY) - mCaret.getCharacterPosition().x, 0, false, false, true);
				}
				else
				{
					mSelectionStart.x = includeTabsInOffset(mDocument.getLineLength(bottomY), bottomY);
					mSelectionEnd.x = 0;
					mCaret.moveRelative(-mCaret.getCharacterPosition().x, 0, false, false, true);
				}
			}
			else
			{
				if (mSelectionStart.y < mSelectionEnd.y)
				{
					if (bottomX > 0)
					{
						indentString(bottomY, aOutdent);
						mSelectionEnd.x = includeTabsInOffset(mDocument.getLineLength(bottomY), bottomY);
					}
					mSelectionStart.x = 0;
				}
				else
				{
					if (bottomX > 0)
					{
						indentString(bottomY, aOutdent);
						mSelectionStart.x = includeTabsInOffset(mDocument.getLineLength(bottomY), bottomY);
					}
					mSelectionEnd.x = 0;
				}
				mCaret.moveRelative(-mCaret.getCharacterPosition().x, 0, false, false, true);
			}
		}

		mDocument.commitUndoableEdit();
	}


	private void indentString(int aLineIndex, boolean aOutdent)
	{
		if (aOutdent)
		{
			int lineLength = mDocument.getLineLength(aLineIndex);

			if (lineLength > 0)
			{
				if (mDocument.getCharAt(aLineIndex, 0) == '\t')
				{
					//aSource = aSource.substring(1);
					mDocument.removeSpan(aLineIndex, 0, 1);
				}
				else
				{
					int i = 0;
					for (; i < 4 && lineLength > i && mDocument.getCharAt(aLineIndex, i) == ' '; i++)
					{
					}
					//aSource = aSource.substring(i);
					mDocument.removeSpan(aLineIndex, 0, i);
				}
			}
		}
		else
		{
			//aSource = "\t" + aSource;
			mDocument.insertSpan(aLineIndex, 0, "\t");
		}
	}


	public void deleteLine()
	{
		mDocument.beginUndoableEdit(new UndoableEdit(this, "Delete line"));

		if (isTextSelected())
		{
			replaceSelection("");
		}
		else
		{
			if (mDocument.getLineCount() == 1)
			{
				mDocument.removeSpan(mCaret.getCharacterPosition().y, 0);
			}
			else
			{
				mDocument.removeLine(mCaret.getCharacterPosition().y);
				if (mCaret.getCharacterPosition().y == mDocument.getLineCount())
				{
					mCaret.moveRelative(-mCaret.getCharacterPosition().x, -1, true, true, false);
				}
				else
				{
					mCaret.moveRelative(-mCaret.getCharacterPosition().x, 0, false, true, false);
				}
			}
		}

		mDocument.commitUndoableEdit();

		recomputePreferredSize();
		mCaret.makePreferredPosition();
		mCaret.forceVisibility();
		repaint();
	}


	public void cut()
	{
		if (!isTextSelected() && mAutoLineCopyCutEnabled)
		{
			setSelectionStart(0, mCaret.getCharacterPosition().y);
			setSelectionEnd(mDocument.getLineLength(mCaret.getCharacterPosition().y), mCaret.getCharacterPosition().y);
		}

		StringBuffer selection = getSelectedText();
		StringSelection stringSelection = new StringSelection(selection.toString());
		getToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
		mClipboardContent = mRectangularSelection ? selection.toString() : "";

		if (isTextSelected())
		{
			replaceSelection("");
		}
		repaint();
	}


	public void copy()
	{
		if (!isTextSelected() && mAutoLineCopyCutEnabled)
		{
			setSelectionStart(0, mCaret.getCharacterPosition().y);
			setSelectionEnd(mDocument.getLineLength(mCaret.getCharacterPosition().y), mCaret.getCharacterPosition().y);
			repaint();
		}

		StringBuffer selection = getSelectedText();
		StringSelection stringSelection = new StringSelection(selection.toString());
		getToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
		mClipboardContent = mRectangularSelection ? selection.toString() : "";
	}


	public void paste()
	{
		try
		{
			String clipboardText = (String) (getToolkit().getSystemClipboard().getContents(this).getTransferData(DataFlavor.stringFlavor));
			insertText(clipboardText, clipboardText.equals(mClipboardContent));
		}
		catch (IllegalStateException e)
		{
			throw new IllegalStateException(e);
		}
		catch (UnsupportedFlavorException e)
		{
			throw new IllegalStateException(e);
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
		repaint();
	}


	public void selectAll()
	{
		setSelectionStart(0, 0);
		setSelectionEnd(mDocument.getLineLength(mDocument.getLineCount() - 1), mDocument.getLineCount() - 1);
		repaint();
	}


	public void deleteToken()
	{
		if (isTextSelected())
		{
			replaceSelection("");
			mCaret.makePreferredPosition();
			return;
		}

		mDocument.beginUndoableEdit(new UndoableEdit(this, "Delete token"));

		boolean status = false;

		Point caretPosition = mCaret.getCharacterPosition();
		mInputSyntaxParser.initialize(mDocument, caretPosition.y);
		for (Token token : mInputSyntaxParser.parse(mDocument, caretPosition.y, false, false))
		{
			int o = token.getOffset() + token.length();
			if (caretPosition.x >= token.getOffset() && caretPosition.x < o)
			{
				while (o < mDocument.getLineLength(caretPosition.y) && Character.isWhitespace(mDocument.getCharAt(caretPosition.y, o)))
				{
					o++;
				}

				if (o != caretPosition.x)
				{
					mDocument.removeSpan(caretPosition.y, caretPosition.x, o);
					status = true;
				}
				break;
			}
		}

		if (!status)
		{
			deleteNextCharacter();
		}

		mDocument.commitUndoableEdit();
	}


	public void deleteNextCharacter()
	{
		if (isTextSelected())
		{
			replaceSelection("");
			mCaret.makePreferredPosition();
			return;
		}

		mDocument.beginUndoableEdit(new UndoableEdit(this, "Delete next character"));

		int x = mCaret.getCharacterPosition().x;
		int y = mCaret.getCharacterPosition().y;

		if (x == mDocument.getLineLength(y))
		{
			if (y < mDocument.getLineCount() - 1)
			{
				mDocument.concatLines(y, y + 1);
				mDocument.removeLine(y + 1);
				recomputePreferredSize();
			}
		}
		else
		{
			mDocument.removeSpan(y, x, x + 1);
		}
		mCaret.makePreferredPosition();

		mDocument.commitUndoableEdit();
	}


	public void indent()
	{
		indentOutdentSelection(false);
	}


	public void outdent()
	{
		indentOutdentSelection(true);
	}


	public void moveCaretDocumentTop()
	{
		mCaret.moveAbsolute(0, 0, false, true, true);
		mCaret.makePreferredPosition();
	}


	public void moveCaretLineStart()
	{
		mCaret.moveAbsolute(0, mCaret.getCharacterPosition().y, false, true, true);
		mCaret.makePreferredPosition();
	}


	public void scrollPageUp()
	{
		int firstRow = Math.max(0, ((JViewport) getParent()).getViewPosition().y / (getFontHeight() + mLineSpacing) - getParent().getHeight() / (getFontHeight() + mLineSpacing));
		int lastRow = Math.min(mDocument.getLineCount(), firstRow + getParent().getHeight() / (getFontHeight() + mLineSpacing));
		scrollRectToVisible(new Rectangle(mMargins.left + mCaret.getPixelPosition().x, mMargins.top + (getFontHeight() + mLineSpacing) * firstRow, 1, (getFontHeight() + mLineSpacing) * (lastRow - firstRow)));
		mCaret.paintImmediately();
	}


	public void moveCaretPageUp()
	{
		mCaret.moveRelative(0, -getParent().getHeight() / (getFontHeight() + mLineSpacing) + 1, true, true, true);
	}


	public void scrollPageDown()
	{
		int firstRow = (int) Math.ceil(((JViewport) getParent()).getViewPosition().y / (double) (getFontHeight() + mLineSpacing)) + getParent().getHeight() / (getFontHeight() + mLineSpacing);
		int lastRow = Math.min(mDocument.getLineCount(), firstRow + getParent().getHeight() / (getFontHeight() + mLineSpacing)) + 1;
		scrollRectToVisible(new Rectangle(mMargins.left + mCaret.getPixelPosition().x, mMargins.top + (getFontHeight() + mLineSpacing) * firstRow, 1, (getFontHeight() + mLineSpacing) * (lastRow - firstRow)));
		mCaret.paintImmediately();
	}


	public void moveCaretPageDown()
	{
		mCaret.moveRelative(0, getParent().getHeight() / (getFontHeight() + mLineSpacing) - 1, true, true, true);
	}


	public void scrollLineUp()
	{
		int firstRow = Math.max(0, ((JViewport) getParent()).getViewPosition().y / (getFontHeight() + mLineSpacing) - 1);
		int lastRow = Math.min(mDocument.getLineCount(), firstRow + getParent().getHeight() / (getFontHeight() + mLineSpacing));
		scrollRectToVisible(new Rectangle(mMargins.left + mCaret.getPixelPosition().x, mMargins.top + (getFontHeight() + mLineSpacing) * firstRow, 1, (getFontHeight() + mLineSpacing) * (lastRow - firstRow)));
		mCaret.paintImmediately();
	}


	public void moveCaretLineUp()
	{
		mCaret.moveRelative(0, -1, true, true, true);
	}


	public void scrollLineDown()
	{
		int firstRow = 1 + (int) Math.ceil(((JViewport) getParent()).getViewPosition().y / (double) (getFontHeight() + mLineSpacing));
		int lastRow = Math.min(mDocument.getLineCount(), firstRow + getParent().getHeight() / (getFontHeight() + mLineSpacing)) + 1;
		scrollRectToVisible(new Rectangle(mMargins.left + mCaret.getPixelPosition().x, mMargins.top + (getFontHeight() + mLineSpacing) * firstRow, 1, (getFontHeight() + mLineSpacing) * (lastRow - firstRow)));
		mCaret.paintImmediately();
	}


	public void moveCaretLineDown()
	{
		mCaret.moveRelative(0, 1, true, true, true);
	}


	public void moveCaretPreviousToken()
	{
		int x = mCaret.getCharacterPosition().x;
		int y = mCaret.getCharacterPosition().y;

		if (x == 0)
		{
			if (y > 0)
			{
				mCaret.moveRelative(mDocument.getLineLength(y - 1), -1, false, true, true);
				mCaret.makePreferredPosition();
			}
			return;
		}

		mCaret.moveRelative(getPreviousTokenOffset(x, y)-x, 0, false, true, true);
	}


	public void moveCaretLeft()
	{
		if (mCaret.getCharacterPosition().x == 0)
		{
			if (mCaret.getCharacterPosition().y > 0)
			{
				mCaret.moveRelative(mDocument.getLineLength(mCaret.getCharacterPosition().y - 1), -1, false, true, true);
				mCaret.makePreferredPosition();
			}
		}
		else
		{
			mCaret.moveRelative(-1, 0, false, true, true);
		}
	}


	public void moveCaretNextToken()
	{
		int x = mCaret.getCharacterPosition().x;
		int y = mCaret.getCharacterPosition().y;

		if (x == mDocument.getLineLength(y))
		{
			if (y < mDocument.getLineCount() - 1)
			{
				mCaret.moveRelative(-x, 1, false, true, true);
				mCaret.makePreferredPosition();
			}
		}
		else
		{
			mCaret.moveRelative(getNextTokenOffset(x, y, true)-x, 0, false, true, true);
		}
	}


	public void moveCaretRight()
	{
		if (mCaret.getCharacterPosition().x == mDocument.getLineLength(mCaret.getCharacterPosition().y))
		{
			if (mCaret.getCharacterPosition().y < mDocument.getLineCount() - 1)
			{
				mCaret.moveRelative(-mCaret.getCharacterPosition().x, 1, false, true, true);
				mCaret.makePreferredPosition();
			}
		}
		else
		{
			mCaret.moveRelative(1, 0, false, true, true);
		}
	}


	public void moveCaretDocumentEnd()
	{
		mCaret.moveAbsolute(mDocument.getLineLength(mDocument.getLineCount() - 1), mDocument.getLineCount(), false, true, true);
		mCaret.makePreferredPosition();
	}


	public void moveCaretLineEnd()
	{
		int y = mCaret.getCharacterPosition().y;
		mCaret.moveAbsolute(mDocument.getLineLength(y), y, false, true, true);
		mCaret.makePreferredPosition();
	}


	public void deletePreviousCharacter()
	{
		if (isTextSelected())
		{
			replaceSelection("");
		}
		else
		{
			mDocument.beginUndoableEdit(new UndoableEdit(this, "Delete previous character"));

			int x = mCaret.getCharacterPosition().x;
			int y = mCaret.getCharacterPosition().y;

			if (x == 0)
			{
				if (y > 0)
				{
					int lineLength = mDocument.getLineLength(y - 1);

					mDocument.concatLines(y - 1, y);
					mDocument.removeLine(y);

					recomputePreferredSize();
					mCaret.moveRelative(lineLength - x, -1, false, true, true);
				}
			}
			else
			{
				mDocument.removeSpan(y, x - 1, x);
				mCaret.moveRelative(-1, 0, false, true, true);
			}

			mDocument.commitUndoableEdit();
		}
		mCaret.makePreferredPosition();
	}


	public void insertBreak()
	{
		if (isTextSelected())
		{
			replaceSelection("");
		}

		mDocument.beginUndoableEdit(new UndoableEdit(this, "Insert break"));

		int x = mCaret.getCharacterPosition().x;
		int y = mCaret.getCharacterPosition().y;

		String indent = "";
		if (mAutoIndentEnabled)
		{
			for (int i = 0, len = mDocument.getLineLength(y); i < len; i++)
			{
				char c = mDocument.getCharAt(y, i);
				if (c == ' ' || c == '\t')
				{
					indent += c;
				}
				else
				{
					break;
				}
			}
		}

		if (x == 0)
		{
			mDocument.insertLine(y, "");
			recomputePreferredSize();
			mCaret.moveRelative(0, 1, false, true, true);
		}
		else
		{
			mDocument.splitLine(y, x);
			mDocument.insertSpan(y + 1, 0, indent);
			recomputePreferredSize();
			mCaret.moveAbsolute(indent.length(), y + 1, false, true, true);
		}
		mCaret.makePreferredPosition();

		mDocument.commitUndoableEdit();
	}


	public void upperCase()
	{
		if (isTextSelected())
		{
			Point ss = mSelectionStart;
			Point se = mSelectionEnd;
			replaceSelection(getSelectedText().toString().toUpperCase());
			mSelectionStart = ss;
			mSelectionEnd = se;
		}
		repaint();
	}


	public void lowerCase()
	{
		if (isTextSelected())
		{
			Point ss = mSelectionStart;
			Point se = mSelectionEnd;
			replaceSelection(getSelectedText().toString().toLowerCase());
			mSelectionStart = ss;
			mSelectionEnd = se;
		}
		repaint();
	}


	public void scrollToCaret()
	{
		mCaret.scrollToCaret();
	}


	public void resetSelection()
	{
		mSelectionStart = null;
		mSelectionEnd = null;
		mRectangularSelection = false;
	}


	public void scrollToSelection()
	{
		Point p = getSelectionStart();
		Point q = getSelectionEnd();

		int lineHeight = getFontHeight() + getLineSpacing();

		Point start = new Point(getPixelOffset(p.x, p.y), p.y * lineHeight);
		Point end = new Point(getPixelOffset(q.x, q.y), q.y * lineHeight);

		if (start.x > end.x)
		{
			int temp = start.x;
			start.x = end.x;
			end.x = temp;
		}
		if (start.y > end.y)
		{
			int temp = start.y;
			start.y = end.y;
			end.y = temp;
		}

		scrollRectToVisible(new Rectangle(start.x, start.y, end.x, end.y));
	}


	public boolean findText(String aSearchString, boolean aForward, boolean aCaseSensative, boolean aWrapSearch, boolean aWholeWordsOnly)
	{
		int x = mCaret.getCharacterPosition().x;
		int y = mCaret.getCharacterPosition().y;
		int startY = y;
		String s = mDocument.getLine(y).substring(x);
		if (!aCaseSensative)
		{
			s = s.toLowerCase();
			aSearchString = aSearchString.toLowerCase();
		}
		boolean terminate = false;

		while (true)
		{
			int offset = s.indexOf(aSearchString);
			if (aWholeWordsOnly && offset != -1)
			{
				s += " ";
				if (!((offset == 0 || !Character.isLetter(s.charAt(offset - 1))) && !Character.isLetter(s.charAt(offset + aSearchString.length()))))
				{
					offset = -1;
				}
			}
			if (offset != -1)
			{
				resetSelection();
				mCaret.moveAbsolute(x + offset + aSearchString.length(), y, false, false, true);
				setSelectionStart(x + offset, y);
				setSelectionEnd(x + offset + aSearchString.length(), y);
				scrollToSelection();
				return true;
			}
			else
			{
				if (terminate)
				{
					return false;
				}

				x = 0;
				y += aForward ? 1 : -1;
				int lineCount = mDocument.getLineCount();
				if (aWrapSearch)
				{
					if (y == lineCount)
					{
						y = 0;
					}
					if (y == -1)
					{
						y = lineCount - 1;
					}
				}
				else
				{
					if (y == lineCount)
					{
						y = lineCount - 1;
					}
					if (y == -1)
					{
						y = 0;
					}
				}
				s = mDocument.getLine(y);
				if (!aCaseSensative)
				{
					s = s.toLowerCase();
				}

				if (y == startY)
				{
					terminate = true;
				}
			}
		}
	}


	public String getTokendAtCaret()
	{
		int x = mCaret.getCharacterPosition().x;
		int y = mCaret.getCharacterPosition().y;

		return mDocument.getLine(y).substring(getPreviousTokenOffset(x, y), getNextTokenOffset(x, y, false));
	}


	protected int getPreviousTokenOffset(int x, int y)
	{
		mInputSyntaxParser.initialize(mDocument, y);
		int prevOfs = 0;
		int prevLen = 0;
		for (Token token : mInputSyntaxParser.parse(mDocument, y, false, false))
		{
			int ofs = token.getOffset();
			int len = token.length();

			if (x >= ofs && x <= ofs+len)
			{
				char c = mDocument.getCharAt(y, ofs);
				if (ofs - len > 0 && Character.isWhitespace(c))
				{
					ofs = prevOfs;
					len = prevLen;
				}
				return ofs;
			}
			if (!Character.isWhitespace(token.getToken().charAt(0)))
			{
				prevOfs = ofs;
				prevLen = len;
			}
		}
		return 0;
	}


	protected int getNextTokenOffset(int x, int y, boolean aIncludeWhiteSpace)
	{
		mInputSyntaxParser.initialize(mDocument, y);
		for (Token token : mInputSyntaxParser.parse(mDocument, y, false, false))
		{
			int o = token.getOffset()+token.length();
			if (x >= token.getOffset() && x < o)
			{
				if (aIncludeWhiteSpace)
				{
					while (o < mDocument.getLineLength(y) && Character.isWhitespace(mDocument.getCharAt(y, o)))
					{
						o++;
					}
				}
				return o;
			}
		}
		return mDocument.getLineLength(y);
	}


	protected Style getTokenStyleAt(int x, int y)
	{
		mInputSyntaxParser.initialize(mDocument, y);
		for (Token token : mInputSyntaxParser.parse(mDocument, y, false, false))
		{
			int o = token.getOffset()+token.length();
			if (x >= token.getOffset() && x < o)
			{
				while (o < mDocument.getLineLength(y) && Character.isWhitespace(mDocument.getCharAt(y, o)))
				{
					o++;
				}
				return token.getStyle();
			}
		}
		return null;
	}


	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return getPreferredSize();
	}


	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		switch (orientation)
		{
			case SwingConstants.VERTICAL:
				return visibleRect.height;
			case SwingConstants.HORIZONTAL:
				return visibleRect.width;
			default:
				throw new IllegalArgumentException("Invalid orientation: " + orientation);
		}
	}


	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		if (getParent() instanceof JViewport)
		{
			return (((JViewport)getParent()).getHeight() > getPreferredSize().height);
		}
		return false;
	}


	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		if (getParent() instanceof JViewport)
		{
			return (((JViewport)getParent()).getWidth() > getPreferredSize().width);
		}
		return false;
	}


	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		switch (orientation)
		{
			case SwingConstants.VERTICAL:
				return mPaintSyntaxParser.getStyle(SyntaxParser.SELECTION).getFontHeight();
			case SwingConstants.HORIZONTAL:
				return mPaintSyntaxParser.getStyle(SyntaxParser.SELECTION).getStringWidth("m");
			default:
				throw new IllegalArgumentException("Invalid orientation: " + orientation);
		}
	}
}