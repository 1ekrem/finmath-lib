/*
 * (c) Copyright Christian P. Fries, Germany. All rights reserved. Contact: email@christian-fries.de.
 *
 * Created on 30.11.2012
 */
package net.finmath.marketdata.calibration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelInterface;
import net.finmath.marketdata.model.curves.Curve;
import net.finmath.marketdata.model.curves.CurveInterface;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveInterface;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurveFromDiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurveInterface;
import net.finmath.marketdata.products.AnalyticProductInterface;
import net.finmath.marketdata.products.Swap;
import net.finmath.marketdata.products.SwapLeg;
import net.finmath.optimizer.SolverException;
import net.finmath.time.RegularSchedule;
import net.finmath.time.ScheduleInterface;
import net.finmath.time.TimeDiscretization;

/**
 * Generate a collection of calibrated curves (discount curves, forward curves)
 * from a vector of calibration products.
 * 
 * @author Christian Fries
 */
public class CalibratedCurves {

	/**
	 * Specification of calibration product.
	 * 
     * @author Christian Fries
     */
    public static class CalibrationSpec {

    	private String				type;
    	
    	private	ScheduleInterface	swapTenorDefinitionReceiver;
		private String				forwardCurveReceiverName;
		private double				spreadReceiver;
		private String				discountCurveReceiverName;

		private ScheduleInterface	swapTenorDefinitionPayer;
		private String				forwardCurvePayerName;
		private double				spreadPayer;
		private String				discountCurvePayerName;
		
		private String				calibrationCurveName;
		private double				calibrationTime;

	       /**
         * Calibration specification.
         * 
         * @param type
         * @param swapTenorDefinitionReceiver
         * @param forwardCurveReceiverName
         * @param spreadReceiver
         * @param discountCurveReceiverName
         * @param swapTenorDefinitionPayer
         * @param forwardCurvePayerName
         * @param spreadPayer
         * @param discountCurvePayerName
         * @param calibrationCurveName
         * @param calibrationTime
         */
        public CalibrationSpec(
        		String type,
        		ScheduleInterface swapTenorDefinitionReceiver,
                String forwardCurveReceiverName, double spreadReceiver,
                String discountCurveReceiverName,
                ScheduleInterface swapTenorDefinitionPayer,
                String forwardCurvePayerName, double spreadPayer,
                String discountCurvePayerName,
                String calibrationCurveName,
                double calibrationTime) {
	        super();
	        this.type = type;
	        this.swapTenorDefinitionReceiver = swapTenorDefinitionReceiver;
	        this.forwardCurveReceiverName = forwardCurveReceiverName;
	        this.spreadReceiver = spreadReceiver;
	        this.discountCurveReceiverName = discountCurveReceiverName;
	        this.swapTenorDefinitionPayer = swapTenorDefinitionPayer;
	        this.forwardCurvePayerName = forwardCurvePayerName;
	        this.spreadPayer = spreadPayer;
	        this.discountCurvePayerName = discountCurvePayerName;
	        this.calibrationCurveName = calibrationCurveName;
	        this.calibrationTime = calibrationTime;
        }

