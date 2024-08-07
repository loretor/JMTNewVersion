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
package jmt.gui.common.panels.parametric;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jmt.gui.common.CommonConstants;
import jmt.gui.common.definitions.ClassDefinition;
import jmt.gui.common.definitions.GuiInterface;
import jmt.gui.common.definitions.SimulationDefinition;
import jmt.gui.common.definitions.StationDefinition;
import jmt.gui.common.definitions.parametric.NumberOfCustomerParametricAnalysis;
import jmt.gui.common.definitions.parametric.ParametricAnalysis;
import jmt.gui.table.ExactTable;
import jmt.gui.table.ExactTableModel;

/**
 * <p>Title: NumberOfCustomersPanel</p>
 * <p>Description: this is the panel for the parametric analysis where the
 *  global number of customers is changed. It contains a nested class <code>PopulationVectorTable</code>
 *  used to show the population composition</p>
 *
 * @author Francesco D'Aquino
 *         Date: 9-mar-2006
 *         Time: 16.17.48
 */
public class NumberOfCustomersPanel extends ParameterOptionPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JRadioButton allClass;
	private JRadioButton singleClass;
	private JLabel fromLabel;
	private JTextField from;
	private JLabel toLabel;
	private JSpinner to;
	private JLabel stepsLabel;
	private JSpinner steps;
	private JLabel classChooserLabel;
	private JComboBox classChooser;
	private JScrollPane scroll;
	private JTextArea description;
	private JScrollPane descrPane;
	private TitledBorder descriptionTitle;
	private String DESCRIPTION_SINGLE;
	private TablePanel tablePanel;
	private JPanel globalEditPanel;

	private NumberOfCustomerParametricAnalysis NCPA;
	private GuiInterface gui;

	public NumberOfCustomersPanel(NumberOfCustomerParametricAnalysis ncpa, ClassDefinition classDef, StationDefinition stationDef,
			SimulationDefinition simDef, GuiInterface guiInterface) {
		super();
		super.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		super.setDividerSize(3);
		DESCRIPTION = "Repeat the simulation with different number of jobs in each iteration, "
				+ "starting from the current number of jobs in the closed class.\n\n" + "The proportion of the number of jobs in the different "
				+ "classes will be kept constant, so the number of steps that can be practically executed "
				+ "may be very small (since only integer values are allowed).";
		DESCRIPTION_SINGLE = "Repeat the simulation with different number of jobs in each iteration, "
				+ "starting from the current number of jobs in the closed class, and increasing the number of jobs of" + " selected class only.\n\n";
		NCPA = ncpa;
		cd = classDef;
		sd = stationDef;
		simd = simDef;
		gui = guiInterface;
		initialize();
	}

	public void initialize() {
		JPanel radioButtonsPanel = new JPanel(new GridLayout(2, 1));
		allClass = new JRadioButton("Increase number of jobs of all closed classes");
		allClass.setToolTipText("Increase population of all closed classes");
		singleClass = new JRadioButton("Increase number of jobs of one closed class");
		singleClass.setToolTipText("Increase only population of one class");
		ButtonGroup group = new ButtonGroup();
		group.add(allClass);
		group.add(singleClass);
		radioButtonsPanel.add(allClass);
		radioButtonsPanel.add(singleClass);
		radioButtonsPanel.setBorder(new EmptyBorder(5, 20, 0, 20));
		if (cd.getClosedClassKeys().size() == 1) {
			allClass.setEnabled(false);
		}
		JPanel edit = new JPanel(new GridLayout(4, 1, 0, 5));
		classChooserLabel = new JLabel("Class: ");
		classChooser = new JComboBox();
		classChooser.setToolTipText("Choose the class whose number of jobs will be incremented");
		SpinnerNumberModel toModel;
		int minTo;
		if (NCPA.isSingleClass()) {
			Vector<Object> classes = cd.getClosedClassKeys();
			String[] classNames = new String[classes.size()];
			for (int i = 0; i < classes.size(); i++) {
				classNames[i] = cd.getClassName(classes.get(i));
			}
			classChooser.removeAllItems();
			for (String className : classNames) {
				classChooser.addItem(className);
			}
			classChooser.setEnabled(true);
			classChooser.setSelectedItem(cd.getClassName(NCPA.getReferenceClass()));
			singleClass.setSelected(true);
			allClass.setSelected(false);
			//set the minimum final value
			int totalPop = cd.getTotalClosedClassPopulation();
			minTo = totalPop + 1;
		} else {
			classChooser.removeAllItems();
			classChooser.addItem("All closed classes");
			classChooser.setEnabled(false);
			singleClass.setSelected(false);
			allClass.setSelected(true);
			// set the minimum final value.
			Vector<Object> classes = cd.getClosedClassKeys();
			int popGCD = cd.getClassPopulation(classes.get(0));
			for (int i = 1; i < classes.size(); i++) {
				int classPop = cd.getClassPopulation(classes.get(i));
				popGCD = findGCD(popGCD, classPop);
			}
			int totalPop = cd.getTotalClosedClassPopulation();
			minTo = totalPop + totalPop / popGCD;
		}
		fromLabel = new JLabel("From N: ");
		from = new JTextField();
		from.setEnabled(false);
		from.setText(Integer.toString((int) NCPA.getInitialValue()));
		from.setBorder(new TitledBorder(""));
		from.setBackground(Color.WHITE);
		from.setAlignmentX(SwingConstants.EAST);
		from.setToolTipText("To change number of jobs double click here");
		toLabel = new JLabel("To N: ");
		toModel = new SpinnerNumberModel((int) NCPA.getFinalValue(), minTo, Integer.MAX_VALUE, 1);
		to = new JSpinner(toModel);
		to.setToolTipText("Set the final number of jobs");
		stepsLabel = new JLabel("Steps (n. of exec.): ");
		int maximumSteps = NCPA.searchForAvailableSteps();
		if (maximumSteps > ParametricAnalysis.MAX_STEP_NUMBER) {
			maximumSteps = ParametricAnalysis.MAX_STEP_NUMBER;
		}
		steps = new JSpinner(new SpinnerNumberModel(NCPA.getNumberOfSteps(), 2, maximumSteps, 1));
		steps.setToolTipText("Set the number of steps to be performed");
		edit.add(fromLabel);
		edit.add(from);
		edit.add(toLabel);
		edit.add(to);
		edit.add(stepsLabel);
		edit.add(steps);
		edit.add(classChooserLabel);
		edit.add(classChooser);
		edit.setPreferredSize(new Dimension((int)(130 * CommonConstants.widthScaling), (int)(100 * CommonConstants.heightScaling)));
		JPanel editLables = new JPanel(new GridLayout(4, 1, 0, 5));
		editLables.add(fromLabel);
		editLables.add(toLabel);
		editLables.add(stepsLabel);
		editLables.add(classChooserLabel);
		editLables.setPreferredSize(new Dimension((int)(100 * CommonConstants.widthScaling), (int)(100 * CommonConstants.heightScaling)));
		JPanel editPanel = new JPanel();
		editPanel.add(editLables);
		editPanel.add(edit);
		editPanel.setBorder(new EmptyBorder(10, 0, 0, 10));
		//Create values table and add to JScrollPane
		tablePanel = new TablePanel();
		globalEditPanel = new JPanel(new BorderLayout());
		globalEditPanel.add(tablePanel, BorderLayout.CENTER);
		globalEditPanel.add(editPanel, BorderLayout.NORTH);
		JPanel cont = new JPanel(new BorderLayout());
		cont.add(radioButtonsPanel, BorderLayout.NORTH);
		cont.add(globalEditPanel, BorderLayout.CENTER);
		scroll = new JScrollPane(cont);
		title = new TitledBorder("Type of population growth");
		scroll.setBorder(title);
		description = new JTextArea();
		if (NCPA.isSingleClass()) {
			description.setText(DESCRIPTION_SINGLE);
		} else {
			description.setText(DESCRIPTION);
		}
		description.setOpaque(false);
		description.setEditable(false);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		descrPane = new JScrollPane(description);
		descriptionTitle = new TitledBorder(new EtchedBorder(), "Description");
		descrPane.setBorder(descriptionTitle);
		descrPane.setMinimumSize(new Dimension((int)(80 * CommonConstants.widthScaling), (int)(0 * CommonConstants.heightScaling)));
		scroll.setMinimumSize(new Dimension((int)(360 * CommonConstants.widthScaling), (int)(0 * CommonConstants.heightScaling)));
		setLeftComponent(scroll);
		setRightComponent(descrPane);
		setListeners();
		this.setBorder(new EmptyBorder(5, 0, 5, 0));
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (cd.getClosedClassKeys().size() == 1) {
			allClass.setEnabled(false);
		} else {
			allClass.setEnabled(enabled);
		}
		singleClass.setEnabled(enabled);
		fromLabel.setEnabled(enabled);
		from.setEnabled(false);
		toLabel.setEnabled(enabled);
		to.setEnabled(enabled);
		stepsLabel.setEnabled(enabled);
		steps.setEnabled(enabled);
		classChooserLabel.setEnabled(enabled);
		description.setEnabled(enabled);
		if (!enabled) {
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			descrPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		} else {
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			descrPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		}
		if (!enabled) {
			classChooser.setEnabled(enabled);
		} else if (singleClass.isSelected()) {
			classChooser.setEnabled(enabled);
		}
		if (!enabled) {
			title.setTitleColor(Color.LIGHT_GRAY);
			descriptionTitle.setTitleColor(Color.LIGHT_GRAY);
			globalEditPanel.remove(tablePanel);
			globalEditPanel.doLayout();
			globalEditPanel.validate();
		} else {
			title.setTitleColor(DEFAULT_TITLE_COLOR);
			descriptionTitle.setTitleColor(DEFAULT_TITLE_COLOR);
			tablePanel = new TablePanel();
			globalEditPanel.add(tablePanel, BorderLayout.CENTER);
			globalEditPanel.doLayout();
			globalEditPanel.validate();
		}
	}

	private void setListeners() {
		singleClass.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					return;
				}
				NCPA.setSingleClass(true);
				classChooser.setEnabled(true);
				ItemListener listener = classChooser.getItemListeners()[0];
				classChooser.removeItemListener(listener);
				classChooser.removeAllItems();
				Vector<Object> classes = cd.getClosedClassKeys();
				String[] classNames = new String[classes.size()];
				for (int i = 0; i < classes.size(); i++) {
					classNames[i] = cd.getClassName(classes.get(i));
				}
				for (String className : classNames) {
					classChooser.addItem(className);
				}
				classChooser.addItemListener(listener);
				if (!classes.contains(NCPA.getReferenceClass())) {
					NCPA.setReferenceClass(classes.get(0));
				}
				classChooser.setSelectedItem(cd.getClassName(NCPA.getReferenceClass()));
				int totalPop = cd.getTotalClosedClassPopulation();
				int minTo = totalPop + 1;
				ChangeListener cl = to.getChangeListeners()[0];
				to.removeChangeListener(cl);
				SpinnerNumberModel toModel = (SpinnerNumberModel) to.getModel();
				toModel.setMinimum(new Integer(minTo));
				to.addChangeListener(cl);
				int oldStep = ((Integer) steps.getValue()).intValue();
				int newMaximumStep = NCPA.searchForAvailableSteps();
				if (newMaximumStep > ParametricAnalysis.MAX_STEP_NUMBER) {
					newMaximumStep = ParametricAnalysis.MAX_STEP_NUMBER;
				}
				SpinnerNumberModel stepModel = (SpinnerNumberModel) steps.getModel();
				if (oldStep > newMaximumStep) {
					NCPA.setNumberOfSteps(newMaximumStep);
					steps.setValue(new Integer(newMaximumStep));
				}
				stepModel.setMaximum(new Integer(newMaximumStep));
				description.setText(DESCRIPTION_SINGLE);
				tablePanel.repaint();
				tablePanel.setSelectedClass(NCPA.getReferenceClass());
			}
		});
		allClass.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					return;
				}
				NCPA.setSingleClass(false);
				Vector<Object> classes = cd.getClosedClassKeys();
				int popGCD = cd.getClassPopulation(classes.get(0));
				for (int i = 1; i < classes.size(); i++) {
					int classPop = cd.getClassPopulation(classes.get(i));
					popGCD = findGCD(popGCD, classPop);
				}
				int totalPop = cd.getTotalClosedClassPopulation();
				int minTo = totalPop + totalPop / popGCD;
				if (NCPA.getFinalValue() < minTo) {
					NCPA.setFinalValue(minTo);
				}
				SpinnerNumberModel toModel = (SpinnerNumberModel) to.getModel();
				toModel.setValue(new Integer((int) NCPA.getFinalValue()));
				toModel.setMinimum(new Integer(minTo));
				classChooser.removeAllItems();
				classChooser.addItem("All closed classes");
				classChooser.setEnabled(false);
				int newMaximumStep = NCPA.searchForAvailableSteps();
				if (newMaximumStep > ParametricAnalysis.MAX_STEP_NUMBER) {
					newMaximumStep = ParametricAnalysis.MAX_STEP_NUMBER;
				}
				int oldStep = ((Integer) steps.getValue()).intValue();
				SpinnerNumberModel stepModel = (SpinnerNumberModel) steps.getModel();
				if (oldStep > newMaximumStep) {
					NCPA.setNumberOfSteps(newMaximumStep);
					steps.setValue(new Integer(newMaximumStep));
				}
				stepModel.setMaximum(new Integer(newMaximumStep));
				description.setText(DESCRIPTION);
				tablePanel.repaint();
				tablePanel.setSelectedClass(null);
			}
		});
		to.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (to.getValue() instanceof Integer) {
					Integer tValue = (Integer) to.getValue();
					Integer sValue = (Integer) steps.getValue();
					SpinnerNumberModel snm = (SpinnerNumberModel) steps.getModel();
					int oldValue = (int) NCPA.getFinalValue();
					NCPA.setFinalValue(tValue.intValue());
					int newSteps = NCPA.searchForAvailableSteps();
					if (newSteps > ParametricAnalysis.MAX_STEP_NUMBER) {
						newSteps = ParametricAnalysis.MAX_STEP_NUMBER;
					}
					if (newSteps >= 2) {
						if (sValue.intValue() > newSteps) {
							steps.setValue(new Integer(newSteps));
							NCPA.setNumberOfSteps(newSteps);
						}
						snm.setMaximum(new Integer(newSteps));
					} else {
						JOptionPane.showMessageDialog(to,
								"The inserted number of final jobs is too close to the initial one. The number of steps must be at least 2",
								"JMT - Error", JOptionPane.ERROR_MESSAGE);
						NCPA.setFinalValue(oldValue); //set the old value
					}
				}
			}
		});
		steps.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (steps.getValue() instanceof Integer) {
					int sValue = ((Integer) steps.getValue()).intValue();
					int sMax = ((Integer) ((SpinnerNumberModel) steps.getModel()).getMaximum()).intValue();
					int sMin = ((Integer) ((SpinnerNumberModel) steps.getModel()).getMinimum()).intValue();
					if ((sValue >= sMin) && (sValue <= sMax)) {
						NCPA.setNumberOfSteps(sValue);
					}
				}
				steps.setValue(new Integer(NCPA.getNumberOfSteps()));
			}
		});
		classChooser.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					return;
				}
				String className = (String) classChooser.getSelectedItem();
				Object classKey = null;
				Vector<Object> classes = cd.getClassKeys();
				//for cycle used to get the key of the selected class
				for (int i = 0; i < classes.size(); i++) {
					if (cd.getClassName(classes.get(i)).equals(className)) {
						classKey = classes.get(i);
						break;
					}
				}
				NCPA.setReferenceClass(classKey);
				int newMaximumStep = NCPA.searchForAvailableSteps();
				if (newMaximumStep > ParametricAnalysis.MAX_STEP_NUMBER) {
					newMaximumStep = ParametricAnalysis.MAX_STEP_NUMBER;
				}
				int oldStep = ((Integer) steps.getValue()).intValue();
				SpinnerNumberModel stepModel = (SpinnerNumberModel) steps.getModel();
				if (oldStep > newMaximumStep) {
					NCPA.setNumberOfSteps(newMaximumStep);
					steps.setValue(new Integer(newMaximumStep));
				}
				stepModel.setMaximum(new Integer(newMaximumStep));
				tablePanel.repaint();
				tablePanel.setSelectedClass(NCPA.getReferenceClass());
			}
		});
	}

	private int findGCD(int a, int b) {
		if (b == 0) {
			return a;
		}
		return findGCD(b, a % b);
	}

	protected class TablePanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		PopulationVectorTable table;
		JScrollPane jsp;
		JPanel globalPanel;
		JLabel title;
		JPanel titlePanel;

		public TablePanel() {
			table = new PopulationVectorTable();
			table.setAutoscrolls(true);
			jsp = new JScrollPane(table);
			jsp.setBorder(new EmptyBorder(10, 10, 10, 10));
			jsp.setPreferredSize(new Dimension((int)(202 * CommonConstants.widthScaling), (int)(122 * CommonConstants.heightScaling)));
			title = new JLabel("<html><b>Population mix</b>");
			titlePanel = new JPanel(new FlowLayout());
			titlePanel.add(title);
			globalPanel = new JPanel(new BorderLayout());
			globalPanel.add(titlePanel, BorderLayout.NORTH);
			globalPanel.add(jsp, BorderLayout.CENTER);
			this.setLayout(new BorderLayout());
			this.add(globalPanel, BorderLayout.NORTH);
		}

		public void setSelectedClass(Object selectedClass) {
			int column = 0;
			Vector<Object> closedClasses = cd.getClosedClassKeys();
			if (NCPA.isSingleClass()) {
				for (int i = 0; i < closedClasses.size(); i++) {
					if (closedClasses.get(i) == selectedClass) {
						column = i;
						break;
					}
				}
			}
			double scrollMax = jsp.getHorizontalScrollBar().getMaximum();
			double thisPos = (((double) column) / (double) closedClasses.size()) * scrollMax;
			jsp.getHorizontalScrollBar().setValue((int) thisPos);
		}
	}

	/**
	 * Inner class to show jobs composition
	 */
	protected class PopulationVectorTable extends ExactTable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();

		public PopulationVectorTable() {
			super(new PopulationVectorTableModel());
			autoResizeMode = AUTO_RESIZE_OFF;
			setRowSelectionAllowed(true);
			setColumnSelectionAllowed(true);
			setRowHeaderWidth(40);
			setRowHeight(CommonConstants.ROW_HEIGHT);
		}

		/**
		 * The original getCellRenderer method is overwritten, since the table
		 * displays in red the values of the selected class
		 * @param row the row of the cell
		 * @param column the column of the cell
		 * @return a the TableCellRenderer for the requested cell (row,column)
		 */
		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			dtcr.setHorizontalAlignment(SwingConstants.CENTER);
			Component c;
			if (column < cd.getClosedClassKeys().size()) {
				Vector<Object> closedClasses = cd.getClosedClassKeys();
				Object thisClass = closedClasses.get(column);
				int thisPop = cd.getClassPopulation(thisClass).intValue();
				c = dtcr.getTableCellRendererComponent(this, Integer.toString(thisPop), false, false, row, column);
				if (NCPA.isSingleClass()) {
					if (thisClass == NCPA.getReferenceClass()) {
						c.setForeground(Color.RED);
					} else {
						c.setForeground(Color.BLACK);
					}
				} else {
					c.setForeground(Color.RED);
				}
			} else {
				c = dtcr.getTableCellRendererComponent(this, "-", false, false, row, column);
				c.setForeground(Color.BLACK);
			}
			((JLabel) c).setToolTipText(cd.getClassName(cd.getClosedClassKeys().get(column)));
			return dtcr;
		}
	}

	/**
	 * Model for PopulationVectorTable
	 * Columns represent the closed classes names.
	 */
	protected class PopulationVectorTableModel extends ExactTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public PopulationVectorTableModel() {
			//prototype = "XXXX.XXX XXX";
			rowHeaderPrototype = "XXX";
		}

		@Override
		public Object getPrototype(int i) {
			if (i == -1) {
				return rowHeaderPrototype;
			} else {
				Vector<Object> classes = cd.getClosedClassKeys();
				int max = 0;
				int index = 0;
				for (int k = 0; k < classes.size(); k++) {
					String name = cd.getClassName(classes.get(k));
					int length = name.length();
					if (length > max) {
						max = length;
						index = k;
					}
				}
				prototype = cd.getClassName(classes.get(index)) + "  ";
				return prototype;
			}
		}

		/**
		 * @return the object at (rowIndex, columnIndex)
		 */
		@Override
		protected Object getValueAtImpl(int rowIndex, int columnIndex) {
			String toReturn;
			Object thisClass = cd.getClosedClassKeys().get(columnIndex);
			double thisPop = cd.getClassPopulation(thisClass).doubleValue();
			double totalPop = cd.getTotalClosedClassPopulation();
			if (rowIndex == 0) {
				toReturn = Integer.toString((int) thisPop);
			} else {
				DecimalFormat twoDec = new DecimalFormat("0.00");
				double beta = 0;
				if (totalPop > 0) {
					beta = thisPop / totalPop;
				}
				toReturn = twoDec.format(beta);
			}
			return toReturn;
		}

		public int getRowCount() {
			return 2;
		}

		public int getColumnCount() {
			return cd.getClosedClassKeys().size();
		}

		/**
		 * @return the header for row <code>rowIndex</code>
		 */
		@Override
		protected Object getRowName(int rowIndex) {
			if (rowIndex == 0) {
				return "Ni";
			} else {
				return "ßi";
			}
		}

		/**
		 * Gets the name of the column, given the column index
		 * @param index the index of the column to give the name
		 * @return the column name
		 */
		@Override
		public String getColumnName(int index) {
			return cd.getClassName(cd.getClosedClassKeys().get(index));
		}
	}
}
