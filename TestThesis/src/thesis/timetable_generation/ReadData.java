package thesis.timetable_generation;

import helpers.FileHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import entities.Student;
import entities.StudentExams;
import entities.StudyUnit;
 
public class ReadData 
{
	private int semester;

	private TreeMap<Integer, String> studentIDMap = new TreeMap<Integer, String>();
	private TreeMap<Integer, Integer> indexStudentID = new TreeMap<Integer, Integer>();
	
	private HashMap<Integer, ArrayList<Integer>> studentExams = new HashMap<Integer, ArrayList<Integer>>();
	private ArrayList<StudyUnit> studyUnits = new ArrayList<StudyUnit>();
	
	public ReadData(int semester)
	{
		this.semester = semester;
	}
	
	public TreeMap<Integer, String> getStudentIDMap() {
		return studentIDMap;
	}
	
	public TreeMap<Integer, Integer> getIndexStudentID() {
		return indexStudentID;
	}

	public ArrayList<StudyUnit> getStudyUnits() {
		return studyUnits;
	}
	
	// get experiment data
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
			gaParameters.setSameDayPunishment(Integer.parseInt(parameters.get(13)));
			gaParameters.setTwoDaysPunishment(Integer.parseInt(parameters.get(14)));
			gaParameters.setThreeDaysPunishment(Integer.parseInt(parameters.get(15)));
			
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
	
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	public int getTimeslotNoExamMoved(TreeMap<Integer, Timeslot> timeslotMap, Timeslot timeslotMoved) {
		
		int timeslotNo = -1;
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");
		
		String startDate = timeslotMoved.getStartDate();
		String endDate = timeslotMoved.getEndDate();
		
		DateTime startDateMoved = formatter.parseDateTime(startDate);
		DateTime endDateMoved = formatter.parseDateTime(endDate);
				
		for (Entry<Integer, Timeslot> entry : timeslotMap.entrySet())
		{
			Timeslot timeslot = entry.getValue();
			DateTime startDateThis = formatter.parseDateTime(timeslot.getStartDate());
			DateTime endDateThis = formatter.parseDateTime(timeslot.getEndDate());
			
			Interval intervalMoved = new Interval(startDateMoved, endDateMoved);
			Interval intervalThis = new Interval(startDateThis, endDateThis);

			boolean overlaps = intervalMoved.overlaps(intervalThis);
			
			if (startDateMoved.isEqual(startDateThis) || overlaps)
			{
				timeslotNo = entry.getKey();
				break;
			}
		}
		
		return timeslotNo;
	}
	
	@SuppressWarnings("unchecked")
	public static TreeMap<Integer, Integer> getIndexExamId()
	{
		ObjectInputStream inputStream = null;
		TreeMap<Integer, Integer> indexExamID = null;
		
		try
		{
			inputStream = FileHelper.getObjectReader("indexExamID.data");
			indexExamID = (TreeMap<Integer, Integer>) inputStream.readObject();
		}
		catch (Exception e)
		{
			System.out.println("[ReadData.getIndexExamId()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeInputStream(inputStream);
		}
		
		return indexExamID;
	}
	
	public void saveIndexExamId(TreeMap<Integer, Integer> indexExamID)
	{
		ObjectOutputStream outputStream = null;
		
		try
		{
			outputStream = FileHelper.getObjectWriter("indexExamID.data");
			outputStream.writeObject(indexExamID);
		}
		catch (IOException e)
		{
			System.out.println("[ReadData.saveIndexExamId()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeOutputStream(outputStream);
		}
	}
	
	public void mapExamIndexes(Connection conn)
	{
		//SELECT DISTINCT st.*
		//FROM dbo.StudyUnits st JOIN dbo.STUDENT_EXAMS se
		//ON st.UNITCODE = se.UNITCODE
		//WHERE st.semester = '1' AND st.evening = 'false';
		StringBuffer query = null; 
		Statement stmt = null;
		TreeMap<Integer, Integer> indexExamID = new TreeMap<Integer, Integer>();
		
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
			
			saveIndexExamId(indexExamID);
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
			query.append(" AS 'StudID', s.");
			query.append(Student.FLD_STUDENTID);
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
				String studentIdNumber = rs.getString(Student.FLD_STUDENTID);
				
				studentIDMap.put(studentID, studentIdNumber);
				
				int examID = rs.getInt("ExamID");
				TreeMap<Integer, Integer> indexExamId = getIndexExamId();
				
				if (lastStudentID != studentID && lastStudentID != -1)
				{
					studentExams.put(lastStudentID, examList);
					
					examList = new ArrayList<Integer>();
					examList.add(indexExamId.get(examID));
				}
				else
				{
					examList.add(indexExamId.get(examID));
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