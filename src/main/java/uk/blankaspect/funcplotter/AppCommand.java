/*====================================================================*\

AppCommand.java

Application command enumeration.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.KeyStroke;

import uk.blankaspect.ui.swing.action.Command;

//----------------------------------------------------------------------


// APPLICATION COMMAND ENUMERATION


enum AppCommand
	implements Action
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	// Commands

	CHECK_MODIFIED_FILE
	(
		"checkModifiedFile"
	),

	IMPORT_FILES
	(
		"importFiles"
	),

	CREATE_FILE
	(
		"createFile",
		"New file",
		KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK)
	),

	OPEN_FILE
	(
		"openFile",
		"Open file" + AppConstants.ELLIPSIS_STR,
		KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK)
	),

	REVERT_FILE
	(
		"revertFile",
		"Revert file"
	),

	CLOSE_FILE
	(
		"closeFile",
		"Close file",
		KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK)
	),

	CLOSE_ALL_FILES
	(
		"closeAllFiles",
		"Close all files"
	),

	SAVE_FILE
	(
		"saveFile",
		"Save file",
		KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)
	),

	SAVE_FILE_AS
	(
		"saveFileAs",
		"Save file as" + AppConstants.ELLIPSIS_STR
	),

	EXPORT_IMAGE
	(
		"exportImage",
		"Export image file" + AppConstants.ELLIPSIS_STR,
		KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK)
	),

	EXIT
	(
		"exit",
		"Exit"
	),

	COPY_INTERVALS
	(
		"copyIntervals",
		"Copy intervals to other documents" + AppConstants.ELLIPSIS_STR,
		KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK)
	),

	EDIT_PREFERENCES
	(
		"editPreferences",
		"Preferences"
	),

	TOGGLE_SHOW_FULL_PATHNAMES
	(
		"toggleShowFullPathnames",
		"Show full pathnames"
	);

	//------------------------------------------------------------------

	// Property keys
	interface Property
	{
		String	FILES	= "files";
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private AppCommand(String key)
	{
		command = new Command(this);
		putValue(Action.ACTION_COMMAND_KEY, key);
	}

	//------------------------------------------------------------------

	private AppCommand(String key,
					   String name)
	{
		this(key);
		putValue(Action.NAME, name);
	}

	//------------------------------------------------------------------

	private AppCommand(String    key,
					   String    name,
					   KeyStroke acceleratorKey)
	{
		this(key, name);
		putValue(Action.ACCELERATOR_KEY, acceleratorKey);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Action interface
////////////////////////////////////////////////////////////////////////

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		command.addPropertyChangeListener(listener);
	}

	//------------------------------------------------------------------

	public Object getValue(String key)
	{
		return command.getValue(key);
	}

	//------------------------------------------------------------------

	public boolean isEnabled()
	{
		return command.isEnabled();
	}

	//------------------------------------------------------------------

	public void putValue(String key,
						 Object value)
	{
		command.putValue(key, value);
	}

	//------------------------------------------------------------------

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		command.removePropertyChangeListener(listener);
	}

	//------------------------------------------------------------------

	public void setEnabled(boolean enabled)
	{
		command.setEnabled(enabled);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		FuncPlotterApp.INSTANCE.executeCommand(this);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void setSelected(boolean selected)
	{
		putValue(Action.SELECTED_KEY, selected);
	}

	//------------------------------------------------------------------

	public void execute()
	{
		actionPerformed(null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Command	command;

}

//----------------------------------------------------------------------
