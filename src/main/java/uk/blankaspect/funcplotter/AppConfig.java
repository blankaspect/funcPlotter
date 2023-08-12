/*====================================================================*\

AppConfig.java

Application configuration class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.lang.reflect.Field;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import uk.blankaspect.common.cls.ClassUtils;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;

import uk.blankaspect.common.filesystem.PathnameUtils;

import uk.blankaspect.common.misc.FilenameSuffixFilter;
import uk.blankaspect.common.misc.NoYesAsk;

import uk.blankaspect.common.property.Property;
import uk.blankaspect.common.property.PropertySet;

import uk.blankaspect.common.range.IntegerRange;

import uk.blankaspect.common.ui.progress.IProgressView;

import uk.blankaspect.ui.swing.colour.ColourProperty;
import uk.blankaspect.ui.swing.colour.Colours;
import uk.blankaspect.ui.swing.colour.ColourUtils;

import uk.blankaspect.ui.swing.font.FontEx;

import uk.blankaspect.ui.swing.text.TextRendering;

//----------------------------------------------------------------------


// APPLICATION CONFIGURATION CLASS


class AppConfig
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		AppConfig	INSTANCE;

	public static final		int	MIN_NUM_FRACTION_DIGITS	= 1;
	public static final		int	MAX_NUM_FRACTION_DIGITS	= 12;

	public static final		int	MIN_FIXED_POINT_EXPONENT_BOUND	= -12;
	public static final		int	MAX_FIXED_POINT_EXPONENT_BOUND	= 12;

	private static final	int	VERSION					= 0;
	private static final	int	MIN_SUPPORTED_VERSION	= 0;
	private static final	int	MAX_SUPPORTED_VERSION	= 0;

	private static final	Color	DEFAULT_FUNCTION_OBSCURED_COLOUR	= new Color(224, 224, 224);

	private static final	List<Color>	DEFAULT_FUNCTION_COLOURS	=   Arrays.asList
	(
		new Color(255,   0,   0),
		new Color(160, 192,  96),
		new Color(  0,   0, 255),
		new Color(192,  64,   0),
		new Color(  0, 255, 170),
		new Color(128, 128, 255),
		new Color(255, 128, 128),
		new Color(  0, 128,  85),
		new Color( 64,  64, 128),
		new Color(255, 170,   0),
		new Color(  0, 170, 255),
		new Color(192,  64, 255),
		new Color(128,  85,   0),
		new Color( 32,  96, 128),
		new Color(255,   0, 170),
		new Color(170, 255,   0),
		new Color(  0, 192, 170),
		new Color(128,   0,  85),
		Color.BLACK,
		Color.GRAY
	);

	private static final	String	CONFIG_ERROR_STR	= "Configuration error";
	private static final	String	CONFIG_DIR_KEY		= Property.APP_PREFIX + "configDir";
	private static final	String	PROPERTIES_FILENAME	= App.NAME_KEY + "-properties" + AppConstants.XML_FILE_SUFFIX;
	private static final	String	FILENAME_STEM		= App.NAME_KEY + "-config";
	private static final	String	CONFIG_FILENAME		= FILENAME_STEM + AppConstants.XML_FILE_SUFFIX;
	private static final	String	CONFIG_OLD_FILENAME	= FILENAME_STEM + "-old" + AppConstants.XML_FILE_SUFFIX;

	private static final	String	SAVE_CONFIGURATION_FILE_STR	= "Save configuration file";
	private static final	String	WRITING_STR					= "Writing";

	private interface Key
	{
		String	APPEARANCE						= "appearance";
		String	AXIS							= "axis";
		String	BACKGROUND						= "background";
		String	CLEAR_EDIT_LIST_ON_SAVE			= "clearEditListOnSave";
		String	COLOUR							= "colour";
		String	CONFIGURATION					= App.NAME_KEY + "Configuration";
		String	DEFAULT_FILE_KIND				= "defaultFileKind";
		String	DIRECTORY						= "directory";
		String	FIXED_POINT_EXPONENT_RANGE		= "fixedPointExponentRange";
		String	FOCUSED_BORDER					= "focusedBorder";
		String	FONT							= "font";
		String	FUNCTION						= "function";
		String	GENERAL							= "general";
		String	GRID							= "grid";
		String	IMAGE_MARGIN					= "imageMargin";
		String	LIST_SIZE						= "listSize";
		String	LOOK_AND_FEEL					= "lookAndFeel";
		String	MAIN_WINDOW_LOCATION			= "mainWindowLocation";
		String	MAX_EDIT_LIST_LENGTH			= "maxEditListLength";
		String	NEW_DOCUMENT_ON_STARTUP			= "newDocumentOnStartup";
		String	NORMALISE_SCIENTIFIC_NOTATION	= "normaliseScientificNotation";
		String	NUM_FRACTION_DIGITS				= "numFractionDigits";
		String	NUM_Y_SCALE_DIGITS				= "numYScaleDigits";
		String	OBSCURED_COLOUR					= "obscuredColour";
		String	PLOT							= "plot";
		String	SAVE_FUNCTION_COLOURS			= "saveFunctionColours";
		String	SCALE							= "scale";
		String	SELECT_TEXT_ON_FOCUS_GAINED		= "selectTextOnFocusGained";
		String	SHOW_FULL_PATHNAMES				= "showFullPathnames";
		String	SHOW_GRID						= "showGrid";
		String	SHOW_UNIX_PATHNAMES				= "showUnixPathnames";
		String	SIZE							= "size";
		String	TEXT_ANTIALIASING				= "textAntialiasing";
		String	TRUNCATE_X_SCALE_TEXT			= "truncateXScaleText";
	}

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		ERROR_READING_PROPERTIES_FILE
		("An error occurred when reading the properties file."),

		NO_CONFIGURATION_FILE
		("No configuration file was found at the specified location."),

		MALFORMED_URL
		("The pathname of the configuration file is malformed."),

		NO_VERSION_NUMBER
		("The configuration file does not have a version number."),

		INVALID_VERSION_NUMBER
		("The version number of the configuration file is invalid."),

		UNSUPPORTED_CONFIGURATION_FILE
		("The version of the configuration file (%1) is not supported by this version of " + App.SHORT_NAME + "."),

		FAILED_TO_CREATE_DIRECTORY
		("Failed to create the directory for the configuration file.");

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(String message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CONFIGURATION FILE CLASS


	private static class ConfigFile
		extends PropertySet
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	CONFIG_FILE1_STR	= "configuration file";
		private static final	String	CONFIG_FILE2_STR	= "Configuration file";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ConfigFile()
		{
		}

		//--------------------------------------------------------------

		private ConfigFile(String versionStr)
			throws AppException
		{
			super(Key.CONFIGURATION, null, versionStr);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getSourceName()
		{
			return CONFIG_FILE2_STR;
		}

		//--------------------------------------------------------------

		@Override
		protected String getFileKindString()
		{
			return CONFIG_FILE1_STR;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void read(File file)
			throws AppException
		{
			// Read file
			read(file, Key.CONFIGURATION);

			// Validate version number
			try
			{
				validateVersion();
			}
			catch (AppException e)
			{
				throw new FileException(e, file);
			}
		}

		//--------------------------------------------------------------

		private void validateVersion()
			throws AppException
		{
			String versionStr = getVersionString();
			if (versionStr == null)
				throw new AppException(ErrorId.NO_VERSION_NUMBER);
			try
			{
				int version = Integer.parseInt(versionStr);
				if ((version < MIN_SUPPORTED_VERSION) || (version > MAX_SUPPORTED_VERSION))
					throw new AppException(ErrorId.UNSUPPORTED_CONFIGURATION_FILE, versionStr);
			}
			catch (NumberFormatException e)
			{
				throw new AppException(ErrorId.INVALID_VERSION_NUMBER);
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// PROPERTY CLASS: DEFAULT FILE KIND


	private class CPDefaultFileKind
		extends Property.EnumProperty<FileKind>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPDefaultFileKind()
		{
			super(concatenateKeys(Key.GENERAL, Key.DEFAULT_FILE_KIND), FileKind.class);
			value = FileKind.XML;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public FileKind getDefaultFileKind()
	{
		return cpDefaultFileKind.getValue();
	}

	//------------------------------------------------------------------

	public void setDefaultFileKind(FileKind value)
	{
		cpDefaultFileKind.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPDefaultFileKind	cpDefaultFileKind	= new CPDefaultFileKind();

	//==================================================================


	// PROPERTY CLASS: NEW DOCUMENT ON STARTUP


	private class CPNewDocumentOnStartup
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPNewDocumentOnStartup()
		{
			super(concatenateKeys(Key.GENERAL, Key.NEW_DOCUMENT_ON_STARTUP));
			value = false;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isNewDocumentOnStartup()
	{
		return cpNewDocumentOnStartup.getValue();
	}

	//------------------------------------------------------------------

	public void setNewDocumentOnStartup(boolean value)
	{
		cpNewDocumentOnStartup.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPNewDocumentOnStartup	cpNewDocumentOnStartup	= new CPNewDocumentOnStartup();

	//==================================================================


	// PROPERTY CLASS: SAVE FUNCTION COLOURS


	private class CPSaveFunctionColours
		extends Property.EnumProperty<NoYesAsk>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPSaveFunctionColours()
		{
			super(concatenateKeys(Key.GENERAL, Key.SAVE_FUNCTION_COLOURS), NoYesAsk.class);
			value = NoYesAsk.YES;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public NoYesAsk getSaveFunctionColours()
	{
		return cpSaveFunctionColours.getValue();
	}

	//------------------------------------------------------------------

	public void setSaveFunctionColours(NoYesAsk value)
	{
		cpSaveFunctionColours.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPSaveFunctionColours	cpSaveFunctionColours	= new CPSaveFunctionColours();

	//==================================================================


	// PROPERTY CLASS: SHOW UNIX PATHNAMES


	private class CPShowUnixPathnames
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPShowUnixPathnames()
		{
			super(concatenateKeys(Key.GENERAL, Key.SHOW_UNIX_PATHNAMES));
			value = false;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isShowUnixPathnames()
	{
		return cpShowUnixPathnames.getValue();
	}

	//------------------------------------------------------------------

	public void setShowUnixPathnames(boolean value)
	{
		cpShowUnixPathnames.setValue(value);
	}

	//------------------------------------------------------------------

	public void addShowUnixPathnamesObserver(Property.IObserver observer)
	{
		cpShowUnixPathnames.addObserver(observer);
	}

	//------------------------------------------------------------------

	public void removeShowUnixPathnamesObserver(Property.IObserver observer)
	{
		cpShowUnixPathnames.removeObserver(observer);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPShowUnixPathnames	cpShowUnixPathnames	= new CPShowUnixPathnames();

	//==================================================================


	// PROPERTY CLASS: SELECT TEXT ON FOCUS GAINED


	private class CPSelectTextOnFocusGained
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPSelectTextOnFocusGained()
		{
			super(concatenateKeys(Key.GENERAL, Key.SELECT_TEXT_ON_FOCUS_GAINED));
			value = true;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isSelectTextOnFocusGained()
	{
		return cpSelectTextOnFocusGained.getValue();
	}

	//------------------------------------------------------------------

	public void setSelectTextOnFocusGained(boolean value)
	{
		cpSelectTextOnFocusGained.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPSelectTextOnFocusGained	cpSelectTextOnFocusGained	= new CPSelectTextOnFocusGained();

	//==================================================================


	// PROPERTY CLASS: SHOW FULL PATHNAMES


	private class CPShowFullPathnames
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPShowFullPathnames()
		{
			super(concatenateKeys(Key.GENERAL, Key.SHOW_FULL_PATHNAMES));
			value = false;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isShowFullPathnames()
	{
		return cpShowFullPathnames.getValue();
	}

	//------------------------------------------------------------------

	public void setShowFullPathnames(boolean value)
	{
		cpShowFullPathnames.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPShowFullPathnames	cpShowFullPathnames	= new CPShowFullPathnames();

	//==================================================================


	// PROPERTY CLASS: MAIN WINDOW LOCATION


	private class CPMainWindowLocation
		extends Property.SimpleProperty<Point>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPMainWindowLocation()
		{
			super(concatenateKeys(Key.GENERAL, Key.MAIN_WINDOW_LOCATION));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			if (input.getValue().isEmpty())
				value = null;
			else
			{
				int[] outValues = input.parseIntegers(2, null);
				value = new Point(outValues[0], outValues[1]);
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return (value == null) ? "" : value.x + ", " + value.y;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isMainWindowLocation()
	{
		return (getMainWindowLocation() != null);
	}

	//------------------------------------------------------------------

	public Point getMainWindowLocation()
	{
		return cpMainWindowLocation.getValue();
	}

	//------------------------------------------------------------------

	public void setMainWindowLocation(Point value)
	{
		cpMainWindowLocation.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPMainWindowLocation	cpMainWindowLocation	= new CPMainWindowLocation();

	//==================================================================


	// PROPERTY CLASS: MAXIMUM EDIT LIST LENGTH


	private class CPMaxEditListLength
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPMaxEditListLength()
		{
			super(concatenateKeys(Key.GENERAL, Key.MAX_EDIT_LIST_LENGTH), FunctionDocument.MIN_MAX_EDIT_LIST_LENGTH,
				  FunctionDocument.MAX_MAX_EDIT_LIST_LENGTH);
			value = FunctionDocument.DEFAULT_MAX_EDIT_LIST_LENGTH;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getMaxEditListLength()
	{
		return cpMaxEditListLength.getValue();
	}

	//------------------------------------------------------------------

	public void setMaxEditListLength(int value)
	{
		cpMaxEditListLength.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPMaxEditListLength	cpMaxEditListLength	= new CPMaxEditListLength();

	//==================================================================


	// PROPERTY CLASS: CLEAR EDIT LIST ON SAVE


	private class CPClearEditListOnSave
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPClearEditListOnSave()
		{
			super(concatenateKeys(Key.GENERAL, Key.CLEAR_EDIT_LIST_ON_SAVE));
			value = false;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isClearEditListOnSave()
	{
		return cpClearEditListOnSave.getValue();
	}

	//------------------------------------------------------------------

	public void setClearEditListOnSave(boolean value)
	{
		cpClearEditListOnSave.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPClearEditListOnSave	cpClearEditListOnSave	= new CPClearEditListOnSave();

	//==================================================================


	// PROPERTY CLASS: LOOK-AND-FEEL


	private class CPLookAndFeel
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPLookAndFeel()
		{
			super(concatenateKeys(Key.APPEARANCE, Key.LOOK_AND_FEEL));
			value = "";
			for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels())
			{
				if (lookAndFeelInfo.getClassName().equals(UIManager.getCrossPlatformLookAndFeelClassName()))
				{
					value = lookAndFeelInfo.getName();
					break;
				}
			}
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getLookAndFeel()
	{
		return cpLookAndFeel.getValue();
	}

	//------------------------------------------------------------------

	public void setLookAndFeel(String value)
	{
		cpLookAndFeel.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPLookAndFeel	cpLookAndFeel	= new CPLookAndFeel();

	//==================================================================


	// PROPERTY CLASS: TEXT ANTIALIASING


	private class CPTextAntialiasing
		extends Property.EnumProperty<TextRendering.Antialiasing>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPTextAntialiasing()
		{
			super(concatenateKeys(Key.APPEARANCE, Key.TEXT_ANTIALIASING), TextRendering.Antialiasing.class);
			value = TextRendering.Antialiasing.DEFAULT;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public TextRendering.Antialiasing getTextAntialiasing()
	{
		return cpTextAntialiasing.getValue();
	}

	//------------------------------------------------------------------

	public void setTextAntialiasing(TextRendering.Antialiasing value)
	{
		cpTextAntialiasing.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPTextAntialiasing	cpTextAntialiasing	= new CPTextAntialiasing();

	//==================================================================


	// PROPERTY CLASS: PLOT SIZE


	private class CPPlotSize
		extends Property.SimpleProperty<Dimension>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPPlotSize()
		{
			super(concatenateKeys(Key.PLOT, Key.SIZE));
			value = new Dimension(480, 480);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			IntegerRange[] ranges =
			{
				new IntegerRange(PlotPanel.MIN_PLOT_WIDTH, PlotPanel.MAX_PLOT_WIDTH),
				new IntegerRange(PlotPanel.MIN_PLOT_HEIGHT, PlotPanel.MAX_PLOT_HEIGHT)
			};
			int[] outValues = input.parseIntegers(2, ranges);
			value = new Dimension(outValues[0], outValues[1]);
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return (value.width + ", " + value.height);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Dimension getPlotSize()
	{
		return cpPlotSize.getValue();
	}

	//------------------------------------------------------------------

	public void setPlotSize(Dimension value)
	{
		cpPlotSize.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPPlotSize	cpPlotSize	= new CPPlotSize();

	//==================================================================


	// PROPERTY CLASS: SHOW GRID


	private class CPShowGrid
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPShowGrid()
		{
			super(concatenateKeys(Key.PLOT, Key.SHOW_GRID));
			value = true;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isShowGrid()
	{
		return cpShowGrid.getValue();
	}

	//------------------------------------------------------------------

	public void setShowGrid(boolean value)
	{
		cpShowGrid.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPShowGrid	cpShowGrid	= new CPShowGrid();

	//==================================================================


	// PROPERTY CLASS: NUMBER OF FRACTION DIGITS


	private class CPNumFractionDigits
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPNumFractionDigits()
		{
			super(concatenateKeys(Key.PLOT, Key.NUM_FRACTION_DIGITS), MIN_NUM_FRACTION_DIGITS, MAX_NUM_FRACTION_DIGITS);
			value = 5;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getNumFractionDigits()
	{
		return cpNumFractionDigits.getValue();
	}

	//------------------------------------------------------------------

	public void setNumFractionDigits(int value)
	{
		cpNumFractionDigits.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPNumFractionDigits	cpNumFractionDigits	= new CPNumFractionDigits();

	//==================================================================


	// PROPERTY CLASS: NUMBER OF Y-SCALE DIGITS


	private class CPNumYScaleDigits
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPNumYScaleDigits()
		{
			super(concatenateKeys(Key.PLOT, Key.NUM_Y_SCALE_DIGITS), PlotPanel.MIN_NUM_Y_SCALE_DIGITS,
				  PlotPanel.MAX_NUM_Y_SCALE_DIGITS);
			value = 8;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getNumYScaleDigits()
	{
		return cpNumYScaleDigits.getValue();
	}

	//------------------------------------------------------------------

	public void setNumYScaleDigits(int value)
	{
		cpNumYScaleDigits.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPNumYScaleDigits	cpNumYScaleDigits	= new CPNumYScaleDigits();

	//==================================================================


	// PROPERTY CLASS: FIXED-POINT EXPONENT RANGE


	private class CPFixedPointExponentRange
		extends Property.SimpleProperty<IntegerRange>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPFixedPointExponentRange()
		{
			super(concatenateKeys(Key.PLOT, Key.FIXED_POINT_EXPONENT_RANGE));
			value = new IntegerRange(-3, 6);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			IntegerRange[] ranges =
			{
				new IntegerRange(MIN_FIXED_POINT_EXPONENT_BOUND, MAX_FIXED_POINT_EXPONENT_BOUND),
				new IntegerRange(MIN_FIXED_POINT_EXPONENT_BOUND, MAX_FIXED_POINT_EXPONENT_BOUND)
			};
			int[] outValues = input.parseIntegers(2, ranges, Order.GREATER_THAN_OR_EQUAL_TO);
			value = new IntegerRange(outValues[0], outValues[1]);
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return value.toString();
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public IntegerRange getFixedPointExponentRange()
	{
		return cpFixedPointExponentRange.getValue();
	}

	//------------------------------------------------------------------

	public void setFixedPointExponentRange(IntegerRange value)
	{
		cpFixedPointExponentRange.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPFixedPointExponentRange	cpFixedPointExponentRange	= new CPFixedPointExponentRange();

	//==================================================================


	// PROPERTY CLASS: NORMALISE SCIENTIFIC NOTATION


	private class CPNormaliseScientificNotation
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPNormaliseScientificNotation()
		{
			super(concatenateKeys(Key.PLOT, Key.NORMALISE_SCIENTIFIC_NOTATION));
			value = true;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isNormaliseScientificNotation()
	{
		return cpNormaliseScientificNotation.getValue();
	}

	//------------------------------------------------------------------

	public void setNormaliseScientificNotation(boolean value)
	{
		cpNormaliseScientificNotation.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPNormaliseScientificNotation	cpNormaliseScientificNotation	= new CPNormaliseScientificNotation();

	//==================================================================


	// PROPERTY CLASS: TRUNCATE X-SCALE TEXT


	private class CPTruncateXScaleText
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPTruncateXScaleText()
		{
			super(concatenateKeys(Key.PLOT, Key.TRUNCATE_X_SCALE_TEXT));
			value = true;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isTruncateXScaleText()
	{
		return cpTruncateXScaleText.getValue();
	}

	//------------------------------------------------------------------

	public void setTruncateXScaleText(boolean value)
	{
		cpTruncateXScaleText.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPTruncateXScaleText	cpTruncateXScaleText	= new CPTruncateXScaleText();

	//==================================================================


	// PROPERTY CLASS: PLOT FOCUSED BORDER COLOUR


	private class CPPlotColourFocusedBorder
		extends ColourProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPPlotColourFocusedBorder()
		{
			super(concatenateKeys(Key.PLOT, Key.COLOUR, Key.FOCUSED_BORDER));
			value = new Color(160, 128, 128);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getPlotColourFocusedBorder()
	{
		return cpPlotColourFocusedBorder.getValue();
	}

	//------------------------------------------------------------------

	public void setPlotColourFocusedBorder(Color value)
	{
		cpPlotColourFocusedBorder.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPPlotColourFocusedBorder	cpPlotColourFocusedBorder	= new CPPlotColourFocusedBorder();

	//==================================================================


	// PROPERTY CLASS: PLOT IMAGE MARGIN COLOUR


	private class CPPlotColourImageMargin
		extends ColourProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPPlotColourImageMargin()
		{
			super(concatenateKeys(Key.PLOT, Key.COLOUR, Key.IMAGE_MARGIN));
			value = new Color(232, 232, 224);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getPlotColourImageMargin()
	{
		return cpPlotColourImageMargin.getValue();
	}

	//------------------------------------------------------------------

	public void setPlotColourImageMargin(Color value)
	{
		cpPlotColourImageMargin.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPPlotColourImageMargin	cpPlotColourImageMargin	= new CPPlotColourImageMargin();

	//==================================================================


	// PROPERTY CLASS: PLOT BACKGROUND COLOUR


	private class CPPlotColourBackground
		extends ColourProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPPlotColourBackground()
		{
			super(concatenateKeys(Key.PLOT, Key.COLOUR, Key.BACKGROUND));
			value = Colours.BACKGROUND;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getPlotColourBackground()
	{
		return cpPlotColourBackground.getValue();
	}

	//------------------------------------------------------------------

	public void setPlotColourBackground(Color value)
	{
		cpPlotColourBackground.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPPlotColourBackground	cpPlotColourBackground	= new CPPlotColourBackground();

	//==================================================================


	// PROPERTY CLASS: PLOT GRID COLOUR


	private class CPPlotColourGrid
		extends ColourProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPPlotColourGrid()
		{
			super(concatenateKeys(Key.PLOT, Key.COLOUR, Key.GRID));
			value = new Color(216, 216, 216);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getPlotColourGrid()
	{
		return cpPlotColourGrid.getValue();
	}

	//------------------------------------------------------------------

	public void setPlotColourGrid(Color value)
	{
		cpPlotColourGrid.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPPlotColourGrid	cpPlotColourGrid	= new CPPlotColourGrid();

	//==================================================================


	// PROPERTY CLASS: PLOT AXIS COLOUR


	private class CPPlotColourAxis
		extends ColourProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPPlotColourAxis()
		{
			super(concatenateKeys(Key.PLOT, Key.COLOUR, Key.AXIS));
			value = new Color(144, 144, 144);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getPlotColourAxis()
	{
		return cpPlotColourAxis.getValue();
	}

	//------------------------------------------------------------------

	public void setPlotColourAxis(Color value)
	{
		cpPlotColourAxis.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPPlotColourAxis	cpPlotColourAxis	= new CPPlotColourAxis();

	//==================================================================


	// PROPERTY CLASS: PLOT SCALE COLOUR


	private class CPPlotColourScale
		extends ColourProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPPlotColourScale()
		{
			super(concatenateKeys(Key.PLOT, Key.COLOUR, Key.SCALE));
			value = Color.BLACK;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getPlotColourScale()
	{
		return cpPlotColourScale.getValue();
	}

	//------------------------------------------------------------------

	public void setPlotColourScale(Color value)
	{
		cpPlotColourScale.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPPlotColourScale	cpPlotColourScale	= new CPPlotColourScale();

	//==================================================================


	// PROPERTY CLASS: PATHNAME OF FUNCTION DIRECTORY


	private class CPFunctionPathname
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPFunctionPathname()
		{
			super(concatenateKeys(Key.FUNCTION, Key.DIRECTORY));
			value = PathnameUtils.USER_HOME_PREFIX;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getFunctionPathname()
	{
		return cpFunctionPathname.getValue();
	}

	//------------------------------------------------------------------

	public File getFunctionDirectory()
	{
		return new File(PathnameUtils.parsePathname(getFunctionPathname()));
	}

	//------------------------------------------------------------------

	public void setFunctionPathname(String value)
	{
		cpFunctionPathname.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPFunctionPathname	cpFunctionPathname	= new CPFunctionPathname();

	//==================================================================


	// PROPERTY CLASS: FUNCTION LIST SIZE


	private class CPFunctionListSize
		extends Property.SimpleProperty<Dimension>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPFunctionListSize()
		{
			super(concatenateKeys(Key.FUNCTION, Key.LIST_SIZE));
			value = new Dimension(32, 12);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			IntegerRange[] ranges =
			{
				new IntegerRange(FunctionView.MIN_FUNCTION_LIST_NUM_COLUMNS,
								 FunctionView.MAX_FUNCTION_LIST_NUM_COLUMNS),
				new IntegerRange(FunctionView.MIN_FUNCTION_LIST_NUM_ROWS,
								 FunctionView.MAX_FUNCTION_LIST_NUM_ROWS)
			};
			int[] outValues = input.parseIntegers(2, ranges);
			value = new Dimension(outValues[0], outValues[1]);
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return (value.width + ", " + value.height);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Dimension getFunctionListSize()
	{
		return cpFunctionListSize.getValue();
	}

	//------------------------------------------------------------------

	public void setFunctionListSize(Dimension value)
	{
		cpFunctionListSize.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPFunctionListSize	cpFunctionListSize	= new CPFunctionListSize();

	//==================================================================


	// PROPERTY CLASS: FUNCTION OBSCURED COLOUR


	private class CPFunctionObscuredColour
		extends ColourProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPFunctionObscuredColour()
		{
			super(concatenateKeys(Key.FUNCTION, Key.OBSCURED_COLOUR));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			value = parseColour(input);
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return ((value == null) ? null : ColourUtils.colourToRgbString(value));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Color getColour()
		{
			return ((value == null) ? DEFAULT_FUNCTION_OBSCURED_COLOUR : value);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isFunctionObscuredColour()
	{
		return (cpFunctionObscuredColour.getValue() != null);
	}

	//------------------------------------------------------------------

	public Color getFunctionObscuredColour()
	{
		return cpFunctionObscuredColour.getColour();
	}

	//------------------------------------------------------------------

	public void setFunctionObscuredColour(Color value)
	{
		cpFunctionObscuredColour.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPFunctionObscuredColour	cpFunctionObscuredColour	= new CPFunctionObscuredColour();

	//==================================================================


	// PROPERTY CLASS: FUNCTION COLOURS


	private class CPFunctionColours
		extends Property.PropertyList<Color>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	NUM_COLOURS	= FunctionDocument.MAX_NUM_FUNCTIONS;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPFunctionColours()
		{
			super(concatenateKeys(Key.FUNCTION, Key.COLOUR), NUM_COLOURS);
			values.addAll(DEFAULT_FUNCTION_COLOURS);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void parse(Input input,
							 int   index)
		{
			try
			{
				values.set(index, ColourProperty.parseColour(input));
			}
			catch (AppException e)
			{
				showWarningMessage(e);
			}
		}

		//--------------------------------------------------------------

		@Override
		protected String toString(int index)
		{
			return ColourUtils.colourToRgbString(values.get(index));
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getFunctionColour(int index)
	{
		return cpFunctionColours.getValue(index);
	}

	//------------------------------------------------------------------

	public void setFunctionColour(int   index,
								  Color value)
	{
		cpFunctionColours.setValue(index, value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPFunctionColours	cpFunctionColours	= new CPFunctionColours();

	//==================================================================


	// PROPERTY CLASS: FONTS


	private class CPFonts
		extends Property.PropertyMap<AppFont, FontEx>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	DEFAULT_PLOT_FONT_SIZE	= 10;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPFonts()
		{
			super(Key.FONT, AppFont.class);
			for (AppFont font : AppFont.values())
				values.put(font, new FontEx());
			values.get(AppFont.PLOT).setSize(DEFAULT_PLOT_FONT_SIZE);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input   input,
						  AppFont appFont)
		{
			try
			{
				FontEx font = new FontEx(input.getValue());
				appFont.setFontEx(font);
				values.put(appFont, font);
			}
			catch (IllegalArgumentException e)
			{
				showWarningMessage(new IllegalValueException(input));
			}
			catch (uk.blankaspect.common.exception.ValueOutOfBoundsException e)
			{
				showWarningMessage(new ValueOutOfBoundsException(input));
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString(AppFont appFont)
		{
			return getValue(appFont).toString();
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public FontEx getFont(int index)
	{
		return cpFonts.getValue(AppFont.values()[index]);
	}

	//------------------------------------------------------------------

	public void setFont(int    index,
						FontEx font)
	{
		cpFonts.setValue(AppFont.values()[index], font);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPFonts	cpFonts	= new CPFonts();

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private AppConfig()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void showWarningMessage(AppException exception)
	{
		App.INSTANCE.showWarningMessage(App.SHORT_NAME + " : " + CONFIG_ERROR_STR, exception);
	}

	//------------------------------------------------------------------

	public static void showErrorMessage(AppException exception)
	{
		App.INSTANCE.showErrorMessage(App.SHORT_NAME + " : " + CONFIG_ERROR_STR, exception);
	}

	//------------------------------------------------------------------

	private static File getFile()
		throws AppException
	{
		File file = null;

		// Get location of container of class file of application
		Path containerLocation = null;
		try
		{
			containerLocation = ClassUtils.getClassFileContainer(AppConfig.class);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// Get pathname of configuration directory from properties file
		String pathname = null;
		Path propertiesFile = (containerLocation == null) ? Path.of(PROPERTIES_FILENAME)
														  : containerLocation.resolveSibling(PROPERTIES_FILENAME);
		if (Files.isRegularFile(propertiesFile, LinkOption.NOFOLLOW_LINKS))
		{
			try
			{
				Properties properties = new Properties();
				properties.loadFromXML(new FileInputStream(propertiesFile.toFile()));
				pathname = properties.getProperty(CONFIG_DIR_KEY);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.ERROR_READING_PROPERTIES_FILE, propertiesFile.toFile());
			}
		}

		// Get pathname of configuration directory from system property or set system property to pathname
		try
		{
			if (pathname == null)
				pathname = System.getProperty(CONFIG_DIR_KEY);
			else
				System.setProperty(CONFIG_DIR_KEY, pathname);
		}
		catch (SecurityException e)
		{
			// ignore
		}

		// Look for configuration file in default locations
		if (pathname == null)
		{
			// Look for configuration file in local directory
			file = new File(CONFIG_FILENAME);

			// Look for configuration file in default configuration directory
			if (!file.isFile())
			{
				file = null;
				pathname = Utils.getPropertiesPathname();
				if (pathname != null)
				{
					file = new File(pathname, CONFIG_FILENAME);
					if (!file.isFile())
						file = null;
				}
			}
		}

		// Get location of configuration file from pathname of configuration directory
		else if (!pathname.isEmpty())
		{
			file = new File(PathnameUtils.parsePathname(pathname), CONFIG_FILENAME);
			if (!file.isFile())
				throw new FileException(ErrorId.NO_CONFIGURATION_FILE, file);
		}

		return file;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public File chooseFile(Component parent)
	{
		if (fileChooser == null)
		{
			fileChooser = new JFileChooser();
			fileChooser.setDialogTitle(SAVE_CONFIGURATION_FILE_STR);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setFileFilter(new FilenameSuffixFilter(AppConstants.XML_FILES_STR,
															   AppConstants.XML_FILE_SUFFIX));
			selectedFile = file;
		}

		fileChooser.setSelectedFile((selectedFile == null) ? new File(CONFIG_FILENAME).getAbsoluteFile()
														   : selectedFile.getAbsoluteFile());
		fileChooser.rescanCurrentDirectory();
		if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
		{
			selectedFile = Utils.appendSuffix(fileChooser.getSelectedFile(), AppConstants.XML_FILE_SUFFIX);
			return selectedFile;
		}
		return null;
	}

	//------------------------------------------------------------------

	public void read()
	{
		// Read configuration file
		fileRead = false;
		ConfigFile configFile = null;
		try
		{
			file = getFile();
			if (file != null)
			{
				configFile = new ConfigFile();
				configFile.read(file);
				fileRead = true;
			}
		}
		catch (AppException e)
		{
			showErrorMessage(e);
		}

		// Get properties
		Property.ISource propertySource = Property.getSystemSource();
		if (fileRead)
			getProperties(configFile, propertySource);
		else
			getProperties(propertySource);

		// Reset changed status of properties
		resetChanged();
	}

	//------------------------------------------------------------------

	public void write()
	{
		if (isChanged())
		{
			try
			{
				if (file == null)
				{
					if (System.getProperty(CONFIG_DIR_KEY) == null)
					{
						String pathname = Utils.getPropertiesPathname();
						if (pathname != null)
						{
							File directory = new File(pathname);
							if (!directory.exists() && !directory.mkdirs())
								throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, directory);
							file = new File(directory, CONFIG_FILENAME);
						}
					}
				}
				else
				{
					if (!fileRead)
						file.renameTo(new File(file.getParentFile(), CONFIG_OLD_FILENAME));
				}
				if (file != null)
				{
					write(file);
					resetChanged();
				}
			}
			catch (AppException e)
			{
				showErrorMessage(e);
			}
		}
	}

	//------------------------------------------------------------------

	public void write(File file)
		throws AppException
	{
		// Initialise progress view
		IProgressView progressView = Task.getProgressView();
		if (progressView != null)
		{
			progressView.setInfo(WRITING_STR, file);
			progressView.setProgress(0, -1.0);
		}

		// Create new DOM document
		ConfigFile configFile = new ConfigFile(Integer.toString(VERSION));

		// Set configuration properties in document
		putProperties(configFile);

		// Write file
		configFile.write(file);
	}

	//------------------------------------------------------------------

	private void getProperties(Property.ISource... propertySources)
	{
		for (Property property : getProperties())
		{
			try
			{
				property.get(propertySources);
			}
			catch (AppException e)
			{
				showWarningMessage(e);
			}
		}
	}

	//------------------------------------------------------------------

	private void putProperties(Property.ITarget propertyTarget)
	{
		for (Property property : getProperties())
			property.put(propertyTarget);
	}

	//------------------------------------------------------------------

	private boolean isChanged()
	{
		for (Property property : getProperties())
		{
			if (property.isChanged())
				return true;
		}
		return false;
	}

	//------------------------------------------------------------------

	private void resetChanged()
	{
		for (Property property : getProperties())
			property.setChanged(false);
	}

	//------------------------------------------------------------------

	private List<Property> getProperties()
	{
		if (properties == null)
		{
			properties = new ArrayList<>();
			for (Field field : getClass().getDeclaredFields())
			{
				try
				{
					if (field.getName().startsWith(Property.FIELD_PREFIX))
						properties.add((Property)field.get(this));
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
			}
		}
		return properties;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		INSTANCE = new AppConfig();
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	File			file;
	private	boolean			fileRead;
	private	File			selectedFile;
	private	JFileChooser	fileChooser;
	private	List<Property>	properties;

}

//----------------------------------------------------------------------
