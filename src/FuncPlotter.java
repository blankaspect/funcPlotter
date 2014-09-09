/*====================================================================*\

FuncPlotter.java

Function plotter applet class.

\*====================================================================*/


// IMPORTS


import javax.swing.JApplet;

import uk.org.blankaspect.util.Property;

//----------------------------------------------------------------------


// FUNCTION PLOTTER APPLET CLASS


public class FuncPlotter
    extends JApplet
    implements Property.Source
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    private static final    String  TITLE_PREFIX        = "Title: ";
    private static final    String  AUTHOR_PREFIX       = "Author: ";
    private static final    String  AUTHOR_NAME         = "Andy Morgan-Richards";
    private static final    String  APPLET_DESCRIPTION  = "Plots explicit functions in one variable.";

    private static final    String  APPLET_PARAMETER_STR    = "Applet parameter";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    public FuncPlotter( )
    {
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Property.Source interface
////////////////////////////////////////////////////////////////////////

    public String getSourceName( )
    {
        return APPLET_PARAMETER_STR;
    }

    //------------------------------------------------------------------

    public String getProperty( String key )
    {
        return getParameter( Property.APP_PREFIX + key );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

    @Override
    public void init( )
    {
        App.getInstance( ).init( null, this );
    }

    //------------------------------------------------------------------

    @Override
    public String getAppletInfo( )
    {
        return ( TITLE_PREFIX + App.getInstance( ).getTitleString( ) + "\n" +
                 AUTHOR_PREFIX + AUTHOR_NAME + "\n" + APPLET_DESCRIPTION );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public int getPreferredWidth( )
    {
        return getPreferredSize( ).width;
    }

    //------------------------------------------------------------------

    public int getPreferredHeight( )
    {
        return getPreferredSize( ).height;
    }

    //------------------------------------------------------------------

}

//----------------------------------------------------------------------
