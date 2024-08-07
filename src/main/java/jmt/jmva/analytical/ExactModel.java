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

package jmt.jmva.analytical;

import jmt.framework.data.ArrayUtils;
import jmt.jmva.analytical.exactModelUtils.ExactDocumentCreator;
import jmt.jmva.analytical.solvers.SolverAlgorithm;
import jmt.jmva.analytical.solvers.SolverMultiClosedAMVA;
import jmt.jmva.analytical.solvers.multiSolverAlgorithms.SolverMultiClosedMonteCarloLogistic;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

import static jmt.jmva.analytical.exactModelUtils.ExactDocumentConstants.*;

/**
 * An object grouping all the data describing a system.
 * <br><br>
 * WARNING:
 * ACCESS TO THIS OBJECT MUST BE IMPLEMENTED IN A TRANSACTION-LIKE SYNCHRONIZED WAY!
 *
 * @author alyf (Andrea Conti), Stefano Omini, Bertoli Marco
 * 
 * @author Ashanka
 * Cleaned code by removing unnecessary comments of old code.
 * @version Date: Sep-2009
 * 
 * @author Kourosh
 *  Add a new property ReferenceStation which is used to declare the reference station for each closed class. and save and load the files
 * @version Date: OCT-2013
 * @author Cerotti
 *  Fixed results retrieval according to reference station
 * @version Date: JAN-2015
 */

// TODO: This file needs breaking up, it's too big
// E.g. separate the functions for saving and loading into a separate file
// (maybe even 2 separate files, 1 for loading, 1 for saving)
// The WhatIf parts could be in a separate class and then composition used if the file is still too long

public class ExactModel implements ExactConstants  {

	//Set by default to the Exact MVA algorithm
	public SolverAlgorithm algorithmType = SolverAlgorithm.EXACT;
	//Set by default if user does not enter a tolerance value
	public double tolerance = SolverMultiClosedAMVA.DEFAULT_TOLERANCE;
	//Set by default if user does not enter a max samples value
	//private int maxSamples = SolverMultiClosedMonteCarlo.DEFAULT_MAX_SAMPLES;
	private int maxSamples = SolverMultiClosedMonteCarloLogistic.DEFAULT_MAX_SAMPLES;
	//Names of the algorithms to be compared
	private Set<SolverAlgorithm> whatifAlgorithms;
	private Map<SolverAlgorithm, Double> whatifAlgorithmsTolerance;
	private Map<SolverAlgorithm, Integer> whatifAlgorithmsMaxSamples;

	//true if the model is closed
	private boolean closed;
	//true if the model is open
	private boolean open;
	//true if the model contains load dependent stations
	private boolean ld;
	//true if visits are set (otherwise they will be all unitary)
	private boolean unitaryVisits;
	//true if the model contains priority stations
	private boolean priority;

	//true if the model has been modified
	private boolean changed;
	//true if results are available
	private boolean hasResults;
	//true if the results are valid (no modify has been made in the model after results computation)
	private boolean resultsOK;
	//description of the model
	private String description;

	/***********************STATIONS AND CLASSES******************************/

	//number of service centers
	private int stations;
	//number of classes
	private int classes;
	//total population (computed as the sum of all closed class populations)
	private int totalPop;

	//class data is class population for closed classes, class arrival rate for open classes
	//dim: classData[classes]
	private double[] classData;
	//station names
	//dim: stationNames[stations]
	private String[] stationNames;
	//station types
	//dim: stationTypes[stations]
	private int[] stationTypes;
	//number of servers of each station
	//dim: stationServers[stations]
	private int[] stationServers;
	//class names
	//dim: classNames[classes]
	private String[] classNames;
	//class types
	//dim: classTypes[classes]
	private int[] classTypes;
	//class priorities
	//dim: classPriorities[classes]
	private int[] classPriorities;
	//class refstations
	//private double[][] ReferenceStation;
	private int[] referenceStation;

	/***********************SERVICE PARAMETERS**************************/

	/**
	 * visits to the service centers
	 * dim: visits[stations][classes]
	 */
	private double[][] visits;
	/**
	 * service times of the service centers
	 * dim: serviceTimes[stations][classes][p]
	 * p=totalPop   if stationTypes[s]==STATION_LD
	 * p=1          otherwise
	 */
	private double[][][] serviceTimes;

	/***********************RESULTS******************************/

	/**
	 * number of iterations algorithm performed for each (what-if) iteration/execution
	 * dim: algIterations<Algorithm, [iterations]>
	 */
	private Map<SolverAlgorithm, int[]> algIterations;

	/**
	 * queue length
	 * dim: queueLen<Algorithm, [stations][classes][iterations]>
	 */
	private Map<SolverAlgorithm, double[][][]> queueLen;

	/**
	 * throughput
	 * dim: throughput<Algorithm, [stations][classes][iterations]>
	 */
	private Map<SolverAlgorithm, double[][][]> throughput;

	/**
	 * residence times
	 * dim: resTimes<Algorithm, [stations][classes][iterations]>
	 */
	private Map<SolverAlgorithm, double[][][]> resTimes;

	/**
	 * utilization
	 * dim: util<Algorithm, [stations][classes][iterations]>
	 */
	private Map<SolverAlgorithm, double[][][]> util;

	/**
	 * logarithm of normalising constants
	 * dim: logNormConst<Algorithm, 1>
	 */
	private Map<SolverAlgorithm, Double> logNormConst;

	/*****************************************************************/
	//parameters for randomization
	private static final double MAXRAND = 100;
	private static final double MAXRANGE = 10;

	/*****************************************************************/

	/********************** WHAT-IF ANALYSIS *** Bertoli Marco *******/
	/** Number of iterations (1 for normal use, >1 for what-if analysis) */
	private int iterations = 1;
	/** Index of class selected for what-if analysis. -1 means all classes */
	private int whatIfClass = -1;
	/** Index of station selected for what-if analysis. -1 means all stations */
	private int whatIfStation = -1;
	/**
	 * Type of what-if analysis
	 * @see ExactConstants
	 */
	private String whatIfType;
	/** Array with considered values */
	private double[] whatIfValues;

	/**
	 * make an object with default values
	 */
	public ExactModel() {
		setDefaults();
	}

	/**
	 * copy constructor
	 */
	public ExactModel(ExactModel e) {
		closed = e.closed;
		open = e.open;
		unitaryVisits = e.unitaryVisits;
		hasResults = e.hasResults;
		resultsOK = e.resultsOK;
		changed = e.changed;

		stations = e.stations;
		classes = e.classes;
		totalPop = e.totalPop;

		description = e.description;

		stationNames = ArrayUtils.copy(e.stationNames);
		stationTypes = ArrayUtils.copy(e.stationTypes);
		stationServers = ArrayUtils.copy(e.stationServers);

		classNames = ArrayUtils.copy(e.classNames);
		classTypes = ArrayUtils.copy(e.classTypes);
		classData = ArrayUtils.copy(e.classData);
		classPriorities = ArrayUtils.copy(e.classPriorities);

		algorithmType = e.algorithmType;
		tolerance = e.tolerance;
		maxSamples = e.maxSamples;
		whatifAlgorithms = EnumSet.copyOf(e.whatifAlgorithms);
		whatifAlgorithmsTolerance = new EnumMap<SolverAlgorithm, Double>(e.whatifAlgorithmsTolerance);
		whatifAlgorithmsMaxSamples = new EnumMap<SolverAlgorithm, Integer>(e.whatifAlgorithmsMaxSamples);

		visits = ArrayUtils.copy2(e.visits);

		serviceTimes = ArrayUtils.copy3(e.serviceTimes);				

		//ReferenceStation = ArrayUtils.copy2(e.ReferenceStation);
		referenceStation = ArrayUtils.copy(e.referenceStation);

		// What-if analysis
		iterations = e.iterations;
		whatIfClass = e.whatIfClass;
		whatIfStation = e.whatIfStation;
		whatIfType = e.whatIfType;
		whatIfValues = e.whatIfValues;

		if (hasResults) {
			queueLen = new LinkedHashMap<SolverAlgorithm, double[][][]>();
			throughput = new LinkedHashMap<SolverAlgorithm, double[][][]>();
			resTimes = new LinkedHashMap<SolverAlgorithm, double[][][]>();
			util = new LinkedHashMap<SolverAlgorithm, double[][][]>();
			logNormConst = new LinkedHashMap<SolverAlgorithm, Double>();
			algIterations = new LinkedHashMap<SolverAlgorithm, int[]>();

			for (SolverAlgorithm alg : e.queueLen.keySet()) {
				queueLen.put(alg, ArrayUtils.copy3(e.queueLen.get(alg)));
				throughput.put(alg, ArrayUtils.copy3(e.throughput.get(alg)));
				resTimes.put(alg, ArrayUtils.copy3(e.resTimes.get(alg)));
				util.put(alg, ArrayUtils.copy3(e.util.get(alg)));
				logNormConst.put(alg, e.logNormConst.get(alg));
				algIterations.put(alg, ArrayUtils.copy(e.algIterations.get(alg)));
			}
		}
	}

	public SolverAlgorithm getAlgorithmType() {
		return algorithmType;
	}

	public void setAlgorithmType(SolverAlgorithm algType) {
		algorithmType = algType;
	}

	public double getTolerance() {
		return tolerance;
	}

	public void setTolerance (double tol) {
		tolerance = tol;
	}

	public int getMaxSamples() {
		return maxSamples;
	}

	public void setMaxSamples(int mSamples) {
		maxSamples = mSamples;
	}

	/**
	 * @return the set of algorithm to perform whatif on
	 */
	public Set<SolverAlgorithm> getWhatifAlgorithms() {
		return whatifAlgorithms;
	}

	/**
	 * Set algorithm to perform whatif on
	 * @param algorithm the algorithm
	 * @param compare true to do whatif, false otherwise
	 */
	public void setWhatifAlgorithm(SolverAlgorithm algorithm, boolean compare) {
		if (compare) {
			this.whatifAlgorithms.add(algorithm);
		} else {
			this.whatifAlgorithms.remove(algorithm);
		}
	}

	/**
	 * Returns the tolerance for a whatif algorithm
	 * @param algorithm the algorithm
	 * @return the tolerance
	 */
	public double getWhatifAlgorithmTolerance(SolverAlgorithm algorithm) {
		Double val = whatifAlgorithmsTolerance.get(algorithm);
		if (val != null) {
			return val;
		} else {
			return SolverMultiClosedAMVA.DEFAULT_TOLERANCE;
		}
	}

	/**
	 * Sets the tolerance for a whatif algorithm
	 * @param algorithm the algorithm
	 * @param value the tolerance
	 */
	public void setWhatifAlgorithmTolerance(SolverAlgorithm algorithm, double value) {
		whatifAlgorithmsTolerance.put(algorithm, value);
	}

