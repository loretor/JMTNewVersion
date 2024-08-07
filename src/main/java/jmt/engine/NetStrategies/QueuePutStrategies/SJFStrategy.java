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

package jmt.engine.NetStrategies.QueuePutStrategies;

import java.util.Iterator;
import java.util.List;

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.QueuePutStrategy;
import jmt.engine.NetStrategies.ServiceStrategy;
import jmt.engine.NodeSections.Server;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobInfo;
import jmt.engine.QueueNet.JobInfoList;
import jmt.engine.QueueNet.NodeSection;

/**
 * This class implements a specific queue put strategy: all arriving jobs
 * are put to the queue in a Shortest-Job-First manner.
 * @author Lulai Zhu
 */
public class SJFStrategy extends QueuePutStrategy {

	public void put(Job job, JobInfoList queue, NodeSection nodeSection) throws NetException {
		Server server = (Server) nodeSection.getOwnerNode().getSection(NodeSection.SERVICE);
		ServiceStrategy[] strategies = (ServiceStrategy[]) server.getServiceStrategies();
		double serviceTime = strategies[job.getJobClass().getId()].wait(server, job.getJobClass());
		job.setServiceTime(serviceTime);

		List<JobInfo> list = queue.getInternalJobInfoList();
		if (list.size() == 0) {
			queue.addFirst(new JobInfo(job));
			return;
		}

		double currentServiceTime = 0.0;
		Iterator<JobInfo> it = list.iterator();
		int index = -1;
		while (it.hasNext()) {
			currentServiceTime = it.next().getJob().getServiceTime();
			index++;
			if (currentServiceTime > serviceTime) {
				queue.add(index, new JobInfo(job));
				return;
			}
		}
		queue.addLast(new JobInfo(job));
	}

}
