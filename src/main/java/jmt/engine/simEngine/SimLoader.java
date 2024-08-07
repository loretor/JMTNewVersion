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

package jmt.engine.simEngine;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import jmt.common.exception.IncorrectDistributionParameterException;
import jmt.common.exception.LoadException;
import jmt.common.xml.XSDSchemaLoader;
import jmt.engine.NetStrategies.ForkStrategy;
import jmt.engine.NetStrategies.RoutingStrategies.ClassSwitchRoutingStrategy;
import jmt.engine.NetStrategies.RoutingStrategies.EmpiricalStrategy;
import jmt.engine.NetStrategies.RoutingStrategies.LoadDependentRoutingParameter;
import jmt.engine.NetStrategies.ServiceStrategies.LDParameter;
import jmt.engine.NetStrategies.ServiceStrategies.ServiceTimeStrategy;
import jmt.engine.NodeSections.*;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NodeSection;
import jmt.engine.dataAnalysis.InverseMeasure;
import jmt.engine.dataAnalysis.Measure;
import jmt.engine.dataAnalysis.SimParameters;
import jmt.engine.random.AbstractDistribution;
import jmt.engine.random.Burst;
import jmt.engine.random.Distribution;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * This class contains the methods to load the DOM description of a
 * queueing network model from a XML file and then to create a Simulation object
 * from that description.
 *
 * For each node, all parameters and sub-parameters are loaded using the suitable constructors.
 *
 * @author Federico Granata, Stefano Omini, Bertoli Marco
 * @version 26-ago-2003 14.23.27
 *       
 * Modified by Ashanka (May 2010): 
 * Patch: Multi-Sink Perf. Index 
 * Description: Added new Performance index for capturing 
 * 				1. global response time (ResponseTime per Sink)
 *              2. global throughput (Throughput per Sink)
 *              each sink per class.
 */
public class SimLoader {

	//used for debug purposes
	private static final boolean DEBUG = false;

	//represents the entire XML document. it is the root
	//of the document tree, and provides the primary access to the document's data
	Document document;

	//customer class array
	private JobClass[] jobClasses;
	// simulation object created by this loader
	private Simulation sim;

	//path of the xml file containing the sim model
	private String simModelPath;

	/**
	 * Creates a Simulation object, loading all the model definition from the
	 * passed xml file
	 *
	 * @param xmlPath the <em>absolute</em> path of the xml model definition
	 */
	public SimLoader(String xmlPath) throws IOException, LoadException {
		simModelPath = xmlPath;
		InputStream is = new BufferedInputStream(new FileInputStream(xmlPath));
		//loads the model and creates a Simulation object
		load(is);
	}

	private void load(InputStream is) throws LoadException {
		if (is == null) {
			throw new LoadException("File not Found");
		}
		InputSource inputSource = new InputSource(is);
		//create a parser
		DOMParser parser = new DOMParser();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);

		try {
			// turn on schema validation ( note need to set both sax and dom validation )
			parser.setFeature("http://xml.org/sax/features/validation", true);
			parser.setFeature("http://apache.org/xml/features/validation/schema", true);
			parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);

			//NEW
			//TODO: set the schema to do parsing
			String externalSchemaLocation = XSDSchemaLoader.loadSchema(XSDSchemaLoader.JSIM_MODEL_DEFINITION);
			parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", externalSchemaLocation);
			//end NEW

			try {
				//document parsing
				parser.parse(inputSource);
			} catch (FileNotFoundException e) {
				throw new LoadException("Problems while parsing", e);
			}

			//get the w3c document
			document = parser.getDocument();
			if (DEBUG) {
				System.out.println(" created document");
			}
			//gets root - node name = 'sim'
			Element root = document.getDocumentElement();
			if (DEBUG) {
				System.out.println("root = " + root.getAttribute("name"));
			}

			//recovers the name of the simulation & creates a getLog with the same
			//name
			if (root.getNodeName() == null) {
				throw new LoadException("Problems loading");
			} else if (!root.getNodeName().equalsIgnoreCase("sim")) {
				throw new LoadException("Problems loading");
			}

			//OLD
			//sim = new Simulation(root.getAttribute("name"), root.getAttribute("debug").equals("true"));

			//NEW
			//@author Stefano Omini
			//default values
			long seed = -1;
			String simName = "";

			if (root.hasAttribute("name")) {
				simName = root.getAttribute("name");
			}

			//variable debug is no longer USED

			if (root.getAttribute("seed") != "") {
				seed = Long.parseLong(root.getAttribute("seed"));
			}

			if (simName.equalsIgnoreCase("")) {
				//NEW
				//@author Stefano Omini
				//no name specified: uses current time as name
				String datePattern = "yyyyMMdd_HHmmss";
				SimpleDateFormat formatter = new SimpleDateFormat(datePattern);

				Date today = new Date();
				String todayString = formatter.format(today);

				simName = "JSIM_" + todayString;

				//OLD
				//sim = new Simulation(seed, null, debug);
				sim = new Simulation(seed, simName);
				//end NEW
			} else {
				//OLD
				//sim = new Simulation(seed, simName, debug);
				sim = new Simulation(seed, simName);
			}

			sim.setXmlSimModelDefPath(simModelPath);
			//end NEW

			//-------------- SIM PARAMETERS -------------------//

			//TODO: code to set sim parameters

			// Create a class SimParameter, whose parameters will be shared by all
			// dynamic data analyzer in order to compute confidence intervals.
			// For example, number of batches, batch size, ecc..

			//this constructor will use default values
			SimParameters simParam = new SimParameters();

		//TODO: here I should put blocks like if (has attribute("batch")) then set(..) etc
//once added in the xml schema, they must be read and
//inserted with the respective set methods

// {......}

//TODO: finished the part with parsing and set of attributes, put this method
//(which for the moment is limited to setting the default values)

			//sets the reference in sim object
			sim.setSimParameters(simParam);

