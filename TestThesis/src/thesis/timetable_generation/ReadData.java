package thesis.timetable_generation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import entities.Student;
import entities.StudentExams;
import entities.StudyUnit;
 
public class ReadData 
{
	private int semester;
	private String experimentsFilePath;
	
	private HashMap<Integer, Integer> indexStudentID = new HashMap<Integer, Integer>();
	private static HashMap<Integer, Integer> indexExamID = new HashMap<Integer, Integer>();
	
	private HashMap<Integer, ArrayList<Integer>> studentExams = new HashMap<Integer, ArrayList<Integer>>();
	private ArrayList<StudyUnit> studyUnits = new ArrayList<StudyUnit>();
	
	//private HashMap<String, ArrayList<StudyUnit>> studyUnits = new HashMap<String, ArrayList<StudyUnit>>();
	
	public ReadData(int semester)
	{
		this.semester = semester;
	}
		
	public ReadData(String experimentsFilePath, int semester)
	{
		this.experimentsFilePath = experimentsFilePath;
		this.semester = semester;
	}
	
	public HashMap<Integer, Integer> getIndexStudentID() {
		return indexStudentID;
	}

	public ArrayList<StudyUnit> getStudyUnits() {
		return studyUnits;
	}
	
	public static HashMap<Integer, Integer> getIndexExamID() {
		return indexExamID;
	}
	
	// get experiment data
	public List<List<String>> getExperimentData()
	{
		BufferedReader bufRdr = null;
		List<List<String>> experiments = null;  

		try
		{
			File file = new File(experimentsFilePath);
	    	
			if (file.exists())
			{
				bufRdr = new BufferedReader(new FileReader(file));
				experiments = new ArrayList<List<String>>();
		    	String line = null;
		    	
		    	while ((line = bufRdr.readLine()) != null)
		    	{
		    		StringTokenizer st = new StringTokenizer(line, ",");
		    		ArrayList<String> experiment = new ArrayList<String>(); 
		    		
		    		while (st.hasMoreTokens())
		    		{
		    			String next_arg = st.nextToken();
		    			
		    			if (!next_arg.trim().equals(""))
		    				experiment.add(next_arg.trim());
		    		}
		    		experiments.add(experiment);
		    	}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				bufRdr.close();
			}
			catch (IOException e2)
			{
				System.out.println(e2.getMessage());
			}
		}
		return experiments;
	}
	
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	public void mapExamIndexes(Connection conn)
	{
		//SELECT DISTINCT st.*
		//FROM dbo.StudyUnits st JOIN dbo.STUDENT_EXAMS se
		//ON st.UNITCODE = se.UNITCODE
		//WHERE st.semester = '1' AND st.evening = 'false';
		StringBuffer query = null; 
		Statement stmt = null;
		
		try
		{
			query = new StringBuffer();
	        
			query.append("SELECT DISTINCT st.*");
			query.append("\nFROM ");
			query.append(StudyUnit.TBL_STUDYUNITS);
			query.append(" st JOIN ");
			query.append(StudentExams.TBL_STUDENTEXAMS);
			query.append(" se \nON st.");
			query.append(StudyUnit.FLD_UNITCODE);
			query.append(" = se.");
			query.append(StudentExams.FLD_UNITCODE);
			query.append("\nWHERE st.");
			query.append(StudyUnit.FLD_SEMESTER);
			query.append(" = '");
			query.append(semester);
			query.append("' AND st.");
			query.append(StudyUnit.FLD_EVENING);
			query.append(" = '");
			query.append(false);
			query.append("'");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			int count = 0;
			while (rs.next())
			{
				int examID = rs.getInt(StudyUnit.FLD_ID);
				indexExamID.put(examID, count);
				
				String unitcode = rs.getString(StudyUnit.FLD_UNITCODE);
				String title = rs.getString(StudyUnit.FLD_TITLE);
				String year = rs.getString(StudyUnit.FLD_YEAR);
				short semester = rs.getShort(StudyUnit.FLD_SEMESTER);
				float examLength = rs.getFloat(StudyUnit.FLD_EXAMLENGTH);
				short noOfStudents = rs.getShort(StudyUnit.FLD_NOOFSTUDENTS);
				String department = rs.getString(StudyUnit.FLD_DEPARTMENT);
				short credits = rs.getShort(StudyUnit.FLD_CREDITS);
				boolean evening = rs.getBoolean(StudyUnit.FLD_EVENING);
				String venue = rs.getString(StudyUnit.FLD_VENUE);
			
				StudyUnit studyUnit = new StudyUnit(unitcode, title, year, semester, examLength, noOfStudents, department, credits, evening, venue);
				studyUnits.add(studyUnit);
				
				count++;
			}
		}
		catch (SQLException e)
		{
      		System.out.println("[ReadData.mapExamIndexes()]: " + e.getMessage());
		}
	}
	
