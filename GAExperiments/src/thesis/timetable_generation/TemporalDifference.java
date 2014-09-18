package thesis.timetable_generation;

import java.io.Serializable;

public class TemporalDifference implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean overlaps;
	private int daysBetween;
	private boolean eveningMorning;
	
	public TemporalDifference(boolean overlaps, int daysBetween, boolean eveningMorning) {
		this.overlaps = overlaps;
		this.daysBetween = daysBetween;
		this.eveningMorning = eveningMorning;
	}
	
	public boolean isOverlaps() {
		return overlaps;
	}
	
	public void setOverlaps(boolean overlaps) {
		this.overlaps = overlaps;
	}
	
	public int getDaysBetween() {
		return daysBetween;
	}
	
	public void setDaysBetween(int daysBetween) {
		this.daysBetween = daysBetween;
	}
	
	public boolean isEveningMorning() {
		return eveningMorning;
	}
	
	public void setEveningMorning(boolean eveningMorning) {
		this.eveningMorning = eveningMorning;
	}
}
