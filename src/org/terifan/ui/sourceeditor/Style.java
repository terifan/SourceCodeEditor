package org.terifan.ui.sourceeditor;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;


public class Style
{
	private boolean mStrikethrough;
	private boolean mUnderlined;
	private boolean mFontMonospaced;
	private boolean mBackgroundOptional;
	private Color mBackground;
	private Color mForeground;
	private Font mFont;
	private int mFontAscent;
	private int mFontDescent;
	private int mFontHeight;
	private int mFontLeading;
	private int mFontStrikethroughOffset;
	private int mFontStrikethroughThickness;
	private int mFontUnderlineOffset;
	private int mFontUnderlineThickness;
	private String mName;
	private boolean mSupportHighlight;

	private int mHashCode;

//	private int [] mCharWidths;
//	private int [][] mBiCharWidths;
//	private static HashMap mCache = new HashMap();

	/**
	 * Constructs a new Style.
	 *
	 * @param aName
	 *    A human readable name of this style.
	 * @param aFont
	 *    The font used.
	 * @param aForeground
	 *    The foreground color of this style.
	 * @param aBackground
	 *    The background color of this style.
	 * @param aUnderlined
	 *    Enables or disables underlining in this style.
	 * @param aStrikethrough
	 *    Enables or disables strikethrough in this style.
	 * @param aBackgroundOptional
	 *    Markes the background color as optional.
	 */
	public Style(String aName, Font aFont, Color aForeground, Color aBackground, boolean aUnderlined, boolean aStrikethrough, boolean aBackgroundOptional, boolean aSupportHighlight)
	{
		initialize(aName, aFont, aForeground, aBackground, aUnderlined, aStrikethrough, aBackgroundOptional, aSupportHighlight);
	}


	public void initialize(String aName, Font aFont, Color aForeground, Color aBackground, boolean aUnderlined, boolean aStrikethrough, boolean aBackgroundOptional, boolean aSupportHighlight)
	{
		mName = aName;
		mFont = aFont;
		mForeground = aForeground;
		mBackground = aBackground;
		mUnderlined = aUnderlined;
		mStrikethrough = aStrikethrough;
		mBackgroundOptional = aBackgroundOptional;
		mSupportHighlight = aSupportHighlight;

		init();
	}

	/**
	 * Initializes the internal state of this object.
	 */
	private void init()
	{
		FontRenderContext frc = new FontRenderContext(null, false, false);
		LineMetrics lm = mFont.getLineMetrics("", frc);

		mFontHeight = (int)Math.ceil(lm.getHeight());
		mFontDescent = (int)Math.round(lm.getDescent());
		mFontAscent = (int)Math.round(lm.getAscent());
		mFontUnderlineOffset = (int)Math.round(lm.getUnderlineOffset());
		mFontUnderlineThickness = (int)Math.round(lm.getUnderlineThickness());
		mFontStrikethroughOffset = (int)Math.round(lm.getStrikethroughOffset());
		mFontStrikethroughThickness = (int)Math.round(lm.getStrikethroughThickness());
		mFontLeading = (int)Math.round(lm.getLeading());
		mFontMonospaced = true;

		int w0 = (int)mFont.getStringBounds("m", frc).getWidth();

		for (int i=0; i<256; i++)
		{
			if (mFont.canDisplay((char)i))
			{
				int w = (int)mFont.getStringBounds(Character.toString((char)i), frc).getWidth();
				if (w != w0)
				{
					mFontMonospaced = false;
				}
			}
		}

/*
		if (mCache.containsKey(mFont))
		{
			Style s = (Style)mCache.get(mFont);
			mCharWidths = s.mCharWidths;
			mBiCharWidths = s.mBiCharWidths;
			mFontMonospaced = s.mFontMonospaced;
		}
		else
		{
			mCharWidths = new int[256];
			mBiCharWidths = new int[256][256];

			int w0 = (int)mFont.getStringBounds("m", frc).getWidth();

			for (int i=0; i<256; i++)
			{
				if (mFont.canDisplay((char)i))
				{
					int w = mCharWidths[i] = (int)mFont.getStringBounds(Character.toString((char)i), frc).getWidth();
					
					if (w != w0)
					{
						mFontMonospaced = false;
					}
	
					for (int j=0; j<256; j++)
					{
						if (mFont.canDisplay((char)j))
						{
							mBiCharWidths[i][j] = (int)Math.ceil(mFont.getStringBounds((char)i + "" + (char)j, frc).getWidth()) - w;
						}
					}
				}
			}

			mCache.put(mFont, this);
		}
*/
	}
	
