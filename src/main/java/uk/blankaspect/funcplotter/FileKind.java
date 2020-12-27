/*====================================================================*\

FileKind.java

Enumeration: kinds of function file.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.util.Arrays;

import uk.blankaspect.common.misc.FilenameSuffixFilter;
import uk.blankaspect.common.misc.IStringKeyed;

//----------------------------------------------------------------------


// ENUMERATION: KINDS OF FUNCTION FILE


enum FileKind
	implements IStringKeyed
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
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String					key;
	private	String					text;
	private	FilenameSuffixFilter	filter;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private FileKind(String key,
					 String text,
					 String suffix,
					 String description)
	{
		this.key = key;
		this.text = text;
		filter = new FilenameSuffixFilter(description, suffix);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static FileKind forKey(String key)
	{
		return Arrays.stream(values())
						.filter(value -> value.key.equals(key))
						.findFirst()
						.orElse(null);
	}

	//------------------------------------------------------------------

	public static FileKind forDescription(String description)
	{
		return Arrays.stream(values())
						.filter(value -> value.filter.getDescription().equals(description))
						.findFirst()
						.orElse(null);
	}

	//------------------------------------------------------------------

	public static FileKind forFilename(String filename)
	{
		return Arrays.stream(values())
						.filter(value -> value.filter.accepts(filename))
						.findFirst()
						.orElse(null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IStringKeyed interface
////////////////////////////////////////////////////////////////////////

	@Override
	public String getKey()
	{
		return key;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public String toString()
	{
		return text;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String getText()
	{
		return text;
	}

	//------------------------------------------------------------------

	public FilenameSuffixFilter getFilter()
	{
		return filter;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
