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

package jmt.engine.NetStrategies.PSStrategies;

import jmt.engine.NetStrategies.PSStrategy;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.JobClassList;
import jmt.engine.QueueNet.JobInfoList;

import java.util.Arrays;

/**
 * This class implements a specific PS strategy: Egalitarian Processor-
 * Sharing. All present jobs regardless of their classes receive the same
 * fraction of the service capacity.
 * @author Lulai Zhu
 */
public class EPSStrategy extends PSStrategy {

	public double slice(JobInfoList list, JobClassList classes, double[] weights, boolean[] saturated, JobClass jobClass) {
		Boolean[] compatibilities = new Boolean[classes.size()];
		Arrays.fill(compatibilities, true);
		double[] splits = new double[classes.size()];
		Arrays.fill(splits, 1);
		return slice(list, classes, weights, saturated, jobClass, compatibilities, splits);	}

	public double slice(JobInfoList list, JobClassList classes, double[] weights, boolean[] saturated, JobClass jobClass, Boolean[] compatibilities, double[] splits) {
		if (list.size() <= 0 || !compatibilities[jobClass.getId()]) {
			return 0.0;
		}
		double compatibleJobs = 0;
		for (int i = 0; i < classes.size(); i++) {
			if (compatibilities[i]) {
				compatibleJobs += list.size(classes.get(i)) * splits[i];
			}
		}
		return 1 / compatibleJobs;
	}

}
