/*====================================================================*\

App.java

Application class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Point;

import java.io.File;
import java.io.IOException;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import uk.blankaspect.common.cls.ClassUtils;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.ExceptionUtils;
import uk.blankaspect.common.exception.TaskCancelledException;

import uk.blankaspect.common.exception2.LocationException;

import uk.blankaspect.common.filesystem.PathnameUtils;

import uk.blankaspect.common.logging.ErrorLogger;

import uk.blankaspect.common.misc.FilenameSuffixFilter;

import uk.blankaspect.common.resource.ResourceProperties;

import uk.blankaspect.common.swing.misc.GuiUtils;

import uk.blankaspect.common.swing.text.TextRendering;

import uk.blankaspect.common.swing.textfield.TextFieldUtils;

//----------------------------------------------------------------------


// APPLICATION CLASS


public class App
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		App		INSTANCE	= new App();

	public static final		String	SHORT_NAME	= "FuncPlotter";
	public static final		String	LONG_NAME	= "Function plotter";
	public static final		String	NAME_KEY	= "funcPlotter";

	public static final		int		MAX_NUM_DOCUMENTS	= 64;

	private static final	int	FILE_CHECK_TIMER_INTERVAL	= 500;

	private static final	String	VERSION_PROPERTY_KEY	= "version";
	private static final	String	BUILD_PROPERTY_KEY		= "build";
	private static final	String	RELEASE_PROPERTY_KEY	= "release";

	private static final	String	VERSION_DATE_TIME_PATTERN	= "uuuuMMdd-HHmmss";

	private static final	String	BUILD_PROPERTIES_FILENAME	= "build.properties";

	private static final	String	CONFIG_ERROR_STR		= "Configuration error";
	private static final	String	LAF_ERROR1_STR			= "Look-and-feel: ";
	private static final	String	LAF_ERROR2_STR			= "\nThe look-and-feel is not installed.";

	private static final	String	OPEN_FILE_STR			= "Open file";
	private static final	String	REVERT_FILE_STR			= "Revert file";
	private static final	String	SAVE_FILE_STR			= "Save file";
	private static final	String	SAVE_FILE_AS_STR		= "Save file as";
	private static final	String	SAVE_CLOSE_FILE_STR		= "Save file before closing";
	private static final	String	EXPORT_IMAGE_STR		= "Export image";
	private static final	String	MODIFIED_FILE_STR		= "Modified file";
	private static final	String	OPEN_FUNCTION_FILE_STR	= "Open function file";
	private static final	String	SAVE_FUNCTION_FILE_STR	= "Save function file";
	private static final	String	EXPORT_IMAGE_FILE_STR	= "Export image file";
	private static final	String	COPY_INTERVALS_STR		= "Copy intervals to other documents";
	private static final	String	DOCUMENTS_STR			= "Documents";
	private static final	String	ERRORS_STR				= "Errors in file";
	private static final	String	WRITE_IMAGE_STR			= "Write image";
	private static final	String	READ_DOCUMENT_STR		= "Read document";
	private static final	String	WRITE_DOCUMENT_STR		= "Write document";
	private static final	String	UNNAMED_FILE_STR		= "The unnamed file";
	private static final	String	REVERT_STR				= "Revert";
	private static final	String	SAVE_STR				= "Save";
	private static final	String	DISCARD_STR				= "Discard";
	private static final	String	MODIFIED_MESSAGE_STR	= "\nThe file has been modified externally.\n" +
																"Do you want to open the modified file?";
	private static final	String	REVERT_MESSAGE_STR		= "\nDo you want discard the changes to the " +
																"current document and reopen the " +
																"original file?";
	private static final	String	CHANGED_MESSAGE1_STR	= "\nThe file";
	private static final	String	CHANGED_MESSAGE2_STR	= " has changed.\nDo you want to save the " +
																"changed file?";
	private static final	String	SAVE_COLOURS_STR		= "Do you want to save the colours of the " +
																"functions?";
	private static final	String	HAS_COMMENTS_STR		= "The document contains comments that will " +
																"be lost if the document is saved.";

	private enum IncludeColours
	{
		NO,
		YES
	}

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// DOCUMENT-VIEW CLASS


	private static class DocumentView
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private DocumentView(FunctionDocument document)
		{
			this.document = document;
			view = new FunctionView(document);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	FunctionDocument	document;
		private	FunctionView		view;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private App()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		INSTANCE.init(args);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public MainWindow getMainWindow()
	{
		return mainWindow;
	}

	//------------------------------------------------------------------

	public int getNumDocuments()
	{
		return documentsViews.size();
	}

	//------------------------------------------------------------------

	public boolean hasDocuments()
	{
		return !documentsViews.isEmpty();
	}

	//------------------------------------------------------------------

	public boolean isDocumentsFull()
	{
		return (documentsViews.size() >= MAX_NUM_DOCUMENTS);
	}

	//------------------------------------------------------------------

	public FunctionDocument getDocument()
	{
		return hasDocuments() ? getDocument(mainWindow.getTabIndex()) : null;
	}

	//------------------------------------------------------------------

	public FunctionDocument getDocument(int index)
	{
		return hasDocuments() ? documentsViews.get(index).document : null;
	}

	//------------------------------------------------------------------

	public FunctionView getView()
	{
		return hasDocuments() ? getView(mainWindow.getTabIndex()) : null;
	}

	//------------------------------------------------------------------

	public FunctionView getView(int index)
	{
		return hasDocuments() ? documentsViews.get(index).view : null;
	}

	//------------------------------------------------------------------

	public FunctionView getView(FunctionDocument document)
	{
		for (DocumentView documentView : documentsViews)
		{
			if (documentView.document == document)
				return documentView.view;
		}
		return null;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a string representation of the version of this application.  If this class was loaded from a JAR, the
	 * string is created from the values of properties that are defined in a resource named 'build.properties';
	 * otherwise, the string is created from the date and time when this method is first called.
	 *
	 * @return a string representation of the version of this application.
	 */

	public String getVersionString()
	{
		if (versionStr == null)
		{
			StringBuilder buffer = new StringBuilder(32);
			if (ClassUtils.isFromJar(getClass()))
			{
				// Append version number
				String str = buildProperties.get(VERSION_PROPERTY_KEY);
				if (str != null)
					buffer.append(str);

				// If this is not a release, append build
				boolean release = Boolean.parseBoolean(buildProperties.get(RELEASE_PROPERTY_KEY));
				if (!release)
				{
					str = buildProperties.get(BUILD_PROPERTY_KEY);
					if (str != null)
					{
						if (buffer.length() > 0)
							buffer.append(' ');
						buffer.append(str);
					}
				}
			}
			else
			{
				buffer.append('b');
				buffer.append(DateTimeFormatter.ofPattern(VERSION_DATE_TIME_PATTERN).format(LocalDateTime.now()));
			}
			versionStr = buffer.toString();
		}
		return versionStr;
	}

	//------------------------------------------------------------------

	public String getTitleString()
	{
		return (LONG_NAME + " " + getVersionString());
	}

	//------------------------------------------------------------------

	public void showWarningMessage(String titleStr,
								   Object message)
	{
		showMessageDialog(titleStr, message, JOptionPane.WARNING_MESSAGE);
	}

	//------------------------------------------------------------------

	public void showErrorMessage(String titleStr,
								 Object message)
	{
		showMessageDialog(titleStr, message, JOptionPane.ERROR_MESSAGE);
	}

	//------------------------------------------------------------------

	public void showMessageDialog(String titleStr,
								  Object message,
								  int    messageKind)
	{
		JOptionPane.showMessageDialog(mainWindow, message, titleStr, messageKind);
	}

	//------------------------------------------------------------------

	public void init(String[] args)
	{
		// Log stack trace of uncaught exception
		if (ClassUtils.isFromJar(getClass()))
		{
			Thread.setDefaultUncaughtExceptionHandler((thread, exception) ->
			{
				try
				{
					ErrorLogger.INSTANCE.write(exception);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			});
		}

		// Initialise instance variables
		documentsViews = new ArrayList<>();

		// Read build properties
		try
		{
			buildProperties = new ResourceProperties(BUILD_PROPERTIES_FILENAME);
		}
		catch (LocationException e)
		{
			e.printStackTrace();
		}

		// Get clipboard access permission
		AppConfig config = AppConfig.INSTANCE;
		config.getPermissions();

		// Read configuration
		config.read();

		// Set UNIX style for pathnames in file exceptions
		ExceptionUtils.setUnixStyle(config.isShowUnixPathnames());

		// Set text antialiasing
		TextRendering.setAntialiasing(config.getTextAntialiasing());

		// Set look-and-feel
		String lookAndFeelName = config.getLookAndFeel();
		for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels())
		{
			if (lookAndFeelInfo.getName().equals(lookAndFeelName))
			{
				try
				{
					UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
				}
				catch (Exception e)
				{
					// ignore
				}
				lookAndFeelName = null;
				break;
			}
		}
		if (lookAndFeelName != null)
			showWarningMessage(SHORT_NAME + " : " + CONFIG_ERROR_STR,
							   LAF_ERROR1_STR + lookAndFeelName + LAF_ERROR2_STR);

		// Select all text when a text field gains focus
		if (config.isSelectTextOnFocusGained())
			TextFieldUtils.selectAllOnFocusGained();

		// Initialise file choosers
		initFileChoosers();

		// Perform remaining initialisation from event-dispatching thread
		SwingUtilities.invokeLater(() ->
		{
			// Create main window
			mainWindow = new MainWindow();

			// Start file-check timer
			new Timer(FILE_CHECK_TIMER_INTERVAL, AppCommand.CHECK_MODIFIED_FILE).start();

			// No command-line arguments: open new file if configured to do so
			if (args.length == 0)
			{
				if (AppConfig.INSTANCE.isNewDocumentOnStartup())
					AppCommand.CREATE_FILE.execute();
			}

			// Command-line arguments: open files
			else
			{
				// Create list of files from command-line arguments
				List<File> files = Arrays.stream(args)
											.map(argument -> new File(PathnameUtils.parsePathname(argument)))
											.collect(Collectors.toList());

				// Open files
				openFiles(files);

				// Update title and menus
				mainWindow.updateTitleAndMenus();
			}
		});
	}

	//------------------------------------------------------------------

	public boolean confirmWriteFile(File   file,
									String titleStr)
	{
		String[] optionStrs = Utils.getOptionStrings(AppConstants.REPLACE_STR);
		return !file.exists()
				|| (JOptionPane.showOptionDialog(mainWindow, Utils.getPathname(file) + AppConstants.ALREADY_EXISTS_STR,
												 titleStr, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
												 null, optionStrs, optionStrs[1]) == JOptionPane.OK_OPTION);
	}

	//------------------------------------------------------------------

	public void updateTabText(FunctionDocument document)
	{
		for (int i = 0; i < getNumDocuments(); i++)
		{
			if (getDocument(i) == document)
			{
				mainWindow.setTabText(i, document.getTitleString(false), document.getTitleString(true));
				break;
			}
		}
	}

	//------------------------------------------------------------------

	public void updateCommands()
	{
		FunctionDocument document = getDocument();
		boolean isDocument = (document != null);
		boolean notFull = !isDocumentsFull();
		boolean documentChanged = isDocument && document.isChanged();

		AppCommand.CHECK_MODIFIED_FILE.setEnabled(true);
		AppCommand.IMPORT_FILES.setEnabled(true);
		AppCommand.CREATE_FILE.setEnabled(notFull);
		AppCommand.OPEN_FILE.setEnabled(notFull);
		AppCommand.REVERT_FILE.setEnabled(documentChanged && (document.getFile() != null));
		AppCommand.CLOSE_FILE.setEnabled(isDocument);
		AppCommand.CLOSE_ALL_FILES.setEnabled(isDocument);
		AppCommand.SAVE_FILE.setEnabled(documentChanged);
		AppCommand.SAVE_FILE_AS.setEnabled(isDocument);
		AppCommand.EXPORT_IMAGE.setEnabled(isDocument && FunctionDocument.canWriteImages());
		AppCommand.EXIT.setEnabled(true);
		AppCommand.COPY_INTERVALS.setEnabled(getNumDocuments() > 1);
		AppCommand.EDIT_PREFERENCES.setEnabled(true);
		AppCommand.TOGGLE_SHOW_FULL_PATHNAMES.setEnabled(true);
		AppCommand.TOGGLE_SHOW_FULL_PATHNAMES.setSelected(AppConfig.INSTANCE.isShowFullPathnames());
	}

	//------------------------------------------------------------------

	public void executeCommand(AppCommand command)
	{
		try
		{
			switch (command)
			{
				case CHECK_MODIFIED_FILE:
					onCheckModifiedFile();
					break;

				case IMPORT_FILES:
					onImportFiles();
					break;

				case CREATE_FILE:
					onCreateFile();
					break;

				case OPEN_FILE:
					onOpenFile();
					break;

				case REVERT_FILE:
					onRevertFile();
					break;

				case CLOSE_FILE:
					onCloseFile();
					break;

				case CLOSE_ALL_FILES:
					onCloseAllFiles();
					break;

				case SAVE_FILE:
					onSaveFile();
					break;

				case SAVE_FILE_AS:
					onSaveFileAs();
					break;

				case EXPORT_IMAGE:
					onExportImage();
					break;

				case EXIT:
					onExit();
					break;

				case COPY_INTERVALS:
					onCopyIntervals();
					break;

				case EDIT_PREFERENCES:
					onEditPreferences();
					break;

				case TOGGLE_SHOW_FULL_PATHNAMES:
					onToggleShowFullPathnames();
					break;
			}
		}
		catch (AppException e)
		{
			showErrorMessage(SHORT_NAME, e);
		}

		if (command != AppCommand.CHECK_MODIFIED_FILE)
		{
			updateTabText(getDocument());
			mainWindow.updateTitleAndMenus();
		}
	}

	//------------------------------------------------------------------

	public void closeDocument(int index)
	{
		if (confirmCloseDocument(index))
			removeDocument(index);
	}

	//------------------------------------------------------------------

	private void addDocument(FunctionDocument document)
	{
		DocumentView documentView = new DocumentView(document);
		documentsViews.add(documentView);
		mainWindow.addView(document.getTitleString(false), document.getTitleString(true), documentView.view);
	}

	//------------------------------------------------------------------

	private void removeDocument(int index)
	{
		documentsViews.remove(index);
		mainWindow.removeView(index);
	}

	//------------------------------------------------------------------

	private boolean readDocument(FunctionDocument          document,
								 FunctionDocument.FileInfo fileInfo)
		throws AppException
	{
		boolean documentRead = false;
		try
		{
			List<String> errorStrs = new ArrayList<>();
			TaskProgressDialog.showDialog(mainWindow, READ_DOCUMENT_STR,
										  new Task.ReadDocument(document, fileInfo, errorStrs));
			if (errorStrs.isEmpty())
				documentRead = true;
			else
				ErrorListDialog.showDialog(mainWindow, ERRORS_STR, Utils.getPathname(fileInfo.file), errorStrs);
		}
		catch (TaskCancelledException e)
		{
			// ignore
		}
		return documentRead;
	}

	//------------------------------------------------------------------

	private void writeDocument(FunctionDocument          document,
							   FunctionDocument.FileInfo fileInfo,
							   boolean                   includeColours)
		throws AppException
	{
		TaskProgressDialog.showDialog(mainWindow, WRITE_DOCUMENT_STR,
									  new Task.WriteDocument(document, fileInfo, includeColours));
	}

	//------------------------------------------------------------------

	private void openDocument(FunctionDocument.FileInfo fileInfo)
		throws AppException
	{
		// Test whether document is already open
		for (int i = 0; i < documentsViews.size(); i++)
		{
			if (Utils.isSameFile(fileInfo.file, getDocument(i).getFile()))
			{
				mainWindow.selectView(i);
				return;
			}
		}

		// Read document and add to list
		FunctionDocument document = new FunctionDocument();
		if (readDocument(document, fileInfo))
		{
			addDocument(document);
			warnComments(document);
		}
	}

	//------------------------------------------------------------------

	private void revertDocument(FunctionDocument.FileInfo fileInfo)
		throws AppException
	{
		FunctionDocument document = new FunctionDocument();
		if (readDocument(document, fileInfo))
		{
			int index = mainWindow.getTabIndex();
			documentsViews.set(index, new DocumentView(document));
			mainWindow.setTabText(index, document.getTitleString(false), document.getTitleString(true));
			mainWindow.setView(index, getView());
			warnComments(document);
		}
	}

	//------------------------------------------------------------------

	private boolean confirmCloseDocument(int index)
	{
		// Test whether document has changed
		FunctionDocument document = getDocument(index);
		if (!document.isChanged())
			return true;

		// Restore window
		GuiUtils.restoreFrame(mainWindow);

		// Display document
		mainWindow.selectView(index);

		// Display prompt to save changed document
		FunctionDocument.FileInfo fileInfo = document.getFileInfo();
		String messageStr = ((fileInfo.file == null)
									? UNNAMED_FILE_STR
									: Utils.getPathname(fileInfo.file) + CHANGED_MESSAGE1_STR) + CHANGED_MESSAGE2_STR;
		String[] optionStrs = Utils.getOptionStrings(SAVE_STR, DISCARD_STR);
		int result = JOptionPane.showOptionDialog(mainWindow, messageStr, SAVE_CLOSE_FILE_STR,
												  JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
												  optionStrs, optionStrs[0]);

		// Discard changed document
		if (result == JOptionPane.NO_OPTION)
			return true;

		// Save changed document
		if (result == JOptionPane.YES_OPTION)
		{
			// Choose filename
			if (fileInfo.file == null)
			{
				fileInfo = chooseSave(fileInfo);
				if (fileInfo == null)
					return false;

				if (fileInfo.file.exists())
				{
					messageStr = Utils.getPathname(fileInfo.file) + AppConstants.ALREADY_EXISTS_STR;
					result = JOptionPane.showConfirmDialog(mainWindow, messageStr, SAVE_CLOSE_FILE_STR,
														   JOptionPane.YES_NO_CANCEL_OPTION,
														   JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.NO_OPTION)
						return true;
					if (result != JOptionPane.YES_OPTION)
						return false;
				}
			}

			// Write file
			try
			{
				IncludeColours includeColours = confirmIncludeColours(document, SAVE_CLOSE_FILE_STR);
				if (includeColours == null)
					return false;
				writeDocument(document, fileInfo, includeColours == IncludeColours.YES);
				return true;
			}
			catch (AppException e)
			{
				showErrorMessage(SAVE_CLOSE_FILE_STR, e);
			}
		}

		return false;
	}

	//------------------------------------------------------------------

	private void initFileChoosers()
	{
		AppConfig config = AppConfig.INSTANCE;

		openFileChooser = new JFileChooser(config.getFunctionDirectory());
		openFileChooser.setDialogTitle(OPEN_FUNCTION_FILE_STR);
		openFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		for (FileKind fileKind : FileKind.values())
			openFileChooser.addChoosableFileFilter(fileKind.getFilter());
		openFileChooser.setFileFilter(config.getDefaultFileKind().getFilter());

		saveFileChooser = new JFileChooser(config.getFunctionDirectory());
		saveFileChooser.setDialogTitle(SAVE_FUNCTION_FILE_STR);
		saveFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		for (FileKind fileKind : FileKind.values())
			saveFileChooser.addChoosableFileFilter(fileKind.getFilter());
		saveFileChooser.setFileFilter(config.getDefaultFileKind().getFilter());

		exportFileChooser = new JFileChooser(config.getFunctionDirectory());
		exportFileChooser.setDialogTitle(EXPORT_IMAGE_FILE_STR);
		exportFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		exportFileChooser.setFileFilter(new FilenameSuffixFilter(AppConstants.PNG_FILES_STR,
																 AppConstants.PNG_FILE_SUFFIX));
	}

	//------------------------------------------------------------------

	private FunctionDocument.FileInfo chooseOpen()
	{
		FunctionDocument.FileInfo fileInfo = null;
		openFileChooser.rescanCurrentDirectory();
		if (openFileChooser.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION)
		{
			FileKind fileKind = FileKind.forDescription(openFileChooser.getFileFilter().getDescription());
			fileInfo = new FunctionDocument.FileInfo(openFileChooser.getSelectedFile(), fileKind);
		}
		return fileInfo;
	}

	//------------------------------------------------------------------

	private FunctionDocument.FileInfo chooseSave(FunctionDocument.FileInfo fileInfo)
	{
		if (fileInfo.file != null)
			saveFileChooser.setSelectedFile(fileInfo.file.getAbsoluteFile());
		saveFileChooser.rescanCurrentDirectory();
		saveFileChooser.setFileFilter(fileInfo.fileKind.getFilter());
		if (saveFileChooser.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION)
		{
			File file = saveFileChooser.getSelectedFile();
			FileKind fileKind = FileKind.forDescription(saveFileChooser.getFileFilter().getDescription());
			if (fileKind != null)
				file = Utils.appendSuffix(file, fileKind.getFilter().getSuffix(0));
			fileInfo = new FunctionDocument.FileInfo(file, fileKind);
		}
		else
			fileInfo = null;
		return fileInfo;
	}

	//------------------------------------------------------------------

	private File chooseExport()
	{
		if (exportFile != null)
			exportFileChooser.setSelectedFile(exportFile.getAbsoluteFile());
		exportFileChooser.rescanCurrentDirectory();
		if (exportFileChooser.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION)
		{
			exportFile = Utils.appendSuffix(exportFileChooser.getSelectedFile(), AppConstants.PNG_FILE_SUFFIX);
			return exportFile;
		}
		return null;
	}

	//------------------------------------------------------------------

	private void warnComments(FunctionDocument document)
	{
		if (document.hasComments())
			JOptionPane.showMessageDialog(mainWindow, HAS_COMMENTS_STR, OPEN_FILE_STR, JOptionPane.WARNING_MESSAGE);
	}

	//------------------------------------------------------------------

	private IncludeColours confirmIncludeColours(FunctionDocument document,
												 String           titleStr)
	{
		if (!document.hasFunctions())
			return IncludeColours.NO;

		switch (AppConfig.INSTANCE.getSaveFunctionColours())
		{
			case NO:
				return IncludeColours.NO;

			case YES:
				return IncludeColours.YES;

			case ASK:
				switch (JOptionPane.showConfirmDialog(mainWindow, SAVE_COLOURS_STR, titleStr,
													  JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE))
				{
					case JOptionPane.YES_OPTION:
						return IncludeColours.YES;

					case JOptionPane.NO_OPTION:
						return IncludeColours.NO;
				}
		}
		return null;
	}

	//------------------------------------------------------------------

	private void updateConfiguration()
	{
		// Set location of main window
		AppConfig config = AppConfig.INSTANCE;
		if (config.isMainWindowLocation())
		{
			Point location = GuiUtils.getFrameLocation(mainWindow);
			if (location != null)
				config.setMainWindowLocation(location);
		}

		// Write configuration file
		config.write();
	}

	//------------------------------------------------------------------

	private void openFiles(List<File> files)
	{
		for (int i = 0; i < files.size(); i++)
		{
			if (isDocumentsFull())
				break;
			try
			{
				openDocument(new FunctionDocument.FileInfo(files.get(i), null));
				if (Task.isCancelled())
					break;
			}
			catch (AppException e)
			{
				if (i == files.size() - 1)
					showErrorMessage(OPEN_FILE_STR, e);
				else
				{
					String[] optionStrs = Utils.getOptionStrings(AppConstants.CONTINUE_STR);
					if (JOptionPane.showOptionDialog(mainWindow, e, OPEN_FILE_STR, JOptionPane.OK_CANCEL_OPTION,
													 JOptionPane.ERROR_MESSAGE, null, optionStrs, optionStrs[1])
																							!= JOptionPane.OK_OPTION)
						break;
				}
			}
		}
	}

	//------------------------------------------------------------------

	private void onCheckModifiedFile()
		throws AppException
	{
		FunctionDocument document = getDocument();
		if ((document != null) && !document.isExecutingCommand())
		{
			FunctionDocument.FileInfo fileInfo = document.getFileInfo();
			File file = fileInfo.file;
			long timestamp = document.getTimestamp();
			if ((file != null) && (timestamp != 0))
			{
				long currentTimestamp = file.lastModified();
				if ((currentTimestamp != 0) && (currentTimestamp != timestamp))
				{
					String messageStr = Utils.getPathname(file) + MODIFIED_MESSAGE_STR;
					if (JOptionPane.showConfirmDialog(mainWindow, messageStr, MODIFIED_FILE_STR,
													  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
																						== JOptionPane.YES_OPTION)
					{
						try
						{
							revertDocument(fileInfo);
							mainWindow.updateTitleAndMenus();
						}
						catch (AppException e)
						{
							document.setTimestamp(currentTimestamp);
							throw e;
						}
					}
					else
						document.setTimestamp(currentTimestamp);
				}
			}
		}
	}

	//------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	private void onImportFiles()
	{
		openFiles((List<File>)AppCommand.IMPORT_FILES.getValue(AppCommand.Property.FILES));
	}

	//------------------------------------------------------------------

	private void onCreateFile()
	{
		if (!isDocumentsFull())
			addDocument(new FunctionDocument(++newFileIndex));
	}

	//------------------------------------------------------------------

	private void onOpenFile()
		throws AppException
	{
		if (!isDocumentsFull())
		{
			FunctionDocument.FileInfo fileInfo = chooseOpen();
			if (fileInfo != null)
				openDocument(fileInfo);
		}
	}

	//------------------------------------------------------------------

	private void onRevertFile()
		throws AppException
	{
		FunctionDocument document = getDocument();
		if ((document != null) && document.isChanged())
		{
			FunctionDocument.FileInfo fileInfo = document.getFileInfo();
			File file = fileInfo.file;
			if (file != null)
			{
				String messageStr = Utils.getPathname(file) + REVERT_MESSAGE_STR;
				String[] optionStrs = Utils.getOptionStrings(REVERT_STR);
				if (JOptionPane.showOptionDialog(mainWindow, messageStr, REVERT_FILE_STR, JOptionPane.OK_CANCEL_OPTION,
												 JOptionPane.QUESTION_MESSAGE, null, optionStrs, optionStrs[1])
																							== JOptionPane.OK_OPTION)
					revertDocument(fileInfo);
			}
		}
	}

	//------------------------------------------------------------------

	private void onCloseFile()
	{
		if (hasDocuments())
			closeDocument(mainWindow.getTabIndex());
	}

	//------------------------------------------------------------------

	private void onCloseAllFiles()
	{
		while (hasDocuments())
		{
			int index = getNumDocuments() - 1;
			if (!confirmCloseDocument(index))
				break;
			removeDocument(index);
		}
	}

	//------------------------------------------------------------------

	private void onSaveFile()
		throws AppException
	{
		FunctionDocument document = getDocument();
		if ((document != null) && document.isChanged())
		{
			if (document.getFile() == null)
				onSaveFileAs();
			else
			{
				IncludeColours includeColours = confirmIncludeColours(document, SAVE_FILE_STR);
				if (includeColours != null)
					writeDocument(document, null, includeColours == IncludeColours.YES);
			}
		}
	}

	//------------------------------------------------------------------

	private void onSaveFileAs()
		throws AppException
	{
		FunctionDocument document = getDocument();
		if (document != null)
		{
			FunctionDocument.FileInfo fileInfo = chooseSave(document.getFileInfo());
			if ((fileInfo != null) && confirmWriteFile(fileInfo.file, SAVE_FILE_AS_STR))
			{
				IncludeColours includeColours = confirmIncludeColours(document, SAVE_FILE_AS_STR);
				if (includeColours != null)
					writeDocument(document, fileInfo, includeColours == IncludeColours.YES);
			}
		}
	}

	//------------------------------------------------------------------

	private void onExportImage()
		throws AppException
	{
		FunctionDocument document = getDocument();
		if (document != null)
		{
			File file = chooseExport();
			if ((file != null) && confirmWriteFile(file, EXPORT_IMAGE_STR))
				TaskProgressDialog.showDialog(mainWindow, WRITE_IMAGE_STR, new Task.WriteImage(document, file));
		}
	}

	//------------------------------------------------------------------

	private void onExit()
	{
		if (!exiting)
		{
			try
			{
				// Prevent re-entry to this method
				exiting = true;

				// Close all open documents
				while (hasDocuments())
				{
					int index = getNumDocuments() - 1;
					if (!confirmCloseDocument(index))
						return;
					removeDocument(index);
				}

				// Update configuration
				updateConfiguration();

				// Destroy main window
				mainWindow.setVisible(false);
				mainWindow.dispose();

				// Exit application
				System.exit(0);
			}
			finally
			{
				exiting = false;
			}
		}
	}

	//------------------------------------------------------------------

	private void onCopyIntervals()
	{
		int numDocuments = getNumDocuments();
		if (numDocuments >= 2)
		{
			int currentIndex = mainWindow.getTabIndex();
			String[] strs = new String[numDocuments - 1];
			int index = 0;
			for (int i = 0; i < numDocuments; i++)
			{
				if (i != currentIndex)
					strs[index++] = getDocument(i).getTitleString(AppConfig.INSTANCE.isShowFullPathnames());
			}
			int[] selections = ListSelectionDialog.showDialog(mainWindow, COPY_INTERVALS_STR, DOCUMENTS_STR, strs);
			if (selections != null)
			{
				FunctionDocument document = getDocument();
				for (int i : selections)
				{
					if (i >= currentIndex)
						++i;
					getDocument(i).setIntervals(document.getXInterval(), document.getYInterval());
					getView(i).updateIntervals();
				}
			}
		}
	}

	//------------------------------------------------------------------

	private void onEditPreferences()
	{
		if (PreferencesDialog.showDialog(mainWindow))
		{
			ExceptionUtils.setUnixStyle(AppConfig.INSTANCE.isShowUnixPathnames());
			if (getView() != null)
				getView().repaint();
		}
	}

	//------------------------------------------------------------------

	private void onToggleShowFullPathnames()
	{
		AppConfig config = AppConfig.INSTANCE;
		config.setShowFullPathnames(!config.isShowFullPathnames());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	ResourceProperties	buildProperties;
	private	String				versionStr;
	private	MainWindow			mainWindow;
	private	List<DocumentView>	documentsViews;
	private	JFileChooser		openFileChooser;
	private	JFileChooser		saveFileChooser;
	private	JFileChooser		exportFileChooser;
	private	File				exportFile;
	private	int					newFileIndex;
	private	boolean				exiting;

}

//----------------------------------------------------------------------
