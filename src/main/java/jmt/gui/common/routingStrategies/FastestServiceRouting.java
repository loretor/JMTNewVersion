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
 * Date: 13-lug-2005
 * Time: 10.20.14
 * To change this template use Options | File Templates.
 */
public class FastestServiceRouting extends RoutingStrategy {

	public FastestServiceRouting() {
		description = "Jobs are routed to the station with the smallest service time, for this customer class.";
	}

	@Override
	public String getName() {
		return "Fastest Service";
	}

	@Override
	public FastestServiceRouting clone() {
		return new FastestServiceRouting();
	}

	@Override
	public String getClassPath() {
		return "jmt.engine.NetStrategies.RoutingStrategies.FastestServiceRoutingStrategy";
	}

	@Override
	public boolean isModelStateDependent() {
		return true;
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
