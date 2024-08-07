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

package jmt.gui.common.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import jmt.engine.log.JSimLogger;
import jmt.framework.gui.components.JMTFrame;
import jmt.framework.gui.graph.FastGraph;
import jmt.framework.gui.graph.MeasureValue;
import jmt.framework.gui.image.ImageLoader;
import jmt.framework.gui.layouts.SpringUtilities;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.JMTImageLoader;
import jmt.gui.common.definitions.AbortMeasure;
import jmt.gui.common.definitions.MeasureDefinition;
import jmt.gui.common.definitions.ResultsConstants;
import jmt.gui.common.definitions.SimulationDefinition;
import jmt.gui.common.editors.StatisticalOutputsWindow;

/**
 * <p>Title: Results Window</p>
 * <p>Description: Window to show simulation results.</p>
 * 
 * @author Bertoli Marco
 *         Date: 22-set-2005
 *         Time: 15.10.52
 *       
 * Modified by Ashanka (Nov 09):
 * Desc: Added the description of the Drop Rate
 * 
 * Modified by Ashanka (Nov 09):
 * Desc: Appended the values of various measures to the tool tip.
 * 
 * Modified by Ashanka (May 2010): 
 * Patch: Multi-Sink Perf. Index 
 * Description: Added new Performance index for capturing 
 * 				1. global response time (ResponseTime per Sink)
 *              2. global throughput (Throughput per Sink)
 *              each sink per class.
 */
public class ResultsWindow extends JMTFrame implements ResultsConstants {

	private static final long serialVersionUID = 1L;

	private final String TITLE = "Simulation Results";

	private SimulationDefinition sd;
	private MeasureDefinition results;
	private AbortMeasure abort;
	private JButton start;
	private JButton stop;
	private JButton pause;
	private JProgressBar progressBar;
	// Used to format numbers
	private static DecimalFormat decimalFormatNorm = new DecimalFormat("0.0000");
	private static DecimalFormat decimalFormatExp = new DecimalFormat("0.00E0");
	private JSimLogger logger = JSimLogger.getLogger(ResultsWindow.class);

	/**
	 * Creates a new ResultsWindow
	 * @param sd simulation definition data structure
	 * @param fileName name of simulation file
	 */
	public ResultsWindow(SimulationDefinition sd, String fileName) {
		this.sd = sd;
		this.results = sd.getSimulationResults();
		this.updateTitle(fileName);
		initGUI();
	}

	/**
	 * Creates a new ResultsWindow
	 * @param sd simulation definition data structure
	 * @param abort object used to implement abort button action
	 * @param fileName name of simulation file
	 */
	public ResultsWindow(SimulationDefinition sd, AbortMeasure abort, String fileName) {
		this.sd = sd;
		this.results = sd.getSimulationResults();
		this.abort = abort;
		this.updateTitle(fileName);
		initGUI();
	}

