package org.terifan.sourcecodeeditor;

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


public final class SourceEditor extends JComponent implements Scrollable
{
	private final static long serialVersionUID = 1L;
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
	private boolean mPaintFullRowSelectionEnabled;
	private Insets mMargins;
	private int mFontSize;
	private int mLineSpacing;
	private int mCaretBlinkRate;
	private int mTabSize;
	private String mHighlightText;
	private String mLineBreakSymbol;
	private boolean mAlternateMode;
	private transient Object mAntialiase;

	private SyntaxParser mInputSyntaxParser;
	private SyntaxParser mOffsetSyntaxParser;
	private SyntaxParser mPaintSyntaxParser;
	private SyntaxParser mPixelOffsetSyntaxParser;
	private Color mHighlightCaretRow;
	private Color mCaretColor;


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
		setCaretBlinkRate(350);
		setHighlightTextCaseSensative(false);
		setLineBreakSymbolEnabled(false);
		setOverwriteTextEnabled(false);
		setTabIndentsTextEnabled(true);
		setWhitespaceSymbolEnabled(false);
		setPaintFullRowSelectionEnabled(true);
		setMargins(new Insets(0, 0, 0, 0));
		setFontPointSize(13);
		setLineSpacing(0);
		setTabSize(4);
		setLineBreakSymbol('\u00B6');
		setAlternateMode(false);
		setAntialiase(RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		setHighlightCaretRow(new Color(50,50,50));

		SourceEditorMouseListener mouseListener = new SourceEditorMouseListener(this);
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		addKeyListener(new SourceEditorKeyListener(this));
		addFocusListener(new SourceEditorFocusListener(this));

		mRequestFocus = true;

		registerDefaultKeyboardActions();
	}


	@Override
	public void doLayout()
	{
		if (mCaret == null)
		{
			mCaret = new Caret(this);
			mCaret.setEnabled(false);
			mCaret.start();
		}

		super.doLayout();
	}


	public void dispose()
	{
		mCaret.dispose();
	}


