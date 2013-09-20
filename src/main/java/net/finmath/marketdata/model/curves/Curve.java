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
 *  <li>linear interpolation of the log of the input points divided by their respective time</li>
 * 	<li>cubic spline of the input points</li>
 *  <li>etc.</li>
 * </ul>
 * For the interpolation methods provided see {@link net.finmath.marketdata.model.curves.Curve.InterpolationMethod}.
 * For the extrapolation methods provided see {@link net.finmath.marketdata.model.curves.Curve.ExtrapolationMethod}.
 * For the possible interpolation entities see {@link net.finmath.marketdata.model.curves.Curve.InterpolationEntity}.
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
		/** Interpolation is performed on the native point values, i.e. value(t) **/
		VALUE,
		/** Interpolation is performed on the log of the point values, i.e. log(value(t)) **/
		LOG_OF_VALUE,
		/** Interpolation is performed on the log of the point values divided by their respective time, i.e. log(value(t))/t **/
		LOG_OF_VALUE_PER_TIME
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

		@Override
        public int compareTo(Point point) {
			if(this.time < point.time) return -1;
			if(this.time > point.time) return +1;

			return 0;
		}
		
		@Override
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
	@Override
    public double getValue(double time)
	{
		return getValue(null, time);
	}


	/* (non-Javadoc)
	 * @see net.finmath.marketdata.model.curves.CurveInterface#getValue(double)
	 */
	@Override
    public double getValue(AnalyticModelInterface model, double time)
	{
		return valueFromInterpolationEntity(getInterpolationEntityValue(time), time);
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
		double interpolationEntityValue = interpolationEntityFromValue(value, time);

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
    		parameters[i] = valueFromInterpolationEntity(points.get(i).value, points.get(i).time);
    	}
    	return parameters;
    }

	/* (non-Javadoc)
	 * @see net.finmath.marketdata.calibration.UnconstrainedParameterVectorInterface#setParameter(double[])
	 */
    @Override
    public void setParameter(double[] parameter) {
    	for(int i=0; i<points.size(); i++) {
    		points.get(i).value = interpolationEntityFromValue(parameter[i], points.get(i).time);
    	}
    	this.rationalFunctionInterpolation = null;
    }

	public String toString() {
		String objectAsString = super.toString() + "\n";
        for (Point point : points) {
            objectAsString = objectAsString + point.time + "\t" + valueFromInterpolationEntity(point.value, point.time) + "\n";
        }
		return objectAsString;
	}
	
	private double interpolationEntityFromValue(double value, double time) {
		switch(interpolationEntity) {
		case VALUE:
		default:
			return value;
		case LOG_OF_VALUE:
			return Math.log(value);
		case LOG_OF_VALUE_PER_TIME:
			return Math.log(value) / time;
		}
	}

	private double valueFromInterpolationEntity(double interpolationEntityValue, double time) {
		switch(interpolationEntity) {
		case VALUE:
		default:
			return interpolationEntityValue;
		case LOG_OF_VALUE:
			return Math.exp(interpolationEntityValue);
		case LOG_OF_VALUE_PER_TIME:
			return Math.exp(interpolationEntityValue * time);
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
