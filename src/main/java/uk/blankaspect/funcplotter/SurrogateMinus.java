/*====================================================================*\

SurrogateMinus.java

Surrogate minus class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Font;

import javax.swing.JOptionPane;

import javax.swing.text.BadLocationException;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.textfield.ConstrainedTextField;

//----------------------------------------------------------------------


// SURROGATE MINUS CLASS


class SurrogateMinus
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	char[]	MINUS_CHARS	=
	{
		'\u2212',   // minus sign
		'\u2012',   // figure dash
		'\u2013'    // en dash
	};

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// SURROGATE MINUS TEXT FIELD CLASS


	public static class Field
		extends ConstrainedTextField
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public Field(int maxLength,
					 int numColumns)
		{
			super(maxLength, numColumns);
			AppFont.TEXT_FIELD.apply(this);
			GuiUtils.setTextComponentMargins(this);
			minusChar = getMinusChar(getFont());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected String translateInsertString(String str,
											   int    offset)
		{
			return minusToSurrogate(str, minusChar);
		}

		//--------------------------------------------------------------

		@Override
		public void cut()
		{
			copySelection();

			int startIndex = getSelectionStart();
			int endIndex = getSelectionEnd();
			if (startIndex < endIndex)
			{
				String str = getText();
				setText(str.substring(0, startIndex) + str.substring(endIndex));
				getCaret().setDot(startIndex);
			}
		}

		//--------------------------------------------------------------

		@Override
		public void copy()
		{
			copySelection();
		}

		//--------------------------------------------------------------

		@Override
		public String getText()
		{
			return surrogateToMinus(super.getText(), minusChar);
		}

		//--------------------------------------------------------------

		@Override
		public String getText(int offset,
							  int length)
			throws BadLocationException
		{
			return surrogateToMinus(super.getText(offset, length), minusChar);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		protected boolean isMinusCharacter(char ch)
		{
			return (ch == minusChar);
		}

		//--------------------------------------------------------------

		private void copySelection()
		{
			int startIndex = getSelectionStart();
			int endIndex = getSelectionEnd();
			if (startIndex < endIndex)
			{
				try
				{
					Utils.putClipboardText(getText().substring(startIndex, endIndex));
				}
				catch (AppException e)
				{
					JOptionPane.showMessageDialog(this, e, FuncPlotterApp.SHORT_NAME, JOptionPane.ERROR_MESSAGE);
				}
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	char	minusChar;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private SurrogateMinus()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static char getMinusChar(Font font)
	{
		for (char minusChar : MINUS_CHARS)
		{
			if (font.canDisplay(minusChar))
				return minusChar;
		}
		return '-';
	}

	//------------------------------------------------------------------

	public static String minusToSurrogate(String str,
										  char   ch)
	{
		return (str == null)
					? null
					: (ch == '-')
							? str
							: str.replace('-', ch);
	}

	//------------------------------------------------------------------

	public static String surrogateToMinus(String str,
										  char   ch)
	{
		return (str == null)
					? null
					: (ch == '-')
							? str
							: str.replace(ch, '-');
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
