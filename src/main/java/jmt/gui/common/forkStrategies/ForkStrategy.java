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

package jmt.gui.common.forkStrategies;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

public abstract class ForkStrategy {

	protected static ForkStrategy[] all = null;

	protected String description;

	public abstract String getName();

	public String getDescription() {
		return description;
	}

	public abstract Map<Object, ? extends Object> getOutDetails();

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public abstract ForkStrategy clone();

	@Override
	public boolean equals(Object o) {
		if (o instanceof ForkStrategy) {
			return this.getName().equals(((ForkStrategy) o).getName());
		} else {
			return false;
		}
	}

	public abstract String getClassPath();

	public static ForkStrategy[] findAll() {
		if (all != null) {	
			return all;
		}
		ArrayList<ForkStrategy> strategies = new ArrayList<ForkStrategy>();
		Field[] fields = jmt.gui.common.CommonConstants.class.getFields();
		try {
			for (Field field : fields) {
				if (field.getName().startsWith("FORK_")) {
					strategies.add((ForkStrategy) field.get(null));
				}
			}
		} catch (IllegalAccessException ex) {
			System.err.println("A security manager has blocked reflection");
			ex.printStackTrace();
		}
		all = new ForkStrategy[strategies.size()];
		for (int i = 0; i < strategies.size(); i++) {
			all[i] = strategies.get(i);
		}
		return all;
	}

	public abstract boolean isModelStateDependent();

	public abstract void addStation(Object stationKey, Object classKey, Vector<Object> classKeys);

	public abstract void removeStation(Object stationKey);

}
