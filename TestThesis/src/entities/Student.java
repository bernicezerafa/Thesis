package entities;

import helpers.SQLHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Student {

	private String studentID;
	
	public static final String TBL_STUDENTS = "dbo.Students";
	public static final String FLD_ID = "ID";
	public static final String FLD_STUDENTID = "StudentID";
	
	public Student(String studentID) {
		this.studentID = studentID;
	}
	
	public String getStudentID() {
		return studentID;
	}

	public void setStudentID(String studentID) {
		this.studentID = studentID;
	}

	public void insertStudent(Connection conn) {
		
		StringBuffer query = null; 
		PreparedStatement pstmt = null;
		
		try
		{
			query = new StringBuffer();
		     
			query.append("INSERT INTO ");
			query.append(TBL_STUDENTS);
			query.append(" ( ");
			query.append(FLD_STUDENTID);
			query.append(") VALUES (?)");
		
			pstmt = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, studentID);
			
			pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
      		System.out.println("[Student.insertStudent()]: " + e.getMessage());
		}		
	}
	
	public static ArrayList<String> getStudentSuggestions(Connection conn, String pattern) {
	
		StringBuffer query = null;
		Statement stmt = null;
		ArrayList<String> studentIds = new ArrayList<String>();
		
		try {
   			
   	    	query = new StringBuffer();
   	    	
   	    	query.append("SELECT * \nFROM ");
   			query.append(TBL_STUDENTS);
   			
   			if (pattern != null)
   			{
   	   			query.append("\nWHERE ");			
   	   			query.append(FLD_STUDENTID);
   	   			query.append(" LIKE '%");
   	   			query.append(pattern);
   	   			query.append("%'");
   			}
   			
   			stmt = conn.createStatement();
   	    	ResultSet rs = stmt.executeQuery(query.toString());

   	    	while (rs.next()) {
   	    		studentIds.add(rs.getString(FLD_STUDENTID));
   	    	}
   	    	
   	    } catch (SQLException e) {
      		System.out.println("[Student.getStudentSuggestions(String pattern)]: " + e.getMessage());
        
   	    } finally {
   	    	SQLHelper.closeConnection(conn);
        }
		
		return studentIds;
	}
	
	public static boolean studentExists(Connection conn, String studentID) {
		
		StringBuffer query = null;
		Statement stmt = null;
		
		try {
   			
   	    	query = new StringBuffer();
   	    	
   	    	query.append("SELECT * \nFROM ");
   			query.append(TBL_STUDENTS);
 	   		query.append("\nWHERE ");			
   	   		query.append(FLD_STUDENTID);
   	   		query.append(" = '");
   	   		query.append(studentID);
   	   		query.append("'");
   		
   			stmt = conn.createStatement();
   	    	ResultSet rs = stmt.executeQuery(query.toString());

   	    	while (rs.next()) {
   	    		return true;
   	    	}
   	    	
   	    } catch (SQLException e) {
      		System.out.println("[Student.studentExists(String studentID)]: " + e.getMessage());
        
   	    } 
		
		return false;
	}
	
}
