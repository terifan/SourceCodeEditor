package org.terifan.sourcecodeeditor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;


public final class Document implements Serializable
{
	private final static long serialVersionUID = 1L;
	private final static boolean DEBUG = false;

	private transient SourceEditor mSourceEditor;
	private transient UndoableEdit mUndoableEdit;
	private transient UndoManager mUndoManager;
	private ArrayList<String> mSourceLines;
	private boolean mModified;
	private int mUndoableEditDepth;


	public Document()
	{
		mUndoManager = new UndoManager();
		mSourceLines = new ArrayList<>();

		new Add(0, "").redo();
	}


	public Document(Document aSource) throws IOException
	{
		this();

		mSourceLines.clear();
		mSourceLines.addAll(aSource.mSourceLines);

		mModified = false;
	}


	public Document(File aFile) throws IOException
	{
		this();

		try	(Reader in = new FileReader(aFile))
		{
			load(in);
		}

		mModified = false;
	}


	public Document(String aText)
	{
		this();

		setText(aText);

		mModified = false;
	}


	public UndoManager getUndoManager()
	{
		return mUndoManager;
	}


	public void setParent(SourceEditor aSourceEditor)
	{
		mSourceEditor = aSourceEditor;
	}


	public void setText(String aText)
	{
		if (aText == null)
		{
			throw new IllegalArgumentException("aText is null");
		}

		try
		{
			load(new StringReader(aText));
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}


	public void load(Reader aReader) throws IOException
	{
		if (aReader == null)
		{
			throw new IllegalArgumentException("aReader is null");
		}

		ArrayList<String> text = new ArrayList<>();

		LineNumberReader in = new LineNumberReader(aReader);

		for (String s; (s = in.readLine()) != null;)
		{
			text.add(s);
		}

		if (text.isEmpty())
		{
			text.add("");
		}

		Load load = new Load(text);
		if (mUndoableEdit != null)
		{
			mUndoableEdit.addAction(load);
		}
		load.redo();
	}


	// TODO: use line break property
	public int getSizeInBytes()
	{
		int size = 0;

		for (int i = mSourceLines.size(); --i >= 0;)
		{
			size += mSourceLines.get(i).length() + 1;
		}

		size -= 1;

		return size;
	}


	protected boolean isModified()
	{
		return mModified;
	}


	protected void setModified(boolean aModified)
	{
		mModified = aModified;
	}


	protected int getLongestLineLength()
	{
		int longestLine = 0;
		int tabSize = mSourceEditor.getTabSize();

		for (int lineIndex = mSourceLines.size(); --lineIndex >= 0;)
		{
			String s = mSourceLines.get(lineIndex);

			int offset = s.indexOf('\t');

			if (offset == -1)
			{
				int l = s.length();
				if (l > longestLine)
				{
					longestLine = l;
				}
			}
			else
			{
				int lineLength = offset + tabSize - (offset % tabSize);
				while (true)
				{
					int prevOffset = offset;
					offset = s.indexOf('\t', offset + 1);
					if (offset == -1)
					{
						int x = s.length() - prevOffset - 1;
						lineLength += x;
						break;
					}

					int x = (offset - prevOffset - 1) + tabSize - ((offset - prevOffset - 1) % tabSize);
					lineLength += x;
				}

				if (lineLength > longestLine)
				{
					longestLine = lineLength;
				}
			}
		}

		return longestLine;
	}


	public void removeSpan(int aLineIndex, int aStartColumn)
	{
		replaceLine(aLineIndex, mSourceLines.get(aLineIndex).substring(0, aStartColumn));
	}


	public void removeSpan(int aLineIndex, int aStartColumn, int aEndColumn)
	{
		assert aLineIndex >= 0 && aLineIndex < mSourceLines.size();
		assert aStartColumn >= 0 && aStartColumn <= getLineLength(aLineIndex);

		String text = mSourceLines.get(aLineIndex);
		replaceLine(aLineIndex, text.substring(0, aStartColumn) + text.substring(aEndColumn));
	}


	public void removeLine(int aLineIndex)
	{
		Remove remove = new Remove(aLineIndex);
		if (mUndoableEdit != null)
		{
			mUndoableEdit.addAction(remove);
		}
		remove.redo();
	}


	public void removeAllLines()
	{
		Load load = new Load(new ArrayList<>());
		if (mUndoableEdit != null)
		{
			mUndoableEdit.addAction(load);
		}
		load.redo();
	}


	public void replaceSpan(int aLineIndex, int aStartColumn, String aText)
	{
		if (aText.length() == 0)
		{
			removeSpan(aLineIndex, aStartColumn);
			return;
		}
		replaceLine(aLineIndex, mSourceLines.get(aLineIndex).substring(0, aStartColumn) + aText);
	}


	public void replaceSpan(int aLineIndex, int aStartColumn, int aEndColumn, String aText)
	{
		if (aText.length() == 0)
		{
			removeSpan(aLineIndex, aStartColumn, aEndColumn);
			return;
		}
		replaceLine(aLineIndex, mSourceLines.get(aLineIndex).substring(0, aStartColumn) + aText + mSourceLines.get(aLineIndex).substring(aEndColumn));
	}


	public void replaceLine(int aLineIndex, String aText)
	{
		assert aLineIndex >= 0 && aLineIndex < mSourceLines.size();

		Set set = new Set(aLineIndex, aText);
		if (mUndoableEdit != null)
		{
			mUndoableEdit.addAction(set);
		}
		set.redo();
	}


	public void appendSpan(final int aLineIndex, final String aText)
	{
		if (aText.length() == 0)
		{
			return;
		}
		replaceLine(aLineIndex, mSourceLines.get(aLineIndex) + aText);
	}


	public void appendLine(String aText)
	{
		insertLine(mSourceLines.size(), aText);
	}


	public void insertSpan(final int aLineIndex, final int aStartColumn, final String aText)
	{
		if (aText.length() == 0)
		{
			return;
		}
		replaceLine(aLineIndex, mSourceLines.get(aLineIndex).substring(0, aStartColumn) + aText + mSourceLines.get(aLineIndex).substring(aStartColumn));
	}


	public void insertLine(int aLineIndex, String aText)
	{
		Add add = new Add(aLineIndex, aText);
		if (mUndoableEdit != null)
		{
			mUndoableEdit.addAction(add);
		}
		add.redo();
	}


	public void concatLines(int aStartLineIndex, int aEndLineIndex)
	{
		replaceLine(aStartLineIndex, mSourceLines.get(aStartLineIndex) + mSourceLines.get(aEndLineIndex));
	}


	public void splitLine(int aLineIndex, int aColumn)
	{
		insertLine(aLineIndex + 1, mSourceLines.get(aLineIndex).substring(aColumn));
		replaceLine(aLineIndex, mSourceLines.get(aLineIndex).substring(0, aColumn));
	}


	public char getCharAt(int aLineIndex, int aColumn)
	{
		return mSourceLines.get(aLineIndex).charAt(aColumn);
	}


	public String getSpan(int aLineIndex, int aStartColumn)
	{
		return mSourceLines.get(aLineIndex).substring(aStartColumn);
	}


	public String getLine(int aLineIndex)
	{
		if (aLineIndex < 0 || aLineIndex >= mSourceLines.size())
		{
			throw new IllegalArgumentException("Line not found: " + aLineIndex+", size: "+mSourceLines.size());
		}
		return mSourceLines.get(aLineIndex);
	}


	public int getLineLength(int aLineIndex)
	{
		return mSourceLines.get(aLineIndex).length();
	}


	public int getLineCount()
	{
		return mSourceLines.size();
	}


	@Override
	public String toString()
	{
		String lineBreak = System.getProperty("line.separator");
		if (lineBreak == null || lineBreak.isEmpty())
		{
			lineBreak = "\n";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0, sz = mSourceLines.size(); i < sz; i++)
		{
			sb.append(mSourceLines.get(i)).append(lineBreak);
		}
		return sb.toString();
	}


	// called by java.lang.ObjectInputStream when deserialzing an instance
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		mUndoManager = new UndoManager();
	}


