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

/**
 * 
 * This is the Normal distribution (see the constructor
 * description for its pdf definition).
 * 
 * <br><br>Copyright (c) 2003
 * <br>Politecnico di Milano - dipartimento di Elettronica e Informazione
 * @author Fabrizio Frontera - ffrontera@yahoo.it
 * @author Modified by Stefano Omini, 7/5/2004
 * @author Modified by Bertoli Marco, 8/9/2005
 * 
 */
public class Normal extends AbstractDistribution implements Distribution {

	/**
	 * This is the constructor. It creates a new normal distribution which is
	 * defined from pdf:
	 * <pre>               1                   (x-m)^2
	 * pdf(x) = -------------- * exp (- ----------- )
	 *           sqrt(2*pi)*v              2v^2</pre>
	 * where v^2 is the variance and m is the mean of the distribution
	 * pi is the pi-greco constant.
	 */
	public Normal() {
	}

	/**
	 * This method is used to obtain from the distribution its probability distribution
	 * function evaluated where required by the user.
	 *
	 * @param x double indicating where to evaluate the pdf.
	 * @param p parameter of the normal distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the probability distribution function evaluated in x.
	 */
	public double pdf(double x, Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			double variance = ((NormalPar) p).getStandardDeviation() * ((NormalPar) p).getStandardDeviation();
			double SQRT_INV = 1.0 / Math.sqrt(2.0 * Math.PI * variance);
			double mean = ((NormalPar) p).getMean();
			double diff = x - mean;
			return SQRT_INV * Math.exp(-(diff * diff) / (2.0 * variance));
		} else {
			throw new IncorrectDistributionParameterException("Remember: standardDeviation must be gtz");
		}
	}

	/**
	 * This method is used to obtain from the distribution its cumulative distribution
	 * function evaluated where required by the user.
	 *
	 * @param x double indicating where to evaluate the cdf.
	 * @param p parameter of the normal distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the cumulative distribution function evaluated in x.
	 */
	public double cdf(double x, Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			double mean = ((NormalPar) p).getMean();
			double variance = ((NormalPar) p).getStandardDeviation() * ((NormalPar) p).getStandardDeviation();
			return Probability.normal(mean, variance, x);
		} else {
			throw new IncorrectDistributionParameterException("Remember: standardDeviation must be gtz");
		}
	}

	/**
	 * This method is used to obtain from the distribution the value of its own
	 * theoretic mean.
	 *
	 * @param p parameter of the normal distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the theoretic mean of the distribution.
	 */
	public double theorMean(Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			return ((NormalPar) p).getMean();
		} else {
			throw new IncorrectDistributionParameterException("Remember: standardDeviation must be gtz");
		}
	}

	/**
	 * This method is used to obtain from the distribution the value of its own
	 * theoretic variance.
	 *
	 * @param p parameter of the normal distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the theoretic variance of the distribution.
	 */
	public double theorVariance(Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			return ((NormalPar) p).getStandardDeviation() * ((NormalPar) p).getStandardDeviation();
		} else {
			throw new IncorrectDistributionParameterException("Remember: standardDeviation must be gtz");
		}
	}

	/**
	 * This method is used to obtain from the distribution the next number distributed
	 * according to the distribution parameter.
	 *
	 * @param p parameter of the normal distribution.
	 * @throws IncorrectDistributionParameterException
	 * @return double with the next random number of this distribution.
	 */
	public double nextRand(Parameter p) throws IncorrectDistributionParameterException {
		if (p.check()) {
			double mean = ((NormalPar) p).getMean();
			double standardDeviation = ((NormalPar) p).getStandardDeviation();
			// Uses polar Box-Muller transformation.
			double U1 = engine.raw();
			double U2 = engine.raw();
			double z = Math.sqrt(-2.0 * Math.log(U1)) * Math.cos(2 * Math.PI * U2);
			double ret = mean + standardDeviation * z;
			// If generated number is in the past, reruns this method
			return (ret > 0.0) ? ret : nextRand(p);
		} else {
			throw new IncorrectDistributionParameterException("Remember: standardDeviation must be gtz");
		}
	}

} // end Normal
