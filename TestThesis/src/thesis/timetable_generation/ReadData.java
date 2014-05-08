package thesis.timetable_generation;

import helpers.FileHelper;

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
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import entities.Student;
import entities.StudentExams;
import entities.StudyUnit;
 
public class ReadData 
{
	private static int semester;
	private ArrayList<StudyUnit> studyUnits = null;
	
	public ReadData(int semester) {
		ReadData.semester = semester;
	}
	
	public ArrayList<StudyUnit> getStudyUnits() {
		return studyUnits;
	}
	
	public static int getSemester() {
		return semester;
	}

	public static void setSemester(int semester) {
		ReadData.semester = semester;
	}

	public GAParameters getGAParameters()
	{
		BufferedReader bufRdr = null;
		ArrayList<String> parameters = null;  
		GAParameters gaParameters = null;
		
		try
		{
			File file = new File("C://Users//Bernice//Desktop//GA Parameters.csv");
	    	
			if (file.exists())
			{
				bufRdr = new BufferedReader(new FileReader(file));
				parameters = new ArrayList<String>();
		    	String line = null;
		    	
		    	while ((line = bufRdr.readLine()) != null)
		    	{
		    		StringTokenizer st = new StringTokenizer(line, ",");
		    		
		    		while (st.hasMoreTokens())
		    		{
		    			String next_arg = st.nextToken();
		    			
		    			if (!next_arg.trim().equals(""))
		    				parameters.add(next_arg.trim());
		    		}
				}
			}
			
			gaParameters = new GAParameters();
			
			gaParameters.setCrossoverType(Integer.parseInt(parameters.get(0)));
			gaParameters.setCrossoverRate(Double.parseDouble(parameters.get(1)));
			gaParameters.setMutationRate(Double.parseDouble(parameters.get(2)));
			gaParameters.setInverseSquarePressure(Integer.parseInt(parameters.get(3)));
			gaParameters.setNoOfGenerations(Integer.parseInt(parameters.get(4)));
			gaParameters.setNoOfChromosomes(Integer.parseInt(parameters.get(5)));
			gaParameters.setElitistSelection(Integer.parseInt(parameters.get(6)));
			gaParameters.setRandomIntroduction(Integer.parseInt(parameters.get(7)));
			gaParameters.setInterpolatingRates(Integer.parseInt(parameters.get(8)));
			gaParameters.setMinCrossoverRate(Double.parseDouble(parameters.get(9)));
			gaParameters.setMaxMutationRate(Double.parseDouble(parameters.get(10)));
			gaParameters.setStepValue(Double.parseDouble(parameters.get(11)));
			
			gaParameters.setClashPunishment(Integer.parseInt(parameters.get(12)));
			gaParameters.setEveningPunishment(Integer.parseInt(parameters.get(13)));
			gaParameters.setSameDayPunishment(Integer.parseInt(parameters.get(14)));
			gaParameters.setTwoDaysPunishment(Integer.parseInt(parameters.get(15)));
			gaParameters.setTwentyHourPunishment(Integer.parseInt(parameters.get(16)));
			gaParameters.setThreeDaysPunishment(Integer.parseInt(parameters.get(17)));
			gaParameters.setSpreadOutPunishment(Integer.parseInt(parameters.get(18)));
			gaParameters.setNoOfStudentPunishment(Integer.parseInt(parameters.get(19)));
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
		return gaParameters;
	}
	
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) 
	{
	    for (Entry<T, E> entry : map.entrySet()) 
	    {
	        if (value.equals(entry.getValue())) 
	            return entry.getKey();
	    }
	    return null;
	}
		