	public void beginUndoableEdit(UndoableEdit aUndoableEdit)
	{
		if (mUndoableEditDepth == 0)
		{
			mUndoableEdit = aUndoableEdit;
			mUndoableEdit.saveStartState();
		}
		else
		{
			mUndoableEdit.addSubAction(aUndoableEdit.getPresentationName());
		}
		mUndoableEditDepth++;

		if (DEBUG) System.out.println("Begin "+mUndoableEdit.getPresentationName());
	}


	public void commitUndoableEdit()
	{
		mUndoableEditDepth--;
		if (mUndoableEditDepth == 0)
		{
			if (mUndoableEdit != null && !mUndoableEdit.isEmpty())
			{
				mUndoableEdit.saveEndState();
				mUndoManager.addEdit(mUndoableEdit);
			}
		}
		else if (mUndoableEditDepth < 0)
		{
			throw new IllegalStateException("More calls made to commit than to begin.");
		}

		if (DEBUG) System.out.println("Commit "+mUndoableEdit.getPresentationName());
	}


	class Add implements UndoableAction
	{
		private int mLineIndex;
		private String mText;

		private Add(int aLineIndex, String aText)
		{
			mLineIndex = aLineIndex;
			mText = aText;

			if (DEBUG) System.out.println("\tAdd "+mLineIndex+" "+mText);
		}

		@Override
		public void undo()
		{
			mSourceLines.remove(mLineIndex);
		}

		@Override
		public void redo()
		{
			mSourceLines.add(mLineIndex, mText);
		}
	}


	class Set implements UndoableAction
	{
		private final int mLineIndex;
		private final String mText;
		private final String mOldText;

		private Set(int aLineIndex, String aText)
		{
			mLineIndex = aLineIndex;
			mText = aText;
			mOldText = mSourceLines.get(aLineIndex);

			if (DEBUG) System.out.println("\tSet "+mLineIndex+" "+mText);
		}

		@Override
		public void undo()
		{
			mSourceLines.set(mLineIndex, mOldText);
		}

		@Override
		public void redo()
		{
			mSourceLines.set(mLineIndex, mText);
		}
	}


	class Remove implements UndoableAction
	{
		private final int mLineIndex;
		private final String mOldText;

		private Remove(int aLineIndex)
		{
			mLineIndex = aLineIndex;
			mOldText = mSourceLines.get(aLineIndex);

			if (DEBUG) System.out.println("\tRemove "+mLineIndex+" "+mOldText);
		}

		@Override
		public void undo()
		{
			mSourceLines.add(mLineIndex, mOldText);
		}

		@Override
		public void redo()
		{
			mSourceLines.remove(mLineIndex);
		}
	}


	class Load implements UndoableAction
	{
		private final ArrayList<String> mText;
		private final ArrayList<String> mOldText;

		private Load(ArrayList<String> aText)
		{
			mText = new ArrayList<>(aText);
			mOldText = new ArrayList<>(mSourceLines);
		}

		@Override
		public void undo()
		{
			mSourceLines.clear();
			mSourceLines.addAll(mOldText);
		}

		@Override
		public void redo()
		{
			mSourceLines.clear();
			mSourceLines.addAll(mText);
		}
	}


	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		return this.toString().equals(obj.toString());
	}


	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
}