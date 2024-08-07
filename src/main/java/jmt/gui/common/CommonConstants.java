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

package jmt.gui.common;

import java.awt.*;

import jmt.framework.data.ConstMap;
import jmt.gui.common.forkStrategies.*;
import jmt.gui.common.joinStrategies.GuardJoin;
import jmt.gui.common.joinStrategies.JoinStrategy;
import jmt.gui.common.joinStrategies.NormalJoin;
import jmt.gui.common.joinStrategies.PartialJoin;
import jmt.gui.common.routingStrategies.*;
import jmt.gui.common.semaphoreStrategies.NormalSemaphore;
import jmt.gui.common.semaphoreStrategies.SemaphoreStrategy;

/**
 * Created by IntelliJ IDEA.
 * User: OrsotronIII
 * Date: 17-mag-2005
 * Time: 11.15.51
 */
public interface CommonConstants {

	public static final int CLASS_TYPE_OPEN = 0;
	public static final int CLASS_TYPE_CLOSED = 1;

	/** Table row height */
	public static final int ROW_HEIGHT = 30;

	public static final String STATION_TYPE_SOURCE = "Source";
	public static final String STATION_TYPE_SINK = "Sink";
	public static final String STATION_TYPE_TERMINAL = "Terminal";
	public static final String STATION_TYPE_ROUTER = "Router";
	public static final String STATION_TYPE_DELAY = "Delay";
	public static final String STATION_TYPE_SERVER = "Server";
	public static final String STATION_TYPE_FORK = "Fork";
	public static final String STATION_TYPE_JOIN = "Join";
	public static final String STATION_TYPE_LOGGER = "Logger";
	public static final String STATION_TYPE_CLASSSWITCH = "ClassSwitch";
	public static final String STATION_TYPE_SEMAPHORE = "Semaphore";
	public static final String STATION_TYPE_SCALER = "Scaler";
	public static final String STATION_TYPE_PLACE = "Place";
	public static final String STATION_TYPE_TRANSITION = "Transition";

	//For BPMN
	//public static final String STATION_TYPE_INGATEWAY = "InGateWay";
	//public static final String STATION_TYPE_OUTGATEWAY = "OutGateWay";

	/**
	 * A map that decodes from station type to station name for the GUI. It also
	 * determines the display order of station icons/names. To disable a type of
	 * station, simply comment out the corresponding line.
	 */
	public static final ConstMap<String, String> STATION_NAMES = new ConstMap<String, String>() {
		@Override
		protected void fill() {
			put(STATION_TYPE_SOURCE, STATION_TYPE_SOURCE);
			put(STATION_TYPE_SINK, STATION_TYPE_SINK);
			//put(STATION_TYPE_TERMINAL, STATION_TYPE_TERMINAL);
			put(STATION_TYPE_ROUTER, STATION_TYPE_ROUTER);
			put(STATION_TYPE_DELAY, STATION_TYPE_DELAY);
			put(STATION_TYPE_SERVER, "Queue");
			put(STATION_TYPE_FORK, STATION_TYPE_FORK);
			put(STATION_TYPE_JOIN, STATION_TYPE_JOIN);
			put(STATION_TYPE_SCALER, STATION_TYPE_SCALER);
			put(STATION_TYPE_SEMAPHORE, STATION_TYPE_SEMAPHORE);
			put(STATION_TYPE_LOGGER, STATION_TYPE_LOGGER);
			put(STATION_TYPE_CLASSSWITCH, STATION_TYPE_CLASSSWITCH);
			put(STATION_TYPE_PLACE, STATION_TYPE_PLACE);
			put(STATION_TYPE_TRANSITION, STATION_TYPE_TRANSITION);

			//For BPMN
			//put(STATION_TYPE_INGATEWAY, STATION_TYPE_INGATEWAY);
			//put(STATION_TYPE_OUTGATEWAY, STATION_TYPE_OUTGATEWAY);
		}
	};

	/**Constants for GUI scaling*/
	//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	public float heightScaling = 1.25F; //(float) (screenSize.height/768/2);
	public float widthScaling = 1.25F; //(float) (screenSize.height/768/2);

