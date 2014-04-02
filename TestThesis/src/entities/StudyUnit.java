package entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class StudyUnit {

	private int id;
	private String unitCode;
	private String title;
	private String year;
	private short semester;
	private float examLength;
	private short noOfStudents;
	private String department;
	private short credits;
	private boolean evening;
	private String venue;
	
	public static final String TBL_STUDYUNITS = "dbo.StudyUnits";
	public static final String FLD_ID = "ID";
	public static final String FLD_UNITCODE = "UnitCode";
	public static final String FLD_TITLE = "Title";
	public static final String FLD_YEAR = "Year";
	public static final String FLD_SEMESTER = "Semester";
	public static final String FLD_EXAMLENGTH = "ExamLength";
	public static final String FLD_NOOFSTUDENTS = "NoOfStudents";
	public static final String FLD_DEPARTMENT = "Department";
	public static final String FLD_CREDITS = "Credits";
	public static final String FLD_EVENING = "Evening";
	public static final String FLD_VENUE = "Venue";
	
	public StudyUnit() {
		
	}
	
	public StudyUnit(String unitCode, String title, String year, short semester, float examLength, 
					 short noOfStudents, String department, short credits, boolean evening, String venue) {
		
		this.unitCode = unitCode;
		this.title = title;
		this.year = year;
		this.semester = semester;
		this.examLength = examLength;
		this.noOfStudents = noOfStudents;
		this.department = department;
		this.credits = credits;
		this.evening = evening;
		this.venue = venue;
	}
	
	public int getID() {
		return id;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public String getUnitCode() {
		return unitCode;
	}
	
	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getYear() {
		return year;
	}
	
	public void setYear(String year) {
		this.year = year;
	}
	
	public short getSemester() {
		return semester;
	}
	
	public void setSemester(short semester) {
		this.semester = semester;
	}
	
	public float getExamLength() {
		return examLength;
	}
	
	public void setExamLength(float examLength) {
		this.examLength = examLength;
	}
	
	public short getNoOfStudents() {
		return noOfStudents;
	}
	
	public void setNoOfStudents(short noOfStudents) {
		this.noOfStudents = noOfStudents;
	}
	
	public String getDepartment() {
		return department;
	}
	
	public void setDepartment(String department) {
		this.department = department;
	}
	
	public short getCredits() {
		return credits;
	}
	
	public void setCredits(short credits) {
		this.credits = credits;
	}
	
	public boolean isEvening() {
		return evening;
	}
	
	public void setEvening(boolean evening) {
		this.evening = evening;
	}
	
	public String getVenue() {
		return venue;
	}
	
	public void setVenue(String venue) {
		this.venue = venue;
	}

	public void insertStudyUnit(Connection conn)
	{ 		
		StringBuffer query = null; 
		PreparedStatement pstmt = null;
		
		try
		{
			query = new StringBuffer();
		     
			query.append("INSERT INTO ");
			query.append(TBL_STUDYUNITS);
			query.append(" ( ");
			query.append(FLD_UNITCODE);
			query.append(", ");
			query.append(FLD_TITLE);
			query.append(", ");
			query.append(FLD_YEAR);
			query.append(", ");
			query.append(FLD_SEMESTER);
			query.append(", ");
			query.append(FLD_EXAMLENGTH);
			query.append(", ");
			query.append(FLD_NOOFSTUDENTS);
			query.append(", ");
			query.append(FLD_DEPARTMENT);
			query.append(", ");
			query.append(FLD_CREDITS);
			query.append(", ");
			query.append(FLD_EVENING);
			query.append(", ");
			query.append(FLD_VENUE);
			query.append(") VALUES (?,?,?,?,?,?,?,?,?,?)");
		
			pstmt = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, unitCode);
			pstmt.setString(2, title);
			pstmt.setString(3, year);
			pstmt.setShort(4, semester);
			pstmt.setFloat(5, examLength);
			pstmt.setShort(6, noOfStudents);
			pstmt.setString(7, department);
			pstmt.setShort(8, credits);
			pstmt.setBoolean(9, evening);
			pstmt.setString(10, venue);
			
			pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
      		System.out.println("[StudyUnits.insertStudyUnit()]: " + e.getMessage());
		}		
	}
	
	public void updateStudyUnit(Connection conn)
	{ 		
		StringBuffer query = null; 
		PreparedStatement pstmt = null;
		
		try
		{
			query = new StringBuffer();
		     
			query.append("UPDATE ");
			query.append(TBL_STUDYUNITS);
			query.append("\nSET ");
			query.append(FLD_UNITCODE);
			query.append(" =?, ");
			query.append(FLD_TITLE);
			query.append(" =?, ");
			query.append(FLD_YEAR);
			query.append(" =?, ");
			query.append(FLD_SEMESTER);
			query.append(" =?, ");
			query.append(FLD_EXAMLENGTH);
			query.append(" =?, ");
			query.append(FLD_NOOFSTUDENTS);
			query.append(" =?, ");
			query.append(FLD_DEPARTMENT);
			query.append(" =?, ");
			query.append(FLD_CREDITS);
			query.append(" =?, ");	
			query.append(FLD_EVENING);
			query.append(" =?, ");
			query.append(FLD_VENUE);
			query.append(" =? ");
			query.append("\nWHERE ");
			query.append(FLD_UNITCODE);
			query.append(" = '");
			query.append(unitCode);
			query.append("'");
			
			pstmt = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, unitCode);
			pstmt.setString(2, title);
			pstmt.setString(3, year);
			pstmt.setShort(4, semester);
			pstmt.setFloat(5, examLength);
			pstmt.setShort(6, noOfStudents);
			pstmt.setString(7, department);
			pstmt.setShort(8, credits);
			pstmt.setBoolean(9, evening);
			pstmt.setString(10, venue);
			
			pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
      		System.out.println("[StudyUnits.updateStudyUnit()]: " + e.getMessage());
		}		
	}

	public static void deleteStudyUnit(Connection conn, String unitCode)
	{
		StringBuffer query = null; 
		Statement stmt = null;
		
		try
		{
			query = new StringBuffer();
	        
			query.append("DELETE FROM ");
			query.append(TBL_STUDYUNITS);
			query.append(" WHERE ");
			query.append(FLD_UNITCODE);
			query.append(" = '");
			query.append(unitCode);
			query.append("'");
			
			stmt = conn.createStatement();
			stmt.executeUpdate(query.toString());
		} 
		catch (SQLException e)
		{
      		System.out.println("[StudyUnit.deleteStudyUnit()]: " + e.getMessage());
		}
	}	
	
	public static StudyUnit getStudyUnit(Connection conn, String unitCode)
	{	
		StringBuffer query = null; 
		Statement stmt = null;
		StudyUnit studyUnit = null;
		
		try
		{
			query = new StringBuffer();
	        
			query.append("SELECT * FROM ");
			query.append(TBL_STUDYUNITS);
			query.append(" WHERE ");
			query.append(FLD_UNITCODE);
			query.append(" = '");
			query.append(unitCode);
			query.append("'");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			while (rs.next())
			{
				String unitcode = rs.getString(FLD_UNITCODE);
				String title = rs.getString(FLD_TITLE);
				String year = rs.getString(FLD_YEAR);
				short semester = rs.getShort(FLD_SEMESTER);
				float examLength = rs.getFloat(FLD_EXAMLENGTH);
				short noOfStudents = rs.getShort(FLD_NOOFSTUDENTS);
				String department = rs.getString(FLD_DEPARTMENT);
				short credits = rs.getShort(FLD_CREDITS);
				boolean evening = rs.getBoolean(FLD_EVENING);
				String venue = rs.getString(FLD_VENUE);
			
				studyUnit = new StudyUnit(unitcode, title, year, semester, examLength, noOfStudents, department, credits, evening, venue);
			}
		} 
		catch (SQLException e)
		{
      		System.out.println("[StudyUnit.getStudyUnit(String unitCode)]: " + e.getMessage());
		}
		
		return studyUnit;
	}
	
	public static ArrayList<String> getStudyUnitSuggestions(Connection conn, String pattern)
	{	
		StringBuffer query = null; 
		Statement stmt = null;
		ArrayList<String> studyUnitCodes = new ArrayList<String>();
		
		try
		{
			query = new StringBuffer();
	        
			query.append("SELECT ");
			query.append(FLD_UNITCODE);
			query.append(" FROM ");
			query.append(TBL_STUDYUNITS);
			query.append(" WHERE ");
			query.append(FLD_UNITCODE);
			query.append(" LIKE '");
			query.append(pattern);
			query.append("%'");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			while (rs.next())
			{
				studyUnitCodes.add(rs.getString(FLD_UNITCODE));
			}
		} 
		catch (SQLException e)
		{
      		System.out.println("[StudyUnit.getStudyUnitSuggestions(String pattern)]: " + e.getMessage());
		}
		
		return studyUnitCodes;
	}
	
	public static int getStudyUnitID(Connection conn, String unitCode, int semester)
	{	
		StringBuffer query = null; 
		Statement stmt = null;
		int id = -1;
		
		try
		{
			query = new StringBuffer();
	        
			query.append("SELECT ");
			query.append(StudyUnit.FLD_ID);
			query.append("\nFROM ");
			query.append(StudyUnit.TBL_STUDYUNITS);
			query.append("\nWHERE ");
			query.append(StudyUnit.FLD_UNITCODE);
			query.append(" = '");
			query.append(unitCode);
			query.append("' AND ");
			query.append(StudyUnit.FLD_SEMESTER);
			query.append(" = '");
			query.append(semester);
			query.append("' AND ");
			query.append(StudyUnit.FLD_EVENING);
			query.append(" = '");
			query.append(false);
			query.append("'");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			while (rs.next())
			{
				id = rs.getInt(StudyUnit.FLD_ID);
			}
		} 
		catch (SQLException e)
		{
      		System.out.println("[StudyUnit.getStudyUnitID(String unitCode, semester)]: " + e.getMessage());
		}
		
		return id;
	}

}