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

package jmt.engine.NetStrategies;

import jmt.common.AutoCheck;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.JobInfoList;
import jmt.engine.QueueNet.NetNode;

/**
 * Use this class to implement a specific queue get strategy. A queue get
 * strategy is a rule which removes a job from a queue.
 * @author Francesco Radaelli
 */
public abstract class QueueGetStrategy implements AutoCheck {

	/**
	 * This method should be overridden to implement a specific job strategy.
	 * @param queue Job queue.
	 * @return Job selected and removed from the queue.
	 */
	public abstract Job get(JobInfoList queue);

	/**
	 * This method should be overridden to implement a specific job strategy.
	 * @param queue Job queue.
	 * @param ownerNode ownerNode
	 * @return Job selected and removed from the queue.
	 */
	public abstract Job get(JobInfoList queue, NetNode ownerNode);

	/**
	 * This method should be overridden to implement a specific job strategy.
	 * @param queue Job queue.
	 * @param jobClass Job class.
	 * @return Job selected and removed from the queue.
	 */
	public abstract Job get(JobInfoList queue, JobClass jobClass);

	public boolean check() {
		return true;
	}

	public abstract int GetJobClassIdOfNextJob(JobInfoList queue);
}