	private void registerDefaultKeyboardActions()
	{
		ActionListener actionListener = aEvent -> performAction(aEvent.getActionCommand());

		super.registerKeyboardAction(actionListener, "undo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "redo", KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "cut", KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "paste", KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "selectAll", KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "deleteLine", KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "upperCase", KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "lowerCase", KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK), JComponent.WHEN_FOCUSED);
		super.registerKeyboardAction(actionListener, "find", KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
	}


	public void performAction(String aAction)
	{
		if (null == aAction)
		{
			throw new IllegalArgumentException("Unsupported action: " + aAction);
		}

		switch (aAction)
		{
			case "undo":
				undo();
				break;
			case "redo":
				redo();
				break;
			case "cut":
				cut();
				break;
			case "copy":
				copy();
				break;
			case "paste":
				paste();
				break;
			case "deleteLine":
				deleteLine();
				break;
			case "selectAll":
				selectAll();
				break;
			case "upperCase":
				upperCase();
				break;
			case "lowerCase":
				lowerCase();
				break;
			case "deleteNextCharacter":
				deleteNextCharacter();
				break;
			case "deletePreviousCharacter":
				deletePreviousCharacter();
				break;
			case "deleteToken":
				deleteToken();
				break;
			case "moveCaretDocumentTop":
				moveCaretDocumentTop();
				break;
			case "moveCaretDocumentEnd":
				moveCaretDocumentEnd();
				break;
			case "moveCaretLineStart":
				moveCaretLineStart();
				break;
			case "moveCaretPageUp":
				moveCaretPageUp();
				break;
			case "moveCaretPageDown":
				moveCaretPageDown();
				break;
			case "resetSelection":
				resetSelection();
				break;
			case "find":
				if (mFindDialog == null)
				{
					mFindDialog = new FindDialog(this);
					mFindDialog.setSize(600, 150);
				}	mFindDialog.setLocationRelativeTo(this);
				mFindDialog.setVisible(true);
				break;
			default:
				throw new IllegalArgumentException("Unsupported action: " + aAction);
		}
	}


	public int getCaretBlinkRate()
	{
		return mCaretBlinkRate;
	}


	public SourceEditor setCaretBlinkRate(int aCaretBlinkRate)
	{
		mCaretBlinkRate = aCaretBlinkRate;
		return this;
	}


	public boolean canUndo()
	{
		return mDocument.getUndoManager().canUndo();
	}


	public SourceEditor undo()
	{
		if (canUndo())
		{
			mDocument.getUndoManager().undo();
			repaint();
		}
		return this;
	}


	public boolean canRedo()
	{
		return mDocument.getUndoManager().canRedo();
	}


	public SourceEditor redo()
	{
		if (canRedo())
		{
			mDocument.getUndoManager().redo();
			repaint();
		}
		return this;
	}


	public SourceEditor dontRequestFocus()
	{
		mRequestFocus = false;
		return this;
	}


	public SourceEditor setSyntaxParser(SyntaxParser aSyntaxParser)
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
		return this;
	}


	public SourceEditor setHighlightText(String aHighlightText)
	{
		mHighlightText = aHighlightText;

		if (mHighlightText != null && mHighlightText.length() == 0)
		{
			mHighlightText = null;
		}
		return this;
	}


	public String getHighlightText()
	{
		return mHighlightText;
	}


	public SourceEditor setHighlightTextCaseSensative(boolean aState)
	{
		mHighlightTextCaseSensative = aState;
		return this;
	}


	public boolean isHighlightTextCaseSensative()
	{
		return mHighlightTextCaseSensative;
	}


	public SourceEditor setTabIndentsTextEnabled(boolean aTabIndentsTextEnabled)
	{
		mTabIndentsTextEnabled = aTabIndentsTextEnabled;
		return this;
	}


	public boolean isAlternateMode()
	{
		return mAlternateMode;
	}


	public SourceEditor setAlternateMode(boolean aAlternateMode)
	{
		mAlternateMode = aAlternateMode;
		return this;
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
	public SourceEditor setAntialiase(Object aAntialiase)
	{
		mAntialiase = aAntialiase;
		return this;
	}


	public Color getHighlightCaretRow()
	{
		return mHighlightCaretRow;
	}


	public SourceEditor setHighlightCaretRow(Color aHighlightCaretRow)
	{
		mHighlightCaretRow = aHighlightCaretRow;
		return this;
	}


	public boolean isTabIndentsTextEnabled()
	{
		return mTabIndentsTextEnabled;
	}


	public SourceEditor setAutoLineCopyCutEnabled(boolean aAutoLineCopyCutEnabled)
	{
		mAutoLineCopyCutEnabled = aAutoLineCopyCutEnabled;
		return this;
	}


	public boolean isAutoLineCopyCutEnabled()
	{
		return mAutoLineCopyCutEnabled;
	}


	public SourceEditor setAutoIndentEnabled(boolean aAutoIndentEnabled)
	{
		mAutoIndentEnabled = aAutoIndentEnabled;
		return this;
	}


	public boolean isAutoIndentEnabled()
	{
		return mAutoIndentEnabled;
	}


	public SourceEditor setBoldCaretEnabled(boolean aBoldCaretEnabled)
	{
		mBoldCaretEnabled = aBoldCaretEnabled;
		return this;
	}


	public boolean isBoldCaretEnabled()
	{
		return mBoldCaretEnabled;
	}


	public Color getCaretColor()
	{
		return mCaretColor;
	}


	/**
	 * @param aCaretColor
	 *   null or the caret color. If null then the caret is the inverted (xor) of background.
	 */
	public SourceEditor setCaretColor(Color aCaretColor)
	{
		mCaretColor = aCaretColor;
		return this;
	}


	public boolean isPaintFullRowSelectionEnabled()
	{
		return mPaintFullRowSelectionEnabled;
	}


	public SourceEditor setPaintFullRowSelectionEnabled(boolean aPaintFullRowSelectionEnabled)
	{
		mPaintFullRowSelectionEnabled = aPaintFullRowSelectionEnabled;
		return this;
	}


	public SourceEditor setLineBreakSymbolEnabled(boolean aLineBreakSymbolEnabled)
	{
		mLineBreakSymbolEnabled = aLineBreakSymbolEnabled;
		return this;
	}


	public boolean isLineBreakSymbolEnabled()
	{
		return mLineBreakSymbolEnabled;
	}


	public SourceEditor setOverwriteTextEnabled(boolean aOverwriteTextEnabled)
	{
		mOverwriteTextEnabled = aOverwriteTextEnabled;
		return this;
	}


	public boolean isOverwriteTextEnabled()
	{
		return mOverwriteTextEnabled;
	}


	public SourceEditor setLineSpacing(int aLineSpacing)
	{
		mLineSpacing = aLineSpacing;
		return this;
	}


	public int getLineSpacing()
	{
		return mLineSpacing;
	}


	public SourceEditor setWhitespaceSymbolEnabled(boolean aWhitespaceSymbolEnabled)
	{
		mWhitespaceSymbolEnabled = aWhitespaceSymbolEnabled;
		return this;
	}


	public boolean isWhitespaceSymbolEnabled()
	{
		return mWhitespaceSymbolEnabled;
	}


	public SourceEditor setTabSize(int aTabSize)
	{
		mTabSize = aTabSize;
		return this;
	}


	public int getTabSize()
	{
		return mTabSize;
	}


	public SourceEditor setMargins(Insets aMargins)
	{
		mMargins = aMargins;
		return this;
	}


	public Insets getMargins()
	{
		return mMargins;
	}


	public SourceEditor setLineBreakSymbol(char aLineBreakSymbol)
	{
		mLineBreakSymbol = Character.toString(aLineBreakSymbol);
		return this;
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


	public SourceEditor setDocument(Document aDocument)
	{
		if (aDocument == null)
		{
			throw new IllegalArgumentException("Document provided is null.");
		}

		mDocument = aDocument;
		mDocument.setParent(this);
		recomputePreferredSize();
		return this;
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


	public SourceEditor setFontPointSize(int aFontPointSize)
	{
		for (String key : mPaintSyntaxParser.getStyleKeys())
		{
			mPaintSyntaxParser.getStyle(key).setFontSize(aFontPointSize);
		}
		return this;
	}


	public int getFontPointSize()
	{
		return mPaintSyntaxParser.getStyle(SyntaxParser.WHITESPACE).getFont().getSize();
	}


	public SourceEditor setSelectionStart(int aColumn, int aRow)
	{
		if (mSelectionStart == null)
		{
			mSelectionStart = new Point(includeTabsInOffset(aColumn, aRow), aRow);
		}
		else
		{
			mSelectionStart.move(includeTabsInOffset(aColumn, aRow), aRow);
		}
		return this;
	}


	public Point getSelectionStart()
	{
		if (mSelectionStart == null)
		{
			return null;
		}
		return new Point(removeTabsFromOffset(mSelectionStart.x, mSelectionStart.y), mSelectionStart.y);
	}


	protected SourceEditor setSelectionStartUnmodified(Point aPoint)
	{
		mSelectionStart = aPoint;
		return this;
	}


	protected Point getSelectionStartUnmodified()
	{
		return mSelectionStart;
	}


	public SourceEditor setSelectionEnd(int aColumn, int aRow)
	{
		if (mSelectionEnd == null)
		{
			mSelectionEnd = new Point(includeTabsInOffset(aColumn, aRow), aRow);
		}
		else
		{
			mSelectionEnd.move(includeTabsInOffset(aColumn, aRow), aRow);
		}
		return this;
	}


	public Point getSelectionEnd()
	{
		if (mSelectionEnd == null)
		{
			return null;
		}
		return new Point(removeTabsFromOffset(mSelectionEnd.x, mSelectionEnd.y), mSelectionEnd.y);
	}


	protected SourceEditor setSelectionEndUnmodified(Point aPoint)
	{
		mSelectionEnd = aPoint;
		return this;
	}


	protected Point getSelectionEndUnmodified()
	{
		return mSelectionEnd;
	}


	public SourceEditor replaceSelection(String aNewText)
	{
		if (!isTextSelected())
		{
			return this;
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
		return this;
	}


	public boolean isTextSelected()
	{
		return mSelectionStart != null && mSelectionEnd != null && (mRectangularSelection || !mSelectionStart.equals(mSelectionEnd));
	}


	public boolean isRectangularSelection()
	{
		return mRectangularSelection;
	}


	public SourceEditor setRectangularSelection(boolean aState)
	{
		mRectangularSelection = aState;
		return this;
	}


	public SourceEditor insertText(String aText)
	{
		insertText(aText, false);

		recomputePreferredSize();
		return this;
	}


	public SourceEditor insertText(String aText, boolean aRectangularText)
	{
		if (aText.isEmpty())
		{
			return this;
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

					while (paste.indexOf('\r') > 0)
					{
						paste = paste.substring(0, paste.indexOf('\r')) + paste.substring(paste.indexOf('\r') + 1);
					}
					while (paste.indexOf('\n') > 0)
					{
						paste = paste.substring(0, paste.indexOf('\n')) + paste.substring(paste.indexOf('\n') + 1);
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

				while (paste.indexOf('\r') > 0)
				{
					paste = paste.substring(0, paste.indexOf('\r')) + paste.substring(paste.indexOf('\r') + 1);
				}
				while (paste.indexOf('\n') > 0)
				{
					paste = paste.substring(0, paste.indexOf('\n')) + paste.substring(paste.indexOf('\n') + 1);
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
		return this;
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

		int fontHeight = getFontHeight();
		int fontAscent = getFontAscent();
		int editorW = getWidth();

		Rectangle clipBounds = g.getClipBounds();
		int firstRow = clipBounds.y / (fontHeight + mLineSpacing);
		int lastRow = (clipBounds.y + clipBounds.height) / (fontHeight + mLineSpacing) - 1 + 2;

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
		g.fillRect(0, 0, editorW, getHeight());

		for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++)
		{
			boolean highlightText = mHighlightText != null && mDocument.getLine(rowIndex).contains(mHighlightText);

			List<Token> tokens = mPaintSyntaxParser.parse(mDocument, rowIndex, optimizeTokens && !highlightText, optimizeWhitespace);
			int positionX = 0;
			int y = fontAscent + rowIndex * (fontHeight + mLineSpacing) + mMargins.top;

			if (mHighlightCaretRow != null && rowIndex == mCaret.getCharacterPosition().y)
			{
				int h = fontHeight + mLineSpacing;
				g.setColor(mHighlightCaretRow);
				g.fillRect(0, mMargins.top + rowIndex * h, editorW, h);
			}

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
							positionX = paintToken(g, positionX, y, token, 0, selectionIntersect.x, clipBounds, highlightText, false);
						}

						positionX = paintToken(g, positionX, y, token, selectionIntersect.x, selectionIntersect.y, clipBounds, highlightText, true);

						if (selectionIntersect.y < len)
						{
							positionX = paintToken(g, positionX, y, token, selectionIntersect.y, len, clipBounds, highlightText, false);
						}
					}
					else
					{
						positionX = paintToken(g, positionX, y, token, -1, -1, clipBounds, highlightText, false);
					}
				}
			}

			if (token != null)
			{
				boolean selectionIntersected = isTextSelected() && intersectSelection(mDocument.getLineLength(rowIndex), rowIndex);

				if (selectionIntersected || mLineBreakSymbolEnabled)
				{
					Style fontStyle = mPaintSyntaxParser.getStyle(SyntaxParser.LINE_BREAK);
					Style colorStyle = token.isComment() ? token.getStyle() : fontStyle;

					int w = fontStyle.getStringWidth(mLineBreakSymbol);

					if (mPaintFullRowSelectionEnabled)
					{
						g.setColor(mPaintSyntaxParser.getStyle(SyntaxParser.SELECTION).getBackground());
						g.fillRect(positionX + mMargins.left, y - fontAscent, editorW - (positionX + mMargins.left), fontHeight);
					}
					else if (highlightText || !getBackground().equals(colorStyle.getBackground()))
					{
						g.setColor(colorStyle.getBackground());
						g.fillRect(positionX + mMargins.left, y - fontAscent, w, fontHeight);
					}

					Color fontColor = colorStyle.getForeground();
					if (selectionIntersected)
					{
						fontColor = mPaintSyntaxParser.getStyle(SyntaxParser.SELECTION).getForeground();
					}

					g.setFont(fontStyle.getFont());
					g.setColor(fontColor);
					g.drawString(mLineBreakSymbol, positionX + mMargins.left, y);

					positionX += w;
				}

				if (positionX + mMargins.left + mMargins.right > editorW)
				{
					mPreferredSize = null;
					revalidate();
				}
			}
		}

		mCaret.paintCaret(g);
	}


	private int paintToken(Graphics aGraphics, int aPixelX, int aPixelY, Token aToken, int aTokenOffset, int aTokenLength, Rectangle aClipBounds, boolean aHighlightText, boolean aIsSelection)
	{
		if (aTokenOffset != -1 && aTokenOffset == aTokenLength)
		{
			return aPixelX;
		}

		String text = aToken.getToken();

		if (aTokenOffset != -1)
		{
			text = text.substring(aTokenOffset, aTokenLength);
		}

		int fontHeight = getFontHeight();
		int fontAscent = getFontAscent();

		Style tokenStyle = aToken.getStyle();
		int x0 = aPixelX + mMargins.left;
		int x1 = advancePosition(x0, text, tokenStyle);

		if (x1 >= aClipBounds.x)
		{
			Style style = aIsSelection ? mPaintSyntaxParser.getStyle(SyntaxParser.SELECTION) : tokenStyle;
			Style colorStyle = tokenStyle;

			if (aHighlightText && tokenStyle.isSupportHighlight() && style.isBackgroundOptional() && text.equalsIgnoreCase(mHighlightText))
			{
				aGraphics.setColor(getStyle(SyntaxParser.HIGHLIGHT).getBackground());
				aGraphics.fillRect(x0, aPixelY - fontAscent, x1 - x0, fontHeight);
			}
			else if (!getBackground().equals(style.getBackground()))
			{
				aGraphics.setColor(style.getBackground());
				aGraphics.fillRect(x0, aPixelY - fontAscent, x1 - x0, fontHeight);
			}

			if (DEBUG_GRAPHICS)
			{
				aGraphics.setColor(new Color(192, 192, 192));
				aGraphics.drawRect(x0, aPixelY - fontAscent, x1 - x0, fontHeight);
			}

			aGraphics.setFont(tokenStyle.getFont());
			aGraphics.setColor(colorStyle.getForeground());
			aGraphics.drawString(text, x0, aPixelY);

			if (tokenStyle.isUnderlined())
			{
				aGraphics.drawLine(x0, aPixelY + 1, x1, aPixelY + 1);
			}

			if (mWhitespaceSymbolEnabled && (text.startsWith(" ") || text.startsWith("\t")))
			{
				int x = (x0 + x1) / 2;
				int v = aPixelY - fontAscent + fontHeight / 2;

				aGraphics.setColor((aToken.isComment() && !aIsSelection ? aToken.getStyle() : style).getForeground());
				if (text.charAt(0) == ' ')
				{
					aGraphics.drawLine(x, v, x, v);
				}
				else
				{
					aGraphics.drawLine(x - 2, v, x + 2, v);
					aGraphics.drawLine(x + 2, v, x, v - 2);
					aGraphics.drawLine(x + 2, v, x, v + 2);
				}
			}
		}

		aPixelX = x1;

		return aPixelX;
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

			return aColumn < removeTabsFromOffset(selectionEnd.x, selectionEnd.y) + adjust;
		}
		else
		{
			if (aRow == selectionStart.y && aColumn < removeTabsFromOffset(selectionStart.x, selectionStart.y))
			{
				return false;
			}

			return !(aRow == selectionEnd.y && aColumn >= removeTabsFromOffset(selectionEnd.x, selectionEnd.y));
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


	Point getSourceOffset(Point aMousePoint)
	{
		int y = (aMousePoint.y - mMargins.top) / (getFontHeight() + mLineSpacing);

		y = Math.min(Math.max(y, 0), mDocument.getLineCount() - 1);

		String sourceLine = mDocument.getLine(y);

		mOffsetSyntaxParser.initialize(mDocument, y);
		int positionX = 0;
		int offsetX = -1;

		for (Token token : mOffsetSyntaxParser.parse(mDocument, y, true, false))
		{
			String s = token.getToken();
			Style style = token.getStyle();
			int x1 = advancePosition(positionX, s, style);

			if (x1 >= aMousePoint.x - mMargins.left)
			{
				int min = 0;
				int max = s.length();
				int mid = max / 2;

				while (max - min > 1)
				{
					x1 = advancePosition(positionX, s.substring(0, mid), style);

					if (x1 > aMousePoint.x)
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

				break;
			}

			positionX = x1;
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
					positionX = advancePosition(positionX, s, token.getStyle());
				}
			}
			else
			{
				if (token.getOffset()+token.length() >= aCharacterOffset)
				{
					int len = s.length() - (token.getOffset()+token.length() - aCharacterOffset);
					if (len > 0)
					{
						positionX = advancePosition(positionX, s.substring(0, len), token.getStyle());
					}
					break;
				}
				else
				{
					positionX = advancePosition(positionX, s, token.getStyle());
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


	public SourceEditor setText(String aText)
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
		return this;
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


	public StringBuilder getSelectedText()
	{
		String lineBreak = "\n";

		StringBuilder selection = new StringBuilder();

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


	public SourceEditor deleteLine()
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
		return this;
	}


	public SourceEditor cut()
	{
		if (isTextSelected())
		{
			StringBuilder selection = getSelectedText();
			StringSelection stringSelection = new StringSelection(selection.toString());
			getToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
			mClipboardContent = mRectangularSelection ? selection.toString() : "";

			replaceSelection("");
			repaint();
		}
		else if (mAutoLineCopyCutEnabled)
		{
			String selection = mDocument.getLine(mCaret.getCharacterPosition().y);
			StringSelection stringSelection = new StringSelection(selection);
			getToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
			mClipboardContent = selection;

			deleteLine();
		}

		return this;
	}


	public SourceEditor copy()
	{
		if (!isTextSelected() && mAutoLineCopyCutEnabled)
		{
			int y = mCaret.getCharacterPosition().y;
			setSelectionStart(0, y);
			setSelectionEnd(mDocument.getLineLength(y), y);
			repaint();
		}

		StringBuilder selection = getSelectedText();
		StringSelection stringSelection = new StringSelection(selection.toString());
		getToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
		mClipboardContent = mRectangularSelection ? selection.toString() : "";
		return this;
	}


	public SourceEditor paste()
	{
		try
		{
			String clipboardText = (String) (getToolkit().getSystemClipboard().getContents(this).getTransferData(DataFlavor.stringFlavor));
			insertText(clipboardText, clipboardText.equals(mClipboardContent));
		}
		catch (IllegalStateException | UnsupportedFlavorException | IOException e)
		{
			throw new IllegalStateException(e);
		}
		repaint();
		return this;
	}


	public SourceEditor selectAll()
	{
		setSelectionStart(0, 0);
		setSelectionEnd(mDocument.getLineLength(mDocument.getLineCount() - 1), mDocument.getLineCount() - 1);
		repaint();
		return this;
	}


	public SourceEditor deleteToken()
	{
		if (isTextSelected())
		{
			replaceSelection("");
			mCaret.makePreferredPosition();
			return this;
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
		return this;
	}


	public SourceEditor deleteNextCharacter()
	{
		if (isTextSelected())
		{
			replaceSelection("");
			mCaret.makePreferredPosition();
			return this;
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
		return this;
	}


	public SourceEditor indent()
	{
		indentOutdentSelection(false);
		return this;
	}


	public SourceEditor outdent()
	{
		indentOutdentSelection(true);
		return this;
	}


	public SourceEditor moveCaretDocumentTop()
	{
		mCaret.moveAbsolute(0, 0, false, true, true);
		mCaret.makePreferredPosition();
		return this;
	}


	public SourceEditor moveCaretLineStart()
	{
		mCaret.moveAbsolute(0, mCaret.getCharacterPosition().y, false, true, true);
		mCaret.makePreferredPosition();
		return this;
	}


	public SourceEditor scrollPageUp()
	{
		int firstRow = Math.max(0, ((JViewport) getParent()).getViewPosition().y / (getFontHeight() + mLineSpacing) - getParent().getHeight() / (getFontHeight() + mLineSpacing));
		int lastRow = Math.min(mDocument.getLineCount(), firstRow + getParent().getHeight() / (getFontHeight() + mLineSpacing));
		scrollRectToVisible(new Rectangle(mMargins.left + mCaret.getPixelPosition().x, mMargins.top + (getFontHeight() + mLineSpacing) * firstRow, 1, (getFontHeight() + mLineSpacing) * (lastRow - firstRow)));
		mCaret.paintImmediately();
		return this;
	}


	public SourceEditor moveCaretPageUp()
	{
		mCaret.moveRelative(0, -getParent().getHeight() / (getFontHeight() + mLineSpacing) + 1, true, true, true);
		return this;
	}


	public SourceEditor scrollPageDown()
	{
		int firstRow = (int) Math.ceil(((JViewport) getParent()).getViewPosition().y / (double) (getFontHeight() + mLineSpacing)) + getParent().getHeight() / (getFontHeight() + mLineSpacing);
		int lastRow = Math.min(mDocument.getLineCount(), firstRow + getParent().getHeight() / (getFontHeight() + mLineSpacing)) + 1;
		scrollRectToVisible(new Rectangle(mMargins.left + mCaret.getPixelPosition().x, mMargins.top + (getFontHeight() + mLineSpacing) * firstRow, 1, (getFontHeight() + mLineSpacing) * (lastRow - firstRow)));
		mCaret.paintImmediately();
		return this;
	}


	public SourceEditor moveCaretPageDown()
	{
		mCaret.moveRelative(0, getParent().getHeight() / (getFontHeight() + mLineSpacing) - 1, true, true, true);
		return this;
	}


	public SourceEditor scrollLineUp()
	{
		int firstRow = Math.max(0, ((JViewport) getParent()).getViewPosition().y / (getFontHeight() + mLineSpacing) - 1);
		int lastRow = Math.min(mDocument.getLineCount(), firstRow + getParent().getHeight() / (getFontHeight() + mLineSpacing));
		scrollRectToVisible(new Rectangle(mMargins.left + mCaret.getPixelPosition().x, mMargins.top + (getFontHeight() + mLineSpacing) * firstRow, 1, (getFontHeight() + mLineSpacing) * (lastRow - firstRow)));
		mCaret.paintImmediately();
		return this;
	}


	public SourceEditor moveCaretLineUp()
	{
		mCaret.moveRelative(0, -1, true, true, true);
		return this;
	}


	public SourceEditor scrollLineDown()
	{
		int firstRow = 1 + (int) Math.ceil(((JViewport) getParent()).getViewPosition().y / (double) (getFontHeight() + mLineSpacing));
		int lastRow = Math.min(mDocument.getLineCount(), firstRow + getParent().getHeight() / (getFontHeight() + mLineSpacing)) + 1;
		scrollRectToVisible(new Rectangle(mMargins.left + mCaret.getPixelPosition().x, mMargins.top + (getFontHeight() + mLineSpacing) * firstRow, 1, (getFontHeight() + mLineSpacing) * (lastRow - firstRow)));
		mCaret.paintImmediately();
		return this;
	}


	public SourceEditor moveCaretLineDown()
	{
		mCaret.moveRelative(0, 1, true, true, true);
		return this;
	}


	public SourceEditor moveCaretPreviousToken()
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
			return this;
		}

		mCaret.moveRelative(getPreviousTokenOffset(x, y)-x, 0, false, true, true);
		return this;
	}


	public SourceEditor moveCaretLeft()
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
		return this;
	}


	public SourceEditor moveCaretNextToken()
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
		return this;
	}


	public SourceEditor moveCaretRight()
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
		return this;
	}


	public SourceEditor moveCaretDocumentEnd()
	{
		mCaret.moveAbsolute(mDocument.getLineLength(mDocument.getLineCount() - 1), mDocument.getLineCount(), false, true, true);
		mCaret.makePreferredPosition();
		return this;
	}


	public SourceEditor moveCaretLineEnd()
	{
		int y = mCaret.getCharacterPosition().y;
		mCaret.moveAbsolute(mDocument.getLineLength(y), y, false, true, true);
		mCaret.makePreferredPosition();
		return this;
	}


	public SourceEditor deletePreviousCharacter()
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
		return this;
	}


	public SourceEditor insertBreak()
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
		return this;
	}


	public SourceEditor upperCase()
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
		return this;
	}


	public SourceEditor lowerCase()
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
		return this;
	}


