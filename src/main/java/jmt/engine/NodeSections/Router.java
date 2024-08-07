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

package jmt.engine.NodeSections;

import java.util.Arrays;

import javax.swing.JOptionPane;

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.RoutingStrategy;
import jmt.engine.QueueNet.BlockingRegion;
import jmt.engine.QueueNet.ForkJob;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.JobClassList;
import jmt.engine.QueueNet.NetEvent;
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NodeList;
import jmt.engine.QueueNet.NodeSection;
import jmt.engine.random.engine.RandomEngine;

/**
 * This class implements a router, which routes the jobs according to the specified
 * routing strategies (one for each job class).
 * <br><br>
 * The class has different constructors to create a generic router or a blocking
 * region border queue, that is the router of a node which is inside a blocking
 * region and which sends jobs also to nodes outside the region.
 * When a job leaves the blocking region, the region input station must receive
 * a message, in order to serve the blocked jobs.
 * <br>
 * However it is also possible to create a generic router and then to turn on/off the
 * "border router" behaviour using the <tt>borderRouterTurnON(..)</tt> and
 * <tt>borderRouterTurnOFF()</tt> methods.
 *
 * @author Francesco Radaelli, Stefano Omini
 * @author Bertoli Marco - Fixed lockup issues with closed class and sinks 13/11/2005
 * @author Bertoli Marco - Fixed bug with multiserver stations
 * 
 * Modified by Ashanka (Oct 2009) for FCR Bug fix: Events are created with job instead of null for EVENT_JOB_OUT_OF_REGION
 */
public class Router extends OutputSection {

	private RoutingStrategy[] routingStrategies;

	private NodeList outNodeList;
	private RandomEngine randomEngine;
	private boolean[] isClosedJobRoutingWarningShown;

	/*-------------------BLOCKING REGION PROPERTIES-------------------*/

	/*
	these properties are used if this router is the border router of
	a blocking region (i.e. it is connected also to nodes outside the
	blocking region)
	in fact, if the router sends a job outside the region, a message
	must be sent to the input station of that region, to decrease the
	number of jobs inside the region
	 */

	/** true if this router is the border router of a blocking region */
	private boolean borderRouterON;
	/** the blocking region this router belongs to */
	private BlockingRegion myRegion;
	/** the region input station of the blocking region */
	private NetNode regionInputStation;

	/*----------------------------------------------------------------*/

	/**
	 * Creates a new instance of Router.
	 * @param routingStrategies Routing strategies, one for each class.
	 */
	public Router(RoutingStrategy[] routingStrategies) {
		this.routingStrategies = routingStrategies;
		borderRouterTurnOFF();
	}

	/**
	 * Tells whether the "border router" behaviour has been turned on.
	 * @return true, if the "border router" behaviour is on; false otherwise.
	 */
	public boolean isBorderRouterON() {
		return borderRouterON;
	}

	/**
	 * Turns on the "border router" behaviour.
	 * @param region the blocking region to which the owner node of this router
	 * belongs.
	 */
	public void borderRouterTurnON(BlockingRegion region) {
		//sets blocking region properties
		borderRouterON = true;
		myRegion = region;
		regionInputStation = myRegion.getInputStation();
	}

	/**
	 * Turns off the "borderRouter" behaviour.
	 */
	public void borderRouterTurnOFF() {
		//sets blocking region properties
		borderRouterON = false;
		myRegion = null;
		regionInputStation = null;
	}

	@Override
	protected void nodeLinked(NetNode node) throws NetException {
		outNodeList = node.getOutputNodes();
		randomEngine = node.getNetSystem().getEngine();
		JobClassList jobClasses = getJobClasses();
		isClosedJobRoutingWarningShown = new boolean[jobClasses.size()];
		Arrays.fill(isClosedJobRoutingWarningShown, false);
	}

	@Override
	protected int process(NetMessage message) throws NetException {
		switch (message.getEvent()) {

		case NetEvent.EVENT_START:
			break;

		case NetEvent.EVENT_JOB:

			Job job = message.getJob();

			//EVENT_JOB
			//if the router is not busy, an output node is chosen using
			//the routing strategy and a message containing the job is sent to it.

			JobClass jobClass = job.getJobClass();

			//choose the outNode using the corresponding routing strategy
			NetNode outNode = routingStrategies[jobClass.getId()].getOutNode(this, job);
			// Bertoli Marco: sanity checks with closed classes and sinks were moved inside
			// routing strategies

			if (outNode == null) {
				if (job instanceof ForkJob) {
					outNode = outNodeList.get((int) Math.floor(randomEngine.raw() * outNodeList.size()));
				} else {
					showClosedJobRoutingWarning(jobClass);
					break;
				}
			}

			//send the job to all nodes identified by the strategy
			send(job, 0.0, outNode);

			//Border router behaviour (used in case of blocking region)
			if (borderRouterON) {
				//the owner node of this router is inside the region: if the outNode is outside
				//the region, it means that one job has left the blocking region so the region
				//input station (its blocking router) must receive a particular message
				if (!myRegion.belongsToRegion(outNode)) {
					myRegion.decreaseOccupation(jobClass);
					send(NetEvent.EVENT_JOB_OUT_OF_REGION, job, 0.0, NodeSection.INPUT, regionInputStation);
					//Since now for blocking regions the job dropping is handles manually at node 
					//level hence need to create events with Jobs ..Modified for FCR Bug Fix
				}
			}
			break;

		case NetEvent.EVENT_ACK:

			//EVENT_ACK
			//An ack is sent back to the service section.

			sendBackward(NetEvent.EVENT_ACK, message.getJob(), 0.0);
			break;

		case NetEvent.EVENT_STOP:
			break;

		default:
			return MSG_NOT_PROCESSED;
		}

		return MSG_PROCESSED;
	}

	/**
	 * Shows the closed job routing error.
	 */
	private void showClosedJobRoutingWarning(final JobClass jobClass) {
		if (!isClosedJobRoutingWarningShown[jobClass.getId()]) {
			if (getOwnerNode().getQueueNet().isTerminalSimulation()) {
				System.out.println("JSIMengine - Warning: " + getOwnerNode().getName()
						+ " could not route one or more " + jobClass.getName()
						+ " jobs forward as its output stations are all sinks or routing is disabled.");
			} else {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(800);
						} catch (InterruptedException e) {
						}
						JOptionPane.showMessageDialog(null, getOwnerNode().getName()
								+ " could not route one or more " + jobClass.getName()
								+ " jobs forward as its output stations are all sinks or routing is disabled.",
								"JSIMengine - Warning", JOptionPane.WARNING_MESSAGE);
					}
				}).start();
			}
			isClosedJobRoutingWarningShown[jobClass.getId()] = true;
		}
	}

}
