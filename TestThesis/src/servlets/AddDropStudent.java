package servlets;

import helpers.FileHelper;
import helpers.SQLHelper;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import thesis.timetable_generation.BestChromosome;
import thesis.timetable_generation.Constraint;
import thesis.timetable_generation.GeneticAlgorithm;
import thesis.timetable_generation.IntTuple;
import thesis.timetable_generation.ReadData;
import entities.StudentExams;
import entities.StudyUnit;
import entities.TimetableEvent;

/**
 * Servlet implementation class AddDropStudent
 */
public class AddDropStudent extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
    
	private Connection conn = null;
	private HashMap<Integer, Integer> indexExamID = null;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddDropStudent() {
    	super();
    }

    public List<Integer> getStudyUnitsIdsAddDrop(Connection conn, String[] unitCodes)
    {
    	List<Integer> studyUnitIDs = new ArrayList<Integer>();
    	
    	for (int i=0; i < unitCodes.length; i++)
		{
			String unitCode = unitCodes[i];
			boolean evening = false;
			
			if (unitCode.contains("evening"))
			{
				unitCode = unitCode.substring(0, unitCode.indexOf(" ")).trim();
				evening = true;
			}
			
			int studyUnitID = StudyUnit.getStudyUnitID(conn, unitCode, evening);
			studyUnitIDs.add(studyUnitID);
		}
    	
    	return studyUnitIDs;
    }
    
    public void insertOrDropStudent(Connection conn, String studentId, List<Integer> unitIds, boolean drop)
    {
    	for (Integer studyUnitID: unitIds)
		{
			StudentExams studentExam = new StudentExams(studyUnitID, studentId);
			
			if (drop) // if student dropped
			{
				studentExam.dropStudentFromUnit(conn);
			}
			else // if student added
			{
				studentExam.insertStudentExamRel(conn);
			}
		}
    }
    
    @SuppressWarnings({ "unchecked" })
	public JSONArray addDropStudentsFromUnit(Connection conn, String[] studentIds, int studyUnitId)
    {
    	JSONArray studentAddDrop = new JSONArray();
    	
    	for (String studentId: studentIds)
    	{
    		JSONObject thisStudent = new JSONObject();
    		
    		StudentExams studentExam = new StudentExams(studyUnitId, studentId);
    		thisStudent.put("studentId", studentId);
    		
    		// if student exam relationship exists, drop this relationship
    		if (studentExam.studentExamRelExists(conn))
    		{
    			thisStudent.put("drop", true);
    			studentExam.dropStudentFromUnit(conn);
    		}
    		// else insert student, studyunit relationship
    		else
    		{
    			thisStudent.put("drop", false);
    			studentExam.insertStudentExamRel(conn);
    		}
    		
    		studentAddDrop.add(thisStudent);
    	}
    	
    	return studentAddDrop;
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
    
    // compares the timetable violations before student was added or dropped from study units
    @SuppressWarnings("unchecked")
	public JSONArray compareViolations(BestChromosome chromosomeBefore, BestChromosome chromosomeAfter, 
									   ArrayList<Integer> violationsAffected)
    {
    	
    	HashMap<Integer, HashMap<IntTuple, ArrayList<String>>> violationMapAfter = chromosomeAfter.getViolationMap();
    	
		JSONArray jsonArray = new JSONArray();
    	indexExamID = FileHelper.getIndexExamId();
    	
		for (Integer violationKey: violationsAffected)
		{
			JSONObject violation = new JSONObject();
			violation.put("violationType", Constraint.getViolation(violationKey));
						
			// if violation map for chromosome after doesnt have this violation, it means that this violation was
			// dropped from chromosome before
			HashMap<IntTuple, ArrayList<String>> studentsExamViolations = violationMapAfter.get(violationKey);
			
			if(studentsExamViolations != null) {
	
				for (Entry<IntTuple, ArrayList<String>> studentExamViolation: studentsExamViolations.entrySet())
				{
					IntTuple examMap = studentExamViolation.getKey();
					ArrayList<String> commonStudents = studentExamViolation.getValue();
					
					int eventIDExam1 = getEventID(examMap.get(0));
					int eventIDExam2 = getEventID(examMap.get(1));
					int eventIDExam3 = getEventID(examMap.get(2));
									
					violation.put("eventId1", eventIDExam1);
					violation.put("eventId2", eventIDExam2);
					violation.put("eventId3", eventIDExam3);
									
					JSONArray studentsAffected = new JSONArray();
					for (String studentId: commonStudents)
					{
						JSONObject student = new JSONObject();
						student.put("studentId", studentId);
						studentsAffected.add(student);
					}
					
					violation.put("students", studentsAffected);
				}
				jsonArray.add(violation);
			}
		}
		
		return jsonArray;
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		conn = SQLHelper.getConnection();
		
		try
		{
			String studentId = request.getParameter("studentID");
			JSONObject jsonObject = new JSONObject();
			
			if (studentId != null)
			{
				// insert and drop units accordingly from StudentExams table
				String[] droppedUnits = request.getParameterValues("dropped[]");
				String[] addedUnits = request.getParameterValues("added[]");
				List<Integer> droppedIds = null;
				List<Integer> addedIds = null;
				
				if (droppedUnits != null)
				{
					droppedIds = getStudyUnitsIdsAddDrop(conn, droppedUnits);
					insertOrDropStudent(conn, studentId, droppedIds, true);
				}
				
				if (addedUnits != null)
				{
					addedIds = getStudyUnitsIdsAddDrop(conn, addedUnits);
					insertOrDropStudent(conn, studentId, addedIds, false);
				}
			}
			else
			{
				String[] studentIds = request.getParameterValues("studentIds[]");
				String unitCode = request.getParameter("unit");
				boolean evening = Boolean.parseBoolean(request.getParameter("evening"));
				
				int studyUnitId = StudyUnit.getStudyUnitID(conn, unitCode, evening);
				
				JSONArray studentsAddDrop = addDropStudentsFromUnit(conn, studentIds, studyUnitId);
		    	jsonObject.put("studentAddDrop", studentsAddDrop);
			}
				// get violations already present in the timetable
			BestChromosome chromosomeBefore = FileHelper.getBestChromosome();
			
			// reconstruct clashes matrix with the new changes
			GeneticAlgorithm ga = new GeneticAlgorithm();
			ga.constructClashesMatrix(conn, false);
			
			// evaluate chromosome in affect with new changes in this matrix and return new violation map
			BestChromosome chromosomeAfter = ga.evaluateChromosome(chromosomeBefore);
			FileHelper.saveBestChromosome(chromosomeAfter);
			
			ArrayList<Integer> violationsAffected = Constraint.getAffectedViolations(chromosomeBefore, chromosomeAfter);
						
			// send changes information to JSP page and update information from client-side
			if (violationsAffected.size() == 0)
			{
				jsonObject.put("timetableAffected", false);
			}
			else
			{
				jsonObject.put("timetableAffected", true);
				JSONArray jsonArray = compareViolations(chromosomeBefore, chromosomeAfter, violationsAffected);
				jsonObject.put("changesArray", jsonArray);
			}
			
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(jsonObject.toJSONString());
		} 
		finally
		{
			SQLHelper.closeConnection(conn);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
	}
}
