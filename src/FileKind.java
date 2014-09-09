/*====================================================================*\

FileKind.java

File kind enumeration.

\*====================================================================*/


// IMPORTS


import uk.org.blankaspect.util.FilenameSuffixFilter;
import uk.org.blankaspect.util.StringKeyed;

//----------------------------------------------------------------------


// FILE KIND ENUMERATION


enum FileKind
    implements StringKeyed
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    XML
    (
        "xml",
        "XML",
        AppConstants.XML_FILE_SUFFIX,
        AppConstants.XML_FILES_STR
    ),

    TEXT
    (
        "text",
        "Text",
        AppConstants.TEXT_FILE_SUFFIX,
        AppConstants.TEXT_FILES_STR
    );

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private FileKind( String key,
                      String text,
                      String suffix,
                      String description )
    {
        this.key = key;
        this.text = text;
        filter = new FilenameSuffixFilter( description, suffix );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static FileKind forKey( String key )
    {
        for ( FileKind value : values( ) )
        {
            if ( value.key.equals( key ) )
                return value;
        }
        return null;
    }

    //------------------------------------------------------------------

    public static FileKind forDescription( String description )
    {
        for ( FileKind value : values( ) )
        {
            if ( value.filter.getDescription( ).equals( description ) )
                return value;
        }
        return null;
    }

    //------------------------------------------------------------------

    public static FileKind forFilename( String filename )
    {
        if ( filename != null )
        {
            for ( FileKind value : values( ) )
            {
                if ( filename.endsWith( value.filter.getSuffix( 0 ) ) )
                    return value;
            }
        }
        return null;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : StringKeyed interface
////////////////////////////////////////////////////////////////////////

    public String getKey( )
    {
        return key;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

    @Override
    public String toString( )
    {
        return text;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public String getText( )
    {
        return text;
    }

    //------------------------------------------------------------------

    public FilenameSuffixFilter getFilter( )
    {
        return filter;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private String                  key;
    private String                  text;
    private FilenameSuffixFilter    filter;

}

//----------------------------------------------------------------------
