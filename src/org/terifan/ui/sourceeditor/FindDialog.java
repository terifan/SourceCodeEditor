package org.terifan.ui.sourceeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
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
	private static FindDialog mFormInstance;

	protected boolean mWasCanceled;
	protected JButton mSearchButton;
	protected JButton mReplaceButton;
	protected JButton mReplaceAllButton;
	protected JButton mReplaceAndSearchButton;
	protected JCheckBox mCaseSensative;
	protected JCheckBox mWholeWordsOnly;
	protected JCheckBox mSearchBackwards;
	protected JCheckBox mWrapSearch;
	protected JCheckBox mSelectionOnly;
	protected SourceEditor mSearchField;
	protected SourceEditor mReplaceField;
	protected SourceEditor mSourceEditor;


	public FindDialog(SourceEditor aSourceEditor)
	{
		super(SwingUtilities.getWindowAncestor(aSourceEditor), "Search & Replace");
		
		mSourceEditor = aSourceEditor;

		mCaseSensative = new JCheckBox("Case Sensative", false);
		mWholeWordsOnly = new JCheckBox("Whole Words Only", false);
		mSearchBackwards = new JCheckBox("Search Backwards", false);
		mWrapSearch = new JCheckBox("Wrap Search", true);
		mSelectionOnly = new JCheckBox("Selection Only", false);

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
		JScrollPane searchPane = new JScrollPane(mSearchField);

		mReplaceField.setAlternateMode(true);
		mReplaceField.addKeyListener(new FocusMover(mSearchField, mCaseSensative));
		JScrollPane replacePane = new JScrollPane(mReplaceField);

		Font boldFont = mSearchButton.getFont().deriveFont(Font.BOLD);

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
		searchPanel.add(new JLabel("Search", boldFont, 50), BorderLayout.WEST);
		searchPanel.add(searchPane, BorderLayout.CENTER);

		JPanel replacePanel = new JPanel(new BorderLayout(0, 0));
		replacePanel.add(new JLabel("Replace", boldFont, 50), BorderLayout.WEST);
		replacePanel.add(replacePane, BorderLayout.CENTER);

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

		if(command.equals("search") || command.equals("searchforwards") || command.equals("searchbackwards"))
		{
			if (command.equals("searchforwards"))
			{
				forward = true;
			}
			else if (command.equals("searchbackwards"))
			{
				forward = false;
			}
			if (editor.findText(search, forward, caseSensative, wrapSearch, wholeWordsOnly))
			{
				editor.repaint();
			}
			else
			{
				JOptionPane.showMessageDialog(null, "No search matches was found.", "Search and Replace", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		else if (command.equals("replace"))
		{
			if (editor.isTextSelected())
			{
				editor.replaceSelection(replace);
				editor.repaint();
			}
		}
		else if (command.equals("replaceall"))
		{
			Point caretPosition = new Point(editor.getCaret().getVirtualPosition());

			int count = 0;
			while (editor.findText(search, forward, caseSensative, wrapSearch, wholeWordsOnly))
			{
				editor.replaceSelection(replace);
				count++;
			}

			editor.getCaret().moveAbsolute(caretPosition.x, caretPosition.y, true, false, true);
			editor.repaint();
		}
		else if (command.equals("replaceandsearch"))
		{
			if (editor.isTextSelected())
			{
				editor.replaceSelection(replace);
			}

			editor.repaint();
		}
		else
		{
			System.out.println("Unrecognized command:" + command);
		}
	}


	class JLabel extends javax.swing.JLabel
	{
		JLabel(String aLabel, Font aFont, int aWidth)
		{
			super(aLabel);
			setForeground(Color.WHITE);
			setFont(aFont);
			setPreferredSize(new Dimension(15, 50));
		}


		@Override
		public void paintComponent(Graphics g)
		{
//			Image image;
//			if (getText().equals("Search"))
//			{
//				image = new ImageIcon(FindDialog.class.getResource("resources/search_label.gif").getPath()).getImage();
//			}
//			else
//			{
//				image = new ImageIcon(FindDialog.class.getResource("resources/replace_label.gif").getPath()).getImage();
//			}

			g.setColor(Color.WHITE);
//			g.drawImage(image, 1, (getHeight() - image.getHeight(null)) / 2, null);
		}
	}


	class JCheckBox extends javax.swing.JCheckBox
	{
		JCheckBox(String aLabel, boolean aChecked)
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
