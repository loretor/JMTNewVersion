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

package jmt.engine.simEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jmt.common.exception.LoadException;
import jmt.common.exception.NetException;
import jmt.engine.NodeSections.*;
import jmt.engine.QueueNet.BlockingRegion;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NetSystem;
import jmt.engine.QueueNet.NodeSection;
import jmt.engine.QueueNet.QueueNetwork;
import jmt.engine.QueueNet.SimConstants;
import jmt.engine.dataAnalysis.Measure;
import jmt.engine.dataAnalysis.SimParameters;
import jmt.engine.dataAnalysis.XMLSimulationOutput;
import jmt.engine.dataAnalysis.measureOutputs.VerboseCSVMeasureOutput;
import jmt.engine.log.JSimLogger;
import jmt.engine.random.engine.RandomEngine;

/**
 * This class creates a new Simulation. It provides an easy way to initialize
 * the jmt engine.
 *
 * @author Federico Granata, Stefano Omini, Bertoli Marco
 * 
 * Modified by Ashanka (May 2010): 
 * Patch: Multi-Sink Perf. Index 
 * Description: Added new Performance index for capturing 
 * 				1. global response time (ResponseTime per Sink)
 *              2. global throughput (Throughput per Sink)
 *              each sink per class.
 */
public class Simulation {

	/** Output file handler */
	File outputFile;

	//---------------------- SIMULATION COMPONENTS --------------------------//

	//name of the simulation
	private String name;
	//queueing network associated to this simulation
	private QueueNetwork network;

	//classes of the system
	private JobClass[] classes = null;
	//service centers of the system
	private List<SimNode> nodes = new ArrayList<SimNode>();
	//Burst distributions to register to the event handler
	private List<NetNode> distrNetNodes = new ArrayList<NetNode>();
	//connections
	private List<Connection> connections = new ArrayList<Connection>();
	//measures requested by the simulation
	private List<SimMeasure> measures = new ArrayList<SimMeasure>();
	//blocking region measures requested by the simulator
	private List<SimMeasure> regionMeasures = new ArrayList<SimMeasure>();
	//blocking regions
	private List<BlockingRegion> regions = new ArrayList<BlockingRegion>();

	//used to run the simulation only if the queue network has been initialized
	private boolean initialized = false;

	//logger of the simulation
	private JSimLogger logger = JSimLogger.getLogger(JSimLogger.STD_LOGGER);

	//---------------------- end SIMULATION COMPONENTS --------------------------//

	//---------------------- SIMULATION TIMER --------------------------//

	//the timer which controls max simulation time
	//true if the simulation has finished
	private boolean finished = false;

	//---------------------- end SIMULATION TIMER --------------------------//

	private SimParameters simParameters = null;

	//---------------------- XML FILES PATH --------------------------//
	//path of the xml file containing the mva model description
	//this attribute has been set only if a SimLoader has been used to
	//create this Simulation object
	String xmlModelDefPath;

	//path of the xml file containing the simulation model description
	//this attribute has been set only if a SimLoader has been used to
	//create this Simulation object
	String xmlSimModelDefPath;
	//---------------------- end XML FILES PATH --------------------------//

	//---------------------- PRELOAD ------------------------------------//

	//if true preload jobs
	private boolean preloadEnabled = false;
	//the names of the stations to be preloaded
	private String[] preload_stationNames;
	//the matrix of initial populations
	private int[][] preload_initialPopulations;

	//---------------------- end PRELOAD -------------------------------//

	private boolean isTerminalSimulation = false;
	private int parametricStep = -1;

	//-------------------------CONSTRUCTORS-------------------------------//

	private NetSystem netSystem;

	/**
	 * Creates a new Simulation object.
	 * @param seed simulation seed (if -1, an automatic seed will be generated)
	 * @param simName Name of simulation
	 * @param maxSimulationTime The max duration (in milliseconds) of the simulation
	 * @throws IOException
	 *
	 */
	public Simulation(long seed, String simName, long maxSimulationTime) throws IOException {
		//name of the simulation
		name = simName;

		//Initializes NetSystem
		netSystem = new NetSystem();
		netSystem.initialize();

		//sets seed (if -1 --> automatic seed)
		if (seed != -1) {
			//sets the seed
			setRandomEngineSeed(seed);
		}
	}

