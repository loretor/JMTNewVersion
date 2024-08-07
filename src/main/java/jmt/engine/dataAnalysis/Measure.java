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

package jmt.engine.dataAnalysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import jmt.common.exception.NetException;
import jmt.engine.NodeSections.Enabling;
import jmt.engine.NodeSections.Firing;
import jmt.engine.QueueNet.ForkJob;
import jmt.engine.QueueNet.GlobalJobInfoList;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NetSystem;
import jmt.engine.QueueNet.NodeSection;
import jmt.engine.QueueNet.QueueNetwork;
import jmt.engine.QueueNet.SimConstants;
import jmt.engine.log.JSimLogger;

import org.apache.commons.math3.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements a measure object.
 *
 * @author Federico Granata (modified by Stefano Omini) (modified by Bertoli Marco)
 * @author Bertoli Marco (new Data Analyzer, bugfixed scaling)
 */
public class Measure {

	/**This is the object which receives the collected samples, computes the mean value
	 and determines when the simulation must be stopped (confidence interval reached or
	 too many analyzed samples*/
	protected DynamicDataAnalyzer analyzer;

	/** name of the measure*/
	private String name;

	/** becomes true only when the analyzer has finished its computation*/
	private boolean finish;

	/** True, if each measure should be sent on output */
	private boolean verbose;

	/** max precision percentage reached */
	private double maxPrecisionPercentage;

	/** A measure output object is used to print a measure values */
	private MeasureOutput output;

	private JSimLogger logger = JSimLogger.getLogger(JSimLogger.STD_LOGGER);

	//these informations were already contained in the SimMeasure class, but were no longer
	//available in the Measure object, for this reason they have been replicated in the Measure
	//object. The info are stored with the measureTarget(...) method.

	//the node of the queue network on which this measure is computed
	private String nodeName;
	//the job class this measure refers to
	private String jobClassName;
	//the measure type
	private int measureType;

	//the queue network this measure belongs to
	private QueueNetwork network;

	//true if no samples have been received
	//in this case the measure should be forced to end, otherwise
	//the simulation would not stop
	private boolean noSamplesTest = false;

	//true if measure has been aborted
	private boolean aborted = false;

	/**
	 * deadState is used to check if a measure is in a dead state (so does not receive
	 * any more samples). A measure is marked as dead when it reaches maxDeadState.
	 */
	private int deadState = 0;
	private int maxDeadState = 2;

	//-------SCALE FACTOR FOR RESIDENCE TIME MEASURES------------//

	// due to its architecture, JSIM correctly computes the
	// residence time for a single visit,
	// not the total residence time for all visits
	// to correct this measure is therefore needed a scale factor
	// (the visit ratio, i.e. the mean number of accesses at a resource)

	// the other types of measures do not need any scaling, so
	// the scale factor remains equal to 1

	double scaleFactor = 1.0;

	Set<Pair<NetNode, JobClass>> collectedPairs = null;

	protected double lastIntervalAvgValue;

	protected double simulationTime;

	protected double lastSampleWeight;

	protected double lastWeight;

	//-------end SCALE FACTOR FOR RESIDENCE TIME MEASURES------------//

	/** Creates a new instance of measure class.
	 * @param Name name of the measure.
	 * @param alfa    the quantile required for the confidence interval
	 * @param precision   indicator of maximum amplitude of confidence interval
	 *                      (precision = maxamplitude / mean)
	 * @param maxSamples  maximum number of data to be analyzed
	 * @param Verbose   True, if each measure should be sent on output
	 * @param quantiles the quantiles to be computed (null, if no quantiles should be computed)
	 */

	public Measure(String Name, double alfa, double precision, int maxSamples, boolean Verbose, double[] quantiles) {
		this.name = Name;
		if (quantiles != null && quantiles.length > 0) {
			//quantile calculation is requested too
			analyzer = new QuantileDataAnalyzer(alfa, precision, maxSamples, quantiles);
		} else {
			//analyzer = new DynamicDataAnalyzerImpl(alfa, precision, maxSamples);
			analyzer = new NewDynamicDataAnalyzer(alfa, precision, maxSamples);
		}

		finish = false;
		maxPrecisionPercentage = 0.0;
		this.verbose = Verbose;
		createDOM();
	}

