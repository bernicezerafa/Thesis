package thesis.timetable_generation;

import helpers.FileHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import entities.ContemporaneousExams;
import entities.Exam;
import entities.Student;
import entities.StudentExams;
 
public class ReadData 
{
	private static int semester;	
	private HashMap<Integer, Exam> studyUnits = null;
	private ExamIndexMaps examIndexMaps;
	
	public ReadData() {
		InputParameters params = getInputParameters();
		ReadData.semester = params.getSemester();
	}
	
	public HashMap<Integer, Exam> getStudyUnits() {
		return studyUnits;
	}
	
	public static int getSemester() {
		return semester;
	}

	public static void setSemester(int semester) {
		ReadData.semester = semester;
	}

	@SuppressWarnings("rawtypes")
	public GAParameters getGAParameters(String experimentName) {
		
		GAParameters gaParameters = new GAParameters();
		
		try {
			
			Properties properties = new Properties();
			InputStream in = ReadData.class.getResourceAsStream("/" + experimentName + ".properties");
			properties.load(in);
			in.close();

			Enumeration enuKeys = properties.keys();
			
			while (enuKeys.hasMoreElements()) {
				
				String propertyName = (String) enuKeys.nextElement();
				String methodName = "set" + StringUtils.capitalize(propertyName);

				String value = properties.getProperty(propertyName);
				Object parameterType = null;
				
				if ((propertyName.contains("Rate") && !propertyName.contains("interpolating")) 
					|| propertyName.contains("Value")) {
					Double dblValue = Double.parseDouble(value);
					parameterType = dblValue;
				
				} else {
					Integer intValue = Integer.parseInt(value);
					parameterType = intValue;
				}
				
				try {
					
					// invoke appropriate setter method for this property
					gaParameters.getClass().getMethod(methodName, parameterType.getClass())
										   .invoke(gaParameters, parameterType);
				
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return gaParameters;
	}
	
	@SuppressWarnings("rawtypes")
	public InputParameters getInputParameters() {
		
		InputParameters inputParameters = new InputParameters();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		
		try {
			
			Properties properties = new Properties();
			InputStream in = ReadData.class.getResourceAsStream("/input_parameters.properties");
			properties.load(in);
			in.close();

			Enumeration enuKeys = properties.keys();
			
			while (enuKeys.hasMoreElements()) {
				
				String propertyName = (String) enuKeys.nextElement();
				String methodName = "set" + StringUtils.capitalize(propertyName);
				
				String value = properties.getProperty(propertyName);
				Object parameterType = null;
				
				if (propertyName.contains("Date")) {
					
					try {
						parameterType = sdf.parse(value);
					} catch (ParseException e) {
						e.printStackTrace();
					}
						
				} else if (propertyName.contains("include")) {
					parameterType = Boolean.parseBoolean(value);
					
				} else {
					parameterType = Integer.parseInt(value);
				}
				
				try {
					
					// invoke appropriate setter method for this property
					inputParameters.getClass().getMethod(methodName, parameterType.getClass())
											  .invoke(inputParameters, parameterType);
				
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return inputParameters;
	}
	
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    
		for (Entry<T, E> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) 
	            return entry.getKey();
	    }
	    return null;
	}
		
	public void mapExamIndexes(Connection conn) {
		
		// SELECT *
		// FROM dbo.Exams 
		// WHERE st.semester = '1';
		
		StringBuffer query = null; 
		Statement stmt = null;
		
		examIndexMaps = new ExamIndexMaps();		
		studyUnits = new HashMap<Integer, Exam>();
		
		try {
			
			query = new StringBuffer();
	        
			query.append("SELECT *");
			query.append("\nFROM ");
			query.append(Exam.TBL_EXAMS);
			query.append("\nWHERE ");
			query.append(Exam.FLD_SEMESTER);
			query.append(" = '");
			query.append(semester);
			query.append("' AND ");
			query.append(Exam.FLD_NOOFSTUDENTS);
			query.append(" > 0");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			int count = 0;
			while (rs.next()) {
				
				int examID = rs.getInt(Exam.FLD_ID);
				String unitcode = rs.getString(Exam.FLD_UNITCODE);
				String title = rs.getString(Exam.FLD_TITLE);
				String year = rs.getString(Exam.FLD_YEAR);
				short sem = rs.getShort(Exam.FLD_SEMESTER);
				float examLength = rs.getFloat(Exam.FLD_EXAMLENGTH);
				short noOfStudents = rs.getShort(Exam.FLD_NOOFSTUDENTS);
				String department = rs.getString(Exam.FLD_DEPARTMENT);
				short credits = rs.getShort(Exam.FLD_CREDITS);
				boolean evening = rs.getBoolean(Exam.FLD_EVENING);
				String venue = rs.getString(Exam.FLD_VENUE);
				
				Exam studyUnit = new Exam(examID, unitcode, title, year, sem, examLength, noOfStudents, department, credits, evening, venue);
				studyUnits.put(examID, studyUnit);
				
				HashMap<Integer, Boolean> contExams = ContemporaneousExams.getContemporaneousExams(conn, examID, semester);
				
				if (contExams.size() > 0) {
					
					for (Entry<Integer, Boolean> contExamsEntry: contExams.entrySet()) {
							
						int examId = contExamsEntry.getKey();
						boolean isEvening = contExamsEntry.getValue();
							
						if (isEvening) examIndexMaps.addEveningIndexExamEntry(count, examId);
						
						if (!examIndexMaps.isMappedToTimeslot(count, examId)) {
							examIndexMaps.addIndexExamEntry(count, examId);
						}
					}
				}
					
				if (!examIndexMaps.isMappedToTimeslot(count, examID)) {
					examIndexMaps.addIndexExamEntry(count, examID);
					if (evening) examIndexMaps.addEveningIndexExamEntry(count, examID);
					count++;
				}
				//count++;
			}
			
			FileHelper.saveExamIndexMaps(examIndexMaps);
		
		} catch (SQLException e) {
      		System.out.println("[ReadData.mapExamIndexes()]: " + e.getMessage());
		}
	}
	
	// builds a hashmap, mapping the student with each of his/her study units
	public HashMap<String, ArrayList<Pair<Integer, Integer>>> getStudentExamRel(Connection conn) {
		//SELECT st.*, s.StudentID
		//FROM dbo.STUDENT_EXAMS se
		//JOIN dbo.Exams st ON se.ExamID = st.ID
		//JOIN dbo.STUDENTS s ON se.StudentID = s.StudentID
		//WHERE st.semester = '1' AND st.noOfStudents > 0
		//ORDER BY se.StudentID ASC;
		
		// map exam ID's in database with an incrementing counter
		mapExamIndexes(conn);
		
		HashMap<String, ArrayList<Pair<Integer, Integer>>> clashesMatrix = new HashMap<String, ArrayList<Pair<Integer, Integer>>>();
		HashMap<Integer, Integer> examIDIndex = examIndexMaps.getExamIDIndex();
		
		StringBuffer query = null; 
		Statement stmt = null;
		
		try {
			
			query = new StringBuffer();
	        
			query.append("SELECT st.");
			query.append(Exam.FLD_ID);
			query.append(", s.");
			query.append(Student.FLD_STUDENTID);
			query.append("\nFROM ");
			query.append(StudentExams.TBL_STUDENTEXAMS);
			query.append(" se \nJOIN ");
			query.append(Exam.TBL_EXAMS);
			query.append(" st ON se.");
			query.append(StudentExams.FLD_EXAMID);
			query.append(" = st.");
			query.append(Exam.FLD_ID);
			query.append("\nJOIN ");
			query.append(Student.TBL_STUDENTS);
			query.append(" s ON se.");
			query.append(StudentExams.FLD_STUDENTID);
			query.append(" = s.");
			query.append(Student.FLD_STUDENTID);
			query.append("\nWHERE st.");
			query.append(Exam.FLD_SEMESTER);
			query.append(" = '");
			query.append(semester);
			query.append("' AND st.");
			query.append(Exam.FLD_NOOFSTUDENTS);
			query.append(" > 0 \nORDER BY se.");
			query.append(StudentExams.FLD_STUDENTID);
			query.append(" ASC");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());

			ArrayList<Pair<Integer, Integer>> exams = null;
						
			while (rs.next()) {
				
				String studentId = rs.getString(Student.FLD_STUDENTID);
				int examID = rs.getInt(Exam.FLD_ID);
				
				int examPos = examIDIndex.get(examID);
				List<Integer> examsInThisPos = examIndexMaps.getIndexExamID().get(examPos);
				int posInList = examsInThisPos.indexOf(examID);
				
				Pair<Integer, Integer> examPair = new MutablePair<Integer, Integer>(examPos, posInList);
				
				if (clashesMatrix.get(studentId) == null) {
					exams = new ArrayList<Pair<Integer, Integer>>();
					exams.add(examPair);
					clashesMatrix.put(studentId, exams);
				} else {
					clashesMatrix.get(studentId).add(examPair);
				}
			}
			
		} catch (SQLException e) {
      		System.out.println("[ReadData.getStudentExamRel()]: " + e.getMessage());
      		
		} catch (Exception e2) {
			System.out.println("[ReadData.getStudentExamRel()]: " + e2.getMessage());
			e2.printStackTrace();
		}
		
		return clashesMatrix;
	}
}