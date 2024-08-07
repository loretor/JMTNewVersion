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

package jmt.gui.jwat.trafficAnalysis.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import jmt.engine.jwat.trafficAnalysis.ModelTrafficAnalysis;
import jmt.engine.jwat.trafficAnalysis.OnResetModel;
import jmt.engine.jwat.trafficAnalysis.TrafficAnalysisSession;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.jwat.JWATConstants;
import jmt.gui.jwat.JWatWizard;
import jmt.gui.jwat.MainJwatWizard;

public class EpochPanel extends WizardPanel implements JWATConstants {

	private static final long serialVersionUID = 1L;

	private MainJwatWizard burstwizard = null;
	private JSpinner epochs = null;
	private ModelTrafficAnalysis model = null;
	private TrafficAnalysisSession session = null;
	private boolean canGoOn = false;

	public EpochPanel(MainJwatWizard burstwizard) {
		super();
		this.burstwizard = burstwizard;
		model = (ModelTrafficAnalysis) burstwizard.getModel();
		session = (TrafficAnalysisSession) burstwizard.getSession();

		initComponents();
		((ModelTrafficAnalysis) this.burstwizard.getModel()).addResetModelListener(new OnResetModel() {

			public void modelReset() {
				EpochPanel.this.removeAll();
				initComponents();
				canGoOn = false;
			}

		});
	}

	private void initComponents() {
		this.setLayout(new BorderLayout());
		epochs = new JSpinner(new SpinnerNumberModel(10, 10, 50, 1));
		JPanel epochOption = new JPanel(new BorderLayout());
		JPanel flowTemp = new JPanel(new FlowLayout(FlowLayout.LEFT));
		epochs.setPreferredSize(new Dimension((int)(70 * CommonConstants.widthScaling), (int)(40 * CommonConstants.heightScaling)));
		epochs.setFont(new Font(epochs.getFont().getName(), epochs.getFont().getStyle(), epochs.getFont().getSize() + 4));
		flowTemp.add(new JLabel("<html><body><h3>Select the maximum number of epochs: </h3></body></html> "));
		flowTemp.add(epochs);
		JButton setEpoch = new JButton(this.setEpoch);
		setEpoch.setPreferredSize(new Dimension((int)(85 * CommonConstants.widthScaling), (int)(35 * CommonConstants.heightScaling)));
		flowTemp.add(setEpoch);
		epochOption.add(flowTemp, BorderLayout.CENTER);
		this.add(epochOption, BorderLayout.NORTH);
	}

	@Override
	public String getName() {
		return "Epoch";
	}

	private Action setEpoch = new AbstractAction("Set epoch") {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			session.setParameters(((Integer) epochs.getValue()).intValue());
			((JWatWizard) getParentWizard()).setEnableButton("Next >", true);
			((JWatWizard) getParentWizard()).setEnableButton("Solve", false);
			canGoOn = true;
			((JWatWizard) getParentWizard()).showNextPanel();
		}

	};

// TODO check the validity of the data provided in the panel and create and pass information to the model for the next panel
// Called before moving to the next panel	@Override
	public boolean canGoForward() {
		return canGoOn;
	}

// TODO check with Fuma what to do
// Called when navigating back from the panel
	@Override
	public boolean canGoBack() {
		return true;
	}

	@Override
	public void gotFocus() {
		burstwizard.setCurrentPanel(TRAFFIC_EPOCH_PANEL);
	}

	@Override
	public void lostFocus() {
		burstwizard.setLastPanel(TRAFFIC_EPOCH_PANEL);
	}

}
