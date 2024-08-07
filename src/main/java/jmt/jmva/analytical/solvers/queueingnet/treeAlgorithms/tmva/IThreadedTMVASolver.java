package jmt.jmva.analytical.solvers.queueingNet.treeAlgorithms.tmva;

import jmt.jmva.analytical.solvers.dataStructures.PopulationVector;
import jmt.jmva.analytical.solvers.queueingNet.treeAlgorithms.Node;

/**
 * The solver interface for tree MVA solvers.
 * @author Ben Homer, 2014.
 */
public interface IThreadedTMVASolver {
	
	/**
	 * Solves the network rooted at the specified node.
	 * @param node The root node of the tree.
	 * @param N The population vector.
	 * @return The MVAResults object containing the final performance measures.
	 */
	MVAResults solve(Node node, PopulationVector N);	
}
