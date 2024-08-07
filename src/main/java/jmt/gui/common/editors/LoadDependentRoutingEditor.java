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

package jmt.gui.common.editors;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jmt.framework.gui.components.JMTDialog;
import jmt.framework.gui.table.editors.ButtonCellEditor;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.JMTImageLoader;
import jmt.gui.common.definitions.ClassDefinition;
import jmt.gui.common.definitions.StationDefinition;
import jmt.gui.common.routingStrategies.LoadDependentRouting;
import jmt.gui.table.ExactCellEditor;

/**
 * Created by IntelliJ IDEA.
 * User: Ashanka
 * Date: 6/15/12
 * Time: 12:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class LoadDependentRoutingEditor extends JMTDialog implements CommonConstants {

	private static final long serialVersionUID = 1L;

	private Object stationKey;
	private Object classKey;
	private StationDefinition stations;
	private ClassDefinition classes;

	private LoadDependentRouting routingStrategy;
	private Vector<Object> forwardStationKeys;
	private Vector<Object> validForwardStationKeys;
	private HashMap<Integer, Integer> mapRowIndexAndFrom;
	protected JTable rangesTable;

	public LoadDependentRoutingEditor(Frame parent, HashMap<String, Object> params) {
		super(parent, true);
		init(params);
	}

	public LoadDependentRoutingEditor(Dialog parent, HashMap<String, Object> params) {
		super(parent, true);
		init(params);
	}

	private void init(HashMap<String, Object> params) {
		stations = (StationDefinition) params.get("StationDefinition");
		classes = (ClassDefinition) params.get("ClassDefinition");
		stationKey = params.get("stationKey");
		classKey = params.get("classKey");
		routingStrategy = (LoadDependentRouting) stations.getRoutingStrategy(stationKey, classKey);
		forwardStationKeys = stations.getForwardConnections(stationKey);
		validForwardStationKeys = new Vector<Object>();
		for (Object stationKey : forwardStationKeys) {
			if (classes.getClassType(classKey) != CLASS_TYPE_CLOSED
					|| !stations.getStationType(stationKey).equals(STATION_TYPE_SINK)) {
				validForwardStationKeys.add(stationKey);
			}
		}
		routingStrategy.refreshRouting(forwardStationKeys, validForwardStationKeys);
		if (mapRowIndexAndFrom == null) {
			mapRowIndexAndFrom = new HashMap<Integer, Integer>();
		}
		int range = 0;
		List<Integer> fromAsList = new ArrayList<Integer>();
		fromAsList.addAll(routingStrategy.getAllEmpiricalEntries().keySet());
		Collections.sort(fromAsList);
		for (Integer from : fromAsList) {
			mapRowIndexAndFrom.put(range, from);
			range++;
		}
		initGUI((String) params.get("title"));
	}

	private void initGUI(String title) {
		centerWindow(CommonConstants.MAX_GUI_WIDTH_LDROUTING, CommonConstants.MAX_GUI_HEIGHT_LDROUTING);
		setTitle(title);
		// Adds "Add Range" Button
		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel rightTopPanel = new JPanel(new BorderLayout());
		JButton addRange = new JButton(addRangeAction);
		addRange.setMinimumSize(DIM_BUTTON_S);
		rightTopPanel.add(addRange, BorderLayout.EAST);
		topPanel.add(rightTopPanel, BorderLayout.NORTH);

		JPanel LDPanel = new JPanel(new BorderLayout());
		LDPanel.add(topPanel, BorderLayout.NORTH);
		// Adds StrategyTable
		rangesTable = new LoadDependentRoutingStrategyTable();
		JScrollPane scrollPane = new JScrollPane(rangesTable);
		LDPanel.add(scrollPane, BorderLayout.CENTER);

		JTabbedPane mainPanel = new JTabbedPane();
		JPanel bottomPanel = new JPanel(new FlowLayout());
		// Adds Okay button to bottom_panel
		JButton okayButton = new JButton(okayAction);
		bottomPanel.add(okayButton);

		mainPanel.addTab("Edit", LDPanel);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add((bottomPanel), BorderLayout.SOUTH);
		refreshComponents();
	}

	protected AbstractAction okayAction = new AbstractAction("OK") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Accepts changes and closes this window");
		}

		public void actionPerformed(ActionEvent e) {
			List<String> errors = routingStrategy.validate();
			StringBuilder msg = new StringBuilder();
			if (errors.size() > 0) {
				for (Map<Object, Double> routingProbs : routingStrategy.getAllEmpiricalEntries().values()) {
					stations.normalizeRoutingProbabilities(stationKey, classKey, routingProbs);
				}
				for (String error : errors) {
					msg.append(error).append("\n");
				}
				showErrorMessageToUser(msg.toString());
				refreshComponents();
			} else {
				LoadDependentRoutingEditor.this.setVisible(false);
			}
		}

	};

	// When Add Range button is pressed
	protected AbstractAction addRangeAction = new AbstractAction("Add Range") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Adds a new range into this service time strategy");
		}

		public void actionPerformed(ActionEvent e) {
			addRange();
		}

	};

	protected void addRange() {
		if (routingStrategy.getAllEmpiricalEntries().size() >= 100) {
			return;
		}
		if (mapRowIndexAndFrom == null) {
			mapRowIndexAndFrom = new HashMap<Integer, Integer>();
		}
		Integer newRowIndex = mapRowIndexAndFrom.size();
		int newFrom = 1;
		if (newRowIndex != 0) {
			int lastRowIndex = newRowIndex - 1;
			int lastFrom = mapRowIndexAndFrom.get(lastRowIndex);
			newFrom = lastFrom + 1;
		}
		for (Object stationKey : forwardStationKeys)  {
			routingStrategy.addEmpiricalEntry(newFrom, stationKey, Double.valueOf(0.0));
		}
		mapRowIndexAndFrom.put(newRowIndex, newFrom);
		refreshComponents();
	}

	// deletion of one range
	protected AbstractAction deleteRange = new AbstractAction("") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Deletes this range from current service section");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Delete"));
		}

		public void actionPerformed(ActionEvent e) {
			int index = rangesTable.getSelectedRow();
			if (index >= 0 && index < rangesTable.getRowCount()) {
				deleteRange(index);
			}
		}

	};

	protected void refreshComponents() {
		rangesTable.tableChanged(new TableModelEvent(rangesTable.getModel()));
	}

	/**
	 * delete a range from strategy given the index the range to be deleted is displayed at
	 * inside the table.
	 * @param index
	 * corresponding row index where delete will be performed
	 */
	protected void deleteRange(int index) {
		Integer from = mapRowIndexAndFrom.get(index);
		int nextRowIndex = index + 1;
		while (mapRowIndexAndFrom.get(nextRowIndex) != null) {
			mapRowIndexAndFrom.put(index, mapRowIndexAndFrom.get(nextRowIndex));
			nextRowIndex++;
			index++;
		}
		mapRowIndexAndFrom.remove(index);
		routingStrategy.removeEmpiricalEntriesForFrom(from);
		refreshComponents();
	}

	protected class LoadDependentRoutingStrategyTable extends JTable {

		private static final long serialVersionUID = 1L;

		/*This button allow a single range to be deleted directly from the table.
		Corresponding value contained into cell must be zero.*/
		public JButton deleteButton = new JButton() {

			private static final long serialVersionUID = 1L;

			{
				setAction(deleteRange);
				setFocusable(false);
			}

		};

		protected int[] columnSizes = null;

		public LoadDependentRoutingStrategyTable() {
			setModel(new LoadDependentRoutingStrategyTableModel());
			setDefaultEditor(Object.class, new ExactCellEditor());
			setDefaultRenderer(String.class, new GrayCellRenderer());
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			columnSizes = new int[getModel().getColumnCount()];
			columnSizes[0] = 20;
			columnSizes[1] = 20;
			for (int index = 2; index < columnSizes.length; index++) {
				columnSizes[index] = 50;
				if (index == columnSizes.length - 1) {
					columnSizes[index] = 20;
				}
			}
			sizeColumnsAndRows();
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			getTableHeader().setReorderingAllowed(false);
		}

		//returns a component to be contained inside a table column(or cell)
		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			int deleteButtonPosition = validForwardStationKeys.size() + 2;
			if (column == deleteButtonPosition && row > 0) {
				return new ButtonCellEditor(deleteButton);
			} else if (column == deleteButtonPosition && row == 0) {
				return getDefaultRenderer(String.class);
			} else {
				return getDefaultRenderer(getModel().getColumnClass(column));
			}
		}

		/*returns customized editor for table cells.*/
		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			int deleteButtonPosition = validForwardStationKeys.size() + 2;
			if (column == deleteButtonPosition && row > 0) {
				return new ButtonCellEditor(new JButton(deleteRange));
			} else {
				return super.getCellEditor(row, column);
			}
		}

		//set sizes for columns and rows of this table.
		protected void sizeColumnsAndRows() {
			for (int i = 0; i < columnSizes.length; i++) {
				getColumnModel().getColumn(i).setMinWidth(columnSizes[i]);
				if (i == columnSizes.length - 1) {
					getColumnModel().getColumn(i).setMaxWidth(columnSizes[i]);
				}
			}
		}

	}

	protected class LoadDependentRoutingStrategyTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		protected String[] columnNames = null;
		protected Class<?>[] colClasses = null;

		public LoadDependentRoutingStrategyTableModel() {
			columnNames = new String[validForwardStationKeys.size() + 3];
			colClasses = new Class[validForwardStationKeys.size() + 3];
			columnNames[0] = "From";
			colClasses[0] = String.class;
			columnNames[1] = "To";
			colClasses[1] = String.class;
			int columnIndex = 2;
			for (Object stationKey : validForwardStationKeys) {
				columnNames[columnIndex] = stations.getStationName(stationKey);
				colClasses[columnIndex] = String.class;
				columnIndex++;
			}
			columnNames[columnIndex] = "";
			colClasses[columnIndex] = JButton.class;
		}

		/**
		 * Returns the number of rows in the model. A
		 * <code>JTable</code> uses this method to determine how many rows it
		 * should display.  This method should be quick, as it
		 * is called frequently during rendering.
		 *
		 * @return the number of rows in the model
		 * @see #getColumnCount
		 */
		public int getRowCount() {
			return routingStrategy.getAllEmpiricalEntries().size();
		}

		/**
		 * Returns the number of columns in the model. A
		 * <code>JTable</code> uses this method to determine how many columns it
		 * should create and display by default.
		 *
		 * @return the number of columns in the model
		 * @see #getRowCount
		 */
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex < columnNames.length) {
				return columnNames[columnIndex];
			} else {
				return null;
			}
		}

		/**Returns class describing data contained in specific column.*/
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex < colClasses.length) {
				return colClasses[columnIndex];
			} else {
				return Object.class;
			}
		}

		/**
		 * Returns the value for the cell at <code>columnIndex</code> and
		 * <code>rowIndex</code>.
		 *
		 * @param    rowIndex    the row whose value is to be queried
		 * @param    columnIndex the column whose value is to be queried
		 * @return the value Object at the specified cell
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			Integer from = mapRowIndexAndFrom.get(rowIndex);
			Object to = "\u221e";
			int nextRowIndex = rowIndex + 1;
			if (mapRowIndexAndFrom.get(nextRowIndex) != null) {
				Integer nextRowFrom = mapRowIndexAndFrom.get(nextRowIndex);
				to = nextRowFrom - 1;
			}
			switch (columnIndex) {
			case (0): {
				return from;
			}
			case (1): {
				return to;
			}
			default: {
				Map<Object, Double> columnEntries = routingStrategy.getEmpiricalEntriesForFrom(from);
				if (columnIndex < validForwardStationKeys.size() + 2) {
					Object stationKey = validForwardStationKeys.get(columnIndex - 2);
					return columnEntries.get(stationKey);
				} else {
					return null;
				}
			}
			}
		}

		/**Tells whether data contained in a specific cell(given row and column index)
		 * is editable or not. In this case distribution column is not editable, as
		 * editing functionality is implemented via edit button*/
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return !(rowIndex == 0 && (columnIndex == 0 || columnIndex == validForwardStationKeys.size() + 2)) && columnIndex != 1;
		}

		/**Puts edited values to the underlying data structure for model implementation*/
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Integer from = mapRowIndexAndFrom.get(rowIndex);
			switch (columnIndex) {
			case (0): {
				try {
					Integer value = Integer.valueOf((String) aValue);
					if (value.intValue() < 2) {
						return;
					}
					if (mapRowIndexAndFrom.values().contains(value)) {
						return;
					}
					Map<Object, Double> columnEntries = routingStrategy.getEmpiricalEntriesForFrom(from);
					for (Entry<Object, Double> entry : columnEntries.entrySet()) {
						routingStrategy.addEmpiricalEntry(value, entry.getKey(), entry.getValue());
					}
					routingStrategy.removeEmpiricalEntriesForFrom(from);
					mapRowIndexAndFrom.put(rowIndex, value);
				} catch (NumberFormatException ignored) {
				}
				break;
			}
			case (1): {
				break;
			}
			default: {
				Map<Object, Double> columnEntries = routingStrategy.getEmpiricalEntriesForFrom(from);
				if (columnIndex < validForwardStationKeys.size() + 2) {
					Object stationKey = validForwardStationKeys.get(columnIndex - 2);
					columnEntries.put(stationKey, Double.valueOf((String) aValue));
				}
				break;
			}
			}

			// If from has changed, need to repaint the table
			if (columnIndex == 0) {
				//repaint();
				List<Integer> fromAsList = new ArrayList<Integer>();
				fromAsList.addAll(mapRowIndexAndFrom.values());
				Collections.sort(fromAsList);
				mapRowIndexAndFrom.clear();
				for (int row = 0; row < fromAsList.size(); row++) {
					mapRowIndexAndFrom.put(row, fromAsList.get(row));
				}
				refreshComponents();
			}
		}

	}

	private void showErrorMessageToUser(String arg) {
		JOptionPane.showMessageDialog(LoadDependentRoutingEditor.this, arg, "Stop! Verify", JOptionPane.WARNING_MESSAGE);
	}

}
