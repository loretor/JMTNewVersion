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
package jmt.jmch.wizard;


import jmt.gui.common.CommonConstants;
import jmt.gui.common.JMTImageLoader;
import jmt.jmch.simulation.RoutingSimulation;
import jmt.jmch.simulation.Simulation;
import jmt.jmch.wizard.panels.AnimationPanel;
import jmt.jmch.wizard.panels.MainPanel;
import jmt.jmch.wizard.panels.markovPanel.MMQueuesPanel;
import jmt.jmch.wizard.panels.resultsPanel.ResultsPanel;
import jmt.jmch.wizard.panels.resultsPanel.ResultsPanelRouting;
import jmt.jmch.wizard.panels.resultsPanel.ResultsPanelScheduling;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import jmt.framework.gui.wizard.WizardPanel;


/**
 * Main Wizard that contains all the panels for JTeach Models and Markov chains
 *
 * @author Lorenzo Torri
 * Date: 29-mar-2024
 * Time: 15.04
 */
public class MainWizard extends JMCHWizard{

    //general variables for the JMTFrame
    private String IMG_JWATICON = "JMCHIcon";
    private static final String TITLE = "JMCH - Modelling Classroom Helper"; 
	private static final String TITLE_QUEUEING = "Queueing";
	private int lastPanel = 0; //this variable is used to set the focus on the last panel visited when going back to the main panel and answering NO to the "Are you sure..."

    //components of the panel
    private JPanel menus;
    private MainPanel mainPanel;

	//list of panels after the MainPanel
	private List<WizardPanel> panelCollection = new ArrayList<>();
	private AnimationPanel animationPanel;
	private Simulation sim;
	private ResultsPanel resultsPanel = null;

	//results information
	private ResultStructure rs;
    
	public MainWizard() {
        this.setIconImage(JMTImageLoader.loadImage(IMG_JWATICON).getImage());
		this.setTitle(TITLE);
		this.setSize(CommonConstants.MAX_GUI_WIDTH_JWAT, CommonConstants.MAX_GUI_HEIGHT_JWAT);
		this.setResizable(true);

        centerWindow();

		initGUI();
	}

    /**
	 * Initializes JTCH start screen GUI
	 */
	private void initGUI() {        
        menus = new JPanel(new BorderLayout());
        menus.setBackground(Color.BLUE);
		//help = this.getHelp();
		getContentPane().add(menus, BorderLayout.NORTH);
		mainPanel = new MainPanel(this);
		this.addPanel(mainPanel);
		setEnableButton("Solve", false);
	}

    public static void main(String[] args) {
		new MainWizard().setVisible(true);
	}

    
	/**
	 * Method to create a new AnimationPanel for Non Preemptive Scheduling
	 * @param simulation the type of Simulation
	 */
	public void setAnimationPanelEnv(Simulation simulation){
		this.sim = simulation;
		this.setTitle(TITLE + " - "+ TITLE_QUEUEING + ", "+simulation.getType().toString());
		
		
		animationPanel = new AnimationPanel(this, simulation);
		this.addPanel(animationPanel);
		panelCollection.add(animationPanel);


		if(simulation instanceof RoutingSimulation){
			//decide if the results stored in this class should be forwarded to the new resultsPanel
			if(resultsPanel != null && resultsPanel instanceof ResultsPanelRouting && rs != null){ //send old results only if previous simulation was stil a routing one
				resultsPanel = new ResultsPanelRouting(this);
				resultsPanel.setResults(rs.algorithms, rs.arrivalDistibutions, rs.lambdas, rs.serviceDistributions, rs.queuesNumber, rs.responseTimes, rs.thoughputs, rs.nCustomers);
			}
			else{
				resultsPanel = new ResultsPanelRouting(this);
			}			
		}
		else{
			resultsPanel = new ResultsPanelScheduling(this);
		}
		
		this.addPanel(resultsPanel);
		panelCollection.add(resultsPanel);

		this.showNext();
	}

	/**
	 * Method to create a new MMQueuesPanel
	 */
	public void setMMQueuesPanelEnv(String selectedMethod) {
		WizardPanel p = new MMQueuesPanel(this, selectedMethod);
		String title;
		if (selectedMethod == "mm1") {
			title = "Markov Chain M/M/1 Station";
		} else if (selectedMethod == "mm1k") {
			title = "Markov Chain M/M/1/k";
		} else if (selectedMethod == "mmn Finite Capacity Station") {
			title = "Markov Chain M/M/n Station";
		} else{
			title = "Markov Chain M/M/n/k Finite Capacity Station";
		}
		this.setTitle(TITLE + " - "+ title);
		this.addPanel(p);
		panelCollection.add(p);

		this.showNext();
	}


