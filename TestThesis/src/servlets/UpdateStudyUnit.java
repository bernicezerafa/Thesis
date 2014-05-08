package servlets;

import helpers.DateHelper;
import helpers.FileHelper;
import helpers.SQLHelper;

import java.io.IOException;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import thesis.timetable_generation.BestChromosome;
import thesis.timetable_generation.GeneticAlgorithm;
import thesis.timetable_generation.Timeslot;
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
		String search = request.getParameter("term");
		
		Connection conn = SQLHelper.getConnection();
		
		if (search != null)
		{
			ArrayList<String> studyUnitCodes = StudyUnit.getStudyUnitSuggestions(conn, search, -1);				
			JSONObject jo = new JSONObject();
			
			for (int i=0; i < studyUnitCodes.size(); i++)
			{
				jo.put(Integer.toString(i), studyUnitCodes.get(i));
			}
			
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(jo.toJSONString());
			return;
		}
		
		if (studyUnitSuggest != null)
		{
			String query = request.getParameter("query");
			int semester = Integer.parseInt(request.getParameter("semester"));
			
			ArrayList<String> studyUnitCodes = StudyUnit.getStudyUnitSuggestions(conn, query, semester);				
			JSONObject jo = new JSONObject();
			
			for (int i=0; i<studyUnitCodes.size(); i++)
			{
				jo.put(Integer.toString(i), studyUnitCodes.get(i));
			}
			
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(jo.toJSONString());
			return;
		}
		
		if (getDetails == null && studyUnitSuggest == null)
		{
			String examCode = request.getParameter("exam-code");
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
			
			StudyUnit exam = new StudyUnit(examCode, examTitle, year, semester, examLength, noOfStudents, department, credits, evening, venue);
						
			if (StudyUnit.getStudyUnitID(conn, exam.getUnitCode(), exam.isEvening()) == -1)
			{
				exam.insertStudyUnit(conn);
				
				String date = request.getParameter("start_date");
				String startTime = request.getParameter("start_time");
				String endTime = request.getParameter("end_time");
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
				SimpleDateFormat sql_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				String startString = date + " " + startTime;
				String endString = date + " " + endTime;
				
				Timeslot timeslotExam = new Timeslot(startString, endString);
				
				Date startDate = null;
				Date endDate = null;
					
				try 
				{
					startDate = sdf.parse(startString);
					endDate = sdf.parse(endString);
						
				} catch (ParseException ignore) {
					System.out.println("Parse Exception save event: " + ignore.getMessage());
				}
					
				String eventStartDate = sql_sdf.format(startDate);
				String eventEndDate = sql_sdf.format(endDate);
				
				int examId = exam.getExamID();
				int eventId = Integer.parseInt(request.getParameter("event_id"));
				
				TimetableEvent event = new TimetableEvent(eventId, eventStartDate, eventEndDate);
				event.setExamId(examId);
				event.updateEventDates(conn);
				
				TimetableEvent.addExam(conn, examId, timeslotExam);
			}
			else
			{
				exam.updateStudyUnit(conn);
			}
			
			return;
		}
		else if (getDetails != null)
		{
			int eventId = Integer.parseInt(request.getParameter("event_id"));
			
			String tooltip = request.getParameter("tooltip");			
			StudyUnit details = StudyUnit.getStudyUnit(conn, eventId);
			
			JSONObject jo = new JSONObject();
			jo.put("unitCode", details.getUnitCode());
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
				
				String examCode = request.getParameter("exam_code");	
				int studyUnitId = StudyUnit.getStudyUnitID(conn, examCode, details.isEvening());
				ArrayList<String> studentsInExam = StudentExams.getStudentsInExam(conn, studyUnitId);

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
				String eventStart = request.getParameter("event_start");
				String eventEnd = request.getParameter("event_end");
				
				int minutes = DateHelper.getExamDurationInMinutes(eventStart, eventEnd);
				int examID = TimetableEvent.getStudyUnitID(conn, eventId);
				
				HashMap<Integer, Double> fitnesses = new HashMap<Integer, Double>();
				GeneticAlgorithm ga = new GeneticAlgorithm();
				
				BestChromosome chrom = FileHelper.getBestChromosome();
				Integer[] timetableState = chrom.getChromosome();
				
				HashMap<Integer, Integer> indexExamID = ga.getIndexExamID();
				HashMap<Integer, Timeslot> timeslotMap = ga.getTimeslotMap();

				int examIndex = indexExamID.get(examID);
				int noOfTimeslots = FileHelper.getInputParameters().getNoOfTimeslots();
				
				// evaluate chromosome when this exam changes timeslot number
				// add fitness for each chromosome in ArrayList
				for (int i=0; i < noOfTimeslots; i++)
				{
					timetableState[examIndex] = i;
					BestChromosome chromosome = new BestChromosome(timetableState);
					chromosome = ga.evaluateChromosome(chromosome);
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
					
					String timeslotStart = timeslot.getStartDate();
					timeslotObject.put("start_date", timeslotStart);
					
					DateTime startDateTime = DateHelper.formatDateTime(timeslotStart);

					DateTime endTime = new DateTime(startDateTime);
					endTime = endTime.plusMinutes(minutes);
					
					timeslotObject.put("end_date", endTime.toString(DateHelper.geTimeFormatter()));
					
					jsonArray.add(timeslotObject);
				}
						    
		        jo.put("recommendations", jsonArray);
			}
			
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(jo.toJSONString());
		}
	} 

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
}