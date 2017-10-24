/*====================================================================*\

PreferencesDialog.java

Preferences dialog box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;

import uk.blankaspect.common.gui.BooleanComboBox;
import uk.blankaspect.common.gui.ColourSampleIcon;
import uk.blankaspect.common.gui.DimensionsSpinnerPanel;
import uk.blankaspect.common.gui.FButton;
import uk.blankaspect.common.gui.FCheckBox;
import uk.blankaspect.common.gui.FComboBox;
import uk.blankaspect.common.gui.FIntegerSpinner;
import uk.blankaspect.common.gui.FLabel;
import uk.blankaspect.common.gui.FontEx;
import uk.blankaspect.common.gui.FontStyle;
import uk.blankaspect.common.gui.FTabbedPane;
import uk.blankaspect.common.gui.GuiUtils;
import uk.blankaspect.common.gui.IntegerSpinner;
import uk.blankaspect.common.gui.PathnamePanel;
import uk.blankaspect.common.gui.TextRendering;
import uk.blankaspect.common.gui.TitledBorder;

import uk.blankaspect.common.misc.IntegerRange;
import uk.blankaspect.common.misc.KeyAction;
import uk.blankaspect.common.misc.NoYesAsk;
import uk.blankaspect.common.misc.StringUtils;

import uk.blankaspect.common.textfield.IntegerValueField;

//----------------------------------------------------------------------


// PREFERENCES DIALOG BOX CLASS


class PreferencesDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	KEY	= PreferencesDialog.class.getCanonicalName();

	// Main panel
	private static final	String	TITLE_STR				= "Preferences";
	private static final	String	SAVE_CONFIGURATION_STR	= "Save configuration";
	private static final	String	SAVE_CONFIG_FILE_STR	= "Save configuration file";
	private static final	String	WRITE_CONFIG_FILE_STR	= "Write configuration file";

	// General panel
	private static final	int		MAX_EDIT_LIST_LENGTH_FIELD_LENGTH	= 4;

	private static final	String	DEFAULT_FILE_KIND_STR			= "Default file kind";
	private static final	String	NEW_DOCUMENT_ON_STARTUP_STR		= "New document on startup";
	private static final	String	SAVE_FUNCTION_COLOURS_STR		= "Save function colours in document";
	private static final	String	SHOW_UNIX_PATHNAMES_STR			= "Display UNIX-style pathnames";
	private static final	String	SELECT_TEXT_ON_FOCUS_GAINED_STR	= "Select text when focus is gained";
	private static final	String	SAVE_MAIN_WINDOW_LOCATION_STR	= "Save location of main window";
	private static final	String	MAX_EDIT_HISTORY_SIZE_STR		= "Maximum size of edit history";
	private static final	String	CLEAR_EDIT_HISTORY_ON_SAVE_STR	= "Clear edit history on save";

	// Appearance panel
	private static final	String	LOOK_AND_FEEL_STR		= "Look-and-feel";
	private static final	String	TEXT_ANTIALIASING_STR	= "Text antialiasing";
	private static final	String	NO_LOOK_AND_FEELS_STR	= "<no look-and-feels>";

	// Plot panel
	private static final	int		PLOT_SIZE_FIELD_LENGTH				= 4;
	private static final	int		NUM_FRACTION_DIGITS_FIELD_LENGTH	= 2;
	private static final	int		NUM_Y_SCALE_DIGITS_FIELD_LENGTH		= 2;
	private static final	int		FIXED_POINT_RANGE_FIELD_LENGTH		= 3;

	private static final	String	PLOT_SIZE_STR					= "Plot size";
	private static final	String	SHOW_GRID_STR					= "Show grid";
	private static final	String	NUM_FRACTION_DIGITS_STR			= "Maximum number of fraction digits";
	private static final	String	NUM_Y_SCALE_DIGITS_STR			= "Number of y-scale digits";
	private static final	String	FIXED_POINT_EXPONENT_RANGE_STR	= "Fixed-point exponent range";
	private static final	String	TO_STR							= "to";
	private static final	String	NORMALISE_SCI_NOTATION_STR		= "Normalise scientific notation";
	private static final	String	TRUNCATE_X_SCALE_TEXT_STR		= "Truncate x-scale text";
	private static final	String	PLOT_COLOURS_STR				= "Plot colours";
	private static final	String	FOCUSED_BORDER_STR				= "Focused border";
	private static final	String	IMAGE_MARGIN_STR				= "Image margin";
	private static final	String	BACKGROUND_STR					= "Background";
	private static final	String	GRID_STR						= "Grid";
	private static final	String	AXIS_STR						= "Axis";
	private static final	String	SCALE_MARKINGS_STR				= "Scale markings";
	private static final	String	PLOT_COLOUR_STR					= "Plot colour | ";
	private static final	String	FOCUSED_BORDER_TITLE_STR		= "Focused border";
	private static final	String	IMAGE_MARGIN_TITLE_STR			= "Image margin";
	private static final	String	BACKGROUND_TITLE_STR			= "Background";
	private static final	String	GRID_TITLE_STR					= "Grid";
	private static final	String	AXIS_TITLE_STR					= "Axis";
	private static final	String	SCALE_MARKINGS_TITLE_STR		= "Scale markings";

	// Function panel
	private static final	int		LIST_WIDTH_FIELD_LENGTH		= 2;
	private static final	int		LIST_HEIGHT_FIELD_LENGTH	= 2;

	private static final	int		NUM_FUNCTION_COLOUR_ROWS	= 4;

	private static final	String	DOCUMENT_DIRECTORY_STR		= "Document directory";
	private static final	String	LIST_SIZE_STR				= "Function list size";
	private static final	String	COLUMNS_STR					= "columns";
	private static final	String	ROWS_STR					= "rows";
	private static final	String	OBSCURED_COLOUR_STR			= "Obscured colour";
	private static final	String	OBSCURED_COLOUR_TITLE_STR	= "Obscured function colour";
	private static final	String	FUNCTION_COLOURS_STR		= "Default function colours";
	private static final	String	FUNCTION_COLOUR_TITLE_STR	= "Default function colour ";
	private static final	String	DIRECTORY_TITLE_STR			= "Document directory";
	private static final	String	SELECT_STR					= "Select";
	private static final	String	SELECT_DIRECTORY_STR		= "Select directory";

	// Fonts panel
	private static final	String	PT_STR	= "pt";

	// Commands
	private interface Command
	{
		String	CHOOSE_PLOT_COLOUR_FOCUSED_BORDER	= "choosePlotColourFocusedBorder";
		String	CHOOSE_PLOT_COLOUR_IMAGE_MARGIN		= "choosePlotColourImageMargin";
		String	CHOOSE_PLOT_COLOUR_BACKGROUND		= "choosePlotColourBackground";
		String	CHOOSE_PLOT_COLOUR_GRID				= "choosePlotColourGrid";
		String	CHOOSE_PLOT_COLOUR_AXIS				= "choosePlotColourAxis";
		String	CHOOSE_PLOT_COLOUR_SCALE			= "choosePlotColourScale";
		String	CHOOSE_FUNCTION_DIRECTORY			= "chooseFunctionDirectory";
		String	TOGGLE_FUNCTION_OBSCURED_COLOUR		= "toggleFunctionObscuredColour";
		String	CHOOSE_FUNCTION_OBSCURED_COLOUR		= "chooseFunctionObscuredColour";
		String	CHOOSE_FUNCTION_COLOUR				= "chooseFunctionColour";
		String	SAVE_CONFIGURATION					= "saveConfiguration";
		String	ACCEPT								= "accept";
		String	CLOSE								= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// TABS


	private enum Tab
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		GENERAL
		(
			"General"
		)
		{
			@Override
			protected JPanel createPanel(PreferencesDialog dialog)
			{
				return dialog.createPanelGeneral();
			}

			//----------------------------------------------------------

			@Override
			protected void validatePreferences(PreferencesDialog dialog)
				throws AppException
			{
				dialog.validatePreferencesGeneral();
			}

			//----------------------------------------------------------

			@Override
			protected void setPreferences(PreferencesDialog dialog)
			{
				dialog.setPreferencesGeneral();
			}

			//----------------------------------------------------------
		},

		APPEARANCE
		(
			"Appearance"
		)
		{
			@Override
			protected JPanel createPanel(PreferencesDialog dialog)
			{
				return dialog.createPanelAppearance();
			}

			//----------------------------------------------------------

			@Override
			protected void validatePreferences(PreferencesDialog dialog)
				throws AppException
			{
				dialog.validatePreferencesAppearance();
			}

			//----------------------------------------------------------

			@Override
			protected void setPreferences(PreferencesDialog dialog)
			{
				dialog.setPreferencesAppearance();
			}

			//----------------------------------------------------------
		},

		PLOT
		(
			"Plot"
		)
		{
			@Override
			protected JPanel createPanel(PreferencesDialog dialog)
			{
				return dialog.createPanelPlot();
			}

			//----------------------------------------------------------

			@Override
			protected void validatePreferences(PreferencesDialog dialog)
				throws AppException
			{
				dialog.validatePreferencesPlot();
			}

			//----------------------------------------------------------

			@Override
			protected void setPreferences(PreferencesDialog dialog)
			{
				dialog.setPreferencesPlot();
			}

			//----------------------------------------------------------
		},

		FUNCTION
		(
			"Function"
		)
		{
			@Override
			protected JPanel createPanel(PreferencesDialog dialog)
			{
				return dialog.createPanelFunction();
			}

			//----------------------------------------------------------

			@Override
			protected void validatePreferences(PreferencesDialog dialog)
				throws AppException
			{
				dialog.validatePreferencesFunction();
			}

			//----------------------------------------------------------

			@Override
			protected void setPreferences(PreferencesDialog dialog)
			{
				dialog.setPreferencesFunction();
			}

			//----------------------------------------------------------
		},

		FONTS
		(
			"Fonts"
		)
		{
			@Override
			protected JPanel createPanel(PreferencesDialog dialog)
			{
				return dialog.createPanelFonts();
			}

			//----------------------------------------------------------

			@Override
			protected void validatePreferences(PreferencesDialog dialog)
				throws AppException
			{
				dialog.validatePreferencesFonts();
			}

			//----------------------------------------------------------

			@Override
			protected void setPreferences(PreferencesDialog dialog)
			{
				dialog.setPreferencesFonts();
			}

			//----------------------------------------------------------
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Tab(String text)
		{
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract JPanel createPanel(PreferencesDialog dialog);

		//--------------------------------------------------------------

		protected abstract void validatePreferences(PreferencesDialog dialog)
			throws AppException;

		//--------------------------------------------------------------

		protected abstract void setPreferences(PreferencesDialog dialog);

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	String	text;

	}

	//==================================================================


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		DIRECTORY_DOES_NOT_EXIST
		("The directory does not exist."),

		NOT_A_DIRECTORY
		("The pathname does not denote a directory."),

		DIRECTORY_ACCESS_NOT_PERMITTED
		("Access to the directory was not permitted."),

		FIXED_POINT_BOUNDS_OUT_OF_ORDER
		("The upper bound of the fixed-point exponent range is less than the lower bound.");

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
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// COLOUR BUTTON CLASS


	private static class ColourButton
		extends JButton
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		ICON_WIDTH	= 40;
		private static final	int		ICON_HEIGHT	= 16;
		private static final	Insets	MARGINS		= new Insets(2, 2, 2, 2);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ColourButton(Color colour)
		{
			super(new ColourSampleIcon(ICON_WIDTH, ICON_HEIGHT));
			setMargin(MARGINS);
			setForeground(colour);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// COLOUR BUTTON PANEL CLASS


	private static class ColourButtonPanel
		extends JPanel
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ColourButtonPanel(int     index,
								  JButton button)
		{
			// Set layout
			GridBagLayout gridBag = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			setLayout(gridBag);

			// Label
			JLabel label = new FLabel(Integer.toString(index));

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_END;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 4);
			gridBag.setConstraints(label, gbc);
			add(label);

			// Button
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(button, gbc);
			add(button);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// FONT PANEL CLASS


	private static class FontPanel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	MIN_SIZE	= 0;
		private static final	int	MAX_SIZE	= 99;

		private static final	int	SIZE_FIELD_LENGTH	= 2;

		private static final	String	DEFAULT_FONT_STR	= "<default font>";

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// SIZE SPINNER CLASS


		private static class SizeSpinner
			extends IntegerSpinner
		{

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private SizeSpinner(int value)
			{
				super(value, MIN_SIZE, MAX_SIZE, SIZE_FIELD_LENGTH);
				AppFont.TEXT_FIELD.apply(this);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			/**
			 * @throws NumberFormatException
			 */

			@Override
			protected int getEditorValue()
			{
				IntegerValueField field = (IntegerValueField)getEditor();
				return (field.isEmpty() ? 0 : field.getValue());
			}

			//----------------------------------------------------------

			@Override
			protected void setEditorValue(int value)
			{
				IntegerValueField field = (IntegerValueField)getEditor();
				if (value == 0)
					field.setText(null);
				else
					field.setValue(value);
			}

			//----------------------------------------------------------

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private FontPanel(FontEx   font,
						  String[] fontNames)
		{
			nameComboBox = new FComboBox<>();
			nameComboBox.addItem(DEFAULT_FONT_STR);
			for (String fontName : fontNames)
				nameComboBox.addItem(fontName);
			nameComboBox.setSelectedIndex(Utils.indexOf(font.getName(), fontNames) + 1);

			styleComboBox = new FComboBox<>(FontStyle.values());
			styleComboBox.setSelectedValue(font.getStyle());

			sizeSpinner = new SizeSpinner(font.getSize());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public FontEx getFont()
		{
			String name = (nameComboBox.getSelectedIndex() <= 0) ? null : nameComboBox.getSelectedValue();
			return new FontEx(name, styleComboBox.getSelectedValue(), sizeSpinner.getIntValue());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	FComboBox<String>		nameComboBox;
		private	FComboBox<FontStyle>	styleComboBox;
		private	SizeSpinner				sizeSpinner;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private PreferencesDialog(Window owner)
	{

		// Call superclass constructor
		super(owner, TITLE_STR, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());


		//----  Tabbed panel

		tabbedPanel = new FTabbedPane();
		for (Tab tab : Tab.values())
			tabbedPanel.addTab(tab.text, tab.createPanel(this));
		tabbedPanel.setSelectedIndex(tabIndex);


		//----  Button panel: save configuration

		JPanel saveButtonPanel = new JPanel(new GridLayout(1, 0, 8, 0));

		// Button: save configuration
		JButton saveButton = new FButton(SAVE_CONFIGURATION_STR + AppConstants.ELLIPSIS_STR);
		saveButton.setActionCommand(Command.SAVE_CONFIGURATION);
		saveButton.addActionListener(this);
		saveButtonPanel.add(saveButton);


		//----  Button panel: OK, cancel

		JPanel okCancelButtonPanel = new JPanel(new GridLayout(1, 0, 8, 0));

		// Button: OK
		JButton okButton = new FButton(AppConstants.OK_STR);
		okButton.setActionCommand(Command.ACCEPT);
		okButton.addActionListener(this);
		okCancelButtonPanel.add(okButton);

		// Button: cancel
		JButton cancelButton = new FButton(AppConstants.CANCEL_STR);
		cancelButton.setActionCommand(Command.CLOSE);
		cancelButton.addActionListener(this);
		okCancelButtonPanel.add(cancelButton);


		//----  Button panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel buttonPanel = new JPanel(gridBag);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 24, 3, 24));

		int gridX = 0;

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 12);
		gridBag.setConstraints(saveButtonPanel, gbc);
		buttonPanel.add(saveButtonPanel);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 12, 0, 0);
		gridBag.setConstraints(okCancelButtonPanel, gbc);
		buttonPanel.add(okCancelButtonPanel);


		//----  Main panel

		JPanel mainPanel = new JPanel(gridBag);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		int gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(tabbedPanel, gbc);
		mainPanel.add(tabbedPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		mainPanel.add(buttonPanel);

		// Add commands to action map
		KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						 KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Command.CLOSE, this);


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Handle window closing
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent event)
			{
				onClose();
			}
		});

		// Prevent dialog from being resized
		setResizable(false);

		// Resize dialog to its preferred size
		pack();

		// Set location of dialog box
		if (location == null)
			location = GuiUtils.getComponentLocation(this, owner);
		setLocation(location);

		// Set default button
		getRootPane().setDefaultButton(okButton);

		// Show dialog
		setVisible(true);

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static boolean showDialog(Component parent)
	{
		return new PreferencesDialog(GuiUtils.getWindow(parent)).accepted;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.CHOOSE_PLOT_COLOUR_FOCUSED_BORDER))
			onChoosePlotColourFocusedBorder();

		else if (command.equals(Command.CHOOSE_PLOT_COLOUR_IMAGE_MARGIN))
			onChoosePlotColourImageMargin();

		else if (command.equals(Command.CHOOSE_PLOT_COLOUR_BACKGROUND))
			onChoosePlotColourBackground();

		else if (command.equals(Command.CHOOSE_PLOT_COLOUR_GRID))
			onChoosePlotColourGrid();

		else if (command.equals(Command.CHOOSE_PLOT_COLOUR_AXIS))
			onChoosePlotColourAxis();

		else if (command.equals(Command.CHOOSE_PLOT_COLOUR_SCALE))
			onChoosePlotColourScale();

		else if (command.equals(Command.CHOOSE_FUNCTION_DIRECTORY))
			onChooseFunctionDirectory();

		else if (command.equals(Command.TOGGLE_FUNCTION_OBSCURED_COLOUR))
			onToggleFunctionObscuredColour();

		else if (command.equals(Command.CHOOSE_FUNCTION_OBSCURED_COLOUR))
			onChooseFunctionObscuredColour();

		else if (command.startsWith(Command.CHOOSE_FUNCTION_COLOUR))
			onChooseFunctionColour(StringUtils.removePrefix(command,
															Command.CHOOSE_FUNCTION_COLOUR));

		else if (command.equals(Command.SAVE_CONFIGURATION))
			onSaveConfiguration();

		else if (command.equals(Command.ACCEPT))
			onAccept();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private void validatePreferences()
		throws AppException
	{
		for (Tab tab : Tab.values())
			tab.validatePreferences(this);
	}

	//------------------------------------------------------------------

	private void setPreferences()
	{
		for (Tab tab : Tab.values())
			tab.setPreferences(this);
	}

	//------------------------------------------------------------------

	private void onChoosePlotColourFocusedBorder()
	{
		Color colour = JColorChooser.showDialog(this, PLOT_COLOUR_STR + FOCUSED_BORDER_TITLE_STR,
												plotColourFocusedBorderButton.getForeground());
		if (colour != null)
			plotColourFocusedBorderButton.setForeground(colour);
	}

	//------------------------------------------------------------------

	private void onChoosePlotColourImageMargin()
	{
		Color colour = JColorChooser.showDialog(this, PLOT_COLOUR_STR + IMAGE_MARGIN_TITLE_STR,
												plotColourImageMarginButton.getForeground());
		if (colour != null)
			plotColourImageMarginButton.setForeground(colour);
	}

	//------------------------------------------------------------------

	private void onChoosePlotColourBackground()
	{
		Color colour = JColorChooser.showDialog(this, PLOT_COLOUR_STR + BACKGROUND_TITLE_STR,
												plotColourBackgroundButton.getForeground());
		if (colour != null)
			plotColourBackgroundButton.setForeground(colour);
	}

	//------------------------------------------------------------------

	private void onChoosePlotColourGrid()
	{
		Color colour = JColorChooser.showDialog(this, PLOT_COLOUR_STR + GRID_TITLE_STR,
												plotColourGridButton.getForeground());
		if (colour != null)
			plotColourGridButton.setForeground(colour);
	}

	//------------------------------------------------------------------

	private void onChoosePlotColourAxis()
	{
		Color colour = JColorChooser.showDialog(this, PLOT_COLOUR_STR + AXIS_TITLE_STR,
												plotColourAxisButton.getForeground());
		if (colour != null)
			plotColourAxisButton.setForeground(colour);
	}

	//------------------------------------------------------------------

	private void onChoosePlotColourScale()
	{
		Color colour = JColorChooser.showDialog(this, PLOT_COLOUR_STR + SCALE_MARKINGS_TITLE_STR,
												plotColourScaleMarkingsButton.getForeground());
		if (colour != null)
			plotColourScaleMarkingsButton.setForeground(colour);
	}

	//------------------------------------------------------------------

	private void onChooseFunctionDirectory()
	{
		if (directoryFileChooser == null)
		{
			directoryFileChooser = new JFileChooser();
			directoryFileChooser.setDialogTitle(DIRECTORY_TITLE_STR);
			directoryFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			directoryFileChooser.setApproveButtonMnemonic(KeyEvent.VK_S);
			directoryFileChooser.setApproveButtonToolTipText(SELECT_DIRECTORY_STR);
		}
		directoryFileChooser.setCurrentDirectory(directoryField.getCanonicalFile());
		directoryFileChooser.rescanCurrentDirectory();
		if (directoryFileChooser.showDialog(this, SELECT_STR) == JFileChooser.APPROVE_OPTION)
			directoryField.setFile(directoryFileChooser.getSelectedFile());
	}

	//------------------------------------------------------------------

	private void onToggleFunctionObscuredColour()
	{
		functionObscuredColourButton.setEnabled(functionObscuredColourCheckBox.isSelected());
	}

	//------------------------------------------------------------------

	private void onChooseFunctionObscuredColour()
	{
		Color colour = JColorChooser.showDialog(this, OBSCURED_COLOUR_TITLE_STR,
												functionObscuredColourButton.getForeground());
		if (colour != null)
			functionObscuredColourButton.setForeground(colour);
	}

	//------------------------------------------------------------------

	private void onChooseFunctionColour(String str)
	{
		int index = Integer.parseInt(str);
		Color colour = JColorChooser.showDialog(this, FUNCTION_COLOUR_TITLE_STR + (index + 1),
												functionColourButtons[index].getForeground());
		if (colour != null)
			functionColourButtons[index].setForeground(colour);
	}

	//------------------------------------------------------------------

	private void onSaveConfiguration()
	{
		try
		{
			validatePreferences();

			File file = AppConfig.INSTANCE.chooseFile(this);
			if (file != null)
			{
				String[] optionStrs = Utils.getOptionStrings(AppConstants.REPLACE_STR);
				if (!file.exists() ||
					 (JOptionPane.showOptionDialog(this, Utils.getPathname(file) +
																			AppConstants.ALREADY_EXISTS_STR,
												   SAVE_CONFIG_FILE_STR, JOptionPane.OK_CANCEL_OPTION,
												   JOptionPane.WARNING_MESSAGE, null, optionStrs,
												   optionStrs[1]) == JOptionPane.OK_OPTION))
				{
					setPreferences();
					accepted = true;
					TaskProgressDialog.showDialog(this, WRITE_CONFIG_FILE_STR,
												  new Task.WriteConfig(file));
				}
			}
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, App.SHORT_NAME, JOptionPane.ERROR_MESSAGE);
		}
		if (accepted)
			onClose();
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		try
		{
			validatePreferences();
			setPreferences();
			accepted = true;
			onClose();
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, App.SHORT_NAME, JOptionPane.ERROR_MESSAGE);
		}
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		FPathnameField.removeObservers(KEY);

		location = getLocation();
		tabIndex = tabbedPanel.getSelectedIndex();
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

	private JPanel createPanelGeneral()
	{

		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		AppConfig config = AppConfig.INSTANCE;

		// Label: default file kind
		JLabel defaultFileKindLabel = new FLabel(DEFAULT_FILE_KIND_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(defaultFileKindLabel, gbc);
		controlPanel.add(defaultFileKindLabel);

		// Combo box: default file kind
		defaultFileKindComboBox = new FComboBox<>(FileKind.values());
		defaultFileKindComboBox.setSelectedValue(config.getDefaultFileKind());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(defaultFileKindComboBox, gbc);
		controlPanel.add(defaultFileKindComboBox);

		// Label: new document on startup
		JLabel newDocOnStartupLabel = new FLabel(NEW_DOCUMENT_ON_STARTUP_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(newDocOnStartupLabel, gbc);
		controlPanel.add(newDocOnStartupLabel);

		// Combo box: new document on startup
		newDocOnStartupComboBox = new BooleanComboBox(config.isNewDocumentOnStartup());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(newDocOnStartupComboBox, gbc);
		controlPanel.add(newDocOnStartupComboBox);

		// Label: save function colours
		JLabel saveFunctionColoursLabel = new FLabel(SAVE_FUNCTION_COLOURS_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(saveFunctionColoursLabel, gbc);
		controlPanel.add(saveFunctionColoursLabel);

		// Combo box: save function colours
		saveFunctionColoursComboBox = new FComboBox<>(NoYesAsk.values());
		saveFunctionColoursComboBox.setSelectedValue(config.getSaveFunctionColours());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(saveFunctionColoursComboBox, gbc);
		controlPanel.add(saveFunctionColoursComboBox);

		// Label: show UNIX pathnames
		JLabel showUnixPathnamesLabel = new FLabel(SHOW_UNIX_PATHNAMES_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(showUnixPathnamesLabel, gbc);
		controlPanel.add(showUnixPathnamesLabel);

		// Combo box: show UNIX pathnames
		showUnixPathnamesComboBox = new BooleanComboBox(config.isShowUnixPathnames());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(showUnixPathnamesComboBox, gbc);
		controlPanel.add(showUnixPathnamesComboBox);

		// Label: select text on focus gained
		JLabel selectTextOnFocusGainedLabel = new FLabel(SELECT_TEXT_ON_FOCUS_GAINED_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(selectTextOnFocusGainedLabel, gbc);
		controlPanel.add(selectTextOnFocusGainedLabel);

		// Combo box: select text on focus gained
		selectTextOnFocusGainedComboBox = new BooleanComboBox(config.isSelectTextOnFocusGained());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(selectTextOnFocusGainedComboBox, gbc);
		controlPanel.add(selectTextOnFocusGainedComboBox);

		// Label: save main window location
		JLabel saveMainWindowLocationLabel = new FLabel(SAVE_MAIN_WINDOW_LOCATION_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(saveMainWindowLocationLabel, gbc);
		controlPanel.add(saveMainWindowLocationLabel);

		// Combo box: save main window location
		saveMainWindowLocationComboBox = new BooleanComboBox(config.isMainWindowLocation());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(saveMainWindowLocationComboBox, gbc);
		controlPanel.add(saveMainWindowLocationComboBox);

		// Label: maximum edit list length
		JLabel maxEditListLengthLabel = new FLabel(MAX_EDIT_HISTORY_SIZE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(maxEditListLengthLabel, gbc);
		controlPanel.add(maxEditListLengthLabel);

		// Spinner: maximum edit list length
		maxEditListLengthSpinner = new FIntegerSpinner(config.getMaxEditListLength(),
													   FunctionDocument.MIN_MAX_EDIT_LIST_LENGTH,
													   FunctionDocument.MAX_MAX_EDIT_LIST_LENGTH,
													   MAX_EDIT_LIST_LENGTH_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(maxEditListLengthSpinner, gbc);
		controlPanel.add(maxEditListLengthSpinner);

		// Label: clear edit list on save
		JLabel clearEditListOnSaveLabel = new FLabel(CLEAR_EDIT_HISTORY_ON_SAVE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(clearEditListOnSaveLabel, gbc);
		controlPanel.add(clearEditListOnSaveLabel);

		// Combo box: clear edit list on save
		clearEditListOnSaveComboBox = new BooleanComboBox(config.isClearEditListOnSave());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(clearEditListOnSaveComboBox, gbc);
		controlPanel.add(clearEditListOnSaveComboBox);


		//----  Outer panel

		JPanel outerPanel = new JPanel(gridBag);
		outerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		outerPanel.add(controlPanel);

		return outerPanel;

	}

	//------------------------------------------------------------------

	private JPanel createPanelAppearance()
	{

		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		AppConfig config = AppConfig.INSTANCE;

		// Label: look-and-feel
		JLabel lookAndFeelLabel = new FLabel(LOOK_AND_FEEL_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(lookAndFeelLabel, gbc);
		controlPanel.add(lookAndFeelLabel);

		// Combo box: look-and-feel
		lookAndFeelComboBox = new FComboBox<>();

		UIManager.LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels();
		if (lookAndFeelInfos.length == 0)
		{
			lookAndFeelComboBox.addItem(NO_LOOK_AND_FEELS_STR);
			lookAndFeelComboBox.setSelectedIndex(0);
			lookAndFeelComboBox.setEnabled(false);
		}
		else
		{
			String[] lookAndFeelNames = new String[lookAndFeelInfos.length];
			for (int i = 0; i < lookAndFeelInfos.length; i++)
			{
				lookAndFeelNames[i] = lookAndFeelInfos[i].getName();
				lookAndFeelComboBox.addItem(lookAndFeelNames[i]);
			}
			lookAndFeelComboBox.setSelectedValue(config.getLookAndFeel());
		}

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(lookAndFeelComboBox, gbc);
		controlPanel.add(lookAndFeelComboBox);

		// Label: text antialiasing
		JLabel textAntialiasingLabel = new FLabel(TEXT_ANTIALIASING_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(textAntialiasingLabel, gbc);
		controlPanel.add(textAntialiasingLabel);

		// Combo box: text antialiasing
		textAntialiasingComboBox = new FComboBox<>(TextRendering.Antialiasing.values());
		textAntialiasingComboBox.setSelectedValue(config.getTextAntialiasing());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(textAntialiasingComboBox, gbc);
		controlPanel.add(textAntialiasingComboBox);


		//----  Outer panel

		JPanel outerPanel = new JPanel(gridBag);
		outerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		outerPanel.add(controlPanel);

		return outerPanel;

	}

	//------------------------------------------------------------------

	private JPanel createPanelPlot()
	{

		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		AppConfig config = AppConfig.INSTANCE;

		// Label: plot size
		JLabel plotSizeLabel = new FLabel(PLOT_SIZE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(plotSizeLabel, gbc);
		controlPanel.add(plotSizeLabel);

		// Panel: plot size
		plotSizePanel = new DimensionsSpinnerPanel(config.getPlotSize().width, PlotPanel.MIN_PLOT_WIDTH,
												   PlotPanel.MAX_PLOT_WIDTH, PLOT_SIZE_FIELD_LENGTH,
												   config.getPlotSize().height, PlotPanel.MIN_PLOT_HEIGHT,
												   PlotPanel.MAX_PLOT_HEIGHT, PLOT_SIZE_FIELD_LENGTH,
												   null);
		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(plotSizePanel, gbc);
		controlPanel.add(plotSizePanel);

		// Label: show grid
		JLabel showGridLabel = new FLabel(SHOW_GRID_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(showGridLabel, gbc);
		controlPanel.add(showGridLabel);

		// Combo box: show grid
		showGridComboBox = new BooleanComboBox(config.isShowGrid());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(showGridComboBox, gbc);
		controlPanel.add(showGridComboBox);

		// Label: number of fraction digits
		JLabel numFractionDigitsLabel = new FLabel(NUM_FRACTION_DIGITS_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numFractionDigitsLabel, gbc);
		controlPanel.add(numFractionDigitsLabel);

		// Spinner: number of fraction digits
		numFractionDigitsSpinner = new FIntegerSpinner(config.getNumFractionDigits(),
													   AppConfig.MIN_NUM_FRACTION_DIGITS,
													   AppConfig.MAX_NUM_FRACTION_DIGITS,
													   NUM_FRACTION_DIGITS_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numFractionDigitsSpinner, gbc);
		controlPanel.add(numFractionDigitsSpinner);

		// Label: number of y-scale digits
		JLabel numYScaleDigitsLabel = new FLabel(NUM_Y_SCALE_DIGITS_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numYScaleDigitsLabel, gbc);
		controlPanel.add(numYScaleDigitsLabel);

		// Spinner: number of y-scale digits
		numYScaleDigitsSpinner = new FIntegerSpinner(config.getNumYScaleDigits(),
													 PlotPanel.MIN_NUM_Y_SCALE_DIGITS,
													 PlotPanel.MAX_NUM_Y_SCALE_DIGITS,
													 NUM_Y_SCALE_DIGITS_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numYScaleDigitsSpinner, gbc);
		controlPanel.add(numYScaleDigitsSpinner);

		// Label: fixed-point exponent range
		JLabel fixedPointRangeLabel = new FLabel(FIXED_POINT_EXPONENT_RANGE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(fixedPointRangeLabel, gbc);
		controlPanel.add(fixedPointRangeLabel);

		// Panel: fixed-point range
		JPanel fixedPointRangePanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(fixedPointRangePanel, gbc);
		controlPanel.add(fixedPointRangePanel);

		// Spinner: fixed-point exponent lower bound
		fixedPointLowerBoundSpinner = new FIntegerSpinner(config.getFixedPointExponentRange().lowerBound,
														  AppConfig.MIN_FIXED_POINT_EXPONENT_BOUND,
														  AppConfig.MAX_FIXED_POINT_EXPONENT_BOUND,
														  FIXED_POINT_RANGE_FIELD_LENGTH, true);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(fixedPointLowerBoundSpinner, gbc);
		fixedPointRangePanel.add(fixedPointLowerBoundSpinner);

		// Label: to
		JLabel toLabel = new FLabel(TO_STR);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 4, 0, 0);
		gridBag.setConstraints(toLabel, gbc);
		fixedPointRangePanel.add(toLabel);

		// Spinner: fixed-point exponent upper bound
		fixedPointUpperBoundSpinner = new FIntegerSpinner(config.getFixedPointExponentRange().upperBound,
														  AppConfig.MIN_FIXED_POINT_EXPONENT_BOUND,
														  AppConfig.MAX_FIXED_POINT_EXPONENT_BOUND,
														  FIXED_POINT_RANGE_FIELD_LENGTH, true);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 4, 0, 0);
		gridBag.setConstraints(fixedPointUpperBoundSpinner, gbc);
		fixedPointRangePanel.add(fixedPointUpperBoundSpinner);

		// Label: normalise scientific notation
		JLabel normaliseSciNotationLabel = new FLabel(NORMALISE_SCI_NOTATION_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(normaliseSciNotationLabel, gbc);
		controlPanel.add(normaliseSciNotationLabel);

		// Combo box: normalise scientific notation
		normaliseSciNotationComboBox = new BooleanComboBox(config.isNormaliseScientificNotation());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(normaliseSciNotationComboBox, gbc);
		controlPanel.add(normaliseSciNotationComboBox);

		// Label: truncate x-scale text
		JLabel truncateXScaleTextLabel = new FLabel(TRUNCATE_X_SCALE_TEXT_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(truncateXScaleTextLabel, gbc);
		controlPanel.add(truncateXScaleTextLabel);

		// Combo box: truncate x-scale text
		truncateXScaleTextComboBox = new BooleanComboBox(config.isTruncateXScaleText());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(truncateXScaleTextComboBox, gbc);
		controlPanel.add(truncateXScaleTextComboBox);


		//----  Plot colours panel

		JPanel plotColoursPanel = new JPanel(gridBag);
		TitledBorder.setPaddedBorder(plotColoursPanel, PLOT_COLOURS_STR);

		// Plot colours panel A
		JPanel plotColoursPanelA = new JPanel(gridBag);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(plotColoursPanelA, gbc);
		plotColoursPanel.add(plotColoursPanelA);

		gridY = 0;

		// Label: focused border colour
		JLabel focusedBorderColourLabel = new FLabel(FOCUSED_BORDER_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(focusedBorderColourLabel, gbc);
		plotColoursPanelA.add(focusedBorderColourLabel);

		// Button: focused border colour
		plotColourFocusedBorderButton = new ColourButton(config.getPlotColourFocusedBorder());
		plotColourFocusedBorderButton.setActionCommand(Command.CHOOSE_PLOT_COLOUR_FOCUSED_BORDER);
		plotColourFocusedBorderButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(plotColourFocusedBorderButton, gbc);
		plotColoursPanelA.add(plotColourFocusedBorderButton);

		// Label: image margin colour
		JLabel imageMarginColourLabel = new FLabel(IMAGE_MARGIN_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(imageMarginColourLabel, gbc);
		plotColoursPanelA.add(imageMarginColourLabel);

		// Button: image margin colour
		plotColourImageMarginButton = new ColourButton(config.getPlotColourImageMargin());
		plotColourImageMarginButton.setActionCommand(Command.CHOOSE_PLOT_COLOUR_IMAGE_MARGIN);
		plotColourImageMarginButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(plotColourImageMarginButton, gbc);
		plotColoursPanelA.add(plotColourImageMarginButton);

		// Label: background colour
		JLabel backgroundColourLabel = new FLabel(BACKGROUND_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(backgroundColourLabel, gbc);
		plotColoursPanelA.add(backgroundColourLabel);

		// Button: background colour
		plotColourBackgroundButton = new ColourButton(config.getPlotColourBackground());
		plotColourBackgroundButton.setActionCommand(Command.CHOOSE_PLOT_COLOUR_BACKGROUND);
		plotColourBackgroundButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(plotColourBackgroundButton, gbc);
		plotColoursPanelA.add(plotColourBackgroundButton);

		// Plot colours panel B
		JPanel plotColoursPanelB = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 8, 0, 0);
		gridBag.setConstraints(plotColoursPanelB, gbc);
		plotColoursPanel.add(plotColoursPanelB);

		gridY = 0;

		// Label: grid colour
		JLabel gridColourLabel = new FLabel(GRID_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridColourLabel, gbc);
		plotColoursPanelB.add(gridColourLabel);

		// Button: grid colour
		plotColourGridButton = new ColourButton(config.getPlotColourGrid());
		plotColourGridButton.setActionCommand(Command.CHOOSE_PLOT_COLOUR_GRID);
		plotColourGridButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(plotColourGridButton, gbc);
		plotColoursPanelB.add(plotColourGridButton);

		// Label: axis colour
		JLabel axisColourLabel = new FLabel(AXIS_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(axisColourLabel, gbc);
		plotColoursPanelB.add(axisColourLabel);

		// Button: axis colour
		plotColourAxisButton = new ColourButton(config.getPlotColourAxis());
		plotColourAxisButton.setActionCommand(Command.CHOOSE_PLOT_COLOUR_AXIS);
		plotColourAxisButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(plotColourAxisButton, gbc);
		plotColoursPanelB.add(plotColourAxisButton);

		// Label: scale markings
		JLabel scaleMarkingsColourLabel = new FLabel(SCALE_MARKINGS_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(scaleMarkingsColourLabel, gbc);
		plotColoursPanelB.add(scaleMarkingsColourLabel);

		// Button: scale markings colour
		plotColourScaleMarkingsButton = new ColourButton(config.getPlotColourScale());
		plotColourScaleMarkingsButton.setActionCommand(Command.CHOOSE_PLOT_COLOUR_SCALE);
		plotColourScaleMarkingsButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(plotColourScaleMarkingsButton, gbc);
		plotColoursPanelB.add(plotColourScaleMarkingsButton);


		//----  Outer panel

		JPanel outerPanel = new JPanel(gridBag);
		outerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		outerPanel.add(controlPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(plotColoursPanel, gbc);
		outerPanel.add(plotColoursPanel);

		return outerPanel;

	}

	//------------------------------------------------------------------

	private JPanel createPanelFunction()
	{

		//----  Top panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel topPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(topPanel);

		int gridY = 0;

		AppConfig config = AppConfig.INSTANCE;

		// Label: directory
		JLabel directoryLabel = new FLabel(DOCUMENT_DIRECTORY_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(directoryLabel, gbc);
		topPanel.add(directoryLabel);

		// Panel: directory
		directoryField = new FPathnameField(config.getFunctionDirectory());
		FPathnameField.addObserver(KEY, directoryField);
		JPanel directoryPanel = new PathnamePanel(directoryField, Command.CHOOSE_FUNCTION_DIRECTORY,
												  this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(directoryPanel, gbc);
		topPanel.add(directoryPanel);

		// Label: list size
		JLabel listSizeLabel = new FLabel(LIST_SIZE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(listSizeLabel, gbc);
		topPanel.add(listSizeLabel);

		// Panel: list size
		listSizePanel = new DimensionsSpinnerPanel(config.getFunctionListSize().width,
												   FunctionView.MIN_FUNCTION_LIST_NUM_COLUMNS,
												   FunctionView.MAX_FUNCTION_LIST_NUM_COLUMNS,
												   LIST_WIDTH_FIELD_LENGTH,
												   config.getFunctionListSize().height,
												   FunctionView.MIN_FUNCTION_LIST_NUM_ROWS,
												   FunctionView.MAX_FUNCTION_LIST_NUM_ROWS,
												   LIST_HEIGHT_FIELD_LENGTH,
												   new String[]{ COLUMNS_STR, ROWS_STR });

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(listSizePanel, gbc);
		topPanel.add(listSizePanel);

		// Check box: obscured colour
		functionObscuredColourCheckBox = new FCheckBox(OBSCURED_COLOUR_STR);
		functionObscuredColourCheckBox.setSelected(config.isFunctionObscuredColour());
		functionObscuredColourCheckBox.setActionCommand(Command.TOGGLE_FUNCTION_OBSCURED_COLOUR);
		functionObscuredColourCheckBox.addActionListener(this);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(functionObscuredColourCheckBox, gbc);
		topPanel.add(functionObscuredColourCheckBox);

		// Button: obscured colour
		functionObscuredColourButton = new ColourButton(config.getFunctionObscuredColour());
		functionObscuredColourButton.setEnabled(functionObscuredColourCheckBox.isSelected());
		functionObscuredColourButton.setActionCommand(Command.CHOOSE_FUNCTION_OBSCURED_COLOUR);
		functionObscuredColourButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(functionObscuredColourButton, gbc);
		topPanel.add(functionObscuredColourButton);


		//----  Function colours panel

		int numColours = FunctionDocument.MAX_NUM_FUNCTIONS;
		JPanel functionColoursPanel = new JPanel(gridBag);
		TitledBorder.setPaddedBorder(functionColoursPanel, FUNCTION_COLOURS_STR);

		JPanel functionColoursInnerPanel =
							new JPanel(new GridLayout(NUM_FUNCTION_COLOUR_ROWS,
													  numColours / NUM_FUNCTION_COLOUR_ROWS, 10, 3));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(functionColoursInnerPanel, gbc);
		functionColoursPanel.add(functionColoursInnerPanel);

		// Buttons: function colour
		ColourButtonPanel[] colourButtonPanels = new ColourButtonPanel[numColours];
		functionColourButtons = new JButton[numColours];
		for (int i = 0; i < numColours; i++)
		{
			functionColourButtons[i] = new ColourButton(config.getFunctionColour(i));
			functionColourButtons[i].setActionCommand(Command.CHOOSE_FUNCTION_COLOUR + i);
			functionColourButtons[i].addActionListener(this);

			colourButtonPanels[i] = new ColourButtonPanel(i + 1, functionColourButtons[i]);
			functionColoursInnerPanel.add(colourButtonPanels[i]);
		}


		//----  Outer panel

		JPanel outerPanel = new JPanel(gridBag);
		outerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(topPanel, gbc);
		outerPanel.add(topPanel);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(functionColoursPanel, gbc);
		outerPanel.add(functionColoursPanel);

		return outerPanel;

	}

	//------------------------------------------------------------------

	private JPanel createPanelFonts()
	{

		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		fontPanels = new FontPanel[AppFont.getNumFonts()];
		for (int i = 0; i < fontPanels.length; i++)
		{
			FontEx fontEx = AppConfig.INSTANCE.getFont(i);
			fontPanels[i] = new FontPanel(fontEx, fontNames);

			int gridX = 0;

			// Label: font
			JLabel fontLabel = new FLabel(AppFont.values()[i].toString());

			gbc.gridx = gridX++;
			gbc.gridy = i;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_END;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(fontLabel, gbc);
			controlPanel.add(fontLabel);

			// Combo box: font name
			gbc.gridx = gridX++;
			gbc.gridy = i;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(fontPanels[i].nameComboBox, gbc);
			controlPanel.add(fontPanels[i].nameComboBox);

			// Combo box: font style
			gbc.gridx = gridX++;
			gbc.gridy = i;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(fontPanels[i].styleComboBox, gbc);
			controlPanel.add(fontPanels[i].styleComboBox);

			// Panel: font size
			JPanel sizePanel = new JPanel(gridBag);

			gbc.gridx = gridX++;
			gbc.gridy = i;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(sizePanel, gbc);
			controlPanel.add(sizePanel);

			// Spinner: font size
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(fontPanels[i].sizeSpinner, gbc);
			sizePanel.add(fontPanels[i].sizeSpinner);

			// Label: "pt"
			JLabel ptLabel = new FLabel(PT_STR);

			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 4, 0, 0);
			gridBag.setConstraints(ptLabel, gbc);
			sizePanel.add(ptLabel);
		}


		//----  Outer panel

		JPanel outerPanel = new JPanel(gridBag);
		outerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		outerPanel.add(controlPanel);

		return outerPanel;

	}

	//------------------------------------------------------------------

	private void setFocus(Tab        tab,
						  JComponent component)
	{
		tabbedPanel.setSelectedIndex(tab.ordinal());
		GuiUtils.setFocus(component);
	}

	//------------------------------------------------------------------

	private void validatePreferencesGeneral()
	{
		// do nothing
	}

	//------------------------------------------------------------------

	private void validatePreferencesAppearance()
	{
		// do nothing
	}

	//------------------------------------------------------------------

	private void validatePreferencesPlot()
		throws AppException
	{
		// Fixed-point range
		try
		{
			if (fixedPointLowerBoundSpinner.getIntValue() > fixedPointUpperBoundSpinner.getIntValue())
				throw new AppException(ErrorId.FIXED_POINT_BOUNDS_OUT_OF_ORDER);
		}
		catch (AppException e)
		{
			setFocus(Tab.PLOT, fixedPointUpperBoundSpinner);
			throw e;
		}
	}

	//------------------------------------------------------------------

	private void validateDirectory(File directory)
		throws AppException
	{
		try
		{
			if (!directory.exists())
				throw new FileException(ErrorId.DIRECTORY_DOES_NOT_EXIST, directory);
			if (!directory.isDirectory())
				throw new FileException(ErrorId.NOT_A_DIRECTORY, directory);
		}
		catch (SecurityException e)
		{
			throw new FileException(ErrorId.DIRECTORY_ACCESS_NOT_PERMITTED, directory, e);
		}
	}

	//------------------------------------------------------------------

	private void validatePreferencesFunction()
		throws AppException
	{
		// Directory
		try
		{
			if (!directoryField.isEmpty())
				validateDirectory(directoryField.getFile());
		}
		catch (AppException e)
		{
			setFocus(Tab.FUNCTION, directoryField);
			throw e;
		}
	}

	//------------------------------------------------------------------

	private void validatePreferencesFonts()
	{
		// do nothing
	}

	//------------------------------------------------------------------

	private void setPreferencesGeneral()
	{
		AppConfig config = AppConfig.INSTANCE;
		config.setDefaultFileKind(defaultFileKindComboBox.getSelectedValue());
		config.setNewDocumentOnStartup(newDocOnStartupComboBox.getSelectedValue());
		config.setSaveFunctionColours(saveFunctionColoursComboBox.getSelectedValue());
		config.setShowUnixPathnames(showUnixPathnamesComboBox.getSelectedValue());
		config.setSelectTextOnFocusGained(selectTextOnFocusGainedComboBox.getSelectedValue());
		if (saveMainWindowLocationComboBox.getSelectedValue() != config.isMainWindowLocation())
			config.setMainWindowLocation(saveMainWindowLocationComboBox.getSelectedValue() ? new Point()
																						   : null);
		config.setMaxEditListLength(maxEditListLengthSpinner.getIntValue());
		config.setClearEditListOnSave(clearEditListOnSaveComboBox.getSelectedValue());
	}

	//------------------------------------------------------------------

	private void setPreferencesAppearance()
	{
		AppConfig config = AppConfig.INSTANCE;
		if (lookAndFeelComboBox.isEnabled() && (lookAndFeelComboBox.getSelectedIndex() >= 0))
			config.setLookAndFeel(lookAndFeelComboBox.getSelectedValue());
		config.setTextAntialiasing(textAntialiasingComboBox.getSelectedValue());
	}

	//------------------------------------------------------------------

	private void setPreferencesPlot()
	{
		AppConfig config = AppConfig.INSTANCE;
		config.setPlotSize(plotSizePanel.getDimensions());
		config.setShowGrid(showGridComboBox.getSelectedValue());
		config.setNumFractionDigits(numFractionDigitsSpinner.getIntValue());
		config.setNumYScaleDigits(numYScaleDigitsSpinner.getIntValue());
		config.setFixedPointExponentRange(new IntegerRange(fixedPointLowerBoundSpinner.getIntValue(),
														   fixedPointUpperBoundSpinner.getIntValue()));
		config.setNormaliseScientificNotation(normaliseSciNotationComboBox.getSelectedValue());
		config.setTruncateXScaleText(truncateXScaleTextComboBox.getSelectedValue());
		config.setPlotColourFocusedBorder(plotColourFocusedBorderButton.getForeground());
		config.setPlotColourImageMargin(plotColourImageMarginButton.getForeground());
		config.setPlotColourBackground(plotColourBackgroundButton.getForeground());
		config.setPlotColourGrid(plotColourGridButton.getForeground());
		config.setPlotColourAxis(plotColourAxisButton.getForeground());
		config.setPlotColourScale(plotColourScaleMarkingsButton.getForeground());
	}

	//------------------------------------------------------------------

	private void setPreferencesFunction()
	{
		AppConfig config = AppConfig.INSTANCE;
		config.setFunctionPathname(directoryField.getText());
		config.setFunctionListSize(listSizePanel.getDimensions());
		config.setFunctionObscuredColour(functionObscuredColourCheckBox.isSelected()
															? functionObscuredColourButton.getForeground()
															: null);
		for (int i = 0; i < functionColourButtons.length; i++)
			config.setFunctionColour(i, functionColourButtons[i].getForeground());
	}

	//------------------------------------------------------------------

	private void setPreferencesFonts()
	{
		for (int i = 0; i < fontPanels.length; i++)
		{
			if (fontPanels[i].nameComboBox.getSelectedIndex() >= 0)
				AppConfig.INSTANCE.setFont(i, fontPanels[i].getFont());
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class fields
////////////////////////////////////////////////////////////////////////

	private static	Point	location;
	private static	int		tabIndex;

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	// Main panel
	private	boolean									accepted;
	private	JTabbedPane								tabbedPanel;

	// General panel
	private	FComboBox<FileKind>						defaultFileKindComboBox;
	private	BooleanComboBox							newDocOnStartupComboBox;
	private	FComboBox<NoYesAsk>						saveFunctionColoursComboBox;
	private	BooleanComboBox							showUnixPathnamesComboBox;
	private	BooleanComboBox							selectTextOnFocusGainedComboBox;
	private	BooleanComboBox							saveMainWindowLocationComboBox;
	private	FIntegerSpinner							maxEditListLengthSpinner;
	private	BooleanComboBox							clearEditListOnSaveComboBox;

	// Appearance panel
	private	FComboBox<String>						lookAndFeelComboBox;
	private	FComboBox<TextRendering.Antialiasing>	textAntialiasingComboBox;

	// Plot panel
	private	DimensionsSpinnerPanel					plotSizePanel;
	private	BooleanComboBox							showGridComboBox;
	private	FIntegerSpinner							numFractionDigitsSpinner;
	private	FIntegerSpinner							numYScaleDigitsSpinner;
	private	FIntegerSpinner							fixedPointLowerBoundSpinner;
	private	FIntegerSpinner							fixedPointUpperBoundSpinner;
	private	BooleanComboBox							normaliseSciNotationComboBox;
	private	BooleanComboBox							truncateXScaleTextComboBox;
	private	JButton									plotColourFocusedBorderButton;
	private	JButton									plotColourImageMarginButton;
	private	JButton									plotColourBackgroundButton;
	private	JButton									plotColourGridButton;
	private	JButton									plotColourAxisButton;
	private	JButton									plotColourScaleMarkingsButton;

	// Function panel
	private	FPathnameField							directoryField;
	private	JFileChooser							directoryFileChooser;
	private	DimensionsSpinnerPanel					listSizePanel;
	private	JCheckBox								functionObscuredColourCheckBox;
	private	JButton									functionObscuredColourButton;
	private	JButton[]								functionColourButtons;

	// Fonts panel
	private	FontPanel[]								fontPanels;

}

//----------------------------------------------------------------------
