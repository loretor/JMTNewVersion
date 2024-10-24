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
import java.text.DecimalFormat;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import jmt.framework.data.ArrayUtils;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.table.ExactTable;
import jmt.gui.table.ExactTableModel;
import jmt.gui.jaba.JabaConstants;
import jmt.gui.jaba.JabaModel;
import jmt.gui.jaba.JabaWizard;
import jmt.jmva.gui.panels.ForceUpdatablePanel;

/**

 * @author alyf (Andrea Conti)
 * Date: 11-set-2003
 * Time: 23.48.19

 */

/**
 * 3rd panel: visits
 */
public final class VisitsPanel extends WizardPanel implements JabaConstants, ForceUpdatablePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Bertoli Marco - Used to show only two decimal digits
	private static DecimalFormat formatter = new DecimalFormat("#0.000");

	private JabaWizard ew;
	private HoverHelp help;
	private static final String helpText = "<html>In this panel you can edit the station visits for each class.<br><br>"
			+ " To enter values, double-click on the desired cell"
			+ " and start typing.<br> To select multiple cells drag the mouse on them; click or drag on"
			+ " row/column headers to select whole rows/columns.<br> <b>For a list of the available operations right-click"
			+ " on the table</b>; all operations except pasting affect selected cells.<br>"
			+ " To copy one value to multiple cells click on the cell containing the value, select the"
			+ " target cells by dragging and select <b>\"Fill\"</b>.<br><br></html>";

	private int classes;

	private int stations;
	private String[] classNames;
	private String[] stationNames;
	private double[][] visits;

	private VisitTable visitTable;

	public VisitsPanel(JabaWizard ew) {
		this.ew = ew;
		help = ew.getHelp();

		/* sync status with data object */
		sync();
		initComponents();
	}

	/**
	 * gets status from data object
	 */
	private void sync() {
		/* arrays are copied to ensure data object consistency is preserved */
		JabaModel data = ew.getData();
		synchronized (data) {
			classes = data.getClasses();
			stations = data.getStations();

			classNames = data.getClassNames();
			stationNames = data.getStationNames();
			visits = ArrayUtils.copy2(data.getVisits());
		}
	}

	/**
	 * Set up the panel contents and layout
	 */
	private void initComponents() {

		visitTable = new VisitTable();

		JPanel totalBox = new JPanel(new BorderLayout(10, 10));

		//Horizontal box containing Description label and buttons
		JLabel descrLabel = new JLabel(DESCRIPTION_VISITS);
		JPanel descrBox = new JPanel(new BorderLayout());
		descrBox.setPreferredSize(new Dimension((int)(200 * CommonConstants.widthScaling), (int)(1000 * CommonConstants.heightScaling)));
		descrBox.add(descrLabel, BorderLayout.NORTH);

		JScrollPane visitTablePane = new JScrollPane(visitTable);
		visitTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		visitTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		totalBox.add(visitTablePane, BorderLayout.CENTER);
		totalBox.add(descrBox, BorderLayout.WEST);

		setLayout(new BorderLayout());
		add(totalBox, BorderLayout.CENTER);
		add(Box.createVerticalStrut(30), BorderLayout.NORTH);
		add(Box.createVerticalStrut(30), BorderLayout.SOUTH);
		add(Box.createHorizontalStrut(20), BorderLayout.EAST);
		add(Box.createHorizontalStrut(20), BorderLayout.WEST);

	}

	@Override
	public String getName() {
		return "Visits";
	}

	private void commit() {

		visitTable.stopEditing();

		JabaModel data = ew.getData();
		synchronized (data) {
			data.setVisits(visits);
		}
	}

	@Override
	public void gotFocus() {
		sync();
		visitTable.updateStructure();
	}

	@Override
	public void lostFocus() {
		commit();
		//release();
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

	private class VisitTable extends ExactTable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		VisitTable() {
			super(new VisitTableModel());
			autoResizeMode = AUTO_RESIZE_OFF;
			setRowHeight(CommonConstants.ROW_HEIGHT);

			setDisplaysScrollLabels(true);
			help.addHelp(this,
					"Click or drag to select cells; to edit data double-click and start typing. Right-click for a list of available operations");
			help.addHelp(moreColumnsLabel, "There are more classes: scroll right to see them");
			help.addHelp(moreRowsLabel, "There are more stations: scroll down to see them");
			help.addHelp(selectAllButton, "Click to select all cells");
			tableHeader.setToolTipText(null);
			help.addHelp(tableHeader, "Click, SHIFT-click or drag to select columns");
			rowHeader.setToolTipText(null);
			help.addHelp(rowHeader, "Click, SHIFT-click or drag to select rows");

			setRowSelectionAllowed(true);
			setColumnSelectionAllowed(true);
			setBatchEditingEnabled(true);
		}

	}

	/**
	 * the model backing the visit table.
	 * Rows represent stations, columns classes.
	 */
	private class VisitTableModel extends ExactTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		VisitTableModel() {
			prototype = new Double(1000);
			rowHeaderPrototype = "Station1000";
		}

		public int getRowCount() {
			return stations;
		}

		public int getColumnCount() {
			return classes;
		}

		@Override
		protected Object getRowName(int rowIndex) {
			return stationNames[rowIndex];
		}

		@Override
		public String getColumnName(int index) {
			return classNames[index];
		}

		@Override
		protected Object getValueAtImpl(int rowIndex, int columnIndex) {
			return formatter.format(visits[rowIndex][columnIndex]);
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			try {
				double newVal = Double.parseDouble((String) value);
				if (newVal <= 0.01) {
					visits[rowIndex][columnIndex] = 0.01;
				} else {
					visits[rowIndex][columnIndex] = newVal;
				}
			} catch (NumberFormatException e) {
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void clear(int row, int col) {
			visits[row][col] = 0.01;
		}

	}
}