        /**
         * Calibration specification.
         * 
         * @param type
         * @param swapTenorDefinitionReceiver
         * @param forwardCurveReceiverName
         * @param spreadReceiver
         * @param discountCurveReceiverName
         * @param swapTenorDefinitionPayer
         * @param forwardCurvePayerName
         * @param spreadPayer
         * @param discountCurvePayerName
         * @param calibrationCurveName
         * @param calibrationTime
         */
        public CalibrationSpec(
        		String type,
        		double[] swapTenorDefinitionReceiver,
                String forwardCurveReceiverName, double spreadReceiver,
                String discountCurveReceiverName,
                double[] swapTenorDefinitionPayer,
                String forwardCurvePayerName, double spreadPayer,
                String discountCurvePayerName,
                String calibrationCurveName,
                double calibrationTime) {
	        super();
	        this.type = type;
	        this.swapTenorDefinitionReceiver = new RegularSchedule(new TimeDiscretization(swapTenorDefinitionReceiver[0] /* initial */, swapTenorDefinitionReceiver[1] /* numberOfTimeSteps */, swapTenorDefinitionReceiver[2] /* deltaT */, TimeDiscretization.ShortPeriodLocation.SHORT_PERIOD_AT_START));
	        this.forwardCurveReceiverName = forwardCurveReceiverName;
	        this.spreadReceiver = spreadReceiver;
	        this.discountCurveReceiverName = discountCurveReceiverName;
	        this.swapTenorDefinitionPayer = new RegularSchedule(new TimeDiscretization(swapTenorDefinitionPayer[0] /* initial */, swapTenorDefinitionPayer[1] /* numberOfTimeSteps */, swapTenorDefinitionPayer[2] /* deltaT */, TimeDiscretization.ShortPeriodLocation.SHORT_PERIOD_AT_START));
	        this.forwardCurvePayerName = forwardCurvePayerName;
	        this.spreadPayer = spreadPayer;
	        this.discountCurvePayerName = discountCurvePayerName;
	        this.calibrationCurveName = calibrationCurveName;
	        this.calibrationTime = calibrationTime;
        }

        /**
         * Calibration specification.
         * 
         * @param type
         * @param swapTenorDefinitionReceiver
         * @param forwardCurveReceiverName
         * @param spreadReceiver
         * @param discountCurveReceiverName
         * @param calibrationCurveName
         * @param calibrationTime
         */
        public CalibrationSpec(
        		String type,
        		double[] swapTenorDefinitionReceiver,
                String forwardCurveReceiverName, double spreadReceiver,
                String discountCurveReceiverName,
                String calibrationCurveName,
                double calibrationTime) {
	        super();
	        this.type = type;
	        this.swapTenorDefinitionReceiver = new RegularSchedule(new TimeDiscretization(swapTenorDefinitionReceiver[0] /* initial */, swapTenorDefinitionReceiver[1] /* numberOfTimeSteps */, swapTenorDefinitionReceiver[2] /* deltaT */, TimeDiscretization.ShortPeriodLocation.SHORT_PERIOD_AT_START));
	        this.forwardCurveReceiverName = forwardCurveReceiverName;
	        this.spreadReceiver = spreadReceiver;
	        this.discountCurveReceiverName = discountCurveReceiverName;
	        this.calibrationCurveName = calibrationCurveName;
	        this.calibrationTime = calibrationTime;
        }

		@Override
		public String toString() {
			return "CalibrationSpec [type=" + type
					+ ", swapTenorDefinitionReceiver="
					+ swapTenorDefinitionReceiver
					+ ", forwardCurveReceiverName=" + forwardCurveReceiverName
					+ ", spreadReceiver=" + spreadReceiver
					+ ", discountCurveReceiverName="
					+ discountCurveReceiverName + ", swapTenorDefinitionPayer="
					+ swapTenorDefinitionPayer + ", forwardCurvePayerName="
					+ forwardCurvePayerName + ", spreadPayer=" + spreadPayer
					+ ", discountCurvePayerName=" + discountCurvePayerName
					+ ", calibrationCurveName=" + calibrationCurveName
					+ ", calibrationTime=" + calibrationTime + "]";
		}
      }

	private AnalyticModelInterface				model				= new AnalyticModel();
    private Set<CurveInterface>					curvesToCalibrate	= new HashSet<CurveInterface>();
	private Vector<AnalyticProductInterface>	calibrationProducts	= new Vector<AnalyticProductInterface>();
	
	private int lastNumberOfInterations;

	/**
	 * Generate a collection of calibrated curves (discount curves, forward curves)
	 * from a vector of calibration products.
	 * 
	 * @param calibrationSpecs Array of calibration specs.
	 * @throws net.finmath.optimizer.SolverException
	 */
	public CalibratedCurves(CalibrationSpec[] calibrationSpecs) throws SolverException {
		model	= new AnalyticModel();

		for(CalibrationSpec calibrationSpec : calibrationSpecs) {
			add(calibrationSpec);
		}

		lastNumberOfInterations = calibrate();
	}