	/**
	 * Returns the max samples for a whatif algorithm
	 * @param algorithm the algorithm
	 * @return the max samples
	 */
	public int getWhatifAlgorithmMaxSamples(SolverAlgorithm algorithm) {
		Integer val = whatifAlgorithmsMaxSamples.get(algorithm);
		if (val != null) {
			return val;
		} else {
			//return SolverMultiClosedMonteCarlo.DEFAULT_MAX_SAMPLES;
			return SolverMultiClosedMonteCarloLogistic.DEFAULT_MAX_SAMPLES;
		}
	}

	/**
	 * Sets the max samples for a whatif algorithm
	 * @param algorithm the algorithm
	 * @param value the max samples
	 */
	public void setWhatifAlgorithmMaxSamples(SolverAlgorithm algorithm, int value) {
		whatifAlgorithmsMaxSamples.put(algorithm, value);
	}

	/**
	 * @return true if AMVA algorithm comparison was requested. false otherwise.
	 */
	public boolean isWhatifAlgorithms() {
		return whatifAlgorithms.size() > 0;
	}

	/**
	 * Clears whatif algorithms selection
	 */
	public void clearWhatifAlgorithms() {
		whatifAlgorithms.clear();
	}

	public int getIterations() {
		return iterations;
	}

	/**
	 * Clears all the results
	 */
	public void discardResults() {
		queueLen = null;
		throughput = null;
		resTimes = null;
		util = null;
		logNormConst = null;
		discardChanges();
	}

	/**
	 * Discards all change elements but does not touch results
	 */
	public void discardChanges() {
		hasResults = false;
		resultsOK = false;
		changed = true;
	}

	/**
	 * sets all the result data for this model.
	 * @throws IllegalArgumentException if any argument is null or not of the correct size
	 */
	public void setResults(SolverAlgorithm alg, int[] algIterations, double[][][] queueLen, double[][][] throughput, double[][][] resTimes, double[][][] util, double logNormConst) {
		if (queueLen == null || queueLen.length != stations || queueLen[0].length != classes) {
			throw new IllegalArgumentException("queueLen must be non null and of size [stations][classes][iterations]");
		}
		if (throughput == null || throughput.length != stations || throughput[0].length != classes) {
			throw new IllegalArgumentException("throughput must be non null and of size [stations][classes][iterations]");
		}
		if (resTimes == null || resTimes.length != stations || resTimes[0].length != classes) {
			throw new IllegalArgumentException("resTimes must be non null and of size [stations][classes][iterations]");
		}
		if (util == null || util.length != stations || util[0].length != classes) {
			throw new IllegalArgumentException("util must be non null and of size [stations][classes][iterations]");
		}
		// it does not check the number of classes for all station, just for the first one!!
		this.queueLen.put(alg, ArrayUtils.copy3(queueLen));
		this.throughput.put(alg, ArrayUtils.copy3(throughput));
		this.resTimes.put(alg, ArrayUtils.copy3(resTimes));
		this.util.put(alg, ArrayUtils.copy3(util));
		this.logNormConst.put(alg, new Double(logNormConst));
		this.algIterations.put(alg, ArrayUtils.copy(algIterations));

		iterations = queueLen[0][0].length;
	}

	/**
	 * sets all the result data for this model. This is called when only one iteration is performed.
	 * @throws IllegalArgumentException if any argument is null or not of the correct size
	 */
	public void setResults(SolverAlgorithm alg, int algIterations, double[][] queueLen, double[][] throughput, double[][] resTimes, double[][] util, double logNormConst) {
		resetResults();
		setResults(alg, algIterations, queueLen, throughput, resTimes, util, logNormConst, 0);
	}

	/**
	 * Sets ResultsOK flag
	 * @param value value of ResultsOK flag
	 */
	public void setResultsOK(boolean value) {
		this.resultsOK = value;
	}

	/**
	 * sets all the result data for this model. This is called on multiple iterations (what-if analysis)
	 * @throws IllegalArgumentException if any argument is null or not of the correct size
	 */
	public void setResults(SolverAlgorithm alg, int algIterations, double[][] queueLen, double[][] throughput, double[][] resTimes, double[][] util, double logNormConst, int iteration) {
		if (queueLen == null || queueLen.length != stations || queueLen[0].length != classes) {
			throw new IllegalArgumentException("queueLen must be non null and of size [stations][classes]");
		}
		if (throughput == null || throughput.length != stations || throughput[0].length != classes) {
			throw new IllegalArgumentException("throughput must be non null and of size [stations][classes]");
		}
		if (resTimes == null || resTimes.length != stations || resTimes[0].length != classes) {
			throw new IllegalArgumentException("resTimes must be non null and of size [stations][classes]");
		}
		if (util == null || util.length != stations || util[0].length != classes) {
			throw new IllegalArgumentException("util must be non null and of size [stations][classes]");
		}
		if (iteration >= iterations) {
			throw new IllegalArgumentException("iteration is greater than expected number of iterations");
		}
		if (this.queueLen.get(alg) == null) {
			this.queueLen.put(alg, new double[stations][classes][iterations]);
			this.throughput.put(alg, new double[stations][classes][iterations]);
			this.resTimes.put(alg, new double[stations][classes][iterations]);
			this.util.put(alg, new double[stations][classes][iterations]);
			this.logNormConst.put(alg, new Double(Double.NaN));
			this.algIterations.put(alg, new int[iterations]);
		}

		ArrayUtils.copy2to3(queueLen, this.queueLen.get(alg), iteration);
		ArrayUtils.copy2to3(throughput, this.throughput.get(alg), iteration);
		ArrayUtils.copy2to3(resTimes, this.resTimes.get(alg), iteration);
		ArrayUtils.copy2to3(util, this.util.get(alg), iteration);
		this.logNormConst.put(alg, new Double(logNormConst));
		this.algIterations.get(alg)[iteration] = algIterations;
	}

	public void setResultsBooleans(boolean value) {
		hasResults = value;
		resultsOK = value;
		changed = value;
	}

	/**
	 * Resets a particular algorithm's arrays used to store results
	 */
	public void resetAlgResults(SolverAlgorithm alg) {
		if (queueLen.containsKey(alg)) {
			queueLen.put(alg, new double[stations][classes][iterations]);
			throughput.put(alg, new double[stations][classes][iterations]);			
			resTimes.put(alg, new double[stations][classes][iterations]);
			util.put(alg, new double[stations][classes][iterations]);
			logNormConst.put(alg, new Double(Double.NaN));
			algIterations.put(alg, new int[iterations]);
		}

		hasResults = false;
		changed = true;
	}

	/**
	 * Resets all algorithms' arrays used to store results
	 */
	public void resetAlgResults() {
		for (SolverAlgorithm alg : queueLen.keySet()) {
			queueLen.put(alg, new double[stations][classes][iterations]);
			throughput.put(alg, new double[stations][classes][iterations]);
			resTimes.put(alg, new double[stations][classes][iterations]);
			util.put(alg, new double[stations][classes][iterations]);
			logNormConst.put(alg, new Double(Double.NaN));
			algIterations.put(alg, new int[iterations]);
		}

		hasResults = false;
		changed = true;
	}

	/**
	 * Resets arrays used to store results
	 */
	public void resetResults() {
		queueLen = new LinkedHashMap<SolverAlgorithm, double[][][]>();
		throughput = new LinkedHashMap<SolverAlgorithm, double[][][]>();
		resTimes = new LinkedHashMap<SolverAlgorithm, double[][][]>();
		util = new LinkedHashMap<SolverAlgorithm, double[][][]>();
		logNormConst = new LinkedHashMap<SolverAlgorithm, Double>();
		algIterations = new LinkedHashMap<SolverAlgorithm, int[]>();

		hasResults = false;
		changed = true;
	}

	/**
	 * Initialize the object with defaults:
	 * 1 closed class, 1 LI station, 0 customers, all visits to one, all service times to zero, no results
	 */
	public void setDefaults() {
		closed = true;
		hasResults = false;
		resultsOK = false;
		stations = 1;
		classes = 1;

		totalPop = 1;
		changed = true;

		classData = new double[1];
		classData[0] = 1;

		stationNames = new String[1];
		stationNames[0] = "Station1";

		stationTypes = new int[1];
		stationTypes[0] = STATION_LI;

		stationServers = new int[1];
		stationServers[0] = 1;

		classNames = new String[1];
		classNames[0] = "Class1";

		classTypes = new int[1];
		classTypes[0] = CLASS_CLOSED;

		classPriorities = new int[1];
		classPriorities[0]  = DEFAULT_CLASS_PRIORITY;

		visits = new double[1][1];
		visits[0][0] = 1.0;

		//ReferenceStation = new double[1][2];
		//ReferenceStation[0][0]= 0.0;
		//ReferenceStation[0][1]= 0.0;

		referenceStation = new int[1];
		referenceStation[0]= 0;

		serviceTimes = new double[1][1][1];
		serviceTimes[0][0][0] = 0.0;

		description = "";

		whatifAlgorithms = EnumSet.noneOf(SolverAlgorithm.class);
		whatifAlgorithmsTolerance = new EnumMap<SolverAlgorithm, Double>(SolverAlgorithm.class);
		whatifAlgorithmsMaxSamples = new EnumMap<SolverAlgorithm, Integer>(SolverAlgorithm.class);
	}

	/**
	 * Gets the model description
	 * @return the model description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the model description
	 * @param description the model description
	 * @return true if data was changed, false otherwise
	 */
	public boolean setDescription(String description) {
		if (description.equals(this.description)) {
			return false;
		}
		this.description = description;
		changed = true;
		return true;
	}

	/**
	 * @return true if this object describes a single class system
	 */
	public boolean isSingleClass() {
		return (classes == 1);
	}

	/**
	 * @return true if this object describes a multiclass system
	 */
	public boolean isMultiClass() {
		return (classes > 1);
	}

	/**
	 * @return true if this object describes a closed system
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * @return true if this object describes an open system
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * @return true if this object describes a mixed system
	 */
	public boolean isMixed() {
		return !(closed || open);
	}

	/**
	 * @return true if this object describes a system containing LD stations
	 */
	public boolean isLd() {
		return ld;
	}

	/**
	 * @return true if this object describes a system containing priority stations
	 */
	public boolean isPriority() {
		return priority;
	}

	/**
	 * @return number of service centers
	 */
	public int getStations() {
		return stations;
	}

	/**
	 * @return number of classes
	 */
	public int getClasses() {
		return classes;
	}

	/**
	 * @return get the matrix of ReferenceStations
	 */
	/*public double[][] getReferenceStation() {
		return ReferenceStation;
	}*/

	/**
	 * @return get the matrix of ReferenceStations
	 */
	public int[] getReferenceStation() {
		return referenceStation;
	}

	/**
	 * @return total population
	 */
	public int getTotalPop() {
		return totalPop;
	}

	/**
	 * @return names of the service centers
	 */
	public String[] getStationNames() {
		return stationNames;
	}

	/**
	 * @return the number of servers for each station. For delay stations this parameter is unsensed.
	 */
	public int[] getStationServers() {
		return stationServers;
	}

