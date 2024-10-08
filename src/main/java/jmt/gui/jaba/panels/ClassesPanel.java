/**
 * Copyright (C) 2016, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package jmt.gui.jaba.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jmt.framework.data.ArrayUtils;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.table.editors.ButtonCellEditor;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.JMTImageLoader;
import jmt.gui.table.ComboBoxCell;
import jmt.gui.table.DisabledCellRenderer;
import jmt.gui.table.ExactTable;
import jmt.gui.table.ExactTableModel;
import jmt.gui.table.ListOp;
import jmt.gui.jaba.JabaConstants;
import jmt.gui.jaba.JabaModel;
import jmt.gui.jaba.JabaWizard;
import jmt.jmva.gui.panels.ForceUpdatablePanel;

/**

 * @author alyf (Andrea Conti)
 * Date: 11-set-2003
 * Time: 23.48.19

 * Adapted to JABA by Zanzottera

 * Heavily bugfixed by Bertoli Marco (31-jan-2006)

 */

/**
 * 1st panel: classes number, names, types and data
 */
public class ClassesPanel extends WizardPanel implements JabaConstants, ForceUpdatablePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Added by Bertoli Marco
	private static final int MINCLASSES = 2;
	private static final int MAXCLASSES = 3;
	// end

	private HoverHelp help;
	private static final String helpText = "<html>In this panel you can define the number of stations in the system and their properties.<br><br>"
			+ " To edit values, double-click on the desired cell"
			+ " and start typing.<br> To select classes click or drag on the row headers.<br> <b>For a list of the available operations right-click"
			+ " on the table</b>.<br>" + " Pressing DELETE removes all selected classes from the system.</html>";

	private JabaWizard ew;
	private boolean isLd;
	private int classes;
	private String[] classNames;
	private int[] classTypes;
	private double[] classData;
	private int nameCounter;

	private List<ListOp> classOps;
	private boolean hasDeletes;
	private boolean deleting = false;

	private JSpinner classSpinner = new JSpinner(new SpinnerNumberModel(2, 2, MAX_CLASSES, 1));

	private ClassTable classTable;

	private ChangeListener spinnerListener = new ChangeListener() {
		public void stateChanged(ChangeEvent ce) {
			if (!deleting) {
				updateSizes();
			}
		}
	};

	private AbstractAction deleteClass = new AbstractAction("Delete selected classes") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false));
			putValue(Action.SHORT_DESCRIPTION, "Deletes selected classes from the system");
		}

		public void actionPerformed(ActionEvent e) {
			deleteSelectedClasses();
		}
	};

	private AbstractAction deleteOneClass = new AbstractAction("") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Delete This Class");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Delete"));
		}

		public void actionPerformed(ActionEvent e) {
		}
	};

	private AbstractAction addClass = new AbstractAction("New Class") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK));
			putValue(Action.SHORT_DESCRIPTION, "Adds a new Class to Model");
		}

		public void actionPerformed(ActionEvent e) {
			addClass();
		}
	};

	public ClassesPanel(JabaWizard ew) {
		this.ew = ew;
		help = ew.getHelp();
		classOps = new ArrayList<ListOp>();

		sync();

		initComponents();
		makeNames();
	}

	private void sync() {
		hasDeletes = false;
		classOps.clear();

		/* sync status with data object */
		/* arrays are copied to ensure data object consistency is preserved */
		JabaModel data = ew.getData();
		synchronized (data) {
			classes = data.getClasses();
			nameCounter = classes;
			isLd = data.isLd();
			classNames = ArrayUtils.copy(data.getClassNames());
			classTypes = ArrayUtils.copy(data.getClassTypes());
			classData = ArrayUtils.copy(data.getClassData());

			//NEW
			//@author Stefano Omini
			if (isLd) {
				//if load dependent is no more supported after class changes
				//remove all load dependent stations
				if (!(data.isClosed() && data.isSingleClass())) {
					JOptionPane
							.showMessageDialog(
									this,
									"<html><center>JMVA allows Load Dependent stations only for single class closed model. <br> Load Dependent stations will be replaced with Load Independent stations.</center></html>",
									"Warning", JOptionPane.WARNING_MESSAGE);
					data.removeLD();
					isLd = false;
				}
			}
			//end NEW
		}
		classSpinner.setValue(new Integer(classes));
	}

	/**
	 * make up names for null entries
	 */
	private void makeNames() {
		for (int i = 0; i < classNames.length; i++) {
			if (classNames[i] == null) {
				classNames[i] = "Class" + (++nameCounter);
			}
		}
	}

	/**
	 * resize internal data structures according to new values. intended to be called from a listener.
	 */
	private void updateSizes()
	//todo add control on number of classes 
	{
		if (((Integer) classSpinner.getValue()).intValue() <= MAXCLASSES) {
			setNumberOfClasses(((Integer) classSpinner.getValue()).intValue());
		} else if (((Integer) classSpinner.getValue()).intValue() < MINCLASSES) {
			JOptionPane.showMessageDialog(this, "Sorry, jABA needs at least " + MINCLASSES + " classes.", "jABA classes warning",
					JOptionPane.ERROR_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, "Sorry, jABA admits only up to " + MAXCLASSES + " classes.", "jABA classes warning",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	private void addClass() {
		setNumberOfClasses(classes + 1);
	}

	private void setNumberOfClasses(int number) {
		classTable.stopEditing();

		if (number <= MAXCLASSES) {
			classes = number;
		} else {
			JOptionPane.showMessageDialog(this, "Sorry, jABA admits only up to " + MAXCLASSES + " classes", "jABA classes warning",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		classNames = ArrayUtils.resize(classNames, classes, null);
		makeNames();

		classTypes = ArrayUtils.resize(classTypes, classes, CLASS_CLOSED);
		classData = ArrayUtils.resize(classData, classes, 0.0);

		classTable.updateStructure();
		if (!deleting) {
			classOps.add(ListOp.createResizeOp(classes));
		}

		classSpinner.setValue(new Integer(classes));
		classTable.updateDeleteCommand();
	}

	/**
	 * Set up the panel contents and layout
	 */
	private void initComponents() {
		classSpinner.addChangeListener(spinnerListener);
		help.addHelp(classSpinner, "Enter the number of classes for this system");

		classTable = new ClassTable();

		/* and now some Box black magic */
		//DEK (Federico Granata) 26-09-2003
		Box classSpinnerBox = Box.createHorizontalBox();
		//OLD
		//JLabel spinnerLabel = new JLabel("<html><font size=\"4\">Set the Number of classes (1-" + MAX_CLASSES + "):</font></html>");
		//NEW
		//@author Stefano
		JLabel spinnerLabel = new JLabel(DESCRIPTION_CLASSES);

		classSpinnerBox.add(spinnerLabel);
		//END
		//BEGIN Federico Dall'Orso 9/3/2005
		//OLD
		/*
		classSpinnerBox.add(Box.createGlue());
		*/
		//NEW
		classSpinnerBox.add(Box.createHorizontalStrut(10));
		Box numberBox = Box.createVerticalBox();

		JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JLabel numberLabel = new JLabel("Number (Max=3):");
		classSpinner.setMaximumSize(new Dimension((int)(600 * CommonConstants.widthScaling), (int)(18 * CommonConstants.heightScaling)));
		spinnerPanel.add(numberLabel);
		spinnerPanel.add(classSpinner);
		numberBox.add(spinnerPanel);

		numberBox.add(new JButton(addClass));

		numberBox.setMaximumSize(new Dimension((int)(300 * CommonConstants.widthScaling), (int)(150 * CommonConstants.heightScaling)));

		classSpinnerBox.add(numberBox);
		//END  Federico Dall'Orso 9/3/2005

		Box classBox = Box.createVerticalBox();
		classBox.add(Box.createVerticalStrut(30));
		classBox.add(classSpinnerBox);
		classBox.add(Box.createVerticalStrut(10));
		JScrollPane classTablePane = new JScrollPane(classTable);
		classTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		classTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		classBox.add(classTablePane);
		classBox.add(Box.createVerticalStrut(30));

		Box totalBox = Box.createHorizontalBox();
		totalBox.add(Box.createHorizontalStrut(20));
		totalBox.add(classBox);
		totalBox.add(Box.createHorizontalStrut(20));

		setLayout(new BorderLayout());
		add(totalBox, BorderLayout.CENTER);

	}

	/**
	 * @return the total number of customers in the system
	 */
	private int sumPop() {
		int pop = 0;
		for (int i = 0; i < classes; i++) {
			if (classTypes[i] == CLASS_CLOSED) {
				pop += classData[i];
			}
		}
		return pop;
	}

	@Override
	public String getName() {
		return "Classes";
	}

	@Override
	public void lostFocus() {
		commit();
		//release();
	}

	@Override
	public void gotFocus() {
		sync();
		classTable.update();
	}

	@Override
	public boolean canFinish() {
		return checkPop() && !areThereDuplicates();
	}

	@Override
	public boolean canGoBack() {
		checkPop();
		if (areThereDuplicates()) {
			return false;
		}
		return true; //so that the user can correct errors
	}

	@Override
	public boolean canGoForward() {
		checkPop();
		if (areThereDuplicates()) {
			return false;
		}
		return true; // so that the user can correct errors
	}

	//checks whether population is greater than 0
	private boolean checkPop() {
		classTable.stopEditing();
		if (isLd && sumPop() < 1) {
			JOptionPane
					.showMessageDialog(
							this,
							"<html><center>A system with load dependent stations cannot have zero customers.<br>Increase the number of customers or remove all load dependent stations.</center></html>",
							"Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		return true;
	}

	//checks for presence of classes with same name
	private boolean areThereDuplicates() {
		boolean thereAreDupl = false;
		for (int i = 0; i < classNames.length; i++) {
			for (int j = i + 1; j < classNames.length; j++) {
				thereAreDupl = thereAreDupl || classNames[i].equalsIgnoreCase(classNames[j]);
			}
		}
		if (thereAreDupl) {
			JOptionPane.showMessageDialog(this,
					"<html><center>Two or more classes in this system are identified by the same name.<br>Please modify names.</center></html>",
					"Warning", JOptionPane.WARNING_MESSAGE);
			return true;
		} else {
			return false;
		}
	}

	private void commit() {
		/* stop any editing in progress */
		if (classSpinner.getEditor().getComponent(0).hasFocus()) {
			//disgusting. there must be a better way...
			try {
				classSpinner.commitEdit();
				updateSizes();
			} catch (java.text.ParseException e) {
			}
		}

		classTable.stopEditing();

		JabaModel data = ew.getData();

		synchronized (data) {

			if (hasDeletes) {
				//if at some point rows have been deleted
				//play back all ops in the same order on the data object
				playbackClassOps(data);
			} else {
				data.resize(data.getStations(), classes); //otherwise a simple resize is ok
			}

			data.setClassNames(classNames);
			data.setClassTypes(classTypes);
			data.setClassData(classData);

			//NEW
			//@author Stefano Omini
			sync();
			//end NEW
		}
	}

	private void deleteSelectedClasses() {
		int[] selectedRows = classTable.getSelectedRows();
		int nrows = selectedRows.length;
		int left = classTable.getRowCount() - nrows;
		if (left < 1) {
			classTable.removeRowSelectionInterval(selectedRows[nrows - 1], selectedRows[nrows - 1]);
			deleteSelectedClasses();
			return;
		}
		deleteClasses(selectedRows);
	}

	private void deleteClasses(int[] idx) {
		deleting = true;
		Arrays.sort(idx);
		for (int i = idx.length - 1; i >= 0; i--) {
			deleteClass(idx[i]);
		}
		updateSizes();
		deleting = false;
	}

	private void deleteClass(int i) {

		classes--;
		classSpinner.setValue(new Integer(classes));

		classNames = ArrayUtils.delete(classNames, i);
		classTypes = ArrayUtils.delete(classTypes, i);
		classData = ArrayUtils.delete(classData, i);

		classOps.add(ListOp.createDeleteOp(i));
		hasDeletes = true;

	}

	private void playbackClassOps(JabaModel data) {
		for (int i = 0; i < classOps.size(); i++) {
			ListOp lo = classOps.get(i);
			if (lo.isDeleteOp()) {
				data.deleteClass(lo.getData());
			}
			if (lo.isResizeOp()) {
				data.resize(data.getStations(), lo.getData());
			}

		}
	}

	@Override
	public void help() {
		JOptionPane.showMessageDialog(this, helpText, "Help", JOptionPane.INFORMATION_MESSAGE);

	}

	//NEW Federico Dall'Orso
	//Methods added to implement forcing of data refresh
	public void retrieveData() {
		sync();
	}

	public void commitData() {
		commit();
	}

	//END

	/* ------------------------------------------------------------------
	   The ClassTable is a fairly complex object that would be probably better of as an outer class.
	   However, it is very specialized and it needs access to the data structures of the ClassesPanel,
	   so having it as an inner class is *much* more practical
	   ------------------------------------------------------------------
	   */

	/**
	 * Her Majesty, the class table herself
	 */
	private class ClassTable extends ExactTable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		TableCellRenderer disabledCellRenderer;
		TableCellEditor classTypeCellEditor;
		//BEGIN Federico Dall'Orso 8/3/2005
		ComboBoxCell classTypeComboBoxCell;
		ButtonCellEditor deleteButtonCellRenderer;
		JButton deleteButton;

		//END Federico Dall'Orso 8/3/2005

		ClassTable() {
			super(new ClassTableModel());

			disabledCellRenderer = new DisabledCellRenderer();

			//BEGIN Federico Dall'Orso 8/3/2005
			//NEW
			classTypeComboBoxCell = new ComboBoxCell(CLASS_TYPENAMES);
			//todo replace after adding open classes
			classTypeComboBoxCell.getComboBox().setEnabled(false);

			deleteButton = new JButton(deleteOneClass);
			deleteButtonCellRenderer = new ButtonCellEditor(deleteButton);
			enableDeletes();
			rowHeader.setRowHeight(CommonConstants.ROW_HEIGHT);
			setRowHeight(CommonConstants.ROW_HEIGHT);
			//END Federico Dall'Orso 8/3/2005

			JComboBox<String> classTypeBox = new JComboBox<String>(CLASS_TYPENAMES);
			//todo replace after adding open classes
			classTypeBox.setEnabled(false);

			classTypeCellEditor = new DefaultCellEditor(classTypeBox);
			classTypeBox.setEditable(false);

			setRowSelectionAllowed(true);
			setColumnSelectionAllowed(false);

			// not beautiful, but effective. See ClassTableModel.getColumnClass()
			setDefaultRenderer(DisabledCellRenderer.class, disabledCellRenderer);
			setDefaultEditor(String.class, classTypeCellEditor);

			setDisplaysScrollLabels(true);

			installKeyboardAction(getInputMap(), getActionMap(), deleteClass);
			mouseHandler = new ExactTable.MouseHandler(makeMouseMenu());
			mouseHandler.install();

			help.addHelp(this,
					"Click or drag to select classes; to edit data double-click and start typing. Right-click for a list of available operations");
			help.addHelp(moreRowsLabel, "There are more classes: scroll down to see them");
			help.addHelp(selectAllButton, "Click to select all classes");
			tableHeader.setToolTipText(null);
			rowHeader.setToolTipText(null);
			help.addHelp(rowHeader, "Click, SHIFT-click or drag to select classes");
		}

		//BEGIN Federico Dall'Orso 14/3/2005
		/*enables deleting operations with last column's button*/
		private void enableDeletes() {
			//deleteOneClass.setEnabled(classes>1);
			//todo zanzottera added control initially it is not possible to cancel the 2 classes
						deleteOneClass.setEnabled(classes > MINCLASSES);
			/*It seems the only way to implement row deletion...*/
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (classes > MINCLASSES && (columnAtPoint(e.getPoint()) == getColumnCount() - 1) && getRowCount() > 1) {
						setRowSelectionInterval(rowAtPoint(e.getPoint()), rowAtPoint(e.getPoint()));
						deleteSelectedClasses();
					}
				}
			});
			getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(20);
			getColumnModel().getColumn(getColumnCount() - 1).setMaxWidth(20);
		}

		//END Federico Dall'Orso 14/3/2005

		//new Federico Dall'Orso 8/3/2005

		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			//if this is type column, i must render it as a combo box instead of a jtextfield
			if (column == 1) {
				return classTypeComboBoxCell;
			} else if (column == 4) {
				return deleteButtonCellRenderer;
			} else {
				return disabledCellRenderer;
			}
		}

		//end Federico Dall'Orso 8/3/2005

		@Override
		protected void installKeyboard() {
		}

		@Override
		protected void installMouse() {
		}

		@Override
		protected JPopupMenu makeMouseMenu() {
			JPopupMenu menu = new JPopupMenu();
			menu.add(deleteClass);
			return menu;
		}

		/**
		 * Make sure the table reflects changes on editing end
		 * Overridden to truncate decimals in data if current class is closed
		 */
		@Override
		public void editingStopped(ChangeEvent ce) {
			if (classTypes[editingRow] == CLASS_CLOSED) {
				classData[editingRow] = (int) classData[editingRow];
			}
			updateRow(editingRow);
			super.editingStopped(ce);
		}

		//BEGIN Federico Dall'Orso 14/3/2005
		//NEW
		//Updates appearence of last column's buttons
		void updateDeleteCommand() {
			//todo inserito controllo non si possono cancellare classi se ce ne sono solo 2
			deleteOneClass.setEnabled(classes > MINCLASSES);
			getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(20);
			getColumnModel().getColumn(getColumnCount() - 1).setMaxWidth(20);

		}

		//END Federico Dall'Orso 14/3/2005

		@Override
		protected void updateActions() {
			boolean isEnabled = classes > MINCLASSES && getSelectedRowCount() > 0;
			deleteClass.setEnabled(isEnabled);
			deleteOneClass.setEnabled(classes > MINCLASSES);
		}
	}

	/**
	 * the model backing the class table
	 */
	private class ClassTableModel extends ExactTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Object[] prototypes = { "10000", new String(new char[12]), "closed+cbox", new Integer(1000), new String(new char[12]), "" };
		private int suffixClassCollision = 1;

		@Override
		public Object getPrototype(int columnIndex) {
			return prototypes[columnIndex + 1];
		}

		public int getRowCount() {
			return classes;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int col) {
			switch (col) {
				case 1:
					return String.class;
				case 2:
				case 3:
					return DisabledCellRenderer.class;
				default:
					return Object.class;
			}
		}

		public int getColumnCount() {
			return 5;
		}

		@Override
		public String getColumnName(int index) {
			switch (index) {
				case 0:
					return "Name";
				case 1:
					return "Type (Closed Only)";
				case 2:
					return "No. of Customers (+inf.)";
				case 3:
					//return "Arrival Rate (\u03BB)";
					return "Arrival Rate (Further versions)";
				default:
					return null;
			}
		}

		@Override
		protected Object getValueAtImpl(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0://name
					return classNames[rowIndex];
				case 1://type
					return CLASS_TYPENAMES[classTypes[rowIndex]];
				case 2://customers
					/*
					if (classTypes[rowIndex] == CLASS_CLOSED) {
						return new Integer((int) classData[rowIndex]);
					} else {
						return null;
					}
					*/
				case 3://arrival rate
					/*
					if (classTypes[rowIndex] == CLASS_OPEN) {
						return new Double(classData[rowIndex]);
					} else {
						return null;
					}
					*/
				default:
					return null;
			}
		}

		@Override
		protected Object getRowName(int rowIndex) {
			return new Integer(rowIndex + 1);
		}
		
		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0: //name
					String tmpName = (String) value;
					//Check for class name collision
					for (int i = 0; i < classNames.length; i++ ) {
						if (classNames[i].equals(tmpName) && i != rowIndex) {
							tmpName = tmpName + "_" + suffixClassCollision++;
							break;
						}
					}
					classNames[rowIndex] = tmpName;
					break;
				case 1: //type
					for (int i = 0; i < CLASS_TYPENAMES.length; i++) {
						if (value == CLASS_TYPENAMES[i]) { //literal strings are canonicalized, hence == is ok
							classTypes[rowIndex] = i;
							break;
						}
					}
					break;
				case 2: {//customers
					try {
						int newval = (int) Double.parseDouble((String) value);
						if (newval >= 0) {
							classData[rowIndex] = newval;
						}
					} catch (NumberFormatException e) {
					}
					break;
				}
				case 3: { //arrival rate
					try {
						double newval = Double.parseDouble((String) value);
						if (newval >= 0.0) {
							classData[rowIndex] = newval;
						}
					} catch (NumberFormatException e) {
					}
					break;
				}
				default:
			}
		}


		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0:
					return true;
				case 1:
					return false;
				case 2:
					//todo restore when open classes will be accepted
					//return (classTypes[rowIndex] == CLASS_CLOSED);
				case 3:
					return (classTypes[rowIndex] == CLASS_OPEN);
				default:
					return false;
			}
		}
	}

}
