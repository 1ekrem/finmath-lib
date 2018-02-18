/*
 * (c) Copyright Christian P. Fries, Germany. Contact: email@christian-fries.de.
 *
 * Created on 09.02.2018
 */

package net.finmath.experimental.model.implementation;

import net.finmath.experimental.model.Product;
import net.finmath.experimental.model.ProductFactory;
import net.finmath.experimental.model.SingleAssetProductDescriptor;

/**
 * @author Christian Fries
 */
public class SingleAssetMonteCarloProductFactory implements ProductFactory<SingleAssetProductDescriptor> {

	/**
	 * Create factory.
	 */
	public SingleAssetMonteCarloProductFactory() {
	}

	@Override
	public Product<?> getProductFromDescription(SingleAssetProductDescriptor descriptor) {

		if(descriptor instanceof SingleAssetEuropeanOptionProductDescriptor) {
			Product<SingleAssetEuropeanOptionProductDescriptor> product = new net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption((SingleAssetEuropeanOptionProductDescriptor) descriptor);
			return product;
		}
		else {
			String name = descriptor.name();
			throw new IllegalArgumentException("Unsupported product type " + name);
		}
	}
}