	/**
	 * Generate a collection of calibrated curves (discount curves, forward curves)
	 * from a vector of calibration products and a given model.
	 * 
	 * WARNING: If the model already contains a curve referenced as calibration curve the
	 * calibration will modify/alter this curve. The result is currently undefined.
	 * 
	 * If the model does not contain the curve referenced as calibration curve, the
	 * curve will be added to the model. 
	 * 
	 * Use case: You already have a discount curve as part of the model and like
	 * to calibrate an additional curve to an additional set of instruments.
	 * 
	 * @param calibrationSpecs Array of calibration specs.
	 * @param calibrationModel A given model used to value the calibration products.
	 * @throws net.finmath.optimizer.SolverException
	 */
	public CalibratedCurves(CalibrationSpec[] calibrationSpecs, AnalyticModel calibrationModel) throws SolverException {
		model	= calibrationModel;

		for(CalibrationSpec calibrationSpec : calibrationSpecs) {
			add(calibrationSpec);
		}

		lastNumberOfInterations = calibrate();
	}

	/**
	 * Generate a collection of calibrated curves (discount curves, forward curves)
	 * from a vector of calibration products.
	 * 
	 * @param calibrationSpecs Array of calibration specs.
	 * @throws net.finmath.optimizer.SolverException
	 */
	public CalibratedCurves(Collection<CalibrationSpec> calibrationSpecs) throws SolverException {
		model	= new AnalyticModel();

		for(CalibrationSpec calibrationSpec : calibrationSpecs) {
			add(calibrationSpec);
		}

		lastNumberOfInterations = calibrate();
	}
	
	public AnalyticProductInterface getCalibrationProductForSpec(CalibrationSpec calibrationSpec) {
		createDiscountCurve(calibrationSpec.discountCurveReceiverName);
		createDiscountCurve(calibrationSpec.discountCurvePayerName);
		
		String forwardCurveReceiverName = createForwardCurve(calibrationSpec.swapTenorDefinitionReceiver, calibrationSpec.forwardCurveReceiverName);
		String forwardCurvePayerName	= createForwardCurve(calibrationSpec.swapTenorDefinitionPayer, calibrationSpec.forwardCurvePayerName);
		
		ScheduleInterface tenorReceiver = calibrationSpec.swapTenorDefinitionReceiver;
		ScheduleInterface tenorPayer	= calibrationSpec.swapTenorDefinitionPayer;

		AnalyticProductInterface product = null;
		if(calibrationSpec.type.toLowerCase().equals("swap")) {
			product = new Swap(tenorReceiver, forwardCurveReceiverName, calibrationSpec.spreadReceiver, calibrationSpec.discountCurveReceiverName, tenorPayer, forwardCurvePayerName, calibrationSpec.spreadPayer, calibrationSpec.discountCurvePayerName);
		}
		else if(calibrationSpec.type.toLowerCase().equals("swapleg")) {
			product = new SwapLeg(tenorReceiver, forwardCurveReceiverName, calibrationSpec.spreadReceiver, calibrationSpec.discountCurveReceiverName, true);
		}
		else {
			throw new RuntimeException("Product of type " + calibrationSpec.type + " unknown.");
		}

		return product;
	}
	
	/**
	 * Return the calibrated model, i.e., the model maintaining a collection of curves calibrated to the
	 * given calibration specifications.
	 * 
	 * @return The calibrated model.
	 */
	public AnalyticModelInterface getModel() {
		return model;
	}

	/**
	 * Get a curve for a given name.
	 * 
	 * @param name Name of the curve
	 * @return The curve model.
	 */
	public CurveInterface getCurve(String name) {
		return model.getCurve(name);
	}

	/**
	 * Return the number of calibrations needed to calibrate the model.
	 * 
	 * @return The number of calibrations needed to calibrate the model.
	 */
	public int getLastNumberOfInterations() {
		return lastNumberOfInterations;
	}

