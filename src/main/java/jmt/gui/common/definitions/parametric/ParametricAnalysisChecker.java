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

package jmt.gui.common.definitions.parametric;

import java.util.Vector;
import java.util.Map;

import jmt.gui.common.definitions.ClassDefinition;
import jmt.gui.common.definitions.SimulationDefinition;
import jmt.gui.common.definitions.StationDefinition;
import jmt.gui.common.distributions.Distribution;
import jmt.gui.common.routingStrategies.ProbabilityRouting;

/**
 * <p>Title: ParametricAnalysisChecker </p>
 * <p>Description: the main task of this class is to check which parametric analysis simulation
 *           are available. It offers also some other methods used to get available stations and
 *           classes for some types of parametric analysis.</p>
 *
 * @author Francesco D'Aquino
 *         Date: 1-feb-2006
 *         Time: 11.00.12
 */
public class ParametricAnalysisChecker {
	private ClassDefinition cd;
	private StationDefinition sd;
	private SimulationDefinition simd;

	/**
	 *
	 * @param cd class definition
	 * @param sd simulation definition
	 * @param simd simulation definition
	 */
	public ParametricAnalysisChecker(ClassDefinition cd, StationDefinition sd, SimulationDefinition simd) {
		this.cd = cd;
		this.sd = sd;
		this.simd = simd;
	}

	/**
	 * Checks if at least one parametric simulation is available.
	 * @return true if at least one parametric simulation is available.
	 */
	public boolean canBeEnabled() {
		return !cd.getClassKeys().isEmpty() && !sd.getStationKeys().isEmpty()
				&& getRunnableParametricAnalysis().length > 0;
	}

	/**
	 * Gets an array of String containing the description of available parametric simulations
	 * @return the array
	 */
	public String[] getRunnableParametricAnalysis() {
		String[] runnable;
		Vector<String> runnableVector = new Vector<String>(0, 1);
		//check if "Number of customer" parametric analysis is runnable
		Vector<Object> temp = cd.getClosedClassKeys();
		if (!temp.isEmpty()) {
			runnableVector.add(ParametricAnalysis.PA_TYPE_NUMBER_OF_CUSTOMERS);
		}
		//check if "Population mix" parametric analysis is runnable
		if (temp.size() == 2) {
			runnableVector.add(ParametricAnalysis.PA_TYPE_POPULATION_MIX);
		}
		//check if "Service time" parametric analysis is runnable
		temp = checkForServiceTimesParametricAnalysisAvailableStations();
		if (!temp.isEmpty()) {
			runnableVector.add(ParametricAnalysis.PA_TYPE_SERVICE_TIMES);
		}
		//check if "Arrival rate" parametric analysis is runnable
		temp = checkForArrivalRatesParametricSimulationAvailableClasses();
		if (!temp.isEmpty()) {
			runnableVector.add(ParametricAnalysis.PA_TYPE_ARRIVAL_RATE);
		}
		//check if "Number of server" parametric analysis is runnable
		temp = checkForNumberOfServersParametricAnalysisAvailableStations();
		if (!temp.isEmpty()) {
			runnableVector.add(ParametricAnalysis.PA_TYPE_NUMBER_OF_SERVERS);
		}
		//check if "Total station capacity" parametric analysis is runnable
		temp = checkForTotalStationCapacityParametricAnalysisAvailableStations();
		if (!temp.isEmpty()) {
			runnableVector.add(ParametricAnalysis.PA_TYPE_TOTAL_CAPACITY);
		}
		//check if "Routing probabilities" parametric analysis is runnable
		temp = checkForRoutingProbabilitiesParametricAnalysisAvailableSourceStations();
		if (!temp.isEmpty()) {
			runnableVector.add(ParametricAnalysis.PA_TYPE_ROUTING_PROBABILITY);
		}
		//check if "Seed" parametric analysis is available
		if (!cd.getClassKeys().isEmpty() && !sd.getStationKeys().isEmpty()) {
			runnableVector.add(ParametricAnalysis.PA_TYPE_SEED);
		}
		//initialize runnable array
		runnable = new String[runnableVector.size()];
		for (int i = 0; i < runnable.length; i++) {
			runnable[i] = runnableVector.get(i);
		}
		return runnable;
	}

	/**
	 *  This method has a meaning only if used inside a service times parametric analysis. It
	 *  can be used to get the keys of stations available to perform that kind of parametric
	 *  analysis.
	 * @return the Vector containing the keys of available stations
	 */
	public Vector<Object> checkForServiceTimesParametricAnalysisAvailableStations() {
		Vector<Object> availableStations = new Vector<Object>(0, 1);
		// get all stations
		Vector<Object> stations = sd.getStationKeys();
		// get all classes
		Vector<Object> classes = cd.getClassKeys();

		for (int i = 0; i < stations.size(); i++) {
			Object thisStation = stations.get(i);
			// used to see whether current station is suitable for PA
			boolean stationOK = false;
			for (int j = 0; j < classes.size(); j++) {
				Object thisClass = classes.get(j);
				Object temp = sd.getServiceTimeDistribution(thisStation, thisClass);
				if (temp instanceof Distribution) {
					Distribution distr = (Distribution) temp;
					if (distr.hasMean()) {
						stationOK = true;
						break;
					}
				}
			}
			if (stationOK) {
				availableStations.add(thisStation);
			}
		}
		return availableStations;
	}