	/**
	 * Creates a new Simulation object.
	 * @param seed simulation seed (if -1, an automatic seed will be generated)
	 * @param simName Name of simulation
	 * @throws IOException
	 */
	public Simulation(long seed, String simName) throws IOException {
		//name of the simulation
		name = simName;

		//Initializes NetSystem
		netSystem = new NetSystem();
		netSystem.initialize();

		//sets seed (if -1 --> automatic seed)
		if (seed != -1) {
			//sets the seed
			setRandomEngineSeed(seed);
		}
	}

	//-------------------------end CONSTRUCTORS-------------------------------//

	//--------------METHODS TO ADD SIMULATION COMPONENTS----------------------//

	// These methods are used while class SimLoader loads the model from an xml file:
	// all the simulation components are at first put into vectors,
	// then the method initialize() uses these vectors to setup the Simulation object.

	/**
	 * Adds all Job Classes to the simulation.	 *
	 * @param classes the job classes of simulation
	 */
	public void addClasses(JobClass[] classes) {
		this.classes = classes;
	}

	/**
	 * Adds a node to the simulation model.
	 * @param name name of the node
	 * @param inSec input section of the node
	 * @param serSec service section of the node
	 * @param outSec output section of the node
	 */
	public void addNode(String name, InputSection inSec, ServiceSection serSec, OutputSection outSec) {
		nodes.add(new SimNode(name, inSec, serSec, outSec));
	}

	/**
	 * Connects two nodes of the simulation: if a node has not been inserted in the model,
	 * then return a LoadException.
	 * @param start the source node
	 * @param end the target node
	 * @throws LoadException one of the nodes has not been inserted in the model
	 */
	public void addConnection(String start, String end) throws LoadException {
		if (isNode(start) && isNode(end)) {
			connections.add(new Connection(start, end));
		} else {
			throw new LoadException("Trying to connect nodes that have not been inserted yet.");
		}
	}

	/**
	 * Adds to the system a new measure to be computed.
	 * @param measureType type of measure (see the constants specified in this class).
	 * @param nodeName name of the node to be measured.
	 * @param measure
	 * @param jClass
	 * @throws LoadException
	 */
	public void addMeasure(int measureType, String nodeName, Measure measure, String jClass) throws LoadException {
		addMeasure(measureType, nodeName, measure, jClass, null);
	}

	/**
	 * Adds to the system a new measure to be computed.
	 * @param measureType type of measure (see the constants specified in this class).
	 * @param nodeName name of the node to be measured. [Attribute assign in `referenceNode`]<br>
	 *                 "" (empty string) means Global Measure, no need specify referNode.
	 * @param measure
	 * @param jClass name of the job class to be measured. [Attribute assign in `referenceUserClass`]<br>
	 *               null means measuring all the class, not to the specific class
	 * @param nodeType type of the node to be measured. [Attribute assign in `nodeType`] <br>
	 *                 - null -> Global Measure, no specific referNode. <br>
	 *                 - station -> Node Measure, bind to a specific referNode <br>
	 *                 - region -> Capacity Region Measure.
	 * @throws LoadException
	 */
	public void addMeasure(int measureType, String nodeName, Measure measure, String jClass, String nodeType) throws LoadException {
		//sets all the parameters shared by all Measure object
		// (i.e. number and size of batches, ...) 
		if (simParameters != null) {
			measure.setSimParameters(simParameters);
		}
		if (SimConstants.NODE_TYPE_REGION.equals(nodeType)) {
			// Region measures
			regionMeasures.add(new SimMeasure(measureType, nodeName, measure, jClass));
		} else {
			// Normal measures
			measures.add(new SimMeasure(measureType, nodeName, measure, jClass));
		}
	}

	/**
	 * Creates a blocking region.
	 */
	public void addRegion(String name, double maxCapacity, double maxMemory, double[] maxCapacityPerClass, double[] maxMemoryPerClass, boolean[] drop, double[] classWeights, double[] classSizes,
			double[] classSoftDeadlines, String[] groupNames, double[] maxCapacityPerGroup, double[] maxMemoryPerGroup, List<List<Integer>> groupClassIndexLists, String[] stations) throws LoadException {
		regions.add(new BlockingRegion(name, maxCapacity, maxMemory, maxCapacityPerClass, maxMemoryPerClass, drop, classWeights, classSizes,
				classSoftDeadlines, groupNames, maxCapacityPerGroup, maxMemoryPerGroup, groupClassIndexLists, this, stations));
	}

