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

package jmt.jmva.gui.panels;

import jmt.gui.table.ExactTableModel;
import jmt.jmva.analytical.ExactConstants;
import jmt.jmva.analytical.solvers.SolverAlgorithm;
import jmt.jmva.gui.JMVAWizard;

public final class RespTimePanel extends SolutionPanel {

	private static final long serialVersionUID = 1L;
	private double[][][] resTimes;
	private double[][] stationAggs;

	/* EDITED by Abhimanyu Chugh */
	public RespTimePanel(JMVAWizard ew, SolverAlgorithm alg) {
		super(ew, alg);
		helpText = "<html>Response Times</html>";
		name = "Response Times";
	}
	/* END */

	/**
	 * gets status from data object
	 */
	@Override
	protected void sync() {
		super.sync();
		/* EDITED by Abhimanyu Chugh */
		resTimes = data.getResidTimes(algorithm);
		double [][] visits = data.getVisits();
		for (int i = 0; i < resTimes.length; i++) {
			for (int j = 0; j < resTimes[0].length; j++) {
				for (int k = 0; k < resTimes[0][0].length; k++) {
					if (visits[i][j] > 0.0) {
						resTimes[i][j][k] = resTimes[i][j][k] / visits[i][j];
					}
				}
			}
		}
		stationAggs = data.getPerStationRespT(algorithm);
		/* END */
	}

	@Override
	protected ExactTableModel getTableModel() {
		return new RTTableModel();
	}

	@Override
	protected String getDescriptionMessage() {
		return ExactConstants.DESCRIPTION_RESPONSETIMES;
	}

	/**
	 * the model backing the visit table.
	 * Rows represent stations, columns classes.
	 */
	private class RTTableModel extends ExactTableModel {

		private static final long serialVersionUID = 1L;

		RTTableModel() {
			prototype = new Double(1000);
			rowHeaderPrototype = "Station1000";
		}

		@Override
		public int getRowCount() {
			if (resTimes == null) {
				return 0;
			}
			return stations + 1;
		}

		@Override
		public int getColumnCount() {
			if (resTimes == null) {
				return 0;
			}
			return classes + 1;
		}

		@Override
		protected Object getRowName(int rowIndex) {
			if (rowIndex == 0) {
				return "<html><i>Aggregate</i></html>";
			}
			return stationNames[rowIndex - 1];
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return "<html><i>Aggregate</i></html>";
			}
			return classNames[columnIndex - 1];
		}

		@Override
		protected Object getValueAtImpl(int rowIndex, int columnIndex) {
			double d = -1.0;

			if (rowIndex == 0 && columnIndex == 0  && !data.isMixed()) {
				//d = globalAgg[iteration];
			} else if (rowIndex == 0 && columnIndex > 0  && !data.isMixed()) {
				//d = classAggs[columnIndex - 1][iteration];
			} else if (rowIndex > 0 && columnIndex == 0  && !data.isMixed()) {
				d = stationAggs[rowIndex - 1][iteration];
			} else if (rowIndex > 0 && columnIndex > 0) {
				d = resTimes[rowIndex - 1][columnIndex - 1][iteration];
			}

			if (d < 0.0) {
				return new String("--");
			} else {
				return new Double(d);
			}
		}

	}

}
