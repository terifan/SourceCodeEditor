package org.terifan.ui.sourceeditor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;


public class StringTokenizer implements Iterable<String>
{
	private boolean mEndReached;
	private int mDelimiter;
	private Reader mSource;
	private String mNext = null;
	private StringBuilder mBuffer = new StringBuilder();


	public StringTokenizer(InputStream aSource, char aDelimiter)
	{
		mSource = new InputStreamReader(aSource);
		mDelimiter = (int)aDelimiter;
	}


	public StringTokenizer(Reader aSource, char aDelimiter)
	{
		mSource = aSource;
		mDelimiter = (int)aDelimiter;
	}


	public StringTokenizer(String aSource, char aDelimiter)
	{
		mSource = new StringReader(aSource);
		mDelimiter = (int)aDelimiter;
	}


	public boolean hasNext()
	{
		fetch();
		if (mEndReached && mNext == null)
		{
			return false;
		}
		return mNext != null;
	}


	public String next()
	{
		fetch();
		if (mEndReached && mNext == null)
		{
			return null;
		}
		String temp = mNext;
		mNext = null;
		return temp;
	}


	@Override
	public Iterator<String> iterator()
	{
		return new Iterator<String>()
		{
			@Override
			public boolean hasNext()
			{
				return StringTokenizer.this.hasNext();
			}
			@Override
			public String next()
			{
				return StringTokenizer.this.next();
			}
			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}


	private void fetch()
	{
		if (mNext != null || mEndReached)
		{
			return;
		}

		mBuffer.setLength(0);

		try
		{
			while (true)
			{
				int c = mSource.read();

				if (c == -1)
				{
					mEndReached = true;
					break;
				}
				if (c == mDelimiter)
				{
					break;
				}
				mBuffer.append((char)c);
			}
			if (mBuffer.length() == 0 && mEndReached)
			{
				mNext = "";
				return;
			}
			mNext = mBuffer.toString();
		}
		catch (IOException e)
		{
			mEndReached = true;
		}
	}
}