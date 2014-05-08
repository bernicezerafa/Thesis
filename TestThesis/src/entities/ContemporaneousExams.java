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

import thesis.timetable_generation.InputParameters;
import thesis.timetable_generation.IntTuple;

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
	
	// SELECT e.Exam1ID, e.Exam2ID
	// FROM dbo.CONTEMPORANEOUS_EXAMS e 
	// JOIN dbo.STUDYUNITS s ON e.Exam1ID = s.ID 
	// JOIN dbo.STUDYUNITS st ON e.exam2ID = st.ID
	// WHERE s.semester = '1'
	
	public static ArrayList<IntTuple> getAllExamRelationships(Connection conn) {
		
		StringBuffer query = null; 
		Statement stmt = null;
		InputParameters input = FileHelper.getInputParameters();
		HashMap<Integer, Integer> indexExamID = FileHelper.getIndexExamId();
		
		HashMap<Integer, IntTuple> examsRelMap = new HashMap<Integer, IntTuple>();
		ArrayList<IntTuple> examsRel = new ArrayList<IntTuple>();
		
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
			query.append(StudyUnit.TBL_STUDYUNITS);
			query.append(" s ON e.");
			query.append(FLD_EXAM1ID);
			query.append(" = s.");
			query.append(StudyUnit.FLD_ID);
			query.append("\nJOIN ");
			query.append(StudyUnit.TBL_STUDYUNITS);
			query.append(" st ON e.");
			query.append(FLD_EXAM2ID);
			query.append(" = st.");
			query.append(StudyUnit.FLD_ID);
			query.append("\nWHERE s.");
			query.append(StudyUnit.FLD_SEMESTER);
			query.append(" = '");
			query.append(input.getSemester());
			query.append("' \n ORDER BY e.");
			query.append(FLD_EXAM1ID);
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			int lastExam1ID = -1;
			
			while (rs.next())
			{
				int exam1ID = indexExamID.get(rs.getInt(FLD_EXAM1ID));
				int exam2ID = indexExamID.get(rs.getInt(FLD_EXAM2ID));
				
				if (lastExam1ID == exam1ID)
				{
					IntTuple examMap = examsRelMap.get(exam1ID);
					int[] data = Arrays.copyOf(examMap.getData(), examMap.getDimensions() + 1);
					data[examMap.getDimensions()] = exam2ID;
					
					examsRelMap.put(exam1ID, IntTuple.of(data));
				}
				else if (lastExam1ID == -1 || lastExam1ID != exam1ID)
				{
					examsRelMap.put(exam1ID, IntTuple.of(exam1ID, exam2ID));
				}
				
				lastExam1ID = exam1ID;
			}
			
			for (Entry<Integer, IntTuple> hashmap: examsRelMap.entrySet())
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
