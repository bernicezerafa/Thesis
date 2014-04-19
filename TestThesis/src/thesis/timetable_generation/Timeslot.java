package thesis.timetable_generation;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Timeslot implements Serializable {
	
	private String startDate;
	private String endDate;
	
	public Timeslot(String startDate, String endDate)
	{
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public String getStartDate() {
		return startDate;
	}
	
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	public String getEndDate() {
		return endDate;
	}
	
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	@Override
	public String toString() {
		
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(this.startDate);
		strBuffer.append(",");
		strBuffer.append(this.endDate);
		
		return strBuffer.toString();
	}
}
