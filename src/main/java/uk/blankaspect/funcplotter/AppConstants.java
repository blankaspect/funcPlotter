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

	// Filename extensions
	String	PNG_FILENAME_EXTENSION	= ".png";
	String	TEXT_FILENAME_EXTENSION	= ".txt";
	String	XML_FILENAME_EXTENSION	= ".xml";

	// File-filter descriptions
	String	PNG_FILES_STR	= "PNG files";
	String	TEXT_FILES_STR	= "Text files";
	String	XML_FILES_STR	= "XML files";

}

//----------------------------------------------------------------------
