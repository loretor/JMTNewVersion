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

package jmt.gui.jwat.fitting.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jmt.engine.jwat.ProgressStatusListener;
import jmt.engine.jwat.fitting.utils.ModelFitting;
import jmt.engine.jwat.input.EventFinishAbort;
import jmt.engine.jwat.input.EventFinishLoad;
import jmt.engine.jwat.input.EventStatus;
import jmt.engine.jwat.input.Loader;
import jmt.engine.jwat.input.ProgressMonitorShow;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.Defaults;
import jmt.gui.jwat.JWATConstants;
import jmt.gui.jwat.JWatWizard;
import jmt.gui.jwat.MainJwatWizard;

public class LoadDemoFittingPanel extends WizardPanel implements CommonConstants, JWATConstants {

	private static final long serialVersionUID = 1L;

	private String demoDescription = "<HTML>" + "<b>Type of demo:</b> Fitting algorithm <p>"
			+ "<b># observations:</b> This demo has 500 observations <p>"
			+ "<b># variables:</b> Each observation is characterized by one numeric variable<p>"
			+ "After the fitting you will discover that the data is Pareto distributed";
	private String useDescription = "<HTML>"
			+ "<b>STEP 1:</b> Choose demo file from the list then click 'Load demo'.<p><p>"
			+ "<b>STEP 2:</b> Analyze the results of the fitting in the Exponential and Pareto panels.<p><p>"
			+ "</HTML>";

	private static final String TEMP_DEMO_NAME_FITTING = "Demo_FittingPareto";
	private static final String DEMO_FILE = EXAMPLE_PATH + TEMP_DEMO_NAME_FITTING + "Data.jwat";
	private JButton loadDemo;
	private boolean canGoOn;
	private ModelFitting model;
	private JList demos;
	private JLabel demoDesc;
	private JLabel useDesc;
	private MainJwatWizard parent;

	public LoadDemoFittingPanel(MainJwatWizard parent) {
		super();
		model = (ModelFitting) parent.getModel();
		canGoOn = false;
		this.parent = parent;
		initGUI();
	}

	@Override
	public String getName() {
		return "Load Demo";
	}

	private void initGUI() {
		this.setLayout(new BorderLayout());

		JPanel grid = new JPanel(new GridLayout(2, 1, 5, 5));

		JPanel upper = new JPanel(new BorderLayout(10, 10));
		demos = new JList(new String[] { TEMP_DEMO_NAME_FITTING });
		Font f = demos.getFont();
		demos.setFont(new Font(f.getFontName(), f.getStyle(), f.getSize() + 2));
		demos.setFixedCellWidth(150);
		demos.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				if (demos.getSelectedIndex() >= 0) {
					//TODO insert peculiar strings for demo
				} else {
					demos.setSelectedIndex(0);
				}
			}

		});
		demos.setSelectedIndex(0);
		demoDesc = new JLabel(demoDescription);
		upper.add(new JScrollPane(demos), BorderLayout.WEST);
		upper.add(new JScrollPane(demoDesc), BorderLayout.CENTER);
		demoDesc.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Demo description"));

		useDesc = new JLabel(useDescription);
		useDesc.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "How to execute a demo"));

		grid.add(upper);
		grid.add(new JScrollPane(useDesc));

		JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
		south.add(new JLabel("Click this button to load selected demo   --->   "));
		loadDemo = new JButton("Load demo");
		loadDemo.setBackground(Color.RED);
		loadDemo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File demoFile = new File(Defaults.getWorkingPath(), DEMO_FILE);
				if (!demoFile.isFile()) {
					demoFile = new File(".", DEMO_FILE);
				}
				//Calls loader
				try {
					Loader.readData(demoFile.getAbsolutePath(), Loader.loadParameter(demoFile.getParentFile(), TEMP_DEMO_NAME_FITTING),
							new ProgressMonitorShow(LoadDemoFittingPanel.this, "Loading Data...", 1000), new InputStatusListener());
				} catch (FileNotFoundException ee) {
					JOptionPane.showMessageDialog(LoadDemoFittingPanel.this, "Loading aborted. File not found.", "ABORT!!", JOptionPane.WARNING_MESSAGE);
				} catch (IOException ee) {
					JOptionPane.showMessageDialog(LoadDemoFittingPanel.this, "Loading demo failed.", "ABORT!!", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		south.add(loadDemo);
		this.add(grid, BorderLayout.CENTER);
		this.add(south, BorderLayout.SOUTH);
	}

	private class InputStatusListener implements ProgressStatusListener {

		public void statusEvent(EventStatus e) {

			switch (e.getType()) {
			case EventStatus.ABORT_EVENT:
				abortEvent((EventFinishAbort) e);
				break;
			case EventStatus.DONE_EVENT:
				finishedEvent((EventFinishLoad) e);
				break;
			}

		}

		//Abort input file loading
		private void abortEvent(EventFinishAbort e) {
			JOptionPane.showMessageDialog(LoadDemoFittingPanel.this, e.getMessage(), "LOADING ABORTED!!", JOptionPane.WARNING_MESSAGE);
			canGoOn = false;
			((JWatWizard) getParentWizard()).setEnableButton("Next >", false);
			((JWatWizard) getParentWizard()).setEnableButton("Solve", false);
		}

		//		data loaded
		private void finishedEvent(final EventFinishLoad e) {
			model.setMatrix(e.getSession());
			((JWatWizard) getParentWizard()).setEnableButton("Next >", true);
			((JWatWizard) getParentWizard()).setEnableButton("Solve", false);
			canGoOn = true;
			((JWatWizard) getParentWizard()).showNextPanel();
		}
	}

	@Override
	public void lostFocus() {
		parent.setLastPanel(WORKLOAD_INPUT_PANEL);
	}

	/********** WIZARD MANAGEMENT FUNCTIONS **********/
// TODO check the validity of the data provided in the panel and create and pass information to the model for the next panel
// Called before moving to the next panel
	@Override
	public boolean canGoForward() {
		return canGoOn;
	}

	public void setCanGoForward(boolean canGo) {
		canGoOn = canGo;
	}

// TODO check with Fuma what to do
// Called when navigating back from the panel
	@Override
	public boolean canGoBack() {
		if (JOptionPane.showConfirmDialog(this, "Are you sure you want to go back to start screen?", "Back operation", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
			return false;
		}
//Reset the main window
		parent.resetScreen();
		return true;
	}

}