	/**
	 * Initialize all gui related stuff
	 */
	private void initGUI() {
		// Sets default title, close operation and dimensions
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.setIconImage(JMTImageLoader.loadImage("Results").getImage());
		this.centerWindow(CommonConstants.MAX_GUI_WIDTH_JSIM_RESULTS, CommonConstants.MAX_GUI_HEIGHT_JSIM_RESULTS);

		// Creates all tabs
		JTabbedPane mainPanel = new JTabbedPane();
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		addTabPane(mainPanel, SimulationDefinition.MEASURE_QL, DESCRIPTION_QUEUE_LENGTH, results.getQueueLengthMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_QT, DESCRIPTION_QUEUE_TIME, results.getQueueTimeMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_RP, DESCRIPTION_RESPONSE_TIME, results.getResponseTimeMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_RD, DESCRIPTION_RESIDENCE_TIME, results.getResidenceTimeMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_AR, DESCRIPTION_ARRIVAL_RATE, results.getArrivalRateMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_X, DESCRIPTION_THROUGHPUT, results.getThroughputMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_U, DESCRIPTION_UTILIZATION, results.getUtilizationMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_T, DESCRIPTION_TARDINESS, results.getTardinessMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_E, DESCRIPTION_EARLINESS, results.getEarlinessMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_L, DESCRIPTION_LATENESS, results.getLatenessMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_EU, DESCRIPTION_EFFECTIVE_UTILIZATION, results.getEffectiveUtilizationMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_DR, DESCRIPTION_DROP_RATE, results.getDropRateMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_BR, DESCRIPTION_BALKING_RATE, results.getBalkingRateMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_RN, DESCRIPTION_RENEGING_RATE, results.getRenegingRateMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_RT, DESCRIPTION_RETRIAL_ATTEMPTS_RATE, results.getRetrialAttemptsRateMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_OS, DESCRIPTION_RETRIAL_ORBIT_SIZE, results.getRetrialOrbitSizeMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_OT, DESCRIPTION_RETRIAL_ORBIT_TIME, results.getRetrialOrbitTimeMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_S_CN, DESCRIPTION_SYSTEM_CUSTOMER_NUMBER, results.getSystemCustomerNumberMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_S_RP, DESCRIPTION_SYSTEM_RESPONSE_TIME, results.getSystemResponseTimeMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_S_X, DESCRIPTION_SYSTEM_THROUGHPUT, results.getSystemThroughputMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_S_DR, DESCRIPTION_SYSTEM_DROP_RATE, results.getSystemDropRateMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_S_BR, DESCRIPTION_SYSTEM_BALKING_RATE, results.getSystemBalkingRateMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_S_RN, DESCRIPTION_SYSTEM_RENEGING_RATE, results.getSystemRenegingRateMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_S_RT, DESCRIPTION_SYSTEM_RETRIAL_ATTEMPTS_RATE, results.getSystemRetrialAttemptsRateMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_S_P, DESCRIPTION_SYSTEM_POWER, results.getSystemPowerMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_S_T, DESCRIPTION_SYSTEM_TARDINESS, results.getSystemTardinessMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_S_E, DESCRIPTION_SYSTEM_EARLINESS, results.getSystemEarlinessMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_S_L, DESCRIPTION_SYSTEM_LATENESS, results.getSystemLatenessMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_RP_PER_SINK, DESCRIPTION_RESPONSE_TIME_PER_SINK, results.getResponsetimePerSinkMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_X_PER_SINK, DESCRIPTION_THROUGHPUT_PER_SINK, results.getThroughputPerSinkMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_FCR_TW, DESCRIPTION_FCR_TOTAL_WEIGHT, results.getFCRTotalWeightMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_FCR_MO, DESCRIPTION_FCR_MEMORY_OCCUPATION, results.getFCRMemoryOccupationMeasures());	
		addTabPane(mainPanel, SimulationDefinition.MEASURE_FJ_CN, DESCRIPTION_FJ_CUSTOMER_NUMBER, results.getFJCustomerNumberMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_FJ_RP, DESCRIPTION_FJ_RESPONSE_TIME, results.getFJResponseTimeMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_FX, DESCRIPTION_FIRING_THROUGHPUT, results.getFiringThroughputMeasures());
		addTabPane(mainPanel, SimulationDefinition.MEASURE_NS, DESCRIPTION_NUMBER_OF_ACTIVE_SERVERS, results.getNumberOfActiveServersMeasures());
		// Creates bottom panel
		JPanel bottomPanel = new JPanel(new BorderLayout());
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		start = new JButton();
		toolbar.add(start);
		start.setVisible(false);
		pause = new JButton();
		toolbar.add(pause);
		pause.setVisible(false);
		stop = new JButton();
		toolbar.add(stop);
		stop.setVisible(false);
		bottomPanel.add(toolbar, BorderLayout.WEST);
		UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setForeground(Color.BLUE);
		setProgressBar(results.getProgress(), results.getElapsedTime());		
		bottomPanel.add(progressBar, BorderLayout.CENTER);
		// Adds bottom panel
		this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

		// Adds listener for progressBar
		results.setProgressListener(new MeasureDefinition.ProgressListener() {
			public void progressChanged(double progress, long elapsedTime) {
				setProgressBar(progress, elapsedTime);
			}
		});
	}