	/**
	 * Returns true if the font in this style is monospaced.
	 */
	public boolean isFontMonospaced()
	{
		return mFontMonospaced;
	}

	/**
	 * Returns the total advance width for showing the specified String in this Style.
	 */
	public int getStringWidth(String aText)
	{
		FontRenderContext frc = new FontRenderContext(null, false, false);
		return (int)mFont.getStringBounds(aText, frc).getWidth();

/*
		char [] chars = aText.toCharArray();
		char prev = chars[0];
		int width = mCharWidths[prev];

		for (int i = 1, len = chars.length; i < len; i++)
		{
			char next = chars[i];
			width += mBiCharWidths[prev][next];
			prev = next;
		}

		return width;
*/
	}


	/**
	 * Returns the advance width showing the specified character in this Style.
	 */
	public int getCharWidth(char aChar)
	{
		return getStringWidth(Character.toString(aChar));
	}

	/**
	 * Returns the height of the text. The height is equal to the sum of the 
	 * ascent, the descent and the leading.
	 */
	public int getFontHeight()
	{
		return mFontHeight;
	}

	/**
	 * Returns the ascent of the text. The ascent is the distance from the 
	 * baseline to the ascender line. The ascent usually represents the the 
	 * height of the capital letters of the text. Some characters can extend 
	 * above the ascender line.
	 */
	public int getFontAscent()
	{
		return mFontAscent;
	}

	/**
	 * Returns the descent of the text. The descent is the distance from the 
	 * baseline to the descender line. The descent usually represents the 
	 * distance to the bottom of lower case letters like 'p'. Some characters 
	 * can extend below the descender line.
	 */
	public int getFontDescent()
	{
		return mFontDescent;
	}

	/**
	 * Returns the leading of the text. The leading is the recommended 
	 * distance from the bottom of the descender line to the top of the next 
	 * line.
	 */
	public int getFontLeading()
	{
		return mFontLeading;
	}

	/**
	 * Returns the position of the strike-through line relative to the 
	 * baseline.
	 */
	public int getStrikethroughOffset()
	{
		return mFontStrikethroughOffset;
	}

	/**
	 * Returns the thickness of the strike-through line.
	 */ 
	public int getStrikethroughThickness()
	{
		return mFontStrikethroughThickness;
	}

	/**
	 * Returns the position of the underline relative to the baseline.
	 */ 
	public int getUnderlineOffset()
	{
		return mFontUnderlineOffset;
	}

	/**
	 * Returns the thickness of the underline.
	 */ 
	public int getUnderlineThickness()
	{
		return mFontUnderlineThickness;
	}

	/**
	 * Enables or disables strikethrough in this style.
	 */ 
	public void setStrikethrough(boolean aStrikethrough)
	{
		mStrikethrough = aStrikethrough;
	}

	/**
	 * Returns true if the background is optional.
	 */ 
	public boolean isBackgroundOptional()
	{
		return mBackgroundOptional;
	}

	/**
	 * Enables or disables underlining in this style.
	 */ 
	public void setBackgroundOptional(boolean aBackgroundOptional)
	{
		mBackgroundOptional = aBackgroundOptional;
	}

	/**
	 * Returns true if this style is underlined.
	 */ 
	public boolean isUnderlined()
	{
		return mUnderlined;
	}

	/**
	 * Enables or disables underlining in this style.
	 */ 
	public void setUnderlined(boolean aUnderlined)
	{
		mUnderlined = aUnderlined;
	}

	/**
	 * Gets the foreground color in this style.
	 */ 
	public Color getForeground()
	{
		return mForeground;
	}

	/**
	 * Sets the foreground color in this style.
	 */ 
	public void setForeground(Color aColor)
	{
		mForeground = aColor;
	}

