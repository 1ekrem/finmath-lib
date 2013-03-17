package net.finmath.montecarlo.process.component.factordrift;

import net.finmath.exception.CalculationException;
import net.finmath.stochastic.ImmutableRandomVariableInterface;

public interface FactorDriftInterface {

	/**
	 * The interface describes how an additional factor scaling may be specified for the generation of a process (see e.g. LogNormalProcess).
	 * The factor scaling may be used to implement importance sampling or proxy simulation on the level of the discrete process.
	 * 
	 * @param timeIndex
	 * @param realizationPredictor
	 * @return
	 */
	ImmutableRandomVariableInterface[]	getFactorScaling(int timeIndex, ImmutableRandomVariableInterface[] realizationPredictor);

	/**
	 * The interface describes how an additional factor drift may be specified for the generation of a process (see e.g. LogNormalProcess).
	 * The factor drift may be used to implement importance sampling or proxy simulation on the level of the discrete process.
	 * 
	 * @param timeIndex
	 * @param realizationPredictor
	 * @return A vector of random variables given the factor drift for each factor. If the size is less then the number of factors, then higher order factors have no drift.
	 * @throws CalculationException 
	 */
	ImmutableRandomVariableInterface[]	getFactorDrift(int timeIndex, ImmutableRandomVariableInterface[] realizationPredictor) throws CalculationException;

    /**
     * The interface describes how an additional factor drift may be specified for the generation of a process (see e.g. LogNormalProcess).
     * The factor drift may be used to implement importance sampling or proxy simulation on the level of the discrete process.
     * 
     * @param timeIndex
     * @param realizationPredictor
     * @return
     */
	ImmutableRandomVariableInterface    getFactorDriftDeterminant(int timeIndex, ImmutableRandomVariableInterface[] realizationPredictor);
}
