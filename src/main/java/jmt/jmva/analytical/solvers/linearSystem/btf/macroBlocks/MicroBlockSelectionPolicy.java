package jmt.jmva.analytical.solvers.linearSystem.btf.macroBlocks;

import jmt.jmva.analytical.solvers.dataStructures.QNModel;
import jmt.jmva.analytical.solvers.linearSystem.btf.microBlocks.MicroBlock;

/**
 * A class to encapsulate the various policies for selecting Macro Blocks when considering lower classes
 * @author Jack Bradshaw
 *
 */
public abstract class MicroBlockSelectionPolicy {
	
	/**
	 * The full TopLevelBlock containing ALL macro blocks
	 */
	protected MacroBlock full_block;
	
	/**
	 * The Model under consideration.
	 */
	QNModel qnm;

	/**
	 * Constructor
	 * @param full_block The full TopLevelBlock containing ALL macro blocks
	 * @param h The number of non-zeros associated with the Macro Block
	 */
	protected MicroBlockSelectionPolicy(QNModel qnm, MacroBlock full_block) {
		this.qnm = qnm;
		this.full_block = full_block;		
	}	

	/**
	 * Selects the required macro blocks as per the policy
	 * @param macro_blocks The array of selected macro blocks
	 * @param current_class The current class being considered.
	 */
	protected abstract MicroBlock[] selectMicroBlocks(int current_class);
}
