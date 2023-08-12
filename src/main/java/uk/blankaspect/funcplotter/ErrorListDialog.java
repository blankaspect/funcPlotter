/*====================================================================*\

ErrorListDialog.java

Error list dialog class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Window;

import java.util.List;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import uk.blankaspect.ui.swing.dialog.NonEditableTextPaneDialog;

import uk.blankaspect.ui.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// ERROR LIST DIALOG CLASS


class ErrorListDialog
	extends NonEditableTextPaneDialog
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	MAX_NUM_ERRORS	= FunctionDocument.MAX_NUM_FUNCTIONS;

	private static final	int	NUM_COLUMNS	= 72;
	private static final	int	NUM_ROWS	= MAX_NUM_ERRORS + 1;

	private static final	String	FIRST_ERRORS_STR	= " : First " + MAX_NUM_ERRORS + " errors";

	private static final	String	KEY	= ErrorListDialog.class.getCanonicalName();

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// SPAN STYLE


	private enum SpanStyle
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		PATHNAME
		(
			"pathname",
			Color.BLACK
		)
		{
			@Override
			protected void apply(Style style)
			{
				StyleConstants.setForeground(style, getColour());
				StyleConstants.setBold(style, true);
			}
		},

		LINE_NUMBER
		(
			"lineNumber",
			new Color(0, 0, 160)
		)
		{
			@Override
			protected void apply(Style style)
			{
				StyleConstants.setForeground(style, getColour());
			}
		},

		ERROR
		(
			"error",
			new Color(208, 64, 0)
		)
		{
			@Override
			protected void apply(Style style)
			{
				StyleConstants.setForeground(style, getColour());
			}
		};

		//--------------------------------------------------------------

		private static final	String	PREFIX	= "span.";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SpanStyle(String key,
						  Color  colour)
		{
			this.key = PREFIX + key;
			this.colour = colour;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract void apply(Style style);

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		protected Color getColour()
		{
			return colour;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;
		private	Color	colour;

	}

	//==================================================================


	// PARAGRAPH STYLE


	private enum ParagraphStyle
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		PATHNAME
		(
			"pathname"
		)
		{
			@Override
			protected void apply(Style style)
			{
				StyleConstants.setSpaceBelow(style, (float)StyleConstants.getFontSize(style) * 0.5f);
			}
		};

		//--------------------------------------------------------------

		private static final	String	PREFIX	= "paragraph.";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ParagraphStyle(String key)
		{
			this.key = PREFIX + key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract void apply(Style style);

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private ErrorListDialog(Window owner,
							String titleStr)
	{
		// Call superclass constructor
		super(owner, titleStr, KEY, NUM_COLUMNS, NUM_ROWS);

		// Add styles
		for (SpanStyle spanStyle : SpanStyle.values())
			spanStyle.apply(addStyle(spanStyle.key, getDefaultStyle()));
		for (ParagraphStyle paragraphStyle : ParagraphStyle.values())
			paragraphStyle.apply(addStyle(paragraphStyle.key, getDefaultStyle()));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void showDialog(Component    parent,
								  String       titleStr,
								  String       pathname,
								  List<String> errorStrs)
	{
		if (errorStrs.size() > MAX_NUM_ERRORS)
			titleStr += FIRST_ERRORS_STR;

		ErrorListDialog dialog = new ErrorListDialog(GuiUtils.getWindow(parent), titleStr);
		Paragraph paragraph = new Paragraph(ParagraphStyle.PATHNAME.key);
		paragraph.add(new Span(pathname, SpanStyle.PATHNAME.key));
		dialog.append(paragraph);

		int numErrors = Math.min(errorStrs.size(), MAX_NUM_ERRORS);
		for (int i = 0; i < numErrors; i++)
		{
			paragraph = new Paragraph(StyleContext.DEFAULT_STYLE);
			String[] strs = errorStrs.get(i).split(": ", 2);
			paragraph.add(new Span(strs[0] + ": ", SpanStyle.LINE_NUMBER.key));
			paragraph.add(new Span(strs[1], SpanStyle.ERROR.key));
			dialog.append(paragraph);
		}

		dialog.setCaretToStart();
		dialog.setVisible(true);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
