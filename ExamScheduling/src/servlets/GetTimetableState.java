package servlets;

import helpers.FileHelper;
import helpers.SQLHelper;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import thesis.timetable_generation.Chromosome;
import thesis.timetable_generation.Constraint;
import thesis.timetable_generation.ExamMap;
import thesis.timetable_generation.ReadData;
import thesis.timetable_generation.Timeslot;
import entities.StudyUnit;
import entities.TimetableEvent;

public class GetTimetableState extends HttpServlet 
{	
	private static final long serialVersionUID = 1L;
    private Connection conn = null;
    private HashMap<Integer, Integer> indexExamID = null;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetTimetableState()
    {
        super();
    }

    public int getEventID(int examPos)
    {
		int eventID = -1;
		if (examPos != -1)
		{
			int exam2ID = ReadData.getKeyByValue(indexExamID, examPos);
		    eventID = TimetableEvent.getEventID(conn, exam2ID);
		}
		
		return eventID;
    }
   
    @SuppressWarnings("unchecked")
	public JSONArray getViolations(Chromosome chromosome)
    {
    	JSONArray allViolations = new JSONArray();
		HashMap<ExamMap, ArrayList<String>>[] violationMap = chromosome.getViolationMap();
		
		for (int index = 0; index < violationMap.length; index++)
		{
			Integer violationKey = index;
			HashMap<ExamMap, ArrayList<String>> violationsOfTypeKey = violationMap[index];
			
			if (violationsOfTypeKey != null)
			{
				JSONObject jsonObject = new JSONObject();
				JSONArray violationsOfType = new JSONArray();
							
				for (Entry<ExamMap, ArrayList<String>> examStudentsViolation: violationsOfTypeKey.entrySet())
				{
					JSONObject violation = new JSONObject();
					
					ExamMap examMap = examStudentsViolation.getKey();
					ArrayList<String> commonStudents = examStudentsViolation.getValue();
					
					int[] examsArr = examMap.getData();
					
					JSONArray exams = new JSONArray();
					for (int i=0; i < examsArr.length; i++)
					{
						JSONObject exam = new JSONObject();
						int eventId = getEventID(examsArr[i]);
						StudyUnit unit = StudyUnit.getStudyUnit(conn, eventId);		
						Timeslot timeslot = TimetableEvent.getEventTimeslot(conn, eventId);
						
						exam.put("unitCode", unit.getUnitCode());
						exam.put("evening", unit.isEvening());
						exam.put("eventStart", timeslot.getStartDate());
						exam.put("eventEnd", timeslot.getEndTime());
						exams.add(exam);
					}
					violation.put("exams", exams);
					
					JSONArray studentsAffected = new JSONArray();
					for (String studentId: commonStudents)
					{
						JSONObject student = new JSONObject();
						student.put("studentId", studentId);
						studentsAffected.add(student);
					}
					
					violation.put("students", studentsAffected);
					violation.put("thisNoOfStudents", commonStudents.size());
					violationsOfType.add(violation);
				}
				jsonObject.put("type", Constraint.getViolation(violationKey));
				jsonObject.put("noOfStudents", Constraint.getNoOfStudentsAffected(chromosome.getConstraintViolations(), violationKey));
				jsonObject.put("violations", violationsOfType);
				
				allViolations.add(jsonObject);
			}
		}
		return allViolations;
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				
		Chromosome bestChromosome = FileHelper.getBestChromosome();
		conn = SQLHelper.getConnection();
		indexExamID = FileHelper.getIndexExamId();
		
		JSONArray allViolations = getViolations(bestChromosome);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(allViolations.toJSONString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}
}