	/**
	 * Sets the station, the class, the type of this Measure
	 * @param node the node on which the measure is computed
	 * @param jClass the job class this measure refers to
	 * @param mType the type of measure (see constants defined in class Simulation)
	 */
	public void measureTarget(String node, String jClass, int mType) {
		nodeName = node;
		jobClassName = jClass;
		measureType = mType;
	}

	/**
	 *  Gets the name of the node this measure refers to
	 */
	public String getNodeName() {
		return nodeName;
	}

	/**
	 *  Gets the name of the job class this measure refers to
	 */
	public String getJobClassName() {
		return jobClassName;
	}

	/**
	 *  Gets the measure type (see constants defined in class Simulation)
	 */
	public int getMeasureType() {
		return measureType;
	}

	/**
	 * Gets the QueueNetwork this measure belongs to
	 * @return the QueueNetwork this measure belongs to
	 */
	public QueueNetwork getNetwork() {
		return network;
	}

	/**
	 * Sets the QueueNetwork this measure belongs to
	 * @param network the QueueNetwork this measure belongs to
	 */
	public void setNetwork(QueueNetwork network) {
		this.network = network;
	}

	/**
	 * Returns true if the analysis measure is verbose
	 * @return true if the analysis measure outputs all the samples
	 */
	public boolean getVerbose() {
		return verbose;
	}

	/**
	 * Gets output object.
	 * @return output object.
	 */
	public MeasureOutput getOutput() {
		return output;
	}

	/**
	 * Sets output object.
	 * @param output output object.
	 */
	// When initializing the simulation, add each measure to the queueNetwork,
	// if the <measure verbose="true"> verbose attribute set true, it will create an `MeasureOutput`
	// object, that can be XML, cvs... format. And will setOutput to that object.
	void setOutput(MeasureOutput output) {
		this.output = output;
	}

	/**
	 * Returns true if the analysis measure is successful
	 * @return true if the analysis measure respects all users requests
	 */
	public boolean getSuccess() {
		return analyzer.getSuccess();
	}

	/** Gets measure value.
	 * @return measure value.
	 */
	public double getMeanValue() {
		if (!noSamplesTest) {
			return analyzer.getMean() * scaleFactor;
		} else {
			return 0.0;
		}
	}

	/** Gets measure value: if the confidence requirements have not been
	 * reached, it is returned the value estimated up to that moment.
	 * @return measure value.
	 */
	public double getEstimatedMeanValue() {
		if (!noSamplesTest) {
			return analyzer.estimatedMean() * scaleFactor;
		} else {
			return 0.0;
		}
	}

	/** Gets lower limit.
	 * @return Lower limit.
	 */
	public double getLowerLimit() {
		if (analyzer.isZero()) {
			return 0.0;
		}

		double lower = analyzer.getMean() - analyzer.getConfInt();
		if (analyzer.getConfInt() == 0.0) {
			return 0.0;
		}

		if (lower > 0.0) {
			return lower * scaleFactor;
		} else {
			return 0.0;
		}
	}

	/** Gets upper limit.
	 * @return Upper limit.
	 */
	public double getUpperLimit() {
		if (analyzer.isZero()) {
			return analyzer.getNullMeasure_upperLimit();
		}

		double upper = analyzer.getMean() + analyzer.getConfInt();
		if (analyzer.getConfInt() == 0.0) {
			return 0.0;
		}

		return upper * scaleFactor;
	}

	/** Gets last interval average value.
	 * @return Last interval average value.
	 */
	public double getLastIntervalAvgValue() {
		if (lastWeight == 0.0) {
			lastIntervalAvgValue = 0.0;
		} else {
			lastIntervalAvgValue = (lastSampleWeight / lastWeight) * scaleFactor;
		}
		lastWeight = 0.0;
		lastSampleWeight = 0.0;
		return lastIntervalAvgValue;
	}

