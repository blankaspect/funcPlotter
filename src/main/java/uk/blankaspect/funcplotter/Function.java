/*====================================================================*\

Function.java

Function class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;

import uk.blankaspect.common.exception.UnexpectedRuntimeException;

import uk.blankaspect.ui.swing.colour.ColourUtils;

//----------------------------------------------------------------------


// FUNCTION CLASS


class Function
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final	char	SEPARATOR_CHAR	= ';';

	public enum Highlight
	{
		NONE,
		HIGHLIGHTED,
		OBSCURED
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public Function(Color      colour,
					Expression expression)
	{
		this(colour, expression, false);
	}

	//------------------------------------------------------------------

	public Function(Color      colour,
					Expression expression,
					boolean    hidden)
	{
		this.colour = colour;
		this.expression = expression;
		this.hidden = hidden;
		highlight = Highlight.NONE;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Function clone()
	{
		try
		{
			Function copy = (Function)super.clone();
			copy.colour = ColourUtils.copy(colour);
			try
			{
				copy.expression = new Expression(expression.toString());
			}
			catch (Expression.Exception e)
			{
				// no exception is thrown
			}
			return copy;
		}
		catch (CloneNotSupportedException e)
		{
			throw new UnexpectedRuntimeException(e);
		}
	}

	//------------------------------------------------------------------

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Function)
		{
			Function function = (Function)obj;
			return (colour.equals(function.colour) && expression.equals(function.expression));
		}
		return false;
	}

	//------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		return (colour.hashCode() * 31 + expression.hashCode());
	}

	//------------------------------------------------------------------

	@Override
	public String toString()
	{
		return expression.toString();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Color getColour()
	{
		return colour;
	}

	//------------------------------------------------------------------

	public Expression getExpression()
	{
		return expression;
	}

	//------------------------------------------------------------------

	public boolean isHidden()
	{
		return hidden;
	}

	//------------------------------------------------------------------

	public Highlight getHighlight()
	{
		return highlight;
	}

	//------------------------------------------------------------------

	public boolean isHighlighted()
	{
		return (highlight == Highlight.HIGHLIGHTED);
	}

	//------------------------------------------------------------------

	public boolean isObscured()
	{
		return (highlight == Highlight.OBSCURED);
	}

	//------------------------------------------------------------------

	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}

	//------------------------------------------------------------------

	public void setHighlight(Highlight highlight)
	{
		this.highlight = highlight;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Color		colour;
	private	Expression	expression;
	private	boolean		hidden;
	private	Highlight	highlight;

}

//----------------------------------------------------------------------
