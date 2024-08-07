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
import jmt.engine.QueueNet.*;

/**
 * Use this class to implement a specific routing strategy. A routing
 * strategy is a rule which selects an output node from a node list.
 * @author Francesco Radaelli
 * @author Bertoli Marco 13-11-2005 (Added job class)
 * @author Das Ashanka 11-2011 (Added getOutNode with NodeSection).
 */
public abstract class RoutingStrategy implements AutoCheck {
	 
	/**
	 * This method should be overridden to implement a specific strategy.
	 * @param ownerNode Owner node of the output section.
	 * @param jobClass Class of current job to be routed.
	 * @return Selected node.
	 */
	public abstract NetNode getOutNode(NetNode ownerNode, JobClass jobClass);

	/**
	 * @author Ashanka
	 * Made it normal function instead of abstract as I do not
	 * want other Strategies to forcefully override this method.
	 * Currently only LoadDependentRoutingStrategy overrides it.
	 */
	public NetNode getOutNode(NodeSection section, JobClass jobClass) {
		return getOutNode(section.getOwnerNode(), jobClass);
	}

	/**
	 * @author Ashanka
	 * Made it normal function instead of abstract as I do not
	 * want other Strategies to forcefully override this method.
	 * Currently only LoadDependentRoutingStrategy overrides it.
	 */
	public NetNode getOutNode(NodeSection section, Job job) {
		return getOutNode(section.getOwnerNode(), job.getJobClass());
	}

	public boolean check() {
		return true;
	}

}
