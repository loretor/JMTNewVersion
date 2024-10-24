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
import jmt.engine.math.Probability;
import jmt.engine.math.Normal;
/**
 * 
 * This is the Lognormal distribution (see the constructor
 * description for its pdf definition).
 * 
 * <br><br>Copyright (c) 2021
 * <br>Imperial College London
 * @author Giuliano Casale, g.casale@imperial.ac.uk
 * 
 */
public class Lognormal extends AbstractDistribution implements Distribution {

	private double cache; // cache for Box-Mueller algorithm
	private boolean cacheFilled; // Box-Mueller

	/**
	 * This is the constructor. It creates a new lognormal distribution which is
	 * defined from pdf:
	 * <pre>               1                   (x-m)^2
	 * pdf(x) = -------------- * exp (- ----------- )
	 *           sqrt(2*pi)*v              2v^2</pre>
	 * where v^2 is the variance and m is the mean of the distribution
	 * pi is the pi-greco constant.
	 */
	public Lognormal() {
	}

	/**
	 * This method is used to obtain from the distribution its probability distribution
	 * function evaluated where required by the user.
	 *
	 * @param x double indicating where to evaluate the pdf.
	 * @param p parameter of the lognormal distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the probability distribution function evaluated in x.
	 */
	public double pdf(double x, Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			double mu = ((LognormalPar) p).getMu();
			double sigma = ((LognormalPar) p).getSigma();
			return (1/(x*sigma*Math.sqrt(2*Math.PI))) * Math.exp(-Math.pow((Math.log(x)-mu),2)/(2*sigma*sigma)); 
		} else {
			throw new IncorrectDistributionParameterException("Remember: standardDeviation must be gtz");
		}
	}

	/**
	 * This method is used to obtain from the distribution its cumulative distribution
	 * function evaluated where required by the user.
	 *
	 * @param x double indicating where to evaluate the cdf.
	 * @param p parameter of the lognormal distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the cumulative distribution function evaluated in x.
	 */
	public double cdf(double x, Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			double mu = ((LognormalPar) p).getMu();
			double sigma = ((LognormalPar) p).getSigma();
			return 0.5+0.5*Normal.erf((Math.log(x)-mu)/(Math.sqrt(2)*sigma));
		} else {
			throw new IncorrectDistributionParameterException("Remember: standardDeviation must be gtz");
		}
	}

	/**
	 * This method is used to obtain from the distribution the value of its own
	 * theoretic mean.
	 *
	 * @param p parameter of the lognormal distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the theoretic mean of the distribution.
	 */
	public double theorMean(Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			double mu = ((LognormalPar) p).getMu();
			double sigma = ((LognormalPar) p).getSigma();
			return Math.exp(mu+sigma*sigma/2);
		} else {
			throw new IncorrectDistributionParameterException("Remember: standardDeviation must be gtz");
		}
	}

	/**
	 * This method is used to obtain from the distribution the value of its own
	 * theoretic variance.
	 *
	 * @param p parameter of the lognormal distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the theoretic variance of the distribution.
	 */
	public double theorVariance(Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			double mu = ((LognormalPar) p).getMu();
			double sigma = ((LognormalPar) p).getSigma();
			return (Math.exp(sigma*sigma)-1)*Math.exp(2*mu+sigma*sigma) ;
		} else {
			throw new IncorrectDistributionParameterException("Remember: standardDeviation must be gtz");
		}
	}

	/**
	 * This method is used to obtain from the distribution the next number distributed
	 * according to the distribution parameter.
	 *
	 * @param p parameter of the lognormal distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the next random number of this distribution.
	 */
	public double nextRand(Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			// Sample N(0,1) from polar Box-Muller transformation.
			double x, y, r, z;
			double U1=engine.raw();
			double U2=engine.raw();
			z = Math.sqrt(-2.0 * Math.log(U1)) * Math.cos(2*Math.PI*U2);
			// If generated number is in the past, reruns this method
			double mu = ((LognormalPar) p).getMu();
			double sigma = ((LognormalPar) p).getSigma();
			return Math.exp(mu+sigma*z);
		} else {
			throw new IncorrectDistributionParameterException("Remember: sigma must be gtz");
		}
	}

} // end Lognormal
