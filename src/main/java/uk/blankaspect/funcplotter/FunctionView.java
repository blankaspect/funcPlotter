/*====================================================================*\

FunctionView.java

Function view class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import java.awt.geom.Point2D;

import java.awt.image.BufferedImage;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.colour.ColourUtils;

import uk.blankaspect.ui.swing.combobox.FComboBox;

import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.list.SingleSelectionList;

import uk.blankaspect.ui.swing.menu.FCheckBoxMenuItem;
import uk.blankaspect.ui.swing.menu.FMenu;
import uk.blankaspect.ui.swing.menu.FMenuItem;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.modifiers.InputModifiers;

import uk.blankaspect.ui.swing.text.TextRendering;

//----------------------------------------------------------------------


// FUNCTION VIEW CLASS


class FunctionView
	extends JPanel
	implements ActionListener, ChangeListener, ListSelectionListener, MouseListener, MouseMotionListener,
			   MouseWheelListener, SingleSelectionList.IModelListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int		MIN_FUNCTION_LIST_NUM_COLUMNS	= 8;
	public static final		int		MAX_FUNCTION_LIST_NUM_COLUMNS	= 80;

	public static final		int		MIN_FUNCTION_LIST_NUM_ROWS	= 1;
	public static final		int		MAX_FUNCTION_LIST_NUM_ROWS	= FunctionDocument.MAX_NUM_FUNCTIONS;

	private static final	int		INTERVAL_EP_NUM_COLUMNS	= 22;

	private static final	String	CURSOR_STR		= "Cursor";
	private static final	String	X_INTERVAL_STR	= "x";
	private static final	String	Y_INTERVAL_STR	= "y";
	private static final	String	TO_STR			= "to";
	private static final	String	X_ZOOM_STR		= "x zoom";
	private static final	String	Y_ZOOM_STR		= "y zoom";

	private static final	String[]	ZOOM_FACTOR_STRS	=
	{
		"1.1", "1.2", "1.25", "1.5", "2", "3", "4", "5", "10"
	};
	private static final	String	DEFAULT_ZOOM_FACTOR_STR	= "1.5";

	// Commands
	private interface Command
	{
		String	EDIT_FUNCTION			= "editFunction";
		String	DELETE_FUNCTION			= "deleteFunction";
		String	CONFIRM_DELETE_FUNCTION	= "confirmDeleteFunction";
		String	MOVE_FUNCTION_UP		= "moveFunctionUp";
		String	MOVE_FUNCTION_DOWN		= "moveFunctionDown";
		String	MOVE_FUNCTION			= "moveFunction";
		String	SET_X_INTERVAL			= "setXInterval";
		String	SET_Y_INTERVAL			= "setYInterval";
		String	SELECT_X_ZOOM_FACTOR	= "selectXZoomFactor";
		String	SELECT_Y_ZOOM_FACTOR	= "selectYZoomFactor";
		String	SHOW_CONTEXT_MENU		= "showContextMenu";
	}

	private static final	Map<String, String>	COMMAND_MAP	= Map.of
	(
		SingleSelectionList.Command.EDIT_ELEMENT,      Command.EDIT_FUNCTION,
		SingleSelectionList.Command.DELETE_ELEMENT,    Command.CONFIRM_DELETE_FUNCTION,
		SingleSelectionList.Command.DELETE_EX_ELEMENT, Command.DELETE_FUNCTION,
		SingleSelectionList.Command.MOVE_ELEMENT_UP,   Command.MOVE_FUNCTION_UP,
		SingleSelectionList.Command.MOVE_ELEMENT_DOWN, Command.MOVE_FUNCTION_DOWN,
		SingleSelectionList.Command.DRAG_ELEMENT,      Command.MOVE_FUNCTION
	);

	// Key actions
	private static final	KeyAction.KeyActionPair[]	PLOT_PANEL_KEY_ACTIONS	=
	{
		KeyAction.action(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
						 FunctionDocument.Command.SCROLL_LEFT),
		KeyAction.action(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
						 FunctionDocument.Command.SCROLL_RIGHT),
		KeyAction.action(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
						 FunctionDocument.Command.SCROLL_DOWN),
		KeyAction.action(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
						 FunctionDocument.Command.SCROLL_UP),
		KeyAction.action(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
						 FunctionDocument.Command.CENTRE_ON_ORIGIN)
	};

	private static final	KeyAction.KeyActionPair[]	FUNCTION_LIST_KEY_ACTIONS	=
	{
		KeyAction.action(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK),
						 FunctionDocument.Command.COPY_FUNCTION),
		KeyAction.action(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK),
						 FunctionDocument.Command.TOGGLE_HIGHLIGHT_FUNCTION)
	};

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Dimension	plotSize;
	private static	Dimension	functionListSize;
	private static	String		xZoomFactorStr		= DEFAULT_ZOOM_FACTOR_STR;
	private static	String		yZoomFactorStr		= DEFAULT_ZOOM_FACTOR_STR;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	FunctionDocument	document;
	private	PlotPanel			plotPanel;
	private	FunctionList		functionList;
	private	JScrollPane			functionListScrollPane;
	private	CoordinatesField	coordinatesField;
	private	EndpointField		xLowerEndpointField;
	private	EndpointField		xUpperEndpointField;
	private	EndpointField		yLowerEndpointField;
	private	EndpointField		yUpperEndpointField;
	private	JButton				addButton;
	private	JButton				viewButton;
	private	JButton				deleteButton;
	private	FComboBox<String>	xZoomFactorComboBox;
	private	FComboBox<String>	yZoomFactorComboBox;
	private	Point				mouseCursorLocation;
	private	DragStart			dragStart;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FunctionView(FunctionDocument document)
	{
		// Initialise instance variables
		this.document = document;

		// Initialise dimensions of plot and function list
		AppConfig config = AppConfig.INSTANCE;
		if (plotSize == null)
			plotSize = new Dimension(config.getPlotSize());
		if (functionListSize == null)
			functionListSize = new Dimension(config.getFunctionListSize());


		//----  Plot panel

		plotPanel = new PlotPanel(document, plotSize.width + 1, plotSize.height + 1, config.isShowGrid());
		plotPanel.addChangeListener(this);
		plotPanel.addMouseListener(this);
		plotPanel.addMouseMotionListener(this);
		plotPanel.addMouseWheelListener(this);
		KeyAction.create(plotPanel, JComponent.WHEN_FOCUSED, PLOT_PANEL_KEY_ACTIONS);


		//----  Function list

		// Function list
		functionList = new FunctionList(document, functionListSize.width, functionListSize.height);
		functionList.addActionListener(this);
		functionList.addListSelectionListener(this);
		functionList.addModelListener(this);
		functionList.addMouseListener(this);
		KeyAction.create(functionList, JComponent.WHEN_FOCUSED, FUNCTION_LIST_KEY_ACTIONS);

		// Scroll pane: function list
		functionListScrollPane = new JScrollPane(functionList,
												 (functionListSize.height < FunctionDocument.MAX_NUM_FUNCTIONS)
																				? JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
																				: JScrollPane.VERTICAL_SCROLLBAR_NEVER,
												 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		functionListScrollPane.setWheelScrollingEnabled(false);
		functionListScrollPane.getVerticalScrollBar().setFocusable(false);
		functionListScrollPane.getVerticalScrollBar().getModel().addChangeListener(this);

		functionList.setViewport(functionListScrollPane.getViewport());


		//----  Function command button panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel functionButtonPanel = new JPanel(gridBag);
		functionButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

		int gridX = 0;

		// Reset button width
		FunctionButton.reset();

		// Button: add
		addButton = new FunctionButton(FunctionDocument.Command.ADD_FUNCTION);
		addButton.setMnemonic(KeyEvent.VK_A);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(addButton, gbc);
		functionButtonPanel.add(addButton);

		// Button: edit
		JButton editButton = new FunctionButton(FunctionDocument.Command.EDIT_FUNCTION);
		editButton.setMnemonic(KeyEvent.VK_T);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(editButton, gbc);
		functionButtonPanel.add(editButton);

		// Button: copy
		JButton copyButton = new FunctionButton(FunctionDocument.Command.COPY_FUNCTION);
		copyButton.setMnemonic(KeyEvent.VK_C);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(copyButton, gbc);
		functionButtonPanel.add(copyButton);

		gridX = 0;

		// Button: view
		viewButton = new FunctionButton(FunctionDocument.Command.VIEW_FUNCTION);
		viewButton.setMnemonic(KeyEvent.VK_V);

		gbc.gridx = gridX++;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(viewButton, gbc);
		functionButtonPanel.add(viewButton);

		// Button: delete
		deleteButton = new FunctionButton(FunctionDocument.Command.DELETE_FUNCTION);
		deleteButton.setMnemonic(KeyEvent.VK_D);

		gbc.gridx = gridX++;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(deleteButton, gbc);
		functionButtonPanel.add(deleteButton);

		// Button: clear
		JButton clearButton = new FunctionButton(FunctionDocument.Command.CLEAR_FUNCTIONS);
		clearButton.setMnemonic(KeyEvent.VK_L);

		gbc.gridx = gridX++;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(clearButton, gbc);
		functionButtonPanel.add(clearButton);

		// Update buttons
		FunctionButton.update();
		updateButtons();


		//----  Function panel

		JPanel functionPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(functionPanel, 4);

		int gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 4, 0);
		gridBag.setConstraints(functionListScrollPane, gbc);
		functionPanel.add(functionListScrollPane);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(functionButtonPanel, gbc);
		functionPanel.add(functionButtonPanel);


		//----  Coordinates panel

		JPanel coordPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(coordPanel);

		gridX = 0;

		// Label: cursor
		JLabel cursorLabel = new FLabel(CURSOR_STR);

		gbc.gridheight = 1;

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(cursorLabel, gbc);
		coordPanel.add(cursorLabel);

		// Field: coordinates
		coordinatesField = new CoordinatesField();

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(coordinatesField, gbc);
		coordPanel.add(coordinatesField);


		//----  Interval panel

		JPanel intervalPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(intervalPanel);

		gridX = 0;

		// Label: x interval
		JLabel xIntervalLabel = new FLabel(X_INTERVAL_STR);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(xIntervalLabel, gbc);
		intervalPanel.add(xIntervalLabel);

		// Field: x lower endpoint
		xLowerEndpointField = new EndpointField(INTERVAL_EP_NUM_COLUMNS);
		xLowerEndpointField.setActionCommand(Command.SET_X_INTERVAL);
		xLowerEndpointField.addActionListener(this);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(xLowerEndpointField, gbc);
		intervalPanel.add(xLowerEndpointField);

		// Label: to, x
		JLabel xToLabel = new FLabel(TO_STR);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 1, 2, 1);
		gridBag.setConstraints(xToLabel, gbc);
		intervalPanel.add(xToLabel);

		// Field: x upper endpoint
		xUpperEndpointField = new EndpointField(INTERVAL_EP_NUM_COLUMNS);
		xUpperEndpointField.setActionCommand(Command.SET_X_INTERVAL);
		xUpperEndpointField.addActionListener(this);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(xUpperEndpointField, gbc);
		intervalPanel.add(xUpperEndpointField);

		gridX = 0;

		// Label: y interval
		JLabel yIntervalLabel = new FLabel(Y_INTERVAL_STR);

		gbc.gridx = gridX++;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(yIntervalLabel, gbc);
		intervalPanel.add(yIntervalLabel);

		// Field: y lower endpoint
		yLowerEndpointField = new EndpointField(INTERVAL_EP_NUM_COLUMNS);
		yLowerEndpointField.setActionCommand(Command.SET_Y_INTERVAL);
		yLowerEndpointField.addActionListener(this);

		gbc.gridx = gridX++;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(yLowerEndpointField, gbc);
		intervalPanel.add(yLowerEndpointField);

		// Label: to, y
		JLabel yToLabel = new FLabel(TO_STR);

		gbc.gridx = gridX++;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 1, 2, 1);
		gridBag.setConstraints(yToLabel, gbc);
		intervalPanel.add(yToLabel);

		// Field: y upper endpoint
		yUpperEndpointField = new EndpointField(INTERVAL_EP_NUM_COLUMNS);
		yUpperEndpointField.setActionCommand(Command.SET_Y_INTERVAL);
		yUpperEndpointField.addActionListener(this);

		gbc.gridx = gridX++;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(yUpperEndpointField, gbc);
		intervalPanel.add(yUpperEndpointField);

		// Update interval fields
		updateIntervalFields();


		//----  Scroll panel

		JPanel scrollPanel = new JPanel(new GridLayout(0, 3, 1, 1));
		GuiUtils.setPaddedLineBorder(scrollPanel, 2);
		Color scrollZoomBackgroundColour = ColourUtils.scaleBrightness(getBackground(), 0.95f);
		scrollPanel.setBackground(scrollZoomBackgroundColour);

		scrollPanel.add(GuiUtils.spacer());

		// Button: scroll up
		scrollPanel.add(new ScrollButton(FunctionDocument.Command.SCROLL_UP));

		scrollPanel.add(GuiUtils.spacer());

		// Button: scroll left
		scrollPanel.add(new ScrollButton(FunctionDocument.Command.SCROLL_LEFT));

		// Button: centre on origin
		scrollPanel.add(new ScrollButton(FunctionDocument.Command.CENTRE_ON_ORIGIN));

		// Button: scroll right
		scrollPanel.add(new ScrollButton(FunctionDocument.Command.SCROLL_RIGHT));

		scrollPanel.add(GuiUtils.spacer());

		// Button: scroll down
		scrollPanel.add(new ScrollButton(FunctionDocument.Command.SCROLL_DOWN));


		//----  X zoom panel

		JPanel xZoomPanel = new JPanel(gridBag);

		gridX = 0;

		// Label: x zoom
		JLabel xZoomLabel = new FLabel(X_ZOOM_STR);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(xZoomLabel, gbc);
		xZoomPanel.add(xZoomLabel);

		// Panel: x zoom buttons
		JPanel xZoomButtonPanel = new JPanel(new GridLayout(1, 0, 4, 0));
		GuiUtils.setPaddedLineBorder(xZoomButtonPanel, 2);
		xZoomButtonPanel.setBackground(scrollZoomBackgroundColour);

		xZoomButtonPanel.add(new ZoomButton(FunctionDocument.Command.X_ZOOM_IN));
		xZoomButtonPanel.add(new ZoomButton(FunctionDocument.Command.X_ZOOM_OUT));

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(xZoomButtonPanel, gbc);
		xZoomPanel.add(xZoomButtonPanel);

		// Combo box: x zoom factor
		xZoomFactorComboBox = new FComboBox<>(ZOOM_FACTOR_STRS);
		xZoomFactorComboBox.setSelectedValue(xZoomFactorStr);
		xZoomFactorComboBox.setActionCommand(Command.SELECT_X_ZOOM_FACTOR);
		xZoomFactorComboBox.addActionListener(this);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(xZoomFactorComboBox, gbc);
		xZoomPanel.add(xZoomFactorComboBox);


		//----  Y zoom panel

		JPanel yZoomPanel = new JPanel(gridBag);

		gridX = 0;

		// Label: y zoom
		JLabel yZoomLabel = new FLabel(Y_ZOOM_STR);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(yZoomLabel, gbc);
		yZoomPanel.add(yZoomLabel);

		// Panel: y zoom buttons
		JPanel yZoomButtonPanel = new JPanel(new GridLayout(1, 0, 4, 0));
		GuiUtils.setPaddedLineBorder(yZoomButtonPanel, 2);
		yZoomButtonPanel.setBackground(scrollZoomBackgroundColour);

		yZoomButtonPanel.add(new ZoomButton(FunctionDocument.Command.Y_ZOOM_IN));
		yZoomButtonPanel.add(new ZoomButton(FunctionDocument.Command.Y_ZOOM_OUT));

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(yZoomButtonPanel, gbc);
		yZoomPanel.add(yZoomButtonPanel);

		// Combo box: y zoom factor
		yZoomFactorComboBox = new FComboBox<>(ZOOM_FACTOR_STRS);
		yZoomFactorComboBox.setSelectedValue(yZoomFactorStr);
		yZoomFactorComboBox.setActionCommand(Command.SELECT_Y_ZOOM_FACTOR);
		yZoomFactorComboBox.addActionListener(this);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(yZoomFactorComboBox, gbc);
		yZoomPanel.add(yZoomFactorComboBox);


		//----  Zoom panel

		JPanel zoomPanel = new JPanel(gridBag);
		zoomPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(xZoomPanel, gbc);
		zoomPanel.add(xZoomPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(yZoomPanel, gbc);
		zoomPanel.add(yZoomPanel);


		//----  Scroll/zoom panel

		JPanel scrollZoomPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(scrollZoomPanel, 4, 6);

		gridX = 0;

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(scrollPanel, gbc);
		scrollZoomPanel.add(scrollPanel);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 8, 0, 0);
		gridBag.setConstraints(zoomPanel, gbc);
		scrollZoomPanel.add(zoomPanel);


		//----  Control panel

		JPanel controlPanel = new JPanel(gridBag);

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
		gridBag.setConstraints(functionPanel, gbc);
		controlPanel.add(functionPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(coordPanel, gbc);
		controlPanel.add(coordPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(intervalPanel, gbc);
		controlPanel.add(intervalPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(scrollZoomPanel, gbc);
		controlPanel.add(scrollZoomPanel);


		//----  Outer panel

		setLayout(gridBag);
		setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(plotPanel, gbc);
		add(plotPanel);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 3, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		add(controlPanel);

		// Add listener
		addMouseListener(this);

		// Add key actions
		KeyAction.create(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						 KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), Command.SHOW_CONTEXT_MENU, this);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static double getXZoomFactor()
	{
		return Double.parseDouble(xZoomFactorStr);
	}

	//------------------------------------------------------------------

	public static double getYZoomFactor()
	{
		return Double.parseDouble(yZoomFactorStr);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(ActionEvent event)
	{
		try
		{
			String command = event.getActionCommand();
			if (COMMAND_MAP.containsKey(command))
				command = COMMAND_MAP.get(command);

			switch (command)
			{
				case Command.EDIT_FUNCTION           -> onEditFunction();
				case Command.DELETE_FUNCTION         -> onDeleteFunction();
				case Command.CONFIRM_DELETE_FUNCTION -> onConfirmDeleteFunction();
				case Command.MOVE_FUNCTION_UP        -> onMoveFunctionUp();
				case Command.MOVE_FUNCTION_DOWN      -> onMoveFunctionDown();
				case Command.MOVE_FUNCTION           -> onMoveFunction();
				case Command.SET_X_INTERVAL          -> onSetXInterval();
				case Command.SET_Y_INTERVAL          -> onSetYInterval();
				case Command.SELECT_X_ZOOM_FACTOR    -> onSelectXZoomFactor();
				case Command.SELECT_Y_ZOOM_FACTOR    -> onSelectYZoomFactor();
				case Command.SHOW_CONTEXT_MENU       -> onShowContextMenu();
			}
		}
		catch (AppException e)
		{
			FuncPlotterApp.INSTANCE.showErrorMessage(FuncPlotterApp.SHORT_NAME, e);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ChangeListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void stateChanged(ChangeEvent event)
	{
		Object eventSource = event.getSource();

		if (eventSource == plotPanel)
		{
			updateIntervalFields();
			updateMouseCursorCoords();
		}

		else if (eventSource == functionListScrollPane.getVerticalScrollBar().getModel())
		{
			if (!functionListScrollPane.getVerticalScrollBar().getValueIsAdjusting() &&
				 !functionList.isDragging())
				functionList.snapViewPosition();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ListSelectionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void valueChanged(ListSelectionEvent event)
	{
		if (!event.getValueIsAdjusting())
			updateButtons();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void mouseClicked(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	public void mouseEntered(MouseEvent event)
	{
		if (event.getComponent() == plotPanel)
			updateMouseCursorCoords(event);
	}

	//------------------------------------------------------------------

	@Override
	public void mouseExited(MouseEvent event)
	{
		if (event.getComponent() == plotPanel)
			updateMouseCursorCoords((MouseEvent)null);
	}

	//------------------------------------------------------------------

	@Override
	public void mousePressed(MouseEvent event)
	{
		if (SwingUtilities.isLeftMouseButton(event))
		{
			if (event.getComponent() == plotPanel)
			{
				dragStart = new DragStart(event.getX(), event.getY(), document.getXInterval(),
										  document.getYInterval());
				plotPanel.setCursor(true);
			}
		}

		showContextMenu(event);
	}

	//------------------------------------------------------------------

	@Override
	public void mouseReleased(MouseEvent event)
	{
		if (SwingUtilities.isLeftMouseButton(event))
		{
			if (dragStart != null)
			{
				setIntervals(event, true);
				dragStart = null;
				plotPanel.setCursor(false);
			}
		}

		showContextMenu(event);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseMotionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void mouseDragged(MouseEvent event)
	{
		if (dragStart != null)
			setIntervals(event, false);

		updateMouseCursorCoords(event);
	}

	//------------------------------------------------------------------

	@Override
	public void mouseMoved(MouseEvent event)
	{
		updateMouseCursorCoords(event);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseWheelListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void mouseWheelMoved(MouseWheelEvent event)
	{
		FunctionDocument.Command command = null;
		InputModifiers modifiers = InputModifiers.forEvent(event);
		int numUnits = event.getWheelRotation();
		if (numUnits < 0)
		{
			numUnits = -numUnits;
			switch (modifiers)
			{
				case NONE:
					command = FunctionDocument.Command.SCROLL_UP;
					break;

				case CTRL:
					command = FunctionDocument.Command.SCROLL_RIGHT;
					break;

				default:
					// do nothing
					break;
			}
		}
		else
		{
			switch (modifiers)
			{
				case NONE:
					command = FunctionDocument.Command.SCROLL_DOWN;
					break;

				case CTRL:
					command = FunctionDocument.Command.SCROLL_LEFT;
					break;

				default:
					// do nothing
					break;
			}
		}
		if (command != null)
		{
			while (--numUnits >= 0)
				command.execute();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : SingleSelectionList.IModelListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void modelChanged(SingleSelectionList.ModelEvent event)
	{
		updatePlot();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public PlotPanel getPlotPanel()
	{
		return plotPanel;
	}

	//------------------------------------------------------------------

	public FunctionList getFunctionList()
	{
		return functionList;
	}

	//------------------------------------------------------------------

	public int getSelectedIndex()
	{
		return functionList.getSelectedIndex();
	}

	//------------------------------------------------------------------

	public void setDefaultFocus()
	{
		if (document.hasFunctions())
		{
			functionList.setSelectedIndex(0);
			functionList.requestFocusInWindow();
		}
		else
			addButton.requestFocusInWindow();
	}

	//------------------------------------------------------------------

	public void updatePlot()
	{
		plotPanel.repaint();
	}

	//------------------------------------------------------------------

	public void updateFunctionList()
	{
		functionList.repaint();
	}

	//------------------------------------------------------------------

	public void updateButtons()
	{
		document.updateCommands();
		Utils.moveFocus(SwingUtilities.getWindowAncestor(this));
	}

	//------------------------------------------------------------------

	public void updateXInterval()
	{
		updateXIntervalFields();
		plotPanel.setXInterval(document.getXInterval());
		updateMouseCursorCoords();
	}

	//------------------------------------------------------------------

	public void updateYInterval()
	{
		updateYIntervalFields();
		plotPanel.setYInterval(document.getYInterval());
		updateMouseCursorCoords();
	}

	//------------------------------------------------------------------

	public void updateIntervals()
	{
		updateIntervalFields();
		plotPanel.setIntervals(document.getXInterval(), document.getYInterval());
		updateMouseCursorCoords();
	}

	//------------------------------------------------------------------

	public void showMouseCursorCoords(boolean show)
	{
		updateMouseCursorCoords(show ? mouseCursorLocation : (Point)null);
	}

	//------------------------------------------------------------------

	public void showViewFunctionMenu()
	{
		// Create context menu
		JPopupMenu menu = new JPopupMenu();
		menu.add(new FCheckBoxMenuItem(FunctionDocument.Command.TOGGLE_HIGHLIGHT_FUNCTION));
		menu.add(new FCheckBoxMenuItem(FunctionDocument.Command.TOGGLE_HIDE_FUNCTION));

		// Update commands for menu items
		document.updateCommands();

		// Display menu
		menu.show(viewButton, viewButton.getWidth() - 1, 0);
	}

	//------------------------------------------------------------------

	private void updateMouseCursorCoords()
	{
		updateMouseCursorCoords(mouseCursorLocation);
	}

	//------------------------------------------------------------------

	private void updateMouseCursorCoords(Point location)
	{
		String xStr = null;
		String yStr = null;
		Point2D.Double coords = (location == null) ? null : plotPanel.pointToCoords(location);
		if (coords != null)
		{
			xStr = document.getXInterval().coordToString(coords.x);
			yStr = document.getYInterval().coordToString(coords.y);
		}
		coordinatesField.setText(xStr, yStr);
	}

	//------------------------------------------------------------------

	private void updateMouseCursorCoords(MouseEvent event)
	{
		mouseCursorLocation = (event == null) ? null : event.getPoint();
		updateMouseCursorCoords();
	}

	//------------------------------------------------------------------

	private void updateXIntervalFields()
	{
		PlotInterval xInterval = document.getXInterval();
		xLowerEndpointField.setText(xInterval.getLowerEndpointString());
		xUpperEndpointField.setText(xInterval.getUpperEndpointString());
	}

	//------------------------------------------------------------------

	private void updateYIntervalFields()
	{
		PlotInterval yInterval = document.getYInterval();
		yLowerEndpointField.setText(yInterval.getLowerEndpointString());
		yUpperEndpointField.setText(yInterval.getUpperEndpointString());
	}

	//------------------------------------------------------------------

	private void updateIntervalFields()
	{
		updateXIntervalFields();
		updateYIntervalFields();
	}

	//------------------------------------------------------------------

	private void setIntervals(MouseEvent event,
							  boolean    last)
	{
		// Get x endpoints
		double increment = -plotPanel.getXScrollIncrement(event.getX() - dragStart.x);
		double xLowerEndpoint = dragStart.xInterval.getLowerEndpoint() + increment;
		double xUpperEndpoint = dragStart.xInterval.getUpperEndpoint() + increment;

		// Get y endpoints
		increment = plotPanel.getYScrollIncrement(event.getY() - dragStart.y);
		double yLowerEndpoint = dragStart.yInterval.getLowerEndpoint() + increment;
		double yUpperEndpoint = dragStart.yInterval.getUpperEndpoint() + increment;

		// Set intervals
		if (last || (xLowerEndpoint != document.getXInterval().getLowerEndpoint()) ||
			 (yLowerEndpoint != document.getYInterval().getLowerEndpoint()))
		{
			FunctionDocument.Command command = FunctionDocument.Command.SET_INTERVALS;
			command.putValue(FunctionDocument.Command.Property.X_INTERVAL,
							 new PlotInterval(xLowerEndpoint, xUpperEndpoint));
			command.putValue(FunctionDocument.Command.Property.Y_INTERVAL,
							 new PlotInterval(yLowerEndpoint, yUpperEndpoint));
			command.putValue(FunctionDocument.Command.Property.END, last);
			command.execute();
		}
	}

	//------------------------------------------------------------------

	private void showContextMenu(MouseEvent event)
	{
		if ((event == null) || event.isPopupTrigger())
		{
			// Create context menu
			JPopupMenu menu = new JPopupMenu();
			menu.add(new FMenuItem(FunctionDocument.Command.ADD_FUNCTION));
			menu.add(new FMenuItem(FunctionDocument.Command.EDIT_FUNCTION));
			menu.add(new FMenuItem(FunctionDocument.Command.COPY_FUNCTION));

			JMenu submenu = new FMenu(FunctionDocument.Command.VIEW_FUNCTION);
			submenu.add(new FCheckBoxMenuItem(FunctionDocument.Command.TOGGLE_HIGHLIGHT_FUNCTION));
			submenu.add(new FCheckBoxMenuItem(FunctionDocument.Command.TOGGLE_HIDE_FUNCTION));
			menu.add(submenu);

			menu.add(new FMenuItem(FunctionDocument.Command.DELETE_FUNCTION));
			menu.add(new FMenuItem(FunctionDocument.Command.CLEAR_FUNCTIONS));

			menu.addSeparator();

			menu.add(new FCheckBoxMenuItem(FunctionDocument.Command.TOGGLE_SHOW_GRID));

			// Update commands for menu items
			document.updateCommands();

			// Display menu
			if (event == null)
				menu.show(this, 0, 0);
			else
				menu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	//------------------------------------------------------------------

	private void onEditFunction()
	{
		FunctionDocument.Command.EDIT_FUNCTION.execute();
	}

	//------------------------------------------------------------------

	private void onDeleteFunction()
	{
		FunctionDocument.Command.DELETE_FUNCTION.execute();
	}

	//------------------------------------------------------------------

	private void onConfirmDeleteFunction()
	{
		FunctionDocument.Command.CONFIRM_DELETE_FUNCTION.execute();
	}

	//------------------------------------------------------------------

	private void onMoveFunctionUp()
	{
		FunctionDocument.Command command = FunctionDocument.Command.MOVE_FUNCTION;
		command.putValue(FunctionDocument.Command.Property.INDEX, functionList.getSelectedIndex() - 1);
		command.execute();
	}

	//------------------------------------------------------------------

	private void onMoveFunctionDown()
	{
		FunctionDocument.Command command = FunctionDocument.Command.MOVE_FUNCTION;
		command.putValue(FunctionDocument.Command.Property.INDEX, functionList.getSelectedIndex() + 1);
		command.execute();
	}

	//------------------------------------------------------------------

	private void onMoveFunction()
	{
		int index = functionList.getDragEndIndex();
		if (index > functionList.getSelectedIndex())
			--index;
		FunctionDocument.Command command = FunctionDocument.Command.MOVE_FUNCTION;
		command.putValue(FunctionDocument.Command.Property.INDEX, index);
		command.execute();
	}

	//------------------------------------------------------------------

	private void onSetXInterval()
		throws AppException
	{
		// Validate lower endpoint
		try
		{
			try
			{
				double value = xLowerEndpointField.getValue().doubleValue();
				if ((value < PlotInterval.MIN_VALUE) || (value > PlotInterval.MAX_VALUE))
					throw new AppException(ErrorId.X_LOWER_ENDPOINT_OUT_OF_BOUNDS);
				if (PlotInterval.hasTooManySignificantDigits(xLowerEndpointField.getText()))
					throw new AppException(ErrorId.X_LOWER_ENDPOINT_HAS_TOO_MANY_SIGNIFICANT_DIGITS);
			}
			catch (NumberFormatException e)
			{
				throw new AppException(ErrorId.INVALID_X_LOWER_ENDPOINT);
			}
		}
		catch (AppException e)
		{
			GuiUtils.setFocus(xLowerEndpointField);
			throw e;
		}

		// Validate upper endpoint
		try
		{
			try
			{
				double value = xUpperEndpointField.getValue().doubleValue();
				if ((value < PlotInterval.MIN_VALUE) || (value > PlotInterval.MAX_VALUE))
					throw new AppException(ErrorId.X_UPPER_ENDPOINT_OUT_OF_BOUNDS);
				if (PlotInterval.hasTooManySignificantDigits(xUpperEndpointField.getText()))
					throw new AppException(ErrorId.X_UPPER_ENDPOINT_HAS_TOO_MANY_SIGNIFICANT_DIGITS);
			}
			catch (NumberFormatException e)
			{
				throw new AppException(ErrorId.INVALID_X_UPPER_ENDPOINT);
			}

			if (xLowerEndpointField.getValue().doubleValue() >= xUpperEndpointField.getValue().doubleValue())
				throw new AppException(ErrorId.X_ENDPOINTS_OUT_OF_ORDER);
		}
		catch (AppException e)
		{
			GuiUtils.setFocus(xUpperEndpointField);
			throw e;
		}

		// Set interval
		FunctionDocument.Command command = FunctionDocument.Command.SET_X_INTERVAL;
		command.putValue(FunctionDocument.Command.Property.X_INTERVAL,
						 new PlotInterval(xLowerEndpointField.getText(), xUpperEndpointField.getText()));
		command.execute();
	}

	//------------------------------------------------------------------

	private void onSetYInterval()
		throws AppException
	{
		// Validate lower endpoint
		try
		{
			try
			{
				double value = yLowerEndpointField.getValue().doubleValue();
				if ((value < PlotInterval.MIN_VALUE) || (value > PlotInterval.MAX_VALUE))
					throw new AppException(ErrorId.Y_LOWER_ENDPOINT_OUT_OF_BOUNDS);
				if (PlotInterval.hasTooManySignificantDigits(yLowerEndpointField.getText()))
					throw new AppException(ErrorId.Y_LOWER_ENDPOINT_HAS_TOO_MANY_SIGNIFICANT_DIGITS);
			}
			catch (NumberFormatException e)
			{
				throw new AppException(ErrorId.INVALID_Y_LOWER_ENDPOINT);
			}
		}
		catch (AppException e)
		{
			GuiUtils.setFocus(yLowerEndpointField);
			throw e;
		}

		// Validate upper endpoint
		try
		{
			try
			{
				double value = yUpperEndpointField.getValue().doubleValue();
				if ((value < PlotInterval.MIN_VALUE) || (value > PlotInterval.MAX_VALUE))
					throw new AppException(ErrorId.Y_UPPER_ENDPOINT_OUT_OF_BOUNDS);
				if (PlotInterval.hasTooManySignificantDigits(yUpperEndpointField.getText()))
					throw new AppException(ErrorId.Y_UPPER_ENDPOINT_HAS_TOO_MANY_SIGNIFICANT_DIGITS);
			}
			catch (NumberFormatException e)
			{
				throw new AppException(ErrorId.INVALID_Y_UPPER_ENDPOINT);
			}

			if (yLowerEndpointField.getValue().doubleValue() >= yUpperEndpointField.getValue().doubleValue())
				throw new AppException(ErrorId.Y_ENDPOINTS_OUT_OF_ORDER);
		}
		catch (AppException e)
		{
			GuiUtils.setFocus(yUpperEndpointField);
			throw e;
		}

		// Set interval
		FunctionDocument.Command command = FunctionDocument.Command.SET_Y_INTERVAL;
		command.putValue(FunctionDocument.Command.Property.Y_INTERVAL,
						 new PlotInterval(yLowerEndpointField.getText(), yUpperEndpointField.getText()));
		command.execute();
	}

	//------------------------------------------------------------------

	private void onSelectXZoomFactor()
	{
		xZoomFactorStr = xZoomFactorComboBox.getSelectedValue();
	}

	//------------------------------------------------------------------

	private void onSelectYZoomFactor()
	{
		yZoomFactorStr = yZoomFactorComboBox.getSelectedValue();
	}

	//------------------------------------------------------------------

	private void onShowContextMenu()
	{
		showContextMenu(null);
	}

	//------------------------------------------------------------------

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

		INVALID_X_LOWER_ENDPOINT
		("The lower endpoint of the x interval is invalid."),

		X_LOWER_ENDPOINT_OUT_OF_BOUNDS
		("The lower endpoint of the x interval must be between " + PlotInterval.MIN_VALUE + " and " +
			PlotInterval.MAX_VALUE + "."),

		X_LOWER_ENDPOINT_HAS_TOO_MANY_SIGNIFICANT_DIGITS
		("The lower endpoint of the x interval must not have more than " +
			PlotInterval.MAX_NUM_SIGNIFICANT_DIGITS + " significant digits."),

		INVALID_X_UPPER_ENDPOINT
		("The upper endpoint of the x interval is invalid."),

		X_UPPER_ENDPOINT_OUT_OF_BOUNDS
		("The upper endpoint of the x interval must be between " + PlotInterval.MIN_VALUE + " and " +
			PlotInterval.MAX_VALUE + "."),

		X_UPPER_ENDPOINT_HAS_TOO_MANY_SIGNIFICANT_DIGITS
		("The upper endpoint of the x interval must not have more than " +
			PlotInterval.MAX_NUM_SIGNIFICANT_DIGITS + " significant digits."),

		X_ENDPOINTS_OUT_OF_ORDER
		("The upper endpoint of the x interval is less than or equal to the lower endpoint."),

		INVALID_Y_LOWER_ENDPOINT
		("The lower endpoint of the y interval is invalid."),

		Y_LOWER_ENDPOINT_OUT_OF_BOUNDS
		("The lower endpoint of the y interval must be between " + PlotInterval.MIN_VALUE + " and " +
			PlotInterval.MAX_VALUE + "."),

		Y_LOWER_ENDPOINT_HAS_TOO_MANY_SIGNIFICANT_DIGITS
		("The lower endpoint of the y interval must not have more than " +
			PlotInterval.MAX_NUM_SIGNIFICANT_DIGITS + " significant digits."),

		INVALID_Y_UPPER_ENDPOINT
		("The upper endpoint of the y interval is invalid."),

		Y_UPPER_ENDPOINT_OUT_OF_BOUNDS
		("The upper endpoint of the y interval must be between " + PlotInterval.MIN_VALUE + " and " +
			PlotInterval.MAX_VALUE + "."),

		Y_UPPER_ENDPOINT_HAS_TOO_MANY_SIGNIFICANT_DIGITS
		("The upper endpoint of the y interval must not have more than " +
			PlotInterval.MAX_NUM_SIGNIFICANT_DIGITS + " significant digits."),

		Y_ENDPOINTS_OUT_OF_ORDER
		("The upper endpoint of the y interval is less than or equal to the lower endpoint.");

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


	// FUNCTION LIST CLASS


	public static class FunctionList
		extends SingleSelectionList<Function>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		ICON_WIDTH	= 20;
		private static final	int		ICON_HEIGHT	= 12;

		private static final	int		ICON_MARGIN	= 3;

		private static final	Color	ICON_BORDER_COLOUR	= Color.GRAY;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	char	minusChar;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private FunctionList(FunctionDocument document,
							 int              viewableColumns,
							 int              viewableRows)
		{
			// Call superclass constructor
			super(viewableColumns, viewableRows, AppFont.MAIN.getFont(), document);

			// Initialise instance variables
			minusChar = SurrogateMinus.getMinusChar(getFont());

			// Set properties
			int height = Math.max(getFontMetrics(getFont()).getHeight(), ICON_HEIGHT);
			setRowHeight(2 * DEFAULT_VERTICAL_MARGIN + height);
			setExtraWidth(ICON_MARGIN + ICON_WIDTH);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static BufferedImage createIconImage(Color   colour,
													 boolean hidden,
													 boolean obscured)
		{
			BufferedImage image = new BufferedImage(ICON_WIDTH, ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
			Graphics2D gr = image.createGraphics();

			if (!hidden)
			{
				AppConfig config = AppConfig.INSTANCE;
				if (!obscured || config.isFunctionObscuredColour())
				{
					gr.setColor(obscured ? config.getFunctionObscuredColour() : colour);
					gr.fillRect(1, 1, ICON_WIDTH - 2, ICON_HEIGHT - 2);
				}
				gr.setColor(ICON_BORDER_COLOUR);
				gr.drawRect(0, 0, ICON_WIDTH - 1, ICON_HEIGHT - 1);
			}

			return image;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getElementText(int index)
		{
			return SurrogateMinus.minusToSurrogate(getElement(index).toString(), minusChar);
		}

		//--------------------------------------------------------------

		@Override
		protected void drawElement(Graphics gr,
								   int      index)
		{
			// Create copy of graphics context
			Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

			// Draw icon
			int rowHeight = getRowHeight();
			int x = ICON_MARGIN;
			int y = index * rowHeight;
			Function function = getElement(index);
			gr2d.drawImage(createIconImage(function.getColour(), function.isHidden(), function.isObscured()),
						   x, y + (rowHeight - ICON_HEIGHT) / 2, null);

			// Set rendering hints for text antialiasing and fractional metrics
			TextRendering.setHints(gr2d);

			// Get text and truncate it if it is too wide
			FontMetrics fontMetrics = gr2d.getFontMetrics();
			String text = truncateText(getElementText(index), fontMetrics, getMaxTextWidth());

			// Draw text
			x = getExtraWidth() + getHorizontalMargin();
			gr2d.setColor(getForegroundColour(index));
			gr2d.drawString(text, x, y + FontUtils.getBaselineOffset(rowHeight, fontMetrics));
		}

		//--------------------------------------------------------------

		@Override
		protected int getPopUpXOffset()
		{
			return getExtraWidth();
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// FUNCTION BUTTON CLASS


	private static class FunctionButton
		extends FButton
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	Insets	MARGINS	= new Insets(2, 8, 2, 8);

	////////////////////////////////////////////////////////////////////
	//  Class variables
	////////////////////////////////////////////////////////////////////

		private static	List<FunctionButton>	instances;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private FunctionButton(Action action)
		{
			super(action);
			setMargin(MARGINS);

			if (instances == null)
				instances = new ArrayList<>();
			instances.add(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static void reset()
		{
			if (instances != null)
				instances.clear();
		}

		//--------------------------------------------------------------

		private static void update()
		{
			int maxWidth = 0;
			for (FunctionButton button : instances)
			{
				int width = button.getPreferredSize().width;
				if (maxWidth < width)
					maxWidth = width;
			}

			for (FunctionButton button : instances)
				button.setPreferredSize(new Dimension(maxWidth, button.getPreferredSize().height));
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// SCROLL BUTTON CLASS


	private static class ScrollButton
		extends JButton
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	Insets	MARGINS	= new Insets(1, 1, 1, 1);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ScrollButton(Action action)
		{
			super(action);
			setMargin(MARGINS);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// ZOOM BUTTON CLASS


	private static class ZoomButton
		extends JButton
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	Insets	MARGINS	= new Insets(1, 1, 1, 1);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ZoomButton(Action action)
		{
			super(action);
			setMargin(MARGINS);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// COORDINATES FIELD CLASS


	private static class CoordinatesField
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		NUM_ROWS	= 2;

		private static final	int		TOP_MARGIN		= 3;
		private static final	int		BOTTOM_MARGIN	= TOP_MARGIN;
		private static final	int		LEADING_MARGIN	= 5;
		private static final	int		TRAILING_MARGIN	= LEADING_MARGIN;
		private static final	int		GAP				= 2 * (LEADING_MARGIN - 1);

		private static final	Color	BACKGROUND_COLOUR		= new Color(248, 240, 200);
		private static final	Color	FOREGROUND_COLOUR		= Color.BLACK;
		private static final	Color	X_Y_BACKGROUND_COLOUR	= new Color(224, 216, 176);
		private static final	Color	BORDER_COLOUR			= new Color(240, 192, 144);

		private static final	String	BASE_STR	= "-.E-000";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int		width;
		private	int		height;
		private	int		charWidth;
		private	char	minusChar;
		private	String	text1;
		private	String	text2;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CoordinatesField()
		{
			AppFont.TEXT_FIELD.apply(this);
			minusChar = SurrogateMinus.getMinusChar(getFont());
			FontMetrics fontMetrics = getFontMetrics(getFont());
			charWidth = Math.max(FontUtils.getCharWidth('x', fontMetrics),
								 FontUtils.getCharWidth('y', fontMetrics));
			int maxStrWidth = fontMetrics.stringWidth(convertString(BASE_STR))
									+ fontMetrics.stringWidth("0".repeat(PlotInterval.MAX_NUM_SIGNIFICANT_DIGITS));
			width = LEADING_MARGIN + charWidth + GAP + maxStrWidth + TRAILING_MARGIN;
			height = TOP_MARGIN + NUM_ROWS * fontMetrics.getHeight() + BOTTOM_MARGIN;

			setOpaque(true);
			setFocusable(false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(width, height);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

			// Draw background
			Rectangle rect = gr2d.getClipBounds();
			gr2d.setColor(BACKGROUND_COLOUR);
			gr2d.fillRect(rect.x, rect.y, rect.width, rect.height);

			// Draw x/y background
			int x = 0;
			gr2d.setColor(X_Y_BACKGROUND_COLOUR);
			gr2d.fillRect(x, rect.y, LEADING_MARGIN + charWidth + GAP / 2, rect.height);

			// Set rendering hints for text antialiasing and fractional metrics
			TextRendering.setHints(gr2d);

			// Get text x and y coordinates
			FontMetrics fontMetrics = gr2d.getFontMetrics();
			x += LEADING_MARGIN;
			int y1 = TOP_MARGIN + fontMetrics.getAscent();
			int y2 = y1 + fontMetrics.getHeight();

			// Draw text
			gr2d.setColor(FOREGROUND_COLOUR);
			gr2d.drawString("x", x, y1);
			gr2d.drawString("y", x, y2);
			if (text1 != null)
			{
				int x1 = x + charWidth + GAP;
				gr2d.drawString(text1, x1, y1);
			}
			if (text2 != null)
			{
				int x1 = x + charWidth + GAP;
				gr2d.drawString(text2, x1, y2);
			}

			// Draw border
			gr2d.setColor(BORDER_COLOUR);
			gr2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void setText(String text1,
							String text2)
		{
			this.text1 = convertString(text1);
			this.text2 = convertString(text2);
			repaint();
		}

		//--------------------------------------------------------------

		private String convertString(String str)
		{
			return SurrogateMinus.minusToSurrogate(str, minusChar);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// INTERVAL ENDPOINT FIELD CLASS


	private static class EndpointField
		extends SurrogateMinus.Field
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	NUM_COLUMNS	= 12;

		private static final	String	VALID_CHARS	= "+.0123456789E";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EndpointField(int maxLength)
		{
			super(maxLength, NUM_COLUMNS);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected int getColumnWidth()
		{
			return FontUtils.getCharWidth('0', getFontMetrics(getFont())) + 1;
		}

		//--------------------------------------------------------------

		@Override
		protected String translateInsertString(String str,
											   int    offset)
		{
			return super.translateInsertString(str, offset).toUpperCase();
		}

		//--------------------------------------------------------------

		@Override
		protected boolean acceptCharacter(char ch,
										  int  index)
		{
			return isMinusCharacter(ch) || (VALID_CHARS.indexOf(ch) >= 0);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * @throws NumberFormatException
		 */

		public BigDecimal getValue()
		{
			return new BigDecimal(getText());
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// DRAG START CLASS


	private static class DragStart
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		int				x;
		int				y;
		PlotInterval	xInterval;
		PlotInterval	yInterval;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private DragStart(int          x,
						  int          y,
						  PlotInterval xInterval,
						  PlotInterval yInterval)
		{
			this.x = x;
			this.y = y;
			this.xInterval = xInterval;
			this.yInterval = yInterval;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
