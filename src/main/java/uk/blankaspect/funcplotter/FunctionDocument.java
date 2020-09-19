/*====================================================================*\

FunctionDocument.java

Function document class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.awt.image.BufferedImage;

import java.beans.PropertyChangeListener;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;

import java.math.BigDecimal;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;
import uk.blankaspect.common.exception.ValueOutOfBoundsException;

import uk.blankaspect.common.list.IListModel;

import uk.blankaspect.common.misc.FileWritingMode;
import uk.blankaspect.common.misc.SystemUtils;
import uk.blankaspect.common.misc.TextFile;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.swing.colour.ColourUtils;

import uk.blankaspect.common.swing.image.PngOutputFile;

import uk.blankaspect.common.xml.AttributeList;
import uk.blankaspect.common.xml.Comment;
import uk.blankaspect.common.xml.XmlConstants;
import uk.blankaspect.common.xml.XmlParseException;
import uk.blankaspect.common.xml.XmlUtils;
import uk.blankaspect.common.xml.XmlValidationException;
import uk.blankaspect.common.xml.XmlWriter;

//----------------------------------------------------------------------


// FUNCTION DOCUMENT CLASS


class FunctionDocument
	implements IListModel<Function>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int	MAX_NUM_FUNCTIONS	= 20;

	public static final		int	MIN_MAX_EDIT_LIST_LENGTH		= 1;
	public static final		int	MAX_MAX_EDIT_LIST_LENGTH		= 9999;
	public static final		int	DEFAULT_MAX_EDIT_LIST_LENGTH	= 200;

	public static final		String	X_INTERVAL_KEY	= "xInterval";
	public static final		String	Y_INTERVAL_KEY	= "yInterval";
	public static final		String	FUNCTION_KEY	= "function";

	private static final	int	VERSION					= 0;
	private static final	int	MIN_SUPPORTED_VERSION	= 0;
	private static final	int	MAX_SUPPORTED_VERSION	= 0;

	private static final	int	MERGE_INTERVAL_EDITS_INTERVAL	= 500;

	private static final	char	INTERVAL_PREFIX_CHAR	= '@';
	private static final	char	COMMENT_PREFIX_CHAR		= '#';

	private static final	String	XML_PREFIX			= "<?xml";
	private static final	String	XML_VERSION_STR		= "1.0";

	private static final	String	NAMESPACE_NAME			= "http://ns.blankaspect.uk/funcPlotter-1";
	private static final	String	OLD_NAMESPACE_NAME		= "data:text/plain;charset=UTF-8,FuncPlotter/functionList";
	private static final	String	NAMESPACE_NAME_REGEX	= "http://ns\\.[a-z.]+/funcPlotter-1|" + OLD_NAMESPACE_NAME;

	private static final	String	UNNAMED_STR					= "Unnamed";
	private static final	String	LINE_STR					= "Line ";
	private static final	String	PNG_STR						= "png";
	private static final	String	READING_STR					= "Reading";
	private static final	String	WRITING_STR					= "Writing";
	private static final	String	COMMENT_STR					= "Comment";
	private static final	String	DELETE_STR					= "Delete";
	private static final	String	ADD_FUNCTION_STR			= "Add function";
	private static final	String	EDIT_FUNCTION_STR			= "Edit function";
	private static final	String	DELETE_FUNCTION_STR			= "Delete function";
	private static final	String	DELETE_ALL_FUNCTIONS_STR	= "Delete all functions";
	private static final	String	CLEAR_EDIT_LIST_STR			= "Do you want to clear all the " +
																	"undo/redo actions?";
	private static final	String	DELETE_MESSAGE_STR			= "Do you want to delete the selected " +
																	"function?";
	private static final	String	DELETE_ALL_MESSAGE_STR		= "Do you want to delete all the " +
																	"functions?";

	private enum ViewComponent
	{
		PLOT,
		BUTTONS,
		X_INTERVAL,
		Y_INTERVAL,
		INTERVALS,
		FUNCTION_LIST
	}

	private enum TextState
	{
		COMMENT_FIRST_LINE,
		COMMENT,
		STATEMENT,
		INTERVAL,
		FUNCTION,
		DONE
	}

	private interface ElementName
	{
		String	FUNCTION		= "function";
		String	FUNCTION_LIST	= "functionList";
	}

	private interface AttrName
	{
		String	COLOUR		= "colour";
		String	EXPRESSION	= "expression";
		String	VERSION		= "version";
		String	XMLNS		= "xmlns";
		String	X_INTERVAL	= "xInterval";
		String	Y_INTERVAL	= "yInterval";
	}

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// COMMANDS


	enum Command
		implements Action
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		// Commands

		UNDO
		(
			"undo",
			"Undo",
			KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK)
		),

		REDO
		(
			"redo",
			"Redo",
			KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK)
		),

		CLEAR_EDIT_LIST
		(
			"clearEditList",
			"Clear edit history" + AppConstants.ELLIPSIS_STR
		),

		EDIT_COMMENT
		(
			"editComment",
			"Comment" + AppConstants.ELLIPSIS_STR,
			KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK)
		),

		ADD_FUNCTION
		(
			"addFunction",
			"Add" + AppConstants.ELLIPSIS_STR,
			"Add a function"
		),

		EDIT_FUNCTION
		(
			"editFunction",
			"Edit" + AppConstants.ELLIPSIS_STR,
			"Edit the selected function"
		),

		COPY_FUNCTION
		(
			"copyFunction",
			"Copy",
			"Copy the selected function to the clipboard"
		),

		VIEW_FUNCTION
		(
			"viewFunction",
			"View"
		),

		TOGGLE_HIGHLIGHT_FUNCTION
		(
			"toggleHighlightFunction",
			"Highlight"
		),

		TOGGLE_HIDE_FUNCTION
		(
			"toggleHideFunction",
			"Hide"
		),

		DELETE_FUNCTION
		(
			"deleteFunction",
			"Delete",
			"Delete the selected function"
		),

		CONFIRM_DELETE_FUNCTION
		(
			"confirmDeleteFunction"
		),

		CLEAR_FUNCTIONS
		(
			"clearFunctions",
			"Clear",
			"Delete all functions"
		),

		MOVE_FUNCTION
		(
			"moveFunction"
		),

		REVERSE_FUNCTIONS
		(
			"reverseFunctions",
			"Reverse order of functions"
		),

		SCROLL_LEFT
		(
			"scrollLeft",
			Icons.ARROW_LEFT,
			"Scroll left"
		),

		SCROLL_RIGHT
		(
			"scrollRight",
			Icons.ARROW_RIGHT,
			"Scroll right"
		),

		SCROLL_DOWN
		(
			"scrollDown",
			Icons.ARROW_DOWN,
			"Scroll down"
		),

		SCROLL_UP
		(
			"scrollUp",
			Icons.ARROW_UP,
			"Scroll up"
		),

		CENTRE_ON_ORIGIN
		(
			"centreOnOrigin",
			Icons.ORIGIN,
			"Centre on origin"
		),

		X_ZOOM_OUT
		(
			"xZoomOut",
			Icons.ZOOM_OUT,
			"x zoom out"
		),

		X_ZOOM_IN
		(
			"xZoomIn",
			Icons.ZOOM_IN,
			"x zoom in"
		),

		Y_ZOOM_OUT
		(
			"yZoomOut",
			Icons.ZOOM_OUT,
			"y zoom out"
		),

		Y_ZOOM_IN
		(
			"yZoomIn",
			Icons.ZOOM_IN,
			"y zoom in"
		),

		SET_X_INTERVAL
		(
			"setXInterval"
		),

		SET_Y_INTERVAL
		(
			"setYInterval"
		),

		SET_INTERVALS
		(
			"setIntervals"
		),

		TOGGLE_SHOW_GRID
		(
			"toggleShowGrid",
			"Grid",
			KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK)
		);

		//--------------------------------------------------------------

		// Property keys
		interface Property
		{
			String	END			= "end";
			String	INDEX		= "index";
			String	X_INTERVAL	= "xInterval";
			String	Y_INTERVAL	= "yInterval";
		}

		// Other constants
		public static final	String	UNDO_STR	= "Undo the previous edit";
		public static final	String	REDO_STR	= "Redo the next edit";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Command(String key)
		{
			command = new uk.blankaspect.common.swing.action.Command(this);
			putValue(Action.ACTION_COMMAND_KEY, key);
		}

		//--------------------------------------------------------------

		private Command(String key,
						String name)
		{
			this(key);
			putValue(Action.NAME, name);
		}

		//--------------------------------------------------------------

		private Command(String key,
						String name,
						String text)
		{
			this(key, name);
			putValue(Action.SHORT_DESCRIPTION, text);
		}

		//--------------------------------------------------------------

		private Command(String    key,
						ImageIcon icon,
						String    text)
		{
			this(key);
			putValue(Action.LARGE_ICON_KEY, icon);
			putValue(Action.SHORT_DESCRIPTION, text);
		}

		//--------------------------------------------------------------

		private Command(String    key,
						String    name,
						KeyStroke acceleratorKey)
		{
			this(key, name);
			putValue(Action.ACCELERATOR_KEY, acceleratorKey);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static void setAllEnabled(boolean enabled)
		{
			for (Command command : values())
				command.setEnabled(enabled);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Action interface
	////////////////////////////////////////////////////////////////////

		public void addPropertyChangeListener(PropertyChangeListener listener)
		{
			command.addPropertyChangeListener(listener);
		}

		//--------------------------------------------------------------

		public Object getValue(String key)
		{
			return command.getValue(key);
		}

		//--------------------------------------------------------------

		public boolean isEnabled()
		{
			return command.isEnabled();
		}

		//--------------------------------------------------------------

		public void putValue(String key,
							 Object value)
		{
			command.putValue(key, value);
		}

		//--------------------------------------------------------------

		public void removePropertyChangeListener(PropertyChangeListener listener)
		{
			command.removePropertyChangeListener(listener);
		}

		//--------------------------------------------------------------

		public void setEnabled(boolean enabled)
		{
			command.setEnabled(enabled);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		public void actionPerformed(ActionEvent event)
		{
			FunctionDocument document = App.INSTANCE.getDocument();
			if (document != null)
				document.executeCommand(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void setSelected(boolean selected)
		{
			putValue(Action.SELECTED_KEY, selected);
		}

		//--------------------------------------------------------------

		public void execute()
		{
			actionPerformed(null);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	uk.blankaspect.common.swing.action.Command	command;

	}

	//==================================================================


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		FILE_DOES_NOT_EXIST
		("The file does not exist."),

		NOT_A_FILE
		("The pathname does not denote a normal file."),

		ERROR_WRITING_FILE
		("An error occurred when writing the file."),

		INVALID_DOCUMENT
		("The file is not a valid " + App.SHORT_NAME + " document."),

		UNEXPECTED_DOCUMENT_FORMAT
		("The document does not have the expected format."),

		UNSUPPORTED_DOCUMENT_VERSION
		("The version of the document (%1) is not supported by this version of " + App.SHORT_NAME + "."),

		NO_ATTRIBUTE
		("The required attribute is missing."),

		INVALID_ATTRIBUTE
		("The attribute is invalid."),

		ATTRIBUTE_OUT_OF_BOUNDS
		("The attribute value is out of bounds."),

		MULTIPLE_COMMENT_ELEMENTS
		("The file contains more than one comment element."),

		INVALID_INTERVAL_IDENTIFIER
		("The interval identifier is invalid."),

		INVALID_INTERVAL
		("The interval is invalid."),

		INVALID_INTERVAL_LOWER_ENDPOINT
		("The lower endpoint of the interval is invalid."),

		INTERVAL_LOWER_ENDPOINT_OUT_OF_BOUNDS
		("The lower endpoint of the interval must be between " + PlotInterval.MIN_VALUE + " and " +
			PlotInterval.MAX_VALUE + "."),

		INTERVAL_LOWER_ENDPOINT_HAS_TOO_MANY_SIGNIFICANT_DIGITS
		("The lower endpoint of the interval must not have more than " +
			PlotInterval.MAX_NUM_SIGNIFICANT_DIGITS + " significant digits."),

		INVALID_INTERVAL_UPPER_ENDPOINT
		("The upper endpoint of the interval is invalid."),

		INTERVAL_UPPER_ENDPOINT_OUT_OF_BOUNDS
		("The upper endpoint of the interval must be between " + PlotInterval.MIN_VALUE + " and " +
			PlotInterval.MAX_VALUE + "."),

		INTERVAL_UPPER_ENDPOINT_HAS_TOO_MANY_SIGNIFICANT_DIGITS
		("The upper endpoint of the interval must not have more than " +
			PlotInterval.MAX_NUM_SIGNIFICANT_DIGITS + " significant digits."),

		INTERVAL_ENDPOINTS_OUT_OF_ORDER
		("The upper endpoint of the interval is less than or equal to the lower endpoint."),

		TOO_MANY_FUNCTIONS
		("The document contains too many functions."),

		INVALID_COLOUR
		("The colour is invalid."),

		COLOUR_COMPONENT_OUT_OF_BOUNDS
		("Colour component values must be between 0 and 255."),

		NOT_ENOUGH_MEMORY_TO_PERFORM_COMMAND
		("There was not enough memory to perform the command.\n" +
			"Clearing the list of undo/redo actions may make more memory available.");

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


	// FILE INFORMATION CLASS


	public static class FileInfo
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public FileInfo(File     file,
						FileKind fileKind)
		{
			this.file = file;
			this.fileKind = fileKind;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		File		file;
		FileKind	fileKind;

	}

	//==================================================================


	// EDIT CLASS


	public static abstract class Edit
	{

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// ADD FUNCTION EDIT CLASS


		private static class Add
			extends Edit
		{

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Add(int      index,
						Function function)
			{
				this.index = index;
				this.function = function.clone();
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			protected void undo(FunctionDocument document)
			{
				document.getFunctionList().removeElement(index);
			}

			//----------------------------------------------------------

			@Override
			protected void redo(FunctionDocument document)
			{
				document.getFunctionList().addElement(index, function);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	int			index;
			private	Function	function;

		}

		//==============================================================


		// REMOVE FUNCTION EDIT CLASS


		private static class Remove
			extends Edit
		{

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Remove(int      index,
						   Function function)
			{
				this.index = index;
				this.function = function.clone();
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			protected void undo(FunctionDocument document)
			{
				document.getFunctionList().addElement(index, function);
			}

			//----------------------------------------------------------

			@Override
			protected void redo(FunctionDocument document)
			{
				document.getFunctionList().removeElement(index);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	int			index;
			private	Function	function;

		}

		//==============================================================


		// X INTERVAL EDIT CLASS


		private static class XInterval
			extends Edit
		{

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private XInterval(PlotInterval oldInterval,
							  PlotInterval newInterval)
			{
				this.oldInterval = new PlotInterval(oldInterval);
				this.newInterval = new PlotInterval(newInterval);
				timestamp = System.currentTimeMillis();
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			protected void undo(FunctionDocument document)
			{
				document.setXInterval(oldInterval);
			}

			//----------------------------------------------------------

			@Override
			protected void redo(FunctionDocument document)
			{
				document.setXInterval(newInterval);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	PlotInterval	oldInterval;
			private	PlotInterval	newInterval;
			private	long			timestamp;

		}

		//==============================================================


		// Y INTERVAL EDIT CLASS


		private static class YInterval
			extends Edit
		{

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private YInterval(PlotInterval oldInterval,
							  PlotInterval newInterval)
			{
				this.oldInterval = new PlotInterval(oldInterval);
				this.newInterval = new PlotInterval(newInterval);
				timestamp = System.currentTimeMillis();
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			protected void undo(FunctionDocument document)
			{
				document.setYInterval(oldInterval);
			}

			//----------------------------------------------------------

			@Override
			protected void redo(FunctionDocument document)
			{
				document.setYInterval(newInterval);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	PlotInterval	oldInterval;
			private	PlotInterval	newInterval;
			private	long			timestamp;

		}

		//==============================================================


		// COMMENT EDIT CLASS


		private static class Comment
			extends Edit
		{

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Comment(String oldText,
							String newText)
			{
				this.oldText = oldText;
				this.newText = newText;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			protected void undo(FunctionDocument document)
			{
				document.comment.setText(oldText);
			}

			//----------------------------------------------------------

			@Override
			protected void redo(FunctionDocument document)
			{
				document.comment.setText(newText);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	String	oldText;
			private	String	newText;

		}

		//==============================================================


		// COMPOUND EDIT CLASS


		private static class Compound
			extends Edit
		{

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Compound()
			{
				edits = new ArrayList<>();
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			protected void undo(FunctionDocument document)
			{
				for (int i = edits.size() - 1; i >= 0; i--)
					edits.get(i).undo(document);
			}

			//----------------------------------------------------------

			@Override
			protected void redo(FunctionDocument document)
			{
				for (Edit edit : edits)
					edit.redo(document);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods
		////////////////////////////////////////////////////////////////

			public void addEdit(Edit edit)
			{
				edits.add(edit);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	List<Edit>	edits;

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Edit()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract void undo(FunctionDocument document);

		//--------------------------------------------------------------

		protected abstract void redo(FunctionDocument document);

		//--------------------------------------------------------------

	}

	//==================================================================


	// EDIT LIST CLASS


	private static class EditList
		extends LinkedList<Edit>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EditList()
		{
			maxLength = AppConfig.INSTANCE.getMaxEditListLength();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void clear()
		{
			super.clear();
			unchangedIndex = currentIndex = 0;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Edit removeUndo()
		{
			return (canUndo() ? get(--currentIndex) : null);
		}

		//--------------------------------------------------------------

		private Edit removeRedo()
		{
			return (canRedo() ? get(currentIndex++) : null);
		}

		//--------------------------------------------------------------

		private boolean canUndo()
		{
			return (currentIndex > 0);
		}

		//--------------------------------------------------------------

		private boolean canRedo()
		{
			return (currentIndex < size());
		}

		//--------------------------------------------------------------

		private boolean isChanged()
		{
			return (currentIndex != unchangedIndex);
		}

		//--------------------------------------------------------------

		private void addEdit(Edit edit)
		{
			// Remove redos
			while (size() > currentIndex)
				removeLast();

			// Preserve changed status if unchanged state cannot be recovered
			if (unchangedIndex > currentIndex)
				unchangedIndex = -1;

			// Merge intervals
			mergeXIntervals(edit);
			mergeYIntervals(edit);

			// Remove oldest edits while list is full
			while (size() >= maxLength)
			{
				removeFirst();
				if (--unchangedIndex < 0)
					unchangedIndex = -1;
				if (--currentIndex < 0)
					currentIndex = 0;
			}

			// Add new edit
			add(edit);
			++currentIndex;
		}

		//--------------------------------------------------------------

		private void reset()
		{
			while (size() > currentIndex)
				removeLast();

			unchangedIndex = currentIndex;
		}

		//--------------------------------------------------------------

		private void mergeXIntervals(Edit edit)
		{
			if (edit instanceof Edit.XInterval)
			{
				Edit.XInterval currEdit = (Edit.XInterval)edit;
				while (canUndo())
				{
					if (!(getLast() instanceof Edit.XInterval))
						break;
					Edit.XInterval prevEdit = (Edit.XInterval)getLast();
					if (currEdit.timestamp - prevEdit.timestamp > MERGE_INTERVAL_EDITS_INTERVAL)
						break;
					currEdit.oldInterval = prevEdit.oldInterval;
					removeLast();
					--currentIndex;
				}
			}
		}

		//--------------------------------------------------------------

		private void mergeYIntervals(Edit edit)
		{
			if (edit instanceof Edit.YInterval)
			{
				Edit.YInterval currEdit = (Edit.YInterval)edit;
				while (canUndo())
				{
					if (!(getLast() instanceof Edit.YInterval))
						break;
					Edit.YInterval prevEdit = (Edit.YInterval)getLast();
					if (currEdit.timestamp - prevEdit.timestamp > MERGE_INTERVAL_EDITS_INTERVAL)
						break;
					currEdit.oldInterval = prevEdit.oldInterval;
					removeLast();
					--currentIndex;
				}
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int	maxLength;
		private	int	currentIndex;
		private	int	unchangedIndex;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FunctionDocument()
	{
		fileKind = AppConfig.INSTANCE.getDefaultFileKind();
		functions = new ArrayList<>();
		xInterval = new PlotInterval();
		yInterval = new PlotInterval();
		comment = new Comment();
		editList = new EditList();
	}

	//------------------------------------------------------------------

	public FunctionDocument(int unnamedIndex)
	{
		this();
		this.unnamedIndex = unnamedIndex;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static boolean canWriteImages()
	{
		return (Utils.indexOf(PNG_STR, ImageIO.getWriterFormatNames()) >= 0);
	}

	//------------------------------------------------------------------

	private static MainWindow getWindow()
	{
		return App.INSTANCE.getMainWindow();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IListModel interface
////////////////////////////////////////////////////////////////////////

	public int getNumElements()
	{
		return getNumFunctions();
	}

	//------------------------------------------------------------------

	public Function getElement(int index)
	{
		return getFunction(index);
	}

	//------------------------------------------------------------------

	public String getElementText(int index)
	{
		return getFunction(index).toString();
	}

	//------------------------------------------------------------------

	public void setElement(int      index,
						   Function function)
	{
		functions.set(index, function);
	}

	//------------------------------------------------------------------

	public void addElement(int      index,
						   Function function)
	{
		for (Function func : functions)
		{
			if (func.isHighlighted())
			{
				function.setHighlight(Function.Highlight.OBSCURED);
				break;
			}
		}
		functions.add(index, function);
	}

	//------------------------------------------------------------------

	public Function removeElement(int index)
	{
		return functions.remove(index);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public File getFile()
	{
		return file;
	}

	//------------------------------------------------------------------

	public FileInfo getFileInfo()
	{
		return new FileInfo(file, fileKind);
	}

	//------------------------------------------------------------------

	public long getTimestamp()
	{
		return timestamp;
	}

	//------------------------------------------------------------------

	public PlotInterval getXInterval()
	{
		return xInterval;
	}

	//------------------------------------------------------------------

	public PlotInterval getYInterval()
	{
		return yInterval;
	}

	//------------------------------------------------------------------

	public boolean hasComments()
	{
		return commented;
	}

	//------------------------------------------------------------------

	public boolean isExecutingCommand()
	{
		return executingCommand;
	}

	//------------------------------------------------------------------

	public boolean isChanged()
	{
		return editList.isChanged();
	}

	//------------------------------------------------------------------

	public int getNumFunctions()
	{
		return functions.size();
	}

	//------------------------------------------------------------------

	public Function getFunction(int index)
	{
		return functions.get(index);
	}

	//------------------------------------------------------------------

	public boolean hasFunctions()
	{
		return !functions.isEmpty();
	}

	//------------------------------------------------------------------

	public boolean isFull()
	{
		return (functions.size() >= MAX_NUM_FUNCTIONS);
	}

	//------------------------------------------------------------------

	public String getTitleString(boolean fullPathname)
	{
		String str = (file == null) ? UNNAMED_STR + unnamedIndex
									: fullPathname ? Utils.getPathname(file)
												   : file.getName();
		if (isChanged())
			str += AppConstants.FILE_CHANGED_SUFFIX;
		return str;
	}

	//------------------------------------------------------------------

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}

	//------------------------------------------------------------------

	public void setIntervals(PlotInterval xInterval,
							 PlotInterval yInterval)
	{
		setXInterval(xInterval);
		setYInterval(yInterval);
	}

	//------------------------------------------------------------------

	public void read(FileInfo     fileInfo,
					 List<String> errorStrs)
		throws AppException
	{
		// Clear error strings
		errorStrs.clear();

		// Test whether file exists
		if (!fileInfo.file.exists())
			throw new FileException(ErrorId.FILE_DOES_NOT_EXIST, fileInfo.file);
		if (!fileInfo.file.isFile())
			throw new FileException(ErrorId.NOT_A_FILE, fileInfo.file);

		// Set instance variables
		file = fileInfo.file;
		fileKind = fileInfo.fileKind;

		// Reset progress view
		TaskProgressDialog progressView = (TaskProgressDialog)Task.getProgressView();
		progressView.setInfo(READING_STR, file);
		progressView.setProgress(0, 0.0);

		// Run garbage collector to maximise available memory
		System.gc();

		// Read file
		TextFile textFile = new TextFile(file, StandardCharsets.UTF_8);
		textFile.addProgressListener(progressView);
		StringBuilder text = textFile.read();

		// Change line separators to LFs
		TextFile.changeLineSeparators(text, false);

		// Parse file
		if (fileKind == null)
			fileKind = FileKind.forFilename(file.getName());
		if (fileKind == null)
			fileKind = isXml(text) ? FileKind.XML : FileKind.TEXT;
		switch (fileKind)
		{
			case XML:
				parseXml(text);
				break;

			case TEXT:
				parseText(text, errorStrs);
				break;
		}

		// Set timestamp
		if (errorStrs.isEmpty())
			timestamp = file.lastModified();
	}

	//------------------------------------------------------------------

	public void write(FileInfo fileInfo,
					  boolean  includeColours)
		throws AppException
	{
		// Set instance variables
		if (fileInfo != null)
		{
			file = fileInfo.file;
			if (fileInfo.fileKind != null)
				fileKind = fileInfo.fileKind;
		}

		// Get text according to file kind
		CharSequence text = null;
		switch (fileKind)
		{
			case XML:
				text = getXml(includeColours);
				break;

			case TEXT:
				text = getText(includeColours);
				break;
		}

		// Update information field in progress view
		TaskProgressDialog progressView = (TaskProgressDialog)Task.getProgressView();
		progressView.setInfo(WRITING_STR, file);
		progressView.setProgress(0, 0.0);

		long timestamp = this.timestamp;
		this.timestamp = 0;
		try
		{
			// Write file
			TextFile textFile = new TextFile(file, StandardCharsets.UTF_8);
			textFile.addProgressListener(progressView);
			textFile.write(text, FileWritingMode.USE_TEMP_FILE);

			// Set timestamp
			timestamp = file.lastModified();
		}
		finally
		{
			this.timestamp = timestamp;
		}

		// Reset list of edits
		if (AppConfig.INSTANCE.isClearEditListOnSave())
			editList.clear();
		else
			editList.reset();
	}

	//------------------------------------------------------------------

	public void writeImage(File file)
		throws AppException
	{
		// Reset progress view
		TaskProgressDialog progressView = (TaskProgressDialog)Task.getProgressView();
		progressView.setInfo(WRITING_STR, file);
		progressView.setProgress(0, -1.0);

		// Write file
		PngOutputFile imageFile = new PngOutputFile(file, getImage());
		imageFile.addProgressListener(progressView);
		imageFile.write(FileWritingMode.USE_TEMP_FILE);
	}

	//------------------------------------------------------------------

	public void parseStartupParam(String name,
								  String value,
								  int    functionIndex)
	{
		try
		{
			if (name.equals(X_INTERVAL_KEY))
				setXInterval(parseInterval(value));

			else if (name.equals(Y_INTERVAL_KEY))
				setYInterval(parseInterval(value));

			else if (name.equals(FUNCTION_KEY))
			{
				if (!isFull())
				{
					Color defaultColour = AppConfig.INSTANCE.getFunctionColour(functionIndex++);
					functions.add(parseFunction(value, defaultColour));
				}
			}
		}
		catch (AppException e)
		{
			App.INSTANCE.showErrorMessage(App.SHORT_NAME, e);
		}
	}

	//------------------------------------------------------------------

	public void updateCommands()
	{
		int index = getSelectedIndex();
		boolean isFunctionSelected = hasFunctions() && (index >= 0);
		boolean isView = (getView() != null);

		Command.setAllEnabled(true);

		Command.UNDO.setEnabled(editList.canUndo());
		Command.REDO.setEnabled(editList.canRedo());
		Command.CLEAR_EDIT_LIST.setEnabled(!editList.isEmpty());
		Command.ADD_FUNCTION.setEnabled(!isFull());
		Command.EDIT_FUNCTION.setEnabled(isFunctionSelected);
		Command.COPY_FUNCTION.setEnabled(AppConfig.INSTANCE.hasPermissionAccessClipboard() &&
										  isFunctionSelected);
		Command.VIEW_FUNCTION.setEnabled(isFunctionSelected);
		Command.TOGGLE_HIGHLIGHT_FUNCTION.setEnabled(isFunctionSelected && (getNumFunctions() > 1));
		Command.TOGGLE_HIGHLIGHT_FUNCTION.setSelected(isFunctionSelected &&
													   getFunction(index).isHighlighted());
		Command.TOGGLE_HIDE_FUNCTION.setEnabled(isFunctionSelected);
		Command.TOGGLE_HIDE_FUNCTION.setSelected(isFunctionSelected && getFunction(index).isHidden());
		Command.DELETE_FUNCTION.setEnabled(isFunctionSelected);
		Command.CONFIRM_DELETE_FUNCTION.setEnabled(isFunctionSelected);
		Command.CLEAR_FUNCTIONS.setEnabled(hasFunctions());
		Command.REVERSE_FUNCTIONS.setEnabled(getNumFunctions() > 1);
		Command.TOGGLE_SHOW_GRID.setEnabled(isView);
		Command.TOGGLE_SHOW_GRID.setSelected(isView && getView().getPlotPanel().isGridVisible());
	}

	//------------------------------------------------------------------

	public void executeCommand(Command command)
	{
		// Set command execution flag
		executingCommand = true;

		// Perform command
		Edit edit = null;
		try
		{
			try
			{
				switch (command)
				{
					case UNDO:
						edit = onUndo();
						break;

					case REDO:
						edit = onRedo();
						break;

					case CLEAR_EDIT_LIST:
						edit = onClearEditList();
						break;

					case EDIT_COMMENT:
						edit = onEditComment();
						break;

					case ADD_FUNCTION:
						edit = onAddFunction();
						break;

					case EDIT_FUNCTION:
						edit = onEditFunction();
						break;

					case COPY_FUNCTION:
						edit = onCopyFunction();
						break;

					case VIEW_FUNCTION:
						edit = onViewFunction();
						break;

					case TOGGLE_HIGHLIGHT_FUNCTION:
						edit = onToggleHighlightFunction();
						break;

					case TOGGLE_HIDE_FUNCTION:
						edit = onToggleHideFunction();
						break;

					case DELETE_FUNCTION:
						edit = onDeleteFunction();
						break;

					case CONFIRM_DELETE_FUNCTION:
						edit = onConfirmDeleteFunction();
						break;

					case CLEAR_FUNCTIONS:
						edit = onClearFunctions();
						break;

					case MOVE_FUNCTION:
						edit = onMoveFunction();
						break;

					case REVERSE_FUNCTIONS:
						edit = onReverseFunctions();
						break;

					case SCROLL_LEFT:
						edit = onScrollLeft();
						break;

					case SCROLL_RIGHT:
						edit = onScrollRight();
						break;

					case SCROLL_DOWN:
						edit = onScrollDown();
						break;

					case SCROLL_UP:
						edit = onScrollUp();
						break;

					case CENTRE_ON_ORIGIN:
						edit = onCentreOnOrigin();
						break;

					case X_ZOOM_OUT:
						edit = onXZoomOut();
						break;

					case X_ZOOM_IN:
						edit = onXZoomIn();
						break;

					case Y_ZOOM_OUT:
						edit = onYZoomOut();
						break;

					case Y_ZOOM_IN:
						edit = onYZoomIn();
						break;

					case SET_X_INTERVAL:
						edit = onSetXInterval();
						break;

					case SET_Y_INTERVAL:
						edit = onSetYInterval();
						break;

					case SET_INTERVALS:
						edit = onSetIntervals();
						break;

					case TOGGLE_SHOW_GRID:
						edit = onToggleShowGrid();
						break;
				}
			}
			catch (OutOfMemoryError e)
			{
				throw new AppException(ErrorId.NOT_ENOUGH_MEMORY_TO_PERFORM_COMMAND);
			}
		}
		catch (AppException e)
		{
			App.INSTANCE.showErrorMessage(App.SHORT_NAME, e);
		}

		// Add edit to undo list
		if (edit != null)
			editList.addEdit(edit);

		// Update function buttons
		updateView(ViewComponent.BUTTONS);

		// Update tab text and title and menus in main window
		MainWindow mainWindow = getWindow();
		if (mainWindow != null)
		{
			App.INSTANCE.updateTabText(this);
			mainWindow.updateTitleAndMenus();
		}

		// Clear command execution flag
		executingCommand = false;
	}

	//------------------------------------------------------------------

	private FunctionView getView()
	{
		return App.INSTANCE.getView(this);
	}

	//------------------------------------------------------------------

	private FunctionView.FunctionList getFunctionList()
	{
		return getView().getFunctionList();
	}

	//------------------------------------------------------------------

	private int getSelectedIndex()
	{
		return ((getView() == null) ? -1 : getFunctionList().getSelectedIndex());
	}

	//------------------------------------------------------------------

	private void setXInterval(PlotInterval interval)
	{
		xInterval = new PlotInterval(interval);
		updateView(ViewComponent.X_INTERVAL);
	}

	//------------------------------------------------------------------

	private void setYInterval(PlotInterval interval)
	{
		yInterval = new PlotInterval(interval);
		updateView(ViewComponent.Y_INTERVAL);
	}

	//------------------------------------------------------------------

	private Edit setXIntervalEdit(PlotInterval interval)
	{
		Edit edit = null;
		if (interval.isValid())
		{
			edit = new Edit.XInterval(xInterval, interval);
			setXInterval(interval);
		}
		else
			Toolkit.getDefaultToolkit().beep();
		return edit;
	}

	//------------------------------------------------------------------

	private Edit setYIntervalEdit(PlotInterval interval)
	{
		Edit edit = null;
		if (interval.isValid())
		{
			edit = new Edit.YInterval(yInterval, interval);
			setYInterval(interval);
		}
		else
			Toolkit.getDefaultToolkit().beep();
		return edit;
	}

	//------------------------------------------------------------------

	private Edit setIntervals(PlotInterval xInterval,
							  PlotInterval yInterval,
							  boolean      end)
	{
		Edit.Compound edit = null;

		if (xStartInterval == null)
			xStartInterval = new PlotInterval(this.xInterval);
		if (yStartInterval == null)
			yStartInterval = new PlotInterval(this.yInterval);

		if (xInterval.isValid() && yInterval.isValid())
		{
			if (end && (!xStartInterval.equals(xInterval) || !yStartInterval.equals(yInterval)))
			{
				edit = new Edit.Compound();
				edit.addEdit(new Edit.XInterval(xStartInterval, xInterval));
				edit.addEdit(new Edit.YInterval(yStartInterval, yInterval));
			}
			setXInterval(xInterval);
			setYInterval(yInterval);
		}
		else
			Toolkit.getDefaultToolkit().beep();

		if (end)
		{
			xStartInterval = null;
			yStartInterval = null;
		}

		return edit;
	}

	//------------------------------------------------------------------

	private boolean setHighlight(int     index,
								 boolean highlighted)
	{
		boolean changed = false;
		for (int i = 0; i < functions.size(); i++)
		{
			Function function = functions.get(i);
			Function.Highlight highlight = highlighted ? (i == index) ? Function.Highlight.HIGHLIGHTED
																	  : Function.Highlight.OBSCURED
													   : Function.Highlight.NONE;
			if (highlight != function.getHighlight())
			{
				function.setHighlight(highlight);
				changed = true;
			}
		}
		return changed;
	}

	//------------------------------------------------------------------

	private boolean isXml(CharSequence text)
	{
		return text.subSequence(0, Math.min(text.length(), XML_PREFIX.length())).toString().
																					equals(XML_PREFIX);
	}

	//------------------------------------------------------------------

	private boolean hasCommentNodes(Node node)
	{
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++)
		{
			node = childNodes.item(i);
			if ((node.getNodeType() == Node.COMMENT_NODE) || hasCommentNodes(node))
				return true;
		}
		return false;
	}

	//------------------------------------------------------------------

	private void parseXml(StringBuilder text)
		throws AppException
	{
		// Convert file to DOM document
		Document document = XmlUtils.createDocument(text.toString());
		if (!XmlUtils.getErrorHandler().isEmpty())
			throw new XmlValidationException(ErrorId.INVALID_DOCUMENT, file,
											 XmlUtils.getErrorHandler().getErrorStrings());

		// Test root element
		Element element = document.getDocumentElement();
		if (!element.getNodeName().equals(ElementName.FUNCTION_LIST))
			throw new FileException(ErrorId.UNEXPECTED_DOCUMENT_FORMAT, file);
		String elementPath = ElementName.FUNCTION_LIST;

		// Attribute: namespace
		String attrName = AttrName.XMLNS;
		String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		String attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, file, attrKey);
		if (!attrValue.matches(NAMESPACE_NAME_REGEX))
			throw new FileException(ErrorId.UNEXPECTED_DOCUMENT_FORMAT, file);

		// Attribute: version
		attrName = AttrName.VERSION;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, file, attrKey);
		try
		{
			int version = Integer.parseInt(attrValue);
			if ((version < MIN_SUPPORTED_VERSION) || (version > MAX_SUPPORTED_VERSION))
				throw new FileException(ErrorId.UNSUPPORTED_DOCUMENT_VERSION, file, attrValue);
		}
		catch (NumberFormatException e)
		{
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, file, attrKey, attrValue);
		}

		// Attribute: x interval
		attrName = AttrName.X_INTERVAL;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue != null)
		{
			try
			{
				xInterval = parseInterval(attrValue);
			}
			catch (AppException e)
			{
				throw new XmlParseException(e.getId(), file, attrKey, attrValue);
			}
		}

		// Attribute: y interval
		attrName = AttrName.Y_INTERVAL;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue != null)
		{
			try
			{
				yInterval = parseInterval(attrValue);
			}
			catch (AppException e)
			{
				throw new XmlParseException(e.getId(), file, attrKey, attrValue);
			}
		}

		// Parse function elements
		int colourIndex = 0;
		try
		{
			boolean hasComment = false;
			NodeList childNodes = element.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++)
			{
				Node node = childNodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					String elementName = node.getNodeName();

					if (elementName.equals(Comment.getElementName()))
					{
						if (hasComment)
							throw new FileException(ErrorId.MULTIPLE_COMMENT_ELEMENTS, file);
						comment = new Comment((Element)node);
						hasComment = true;
					}

					else if (elementName.equals(ElementName.FUNCTION))
					{
						if (isFull())
							throw new FileException(ErrorId.TOO_MANY_FUNCTIONS, file);
						Color defaultColour = AppConfig.INSTANCE.getFunctionColour(colourIndex++);
						functions.add(parseFunction((Element)node, defaultColour));
					}
				}
			}
		}
		catch (XmlParseException e)
		{
			throw new XmlParseException(e, file);
		}

		// Test for comments
		commented = hasCommentNodes(document);
	}

	//------------------------------------------------------------------

	private Function parseFunction(Element element,
								   Color   defaultColour)
		throws XmlParseException
	{
		String elementPath = XmlUtils.getElementPath(element);

		// Attribute: expression
		String attrName = AttrName.EXPRESSION;
		String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		String attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		Expression expression = null;
		try
		{
			expression = new Expression(attrValue);
		}
		catch (Expression.Exception e)
		{
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
		}

		// Attribute: colour
		attrName = AttrName.COLOUR;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		Color colour = defaultColour;
		if (attrValue != null)
		{
			try
			{
				colour = ColourUtils.parseColour(attrValue);
			}
			catch (IllegalArgumentException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}
			catch (ValueOutOfBoundsException e)
			{
				throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
			}
		}

		return new Function(colour, expression);
	}

	//------------------------------------------------------------------

	private void parseText(StringBuilder text,
						   List<String>  errorStrs)
		throws AppException
	{
		int commentIndent = 0;
		StringBuilder commentBuffer = new StringBuilder();
		int colourIndex = 0;
		int lineNum = 0;
		int fileIndex = 0;
		String line = null;
		TextState state = TextState.COMMENT_FIRST_LINE;
		while (state != TextState.DONE)
		{
			// Get next line from file
			while (line == null)
			{
				if (fileIndex < text.length())
				{
					int startIndex = fileIndex;
					fileIndex = text.indexOf("\n", fileIndex);
					if (fileIndex < 0)
						fileIndex = text.length();
					line = text.substring(startIndex, fileIndex);
					++fileIndex;

					++lineNum;

					if (line.isEmpty())
						line = null;
				}
				else
				{
					state = TextState.DONE;
					break;
				}
			}

			// Process line
			switch (state)
			{
				case COMMENT_FIRST_LINE:
					if (line.charAt(0) == COMMENT_PREFIX_CHAR)
					{
						for (int i = 1; i < line.length(); i++)
						{
							if (line.charAt(i) != ' ')
								break;
							++commentIndent;
						}
						commentBuffer.append(line.substring(commentIndent + 1));
						line = null;
						state = TextState.COMMENT;
					}
					else
						state = TextState.STATEMENT;
					break;

				case COMMENT:
					if (line.charAt(0) == COMMENT_PREFIX_CHAR)
					{
						int endIndex = Math.min(commentIndent + 1, line.length());
						int index = 1;
						while (index < endIndex)
						{
							if (line.charAt(index) != ' ')
								break;
							++index;
						}
						commentBuffer.append('\n');
						commentBuffer.append(line.substring(index));
						line = null;
					}
					else
						state = TextState.STATEMENT;
					break;

				case STATEMENT:
				{
					// Strip any comment
					int index = line.indexOf(COMMENT_PREFIX_CHAR);
					if (index >= 0)
					{
						commented = true;
						line = line.substring(0, index);
					}

					// Strip leading and trailing spaces and control chars
					line = line.trim();

					// Set next state according to kind of statement
					if (line.isEmpty())
						line = null;
					else
						state = (line.charAt(0) == INTERVAL_PREFIX_CHAR) ? TextState.INTERVAL
																		 : TextState.FUNCTION;
					break;
				}

				case INTERVAL:
					try
					{
						if (line.length() < 2)
							throw new AppException(ErrorId.INVALID_INTERVAL_IDENTIFIER);
						switch (Character.toLowerCase(line.charAt(1)))
						{
							case 'x':
								xInterval = parseInterval(line.substring(2));
								break;

							case 'y':
								yInterval = parseInterval(line.substring(2));
								break;

							default:
								throw new AppException(ErrorId.INVALID_INTERVAL_IDENTIFIER);
						}
					}
					catch (AppException e)
					{
						errorStrs.add(LINE_STR + lineNum + ": " + e);
					}
					line = null;
					state = TextState.STATEMENT;
					break;

				case FUNCTION:
					try
					{
						// Test whether function list is full
						if (isFull())
							throw new AppException(ErrorId.TOO_MANY_FUNCTIONS);

						// Parse function
						Color defaultColour = AppConfig.INSTANCE.getFunctionColour(colourIndex);
						Function function = parseFunction(line, defaultColour);
						++colourIndex;

						// Add function to list
						functions.add(function);
					}
					catch (Expression.Exception e)
					{
						errorStrs.add(LINE_STR + lineNum + " [" + (e.getOffset() + 1) + "]: " + e);
					}
					catch (AppException e)
					{
						errorStrs.add(LINE_STR + lineNum + ": " + e);
					}
					line = null;
					state = TextState.STATEMENT;
					break;

				case DONE:
					comment.setText(commentBuffer.toString());
					break;
			}
		}
	}

	//------------------------------------------------------------------

	private PlotInterval parseInterval(String str)
		throws AppException
	{
		// Split string into lower and upper endpoints
		List<String> strs = StringUtils.split(str, ',');
		if (strs.size() != 2)
			throw new AppException(ErrorId.INVALID_INTERVAL);

		// Parse lower endpoint
		BigDecimal lowerEndpoint = null;
		try
		{
			String epStr = strs.get(0).trim();
			lowerEndpoint = new BigDecimal(epStr);
			double value = lowerEndpoint.doubleValue();
			if ((value < PlotInterval.MIN_VALUE) || (value > PlotInterval.MAX_VALUE))
				throw new AppException(ErrorId.INTERVAL_LOWER_ENDPOINT_OUT_OF_BOUNDS);
			if (PlotInterval.hasTooManySignificantDigits(epStr))
				throw new AppException(ErrorId.INTERVAL_LOWER_ENDPOINT_HAS_TOO_MANY_SIGNIFICANT_DIGITS);
		}
		catch (NumberFormatException e)
		{
			throw new AppException(ErrorId.INVALID_INTERVAL_LOWER_ENDPOINT);
		}

		// Parse upper endpoint
		BigDecimal upperEndpoint = null;
		try
		{
			String epStr = strs.get(1).trim();
			upperEndpoint = new BigDecimal(epStr);
			double value = upperEndpoint.doubleValue();
			if ((value < PlotInterval.MIN_VALUE) || (value > PlotInterval.MAX_VALUE))
				throw new AppException(ErrorId.INTERVAL_UPPER_ENDPOINT_OUT_OF_BOUNDS);
			if (PlotInterval.hasTooManySignificantDigits(epStr))
				throw new AppException(ErrorId.INTERVAL_UPPER_ENDPOINT_HAS_TOO_MANY_SIGNIFICANT_DIGITS);
		}
		catch (NumberFormatException e)
		{
			throw new AppException(ErrorId.INVALID_INTERVAL_UPPER_ENDPOINT);
		}

		// Check that lower endpoint is less than upper endpoint
		PlotInterval interval = new PlotInterval(lowerEndpoint, upperEndpoint);
		if (interval.getLowerEndpoint() >= interval.getUpperEndpoint())
			throw new AppException(ErrorId.INTERVAL_ENDPOINTS_OUT_OF_ORDER);
		return interval;
	}

	//------------------------------------------------------------------

	private Function parseFunction(String str,
								   Color  colour)
		throws AppException
	{
		// Split function specifier into expression and colour
		String[] functionParts = StringUtils.splitAtFirst(str, Function.SEPARATOR_CHAR);

		// Parse expression
		Expression expression = new Expression(functionParts[0]);

		// Parse colour
		if (functionParts[1] != null)
		{
			try
			{
				colour = ColourUtils.parseColour(functionParts[1].trim());
			}
			catch (IllegalArgumentException e)
			{
				throw new AppException(ErrorId.INVALID_COLOUR);
			}
			catch (ValueOutOfBoundsException e)
			{
				throw new AppException(ErrorId.COLOUR_COMPONENT_OUT_OF_BOUNDS);
			}
		}

		// Return function
		return new Function(colour, expression);
	}

	//------------------------------------------------------------------

	private String getXml(boolean includeColours)
		throws AppException
	{
		// Create writer
		CharArrayWriter charArrayWriter = new CharArrayWriter();
		XmlWriter writer = new XmlWriter(charArrayWriter);
		writer.setLineSeparator(null);

		// Write file to character array
		try
		{
			// Write XML declaration
			writer.writeXmlDeclaration(XML_VERSION_STR, XmlConstants.ENCODING_NAME_UTF8,
									   XmlWriter.Standalone.NO);

			// Write root element start tag
			AttributeList attributes = new AttributeList();
			attributes.add(AttrName.XMLNS, NAMESPACE_NAME);
			attributes.add(AttrName.VERSION, VERSION);
			attributes.add(AttrName.X_INTERVAL, xInterval, true);
			attributes.add(AttrName.Y_INTERVAL, yInterval, true);
			writer.writeElementStart(ElementName.FUNCTION_LIST, attributes, 0, true, true);

			// Write comment element
			if (!comment.isEmpty())
				comment.write(writer, 2, 4);

			// Write function elements
			for (Function function : functions)
			{
				attributes.clear();
				attributes.add(AttrName.EXPRESSION, function.getExpression(), true);
				if (includeColours)
					attributes.add(AttrName.COLOUR, ColourUtils.colourToRgbString(function.getColour()));
				writer.writeEmptyElement(ElementName.FUNCTION, attributes, 2, true);
			}

			// Write root element end tag
			writer.writeElementEnd(ElementName.FUNCTION_LIST, 0);
		}
		catch (IOException e)
		{
			throw new FileException(ErrorId.ERROR_WRITING_FILE, file, e);
		}

		// Return text as string
		return charArrayWriter.toString();
	}

	//------------------------------------------------------------------

	private StringBuilder getText(boolean includeColours)
	{
		String lineSeparator = SystemUtils.getLineSeparator();
		StringBuilder buffer = new StringBuilder(1024);

		// Document comment
		if (!comment.isEmpty())
		{
			String text = comment.getText();
			int index = 0;
			while (index < text.length())
			{
				int startIndex = index;
				index = text.indexOf('\n', startIndex);
				if (index < 0)
					index = text.length();
				buffer.append(COMMENT_PREFIX_CHAR);
				if (startIndex < index)
				{
					buffer.append(' ');
					buffer.append(text.substring(startIndex, index));
				}
				buffer.append(lineSeparator);
				++index;
			}
		}

		// Intervals
		buffer.append(INTERVAL_PREFIX_CHAR);
		buffer.append("x ");
		buffer.append(xInterval);
		buffer.append(lineSeparator);

		buffer.append(INTERVAL_PREFIX_CHAR);
		buffer.append("y ");
		buffer.append(yInterval);
		buffer.append(lineSeparator);

		// Functions
		for (Function function : functions)
		{
			buffer.append(function.toString());
			if (includeColours)
			{
				buffer.append(Function.SEPARATOR_CHAR);
				buffer.append(' ');
				buffer.append(ColourUtils.colourToRgbString(function.getColour()));
			}
			buffer.append(lineSeparator);
		}

		return buffer;
	}

	//------------------------------------------------------------------

	private BufferedImage getImage()
		throws AppException
	{
		PlotPanel plotPanel = getView().getPlotPanel();
		Dimension size = plotPanel.getPreferredSize();
		BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		plotPanel.drawPanel(image.createGraphics());
		return image;
	}

	//------------------------------------------------------------------

	private void updateView(ViewComponent... components)
	{
		FunctionView view = getView();
		if (view != null)
		{
			for (ViewComponent component : components)
			{
				switch (component)
				{
					case PLOT:
						view.updatePlot();
						break;

					case BUTTONS:
						view.updateButtons();
						break;

					case X_INTERVAL:
						view.updateXInterval();
						break;

					case Y_INTERVAL:
						view.updateYInterval();
						break;

					case INTERVALS:
						view.updateIntervals();
						break;

					case FUNCTION_LIST:
						view.updateFunctionList();
						break;
				}
			}
		}
	}

	//------------------------------------------------------------------

	private Edit onUndo()
	{
		Edit edit = editList.removeUndo();
		if (edit != null)
		{
			int selectedIndex = getSelectedIndex();
			edit.undo(this);
			if (selectedIndex < getNumFunctions())
				getFunctionList().setSelectedIndex(selectedIndex);
			updateView(ViewComponent.PLOT, ViewComponent.INTERVALS);
		}
		return null;
	}

	//------------------------------------------------------------------

	private Edit onRedo()
	{
		Edit edit = editList.removeRedo();
		if (edit != null)
		{
			int selectedIndex = getSelectedIndex();
			edit.redo(this);
			if (selectedIndex < getNumFunctions())
				getFunctionList().setSelectedIndex(selectedIndex);
			updateView(ViewComponent.PLOT, ViewComponent.INTERVALS);
		}
		return null;
	}

	//------------------------------------------------------------------

	private Edit onClearEditList()
	{
		String[] optionStrs = Utils.getOptionStrings(AppConstants.CLEAR_STR);
		if (JOptionPane.showOptionDialog(getWindow(), CLEAR_EDIT_LIST_STR, App.SHORT_NAME,
										 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
										 optionStrs, optionStrs[1]) == JOptionPane.OK_OPTION)
		{
			editList.clear();
			System.gc();
		}
		return null;
	}

	//------------------------------------------------------------------

	private Edit onEditComment()
	{
		Edit.Comment edit = null;
		String text = CommentDialog.showDialog(getWindow(), COMMENT_STR, comment.getText());
		if ((text != null) && !text.equals(comment.getText()))
		{
			edit = new Edit.Comment(comment.getText(), text);
			comment.setText(text);
		}
		return edit;
	}

	//------------------------------------------------------------------

	private Edit onAddFunction()
	{
		Edit edit = null;
		if (!isFull())
		{
			Color colour = AppConfig.INSTANCE.getFunctionColour(getNumFunctions());
			FunctionDialog dialog = FunctionDialog.showDialog(getWindow(), ADD_FUNCTION_STR, colour,
															  null);
			if (dialog.isAccepted())
			{
				Function function = new Function(dialog.getColour(), dialog.getExpression());
				edit = new Edit.Add(getNumFunctions(), function);

				getFunctionList().addElement(function);
			}
		}
		return edit;
	}

	//------------------------------------------------------------------

	private Edit onEditFunction()
	{
		Edit.Compound edit = null;
		int index = getSelectedIndex();
		if (hasFunctions() && (index >= 0))
		{
			Function function = getFunction(index);
			FunctionDialog dialog = FunctionDialog.showDialog(getWindow(), EDIT_FUNCTION_STR,
															  function.getColour(),
															  function.getExpression().toString());
			if (dialog.isAccepted())
			{
				Function newFunction = new Function(dialog.getColour(), dialog.getExpression(),
													function.isHidden());
				edit = new Edit.Compound();
				edit.addEdit(new Edit.Remove(index, function));
				edit.addEdit(new Edit.Add(index, newFunction));

				getFunctionList().setElement(index, newFunction);
			}
		}
		return edit;
	}

	//------------------------------------------------------------------

	private Edit onCopyFunction()
		throws AppException
	{
		int index = getSelectedIndex();
		if (hasFunctions() && (index >= 0))
			Utils.putClipboardText(getFunction(index).getExpression().toString());
		return null;
	}

	//------------------------------------------------------------------

	private Edit onViewFunction()
	{
		getView().showViewFunctionMenu();
		return null;
	}

	//------------------------------------------------------------------

	private Edit onToggleHighlightFunction()
	{
		int index = getSelectedIndex();
		if (hasFunctions() && (index >= 0))
		{
			Function function = getFunction(index);
			setHighlight(index, !function.isHighlighted());

			getFunctionList().setElement(index, function);
			updateView(ViewComponent.PLOT, ViewComponent.FUNCTION_LIST);
		}
		return null;
	}

	//------------------------------------------------------------------

	private Edit onToggleHideFunction()
	{
		int index = getSelectedIndex();
		if (hasFunctions() && (index >= 0))
		{
			Function function = getFunction(index);
			function.setHidden(!function.isHidden());

			getFunctionList().setElement(index, function);
		}
		return null;
	}

	//------------------------------------------------------------------

	private Edit onDeleteFunction()
	{
		Edit edit = null;
		int index = getSelectedIndex();
		if (hasFunctions() && (index >= 0))
		{
			Function function = getFunction(index);
			if (function.isHighlighted())
				setHighlight(index, false);
			else if (function.isObscured())
				function.setHighlight(Function.Highlight.NONE);
			updateView(ViewComponent.FUNCTION_LIST);

			edit = new Edit.Remove(index, function);

			getFunctionList().removeElement(index);
		}
		return edit;
	}

	//------------------------------------------------------------------

	private Edit onConfirmDeleteFunction()
	{
		Edit edit = null;
		String[] optionStrs = Utils.getOptionStrings(DELETE_STR);
		if (JOptionPane.showOptionDialog(getWindow(), DELETE_MESSAGE_STR,
										 App.SHORT_NAME + " : " + DELETE_FUNCTION_STR,
										 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
										 optionStrs, optionStrs[0]) == JOptionPane.OK_OPTION)
			edit = onDeleteFunction();
		return edit;
	}

	//------------------------------------------------------------------

	private Edit onClearFunctions()
	{
		Edit.Compound edit = null;
		String[] optionStrs = Utils.getOptionStrings(DELETE_STR);
		if (hasFunctions() &&
			 (JOptionPane.showOptionDialog(getWindow(), DELETE_ALL_MESSAGE_STR,
										   App.SHORT_NAME + " : " + DELETE_ALL_FUNCTIONS_STR,
										   JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
										   null, optionStrs, optionStrs[1]) == JOptionPane.OK_OPTION))
		{
			edit = new Edit.Compound();
			for (int i = getNumFunctions() - 1; i >= 0; i--)
				edit.addEdit(new Edit.Remove(i, getFunction(i)));

			getFunctionList().setElements((Function[])null);
		}
		return edit;
	}

	//------------------------------------------------------------------

	private Edit onMoveFunction()
	{
		Edit.Compound edit = null;
		int fromIndex = getSelectedIndex();
		int toIndex = (Integer)Command.MOVE_FUNCTION.getValue(Command.Property.INDEX);
		if (toIndex != fromIndex)
		{
			Function function = getFunction(fromIndex);
			edit = new Edit.Compound();
			edit.addEdit(new Edit.Remove(fromIndex, function));
			edit.addEdit(new Edit.Add(toIndex, function));

			getFunctionList().moveElement(fromIndex, toIndex);
		}
		return edit;
	}

	//------------------------------------------------------------------

	private Edit onReverseFunctions()
	{
		Edit.Compound edit = null;
		if (getNumFunctions() > 1)
		{
			int selectedIndex = getSelectedIndex();
			edit = new Edit.Compound();
			List<Function> reversedFunctions = new ArrayList<>(functions);
			Collections.reverse(reversedFunctions);
			for (int i = getNumFunctions() - 1; i >= 0; i--)
				edit.addEdit(new Edit.Remove(i, getFunction(i)));
			for (int i = 0; i < getNumFunctions(); i++)
				edit.addEdit(new Edit.Add(i, reversedFunctions.get(i)));

			getFunctionList().setElements(reversedFunctions);
			getFunctionList().setSelectedIndex(selectedIndex);
		}
		return edit;
	}

	//------------------------------------------------------------------

	private Edit onScrollLeft()
	{
		double increment = getView().getPlotPanel().getXScrollIncrement();
		return setXIntervalEdit(new PlotInterval(xInterval.getLowerEndpoint() - increment,
												 xInterval.getUpperEndpoint() - increment));
	}

	//------------------------------------------------------------------

	private Edit onScrollRight()
	{
		double increment = getView().getPlotPanel().getXScrollIncrement();
		return setXIntervalEdit(new PlotInterval(xInterval.getLowerEndpoint() + increment,
												 xInterval.getUpperEndpoint() + increment));
	}

	//------------------------------------------------------------------

	private Edit onScrollDown()
	{
		double increment = getView().getPlotPanel().getYScrollIncrement();
		return setYIntervalEdit(new PlotInterval(yInterval.getLowerEndpoint() - increment,
												 yInterval.getUpperEndpoint() - increment));
	}

	//------------------------------------------------------------------

	private Edit onScrollUp()
	{
		double increment = getView().getPlotPanel().getYScrollIncrement();
		return setYIntervalEdit(new PlotInterval(yInterval.getLowerEndpoint() + increment,
												 yInterval.getUpperEndpoint() + increment));
	}

	//------------------------------------------------------------------

	private Edit onCentreOnOrigin()
	{
		Edit.Compound edit = null;
		double halfWidth = xInterval.getHalfInterval();
		PlotInterval newXInterval = new PlotInterval(-halfWidth, halfWidth);

		double halfHeight = yInterval.getHalfInterval();
		PlotInterval newYInterval = new PlotInterval(-halfHeight, halfHeight);

		if (newXInterval.isValid() && newYInterval.isValid())
		{
			edit = new Edit.Compound();
			edit.addEdit(new Edit.XInterval(xInterval, newXInterval));
			edit.addEdit(new Edit.YInterval(yInterval, newYInterval));

			setIntervals(newXInterval, newYInterval);
		}
		else
			Toolkit.getDefaultToolkit().beep();
		return edit;
	}

	//------------------------------------------------------------------

	private Edit onXZoomOut()
	{
		double median = xInterval.getMedian();
		double halfInterval = xInterval.getHalfInterval() * FunctionView.getXZoomFactor();
		return setXIntervalEdit(new PlotInterval(median - halfInterval, median + halfInterval));
	}

	//------------------------------------------------------------------

	private Edit onXZoomIn()
	{
		double median = xInterval.getMedian();
		double halfInterval = xInterval.getHalfInterval() / FunctionView.getXZoomFactor();
		return setXIntervalEdit(new PlotInterval(median - halfInterval, median + halfInterval));
	}

	//------------------------------------------------------------------

	private Edit onYZoomOut()
	{
		double median = yInterval.getMedian();
		double halfInterval = yInterval.getHalfInterval() * FunctionView.getYZoomFactor();
		return setYIntervalEdit(new PlotInterval(median - halfInterval, median + halfInterval));
	}

	//------------------------------------------------------------------

	private Edit onYZoomIn()
	{
		double median = yInterval.getMedian();
		double halfInterval = yInterval.getHalfInterval() / FunctionView.getYZoomFactor();
		return setYIntervalEdit(new PlotInterval(median - halfInterval, median + halfInterval));
	}

	//------------------------------------------------------------------

	private Edit onSetXInterval()
	{
		return setXIntervalEdit((PlotInterval)Command.SET_X_INTERVAL.
																getValue(Command.Property.X_INTERVAL));
	}

	//------------------------------------------------------------------

	private Edit onSetYInterval()
	{
		return setYIntervalEdit((PlotInterval)Command.SET_Y_INTERVAL.
																getValue(Command.Property.Y_INTERVAL));
	}

	//------------------------------------------------------------------

	private Edit onSetIntervals()
	{
		Command command = Command.SET_INTERVALS;
		PlotInterval xInterval = (PlotInterval)command.getValue(Command.Property.X_INTERVAL);
		PlotInterval yInterval = (PlotInterval)command.getValue(Command.Property.Y_INTERVAL);
		boolean end = (Boolean)command.getValue(Command.Property.END);
		return setIntervals(xInterval, yInterval, end);
	}

	//------------------------------------------------------------------

	private Edit onToggleShowGrid()
	{
		PlotPanel plotPanel = getView().getPlotPanel();
		plotPanel.setGridVisible(!plotPanel.isGridVisible());
		return null;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	File			file;
	private	FileKind		fileKind;
	private	long			timestamp;
	private	int				unnamedIndex;
	private	boolean			executingCommand;
	private	List<Function>	functions;
	private	PlotInterval	xInterval;
	private	PlotInterval	yInterval;
	private	PlotInterval	xStartInterval;
	private	PlotInterval	yStartInterval;
	private	boolean			commented;
	private	Comment			comment;
	private	EditList		editList;

}

//----------------------------------------------------------------------
