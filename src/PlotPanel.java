/*====================================================================*\

PlotPanel.java

Plot panel class.

\*====================================================================*/


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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

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

import uk.org.blankaspect.gui.Colours;
import uk.org.blankaspect.gui.CrosshairCursor;
import uk.org.blankaspect.gui.GuiUtilities;
import uk.org.blankaspect.gui.TextRendering;

import uk.org.blankaspect.util.IntegerRange;
import uk.org.blankaspect.util.StringUtilities;
import uk.org.blankaspect.util.TextUtilities;

//----------------------------------------------------------------------


// PLOT PANEL CLASS


class PlotPanel
    extends JComponent
    implements FocusListener, MouseListener, MouseMotionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    public static final     int MIN_PLOT_WIDTH  = 128;
    public static final     int MAX_PLOT_WIDTH  = 2048;

    public static final     int MIN_PLOT_HEIGHT = MIN_PLOT_WIDTH;
    public static final     int MAX_PLOT_HEIGHT = MAX_PLOT_WIDTH;

    public static final     int MIN_NUM_Y_SCALE_DIGITS  = 4;
    public static final     int MAX_NUM_Y_SCALE_DIGITS  = 20;

    private static final    int LEFT_MARGIN             = 4;
    private static final    int RIGHT_MARGIN            = 4;
    private static final    int TOP_MARGIN              = 8;
    private static final    int BOTTOM_MARGIN           = 4;
    private static final    int SCALE_LINE_LENGTH       = 4;
    private static final    int X_SCALE_TOP_MARGIN      = 1;
    private static final    int X_SCALE_GAP             = 3;
    private static final    int Y_SCALE_LEFT_MARGIN     = 3;
    private static final    int MIN_PIXELS_PER_DIVISION = 32;

    private static final    int CURSOR_SIZE = 21;

    private static final    double  SCROLL_FACTOR   = 0.5;

    private static final    double  ONE_OVER_LOG_10 = 1.0 / Math.log( 10.0 );

    private static final    String  ZERO_STR    = "0";

    private static final    Color   POP_UP_TEXT_COLOUR      = Color.BLACK;
    private static final    Color   POP_UP_BORDER_COLOUR    = Colours.LINE_BORDER;

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


    // PLOT PARAMETERS CLASS


    private static class PlotParams
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private PlotParams( PlotInterval interval,
                            int          dimension )
        {
            final   double  TOLERANCE       = 0.25 / (double)MIN_PIXELS_PER_DIVISION;
            final   double  UPPER_LIMIT_1   = 1.0 + TOLERANCE;
            final   double  UPPER_LIMIT_2   = 2.0 + TOLERANCE;
            final   double  UPPER_LIMIT_5   = 5.0 + TOLERANCE;

            pixelDelta = interval.getInterval( ) / (double)dimension;
            divisionDelta = pixelDelta * MIN_PIXELS_PER_DIVISION;
            int exponent = (int)Math.floor( Math.log( divisionDelta ) * ONE_OVER_LOG_10 );
            divisionDelta *= Math.pow( 10.0, -exponent );
            int divIncrement = 1;
            if ( divisionDelta > UPPER_LIMIT_1 )
            {
                if ( divisionDelta <= UPPER_LIMIT_2 )
                    divIncrement = 2;
                else if ( divisionDelta <= UPPER_LIMIT_5 )
                    divIncrement = 5;
                else
                    ++exponent;
            }
            divisionDelta = (double)divIncrement * Math.pow( 10.0, exponent );
            pixelsPerDivision = (int)(divisionDelta / pixelDelta);
            pixelDelta = divisionDelta / (double)pixelsPerDivision;
            start = Math.floor( interval.getLowerEndpoint( ) / pixelDelta );
            divisionOffset = pixelsPerDivision - (int)start % pixelsPerDivision;
            if ( divisionOffset >= pixelsPerDivision )
                divisionOffset -= pixelsPerDivision;
            start *= pixelDelta;
            intervalExponent = interval.getExponent( );

/*
            // Debugging: print values
            System.out.println( "dimension              = " + dimension );
            System.out.println( "interval.lowerEndpoint = " + interval.lowerEndpoint );
            System.out.println( "interval.upperEndpoint = " + interval.upperEndpoint );
            System.out.println( "interval               = " + interval );
            System.out.println( "start                  = " + start );
            System.out.println( "pixelDelta             = " + pixelDelta );
            System.out.println( "pixelsPerDivision      = " + pixelsPerDivision );
            System.out.println( "divisionOffset         = " + divisionOffset );
            System.out.println( "divisionDelta          = " + divisionDelta );
            System.out.println( "intervalExponent       = " + intervalExponent );
            System.out.println( "-----------------------" );
*/
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        public boolean equals( PlotParams params )
        {
            return ( (params != null) && (start == params.start) && (pixelDelta == params.pixelDelta) &&
                     (pixelsPerDivision == params.pixelsPerDivision) &&
                     (divisionOffset == params.divisionOffset) && (divisionDelta == params.divisionDelta) &&
                     (intervalExponent == params.intervalExponent) );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private double  start;
        private double  pixelDelta;
        private int     pixelsPerDivision;
        private int     divisionOffset;
        private double  divisionDelta;
        private int     intervalExponent;

    }

    //==================================================================


    // SCALE TEXT CLASS


    private static class ScaleText
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private ScaleText( String text,
                           int    x,
                           int    y,
                           int    width,
                           int    height,
                           int    markX )
        {
            this.text = text;
            rect = new Rectangle( x, y - PopUpComponent.VERTICAL_MARGIN, width,
                                  height + 2 * PopUpComponent.VERTICAL_MARGIN );
            this.markX = markX;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        private String getText( Point point )
        {
            return ( rect.contains( point ) ? text : null );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private String      text;
        private Rectangle   rect;
        private int         markX;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


    // POP-UP COMPONENT CLASS


    private class PopUpComponent
        extends JComponent
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    int VERTICAL_MARGIN     = 2;
        private static final    int HORIZONTAL_MARGIN   = 3;

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private PopUpComponent( String text )
        {
            // Initialise instance variables
            this.text = text;

            // Set component attributes
            AppFont.PLOT.apply( this );
            FontMetrics fontMetrics = getFontMetrics( getFont( ) );
            setPreferredSize( new Dimension( 2 * HORIZONTAL_MARGIN + fontMetrics.stringWidth( text ),
                                             2 * VERTICAL_MARGIN + fontMetrics.getAscent( ) +
                                                                            fontMetrics.getDescent( ) ) );
            setOpaque( true );
            setFocusable( false );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        protected void paintComponent( Graphics gr )
        {
            // Create copy of graphics context
            gr = gr.create( );

            // Get dimensions
            int width = getWidth( );
            int height = getHeight( );

            // Fill interior
            gr.setColor( PlotPanel.this.getBackground( ) );
            gr.fillRect( 0, 0, width, height );

            // Set rendering hints for text antialiasing and fractional metrics
            TextRendering.setHints( (Graphics2D)gr );

            // Draw text
            gr.setColor( POP_UP_TEXT_COLOUR );
            gr.drawString( text, HORIZONTAL_MARGIN,
                           GuiUtilities.getBaselineOffset( height, gr.getFontMetrics( ) ) );

            // Draw border
            gr.setColor( POP_UP_BORDER_COLOUR );
            gr.drawRect( 0, 0, width - 1, height - 1 );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private String  text;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    public PlotPanel( FunctionDocument document,
                      int              plotWidth,
                      int              plotHeight,
                      boolean          gridVisible )
    {
        // Set border and font
        setBorder( false );
        borderInsets = getBorder( ).getBorderInsets( this );
        AppFont.PLOT.apply( this );

        // Initialise class variables
        if ( numYScaleDigits == 0 )
            numYScaleDigits = AppConfig.getInstance( ).getNumYScaleDigits( );
        setCursor( false );

        // Initialise instance variables
        this.document = document;
        this.gridVisible = gridVisible;
        minusChar = SurrogateMinus.getMinusChar( getFont( ) );
        FontMetrics fontMetrics = getFontMetrics( getFont( ) );
        char[] zeros = StringUtilities.createCharArray( '0', numYScaleDigits );
        maxYScaleStrWidth = fontMetrics.charsWidth( zeros, 0, zeros.length );
        int yScaleWidth = Y_SCALE_LEFT_MARGIN + maxYScaleStrWidth + SCALE_LINE_LENGTH;
        int xScaleHeight = SCALE_LINE_LENGTH + X_SCALE_TOP_MARGIN + fontMetrics.getAscent( ) +
                                                                                fontMetrics.getDescent( );
        plotRect = new Rectangle( borderInsets.left + LEFT_MARGIN + yScaleWidth,
                                  borderInsets.top + TOP_MARGIN, plotWidth, plotHeight );
        panelWidth = borderInsets.left + LEFT_MARGIN + yScaleWidth + plotWidth + maxYScaleStrWidth / 2 +
                                                                        RIGHT_MARGIN + borderInsets.right;
        panelHeight = borderInsets.top + TOP_MARGIN + plotHeight + xScaleHeight + BOTTOM_MARGIN +
                                                                                        borderInsets.bottom;
        xParams = new PlotParams( document.getXInterval( ), plotWidth );
        yParams = new PlotParams( document.getYInterval( ), plotHeight );
        changeListeners = new ArrayList<>( );
        scaleTexts = new ArrayList<>( );

        // Set component attributes
        setOpaque( true );
        setFocusable( true );

        // Add listeners
        addFocusListener( this );
        addMouseListener( this );
        addMouseMotionListener( this );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : FocusListener interface
////////////////////////////////////////////////////////////////////////

    public void focusGained( FocusEvent event )
    {
        setBorder( true );
    }

    //------------------------------------------------------------------

    public void focusLost( FocusEvent event )
    {
        setBorder( false );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseListener interface
////////////////////////////////////////////////////////////////////////

    public void mouseClicked( MouseEvent event )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    public void mouseEntered( MouseEvent event )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    public void mouseExited( MouseEvent event )
    {
        if ( (event.getComponent( ) != this) && (scalePopUp != null) )
        {
            scalePopUp.hide( );
            scalePopUp = null;
        }
    }

    //------------------------------------------------------------------

    public void mousePressed( MouseEvent event )
    {
        requestFocusInWindow( );
    }

    //------------------------------------------------------------------

    public void mouseReleased( MouseEvent event )
    {
        // do nothing
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseMotionListener interface
////////////////////////////////////////////////////////////////////////

    public void mouseDragged( MouseEvent event )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    public void mouseMoved( MouseEvent event )
    {
        if ( scalePopUp == null )
        {
            for ( ScaleText scaleText : scaleTexts )
            {
                String str = scaleText.getText( event.getPoint( ) );
                if ( str != null )
                {
                    PopUpComponent popUpComponent = new PopUpComponent( str );
                    popUpComponent.addMouseListener( this );
                    int x = scaleText.markX;
                    if ( x != 0 )
                        x -= popUpComponent.getPreferredSize( ).width / 2;
                    Point location = new Point( x, scaleText.rect.y );
                    SwingUtilities.convertPointToScreen( location, this );
                    scalePopUp = PopupFactory.getSharedInstance( ).getPopup( this, popUpComponent,
                                                                             location.x, location.y );
                    scalePopUp.show( );
                    break;
                }
            }
        }
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

    @Override
    public Dimension getMinimumSize( )
    {
        return new Dimension( panelWidth, panelHeight );
    }

    //------------------------------------------------------------------

    @Override
    public Dimension getPreferredSize( )
    {
        return getMinimumSize( );
    }

    //------------------------------------------------------------------

    @Override
    protected void paintComponent( Graphics gr )
    {
        // Create copy of graphics context
        gr = gr.create( );

        // Draw component background
        Rectangle rect = gr.getClipBounds( );
        AppConfig config = AppConfig.getInstance( );
        gr.setColor( drawingImage ? config.getPlotColourImageMargin( ) : getBackground( ) );
        gr.fillRect( rect.x, rect.y, rect.width, rect.height );

        // Draw plot background
        gr.setColor( config.getPlotColourBackground( ) );
        gr.fillRect( plotRect.x, plotRect.y, plotRect.width, plotRect.height );

        // Set rendering hints for text antialiasing and fractional metrics
        TextRendering.setHints( (Graphics2D)gr );

        // Initialise local variables
        int numFractionDigits = config.getNumFractionDigits( );
        IntegerRange fixedPointExponentRange = config.getFixedPointExponentRange( );
        boolean applyFixedExponent = !config.isNormaliseScientificNotation( );

        FontMetrics fontMetrics = gr.getFontMetrics( );
        int fontAscent = fontMetrics.getAscent( );
        int textHeight = fontMetrics.getAscent( ) + fontMetrics.getDescent( );

        // Draw x grid lines and scale markings
        scaleTexts.clear( );
        int divisionOffset = xParams.divisionOffset;
        int strEndX = 0;
        int yAxisX = -1;
        String prevStr = null;
        while ( divisionOffset < plotRect.width )
        {
            int x = plotRect.x + divisionOffset;
            double value = xParams.start + (double)divisionOffset * xParams.pixelDelta;
            String str = PlotInterval.doubleToString( value, numFractionDigits, fixedPointExponentRange,
                                                      applyFixedExponent, xParams.intervalExponent );
            if ( Math.abs( value ) < 0.5 * xParams.pixelDelta )
            {
                str = ZERO_STR;
                yAxisX = x;
            }
            else
            {
                if ( gridVisible )
                {
                    gr.setColor( config.getPlotColourGrid( ) );
                    gr.drawLine( x, plotRect.y, x, plotRect.y + plotRect.height - 1 );
                }
            }

            gr.setColor( config.getPlotColourScale( ) );
            gr.drawLine( x, plotRect.y + plotRect.height,
                         x, plotRect.y + plotRect.height + SCALE_LINE_LENGTH - 1 );

            if ( !str.equals( prevStr ) )
            {
                String xStr = SurrogateMinus.minusToSurrogate( str, minusChar );
                int strWidth = fontMetrics.stringWidth( xStr );
                int strX = x - strWidth / 2;
                int strY = plotRect.y + plotRect.height + SCALE_LINE_LENGTH + X_SCALE_TOP_MARGIN +
                                                                                                fontAscent;
                if ( strX < strEndX )
                {
                    String limXStr = null;
                    if ( config.isTruncateXScaleText( ) )
                    {
                        limXStr = TextUtilities.getLimitedWidthString( xStr, fontMetrics, 2 * (x - strEndX),
                                                                       TextUtilities.RemovalMode.START );
                        strWidth = fontMetrics.stringWidth( limXStr );
                    }
                    else
                        strWidth = 2 * (x - strEndX);
                    strX = x - strWidth / 2;
                    scaleTexts.add( new ScaleText( xStr, strX, strY - fontAscent, strWidth, textHeight,
                                                   x ) );
                    xStr = limXStr;
                }
                if ( xStr != null )
                {
                    gr.drawString( xStr, strX, strY );
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
        while ( divisionOffset < plotRect.height )
        {
            int y = plotRect.y + (plotRect.height - 1) - divisionOffset;
            double value = yParams.start + (double)divisionOffset * yParams.pixelDelta;
            String str = PlotInterval.doubleToString( value, numFractionDigits, fixedPointExponentRange,
                                                      applyFixedExponent, yParams.intervalExponent );
            if ( Math.abs( value ) < 0.5 * yParams.pixelDelta )
            {
                str = ZERO_STR;
                xAxisY = y;
            }
            else
            {
                if ( gridVisible )
                {
                    gr.setColor( config.getPlotColourGrid( ) );
                    gr.drawLine( plotRect.x, y, plotRect.x + plotRect.width - 1, y );
                }
            }

            gr.setColor( config.getPlotColourScale( ) );
            gr.drawLine( plotRect.x - SCALE_LINE_LENGTH, y, plotRect.x - 1, y );

            if ( !str.equals( prevStr ) )
            {
                String yStr = SurrogateMinus.minusToSurrogate( str, minusChar );
                String limYStr = TextUtilities.getLimitedWidthString( yStr, fontMetrics, maxYScaleStrWidth,
                                                                      TextUtilities.RemovalMode.START );
                int strWidth = fontMetrics.stringWidth( limYStr );
                int strX = plotRect.x - (strWidth + Y_SCALE_LEFT_MARGIN + SCALE_LINE_LENGTH);
                int strY = y + fontAscent / 2;
                if ( limYStr != yStr )
                    scaleTexts.add( new ScaleText( yStr, strX, strY - fontAscent, strWidth, textHeight,
                                                   0 ) );
                gr.drawString( limYStr, strX, strY );
            }
            prevStr = str;

            divisionOffset += yParams.pixelsPerDivision;
        }

        // Draw axes
        gr.setColor( config.getPlotColourAxis( ) );
        if ( yAxisX >= 0 )
            gr.drawLine( yAxisX, plotRect.y, yAxisX, plotRect.y + plotRect.height - 1 );
        if ( xAxisY >= 0 )
            gr.drawLine( plotRect.x, xAxisY, plotRect.x + plotRect.width - 1, xAxisY );

        // Plot functions
        Graphics plotGr = gr.create( plotRect.x, plotRect.y, plotRect.width, plotRect.height );
        for ( int i = document.getNumFunctions( ) - 1; i >= 0; --i )
        {
            Function function = document.getFunction( i );
            if ( !function.isHidden( ) &&
                 (!function.isObscured( ) || config.isFunctionObscuredColour( )) )
            {
                plotGr.setColor( function.isObscured( ) ? config.getFunctionObscuredColour( )
                                                        : function.getColour( ) );
                drawExpression( plotGr, function.getExpression( ), 0, plotRect.width );
            }
        }
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public boolean isGridVisible( )
    {
        return gridVisible;
    }

    //------------------------------------------------------------------

    public Point2D.Double pointToCoords( Point point )
    {
        Point2D.Double coords = null;
        if ( (point.x >= plotRect.x) && (point.x < plotRect.x + plotRect.width) &&
             (point.y >= plotRect.y) && (point.y < plotRect.y + plotRect.height) )
            coords = new Point2D.Double(
                xParams.start + (double)(point.x - plotRect.x) * xParams.pixelDelta,
                yParams.start + (double)(plotRect.y + plotRect.height - 1 - point.y) * yParams.pixelDelta );
        return coords;
    }

    //------------------------------------------------------------------

    public double getXScrollIncrement( )
    {
        return ( xParams.divisionDelta * SCROLL_FACTOR );
    }

    //------------------------------------------------------------------

    public double getXScrollIncrement( int pixels )
    {
        return ( (double)((pixels << 1) / xParams.pixelsPerDivision) * getXScrollIncrement( ) );
    }

    //------------------------------------------------------------------

    public double getYScrollIncrement( )
    {
        return ( yParams.divisionDelta * SCROLL_FACTOR );
    }

    //------------------------------------------------------------------

    public double getYScrollIncrement( int pixels )
    {
        return ( (double)((pixels << 1) / yParams.pixelsPerDivision) * getYScrollIncrement( ) );
    }

    //------------------------------------------------------------------

    public void setXInterval( PlotInterval interval )
    {
        PlotParams newXParams = new PlotParams( interval, plotRect.width );
        if ( !xParams.equals( newXParams ) )
        {
            xParams = newXParams;
            repaint( );
            fireStateChanged( );
        }
    }

    //------------------------------------------------------------------

    public void setYInterval( PlotInterval interval )
    {
        PlotParams newYParams = new PlotParams( interval, plotRect.height );
        if ( !yParams.equals( newYParams ) )
        {
            yParams = newYParams;
            repaint( );
            fireStateChanged( );
        }
    }

    //------------------------------------------------------------------

    public void setIntervals( PlotInterval xInterval,
                              PlotInterval yInterval )
    {
        PlotParams newXParams = new PlotParams( xInterval, plotRect.width );
        PlotParams newYParams = new PlotParams( yInterval, plotRect.height );
        if ( !xParams.equals( newXParams ) || !yParams.equals( newYParams ) )
        {
            xParams = newXParams;
            yParams = newYParams;
            repaint( );
            fireStateChanged( );
        }
    }

    //------------------------------------------------------------------

    public void setCursor( boolean move )
    {
        setCursor( move ? Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR )
                        : CrosshairCursor.getCursor( CURSOR_SIZE ) );
    }

    //------------------------------------------------------------------

    public void setGridVisible( boolean visible )
    {
        if ( gridVisible != visible )
        {
            gridVisible = visible;
            repaint( );
        }
    }

    //------------------------------------------------------------------

    public void drawPanel( Graphics gr )
    {
        gr.setFont( getFont( ) );
        gr.setClip( 0, 0, panelWidth, panelHeight );
        drawingImage = true;
        paintComponent( gr );
        drawingImage = false;
    }

    //------------------------------------------------------------------

    public void addChangeListener( ChangeListener listener )
    {
        changeListeners.add( listener );
    }

    //------------------------------------------------------------------

    private void fireStateChanged( )
    {
        for ( int i = changeListeners.size( ) - 1; i >= 0; --i )
        {
            if ( changeEvent == null )
                changeEvent = new ChangeEvent( this );
            changeListeners.get( i ).stateChanged( changeEvent );
        }
    }

    //------------------------------------------------------------------

    private void drawExpression( Graphics   gr,
                                 Expression expression,
                                 int        startX,
                                 int        endX )
    {
        final   long Y_LOWER_LIMIT  = Integer.MIN_VALUE >> 1;
        final   long Y_UPPER_LIMIT  = Integer.MAX_VALUE >> 1;

        double prevX = 0.0;
        double prevY = Double.NaN;
        int prevPlotY = 0;
        for ( int plotX = startX - 1; plotX <= endX; ++plotX )
        {
            double x = xParams.start + (double)plotX * xParams.pixelDelta;
            double y = expression.evaluate( x );
            if ( !Double.isNaN( y ) )
            {
                int plotY = (int)Math.min( Math.max( Y_LOWER_LIMIT, Math.round( (y - yParams.start) /
                                                                                    yParams.pixelDelta ) ),
                                           Y_UPPER_LIMIT );
                if ( !Double.isNaN( prevY ) )
                {
                    double medianY = expression.evaluate( 0.5 * (prevX + x) );
                    if ( ((medianY >= prevY) && (medianY <= y)) || ((medianY >= y) && (medianY <= prevY)) ||
                         (y == prevY) )
                        gr.drawLine( plotX - 1, (plotRect.height - 1) - prevPlotY, plotX,
                                     (plotRect.height - 1) - plotY );
                }
                prevPlotY = plotY;
            }
            prevX = x;
            prevY = y;
        }
    }

    //------------------------------------------------------------------

    private void setBorder( boolean focused )
    {
        if ( focused )
            setBorder( BorderFactory.
                            createMatteBorder( borderInsets.top, borderInsets.left,
                                               borderInsets.bottom, borderInsets.right,
                                               AppConfig.getInstance( ).getPlotColourFocusedBorder( ) ) );
        else
            GuiUtilities.setPaddedLineBorder( this, 1 );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

    private static  int numYScaleDigits;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private FunctionDocument        document;
    private int                     panelWidth;
    private int                     panelHeight;
    private int                     maxYScaleStrWidth;
    private Insets                  borderInsets;
    private Rectangle               plotRect;
    private PlotParams              xParams;
    private PlotParams              yParams;
    private List<ChangeListener>    changeListeners;
    private ChangeEvent             changeEvent;
    private char                    minusChar;
    private boolean                 gridVisible;
    private boolean                 drawingImage;
    private List<ScaleText>         scaleTexts;
    private Popup                   scalePopUp;

}

//----------------------------------------------------------------------