	public int MAX_GUI_WIDTH_STARTSCREEN = (int) (800 * widthScaling);
	public int MAX_GUI_HEIGHT_STARTSCREEN = (int) (600 * heightScaling);
	public int MAX_GUI_WIDTH_ABOUT = (int) (800 * widthScaling);
	public int MAX_GUI_HEIGHT_ABOUT = (int) (600 * heightScaling);
	public int MAX_GUI_WIDTH_QUICKHTML = (int) (800 * widthScaling);
	public int MAX_GUI_HEIGHT_QUICKHTML = (int) (600 * heightScaling);

	public int MAX_GUI_WIDTH_DIALOG_DEFAULT = (int)(780 * CommonConstants.widthScaling);
	public int MAX_GUI_HEIGHT_DIALOG_DEFAULT = (int)(520 * CommonConstants.heightScaling);
	public int MAX_GUI_WIDTH_COMMON = (int)(1024);
	public int MAX_GUI_HEIGHT_COMMON = (int)(768);

	public int MAX_GUI_WIDTH_JSIM_DISTRIB = (int)(480);
	public int MAX_GUI_HEIGHT_JSIM_DISTRIB = (int)(600);
	public int MAX_GUI_WIDTH_JSIM_PROBLEMS = (int)(450 * CommonConstants.widthScaling);
	public int MAX_GUI_HEIGHT_JSIM_PROBLEMS = (int)(435 * CommonConstants.heightScaling);
	public int MAX_GUI_WIDTH_JSIM_RESULTS = (int)(1024);
	public int MAX_GUI_HEIGHT_JSIM_RESULTS = (int)(768);
	public int MAX_GUI_WIDTH_JSIM_STAT_OUTPUT = (int)(1024);
	public int MAX_GUI_HEIGHT_JSIM_STAT_OUTPUT = (int)(768);

	public int MAX_GUI_WIDTH_JMVA = (int) (1024);
	public int MAX_GUI_HEIGHT_JMVA = (int) (768);

	public int MAX_GUI_WIDTH_JMCH = (int)(800);
	public int MAX_GUI_HEIGHT_JMCH = (int)(600);

	public int MAX_GUI_WIDTH_JABA = (int)(1152);
	public int MAX_GUI_HEIGHT_JABA = (int)(768); // keep 1152x768 or graphical issues

	public int MAX_GUI_WIDTH_JWAT = (int)(1024);
	public int MAX_GUI_HEIGHT_JWAT = (int)(768);
	public int MAX_GUI_WIDTH_JWAT_STARTSCREEN = (int)(520);
	public int MAX_GUI_HEIGHT_JWAT_STARTSCREEN = (int)(400);
	public int MAX_GUI_WIDTH_JWAT_COLOR = (int)(1024);
	public int MAX_GUI_HEIGHT_JWAT_COLOR = (int)(768);

	public int MAX_GUI_WIDTH_LDSTRATEGY = (int)(800 * CommonConstants.widthScaling);
	public int MAX_GUI_HEIGHT_LDSTRATEGY = (int)(600 * CommonConstants.heightScaling);
	public int MAX_GUI_WIDTH_LDHELP = (int)(600 * CommonConstants.widthScaling);
	public int MAX_GUI_HEIGHT_LDHELP = (int)(400 * CommonConstants.heightScaling);
	public int MAX_GUI_WIDTH_LDROUTING = (int)(800 * CommonConstants.widthScaling);
	public int MAX_GUI_HEIGHT_LDROUTING = (int)(600 * CommonConstants.heightScaling);
	public int MAX_GUI_WIDTH_LDEDITING = (int)(400 * CommonConstants.widthScaling);
	public int MAX_GUI_HEIGHT_LDEDITING = (int)(400 * CommonConstants.heightScaling);

	public int MAX_GUI_WIDTH_POLLING = (int)(640 * CommonConstants.widthScaling);
	public int MAX_GUI_HEIGHT_POLLING = (int)(480 * CommonConstants.heightScaling);
	public int MAX_GUI_WIDTH_PSSERVER = (int)(640 * CommonConstants.widthScaling);
	public int MAX_GUI_HEIGHT_PSSERVER = (int)(200 * CommonConstants.heightScaling);
	public int MAX_GUI_WIDTH_SERVER_FEATURES = (int)(780 * CommonConstants.widthScaling);
	public int MAX_GUI_HEIGHT_SERVER_FEATURES = (int)(520 * CommonConstants.heightScaling);

