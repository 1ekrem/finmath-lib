/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 20.05.2005
 */
package net.finmath.montecarlo;

import java.util.Map;

import net.finmath.exception.CalculationException;
import net.finmath.stochastic.RandomVariableInterface;
import net.finmath.time.TimeDiscretizationInterface;

/**
 * The interface implemented by a simulation of an SDE.
 * Provides the dimension of the SDE and the the time discretization of the
 * simulation.
 * 
 * @author Christian Fries
 */
public interface MonteCarloSimulationInterface {
	
	/**
	 * @return Returns the numberOfPaths.
	 */
    int getNumberOfComponents();

	/**
	 * @return Returns the numberOfPaths.
	 */
    int getNumberOfPaths();

	/**
	 * @return Returns the timeDiscretization.
	 */
    TimeDiscretizationInterface getTimeDiscretization();

	/**
	 * @param timeIndex Time index
	 * @return Returns the time for a given time index.
	 */
    double getTime(int timeIndex);

	/**
	 * @param time
	 * @return Returns the time index for a given time
	 */
    int getTimeIndex(double time);

	/**
	 * This method returns the weights of a weighted Monte Carlo method (the probability density).
	 * 
	 * @param timeIndex Time index at which the process should be observed
	 * @return A vector of positive weights which sums up to one
	 * @throws net.finmath.exception.CalculationException
	 */
    RandomVariableInterface getMonteCarloWeights(int timeIndex) throws CalculationException;

	/**
	 * This method returns the weights of a weighted Monte Carlo method (the probability density).
	 * 
	 * @param time Time at which the process should be observed
	 * @return A vector of positive weights which sums up to one
	 * @throws net.finmath.exception.CalculationException
	 */
    RandomVariableInterface getMonteCarloWeights(double time) throws CalculationException;

	/**
	 * Create a clone of this simulation modifying some of its properties (if any).
	 * 
	 * @param dataModified The data which should be changed in the new model
	 * @return Returns a clone of this model, with some data modified (then it is no longer a clone :-)
	 * @throws net.finmath.exception.CalculationException
	 */
    MonteCarloSimulationInterface getCloneWithModifiedData(Map<String, Object> dataModified) throws CalculationException;
}
