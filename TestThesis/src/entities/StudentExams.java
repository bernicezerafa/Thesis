package entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class StudentExams {

	private String unitCode;
	private String studentID;
	
	public static final String TBL_STUDENTEXAMS = "dbo.Student_Exams";
	public static final String FLD_UNITCODE = "UnitCode";
	public static final String FLD_STUDENTID = "StudentID";
	
	public void insertStudentExamRel(Connection conn) {
		
		StringBuffer query = null; 
		PreparedStatement pstmt = null;
		
		try
		{
			query = new StringBuffer();
		     
			query.append("INSERT INTO ");
			query.append(TBL_STUDENTEXAMS);
			query.append(" ( ");
			query.append(FLD_UNITCODE);
			query.append(", ");
			query.append(FLD_STUDENTID);
			query.append(" ) ");
			
			query.append(") VALUES (?,?)");
		
			pstmt = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, unitCode);
			pstmt.setString(2, studentID);

		}
		catch (SQLException e)
		{
      		System.out.println("[StudentExams.insertStudentExamRel()]: " + e.getMessage());
		}
	}
}
