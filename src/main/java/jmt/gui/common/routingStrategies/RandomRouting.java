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

/**
 * Created by IntelliJ IDEA.
 * User: orsotronIII
 * Date: 7-lug-2005
 * Time: 16.07.58
 * To change this template use Options | File Templates.
 */
public class RandomRouting extends RoutingStrategy {

	public RandomRouting() {
		description = "Jobs are routed randomly to stations connected to the current one. "
				+ "All routes have the same probability to be selected.";
	}

	@Override
	public String getName() {
		return "Random";
	}

	@Override
	public RandomRouting clone() {
		return new RandomRouting();
	}

	@Override
	public String getClassPath() {
		return "jmt.engine.NetStrategies.RoutingStrategies.RandomStrategy";
	}

	@Override
	public boolean isModelStateDependent() {
		return false;
	}

	@Override
	public void addStation(Object stationKey) {
		return;
	}

	@Override
	public void removeStation(Object stationKey) {
		return;
	}

}
