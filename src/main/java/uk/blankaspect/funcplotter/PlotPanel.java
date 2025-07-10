/*====================================================================*\

PlotPanel.java

Class: plot panel.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.blankaspect.common.range.IntegerRange;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.text.TextRendering;
import uk.blankaspect.ui.swing.text.TextUtils;

//----------------------------------------------------------------------


// CLASS: PLOT PANEL


class PlotPanel
	extends JComponent
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int		MIN_PLOT_WIDTH	= 128;
	public static final		int		MAX_PLOT_WIDTH	= 2048;

	public static final		int		MIN_PLOT_HEIGHT	= MIN_PLOT_WIDTH;
	public static final		int		MAX_PLOT_HEIGHT	= MAX_PLOT_WIDTH;

	public static final		int		MIN_NUM_Y_SCALE_DIGITS	= 4;
	public static final		int		MAX_NUM_Y_SCALE_DIGITS	= 20;

	private static final	int		LEFT_MARGIN				= 4;
	private static final	int		RIGHT_MARGIN			= 4;
	private static final	int		TOP_MARGIN				= 8;
	private static final	int		BOTTOM_MARGIN			= 4;
	private static final	int		SCALE_LINE_LENGTH		= 4;
	private static final	int		X_SCALE_TOP_MARGIN		= 1;
	private static final	int		X_SCALE_GAP				= 3;
	private static final	int		Y_SCALE_LEFT_MARGIN		= 3;
	private static final	int		MIN_PIXELS_PER_DIVISION	= 32;

	private static final	double	SCROLL_FACTOR	= 0.5;

	private static final	double	RECIP_LOG_10	= 1.0 / Math.log(10.0);

	private static final	String	ZERO_STR	= "0";

	private static final	Color	POP_UP_TEXT_COLOUR		= Color.BLACK;
	private static final	Color	POP_UP_BORDER_COLOUR	= Colours.LINE_BORDER;

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	int	numYScaleDigits;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	FunctionDocument		document;
	private	int						panelWidth;
	private	int						panelHeight;
	private	int						maxYScaleStrWidth;
	private	Insets					borderInsets;
	private	Rectangle				plotRect;
	private	PlotParams				xParams;
	private	PlotParams				yParams;
	private	List<ChangeListener>	changeListeners;
	private	ChangeEvent				changeEvent;
	private	char					minusChar;
	private	boolean					gridVisible;
	private	boolean					drawingImage;
	private	List<ScaleText>			scaleTexts;
	private	Popup					scalePopUp;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public PlotPanel(
		FunctionDocument	document,
		int					plotWidth,
		int					plotHeight,
		boolean				gridVisible)
	{
		// Set border and font
		setBorder(false);
		borderInsets = getBorder().getBorderInsets(this);
		AppFont.PLOT.apply(this);

		// Initialise class fields
		if (numYScaleDigits == 0)
			numYScaleDigits = AppConfig.INSTANCE.getNumYScaleDigits();
		setCursor(false);

		// Initialise instance variables
		this.document = document;
		this.gridVisible = gridVisible;
		minusChar = SurrogateMinus.getMinusChar(getFont());
		FontMetrics fontMetrics = getFontMetrics(getFont());
		maxYScaleStrWidth = fontMetrics.stringWidth("0".repeat(numYScaleDigits));
		int yScaleWidth = Y_SCALE_LEFT_MARGIN + maxYScaleStrWidth + SCALE_LINE_LENGTH;
		int xScaleHeight = SCALE_LINE_LENGTH + X_SCALE_TOP_MARGIN + fontMetrics.getAscent() + fontMetrics.getDescent();
		plotRect = new Rectangle(borderInsets.left + LEFT_MARGIN + yScaleWidth, borderInsets.top + TOP_MARGIN,
								 plotWidth, plotHeight);
		panelWidth = borderInsets.left + LEFT_MARGIN + yScaleWidth + plotWidth + maxYScaleStrWidth / 2
						+ RIGHT_MARGIN + borderInsets.right;
		panelHeight = borderInsets.top + TOP_MARGIN + plotHeight + xScaleHeight + BOTTOM_MARGIN + borderInsets.bottom;
		xParams = new PlotParams(document.getXInterval(), plotWidth);
		yParams = new PlotParams(document.getYInterval(), plotHeight);
		changeListeners = new ArrayList<>();
		scaleTexts = new ArrayList<>();

		// Set properties
		setOpaque(true);
		setFocusable(true);

		// Add listeners
		addFocusListener(new FocusListener()
		{
			@Override
			public void focusGained(
				FocusEvent	event)
			{
				setBorder(true);
			}

			@Override
			public void focusLost(
				FocusEvent	event)
			{
				setBorder(false);
			}
		});
		MouseAdapter mouseListener = new MouseAdapter()
		{
			@Override
			public void mouseExited(
				MouseEvent	event)
			{
				if ((event.getComponent() != PlotPanel.this) && (scalePopUp != null))
				{
					scalePopUp.hide();
					scalePopUp = null;
				}
			}

			@Override
			public void mousePressed(
				MouseEvent	event)
			{
				requestFocusInWindow();
			}
		};
		addMouseListener(mouseListener);
		addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseMoved(
				MouseEvent	event)
			{
				if (scalePopUp == null)
				{
					for (ScaleText scaleText : scaleTexts)
					{
						String str = scaleText.getText(event.getPoint());
						if (str != null)
						{
							PopUpComponent popUpComponent = new PopUpComponent(str);
							popUpComponent.addMouseListener(mouseListener);
							int x = scaleText.markX;
							if (x != 0)
								x -= popUpComponent.getPreferredSize().width / 2;
							Point location = new Point(x, scaleText.rect.y);
							SwingUtilities.convertPointToScreen(location, PlotPanel.this);
							scalePopUp = PopupFactory.getSharedInstance().getPopup(PlotPanel.this, popUpComponent,
																				   location.x, location.y);
							scalePopUp.show();
							break;
						}
					}
				}
			}
		});
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Dimension getMinimumSize()
	{
		return new Dimension(panelWidth, panelHeight);
	}

	//------------------------------------------------------------------

	@Override
	public Dimension getPreferredSize()
	{
		return getMinimumSize();
	}

	//------------------------------------------------------------------

	@Override
	protected void paintComponent(
		Graphics	gr)
	{
		// Create copy of graphics context
		Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

		// Draw component background
		Rectangle rect = gr2d.getClipBounds();
		AppConfig config = AppConfig.INSTANCE;
		gr2d.setColor(drawingImage ? config.getPlotColourImageMargin() : getBackground());
		gr2d.fillRect(rect.x, rect.y, rect.width, rect.height);

		// Draw plot background
		gr2d.setColor(config.getPlotColourBackground());
		gr2d.fillRect(plotRect.x, plotRect.y, plotRect.width, plotRect.height);

		// Set rendering hints for text antialiasing and fractional metrics
		TextRendering.setHints(gr2d);

		// Initialise local variables
		int numFractionDigits = config.getNumFractionDigits();
		IntegerRange fixedPointExponentRange = config.getFixedPointExponentRange();
		boolean applyFixedExponent = !config.isNormaliseScientificNotation();

		FontMetrics fontMetrics = gr2d.getFontMetrics();
		int fontAscent = fontMetrics.getAscent();
		int textHeight = fontMetrics.getAscent() + fontMetrics.getDescent();

		// Draw x grid lines and scale markings
		scaleTexts.clear();
		int divisionOffset = xParams.divisionOffset;
		int strEndX = 0;
		int yAxisX = -1;
		String prevStr = null;
		while (divisionOffset < plotRect.width)
		{
			int x = plotRect.x + divisionOffset;
			double value = xParams.start + (double)divisionOffset * xParams.pixelDelta;
			String str = PlotInterval.doubleToString(value, numFractionDigits, fixedPointExponentRange,
													 applyFixedExponent, xParams.intervalExponent);
			if (Math.abs(value) < 0.5 * xParams.pixelDelta)
			{
				str = ZERO_STR;
				yAxisX = x;
			}
			else
			{
				if (gridVisible)
				{
					gr2d.setColor(config.getPlotColourGrid());
					gr2d.drawLine(x, plotRect.y, x, plotRect.y + plotRect.height - 1);
				}
			}

			gr2d.setColor(config.getPlotColourScale());
			gr2d.drawLine(x, plotRect.y + plotRect.height, x, plotRect.y + plotRect.height + SCALE_LINE_LENGTH - 1);

			if (!str.equals(prevStr))
			{
				String xStr = SurrogateMinus.minusToSurrogate(str, minusChar);
				int strWidth = fontMetrics.stringWidth(xStr);
				int strX = x - strWidth / 2;
				int strY = plotRect.y + plotRect.height + SCALE_LINE_LENGTH + X_SCALE_TOP_MARGIN + fontAscent;
				if (strX < strEndX)
				{
					String limXStr = null;
					if (config.isTruncateXScaleText())
					{
						limXStr = TextUtils.getLimitedWidthString(xStr, fontMetrics, 2 * (x - strEndX),
																  TextUtils.RemovalMode.START);
						strWidth = fontMetrics.stringWidth(limXStr);
					}
					else
						strWidth = 2 * (x - strEndX);
					strX = x - strWidth / 2;
					scaleTexts.add(new ScaleText(xStr, strX, strY - fontAscent, strWidth, textHeight, x));
					xStr = limXStr;
				}
				if (xStr != null)
				{
					gr2d.drawString(xStr, strX, strY);
					strEndX = strX + strWidth + X_SCALE_GAP;
				}
			}
			prevStr = str;

			divisionOffset += xParams.pixelsPerDivision;
		}

		// Draw y grid lines and scale markings
		divisionOffset = yParams.divisionOffset;
		int xAxisY = -1;
		prevStr = null;
		while (divisionOffset < plotRect.height)
		{
			int y = plotRect.y + (plotRect.height - 1) - divisionOffset;
			double value = yParams.start + (double)divisionOffset * yParams.pixelDelta;
			String str = PlotInterval.doubleToString(value, numFractionDigits, fixedPointExponentRange,
													 applyFixedExponent, yParams.intervalExponent);
			if (Math.abs(value) < 0.5 * yParams.pixelDelta)
			{
				str = ZERO_STR;
				xAxisY = y;
			}
			else
			{
				if (gridVisible)
				{
					gr2d.setColor(config.getPlotColourGrid());
					gr2d.drawLine(plotRect.x, y, plotRect.x + plotRect.width - 1, y);
				}
			}

			gr2d.setColor(config.getPlotColourScale());
			gr2d.drawLine(plotRect.x - SCALE_LINE_LENGTH, y, plotRect.x - 1, y);

			if (!str.equals(prevStr))
			{
				String yStr = SurrogateMinus.minusToSurrogate(str, minusChar);
				String limYStr = TextUtils.getLimitedWidthString(yStr, fontMetrics, maxYScaleStrWidth,
																 TextUtils.RemovalMode.START);
				int strWidth = fontMetrics.stringWidth(limYStr);
				int strX = plotRect.x - (strWidth + Y_SCALE_LEFT_MARGIN + SCALE_LINE_LENGTH);
				int strY = y + fontAscent / 2;
				if (limYStr != yStr)
					scaleTexts.add(new ScaleText(yStr, strX, strY - fontAscent, strWidth, textHeight, 0));
				gr2d.drawString(limYStr, strX, strY);
			}
			prevStr = str;

			divisionOffset += yParams.pixelsPerDivision;
		}

		// Draw axes
		gr2d.setColor(config.getPlotColourAxis());
		if (yAxisX >= 0)
			gr2d.drawLine(yAxisX, plotRect.y, yAxisX, plotRect.y + plotRect.height - 1);
		if (xAxisY >= 0)
			gr2d.drawLine(plotRect.x, xAxisY, plotRect.x + plotRect.width - 1, xAxisY);

		// Plot functions
		Graphics plotGr = gr2d.create(plotRect.x, plotRect.y, plotRect.width, plotRect.height);
		for (int i = document.getNumFunctions() - 1; i >= 0; i--)
		{
			Function function = document.getFunction(i);
			if (!function.isHidden() && (!function.isObscured() || config.isFunctionObscuredColour()))
			{
				plotGr.setColor(function.isObscured() ? config.getFunctionObscuredColour() : function.getColour());
				drawExpression(plotGr, function.getExpression(), 0, plotRect.width);
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public boolean isGridVisible()
	{
		return gridVisible;
	}

	//------------------------------------------------------------------

	public Point2D.Double pointToCoords(
		Point	point)
	{
		Point2D.Double coords = null;
		if ((point.x >= plotRect.x) && (point.x < plotRect.x + plotRect.width) &&
			 (point.y >= plotRect.y) && (point.y < plotRect.y + plotRect.height))
			coords = new Point2D.Double(
				xParams.start + (double)(point.x - plotRect.x) * xParams.pixelDelta,
				yParams.start + (double)(plotRect.y + plotRect.height - 1 - point.y) * yParams.pixelDelta);
		return coords;
	}

	//------------------------------------------------------------------

	public double getXScrollIncrement()
	{
		return xParams.divisionDelta * SCROLL_FACTOR;
	}

	//------------------------------------------------------------------

	public double getXScrollIncrement(
		int	pixels)
	{
		return (double)((pixels << 1) / xParams.pixelsPerDivision) * getXScrollIncrement();
	}

	//------------------------------------------------------------------

	public double getYScrollIncrement()
	{
		return yParams.divisionDelta * SCROLL_FACTOR;
	}

	//------------------------------------------------------------------

	public double getYScrollIncrement(
		int	pixels)
	{
		return (double)((pixels << 1) / yParams.pixelsPerDivision) * getYScrollIncrement();
	}

	//------------------------------------------------------------------

	public void setXInterval(
		PlotInterval	interval)
	{
		PlotParams newXParams = new PlotParams(interval, plotRect.width);
		if (!xParams.equals(newXParams))
		{
			xParams = newXParams;
			repaint();
			fireStateChanged();
		}
	}

	//------------------------------------------------------------------

	public void setYInterval(
		PlotInterval	interval)
	{
		PlotParams newYParams = new PlotParams(interval, plotRect.height);
		if (!yParams.equals(newYParams))
		{
			yParams = newYParams;
			repaint();
			fireStateChanged();
		}
	}

	//------------------------------------------------------------------

	public void setIntervals(
		PlotInterval	xInterval,
		PlotInterval	yInterval)
	{
		PlotParams newXParams = new PlotParams(xInterval, plotRect.width);
		PlotParams newYParams = new PlotParams(yInterval, plotRect.height);
		if (!xParams.equals(newXParams) || !yParams.equals(newYParams))
		{
			xParams = newXParams;
			yParams = newYParams;
			repaint();
			fireStateChanged();
		}
	}

	//------------------------------------------------------------------

	public void setCursor(
		boolean	move)
	{
		setCursor(move ? Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
					   : Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

	//------------------------------------------------------------------

	public void setGridVisible(
		boolean	visible)
	{
		if (gridVisible != visible)
		{
			gridVisible = visible;
			repaint();
		}
	}

	//------------------------------------------------------------------

	public void drawPanel(
		Graphics	gr)
	{
		gr.setFont(getFont());
		gr.setClip(0, 0, panelWidth, panelHeight);
		drawingImage = true;
		paintComponent(gr);
		drawingImage = false;
	}

	//------------------------------------------------------------------

	public void addChangeListener(
		ChangeListener	listener)
	{
		changeListeners.add(listener);
	}

	//------------------------------------------------------------------

	private void fireStateChanged()
	{
		for (int i = changeListeners.size() - 1; i >= 0; i--)
		{
			if (changeEvent == null)
				changeEvent = new ChangeEvent(this);
			changeListeners.get(i).stateChanged(changeEvent);
		}
	}

	//------------------------------------------------------------------

	private void drawExpression(
		Graphics	gr,
		Expression	expression,
		int			startX,
		int			endX)
	{
		final	long	Y_LOWER_BOUND	= Integer.MIN_VALUE >> 1;
		final	long	Y_UPPER_BOUND	= Integer.MAX_VALUE >> 1;

		double prevX = 0.0;
		double prevY = Double.NaN;
		int prevPlotY = 0;
		for (int plotX = startX - 1; plotX <= endX; plotX++)
		{
			double x = xParams.start + (double)plotX * xParams.pixelDelta;
			double y = expression.evaluate(x);
			if (!Double.isNaN(y))
			{
				int plotY = (int)Math.min(Math.max(Y_LOWER_BOUND,
												   Math.round((y - yParams.start) / yParams.pixelDelta)),
										  Y_UPPER_BOUND);
				if (!Double.isNaN(prevY))
				{
					double medianY = expression.evaluate(0.5 * (prevX + x));
					if (((medianY >= prevY) && (medianY <= y)) || ((medianY >= y) && (medianY <= prevY))
							|| (y == prevY))
						gr.drawLine(plotX - 1, (plotRect.height - 1) - prevPlotY, plotX, (plotRect.height - 1) - plotY);
				}
				prevPlotY = plotY;
			}
			prevX = x;
			prevY = y;
		}
	}

	//------------------------------------------------------------------

	private void setBorder(
		boolean	focused)
	{
		if (focused)
		{
			setBorder(BorderFactory
					.createMatteBorder(borderInsets.top, borderInsets.left, borderInsets.bottom, borderInsets.right,
									   AppConfig.INSTANCE.getPlotColourFocusedBorder()));
		}
		else
			GuiUtils.setPaddedLineBorder(this, 1);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: PLOT PARAMETERS


	private static class PlotParams
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	double	start;
		private	double	pixelDelta;
		private	int		pixelsPerDivision;
		private	int		divisionOffset;
		private	double	divisionDelta;
		private	int		intervalExponent;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PlotParams(
			PlotInterval	interval,
			int				dimension)
		{
			final	double	TOLERANCE		= 0.25 / (double)MIN_PIXELS_PER_DIVISION;
			final	double	UPPER_BOUND_1	= 1.0 + TOLERANCE;
			final	double	UPPER_BOUND_2	= 2.0 + TOLERANCE;
			final	double	UPPER_BOUND_5	= 5.0 + TOLERANCE;

			pixelDelta = interval.getInterval() / (double)dimension;
			divisionDelta = pixelDelta * MIN_PIXELS_PER_DIVISION;
			int exponent = (int)Math.floor(Math.log(divisionDelta) * RECIP_LOG_10);
			divisionDelta *= Math.pow(10.0, -exponent);
			int divIncrement = 1;
			if (divisionDelta > UPPER_BOUND_1)
			{
				if (divisionDelta <= UPPER_BOUND_2)
					divIncrement = 2;
				else if (divisionDelta <= UPPER_BOUND_5)
					divIncrement = 5;
				else
					++exponent;
			}
			divisionDelta = (double)divIncrement * Math.pow(10.0, exponent);
			pixelsPerDivision = (int)(divisionDelta / pixelDelta);
			pixelDelta = divisionDelta / (double)pixelsPerDivision;
			start = Math.floor(interval.getLowerEndpoint() / pixelDelta);
			divisionOffset = pixelsPerDivision - (int)start % pixelsPerDivision;
			if (divisionOffset >= pixelsPerDivision)
				divisionOffset -= pixelsPerDivision;
			start *= pixelDelta;
			intervalExponent = interval.getExponent();

/*
			// Debugging: print values
			System.out.println("dimension              = " + dimension);
			System.out.println("interval.lowerEndpoint = " + interval.lowerEndpoint);
			System.out.println("interval.upperEndpoint = " + interval.upperEndpoint);
			System.out.println("interval               = " + interval);
			System.out.println("start                  = " + start);
			System.out.println("pixelDelta             = " + pixelDelta);
			System.out.println("pixelsPerDivision      = " + pixelsPerDivision);
			System.out.println("divisionOffset         = " + divisionOffset);
			System.out.println("divisionDelta          = " + divisionDelta);
			System.out.println("intervalExponent       = " + intervalExponent);
			System.out.println("-----------------------");
*/
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public boolean equals(
			PlotParams	params)
		{
			return ((params != null) && (start == params.start) && (pixelDelta == params.pixelDelta)
					 && (pixelsPerDivision == params.pixelsPerDivision) && (divisionOffset == params.divisionOffset)
					 && (divisionDelta == params.divisionDelta) && (intervalExponent == params.intervalExponent));
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: SCALE TEXT


	private static class ScaleText
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String		text;
		private	Rectangle	rect;
		private	int			markX;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ScaleText(
			String	text,
			int		x,
			int		y,
			int		width,
			int		height,
			int		markX)
		{
			this.text = text;
			rect = new Rectangle(x, y - PopUpComponent.VERTICAL_MARGIN, width,
								 height + 2 * PopUpComponent.VERTICAL_MARGIN);
			this.markX = markX;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private String getText(
			Point	point)
		{
			return rect.contains(point) ? text : null;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: POP-UP COMPONENT


	private class PopUpComponent
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	VERTICAL_MARGIN		= 2;
		private static final	int	HORIZONTAL_MARGIN	= 3;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PopUpComponent(
			String	text)
		{
			// Initialise instance variables
			this.text = text;

			// Set properties
			AppFont.PLOT.apply(this);
			FontMetrics fontMetrics = getFontMetrics(getFont());
			setPreferredSize(new Dimension(2 * HORIZONTAL_MARGIN + fontMetrics.stringWidth(text),
										   2 * VERTICAL_MARGIN + fontMetrics.getAscent() + fontMetrics.getDescent()));
			setOpaque(true);
			setFocusable(false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void paintComponent(
			Graphics	gr)
		{
			// Create copy of graphics context
			Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

			// Get dimensions
			int width = getWidth();
			int height = getHeight();

			// Fill interior
			gr2d.setColor(PlotPanel.this.getBackground());
			gr2d.fillRect(0, 0, width, height);

			// Set rendering hints for text antialiasing and fractional metrics
			TextRendering.setHints(gr2d);

			// Draw text
			gr2d.setColor(POP_UP_TEXT_COLOUR);
			gr2d.drawString(text, HORIZONTAL_MARGIN, FontUtils.getBaselineOffset(height, gr2d.getFontMetrics()));

			// Draw border
			gr2d.setColor(POP_UP_BORDER_COLOUR);
			gr2d.drawRect(0, 0, width - 1, height - 1);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
