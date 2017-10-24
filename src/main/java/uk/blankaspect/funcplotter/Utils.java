/*====================================================================*\

Utils.java

Utility methods class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.File;
import java.io.IOException;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.misc.PropertiesPathname;
import uk.blankaspect.common.misc.SystemUtils;

//----------------------------------------------------------------------


// UTILITY METHODS CLASS


class Utils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	USER_HOME_PREFIX			= "~";
	private static final	String	FAILED_TO_GET_PATHNAME_STR	= "Failed to get the canonical pathname " +
																	"for the file or directory.";

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

		CLIPBOARD_IS_UNAVAILABLE
		("The clipboard is currently unavailable."),

		FAILED_TO_GET_CLIPBOARD_DATA
		("Failed to get data from the clipboard."),

		NO_TEXT_ON_CLIPBOARD
		("There is no text on the clipboard.");

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
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private Utils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static int indexOf(Object   target,
							  Object[] values)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(target))
				return i;
		}
		return -1;
	}

	//------------------------------------------------------------------

	public static int changeLineSeparators(char[] buffer,
										   int    offset,
										   int    length)
	{
		int inIndex = offset;
		int outIndex = offset;
		int endIndex = offset + length;
		while (inIndex < endIndex)
		{
			char ch = buffer[inIndex++];
			if (ch == '\r')
			{
				if ((inIndex < endIndex) && (buffer[inIndex] == '\n'))
					++inIndex;
				ch = '\n';
			}
			buffer[outIndex++] = ch;
		}
		return outIndex;
	}

	//------------------------------------------------------------------

	public static void moveFocus(Window window)
	{
		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		if ((window != null) && (window == focusManager.getFocusedWindow()))
		{
			Component initialFocusOwner = focusManager.getFocusOwner();
			if ((initialFocusOwner != null) && !initialFocusOwner.isEnabled())
			{
				Component focusOwner = null;
				while (focusOwner != initialFocusOwner)
				{
					focusManager.focusNextComponent();
					focusOwner = focusManager.getFocusOwner();
					if ((focusOwner != null) && focusOwner.isEnabled())
						break;
				}
			}
		}
	}

	//------------------------------------------------------------------

	public static char getFileSeparatorChar()
	{
		return (AppConfig.INSTANCE.isShowUnixPathnames() ? '/' : File.separatorChar);
	}

	//------------------------------------------------------------------

	public static String getPathname(File file)
	{
		return getPathname(file, AppConfig.INSTANCE.isShowUnixPathnames());
	}

	//------------------------------------------------------------------

	public static String getPathname(File    file,
									 boolean unixStyle)
	{
		String pathname = null;
		if (file != null)
		{
			try
			{
				try
				{
					pathname = file.getCanonicalPath();
				}
				catch (Exception e)
				{
					System.err.println(file.getPath());
					System.err.println(FAILED_TO_GET_PATHNAME_STR);
					System.err.println("(" + e + ")");
					pathname = file.getAbsolutePath();
				}
			}
			catch (SecurityException e)
			{
				System.err.println(e);
				pathname = file.getPath();
			}

			if (unixStyle)
			{
				try
				{
					String userHome = SystemUtils.getUserHomePathname();
					if ((userHome != null) && pathname.startsWith(userHome))
						pathname = USER_HOME_PREFIX + pathname.substring(userHome.length());
				}
				catch (SecurityException e)
				{
					// ignore
				}
				pathname = pathname.replace(File.separatorChar, '/');
			}
		}
		return pathname;
	}

	//------------------------------------------------------------------

	public static String getPropertiesPathname()
	{
		String pathname = PropertiesPathname.getPathname();
		if (pathname != null)
			pathname += App.NAME_KEY;
		return pathname;
	}

	//------------------------------------------------------------------

	public static boolean isSameFile(File file1,
									 File file2)
	{
		try
		{
			if (file1 == null)
				return (file2 == null);
			return ((file2 != null) && file1.getCanonicalPath().equals(file2.getCanonicalPath()));
		}
		catch (IOException e)
		{
			return false;
		}
	}

	//------------------------------------------------------------------

	public static File appendSuffix(File   file,
									String suffix)
	{
		String filename = file.getName();
		if (!filename.isEmpty() && (filename.indexOf('.') < 0))
			file = new File(file.getParentFile(), filename + suffix);
		return file;
	}

	//------------------------------------------------------------------

	public static String[] getOptionStrings(String... optionStrs)
	{
		String[] strs = new String[optionStrs.length + 1];
		System.arraycopy(optionStrs, 0, strs, 0, optionStrs.length);
		strs[optionStrs.length] = AppConstants.CANCEL_STR;
		return strs;
	}

	//------------------------------------------------------------------

	public static String getClipboardText()
		throws AppException
	{
		try
		{
			Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			if (contents == null)
				throw new AppException(ErrorId.NO_TEXT_ON_CLIPBOARD);
			return (String)contents.getTransferData(DataFlavor.stringFlavor);
		}
		catch (IllegalStateException e)
		{
			throw new AppException(ErrorId.CLIPBOARD_IS_UNAVAILABLE, e);
		}
		catch (UnsupportedFlavorException e)
		{
			throw new AppException(ErrorId.NO_TEXT_ON_CLIPBOARD);
		}
		catch (IOException e)
		{
			throw new AppException(ErrorId.FAILED_TO_GET_CLIPBOARD_DATA, e);
		}
	}

	//------------------------------------------------------------------

	public static void putClipboardText(String text)
		throws AppException
	{
		try
		{
			StringSelection selection = new StringSelection(text);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
		}
		catch (IllegalStateException e)
		{
			throw new AppException(ErrorId.CLIPBOARD_IS_UNAVAILABLE, e);
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
