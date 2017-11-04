/*====================================================================*\

FunctionDialog.java

Function dialog box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.gui.ColourSampleIcon;
import uk.blankaspect.common.gui.FButton;
import uk.blankaspect.common.gui.FLabel;
import uk.blankaspect.common.gui.GuiUtils;

import uk.blankaspect.common.misc.KeyAction;
import uk.blankaspect.common.misc.StringUtils;

//----------------------------------------------------------------------


// FUNCTION DIALOG BOX CLASS


class FunctionDialog
	extends JDialog
	implements ActionListener, FlavorListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		COLOUR_BUTTON_ICON_WIDTH	= 40;
	private static final	int		COLOUR_BUTTON_ICON_HEIGHT	= 16;
	private static final	Insets	COLOUR_BUTTON_MARGINS		= new Insets(2, 2, 2, 2);

	private static final	int	EXPRESSION_FIELD_LENGTH	= 512;

	private static final	String	COLOUR_STR			= "Colour";
	private static final	String	EXPRESSION_STR		= "Expression";
	private static final	String	COPY_STR			= "Copy";
	private static final	String	PASTE_STR			= "Paste";
	private static final	String	CLEAR_STR			= "Clear";
	private static final	String	COLOUR_TITLE_STR	= "Colour of plotted function";
	private static final	String	COPY_TOOLTIP_STR	= "Copy expression to clipboard";
	private static final	String	PASTE_TOOLTIP_STR	= "Replace expression with text from clipboard";
	private static final	String	CLEAR_TOOLTIP_STR	= "Clear expression (Ctrl+Delete)";
	private static final	String	SYNTAX_ERROR_STR	= "Syntax error";

	// Commands
	private interface Command
	{
		String	CHOOSE_COLOUR			= "chooseColour";
		String	SET_TO_FUNCTION_COLOUR	= "setToFunctionColour";
		String	COPY					= "copy";
		String	PASTE					= "paste";
		String	CLEAR					= "clear";
		String	ACCEPT					= "accept";
		String	CLOSE					= "close";
	}

	private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
	{
		new KeyAction.KeyCommandPair(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.CTRL_DOWN_MASK),
									 Command.CLEAR),
		new KeyAction.KeyCommandPair(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
									 Command.CLOSE)
	};

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

		private static final	int	WIDTH	= 18;
		private static final	int	HEIGHT	= 18;

		private static final	Color	BORDER_COLOUR			= Color.GRAY;
		private static final	Color	FOCUSED_BORDER_COLOUR1	= Color.BLACK;
		private static final	Color	FOCUSED_BORDER_COLOUR2	= Color.WHITE;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ColourButton(Color colour)
		{
			setBorder(null);
			setForeground(colour);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(WIDTH, HEIGHT);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			gr = gr.create();

			// Get dimensions
			int width = getWidth();
			int height = getHeight();

			// Fill interior
			gr.setColor(getForeground());
			gr.fillRect(2, 2, width - 4, height - 4);

			// Draw border
			gr.setColor(isFocusOwner() ? FOCUSED_BORDER_COLOUR2 : BORDER_COLOUR);
			gr.drawRect(1, 1, width - 3, height - 3);
			if (isFocusOwner())
			{
				((Graphics2D)gr).setStroke(GuiUtils.getBasicDash());
				gr.setColor(FOCUSED_BORDER_COLOUR1);
			}
			else
				gr.setColor(getBackground());
			gr.drawRect(0, 0, width - 1, height - 1);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// EXPRESSION FIELD CLASS


	private static class ExpressionField
		extends SurrogateMinus.Field
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	NUM_COLUMNS	= 80;

		private static final	String	VALID_CHARS	= " %()*+./\\^";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public ExpressionField(int maxLength)
		{
			super(maxLength, NUM_COLUMNS);
		}

		//--------------------------------------------------------------

		public ExpressionField(int    maxLength,
							   String text)
		{
			this(maxLength);
			setText(text);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected int getColumnWidth()
		{
			return GuiUtils.getCharWidth('0', getFontMetrics(getFont()));
		}

		//--------------------------------------------------------------

		@Override
		protected String translateInsertString(String str,
											   int    offset)
		{
			return super.translateInsertString(str, offset).toLowerCase();
		}

		//--------------------------------------------------------------

		@Override
		protected boolean acceptCharacter(char ch,
										  int  index)
		{
			return (((ch >= '0') && (ch <= '9')) || ((ch >= 'a') && (ch <= 'z')) ||
					 isMinusCharacter(ch) || (VALID_CHARS.indexOf(ch) >= 0));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public Expression getExpression()
			throws Expression.Exception
		{
			return new Expression(getText());
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private FunctionDialog(Window owner,
						   String titleStr,
						   Color  colour,
						   String expression)
	{

		// Call superclass constructor
		super(owner, titleStr, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(AppIcon.getAppIconImages());


		//----  Edit panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label: colour
		JLabel colourLabel = new FLabel(COLOUR_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(colourLabel, gbc);
		controlPanel.add(colourLabel);

		// Button: colour
		JPanel colourPanel = new JPanel(gridBag);

		colourButton = new JButton(new ColourSampleIcon(COLOUR_BUTTON_ICON_WIDTH,
														COLOUR_BUTTON_ICON_HEIGHT));
		colourButton.setMargin(COLOUR_BUTTON_MARGINS);
		colourButton.setForeground(colour);
		colourButton.setActionCommand(Command.CHOOSE_COLOUR);
		colourButton.addActionListener(this);

		colourLabel.setDisplayedMnemonic(KeyEvent.VK_L);
		colourLabel.setLabelFor(colourButton);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(colourButton, gbc);
		colourPanel.add(colourButton);

		// Buttons: function colour
		int numFunctions = 0;
		FunctionDocument document = App.INSTANCE.getDocument();
		if (document != null)
			numFunctions = document.getNumFunctions();
		if (numFunctions > 0)
		{
			JPanel functionColourPanel = new JPanel(new GridLayout(1, 0, 0, 0));
			functionColourButtons = new ColourButton[numFunctions];
			for (int i = 0; i < functionColourButtons.length; i++)
			{
				Function function = document.getFunction(i);
				functionColourButtons[i] = new ColourButton(function.getColour());
				functionColourButtons[i].setToolTipText(function.getExpression().toString());
				functionColourButtons[i].setActionCommand(Command.SET_TO_FUNCTION_COLOUR + i);
				functionColourButtons[i].addActionListener(this);
				functionColourPanel.add(functionColourButtons[i]);
			}

			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 6, 0, 0);
			gridBag.setConstraints(functionColourPanel, gbc);
			colourPanel.add(functionColourPanel);
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
		gridBag.setConstraints(colourPanel, gbc);
		controlPanel.add(colourPanel);

		// Label: expression
		JLabel expressionLabel = new FLabel(EXPRESSION_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(expressionLabel, gbc);
		controlPanel.add(expressionLabel);

		// Field: expression
		expressionField = new ExpressionField(EXPRESSION_FIELD_LENGTH, expression);

		expressionLabel.setDisplayedMnemonic(KeyEvent.VK_E);
		expressionLabel.setLabelFor(expressionField);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(expressionField, gbc);
		controlPanel.add(expressionField);


		//----  Button panel: copy, paste, clear

		JPanel editButtonPanel = new JPanel(new GridLayout(1, 0, 8, 0));

		// Button: copy
		JButton copyButton = new FButton(COPY_STR);
		copyButton.setMnemonic(KeyEvent.VK_C);
		copyButton.setToolTipText(COPY_TOOLTIP_STR);
		copyButton.setActionCommand(Command.COPY);
		copyButton.addActionListener(this);
		if (!AppConfig.INSTANCE.hasPermissionAccessClipboard())
			copyButton.setEnabled(false);
		editButtonPanel.add(copyButton);

		// Button: paste
		pasteButton = new FButton(PASTE_STR);
		pasteButton.setMnemonic(KeyEvent.VK_P);
		pasteButton.setToolTipText(PASTE_TOOLTIP_STR);
		pasteButton.setActionCommand(Command.PASTE);
		pasteButton.addActionListener(this);
		pasteButton.setEnabled(false);
		flavorsChanged(null);
		editButtonPanel.add(pasteButton);

		// Button: clear
		JButton clearButton = new FButton(CLEAR_STR);
		clearButton.setMnemonic(KeyEvent.VK_E);
		clearButton.setToolTipText(CLEAR_TOOLTIP_STR);
		clearButton.setActionCommand(Command.CLEAR);
		clearButton.addActionListener(this);
		editButtonPanel.add(clearButton);


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

		JPanel buttonPanel = new JPanel(gridBag);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 24, 3, 24));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 12);
		gridBag.setConstraints(editButtonPanel, gbc);
		buttonPanel.add(editButtonPanel);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 12, 0, 0);
		gridBag.setConstraints(okCancelButtonPanel, gbc);
		buttonPanel.add(okCancelButtonPanel);


		//----  Main panel

		JPanel mainPanel = new JPanel(gridBag);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

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
		mainPanel.add(controlPanel);

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
		KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Add clipboard flavor listener
		try
		{
			getToolkit().getSystemClipboard().addFlavorListener(this);
		}
		catch (Exception e)
		{
			// ignore
		}

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

		// Set focus
		expressionField.requestFocusInWindow();

		// Show dialog
		setVisible(true);

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static FunctionDialog showDialog(Component parent,
											String    titleStr,
											Color     colour,
											String    expression)
	{
		return new FunctionDialog(GuiUtils.getWindow(parent), titleStr, colour, expression);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.CHOOSE_COLOUR))
			onChooseColour();

		else if (command.startsWith(Command.SET_TO_FUNCTION_COLOUR))
			onSetToFunctionColour(StringUtils.removePrefix(command,
														   Command.SET_TO_FUNCTION_COLOUR));

		else if (command.equals(Command.COPY))
			onCopy();

		else if (command.equals(Command.PASTE))
			onPaste();

		else if (command.equals(Command.CLEAR))
			onClear();

		else if (command.equals(Command.ACCEPT))
			onAccept();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : FlavourListener interface
////////////////////////////////////////////////////////////////////////

	public void flavorsChanged(FlavorEvent event)
	{
		try
		{
			pasteButton.setEnabled(getToolkit().getSystemClipboard().
														isDataFlavorAvailable(DataFlavor.stringFlavor));
		}
		catch (Exception e)
		{
			// ignore
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public boolean isAccepted()
	{
		return accepted;
	}

	//------------------------------------------------------------------

	public Color getColour()
	{
		return colourButton.getForeground();
	}

	//------------------------------------------------------------------

	public Expression getExpression()
	{
		Expression expression = null;
		try
		{
			expression = expressionField.getExpression();
		}
		catch (Expression.Exception e)
		{
			e.printStackTrace();
		}
		return expression;
	}

	//------------------------------------------------------------------

	private void validateUserInput()
		throws AppException
	{
		try
		{
			expressionField.getExpression();
		}
		catch (Expression.Exception e)
		{
			expressionField.requestFocusInWindow();
			expressionField.setCaretPosition(e.getOffset());
			throw e;
		}
	}

	//------------------------------------------------------------------

	private void onChooseColour()
	{
		Color colour = JColorChooser.showDialog(this, COLOUR_TITLE_STR, colourButton.getForeground());
		if (colour != null)
			colourButton.setForeground(colour);
	}

	//------------------------------------------------------------------

	private void onSetToFunctionColour(String str)
	{
		colourButton.setForeground(functionColourButtons[Integer.parseInt(str)].getForeground());
	}

	//------------------------------------------------------------------

	private void onCopy()
	{
		try
		{
			Utils.putClipboardText(expressionField.getText());
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, App.SHORT_NAME, JOptionPane.ERROR_MESSAGE);
		}
	}

	//------------------------------------------------------------------

	private void onPaste()
	{
		try
		{
			expressionField.setText(Utils.getClipboardText());
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, App.SHORT_NAME, JOptionPane.ERROR_MESSAGE);
		}
	}

	//------------------------------------------------------------------

	private void onClear()
	{
		expressionField.setText(null);
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		try
		{
			validateUserInput();
			accepted = true;
			onClose();
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, App.SHORT_NAME + " : " + SYNTAX_ERROR_STR,
										  JOptionPane.ERROR_MESSAGE);
		}
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		try
		{
			getToolkit().getSystemClipboard().removeFlavorListener(this);
		}
		catch (Exception e)
		{
			// ignore
		}
		location = getLocation();
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class fields
////////////////////////////////////////////////////////////////////////

	private static	Point	location;

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	ExpressionField	expressionField;
	private	JButton			colourButton;
	private	ColourButton[]	functionColourButtons;
	private	JButton			pasteButton;
	private	boolean			accepted;

}

//----------------------------------------------------------------------