	/**
	 * Sets progress bar to specified value
	 * @param progress progress of simulation
	 * @param elapsedTime elapsed time of simulation
	 */
	private void setProgressBar(double progress, long elapsedTime) {
		int percent = (int) Math.round(progress * 100);
		progressBar.setValue(percent);
		if (percent < 100) {
			progressBar.setString(percent + "% of simulation performed...");
		} else {
			progressBar.setString("Simulation Complete (Time Elapsed: " + (double) (elapsedTime/100)/10 + "s)");
		}
	}

	/**
	 * Sets action for toolbar buttons and displays them
	 * @param startAction action associated with start simulation button
	 * @param pauseAction action associated with pause simulation button
	 * @param stopAction action associated with stop simulation button
	 */
	public void addButtonActions(AbstractAction startAction, AbstractAction pauseAction, AbstractAction stopAction) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension iconSize = null;
		if (screenSize.width <= CommonConstants.MAX_SMALL_ICON_SCREEN_WIDTH
				&& screenSize.height <= CommonConstants.MAX_SMALL_ICON_SCREEN_HEIGHT) {
			iconSize = CommonConstants.SMALL_ICON_SIZE_TOOL_BAR;
		} else {
			iconSize = CommonConstants.LARGE_ICON_SIZE_TOOL_BAR;
		}

		start.setAction(startAction);
		start.setText("");
		start.setIcon(JMTImageLoader.loadImage("Sim", iconSize));
		start.setFocusPainted(false);
		start.setContentAreaFilled(false);
		start.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		start.setRolloverIcon(JMTImageLoader.loadImage("Sim", ImageLoader.MODIFIER_ROLLOVER, iconSize));
		start.setPressedIcon(JMTImageLoader.loadImage("Sim", ImageLoader.MODIFIER_PRESSED, iconSize));
		start.setVisible(true);

		pause.setAction(pauseAction);
		pause.setText("");
		pause.setIcon(JMTImageLoader.loadImage("Pause", iconSize));
		pause.setFocusPainted(false);
		pause.setContentAreaFilled(false);
		pause.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		pause.setRolloverIcon(JMTImageLoader.loadImage("Pause", ImageLoader.MODIFIER_ROLLOVER, iconSize));
		pause.setPressedIcon(JMTImageLoader.loadImage("Pause", ImageLoader.MODIFIER_PRESSED, iconSize));
		pause.setVisible(true);

