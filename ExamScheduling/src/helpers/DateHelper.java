package helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import thesis.timetable_generation.Timeslot;

public class DateHelper {
	
	private static DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");
	private static DateTimeFormatter timeformatter = DateTimeFormat.forPattern("HH:mm");
	
	public static DateTimeFormatter getFormatter() {
		return formatter;
	}

	public static DateTimeFormatter geTimeFormatter() {
		return timeformatter;
	}
	
	public static DateTime formatDateTime(String date) 
	{
		return formatter.parseDateTime(date);
	}
	
	public static Interval getInterval(Timeslot timeslot)
	{		 
		String examStartStr = timeslot.getStartDate();
		String examEndStr = timeslot.getEndDate();
		
		DateTime examStart = formatDateTime(examStartStr);
		DateTime examEnd = formatDateTime(examEndStr);
		
		Interval interval = new Interval(examStart, examEnd);
		return interval;
	}
	
	public static int getExamDurationInMinutes(String startDate, String endDate)
	{
		DateTime start = formatDateTime(startDate);
		DateTime end = formatDateTime(endDate);
		
		Interval interval = new Interval(start, end);
		int minutes = Minutes.minutesIn(interval).getMinutes();
		
		return minutes;
	}
	
	public static int getDaysBetween(Interval interval, Interval interval2)
	{
		DateTime exam1Start = interval.getStart();
		DateTime exam2Start = interval2.getStart();
		
		boolean before = exam1Start.isBefore(exam2Start);
		Days days = null;
		
		if (before) days = Days.daysBetween(exam1Start.withTimeAtStartOfDay(), exam2Start.withTimeAtStartOfDay());
		else days = Days.daysBetween(exam2Start.withTimeAtStartOfDay(), exam1Start.withTimeAtStartOfDay());
		
		return days.getDays();
	}
	
	public static Date getDateFromInput(String inputDate)
	{
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");  
        Date testDate = null;  
	    
		try
        {
			df.setLenient(false);
			testDate = df.parse(inputDate);
        }
		catch (ParseException e)
        { 
        	System.out.println("Invalid Format " + e.getMessage());
        }  
          
        if (!df.format(testDate).equals(inputDate)) 
            return null;
        else  
            return testDate;
	}
	
	public static Integer getTimeslotNoByTimeslot(HashMap<Integer, Timeslot> timeslotMap, Timeslot timeslot)
	{
		Interval timeslotInterval = getInterval(timeslot);
		
		// check if timeslot fits in one of the mapped timeslots
		for (Entry<Integer, Timeslot> entry : timeslotMap.entrySet())
		{
			Timeslot value = entry.getValue();
			Interval interval = getInterval(value);
			
			if (interval.contains(timeslotInterval) || timeslotInterval.contains(interval))
				return entry.getKey();
		}
		return null;
	}
}
