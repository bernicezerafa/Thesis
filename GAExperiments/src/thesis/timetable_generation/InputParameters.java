package thesis.timetable_generation;

import java.io.Serializable;
import java.util.Date;

public class InputParameters implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Integer semester;
	private Date startDate;
	private Date endDate;
	private Boolean includeSaturdays;
	private Integer weekdayTimeslots;
	private Integer saturdayTimeslots;
	
	private Integer noOfTimeslots;
	private Integer noOfDayExams;
	private Integer noOfEveningExams;
	private Integer noOfEveningTimeslots;
	private Integer noOfDayTimeslots;
	
	public Integer getSemester() {
		return semester;
	}
	
	public void setSemester(Integer semester) {
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
	
	public Boolean includeSaturdays() {
		return includeSaturdays;
	}
	
	public void setIncludeSaturdays(Boolean includeSaturdays) {
		this.includeSaturdays = includeSaturdays;
	}
	
	public Integer getWeekdayTimeslots() {
		return weekdayTimeslots;
	}
	
	public void setWeekdayTimeslots(Integer weekdayTimeslots) {
		this.weekdayTimeslots = weekdayTimeslots;
	}
	
	public Integer getSaturdayTimeslots() {
		return saturdayTimeslots;
	}
	
	public void setSaturdayTimeslots(Integer saturdayTimeslots) {
		this.saturdayTimeslots = saturdayTimeslots;
	}
	
	public Integer getNoOfTimeslots() {
		return noOfTimeslots;
	}

	public void setNoOfTimeslots(Integer noOfTimeslots) {
		this.noOfTimeslots = noOfTimeslots;
	}
	
	public Integer getNoOfEveningTimeslots() {
		return noOfEveningTimeslots;
	}

	public void setNoOfEveningTimeslots(Integer noOfEveningTimeslots) {
		this.noOfEveningTimeslots = noOfEveningTimeslots;
	}

	public Integer getNoOfDayTimeslots() {
		return noOfDayTimeslots;
	}

	public void setNoOfDayTimeslots(Integer noOfDayTimeslots) {
		this.noOfDayTimeslots = noOfDayTimeslots;
	}

	public Integer getNoOfDayExams() {
		return noOfDayExams;
	}

	public void setNoOfDayExams(Integer noOfDayExams) {
		this.noOfDayExams = noOfDayExams;
	}

	public Integer getNoOfEveningExams() {
		return noOfEveningExams;
	}

	public void setNoOfEveningExams(Integer noOfEveningExams) {
		this.noOfEveningExams = noOfEveningExams;
	}

	public Boolean isIncludeSaturdays() {
		return includeSaturdays;
	}
}