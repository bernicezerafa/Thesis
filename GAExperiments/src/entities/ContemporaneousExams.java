package entities;

import helpers.FileHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import thesis.timetable_generation.ExamMap;
import thesis.timetable_generation.InputParameters;

public class ContemporaneousExams {

	private int exam1ID;
	private int exam2ID;
	private int exam1Pos;
	private int exam2Pos;
	
	public static final String TBL_CONTEMPORANEOUS_EXAMS = "dbo.Contemporaneous_Exams";
	public static final String FLD_EXAM1ID = "Exam1ID";
	public static final String FLD_EXAM2ID = "Exam2ID";
	
	public ContemporaneousExams (int exam1ID, int exam2ID, int exam1Pos, int exam2Pos)
	{
		this.exam1ID = exam1ID;
		this.exam2ID = exam2ID;
		this.exam1Pos = exam1Pos;
		this.exam2Pos = exam2Pos;
	}
	
	public int getExam1ID() {
		return exam1ID;
	}

	public void setExam1ID(int exam1id) {
		exam1ID = exam1id;
	}

	public int getExam2ID() {
		return exam2ID;
	}

	public void setExam2ID(int exam2id) {
		exam2ID = exam2id;
	}

	public int getExam1Pos() {
		return exam1Pos;
	}

	public void setExam1Pos(int exam1Pos) {
		this.exam1Pos = exam1Pos;
	}

	public int getExam2Pos() {
		return exam2Pos;
	}

	public void setExam2Pos(int exam2Pos) {
		this.exam2Pos = exam2Pos;
	}
	