	private int calibrate() throws SolverException {
		Solver solver = new Solver(model, calibrationProducts);
		model = solver.getCalibratedModel(curvesToCalibrate);

		return solver.getIterations();
	}

	/**
	 * @param calibrationSpec
	 */
	private String add(CalibrationSpec calibrationSpec)
	{
		/* 
		 * Add one point to the calibration curve and one new objective function
		 */
		
		// Create calibration product (will also create the curve if necessary)
		calibrationProducts.add(getCalibrationProductForSpec(calibrationSpec));

		// Create parameter to calibrate

		Curve calibrationCurve = (Curve) model.getCurve(calibrationSpec.calibrationCurveName);
		if(DiscountCurveInterface.class.isInstance(calibrationCurve)) {
			double paymentTime	= calibrationSpec.swapTenorDefinitionPayer.getPayment(calibrationSpec.swapTenorDefinitionPayer.getNumberOfPeriods()-1);
			calibrationCurve.addPoint(paymentTime, 0.5);
			curvesToCalibrate.add(calibrationCurve);
		}
		else if(ForwardCurveInterface.class.isInstance(calibrationCurve)) {
			double fixingTime	= calibrationSpec.swapTenorDefinitionPayer.getFixing(calibrationSpec.swapTenorDefinitionPayer.getNumberOfPeriods()-1);
			double paymentTime	= calibrationSpec.swapTenorDefinitionPayer.getPayment(calibrationSpec.swapTenorDefinitionPayer.getNumberOfPeriods()-1);
			((Curve)((ForwardCurve)calibrationCurve).getPaymentOffsets()).addPoint(fixingTime, paymentTime-fixingTime);
			calibrationCurve.addPoint(fixingTime, 0.5);
			curvesToCalibrate.add(calibrationCurve);
		}
		else {
			calibrationCurve.addPoint(calibrationSpec.calibrationTime, 0.5);
			curvesToCalibrate.add(calibrationCurve);
		}
	
		return calibrationSpec.type;
	}

	/**
	 * @param discountCurveName
	 * @return The discount factor curve associated with the given name.
	 */
    private DiscountCurveInterface createDiscountCurve(String discountCurveName) {
		DiscountCurveInterface discountCurve	= model.getDiscountCurve(discountCurveName);
		if(discountCurve == null) {
			discountCurve = DiscountCurve.createDiscountCurveFromDiscountFactors(discountCurveName, new double[] { }, new double[] { });
			model.setCurve(discountCurve);
	}

		return discountCurve;
    }

	/**
	 * @param swapTenorDefinition
	 * @param forwardCurveName
	 * @return The forward curve associated with the given name.
	 */
    private String createForwardCurve(ScheduleInterface swapTenorDefinition, String forwardCurveName) {

    	if(forwardCurveName == null || forwardCurveName.isEmpty()) return null;

		// Check if the curves exists, if not create it
		CurveInterface	curve = model.getCurve(forwardCurveName);

		ForwardCurveInterface	forwardCurve = null;
		if(curve == null) {
			// Create a new forward curve
			forwardCurve = new ForwardCurve(forwardCurveName, ForwardCurve.InterpolationEntityForward.FORWARD, null);
		} else if(DiscountCurveInterface.class.isInstance(curve)) {
			/*
			 *  If the specified forward curve exits as a discount curve, we interpret this as "single-curve"
			 *  in the sense that we generate a forward curve wrapping the discount curve and calculating
			 *  forward from discount factors using the formula (df(T)/df(T+Delta T) - 1) / Delta T).
			 */
			double periodLength	= swapTenorDefinition.getPeriodLength(0);		// @TODO It is unclear which periodLength to use for the forward curve, this is just an approximation
			forwardCurve = new ForwardCurveFromDiscountCurve(curve.getName(), periodLength);
		} else {
			// Use a given forward curve
			forwardCurve = (ForwardCurveInterface)curve;
		}

		model.setCurve(forwardCurve);

		return forwardCurve.getName();
    }
}