	/** Returns number of analyzed samples.
	 * @return Samples analyzed.
	 */
	public int getAnalyzedSamples() {
		return analyzer.getSamples();
	}

	/** Returns number of discarded samples.
	 * @return Samples analyzed.
	 */
	public int getDiscardedSamples() {
		return analyzer.getDiscarded();
	}

	/** Returns the maximum number of samples that can be analyzed.
	 * @return Samples analyzed.
	 */
	public int getMaxSamples() {
		return analyzer.getMaxData();
	}

	/** Updates data. This method should be called to add a new sample to the
	 * collected data.
	 * @param sample sample to be added.
	 * @param weight sample weight.
	 * @return True if the computation of this measure has been finished, false otherwise.
	 */

	public synchronized boolean update(double sample, double weight) {
		return update(sample,weight,null, 0);
	}

	public synchronized boolean update(double sample, double weight, String nodeName){
		return update(sample,weight,nodeName, 0);
	}

	public synchronized boolean update(double sample, double weight, String nodeName, int numOfVisits) {
		/*
		The old version has this problem:
		Even if the confidence requirements have been reached, measure is updated until ALL
		confidence intervals have been computed. The MeasureOutput results, on the other hand,
		are no more updated after confidence has been reached for the first time.

		Two possible solutions:
		1. do not update measure after reaching confidence interval
		2. continue updating, but periodically refresh results in MeasureOutput

		At the moment solution 1 is preferred because it saves time and resources of
		the simulation.

		The following if-block is used to skip measure updating phase when the computation
		has already reached the confidence interval.
		 */

		if (finish) {
			//confidence interval computation has been already finished
			//it is not useful to continue updating this measure
			return true;
		}

		// Resets measure dead state
		deadState = 0;
		lastSampleWeight = lastSampleWeight + (sample * weight);
		lastWeight = lastWeight + weight;
		NetSystem netSystem = getNetSystem();
		simulationTime = netSystem.getTime();

		if (analyzer.addSample(sample, weight)) {
			//data analysis finished
			if (!finish) {
				//simulation not finished yet
				finish = true;
				if (output != null) {
					//writes the new sample
					output.write(sample, weight);
					//writes the final measure
					output.finalizeMeasure();
				}
			}
			//simulation already finished

			if (logger.isDebugEnabled()) {
				boolean log_success = analyzer.getSuccess();
				double log_mean = getEstimatedMeanValue();
				logger.debug("Measure " + name + " finished. Mean value: " + log_mean + " Success = " + log_success);
			}

			return true;
		} else if (verbose) {
			//data analysis not finished yet
			if (output != null) {
				//writes the new sample
				output.write(sample, weight);
			}
		}

		if (measureType == SimConstants.RESIDENCE_TIME) {
			scaleMeasureWithVisitRatio(nodeName, numOfVisits);
		}

		return false;
	}

	public NetSystem getNetSystem() {
		return network.getNetSystem();
	}

	/**Gets the name of the measure.
	 * @return name property value.
	 */
	public String getName() {
		return name;
	}

	public DynamicDataAnalyzer getAnalyzer() {
		return analyzer;
	}

	/** Returns analyzed samples percentage.
	 * @return analyzed samples percentage.
	 */
	public double getSamplesAnalyzedPercentage() {
		return (double) analyzer.getSamples() / analyzer.getMaxData();
	}

	/** Returns max precision percentage reached.
	 * @return Max precision percentage reached.
	 */
	public double getMaxPrecisionPercentage() {
		double p;
		p = analyzer.getPrecision() * analyzer.getMean() / analyzer.getConfInt();
		if (maxPrecisionPercentage < p) {
			maxPrecisionPercentage = p;
		}
		return maxPrecisionPercentage;
	}

