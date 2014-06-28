package entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class StudentExams 
{
	private String studentID;
	private int examID;
	
	public static final String TBL_STUDENTEXAMS = "dbo.Student_Exams";
	public static final String FLD_EXAMID = "ExamID";
	public static final String FLD_STUDENTID = "StudentID";
	
	public StudentExams(int examID, String studentID)
	{
		this.examID = examID;
		this.studentID = studentID;
	}
	
	public void insertStudentExamRel(Connection conn) 
	{	
		StringBuffer query = null; 
		PreparedStatement pstmt = null;
		
		try
		{
			query = new StringBuffer();
		     
			query.append("INSERT INTO ");
			query.append(TBL_STUDENTEXAMS);
			query.append(" ( ");
			query.append(FLD_EXAMID);
			query.append(", ");
			query.append(FLD_STUDENTID);
			query.append(" ) VALUES (?,?)");
		
			pstmt = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, examID);
			pstmt.setString(2, studentID);
			
			pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
      		System.out.println("[StudentExams.insertStudentExamRel()]: " + e.getMessage());
		}
	}
		
	// SELECT se.StudentID
	// FROM dbo.STUDENT_EXAMS se JOIN dbo.StudyUnits st ON se.ExamID = st.ID
	// WHERE st.UnitCode = 'unitCode'
	
	public static ArrayList<String> getStudentsInExam(Connection conn, int unitId)
	{
		StringBuffer query = null; 
		Statement stmt = null;
		ArrayList<String> studentIds = new ArrayList<String>();
		
		try
		{
			query = new StringBuffer();
		     
			query.append("SELECT se.");
			query.append(StudentExams.FLD_STUDENTID);
			query.append("\nFROM ");
			query.append(TBL_STUDENTEXAMS);
			query.append(" se JOIN ");
			query.append(StudyUnit.TBL_STUDYUNITS);
			query.append(" st ON se.");
			query.append(StudentExams.FLD_EXAMID);
			query.append(" = st.");
			query.append(StudyUnit.FLD_ID);
			query.append("\nWHERE st.");
			query.append(StudyUnit.FLD_ID);
			query.append(" = '");
			query.append(unitId);
			query.append("'");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			while (rs.next())
			{
				String studentID = rs.getString(FLD_STUDENTID);
				studentIds.add(studentID);
			}
		}
		catch (SQLException e)
		{
      		System.out.println("[StudentExams.insertStudentExamRel()]: " + e.getMessage());
		}
		
		return studentIds;
	}
	
	// SELECT st.UnitCode
	// FROM dbo.Student_Exams se JOIN dbo.StudyUnits st ON se.ExamID = st.ID
	// WHERE se.StudentID = '306993M';
	
	public static ArrayList<String> getStudentExams(Connection conn, String studentID)
	{
		StringBuffer query = null; 
		ArrayList<String> studyUnitCodes = new ArrayList<String>();
		
		try
		{
			query = new StringBuffer();
		     
			query.append("SELECT st.");
			query.append(StudyUnit.FLD_UNITCODE);
			query.append(", ");
			query.append(StudyUnit.FLD_EVENING);
			query.append("\nFROM ");
			query.append(TBL_STUDENTEXAMS);
			query.append(" se JOIN ");
			query.append(StudyUnit.TBL_STUDYUNITS);
			query.append(" st ON se.");
			query.append(StudentExams.FLD_EXAMID);
			query.append(" = st.");
			query.append(StudyUnit.FLD_ID);
			query.append("\nWHERE se.");
			query.append(Student.FLD_STUDENTID);
			query.append(" = '");
			query.append(studentID);
			query.append("'");
			
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			while (rs.next()) 
			{
				String unitCode = rs.getString(StudyUnit.FLD_UNITCODE);
				boolean evening = rs.getBoolean(StudyUnit.FLD_EVENING);
				
				if (evening)
					studyUnitCodes.add(unitCode + " - evening");
				else
					studyUnitCodes.add(unitCode);
			}
		}
		catch (SQLException e)
		{
      		System.out.println("[StudentExams.getStudentExams(String studentID)]: " + e.getMessage());
		}
		
		return studyUnitCodes;
	}
	
	// DELETE se 
	// FROM dbo.STUDENT_EXAMS se JOIN dbo.STUDYUNITS st ON se.ExamID = st.ID		
	// WHERE se.StudentID = '123456M' if (unitCode != null) { AND st.UnitCode = 'CIS3087' }
	
	// if UnitCode = null, student is dropped from all his units
	public void dropStudentFromUnit(Connection conn)
	{
		StringBuffer query = null; 
		
		try
		{
			query = new StringBuffer();
		     
			query.append("DELETE se \nFROM ");
			query.append(TBL_STUDENTEXAMS);
			query.append(" se ");
			query.append("\nWHERE se.");
			query.append(Student.FLD_STUDENTID);
			query.append(" = '");
			query.append(studentID);
			query.append("' AND se.");
			query.append(StudentExams.FLD_EXAMID);
			query.append(" = '");
			query.append(examID);
			query.append("'");
			
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(query.toString());
		}
		catch (SQLException e)
		{
      		System.out.println("[StudentExams.dropStudentFromUnit()]: " + e.getMessage());
		}
	}
	
	public boolean studentExamRelExists(Connection conn)
	{
		StringBuffer query = null; 
		
		try
		{
			query = new StringBuffer();
		     
			query.append(" SELECT * \nFROM ");
			query.append(TBL_STUDENTEXAMS);
			query.append("\nWHERE ");
			query.append(Student.FLD_STUDENTID);
			query.append(" = '");
			query.append(studentID);
			query.append("' AND ");
			query.append(StudentExams.FLD_EXAMID);
			query.append(" = '");
			query.append(examID);
			query.append("'");
			
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			while (rs.next())
			{
				return true;
			}
		}
		catch (SQLException e)
		{
      		System.out.println("[StudentExams.studentExamRelExists()]: " + e.getMessage());
		}
		return false;
	}
	
}