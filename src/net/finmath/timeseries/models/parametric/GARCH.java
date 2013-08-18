/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 * 
 * Created on 15.07.2012
 */


package net.finmath.timeseries.models.parametric;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.finmath.timeseries.HistoricalSimulationModel;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;

/**
 * Lognormal process with GARCH(1,1) volatility.
 * 
 * This class estimates the process
 *   d log(X) = sigma dW
 * 
 * @author Christian Fries
 */
public class GARCH implements HistoricalSimulationModel {

	private double[] values;	
	private int windowIndexStart;
	private int windowIndexEnd;
	private int maxIterations = 1000000;

	/**
	 * Create GARCH model estimated form the given time series of values.
	 * 
	 * @param values
	 */
	public GARCH(double[] values) {
		this.values = values;
		this.windowIndexStart	= 0;
		this.windowIndexEnd		= values.length-1;
	}

	public GARCH(double[] values, int windowIndexStart, int windowIndexEnd) {
		this.values = values;
		this.windowIndexStart	= windowIndexStart;
		this.windowIndexEnd		= windowIndexEnd;
	}
	
	public GARCH getCloneWithWindow(int windowIndexStart, int windowIndexEnd) {
		return new GARCH(this.values, windowIndexStart, windowIndexEnd);
	}

	/**
	 * Get log likelihood of the sample time series for given model parameters.
	 * 
	 * @param omega
	 * @param alpha
	 * @param beta
	 * @return
	 */
	public double getLogLikelihoodForParameters(double omega, double alpha, double beta)
	{
		double logLikelihood = 0.0;

		double volScaling	= 1.0;
		double h			= omega / (1.0 - alpha - beta);
		for (int i = windowIndexStart+1; i <= windowIndexEnd-1; i++) {
			double eval		= volScaling * (Math.log((values[i])/(values[i-1])));
	        h = (omega + alpha * eval * eval) + beta * h;
			double evalNext	= volScaling * (Math.log((values[i+1])/(values[i])));

	        logLikelihood += - Math.log(h) - evalNext*evalNext / h;
		}
		logLikelihood += - Math.log(2 * Math.PI) * (double)(windowIndexEnd-windowIndexStart);
		logLikelihood *= 0.5;
		
		return logLikelihood;
	}
	
	/**
	 * Returns the last estimate of the time series volatility.
	 * 
	 * @param omega
	 * @param alpha
	 * @param beta
	 * @return
	 */
	public double getLastResidualForParameters(double omega, double alpha, double beta) {
		double volScaling = 1.0;
		double h = omega / (1.0 - alpha - beta);
		for (int i = windowIndexStart+1; i <= windowIndexEnd; i++) {
			double eval		= volScaling * (Math.log((values[i])/(values[i-1])));
	        h = omega + alpha * eval * eval + beta * h;
		}
		
		return h;
	}
	
	public double[] getQuantilPredictionsForParameters(double omega, double alpha, double beta, double[] quantiles) {
		double[] szenarios = new double[windowIndexEnd-windowIndexStart+1-1];

		double volScaling = 1.0;
		double h = omega / (1.0 - alpha - beta);
		double vol = Math.sqrt(h) * volScaling;
		for (int i = windowIndexStart+1; i <= windowIndexEnd; i++) {
			szenarios[i-windowIndexStart-1]	= Math.log((values[i])/(values[i-1])) / vol;

			double eval		= volScaling * (Math.log((values[i])/(values[i-1])));
	        h = omega + alpha * eval * eval + beta * h;
	        vol = Math.sqrt(h) * volScaling;
		}
		java.util.Arrays.sort(szenarios);

		double[] quantileValues = new double[quantiles.length];
		for(int i=0; i<quantiles.length; i++) {
			double quantile = quantiles[i];
			double quantileIndex = szenarios.length * quantile  - 1;
			int quantileIndexLo = (int)quantileIndex;
			int quantileIndexHi = quantileIndexLo+1;

			double szenarioRelativeChange =
				    (quantileIndexHi-quantileIndex) * Math.exp(szenarios[Math.max(quantileIndexLo,0               )] * vol)
				  + (quantileIndex-quantileIndexLo) * Math.exp(szenarios[Math.min(quantileIndexHi,szenarios.length)] * vol);

			double quantileValue = (values[windowIndexEnd]) * szenarioRelativeChange; 
			quantileValues[i] = quantileValue;
		}
		
		return quantileValues;
	}

	/* (non-Javadoc)
	 * @see net.finmath.timeseries.HistoricalSimulationModel#getBestParameters()
	 */
	public Map<String, Double> getBestParameters() {
		return getBestParameters(null);
	}
	