	/**
	 * Has the simulation already finished?
	 * @return true, if the simulation has already finished; false otherwise
	 */
	public boolean hasFinished() {
		return finish;
	}

	/**
	 * gets all requested quantiles
	 *
	 * @return vector of quatiles, null if quantiles computation was not requested
	 * while creating the Measure object. See constructor's parameters.
	 */
	public double[] getQuantileResults() {
		double[] results;
		if (analyzer instanceof QuantileDataAnalyzer) {
			results = ((QuantileDataAnalyzer) analyzer).getQuantiles();
		} else {
			results = null;
		}
		return results;
	}

	/**
	 * Creates a DOM (Document Object Model) <code>Document<code>.
	 */
	private void createDOM() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			//data is a Document
			Document data = builder.newDocument();
			Element root = data.createElement("measure");
			root.setAttribute("name", name);
			root.setAttribute("meanValue", "null");
			root.setAttribute("upperBound", "null");
			root.setAttribute("lowerBound", "null");
			root.setAttribute("progress", Double.toString(getSamplesAnalyzedPercentage()));
			root.setAttribute("data", "null");
			root.setAttribute("finished", "false");
			root.setAttribute("discarded", "0");
			root.setAttribute("precision", Double.toString(analyzer.getPrecision()));
			root.setAttribute("maxSamples", Double.toString(getMaxSamples()));
			data.appendChild(root);
		} catch (FactoryConfigurationError factoryConfigurationError) {
			factoryConfigurationError.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates the sample job.
	 * @param sampleJob sample job.
	 */
	public void updateSampleJob(Job sampleJob) {
		updateSampleJob(sampleJob,null);
	}

	public void updateSampleJob(Job sampleJob, String nodeName) {
		if (collectedPairs == null) {
			collectedPairs = new HashSet<Pair<NetNode, JobClass>>();
		}
		if(nodeName == null){
			nodeName = this.nodeName;
		}

		NetNode node = network.getNode(nodeName);
		JobClass jobClass = network.getJobClass(jobClassName);
		Pair<NetNode, JobClass> pair = null;
		if (sampleJob instanceof ForkJob) {
			pair = new Pair<NetNode, JobClass>(node, ((ForkJob) sampleJob).getOriginalJob().getJobClass());
		} else {
			pair = new Pair<NetNode, JobClass>(node, jobClass);
		}
		collectedPairs.add(pair);
	}

	/**
	 * JSIM architecture does not allow to correctly compute residence time for
	 * all visits.
	 * for this reason a scaling operation is needed.
	 * the scale factor is the mean number of accesses to the resource (i.e.
	 * the visit ratio = number of accesses to this station / number of
	 * accesses to the reference station)
	 */
	private void scaleMeasureWithVisitRatio(String nodeName, int numOfVisits) {
		scaleFactor = 1.0;
		try {
			if(nodeName == null){
				nodeName = this.nodeName;
			}
			NetNode node = network.getNode(nodeName);
			NodeSection inputSection = node.getSection(NodeSection.INPUT);
			NodeSection outputSection = node.getSection(NodeSection.OUTPUT);
			JobClass jobClass = network.getJobClass(jobClassName);
			GlobalJobInfoList netJobsList = network.getJobInfoList();

			if (jobClass != null) {
				int localVisits = 0;
				if (inputSection instanceof Enabling && outputSection instanceof Firing) {
					localVisits = Math.max(
							inputSection.getIntSectionProperty(NodeSection.PROPERTY_ID_ARRIVING_JOBS, jobClass),
							outputSection.getIntSectionProperty(NodeSection.PROPERTY_ID_DEPARTING_JOBS, jobClass));
				} else {
					localVisits = inputSection.getIntSectionProperty(NodeSection.PROPERTY_ID_ARRIVING_JOBS, jobClass);
				}

				int globalVisits = 0;
				Map<Set<Pair<NetNode, JobClass>>, Integer> visitCountPerChain = netJobsList.getVisitCountPerChain();
				Map<Pair<NetNode, JobClass>, Set<Pair<NetNode, JobClass>>> pairChainMap = netJobsList.getPairChainMap();
				for (Pair<NetNode, JobClass> pair : collectedPairs) {
					Set<Pair<NetNode, JobClass>> chain = pairChainMap.get(pair);
					globalVisits += visitCountPerChain.get(chain).intValue();
				}

				if (globalVisits > 0) {
					if(numOfVisits > 0){
						scaleFactor = (double) numOfVisits / globalVisits;
					}else {
						scaleFactor = (double) localVisits / globalVisits;
					}
				}
			} else {
				int localVisitsSum = 0;
				if (inputSection instanceof Enabling && outputSection instanceof Firing) {
					localVisitsSum = Math.max(
							inputSection.getIntSectionProperty(NodeSection.PROPERTY_ID_ARRIVING_JOBS),
							outputSection.getIntSectionProperty(NodeSection.PROPERTY_ID_DEPARTING_JOBS));
				} else {
					localVisitsSum = inputSection.getIntSectionProperty(NodeSection.PROPERTY_ID_ARRIVING_JOBS);
				}

				int globalVisitsSum = 0;
				Integer totalVisitCount = netJobsList.getTotalVisitCount();
				globalVisitsSum = totalVisitCount.intValue();

				if (globalVisitsSum > 0) {
					if(numOfVisits > 0){
						scaleFactor = (double) numOfVisits / globalVisitsSum;
					}else {
						scaleFactor = (double) localVisitsSum / globalVisitsSum;
					}
				}
			}
		} catch (NetException ne) {
			logger.error("Error in computing visit ratio.");
			logger.error(ne);
		}
	}

	//******************ABORT**********************//

	public synchronized boolean abortMeasure() {
		if (finish) {
			//measure already finished
			//nothing to do
			return false;
		} else {
			//abort measure
			aborted = true;
			finish = true;
			//stops analyzer
			stopMeasure();
		}
		return true;
	}

	// --- Dead Measures Test - Bertoli Marco --------------------------------------------
	/**
	 * This method will check if a measure is dead i.e. no samples are received
	 * for a long period (so the measure will probably be zero). This is needed to
	 * abort measures in stations that are not reachable after initial transient
	 * @return true iff measure has been marked as dead and was stopped
	 */
	public boolean testDeadMeasure() {
		deadState++;
		if (deadState > maxDeadState) {
			stop_NoSamples();
			return true;
		}
		return false;
	}

	// -----------------------------------------------------------------------------------

	//*****************NO SAMPLES TEST*******************//

	public boolean receivedNoSamples() {
		return noSamplesTest;
	}

	public void stop_NoSamples() {
		finish = true;
		noSamplesTest = true;
		//stop measure with success
		stopMeasure(true);
	}

	protected void stopMeasure(boolean success) {
		//stops measure
		analyzer.stopMeasure(success);

		//writes measure output on log
		if (logger.isDebugEnabled()) {
			double log_mean = getEstimatedMeanValue();
			boolean log_success = analyzer.getSuccess();
			logger.debug("Measure " + name + " finished. Mean value: " + log_mean + " Success = " + log_success);
		}
	}

	protected void stopMeasure() {
		boolean success = analyzer.getSuccess();
		//stops measure
		analyzer.stopMeasure(success);

		//writes measure output on log
		if (logger.isDebugEnabled()) {
			double log_mean = getEstimatedMeanValue();
			boolean log_success = analyzer.getSuccess();
			logger.debug("Measure " + name + " finished. Mean value: " + log_mean + " Success = " + log_success);
			if (output != null) {
				output.finalizeMeasure();
			}
		}
	}

	public boolean hasBeenAborted() {
		return aborted;
	}

	/**
	 * Set simulation parameters for this measure
	 * @param param the simulation parameters to consider
	 */
	public void setSimParameters(SimParameters param) {
		analyzer.setParameters(param);
	}

	public double getSimTime() {
		return simulationTime;
	}

}