/*====================================================================*\

MainWindow.java

Main window class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.datatransfer.UnsupportedFlavorException;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import java.io.File;
import java.io.IOException;

import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.ui.swing.menu.FCheckBoxMenuItem;
import uk.blankaspect.ui.swing.menu.FMenu;
import uk.blankaspect.ui.swing.menu.FMenuItem;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.tabbedpane.TabbedPane;

import uk.blankaspect.ui.swing.transfer.DataImporter;

//----------------------------------------------------------------------


// MAIN WINDOW CLASS


class MainWindow
	extends JFrame
	implements ChangeListener, MenuListener, WindowFocusListener
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	TabbedPane	tabbedPanel;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public MainWindow()
	{
		// Set icons
		setIconImages(Images.APP_ICON_IMAGES);


		//----  Menu bar

		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorder(null);

		// File menu
		JMenu menu = Menu.FILE.menu;
		menu.addMenuListener(this);

		menu.add(new FMenuItem(AppCommand.CREATE_FILE, KeyEvent.VK_N));
		menu.add(new FMenuItem(AppCommand.OPEN_FILE, KeyEvent.VK_O));
		menu.add(new FMenuItem(AppCommand.REVERT_FILE, KeyEvent.VK_R));

		menu.addSeparator();

		menu.add(new FMenuItem(AppCommand.CLOSE_FILE, KeyEvent.VK_C));
		menu.add(new FMenuItem(AppCommand.CLOSE_ALL_FILES, KeyEvent.VK_L));

		menu.addSeparator();

		menu.add(new FMenuItem(AppCommand.SAVE_FILE, KeyEvent.VK_S));
		menu.add(new FMenuItem(AppCommand.SAVE_FILE_AS, KeyEvent.VK_A));

		menu.addSeparator();

		menu.add(new FMenuItem(AppCommand.EXPORT_IMAGE, KeyEvent.VK_E));

		menu.addSeparator();

		menu.add(new FMenuItem(AppCommand.EXIT, KeyEvent.VK_X));

		menuBar.add(menu);

		// Edit menu
		menu = Menu.EDIT.menu;
		menu.addMenuListener(this);

		menu.add(new FMenuItem(FunctionDocument.Command.UNDO, KeyEvent.VK_U));
		menu.add(new FMenuItem(FunctionDocument.Command.REDO, KeyEvent.VK_R));
		menu.add(new FMenuItem(FunctionDocument.Command.CLEAR_EDIT_LIST, KeyEvent.VK_L));

		menu.addSeparator();

		menu.add(new FMenuItem(FunctionDocument.Command.EDIT_COMMENT, KeyEvent.VK_C));

		menu.addSeparator();

		menu.add(new FMenuItem(FunctionDocument.Command.REVERSE_FUNCTIONS, KeyEvent.VK_V));
		menu.add(new FMenuItem(AppCommand.COPY_INTERVALS, KeyEvent.VK_I));

		menuBar.add(menu);

		// View menu
		menu = Menu.VIEW.menu;
		menu.addMenuListener(this);

		menu.add(new FCheckBoxMenuItem(FunctionDocument.Command.TOGGLE_SHOW_GRID, KeyEvent.VK_G));

		menuBar.add(menu);

		// Options menu
		menu = Menu.OPTIONS.menu;
		menu.addMenuListener(this);

		menu.add(new FMenuItem(AppCommand.EDIT_PREFERENCES, KeyEvent.VK_P));
		menu.add(new FCheckBoxMenuItem(AppCommand.TOGGLE_SHOW_FULL_PATHNAMES, KeyEvent.VK_F));

		menuBar.add(menu);

		// Set menu bar
		setJMenuBar(menuBar);


		//----  Tabbed panel

		tabbedPanel = new TabbedPane();
		tabbedPanel.setIgnoreCase(true);
		tabbedPanel.addChangeListener(this);

		// Set transfer handler
		tabbedPanel.setTransferHandler(new FileTransferHandler());


		//----  Window

		// Set content pane
		setContentPane(tabbedPanel);

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Handle window closing
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent event)
			{
				AppCommand.EXIT.execute();
			}
		});

		// Handle change to focused window
		addWindowFocusListener(this);

		// Prevent window from being resized
		setResizable(false);

		// Set window to its default size with a temporary view
		addView("", null, new FunctionView(new FunctionDocument()));
		pack();
		removeView(0);

		// Set location of window
		AppConfig config = AppConfig.INSTANCE;
		setLocation(config.isMainWindowLocation()
								? GuiUtils.getLocationWithinScreen(this, config.getMainWindowLocation())
								: GuiUtils.getComponentLocation(this));

		// Update title and menus
		updateTitleAndMenus();

		// Make window visible
		setVisible(true);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ChangeListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void stateChanged(ChangeEvent event)
	{
		if (event.getSource() == tabbedPanel)
		{
			if (isVisible())
				updateTitleAndMenus();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MenuListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void menuCanceled(MenuEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	public void menuDeselected(MenuEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	public void menuSelected(MenuEvent event)
	{
		Object eventSource = event.getSource();
		for (Menu menu : Menu.values())
		{
			if (eventSource == menu.menu)
				menu.update();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : WindowFocusListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void windowGainedFocus(WindowEvent event)
	{
		FunctionView view = FuncPlotterApp.INSTANCE.getView();
		if (view != null)
			view.showMouseCursorCoords(true);
	}

	//------------------------------------------------------------------

	@Override
	public void windowLostFocus(WindowEvent event)
	{
		FunctionView view = FuncPlotterApp.INSTANCE.getView();
		if (view != null)
			view.showMouseCursorCoords(false);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getTabIndex()
	{
		return tabbedPanel.getSelectedIndex();
	}

	//------------------------------------------------------------------

	public void addView(String       title,
						String       tooltipText,
						FunctionView view)
	{
		tabbedPanel.addComponent(title, new CloseAction(), view);
		view.setDefaultFocus();
		int index = tabbedPanel.getNumTabs() - 1;
		tabbedPanel.setTooltipText(index, tooltipText);
		tabbedPanel.setSelectedIndex(index);
	}

	//------------------------------------------------------------------

	public void removeView(int index)
	{
		tabbedPanel.removeComponent(index);
	}

	//------------------------------------------------------------------

	public void setView(int          index,
						FunctionView view)
	{
		tabbedPanel.setComponent(index, view);
	}

	//------------------------------------------------------------------

	public void selectView(int index)
	{
		tabbedPanel.setSelectedIndex(index);
	}

	//------------------------------------------------------------------

	public void setTabText(int    index,
						   String title,
						   String tooltipText)
	{
		tabbedPanel.setTitle(index, title);
		tabbedPanel.setTooltipText(index, tooltipText);
	}

	//------------------------------------------------------------------

	public void updateTitleAndMenus()
	{
		updateTitle();
		updateMenus();
	}

	//------------------------------------------------------------------

	private void updateTitle()
	{
		FunctionDocument document = FuncPlotterApp.INSTANCE.getDocument();
		boolean fullPathname = AppConfig.INSTANCE.isShowFullPathnames();
		setTitle((document == null) ? FuncPlotterApp.LONG_NAME + " " + FuncPlotterApp.INSTANCE.getVersionString()
									: FuncPlotterApp.SHORT_NAME + " - " + document.getTitleString(fullPathname));
	}

	//------------------------------------------------------------------

	private void updateMenus()
	{
		for (Menu menu : Menu.values())
			menu.update();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// MENUS


	private enum Menu
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		FILE
		(
			"File",
			KeyEvent.VK_F
		)
		{
			@Override
			protected void update()
			{
				updateAppCommands();
			}
		},

		EDIT
		(
			"Edit",
			KeyEvent.VK_E
		)
		{
			@Override
			protected void update()
			{
				getMenu().setEnabled(FuncPlotterApp.INSTANCE.hasDocuments());
				updateAppCommands();
				updateDocumentCommands();
			}
		},

		VIEW
		(
			"View",
			KeyEvent.VK_V
		)
		{
			@Override
			protected void update()
			{
				getMenu().setEnabled(FuncPlotterApp.INSTANCE.hasDocuments());
				updateDocumentCommands();
			}
		},

		OPTIONS
		(
			"Options",
			KeyEvent.VK_O
		)
		{
			@Override
			protected void update()
			{
				updateAppCommands();
			}
		};

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	JMenu	menu;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Menu(String text,
					 int    keyCode)
		{
			menu = new FMenu(text, keyCode);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static void updateAppCommands()
		{
			FuncPlotterApp.INSTANCE.updateCommands();
		}

		//--------------------------------------------------------------

		private static void updateDocumentCommands()
		{
			FunctionDocument document = FuncPlotterApp.INSTANCE.getDocument();
			if (document == null)
				FunctionDocument.Command.setAllEnabled(false);
			else
				document.updateCommands();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract void update();

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		protected JMenu getMenu()
		{
			return menu;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		FILE_TRANSFER_NOT_SUPPORTED
		("File transfer is not supported."),

		ERROR_TRANSFERRING_DATA
		("An error occurred while transferring data.");

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

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

		@Override
		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLOSE ACTION CLASS


	private static class CloseAction
		extends AbstractAction
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CloseAction()
		{
			putValue(Action.ACTION_COMMAND_KEY, "");
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(ActionEvent event)
		{
			FuncPlotterApp.INSTANCE.closeDocument(Integer.parseInt(event.getActionCommand()));
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// FILE TRANSFER HANDLER CLASS


	private class FileTransferHandler
		extends TransferHandler
		implements Runnable
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public FileTransferHandler()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			AppCommand.IMPORT_FILES.execute();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean canImport(TransferHandler.TransferSupport support)
		{
			boolean supported = !support.isDrop() || ((support.getSourceDropActions() & COPY) == COPY);
			if (supported)
				supported = DataImporter.isFileList(support.getDataFlavors());
			if (support.isDrop() && supported)
				support.setDropAction(COPY);
			return supported;
		}

		//--------------------------------------------------------------

		@Override
		public boolean importData(TransferHandler.TransferSupport support)
		{
			if (canImport(support))
			{
				try
				{
					try
					{
						List<File> files = DataImporter.getFiles(support.getTransferable());
						if (!files.isEmpty())
						{
							toFront();
							AppCommand.IMPORT_FILES.putValue(AppCommand.Property.FILES, files);
							SwingUtilities.invokeLater(this);
							return true;
						}
					}
					catch (UnsupportedFlavorException e)
					{
						throw new AppException(ErrorId.FILE_TRANSFER_NOT_SUPPORTED);
					}
					catch (IOException e)
					{
						throw new AppException(ErrorId.ERROR_TRANSFERRING_DATA);
					}
				}
				catch (AppException e)
				{
					FuncPlotterApp.INSTANCE.showErrorMessage(FuncPlotterApp.SHORT_NAME, e);
				}
			}
			return false;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