	public int getNoOfExams(Connection conn)
	{
		//SELECT COUNT (DISTINCT st.UNITCODE)
		//FROM dbo.StudyUnits st JOIN dbo.STUDENT_EXAMS se
		//ON st.UNITCODE = se.UNITCODE
		//WHERE st.semester = '1' AND st.evening = 'false';
		
		int noOfExams = 0;
		StringBuffer query = null; 
		Statement stmt = null;
		
		try
		{
			query = new StringBuffer();
	        
			query.append("SELECT COUNT (DISTINCT st.");
			query.append(StudyUnit.FLD_UNITCODE);
			query.append(") AS 'NoOfExams'\nFROM ");
			query.append(StudyUnit.TBL_STUDYUNITS);
			query.append(" st JOIN ");
			query.append(StudentExams.TBL_STUDENTEXAMS);
			query.append(" se \nON st.");
			query.append(StudyUnit.FLD_UNITCODE);
			query.append(" = se.");
			query.append(StudentExams.FLD_UNITCODE);
			query.append("\nWHERE st.");
			query.append(StudyUnit.FLD_SEMESTER);
			query.append(" = '");
			query.append(semester);
			query.append("' AND st.");
			query.append(StudyUnit.FLD_EVENING);
			query.append(" = '");
			query.append(false);
			query.append("'");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			while (rs.next())
			{
				noOfExams = rs.getInt("noOfExams");
			}
		}
		catch (SQLException e)
		{
      		System.out.println("[ReadData.getNoOfExams()]: " + e.getMessage());
		}
		return noOfExams;
	}
	
	public int[][] getAllStudentExams(Connection conn)
	{
		//SELECT se.*, st.ID AS 'ExamID', s.ID AS 'StudID'
		//FROM dbo.STUDENT_EXAMS se
		//JOIN dbo.STUDYUNITS st ON se.UNITCODE = st.UNITCODE
		//JOIN dbo.STUDENTS s ON se.StudentID = s.StudentID
		//WHERE st.semester = '1' AND st.evening = 'false'
		//ORDER BY se.StudentID ASC;
		
		mapExamIndexes(conn);
		
		int[][] studentExamsArr = null;
		StringBuffer query = null; 
		Statement stmt = null;
		
		try
		{
			query = new StringBuffer();
	        
			query.append("SELECT se.*, st.");
			query.append(StudyUnit.FLD_ID);
			query.append(" AS 'ExamID', s.");
			query.append(Student.FLD_ID);
			query.append(" AS 'StudID'");
			query.append("\nFROM ");
			query.append(StudentExams.TBL_STUDENTEXAMS);
			query.append(" se \nJOIN ");
			query.append(StudyUnit.TBL_STUDYUNITS);
			query.append(" st ON se.");
			query.append(StudyUnit.FLD_UNITCODE);
			query.append(" = st.");
			query.append(StudentExams.FLD_UNITCODE);
			query.append("\nJOIN ");
			query.append(Student.TBL_STUDENTS);
			query.append(" s ON se.");
			query.append(StudentExams.FLD_STUDENTID);
			query.append(" = s.");
			query.append(Student.FLD_STUDENTID);
			query.append("\nWHERE st.");
			query.append(StudyUnit.FLD_SEMESTER);
			query.append(" = '");
			query.append(semester);
			query.append("' AND st.");
			query.append(StudyUnit.FLD_EVENING);
			query.append(" = '");
			query.append(false);
			query.append("'");
			query.append("\nORDER BY se.");
			query.append(StudentExams.FLD_STUDENTID);
			query.append(" ASC");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			int lastStudentID = -1; 
			ArrayList<Integer> examList = new ArrayList<Integer>();
			
			while (rs.next())
			{
				int studentID = rs.getInt("StudID");
				int examID = rs.getInt("ExamID");
				
				if (lastStudentID != studentID && lastStudentID != -1)
				{
					studentExams.put(lastStudentID, examList);
					
					examList = new ArrayList<Integer>();
					examList.add(indexExamID.get(examID));
				}
				else
				{
					examList.add(indexExamID.get(examID));
				}
				lastStudentID = studentID;
			}
			studentExamsArr = new int[studentExams.size()][];
						
			int count = 0;
			for (Map.Entry<Integer, ArrayList<Integer>> studentExamRel : studentExams.entrySet()) 
			{	
				int studentID = studentExamRel.getKey();
				indexStudentID.put(studentID, count);
				count++;
			}
			
			for (Map.Entry<Integer, ArrayList<Integer>> studentExamRel : studentExams.entrySet()) 
			{	
				int index = indexStudentID.get(studentExamRel.getKey());
				ArrayList<Integer> examIDs = studentExamRel.getValue();
				
				studentExamsArr[index] = new int[examIDs.size()];
				
				for (int j=0; j < examIDs.size(); j++)
				{
					studentExamsArr[index][j] = examIDs.get(j);
				}
			}
		}
		catch (SQLException e)
		{
      		System.out.println("[ReadData.getAllStudentExams()]: " + e.getMessage());
		}
		
		return studentExamsArr;
	}
}