package net.finmath.montecarlo.conditionalexpectation;

import java.util.List;
import java.util.function.Function;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.AbstractMonteCarloProduct;
import net.finmath.montecarlo.MonteCarloSimulationInterface;
import net.finmath.stochastic.RandomVariableInterface;

public class RegressionBasisFunctionsFromProducts implements RegressionBasisFunctionsProvider {

	private final List<AbstractMonteCarloProduct> products;

	public RegressionBasisFunctionsFromProducts(List<AbstractMonteCarloProduct> products) {
		super();
		this.products = products;
	}

	@Override
	public RandomVariableInterface[] getBasisFunctions(double evaluationTime, MonteCarloSimulationInterface model) {

		Function<AbstractMonteCarloProduct, RandomVariableInterface> valuation = p -> {
			RandomVariableInterface value = null;
			try {
				value = p.getValue(evaluationTime, model);
			} catch (CalculationException e) {
				throw new IllegalArgumentException("Product " + p + " cannot be valued by model " + model + " at time " + evaluationTime, e);
			}

			if(value.getFiltrationTime() > evaluationTime) {
				throw new IllegalArgumentException(
						"Product " + p + " valued by model " + model + " cannot be used as basis function at time " + evaluationTime + ". "
						+ "Filtration time is " + value.getFiltrationTime());
			}

			return value;
		};

		return products.stream().map(valuation).toArray(RandomVariableInterface[]::new);
	}
}
