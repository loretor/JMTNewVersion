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

package jmt.gui.common.routingStrategies;

import java.util.HashMap;
import java.util.Map;

public class WeightedRoundRobinRouting extends RoutingStrategy {

	private Map<Object, Integer> weights = new HashMap<>();

	public WeightedRoundRobinRouting() {
		description = "Jobs are routed to stations connected to the current one according to a cyclic algorithm. "
				+ "On each cycle, only stations with weights higher than or equal to the current weight are considered. "
				+ "The weight is decremented, starting from the max weight for the first cycle.";
	}

	@Override
	public String getName() {
		return "Weighted Round Robin";
	}

	@Override
	public RoutingStrategy clone() {
		WeightedRoundRobinRouting strategy = new WeightedRoundRobinRouting();
		strategy.setWeights(weights);
		return strategy;
	}

	@Override
	public String getClassPath() {
		return "jmt.engine.NetStrategies.RoutingStrategies" +
				".WeightedRoundRobinStrategy";
	}

	@Override
	public boolean isModelStateDependent() {
		return true;
	}

	@Override
	public void addStation(Object stationKey) {
		weights.put(stationKey, 1);
	}

	@Override
	public void removeStation(Object stationKey) {
		weights.remove(stationKey);
	}

	public void setWeights(Map<Object, Integer> weights) {
		this.weights = weights;
	}

	public Map<Object, Integer> getWeights() {
		return weights;
	}

}
