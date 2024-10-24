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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class contains some constants for exact models.
 * @author alyf (Andrea Conti), Bertoli Marco
 * @version Date: 11-set-2003 Time: 16.40.25
 * 
 * @author Kourosh Sheykhvand
 *  Added modifications regarding ReferenceStation and throughput
 *  Nov 2013
 */

public interface ExactConstants {

	public static final boolean DEBUG = true;

	public static final int STATION_DELAY = 0;
	public static final int STATION_LI = 1; // Load Independent
	public static final int STATION_LD = 2; // Load Dependent
	public static final int STATION_PRS = 3; // FCFS Preemptive Resume
	public static final int STATION_HOL = 4; // FCFS Non-preemptive Head-of-Line
	/** HTML gray text START*/
	public static final String GRAY_S = "<html><font color=\"aaaaaa\">";
	/** HTML gray text END*/
	public static final String GRAY_E = "</font></html>";

	public static final String[] STATION_TYPENAMES = {
			"Delay (Infinite Server)",
			"Load Independent",
			"Load Dependent",
			"Preemptive Resume",
			"Non-preemptive Head-of-Line"
	};

	public static final int CLASS_CLOSED = 0;
	public static final int CLASS_OPEN = 1;
	public static final String[] CLASS_TYPENAMES = { "closed", "open" };

	public static final int DEFAULT_CLASS_PRIORITY = 0;

	public static final int MAX_CLASSES = 100;
	public static final int MAX_STATIONS = 100;

	// Performance indices
	public static final String[] INDICES_TYPES = { "Throughput", "Number of Customers", "Response Times", "Residence Times", "Utilization", "System Power" };
	// Aggregate performance indices
	public static final String[] AGGREGATE_TYPES = { "System Response Time", "System Response Time (No Ref. Stat.)", "System Throughput", "System Number of Customers" };
	public static final Set<String> AGGREGATE_TYPES_SET = new HashSet<String>(Arrays.asList(AGGREGATE_TYPES));