	/**
	 * Gets the background color in this style.
	 */ 
	public Color getBackground()
	{
		return mBackground;
	}

	/**
	 * Sets the background color in this style.
	 */ 
	public void setBackground(Color aColor)
	{
		mBackground = aColor;
	}

	/**
	 * Gets the font in this style.
	 */ 
	public Font getFont()
	{
		return mFont;
	}

	/**
	 * Sets the font in this style.
	 */ 
	public void setFont(Font aFont)
	{
		mFont = aFont;

		init();
	}

	/**
	 * Gets the name of this style.
	 */ 
	public String getName()
	{
		return mName;
	}

	/**
	 * Sets the name of this style.
	 */ 
	public void setName(String aName)
	{
		mName = aName;
	}

	/**
	 * Returns the point size of this Font, rounded to an integer.
	 */
	public int getFontSize()
	{
		return mFont.getSize();
	}

	/**
	 * Sets the point size of this Font.
	 */
	public void setFontSize(int aFontSize)
	{
		mFont = mFont.deriveFont((float)aFontSize);
		init();
	}


	public boolean isSupportHighlight()
	{
		return mSupportHighlight;
	}


	public void setSupportHighlight(boolean aState)
	{
		mSupportHighlight = aState;
	}


	@Override
	public String toString()
	{
		return mName;
	}


	public boolean similar(Style other, boolean aOptimizeWhitespace)
	{
		if (this.mName.equals(SyntaxParser.WHITESPACE))
		{
			return aOptimizeWhitespace && other.mName.equals(SyntaxParser.WHITESPACE);
		}
		if (other.mName.equals(SyntaxParser.WHITESPACE))
		{
			return false;
		}
		if (this.mHashCode != other.mHashCode)
		{
			return false;
		}
		if (this.mStrikethrough != other.mStrikethrough)
		{
			return false;
		}
		if (this.mUnderlined != other.mUnderlined)
		{
			return false;
		}
		if (this.mFontMonospaced != other.mFontMonospaced)
		{
			return false;
		}
		if (this.mBackgroundOptional != other.mBackgroundOptional)
		{
			return false;
		}
		if (this.mBackground != other.mBackground && (this.mBackground == null || !this.mBackground.equals(other.mBackground)))
		{
			return false;
		}
		if (this.mForeground != other.mForeground && (this.mForeground == null || !this.mForeground.equals(other.mForeground)))
		{
			return false;
		}
		if (this.mFont != other.mFont && (this.mFont == null || !this.mFont.equals(other.mFont)))
		{
			return false;
		}
		if (this.mFontStrikethroughOffset != other.mFontStrikethroughOffset)
		{
			return false;
		}
		if (this.mFontStrikethroughThickness != other.mFontStrikethroughThickness)
		{
			return false;
		}
		if (this.mFontUnderlineOffset != other.mFontUnderlineOffset)
		{
			return false;
		}
		if (this.mFontUnderlineThickness != other.mFontUnderlineThickness)
		{
			return false;
		}
//		if (this.mSupportHighlight != other.mSupportHighlight)
//		{
//			return false;
//		}
		return true;
	}


	@Override
	public int hashCode()
	{
		int hash = 3;
		hash = 79 * hash + (this.mStrikethrough ? 1 : 0);
		hash = 79 * hash + (this.mUnderlined ? 1 : 0);
		hash = 79 * hash + (this.mFontMonospaced ? 1 : 0);
		hash = 79 * hash + (this.mBackgroundOptional ? 1 : 0);
		hash = 79 * hash + (this.mBackground != null ? this.mBackground.hashCode() : 0);
		hash = 79 * hash + (this.mForeground != null ? this.mForeground.hashCode() : 0);
		hash = 79 * hash + (this.mFont != null ? this.mFont.hashCode() : 0);
		hash = 79 * hash + this.mFontStrikethroughOffset;
		hash = 79 * hash + this.mFontStrikethroughThickness;
		hash = 79 * hash + this.mFontUnderlineOffset;
		hash = 79 * hash + this.mFontUnderlineThickness;
		hash = 79 * hash + (this.mSupportHighlight ? 1 : 0);
		return hash;
	}
}