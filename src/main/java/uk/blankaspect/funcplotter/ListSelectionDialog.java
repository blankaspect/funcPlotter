/*====================================================================*\

ListSelectionDialog.java

List selection dialog box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.funcplotter;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.Dialog;
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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.blankaspect.common.gui.FButton;
import uk.blankaspect.common.gui.FLabel;
import uk.blankaspect.common.gui.GuiUtils;
import uk.blankaspect.common.gui.SelectionList;

import uk.blankaspect.common.misc.KeyAction;

//----------------------------------------------------------------------


// LIST SELECTION DIALOG BOX CLASS


class ListSelectionDialog
	extends JDialog
	implements ActionListener, ListSelectionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	LIST_NUM_ROWS		= 16;
	private static final	int	LIST_NUM_COLUMNS	= 72;

	private static final	String	SELECT_ALL_STR				= "Select all";
	private static final	String	DESELECT_ALL_STR			= "Deselect all";
	private static final	String	SELECT_ALL_TOOLTIP_STR		= "Select all items (Ctrl+A)";
	private static final	String	DESELECT_ALL_TOOLTIP_STR	= "Deselect all items (Ctrl+D)";

	// Commands
	private interface Command
	{
		String	SELECT_ALL		= "selectAll";
		String	DESELECT_ALL	= "deselectAll";
		String	ACCEPT			= "accept";
		String	CLOSE			= "close";
	}

	private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
	{
		new KeyAction.KeyCommandPair(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK),
									 Command.SELECT_ALL),
		new KeyAction.KeyCommandPair(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK),
									 Command.DESELECT_ALL),
		new KeyAction.KeyCommandPair(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
									 Command.CLOSE)
	};

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private ListSelectionDialog(Window   owner,
								String   titleStr,
								String   listLabelStr,
								String[] listStrs)
	{

		// Call superclass constructor
		super(owner, titleStr, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());


		//----  List panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel listPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(listPanel);

		int gridY = 0;

		// Label: list
		JLabel listLabel = new FLabel(listLabelStr);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(listLabel, gbc);
		listPanel.add(listLabel);

		// List
		list = new SelectionList<>(listStrs, LIST_NUM_COLUMNS, LIST_NUM_ROWS);
		list.addListSelectionListener(this);

		// Scroll pane: list
		JScrollPane listScrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
													 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(4, 0, 0, 0);
		gridBag.setConstraints(listScrollPane, gbc);
		listPanel.add(listScrollPane);


		//----  Button panel: select all, deselect all

		JPanel selectionButtonPanel = new JPanel(new GridLayout(1, 0, 8, 0));

		// Button: select all
		selectAllButton = new FButton(SELECT_ALL_STR);
		selectAllButton.setToolTipText(SELECT_ALL_TOOLTIP_STR);
		selectAllButton.setActionCommand(Command.SELECT_ALL);
		selectAllButton.addActionListener(this);
		selectionButtonPanel.add(selectAllButton);

		// Button: deselect all
		deselectAllButton = new FButton(DESELECT_ALL_STR);
		deselectAllButton.setToolTipText(DESELECT_ALL_TOOLTIP_STR);
		deselectAllButton.setActionCommand(Command.DESELECT_ALL);
		deselectAllButton.addActionListener(this);
		selectionButtonPanel.add(deselectAllButton);


		//----  Button panel: OK, cancel

		JPanel okCancelButtonPanel = new JPanel(new GridLayout(1, 0, 8, 0));

		// Button: OK
		okButton = new FButton(AppConstants.OK_STR);
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
		gridBag.setConstraints(selectionButtonPanel, gbc);
		buttonPanel.add(selectionButtonPanel);

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
		gridBag.setConstraints(listPanel, gbc);
		mainPanel.add(listPanel);

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

		// Update components
		updateComponents();

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

	public static int[] showDialog(Component parent,
								   String    titleStr,
								   String    listLabelStr,
								   String[]  listStrs)
	{
		return new ListSelectionDialog(GuiUtils.getWindow(parent), titleStr, listLabelStr,
									   listStrs).getSelections();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.SELECT_ALL))
			onSelectAll();

		else if (command.equals(Command.DESELECT_ALL))
			onDeselectAll();

		else if (command.equals(Command.ACCEPT))
			onAccept();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ListSelectionListener interface
////////////////////////////////////////////////////////////////////////

	public void valueChanged(ListSelectionEvent event)
	{
		if (!event.getValueIsAdjusting())
			updateComponents();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private int[] getSelections()
	{
		return (accepted ? list.getSelectedIndices() : null);
	}

	//------------------------------------------------------------------

	private void updateComponents()
	{
		boolean enabled = (list.getSelectedIndices().length < list.getModel().getSize());
		selectAllButton.setEnabled(enabled);

		enabled = !list.isSelectionEmpty();
		deselectAllButton.setEnabled(enabled);
		okButton.setEnabled(enabled);

		Utils.moveFocus(this);
	}

	//------------------------------------------------------------------

	private void onSelectAll()
	{
		list.setSelectionInterval(0, list.getModel().getSize() - 1);
	}

	//------------------------------------------------------------------

	private void onDeselectAll()
	{
		list.clearSelection();
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		accepted = true;
		onClose();
	}

	//------------------------------------------------------------------

	private void onClose()
	{
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

	private	boolean					accepted;
	private	SelectionList<String>	list;
	private	JButton					selectAllButton;
	private	JButton					deselectAllButton;
	private	JButton					okButton;

}

//----------------------------------------------------------------------
