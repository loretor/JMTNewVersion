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

package jmt.gui.common.xml;

/**
 * Created by IntelliJ IDEA.
 * User: orsotronIII
 * Date: 15-lug-2005
 * Time: 11.54.26
 * Modified by Bertoli Marco
 */
public interface XMLConstantNames {

	public static final String XML_DOCUMENT_XSD = "SIMmodeldefinition.xsd";
	public static final String XML_DOCUMENT_ROOT = "sim";
	public static final String XML_A_ROOT_NAME = "name";
	public static final String XML_A_ROOT_SEED = "seed";
	public static final String XML_A_ROOT_DURATION = "maxTime";
	public static final String XML_A_ROOT_SIMULATED = "maxSimulated";
	public static final String XML_A_ROOT_LOGPATH = "logPath"; /* MF08 0.7.4 - path of logs */
	public static final String XML_A_ROOT_LOGDELIM = "logDelimiter"; /* MF08 0.7.4 - delimiter character in log */
	public static final String XML_A_ROOT_LOGDECIMALSEPARATOR = "logDecimalSeparator"; /* MF08 0.7.4 - delimiter character in log */
	public static final String XML_A_ROOT_LOGREPLACE = "logReplaceMode"; /* MF08 0.7.4 - replacement mode */
	public static final String XML_A_ROOT_POLLING = "polling";
	public static final String XML_A_ROOT_MAXSAMPLES = "maxSamples";
	public static final String XML_A_ROOT_DISABLESTATISTIC = "disableStatisticStop";
	public static final String XML_A_ROOT_MAXEVENTS = "maxEvents";

	public static final String XML_E_CLASS = "userClass";
	public static final String XML_A_CLASS_NAME = "name";
	public static final String XML_A_CLASS_TYPE = "type";
	public static final String XML_A_CLASS_PRIORITY = "priority";
	public static final String XML_A_CLASS_DUE_DATE = "softDeadline";
	public static final String XML_A_CLASS_REFSOURCE = "referenceSource";
	public static final String XML_A_CLASS_CUSTOMERS = "customers";

	public static final String XML_E_SERVER_TYPE = "serverTypes";

	public static final String XML_E_STATION = "node";
	public static final String XML_A_STATION_NAME = "name";
	public static final String XML_E_STATION_SECTION = "section";
	public static final String XML_E_STATION_DUE_DATES = "classSoftDeadlines";
	public static final String XML_E_STATION_QUANTUM_SIZE = "quantumSize";
	public static final String XML_E_STATION_QUANTUM_SWITCHOVER_TIME = "quantumSwitchoverTime";

	public static final String XML_E_STATION_DELAY_OFF = "delayOff";

	public static final String XML_E_STATION_SET_UP = "setUp";
	public static final String XML_A_STATION_SECTION_CLASSNAME = "className";
	public static final String XML_A_STATION_SECTION_PS_PRIORITY = "psPriority";
	public static final String XML_E_PARAMETER = "parameter";
	public static final String XML_A_PARAMETER_NAME = "name";
	public static final String XML_A_PARAMETER_CLASSPATH = "classPath";
	public static final String XML_A_PARAMETER_ARRAY = "array";
	public static final String XML_E_PARAMETER_VALUE = "value";
	public static final String XML_E_PARAMETER_REFCLASS = "refClass";

	public static final String XML_E_SUBPARAMETER = "subParameter";
	public static final String XML_A_SUBPARAMETER_NAME = "name";
	public static final String XML_A_SUBPARAMETER_CLASSPATH = "classPath";
	public static final String XML_A_SUBPARAMETER_ARRAY = "array";
	public static final String XML_E_SUBPARAMETER_VALUE = "value";
	public static final String XML_E_SUBPARAMETER_REFCLASS = "refClass";