		stop.setAction(stopAction);
		stop.setText("");
		stop.setIcon(JMTImageLoader.loadImage("Stop", iconSize));
		stop.setFocusPainted(false);
		stop.setContentAreaFilled(false);
		stop.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		stop.setRolloverIcon(JMTImageLoader.loadImage("Stop", ImageLoader.MODIFIER_ROLLOVER, iconSize));
		stop.setPressedIcon(JMTImageLoader.loadImage("Stop", ImageLoader.MODIFIER_PRESSED, iconSize));
		stop.setVisible(true);
	}

	/**
	 * Creates a new tabbed pane that shows specified measures and adds it to
	 * specified JTabPane. If measures indices vector is null or empty no panel is added.
	 * @param parent panel where newly created tab should be added
	 * @param name name of the panel to be added
	 * @param description description to be shown into the panel
	 * @param indices array with all measures indices to be shown in this panel
	 */
	private void addTabPane(JTabbedPane parent, String name, String description, int[] indices) {
		// If no measure are present, do not add corresponding tab
		if (indices != null && indices.length > 0) {
			JPanel tabPanel = new JPanel(new BorderLayout());
			// Adds margins
			tabPanel.add(Box.createVerticalStrut(BORDERSIZE), BorderLayout.NORTH);
			tabPanel.add(Box.createVerticalStrut(BORDERSIZE), BorderLayout.SOUTH);
			tabPanel.add(Box.createHorizontalStrut(BORDERSIZE), BorderLayout.WEST);
			tabPanel.add(Box.createHorizontalStrut(BORDERSIZE), BorderLayout.EAST);
			JPanel mainpanel = new JPanel(new BorderLayout());
			tabPanel.add(mainpanel, BorderLayout.CENTER);
			// Adds tab description
			JPanel upperPanel = new JPanel(new BorderLayout());
			JLabel descrLabel = new JLabel(description);
			upperPanel.add(descrLabel, BorderLayout.NORTH);
			upperPanel.add(Box.createVerticalStrut(BORDERSIZE / 2), BorderLayout.SOUTH);
			mainpanel.add(upperPanel, BorderLayout.NORTH);
			// Adds panel with measures
			JPanel scroll = new JPanel(new GridLayout(indices.length, 1, 1, 1));
			// Adds all measures to this panel
			for (int index : indices) {
				scroll.add(new MeasurePanel(results, index));
			}
			mainpanel.add(new JScrollPane(scroll), BorderLayout.CENTER);

			// Adds tab to parent tabbed pane
			parent.addTab(name, tabPanel);
		}
	}

	/**
	 * Helper method used to convert a double into a String avoiding too many decimals
	 * @param d number to be converted
	 * @return string representation of input number
	 */
	protected static String doubleToString(double d) {
		if (d == 0.0) {
			return "0.0";
		} else if (Math.abs(d) >= 1e-2 && Math.abs(d) < 1e4) {
			return decimalFormatNorm.format(d);
		} else {
			return decimalFormatExp.format(d);
		}
	}

	/**
	 * Inner class to create a panel that holds a specified measure
	 */
	protected class MeasurePanel extends JPanel {

		private static final long serialVersionUID = 1L;

		protected MeasureDefinition md;
		protected int measureIndex;
		protected JLabel icon;
		protected JLabel msgLabel;
		protected Vector<MeasureValue> values;
		protected JTextField samples;
		protected JTextField mean;
		protected JTextField lower;
		protected JTextField upper;
		protected JButton abortButton;
		protected FastGraph graph;
		protected FastGraph popupGraph;
		protected JSplitPane graphPanel;
		protected JFrame popupFrame;
		protected JTextArea textState;
		private JButton hideLastIntervalAvgValueButton;
		private JButton statisticalOutputsButton;
		protected boolean lastIntervalAvgValueFlag;
		private JTextArea textState2;

		public MeasurePanel(MeasureDefinition md, int measureIndex) {
			this.md = md;
			this.measureIndex = measureIndex;
			values = md.getValues(measureIndex);
			createPanel();
			addListeners();
		}

		/**
		 * Used to create the panel holding all measure's data
		 */
		protected void createPanel() {
			this.setLayout(new BorderLayout(7, 7));
			this.setBorder(BorderFactory.createRaisedBevelBorder());
			// Sets correct icon for this measure
			icon = new JLabel();
			icon.setIcon(JMTImageLoader.loadImage(IN_PROGRESS_IMAGE));
			add(icon, BorderLayout.WEST);

			//Adds mainPanel with all informations on this measure
			JLabel label;
			JTextField field;
			JPanel mainPanel = new JPanel(new BorderLayout());
			JPanel dataPanel = new JPanel(new SpringLayout());
			// Station name
			label = new JLabel();
			if (sd.isFCRMeasure(md.getMeasureType(measureIndex))) {
				label.setText("Region Name: ");
			} else {
				label.setText("Station Name: ");
			}
			field = new JTextField();
			field.setEditable(false);
			field.setMaximumSize(new Dimension(field.getMaximumSize().width, field.getMinimumSize().height));
			label.setLabelFor(field);
			// If measure is for a server type, use that for station name
			String stationName = null;
			if (measureIndex < sd.getMeasureKeys().size()) {
				stationName = sd.getServerTypeStationName(sd.getMeasureServerTypeKey(sd.getMeasureKeys().get(measureIndex)));
			}
			if (stationName == null) {
				stationName = md.getStationName(measureIndex);
			}
			// If station name is undefined, disables its fields
			if (stationName != null && !stationName.equals("")) {
				if (sd.isFJMeasure(md.getMeasureType(measureIndex))) {
					field.setText(stationName + " (Fork Join)");
				} else {
					field.setText(stationName);
				}
				field.setToolTipText("Name of the station: " + field.getText());
			} else {
				field.setText(ALL_STATIONS);
				field.setToolTipText("This measure is referred to the entire network");
			}
			dataPanel.add(label);
			dataPanel.add(field);
			// Class name
			label = new JLabel();
			if (md.getMeasureType(measureIndex).equals(SimulationDefinition.MEASURE_FX)) {
				label.setText("Mode Name: ");
			} else {
				label.setText("Class Name: ");
			}
			field = new JTextField();
			field.setEditable(false);
			field.setMaximumSize(new Dimension(field.getMaximumSize().width, field.getMinimumSize().height));
			label.setLabelFor(field);
			// If class name is undefined, shows ALL_CLASSES constant
			String className = md.getClassName(measureIndex);
			if (className != null && !className.equals("")) {
				field.setText(className);
				if (md.getMeasureType(measureIndex).equals(SimulationDefinition.MEASURE_FX)) {
					field.setToolTipText("Name of the mode: " + field.getText());
				} else {
					field.setToolTipText("Name of the class: " + field.getText());
				}
			} else {
				if (md.getMeasureType(measureIndex).equals(SimulationDefinition.MEASURE_FX)) {
					field.setText(ResultsConstants.ALL_MODES);
					field.setToolTipText("This measure is an aggregate of every mode");
				} else {
					field.setText(ResultsConstants.ALL_CLASSES);
					field.setToolTipText("This measure is an aggregate of every class");
				}
			}
			dataPanel.add(label);
			dataPanel.add(field);
			// Alpha/Precision
			label = new JLabel("Conf.Int/Max Rel.Err: ");
			field = new JTextField();
			field.setEditable(false);
			field.setMaximumSize(new Dimension(field.getMaximumSize().width, field.getMinimumSize().height));
			label.setLabelFor(field);
			field.setText(md.getAlpha(measureIndex) + " / " + md.getPrecision(measureIndex)); // AnalyzedSamples
			field.setToolTipText("Confidence Interval and Maximum Relative Error requested for this measure: " + field.getText());
			dataPanel.add(label);
			dataPanel.add(field);
			// Analyzed samples
			label = new JLabel("Analyzed samples: ");
			samples = new JTextField();
			samples.setEditable(false);
			samples.setMaximumSize(new Dimension(samples.getMaximumSize().width, samples.getMinimumSize().height));
			label.setLabelFor(samples);
			samples.setText("" + md.getAnalyzedSamples(measureIndex));
			samples.setToolTipText("Number of samples (observations) currently analyzed: " + samples.getText());
			dataPanel.add(label);
			dataPanel.add(samples);

			MeasureValue lastValue = values.lastElement();

			// Lower Bound
			label = new JLabel("Min: ");
			lower = new JTextField("-");
			lower.setEditable(false);
			lower.setMaximumSize(new Dimension(lower.getMaximumSize().width, lower.getMinimumSize().height));
			label.setLabelFor(lower);
			if (lastValue.getLowerBound() > 0 && !Double.isInfinite(lastValue.getLowerBound())) {
				lower.setText(doubleToString(lastValue.getLowerBound()));
			}
			lower.setToolTipText("Minimum value of current confidence interval: " + lower.getText());
			dataPanel.add(label);
			dataPanel.add(lower);

			// Upper Bound
			label = new JLabel("Max: ");
			upper = new JTextField("-");
			upper.setEditable(false);
			upper.setMaximumSize(new Dimension(upper.getMaximumSize().width, upper.getMinimumSize().height));
			label.setLabelFor(upper);
			if (lastValue.getUpperBound() > 0 && !Double.isInfinite(lastValue.getUpperBound())) {
				upper.setText(doubleToString(lastValue.getUpperBound()));
			}
			upper.setToolTipText("Maximum value of current confidence interval: " + upper.getText());
			dataPanel.add(label);
			dataPanel.add(upper);

			SpringUtilities.makeCompactGrid(dataPanel, 3, 4, //rows, cols
					2, 2, //initX, initY
					2, 2);//xPad, yPad
			mainPanel.add(dataPanel, BorderLayout.CENTER);

			// Temp mean and abort button are in a separate panel
			JPanel bottomPanel = new JPanel(new BorderLayout(7, 7));
			label = new JLabel(TEMP_MEAN);
			mean = new JTextField();
			mean.setEditable(false);
			mean.setToolTipText("Current mean value of this measure: " + mean.getText());
			label.setLabelFor(mean);
			mean.setText(doubleToString((values.lastElement()).getMeanValue()));
			bottomPanel.add(label, BorderLayout.WEST);
			bottomPanel.add(mean, BorderLayout.CENTER);
			bottomPanel.add(new JLabel("Warning"), BorderLayout.SOUTH);
			// AbortButton
			abortButton = new JButton();
			abortButton.setText("Abort Measure");
			bottomPanel.add(abortButton, BorderLayout.EAST);
			msgLabel = new JLabel();
			msgLabel.setForeground(Color.RED);
			bottomPanel.add(msgLabel, BorderLayout.SOUTH);
			bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 6));

			JPanel pivotPanel = new JPanel(new BorderLayout());
			JPanel abortAndHidePanel = new JPanel(new BorderLayout());
			JPanel dummyPanel = new JPanel(new BorderLayout());
			hideLastIntervalAvgValueButton = new JButton("Hide instantaneous values");

			hideLastIntervalAvgValueButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (hideLastIntervalAvgValueButton.getText().equals(
							"Hide instantaneous values")) {
						hideLastIntervalAvgValueButton
						.setText("Show instantaneous values");
					} else {
						hideLastIntervalAvgValueButton
						.setText("Hide instantaneous values");
					}

					lastIntervalAvgValueFlag = !lastIntervalAvgValueFlag;
					graph.setHideLastInterval(lastIntervalAvgValueFlag);
					if (popupGraph != null) {
						popupGraph.setHideLastInterval(lastIntervalAvgValueFlag);
					}
				}
			});

			//Creates button to display Statistical Results Window.
			statisticalOutputsButton = new JButton("Statistics");
			statisticalOutputsButton.setToolTipText("Displays requested distribution");
			statisticalOutputsButton.setEnabled(false);

			if (md.getLogFileName(measureIndex) == null) {
				statisticalOutputsButton.setVisible(false);
			}
			statisticalOutputsButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					File logFile = new File(results.getLogFileName(measureIndex));
					if (!logFile.isFile()) {
						JOptionPane.showMessageDialog(ResultsWindow.this,
								"CSV file not found in path '" + logFile.getAbsolutePath() + "'.",
								"Error", JOptionPane.ERROR_MESSAGE);
					} else {
						StatisticalOutputsWindow statistics = new StatisticalOutputsWindow(results, measureIndex);
						statistics.setVisible(true); 
					}
				}
			});
			dummyPanel.add(new JLabel("   "), BorderLayout.CENTER);
			dummyPanel.add(hideLastIntervalAvgValueButton, BorderLayout.EAST);

			//Adds the Statistical Results Button to the DummyPanel.
			dummyPanel.add(statisticalOutputsButton, BorderLayout.WEST);
			dummyPanel.setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 6));

			abortAndHidePanel.add(bottomPanel, BorderLayout.NORTH);
			abortAndHidePanel.add(dummyPanel, BorderLayout.SOUTH);
			pivotPanel.add(mainPanel, BorderLayout.CENTER);
			pivotPanel.add(abortAndHidePanel, BorderLayout.SOUTH);
			// Pack text area in the north of the panel
			JPanel pivotPanel2 = new JPanel(new BorderLayout());
			pivotPanel2.add(pivotPanel, BorderLayout.NORTH);
			// Adds a textPanel to show state
			textState = new JTextArea();
			textState.setEditable(false);
			textState.setLineWrap(true);
			textState.setWrapStyleWord(true);
			JPanel textStatePanel = new JPanel(new BorderLayout());
			textStatePanel.add(textState, BorderLayout.SOUTH);
			textStatePanel.setBorder(BorderFactory.createEmptyBorder(
					BORDERSIZE / 2, BORDERSIZE / 2, BORDERSIZE / 2,
					BORDERSIZE / 2));
			pivotPanel2.add(textStatePanel, BorderLayout.SOUTH);

			textState2 = new JTextArea();
			textState2.setEditable(false);
			textState2.setLineWrap(true);
			textState2.setWrapStyleWord(true);
			JPanel textStatePanel2 = new JPanel(new BorderLayout());
			textStatePanel2.add(textState2, BorderLayout.SOUTH);
			textStatePanel2.setBorder(BorderFactory.createEmptyBorder(
					BORDERSIZE / 2, BORDERSIZE / 2, BORDERSIZE / 2,
					BORDERSIZE / 2));
			pivotPanel2.add(textStatePanel2);

			// Sets a minimal size for text area panel
			pivotPanel2.setMinimumSize(new Dimension((int)(320 * CommonConstants.widthScaling), (int)(190 * CommonConstants.heightScaling)));
			pivotPanel2.setPreferredSize(new Dimension((int)(360 * CommonConstants.widthScaling), (int)(190 * CommonConstants.heightScaling)));

			// Adds graph
			graph = new FastGraph(values, md.getPollingInterval());
			graphPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					pivotPanel2, graph);
			graphPanel.setOneTouchExpandable(true);
			graphPanel.setDividerLocation(600);
			graphPanel.setMinimumSize(new Dimension((int)(320 * CommonConstants.widthScaling), (int)(190 * CommonConstants.heightScaling)));
			graph.setMinimumSize(new Dimension((int)(200 * CommonConstants.widthScaling), (int)(190 * CommonConstants.heightScaling)));
			graph.setPreferredSize(new Dimension((int)(300 * CommonConstants.widthScaling), (int)(190 * CommonConstants.heightScaling)));

			add(graphPanel, BorderLayout.CENTER);
			// Sets icon image and abort button state
			setCorrectState();
			graph.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					graph.mouseClicked(e);
				}
			});
		}

		/**
		 * Adds listeners to this panel, to refresh measures and abort simulation
		 */
		protected void addListeners() {
			if (md.getMeasureState(measureIndex) == MeasureDefinition.MEASURE_IN_PROGRESS) {
				md.setMalformedReplayerFileListener(new MeasureDefinition.MalformedReplayerFileListener() {
					public void detectedError(String msg) {
						if (msgLabel != null) {
							msgLabel.setText(msg);
						}
					}
				});

				// If simulation is not finished, adds a measure listener
				md.addMeasureListener(measureIndex, new MeasureDefinition.MeasureListener() {
					public void measureChanged(List<MeasureValue> measureValues, boolean finished) {
						// Update graphics
						graph.repaint();
						if (popupGraph != null) {
							popupGraph.repaint();
						}

						MeasureValue lastValue = measureValues.get(measureValues.size() - 1);

						// Updates mean, lower, upper and samples
						if (lastValue.getLowerBound() > 0 && !Double.isInfinite(lastValue.getUpperBound())) {
							lower.setText(doubleToString(lastValue.getLowerBound()));
							lower.setToolTipText("Minimum value of current confidence interval: " + lower.getText());
							upper.setText(doubleToString(lastValue.getUpperBound()));
							upper.setToolTipText("Maximum value of current confidence interval: " + upper.getText());
						} else {
							lower.setText("-");
							upper.setText("-");
						}

						mean.setText(doubleToString(lastValue.getMeanValue()));
						mean.setToolTipText("Current mean value of this measure: " + mean.getText());
						samples.setText("" + md.getAnalyzedSamples(measureIndex));
						samples.setToolTipText("Number of samples (observations) currently analyzed: " + samples.getText());
						// If finished is true, state was changed
						if (finished) {
							setCorrectState();
						}
						repaint();
					}
				});

				// Sets AbortButton action, only if abort is available
				if (abort != null) {
					abortButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							abortButton.setEnabled(false);
							abort.abortMeasure(measureIndex);
						}
					});
				}
			}

			// Popups graph if graph window is double clicked
			graph.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						if (popupFrame == null) {
							popupFrame = new JFrame();
							popupGraph = new FastGraph(values, md.getPollingInterval());
							popupFrame.getContentPane().add(popupGraph);
							popupFrame.setTitle(md.getName(measureIndex));
							popupFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
							int width = 640, height = 480;
							Dimension scrDim = Toolkit.getDefaultToolkit().getScreenSize();
							popupFrame.setBounds((scrDim.width - width) / 2, (scrDim.height - height) / 2, width, height);
							popupGraph.addMouseListener(new MouseAdapter() {
								@Override
								public void mouseClicked(MouseEvent e) {
									popupGraph.mouseClicked(e);

								}
							});
						}
						popupFrame.setVisible(true);
					}
				}
			});
		}

		/**
		 * Sets correct state to icon and abort button
		 */
		protected void setCorrectState() {
			switch (md.getMeasureState(measureIndex)) {
			case MeasureDefinition.MEASURE_IN_PROGRESS:
				icon.setIcon(JMTImageLoader.loadImage(IN_PROGRESS_IMAGE));
				icon.setToolTipText(IN_PROGRESS_TEXT);
				textState.setText(IN_PROGRESS_TEXT);
				icon.setToolTipText(FOR_GREEN_GRAPH);
				textState.setText(FOR_GREEN_GRAPH);
				abortButton.setEnabled(true);
				break;
			case MeasureDefinition.MEASURE_SUCCESS:
				icon.setIcon(JMTImageLoader.loadImage(SUCCESS_IMAGE));
				icon.setToolTipText(SUCCESS_TEXT);
				textState.setText(SUCCESS_TEXT);
				icon.setToolTipText(FOR_GREEN_GRAPH);
				textState.setText(FOR_GREEN_GRAPH);
				abortButton.setEnabled(false);
				//Activates the Statistical Results Button.
				statisticalOutputsButton.setEnabled(true);
				break;
			case MeasureDefinition.MEASURE_FAILED:
				icon.setIcon(JMTImageLoader.loadImage(FAILED_IMAGE));
				icon.setToolTipText(FAILED_TEXT);
				textState.setText(FAILED_TEXT);
				icon.setToolTipText(FOR_GREEN_GRAPH);
				textState.setText(FOR_GREEN_GRAPH);
				abortButton.setEnabled(false);
				//Activates the Statistical Results Button.
				statisticalOutputsButton.setEnabled(true);
				break;
			case MeasureDefinition.MEASURE_NO_SAMPLES:
				icon.setIcon(JMTImageLoader.loadImage(NO_SAMPLES_IMAGE));
				icon.setToolTipText(NO_SAMPLES_TEXT);
				textState.setText(NO_SAMPLES_TEXT);
				icon.setToolTipText(FOR_GREEN_GRAPH);
				textState.setText(FOR_GREEN_GRAPH);
				abortButton.setEnabled(false);
				mean.setText("-");
				graph.setVisible(false); // Hides graph if no samples were received
				break;
			}
			if (md.getLogFileName(measureIndex) != null) {
				File file = new File(md.getLogFileName(measureIndex));
				if (file.isFile()) {
					statisticalOutputsButton.setVisible(true);
				} else {
					logger.warn(String.format("Detail CSV file '%s' not found for performance index '%s'. Disabling statistic window.",
							file.getAbsolutePath(), md.getName(measureIndex)));
				}
			}
		}

	}

	/**
	 * Updates this window title adding the file name
	 * @param filename the file name or null to remove it.
	 */
	public void updateTitle(String filename) {
		if (filename != null) {
			setTitle(TITLE + " - " + filename);
		} else {
			setTitle(TITLE);
		}
	}

}
