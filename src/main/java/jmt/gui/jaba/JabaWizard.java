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

package jmt.gui.jaba;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import jmt.common.exception.InputDataException;
import jmt.common.exception.SolverException;
import jmt.framework.gui.components.JMTMenuBar;
import jmt.framework.gui.components.JMTToolBar;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.listeners.AbstractJMTAction;
import jmt.framework.gui.listeners.MenuAction;
import jmt.framework.gui.wizard.Wizard;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.JMTImageLoader;
import jmt.gui.common.panels.AboutDialogFactory;
import jmt.gui.common.panels.WarningWindow;
import jmt.gui.common.xml.ModelLoader;
import jmt.gui.jaba.link.SolverDispatcher;
import jmt.gui.jaba.panels.AllInOnePanel;
import jmt.gui.jaba.panels.ClassesPanel;
import jmt.gui.jaba.panels.ConvexHullPanel;
import jmt.gui.jaba.panels.DescriptionPanel;
import jmt.gui.jaba.panels.PerformanceIndicesPanel;
import jmt.gui.jaba.panels.SectorsGraphicPanel;
import jmt.gui.jaba.panels.SectorsTextualPanel;
import jmt.gui.jaba.panels.ServiceDemandsPanel;
import jmt.gui.jaba.panels.ServiceTimesPanel;
import jmt.gui.jaba.panels.StationsPanel;
import jmt.gui.jaba.panels.VisitsPanel;
import jmt.jmva.gui.panels.ForceUpdatablePanel;
import jmt.manual.ChapterIdentifier;
import jmt.manual.PDFViewer;

/**
 * This is the object you use to define your system structure and parameters
 * 
 * @author alyf (Andrea Conti)
 * @version Date: 11-set-2003 Time: 14.47.11
 * 
 *          Adapted by Andrea Zanzottera Heavily bugfixed by Bertoli Marco
 */
public class JabaWizard extends Wizard {

	private static final long serialVersionUID = 1L;

	private static final boolean DEBUG = false;

	private JabaModel data;
	private JLabel helpLabel;
	private HoverHelp help;

	private ModelLoader modelLoader = new ModelLoader(ModelLoader.JABA, ModelLoader.JABA_SAVE);

	private SolverDispatcher jsolver;

	// keep a reference to these three components to enable switching
	private WizardPanel serviceTimesPanel;

	private WizardPanel serviceDemandsPanel;

	private WizardPanel visitsPanel;

	private WizardPanel performancePanel;
	private ConvexHullPanel hullPanel;

	private AllInOnePanel allInOnePanel;

	public JabaWizard() {
		this(new JabaModel());
	}

	public JabaWizard(JabaModel data) {
		super("JABA");

		System.out.flush();
		setSize(CommonConstants.MAX_GUI_WIDTH_JABA, CommonConstants.MAX_GUI_HEIGHT_JABA);
		this.centerWindow();
		setIconImage(JMTImageLoader.loadImageAwt("JABAIcon"));
		this.data = data;
		data.resetChanged();
		this.setJMenuBar(makeMenubar());
		getContentPane().add(makeToolbar(), BorderLayout.NORTH);
		addPanel(new ClassesPanel(this));
		addPanel(new StationsPanel(this));
		serviceTimesPanel = new ServiceTimesPanel(this);
		visitsPanel = new VisitsPanel(this);
		serviceDemandsPanel = new ServiceDemandsPanel(this);
		addPanel(serviceDemandsPanel);
		addPanel(new DescriptionPanel(this));

		// NEW Andrea Zanzottera 25/11/2005
// Panel with graphical results
		SectorsGraphicPanel drawPanel = new SectorsGraphicPanel();
		drawPanel.setData(this.data);
		addPanel(drawPanel);
// Panel with results in numbers
		// END Andrea Zanzottera 25/11/2005

		// New Carlo Gimondi
		hullPanel = new ConvexHullPanel(this);
		hullPanel.setData(this.data);
		addPanel(hullPanel);
		// End New Carlo Gimondi

		// NEW Sebastiano Spicuglia 2010/12/04
		performancePanel = new PerformanceIndicesPanel(this);
		performancePanel.repaint();
		addPanel(performancePanel);
		// END Sebastiano Spicuglia 2010/12/04

		// NEW Sebastiano Spicuglia 2011/18/05
		allInOnePanel = new AllInOnePanel(this);
		allInOnePanel.repaint();
		addPanel(allInOnePanel);
		// END Sebastiano Spicuglia 2011/18/05

		addPanel(new SectorsTextualPanel(this));
		this.setVisible(true);
	}

