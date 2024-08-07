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

package jmt.engine.random;

import jmt.common.exception.IncorrectDistributionParameterException;

/**
 * 
 * This is the parameter that should be passed to the empirical
 * distribution.
 * 
 * <br><br>Copyright (c) 2003
 * <br>Politecnico di Milano - dipartimento di Elettronica e Informazione
 * @author Fabrizio Frontera - ffrontera@yahoo.it
 * 
 */
public class EmpiricalPar extends AbstractParameter implements Parameter {

	/** cumulative distribution function*/
	protected double[] cdf;
	/** probability distribution function*/
	protected double[] pdf;

	/** values of the parameter*/
	protected Object[] values;

	/**
	 * This is the default constructor. It creates a new empty empirical parameter
	 * that must be set with the setPDF method before being used.
	 *
	 */
	public EmpiricalPar() {
	}

	/**
	 * It creates a new empirical parameter. It accepts an array of double greater or
	 * equal to zero and verifies that the sum of all these data is one or zero.
	 *
	 * @param pdf array of <code>double</code> for the new empirical distribution.
	 * @throws IncorrectDistributionParameterException when any of the provided data
	 * is less than zero or the sum of all of them is not one or zero.
	 *
	 */
	public EmpiricalPar(double[] pdf) throws IncorrectDistributionParameterException {
		setPDF(pdf);
	}

	/**
	 * It creates a new empirical parameter. It accepts an array of Double greater or
	 * equal to zero and verifies that the sum of all these data is one or zero.
	 *
	 * @param wpdf array of <code>Double</code> for the new empirical distribution.
	 * @throws IncorrectDistributionParameterException when any of the provided data
	 * is less than zero or the sum of all of them is not one or zero.
	 *
	 */
	public EmpiricalPar(Double[] wpdf) throws IncorrectDistributionParameterException {
		double[] wpdf2pdf = new double[wpdf.length];
		for (int i = 0; i < wpdf.length; i++) {
			wpdf2pdf[i] = wpdf[i].doubleValue();
		}
		this.setPDF(wpdf2pdf);
	}

	/**
	 * Creates an empirical parameter with these entries (each entry contains a value
	 * and a probability). The Empirical can return not only an integer, but also
	 * directly an object, given the right probability table.
	 *
	 * @param entries Empirical entries.
	 * @throws IncorrectDistributionParameterException
	 */
	public EmpiricalPar(EmpiricalEntry[] entries) throws IncorrectDistributionParameterException {
		values = new Object[entries.length];
		double prob[] = new double[entries.length];
		for (int i = 0; i < entries.length; i++) {
			EmpiricalEntry entry = entries[i];
			values[i] = entry.getValue();
			prob[i] = entry.getProbability();
		}
		setPDF(prob);
	}

	/**
	 * It returns the value of the parameters for the empirical distribution, that is
	 * the vector of double representing the probabilities provided by the user.
	 *
	 * @return array of double with the probability distribution function tabulated.
	 */
	public double[] getPDF() {
		return pdf;
	}

	/**
	 * It returns the value of the parameters for the empirical distribution, that is
	 * the vector of double representing the cumulative distribution function tabulated.
	 *
	 * @return array of double with the cumulative distribution function tabulated.
	 */
	public double[] getCDF() {
		return cdf;
	}

	/**
	 * It allows the user to change the value of the parameter of the empirical distribution.
	 * Takes an existent array of pdf and tries to convert it in a pdf for an empirical
	 * distribution, checking the conditions which must be respected.
	 *
	 * @param pdf array of double containing an existent pdf.
	 * @throws IncorrectDistributionParameterException if, among the provided data, there is
	 * a value less than zero or the sum of all of them is not one or zero.
	 *
	 */
	public void setPDF(double[] pdf) throws IncorrectDistributionParameterException {
		double sumProb = 0.0;
		this.pdf = new double[pdf.length];
		for (int i = 0; i < pdf.length; i++) {
			if (pdf[i] >= 0.0) {
				this.pdf[i] = pdf[i];
				sumProb += this.pdf[i];
			} else {
				//negative probability not allowed
				throw new IncorrectDistributionParameterException("Found a probability less than zero. Only values equal to or greater than zero are allowed.");
			}
		}

		if (Math.abs(sumProb - 1.0) > 1e-6 && Math.abs(sumProb - 0.0) > 1e-6) {
			//total probability must be near 1 or 0 (1e-6 error allowed)
			throw new IncorrectDistributionParameterException("The sum of the probabilities must be one or zero, but the input values sum up to " + sumProb);
		}

		//sets the cdf with the new probabilities
		setCDF();
	}

	/**
	 * Generates the CDF from the PDF. This is only a helper method called by setPdf.
	 * Each new cdf value is the sum of the previous cdf value and the current pdf value.
	 *
	 */
	private void setCDF() {
		int nBins = pdf.length;
		this.cdf = new double[nBins + 1];
		this.cdf[0] = 0.0;
		for (int ptn = 0; ptn < nBins; ptn++) {
			this.cdf[ptn + 1] = cdf[ptn] + pdf[ptn];
		}
	}

	/**
	 * It controls if the parameter is correct or not. For the empirical distribution,
	 * the parameter is correct if the values in the pdf array are equal to or greater
	 * than zero and they sum up to one or zero.
	 *
	 * @return boolean indicating if the parameter is correct or not.
	 *
	 */
	@Override
	public boolean check() {
		return (!(cdf == null));
	}

	/**
	 * Checks whether a parameter value is correct.
	 *
	 * @param parameterName
	 * @param value
	 */
	public static boolean guiCheck(String parameterName, Double value) {
		if (parameterName.compareTo("pdf") == 0) {
			//the string parameterName is equal to "pdf"
			if ((value.doubleValue() >= 0.0) && (value.doubleValue() <= 1.0)) {
				return true;
			}
		}
		return false;
	}

	public static String guiGetErrorMsg(String parameterName) {
		if (parameterName.compareTo("pdf") == 0) {
			//the string parameterName is equal to "pdf"
			return "<html>The parameter <font color=#0000ff><b>" + parameterName
					+ "</b></font> must be <font color=#ff0000><b> in [0, 1]</b></font>.</html>";
		}
		return "";
	}

	/**
	 * Returns the value in the corresponding position: generally only the
	 * empirical distribution needs to access this method (generates the random
	 * number then ask for the corresponding value).
	 *
	 * @param position
	 * @return value
	 */
	public Object getValue(int position) {
		return values[position];
	}

	/**
	 * Returns all the values.
	 *
	 * @return values
	 */
	public Object[] getValues() {
		return values;
	}

} // end EmpiricalPar
