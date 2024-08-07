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

import java.util.LinkedList;
import java.util.ListIterator;

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.ServiceStrategy;
import jmt.engine.QueueNet.GlobalJobInfoList;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.JobClassList;
import jmt.engine.QueueNet.JobInfo;
import jmt.engine.QueueNet.JobInfoList;
import jmt.engine.QueueNet.NetEvent;
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NetSystem;

/**
 * This class implements a job source (generator): a job is created every
 * <i>t</i> simulation time units using a statistical distribution according
 * to its class.
 * @author Francesco Radaelli, Federico Granata
 *
 */
public class RandomSource extends InputSection {

	private boolean coolStart;

	private LinkedList<Job> waitingJobs;

	private ServiceStrategy[] strategies;

	private JobClassList jobClasses;

	private JobInfoList nodeJobsList;

	private GlobalJobInfoList netJobsList;

	/**
	 * Creates a new instance of RandomSource.
	 * strategies[i] = null, if the i-th class is closed.
	 */
	public RandomSource(ServiceStrategy[] strategies) {
		this.strategies = strategies;
		waitingJobs = new LinkedList<Job>();
		coolStart = true;
	}

	//`nodeLinked(ownerNode)` is invoked by `NodeSection.setOwnerNode(ownerNode);` ===>  NetNode.inputSection.setOwnerNode(this);
	// just like the `initialize()` for this section.
	@Override
	protected void nodeLinked(NetNode node) throws NetException {
		// Sets netnode dependent properties

		//  ownerNode.getJobClasses() -> network.getJobClasses();
		jobClasses = getJobClasses();

		// get the ownerNode's jobsList
		nodeJobsList = node.getJobInfoList();
	}

	@Override
	protected int process(NetMessage message) throws NetException {
		Job job;
		int c;
		double delay;
		NetSystem netSystem;

		switch (message.getEvent()) {

		case NetEvent.EVENT_START: // msg is sent by NetSystem.start()

			//case EVENT_START:
			//the random source creates all the jobs requested by each class.
			//for each job created, it sends to itself a message whose delay is the time of
			//departure of the job, calculated using the strategy of the corresponding class

			netJobsList = getOwnerNode().getQueueNet().getJobInfoList();
			netSystem = getOwnerNode().getNetSystem();

			// traverse all the jobClass, and determine the jobClass type.
			// if close_class:
			// 		do not process, since close class has the fix number of jobs.
			// if open_class:
			//		if find the strategies corresponding to the class type:
			//			create a new job
			//			send the job to itself as `NetEvent.EVENT_JOB` with a certain time of `strategy.wait()`
			
			ListIterator<JobClass> it = jobClasses.listIterator();
			while (it.hasNext()) {
				JobClass jobClass = it.next();
				if (jobClass.getType() == JobClass.CLOSED_CLASS) {
					//closed class: no arrivals
					continue;
				}

				c = jobClass.getId();
				// Calculates the delay of departure (1/lambda)

				if (strategies[c] != null) {
					job = new Job(jobClass, netJobsList);
					// assign this netSystem to the job and call `nextjobNumber()` to add job counter.
					// set the systemEnteringTime to compute system response time
					job.initialize(netSystem);
					updateVisitPath(job);
					delay = strategies[c].wait(this, jobClass);
					sendMe(job, delay);
				}
			}
			break;

		case NetEvent.EVENT_JOB:

			//case EVENT_JOB
			//if coolStart=false adds the job to the list of waiting jobs.
			//
			//if coolStart=true (no waiting jobs)  the job is forwarded, an ack message
			//is sent to the source of the message and a new job is created (the random source
			//sends to itself a message, whose delay is the time of departure of the new job).

			// Gets the job from the message
			job = message.getJob();
			if (coolStart) {
				// Gets the class of the job
				c = job.getJobClass().getId(); // jobClass Id

				//no control is made on the number of jobs created
				//it is an open class

				// in RandomSource the job is created (--> SystemEnteringTime is initialized)
				// but then is delayed as long as the random interarrival time ("delay")
				//
				// to compute system response time, the job starting time must be
				// reset (otherwise it will correspond to the creation time and not to the
				// leaving time, which is "delay" seconds after)

				// Signals to section jobInfoList new added job
				jobsList.add(new JobInfo(job));
				// Signals to node jobInfoList new added job
				nodeJobsList.add(new JobInfo(job));
				// Signals to global jobInfoList new added job
				netJobsList.addJob(job);

				// send `NetEvent.EVENT_JOB` signal to `NodeSection.SERVICE` without and delay.
				sendForward(job, 0.0);

				// Create a new job and send it to me delayed
				job = new Job(job.getJobClass(), netJobsList);
				netSystem = getOwnerNode().getNetSystem();
				job.initialize(netSystem);
				updateVisitPath(job);

				delay = strategies[c].wait(this, job.getJobClass());
				sendMe(job, delay);

				// Sets coolStart to false, next job should wait ack
				coolStart = false;
			} else {
				//coolStart is false: there are waiting jobs. Add the received job.
				waitingJobs.add(job);
			}
			break;

		case NetEvent.EVENT_ACK:

			//case EVENT_ACK:
			//if there are waiting jobs, takes the first, set its bornTime and
			//forwards it to the service section.
			//then it creates a new job and sends to itself a message whose delay is the time of
			//departure of that job.
			//otherwise, if there are no waiting jobs, sets coolstart=true

			if (waitingJobs.size() != 0) {
				job = waitingJobs.removeFirst();
				c = job.getJobClass().getId();

				// in RandomSource the job is created (--> SystemEnteringTime is initialized)
				// but then is delayed as long as the random interarrival time ("delay")
				//
				// to compute system response time, the job starting time must be
				// reset (otherwise it will correspond to the creation time and not to the
				// leaving time, which is "delay" seconds after)

				// Signals to section jobInfoList new added job
				jobsList.add(new JobInfo(job));
				// Signals to node jobInfoList new added job
				nodeJobsList.add(new JobInfo(job));
				// Signals to global jobInfoList new added job
				netJobsList.addJob(job);

				sendForward(job, 0.0);

				// Create a new job and send it to me delayed
				job = new Job(jobClasses.get(c), netJobsList);
				netSystem = getOwnerNode().getNetSystem();
				job.initialize(netSystem);
				updateVisitPath(job);
				delay = strategies[c].wait(this, jobClasses.get(c));
				sendMe(job, delay);
			} else {
				coolStart = true;
			}
			break;

		case NetEvent.EVENT_STOP:
			break;

		default:
			return MSG_NOT_PROCESSED;
		}

		return MSG_PROCESSED;
	}

}