	private AbstractJMTAction FILE_SAVE = new AbstractJMTAction("Save as...") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Save Model");
			setIcon("Save", JMTImageLoader.getImageLoader());
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_S, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		}

		public void actionPerformed(ActionEvent e) {
			save();
		}

	};

	private AbstractJMTAction FILE_OPEN = new AbstractJMTAction("Open...") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Open Saved Model");
			setIcon("Open", JMTImageLoader.getImageLoader());
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_O, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		}

		public void actionPerformed(ActionEvent e) {
			open();
		}

	};

	private AbstractJMTAction FILE_NEW = new AbstractJMTAction("New...") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Create New Model");
			setIcon("New", JMTImageLoader.getImageLoader());
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_N, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
		}

		public void actionPerformed(ActionEvent e) {
			newModel();
		}

	};

	private AbstractJMTAction FILE_EXIT = new AbstractJMTAction("Exit") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Exits Application");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));
		}

		public void actionPerformed(ActionEvent e) {
			close();
		}

	};

	private AbstractJMTAction ACTION_RANDOMIZE_MODEL = new AbstractJMTAction(
			"Randomize") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION,
					"Random generation of service demands in the interval [0, 100]");
			setIcon("Dice", JMTImageLoader.getImageLoader());
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_R, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
		}

		public void actionPerformed(ActionEvent e) {
			randomizeModel();
		}

	};


	private AbstractJMTAction HELP = new AbstractJMTAction("JABA Help") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Show JABA help");
			setIcon("Help", JMTImageLoader.getImageLoader());
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_H, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_H));
		}

		public void actionPerformed(ActionEvent e) {
			Runnable r = new Runnable() {
				public void run() {
					try {
						new PDFViewer("JABA Manual", ChapterIdentifier.JABA);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			EventQueue.invokeLater(r);
		}

	};

	private AbstractJMTAction ABOUT = new AbstractJMTAction("About JABA") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "About JABA");
		}

		public void actionPerformed(ActionEvent e) {
			showAbout();
		}

	};

	private AbstractJMTAction ACTION_SOLVE = new AbstractJMTAction("Solve") {

		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Solve model");
			setIcon("Sim", JMTImageLoader.getImageLoader());

			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_L, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_L));

		}

		public void actionPerformed(ActionEvent e) {
			if (checkFinish()) {
				finish();
			}
		}

	};

	/**
	 * @return the toolbar for the jaba wizard. Shamelessly uses icon from the
	 *         main jmt frame
	 */
	protected JMTToolBar makeToolbar() {
		JMTToolBar tb = new JMTToolBar(JMTImageLoader.getImageLoader());
		tb.setFloatable(false);

		// null values add a gap between toolbar icons
		AbstractJMTAction[] actions = { FILE_NEW, FILE_OPEN, FILE_SAVE, null,
				ACTION_SOLVE, ACTION_RANDOMIZE_MODEL
				, null, HELP };
		String[] htext = {
				"Creates a new model",
				"Opens a saved model",
				"Saves the current model",
				"Solves the current model",
				"Import current model to JSIMwiz to solve it with the simulator",
				"Randomize model data", "Show help" };
		ArrayList<AbstractButton> buttons = tb.populateToolbar(actions);
		// Adds help
		for (int i = 0; i < buttons.size(); i++) {
			AbstractButton button = (AbstractButton) buttons.get(i);
			help.addHelp(button, htext[i]);
		}
		return tb;
	}

	private JMTMenuBar makeMenubar() {
		JMTMenuBar jmb = new JMTMenuBar(JMTImageLoader.getImageLoader());
		AbstractJMTAction[] menuItems = new AbstractJMTAction[] {
				new MenuAction("File", new AbstractJMTAction[] { FILE_NEW,
						FILE_OPEN, FILE_SAVE, null, FILE_EXIT }),
				new MenuAction("Action", new AbstractJMTAction[] {
						ACTION_SOLVE, ACTION_RANDOMIZE_MODEL, null,
						ACTION_NEXT, ACTION_PREV }),
				new MenuAction("Help", new AbstractJMTAction[] { HELP, null,
						ABOUT }), };
		jmb.populateMenu(menuItems);
		return jmb;
	}

	/**
	 * @return the button panel
	 */
	@Override
	protected JComponent makeButtons() {
		help = new HoverHelp();
		helpLabel = help.getHelpLabel();

		helpLabel.setBorder(BorderFactory.createEtchedBorder());
		// helpLabel.setHorizontalAlignment(SwingConstants.CENTER);

		ACTION_FINISH.putValue(Action.NAME, "Solve");
		ACTION_CANCEL.putValue(Action.NAME, "Exit");

		JPanel buttons = new JPanel();

		JButton button_finish = new JButton(ACTION_FINISH);
		help.addHelp(button_finish,
				"Validates the system and starts the solver");
		JButton button_cancel = new JButton(ACTION_CANCEL);
		help.addHelp(button_cancel, "Exits the wizard discarding all changes");
		JButton button_next = new JButton(ACTION_NEXT);
		help.addHelp(button_next, "Moves on to the next step");
		JButton button_previous = new JButton(ACTION_PREV);
		help.addHelp(button_previous, "Goes back to the previous step");
		buttons.add(button_previous);
		buttons.add(button_next);
		buttons.add(button_finish);
		buttons.add(button_cancel);

		JPanel labelbox = new JPanel();
		labelbox.setLayout(new BorderLayout());
		labelbox.add(Box.createVerticalStrut(30), BorderLayout.WEST);
		labelbox.add(helpLabel, BorderLayout.CENTER);

		Box buttonBox = Box.createVerticalBox();
		buttonBox.add(buttons);
		buttonBox.add(labelbox);
		return buttonBox;
	}

	// BEGIN Federico Dall'Orso 8/3/2005
	// NEW
	private void newModel() {
		currentPanel.lostFocus();
		if (checkForSave("<html>Save changes before creating a new model?</html>")) {
			return;
		}
		Rectangle bounds = this.getBounds();
		JabaWizard ew = new JabaWizard();
		ew.setBounds(bounds);
		ew.setVisible(true);
		this.setVisible(false);
		this.dispose();
	}

	// END Federico Dall'Orso 8/3/2005
	/**
	 * Shows a confirmation dialog to save before new model or exit operations
	 * 
	 * @param msg
	 *            The message to display.
	 * @return <code>true</code> - if the user select cancel button.
	 */
	public boolean checkForSave(String msg) {
		// Checks if there's an old graph to save
		if (data != null && data.isChanged()) {
			int resultValue = JOptionPane.showConfirmDialog(this, msg,
					"JABA - Warning", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (resultValue == JOptionPane.YES_OPTION) {
				save();
				return true;
			}
			if (resultValue == JOptionPane.CANCEL_OPTION) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Saves current model <br>
	 * Author: Bertoli Marco
	 */
	private void save() {
		currentPanel.lostFocus();
		if (!checkFinish()) {
			return; // panels with problems are expected to notify the user by
					// themselves
		}
		int retval = modelLoader.saveModel(data, this, null);
		switch (retval) {
		case ModelLoader.SUCCESS:
			data.resetChanged();
			this.setTitle("JABA - "
					+ modelLoader.getSelectedFile().getAbsolutePath());
			break;
		case ModelLoader.FAILURE:
			JOptionPane.showMessageDialog(this,
					modelLoader.getFailureMotivation(), "Error",
					JOptionPane.ERROR_MESSAGE);
			break;
		}
	}

	/**
	 * Opens a new model <br>
	 * Author: Bertoli Marco
	 */
	private void open() {
		currentPanel.lostFocus();
		if (checkForSave("<html>Save changes before opening a saved model?</html>")) {
			return;
		}
		JabaModel newdata = new JabaModel();
		int retval = modelLoader.loadModel(newdata, this, null);
		switch (retval) {
		case ModelLoader.SUCCESS:
		case ModelLoader.WARNING:
			data = newdata;
			currentPanel.gotFocus();
			// Shows right panels
			if (data.areVisitsSet()) {
				removePanel(serviceDemandsPanel);
				((ForceUpdatablePanel) serviceTimesPanel).retrieveData();
				((ForceUpdatablePanel) visitsPanel).retrieveData();
				addPanel(visitsPanel, 2);
				addPanel(serviceTimesPanel, 2);
			} else {
				removePanel(visitsPanel);
				removePanel(serviceTimesPanel);
				((ForceUpdatablePanel) serviceDemandsPanel).retrieveData();
				addPanel(serviceDemandsPanel, 2);
			}
			tabbedPane.setSelectedIndex(0);
			this.setTitle("JABA - " + modelLoader.getSelectedFile().getName());
			break;
		case ModelLoader.FAILURE:
			JOptionPane.showMessageDialog(this,
					modelLoader.getFailureMotivation(), "Error",
					JOptionPane.ERROR_MESSAGE);
			break;
		}
		updatePanels();

		// Shows warnings if any
		if (retval == ModelLoader.WARNING) {
			new WarningWindow(modelLoader.getLastWarnings(), this,
					modelLoader.getInputFileFormat(), CommonConstants.JABA)
			.show();
		}
		updatePanels();
	}

	public JabaModel getData() {
		return data;
	}

	@Override
	protected void finish() {
		// OLD
		// do not call this method!!! it is already called inside checkFinish()
		// method.
		// currentPanel.lostFocus();
		solve();
	}

	@Override
	protected boolean cancel() {
		if (currentPanel != null) {
			currentPanel.lostFocus();
		}
		return !checkForSave("<html>Save changes before closing?</html>");
	}

	protected void switchToSimulator() {
		cancel();
		new jmt.gui.jsimgraph.mainGui.JSIMGraphMain();
		dispose();
	}

	public HoverHelp getHelp() {
		return help;
	}

	/**
	 * switches service times and visits panels to service demands panel in
	 * order to change data representation.
	 */
	public void switchFromSTVtoSD() {
		((ForceUpdatablePanel) serviceTimesPanel).commitData();
		((ForceUpdatablePanel) visitsPanel).retrieveData();
		((ForceUpdatablePanel) visitsPanel).commitData();
		removePanel(serviceTimesPanel);
		removePanel(visitsPanel);
		((ForceUpdatablePanel) serviceDemandsPanel).retrieveData();
		addPanel(serviceDemandsPanel, 2);
		tabbedPane.setSelectedIndex(2);
	}

	/**
	 * switches service times and visits panels to service demands panel in
	 * order to change data representation.
	 */
	public void switchFromSDtoSTV() {
		((ForceUpdatablePanel) serviceDemandsPanel).commitData();
		removePanel(serviceDemandsPanel);
		((ForceUpdatablePanel) serviceTimesPanel).retrieveData();
		((ForceUpdatablePanel) visitsPanel).retrieveData();
		addPanel(visitsPanel, 2);
		addPanel(serviceTimesPanel, 2);
		tabbedPane.setSelectedIndex(2);
	}

	public void solve() {
		// New Carlo Gimondi
		int selectTab = 4;
		if (tabbedPane.getSelectedIndex() > 4) {
			selectTab = tabbedPane.getSelectedIndex();
		}
		// End New Carlo Gimondi

		// NEW Zanzottera
		if (jsolver == null) {
			jsolver = new SolverDispatcher();
		}
		JabaModel newdata = new JabaModel(data); // Yields the mean performance
												 // indices

		try {
// check that there are 2 or 3 classes
			// NEW Zanzottera
			newdata.setResults(jsolver.solve(newdata));
			// end NEW

			// @author Stefano Omini
		} catch (InputDataException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
					"Input data error", JOptionPane.ERROR_MESSAGE);
			return;
			// end NEW
		} catch (SolverException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Solver error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		this.data = newdata;

// Zanzottera: the results are displayed inside the
// main screen
		// Dall'Orso createSolutionWindow(newdata.getResults()); //END

		updatePanels();
		currentPanel.gotFocus();
		// Select graphical results panel (before select textual one to avoid a
		// graphical glitch)

		// Modify Carlo Gimondi
		// Remove when 3 class Utilization Panel will create
		if (data.getClasses() == 3) {
			tabbedPane.setEnabledAt(tabbedPane.getComponentCount() - 2, false);
			tabbedPane.setEnabledAt(tabbedPane.getComponentCount() - 3, false);
		} else {
			tabbedPane.setEnabledAt(tabbedPane.getComponentCount() - 2, true);
			tabbedPane.setEnabledAt(tabbedPane.getComponentCount() - 3, true);
		}

		tabbedPane.setSelectedIndex(selectTab);
		repaint();
		// End Modify Carlo Gimondi
	}

	// NEW
	// @author Bertoli Marco
	private void showAbout() {
		AboutDialogFactory.showJABA(this);
	}
	// end NEW

	// randomizes model data
	private void randomizeModel() {
		if (DEBUG) {
			System.out.println("Classes: " + data.getClasses() + "; Stations: "
					+ data.getStations());
		}
		// first get infos about classes and station
		for (int i = 0; i < panels.size() && i < 2; i++) {
			Object o = panels.get(i);
			if (o instanceof ForceUpdatablePanel) {
				((ForceUpdatablePanel) o).commitData();
			}
		}
		// then randomize data
		data.randomizeModelData();
		// and then update all those data into panels
		ForceUpdatablePanel[] fuPanes = {
				(ForceUpdatablePanel) serviceDemandsPanel,
				(ForceUpdatablePanel) serviceTimesPanel,
				(ForceUpdatablePanel) serviceDemandsPanel };
		for (ForceUpdatablePanel fuPane : fuPanes) {
			fuPane.retrieveData();
		}
		repaint();
	}

	private void updatePanels() {
		if (data == null) {
			return;
		}

		for (int i = 0; i < panelCount; i++) {
			panels.get(i).setData(data);
			panels.get(i).redraw();
		}
	}

	@Override
	protected void updateActions() {
		super.updateActions();
		if (currentIndex < (panelCount - 1)) {
			if (!tabbedPane.isEnabledAt(currentIndex + 1)) {
				ACTION_NEXT.setEnabled(false);
			}
		}
		if (currentIndex > 0) {
			if (!tabbedPane.isEnabledAt(currentIndex - 1)) {
				ACTION_PREV.setEnabled(false);
			}
		}
		updatePanels();
	}

	// JABA MAIN
	public static void main(String[] args) {
		new JabaWizard(new JabaModel());
	}

}