	/**
	 * @return true if this model contains multiple server for a station.
	 */
	public boolean isMultipleServers() {
		for (int i = 0; i < stations; i++) {
			if (stationServers[i] > 1 && stationTypes[i] != ExactConstants.STATION_DELAY) {
				return true;
			}
		}
		return false;
	}

	/**
	 * sets the number of servers for each station
	 * @param stationServers the number of servers of each station
	 * @throws IllegalArgumentException if the array is not of the correct size
	 * @return true if data was changed, false otherwise
	 */
	public boolean setStationServers(int[] stationServers) {
		if (stationServers.length != stations) {
			throw new IllegalArgumentException("stationServers.length != stations");
		}
		if (Arrays.equals(this.stationServers, stationServers)) {
			return false;
		}

		this.stationServers = stationServers;
		resultsNotOkDueToModelChange();
		return true;
	}

	/**
	 * sets the names of the service centers.
	 * @param stationNames the names of the service centers
	 * @throws IllegalArgumentException if the array is not of the correct size
	 * @return true if data was changed, false otherwise
	 */
	public boolean setStationNames(String[] stationNames) {
		if (stationNames.length != stations) {
			throw new IllegalArgumentException("stationNames.length!=stations");
		}
		if (!changed) {
			if (Arrays.equals(this.stationNames, stationNames)) {
				return false;
			}
		}
		this.stationNames = stationNames;
		changed = true;
		return true;
	}

	/**
	 * @return names of the classes
	 */
	public String[] getClassNames() {
		return classNames;
	}

	/**
	 * sets the names of the classes.
	 * @param classNames the names of the classes
	 * @throws IllegalArgumentException if the array is not of the correct size
	 * @return true if data was changed, false otherwise
	 */
	public boolean setClassNames(String[] classNames) {
		if (classNames.length != classes) {
			throw new IllegalArgumentException("classNames.length!=classes");
		}
		if (Arrays.equals(this.classNames, classNames)) {
			return false;
		}

		this.classNames = classNames;
		changed = true;
		return true;
	}

	/**
	 * @return data for the classes.
	 */
	public double[] getClassData() {
		return classData;
	}

	/**
	 * sets the data for the classes
	 * @param classData the data for the classes
	 * @throws IllegalArgumentException if the array is not of the correct size
	 * @return true if data was changed, false otherwise
	 */
	public boolean setClassData(double[] classData) {
		if (classData.length != classes) {
			throw new IllegalArgumentException("classData.length!=classes");
		}
		if (Arrays.equals(this.classData, classData)) {
			return false;
		}

		this.classData = classData;
		resultsNotOkDueToModelChange();

		// make sure 3rd dimension of serviceTimes is ok
		resize(stations, classes);
		return true;
	}

	/**
	 * @return type of the classes
	 */
	public int[] getClassTypes() {
		return classTypes;
	}

	/**
	 * sets the type of the classes
	 * @param classTypes the type of the classes
	 * @throws IllegalArgumentException if the array is not of the correct size
	 * @return true if data was changed, false otherwise
	 */
	public boolean setClassTypes(int[] classTypes) {
		if (classTypes.length != classes) {
			throw new IllegalArgumentException("classTypes.length!=classes");
		}
		if (Arrays.equals(this.classTypes, classTypes)) {
			return false;
		}

		this.classTypes = classTypes;
		closed = calcClosed();
		open = calcOpen();
		resultsNotOkDueToModelChange();
		return true;
	}

	/**
	 * @return priorities of the classes
	 */
	public int[] getClassPriorities() {
		return classPriorities;
	}

	/**
	 * sets the priorities of the classes
	 * @param classPriorities the priority of the classes
	 * @throws IllegalArgumentException if the array is not of the correct size
	 * @return true if data was changed, false otherwise
	 */
	public boolean setClassPriorities(int[] classPriorities) {
		//System.out.println("" + classPriorities.length + " "+classes);
		if (classPriorities.length != classes) {
			throw new IllegalArgumentException("classPriorities.length!=classes");
		}
		if (Arrays.equals(this.classPriorities, classPriorities)) {
			return false;
		}

		this.classPriorities = classPriorities;
		resultsNotOkDueToModelChange();
		return true;
	}

	/**
	 * @return type of the stations
	 */
	public int[] getStationTypes() {
		return stationTypes;
	}

	/**
	 * sets the type of the stations
	 * @param stationTypes the type of the stations
	 * @throws IllegalArgumentException if the array is not of the correct size
	 * @return true if data was changed, false otherwise
	 */
	public boolean setStationTypes(int[] stationTypes) {
		if (stationTypes.length != stations) {
			throw new IllegalArgumentException("stationTypes.length!=stations");
		}
		if (Arrays.equals(this.stationTypes, stationTypes)) {
			return false;
		}

		this.stationTypes = stationTypes;
		// adjusts serviceTimes size and recalculates flags
		resize(stations, classes, true);
		resultsNotOkDueToModelChange();
		return true;
	}

	/**
	 * @return the matrix of visits
	 */
	public double[][] getVisits() {
		return visits;
	}

	/**
	 * sets the matrix of visits
	 * @param visits the matrix of visits
	 * @throws IllegalArgumentException if the matrix is not of the correct size
	 * @return true if data was changed, false otherwise
	 */
	public boolean setVisits(double[][] visits) {
		if (visits.length != stations || visits[0].length != classes) {
			throw new IllegalArgumentException("incorrect array dimension");
		}
		if (ArrayUtils.equals2(this.visits, visits)) {
			return false;
		}

		this.visits = visits;
		resultsNotOkDueToModelChange();

		// Checks if visits are all one
		calcUnitaryVisits();
		return true;
	}

	/**
	 * @return the matrix of service times
	 */
	public double[][][] getServiceTimes() {
		return serviceTimes;
	}

	/**
	 * sets the matrix of service times
	 * @param serviceTimes the matrix of service times
	 * @throws IllegalArgumentException if the matrix is not of the correct size
	 * @return true if data was changed, false otherwise
	 */
	public boolean setServiceTimes(double[][][] serviceTimes) {
		if (serviceTimes.length != stations || serviceTimes[0].length != classes) {
			throw new IllegalArgumentException("incorrect array dimension");
		}
		if (ArrayUtils.equals3(this.serviceTimes, serviceTimes)) {
			return false;
		}

		int currSize;
		double[][] subST;

		//validate sizes
		for (int s = 0; s < stations; s++) {
			currSize = (stationTypes[s] == STATION_LD ? totalPop : 1);
			// if a station is LD but there are no jobs, totalPop = 0
			if (currSize == 0) {
				currSize = 1;
			}
			subST = serviceTimes[s];
			for (int c = 0; c < classes; c++) {
				if (subST[c].length != currSize) {
					throw new IllegalArgumentException("Wrong size for station " + stationNames[s]);
				}
			}
		}

		this.serviceTimes = serviceTimes;
		resultsNotOkDueToModelChange();
		return true;
	}

	/**
	 * Used to invalidated current results and note that the model has been edited
	 */
	private void resultsNotOkDueToModelChange() {
		changed = true;
		resultsOK = false;
	}

	/**
	 * sets the matrix of ReferenceStation
	 * @param ReferenceStation the matrix of ReferenceStation
	 * @throws IllegalArgumentException if the matrix is not of the correct size
	 * @return true if data was changed, false otherwise
	 */
	/*public boolean setReferenceStation(double[][] ReferenceStation) {
		if (ReferenceStation.length != classes) {
			throw new IllegalArgumentException("incorrect array dimension");
		}
		this.ReferenceStation = ReferenceStation;
		changed = true;
		resultsOK = false;
		return true;
	}*/

	/**
	 * sets the matrix of ReferenceStation
	 * @param ReferenceStation the matrix of ReferenceStation
	 * @throws IllegalArgumentException if the matrix is not of the correct size
	 * @return true if data was changed, false otherwise
	 */
	public boolean setReferenceStation(int[] ReferenceStation) {
		if (ReferenceStation.length != classes) {
			throw new IllegalArgumentException("incorrect array dimension");
		}
		this.referenceStation = ReferenceStation;
		//changed = true;
		resultsOK = false;
		return true;
	}

	/**
	 * Resizes the data structures according to specified parameters. Data is preserved as far as possible
	 * @return true if data was changed, false otherwise
	 */
	public boolean resize(int stations, int classes, boolean forceResize) {
		if (stations <= 0 || classes <= 0) {
			throw new IllegalArgumentException("stations and classes must be > 0");
		}
		if (forceResize || this.stations != stations || this.classes != classes) {
			// Other cases already handled in setXXX methods
			discardResults();

			this.stations = stations;
			this.classes = classes;

			stationNames = ArrayUtils.resize(stationNames, stations, null);
			stationTypes = ArrayUtils.resize(stationTypes, stations, STATION_LI);
			stationServers = ArrayUtils.resize(stationServers, stations, 1);
			ld = calcLD();
			priority = calcPriority();

			visits = ArrayUtils.resize2(visits, stations, classes, 1.0);

			classNames = ArrayUtils.resize(classNames, classes, null);
			classTypes = ArrayUtils.resize(classTypes, classes, CLASS_CLOSED);
			closed = calcClosed();

			classData = ArrayUtils.resize(classData, classes, 0.0);

			totalPop = calcTotalPop();

			serviceTimes = ArrayUtils.resize3var(serviceTimes, stations, classes, calcSizes(), 0.0);
			// Checks if visits are all one
			calcUnitaryVisits();

			//ReferenceStation = ArrayUtils.resize2(ReferenceStation, classes, 2, 0.0);			
			referenceStation = ArrayUtils.resize(referenceStation, classes, 0);

			return true;
		} else {
			// Checks if total population was changed.
			int newTotalPop = calcTotalPop();
			if (newTotalPop != totalPop) {
				totalPop = newTotalPop;
				serviceTimes = ArrayUtils.resize3var(serviceTimes, stations, classes, calcSizes(), 0.0);
				return true;
			}
		}
		return false;
	}

	/**
	 * Resizes the data structures according to specified parameters. Data is preserved as far as possible
	 * @return true if data was changed, false otherwise
	 */
	public boolean resize(int stations, int classes) {
		return resize(stations, classes, false);
	}

	/**
	 * @return algorithm iterations
	 */
	public Map<SolverAlgorithm, int[]> getAlgIterations() {
		return algIterations;
	}

	/**
	 * @return algorithm iterations for the given algorithm
	 */
	public int[] getAlgIterations(SolverAlgorithm alg) {
		if (alg != null) {
			return algIterations.get(alg);
		}
		return null;
	}

	/**
	 * @return queue lengths
	 */
	public Map<SolverAlgorithm, double[][][]> getQueueLen() {
		return queueLen;
	}

	/**
	 * @return queue lengths for the given algorithm
	 */
	public double[][][] getQueueLen(SolverAlgorithm alg) {
		if (alg != null) {
			return queueLen.get(alg);
		}
		return null;
	}

	/**
	 * @return residence times
	 */
	public Map<SolverAlgorithm, double[][][]> getResidTimes() {
		return resTimes;
	}

