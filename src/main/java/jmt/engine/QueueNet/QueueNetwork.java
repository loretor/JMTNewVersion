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

package jmt.engine.QueueNet;

import java.util.LinkedList;
import java.util.ListIterator;

import jmt.engine.dataAnalysis.Measure;
import jmt.engine.dataAnalysis.TempMeasure;

/**
 * This class implements a queue network.
 * @author Francesco Radaelli, Bertoli Marco (Added support for global measures)
 */
public class QueueNetwork {

	/* Symbolic name for reference node in the network: it should be used
	 * within broadcast communication.
	 */
	public static final int REFERENCE_NODE = 0x0001;

	/* Symbolic name for normal node in the network: it should be used
	 * within broadcast communication.
	 */
	public static final int NODE = 0x0002;

	/** Behaviour ID: Aborts the simulation when a measure has been obtained.*/
	public static final int BEHAVIOUR_ABORT = 0x001;
	/** Behaviour ID: Stops the simulation when a measure has been obtained.*/
	public static final int BEHAVIOUR_STOP = 0x002;
	/** Behaviour ID: Continues the simulation when a measure has been obtained.*/
	public static final int BEHAVIOUR_CONTINUE = 0x003;
	/** Behaviour ID: Continues and waits other measures when a measure has been obtained.*/
	public static final int BEHAVIOUR_OBTAIN_ALL_MEASURES_THEN_ABORT = 0x004;
	/** Behaviour ID: Continues and waits other measures when a measure has been obtained.*/
	public static final int BEHAVIOUR_OBTAIN_ALL_MEASURES_THEN_STOP = 0x005;

	/** State ID: Initial state, the network is ready to be started.*/
	public static final int STATE_READY = 0x0001;
	/** State ID: The network is running.*/
	public static final int STATE_RUNNING = 0x0002;
	/** State ID: The network has been stopped.*/
	public static final int STATE_STOPPED = 0x0003;
	/** State ID: The network has been aborted.*/
	public static final int STATE_ABORTED = 0x0004;
	/** State ID: The network is in final state.*/
	public static final int STATE_FINAL = 0x0005;

	/** Measure ID: queue length */
	public static final int QUEUE_LENGTH = 0x001;
	/** Measure ID: residence time */
	public static final int RESIDENCE_TIME = 0x002;
	/** Measure ID: residence time */
	public static final int SERVICE_TIME = 0x003;

	private NodeList referenceNodes;

	private NodeList nodes;

	private LinkedList<Measure> measures;

	private JobClassList jobClasses;

	private String name;

	private int behaviour;

	private int state;

	private GlobalJobInfoList jobInfoList;

	private boolean isTerminalSimulation;

	private int parametricStep;

	private NetSystem netSystem;

	/** Creates a new instance of QueueNetwork. */
	public QueueNetwork(String name) {
		nodes = new NodeList();
		referenceNodes = new NodeList();
		jobClasses = new JobClassList();
		this.name = name;
		measures = new LinkedList<Measure>();
		behaviour = BEHAVIOUR_OBTAIN_ALL_MEASURES_THEN_STOP;
		state = STATE_READY;
		isTerminalSimulation = false;
		parametricStep = -1;
	}

	/** Adds a generic node to the network. If the node has no inputs the node
	 * is set as reference node.
	 * @param node node to be added.
	 */
	public void addNode(NetNode node) {
		nodes.add(node);
		if (node.getInputNodes().size() == 0) {
			//if no input nodes are present, it is presumed that this node
			//must be a source of jobs
			referenceNodes.add(node);
		}
		node.setNetwork(this);
	}

	/** Adds a node to the network and forces it to be a reference node.
	 * @param node node to be added.
	 */
	public void addReferenceNode(NetNode node) {
		nodes.add(node);
		referenceNodes.add(node);
		node.setNetwork(this);
	}

	/** Adds a new job class to the network.
	 *  @param jobClass Job class to be added.
	 */
	public void addJobClass(JobClass jobClass) {
		jobClasses.add(jobClass);
		jobClass.setId(jobClasses.indexOf(jobClass));
	}

	/**
	 * Gets the list of the queue network nodes.
	 * @return Queue network nodes.
	 */
	public NodeList getNodes() {
		return nodes;
	}

	/**
	 * Gets the NetNode with the specified name.
	 *
	 * @param name the name of the node
	 * @return the specified node
	 */
	public NetNode getNode(String name) {
		return nodes.get(name);
	}

	/**
	 * Gets the JobClass with the specified name.
	 *
	 * @param name the name of the JobClass
	 * @return the job class. Null if it does not exist.
	 */
	public JobClass getJobClass(String name) {
		return jobClasses.get(name);
	}

	/** Gets the list of the queue network reference nodes.
	 * @return Queue network reference nodes.
	 */
	public NodeList getReferenceNodes() {
		return referenceNodes;
	}

	/** Gets the list of the queue network job classes.
	 * @return Queue network job classes.
	 */
	public JobClassList getJobClasses() {
		return jobClasses;
	}

	/** Gets network name
	 * @return Network name.
	 */
	public String getName() {
		return name;
	}

	/** Adds a new measure to the network.
	 * @param measure Reference to the measure to be added.
	 */
	public void addMeasure(Measure measure) {
		// If GlobalJobInfoList is not set, creates it
		// (at this point all classes should be declared)
		if (jobInfoList == null) {
			jobInfoList = new GlobalJobInfoList(jobClasses.size());
			jobInfoList.setNetSystem(netSystem);
		}

		//sets the reference to network
		measure.setNetwork(this);
		measures.add(measure);
	}

	/**
	 * Returns Global jobInfoList associated with this queue network. This is used
	 * to calculate global measures
	 * @return Global JobInfoList of this network
	 */
	public GlobalJobInfoList getJobInfoList() {
		return jobInfoList;
	}

	/**
	 * Gets the measures
	 */
	public LinkedList<Measure> getMeasures() {
		return measures;
	}

	/**
	 * Gets the network behaviour (see behaviour constants).
	 *
	 */
	public int getBehaviour() {
		return behaviour;
	}

	/**
	 * Sets the network behaviour (see behaviour constants).
	 */
	public void setBehaviuor(int behaviour) {
		this.behaviour = behaviour;
	}

	/**
	 * Sets the network state (see state constants).
	 */
	void setState(int state) {
		this.state = state;
	}

	/**
	 * Gets the network state (see state constants).
	 */
	public int getState() {
		if (state == STATE_FINAL || state == STATE_READY) {
			return state;
		}
		boolean flag = false;
		ListIterator<NetNode> nodeList = nodes.listIterator();
		NetNode node;
		while (nodeList.hasNext()) {
			node = nodeList.next();
			if (node.isRunning()) {
				flag = true;
			}
		}
		if (flag) {
			return state;
		} else {
			state = STATE_FINAL;
			return STATE_FINAL;
		}
	}

	/**
	 * Aborts all the measures linked to the queue network
	 */
	public void abortAllMeasures() {
		LinkedList<Measure> measures = this.getMeasures();
		TempMeasure tmp;
		for (Measure m : measures) {
			tmp = new TempMeasure(m);
			tmp.refreshMeasure();
			tmp.abort();
		}
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

	public NetSystem getNetSystem() {
		return netSystem;
	}

	public void setNetSystem(NetSystem netSystem) {
		this.netSystem = netSystem;
	}

}
