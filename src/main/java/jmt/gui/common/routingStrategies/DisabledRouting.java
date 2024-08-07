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
 * User: giuliano casale
 * Date: 19-apr-2019
 * Time: 11.52.01
 * To change this template use Options | File Templates.
 */
public class DisabledRouting extends RoutingStrategy {

	public DisabledRouting() {
		description = "If the user knows that jobs in this class cannot be routed here, "
				+ "this strategy can be used to indicate that routing is left unspecified.";
	}

	@Override
	public String getName() {
		return "Disabled";
	}

	@Override
	public DisabledRouting clone() {
		return new DisabledRouting();
	}

	@Override
	public String getClassPath() {
		return "jmt.engine.NetStrategies.RoutingStrategies.DisabledRoutingStrategy";
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
