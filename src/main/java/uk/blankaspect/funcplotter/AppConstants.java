/*====================================================================*\

AppConstants.java

Application constants interface.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Insets;

//----------------------------------------------------------------------


// APPLICATION CONSTANTS INTERFACE


interface AppConstants
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	// Component constants
	Insets	COMPONENT_INSETS	= new Insets(2, 3, 2, 3);

	// Strings
	String	ELLIPSIS_STR		= "...";
	String	FILE_CHANGED_SUFFIX	= " *";
	String	OK_STR				= "OK";
	String	CANCEL_STR			= "Cancel";
	String	CONTINUE_STR		= "Continue";
	String	REPLACE_STR			= "Replace";
	String	CLEAR_STR			= "Clear";
	String	ALREADY_EXISTS_STR	= "\nThe file already exists.\nDo you want to replace it?";

	// Temporary-file prefix
	String	TEMP_FILE_PREFIX	= "_$_";

	// Filename suffixes
	String	PNG_FILE_SUFFIX		= ".png";
	String	TEXT_FILE_SUFFIX	= ".txt";
	String	XML_FILE_SUFFIX		= ".xml";

	// File-filter descriptions
	String	PNG_FILES_STR	= "PNG files";
	String	TEXT_FILES_STR	= "Text files";
	String	XML_FILES_STR	= "XML files";

}

//----------------------------------------------------------------------
