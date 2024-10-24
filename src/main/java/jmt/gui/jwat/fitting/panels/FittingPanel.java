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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import jmt.engine.jwat.fitting.ExponentialFitting;
import jmt.engine.jwat.fitting.FittingAlgorithm;
import jmt.engine.jwat.fitting.FittingSession;
import jmt.engine.jwat.fitting.ParetoFitting;
import jmt.engine.jwat.fitting.utils.ModelFitting;
import jmt.engine.jwat.workloadAnalysis.utils.SetMatrixListener;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.jwat.JWATConstants;
import jmt.gui.jwat.MainJwatWizard;
import jmt.gui.jwat.fitting.charts.SmallCDF;
import jmt.gui.jwat.fitting.charts.SmallPDF;
import jmt.gui.jwat.fitting.charts.SmallQQPlot;

public class FittingPanel extends WizardPanel implements CommonConstants, JWATConstants {

	private static final long serialVersionUID = 1L;

	private MainJwatWizard parentwizard;
	private ModelFitting model = null;
	private FittingSession session = null;
	private FittingAlgorithm engfitting;
	private boolean canGoOn = false;
	private double[] obslist;

	// Variables declaration - do not modify
	private JScrollPane resultfittingScrollPane;
	private JTextArea resultfittingTextArea;
	// Variables declaration - do not modify
	private JPanel panelqqplot;
	private JPanel panelcdf;
	private JPanel panelpdf;
	private JPanel graphicspanel;
	private SmallQQPlot qqplot;
	private SmallCDF cdfplot;
	private SmallPDF pdfplot;
	// End of variables declaration

	public static int PARETO = 0;
	public static int EXPO = 1;
	private int distribution;

	//structures for the approximation of double
	private String pattern = "#.######";
	private DecimalFormat myFormatter;

	public FittingPanel(MainJwatWizard parentwizard, int distr) {
		super();
		this.parentwizard = parentwizard;
		model = (ModelFitting) parentwizard.getModel();
		session = (FittingSession) parentwizard.getSession();
		this.distribution = distr;
		myFormatter = new DecimalFormat(pattern);

		model.addOnSetMatrixObservationListener(new SetMatrixListener() {

			public void onSetMatrixObservation() {
				obslist = model.getListObservations();
				if (distribution == EXPO) {
					engfitting = new ExponentialFitting(obslist, 0.05d);
					initComponents();
					if (engfitting.isFitted()) {
						if (obslist.length > 20) {
							resultfittingTextArea.setText("The analyzed data fits the exponential distribution with significativity 0.05" + "\n" +
									"(the significativity is the probability of rejecting the Exponential hypothesis when it is true)" + "\n" + 
									"Lambda: " + myFormatter.format(engfitting.getEstimatedParameters()[0]) + "\n\n" +
									"The following statistical indices are obtained from the analyzed data" + "\n" +
									"Mean: " + myFormatter.format(model.getMean()) + "\n" +
									"Variance: " + myFormatter.format(model.getVariance()) + "\n" +
									"Coefficient of variation: " + myFormatter.format(model.getCoeffVariation()));
						} else {
							resultfittingTextArea.setText("The analyzed data fits the exponential distribution with significativity 0.05" + "\n" +
									"(the significativity is the probability of rejecting the Exponential hypothesis when it is true)" + "\n" +   
									"Lambda: " + myFormatter.format(engfitting.getEstimatedParameters()[0]) + "\n\n" +
									"The following statistical indices are obtained from the analyzed data" + "\n" +
									"Mean: " + myFormatter.format(model.getMean()) + "\n" +
									"Variance: " + myFormatter.format(model.getVariance()) + "\n" +
									"Coefficient of variation: " + myFormatter.format(model.getCoeffVariation()) + "\n\n"+
									"Warning! Since the number of observations is too small the test may not be reliable!");
						}
					} else {
						resultfittingTextArea.setText("According to the test your data do not follow the exponential distribution" + "\n\n" +
								"The following statistical indices are obtained from the analyzed data" + "\n" +
								"Mean: " + myFormatter.format(model.getMean()) + "\n" +
								"Variance: " + myFormatter.format(model.getVariance()) + "\n" +
								"Coefficient of variation: " + myFormatter.format(model.getCoeffVariation()));
					}
				} else {
					engfitting = new ParetoFitting(obslist, 0.05d);
					initComponents();
					if (engfitting.isFitted()) {
						if (obslist.length > 20) {
							resultfittingTextArea.setText("The analyzed data fits the Pareto distribution with significativity 0.05" + "\n" +
									"(the significativity is the probability of rejecting the Pareto hypothesis when it is true)" + "\n" +   
									"Alpha (Shape parameter): " + myFormatter.format(engfitting.getEstimatedParameters()[1]) + "\n" +
									"k (Scale parameter): " + myFormatter.format(engfitting.getEstimatedParameters()[0]) + "\n\n" +
									"The following statistical indices are obtained from the analyzed data" + "\n" +
									"Mean: " + myFormatter.format(model.getMean()) + "\n" +
									"Variance: " + myFormatter.format(model.getVariance()) + "\n" +
									"Coefficient of variation: " + myFormatter.format(model.getCoeffVariation()));
						} else {
							resultfittingTextArea.setText("The analyzed data fits the Pareto distribution with significativity 0.05" + "\n" +
									"(the significativity is the probability of rejecting the Pareto hypothesis when it is true)" + "\n" + 
									"Alpha (Shape parameter): " + myFormatter.format(engfitting.getEstimatedParameters()[1]) + "\n" +
									"k (Scale parameter): " + myFormatter.format(engfitting.getEstimatedParameters()[0]) + "\n" +
									"Mean: " + myFormatter.format(model.getMean()) + "\n\n" +
									"The following statistical indices are obtained from the analyzed data" + "\n" +
									"Variance: " + myFormatter.format(model.getVariance()) + "\n" +
									"Coefficient of variation: " + myFormatter.format(model.getCoeffVariation()) + "\n\n"+
									"Warning! Since the number of observations is too small the test may not be reliable!");
						}
					} else {
						resultfittingTextArea.setText("According to the test your data do not follow the Pareto distribution" + "\n\n" +
								"The following statistical indices are obtained from the analyzed data" + "\n" +
								"Mean: " + myFormatter.format(model.getMean()) + "\n" +
								"Variance: " + myFormatter.format(model.getVariance()) + "\n" +
								"Coefficient of variation: " + myFormatter.format(model.getCoeffVariation()));
					}
				}
			}

			public void onResetMatrixObservation() {
				FittingPanel.this.removeAll();
			}

		});
	}

