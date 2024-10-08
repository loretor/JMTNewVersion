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

package jmt.jmch.wizard.panels.markovPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Dictionary;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.panels.AboutDialogFactory;
import jmt.jmarkov.Simulator;
import jmt.jmarkov.Graphics.JobsDrawer;
import jmt.jmarkov.Graphics.LogFile;
import jmt.jmarkov.Graphics.Notifier;
import jmt.jmarkov.Graphics.QueueDrawer;
import jmt.jmarkov.Graphics.StatiDrawer;
import jmt.jmarkov.Graphics.TANotifier;
import jmt.jmarkov.Graphics.constants.DrawBig;
import jmt.jmarkov.Graphics.constants.DrawConstrains;
import jmt.jmarkov.Graphics.constants.DrawNormal;
import jmt.jmarkov.Graphics.constants.DrawSmall;
import jmt.jmarkov.Queues.Arrivals;
import jmt.jmarkov.Queues.JobQueue;
import jmt.jmarkov.Queues.MM1Logic;
import jmt.jmarkov.Queues.MM1dLogic;
import jmt.jmarkov.Queues.MMNLogic;
import jmt.jmarkov.Queues.MMNdLogic;
import jmt.jmarkov.Queues.Processor;
import jmt.jmarkov.Queues.Exceptions.NonErgodicException;
import jmt.jmarkov.utils.Formatter;
import jmt.jmch.wizard.MainWizard;
import jmt.jmarkov.CustomDialog;
import jmt.manual.ChapterIdentifier;
import jmt.manual.PDFViewer;

/**
 * @author Arif Canakoglu
 *
 */
public class MMQueues extends JPanel {
	private MainWizard parent;
	private WizardPanel parentPanel;

	private static final long serialVersionUID = 1L;

	private static final boolean DEBUG = false;

	private boolean nonErgodic;//if the utilization is less than 1
	private double U; // Utilization [%]
	private double Q; // Average customer in station
	private double sMultiplier = 1; //service time slide bar multiplier
	private double lambdaMultiplier = 1; //lambda slide bar multiplier
	private int lambdaMultiplierChange = 0; //for the lambda slide bar
	private int sMultiplierChange = 1; //for the service slide bar

	private int buffer; //number of place for the waiting queue
	private int cpuNum; //number of server in the system
	private boolean paused = false; //if the system is paused 

	private Dimension initSize = new Dimension(CommonConstants.MAX_GUI_WIDTH_JMCH, CommonConstants.MAX_GUI_HEIGHT_JMCH);

	private JPanel sPanel;
	private JPanel lambdaPanel;
	private JSlider sS;
	private JSlider lambdaS;
	private JSlider buffS;

	private JButton playB;
	private JButton stopB;
	private JButton pauseB;

	private QueueDrawer queueDrawer;
	private StatiDrawer statiDrawer;
	private JobsDrawer jobsDrawer;
	private JTabbedPane outputTabP;
	private JScrollPane txtScroll;
	private TANotifier outputTA;
	private LogFile logFile;
	private Notifier[] tan = new Notifier[5];

	private JPanel buttonsP;

	private JPanel resultsP;
	public JPanel mf;
	private JPanel outputP;
	private JPanel parametersP;
	private JPanel simulationP;

	private JPanel buffPanel;
	private JPanel accelerationP;
	private JPanel jobsP;
	private JSlider accelerationS;

	// Label & Label strings
	private JLabel sL;

	private JLabel lambdaL;

	private JLabel mediaJobsL;

	private JLabel utilizationL;

	private JLabel buffL;

	private JLabel thrL;

	private JLabel responseL;

	private JLabel accelerationL;

	private String sStrS = "Avg. Service Time S = ";

	private String sStrE = " s";

	private String lambdaStrS = "Avg. Arrival Rate (lambda) = ";

	private String lambdaStrE = " cust./s";

	private String nStrS = "Avg. Cust. in Station (Queue + Service) N = ";

	private String nStrE = " cust.";

	private String uStrS = "Avg. Utilization (Sum of All Servers) U = ";

	private String uStrE = "";

	private String bufStrS = "Max Station Capacity k = ";

	private String bufStrE = " cust.";

	private String thrStrS = "Avg. Throughput X =";

	private String thrStrE = " cust./s";

	private String respStrS = "Avg. Response Time R = ";

	private String respStrE = " s";

	// Settings
	private Color emptyC = Color.WHITE;

	private Color probC = Color.GREEN;

	private Color queueC = Color.BLUE;