			//gets the default value of maxSamples
			//(max number of samples for each measure)
			int maxSamples = simParam.getMaxSamples();
			//gets the default value of minSamples
			//(min number of samples for each measure)
			int minSamples = simParam.getMinSamples();
			//gets the default value of maxSimulatedTime
			double maxSimulatedTime = simParam.getMaxSimulatedTime();
			//gets the default value of maxProcessedEvents
			int maxProcessedEvents = simParam.getMaxProcessedEvents();
			//gets the default value of disableStatisticStop
			boolean disableStatisticStop = simParam.isDisableStatisticStop();

			// Gets the timestamp value
			simParam.setTimestampValue(Long.toString(System.currentTimeMillis()));

			//-------------- end SIM PARAMETERS -------------------//

			// Read maxSamples if specified
			if (root.getAttribute("maxSamples") != "") {
				maxSamples = Integer.parseInt(root.getAttribute("maxSamples"));
				simParam.setMaxSamples(maxSamples);
			}

			// Read minSamples if specified
			if (root.getAttribute("minSamples") != "") {
				minSamples = Integer.parseInt(root.getAttribute("minSamples"));
				simParam.setMinSamples(minSamples);
			}

			// Read maxSimulatedTime if specified
			if (root.getAttribute("maxSimulated") != "") {
				maxSimulatedTime = Double.parseDouble(root.getAttribute("maxSimulated"));
				simParam.setMaxSimulatedTime(maxSimulatedTime);
			}

			// Read maxProcessedEvents if specified
			if (root.getAttribute("maxEvents") != "") {
				maxProcessedEvents = Integer.parseInt(root.getAttribute("maxEvents"));
				simParam.setMaxProcessedEvents(maxProcessedEvents);
			}

			// Disables confidence interval as stopping criteria
			if (root.hasAttribute("disableStatisticStop")) {
				disableStatisticStop = Boolean.parseBoolean(root.getAttribute("disableStatisticStop"));
				simParam.setDisableStatisticStop(disableStatisticStop);
			}

			// MF08 0.7.4  Michael Fercu (Bertoli Marco) -- re-defines global logger attributes
			// for the purpose of passing them to the Logger constructor
			if (root.hasAttribute("logPath")) {
				String temp_lp = root.getAttribute("logPath");
				simParam.setLogPath(temp_lp);
			}
			if (root.hasAttribute("logDelimiter")) {
				String temp_ld = root.getAttribute("logDelimiter");
				simParam.setLogDelimiter(temp_ld);
			}
			if (root.hasAttribute("logDecimalSeparator")) {
				String temp_ld = root.getAttribute("logDecimalSeparator");
				simParam.setLogDecimalSeparator(temp_ld);
			}
			if (root.hasAttribute("logReplaceMode")) {
				String temp_lr = root.getAttribute("logReplaceMode");
				simParam.setLogReplaceMode(temp_lr);
			}
			//END MF08

			//FIXME read measure logging attributes here...

			//Returns a NodeList of all the Elements with a given tag name in the order in which they
			//are encountered in a preordering traversal of the Document tree.
			NodeList nodeList = root.getElementsByTagName("node");
			NodeList classList = root.getElementsByTagName("userClass");
			NodeList measureList = root.getElementsByTagName("measure");
			NodeList connectionList = root.getElementsByTagName("connection");

			//class array creation
			jobClasses = new JobClass[classList.getLength()];
			for (int i = 0; i < classList.getLength(); i++) {
				//OLD
				//jobClasses[i] = new JobClass(((Element) classList.item(i)).getAttribute("name"));

				//NEW
				//@author Stefano Omini
				Element currentJobClass = (Element) classList.item(i);

				//parse class attributes: name, type and priority
				String currentClassName = currentJobClass.getAttribute("name");
				String currentClassType = currentJobClass.getAttribute("type");
				String currentClassPriority = currentJobClass.getAttribute("priority");
				String referenceNode = currentJobClass.getAttribute("referenceSource");
				String currentClassSoftDeadline = currentJobClass.getAttribute("softDeadline");
				String cacheMissClass = currentJobClass.getAttribute("cacheMissClass");
				String cacheHitClass = currentJobClass.getAttribute("cacheHitClass");

				int type, priority;
				double softDeadline;

				if (currentClassType.equalsIgnoreCase("closed")) {
					type = JobClass.CLOSED_CLASS;
				} else {
					type = JobClass.OPEN_CLASS;
				}

				priority = Integer.parseInt(currentClassPriority);
				if (priority < 0) {
					//negative priorities not allowed
					priority = 0;
				}

				softDeadline = currentClassSoftDeadline.isEmpty() ? 0 : Double.parseDouble(currentClassSoftDeadline);

				//add job class
				jobClasses[i] = new JobClass(currentClassName, priority, type, referenceNode);
				//end NEW

				jobClasses[i].setSoftDeadline(softDeadline);

				if (DEBUG) {
					System.out.println("Class " + jobClasses[i].getName() + " created");
				}
			}
			//inserts all JobClasses in the Simulation object
			sim.addClasses(jobClasses);
			if (DEBUG) {
				System.out.println("classes added\n");
			}

			//creates the nodes from xml & adds them to the simulation object
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element node = (Element) nodeList.item(i);
				if (DEBUG) {
					System.out.println("start creation of node = " + node.getAttribute("name"));
				}
				// Process soft deadlines.
				double[] softDeadlinesArr = null;
				if (node.getElementsByTagName("classSoftDeadlines").getLength() != 0) {
					List<Double> softDeadlines = new ArrayList<>();
					NodeList classSoftDeadlinesChildren = node.getElementsByTagName("classSoftDeadlines").item(0).getChildNodes();
					for (int j = 0; j < classSoftDeadlinesChildren.getLength(); j++) {
						Node item = classSoftDeadlinesChildren.item(j);
						if (item.getNodeType() == Node.ELEMENT_NODE) {
							// This is a soft deadline element.
							softDeadlines.add(Double.parseDouble(item.getTextContent()));
						}
					}
					if (softDeadlines.size() > 0) {
						softDeadlinesArr = new double[softDeadlines.size()];
						for (int j = 0; j < softDeadlines.size(); j++) {
							softDeadlinesArr[j] = softDeadlines.get(j);
						}
					}
				}
				double quantaSize = 0.0;
				if (node.getElementsByTagName("quantaSize").getLength() != 0) {
					quantaSize = Double.parseDouble(node.getElementsByTagName("quantaSize").item(0).getTextContent());
				}
				double switchTime = 0.0;
				if (node.getElementsByTagName("quantumSwitchoverTime").getLength() != 0) {
					switchTime = Double.parseDouble(node.getElementsByTagName("quantumSwitchoverTime").item(0).getTextContent());
				}