	/**
	 * @return response times for the given algorithm
	 */
	public double[][][] getResidTimes(SolverAlgorithm alg) {
		if (alg != null) {
			double[][][] res = ArrayUtils.copy3(resTimes.get(alg));
			for (int s = 0; s < stations; s++)
				for (int c = 0; c < classes; c++)
					for (int i = 0; i < iterations; i++)
						if (classTypes[c] == CLASS_CLOSED)
							if (visits[referenceStation[c]][c] != 0)
								res[s][c][i] /= (visits[referenceStation[c]][c]);
			return res;
		}
		return null;
	}

	/**
	 * @return residence times for the given algorithm
	 */
	public double[][][] getRespTimes(SolverAlgorithm alg) {
		if (alg != null) {
			double[][][] res = ArrayUtils.copy3(resTimes.get(alg));
			for (int s = 0; s < stations; s++)
				for (int c = 0; c < classes; c++)
					for (int i = 0; i < iterations; i++)
						if (classTypes[c] == CLASS_CLOSED) {
							if (visits[referenceStation[c]][c] != 0) {
								res[s][c][i] /= (visits[referenceStation[c]][c]);
							}
							if (visits[s][c] != 0) {
								res[s][c][i] /= (visits[s][c]);
							}
						}
			return res;
		}
		return null;
	}

	/**
	 * @return throughputs
	 */
	public Map<SolverAlgorithm, double[][][]> getThroughput() {
		return throughput;
	}

	/**
	 * @return throughputs for the given algorithm
	 */
	public double[][][] getThroughput(SolverAlgorithm alg) {
		if (alg != null) {
			return throughput.get(alg);
		}
		return null;
	}

	/**
	 * @return utilizations
	 */
	public Map<SolverAlgorithm, double[][][]> getUtilization() {
		return util;
	}

	/**
	 * @return utilizations for the given algorithm
	 */
	public double[][][] getUtilization(SolverAlgorithm alg) {
		if (alg != null) {
			return util.get(alg);
		}
		return null;
	}

	/**
	 * @return logarithms of normalising constants
	 */
	public Map<SolverAlgorithm, Double> getLogNormConst() {
		return logNormConst;
	}

	/**
	 * @return logarithm of normalising constant for the given algorithm
	 */
	public double setLogNormConst(SolverAlgorithm alg) {
		if (alg != null) {
			return logNormConst.get(alg).doubleValue();
		}
		return Double.NaN;
	}

	/**
	 * Removes all LD stations, converting them into LI stations
	 */
	public void removeLD() {
		for (int i = 0; i < stations; i++) {
			if (stationTypes[i] == STATION_LD) {
				stationTypes[i] = STATION_LI;

				//clear old LD service times
				serviceTimes[i] = new double[classes][1];
				for (int c = 0; c < classes; c++) {
					serviceTimes[i][c][0] = 0.0;
				}
			}
		}
		ld = false;
	}

	/**
	 * This method will find if current visits matrix is unitary or not.
	 * If a value is 0 will check service demand. This is used to show correct
	 * panel layout upon loading of a model
	 */
	private void calcUnitaryVisits() {
		double epsilon = 1e-14;
		for (int i = 0; i < stations; i++) {
			for (int j = 0; j < classes; j++) {
				if ((Math.abs(visits[i][j]) < epsilon && Math.abs(serviceTimes[i][j][0]) > epsilon)
						|| (!(Math.abs(visits[i][j]) < epsilon) && Math.abs(visits[i][j] - 1.0) > epsilon)) {
					unitaryVisits = true;
					return;
				}
			}
		}
		unitaryVisits = false;
	}

	/**
	 * @return true if the model contains only closed stations
	 */
	private boolean calcClosed() {
		for (int i = 0; i < classes; i++) {
			if (classTypes[i] != CLASS_CLOSED) {
				//make sure we stay in a consistent state
				//removeLD();
				//Removes LD as multiclass LD is not supported
				return false;
			}
		}
		return true;
	}

