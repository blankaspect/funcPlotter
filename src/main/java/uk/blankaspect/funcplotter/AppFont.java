/*====================================================================*\

AppFont.java

Application font enumeration.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.Font;

import uk.blankaspect.common.misc.IStringKeyed;

import uk.blankaspect.ui.swing.font.FontEx;
import uk.blankaspect.ui.swing.font.FontStyle;
import uk.blankaspect.ui.swing.font.FontUtils;

//----------------------------------------------------------------------


// APPLICATION FONT ENUMERATION


public enum AppFont
	implements IStringKeyed
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	MAIN
	(
		"main",
		"Main"
	),

	TEXT_FIELD
	(
		"textField",
		"Text field"
	),

	COMBO_BOX
	(
		"comboBox",
		"Combo box"
	),

	PLOT
	(
		"plot",
		"Plot",
		null,
		null,
		10
	);

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String	key;
	private	String	text;
	private	FontEx	fontEx;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		FontUtils.setAppFontClass(AppFont.class);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private AppFont(
		String	key,
		String	text)
	{
		this(key, text, null, null, 0);
	}

	//------------------------------------------------------------------

	private AppFont(
		String		key,
		String		text,
		String		name,
		FontStyle	style,
		int			size)
	{
		this.key = key;
		this.text = text;
		fontEx = new FontEx(name, style, size);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static int getNumFonts()
	{
		return values().length;
	}

	//------------------------------------------------------------------

	public static String[] getKeys()
	{
		String[] keys = new String[values().length];
		for (int i = 0; i < keys.length; i++)
			keys[i] = values()[i].key;
		return keys;
	}

	//------------------------------------------------------------------

	public static void setFontExs(FontEx[] fontExs)
	{
		for (AppFont appFont : values())
		{
			FontEx fontEx = fontExs[appFont.ordinal()];
			if (fontEx != null)
				appFont.setFontEx(fontEx);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IStringKeyed interface
////////////////////////////////////////////////////////////////////////

	public String getKey()
	{
		return key;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public String toString()
	{
		return text;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public FontEx getFontEx()
	{
		return fontEx;
	}

	//------------------------------------------------------------------

	public Font getFont()
	{
		return fontEx.toFont();
	}

	//------------------------------------------------------------------

	public void setFontEx(FontEx fontEx)
	{
		this.fontEx = fontEx;
	}

	//------------------------------------------------------------------

	public void apply(Component component)
	{
		fontEx.applyFont(component);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
