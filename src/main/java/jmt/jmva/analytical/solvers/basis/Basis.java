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

package jmt.jmva.analytical.solvers.basis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import jmt.jmva.analytical.solvers.dataStructures.BigRational;
import jmt.jmva.analytical.solvers.dataStructures.EnhancedVector;
import jmt.jmva.analytical.solvers.dataStructures.PopulationChangeVector;
import jmt.jmva.analytical.solvers.dataStructures.QNModel;
import jmt.jmva.analytical.solvers.exceptions.InternalErrorException;

/**
 * A class to encapsulate the basis of CoMoM (or MoM).
 * 
 * Storage and access to the normalising constants is provided
 * 
 * @author Jack Bradshaw
 */
public abstract class Basis {

	/**
	 * The Queueing Network Model under study
	 */
	protected QNModel qnm;

	/**
	 * Data Structure to hold the ordering of PopualtionChangeVectors
	 */
	protected ArrayList<PopulationChangeVector> order;

	/**
	 * Variables to store qnm fields for easy access	
	 **/	
	protected int R;
	protected int M;

	/**
	 * Comparator with which to sort the Vector,
	 * can be set using setComparator()
	 */
	private Comparator<EnhancedVector> vector_comparator;

	/**
	 * The vectors in which to store the basis values
	 */
	protected BigRational[] basis;
	protected BigRational[] previous_basis;

	/**
	 * The current class being considered.
	 */
	protected int current_class;

	/**
	 * The population of the current class being considered.
	 */
	protected int current_class_population;

	/**
	 * Not really sure what this is for yet, but MoM uses it...
	 */
	protected Set<Integer> uncomputables;

	/**
	 * Variable to store the size of the basis, due to frequent use
	 */
	protected int size;

	/**
	 * Constructor
	 * @param qnm The Queueing Network Model under study
	 */
	public Basis(QNModel qnm) {
		this.qnm = qnm;
		R = qnm.R;
		M = qnm.M;
		setSize();
		basis = new BigRational[size];
		previous_basis = new BigRational[size];
		uncomputables = new HashSet<Integer>();
	}

	/**
	 * Initialises the basis for population (0,...0)
	 * @throws InternalErrorException 
	 */	
	public abstract void initialiseBasis() throws InternalErrorException;

	/**
	 * Initialises the basis for recursion on the current class
	 * @param current_class The class to initialise for.
	 * @throws InternalErrorException
	 */
	public void initialiseForClass(int current_class) throws InternalErrorException {
		this.current_class = current_class;
		this.current_class_population = 1;		
	}

	/**
	 * A subclass can choose a vector comparator this method sets the comparator and uses it to sort
	 * the ordering
	 * @param comparator
	 */
	protected void setComparator(Comparator<EnhancedVector> comparator) {
		vector_comparator = comparator;
		sort();
	}

	/**
	 * Sorts the PopulationChangeVectors according to the 'vector_comparator'
	 */
	protected final void sort() {
		if (vector_comparator == null) {
			//No comparator specified, do nothing.
			return;
		} else { //sort the ordering
			Collections.sort(order, vector_comparator);
		}
	}

	/**
	 * Calculates the size of the basis to be store in variable size
	 */
	protected abstract void setSize();

	/**
	 * @return The size of the basis
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Computes Mean Throughput and Mean Queue Length performance indices
	 * and stores them in the queueing network model object, qnm
	 * @throws InternalErrorException
	 */
	public abstract void computePerformanceMeasures() throws InternalErrorException;

	/**
	 * @return The current class being considered
	 */
	public int getCurrentClass() {
		return current_class;
	}

	/**
	 * @return The population in the current class being considered
	 */
	public int getCurrentClassPopulation() {
		return current_class_population;
	}

	/**
	 * Sets the population of the current class being considered
	 * @param popualtion
	 */
	public void setCurrentClassPopulation(int population) {
		current_class_population = population;
	}

	/**
	 * Returns the Normalising Constant for the current computed population  
	 * @throws InternalErrorException 
	 */
	public abstract BigRational getNormalisingConstant() throws InternalErrorException;

	public void reset_uncomputables() {
		for (int i = 0; i < size; i++) {
			uncomputables.add(i);
		}
	}

	public void computatble(int i) {
		uncomputables.remove(i);
	}

	public Set<Integer> getUncomputables() {
		return uncomputables;
	}

	public BigRational getOldValue(int index) {
		return previous_basis[index];
	}

	public BigRational getNewValue(int index) {
		return basis[index];
	}

	public void setValue(BigRational value, int index) {
		basis[index] = value.copy();
	}

	public void startBasisComputation() {
		BigRational[] temp_reference = basis;
		basis = previous_basis;
		previous_basis = temp_reference;
	}

}
