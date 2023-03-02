package org.terifan.sourcecodeeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


public class FindDialog extends JDialog implements ActionListener
{
	private final static long serialVersionUID = 1L;

	protected boolean mWasCanceled;
	protected JButton mSearchButton;
	protected JButton mReplaceButton;
	protected JButton mReplaceAllButton;
	protected JButton mReplaceAndSearchButton;
	protected JCheckBoxEx mCaseSensative;
	protected JCheckBoxEx mWholeWordsOnly;
	protected JCheckBoxEx mSearchBackwards;
	protected JCheckBoxEx mWrapSearch;
	protected JCheckBoxEx mSelectionOnly;
	protected SourceEditor mSearchField;
	protected SourceEditor mReplaceField;
	protected SourceEditor mSourceEditor;


	public FindDialog(SourceEditor aSourceEditor)
	{
		super(SwingUtilities.getWindowAncestor(aSourceEditor), "Search & Replace");

		mSourceEditor = aSourceEditor;

		mCaseSensative = new JCheckBoxEx("Case Sensative", false);
		mWholeWordsOnly = new JCheckBoxEx("Whole Words Only", false);
		mSearchBackwards = new JCheckBoxEx("Search Backwards", false);
		mWrapSearch = new JCheckBoxEx("Wrap Search", true);
		mSelectionOnly = new JCheckBoxEx("Selection Only", false);

		mSearchButton = new JButton("Search");
		mSearchButton.addActionListener(this);
		mSearchButton.setActionCommand("search");
		mSearchButton.setMnemonic('S');

		mReplaceButton = new JButton("Replace");
		mReplaceButton.addActionListener(this);
		mReplaceButton.setActionCommand("replace");
		mReplaceButton.setMnemonic('R');

		mReplaceAllButton = new JButton("Replace All");
		mReplaceAllButton.addActionListener(this);
		mReplaceAllButton.setActionCommand("replaceall");
		mReplaceAllButton.setMnemonic('A');

		mReplaceAndSearchButton = new JButton("Replace & Search");
		mReplaceAndSearchButton.addActionListener(this);
		mReplaceAndSearchButton.setActionCommand("replaceandsearch");
		mReplaceAndSearchButton.setMnemonic('e');

		mSearchField = new SourceEditor(mSourceEditor.getSyntaxParser().newInstance(), new Document());
		mReplaceField = new SourceEditor(mSourceEditor.getSyntaxParser().newInstance(), new Document());

		mSearchField.setAlternateMode(true);
		mSearchField.addKeyListener(new FocusMover(mReplaceAllButton, mReplaceField));
		JScrollPane searchInput = new JScrollPane(mSearchField);

		mReplaceField.setAlternateMode(true);
		mReplaceField.addKeyListener(new FocusMover(mSearchField, mCaseSensative));
		JScrollPane replaceInput = new JScrollPane(mReplaceField);

		Font font = mSearchButton.getFont();

		JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 0, 3));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		buttonPanel.add(mSearchButton);
		buttonPanel.add(mReplaceButton);
		buttonPanel.add(mReplaceAndSearchButton);
		buttonPanel.add(mReplaceAllButton);

		JPanel optionPanel = new JPanel(new GridLayout(7, 1, 0, 0));
		optionPanel.add(mCaseSensative);
		optionPanel.add(mWholeWordsOnly);
		optionPanel.add(mSearchBackwards);
		optionPanel.add(mWrapSearch);
		optionPanel.add(mSelectionOnly);

		JPanel p = new JPanel(new BorderLayout(0, 0));
		p.add(optionPanel, BorderLayout.NORTH);

		JPanel q = new JPanel(new BorderLayout(0, 0));
		q.add(buttonPanel, BorderLayout.NORTH);

		final JPanel controlPanel = new JPanel(new BorderLayout(0, 0));
		controlPanel.add(p, BorderLayout.WEST);
		controlPanel.add(q, BorderLayout.EAST);

		JPanel searchPanel = new JPanel(new BorderLayout(0, 0));
		searchPanel.add(new JLabelEx("Search", font, 50), BorderLayout.WEST);
		searchPanel.add(searchInput, BorderLayout.CENTER);

		JPanel replacePanel = new JPanel(new BorderLayout(0, 0));
		replacePanel.add(new JLabelEx("Replace", font, 50), BorderLayout.WEST);
		replacePanel.add(replaceInput, BorderLayout.CENTER);

		JPanel inputPanel = new JPanel(new GridLayout(2, 1, 0, 5));
		inputPanel.add(searchPanel);
		inputPanel.add(replacePanel);

		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		mainPanel.add(inputPanel, BorderLayout.CENTER);
		mainPanel.add(controlPanel, BorderLayout.EAST);

		mainPanel.registerKeyboardAction(this, "search", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		mainPanel.registerKeyboardAction(this, "searchforwards", KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		mainPanel.registerKeyboardAction(this, "searchbackwards", KeyStroke.getKeyStroke(KeyEvent.VK_F3, ActionEvent.SHIFT_MASK, false), JComponent.WHEN_IN_FOCUSED_WINDOW);

		mReplaceField.dontRequestFocus();

		super.add(mainPanel);
	}


	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		String command = aEvent.getActionCommand();

		boolean forward = !mSearchBackwards.isSelected();
		boolean caseSensative = mCaseSensative.isSelected();
		boolean wrapSearch = mWrapSearch.isSelected();
		boolean wholeWordsOnly = mWholeWordsOnly.isSelected();
		String search = mSearchField.getText().toString();
		String replace = mReplaceField.getText().toString();
		SourceEditor editor = mSourceEditor;

		if (search.length() == 0)
		{
			return;
		}

		switch (command)
		{
			case "search":
			case "searchforwards":
			case "searchbackwards":
				if (command.equals("searchforwards"))
				{
					forward = true;
				}
				else if (command.equals("searchbackwards"))
				{
					forward = false;
				}	if (editor.findText(search, forward, caseSensative, wrapSearch, wholeWordsOnly))
				{
					editor.repaint();
				}
				else
				{
					JOptionPane.showMessageDialog(null, "No search matches was found.", "Search and Replace", JOptionPane.INFORMATION_MESSAGE);
				}
				break;
			case "replace":
				if (editor.isTextSelected())
				{
					editor.replaceSelection(replace);
					editor.repaint();
				}
				break;
			case "replaceall":
				Point caretPosition = new Point(editor.getCaret().getVirtualPosition());
				int count = 0;
				while (editor.findText(search, forward, caseSensative, wrapSearch, wholeWordsOnly))
				{
					editor.replaceSelection(replace);
					count++;
				}	editor.getCaret().moveAbsolute(caretPosition.x, caretPosition.y, true, false, true);
				editor.repaint();
				break;
			case "replaceandsearch":
				if (editor.isTextSelected())
				{
					editor.replaceSelection(replace);
				}	editor.repaint();
				break;
			default:
				System.out.println("Unrecognized command:" + command);
				break;
		}
	}


	public static class JLabelEx extends javax.swing.JLabel
	{
		private final static long serialVersionUID = 1L;
		JLabelEx(String aLabel, Font aFont, int aWidth)
		{
			super(aLabel);
			setForeground(Color.BLACK);
			setFont(aFont);
			setPreferredSize(new Dimension(aWidth, 50));
		}
	}


	public static class JCheckBoxEx extends javax.swing.JCheckBox
	{
		private final static long serialVersionUID = 1L;
		JCheckBoxEx(String aLabel, boolean aChecked)
		{
			super(aLabel, aChecked);
		}


		@Override
		public Dimension getPreferredSize()
		{
			Rectangle2D labelSize = getFont().getStringBounds("m", new FontRenderContext(new AffineTransform(), false, false));
			Dimension d = super.getPreferredSize();
			d.height = (int) labelSize.getHeight() + 4;
			return d;
		}
	}


	class FocusMover extends KeyAdapter
	{
		JComponent mPreviousComponent;
		JComponent mNextComponent;


		public FocusMover(JComponent aPreviousComponent, JComponent aNextComponent)
		{
			mPreviousComponent = aPreviousComponent;
			mNextComponent = aNextComponent;
		}


		@Override
		public void keyPressed(KeyEvent aEvent)
		{
			if (aEvent.getKeyChar() == '\t' && !aEvent.isControlDown())
			{
				if (aEvent.isShiftDown())
				{
					mPreviousComponent.requestFocus();
				}
				else
				{
					mNextComponent.requestFocus();
				}
			}
		}
	}
}
