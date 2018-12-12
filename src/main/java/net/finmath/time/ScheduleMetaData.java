package net.finmath.time;

import java.io.Serializable;
import java.time.LocalDate;

import net.finmath.modelling.descriptor.ScheduleDescriptor;
import net.finmath.time.ScheduleGenerator.DaycountConvention;
import net.finmath.time.ScheduleGenerator.Frequency;
import net.finmath.time.ScheduleGenerator.ShortPeriodConvention;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarInterface;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarInterface.DateRollConvention;

/**
 * Class to store any relevant information to generate schedules, which have different period structure but otherwise follow the same conventions.
 *
 * @author Christian Fries
 * @author Roland Bachl
 *
 */
public class ScheduleMetaData implements Serializable {

	private static final long serialVersionUID = 1547837440284116534L;

	/**
	 * Possible offset units to be used for schedule generation.
	 *
	 * @author Christian Fries
	 * @author Roland Bachl
	 *
	 */
	enum OffsetUnit {
		MONTHS,
		YEARS,
		DAYS,
		WEEKS
	}

	private final Frequency frequency;
	private final DaycountConvention daycountConvention;
	private final ShortPeriodConvention shortPeriodConvention;
	private final DateRollConvention dateRollConvention;
	private final BusinessdayCalendarInterface businessdayCalendar;
	private final int	fixingOffsetDays;
	private final int	paymentOffsetDays;
	private final boolean isUseEndOfMonth;

	/**
	 * Determines the offset code of a forward contract from a schedule. Rounds the average period length to full months.
	 *
	 * @param schedule
	 * @return The offset code as String
	 */
	public static String getOffsetCodeFromSchedule(ScheduleInterface schedule) {

		double doubleLength = 0;
		for(int i = 0; i < schedule.getNumberOfPeriods(); i ++) {
			doubleLength += schedule.getPeriodLength(i);
		}
		doubleLength /= schedule.getNumberOfPeriods();

		doubleLength *= 12;
		int periodLength = (int) Math.round(doubleLength);


		String offsetCode = periodLength + "M";
		return offsetCode;
	}

	/**
	 * Determines the offset code of a forward contract from the name of a forward curve.
	 * This method will extract a group of one or more digits together with the first letter behind them, if any.
	 * If there are multiple groups of digits in the name, this method will extract the last.
	 * If there is no number in the string, this method will return null.
	 *
	 * @param curveName
	 * @return The offset code as String
	 */
	public static String getOffsetCodeFromCurveName(String curveName) {
		if(curveName == null || curveName.length() == 0) {
			return null;
		}
		String[] splits = curveName.split("(?<=\\D)(?=\\d)");
		String offsetCode = splits[splits.length-1];
		if(!Character.isDigit(offsetCode.charAt(0))) {
			return null;
		}

		offsetCode = offsetCode.split("(?<=[A-Za-z])(?=.)", 2)[0];
		offsetCode = offsetCode.replaceAll( "[\\W_]", "" );
		return offsetCode;
	}

	/**
	 * Construct the ScheduleMetaData.
	 *
	 * @param frequency The default frequency.
	 * @param daycountConvention
	 * @param shortPeriodConvention
	 * @param dateRollConvention
	 * @param businessdayCalendar
	 * @param fixingOffsetDays
	 * @param paymentOffsetDays
	 * @param isUseEndOfMonth
	 */
	public ScheduleMetaData(Frequency frequency, DaycountConvention daycountConvention,
			ShortPeriodConvention shortPeriodConvention, DateRollConvention dateRollConvention,
			BusinessdayCalendarInterface businessdayCalendar, int fixingOffsetDays, int paymentOffsetDays,
			boolean isUseEndOfMonth) {
		super();
		this.frequency = frequency;
		this.daycountConvention = daycountConvention;
		this.shortPeriodConvention = shortPeriodConvention;
		this.dateRollConvention = dateRollConvention;
		this.businessdayCalendar = businessdayCalendar;
		this.fixingOffsetDays = fixingOffsetDays;
		this.paymentOffsetDays = paymentOffsetDays;
		this.isUseEndOfMonth = isUseEndOfMonth;
	}