				//gets list of sections
				NodeList sectionList = node.getElementsByTagName("section");
				NodeSection[] sections = new NodeSection[4];
				//creates all sections (max is 3)
				for (int j = 0; j < sectionList.getLength(); j++) {
					if (DEBUG) {
						System.out.println("    start creation of section = " + ((Element) sectionList.item(j)).getAttribute("className"));
					}
					NodeSection ns = createSection((Element) sectionList.item(j));
					if (DEBUG) {
						System.out.println("    finished creation of " + ((Element) sectionList.item(j)).getAttribute("className") + "\n");
					}
					if (ns instanceof InputSection) {
						if (ns instanceof Queue) {
							((Queue) ns).setSoftDeadlines(softDeadlinesArr);

						}
						sections[0] = ns;
					} else if (ns instanceof ServiceSection) {
						if(ns instanceof PSServer){
							((PSServer) ns).setQuantumSize(quantaSize);
							((PSServer) ns).setSwitchTime(switchTime);
						}
						sections[1] = ns;
					} else if (ns instanceof OutputSection) {
						sections[2] = ns;
					} else {
						throw new LoadException("trying to cast the wrong Class type");
					}
				}

				//adds node.
				sim.addNode(node.getAttribute("name"), (InputSection) sections[0], (ServiceSection) sections[1], (OutputSection) sections[2]);
				if (DEBUG) {
					System.out.println("node added\n");
				}
			}
			if (DEBUG) {
				System.out.println("");
			}

			//adds all connections
			for (int i = 0; i < connectionList.getLength(); i++) {
				Element e = (Element) connectionList.item(i);
				sim.addConnection(e.getAttribute("source"), e.getAttribute("target"));
				if (DEBUG) {
					System.out.println("added connection = " + e.getAttribute("source") + " to " + e.getAttribute("target"));
				}
			}
			if (DEBUG) {
				System.out.println("");
			}

			//adds all measures
			for (int i = 0; i < measureList.getLength(); i++) {
				Element e = (Element) measureList.item(i);

				int measureType = EngineUtils.decodeMeasureType(e.getAttribute("type"));
				//throughput measure requires an InverseMeasure object!!
				if (EngineUtils.isInverseMeasure(measureType)) {
					//throughput measure
					InverseMeasure invMeasure = new InverseMeasure(e.getAttribute("name"), Double.parseDouble(e.getAttribute("alpha")),
							Double.parseDouble(e.getAttribute("precision")), maxSamples, e.getAttribute("verbose").equalsIgnoreCase("true"));

					sim.addMeasure(measureType, e.getAttribute("referenceNode"), invMeasure, e.getAttribute("referenceUserClass"),
							e.getAttribute("nodeType"));
				} else {
					//other measures
					Measure measure = new Measure(e.getAttribute("name"), Double.parseDouble(e.getAttribute("alpha")),
							Double.parseDouble(e.getAttribute("precision")), maxSamples, e.getAttribute("verbose").equalsIgnoreCase("true"), null);

					sim.addMeasure(measureType, e.getAttribute("referenceNode"), measure, e.getAttribute("referenceUserClass"),
							e.getAttribute("nodeType"));
				}

				if (DEBUG) {
					System.out.println("added measure = " + e.getAttribute("name"));
				}
			}
			if (DEBUG) {
				System.out.println("");
			}

			//NEW
			//@author Stefano Omini
			NodeList regionList = root.getElementsByTagName("blockingRegion");

			// Use external method
			loadBlockingRegions(root, regionList);
			if (DEBUG) {
				System.out.println("");
			}
			//end NEW

			//Preloading

			NodeList preloadList = root.getElementsByTagName("preload");
			Element preload = (Element) preloadList.item(0);

			//station names
			String[] stationNames;
			//initial populations [station, class]
			int[][] initialP;

			if (preload != null) {
				//preload has been defined by user
				NodeList stations;
				NodeList initialPops;
				Element station;

				//gets all station elements
				stations = preload.getElementsByTagName("stationPopulations");
				//number of stations to be preloaded
				int stationsNumber = stations.getLength();
				int classNumber = jobClasses.length;

				//create the array of station names to be preloaded
				// (not ALL the stations!!)
				stationNames = new String[stationsNumber];
				//create the population matrix
				initialP = new int[stationsNumber][classNumber];

				//initializes matrix
				for (int s = 0; s < stationsNumber; s++) {
					for (int c = 0; c < classNumber; c++) {
						initialP[s][c] = 0;
					}
				}

				//loop over stations
				for (int s = 0; s < stationsNumber; s++) {
					//current station
					station = (Element) stations.item(s);

					//set station name
					stationNames[s] = station.getAttribute("stationName");

					//retrieves the class initial populations
					initialPops = station.getElementsByTagName("classPopulation");
					int entries = initialPops.getLength();

					for (int c = 0; c < entries; c++) {
						String pop = ((Element) initialPops.item(c)).getAttribute("population");
						String className = ((Element) initialPops.item(c)).getAttribute("refClass");

						int classPosition = findClassPosition(className);
						//sets the value in the correct position (using the class ID)
						initialP[s][classPosition] = Integer.parseInt(pop);
					}
				}

				//copies the preload info into simulation object
				sim.setPreloadEnabled(true);
				sim.setPreload_stationNames(stationNames);
				sim.setPreload_initialPopulations(initialP);
			}
			//end NEW
		} catch (SAXNotRecognizedException e) {
			e.printStackTrace();
		} catch (SAXNotSupportedException e) {
			e.printStackTrace();
		} catch (SAXException sxe) {
			// Error generated during parsing)
			Exception x = sxe;
			if (sxe.getException() != null) {
				x = sxe.getException();
			}
			x.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the Simulation object which has been created by this SimLoader while loading the
	 * queueing network model.
	 * @return The simulation created
	 */
	public Simulation getSim() {
		return sim;
	}

	/**
	 * Creates a nodeName section from a dom description. Each parameter (or array of parameters)
	 * is created using the suitable constructor.
	 *
	 * @param section  dom description of the nodeName section.
	 * @return NodeSection created.
	 */
	// section Element represents the content enclosed by the <Section> tag
	// traverse each parameter node in section，creating the object as the input of the section constructor
	private NodeSection createSection(Element section) throws LoadException {
		//gets all the parameters
		NodeList parameterList = section.getElementsByTagName("parameter");
		try {
			//gets appropriate Class Object
			Class<?> c = Class.forName("jmt.engine.NodeSections." + section.getAttribute("className"));
			if (DEBUG) {
				System.out.println("    class found");
			}
			//creates with Default Constructor
			if (parameterList.getLength() == 0) {
				if (DEBUG) {
					System.out.println("    using default constructor");
				}
				NodeSection ns = (NodeSection) c.newInstance();
				return ns;
			} else {
				//creates the array with all parameters & the array with all their classes
				Object[] initargs = new Object[parameterList.getLength()];
				Class<?>[] parameterTypes = new Class[parameterList.getLength()];
				for (int i = 0; i < parameterList.getLength(); i++) {
					//creates the parameter
					//if (DEBUG) {
					//	System.out.println("parameterList.item(i): " + parameterList.item(i).toString());
					//}
					initargs[i] = createParameter((Element) parameterList.item(i));
					if (initargs[i] != null) {
						//gets the class of the parameter
						parameterTypes[i] = initargs[i].getClass();
						if (DEBUG) {
							//System.out.println("parameterTypes[i]: " + parameterTypes[i].toString());
							System.out.println("        parameter of class = " + parameterTypes[i].getName());
						}
					} else {
						if (!((Element) parameterList.item(i)).getAttribute("array").equals("true")) {
							//the parameter is not an array
							parameterTypes[i] = Class.forName(((Element) parameterList.item(i)).getAttribute("classPath"));
						} else {
							// array parameter
							parameterTypes[i] = Array.newInstance(Class.forName(((Element) parameterList.item(i)).getAttribute("classPath")), 0)
									.getClass();
						}
						if (DEBUG) {
							System.out.println("        parameter of class = " + parameterTypes[i].getName());
						}
					}
				}

				Constructor<?> constr = getConstructor(c, parameterTypes);
				if (DEBUG) {
					System.out.println("    constructor found");
				}
				//creates the Section with the constructor
				NodeSection ns = (NodeSection) constr.newInstance(initargs);
				return ns;
			}
		} catch (ClassNotFoundException e) {
			throw new LoadException("Class of Section Not found", e);
		} catch (InstantiationException e) {
			throw new LoadException("Class of Section cannot be istantiated", e);
		} catch (IllegalAccessException e) {
			throw new LoadException("Class of Section illegal access", e);
		} catch (NoSuchMethodException e) {			
			throw new LoadException("Constructor of Section not found", e);
		} catch (InvocationTargetException e) {
			throw new LoadException("problems with Section constructor", e);
		}
	}

	/**
	 * Creates a parameter object given the corresponding description of the tag.
	 * @param param
	 * @throws jmt.common.exception.LoadException
	 */
	private Object createParameter(Element param) throws LoadException {
		if (!param.getTagName().equals("parameter")) {
			throw new LoadException("trying to use createParameter on a : " + param.getTagName());
		}
		try {
			if (DEBUG) {
				System.out.println("        start creation of parameter = " + param.getAttribute("name"));
			}
			//gets Class Object for the parameter
			String classPath = param.getAttribute("classPath");
			NodeList valueList = XMLParser.getElementsByTagName(param, "value");
			//if value == null the object is a null pointer
			if (valueList.getLength() > 0 && valueList.item(0).getChildNodes().item(0).getNodeValue().equals("null")) {
				if (DEBUG) {
					System.out.println("        parameter null");
				}
				return null;
			}
			Class<?> c = Class.forName(classPath);
			if (DEBUG) {
				System.out.println("        parameter class found = " + classPath);
			}
			//parameter is an array;
			if (param.getAttribute("array").equals("true")) {
				if (DEBUG) {
					System.out.println("        parameter is an array");
				}
				//check if there are child nodes in the array
				if (!param.hasChildNodes()) {
					if (DEBUG) {
						System.out.println("        creates 0 size array");
					}
					return Array.newInstance(c, 0);//return a 0 element array
				}
				//there are 2 situations: the parameter is an array or a group of subparameters defined per class

				//first situation: just an array without classes
				Object[] arrayElements;
				if (XMLParser.getElementsByTagName(param, "refClass").getLength() == 0) {
					//gets list of first level subParameters
					NodeList childList = XMLParser.getElementsByTagName(param, "subParameter");
					//creates an array of the appropriate length
					arrayElements = new Object[childList.getLength()];
					if (DEBUG) {
						System.out.println("        instance a simple array of size " + arrayElements.length);
					}
					//creates all the elements of the array
					for (int i = 0; i < arrayElements.length; i++) {
						if (DEBUG) {
							System.out.println("creating subparemter = " + ((Element) childList.item(i)).getAttribute("name"));
						}
						arrayElements[i] = createSubParameter((Element) childList.item(i));
					}
					//creates a fake array object
					Object parameter = Array.newInstance(c, childList.getLength());
					//copy inside all the elements
					System.arraycopy(arrayElements, 0, parameter, 0, arrayElements.length);
					if (DEBUG) {
						System.out.println("        created parameter");
					}
					return parameter;
				} else {
					//it is a group parameter! more complicated(the other was easy... :P )
					if (DEBUG) {
						System.out.println("        it is group class parameter");
					}
					//creates an array of Object that has enough element to store
					//subParametes for each class.
					arrayElements = new Object[jobClasses.length];
					NodeList chiList = param.getChildNodes();
					ArrayList<String> classVect = new ArrayList<String>();
					//iterates over the childList, it is like this, it has a list (optional)of classRef
					//followed by a subParameter
					for (int i = 0; i < chiList.getLength(); i++) {
						Node n = chiList.item(i);
						if (n.getNodeType() == 1) {
							//if gets a class it add to list of classes
							if (n.getNodeName().equals("refClass")) {
								classVect.add(n.getFirstChild().getNodeValue());
								if (DEBUG) {
									System.out.println("        found class " + n.getFirstChild().getNodeValue());
								}
							} else {
								//gets the position of classes
								int[] positions = new int[classVect.size()];
								for (int j = 0; j < positions.length; j++) {
									positions[j] = findClassPosition(classVect.get(j));
								}
								//gets the subparameter
								for (int j = 0; j < positions.length; j++) {
									if (DEBUG) {
										System.out.println("        creating subParameter " + ((Element) n).getAttribute("name") + " for class "
												+ classVect.get(j));
									}
									arrayElements[positions[j]] = createSubParameter((Element) n);
								}
								//clears the classes vector
								classVect.clear();
							}
						}
					}
					//creates a fake array object
					Object parameter = Array.newInstance(c, jobClasses.length);
					//copy inside all the elements
					System.arraycopy(arrayElements, 0, parameter, 0, arrayElements.length);
					if (DEBUG) {
						System.out.println("        created parameter");
					}
					return parameter;
				}
			}
			//check for default constructor
			if (!param.hasChildNodes()) {
				if (DEBUG) {
					System.out.println("       created with default constructor");
				}
				return c.newInstance();
			}

			//check if it is a leaf node (it has a value & not subparameters)
			if (valueList.getLength() > 0) {
				String value = valueList.item(0).getFirstChild().getNodeValue();
				if (DEBUG) {
					System.out.println("        parameter is a leaf node, value = " + value);
				}
				//needs to get the String constructor
				Object[] initargs = { value };
				Class<?>[] parameterTypes = { initargs[0].getClass() };
				Constructor<?> constr = getConstructor(c, parameterTypes);
				if (DEBUG) {
					System.out.println("        created parameter");
				}
				return constr.newInstance(initargs);
			} else {
				//leaf node but has subparameters
				NodeList childList = XMLParser.getElementsByTagName(param, "subParameter");
				Object[] initargs = new Object[childList.getLength()];
				if (DEBUG) {
					System.out.println("        parameter is a leaf node with subparameter ");
				}
				Class<?>[] paramClasses = new Class[childList.getLength()];
				//creates iteratively all the subparameters
				for (int i = 0; i < childList.getLength(); i++) {
					Element e = (Element) childList.item(i);
					if (DEBUG) {
						System.out.println("            creates subparameter = " + e.getAttribute("name"));
					}
					initargs[i] = createSubParameter(e);
					paramClasses[i] = initargs[i].getClass();
				}
				Constructor<?> constr = getConstructor(c, paramClasses);
				return constr.newInstance(initargs);
			}
		} catch (ClassNotFoundException e) {
			throw new LoadException("Class of Parameter not found", e);
		} catch (NoSuchMethodException e) {
			throw new LoadException("there is not a String Constructor", e);
		} catch (InstantiationException e) {
			throw new LoadException("Class of parameter cannot be instantiated", e);
		} catch (IllegalAccessException e) {
			throw new LoadException("Class of parameter cannot be instantiated", e);
		} catch (InvocationTargetException e) {
			throw new LoadException("Class of parameter cannot be instantiated", e);
		}
	}

	/**
	 * Creates a subParameter given an appropriate Element of a DOM
	 * @param subp
	 * @return
	 * @throws jmt.common.exception.LoadException
	 */
	private Object createSubParameter(Element subp) throws LoadException {
		//gets Class object
		try {
			NodeList valueList = XMLParser.getElementsByTagName(subp, "value");
			//if value == null the object is a null pointer
			if (valueList.getLength() > 0 && valueList.item(0).getChildNodes().item(0).getNodeValue().equals("null")) {
				if (DEBUG) {
					System.out.println("         subParameter null");
				}
				return null;
			}
			Class<?> c = Class.forName(subp.getAttribute("classPath"));
			if (DEBUG) {
				System.out.println("            subparameter class found = " + c.getName());
			}
			//check if the subparameter is an array
			if (subp.getAttribute("array").equals("true")) {
				//array subparameter
				if (DEBUG) {
					System.out.println("            subparameter is an array");
				}
				//check if there are child nodes in the array
				if (!subp.hasChildNodes()) {
					if (DEBUG) {
						System.out.println("            creates 0 size array");
					}
					return Array.newInstance(c, 0);//return a 0 element array
				}
				Object[] arrayElements;
				//gets list of first level subParameters
				NodeList childList = XMLParser.getElementsByTagName(subp, "subParameter");
				//creates an array of the appropriate length
				arrayElements = new Object[childList.getLength()];
				if (DEBUG) {
					System.out.println("            instance a simple array of size " + arrayElements.length);
				}
				//creates all the elements of the array
				for (int i = 0; i < arrayElements.length; i++) {
					if (DEBUG) {
						System.out.println("            creating subparameter = " + ((Element) childList.item(i)).getAttribute("name"));
					}
					arrayElements[i] = createSubParameter((Element) childList.item(i));
				}
				//creates a fake array object
				Object parameter = Array.newInstance(c, childList.getLength());
				//copy inside all the elements
				System.arraycopy(arrayElements, 0, parameter, 0, arrayElements.length);
				if (DEBUG) {
					System.out.println("            created parameter");
				}
				return parameter;
			}
			//check for default constructor
			if (!subp.hasChildNodes()) {
				if (DEBUG) {
					System.out.println("            created with default constructor");
				}
				Object o = c.newInstance();
				if (o instanceof AbstractDistribution) {
					((AbstractDistribution) o).setRandomEngine(sim.getEngine());
				}
				return o;
			}

			//check if it is a leaf node (it has a value & not subparameters)
			if (valueList.getLength() > 0) {
				String value = valueList.item(0).getFirstChild().getNodeValue();
				if (DEBUG) {
					System.out.println("            subParameter is leaf node, value = " + value);
				}
				//needs to get the String constructor
				Object[] initargs = { value };
				Class<?>[] parameterTypes = { initargs[0].getClass() };
				Constructor<?> constr = getConstructor(c, parameterTypes);
				if (DEBUG) {
					System.out.println("            created subParameter");
				}
				Object o = constr.newInstance(initargs);

				return o;
			} else {
				//leaf node but has subparameters
				NodeList childList = XMLParser.getElementsByTagName(subp, "subParameter");
				Object[] initargs = new Object[childList.getLength()];
				if (DEBUG) {
					System.out.println("            subParameter is a leaf node with subparameter ");
				}
				Class<?>[] paramClasses = new Class[childList.getLength()];
				//creates iteratively all the subparameters
				for (int i = 0; i < childList.getLength(); i++) {
					Element e = (Element) childList.item(i);
					if (DEBUG) {
						System.out.println("            creates subparameter = " + e.getAttribute("name"));
					}
					initargs[i] = createSubParameter(e);
					paramClasses[i] = initargs[i].getClass();
				}
				//gets the right constructor
				Constructor<?> constr = getConstructor(c, paramClasses);
				Object o = constr.newInstance(initargs);
				if (o instanceof NetNode && o instanceof Distribution) { //change to burst then add a method to burst to do those two lines
					//add to simsystem
					//call the initialize method
					NetNode n = (NetNode) o;
					SimSystem simSystem = sim.getSimSystem();
					simSystem.add(n);
					n.setSimSystem(simSystem);

					Burst b = (Burst) o; //any distribution will do?
					b.setRandomEngine(sim.getNetSystem().getEngine());
					b.initialize();

					sim.addDistrNetNode((NetNode) o);
				}
				if (o instanceof ForkStrategy) {
					((ForkStrategy) o).setRandomEngine(sim.getEngine());
				}
				if (o instanceof EmpiricalStrategy) {
					((EmpiricalStrategy) o).setRandomEngine(sim.getEngine());	
				}
				if (o instanceof LoadDependentRoutingParameter) {
					((LoadDependentRoutingParameter) o).setRandomEngine(sim.getEngine());
				}
				if (o instanceof LDParameter) {
					((LDParameter) o).setRandomEngine(sim.getEngine());
				}
				if(o instanceof ClassSwitchRoutingStrategy) {
					((ClassSwitchRoutingStrategy) o).setRandomEngine(sim.getEngine());
				}

				return o;
			}
		} catch (ClassNotFoundException e) {
			throw new LoadException("class of subparameter not found", e);
		} catch (InstantiationException e) {
			throw new LoadException("class of subparameter not found", e);
		} catch (IllegalAccessException e) {
			throw new LoadException("class of subparameter not found", e);
		} catch (NoSuchMethodException e) {
			throw new LoadException("class of subparameter not found", e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IncorrectDistributionParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (DEBUG) {
			System.out.println("            creation fake of subparameter");
		}
		return null;
	}

	/**
	 * Gets an appropriate constructor for c given the paramClasses
	 * @param c
	 * @param paramClasses
	 * @return found constructor
	 */
	public Constructor<?> getConstructor(Class<?> c, Class<?>[] paramClasses) throws NoSuchMethodException {
		try {
			return c.getConstructor(paramClasses);
		} catch (NoSuchMethodException e) {
			//the right constructor is not contained.. let search for another
			//one that fits
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		//gets all constructors
		Constructor<?>[] constrs = c.getConstructors();
		for (Constructor<?> constr : constrs) {
			Class<?>[] params = constr.getParameterTypes();
			if (params.length == paramClasses.length) {
				//right number of parameters
				boolean ok = true;
				for (int j = 0; j < params.length && ok; j++) {
					Class<?> param = params[j];
					if (!param.isAssignableFrom(paramClasses[j])) {
						ok = false;
					}
				}
				if (ok) {
					return constr;
				}
			}
		}
		String errorMessage = c.getName() + "." + "<init>(";
		for (Class<?> paramClass : paramClasses) {
			errorMessage += paramClass.getName() + ", ";
		}
		errorMessage += ")";
		throw new NoSuchMethodException(errorMessage);
	}

	/**
	 * Finds the position of the specified class
	 * @param className the name of the class
	 * @return the position if such class exists, -1 otherwise
	 */
	private int findClassPosition(String className) {
		if (jobClasses == null) {
			//classes not loaded yet
			return -1;
		}

		//else search in the array
		JobClass jobClass;
		for (int c = 0; c < jobClasses.length; c++) {
			jobClass = jobClasses[c];
			if (jobClass.getName().equals(className)) {
				//it is the class we are searching for
				return c;
			}
		}
		//no class with the specified name
		return -1;
	}

	/**
	 * Loads blocking region from input XML file
	 * @param root root element of XML file
	 * @param regionList a NodeList data structure with all blocking regions
	 * @throws LoadException if some problems occurs during loading
	 */
	private void loadBlockingRegions(Element root, NodeList regionList) throws LoadException {
		int classNumber = root.getElementsByTagName("userClass").getLength();

		//adds all regions
		for (int i = 0; i < regionList.getLength(); i++) {

			//get i-th region
			Element region = (Element) regionList.item(i);

			//name
			String regionName = region.getAttribute("name");

			if (DEBUG) {
				System.out.println("start adding region = " + regionName);
			}

			//NOT USED
			//String regionType = region.getAttribute("type");

			//--------------REGION NODES----------------//
			//gets the names of the stations contained in the region
			NodeList regionNodesList = region.getElementsByTagName("regionNode");

			String[] stationNames = new String[regionNodesList.getLength()];

			for (int j = 0; j < regionNodesList.getLength(); j++) {
				Element rn = (Element) regionNodesList.item(j);
				stationNames[j] = rn.getAttribute("nodeName");
				if (DEBUG) {
					System.out.println("   region contains node = " + stationNames[j]);
				}
			}

			//---------GLOBAL CONSTRAINT-----------//
			double maxCapacity = -1;

			NodeList globalConstraints = region.getElementsByTagName("globalConstraint");

			if (globalConstraints.item(0) != null) {
				Element globalConstraint = (Element) globalConstraints.item(0);
				maxCapacity = Double.parseDouble(globalConstraint.getAttribute("maxJobs"));
				if (DEBUG) {
					System.out.println("   global constraint = " + Double.toString(maxCapacity));
				}
			} else {
				throw new LoadException("Element \"globalConstraint\" missing...");
			}

			//--------------GLOBAL MEMORY CONSTRAINT-------------//
			double maxMemory = -1;

			NodeList globalMemoryConstraints = region.getElementsByTagName("globalMemoryConstraint");

			if (globalMemoryConstraints.item(0) != null) {
				Element globalMemoryConstraint = (Element) globalMemoryConstraints.item(0);
				maxMemory = Double.parseDouble(globalMemoryConstraint.getAttribute("maxMemory"));
				if (DEBUG) {
					System.out.println("   global memory constraint = " + Double.toString(maxMemory));
				}
			} else {
				throw new LoadException("Element \"globalMemoryConstraint\" missing...");
			}

			//--------------DROP RULES---------------//
			NodeList dropRules = region.getElementsByTagName("dropRules");

			//drop rules (one for each class)
			boolean[] dropThisClass = new boolean[classNumber];
			//init
			if (dropRules.getLength() == 0) {
				//no drop rules specified: use default drop values (drop open and keep closed)
				if (DEBUG) {
					System.out.println("Loading default drop rules...");
				}

				for (int c = 0; c < classNumber; c++) {
					int classType = jobClasses[c].getType();
					if (classType == JobClass.OPEN_CLASS) {
						//drop open class jobs
						dropThisClass[c] = true;
					} else {
						//block closed class jobs
						dropThisClass[c] = false;
					}
				}
			} else {
				//drop rules specified by user
				if (DEBUG) {
					System.out.println("Loading specified drop rules...");
				}

				Element dropRule = null;
				String className;
				int classPosition;

				for (int dr = 0; dr < dropRules.getLength(); dr++) {
					dropRule = (Element) dropRules.item(dr);
					className = dropRule.getAttribute("jobClass");
					//entries may be in a wrong order: find the right position
					classPosition = findClassPosition(className);
					dropThisClass[classPosition] = dropRule.getAttribute("dropThisClass").equalsIgnoreCase("true");

					if (DEBUG) {
						System.out.println("   drop for class " + className + " = " + dropThisClass[classPosition]);
					}
				}
			}

			//-----------------CLASS CONSTRAINTS-----------------//
			NodeList classConstraints = region.getElementsByTagName("classConstraint");

			//max capacity for each class (-1 means no constraint)
			double[] maxCapacityPerClass = new double[classNumber];
			//init
			Arrays.fill(maxCapacityPerClass, -1.0);

			if (classConstraints.getLength() > 0) {
				Element clsConst = null;
				String className;
				int classPosition;

				for (int cc = 0; cc < classConstraints.getLength(); cc++) {
					clsConst = (Element) classConstraints.item(cc);
					className = clsConst.getAttribute("jobClass");
					//entries may be in a wrong order: find the right position
					classPosition = findClassPosition(className);
					maxCapacityPerClass[classPosition] = Double.parseDouble(clsConst.getAttribute("maxJobsPerClass"));

					if (DEBUG) {
						System.out.println("   constraint for class " + className + " = " + Double.toString(maxCapacityPerClass[classPosition]));
					}
				}
			}

			//-----------------CLASS MEMORY CONSTRAINTS-----------------//
			NodeList classMemoryConstraints = region.getElementsByTagName("classMemoryConstraint");

			//max memory for each class (-1 means no constraint)
			double[] maxMemoryPerClass = new double[classNumber];
			//init
			Arrays.fill(maxMemoryPerClass, -1.0);

			if (classMemoryConstraints.getLength() > 0) {
				Element clsMemConst = null;
				String className;
				int classPosition;

				for (int cmc = 0; cmc < classMemoryConstraints.getLength(); cmc++) {
					clsMemConst = (Element) classMemoryConstraints.item(cmc);
					className = clsMemConst.getAttribute("jobClass");
					//entries may be in a wrong order: find the right position
					classPosition = findClassPosition(className);
					maxMemoryPerClass[classPosition] = Double.parseDouble(clsMemConst.getAttribute("maxMemoryPerClass"));

					if (DEBUG) {
						System.out.println("   memory constraint for class " + className + " = " + Double.toString(maxMemoryPerClass[classPosition]));
					}
				}
			}

			//----------------CLASS WEIGHTS----------------//
			NodeList classWeights = region.getElementsByTagName("classWeight");

			double[] regionClassWeights = new double[classNumber];
			//init
			Arrays.fill(regionClassWeights, 1.0);

			if (classWeights.getLength() > 0) {
				Element clsWeight = null;
				String className;
				int classPosition;

				for (int cw = 0; cw < classWeights.getLength(); cw++) {
					clsWeight = (Element) classWeights.item(cw);
					className = clsWeight.getAttribute("jobClass");
					//entries may be in a wrong order: find the right position
					classPosition = findClassPosition(className);
					regionClassWeights[classPosition] = Double.parseDouble(clsWeight.getAttribute("weight"));

					if (DEBUG) {
						System.out.println("   weight for class " + className + " = " + Double.toString(regionClassWeights[classPosition]));
					}
				}
			}

			//----------------CLASS SIZES----------------//
			NodeList classSizes = region.getElementsByTagName("classSize");

			double[] regionClassSizes = new double[classNumber];
			//init
			Arrays.fill(regionClassSizes, 1.0);

			if (classSizes.getLength() > 0) {
				Element clsSize = null;
				String className;
				int classPosition;

				for (int cs = 0; cs < classSizes.getLength(); cs++) {
					clsSize = (Element) classSizes.item(cs);
					className = clsSize.getAttribute("jobClass");
					//entries may be in a wrong order: find the right position
					classPosition = findClassPosition(className);
					regionClassSizes[classPosition] = Double.parseDouble(clsSize.getAttribute("size"));

					if (DEBUG) {
						System.out.println("   size for class " + className + " = " + Double.toString(regionClassSizes[classPosition]));
					}
				}
			}

			//----------------CLASS DUE DATES----------------//
			NodeList classSoftDeadlines = region.getElementsByTagName("classSoftDeadline");

			double[] regionClassSoftDeadlines = new double[classNumber];
			//init
			Arrays.fill(regionClassSoftDeadlines, 1.0);

			if (classSoftDeadlines.getLength() > 0) {
				Element clsSoftDeadline = null;
				String className;
				int classPosition;

				for (int cdd = 0; cdd < classSoftDeadlines.getLength(); cdd++) {
					clsSoftDeadline = (Element) classSoftDeadlines.item(cdd);
					className = clsSoftDeadline.getAttribute("jobClass");
					//entries may be in a wrong order: find the right position
					classPosition = findClassPosition(className);
					regionClassSoftDeadlines[classPosition] = Double.parseDouble(clsSoftDeadline.getAttribute("softDeadline"));

					if (DEBUG) {
						System.out.println("   soft deadline for class " + className + " = " + Double.toString(regionClassSoftDeadlines[classPosition]));
					}
				}
			}

			//----------------GROUP CONSTRAINTS----------------//
			NodeList groupConstraints = region.getElementsByTagName("groupConstraint");
			int groupNumber = groupConstraints.getLength();
			String[] groupNames = new String[groupNumber];
			double[] maxCapacityPerGroup = new double[groupNumber];
			for (int j = 0; j < groupNumber; j++) {
				Element groupConstraint = (Element) groupConstraints.item(j);
				groupNames[j] = groupConstraint.getAttribute("jobGroup");
				maxCapacityPerGroup[j] = Double.parseDouble(groupConstraint.getAttribute("maxJobsPerGroup"));
			}

			//----------------GROUP MEMORY CONSTRAINTS----------------//
			NodeList groupMemoryConstraints = region.getElementsByTagName("groupMemoryConstraint");
			double[] maxMemoryPerGroup = new double[groupNumber];
			for (int j = 0; j < groupNumber; j++) {
				Element groupMemoryConstraint = (Element) groupMemoryConstraints.item(j);
				maxMemoryPerGroup[j] = Double.parseDouble(groupMemoryConstraint.getAttribute("maxMemoryPerGroup"));
			}

			//----------------GROUP CLASSLISTS----------------//
			NodeList groupClassLists  = region.getElementsByTagName("groupClassList");
			List<List<Integer>> groupClassIndexLists = new ArrayList<List<Integer>>();
			for (int j = 0; j < groupNumber; j++) {
				Element groupClassList = (Element) groupClassLists.item(j);
				NodeList groupClasses = groupClassList.getElementsByTagName("groupClass");
				groupClassIndexLists.add(new ArrayList<Integer>());
				for (int k = 0; k < groupClasses.getLength(); k++) {
					Element groupClass = (Element) groupClasses.item(k);
					int classIndex = findClassPosition(groupClass.getAttribute("jobClass"));
					groupClassIndexLists.get(j).add(classIndex);
				}
			}

			//------------------ADD BLOCKING REGION TO SIM------------------//
			sim.addRegion(regionName, maxCapacity, maxMemory, maxCapacityPerClass, maxMemoryPerClass, dropThisClass, regionClassWeights, regionClassSizes,
					regionClassSoftDeadlines, groupNames, maxCapacityPerGroup, maxMemoryPerGroup, groupClassIndexLists, stationNames);
			if (DEBUG) {
				System.out.println("added region " + regionName);
			}
		}
	}

	// Cut and paste of old GUI code used by SimLoader (to clear up things) - Bertoli Marco
	public static class XMLParser {

		/**
		 * Returns a NodeList of all descendant Elements with a given tag name, in
		 * the order in which they are encountered in a preordering traversal of
		 * this Element (only at first level).
		 * @param e  The element that is being analyzed.
		 * @param name  The name of the tag to match on.
		 * @return A list of matching Element nodes.
		 */
		public static NodeList getElementsByTagName(Element e, String name) {
			JmtNodeList reqChild = new JmtNodeList();
			NodeList childList = e.getChildNodes();
			if (childList != null && childList.getLength() != 0) {
				for (int i = 0; i < childList.getLength(); i++) {
					Node n = childList.item(i);
					if (n.getNodeType() == 1 && n.getNodeName() != null && n.getNodeName().equals(name)) {
						reqChild.add(n);
					}

				}
			}
			return reqChild;
		}

		/** Implements a NodeList that lets also add Nodes, feel free to
		 * add methods if u need.

		 * @author Federico Granata
		 * Date: 16-ott-2003
		 * Time: 17.17.43

		 */
		public static class JmtNodeList implements NodeList {

			ArrayList<Node> data;

			public JmtNodeList() {
				data = new ArrayList<Node>();
			}

			public void add(Node child) {
				data.add(child);
			}

			public Node item(int index) {
				return data.get(index);
			}

			public int getLength() {
				return data.size();
			}

		}

	}
	//END - Bertoli Marco

}