	/**
	 * @return true if the model contains only open stations
	 */
	private boolean calcOpen() {
		for (int i = 0; i < classes; i++) {
			if (classTypes[i] != CLASS_OPEN) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return true if the model contains Load Dependent stations
	 */
	private boolean calcLD() {
		for (int i = 0; i < stations; i++) {
			if (stationTypes[i] == STATION_LD) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return true if the model contains Priority stations
	 */
	private boolean calcPriority() {
		for (int i = 0; i < stations; i++) {
			if (stationTypes[i] == STATION_PRS || stationTypes[i] == STATION_HOL) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the total population (sum of the customers of all closed class)
	 */
	private int calcTotalPop() {
		/* sum all the closed classes' customers */
		int totalPop = 0;
		for (int i = 0; i < classes; i++) {
			if (classTypes[i] == CLASS_CLOSED) {
				totalPop += (int) classData[i];
			}
		}
		return totalPop;
	}

	/**
	 * @return the sizes of service times for each station (total pop for LD stations, 1 for LI stations)
	 */
	private int[] calcSizes() {
		int mp = (totalPop > 0 ? totalPop : 1);
		int[] sizes = new int[stations];
		for (int s = 0; s < stations; s++) {
			sizes[s] = (stationTypes[s] == STATION_LD ? mp : 1);
		}
		return sizes;
	}

	/**
	 * Warning: Calling this on large systems *will* result in OutOfMemory errors. You have been warned.
	 * @return a String representation of the parameters of this object
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("stations=").append(stations)
				.append(" classes=").append(classes)
				.append(" pop=").append(totalPop)
				.append(" changed=").append(changed)
				.append(" ld=").append(ld)
				.append(" open=").append(open)
				.append(" closed=").append(closed)
				.append(" priority=").append(priority)
				.append(" hasResults=").append(hasResults)
				.append(" resultsOK=").append(resultsOK).append("\n")
				.append("stationNames=").append(ArrayUtils.toString(stationNames)).append("\n")
				.append("stationTypes=").append(ArrayUtils.toString(stationTypes)).append("\n")
				.append("classNames=").append(ArrayUtils.toString(classNames)).append("\n")
				.append("classTypes=").append(ArrayUtils.toString(classTypes)).append("\n")
				.append("classData=").append(ArrayUtils.toString(classData)).append("\n")
				.append("visits=").append(ArrayUtils.toString2(visits)).append("\n")
				.append("serviceTimes=").append(ArrayUtils.toString3(serviceTimes)).append("\n");
		if (hasResults) {
			for (SolverAlgorithm alg : queueLen.keySet()) {
				double[][][] queueLens = queueLen.get(alg);
				double[][][] throughputs = throughput.get(alg);
				double[][][] resTime = resTimes.get(alg);
				double[][][] utils = util.get(alg);
				double logNC = logNormConst.get(alg).doubleValue();

				s.append("algorithm=").append(alg).append("\n")
						.append("number of customers=").append(ArrayUtils.toString3(queueLens)).append("\n")
						.append("throughput=").append(ArrayUtils.toString3(throughputs)).append("\n")
						.append("resTimes=").append(ArrayUtils.toString3(resTime)).append("\n")
						.append("utilization=").append(ArrayUtils.toString3(utils)).append("\n")
						.append("logarithm of normalising constant=").append(Double.toString(logNC)).append("\n");
			}
		}

		return s.toString();
	}

	/**
	 * Deletes a class
	 * @param i the index of the class
	 */
	public void deleteClass(int i) {
		if (classes < 2) {
			throw new RuntimeException("System must have at least one class");
		}

		classes--;

		classNames = ArrayUtils.delete(classNames, i);
		classTypes = ArrayUtils.delete(classTypes, i);
		classData = ArrayUtils.delete(classData, i);

		visits = ArrayUtils.delete2_2(visits, i);
		serviceTimes = ArrayUtils.delete3_2(serviceTimes, i);					
		//ReferenceStation = ArrayUtils.delete2_1(ReferenceStation, i);
		referenceStation = ArrayUtils.delete(referenceStation, i);
		resize(stations, classes);
		//DEK (Federico Granata) 3-10-2003
		//it was considering the results valid when a class is cancelled
		hasResults = false;
		//END
	}

	/**
	 * Deletes a station
	 * @param i the index of the station
	 */
	public void deleteStation(int i) {
		if (stations < 2) {
			throw new RuntimeException("System must have at least one station");
		}
		stations--;
		stationNames = ArrayUtils.delete(stationNames, i);
		stationTypes = ArrayUtils.delete(stationTypes, i);
		stationServers = ArrayUtils.delete(stationServers, i);

		visits = ArrayUtils.delete2_1(visits, i);
		serviceTimes = ArrayUtils.delete3_1(serviceTimes, i);

		resize(stations, classes);

		//it was considering the results valid when a class is cancelled
		hasResults = false;
		//END

		//Delete the selected station in ReferenceStation (Kourosh OCT 2013)		
		for (int j=0; j< classes; j++)
			if (referenceStation[j] == i)
				referenceStation[j] = 0;
	}

	/**
	 * @return true if the model has been changed
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * resets the changed flag.
	 * <br>
	 * WARNING: this enables change checking on parameter setting, which can be quite time-consuming.
	 */
	public void resetChanged() {
		changed = false;
	}

	/**
	 * flags the model as changed.
	 * There is no need to call this, except to disable time-consuming change checking if you're not interested in it
	 */
	public void setChanged() {
		changed = true;
	}

	/**
	 * @return true if results are available
	 */
	public boolean hasResults() {
		return hasResults;
	}

	/**
	 * @return true if results are valid
	 */
	public boolean areResultsOK() {
		return resultsOK;
	}

	/**
	 * Creates a DOM representation of this object
	 * @return a DOM representation of this object
	 */
	public Document createDocument() {
		return ExactDocumentCreator.createDocument(this);
	}

	//Not used
	/*private void appendMatrixCSV(Document root, Element base, double[][] arr, String outer, String inner) {
		// forse devo usare questo per trattare anche il caso LD
		Element elems, elem;
		int n = arr.length;
		elems = root.createElement(outer);
		base.appendChild(elems);
		for (int i = 0; i < n; i++) {
			elem = root.createElement(inner);
			// separa i diversi elementi dell'array con ";"
			elem.appendChild(root.createTextNode(ArrayUtils.toCSV(arr[i])));
			elems.appendChild(elem);
		}
	}*/

	/**
	 * load the state of this object from the Document.
	 * @return true if the operation was successful.
	 * WARNING: If the operation fails the object is left in an incorrect state and should be discarded.
	 */
	public boolean loadDocument(Document doc) {
		Node classNode = doc.getElementsByTagName("classes").item(0);
		Node stationNode = doc.getElementsByTagName("stations").item(0);

		Node ReferenceStationNode = doc.getElementsByTagName("ReferenceStation").item(0);
		NodeList descList = doc.getElementsByTagName("description");
		NodeList solList = doc.getElementsByTagName("solutions");

		//load description
		if (descList.item(0) != null) {
			if (!loadDescription((Element) descList.item(0))) {
				//description loading failed!
				return false;
			}
		} else {
			description = "";
		}

		//load classes
		if (classNode != null) {
			if (!loadClasses(classNode)) {
				//classes loading failed!
				return false;
			}
		}

		//load stations
		if (stationNode != null) {
			if (!loadStations(stationNode)) {
				//stations loading failed!
				return false;
			}
		}

		//load ReferenceStation
		if (ReferenceStationNode != null) {
			if (!loadReferenceStation(ReferenceStationNode)) {
				//ReferenceStation loading failed!
				return false;
			}
		} else {
			loadReferenceStationDefault(classNode);
		}

		/* Algorithm parameters */
		NodeList algParams = doc.getElementsByTagName("algParams");
		if (algParams.getLength() > 0) {
			Element algParam = (Element) algParams.item(0);

			Element algType = (Element) algParam.getElementsByTagName("algType").item(0);
			setAlgorithmType(SolverAlgorithm.fromString(algType.getAttribute("name")));
			setTolerance(Double.parseDouble(algType.getAttribute("tolerance")));
			if (!algType.getAttribute("maxSamples").isEmpty()) {
				setMaxSamples(Integer.parseInt(algType.getAttribute("maxSamples")));
			}

			Element compareAlgs = (Element) algParam.getElementsByTagName("compareAlgs").item(0);
			NodeList whatIfAlgs = compareAlgs.getElementsByTagName("whatIfAlg");
			for (int i = 0; i < whatIfAlgs.getLength(); i++) {
				Element alg = (Element) whatIfAlgs.item(i);
				String name = alg.getAttribute("name");
				SolverAlgorithm algo = SolverAlgorithm.fromString(name);
				this.setWhatifAlgorithm(algo, true);
				this.setWhatifAlgorithmTolerance(algo, Double.parseDouble(alg.getAttribute("tolerance")));
				if (!alg.getAttribute("maxSamples").isEmpty()) {
					this.setWhatifAlgorithmMaxSamples(algo, Integer.parseInt(alg.getAttribute("maxSamples")));
				}
			}
		} else {
			this.whatifAlgorithms = EnumSet.noneOf(SolverAlgorithm.class);
			this.whatifAlgorithmsTolerance = new EnumMap<SolverAlgorithm, Double>(SolverAlgorithm.class);
			this.whatifAlgorithmsMaxSamples = new EnumMap<SolverAlgorithm, Integer>(SolverAlgorithm.class);
		}

		/* What-if Analysis - Bertoli Marco */
		NodeList whatIfs = doc.getElementsByTagName("whatIf");
		if (whatIfs.getLength() > 0) {
			// What-if analysis was saved
			Element whatIf = (Element) whatIfs.item(0);
			setWhatIfType(whatIf.getAttribute("type"));
			setWhatIfValues(ArrayUtils.fromCSV(whatIf.getAttribute("values")));
			// Try to retrieve what-if class informations
			setWhatIfClass(-1);
			String className = whatIf.getAttribute("className");
			if (className != null && !className.equals("")) {
				for (int i = 0; i < classes; i++) {
					if (classNames[i].equals(className)) {
						setWhatIfClass(i);
						break;
					}
				}
			}
			// Try to retrieve what-if station informations
			setWhatIfStation(-1);
			String stationName = whatIf.getAttribute("stationName");
			if (stationName != null && !stationName.equals("")) {
				for (int i = 0; i < stations; i++) {
					if (stationNames[i].equals(stationName)) {
						setWhatIfStation(i);
						break;
					}
				}
			}
		} else {
			// What-if analysis was not saved
			iterations = 1;
			setWhatIfClass(-1);
			setWhatIfStation(-1);
		}

		//load solution
		if (solList.getLength() > 0) {
			if (!loadSolution(solList)) {
				return false;
			}
			hasResults = true;
		} else {
			this.resetResults(); 
		}

		//compute flags
		resize(stations, classes);
		changed = false;
		return true;
	}

	public boolean loadDescription(Element desc) {
		description = desc.getFirstChild().getNodeValue();
		return true;
	}

	public boolean loadClasses(Node classNode) {
		classes = Integer.parseInt(((Element) classNode).getAttribute("number"));

		classNames = new String[classes];
		classTypes = new int[classes];
		classData = new double[classes];
		classPriorities = new int[classes];
		//ReferenceStation = new double[classes][];
		referenceStation = new int[classes];
		NodeList classList = classNode.getChildNodes();				
		int classNum = 0;

		totalPop = 0;
		Node n;
		Element current;
		closed = true;
		open = true;

		/* classes */
		for (int i = 0; i < classList.getLength(); i++) {
			n = classList.item(i);
			if (!(n instanceof Element)) {
				continue;
			}
			current = (Element) n;
			classNames[classNum] = current.getAttribute(DOC_CLASS_NAME);

			// For backwards compatibility so it can still load models which didn't have any priorities
			String priority_str = current.getAttribute(DOC_CLASS_PRIORITY);
			if (priority_str.equals("")) {
				// Priority wasn't set
				classPriorities[classNum] = DEFAULT_CLASS_PRIORITY;
			} else {
				classPriorities[classNum] = Integer.parseInt(priority_str);
			}

			if (current.getTagName().equals(DOC_CLASS_CLOSED)) {
				classTypes[classNum] = CLASS_CLOSED;
				classData[classNum] = Double.parseDouble(current.getAttribute(DOC_CLASS_POPULATION));
				//ReferenceStation[classNum][2] = 0;
				//for (int j=0; j<stations; j++)
				//	if (stationNames[j] == current.getAttribute("refStation"))
				//ReferenceStation[classNum][2] = j;
				totalPop += (int) classData[classNum];
				open = false;
			} else {
				classTypes[classNum] = CLASS_OPEN;
				classData[classNum] = Double.parseDouble(current.getAttribute(DOC_CLASS_RATE));
				//ReferenceStation[classNum][2] = 0;
				//for (int j=0; j<stations; j++)
				//	if (stationNames[j] == current.getAttribute("refStation"))
				//ReferenceStation[classNum][2] = j;
				closed = false;
			}
			classNum++;
		}

		return true;
	}

	public boolean loadStations(Node stationNode) {
		stations = Integer.parseInt(((Element) stationNode).getAttribute(DOC_STATION_NUMBER));

		stationNames = new String[stations];
		stationTypes = new int[stations];
		stationServers = new int[stations];
		visits = new double[stations][];
		serviceTimes = new double[stations][][];

		NodeList stationList = stationNode.getChildNodes();

		ld = false;
		priority = false;

		String statType;
		NodeList sTimes = null;
		int stationNum = 0;

		/* stations */

		Node n;
		Element current;

		for (int i = 0; i < stationList.getLength(); i++) {
			n = stationList.item(i);
			if (!(n instanceof Element)) {
				continue;
			}
			current = (Element) n;
			statType = current.getTagName();
			stationNames[stationNum] = current.getAttribute(DOC_STATION_NAME);
			if (current.hasAttribute(DOC_STATION_SERVERS)) {
				stationServers[stationNum] = Integer.parseInt(current.getAttribute(DOC_STATION_SERVERS));
			} else {
				stationServers[stationNum] = 1;
			}

			/* make arrays */

			visits[stationNum] = new double[classes];
			serviceTimes[stationNum] = new double[classes][];

			/* station types and service times */

			if (statType.equals(DOC_STATION_TYPE_LD)) {
				// Load Dependent
				if (!loadLDStation(stationNum, current, sTimes)) {
					return false;
				}
			} else { //Delay, LI, PRS, HOL
				if (statType.equals(DOC_STATION_TYPE_DELAY)) {
					stationTypes[stationNum] = STATION_DELAY;
				} else if (statType.equals(DOC_STATION_TYPE_LI)) {
					stationTypes[stationNum] = STATION_LI;
				} else if (statType.equals(DOC_STATION_TYPE_PRS)) {
					stationTypes[stationNum] = STATION_PRS;
					priority = true;
				} else if (statType.equals(DOC_STATION_TYPE_HOL)) {
					stationTypes[stationNum] = STATION_HOL;
					priority = true;
				} else {
					// Station type isn't known
					return false;
				}

				/* create arrays */

				sTimes = current.getElementsByTagName(DOC_STATION_SERVICE_TIME);
				NodeList visitsNodeList = current.getElementsByTagName(DOC_STATION_VISIT);

				serviceTimes[stationNum] = new double[classes][1];
				visits[stationNum] = new double[classes];
				for (int k = 0; k < classes; k++) {
					Node node = sTimes.item(k).getFirstChild();
					String nodeValue = (node).getNodeValue();
					serviceTimes[stationNum][k][0] = Double.parseDouble(nodeValue);
					visits[stationNum][k] = Double.parseDouble((visitsNodeList.item(k).getFirstChild()).getNodeValue());
				}
			}
			stationNum++;
		}
		return true;
	}

	private boolean loadLDStation(int stationNum, Element current, NodeList sTimes) {
		ld = true;
		if (totalPop == 0) {
			System.err.println("LD station with zero customers");
			return false;
		}
		stationTypes[stationNum] = STATION_LD;

		/* create arrays */
		for (int k = 0; k < classes; k++) {
			//serviceTimes[stationNum] = new double[classes][totalPop + 1];
			serviceTimes[stationNum] = new double[classes][totalPop];
		}

		//Element sTimesElem = (Element) current.getElementsByTagName("servicetimes").item(0);
		Element sTimesElem = (Element) current.getElementsByTagName("servicetimes").item(0);
		sTimes = sTimesElem.getElementsByTagName("servicetimes");

		if (sTimes.getLength() != classes) {
			System.err.println("Wrong number of service times sets for LD station " + stationNames[stationNum]);
			return false;
		}

		Element visitsElem = (Element) current.getElementsByTagName("visits").item(0);
		NodeList visitsNodeList = visitsElem.getElementsByTagName("visit");

		for (int k = 0; k < classes; k++) {
			String visit = (visitsNodeList.item(k).getFirstChild()).getNodeValue();
			visits[stationNum][k] = Double.parseDouble(visit);

			//string of LD service times for class k
			Element class_st = (Element) sTimes.item(k);
			String stimes = class_st.getFirstChild().getNodeValue();

			double[] servt_arr = new double[totalPop];
			ArrayUtils.fromCSV(servt_arr, stimes);

			System.arraycopy(servt_arr, 0, serviceTimes[stationNum][k], 0, totalPop);
		}

		return true;
	}

	public boolean loadReferenceStation(Node ReferenceStationNode) {
		classes = Integer.parseInt(((Element) ReferenceStationNode).getAttribute("number"));
		referenceStation = new int[classes];
		NodeList classList = ReferenceStationNode.getChildNodes();
		int classNum = 0;
		Node n;
		Element current;
		for (int i = 0; i < classList.getLength(); i++) {
			n = classList.item(i);
			if (!(n instanceof Element)) {
				continue;
			}
			current = (Element) n;
			for (int j = 0; j < stations; j++) {
				if (stationNames[j].equals(current.getAttribute("refStation"))) {
					referenceStation[classNum] = j;
					break;
				} else if (current.getAttribute("refStation").equals("Arrival Process")) {						
					referenceStation[classNum] = stationNames.length;
				} else {
					referenceStation[classNum] = 0;
				}
			}
			classNum++;
		}
		return true;
	}

	public boolean loadReferenceStationDefault(Node classNode) {
		classes = Integer.parseInt(((Element) classNode).getAttribute("number"));
		for (int i = 0; i < classes; i++) {
			referenceStation[i] = 0;
		}
		return true;
	}

	/**
	 * Load solutions from xml file
	 * @param sol NodeList of solution elements
	 * @return true if load was successful, false otherwise
	 */
	public boolean loadSolution(NodeList sol) {
		resultsOK = true;
		if (queueLen == null) {
			resetResults();
		}
		for (int i = 0; i < sol.getLength(); i++) {
			Element solution = (Element) sol.item(i);
			String status = solution.getAttribute("ok");
			resultsOK = resultsOK && (status.equals("true"));
			if (solution.hasAttribute("algCount")) {
				int algCount = Integer.parseInt(solution.getAttribute("algCount"));
				for (int a = 0; a < algCount; a++) {
					Element a_alg = (Element) solution.getElementsByTagName("algorithm").item(a);
					SolverAlgorithm alg = SolverAlgorithm.fromString(a_alg.getAttribute("name"));

					if (alg == null) {
						continue;
					}
					if (i == 0) {
						queueLen.put(alg, new double[stations][classes][iterations]);
						throughput.put(alg, new double[stations][classes][iterations]);
						resTimes.put(alg, new double[stations][classes][iterations]);
						util.put(alg, new double[stations][classes][iterations]);
						logNormConst.put(alg, new Double(Double.NaN));
						algIterations.put(alg, new int[iterations]);
					}

					ArrayUtils.copy2to3(loadResultsMatrix(a_alg, stations, classes, "Number of Customers"), queueLen.get(alg), i);
					ArrayUtils.copy2to3(loadResultsMatrix(a_alg, stations, classes, "Throughput"), throughput.get(alg), i);
					ArrayUtils.copy2to3(loadResultsMatrix(a_alg, stations, classes, "Residence time"), resTimes.get(alg), i);
					ArrayUtils.copy2to3(loadResultsMatrix(a_alg, stations, classes, "Utilization"), util.get(alg), i);
					if (a_alg.getElementsByTagName("normconst").getLength() > 0) {
						Element nc = (Element) a_alg.getElementsByTagName("normconst").item(0);
						logNormConst.put(alg, new Double(Double.parseDouble(nc.getAttribute("logValue"))));
					} else {
						logNormConst.put(alg, new Double(Double.NaN));
					}
					if (a_alg.hasAttribute("iterations")) {
						algIterations.get(alg)[i] = Integer.parseInt(a_alg.getAttribute("iterations"));
					} else {
						algIterations.get(alg)[i] = 0;
					}
				}
			} else {
				SolverAlgorithm alg = SolverAlgorithm.EXACT;

				if (i == 0) {
					queueLen.put(alg, new double[stations][classes][iterations]);
					throughput.put(alg, new double[stations][classes][iterations]);
					resTimes.put(alg, new double[stations][classes][iterations]);
					util.put(alg, new double[stations][classes][iterations]);
					logNormConst.put(alg, new Double(Double.NaN));
					algIterations.put(alg, new int[iterations]);
				}

				ArrayUtils.copy2to3(loadResultsMatrix(solution, stations, classes, "Number of Customers"), queueLen.get(alg), i);
				ArrayUtils.copy2to3(loadResultsMatrix(solution, stations, classes, "Throughput"), throughput.get(alg), i);
				ArrayUtils.copy2to3(loadResultsMatrix(solution, stations, classes, "Residence time"), resTimes.get(alg), i);
				ArrayUtils.copy2to3(loadResultsMatrix(solution, stations, classes, "Utilization"), util.get(alg), i);
				logNormConst.put(alg, new Double(Double.NaN));
				algIterations.get(alg)[i] = 0;
			}
		}
		return true;
	}

	public double[][] loadResultsMatrix(Element base, int len1, int len2, String res) {
		//matrix of results
		double[][] arr = new double[len1][len2];

		if (base.getElementsByTagName("stationresults").getLength() != len1) {
			return null;
		}

		for (int i = 0; i < len1; i++) {
			Element s_res = (Element) base.getElementsByTagName("stationresults").item(i);
			for (int c = 0; c < len2; c++) {
				Element n_cls = (Element) s_res.getElementsByTagName("classresults").item(c);

				NodeList measure_list = n_cls.getElementsByTagName("measure");
				Element measure;
				String value = null;

				for (int m = 0; m < measure_list.getLength(); m++) {
					measure = (Element) measure_list.item(m);
					//Below IF clause is added for backward compatibility of the Perf Index : Number of customers
					//as previously it was known as Queue Length.
					if (res.equalsIgnoreCase("Number of Customers")) {
						if (measure.getAttribute("measureType").equalsIgnoreCase("Queue length")) {
							res = "Queue length";
						}
					}
					if (measure.getAttribute("measureType").equalsIgnoreCase(res)) {
						//it is the measure we are searching for
						value = measure.getAttribute("meanValue");
						break;
					}
				}

				//Element r = (Element) n_cls.getElementsByTagName(res).item(0);
				//String value = r.getFirstChild().getNodeValue();

				if (value != null) {
					arr[i][c] = Double.parseDouble(value);
				} else {
					arr[i][c] = 0.0;
				}

			}
		}
		return arr;
	}

	//methods for aggregate results retrieval

	/**Returns per-class aggregate for throughput*/
	public double[][] getPerClassX(SolverAlgorithm alg) {
		if (throughput == null) {
			return null;
		}
		double[][] retVal = new double[classes][iterations];
		double[][][] throughput = this.throughput.get(alg);
		// Scans for every iteration (what if analysis)
		for (int k = 0; k < iterations; k++) {
			//scan columns to get one value per column
			for (int i = 0; i < classes; i++) {
				//throughput is ratio of specific throughput on specific num of visits
				if (classTypes[i] == CLASS_OPEN) {
					//retVal[i][k] = throughput[0][i][k] / visits[0][i];
					retVal[i][k] = classData[i];
				} else {
					// Added by Cerotti
					retVal[i][k] = throughput[referenceStation[i]][i][k];
				}
			}
		}
		return retVal;
	}

	/**Returns per-station aggregate for throughput*/
	public double[][] getPerStationX(SolverAlgorithm alg) {
		if (throughput == null) {
			return null;
		}
		double[][] retVal = new double[stations][iterations];
		double[][][] throughput = this.throughput.get(alg);
		// Scans for every iteration (what if analysis)
		for (int k = 0; k < iterations; k++) {
			for (int i = 0; i < stations; i++) {
				retVal[i][k] = 0;
				for (int j = 0; j < classes; j++) {
					retVal[i][k] += throughput[i][j][k];
				}
			}
		}
		return retVal;
	}

	/**Returns global aggregate for throughput*/
	public double[] getGlobalX(SolverAlgorithm alg) {
		if (throughput == null) {
			return null;
		}
		double[] retVal = new double[iterations];
		double[][] xClassAggs = getPerClassX(alg);
		// Scans for every iteration (what if analysis)
		for (int k = 0; k < iterations; k++) {
			retVal[k] = 0;
			for (int i = 0; i < classes; i++) {
				retVal[k] += xClassAggs[i][k];
			}
		}
		return retVal;
	}

	/**Returns per-class aggregate for queue length*/
	public double[][] getPerClassQ(SolverAlgorithm alg) {
		if (queueLen == null) {
			return null;
		}
		double[][] retVal = new double[classes][iterations];
		double[][][] queueLen = this.queueLen.get(alg);
		// Scans for every iteration (what if analysis)
		for (int k = 0; k < iterations; k++) {
			for (int i = 0; i < classes; i++) {
				retVal[i][k] = 0;
				for (int j = 0; j < stations; j++) {
					retVal[i][k] += queueLen[j][i][k];
				}
			}
		}
		return retVal;
	}

	/**Returns per-station aggregate for queue length*/
	public double[][] getPerStationQ(SolverAlgorithm alg) {
		if (queueLen == null) {
			return null;
		}
		double[][] retVal = new double[stations][iterations];
		double[][][] queueLen = this.queueLen.get(alg);
		// Scans for every iteration (what if analysis)
		for (int k = 0; k < iterations; k++) {
			for (int i = 0; i < stations; i++) {
				retVal[i][k] = 0;
				for (int j = 0; j < classes; j++) {
					retVal[i][k] += queueLen[i][j][k];
				}
			}
		}
		return retVal;
	}

	/**Returns global aggregate for queue length*/
	public double[] getGlobalQ(SolverAlgorithm alg) {
		if (queueLen == null) {
			return null;
		}
		double[] retVal = new double[iterations];
		double[][] qClassAggs = getPerClassQ(alg);
		// Scans for every iteration (what if analysis)
		for (int k = 0; k < iterations; k++) {
			retVal[k] = 0;
			for (int i = 0; i < classes; i++) {
				retVal[k] += qClassAggs[i][k];
			}
		}
		return retVal;
	}

	/**Returns per-class aggregate for residence times*/
	public double[][] getPerClassR(SolverAlgorithm alg, boolean with) {
		if (resTimes == null) {
			return null;
		}
		double[][] retVal = new double[classes][iterations];
		double[][][] resTimes = this.getResidTimes(alg);
		// Scans for every iteration (what if analysis)
		for (int k = 0; k < iterations; k++) {
			for (int i = 0; i < classes; i++) {
				retVal[i][k] = 0;
				for (int j = 0; j < stations; j++) {
					if (with || classTypes[i] == CLASS_OPEN || j != referenceStation[i]) {
						retVal[i][k] += resTimes[j][i][k];
					}
				}
			}
		}
		return retVal;
	}

	/**Returns per-station aggregate for response times*/
	public double[][] getPerStationRespT(SolverAlgorithm alg) {
		if (throughput == null) {
			return null;
		}
		if (resTimes == null) {
			return null;
		}
		double[][] retVal = new double[stations][iterations];
		double[][] xClassAggs = getPerClassX(alg);
		double[][][] resTimes = this.getResidTimes(alg);
		double[][] visits = this.getVisits();
		// Scans for every iteration (what if analysis)
		for (int k = 0; k < iterations; k++) {
			for (int i = 0; i < stations; i++) {
				retVal[i][k] = 0;
				double dividend = 0;
				for (int j = 0; j < classes; j++) {
					if (visits[i][j] > 0.0) {
						retVal[i][k] += xClassAggs[j][k] * resTimes[i][j][k] / visits[i][j];
						dividend += xClassAggs[j][k];
					}
				}
				if (dividend > 0) {
					retVal[i][k] /= dividend;
				} else {
					retVal[i][k] = 0;
				}
			}
		}
		return retVal;
	}

	/**Returns per-station aggregate for residence times*/
	public double[][] getPerStationResidT(SolverAlgorithm alg) {
		if (throughput == null) {
			return null;
		}
		if (resTimes == null) {
			return null;
		}
		double[][] retVal = new double[stations][iterations];
		double[][] xClassAggs = getPerClassX(alg);
		double[][][] resTimes = this.getResidTimes(alg);
		// Scans for every iteration (what if analysis)
		for (int k = 0; k < iterations; k++) {
			for (int i = 0; i < stations; i++) {
				retVal[i][k] = 0;
				double dividend = 0;
				for (int j = 0; j < classes; j++) {
					retVal[i][k] += xClassAggs[j][k] * resTimes[i][j][k];
					dividend += xClassAggs[j][k];
				}
				if (dividend > 0) {
					retVal[i][k] /= dividend;
				} else {
					retVal[i][k] = 0;
				}
			}
		}
		return retVal;
	}

	/**Returns system response time*/
	public double[] getGlobalR(SolverAlgorithm alg, boolean with) {
		if (throughput == null) {
			return null;
		}
		if (resTimes == null) {
			return null;
		}
		double[] retVal = new double[iterations];
		double[][] xClassAggs = getPerClassX(alg);		
		double[][] rClassAggs = getPerClassR(alg, with);
		// Scans for every iteration (what if analysis)
		for (int k = 0; k < iterations; k++) {
			retVal[k] = 0;
			double dividend = 0;
			for (int i = 0; i < classes; i++) {
				retVal[k] += xClassAggs[i][k] * rClassAggs[i][k];
				dividend += xClassAggs[i][k];
			}
			if (dividend > 0) {
				retVal[k] /= dividend;
			} else {
				retVal[k] = 0;
			}
		}
		return retVal;
	}

	public double[][] getPerStationU(SolverAlgorithm alg) {
		if (util == null) {
			return null;
		}
		double[][] retVal = new double[stations][iterations];
		double[][][] util = this.util.get(alg);
		// Scans for every iteration (what if analysis)
		for (int k = 0; k < iterations; k++) {
			for (int i = 0; i < stations; i++) {
				retVal[i][k] = 0;
				for (int j = 0; j < classes; j++) {
					retVal[i][k] += util[i][j][k];
				}
			}
		}
		return retVal;
	}

	//Per-class System Power
	public double[][] getPerClassSP(SolverAlgorithm alg, boolean with) {
		if (throughput == null) {
			return null;
		}
		if (resTimes == null) {
			return null;
		}
		double[][] retVal = new double[classes][iterations];
		double[][] xClassAggs = getPerClassX(alg);
		double[][] rClassAggs = getPerClassR(alg, with);
		// Scans for every iteration (what if analysis)
		for (int k = 0; k < iterations; k++) {
			for (int i = 0; i < classes; i++) {
				if (rClassAggs[i][k] > 0) {
					retVal[i][k] = xClassAggs[i][k] / rClassAggs[i][k];
				} else {
					retVal[i][k] = 0;
				}
			}
		}
		return retVal;
	}

	//System Power
	public double[] getGlobalSP(SolverAlgorithm alg, boolean with) {
		if (throughput == null) {
			return null;
		}
		if (resTimes == null) {
			return null;
		}
		double[] retVal = new double[iterations];
		double[] xGlobalAggs = getGlobalX(alg);
		double[] rGlobalAggs = getGlobalR(alg, with);
		for (int k = 0; k < iterations; k++) {
			if (rGlobalAggs[k] > 0) {
				retVal[k] = xGlobalAggs[k] / rGlobalAggs[k];
			} else {
				retVal[k] = 0;
			}
		}
		return retVal;
	}

	//Added by ASHANKA STOP
	/**
	 * This method tells if visits were set or are all unitary (or zero if
	 * corresponding service time is zero). This is used to show correct panel layout
	 * upon loading
	 * @return true iff visits were not set
	 */
	public boolean areVisitsSet() {
		return unitaryVisits;
	}

	//NEW Federico Dall'Orso
	/**Randomizes model's service times and visits.*/
	public void randomizeModelData() {
		double globRate = globalArrRate();
		for (int i = 0; i < serviceTimes.length; i++) {
			for (int j = 0; j < serviceTimes[i].length; j++) {
				for (int k = 0; k < serviceTimes[i][j].length; k++) {
					if (j < classTypes.length) {
						if (classTypes[j] == CLASS_CLOSED) {
							serviceTimes[i][j][k] = MAXRAND * Math.exp(-Math.random() * MAXRANGE);
						} else {
							if (globRate != 0) {
								serviceTimes[i][j][k] = Math.random() * (0.9) / globRate;
							} else {
								serviceTimes[i][j][k] = Math.random();
							}
						}
						serviceTimes[i][j][k]=Math.ceil(serviceTimes[i][j][k]*1000)/1000; // keep 3 decimal digits
					}
				}
			}
		}
		for (int i = 0; i < visits.length; i++) {
			for (int j = 0; j < visits[i].length; j++) {
				visits[i][j] = 1;
			}
		}
		changed = true;
	}

	//calculates global arrival rate for open classes
	private double globalArrRate() {
		double sum = 0;
		for (int i = 0; i < classTypes.length && i < classData.length; i++) {
			if (classTypes[i] == CLASS_OPEN) {
				sum += classData[i];
			}
		}
		return sum;
	}

	//END

	//---- Methods for What-If analysis ---- Bertoli Marco ------------------------------
	/**
	 * Tells if this model includes a what-if analysis
	 * @return true if this model includes a what-if analysis
	 */
	public boolean isWhatIf() {
		return iterations > 1;
	}

	/**
	 * Removes What-if analysis from current model
	 */
	public void removeWhatIf() {
		if (iterations != 1) {
			iterations = 1;
			changed = true;
		}
	}

	/**
	 * Sets the array of values used for what-if analysis
	 * @param values vector with values to be used in iterations of what-if analysis
	 */
	public boolean setWhatIfValues(double[] values) {
		if (whatIfValues == null && values == null) {
			return false;
		}

		if (whatIfValues == null || !Arrays.equals(whatIfValues, values)) {
			whatIfValues = values;
			if (values != null) {
				iterations = values.length;
			} else {
				iterations = 1;
			}
			changed = true;
			resultsOK = false;
			return true;
		}
		return true;
	}

	/**
	 * Sets class used for what-if analysis
	 * @param classNum ordered number of selected class or -1 for every class
	 * @return true if data was changed, false otherwise
	 */
	public boolean setWhatIfClass(int classNum) {
		if (whatIfClass != classNum) {
			whatIfClass = classNum;
			changed = true;
			resultsOK = false;
			return true;
		}
		return false;
	}

	/**
	 * Sets station used for what-if analysis
	 * @param stationNum ordered number of selected station or -1 for every station
	 * @return true if data was changed, false otherwise
	 */
	public boolean setWhatIfStation(int stationNum) {
		if (whatIfStation != stationNum) {
			whatIfStation = stationNum;
			changed = true;
			resultsOK = false;
			return true;
		}
		return false;
	}

	/**
	 * Sets type of what-if analysis
	 * @param type WHAT_IF_ARRIVAL, WHAT_IF_CUSTOMERS, WHAT_IF_MIX, WHAT_IF_DEMANDS
	 * @see ExactConstants
	 * @return true if data was changed, false otherwise
	 */
	public boolean setWhatIfType(String type) {
		if (whatIfType == null && type == null) {
			return false;
		}
		if (whatIfType == null || !whatIfType.equalsIgnoreCase(type)) {
			whatIfType = type;
			changed = true;
			resultsOK = false;
			return true;
		}
		return false;
	}

	/**
	 * Returns type of what-if analysis
	 * @return WHAT_IF_ARRIVAL, WHAT_IF_CUSTOMERS, WHAT_IF_MIX, WHAT_IF_DEMANDS
	 * @see ExactConstants
	 */
	public String getWhatIfType() {
		return whatIfType;
	}

	/**
	 * Returns index of station selected for what-if analysis or -1 if every station is selected
	 * @return index of station selected for what-if analysis or -1 if every station is selected
	 */
	public int getWhatIfStation() {
		return whatIfStation;
	}

	/**
	 * Returns index of class selected for what-if analysis or -1 if every class is selected
	 * @return index of class selected for what-if analysis or -1 if every class is selected
	 */
	public int getWhatIfClass() {
		return whatIfClass;
	}

	/**
	 * Returns the array of values used for what-if analysis
	 * @return the array of values used for what-if analysis
	 */
	public double[] getWhatIfValues() {
		return whatIfValues;
	}

	/**
	 * This method is used to generate a suitable vector of values for what-if analysis.
	 * @param type type of what-if analysis to be performed. (WHAT_IF_ARRIVAL,
	 * WHAT_IF_CUSTOMERS, WHAT_IF_MIX, WHAT_IF_DEMANDS)
	 * @param from initial value
	 * @param to final value
	 * @param iterations expected number of iterations (will be adjusted to be compliant
	 * with specified from and to values in WHAT_IF_CUSTOMERS and WHAT_IF_MIX)
	 * @param ClassRef index of class to be analyzed or -1 for all classes
	 * @param stationRef index of station to be analyzed (only for WHAT_IF_DEMANDS)
	 * @return suitable array of iterations to be performed
	 */
	public double[] generateWhatIfValues(String type, double from, double to, int iterations, int ClassRef, int stationRef) {
		double[] ret, tmp;
		int n = iterations - 1;
		boolean inverted = false; // tells if 'from' and 'to' values were exchanged
		// order from and to values
		double f, t;
		if (from < to) {
			f = from;
			t = to;
		} else if (from > to) {
			f = to;
			t = from;
			inverted = true;
		} else {
			// In the case of overlapping from and to values, returns a single-number array
			return new double[] { from };
		}

		// Avoid 0 arrival rate
		if (type.equals(WHAT_IF_ARRIVAL) && f == 0.0) {
			f = 1e-5;
		}

		// Arrival rate and service Demands: this are really simple.
		if (type.equals(WHAT_IF_ARRIVAL) || type.equals(WHAT_IF_DEMANDS)) {
			ret = new double[iterations];
			for (int i = 0; i <= n; i++) {
				ret[i] = (f * (n - i) + i * t) / n;
			}
		}
		// Number of customers: this is complex because only integer values are allowed.
		else if (type.equals(WHAT_IF_CUSTOMERS)) {
			// Single class
			if (ClassRef >= 0) {
				tmp = new double[iterations];
				int c = 0; // a simple counter to remove duplicates
				for (int i = 0; i < iterations; i++) {
					tmp[c] = Math.rint((f * (n - i) + i * t) / n);
					// Increment counter only if last element was not repeated
					if (c < 1 || tmp[c] != tmp[c - 1]) {
						c++;
					}
				}
				// Pack target array
				ret = new double[c];
				System.arraycopy(tmp, 0, ret, 0, c);
			}
			// Multiclass
			else {
				// An array that will hold number of customers for each closed class.
				int[] customers = new int[classes];
				int c = 0; // counter of closed classes
				for (int i = 0; i < classes; i++) {
					if (classTypes[c] == CLASS_CLOSED) {
						customers[c++] = (int) classData[i];
					}
				}
				// Inverse of Highest Common Factor is the minimum percentage allowed.
				int hcf = hcf(customers, c);
				if (hcf == 0) {
					hcf = 1;
				}
				double step = 1.0 / hcf;
				if (f < step) {
					f = step;
				}
				if (t < f) {
					return new double[] { f };
				}

				c = 0; // counter of created steps
				tmp = new double[iterations];
				for (int i = 0; i < iterations; i++) {
					tmp[c] = Math.rint((f / step * (n - i) + i * t / step) / n);
					// Increment counter only if last element was not repeated
					if (c < 1 || tmp[c] != tmp[c - 1]) {
						c++;
					}
				}

				// Now creates results array
				ret = new double[c];
				for (int i = 0; i < c; i++) {
					ret[i] = tmp[i] * step;
				}
			}
		}
		// Population mix: this is complex because only integer values are allowed.
		else if (type.equals(WHAT_IF_MIX)) {
			int cl2 = -1;
			// Finds second class
			for (int i = 0; i < classes; i++) {
				if (classTypes[i] == CLASS_CLOSED && i != ClassRef) {
					cl2 = i;
					break;
				}
			}

			int N = (int) (classData[ClassRef] + classData[cl2]);

			double step = 1.0 / N;
			if (f < step) {
				f = step;
			}
			if (t > (N - 1) * step) {
				t = (N - 1) * step;
			}
			if (t < f) {
				return new double[] { f };
			}

			int c = 0; // counter of created steps
			tmp = new double[iterations];
			for (int i = 0; i < iterations; i++) {
				tmp[c] = Math.rint((f / step * (n - i) + i * t / step) / n);
				// Increment counter only if last element was not repeated
				if (c < 1 || tmp[c] != tmp[c - 1]) {
					c++;
				}
			}

			// Now creates results array
			ret = new double[c];
			for (int i = 0; i < c; i++) {
				ret[i] = tmp[i] * step;
			}
		} else {
			ret = null;
		}

		// Inverts results if from and to values were exchanged
		if (inverted && ret != null) {
			double[] inv = new double[ret.length];
			for (int i = 0; i < ret.length; i++) {
				inv[ret.length - 1 - i] = ret[i];
			}
			ret = inv;
		}
		return ret;
	}

	/**
	 * This method will recalculate whatif analysis values after the initial values were changed. At first
	 * detects changes, than applies modifications. If what-if analysis is no longer appliable, resets it.
	 */
	public void recalculateWhatifValues() {
		if (whatIfType == null) {
			return;
		}

		HashSet<Integer> closedClasses = new HashSet<Integer>();
		HashSet<Integer> openClasses = new HashSet<Integer>();

		for (int i = 0; i < classTypes.length; i++) {
			if (classTypes[i] == CLASS_OPEN) {
				openClasses.add(new Integer(i));
			} else if (classTypes[i] == CLASS_CLOSED) {
				closedClasses.add(new Integer(i));
			}
		}

		// Checks validity first
		if (classTypes.length <= whatIfClass || stationTypes.length <= whatIfStation) {
			removeWhatIf();
		} else if (WHAT_IF_ARRIVAL.equals(whatIfType)) {
			if (openClasses.size() == 0 || (whatIfClass >= 0 && classTypes[whatIfClass] != CLASS_OPEN)) {
				removeWhatIf();
			}
		} else if (WHAT_IF_CUSTOMERS.equals(whatIfType)) {
			if (closedClasses.size() == 0 || (whatIfClass >= 0 && classTypes[whatIfClass] != CLASS_CLOSED)) {
				removeWhatIf();
			}
		} else if (WHAT_IF_MIX.equals(whatIfType)) {
			if (closedClasses.size() != 2 || (whatIfClass >= 0 && classTypes[whatIfClass] != CLASS_CLOSED)) {
				removeWhatIf();
			}
		} else if (WHAT_IF_DEMANDS.equals(whatIfType)) {
			if (whatIfStation >= 0 && stationTypes[whatIfStation] == STATION_LD) {
				removeWhatIf();
			}
		}

		// If what-if is still valid, updates initial values
		if (whatIfType != null) {
			// Check if class data was changed
			if (whatIfClass >= 0) {
				if ((WHAT_IF_ARRIVAL.equals(whatIfType) || WHAT_IF_CUSTOMERS.equals(whatIfType)) && classData[whatIfClass] != whatIfValues[0]) {
					setWhatIfValues(generateWhatIfValues(whatIfType, classData[whatIfClass], whatIfValues[iterations - 1], iterations, whatIfClass,
							whatIfStation));
				} else if (WHAT_IF_DEMANDS.equals(whatIfType)
						&& serviceTimes[whatIfStation][whatIfClass][0] * visits[whatIfStation][whatIfClass] != whatIfValues[0]) {
					setWhatIfValues(generateWhatIfValues(whatIfType,
							serviceTimes[whatIfStation][whatIfClass][0] * visits[whatIfStation][whatIfClass], whatIfValues[iterations - 1],
							iterations, whatIfClass, whatIfStation));
				} else if (WHAT_IF_MIX.equals(whatIfType)) {
					// Check that no fractionary values are used
					int class2 = -1;
					for (Integer integer : closedClasses) {
						int idx = integer.intValue();
						if (idx != whatIfClass) {
							class2 = idx;
							break;
						}
					}
					for (int i = 0; i < iterations; i++) {
						double fClassVal = whatIfValues[i] * classData[whatIfClass];
						double sClassVal = (1 - whatIfValues[i]) * classData[class2];
						if (Math.abs(fClassVal - Math.rint(fClassVal)) > 1e-8 || Math.abs(sClassVal - Math.rint(sClassVal)) > 1e-8) {
							setWhatIfValues(generateWhatIfValues(whatIfType, 0, 1, iterations, whatIfClass, whatIfStation));
							break;
						}

					}
				}
			}
		}
	}

	/**
	 * Helper method that finds Highest Common Factor in a given array of integer values.
	 * @param values array of integer values. MUST have at least 2 elements
	 * @param len number of elements to be considered in input array (must be at least 2)
	 * @return found hcf
	 */
	private static int hcf(int[] values, int len) {
		int min, max, tmp;
		min = values[0];

		for (int i = 1; i < len; i++) {
			// Finds minimum value between min (previous hcf) and values[i]
			if (values[i] > min) {
				max = values[i];
			} else {
				max = min;
				min = values[i];
			}
			tmp = max % min;
			while (tmp > 0) {
				max = min;
				min = tmp;
				tmp = max % min;
			}
			// At this point 'min' holds the hcf value
		}
		return min;
	}

	/**
	 * This function will check if one or more resources are in saturation. This will
	 * consider each iteration of what-if analysis if present.
	 * @return NO_SATURATION if everything is okay, SATURATION if a class saturation is
	 * detected with specified parameters and SATURATION_WHATIF if a saturation will be caused
	 * by whatif values.
	 */
	public int checkSaturation() {
		// Checks saturation without what-if analysis
		if (checkForSaturation(classData, visits, serviceTimes, stationServers)) {
			return SATURATION;
		}
		if (isWhatIf()) {
			double maxValue = whatIfValues[iterations - 1];
			// Checks if values are inverted
			if (whatIfValues[0] > maxValue) {
				maxValue = whatIfValues[0];
			}

			// What if arrival rates
			if (whatIfType.equals(WHAT_IF_ARRIVAL)) {
				double[] newClassData = (double[]) classData.clone();
				// Change arrival rate of a single class only
				if (whatIfClass >= 0) {
					newClassData[whatIfClass] = maxValue;
				}
				// Change arrival rate of all open classes
				else {
					for (int i = 0; i < classes; i++) {
						if (classTypes[i] == ExactConstants.CLASS_OPEN) {
							newClassData[i] *= maxValue;
						}
					}
				}
				if (checkForSaturation(newClassData, visits, serviceTimes, stationServers)) {
					return SATURATION_WHATIF;
				}
			}
			// What if service demands
			else if (whatIfType.equals(WHAT_IF_DEMANDS)) {
				double[][][] newServiceTimes = (double[][][]) serviceTimes.clone();
				double[][] newVisits = (double[][]) visits.clone();

				// Change service demands of a LI station for a single (open) class only
				if (whatIfClass >= 0 && classTypes[whatIfClass] == CLASS_OPEN) {
					newServiceTimes[whatIfStation][whatIfClass][0] = maxValue;
					newVisits[whatIfStation][whatIfClass] = 1;
				}
				// Change service demands of a LI station for all (open) classes
				else {
					for (int i = 0; i < classes; i++) {
						if (classTypes[i] == ExactConstants.CLASS_OPEN) {
							newServiceTimes[whatIfStation][i][0] *= maxValue;
						}
					}
				}
				if (checkForSaturation(classData, newVisits, newServiceTimes, stationServers)) {
					return SATURATION_WHATIF;
				}
			}
		}
		return NO_SATURATION;
	}

	public static final int NO_SATURATION = 0;
	public static final int SATURATION = 1;
	public static final int SATURATION_WHATIF = 2;

	/**
	 * Checks current model for saturation, given arrival rates for customer classes
	 * @param classData arrival rates for customer classes
	 * @param visits number of visits for station
	 * @param serviceTimes service times for station
	 * @param stationServers number of servers for each station
	 * @return true if model will saturate, false otherwise
	 */
	private boolean checkForSaturation(double[] classData, double[][] visits, double[][][] serviceTimes, int[] stationServers) {
		for (int i = 0; i < stations; i++) {
			if (stationTypes[i] == STATION_DELAY) {
				//delay station: do not check saturation
				continue;
			}

			//utiliz is the aggregate utilization for station j
			double utiliz = 0;
			for (int j = 0; j < classes; j++) {
				//consider only open classes
				if (classTypes[j] == CLASS_OPEN) {
					utiliz += classData[j] * visits[i][j] * serviceTimes[i][j][0];
				}
			}
			if (utiliz >= stationServers[i]) {
				return true;
			}
		}
		//there are no stations in saturation
		return false;
	}
	//-----------------------------------------------------------------------------------

	/**
	 * Checks current model for reference station consistency: all the visits of closed class in the reference station must be different from zero
	 * @return true if model is consistent, false otherwise
	 */
	public boolean checkVisitReferenceStation() {
		for (int j = 0; j < classes; j++) {
			if (classTypes[j] == CLASS_CLOSED) { // for all closed classes
				if (visits[referenceStation[j]][j]==0) // check that visits in reference station must be different from zero
					return false;
			}			
		}
		return true;
	}

	/**
	 * Checks current model for reference station consistency: the reference station must be the same for all closed classes
	 * @return true if model is consistent, false otherwise
	 */
	public boolean checkClassesReferenceStation() {
		int classindex=-1;		
		for (int j = 0; j < classes; j++) {
			if (classTypes[j] == CLASS_CLOSED) { // for all closed classes
				if (classindex==-1)
					classindex= referenceStation[j]; // store the first closed class index
				else if (classindex!= referenceStation[j]) // all other closed classes must coincide with the first one
					return false;									
			}			
		}
		return true;
	}

}