	/**
	 * This method has a meaning only if used inside a service times parametric analysis.
	 * It can be used to get the keys of classes available to perform that kind of parametric
	 * analysis.
	 * @param stationKey the key of the station whose service times will be varied.
	 * @return a Vector containing the keys of available classes
	 */
	public Vector<Object> checkForServiceTimesParametricSimulationAvailableClasses(Object stationKey) {
		Vector<Object> valid = new Vector<Object>(0, 1);
		Vector<Object> classes = cd.getClassKeys();
		for (int j = 0; j < classes.size(); j++) {
			Object thisClass = classes.get(j);
			Object temp = sd.getServiceTimeDistribution(stationKey, thisClass);
			if (temp instanceof Distribution) {
				Distribution distr = (Distribution) temp;
				if (distr.hasMean()) {
					valid.add(thisClass);
				}
			}
		}
		return valid;
	}

	/**
	 * This method has a meaning only if used inside a routing probabilities parametric analysis.
	 * It can be used to get the keys of stations available to perform that kind of parametric
	 * analysis.
	 * @param stationKey the key of the station whose routing probabilities will be varied.
	 * @return a Vector containing the keys of available stations
	 */
	public Vector<Object> checkForRoutingProbabilitiesParametricAnalysisAvailableSourceStations() {
		// get all stations
		Vector<Object> stations = sd.getStationKeys();
		// get all classes
		Vector<Object> classes = cd.getClassKeys();
		Vector<Object> availableStations = new Vector<Object>(0, 1);

		for(int i=0; i<stations.size(); i++) {
			Object thisStation = stations.get(i);
			// used to see whether current station is suitable for PA
			boolean stationOK = false;
			for (int j = 0; j < classes.size(); j++) {
				Object thisClass = classes.get(j);
				if (sd.getRoutingStrategy(thisStation, thisClass) instanceof ProbabilityRouting) {
					ProbabilityRouting pr = (ProbabilityRouting) sd.getRoutingStrategy(thisStation, thisClass);
					Map<Object, Double> values = pr.getValues();
					if (!values.isEmpty()) {
						stationOK = true;
						break;
					}
				}
			}
			if (stationOK) {
				availableStations.add(thisStation);
			}
		}
		return availableStations;
	}

	/**
	 * This method has a meaning only if used inside a routing probabilities parametric analysis.
	 * It can be used to get the keys of classes available to perform that kind of parametric
	 * analysis.
	 * @param stationKey the key of the station whose routing probabilities will be varied.
	 * @return a Vector containing the keys of available classes
	 */
	public Vector<Object> checkForRoutingProbabilitiesParametricSimulationAvailableClasses(Object stationKey) {
		Vector<Object> valid = new Vector<Object>(0, 1);
		Vector<Object> classes = cd.getClassKeys();
		for (int j = 0; j < classes.size(); j++) {
			Object thisClass = classes.get(j);
			if (sd.getRoutingStrategy(stationKey, thisClass) instanceof ProbabilityRouting) {
				ProbabilityRouting pr = (ProbabilityRouting) sd.getRoutingStrategy(stationKey, thisClass);
				Map<Object, Double> values = pr.getValues();
				if (!values.isEmpty()) {
					valid.add(thisClass);
				}
			}
		}
		return valid;
	}

	/**
	 * This method has a meaning only if used inside an arrival rate parametric analysis.
	 * It can be used to get the keys of classes available to perform that kind of parametric
	 * analysis.
	 * @return a Vector containing the keys of available classes
	 */
	public Vector<Object> checkForArrivalRatesParametricSimulationAvailableClasses() {
		Vector<Object> valid = new Vector<Object>(0, 1);
		Vector<Object> classes = cd.getOpenClassKeys();
		for (int j = 0; j < classes.size(); j++) {
			Object thisClass = classes.get(j);
			Object temp = cd.getClassDistribution(thisClass);
			if (temp instanceof Distribution) {
				Distribution distr = (Distribution) temp;
				if (distr.hasMean()) {
					valid.add(thisClass);
				}
			}
		}
		return valid;
	}

	public Vector<Object> checkForNumberOfServersParametricAnalysisAvailableStations() {
		Vector<Object> availableStations = new Vector<Object>(0, 1);
		Vector<Object> stations = sd.getStationKeys();
		Vector<Object> classes = cd.getClassKeys();
		for (int i = 0; i < stations.size(); i++) {
			Object thisStation = stations.get(i);
			boolean stationOK = false;
			for (int j = 0; j < classes.size(); j++) {
				Object thisClass = classes.get(j);
				Integer temp = sd.getStationNumberOfServers(thisStation);
				if (temp != null && temp.intValue() > 0) {
					stationOK = true;
					break;
				}
			}
			if (stationOK) {
				availableStations.add(thisStation);
			}
		}
		return availableStations;
	}

	public Vector<Object> checkForTotalStationCapacityParametricAnalysisAvailableStations() {
		Vector<Object> availableStations = new Vector<Object>(0, 1);
		Vector<Object> stations = sd.getStationKeys();
		Vector<Object> classes = cd.getClassKeys();
		for (int i = 0; i < stations.size(); i++) {
			Object thisStation = stations.get(i);
			boolean stationOK = false;
			for (int j = 0; j < classes.size(); j++) {
				Object thisClass = classes.get(j);
				Integer temp = sd.getStationQueueCapacity(thisStation);
				if (temp != null) {
					stationOK = true;
					break;
				}
			}
			if (stationOK) {
				availableStations.add(thisStation);
			}
		}
		return availableStations;
	}
}
