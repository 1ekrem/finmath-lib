package net.finmath.finitedifference.models;

import java.util.function.DoubleUnaryOperator;

/**
 * Interface one dimensional finite difference models.
 * 
 * @author Christian Fries
 */
public interface FiniteDifference1DModel {

	/**
	 * Return the conditional expectation of the given values at a given time contrained by the given boundary conditions.
	 * 
	 * @param time The time at which we observe values.
	 * @param values The values.
	 * @param boundary The given boundary conditions
	 * @return Vector of { states , values }.
	 */
	double[][] getValue(double time, DoubleUnaryOperator values, FiniteDifference1DBoundary boundary);

	double varianceOfStockPrice(double time);

	double getForwardValue(double time);

	double getRiskFreeRate();

	double getNumStandardDeviations();

	int getNumSpacesteps();

	double getVolatility();

}