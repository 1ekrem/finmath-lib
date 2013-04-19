/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 13.08.2004
 */
package net.finmath.montecarlo.conditionalexpectation;

import net.finmath.stochastic.ImmutableRandomVariableInterface;
import net.finmath.stochastic.RandomVariableInterface;

/**
 * The interface which has to be implemented by a fixed conditional expectation operator,
 *  i.e., E( &middot; | Z ) for a fixed Z.
 *
 * @author Christian Fries
 */
public interface MonteCarloConditionalExpectation {
	
    /**
     * Return the conditional expectation of a given random variable.
     * The definition of the filtration time is part of the object implementing this interface.
     * 
     * @param randomVariable
     * @return
     */
    RandomVariableInterface getConditionalExpectation(ImmutableRandomVariableInterface randomVariable);
}