	public SourceEditor scrollToCaret()
	{
		mCaret.scrollToCaret();
		return this;
	}


	public SourceEditor resetSelection()
	{
		mSelectionStart = null;
		mSelectionEnd = null;
		mRectangularSelection = false;
		return this;
	}


	public SourceEditor scrollToSelection()
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
		return this;
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
//		int prevLen = 0;
		for (Token token : mInputSyntaxParser.parse(mDocument, y, false, false))
		{
			int ofs = token.getOffset();
			int len = token.length();

			if (x >= ofs && x <= ofs + len)
			{
				char c = mDocument.getCharAt(y, ofs);
				if (ofs - len > 0 && Character.isWhitespace(c))
				{
					ofs = prevOfs;
//					len = prevLen;
				}
				return ofs;
			}
			if (!Character.isWhitespace(token.getToken().charAt(0)))
			{
				prevOfs = ofs;
//				prevLen = len;
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
			return (getParent().getHeight() > getPreferredSize().height);
		}
		return false;
	}


	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		if (getParent() instanceof JViewport)
		{
			return (getParent().getWidth() > getPreferredSize().width);
		}
		return false;
	}


	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		Style style = mPaintSyntaxParser.getStyle(SyntaxParser.SELECTION);

		switch (orientation)
		{
			case SwingConstants.VERTICAL:
				return style.getFontHeight();
			case SwingConstants.HORIZONTAL:
				return style.getCharWidth('m');
			default:
				throw new IllegalArgumentException("Invalid orientation: " + orientation);
		}
	}


	private int advancePosition(int aPixelX, String aText, Style aStyle)
	{
		for (int i = 0; i < aText.length(); )
		{
			int j = aText.indexOf('\t', i);
			if (j == i)
			{
				int ts = mTabSize * aStyle.getCharWidth(' ');
				aPixelX = ((aPixelX + ts) / ts) * ts;
				i++;
			}
			else
			{
				if (j == -1)
				{
					j = aText.length();
				}
				aPixelX += aStyle.getStringWidth(aText.substring(i, j));
				i = j;
			}
		}

		return aPixelX;
	}
}