	public HashMap<Integer, Integer> mapExamIndexes(Connection conn)
	{
		//SELECT DISTINCT st.*
		//FROM dbo.StudyUnits st JOIN dbo.STUDENT_EXAMS se
		//ON st.ID = se.ExamID
		//WHERE st.semester = '1' AND st.noOfStudents > 0;
		
		StringBuffer query = null; 
		Statement stmt = null;
		HashMap<Integer, Integer> indexExamID = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> eveningIndexExamID = new HashMap<Integer, Integer>();
		studyUnits = new ArrayList<StudyUnit>();
		
		try
		{
			query = new StringBuffer();
	        
			query.append("SELECT DISTINCT st.*");
			query.append("\nFROM ");
			query.append(StudyUnit.TBL_STUDYUNITS);
			query.append(" st JOIN ");
			query.append(StudentExams.TBL_STUDENTEXAMS);
			query.append(" se \nON st.");
			query.append(StudyUnit.FLD_ID);
			query.append(" = se.");
			query.append(StudentExams.FLD_EXAMID);
			query.append("\nWHERE st.");
			query.append(StudyUnit.FLD_SEMESTER);
			query.append(" = '");
			query.append(semester);
			query.append("' AND st.");
			query.append(StudyUnit.FLD_NOOFSTUDENTS);
			query.append(" > 0");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			int count = 0;
			while (rs.next())
			{
				int examID = rs.getInt(StudyUnit.FLD_ID);
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
			
				StudyUnit studyUnit = new StudyUnit(examID, unitcode, title, year, semester, examLength, noOfStudents, department, credits, evening, venue);
				studyUnit.setExamPos(count);
				studyUnits.add(studyUnit);
				
				indexExamID.put(examID, count);
				if (evening)
				{
					eveningIndexExamID.put(examID, count);
				}
				
				count++;
			}
			
			FileHelper.saveIndexExamId(indexExamID);
			FileHelper.saveEveningIndexExamId(eveningIndexExamID);
		}
		catch (SQLException e)
		{
      		System.out.println("[ReadData.mapExamIndexes()]: " + e.getMessage());
		}
		return indexExamID;
	}
	
	// builds a hashmap, mapping the student with each of his/her study units
	public HashMap<String, ArrayList<Integer>> getStudentExamRel(Connection conn)
	{
		//SELECT st.*, s.StudentID
		//FROM dbo.STUDENT_EXAMS se
		//JOIN dbo.STUDYUNITS st ON se.ExamID = st.ID
		//JOIN dbo.STUDENTS s ON se.StudentID = s.StudentID
		//WHERE st.semester = '1' AND st.noOfStudents > 0
		//ORDER BY se.StudentID ASC;
		
		// map exam ID's in database with an incrementing counter
		HashMap<Integer, Integer> indexExamID = mapExamIndexes(conn);
		HashMap<String, ArrayList<Integer>> clashesMatrix = new HashMap<String, ArrayList<Integer>>();
		
		StringBuffer query = null; 
		Statement stmt = null;
		
		try
		{
			query = new StringBuffer();
	        
			query.append("SELECT st.");
			query.append(StudyUnit.FLD_ID);
			query.append(", s.");
			query.append(Student.FLD_STUDENTID);
			query.append("\nFROM ");
			query.append(StudentExams.TBL_STUDENTEXAMS);
			query.append(" se \nJOIN ");
			query.append(StudyUnit.TBL_STUDYUNITS);
			query.append(" st ON se.");
			query.append(StudentExams.FLD_EXAMID);
			query.append(" = st.");
			query.append(StudyUnit.FLD_ID);
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
			query.append(StudyUnit.FLD_NOOFSTUDENTS);
			query.append(" > 0 \nORDER BY se.");
			query.append(StudentExams.FLD_STUDENTID);
			query.append(" ASC");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());

			ArrayList<Integer> exams = null;
						
			while (rs.next())
			{
				String studentId = rs.getString(Student.FLD_STUDENTID);
				int examID = rs.getInt(StudyUnit.FLD_ID);
				
				int examPos = indexExamID.get(examID);
				
				if (clashesMatrix.get(studentId) == null)
				{
					exams = new ArrayList<Integer>();
					exams.add(examPos);
					clashesMatrix.put(studentId, exams);					
				}
				else
				{
					clashesMatrix.get(studentId).add(examPos);
				}
			}
		}
		catch (SQLException e)
		{
      		System.out.println("[ReadData.getStudentExamRel()]: " + e.getMessage());
		}
		
		return clashesMatrix;
	}
}