	public void insertSimultaneousExamRel(Connection conn) {
		
		StringBuffer query = null; 
		PreparedStatement pstmt = null;
		
		try
		{
			query = new StringBuffer();
		     
			query.append("INSERT INTO ");
			query.append(TBL_CONTEMPORANEOUS_EXAMS);
			query.append(" ( ");
			query.append(FLD_EXAM1ID);
			query.append(", ");
			query.append(FLD_EXAM2ID);
			query.append(" ) VALUES (?,?)");
		
			pstmt = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, exam1ID);
			pstmt.setInt(2, exam2ID);
			
			pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
      		System.out.println("[ContemporaneousExams.insertSimultaneousExamRel()]: " + e.getMessage());
		}
	}
	
	// SELECT ce.*
	// FROM ExamScheduling.dbo.CONTEMPORANEOUS_EXAMS ce
	// JOIN ExamScheduling.dbo.EXAMS e1 ON ce.Exam1ID = e1.ID
	// JOIN ExamScheduling.dbo.EXAMS e2 ON ce.Exam2ID = e2.ID
	// WHERE e1.Semester = '1' AND (ce.Exam1ID = '61' OR ce.Exam2ID = '61')
	
	public static HashMap<Integer, Boolean> getContemporaneousExams(Connection conn, int examId, int semester)
	{	
		StringBuffer query = null; 
		Statement stmt = null;
		HashMap<Integer, Boolean> contExams = new HashMap<Integer, Boolean>();
		
		try
		{
			query = new StringBuffer();
	        
			query.append("SELECT ce.* \nFROM ");
			query.append(TBL_CONTEMPORANEOUS_EXAMS);
			query.append(" ce \nJOIN ");
			query.append(Exam.TBL_EXAMS);
			query.append(" e1 ON ce.");
			query.append(FLD_EXAM1ID);
			query.append(" = e1.");
			query.append(Exam.FLD_ID);
			query.append("\nJOIN ");
			query.append(Exam.TBL_EXAMS);
			query.append(" e2 ON ce.");
			query.append(FLD_EXAM2ID);
			query.append(" = e2.");
			query.append(Exam.FLD_ID);
			query.append("\nWHERE e1.");
			query.append(Exam.FLD_SEMESTER);
			query.append(" = '");
			query.append(semester);
			query.append("' AND (ce.");
			query.append(FLD_EXAM1ID);
			query.append(" = '");
			query.append(examId);
			query.append("'");
			query.append(" OR ce.");
			query.append(FLD_EXAM2ID);
			query.append(" = '");
			query.append(examId);
			query.append("')");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			while (rs.next())
			{
				int exam1ID = rs.getInt(FLD_EXAM1ID);
				int exam2ID = rs.getInt(FLD_EXAM2ID);
				
				if (exam1ID == examId)
				{
					if (Exam.isEvening(conn, exam2ID)) contExams.put(exam2ID, true);
					else contExams.put(exam2ID, false);
				}	
				else if (exam2ID == examId)
				{
					if (Exam.isEvening(conn, exam1ID)) contExams.put(exam1ID, true);
					else contExams.put(exam1ID, false);
				}	
			}
		} 
		catch (SQLException e)
		{
      		System.out.println("[ContemporaneousExams.getContemporaneousExams(int examId)]: " + e.getMessage());
		}
		
		return contExams;
	}
	
	// SELECT e.Exam1ID, e.Exam2ID
	// FROM dbo.CONTEMPORANEOUS_EXAMS e 
	// JOIN dbo.Exams s ON e.Exam1ID = s.ID 
	// JOIN dbo.Exams st ON e.exam2ID = st.ID
	// WHERE s.semester = '2' AND s.noOfStudents > 0 AND st.noOfStudents > 0
	
	public static ArrayList<ExamMap> getAllExamRelationships(Connection conn) {
		
		StringBuffer query = null; 
		Statement stmt = null;
		InputParameters input = FileHelper.getInputParameters();
		
		HashMap<Integer, ExamMap> examsRelMap = new HashMap<Integer, ExamMap>();
		ArrayList<ExamMap> examsRel = new ArrayList<ExamMap>();
		
		try
		{
			query = new StringBuffer();
		     
			query.append("SELECT e.");
			query.append(FLD_EXAM1ID);
			query.append(", ");
			query.append(FLD_EXAM2ID);
			query.append("\nFROM ");
			query.append(TBL_CONTEMPORANEOUS_EXAMS);
			query.append(" e \nJOIN ");
			query.append(Exam.TBL_EXAMS);
			query.append(" s ON e.");
			query.append(FLD_EXAM1ID);
			query.append(" = s.");
			query.append(Exam.FLD_ID);
			query.append("\nJOIN ");
			query.append(Exam.TBL_EXAMS);
			query.append(" st ON e.");
			query.append(FLD_EXAM2ID);
			query.append(" = st.");
			query.append(Exam.FLD_ID);
			query.append("\nWHERE s.");
			query.append(Exam.FLD_SEMESTER);
			query.append(" = '");
			query.append(input.getSemester());
			query.append("' AND s.");
			query.append(Exam.FLD_NOOFSTUDENTS);
			query.append(" > 0 AND st.");
			query.append(Exam.FLD_NOOFSTUDENTS);
			query.append(" > 0 \nORDER BY e.");
			query.append(FLD_EXAM1ID);
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			int lastExam1ID = -1;
			
			while (rs.next())
			{
				int exam1ID = rs.getInt(FLD_EXAM1ID);
				int exam2ID = rs.getInt(FLD_EXAM2ID);
				
				if (lastExam1ID == exam1ID)
				{
					ExamMap examMap = examsRelMap.get(exam1ID);
					int[] data = Arrays.copyOf(examMap.getData(), examMap.getDimensions() + 1);
					data[examMap.getDimensions()] = exam2ID;
					
					examsRelMap.put(exam1ID, new ExamMap(data));
				}
				else if (lastExam1ID == -1 || lastExam1ID != exam1ID)
				{
					examsRelMap.put(exam1ID, new ExamMap(exam1ID, exam2ID));
				}
				
				lastExam1ID = exam1ID;
			}
			
			for (Entry<Integer, ExamMap> hashmap: examsRelMap.entrySet())
			{
				examsRel.add(hashmap.getValue());
			}
		}
		catch (SQLException e)
		{
      		System.out.println("[ContemporaneousExams.getAllExamRelationships()]: " + e.getMessage());
		}
		
		return examsRel;
	}
}
