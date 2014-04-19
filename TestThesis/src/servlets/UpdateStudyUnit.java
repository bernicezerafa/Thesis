package servlets;

import helpers.SQLHelper;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import thesis.timetable_generation.Chromosome;
import thesis.timetable_generation.GAParameters;
import thesis.timetable_generation.GeneticAlgorithm;
import thesis.timetable_generation.ReadData;
import thesis.timetable_generation.Timeslot;

import com.google.common.collect.Table;

import entities.StudentExams;
import entities.StudyUnit;
import entities.TimetableEvent;

public class UpdateStudyUnit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateStudyUnit() {
        super();
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<Integer, Double> sortByComparator(Map<Integer,Double> unsortMap) {

        List list = new LinkedList(unsortMap.entrySet());

        //sort list based on comparator
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        //put sorted list into map again
        Map sortedMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }   
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String getDetails = request.getParameter("get_details");
		String studyUnitSuggest = request.getParameter("studyunit_auto");
		
		String examCode = null;
		StudyUnit exam = null;
		String query = null;
		
		if (getDetails == null && studyUnitSuggest == null)
		{
			examCode = request.getParameter("exam-code");
			String examTitle = request.getParameter("exam-title");
			String department = request.getParameter("department");
			
			String examLengthStr = request.getParameter("exam-length");		
			float examLength = 0.0f;
			
			if (examLengthStr != "") {
				examLength = Float.parseFloat(examLengthStr);
			}
	
			String noOfStudentsStr = request.getParameter("no-of-students");
			short noOfStudents = 0;
		
			if (noOfStudentsStr != "") {
				noOfStudents = Short.parseShort(noOfStudentsStr);
			}
			
			String year = request.getParameter("exam-year");
			
			String semesterStr = request.getParameter("exam-semester");
			short semester = 0;
			
			if (semesterStr != "") {
				semester = Short.parseShort(semesterStr);
			}
			
			String creditsStr = request.getParameter("exam-credits");
			short credits = 0;
			
			if (creditsStr != "") {
				credits = Short.parseShort(creditsStr);
			}
			
			boolean evening = Boolean.parseBoolean(request.getParameter("evening"));
			String venue = request.getParameter("room");
			
			exam = new StudyUnit(examCode, examTitle, year, semester, examLength, noOfStudents, department, credits, evening, venue);
		}
		else if (getDetails != null)
		{
			// get details for lightbox
			examCode = request.getParameter("unit_code");
		}
		else if (studyUnitSuggest != null)
		{
			query = request.getParameter("query");
		}
		
		Connection conn = SQLHelper.getConnection();
		
		try {
			
			if (exam != null)
			{
				String startDate = request.getParameter("exam_period_start");
				
				if (StudyUnit.getStudyUnit(conn, examCode) == null)
				{
					exam.insertStudyUnit(conn);
				}
				else
				{
					exam.updateStudyUnit(conn);
				}
				
				response.sendRedirect("timetable.jsp?startdate=" + startDate);
			}
			else if (exam == null && query == null)
			{
				String tooltip = request.getParameter("tooltip");
				
				StudyUnit details = StudyUnit.getStudyUnit(conn, examCode);
				
				JSONObject jo = new JSONObject();
				jo.put("title", details.getTitle());
				jo.put("noOfStudents", details.getNoOfStudents());
				jo.put("year", details.getYear());
				jo.put("credits", details.getCredits());
				jo.put("room", details.getVenue());
				
				// if details are for lightbox, get remaining details
				if (tooltip == null)
				{
					jo.put("department", details.getDepartment());
					jo.put("examLength", details.getExamLength());
					jo.put("semester", details.getSemester());
					jo.put("evening", details.isEvening());

					JSONArray jsonArray = new JSONArray();
					ArrayList<String> studentsInExam = StudentExams.getStudentsInExam(conn, examCode);
	
					for (int i=0; i < studentsInExam.size(); i++)
					{
						JSONObject studentObject = new JSONObject();
						studentObject.put("id", studentsInExam.get(i));
						jsonArray.add(studentObject);
					}
							    
			        jo.put("students", jsonArray);
				}
				
				// else if for tooltip, get recommendations
				else
				{
					String eventID = request.getParameter("event_id");					
					int examID = TimetableEvent.getStudyUnitID(conn, eventID);
					int examIndex = ReadData.getIndexExamId().get(examID);
					
					HashMap<Integer, Double> fitnesses = new HashMap<Integer, Double>();
					GeneticAlgorithm ga = new GeneticAlgorithm();
					
					int[] timetableState = GeneticAlgorithm.getTimetable();
					
					TreeMap<Integer, Timeslot> timeslotMap = GeneticAlgorithm.getTimeslotMap();
					Table<Integer, Integer, ArrayList<String>> clashesMatrix = GeneticAlgorithm.getClashesMatrix();
					GAParameters gaParam = GeneticAlgorithm.getGAParameters();

					// evaluate chromosome when this exam changes timeslot number
					// add fitness for each chromosome in ArrayList
					for (int i=0; i < timeslotMap.size(); i++)
					{
						timetableState[examIndex] = i;
						Chromosome chromosome = ga.evaluateChromosome(timetableState, gaParam, clashesMatrix);
						// timeslot number -> chromosome fitness map
						fitnesses.put(i, chromosome.getFitness());
					}
					
					// sort fitnesses in descending order
					Map<Integer, Double> sortedFitnesses = sortByComparator(fitnesses);
					List<Entry<Integer,Double>> randAccess = new ArrayList<Entry<Integer,Double>>(sortedFitnesses.entrySet());

					JSONArray jsonArray = new JSONArray();
	
					for (int i=0; i < 3; i++)
					{
						Timeslot timeslot = timeslotMap.get(randAccess.get(i).getKey());
						
						JSONObject timeslotObject = new JSONObject();
						timeslotObject.put("start_date", timeslot.getStartDate());
						timeslotObject.put("end_date", timeslot.getEndDate());
						
						jsonArray.add(timeslotObject);
					}
							    
			        jo.put("recommendations", jsonArray);
				}
				
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(jo.toJSONString());
			}
			else if (query != null)
			{
				ArrayList<String> studyUnitCodes = StudyUnit.getStudyUnitSuggestions(conn, query);				
				JSONObject jo = new JSONObject();
				
				for (int i=0; i<studyUnitCodes.size(); i++)
				{
					jo.put(Integer.toString(i), studyUnitCodes.get(i));
				}
				
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(jo.toJSONString());
			}
			
		} finally {
			SQLHelper.closeConnection(conn);
		}
	} 

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
}