	public static final String DESCRIPTION_CLASSES = "<html><body align=\"left\"><font size=\"4\"><b>Classes characteristics</b></font>"
			+ "<font size=\"3\"><br>Number, customized name, type of classes and number of customers (closed class) or arrival rate (open class). Add classes one by one or define total number at once. Higher number means higher priority</font></body></html>";
	public static final String DESCRIPTION_STATIONS = "<html><body align=\"left\"><font size=\"4\"><b>Stations characteristics</b></font>"
			+ "<font size=\"3\"><br>Number, customized name and type of stations. Add stations one by one or define the total number at once. Load Dependent stations necessarily require the use of MVA.</font></body></html>";
	public static final String DESCRIPTION_SERVICETIMES = "<html><body align=\"left\"><font size=\"4\"><b>Service Times</b></font>"
			+ "<font size=\"3\"><br>Input service times of each station for each class.<br>"
			+ "If the station is \"Load Dependent\" you can set the service times for each number of customers by double-click on \"LD Settings...\" button.<br>"
			+ "Press \"Service Demands\" button to enter service demands instead of service times and visits.<br>" 
			+ "MULTICLASS MODELS: when for a station the per-class service times are different, the results are correct ONLY IF its scheduling discipline is assumed Processor Sharing (PS) and not FCFS (See BCMP Theorem).<br></font></body></html>";
	public static final String DESCRIPTION_VISITS = "<html><body align=\"left\"><font size=\"4\"><b>Visits</b></font>"
			+ "<font size=\"3\"><br>Average number of visits to each station per class.</font></body></html>";
	public static final String DESCRIPTION_REFERENCESTATION = "<html><body align=\"left\"><font size=\"4\"><b>Reference Station</b></font>"
			+ "<font size=\"3\"><br>The station is used to compute the system throughput and the system response time for each <b>closed class</b>.<br>"
			+ "Performance metrics of <b>open classes</b> are always computed with respect to the <b>arrival process</b>. Visits at the Reference station can not be Zero.<br>"
			+ "<b>WARNING:</b> the reference station for all closed classes is forced to be the same station.</font></body></html>";
	public static final String DESCRIPTION_SERVICEDEMANDS = "<html><body align=\"left\"><font size=\"4\"><b>Service Demands</b></font>"
			+ "<font size=\"3\"><br>Input service demands of each station and class.<br>"
			+ "If the station is \"Load Dependent\" you can set the service demands for each number of customers by double-click on \"LD Settings...\" button.<br>"
			+ "Press \"Service Times and Visits\" button to enter service times and visits instead of service demands.</font></body></html>";
	public static final String DESCRIPTION_COMMENT = "<html><body align=\"left\"><font size=\"4\"><b>Comment</b></font>"
			+ "<font size=\"3\"><br>Input an optional short comment.<br></font></body></html>";
	public static final String DESCRIPTION_QUEUELENGTHS = "<html><body align=\"left\"><font size=\"4\"><b>Number of Customers</b></font>"
			+ "<font size=\"3\"><br>Average number of customers for each class at each station.</font></body></html>";
	public static final String DESCRIPTION_THROUGHPUTS = "<html><body align=\"left\"><font size=\"4\"><b>Throughput</b></font>"
			+ "<font size=\"3\"><br>Throughput of each class for each station. System Throughput is the completion rate at the <b>Reference Station</b>.</font></body></html>";
	public static final String DESCRIPTION_SYSRESPONSETIMES = "<html><body align=\"left\"><font size=\"4\"><b>System Response Time</b></font>"
			+ "<font size=\"3\"><br>The global aggregate is the \"System Response Time\" and is obtained weighting the aggregated values by the relative per-class throughput.<br>"
			+ "<b>A:</b> This value of System Response Time <b>includes</b> the Residence Time at the Reference Station.<br>"
			+ "<b>B:</b> This value of System Response Time <b>does NOT include</b> the Residence Time at the Reference Station.<br>"
			+ "Notice: For <b>open classes</b> the Reference Station always coincides with the arrival process. Thus the <b>B</b> values are not computed.</font></body></html>";
	public static final String DESCRIPTION_RESPONSETIMES = "<html><body align=\"left\"><font size=\"4\"><b>Response Times</b></font>"
			+ "<font size=\"3\"><br>Average time spent by each customer class for a single visit to a station.</font></body></html>";
	public static final String DESCRIPTION_RESIDENCETIMES = "<html><body align=\"left\"><font size=\"4\"><b>Residence Times</b></font>"
			+ "<font size=\"3\"><br>Average time spent by each customer class summed across all visits to a station.</font></body></html>";
	public static final String DESCRIPTION_UTILIZATIONS = "<html><body align=\"left\"><font size=\"4\"><b>Utilization</b></font>"
			+ "<font size=\"3\"><br>Utilization of a customer class at the selected station. The utilization of a delay station is the average number of customers in the station (it may be greater than 1).</font></body></html>";
	public static final String DESCRIPTION_WHATIF_NONE = "<html><body align=\"left\"><font size=\"4\"><b>What-if analysis</b></font>"
			+ "<font size=\"3\"><br>Select a control parameter if you want to solve several models with its values changing in the selected range. The performance indices will be shown in a graph.</font></body></html>";
	public static final String DESCRIPTION_WHATIF_ARRIVAL_ALL = "<html><body align=\"left\"><font size=\"4\"><b>What-if analysis</b></font>"
			+ "<font size=\"3\"><br>Solve models increasing proportionally the arrival rates of the open classes. Starting from the actual value, the arrival rates are increased (or decreased) by the percentage expressed in the 'to' value.</font></body></html>";
	public static final String DESCRIPTION_WHATIF_ARRIVAL_ONE = "<html><body align=\"left\"><font size=\"4\"><b>What-if analysis</b></font>"
			+ "<font size=\"3\"><br>Solve models with increasing (or decreasing) arrival rate of selected open class.</font></body></html>";
	public static final String DESCRIPTION_WHATIF_CUSTOMERS_ALL = "<html><body align=\"left\"><font size=\"4\"><b>What-if analysis</b></font>"
			+ "<font size=\"3\"><br>Solve models increasing proportionally the number of customers of the closed classes. Starting from the actual value, the population is increased (or decreased) by the percentage expressed in the 'to' value.<br>"
			+ "Since only integer population values are allowed, the number of models executed can be very small.</font></body></html>";
	public static final String DESCRIPTION_WHATIF_CUSTOMERS_ONE = "<html><body align=\"left\"><font size=\"4\"><b>What-if analysis</b></font>"
			+ "<font size=\"3\"><br>Solve models with increasing (or decreasing) number of customers of selected closed class.</font></body></html>";
	public static final String DESCRIPTION_WHATIF_DEMANDS_ALL = "<html><body align=\"left\"><font size=\"4\"><b>What-if analysis</b></font>"
			+ "<font size=\"3\"><br>Solve models increasing proportionally service demands at selected station for all classes. Starting from the actual value, service demands are increased (or decreased) by the percentage expressed in the 'to' value.</font></body></html>";
	public static final String DESCRIPTION_WHATIF_DEMANDS_ONE = "<html><body align=\"left\"><font size=\"4\"><b>What-if analysis</b></font>"
			+ "<font size=\"3\"><br>Solve models with increasing (or decreasing) service demand at selected station for selected class.</font></body></html>";
	public static final String DESCRIPTION_WHATIF_MIX = "<html><body align=\"left\"><font size=\"4\"><b>What-if analysis</b></font>"
			+ "<font size=\"3\"><br>Solve models with different proportion of jobs between two closed classes, keeping constant the total number of jobs N (\u03b2i = Ni / N). It is required that Ni > 0.<br>"
			+ "Since only integer Ni values are allowed, the number of models executed can be very small.</font></body></html>";
	public static final String DESCRIPTION_GRAPH = "<html><body align=\"left\"><font size=\"4\"><b>Graphical Results</b></font>"
			+ "<font size=\"3\"><br>Select performance indices to be plotted. Right-click and drag on the graph to zoom it, right-click to save it in EPS or PNG format.<br>"
			+ "Notice: System Response Time of closed classes includes the Residence Time at the Reference Station.</font></body></html>";
	public static final String DESCRIPTION_SYSTEMPOWERS = "<html><body align=\"left\"><font size=\"4\"><b>System Power</b></font>"
			+ "<font size=\"3\"><br><b>Aggregate System Power:</b> Aggregate System Throughput (sum of the per-class throughputs) divided by the Aggregate System Response Time (sum of the Response Times per class weighted by the relative throughputs).<br>"
			+ "<b>Per-class System Power:</b> Throughput divided by the Response Time of each class.<br>"
			+ "<b>A:</b> This value of System Power is computed using the value of System Response Time that <b>includes</b> the Residence Time at the Reference Station.<br>"
			+ "<b>B:</b> This value of System Power is computed using the value of System Response Time that <b>does NOT include</b> the Residence Time at the Reference Station.<br>"
			+ "Notice: For <b>open classes</b> the Reference Station always coincides with the arrival process. Thus the <b>B</b> values are not computed.</font></body></html>";

	/** What-if Analysis type constants */
	public static final String WHAT_IF_ARRIVAL = "Arrival Rates";
	public static final String WHAT_IF_CUSTOMERS = "Number of Customers";
	public static final String WHAT_IF_MIX = "Population Mix";
	public static final String WHAT_IF_DEMANDS = "Service Demands";
}
