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

package jmt.gui.jsimgraph.JGraphMod;

import org.jgraph.graph.Port;

/**
 * Represents a Sink service center, it used to destroy jobs in open systems
 *
 * @author Federico Granata, Bertoli Marco
 * Date: 5-giu-2003
 * Time: 13.14.30
 */
public class SinkCell extends JmtCell {

	private static final long serialVersionUID = 1L;

	// Do not change this as it is accessed by reflection to forecast new cell dimensions (Bertoli Marco)
	public static final String ICON = "Sink";

	/**
	 * Creates a graph cell and initializes it with the specified user object.
	 *
	 * @param userObject an Object provided by the user that constitutes
	 *                   the cell's data
	 */
	public SinkCell(Object userObject) {
		super(ICON, userObject);
		type = SINK;
	}

	/**
	 * Creates the ports for this vertex
	 *
	 * @return array of ports
	 */
	@Override
	public Port[] createPorts() {
		Port[] ports = new Port[1];
		ports[0] = new InputPort(this);
		return ports;
	}

	/**
	 * Tells if this station generates or destroys jobs (useful for blocking region
	 * management)
	 *
	 * @return true if this station generates or destroy jobs, false otherwise
	 */
	@Override
	public boolean generateOrDestroyJobs() {
		return true;
	}

}