    /**
     * To change dinamically the type of ToolBar.
     * Since different panels have different ToolBars, each panel creates its own toolbar and then it is setted correctly here
     * @param bar new JToolBar
     */
    public void setToolBar(JToolBar bar) {
		if (toolBar != null) {
			menus.remove(toolBar);
		}
		menus.add(bar, BorderLayout.SOUTH);
		toolBar = bar;
	}

    /**
     * To change dinamically the type of MenuBar.
     * Since different panels have different Menus, each panel creates its own menubar and then it is setted correctly here
     * @param bar new JTMMenuBar
     */
	public void setMenuBar(JMenuBar bar) {
		if (menuBar != null) {
			menus.remove(menuBar);
		}
		menus.add(bar, BorderLayout.NORTH);
		menuBar = bar;
	}

	/**
	 * To remove all the panels and to go back to the MainPanel.
	 * It is called by all the panels in the CanGoBack()
	 */
	public void resetScreen() {
		for (int i = 0; i < panelCollection.size(); i++) {
			tabbedPane.remove(panelCollection.get(i));
		}
		panelCollection.clear();

		this.setTitle(TITLE);
		mainPanel.createMenu(); //update the menu and toolbar
		mainPanel.createToolBar();
		setEnableButton("Next >", false);
		setEnableButton("Back", false);
		this.validate();
	}

	public void setSelectedPanel(){
		showPrev();
	}

	public int getNumbersPanel(){
		return panelCollection.size();
	}

	/* Two methods for setting the icon of loading or end result. Called by the Animation Panel*/
	public void setIconLoading(){
		resultsPanel.loadingResult();
	}

	public void setIconFinish(){
		resultsPanel.endResult();
	}

	/** Save the results of the Results Panel, and display them in the next chosen simulation, if the type is the same */
	public void saveResults(){
		if(resultsPanel != null){ 
			rs = new ResultStructure();
			rs.setAll(
				resultsPanel.getAlgorithms(), 
				resultsPanel.getArrivalDistirbutions(), 
				resultsPanel.getLambdas(), 
				resultsPanel.getServiceDistributions(), 
				resultsPanel.getNQueues(), 
				resultsPanel.getServiceTimes(), 
				resultsPanel.getResponseTimes(), 
				resultsPanel.getQueueTimes(), 
				resultsPanel.getThroughputs(), 
				resultsPanel.getNumberOfCustomers());
		}	
	}

	/**
	 * Method called by the AnimationPanel to update the Result Panel
	 * @param algorithm queue algorithm of the animation
	 * @param arrivalDistr arrival time distribution
	 * @param lambda the inter arrival time
	 * @param serviceDistr service time distribution
	 * @param nServers number of servers for each station
	 * @param service service time
	 * @param nQueues number of queues reachable
	 * @param responseTime response time
	 * @param queueTime queue time
	 * @param thoughput thoughput
	 * @param queueNumber queue number
	 */
	public void routeResults(String algorithm, String arrivalDistr, double lambda, String serviceDistr, int nServers, double service, int nQueues, double responseTime, double queueTime, double thoughput, double queueNumber){
		if(sim instanceof RoutingSimulation){
			resultsPanel.addResult(algorithm, arrivalDistr, lambda, serviceDistr, nQueues, responseTime, thoughput, queueNumber);
		}
		else{
			resultsPanel.addResult(algorithm, arrivalDistr, lambda, serviceDistr, nServers, service, responseTime, queueTime, thoughput, queueNumber);
		}
	}

	public void setLastPanel(int value){
		lastPanel = value;
	}

	public void setLastPanel() {
		tabbedPane.setSelectedIndex(lastPanel);
	}
}

class ResultStructure{
	protected String[] algorithms;
    protected String[] arrivalDistibutions;
    protected double[] lambdas;
    protected String[] serviceDistributions;
    protected int[] queuesNumber;
    protected double[] responseTimes;
    protected double[] thoughputs;
    protected double[] nCustomers;

	public ResultStructure(){
		algorithms = new String[0];
		arrivalDistibutions = new String[0];
		lambdas = new double[0];
		serviceDistributions = new String[0];
		queuesNumber = new int[0];
		responseTimes = new double[0];
		thoughputs = new double[0];
		nCustomers = new double[0];
	}

	public void setAll(String[] algo, String[] arrivalD, double[] lamb, String[] serviceD, int[] queueN, double[] S, double[] R, double[] Q, double[] X, double[] N){
		algorithms = Arrays.copyOf(algo, algo.length);
		arrivalDistibutions = Arrays.copyOf(arrivalD, arrivalD.length);
		lambdas = Arrays.copyOf(lamb, lamb.length);
		serviceDistributions = Arrays.copyOf(serviceD, serviceD.length);
		queuesNumber = Arrays.copyOf(queueN, queueN.length);
		responseTimes = Arrays.copyOf(R, R.length);
		thoughputs = Arrays.copyOf(X, X.length);
		nCustomers = Arrays.copyOf(N, N.length);
	}
}
