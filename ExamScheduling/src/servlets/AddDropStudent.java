package servlets;

import helpers.FileHelper;
import helpers.SQLHelper;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import thesis.timetable_generation.Chromosome;
import thesis.timetable_generation.ExamIndexMaps;
import thesis.timetable_generation.GeneticAlgorithm;
import entities.StudentExams;
import entities.Exam;
import entities.TimetableEvent;

/**
 * Servlet implementation class AddDropStudent
 */
public class AddDropStudent extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
    
	private Connection conn = null;
	private ExamIndexMaps examIndexMaps = null;
	
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
			
			int studyUnitID = Exam.getStudyUnitID(conn, unitCode, evening);
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
    
    public int getEventID(int examID)
    {
    	int examIndex = examIndexMaps.getExamIDIndex().get(examID);
    	
    	if (examIndex != -1)
    		return TimetableEvent.getEventID(conn, examID);
    	else return -1;
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
				
				int studyUnitId = Exam.getStudyUnitID(conn, unitCode, evening);
				
				JSONArray studentsAddDrop = addDropStudentsFromUnit(conn, studentIds, studyUnitId);
		    	jsonObject.put("studentAddDrop", studentsAddDrop);
			}
				// get violations already present in the timetable
			Chromosome chromosomeBefore = FileHelper.getBestChromosome();
			
			// reconstruct clashes matrix with the new changes
			GeneticAlgorithm ga = new GeneticAlgorithm();
			ga.constructClashesMatrix(conn, false);
			
			// evaluate chromosome in affect with new changes in this matrix and return new violation map
			Chromosome chromosomeAfter = null;
			try
			{
				chromosomeAfter = ga.evaluateChromosome(chromosomeBefore);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			FileHelper.saveBestChromosome(chromosomeAfter);
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