	//--------------end METHODS TO ADD SIMULATION COMPONENTS---------------//

	//------------------INITIALIZATION AND RUN------------------------------//

	/**
	 * Initializes all the components of this Simulation.
	 * This method assures that all the operations are
	 * executed in the correct order.
	 */
	public void initialize() {
		// Sets the max simulated time for the system, when reached the simulation is stopped
		netSystem.setMaxSimulatedTime(simParameters.getMaxSimulatedTime());

		// Sets the max processed events for the system, when reached the simulation is stopped
		netSystem.setMaxProcessedEvents(simParameters.getMaxProcessedEvents());

		// creates network
		network = new QueueNetwork("JSIM simulation: " + name);
		network.setNetSystem(netSystem);
		network.setTerminalSimulation(isTerminalSimulation);
		network.setParametricStep(parametricStep);
		// adds network to NetSystem
		netSystem.addNetwork(network);

		//add all job classes to QueueNetwork
		for (JobClass classe : classes) {
			network.addJobClass(classe);
		}

		try {
			//creates all nodes
			NetNode[] netNodes = new NetNode[nodes.size()];
			for (int i = 0; i < nodes.size(); i++) {
				netNodes[i] = (nodes.get(i)).getNode();
			}

			//add connections
			for (int i = 0; i < connections.size(); i++) {
				int nodePosition1 = findNodePosition(connections.get(i).getStart());
				int nodePosition2 = findNodePosition(connections.get(i).getEnd());
				netNodes[nodePosition1].connect(netNodes[nodePosition2]);
			}

			//add all nodes to QueueNetwork
			for (int i = 0; i < nodes.size(); i++) {
				SimNode simNode = (nodes.get(i));
				if (simNode.isReference()) {
					//reference nodes are the nodes (random source, terminal, ..)
					//which create jobs: these nodes must receive the start event
					network.addReferenceNode(simNode.getNode());
				} else {
					network.addNode(simNode.getNode());
				}
			}

			//add distribution net nodes to QueueNetwork
			for (int i = 0; i < distrNetNodes.size(); i++) {
				network.addNode(distrNetNodes.get(i));
			}

			//add all nodes sections
			for (int i = 0; i < nodes.size(); i++) {
				SimNode n = nodes.get(i);
				if (n.getInput() != null) {
					n.getNode().addSection(n.getInput());
				}
				if (n.getService() != null) {
					n.getNode().addSection(n.getService());
				}
				if (n.getOutput() != null) {
					n.getNode().addSection(n.getOutput());
				}
				n.getNode().initializeSections();
			}

			//add blocking regions
			BlockingRegion br;
			for (int i = 0; i < regions.size(); i++) {
				br = regions.get(i);

				String regionName = br.getName();
				String inputStationName = regionName + "_inputStation";

				InputSection is = new BlockingQueue(br);
				ServiceSection ss = new ServiceTunnel();
				OutputSection os = new BlockingRouter();

				SimNode inputStation = new SimNode(inputStationName, is, ss, os);
				inputStation.getNode().setBlockingRegionInputStation(br);

				//adds the input station of the blocking region
				nodes.add(inputStation);

				network.addNode(inputStation.getNode());

				//auto-connect the node, to avoid problems in job info lists refreshing
				//(otherwise a node with no connections presents problems)
				inputStation.getNode().connect(inputStation.getNode());

				NetNode inputSt = inputStation.getNode();
				if (inputStation.getInput() != null) {
					inputStation.getNode().addSection(inputStation.getInput());
				}
				if (inputStation.getService() != null) {
					inputStation.getNode().addSection(inputStation.getService());
				}
				if (inputStation.getOutput() != null) {
					inputStation.getNode().addSection(inputStation.getOutput());
				}
				inputStation.getNode().initializeSections();

				//sets the input station of the blocking region
				br.setInputStation(inputSt);

				//sets blocking region behaviour for inner nodes
				String[] regNodes = br.getRegionNodeNames();
				for (String regNode : regNodes) {
					NetNode innerNode = br.getRegionNode(regNode);

					//at the moment inner stations must have a Queue-type input section
					//and a Router-type output section
					//other not compliant sections will cause a NetException

					//nodes which receive jobs from the outside must have the redirection
					//behaviour turned on
					NodeSection input = innerNode.getSection(NodeSection.INPUT);
					if (input instanceof Queue) {
						((Queue) input).redirectionTurnON(br);
					} else if (input instanceof Storage) {
						((Storage) input).redirectionTurnON(br);
					} else {
						throw new NetException("Error in creating blocking region: " + "inner station " + innerNode.getName()
						+ " has a not compliant input section.");
					}

					//nodes which send jobs outside the region must have the border router
					//behaviour turned on
					NodeSection output = innerNode.getSection(NodeSection.OUTPUT);
					if (output instanceof Router) {
						((Router) output).borderRouterTurnON(br);
					} else if (output instanceof Linkage) {
						((Linkage) output).borderRouterTurnON(br);
					} else {
						throw new NetException("Error in creating blocking region: " + "inner station " + innerNode.getName()
						+ " has a not compliant output section.");
					}
				}
				if (regionMeasures.size() > 0) {
					//adds measures in blocking region input station
					for (Iterator<SimMeasure> it = regionMeasures.iterator(); it.hasNext();) {
						SimMeasure measure = it.next();
						if (measure.getNodeName().equals(regionName)) {
							addMeasure(measure.getMeasureType(), inputStationName, measure.getMeasure(), measure.getjClass());
							it.remove();
						}
					}
				}
			}

			//refresh nodes list, after input stations has been added
			netNodes = new NetNode[nodes.size()];
			for (int i = 0; i < nodes.size(); i++) {
				netNodes[i] = (nodes.get(i)).getNode();
			}

			//add measures
			SimMeasure ms;
			for (int i = 0; i < measures.size(); i++) {
				ms = measures.get(i);
				boolean verbose = ms.getMeasure().getVerbose();
				if (verbose) {
					//if true, for each Measure sets the corresponding MeasureOutput
					//a measure output can be a file (xml, txt, ...) which contains
					//samples and final report of that measure
					new VerboseCSVMeasureOutput(ms.getMeasure(), simParameters, isTerminalSimulation, parametricStep);
				}
				network.addMeasure(ms.getMeasure());
			}

			//initialize measures
			for (int i = 0; i < measures.size(); i++) {
				ms = measures.get(i);

				JobClass jClass;
				if (ms.getjClass() != "" && ms.getMeasureType() != SimConstants.FIRING_THROUGHPUT) {
					//measure relative to a specified class
					String[] jName = { ms.getjClass() };
					int[] position = findClassPosition(jName);
					jClass = classes[position[0]];
				} else {
					//aggregated measure
					// job class is null means it measure all the class.
					jClass = null;
				}

				// If measure is not global
				if (ms.getNodeName() != null && !ms.getNodeName().equals("")) {
					// If referenceNode specified, this measure is targeted to the specific node.
					//measures are computed by sampling specific
					int nodePosition = findNodePosition(ms.getNodeName());

					switch (ms.getMeasureType()) {
					case SimConstants.TARDINESS:
						netNodes[nodePosition].analyze(SimConstants.TARDINESS, jClass, ms.getMeasure());
						break;
					case SimConstants.EARLINESS:
						netNodes[nodePosition].analyze(SimConstants.EARLINESS, jClass, ms.getMeasure());
						break;
					case SimConstants.LATENESS:
						netNodes[nodePosition].analyze(SimConstants.LATENESS, jClass, ms.getMeasure());
						break;
					case SimConstants.QUEUE_LENGTH:
						netNodes[nodePosition].analyze(SimConstants.LIST_NUMBER_OF_JOBS, jClass, ms.getMeasure());
						break;
					case SimConstants.QUEUE_TIME:
						if (netNodes[nodePosition].getSection(NodeSection.SERVICE) instanceof PSServer) {
							netNodes[nodePosition].getSection(NodeSection.SERVICE).analyze(SimConstants.LIST_RESIDENCE_TIME, jClass, ms.getMeasure());
						} else {
							netNodes[nodePosition].getSection(NodeSection.INPUT).analyze(SimConstants.LIST_RESIDENCE_TIME, jClass, ms.getMeasure());
						}
						break;
					case SimConstants.RESPONSE_TIME:
						netNodes[nodePosition].analyze(SimConstants.LIST_RESPONSE_TIME, jClass, ms.getMeasure());
						break;
					case SimConstants.RESIDENCE_TIME:
						netNodes[nodePosition].analyze(SimConstants.LIST_RESIDENCE_TIME, jClass, ms.getMeasure());
						break;
					case SimConstants.ARRIVAL_RATE:
						netNodes[nodePosition].analyze(SimConstants.LIST_ARRIVAL_RATE, jClass, ms.getMeasure());
						break;
					case SimConstants.THROUGHPUT:
						if (netNodes[nodePosition].getSection(NodeSection.OUTPUT) instanceof Fork) {
							netNodes[nodePosition].getSection(NodeSection.SERVICE).analyze(SimConstants.LIST_THROUGHPUT, jClass, ms.getMeasure());
						} else {
							netNodes[nodePosition].getSection(NodeSection.OUTPUT).analyze(SimConstants.LIST_THROUGHPUT, jClass, ms.getMeasure());
						}
						break;
					case SimConstants.UTILIZATION:
						if (netNodes[nodePosition].getSection(NodeSection.INPUT) instanceof Join) {
							netNodes[nodePosition].getSection(NodeSection.INPUT).analyzeJoin(SimConstants.UTILIZATION, jClass, ms.getMeasure());
						} else {
							netNodes[nodePosition].getSection(NodeSection.SERVICE).analyze(SimConstants.LIST_NUMBER_OF_JOBS, jClass, ms.getMeasure());
						}
						break;
					case SimConstants.EFFECTIVE_UTILIZATION:
						if (netNodes[nodePosition].getSection(NodeSection.SERVICE) instanceof Server) {
							netNodes[nodePosition].getSection(NodeSection.SERVICE).analyze(SimConstants.LIST_NUMBER_OF_JOBS_IN_SERVICE, jClass, ms.getMeasure());
						} else {
							netNodes[nodePosition].getSection(NodeSection.SERVICE).analyze(SimConstants.LIST_NUMBER_OF_JOBS, jClass, ms.getMeasure());
						}
						break;
					case SimConstants.DROP_RATE:
						netNodes[nodePosition].analyze(SimConstants.LIST_DROP_RATE, jClass, ms.getMeasure());
						break;
					case SimConstants.BALKING_RATE:
						netNodes[nodePosition].analyze(SimConstants.LIST_BALKING_RATE, jClass, ms.getMeasure());
						break;
					case SimConstants.RENEGING_RATE:
						netNodes[nodePosition].analyze(SimConstants.LIST_RENEGING_RATE, jClass, ms.getMeasure());
						break;
					case SimConstants.RETRIAL_ATTEMPTS_RATE:
						netNodes[nodePosition].analyze(SimConstants.LIST_RETRIAL_RATE, jClass, ms.getMeasure());
						break;
					case SimConstants.RETRIAL_ORBIT_SIZE:
						netNodes[nodePosition].analyze(SimConstants.LIST_RETRIAL_ORBIT_SIZE, jClass, ms.getMeasure());
						break;
					case SimConstants.RETRIAL_ORBIT_TIME:
						netNodes[nodePosition].analyze(SimConstants.LIST_RETRIAL_ORBIT_TIME, jClass, ms.getMeasure());
						break;
					case SimConstants.RESPONSE_TIME_PER_SINK:
						netNodes[nodePosition].analyze(SimConstants.RESPONSE_TIME_PER_SINK, jClass, ms.getMeasure());
						break;
					case SimConstants.THROUGHPUT_PER_SINK:
						netNodes[nodePosition].analyze(SimConstants.THROUGHPUT_PER_SINK, jClass, ms.getMeasure());
						break;
					case SimConstants.FCR_TOTAL_WEIGHT:
						netNodes[nodePosition].getSection(NodeSection.INPUT).analyzeFCR(SimConstants.FCR_TOTAL_WEIGHT, ms.getMeasure());
						break;
					case SimConstants.FCR_MEMORY_OCCUPATION:
						netNodes[nodePosition].getSection(NodeSection.INPUT).analyzeFCR(SimConstants.FCR_MEMORY_OCCUPATION, ms.getMeasure());
						break;
					case SimConstants.FORK_JOIN_NUMBER_OF_JOBS:
						netNodes[nodePosition].getSection(NodeSection.INPUT).analyzeFJ(SimConstants.FORK_JOIN_NUMBER_OF_JOBS, jClass, ms.getMeasure());
						break;
					case SimConstants.FORK_JOIN_RESPONSE_TIME:
						netNodes[nodePosition].getSection(NodeSection.INPUT).analyzeFJ(SimConstants.FORK_JOIN_RESPONSE_TIME, jClass, ms.getMeasure());
						break;
					case SimConstants.FIRING_THROUGHPUT:
						netNodes[nodePosition].getSection(NodeSection.SERVICE).analyzeTransition(SimConstants.FIRING_THROUGHPUT, ms.getjClass(), ms.getMeasure());
						break;
					case SimConstants.CACHE_HIT_RATE:
						netNodes[nodePosition].getSection(NodeSection.SERVICE).analyze(SimConstants.CACHE_HIT_RATE, jClass, ms.getMeasure());
						break;
					case SimConstants.NUMBER_OF_ACTIVE_SERVERS:
						netNodes[nodePosition].getSection(NodeSection.SERVICE).analyze(SimConstants.NUMBER_OF_ACTIVE_SERVERS, jClass, ms.getMeasure());
						break;
					}
				}
				// Global measures (new by Bertoli Marco)
				else {
					// If referenceNode not specified, this measure is targeted to the global metric in 'GlobalJobInfoList'.
					switch (ms.getMeasureType()) {
					case SimConstants.SYSTEM_NUMBER_OF_JOBS:
						network.getJobInfoList().analyzeJobNumber(jClass, ms.getMeasure());
						break;
					case SimConstants.SYSTEM_RESPONSE_TIME:
						network.getJobInfoList().analyzeResponseTime(jClass, ms.getMeasure());
						break;
					case SimConstants.SYSTEM_THROUGHPUT:
						network.getJobInfoList().analyzeThroughput(jClass, ms.getMeasure());
						break;
					case SimConstants.SYSTEM_DROP_RATE:
						network.getJobInfoList().analyzeDropRate(jClass, ms.getMeasure());
						break;
					case SimConstants.SYSTEM_BALKING_RATE:
						network.getJobInfoList().analyzeBalkingRate(jClass, ms.getMeasure());
						break;
					case SimConstants.SYSTEM_RENEGING_RATE:
						network.getJobInfoList().analyzeRenegingRate(jClass, ms.getMeasure());
						break;
					case SimConstants.SYSTEM_RETRIAL_ATTEMPTS_RATE:
						network.getJobInfoList().analyzeRetrialAttemptsRate(jClass, ms.getMeasure());
						break;
					case SimConstants.SYSTEM_POWER:
						network.getJobInfoList().analyzeSystemPower(jClass, ms.getMeasure());
						break;
					case SimConstants.SYSTEM_TARDINESS:
						network.getJobInfoList().analyzeTardiness(jClass, ms.getMeasure());
						break;
					case SimConstants.SYSTEM_EARLINESS:
						network.getJobInfoList().analyzeEarliness(jClass, ms.getMeasure());
						break;
					case SimConstants.SYSTEM_LATENESS:
						network.getJobInfoList().analyzeLateness(jClass, ms.getMeasure());
						break;
					}
				}
			}

			//Preload
			if (preloadEnabled) {
				//preload enabled: uses the info loaded by SimLoader
				for (int s = 0; s < preload_stationNames.length; s++) {
					//preload, in the specified station, the specified populations
					preload_station(preload_stationNames[s], preload_initialPopulations[s]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		initialized = true;

		logger.debug("Simulation has been initialized");
	}

	/**
	 * Runs this simulation. If it has not been initialized, then throws an Exception.
	 * Controls the timer for max simulation time (if it has been defined).
	 * @throws NetException
	 * @throws InterruptedException
	 */
	public void run() throws NetException, InterruptedException {
		if (initialized) {
			//runs the simulation
			netSystem.start();
			finished = true;

			//simulation has finished
			//results are put into a xml file
			XMLSimulationOutput output = new XMLSimulationOutput(this);
			outputFile = output.writeAllMeasures();

			netSystem.terminate();
		} else {
			throw new NetException("Simulation has not been initialized");
		}
	}

	/**
	 * Aborts simulation.
	 */
	public void abort() {
		logger.info("Aborting simulation...");
		netSystem.terminate();
	}

	/**
	 * Returns output file handler
	 * @return output file handler or null if a problem occurred
	 */
	public File getOutputFile() {
		return outputFile;
	}

	//------------------end INITIALIZATION AND RUN------------------------------//

	//-------------------------SUPPORT METHODS---------------------------------//

	//check if exists a node with the given name
	private boolean isNode(String name) {
		for (int i = 0; i < nodes.size(); i++) {
			String netNode = (nodes.get(i)).getNode().getName();
			if (netNode.equals(name)) {
				return true;
			}
		}
		return false;
	}

	//finds the position of the classes from the giving names
	private int[] findClassPosition(String[] names) {
		int[] classPos = new int[names.length];
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			for (int j = 0; j < classes.length; j++) {
				JobClass aClass = classes[j];
				if (name.equals(aClass.getName())) {
					classPos[i] = j;
				}
			}
		}
		return classPos;
	}

	//finds the node
	private int findNodePosition(String name) throws LoadException {
		for (int i = 0; i < nodes.size(); i++) {
			if ((nodes.get(i)).getNode().getName().equals(name)) {
				return i;
			}
		}
		throw new LoadException("node not found");
	}

	/**
	 * Preload jobs in a queue
	 */
	public void preload_station(String stationName, int[] jobs) {
		//find the node
		NetNode node = netSystem.getNode(stationName);

		if (node != null) {
			try {
				//retrieves the input section of the node
				NodeSection section = node.getSection(NodeSection.INPUT);
				if (section instanceof Queue) {
					//preload jobs for each class
					((Queue) section).preloadJobs(jobs);
				} else if (section instanceof Storage) {
					//preload jobs for each class
					((Storage) section).preloadJobs(jobs);
				}
			} catch (NetException e) {
				return;
			}
		}
	}

	//-------------------------end SUPPORT METHODS---------------------------------//

	//------------------- GETTER AND SETTER ----------------------------------//

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getXmlModelDefPath() {
		return xmlModelDefPath;
	}

	public void setXmlModelDefPath(String xmlModelDefPath) {
		this.xmlModelDefPath = xmlModelDefPath;
	}

	public String getXmlSimModelDefPath() {
		return xmlSimModelDefPath;
	}

	public void setXmlSimModelDefPath(String xmlSimModelDefPath) {
		this.xmlSimModelDefPath = xmlSimModelDefPath;
	}

	public void setRandomEngineSeed(long seed) {
		netSystem.getEngine().setNewSeed(seed);
	}

	/**
	 * Returns true if the simulation has finished
	 * @return true if the simulation has finished
	 */
	public boolean hasFinished() {
		return finished;
	}

	public QueueNetwork getNetwork() {
		return network;
	}

	/**
	 * Returns an array with all the blocking regions defined inside the model
	 * @return null if no region has been defined
	 */
	public BlockingRegion[] getAllRegions() {
		if (regions.size() == 0) {
			return null;
		}
		BlockingRegion[] regions_vector = new BlockingRegion[regions.size()];

		for (int br = 0; br < regions.size(); br++) {
			regions_vector[br] = regions.get(br);
		}
		return regions_vector;
	}

	/**
	 * Returns the array of classes
	 * @return the array of classes
	 */
	public JobClass[] getClasses() {
		return classes;
	}

	public void setSimParameters(SimParameters simParameters) {
		this.simParameters = simParameters;
	}

	public SimParameters getSimParameters() {
		return simParameters;
	}

	public void setPreloadEnabled(boolean preloadEnabled) {
		this.preloadEnabled = preloadEnabled;
	}

	public void setPreload_stationNames(String[] preload_stationNames) {
		this.preload_stationNames = preload_stationNames;
	}

	public void setPreload_initialPopulations(int[][] preload_initialPopulations) {
		this.preload_initialPopulations = preload_initialPopulations;
	}

	public void addDistrNetNode(NetNode netNode) {
		distrNetNodes.add(netNode);
	}

	public boolean isTerminalSimulation() {
		return isTerminalSimulation;
	}

	public void setTerminalSimulation(boolean isTerminalSimulation) {
		this.isTerminalSimulation = isTerminalSimulation;
	}

	public int getParametricStep() {
		return parametricStep;
	}

	public void setParametricStep(int parametricStep) {
		this.parametricStep = parametricStep;
	}

	public SimSystem getSimSystem() {
		return netSystem.getSimSystem();
	}

	public NetSystem getNetSystem() {
		return netSystem;
	}

	//------------------- end GETTER AND SETTER ----------------------------------//

	//-------------------------SUPPORT CLASSES-------------------------------//

	/**
	 * This is a connection between two nodes of the simulation.
	 */
	class Connection {

		private String start;
		private String end;

		/**
		 * Creates a node connection between two stations of the model
		 * @param startNode start node
		 * @param endNode end node
		 */
		public Connection(String startNode, String endNode) {
			start = startNode;
			end = endNode;
		}

		public String getEnd() {
			return end;
		}

		public String getStart() {
			return start;
		}

	}

	/**
	 * Creates a measure to be estimated on the simulation model.
	 */
	class SimMeasure {

		private String jClass;
		private Measure measure;
		private String nodeName;
		//see constants in class Simulation
		private int measureType;

		/**
		 * Creates a measure to be computed during simulation
		 * @param measureType measure type (see constants in class Simulation)
		 * @param nodeName node to which this measure refers
		 * @param measure Measure object which will control this measure
		 * @param jClass class to which this measure refers
		 *
		 * @throws LoadException
		 */
		public SimMeasure(int measureType, String nodeName, Measure measure, String jClass) throws LoadException {

			this.jClass = jClass;
			this.measureType = measureType;
			this.measure = measure;
			this.measure.measureTarget(nodeName, jClass, measureType);
			this.nodeName = nodeName;
			if (this.nodeName == null) {
				throw new LoadException("Trying to add a measure to a not-existent node.");
			}
		}

		public Measure getMeasure() {
			return measure;
		}

		public int getMeasureType() {
			return measureType;
		}

		public String getNodeName() {
			return nodeName;
		}

		public String getjClass() {
			return jClass;
		}

	}

	/**
	 * Represents a service center in the simulation
	 */
	class SimNode {

		private NetNode node;
		//node sections
		private InputSection input;
		private ServiceSection service;
		private OutputSection output;
		//true if the node has been created with all its sections,
		//false otherwise
		private boolean nodeInit;

		//reference nodes are nodes used to compute job throughput or to create jobs
		//(and therefore must receive the START event of simulation)
		private boolean reference = false;

		/**
		 * Creates a SimNode object
		 * @param name node name
		 * @param inSec input section of the node
		 * @param serSec service section of the node
		 * @param outSec output section of the node
		 */
		public SimNode(String name, InputSection inSec, ServiceSection serSec, OutputSection outSec) {
			this.node = new NetNode(name);
			SimSystem simSystem = netSystem.getSimSystem();
			simSystem.add(this.node);
			node.setSimSystem(simSystem);

			this.input = inSec;
			this.service = serSec;
			this.output = outSec;

			nodeInit = true;

			if (simParameters != null) {
				this.node.setSimParameters(simParameters);
			}

			if ((inSec instanceof RandomSource) || (inSec instanceof Terminal)) {
				reference = true;
			}
		}

		/**
		 * @deprecated
		 * @param nodeName
		 */
		@Deprecated
		public SimNode(String nodeName) {
			this.node = new NetNode(nodeName);
			input = null;
			service = null;
			output = null;
			nodeInit = false;
		}

		public InputSection getInput() {
			return input;
		}

		public NetNode getNode() {
			return node;
		}

		public OutputSection getOutput() {
			return output;
		}

		public ServiceSection getService() {
			return service;
		}

		public void initialize() {
			if (!this.nodeInit) {
			}
		}

		public boolean isReference() {
			return reference;
		}

	}

	public RandomEngine getEngine() {
		return netSystem.getEngine();
	}

}
