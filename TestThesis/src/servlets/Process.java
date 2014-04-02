package servlets;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import thesis.timetable_generation.GeneticAlgorithm;
import thesis.timetable_generation.ReadData;

public class Process extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static ArrayList<String> parameters;
	private static List<List<String>> experiments;   
	private static ReadData datafile = null;
	
    public Process() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String semester = request.getParameter("semester");
		String noOfWkDays = request.getParameter("noOfWkDays");
		String noOfSaturdays = request.getParameter("noOfSats");
		
		String startdate = request.getParameter("startdate");
		String desktopPath = "C://Users//Bernice//Desktop//";
		
		datafile = new ReadData(desktopPath + "GA Parameters.csv", Integer.parseInt(semester));
		experiments = datafile.getExperimentData();

		GeneticAlgorithm ga = null;
		
		try
		{
			 ga = new GeneticAlgorithm(datafile,
									   startdate,
									   Integer.parseInt(noOfWkDays.trim()), 
									   Integer.parseInt(noOfSaturdays.trim()));				
		}
		catch (ParseException e)
		{
			System.out.println(e.getMessage());
		}
		
		// for each experiment
		for (int i=0; i < experiments.size(); i++)
		{
			parameters = (ArrayList<String>) experiments.get(i);
			try
			{
				ga.setCrossoverType(Integer.parseInt(parameters.get(0)));
				ga.setCrossoverRate(Double.parseDouble(parameters.get(1)));
				ga.setMutationRate(Double.parseDouble(parameters.get(2)));
				ga.setInverseSqPress(Integer.parseInt(parameters.get(3)));
				ga.setNoOfGenerations(Integer.parseInt(parameters.get(4)));
				ga.setNoOfChromosomes(Integer.parseInt(parameters.get(5)));
				ga.setElitistSelection(Integer.parseInt(parameters.get(6)));
				ga.setRandomIntroduction(Integer.parseInt(parameters.get(7)));
				ga.setInterpolatingRates(Integer.parseInt(parameters.get(8)));
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
			}
			
			ga.generateTimeslotMaps();
			ga.initGA(); // initialize student and exams arrays
			int[] timetable = ga.runGA(); // run GA
			ga.insertTimetableEvents(timetable); // decode timetable and insert events in database
		}
		
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