	private Color animC = Color.RED;
	private boolean gradientF = false;
	private DrawConstrains dCst = new DrawNormal();
	private int BUFF_I = 15;

	private int LAMBDA_I = 50;

	private int S_I = 95;

	// menu
	private JMenuBar menuB;

	// help
	private JMenu helpMenu;

	// queue
	private JMenu queueMenu;
	private Action selectQueueRB;
	private JRadioButtonMenuItem gradientItem;
	// settings
	private JMenu settingsMenu;
	// colors
	private JMenu colorsMenu;

	// size
	private JMenu sizeMenu;
	// Queues data:
	MM1Logic ql = new MM1Logic(0.0, 0.0);
	JobQueue jq;

	Arrivals arrival;
	Processor[] processors;

	private Simulator sim = null;
	private boolean lambdaSChange = true;
	private boolean sSChange = true;


	//------ new feature
	private HoverHelp help;
	

	public MMQueues(MainWizard main, WizardPanel panel, String selectedMethod) {
		mf = this;
		parent = main;
		parentPanel = panel;
		help = parent.getHoverHelp();
		initGUI();

		this.setVisible(true);

		selectMethod(selectedMethod);
	}

	protected void initGUI() {
		// for setting the place of the screen
		try {
			// Simulation data panel
			simulationP = new JPanel();
			parametersP = new JPanel();
			lambdaPanel = new JPanel();
			lambdaL = new JLabel();
			lambdaS = new JSlider();
			buffS = new JSlider();
			sPanel = new JPanel();
			sS = new JSlider();
			resultsP = new JPanel();
			mediaJobsL = new JLabel();
			utilizationL = new JLabel();
			mediaJobsL = new JLabel();
			thrL = new JLabel();
			responseL = new JLabel();

			// simulation output panels
			outputP = new JPanel();
			outputTabP = new JTabbedPane();
			txtScroll = new JScrollPane();
			outputTA = new TANotifier();
			logFile = new LogFile();
			// logD = new LogDrawer();
			statiDrawer = new StatiDrawer(ql);
			queueDrawer = new QueueDrawer(ql);
			jobsDrawer = new JobsDrawer();

			buffPanel = new JPanel();
			accelerationP = new JPanel();
			jobsP = new JPanel();
			accelerationS = new JSlider();

			// acceleration
			accelerationP.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 0;
			accelerationP.setBorder(addTitle("Simulation time", dCst.getSmallGUIFont()));
			accelerationL = new JLabel("Time x0.0");
			accelerationL.setFont(dCst.getNormalGUIFont());
			accelerationL.setHorizontalAlignment(SwingConstants.CENTER);
			accelerationP.add(accelerationL, gbc);
			accelerationS.setValue(50);
			accelerationS.setMaximum(100);
			accelerationS.setMinimum(1);
			accelerationS.setMajorTickSpacing(50);
			accelerationS.setMinorTickSpacing(1);
			accelerationS.setSnapToTicks(true);
			accelerationS.setPaintTicks(true);
			accelerationS.setPaintLabels(true);
			Dictionary<Integer, JLabel> ad = accelerationS.getLabelTable();
			ad.keys();
			ad.put(new Integer(1), new JLabel("real time"));
			ad.put(new Integer(51), new JLabel("faster"));
			ad.put(new Integer(100), new JLabel("fastest"));
			accelerationS.setLabelTable(ad);
			gbc.gridy = 1;
			gbc.weightx = 1;
			accelerationP.add(accelerationS, gbc);
			accelerationS.setValue(50);
			accelerationS.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent evt) {
					int value = accelerationS.getValue();
					if (sim != null) {
						sim.setTimeMultiplier(value);
						accelerationL.setText("Time x" + Formatter.formatNumber(sim.getTimeMultiplier(), 2));
					} else {
						accelerationL.setText("Time x" + Formatter.formatNumber(value, 2));
					}

				}

			});
			accelerationL.setText("Time x" + Formatter.formatNumber(accelerationS.getValue(), 2));

			// jobs panel
			jobsP.setBorder(addTitle("Customers", dCst.getSmallGUIFont()));
			jobsP.setLayout(new GridLayout(1, 1));
			jobsP.add(jobsDrawer);

			// buttons
			buttonsP = new JPanel();
			playB = new JButton();
			stopB = new JButton();
			pauseB = new JButton();

			// Adding to main frame
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;

			//this.setSize(new java.awt.Dimension(initSize.width, initSize.height));
			simulationP.setLayout(new GridBagLayout());

			//-----------------------new for the size of SimulationP
			simulationP.setMaximumSize(new Dimension(parent.getWidth()-20,parent.getHeight()-220));
			simulationP.setPreferredSize(new Dimension(parent.getWidth()-20,parent.getHeight()-220));


			this.add(simulationP, BorderLayout.CENTER);
			parametersP.setLayout(new GridBagLayout());
			parametersP.setBorder(addTitle("Simulation Parameters", dCst.getSmallGUIFont()));
			c.weightx = 1;
			c.weighty = 0;
			c.gridx = 0;
			c.gridy = 0;
			simulationP.add(parametersP, c);

			// lambda
			lambdaPanel.setLayout(new GridLayout(2, 1));
			c.weightx = 0.5;

			parametersP.add(lambdaPanel, c);

			c.gridx = 1;
			c.weightx = 0;
			parametersP.add(getSplitter(10, 1), c);
			c.weightx = 0.5;

			lambdaL.setAlignmentX(SwingConstants.CENTER);
			lambdaPanel.add(lambdaL);
			lambdaMultiplier = 0.01;
			lambdaMultiplierChange = 0;
			lambdaS.setMaximum(100);
			lambdaS.setMinimum(0);
			lambdaS.setMajorTickSpacing(25);
			lambdaS.setMinorTickSpacing(1);
			lambdaS.setPaintLabels(true);
			lambdaS.setSnapToTicks(true);
			lambdaPanel.add(lambdaS);
			lambdaL.setFont(dCst.getNormalGUIFont());
			lambdaS.setValue(LAMBDA_I);
			setLambdaSlider();
			lambdaS.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent evt) {
					lambdaSStateChanged(evt);
					if (lambdaSChange) {
						setLambdaMultiplier();
					}

				}
			});
			lambdaS.addMouseListener(new MouseListener() {

				public void mouseClicked(MouseEvent e) {
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

				public void mousePressed(MouseEvent e) {
					lambdaSChange = false;
				}

				public void mouseReleased(MouseEvent e) {
					setLambdaMultiplier();
					lambdaSChange = true;
				}

			});
			lambdaS.repaint();

			// S slider
			sPanel.setLayout(new GridLayout(2, 1));
			c.gridx = 2;
			parametersP.add(sPanel, c);

			c.gridx = 3;
			c.weightx = 0;
			parametersP.add(getSplitter(10, 1), c);
			c.weightx = 0.5;

			sL = new JLabel();
			sL.setAlignmentX(SwingConstants.CENTER);
			sPanel.add(sL);
			sS.setMaximum(100);
			sS.setMinimum(0);
			sS.setMajorTickSpacing(25);
			sS.setMinorTickSpacing(1);
			sS.setPaintLabels(true);
			sL.setFont(dCst.getNormalGUIFont());

			sPanel.add(sS);

			sMultiplier = 0.02;
			sMultiplierChange = 1;
			sS.setValue(S_I);

			setSSlider();
			sS.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent evt) {
					sSStateChanged(evt);
					if (sSChange) {
						setSMultiplier();
					}
				}
			});
			sS.addMouseListener(new MouseListener() {

				public void mouseClicked(MouseEvent e) {
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

				public void mousePressed(MouseEvent e) {
					sSChange = false;
				}

				public void mouseReleased(MouseEvent e) {
					setSMultiplier();
					sSChange = true;
				}

			});

			// queueBuffer slider
			buffPanel.setLayout(new GridLayout(2, 1));
			c.gridx = 4;
			buffPanel.setVisible(false);
			parametersP.add(buffPanel, c);
			buffL = new JLabel();
			buffL.setAlignmentX(SwingConstants.CENTER);
			buffL.setFont(dCst.getNormalGUIFont());
			buffPanel.add(buffL);
			buffS.setValue(BUFF_I);
			buffS.setMaximum(31);
			buffS.setMinimum(1);
			buffS.setMajorTickSpacing(5);
			buffS.setMinorTickSpacing(1);
			buffS.setPaintLabels(true);
			buffPanel.add(buffS);
			buffL.setText(bufStrS + buffS.getValue() + bufStrE);
			buffS.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent evt) {
					buffSStateChanged(evt);
				}
			});

			// results
			resultsP.setLayout(new GridLayout(2, 2));
			resultsP.setBorder(addTitle("Simulation Results", dCst.getSmallGUIFont()));
			c.gridx = 0;
			c.gridy = 1;
			simulationP.add(resultsP, c);

			// media
			mediaJobsL.setText(nStrS + "0" + nStrE);
			mediaJobsL.setFont(dCst.getNormalGUIFont());
			resultsP.add(mediaJobsL);

			// utilization
			utilizationL.setText(uStrS + "0" + uStrE);
			utilizationL.setFont(dCst.getNormalGUIFont());
			resultsP.add(utilizationL);

			// throughput
			thrL.setText(thrStrS + "0" + thrStrE);
			thrL.setFont(dCst.getNormalGUIFont());
			resultsP.add(thrL);

			// response time
			responseL.setText(respStrS + "0" + respStrE);
			responseL.setFont(dCst.getNormalGUIFont());
			resultsP.add(responseL);

			updateFields();

			outputP.setLayout(new GridLayout(2, 1));
			c.weightx = 1;
			c.weighty = 0.7;
			c.gridy = 2;
			simulationP.add(outputP, c);
			outputP.add(outputTabP);
			txtScroll.setBorder(addTitle("Simulation Output", dCst.getSmallGUIFont()));
			txtScroll.setName("Text Output");
			outputTabP.add(statiDrawer);
			outputTabP.setTitleAt(0, "States");
			outputTabP.add(txtScroll);
			outputTabP.setTitleAt(1, "Log");
			//outputTA.setEditable(false);
			outputTA.setAutoscrolls(true);
			txtScroll.add(outputTA);
			txtScroll.setViewportView(outputTA);
			// outputTabP.add(logD);
			// outputTabP.setTitleAt(2, "Results");
			outputP.add(queueDrawer);

			//-----------------------new for the size of outputP
			outputP.setPreferredSize(new Dimension(200,200));

			JPanel p = new JPanel(new GridLayout(1, 2));
			p.add(accelerationP);
			p.add(jobsP);
			c.weightx = 0;
			c.weighty = 0;
			c.gridy = 3;
			simulationP.add(p, c);
			c.gridx = 0;
			c.weightx = 0;
			c.gridy = 4;
			c.fill = GridBagConstraints.HORIZONTAL;

			menu();
			parent.setMenuBar(menuB);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Create the menu of this panel */
	public void menu(){
		// menu
		menuB = new JMenuBar();
		// queue
		queueMenu = new JMenu("Queue");

		/*selectQueueRB = new AbstractAction("Select Station Type") {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				// action code goes here
				selectMethod();
			}

		}; */ //removed since now are all separated

		//queueMenu.add(selectQueueRB);

		queueMenu.addSeparator();
		// exitMenuItem = new JMenuItem();
		Action exitAction = new AbstractAction("Exit") {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				parent.dispose();
				parent.close();			
			}

		};
		queueMenu.add(exitAction);
		menuB.add(queueMenu);

		// settings
		settingsMenu = new JMenu("Settings");
		colorsMenu = new JMenu("Colors");
		Action queueCAction = new AbstractAction("Probability...") {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				// action code goes here
				Color tmpC;
				tmpC = JColorChooser.showDialog(null, "Probability color", probC);
				if (tmpC != null) {
					if (DEBUG) {
						System.out.println("queueC - R:" + tmpC.getRed() + " G:" + tmpC.getGreen() + " B:" + tmpC.getBlue());
					}
					probC = tmpC;
					changeColors();
				}
			}

		};
		colorsMenu.add(queueCAction);
		Action queueFCAction = new AbstractAction("Queue...") {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				// action code goes here
				Color tmpC;
				tmpC = JColorChooser.showDialog(null, "Queue color", queueC);
				if (tmpC != null) {
					if (DEBUG) {
						System.out.println("queueFC - R:" + tmpC.getRed() + " G:" + tmpC.getGreen() + " B:" + tmpC.getBlue());
					}
					queueC = tmpC;
					changeColors();
				}
			}

		};
		colorsMenu.add(queueFCAction);
		colorsMenu.addSeparator();
		Action statusCAction = new AbstractAction("Empty state...") {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				// action code goes here
				Color tmpC;
				tmpC = JColorChooser.showDialog(null, "Empty state color", emptyC);
				if (tmpC != null) {
					if (DEBUG) {
						System.out.println("statusC - R:" + tmpC.getRed() + " G:" + tmpC.getGreen() + " B:" + tmpC.getBlue());
					}
					emptyC = tmpC;
					changeColors();
				}
			}

		};
		colorsMenu.add(statusCAction);
		Action animCAction = new AbstractAction("Animation...") {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				// action code goes here
				Color tmpC;
				tmpC = JColorChooser.showDialog(null, "Animation color", animC);
				if (tmpC != null) {
					if (DEBUG) {
						System.out.println("animC - R:" + tmpC.getRed() + " G:" + tmpC.getGreen() + " B:" + tmpC.getBlue());
					}
					animC = tmpC;
					changeColors();
				}
			}

		};
		colorsMenu.add(animCAction);
		colorsMenu.addSeparator();

		// gradientItem = new JRadioButtonMenuItem("usa gradiente", false);
		gradientItem = new JRadioButtonMenuItem("Use gradient", false);
		gradientItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gradientF = gradientItem.isSelected();
				changeColors();
			}
		});
		colorsMenu.add(gradientItem);
		settingsMenu.add(colorsMenu);

		// sizeMenu = new JMenu("Dimensioni");
		sizeMenu = new JMenu("Icon size");

		// Action drawSmallAction = new AbstractAction("Piccole") {
		Action drawSmallAction = new AbstractAction("Small") {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				// action code goes here
				dCst = new DrawSmall();
				changeSize();
			}

		};
		sizeMenu.add(drawSmallAction);

		// Action drawNormalAction = new AbstractAction("Normali") {
		Action drawNormalAction = new AbstractAction("Normal") {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				// action code goes here
				dCst = new DrawNormal();
				changeSize();
			}

		};
		sizeMenu.add(drawNormalAction);
		// Action drawBigAction = new AbstractAction("Grandi") {
		Action drawBigAction = new AbstractAction("Large") {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				// action code goes here
				dCst = new DrawBig();
				changeSize();
			}

		};
		sizeMenu.add(drawBigAction);
		settingsMenu.add(sizeMenu);

		menuB.add(settingsMenu);

		// help
		helpMenu = new JMenu("Help");

		JMenuItem help = new JMenuItem();
		help.setText("JMCH Help");
		help.setToolTipText("Show JMCH help");
		help.addActionListener(new ActionListener() {
			/**
			 * Invoked when an action occurs.
			 */
			public void actionPerformed(ActionEvent e) {
				Runnable r = new Runnable() {
					public void run() {
						try {
							new PDFViewer("JMCH Manual", ChapterIdentifier.JMCH);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				EventQueue.invokeLater(r);
			}
		});
		helpMenu.add(help);

		helpMenu.addSeparator();

		JMenuItem about = new JMenuItem();
		about.setText("About JMCH");
		about.setToolTipText("About JMCH");
		about.addActionListener(new ActionListener() {
			/**
			 * Invoked when an action occurs.
			 */
			public void actionPerformed(ActionEvent e) {
				AboutDialogFactory.showJMCH(parent);
			}
		});
		helpMenu.add(about);

		menuB.add(helpMenu);
	}

	/**
	 * Changes the size of the drawing text
	 */
	protected void changeSize() {
		queueDrawer.changeDrawSettings(dCst);
		queueDrawer.repaint();
		statiDrawer.changeDrawSettings(dCst);
		statiDrawer.repaint();
		outputTA.changeDrawSettings(dCst);
		// logD.changeDrawSettings(dCst);
		validate();

	}

	protected void changeColors() {
		queueDrawer.setColors(emptyC, queueC, animC, gradientF);
		queueDrawer.repaint();
		statiDrawer.setColors(emptyC, queueC, probC, animC);
		statiDrawer.repaint();
	}

	protected void buffSStateChanged(ChangeEvent evt) {
		buffer = buffS.getValue() - cpuNum;
		if (buffer < 1) {
			buffS.setValue(1);
			buffer = 1;
		}
		ql.setMaxStates(buffer);
		queueDrawer.setMaxJobs(buffer + 1);
		statiDrawer.setMaxJobs(buffer + cpuNum);
		buffL.setText(bufStrS + buffS.getValue() + bufStrE);
		updateFields();
	}

	protected void showQueue(int queueType, int cpuNumber) {
		buffer = BUFF_I;
		cpuNum = cpuNumber;
		buffS.setMaximum(30 + cpuNumber + 1);
		buffS.setMinimum(cpuNumber + 1);
		buffS.setValue(buffer + cpuNumber);
		switch (queueType) {

		case 0:
			buffer = 0;
			ql = new MM1Logic(lambdaMultiplier * lambdaS.getValue(), sS.getValue() * sMultiplier);
			buffPanel.setVisible(false);
			sS.setValue(S_I);
			lambdaS.setValue(LAMBDA_I);
			statiDrawer.updateLogic(ql);
			queueDrawer.updateLogic(ql);
			queueDrawer.setMaxJobs(0);
			statiDrawer.setMaxJobs(0);
			queueDrawer.setCpuNumber(1);
			updateFields();
			//this.setTitle("JMCH - Markov Chain M/M/1  Station");
			break;

		case 1:
			//buffer = BUFF_I;
			ql = new MM1dLogic(lambdaMultiplier * lambdaS.getValue(), sS.getValue() * sMultiplier, buffer);
			buffPanel.setVisible(true);
			sS.setValue(S_I);
			lambdaS.setValue(LAMBDA_I);
			statiDrawer.updateLogic(ql);
			queueDrawer.updateLogic(ql);
			queueDrawer.setMaxJobs(buffer + 1);
			statiDrawer.setMaxJobs(buffer + 1);
			queueDrawer.setCpuNumber(1);
			updateFields();
			//this.setTitle("JMCH - Markov Chain M/M/1/k Finite Capacity Station");
			break;

		case 2:
			buffer = 0;
			ql = new MMNLogic(lambdaMultiplier * lambdaS.getValue(), sS.getValue() * sMultiplier, cpuNumber);
			buffPanel.setVisible(false);
			sS.setValue(S_I);
			lambdaS.setValue(LAMBDA_I);
			statiDrawer.updateLogic(ql);
			queueDrawer.updateLogic(ql);
			queueDrawer.setMaxJobs(0);
			statiDrawer.setMaxJobs(0);
			queueDrawer.setCpuNumber(cpuNumber);
			updateFields();
			//this.setTitle("JMCH - Markov Chain M/M/" + cpuNumber + " Station");
			break;

		case 3:
			//buffer = BUFF_I;
			ql = new MMNdLogic(lambdaMultiplier * lambdaS.getValue(), sS.getValue() * sMultiplier, cpuNumber, buffer);
			buffPanel.setVisible(true);
			sS.setValue(S_I);
			lambdaS.setValue(LAMBDA_I);
			statiDrawer.updateLogic(ql);
			queueDrawer.updateLogic(ql);
			queueDrawer.setMaxJobs(buffer + 1);
			statiDrawer.setMaxJobs(buffer + cpuNumber);
			queueDrawer.setCpuNumber(cpuNumber);
			updateFields();
			//this.setTitle("JMCH - Markov Chain M/M/" + cpuNumber + "/k Finite Capacity Station");
			break;

		default:
			break;
		}
	}

	private void setLogAnalyticalResults() {
		try {
			if (ql.getMaxStates() == 0) {
				outputTA.setAnalyticalResult(ql.mediaJobs(), ql.utilization(), ql.throughput(), ql.responseTime(), ql.getLambda(), ql.getS(), 0);
			} else {
				outputTA.setAnalyticalResult(ql.mediaJobs(), ql.utilization(), ql.throughput(), ql.responseTime(), ql.getLambda(), ql.getS(), ql
						.getStatusProbability(ql.getMaxStates() + ql.getNumberServer()));
			}
		} catch (NonErgodicException e) {
			outputTA.setAnalyticalResult();
		}
	}

	public void playBActionPerformed() {
		boolean goOn = true;

		if (nonErgodic) {
			if (JOptionPane.showConfirmDialog(this, "The process is not ergodico.\nDo you want to continue?", "Warning", JOptionPane.YES_NO_OPTION)
					== JOptionPane.NO_OPTION) {
				goOn = false;
			}
		}
		if (goOn) {
			CustomDialog jobsDialog = new CustomDialog(parent);
			jobsDialog.pack();
			jobsDialog.setLocationRelativeTo(mf);
			jobsDialog.setVisible(true);
			logFile.setLogging(false);
			if (jobsDialog.isLogging()) {
				if (jobsDialog.getLogFile() == null) {
					JOptionPane.showMessageDialog(mf, "Logging check box is selected \n" + "but file is not selected.", "Warning",
							JOptionPane.WARNING_MESSAGE);
				} else {
					logFile.setLogFile(jobsDialog.getLogFile(), jobsDialog.getDelimiterType(), mf);
					logFile.setLogging(true);
				}
			}

			jq = new JobQueue();
			queueDrawer.setMediaJobs(Q - U);
			queueDrawer.setTotalJobs(jobsDialog.getValidatedValue());
			jobsDrawer.setTotalJobs(jobsDialog.getValidatedValue());
			tan[0] = outputTA;
			tan[1] = queueDrawer;
			tan[2] = statiDrawer;
			tan[3] = jobsDrawer;
			tan[4] = logFile;

			arrival = new Arrivals(ql, jq, tan, jobsDialog.getValidatedValue());

			int numServer;
			numServer = ql.getNumberServer();
			processors = new Processor[numServer];
			for (int i = 0; i < numServer; i++) {
				processors[i] = new Processor(ql, jq, tan, i/*, jobsDialog.getValidatedValue()*/);
			}

			sim = new Simulator(arrival, processors, accelerationS.getValue(), tan);

			arrival.sim = sim;
			for (int i = 0; i < numServer; i++) {
				processors[i].sim = sim;
			}

			sim.start();

			setLogAnalyticalResults();
		}
	}

	public void stopBActionPerformed() {
		stopProcessing(true);
	}

	/** Auto-generated event handler method */
	public void pauseBActionPerformed() {
		if (paused) {
			paused = false;
			sim.pause();
		} else {
			paused = true;
			sim.pause();
			outputTA.refresh();
			logFile.refresh();
		}
	}

	/** Auto-generated event handler method */
	protected void lambdaSStateChanged(ChangeEvent evt) {
		if (lambdaS.getValue() == 0) {
			lambdaMultiplier = 0.01;
			lambdaMultiplierChange = 0;
			lambdaS.setValue(1);
		}
		ql.setLambda(lambdaMultiplier * lambdaS.getValue());
		lambdaL.setText(lambdaStrS + Formatter.formatNumber(lambdaS.getValue() * lambdaMultiplier, 2) + lambdaStrE);
		setSSlider();
		updateFields();
	}

	protected void sSStateChanged(ChangeEvent evt) {
		setSSlider();
		updateFields();
	}

	private void updateFields() {
		try {
			Q = ql.mediaJobs();
			U = ql.utilization();
			utilizationL.setForeground(Color.BLACK);
			utilizationL.setText(uStrS + Formatter.formatNumber(U, 2) + uStrE);
			mediaJobsL.setText(nStrS + Formatter.formatNumber(Q, 2) + nStrE);

			thrL.setText(thrStrS + Formatter.formatNumber(ql.throughput(), 2) + thrStrE);
			responseL.setText(respStrS + Formatter.formatNumber(ql.responseTime(), 2) + respStrE);
			nonErgodic = false;

			if (sim != null && ql.getLambda() > 0) {
				sim.setLambdaZero(false);
			}
		} catch (NonErgodicException e) {
			Q = 0.0;
			U = 0.0;
			mediaJobsL.setText(nStrS + "Saturation");

			utilizationL.setForeground(Color.RED);
			utilizationL.setText(uStrS + "Saturation");
			thrL.setText(thrStrS + "Saturation");
			responseL.setText(respStrS + "Saturation");
			nonErgodic = true;
		}
		queueDrawer.setMediaJobs(Q - U);
		statiDrawer.repaint();

		if (sim == null || !sim.isStarted()) {
			setLogAnalyticalResults();
		} else {
			outputTA.setAnalyticalResult();
		}
	}

	private TitledBorder addTitle(String title, Font f) {
		return new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.TOP, f, new java.awt.Color(0, 0, 0));
	}

	public void stopProcessing(boolean isSaveFileOption) {
		sim.stop();
		while (sim.isRunning()) {
			//waiting to stop
			try {Thread.sleep(100);} 
			catch (InterruptedException e) {}
		}
		try {Thread.sleep(100);} 
		catch (InterruptedException e) {}
		jq.clearQueue();
		outputTA.reset();
		logFile.reset();
		queueDrawer.reset();
		statiDrawer.reset();
		jobsDrawer.reset();

		playB.setEnabled(true);
		stopB.setEnabled(false);
		pauseB.setEnabled(false);
		selectQueueRB.setEnabled(true);

		updateFields();
	}

	public void setSSlider() {
		//sMultiplier = ql.getMaxErgodicS();
		Dictionary<Integer, JLabel> d = sS.getLabelTable();
		//for (int i = 0; i < 6; i++) {
		//	d.put(new Integer(i * 25), new JLabel("" + Formatter.formatNumber(i * sMultiplier), 2));
		//}
		for (int i = sS.getMinimum(); i <= sS.getMaximum(); i += sS.getMajorTickSpacing()) {
			d.put(new Integer(i), new JLabel("" + Formatter.formatNumber(i * sMultiplier, 2)));
		}
		sS.setLabelTable(d);
		sL.setText(sStrS + Formatter.formatNumber(sS.getValue() * sMultiplier, 2) + sStrE);
		sS.repaint();
		ql.setS(sS.getValue() * sMultiplier);
	}

	public void setLambdaSlider() {
		Dictionary<Integer, JLabel> ld = lambdaS.getLabelTable();

		for (int i = lambdaS.getMinimum(); i <= lambdaS.getMaximum(); i += lambdaS.getMajorTickSpacing()) {
			ld.put(new Integer(i), new JLabel("" + Formatter.formatNumber(i * lambdaMultiplier, 2)));
		}

		//for (int i = 0; i <= 4; i++) {
		//	ld.put(new Integer(i * 25), new JLabel("" + Formatter.formatNumber(i * 0.25, 2)));
		//}
		lambdaS.setLabelTable(ld);
		ql.setLambda(lambdaMultiplier * lambdaS.getValue());
		lambdaL.setText(lambdaStrS + Formatter.formatNumber(lambdaS.getValue() * lambdaMultiplier, 2) + lambdaStrE);
	}

	public void setLambdaMultiplier() {
		while (true) {
			if (lambdaS.getValue() > lambdaS.getMaximum() * 0.95) {
				if (lambdaMultiplierChange <= 4) {
					if (lambdaMultiplierChange % 2 == 0) {
						lambdaMultiplier *= 2;
						setLambdaSlider();
						lambdaS.setValue((lambdaS.getValue() + 1) / 2);
					} else {
						lambdaMultiplier *= 5;
						setLambdaSlider();
						lambdaS.setValue((lambdaS.getValue() + 1) / 5);
					}
					lambdaMultiplierChange++;
				} else {
					break;
				}
			} else if (lambdaS.getValue() < lambdaS.getMaximum() * 0.05) {
				if (lambdaMultiplierChange > 0) {
					if (lambdaMultiplierChange % 2 == 1) {
						lambdaMultiplier /= 2;
						setLambdaSlider();
						lambdaS.setValue(lambdaS.getValue() * 2);
					} else {
						lambdaMultiplier /= 5;
						setLambdaSlider();
						lambdaS.setValue(lambdaS.getValue() * 5);
					}
					lambdaMultiplierChange--;
				} else {
					break;
				}
			} else {
				break;
			}
		}
	}

	public void setSMultiplier() {
		while (true) {
			if (sS.getValue() > sS.getMaximum() * 0.95) {
				if (sMultiplierChange <= 4) {
					if (sMultiplierChange % 2 == 0) {
						sMultiplier *= 2;
						setSSlider();
						sS.setValue((sS.getValue() + 1) / 2);
					} else {
						sMultiplier *= 5;
						setSSlider();
						sS.setValue((sS.getValue() + 1) / 5);
					}
					sMultiplierChange++;
				} else {
					break;
				}
			} else if (sS.getValue() < sS.getMaximum() * 0.05) {
				if (sMultiplierChange > 0) {
					if (sMultiplierChange % 2 == 1) {
						sMultiplier /= 2;
						setSSlider();
						sS.setValue(sS.getValue() * 2);
					} else {
						sMultiplier /= 5;
						setSSlider();
						sS.setValue(sS.getValue() * 5);
					}
					sMultiplierChange--;
				} else {
					break;
				}
			} else {
				break;
			}
		}
	}

	private JPanel getSplitter(int widht, int height) {
		JPanel splitPane = new JPanel();
		Dimension dim = new Dimension(widht, height);
		splitPane.setEnabled(false);
		splitPane.setPreferredSize(dim);
		splitPane.setMaximumSize(dim);
		splitPane.setMinimumSize(dim);
		return splitPane;
	}

	private void selectMethod(String selectedMethod) {
		if (selectedMethod == "mm1") {
			showQueue(0, 1);
		} else if (selectedMethod == "mm1k") {
			showQueue(1, 1);
		} else if (selectedMethod == "mmn") {
			MMQueuesDialog md = displayDialog();
			showQueue(2, md.getValidatedValue());
		} else if (selectedMethod == "mmnk") {
			MMQueuesDialog md = displayDialog();
			showQueue(3, md.getValidatedValue());
		}
	}

	private MMQueuesDialog displayDialog(){
		MMQueuesDialog md = new MMQueuesDialog(parent);
		md.pack();
		md.setLocationRelativeTo(parent);
		md.setVisible(true);
		return md;
	}
}