	private void initComponents() {
		Box mainBox = Box.createVerticalBox();
		Box centralBox = Box.createHorizontalBox();
		// Pannello dei componenti univariate statistics panel
		JPanel componentsPanel = new JPanel(new BorderLayout(0, 5));

		setLayout(new GridLayout(1, 1));

		mainBox.add(Box.createVerticalStrut(10));
		mainBox.add(centralBox);
		mainBox.add(Box.createVerticalStrut(10));
		add(mainBox);

		// components univariate statistics panel
		if (distribution == EXPO) {
			componentsPanel.add(new JLabel("Results of fitting of exponential distribution"), BorderLayout.NORTH);
		} else {
			componentsPanel.add(new JLabel("Results of fitting of Pareto distribution"), BorderLayout.NORTH);
		}

		resultfittingScrollPane = new JScrollPane();
		resultfittingTextArea = new JTextArea();
		resultfittingTextArea.setColumns(20);
		resultfittingTextArea.setRows(5);
		resultfittingScrollPane.setViewportView(resultfittingTextArea);

		componentsPanel.add(resultfittingScrollPane, BorderLayout.CENTER);

		graphicspanel = new JPanel(new GridLayout(1,3));
		graphicspanel.setSize(new Dimension((int)(200 * CommonConstants.widthScaling), (int)(100 * CommonConstants.heightScaling)));

		panelqqplot = new JPanel(new BorderLayout());
		panelqqplot.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "QQ-plot"));

		panelcdf = new JPanel(new BorderLayout());
		panelcdf.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "CDF"));

		panelpdf = new JPanel(new BorderLayout());
		panelpdf.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "pdf"));

		qqplot = new SmallQQPlot(model,engfitting);
		qqplot.setDistribution(distribution); 
		cdfplot = new SmallCDF(model, engfitting);
		cdfplot.setDistribution(distribution);
		pdfplot = new SmallPDF(model, engfitting);
		//just to try
		panelqqplot.add(qqplot);
		panelcdf.add(cdfplot);
		panelpdf.add(pdfplot);

		graphicspanel.add(panelqqplot);
		graphicspanel.add(panelcdf);
		graphicspanel.add(panelpdf);
		graphicspanel.setPreferredSize(new Dimension((int)(300 * CommonConstants.widthScaling), (int)(240 * CommonConstants.heightScaling)));

		componentsPanel.add(graphicspanel, BorderLayout.SOUTH);
		centralBox.add(componentsPanel);
	}// </editor-fold>

	public String getName() {
		if (distribution == EXPO) {
			return "Exponential";
		} else {
			return "Pareto";
		}
	}

	public void gotFocus() {
		if (distribution == EXPO) {
			parentwizard.setCurrentPanel(FITTING_EXPO_PANEL);
		} else {
			parentwizard.setCurrentPanel(FITTING_PARETO_PANEL);
		}
	}

	public void lostFocus() {
		if (distribution == EXPO) {
			parentwizard.setLastPanel(FITTING_EXPO_PANEL);
		} else {
			parentwizard.setLastPanel(FITTING_PARETO_PANEL);
		}
	}

}
