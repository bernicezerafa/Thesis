package thesis.timetable_generation;

import java.io.Serializable;
import java.util.Date;

public class InputParameters implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int semester;
	private Date startDate;
	private Date endDate;
	private boolean includeSaturdays;
	private int weekdayTimeslots;
	private int saturdayTimeslots;
	
	private int noOfTimeslots;
	private int noOfDayExams;
	private int noOfEveningExams;
	private int noOfEveningTimeslots;
	private int noOfDayTimeslots;
	
	public int getSemester() {
		return semester;
	}
	
	public void setSemester(int semester) {
		this.semester = semester;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public boolean includeSaturdays() {
		return includeSaturdays;
	}
	
	public void setIncludeSaturdays(boolean includeSaturdays) {
		this.includeSaturdays = includeSaturdays;
	}
	
	public int getWeekdayTimeslots() {
		return weekdayTimeslots;
	}
	
	public void setWeekdayTimeslots(int weekdayTimeslots) {
		this.weekdayTimeslots = weekdayTimeslots;
	}
	
	public int getSaturdayTimeslots() {
		return saturdayTimeslots;
	}
	
	public void setSaturdayTimeslots(int saturdayTimeslots) {
		this.saturdayTimeslots = saturdayTimeslots;
	}
	
	public int getNoOfTimeslots() {
		return noOfTimeslots;
	}

	public void setNoOfTimeslots(int noOfTimeslots) {
		this.noOfTimeslots = noOfTimeslots;
	}
	
	public int getNoOfEveningTimeslots() {
		return noOfEveningTimeslots;
	}

	public void setNoOfEveningTimeslots(int noOfEveningTimeslots) {
		this.noOfEveningTimeslots = noOfEveningTimeslots;
	}

	public int getNoOfDayTimeslots() {
		return noOfDayTimeslots;
	}

	public void setNoOfDayTimeslots(int noOfDayTimeslots) {
		this.noOfDayTimeslots = noOfDayTimeslots;
	}

	public int getNoOfDayExams() {
		return noOfDayExams;
	}

	public void setNoOfDayExams(int noOfDayExams) {
		this.noOfDayExams = noOfDayExams;
	}

	public int getNoOfEveningExams() {
		return noOfEveningExams;
	}

	public void setNoOfEveningExams(int noOfEveningExams) {
		this.noOfEveningExams = noOfEveningExams;
	}

	public boolean isIncludeSaturdays() {
		return includeSaturdays;
	}
}