	public int MAX_GUI_WIDTH_WHATIF_PROGRESS = (int)(640 * CommonConstants.widthScaling);
	public int MAX_GUI_HEIGHT_WHATIF_PROGRESS = (int)(480 * CommonConstants.heightScaling);
	public int MAX_GUI_WIDTH_WHATIF_RESULTS = (int)(1024);
	public int MAX_GUI_HEIGHT_WHATIF_RESULTS = (int)(768);

	public static final Dimension DIM_BUTTON_XS = new Dimension((int)(60 * CommonConstants.widthScaling), (int)(25 * CommonConstants.heightScaling));
	public static final Dimension DIM_BUTTON_S = new Dimension((int)(80 * CommonConstants.widthScaling), (int)(25 * CommonConstants.heightScaling));
	public static final Dimension DIM_BUTTON_M = new Dimension((int)(100 * CommonConstants.widthScaling), (int)(25 * CommonConstants.heightScaling));
	public static final Dimension DIM_BUTTON_L = new Dimension((int)(120 * CommonConstants.widthScaling), (int)(25 * CommonConstants.heightScaling));
	public static final Dimension DIM_BUTTON_XL = new Dimension((int)(140 * CommonConstants.widthScaling), (int)(25 * CommonConstants.heightScaling));

	public static final int MAX_SMALL_ICON_SCREEN_WIDTH = (int)(1366 * CommonConstants.widthScaling);
	public static final int MAX_SMALL_ICON_SCREEN_HEIGHT = (int)(768 * CommonConstants.heightScaling);

	public static final Dimension SMALL_ICON_SIZE_MENU_BAR = new Dimension((int)(25 * CommonConstants.widthScaling), (int)(25 * CommonConstants.heightScaling));
	public static final Dimension LARGE_ICON_SIZE_MENU_BAR = new Dimension((int)(35 * CommonConstants.widthScaling), (int)(35 * CommonConstants.heightScaling));
	public static final Dimension SMALL_ICON_SIZE_TOOL_BAR = new Dimension((int)(25 * CommonConstants.widthScaling), (int)(25 * CommonConstants.heightScaling));
	public static final Dimension LARGE_ICON_SIZE_TOOL_BAR = new Dimension((int)(35 * CommonConstants.widthScaling), (int)(35 * CommonConstants.heightScaling));

	/**Constants for selection of station queueing strategy*/
	public static final String STATION_QUEUE_STRATEGY_PSSERVER = "Processor Sharing Scheduling";
	public static final String STATION_QUEUE_STRATEGY_PSSERVER_PRIORITY = "Processor Sharing Scheduling (Priority)";
	public static final String STATION_QUEUE_STRATEGY_NON_PREEMPTIVE = "Non-preemptive Scheduling";
	public static final String STATION_QUEUE_STRATEGY_NON_PREEMPTIVE_PRIORITY = "Non-preemptive Scheduling (Priority)";
	public static final String STATION_QUEUE_STRATEGY_PREEMPTIVE = "Preemptive Scheduling";
	public static final String STATION_QUEUE_STRATEGY_PREEMPTIVE_PRIORITY = "Preemptive Scheduling (Priority)";
	public static final String STATION_QUEUE_STRATEGY_POLLING = "Polling Scheduling";

	/**Constants for selection of server type */
	public static final String STATION_QUEUE_STRATEGY_PS = "PS Station Queue Type";
	public static final String STATION_QUEUE_STRATEGY_POLLING_LIMITED = "K-Limited";
	public static final String STATION_QUEUE_STRATEGY_POLLING_GATED = "Gated";
	public static final String STATION_QUEUE_STRATEGY_POLLING_EXHAUSTIVE = "Exhaustive";
	public static final String STATION_SCHEDULING_POLICY_ALIS =  "ALIS (Assign Longest Idle Server)";
	public static final String STATION_SCHEDULING_POLICY_ORDER =  "Order (Assign according to order below)";
	public static final String STATION_SCHEDULING_POLICY_FAIRNESS =  "Fairness (Move back server type when used)";
	public static final String STATION_SCHEDULING_POLICY_RAIS = "RAIS (Random Assignment to Idle Servers)";
	public static final String STATION_SCHEDULING_POLICY_ALFS =  "ALFS (Assign Least Flexible Server)";
	public static final String STATION_SCHEDULING_POLICY_FSF =  "FSF (Fastest Servers First)";

