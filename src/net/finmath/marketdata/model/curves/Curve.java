/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 20.05.2005
 */
package net.finmath.marketdata.model.curves;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Vector;

import net.finmath.interpolation.RationalFunctionInterpolation;
import net.finmath.marketdata.model.AnalyticModelInterface;

/**
 * This class represents a curve build from a set of points in 2D.
 * It provides different interpolation and extrapolation methods applied to a transformation of the input point,
 * examples are
 * <ul>
 * 	<li>linear interpolation of the input points</li>
 *  <li>linear interpolation of the log of the input points</li>
 * 	<li>cubic spline of the input points</li>
 *  <li>etc.</li>
 * </ul>
 * For the interpolation methods provided see {@link InterpolationMethod}.
 * For the extrapolation methods provided see {@link ExtrapolationMethod}.
 * For the possible interpolation entities see {@link InterpolationEntity}.
 * 
 * @author Christian Fries
 */
public class Curve extends AbstractCurve implements Serializable {

	/**
	 * Possible interpolation methods.
	 * @author Christian Fries
	 */
	public enum InterpolationMethod {
		/** Linear interpolation. **/
		LINEAR,
		/** Cubic spline interpolation. **/
		CUBIC_SPLINE
	}

	/**
	 * Possible extrapolation methods.
	 * @author Christian Fries
	 */
	public enum ExtrapolationMethod {
		/** Constant extrapolation. **/
		CONSTANT,
		/** Linear extrapolation. **/
		LINEAR
	}

	/**
	 * Possible interpolation entities.
	 * @author Christian Fries
	 */
	public enum InterpolationEntity {
		/** Interpolation is performed on the native point values **/
		VALUE,
		/** Interpolation is performed on the log of the point values **/
		LOG_OF_VALUE
	}

	private static class Point implements Comparable<Point>, Serializable {
        private static final long serialVersionUID = 8857387999991917430L;

        public double time;
		public double value;
		
		/**
         * @param time
         * @param value
         */
        public Point(double time, double value) {
	        super();
	        this.time = time;
	        this.value = value;
        }

		public int compareTo(Point point) {
			if(this.time < point.time) return -1;
			if(this.time > point.time) return +1;

			return 0;
		}
		
		public Object clone() {
			return new Point(time,value);
		}
	}

	private Vector<Point>		points	= new Vector<Point>();
	private InterpolationMethod	interpolationMethod	= InterpolationMethod.CUBIC_SPLINE;
	private ExtrapolationMethod	extrapolationMethod = ExtrapolationMethod.CONSTANT;
	private InterpolationEntity interpolationEntity = InterpolationEntity.LOG_OF_VALUE;
	
	private RationalFunctionInterpolation rationalFunctionInterpolation =  null;

	private static final long serialVersionUID = -4126228588123963885L;
	static NumberFormat	formatterReal = NumberFormat.getInstance(Locale.US);
	

	/**
	 * Create a curve using
	 * @param name
	 * @param interpolationMethod
	 * @param extrapolationMethod
	 * @param interpolationEntity
	 */
	public Curve(String name, InterpolationMethod interpolationMethod, ExtrapolationMethod extrapolationMethod, InterpolationEntity interpolationEntity) {
		super(name);
		this.interpolationMethod	= interpolationMethod;
		this.extrapolationMethod	= extrapolationMethod;
		this.interpolationEntity	= interpolationEntity;
	}

	
	/* (non-Javadoc)
	 * @see net.finmath.marketdata.model.curves.CurveInterface#getValue(double)
	 */
	public double getValue(double time)
	{
		return getValue(null, time);
	}


	/* (non-Javadoc)
	 * @see net.finmath.marketdata.model.curves.CurveInterface#getValue(double)
	 */
	public double getValue(AnalyticModelInterface model, double time)
	{
		return valueFromInterpolationEntity(getInterpolationEntityValue(time));
	}
	
	private double getInterpolationEntityValue(double time)
	{
		synchronized(this) {
			if(rationalFunctionInterpolation == null) {
				double[] pointsArray = new double[points.size()];
				double[] valuesArray = new double[points.size()];
				for(int i=0; i<points.size(); i++) {
					pointsArray[i] = points.get(i).time;
					valuesArray[i] = points.get(i).value;
				}
				rationalFunctionInterpolation = new RationalFunctionInterpolation(
						pointsArray,
						valuesArray,
						RationalFunctionInterpolation.InterpolationMethod.valueOf(this.interpolationMethod.toString()),
						RationalFunctionInterpolation.ExtrapolationMethod.valueOf(this.extrapolationMethod.toString())
						);
			}
		}
		return rationalFunctionInterpolation.getValue(time);
	}

	
	/**
	 * Add a point to this curve. The method will throw an exception if the point
	 * is already part of the curve.
	 * 
	 * @param time The x<sub>i</sub> in <<sub>i</sub> = f(x<sub>i</sub>).
	 * @param value The y<sub>i</sub> in <<sub>i</sub> = f(x<sub>i</sub>).
	 */
	public void addPoint(double time, double value) {
		double interpolationEntityValue = interpolationEntityFromValue(value);

		int index = getTimeIndex(time);
		if(index >= 0) {
			if(points.get(index).value == interpolationEntityValue) return;			// Already in list
			else throw new RuntimeException("Trying to add a value for a time for which another value already exists.");
		}
		else {
			// Insert the new point, retain ordering.
			points.add(-index-1, new Point(time, interpolationEntityValue));
		}
    	this.rationalFunctionInterpolation = null;
	}
	
	protected int getTimeIndex(double maturity) {
		Point df = new Point(maturity, Double.NaN);
		return java.util.Collections.binarySearch(points, df);
	}
	
	/* (non-Javadoc)
	 * @see net.finmath.marketdata.calibration.UnconstrainedParameterVectorInterface#getParameter()
	 */
    @Override
    public double[] getParameter() {
    	double[] parameters = new double[points.size()];
    	for(int i=0; i<points.size(); i++) {
    		parameters[i] = valueFromInterpolationEntity(points.get(i).value);
    	}
    	return parameters;
    }

	/* (non-Javadoc)
	 * @see net.finmath.marketdata.calibration.UnconstrainedParameterVectorInterface#setParameter(double[])
	 */
    @Override
    public void setParameter(double[] parameter) {
    	for(int i=0; i<points.size(); i++) {
    		points.get(i).value = interpolationEntityFromValue(parameter[i]);
    	}
    	this.rationalFunctionInterpolation = null;
    }

	public String toString() {
		String objectAsString = super.toString() + "\n";
    	for(int i=0; i<points.size(); i++) {
    		objectAsString = objectAsString + (points.get(i).time) + "\t" + valueFromInterpolationEntity(points.get(i).value) + "\n";
    	}
		return objectAsString;
	}
	
	private double interpolationEntityFromValue(double value) {
		switch(interpolationEntity) {
		case VALUE:
		default:
			return value;
		case LOG_OF_VALUE:
			return Math.log(value);
		}
	}

	private double valueFromInterpolationEntity(double interpolationEntityValue) {
		switch(interpolationEntity) {
		case VALUE:
		default:
			return interpolationEntityValue;
		case LOG_OF_VALUE:
			return Math.exp(interpolationEntityValue);
		}
	}


	@Override
	public CurveInterface getCloneForParameter(double[] parameter) {
		Curve newCurve = null;
		try {
			newCurve = (Curve) this.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		newCurve.points = new Vector<Point>();
		for(Point point : points) newCurve.points.add((Point) point.clone());

		newCurve.setParameter(parameter);
		
		return newCurve;
	}

}