	/**
	 * Generate a schedule descriptor for the given start and end date.
	 *
	 * @param startDate
	 * @param endDate
	 * @return The schedule descriptor
	 */
	public ScheduleDescriptor generateScheduleDescriptor(LocalDate startDate, LocalDate endDate) {
		return new ScheduleDescriptor(startDate, endDate, getFrequency(), getDaycountConvention(), getShortPeriodConvention(), getDateRollConvention(),
				getBusinessdayCalendar(), getFixingOffsetDays(), getPaymentOffsetDays(), isUseEndOfMonth());
	}

	/**
	 * Generate a schedule for the given start and end date.
	 *
	 * @param referenceDate
	 * @param startDate
	 * @param endDate
	 * @return The schedule
	 */
	public ScheduleInterface generateSchedule(LocalDate referenceDate, LocalDate startDate, LocalDate endDate) {
		return ScheduleGenerator.createScheduleFromConventions(referenceDate, startDate, endDate, getFrequency(), getDaycountConvention(),
				getShortPeriodConvention(), getDateRollConvention(), getBusinessdayCalendar(), getFixingOffsetDays(), getPaymentOffsetDays(), isUseEndOfMonth());
	}

	/**
	 * Generate a schedule with start / end date determined by an offset in months from the reference date.
	 *
	 * @param referenceDate
	 * @param maturity Offset of the start date to the reference date in months
	 * @param termination Offset of the end date to the reference date in months
	 * @return The schedule
	 */
	public ScheduleInterface generateSchedule(LocalDate referenceDate, int maturity, int termination) {
		return generateSchedule(referenceDate, maturity, termination, OffsetUnit.MONTHS);
	}

	/**
	 * Generate a schedule with start / end date determined by an offset from the reference date.
	 *
	 * @param referenceDate
	 * @param maturity Offset of the start date to the reference date
	 * @param termination Offset of the end date to the reference date
	 * @param unit The convention to use for the offset
	 * @return The schedule
	 */
	public ScheduleInterface generateSchedule(LocalDate referenceDate, int maturity, int termination, OffsetUnit unit) {

		LocalDate startDate;
		LocalDate endDate;

		switch(unit) {
		case YEARS :	startDate = referenceDate.plusYears(maturity);		endDate = startDate.plusYears(termination); break;
		case MONTHS :	startDate = referenceDate.plusMonths(maturity);		endDate = startDate.plusMonths(termination); break;
		case DAYS :		startDate = referenceDate.plusDays(maturity);		endDate = startDate.plusDays(termination); break;
		case WEEKS :	startDate = referenceDate.plusDays(maturity *7);	endDate = startDate.plusDays(termination *7); break;
		default :		startDate = referenceDate.plusMonths(maturity);		endDate = startDate.plusMonths(termination); break;
		}

		return generateSchedule(referenceDate, startDate, endDate);
	}

	/**
	 * @return the frequency
	 */
	public Frequency getFrequency() {
		return frequency;
	}

	/**
	 * @return the daycountConvention
	 */
	public DaycountConvention getDaycountConvention() {
		return daycountConvention;
	}

	/**
	 * @return the shortPeriodConvention
	 */
	public ShortPeriodConvention getShortPeriodConvention() {
		return shortPeriodConvention;
	}

	/**
	 * @return the dateRollConvention
	 */
	public DateRollConvention getDateRollConvention() {
		return dateRollConvention;
	}

	/**
	 * @return the businessdayCalendar
	 */
	public BusinessdayCalendarInterface getBusinessdayCalendar() {
		return businessdayCalendar;
	}

	/**
	 * @return the fixingOffsetDays
	 */
	public int getFixingOffsetDays() {
		return fixingOffsetDays;
	}

	/**
	 * @return the paymentOffsetDays
	 */
	public int getPaymentOffsetDays() {
		return paymentOffsetDays;
	}

	/**
	 * @return the isUseEndOfMonth
	 */
	public boolean isUseEndOfMonth() {
		return isUseEndOfMonth;
	}
}