	/**Constants for selection of queueing strategy*/
	public static final String QUEUE_STRATEGY_PS = "PS";
	public static final String QUEUE_STRATEGY_QBPS = "QBPS";
	public static final String QUEUE_STRATEGY_GPS = "GPS";
	public static final String QUEUE_STRATEGY_DPS = "DPS";
	public static final String QUEUE_STRATEGY_FCFS = "FCFS";
	public static final String QUEUE_STRATEGY_LCFS = "LCFS";
	public static final String QUEUE_STRATEGY_RAND = "RAND";
	public static final String QUEUE_STRATEGY_SJF = "SJF";
	public static final String QUEUE_STRATEGY_LJF = "LJF";
	public static final String QUEUE_STRATEGY_SEPT = "SEPT";
	public static final String QUEUE_STRATEGY_LEPT = "LEPT";
	public static final String QUEUE_STRATEGY_FCFS_PR = "FCFS-PR";
	public static final String QUEUE_STRATEGY_LCFS_PR = "LCFS-PR";
	public static final String QUEUE_STRATEGY_SRPT = "SRPT";
	public static final String QUEUE_STRATEGY_EDD = "EDD";
	public static final String QUEUE_STRATEGY_EDF = "EDF";
	public static final String QUEUE_STRATEGY_TBS = "TBS";

	/**Constants for service time distributions*/
	public static final String SERVICE_LOAD_INDEPENDENT = "Load Independent";
	public static final String SERVICE_LOAD_DEPENDENT = "Load Dependent";
	public static final String SERVICE_ZERO = "Zero Service Time";
	public static final String SWITCHOVER_ZERO = "Zero Switchover Time";
	public static final String SERVICE_DISABLED = "Disabled";

	/**Constants for selection of distributions*/
	public static final String DISTRIBUTION_BURST = "Burst";
	public static final String DISTRIBUTION_MAP = "MAP";
	public static final String DISTRIBUTION_MMPP2 = "MMPP2";
	public static final String DISTRIBUTION_COXIAN = "Coxian";
	public static final String DISTRIBUTION_DETERMINISTIC = "Deterministic";
	public static final String DISTRIBUTION_ERLANG = "Erlang";
	public static final String DISTRIBUTION_EXPONENTIAL = "Exponential";
	public static final String DISTRIBUTION_GAMMA = "Gamma";
	public static final String DISTRIBUTION_HYPEREXPONENTIAL = "Hyperexponential";
	public static final String DISTRIBUTION_LOGNORMAL = "Lognormal";
	public static final String DISTRIBUTION_NORMAL = "Normal";
	public static final String DISTRIBUTION_PARETO = "Pareto";
	public static final String DISTRIBUTION_PHASETYPE = "PhaseType";
	//public static final String DISTRIBUTION_POISSON = "Poisson";
	public static final String DISTRIBUTION_REPLAYER = "Replayer";
	//public static final String DISTRIBUTION_STUDENTT = "StudentT";
	public static final String DISTRIBUTION_UNIFORM = "Uniform";
	public static final String DISTRIBUTION_WEIBULL = "Weibull";

	/**Constants for selection of timing strategy*/
	public static final String TIMING_STRATEGY_TIMED = "Timed";
	public static final String TIMING_STRATEGY_IMMEDIATE = "Immediate";

	public static final RoutingStrategy ROUTING_RANDOM = new RandomRouting();
	public static final RoutingStrategy ROUTING_ROUNDROBIN = new RoundRobinRouting();
	public static final RoutingStrategy ROUTING_PROBABILITIES = new ProbabilityRouting();
	public static final RoutingStrategy ROUTING_SHORTESTQL = new ShortestQueueLengthRouting();
	public static final RoutingStrategy ROUTING_SHORTESTRT = new ShortestResponseTimeRouting();
	public static final RoutingStrategy ROUTING_LEASTUTILIZATION = new LeastUtilizationRouting();
	public static final RoutingStrategy ROUTING_FASTESTSERVICE = new FastestServiceRouting();
	public static final RoutingStrategy ROUTING_LOADDEPENDENT = new LoadDependentRouting();
	public static final RoutingStrategy ROUTING_POWER_OF_K = new PowerOfKRouting();
	public static final RoutingStrategy ROUTING_WEIGHTED_ROUNDROBIN = new WeightedRoundRobinRouting();
	public static final RoutingStrategy ROUTING_CLASS_SWITCH = new ClassSwitchRouting();
	public static final RoutingStrategy ROUTING_DISABLED = new DisabledRouting();

