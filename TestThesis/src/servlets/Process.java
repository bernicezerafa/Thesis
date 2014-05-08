package servlets;

import helpers.DateHelper;
import helpers.FileHelper;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import thesis.timetable_generation.GeneticAlgorithm;
import thesis.timetable_generation.InputParameters;

public class Process extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public Process() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		InputParameters param = new InputParameters();
		
		String startdate = request.getParameter("startdate");
		Date startDate = DateHelper.getDateFromInput(startdate);
		param.setStartDate(startDate);
		
		Date endDate = DateHelper.getDateFromInput(request.getParameter("enddate"));
		param.setEndDate(endDate);
		
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		int month = c.get(Calendar.MONTH);
		
		int semester = 0;
		if (month == Calendar.JANUARY || month == Calendar.FEBRUARY)
			semester = 1;
		else if (month == Calendar.MAY || month == Calendar.JUNE)
			semester = 2;
		
		param.setSemester(semester); 
		
		String[] includeSaturdays = request.getParameterValues("include_sats");
		
		if (includeSaturdays != null)
			param.setIncludeSaturdays(true);
		else
			param.setIncludeSaturdays(false);
		
		String[] sameWeekdays = request.getParameterValues("same_weekdays");
		
		if (sameWeekdays != null)
			param.setSameWeekdays(true);
		else
			param.setSameWeekdays(false);
		
		int weekdayTimeslots = Integer.parseInt(request.getParameter("weekday_ts"));
		param.setWeekdayTimeslots(weekdayTimeslots);
		
		int saturdayTimeslots = Integer.parseInt(request.getParameter("saturday_ts"));
		param.setSaturdayTimeslots(saturdayTimeslots);
		
		if (!param.isSameWeekdays())
		{
			String monParam = request.getParameter("monday_ts");
			if (monParam != null)
			{
				int mondayTimeslots = Integer.parseInt(monParam);
				param.setMondayTimeslots(mondayTimeslots); 
			}
			
			String tueParam = request.getParameter("tuesday_ts");
			if (tueParam != null)
			{
				int tuesdayTimeslots = Integer.parseInt(tueParam);
				param.setTuesdayTimeslots(tuesdayTimeslots); 
			}
			
			String wedParam = request.getParameter("wednesday_ts");
			if (wedParam != null)
			{
				int wednesdayTimeslots = Integer.parseInt(wedParam);
				param.setWednesdayTimeslots(wednesdayTimeslots); 
			}
			
			String thursParam = request.getParameter("thursday_ts");
			if (thursParam != null)
			{
				int thursdayTimeslots = Integer.parseInt(thursParam);
				param.setThursdayTimeslots(thursdayTimeslots);
			}
			
			String friParam = request.getParameter("friday_ts");
			if (friParam != null)
			{
				int fridayTimeslots = Integer.parseInt(friParam);
				param.setFridayTimeslots(fridayTimeslots); 
			}
		}
		
		FileHelper.saveInputParameters(param);
		
		GeneticAlgorithm ga = new GeneticAlgorithm(startDate, endDate, semester);
		ga.setTimeslotMap(request, c, param);
		ga.initGA(); // initialize student and exams arrays
		Integer[] bestTimetable = ga.startGeneticAlgorithm().getChromosome();  // run GA
		ga.insertTimetableEvents(bestTimetable); // save timetable
		
		// redirects to calendar view
		response.sendRedirect("timetable.jsp?startdate=" + startdate);
	} 

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
}