	public static final String XML_E_MEASURE = "measure";
	public static final String XML_A_MEASURE_NAME = "name";
	public static final String XML_A_MEASURE_ALPHA = "alpha";
	public static final String XML_A_MEASURE_PRECISION = "precision";
	public static final String XML_A_MEASURE_VERBOSE = "verbose";
	public static final String XML_A_MEASURE_TYPE = "type";
	public static final String XML_A_MEASURE_CLASS = "referenceUserClass";
	public static final String XML_A_MEASURE_STATION = "referenceNode";
	//Michalis
	public static final String XML_A_MEASURE_ISSERVERTYPE = "isServerType";
	public static final String XML_A_MEASURE_PARENTSTATION = "parentStation";
	public static final String XML_A_MEASURE_NUMOFSERVERTYPES = "numberOfServerTypes";
	public static final String XML_A_MEASURE_SERVERTYPE = "serverType";
	//
	public static final String XML_A_MEASURE_NODETYPE = "nodeType";
	public static final String XML_A_MEASURE_LOG = "logFile";

	public static final String XML_E_CONNECTION = "connection";
	public static final String XML_A_CONNECTION_SOURCE = "source";
	public static final String XML_A_CONNECTION_TARGET = "target";

	public static final String XML_E_PRELOAD = "preload";
	public static final String XML_E_STATIONPOPULATIONS = "stationPopulations";
	public static final String XML_A_PRELOADSTATION_NAME = "stationName";
	public static final String XML_E_CLASSPOPULATION = "classPopulation";
	public static final String XML_A_CLASSPOPULATION_NAME = "refClass";
	public static final String XML_A_CLASSPOPULATION_POPULATION = "population";

	public static final String XML_LOG_FILENAME = "logfileName";
	public static final String XML_LOG_FILEPATH = "logfilePath";
	public static final String XML_LOG_B_EXECTIMESTAMP = "logExecTimestamp";
	public static final String XML_LOG_B_LOGGERNAME = "logLoggerName";
	public static final String XML_LOG_B_TIMESTAMP = "logTimeStamp";
	public static final String XML_LOG_B_JOBID = "logJobID";
	public static final String XML_LOG_B_JOBCLASS = "logJobClass";
	public static final String XML_LOG_B_TIMESAMECLS = "logTimeSameClass";
	public static final String XML_LOG_B_TIMEANYCLS = "logTimeAnyClass";

	public static final String XML_E_REGION = "blockingRegion";
	public static final String XML_A_REGION_NAME = "name";
	public static final String XML_A_REGION_TYPE = "type";
	public static final String XML_E_REGIONNODE = "regionNode";
	public static final String XML_A_REGIONNODE_NAME = "nodeName";
	public static final String XML_E_GLOBALCONSTRAINT = "globalConstraint";
	public static final String XML_A_GLOBALCONSTRAINT_MAXJOBS = "maxJobs";
	public static final String XML_E_GLOBALMEMORYCONSTRAINT = "globalMemoryConstraint";
	public static final String XML_A_GLOBALMEMORYCONSTRAINT_MAXMEMORY = "maxMemory";

	public static final String XML_E_CLASSCONSTRAINT = "classConstraint";
	public static final String XML_A_CLASSCONSTRAINT_CLASS = "jobClass";
	public static final String XML_A_CLASSCONSTRAINT_MAXJOBS = "maxJobsPerClass";
	public static final String XML_E_CLASSMEMORYCONSTRAINT = "classMemoryConstraint";
	public static final String XML_A_CLASSMEMORYCONSTRAINT_CLASS = "jobClass";
	public static final String XML_A_CLASSMEMORYCONSTRAINT_MAXMEMORY = "maxMemoryPerClass";
	public static final String XML_E_DROPRULES = "dropRules";
	public static final String XML_A_DROPRULE_CLASS = "jobClass";
	public static final String XML_A_DROPRULE_DROP = "dropThisClass";
	public static final String XML_E_CLASSWEIGHT = "classWeight";
	public static final String XML_A_CLASSWEIGHT_CLASS = "jobClass";
	public static final String XML_A_CLASSWEIGHT_WEIGHT = "weight";
	public static final String XML_E_CLASSSIZE = "classSize";
	public static final String XML_A_CLASSSIZE_CLASS = "jobClass";
	public static final String XML_A_CLASSSIZE_SIZE = "size";
	public static final String XML_E_CLASSDUEDATE = "classSoftDeadline";
	public static final String XML_A_CLASSDUEDATE_CLASS = "jobClass";
	public static final String XML_A_CLASSDUEDATE_DUEDATE = "softDeadline";