	public static final ForkStrategy FORK_PROBABILITIES = new ProbabilitiesFork();
	public static final ForkStrategy FORK_COMBINATION = new CombFork();
	public static final ForkStrategy FORK_CLASS_SWITCH = new ClassSwitchFork();
	public static final ForkStrategy FORK_MULTI_BRANCH_CLASS_SWITCH = new MultiBranchClassSwitchFork();

	public static final JoinStrategy JOIN_NORMAL = new NormalJoin();
	public static final JoinStrategy JOIN_PARTIAL = new PartialJoin();
	public static final JoinStrategy JOIN_MIX = new GuardJoin();

	public static final SemaphoreStrategy SEMAPHORE_NORMAL = new NormalSemaphore();

	/**HTML formats for panels descriptions*/
	static final String HTML_START = "<html><body align=\"left\">";
	static final String HTML_END = "</body></html>";
	static final String HTML_FONT_TITLE = "<font size=\"4\"><b>";
	static final String HTML_FONT_NORM = "<font size=\"3\">";
	static final String HTML_FONT_TIT_END = "</b></font><br>";
	static final String HTML_FONT_NOR_END = "</font>";

	public static final String FINITE_DROP = "Drop";
	public static final String FINITE_BLOCK = "BAS Blocking";
	public static final String FINITE_WAITING = "Waiting Queue";
	public static final String FINITE_RETRIAL = "Retrial";

	public static final String CLASSES_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Classes Characteristics" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Define type (Open or Closed), name and parameters for each customer class.<br/>"
			+ "<b>Closed Classes:</b> If a <b>ClassSwitch</b> is in the model, then <b>all</b> the closed classes must have the <b>same</b> reference station.<br/>"
			+ "<b>Open Classes:</b> An open class that has <b>Fork</b>, <b>ClassSwitch</b>, <b>Scaler</b> or <b>Transition</b> as the reference station is <b>not</b> generated by <b>any</b> Source.<br/>"
			+ "<b>Priorities:</b> A larger value implies a higher priority." + HTML_FONT_NOR_END + HTML_END;

	public static final String STATIONS_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Station Characteristics" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Define type and name for each station" + HTML_FONT_NOR_END + HTML_END;

	public static final String CONNECTIONS_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Station Connections" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Click on table entry (i,j) to connect station i to station j." + HTML_FONT_NOR_END + HTML_END;

	public static final String STATIONS_PAR_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Station Parameters" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "For each station in the list, define the requested parameters" + HTML_FONT_NOR_END + HTML_END;

	public static final String MEASURES_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Performance Indices" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Define performance indices to be collected and plotted by the simulation engine." + HTML_FONT_NOR_END + HTML_END;

	public static final String METRIC_CONFIGURATIONS_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Advanced Metric Configurations" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "View and Define advanced metric configurations: "
			+ "<br/>" + "- Restrict metric to a specific Server Type (or all Server Types)"
			+ HTML_FONT_NOR_END + HTML_END;

	public static final String COMPATIBILITIES_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Server Type Compatibilities" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "View and Define server type compatibilities " + HTML_FONT_NOR_END + HTML_END;

	public static final String SERVER_SETTINGS_DESCRIPTION = HTML_START + "<p style='padding:5px; text-align:center;': 10px'>"
			+ "Define server types which can be compatible with different job classes. "
			+ "Scheduling policy determines which servers are assigned to jobs. "
			+ "Heterogeneous service time distributions allow server types to have different service time distributions.</p>"
			+ HTML_END;

	public static final String SIMULATION_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Simulation Parameters" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Define simulation parameters and initial customer locations." + HTML_FONT_NOR_END + HTML_END;

	public static final String BATCH_SIMULATION_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "What-if Analysis" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Enable parametric analysis and customize it." + HTML_FONT_NOR_END + HTML_END;