	/* (non-Javadoc)
	 * @see net.finmath.timeseries.HistoricalSimulationModel#getBestParameters(java.util.Map)
	 */
	public Map<String, Double> getBestParameters(Map<String, Double> guess) {

		// Create the objective function for the solver
		class GARCHMaxLikelihoodFunction implements MultivariateFunction, Serializable {

			private static final long serialVersionUID = 7072187082052755854L;

		    public double value(double[] variables) {
				/*
				 * Transform variables: The solver variables are in (-\infty, \infty).
				 * We transform the variable to the admissible domain for GARCH, that is
				 * omega > 0, 0 < alpha < 1, 0 < beta < (1-alpha), displacement > lowerBoundDisplacement
				 */
				double omega	= Math.exp(variables[0]);
				double mucorr	= Math.exp(-Math.exp(-variables[1]));
				double muema	= Math.exp(-Math.exp(-variables[2]));
				double beta		= mucorr * muema;
				double alpha	= mucorr - beta;

		    	double logLikelihood = getLogLikelihoodForParameters(omega,alpha,beta);
		    	
				// Penalty to prevent solver from hitting the bounds
		    	logLikelihood -= Math.max(1E-30-omega,0)/1E-30;
				logLikelihood -= Math.max(1E-30-alpha,0)/1E-30;
				logLikelihood -= Math.max((alpha-1)+1E-30,0)/1E-30;
				logLikelihood -= Math.max(1E-30-beta,0)/1E-30;
				logLikelihood -= Math.max((beta-1)+1E-30,0)/1E-30;

				return logLikelihood;
		    }

		}
		GARCHMaxLikelihoodFunction objectiveFunction = new GARCHMaxLikelihoodFunction();

		// Create a guess for the solver
		double guessOmega = 1.0;
		double guessAlpha = 0.2;
		double guessBeta = 0.2;
		if(guess != null) {
			// A guess was provided, use that one
			guessOmega			= guess.get("Omega");
			guessAlpha			= guess.get("Alpha");
			guessBeta			= guess.get("Beta");
		}

		// Constrain guess to admissible range
		guessOmega			= restrictToOpenSet(guessOmega, 0.0, Double.MAX_VALUE);
		guessAlpha			= restrictToOpenSet(guessAlpha, 0.0, 1.0);
		guessBeta			= restrictToOpenSet(guessBeta, 0.0, 1.0-guessAlpha);


		double guessMucorr	= guessAlpha + guessBeta;
		double guessMuema	= guessBeta / (guessAlpha+guessBeta);

		// Transform guess to solver coordinates
		double[] guessParameters = new double[3];
		guessParameters[0] = Math.log(guessOmega);
		guessParameters[1] = -Math.log(-Math.log(guessMucorr));
		guessParameters[2] = -Math.log(-Math.log(guessMuema));

		// Seek optimal parameter configuration
//		org.apache.commons.math3.optimization.direct.BOBYQAOptimizer optimizer2 = new org.apache.commons.math3.optimization.direct.BOBYQAOptimizer(5);
		org.apache.commons.math3.optimization.direct.CMAESOptimizer optimizer2 = new org.apache.commons.math3.optimization.direct.CMAESOptimizer();

		double[] bestParameters = null;
		try {
			PointValuePair result = optimizer2.optimize(
				maxIterations,
				objectiveFunction,
				GoalType.MAXIMIZE,
				guessParameters /* start point */
				);
			bestParameters = result.getPoint();
		} catch(org.apache.commons.math3.exception.MathIllegalStateException e) {
			// Retry with new guess. This guess corresponds to omaga=1, alpha=0.5; beta=0.25; displacement=1+lowerBoundDisplacement;
			double[] guessParameters2 = {0.0, 0.0, 0.0};
/*			PointValuePair result = optimizer2.optimize(
					maxIterations,
					objectiveFunction,
					GoalType.MAXIMIZE,
					guessParameters2
					);*/
			System.out.println("Solver failed");
			bestParameters = guessParameters2;//result.getPoint();
		}
		
		// Transform parameters to GARCH parameters
		double omega	= Math.exp(bestParameters[0]);
		double mucorr	= Math.exp(-Math.exp(-bestParameters[1]));
		double muema	= Math.exp(-Math.exp(-bestParameters[2]));
		double beta		= mucorr * muema;
		double alpha	= mucorr - beta;

		double[] quantiles = {0.01, 0.05, 0.5};
		double[] quantileValues = this.getQuantilPredictionsForParameters(omega, alpha, beta, quantiles);

		Map<String, Double> results = new HashMap<String, Double>();
		results.put("Omega", omega);
		results.put("Alpha", alpha);
		results.put("Beta", beta);
		results.put("Likelihood", this.getLogLikelihoodForParameters(omega, alpha, beta));
		results.put("Vol", Math.sqrt(this.getLastResidualForParameters(omega, alpha, beta)));
		results.put("Quantile=1%", quantileValues[0]);
		results.put("Quantile=5%", quantileValues[1]);
		results.put("Quantile=50%", quantileValues[2]);
		return results;
	}

	private static double restrictToOpenSet(double value, double lowerBond, double upperBound) {
		value = Math.max(value, lowerBond  * (1.0+Math.signum(lowerBond)*1E-15) + 1E-15);
		value = Math.min(value, upperBound * (1.0-Math.signum(upperBound)*1E-15) - 1E-15);
		return value;
	}
}
