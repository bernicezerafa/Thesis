package thesis.timetable_generation;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Constraint implements Serializable {

	private static final long serialVersionUID = 1L;

	/* Hard Constraint: Clashes i.e. exams at the same time */
	public static final int CLASH_PUNISH = 0;

	/* Hard Constraint: Evening exams must be held in the evening 
	 * during weekdays, or on a Saturday morning */
	public static final int EVENING_PUNISH = 1;
	
	/* Soft Constraint: exams on the same day */
	public static final int SAME_DAY_PUNISH = 2;
	
	/* Soft Constraint: exams held on the evening of one day
	 * and the morning of the following day (worse than two
	 * consecutive days but both in the morning / evening */
	public static final int DAY_EVENING_PUNISH = 3;
	
	/* Soft Constraint: exams on two consecutive days */
	public static final int TWO_DAYS_PUNISH = 4;
	
	/* Soft Constraint: exams on three consecutive days */
	public static final int THREE_DAYS_PUNISH = 5;
	
	/* Soft Constraint: exams better be spread out i.e. not more 
	 * than average (no of exams / no of available timeslots) 
	 * exams in a timeslot */	
	public static final int SPREAD_OUT_PUNISH = 6;
	
	/* Soft Constraint: exams with large number of students are
	 * more comfortable in the start of the timetable so that
	 * lecturers have time to correct */
	public static final int NO_OF_STUDENTS_PUNISH = 7;
	
	public static final int VIOLATION_ADDED = 0;
	public static final int VIOLATION_DROPPED = 1;
	public static final int EXAM_VIOLATION_ADDED = 2;
	public static final int EXAM_VIOLATION_DROPPED = 3;
	public static final int STUDENT_ADDED = 4;
	public static final int STUDENT_DROPPED = 5;
	public static final int NOT_CHANGED = 6;
	
	private int clashPunish;
	private int sameDayPunish;
	private int twoDayPunish;
	private int threeDayPunish;
	private int eveningPunish;
	private int twentyHourPunish;
	private int spreadOutPunish;
	private int noOfStudentsPunish;

	public int getClashPunish() {
		return clashPunish;
	}
	
	public void setClashPunish(int clashPunish) {
		this.clashPunish = clashPunish;
	}
	
	public int getSameDayPunish() {
		return sameDayPunish;
	}
	
	public void setSameDayPunish(int sameDayPunish) {
		this.sameDayPunish = sameDayPunish;
	}
	
	public int getTwoDayPunish() {
		return twoDayPunish;
	}
	
	public void setTwoDayPunish(int twoDayPunish) {
		this.twoDayPunish = twoDayPunish;
	}
	
	public int getThreeDayPunish() {
		return threeDayPunish;
	}
	
	public void setThreeDayPunish(int threeDayPunish) {
		this.threeDayPunish = threeDayPunish;
	}
	
	public int getTwentyHourPunish() {
		return twentyHourPunish;
	}
	
	public void setTwentyHourPunish(int twentyHourPunish) {
		this.twentyHourPunish = twentyHourPunish;
	}
	
	public void setEveningPunish(int eveningPunish) {
		this.eveningPunish = eveningPunish;
	}
	
	public int getEveningPunish() {
		return eveningPunish;
	}
	
	public void setNoOfStudentsPunish(int noOfStudentsPunish) {
		this.noOfStudentsPunish = noOfStudentsPunish;
	}
	
	public int getNoOfStudentsPunish() {
		return noOfStudentsPunish;
	}
	
	public void setSpreadOutPunish(int spreadOutPunish) {
		this.spreadOutPunish = spreadOutPunish;
	}
	
	public int getSpreadOutPunish() {
		return spreadOutPunish;
	}	
		
	public static boolean equalViolationMaps(HashMap<Integer, HashMap<IntTuple, ArrayList<String>>> violationMapBefore,
			 HashMap<Integer, HashMap<IntTuple, ArrayList<String>>> violationMapAfter,
			 int violationKey)
	{
		HashMap<IntTuple, ArrayList<String>> violationEntryBefore = violationMapBefore.get(violationKey);
		HashMap<IntTuple, ArrayList<String>> violationEntryAfter = violationMapAfter.get(violationKey);
		
		// if both have an entry, check if they are equal
		if (violationEntryBefore != null && violationEntryAfter != null) {
		
			if (violationEntryBefore.equals(violationEntryAfter))
				return true;
			else
				return false;
		}
		
		// if one is null and the other is not, not equal
		if (violationEntryBefore == null && violationEntryAfter != null) {
			return false;
		}
		
		// if one is null and the other is not, not equal
		if (violationEntryBefore != null && violationEntryAfter == null) {
			return false;
		}
		
		// if both null, equal
		return true;
	}
	
	// get number of students affected by a violation
	public static int getNoOfStudentsAffected(Constraint violations, int violationKey) {
		
		int noOfStudents = -1;
		
		switch (violationKey) {
		
			case Constraint.CLASH_PUNISH: noOfStudents = violations.getClashPunish(); break;
			case Constraint.EVENING_PUNISH: noOfStudents = violations.getEveningPunish(); break;
			case Constraint.SAME_DAY_PUNISH: noOfStudents = violations.getSameDayPunish(); break;
			case Constraint.TWO_DAYS_PUNISH: noOfStudents = violations.getTwoDayPunish(); break;
			case Constraint.DAY_EVENING_PUNISH: noOfStudents = violations.getTwentyHourPunish(); break;
			case Constraint.THREE_DAYS_PUNISH: noOfStudents = violations.getThreeDayPunish(); break;
			case Constraint.NO_OF_STUDENTS_PUNISH: noOfStudents = violations.getNoOfStudentsPunish(); break;
		}
		
		return noOfStudents;
	}
	
    public static ArrayList<Integer> getAffectedViolations(BestChromosome chromosomeBefore, BestChromosome chromosomeAfter) {
    	
    	ArrayList<Integer> violations = new ArrayList<Integer>();
    	
    	Constraint constraintBefore = chromosomeBefore.getConstraintViolations();
    	Constraint constraintAfter = chromosomeAfter.getConstraintViolations();
    	
    	HashMap<Integer, HashMap<IntTuple, ArrayList<String>>> violationMapBefore = chromosomeBefore.getViolationMap();
    	HashMap<Integer, HashMap<IntTuple, ArrayList<String>>> violationMapAfter = chromosomeAfter.getViolationMap();
    	
    	if (constraintBefore.getClashPunish() != constraintAfter.getClashPunish()
    		|| !equalViolationMaps(violationMapBefore, violationMapAfter, Constraint.CLASH_PUNISH)) {
    		
    		violations.add(Constraint.CLASH_PUNISH);
    	}
    	
    	if (constraintBefore.getEveningPunish() != constraintAfter.getEveningPunish()
    		|| !equalViolationMaps(violationMapBefore, violationMapAfter, Constraint.EVENING_PUNISH)) {
    		
    		violations.add(Constraint.EVENING_PUNISH);
    	}
    	
    	if (constraintBefore.getSameDayPunish() != constraintAfter.getSameDayPunish()
    		|| !equalViolationMaps(violationMapBefore, violationMapAfter, Constraint.SAME_DAY_PUNISH)) {
    	    
    		violations.add(Constraint.SAME_DAY_PUNISH);
    	}
    	
    	if (constraintBefore.getTwentyHourPunish() != constraintAfter.getTwentyHourPunish()
    		|| !equalViolationMaps(violationMapBefore, violationMapAfter, Constraint.DAY_EVENING_PUNISH)) {
    		
    		violations.add(Constraint.DAY_EVENING_PUNISH);
    	}
    	
    	if (constraintBefore.getTwoDayPunish() != constraintAfter.getTwoDayPunish()
    		|| !equalViolationMaps(violationMapBefore, violationMapAfter, Constraint.TWO_DAYS_PUNISH)) {
    		
    		violations.add(Constraint.TWO_DAYS_PUNISH);
    	}
    	
    	if (constraintBefore.getThreeDayPunish() != constraintAfter.getThreeDayPunish()
    		|| !equalViolationMaps(violationMapBefore, violationMapAfter, Constraint.THREE_DAYS_PUNISH)) {
    		
    		violations.add(Constraint.THREE_DAYS_PUNISH);
    	}
    	
    	if (constraintBefore.getNoOfStudentsPunish() != constraintAfter.getNoOfStudentsPunish()
   			|| !equalViolationMaps(violationMapBefore, violationMapAfter, Constraint.NO_OF_STUDENTS_PUNISH)) {
    			
    		violations.add(Constraint.NO_OF_STUDENTS_PUNISH);
    	}
    	
    	return violations;
    }
    
	public static String getViolation(int constraintKey)
	{
		String violation = "";
			
	    switch(constraintKey)
		{
			case Constraint.CLASH_PUNISH: violation = "clashing_exams"; break;
			case Constraint.EVENING_PUNISH: violation = "evening_morning"; break;
			case Constraint.SAME_DAY_PUNISH: violation = "same_day_exams"; break;
			case Constraint.TWO_DAYS_PUNISH: violation = "two_day_exams"; break;
			case Constraint.DAY_EVENING_PUNISH: violation = "twenty_hours_apart"; break;
			case Constraint.THREE_DAYS_PUNISH: violation = "three_day_exams"; break;
			case Constraint.NO_OF_STUDENTS_PUNISH: violation = "large_noofstudents"; break;
		}			
		return violation;
    }
	
	public static Color getViolationColor(int constraintKey)
	{
		Color color = null;
		
	    switch(constraintKey)
		{
			case Constraint.CLASH_PUNISH: color = new Color(233, 109, 99); break; // salmon
			case Constraint.EVENING_PUNISH: color = new Color(5, 50, 109); break; // blue
			case Constraint.SAME_DAY_PUNISH: color = new Color(127, 202, 159); break; // green
			case Constraint.TWO_DAYS_PUNISH: color = new Color(244, 186, 112); break; // yellow
			case Constraint.THREE_DAYS_PUNISH: color = new Color(133, 193, 245); break; // light blue
		}			
		return color;
    }
}