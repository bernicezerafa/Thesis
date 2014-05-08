package thesis.timetable_generation;

import java.io.Serializable;

import org.joda.time.Hours;

public class TemporalDifference implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean overlaps;
	private int daysBetween;
	private Hours hoursBetween;
	
	public TemporalDifference(boolean overlaps, int daysBetween, Hours hoursBetween) {
		this.overlaps = overlaps;
		this.daysBetween = daysBetween;
		this.hoursBetween = hoursBetween;
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
	
	public Hours getHoursBetween() {
		return hoursBetween;
	}
	
	public void setHoursBetween(Hours hoursBetween) {
		this.hoursBetween = hoursBetween;
	}
}