	public static final String XML_E_GROUPCONSTRAINT = "groupConstraint";
	public static final String XML_A_GROUPCONSTRAINT_GROUP = "jobGroup";
	public static final String XML_A_GROUPCONSTRAINT_MAXJOBS = "maxJobsPerGroup";
	public static final String XML_E_GROUPMEMORYCONSTRAINT = "groupMemoryConstraint";
	public static final String XML_A_GROUPMEMORYCONSTRAINT_GROUP = "jobGroup";
	public static final String XML_A_GROUPMEMORYCONSTRAINT_MAXMEMORY = "maxMemoryPerGroup";
	public static final String XML_E_GROUPCLASSLIST = "groupClassList";
	public static final String XML_E_GROUPCLASS = "groupClass";
	public static final String XML_A_GROUPCLASS_GROUP = "jobGroup";
	public static final String XML_A_GROUPCLASS_CLASS = "jobClass";

	public static final String CLASSNAME_SOURCE = "RandomSource";
	public static final String CLASSNAME_TERMINAL = "Terminal";
	public static final String CLASSNAME_QUEUE = "Queue";
	public static final String CLASSNAME_SINK = "JobSink";
	public static final String CLASSNAME_SERVER = "Server";
	public static final String CLASSNAME_PREEMPTIVESERVER = "PreemptiveServer";
	public static final String CLASSNAME_LKPSERVER = "LimitedPollingServer";
	public static final String CLASSNAME_GATEDPSERVER = "GatedPollingServer";
	public static final String CLASSNAME_EXHAUSTIVEPSERVER = "ExhaustivePollingServer";
	public static final String CLASSNAME_PSSERVER = "PSServer";
	public static final String CLASSNAME_PSSERVER_PRIORITY = "PSServerPriority";
	public static final String CLASSNAME_DELAY = "Delay";
	public static final String CLASSNAME_TUNNEL = "ServiceTunnel";
	public static final String CLASSNAME_LOGGER = "LogTunnel"; /* MF08 0.7.4 - extends ServiceTunnel */
	public static final String CLASSNAME_ROUTER = "Router";
	public static final String CLASSNAME_FORK = "Fork";
	public static final String CLASSNAME_JOIN = "Join";
	public static final String CLASSNAME_CLASSSWITCH = "ClassSwitch";
	public static final String CLASSNAME_SEMAPHORE = "Semaphore";
	public static final String CLASSNAME_STORAGE = "Storage";
	public static final String CLASSNAME_LINKAGE = "Linkage";
	public static final String CLASSNAME_ENABLING = "Enabling";
	public static final String CLASSNAME_ENABLING_SERVER = "EnablingServer";
	public static final String CLASSNAME_TIMING = "Timing";
	public static final String CLASSNAME_FIRING = "Firing";

	public static final String XML_E_MAPREDUCE = "template_mapreduce";

	public static final String NODETYPE_REGION = "region";
	public static final String NODETYPE_STATION = "station";

	/**
	 * Parser features
	 */
	public static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";
	public static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";
	public static final String VALIDATION_DYNAMIC_FEATURE_ID = "http://apache.org/xml/features/validation/dynamic";
	public static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";
	public static final String EXTERNAL_SCHEMA_LOCATION_PROPERTY_ID = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";

}
