package thesis.timetable_generation;

import java.util.Date;

public class InputParameters {

	private int semester;
	private Date startDate;
	private Date endDate;
	private boolean includeSaturdays;
	private boolean sameWeekdays;
	private int weekdayTimeslots;
	private int saturdayTimeslots;
	private int mondayTimeslots;
	private int tuesdayTimeslots;
	private int wednesdayTimeslots;
	private int thursdayTimeslots;
	private int fridayTimeslots;
	
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
	
	public boolean isSameWeekdays() {
		return sameWeekdays;
	}
	
	public void setSameWeekdays(boolean sameWeekdays) {
		this.sameWeekdays = sameWeekdays;
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
	
	public int getMondayTimeslots() {
		return mondayTimeslots;
	}
	
	public void setMondayTimeslots(int mondayTimeslots) {
		this.mondayTimeslots = mondayTimeslots;
	}
	
	public int getTuesdayTimeslots() {
		return tuesdayTimeslots;
	}
	
	public void setTuesdayTimeslots(int tuesdayTimeslots) {
		this.tuesdayTimeslots = tuesdayTimeslots;
	}
	
	public int getWednesdayTimeslots() {
		return wednesdayTimeslots;
	}
	
	public void setWednesdayTimeslots(int wednesdayTimeslots) {
		this.wednesdayTimeslots = wednesdayTimeslots;
	}
	
	public int getThursdayTimeslots() {
		return thursdayTimeslots;
	}
	
	public void setThursdayTimeslots(int thursdayTimeslots) {
		this.thursdayTimeslots = thursdayTimeslots;
	}
	
	public int getFridayTimeslots() {
		return fridayTimeslots;
	}
	
	public void setFridayTimeslots(int fridayTimeslots) {
		this.fridayTimeslots = fridayTimeslots;
	}
}
