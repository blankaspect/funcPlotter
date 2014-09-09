/*====================================================================*\

Task.java

Task class.

\*====================================================================*/


// IMPORTS


import java.io.File;

import java.util.List;

import uk.org.blankaspect.exception.AppException;
import uk.org.blankaspect.exception.TaskCancelledException;

//----------------------------------------------------------------------


// TASK CLASS


abstract class Task
    extends uk.org.blankaspect.util.Task
{

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


    // READ DOCUMENT TASK CLASS


    public static class ReadDocument
        extends Task
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        public ReadDocument( FunctionDocument          document,
                             FunctionDocument.FileInfo fileInfo,
                             List<String>              errorStrs )
        {
            this.document = document;
            this.fileInfo = fileInfo;
            this.errorStrs = errorStrs;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : Runnable interface
    ////////////////////////////////////////////////////////////////////

        public void run( )
        {
            // Perform task
            try
            {
                document.read( fileInfo, errorStrs );
            }
            catch ( AppException e )
            {
                setException( e, false );
            }

            // Remove thread
            removeThread( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private FunctionDocument            document;
        private FunctionDocument.FileInfo   fileInfo;
        private List<String>                errorStrs;

    }

    //==================================================================


    // WRITE DOCUMENT TASK CLASS


    public static class WriteDocument
        extends Task
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        public WriteDocument( FunctionDocument          document,
                              FunctionDocument.FileInfo fileInfo,
                              boolean                   includeColours )
        {
            this.document = document;
            this.fileInfo = fileInfo;
            this.includeColours = includeColours;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : Runnable interface
    ////////////////////////////////////////////////////////////////////

        public void run( )
        {
            // Perform task
            try
            {
                document.write( fileInfo, includeColours );
            }
            catch ( TaskCancelledException e )
            {
                // ignore
            }
            catch ( AppException e )
            {
                setException( e, false );
            }

            // Remove thread
            removeThread( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private FunctionDocument            document;
        private FunctionDocument.FileInfo   fileInfo;
        private boolean                     includeColours;

    }

    //==================================================================


    // WRITE IMAGE TASK CLASS


    public static class WriteImage
        extends Task
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        public WriteImage( FunctionDocument document,
                           File             file )
        {
            this.document = document;
            this.file = file;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : Runnable interface
    ////////////////////////////////////////////////////////////////////

        public void run( )
        {
            // Perform task
            try
            {
                document.writeImage( file );
            }
            catch ( TaskCancelledException e )
            {
                // ignore
            }
            catch ( AppException e )
            {
                setException( e, false );
            }

            // Remove thread
            removeThread( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private FunctionDocument    document;
        private File                file;

    }

    //==================================================================


    // WRITE CONFIGURATION TASK CLASS


    public static class WriteConfig
        extends Task
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        public WriteConfig( File file )
        {
            this.file = file;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : Runnable interface
    ////////////////////////////////////////////////////////////////////

        public void run( )
        {
            // Perform task
            try
            {
                AppConfig.getInstance( ).write( file );
            }
            catch ( TaskCancelledException e )
            {
                // ignore
            }
            catch ( AppException e )
            {
                setException( e, false );
            }

            // Remove thread
            removeThread( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private File    file;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private Task( )
    {
    }

    //------------------------------------------------------------------

}

//----------------------------------------------------------------------