	public static final String REFSOURCE_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Reference Station" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Define reference station for each closed class. This is used to calculate system throughput for that class."
			+ HTML_FONT_NOR_END + HTML_END;

	public static final String LDSERVICE_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Load Dependent Service Time Distribution" + HTML_FONT_TIT_END
			+ HTML_FONT_NORM + "Define the distribution and the values of service times for a range of number of jobs inside the station. "
			+ "Mean value of the distribution can be specified with an arithmetic expression, as a function of the current value of 'n' (see Help for operators)."
			+ HTML_FONT_NOR_END + HTML_END;

	public static final String BALKINGSTRATEGY_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Balking Probability" + HTML_FONT_TIT_END
			+ HTML_FONT_NORM + "Define the probability that a job will balk based on the length of the queue when the job arrives. "
			+ "Assign the probability value (between 0.0 and 1.0) that a job will balk to each range." + HTML_FONT_NOR_END + HTML_END;

	public static final String PARAMETRIC_ANALYSIS_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "What-if Analysis" + HTML_FONT_TIT_END
			+ HTML_FONT_NORM + "Define the type of What-If analysis to be performed and modify parameter options." + HTML_FONT_NOR_END + HTML_END;

	public static final String BLOCKING_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Finite Capacity Region Characteristics" + HTML_FONT_TIT_END
			+ HTML_FONT_NORM + "Define number, name, composition, global and class specific constraints for finite capacity regions."
			+ HTML_FONT_NOR_END + HTML_END;

	public static final String CONVERSION_WARNING_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Conversion performed" + HTML_FONT_TIT_END
			+ HTML_FONT_NORM + "Input model was automatically converted from <b>%PAR1%</b> to <b>%PAR2%</b>. "
			+ "The conversion was completed with the warnings shown below" + HTML_FONT_NOR_END + HTML_END;

	public static final String LDROUTING_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Description" + HTML_FONT_TIT_END +
			HTML_FONT_NORM + "Customers of each class are routed depending on their number on the current station. "
			+ "The outgoing paths must have associated probabilities that should sum to 1 for each range."
			+ HTML_FONT_NOR_END + HTML_END;

	public static final String MEASURE_LOG_DESCRIPTION = HTML_START + HTML_FONT_TITLE + "Statistics CSV file" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Check the \"Save Stats\" box to collect samples in a CSV file for additional statistical analysis. This option may produce a file with a large size."
			+ HTML_FONT_NOR_END + HTML_END;

	// String tokens
	public static final String PAR1 = "%PAR1%";
	public static final String PAR2 = "%PAR2%";

	public static final int MAX_NUMBER_OF_PHASES = 1024;
	public static final int MAX_NUMBER_OF_CLASSES = 1024;
	public static final int MAX_NUMBER_OF_GROUPS = 1024;
	public static final int MAX_NUMBER_OF_STATIONS = 1024;
	public static final int MAX_NUMBER_OF_REGIONS = 1024;

	public static final String ALL_CLASSES = "--- All Classes ---";
	public static final String ALL_STATIONS = "System";
	public static final String ALL_MODES = "--- All Modes ---";
	public static final String INFINITE_CAPACITY = "Infinite Capacity";

	// Warnings for missing resources
	public static final String WARNING_CLASS_STATION = "Customer classes and stations have to be defined first";
	public static final String WARNING_CLASS = "Customer classes have to be defined first";
	public static final String WARNING_STATION = "Stations have to be defined first";
	public static final String WARNING_MULTI_CLASS = "At least two customer classes have to be defined first";
	public static final String WARNING_INCOMING_ROUTING = "Station incoming connections undefined";
	public static final String WARNING_OUTGOING_ROUTING = "Station outgoing connections undefined";
	public static final String WARNING_CLASS_INCOMING_ROUTING = "Customer classes and station incoming connections have to be defined first";
	public static final String WARNING_CLASS_OUTGOING_ROUTING = "Customer classes and station outgoing connections have to be defined first";

	// Application names (used for input file format conversion messages)
	public static final String JMVA = "JMVA";
	public static final String JSIM = "JSIM";
	public static final String JABA = "JABA";
	public static final String JWAT = "JWAT";
	public static final String SIMENGINE = "Simulation Engine XML data";

}
