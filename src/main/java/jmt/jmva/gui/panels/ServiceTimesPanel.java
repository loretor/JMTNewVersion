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

package jmt.jmva.gui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jmt.framework.data.ArrayUtils;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.table.editors.ButtonCellEditor;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.jmva.LDEditor;
import jmt.gui.table.ExactTable;
import jmt.gui.table.ExactTableModel;
import jmt.jmva.analytical.ExactConstants;
import jmt.jmva.analytical.ExactModel;
import jmt.jmva.gui.JMVAWizard;

/**

 * @author alyf (Andrea Conti)
 * Date: 11-set-2003
 * Time: 23.48.19

 */

/**
 * 4th panel: service times
 */
public final class ServiceTimesPanel extends WizardPanel implements ExactConstants, ForceUpdatablePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JMVAWizard ew;
	private HoverHelp help;
	private static final String helpText = "<html>In this panel you can edit the service times of LI and delay stations for each class.<br><br>"
			+ " To enter values, double-click on the desired cell"
			+ " and start typing.<br> To select multiple cells drag the mouse on them; click or drag on"
			+ " row/column headers to select whole rows/columns.<br> <b>For a list of the available operations right-click"
			+ " on the table</b>; all operations except pasting affect selected cells.<br>"
			+ " To copy one value to multiple cells click on the cell containing the value, select the"
			+ " target cells by dragging and select <b>\"Fill\"</b>.<br><br>"
			+ " To edit service times of an LD station double-click anywhere on its row.<br></html>";

	private boolean zeroLD;

	private int classes;
	private int stations;
	private String[] classNames;
	private String[] stationNames;
	private int[] stationTypes;
	private double[][][] serviceTimes;

	private STTable stTable;

	private AbstractAction SWITCH_TO_SD = new AbstractAction("Service Demands") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Input by Service Demands");
		}

		public void actionPerformed(ActionEvent e) {
			ew.switchFromSTVtoSD();
		}
	};

	public ServiceTimesPanel(JMVAWizard ew) {
		this.ew = ew;
		help = ew.getHelp();

		/* sync status with data object */
		sync();
		initComponents();
	}

	/**
	 * gets status from data object
	 */
	public void sync() {
		/* arrays are copied to ensure data object consistency is preserved */
		ExactModel data = ew.getData();
		synchronized (data) {
			zeroLD = (data.isLd() && (data.getTotalPop() == 0));
			classes = data.getClasses();
			stations = data.getStations();

			classNames = data.getClassNames();
			stationNames = data.getStationNames();
			stationTypes = data.getStationTypes();

			serviceTimes = ArrayUtils.copy3(data.getServiceTimes());
		}
	}

	/**
	 * Set up the panel contents and layout
	 */
	private void initComponents() {
		stTable = new STTable();

		Box hBox = Box.createHorizontalBox();
		hBox.add(Box.createHorizontalStrut(20));
		//Horizontal box containing Description label and buttons
		Box descrBox = Box.createVerticalBox();
		descrBox.add(new JLabel(DESCRIPTION_SERVICETIMES));
		descrBox.add(Box.createHorizontalStrut(10));
		descrBox.add(new JButton(SWITCH_TO_SD));
		descrBox.setPreferredSize(new Dimension(220, 1000));
		descrBox.setMinimumSize(new Dimension(200, 200));

		hBox.add(descrBox);
		hBox.add(Box.createHorizontalStrut(10));
		JScrollPane visitTablePane = new JScrollPane(stTable);
		visitTablePane.setPreferredSize(new Dimension(1000, 1000));
		visitTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		visitTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		hBox.add(visitTablePane);
		hBox.add(Box.createHorizontalStrut(20));

		Box totalBox = Box.createVerticalBox();
		totalBox.add(Box.createVerticalStrut(30));
		totalBox.add(hBox);
		totalBox.add(Box.createVerticalStrut(30));

		setLayout(new BorderLayout());
		add(totalBox, BorderLayout.CENTER);
	}

	@Override
	public String getName() {
		return "Service Times";
	}

	private void commit() {
		stTable.stopEditing();

		ExactModel data = ew.getData();
		synchronized (data) {
			if (data.setServiceTimes(serviceTimes)) {
				data.recalculateWhatifValues();
			}
		}
	}

	@Override
	public void gotFocus() {
		sync();
		stTable.updateStructure();
	}

	@Override
	public void lostFocus() {
		commit();
		//release();
	}

	/**
	 * Make sure we cannot finish if we are editing LD data
	 */
	@Override
	public boolean canFinish() {
		return !stTable.isLDEditing();
	}

	/**
	 * Make sure we cannot switch tabs if we are editing LD data
	 */
	@Override
	public boolean canGoBack() {
		return !stTable.isLDEditing();
	}

	/**
	 * Make sure we cannot switch tabs if we are editing LD data
	 */
	@Override
	public boolean canGoForward() {
		return !stTable.isLDEditing();
	}

	@Override
	public void help() {
		JOptionPane.showMessageDialog(this, helpText, "Help", JOptionPane.INFORMATION_MESSAGE);

	}

	/**{@see ForceUpdatablePanel} for further details*/
	public void retrieveData() {
		this.sync();
	}

	/**{@see ForceUpdatablePanel} for further details*/
	public void commitData() {
		this.commit();
	}

	private class STTable extends ExactTable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private LDEditor ldEditor;

		STTable() {
			super(new STTableModel());
			autoResizeMode = AUTO_RESIZE_OFF;
			setRowHeight(CommonConstants.ROW_HEIGHT);

			setDisplaysScrollLabels(true);
			setRowSelectionAllowed(true);
			setColumnSelectionAllowed(true);
			setBatchEditingEnabled(true);

			help.addHelp(this,
					"Click or drag to select cells; to edit data double-click and start typing. Right-click for a list of available operations");
			help.addHelp(moreColumnsLabel, "There are more classes: scroll right to see them");
			help.addHelp(moreRowsLabel, "There are more stations: scroll down to see them");
			help.addHelp(selectAllButton, "Click to select all cells");
			tableHeader.setToolTipText(null);
			help.addHelp(tableHeader, "Click, SHIFT-click or drag to select columns");
			rowHeader.setToolTipText(null);
			help.addHelp(rowHeader, "Click, SHIFT-click or drag to select rows");
		}

		/**
		 * We do not want selectable columns
		 */
		/*public boolean getColumnSelectionAllowed() {return false;} */

		/**
		 * A little hack to make LD rows selectable
		 * @return true iif the focused cell is on a LD row
		 */
		/*public boolean getRowSelectionAllowed() {
			if (stationTypes[selectionModel.getAnchorSelectionIndex()]==STATION_LD) return true;
			return false;
		} */

		/**
		 * @return true if the LDEditor window is currently open
		 */
		public boolean isLDEditing() {
			return (cellEditor instanceof LDEditor);
		}

		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (stationTypes[row] == STATION_LD) {
				return new ButtonCellEditor(new JButton("LD Settings..."));
			} else {
				return super.getCellRenderer(row, column);
			}
		}

		/**
		 * Overridden to obtain a different editor for LD stations.
		 * Cannot choose editor via standard JTable mechanism because cell type is not column-dependent
		 */
		@Override
		public TableCellEditor getCellEditor(int row, int column) {
			if (stationTypes[row] == STATION_LD) {
				return getLDEditor();
			} else {
				return super.getCellEditor(row, column);
			}
		}

		/**
		 * the LDEditor needs to be treated in a special way.
		 */
		@Override
		public Component prepareEditor(TableCellEditor editor, int row, int column) {
			if (editor instanceof LDEditor) {
				LDEditor lde = ((LDEditor) editor);
				lde.setStatus("Editing Service Times of " + stationNames[row], classNames[column], serviceTimes[row][column]);
				lde.startEditing(this, row);

				return getCellRenderer(row, column).getTableCellRendererComponent(this, "LD Settings...", true, true, row, column);
			} else {
				return super.prepareEditor(editor, row, column);
			}
		}

		/**
		 * If the request is to edit ld times in a system with zero customers, shows a warning messages and returns false.
		 * Otherwise passes request to superclass method
		 */
		@Override
		public boolean editCellAt(int row, int col, EventObject e) {
			if (zeroLD && (stationTypes[row] == STATION_LD)) {
				JOptionPane.showMessageDialog(ServiceTimesPanel.this,
						"<html><center>Cannot edit LD service times in a system with zero customers</center></html>", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			return super.editCellAt(row, col, e);
		}

		/**
		 * @return a lazily created LDEditor. Creation of the LDEditor at initialization time is not possible
		 * because the LDEditor needs a reference to the root Frame.
		 */
		private LDEditor getLDEditor() {
			if (ldEditor == null) {
				ldEditor = new LDEditor(parentWizard);
			}
			return ldEditor;
		}

	}

	/**
	 * the model backing the service times table.
	 * Rows represent stations, columns classes.
	 */
	private class STTableModel extends ExactTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		STTableModel() {
			prototype = "LD Settings Button";
			rowHeaderPrototype = "Station10000";
		}

		public int getRowCount() {
			return stations;
		}

		public int getColumnCount() {
			return classes;
		}

		@Override
		public String getColumnName(int index) {
			return classNames[index];
		}

		@Override
		protected Object getValueAtImpl(int rowIndex, int columnIndex) {
			switch (stationTypes[rowIndex]) {
			case STATION_DELAY:
			case STATION_LI:
			case STATION_PRS:
			case STATION_HOL:
				return new Double(serviceTimes[rowIndex][columnIndex][0]);
			case STATION_LD:
				return "LD";
			default:
				return null;
			}
		}

		@Override
		protected Object getRowName(int rowIndex) {
			return stationNames[rowIndex];
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (value instanceof String) { //coming from the defaultEditor
				try {
					double newVal = Double.parseDouble((String) value);
					if (newVal >= 0) {
						serviceTimes[rowIndex][columnIndex][0] = newVal;
					}
				} catch (NumberFormatException e) {
				}
			} else if (value instanceof double[]) { // coming from the LDEditor
				serviceTimes[rowIndex][columnIndex] = (double[]) value;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void clear(int row, int col) {
			if (stationTypes[row] == STATION_LD) {
				return;
			}
			serviceTimes[row][col][0] = 0;
		}

	}

}
