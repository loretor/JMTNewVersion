package jmt.jmva.analytical.solvers;

import jmt.framework.data.ArrayUtils;
import jmt.jmva.analytical.solvers.Solver;
import jmt.jmva.analytical.solvers.SolverMultiClosedRECAL;
import jmt.jmva.analytical.solvers.exceptions.InternalErrorException;

/**
 * Solves a single class closed model, using Monte Carlo Logistic algorithm.
 * @author  Ong Wai Hong
 */
public class SolverSingleClosedRECAL extends Solver {

	public SolverMultiClosedRECAL solver;

	public static final boolean DEBUG = false;

	// number of customers or jobs in the system
	protected int customers = 0;

	protected double[][][] serviceTimes;
	protected double[][] custVisits;

	public SolverSingleClosedRECAL(int customers, int stations) throws InternalErrorException {
		this.customers = customers;
		this.stations = stations;
		initialiseSolver();

		name = new String[stations];
		type = new int[stations];

		throughput = new double[stations];
		queueLen = new double[stations];
		utilization = new double[stations];
		residenceTime = new double[stations];
	}

	private void initialiseSolver() throws InternalErrorException {
		int[] population = new int[1];
		population[0] = customers;
		solver = new SolverMultiClosedRECAL(1, stations, population);
	}

	public boolean hasSufficientProcessingCapacity() {
		// closed class: no saturation problem
		return true;
	}

	@Override
	public boolean input(String[] n, int[] t, double[][] s, double[] v) {
		serviceTimes = new double[stations][1][s[0].length];
		custVisits = new double[stations][1];
		if ((n.length != stations) || (t.length != stations) || (s.length != stations) || (v.length != stations)) {
			return false; // wrong input.
		}
		for (int i = 0; i < stations; i++) {
			if (t[i]==LD) {
				serviceTimes[i][0] = new double[s[i].length-1];
				for (int j = 0; j < s[i].length-1; j++)
					serviceTimes[i][0][j] = s[i][j+1]; /*HACK-countering Hack because adjustLD does awful things*/
			} else {
				serviceTimes[i][0] = new double[s[i].length];
				for (int j = 0; j < s[i].length; j++)
					serviceTimes[i][0][j] = s[i][j];
			}
			custVisits[i][0] = v[i];
		}
		return solver.input(t, serviceTimes, custVisits, 1); // single thread
	}

	public void solve() {
		solver.solve();

		// TODO: Move into singleSolverAlgorithms package
		// sys.ResponseTime & co are protected fields in SolverMulti so need to be in the same package
		// Ideally a wrapper would be used
		totUser = customers;
		totRespTime = solver.sysResponseTime;
		totThroughput = solver.sysThroughput;

		queueLen = ArrayUtils.extract1(solver.queueLen, 0);
		throughput = ArrayUtils.extract1(solver.throughput, 0);
		residenceTime = ArrayUtils.extract1(solver.residenceTime, 0);
		utilization = ArrayUtils.extract1(solver.utilization, 0);
	}

}
