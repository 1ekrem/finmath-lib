/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christianfries.com.
 *
 * Created on 07.03.2015
 */

package net.finmath.marketdata.model.curves;

import java.util.Calendar;

import net.finmath.marketdata.model.AnalyticModelInterface;

/**
 * @author Christian Fries
 */
public class SeasonalCurve extends AbstractCurve implements CurveInterface {

	private CurveInterface baseCurve;

	/**
	 * A builder (following the builder pattern) for SeasonalCurve objects.
	 * Allows to successively construct a curve object by adding points to its base points.
	 * 
	 * @author Christian Fries
	 */
	public static class CurveBuilder extends Curve.CurveBuilder implements CurveBuilderInterface {

		private SeasonalCurve			curve = null;
		
		/**
		 * Create a CurveBuilder from a given seasonalCurve.
		 * 
		 * @param seasonalCurve The seasonal curve from which to copy the fixed part upon build().
		 * @throws CloneNotSupportedException Thrown, when the base curve could not be cloned.
		 */
		public CurveBuilder(SeasonalCurve seasonalCurve) throws CloneNotSupportedException {
			super((Curve)(seasonalCurve.baseCurve));
			this.curve = seasonalCurve;
		}
		
		@Override
		public CurveInterface build() throws CloneNotSupportedException {
			SeasonalCurve buildCurve = curve.clone();
			buildCurve.baseCurve = super.build();
			curve = null;
			return buildCurve;
		}
	}
	
	/**
	 * @param name
	 * @param referenceDate
	 */
	public SeasonalCurve(String name, Calendar referenceDate, CurveInterface baseCurve) {
		super(name, referenceDate);
		this.baseCurve = baseCurve;
	}

	@Override
	public double[] getParameter() {
		return baseCurve.getParameter();
	}

	@Override
	public void setParameter(double[] parameter) {
		baseCurve.setParameter(parameter);
	}

	@Override
	public double getValue(AnalyticModelInterface model, double time) {
		Calendar calendar = (Calendar) getReferenceDate().clone();
		calendar.add(Calendar.DAY_OF_YEAR, (int) Math.round(time*365));
		int month = calendar.get(Calendar.MONTH);			// Note: month = 0,1,2,...,11
		int day = calendar.get(Calendar.DAY_OF_MONTH);		// Note: day = 1,2,3,...,numberOfDays
		int numberOfDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		double season = month / 12.0 + (day-1) / (double)numberOfDays / 12.0;

		return baseCurve.getValue(model, season);
	}

	@Override
	public CurveInterface getCloneForParameter(double[] value) throws CloneNotSupportedException {
		SeasonalCurve newCurve = clone();
		newCurve.baseCurve = (CurveInterface) baseCurve.getCloneForParameter(value);
		
		return newCurve;
	}

	@Override
	public SeasonalCurve clone() throws CloneNotSupportedException {
		return new SeasonalCurve(this.getName(), this.getReferenceDate(), (CurveInterface) baseCurve.clone());
	}

	@Override
	public CurveBuilderInterface getCloneBuilder() throws CloneNotSupportedException {
		return new CurveBuilder(this);
	}
}
