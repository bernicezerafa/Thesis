package entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Exam {
	
	private int examID;
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
	
	public static final String TBL_EXAMS = "dbo.Exams";
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
	
	public Exam() {
		
	}
	
	public Exam(String unitCode, String title, String year, short semester, float examLength, 
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
	
	public Exam(int examID, String unitCode, String title, String year, short semester, float examLength, 
					 short noOfStudents, String department, short credits, boolean evening, String venue) {
		
		this.examID = examID;
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
	
	public int getExamID() {
		return examID;
	}
	
	public void setExamID(int examID) {
		this.examID = examID;
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
		ResultSet generatedKeys = null;
		
		try
		{
			query = new StringBuffer();
		     
			query.append("INSERT INTO ");
			query.append(TBL_EXAMS);
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
			
			generatedKeys = pstmt.getGeneratedKeys();
	        if (generatedKeys.next()) {
	            examID = generatedKeys.getInt(1);
	        }
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
			query.append(TBL_EXAMS);
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
	
	// SELECT UnitCode, Evening
	// FROM dbo.Exams
	// WHERE UnitCode LIKE '%pattern%'
	// AND Semester = 'semester'
	
	public static ArrayList<String> getStudyUnitSuggestions(Connection conn, String pattern, int semester)
	{	
		StringBuffer query = null; 
		Statement stmt = null;
		ArrayList<String> studyUnitCodes = new ArrayList<String>();
		
		try
		{
			query = new StringBuffer();
	        
			query.append("SELECT ");
			query.append(FLD_UNITCODE);
			query.append(", ");
			query.append(FLD_EVENING);
			query.append("\nFROM ");
			query.append(TBL_EXAMS);
			
			if (pattern != null)
			{
				query.append("\nWHERE ");
				query.append(FLD_UNITCODE);
				query.append(" LIKE '%");
				query.append(pattern);
				query.append("%'");
			}

			if (semester != -1)
			{
				query.append(" AND ");
				query.append(FLD_SEMESTER);
				query.append(" = '");
				query.append(semester);
				query.append("'");
			}
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			while (rs.next())
			{
				String studyUnitCode = rs.getString(FLD_UNITCODE);
				boolean evening = rs.getBoolean(FLD_EVENING);
				
				if (evening)
					studyUnitCodes.add(studyUnitCode + " - evening");
				else
					studyUnitCodes.add(studyUnitCode);
				
			}
		} 
		catch (SQLException e)
		{
      		System.out.println("[Exam.getStudyUnitSuggestions(String pattern)]: " + e.getMessage());
		}
		
		return studyUnitCodes;
	}
	
	// SELECT ID
	// FROM dbo.Exams
	// WHERE UnitCode = 'CIS3087' AND Evening = 'true'
	
	public static int getStudyUnitID(Connection conn, String unitCode, boolean evening)
	{	
		StringBuffer query = null; 
		Statement stmt = null;
		int id = -1;
		
		try
		{
			query = new StringBuffer();
	        
			query.append("SELECT ");
			query.append(Exam.FLD_ID);
			query.append("\nFROM ");
			query.append(Exam.TBL_EXAMS);
			query.append("\nWHERE ");
			query.append(Exam.FLD_UNITCODE);
			query.append(" = '");
			query.append(unitCode);
			query.append("' AND ");
			query.append(Exam.FLD_EVENING);
			query.append(" = '");
			query.append(evening);
			query.append("'");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			while (rs.next())
			{
				id = rs.getInt(Exam.FLD_ID);
			}
		} 
		catch (SQLException e)
		{
      		System.out.println("[Exam.getStudyUnitID(String unitCode, semester)]: " + e.getMessage());
		}
		
		return id;
	}
	
	public static String getStudyUnitCode(Connection conn, int id)
	{	
		StringBuffer query = null; 
		Statement stmt = null;
		String unitCode = null;
		
		try
		{
			query = new StringBuffer();
	        
			query.append("SELECT ");
			query.append(Exam.FLD_UNITCODE);
			query.append("\nFROM ");
			query.append(Exam.TBL_EXAMS);
			query.append("\nWHERE ");
			query.append(Exam.FLD_ID);
			query.append(" = '");
			query.append(id);
			query.append("'");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			while (rs.next())
			{
				unitCode = rs.getString(Exam.FLD_UNITCODE);
			}
		} 
		catch (SQLException e)
		{
      		System.out.println("[Exam.getStudyUnitID(String unitCode, semester)]: " + e.getMessage());
		}
		
		return unitCode;
	}

	
	// SELECT COUNT(*) AS 'noOfEvening'
	// FROM dbo.Exams
	// WHERE Evening = 'true'
	
	public static int getNoOfEveningExams(Connection conn)
	{
		StringBuffer query = null; 
		Statement stmt = null;
		int noOfEveningExams = -1;
		
		try
		{
			query = new StringBuffer();
	        
			query.append("SELECT COUNT(*) AS 'noOfEvening' \nFROM ");
			query.append(TBL_EXAMS);
			query.append("\nWHERE ");
			query.append(FLD_EVENING);
			query.append(" = '");
			query.append(true);
			query.append("'");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			while (rs.next())
			{
				noOfEveningExams = rs.getInt("noOfEvening");
			}
		} 
		catch (SQLException e)
		{
      		System.out.println("[Exam.getNoOfEveningExams()]: " + e.getMessage());
		}
		
		return noOfEveningExams;
	}
	
	// SELECT Evening
	// FROM dbo.Exams
	// WHERE ID = '12'
	
	public static boolean isEvening(Connection conn, int examID)
	{
		StringBuffer query = null; 
		Statement stmt = null;
		boolean evening = false;
		
		try
		{
			query = new StringBuffer();
	        
			query.append("SELECT ");
			query.append(FLD_EVENING);
			query.append("\nFROM ");
			query.append(TBL_EXAMS);
			query.append("\nWHERE ");
			query.append(FLD_ID);
			query.append(" = '");
			query.append(examID);
			query.append("'");
			
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
			
			while (rs.next())
			{
				evening = rs.getBoolean(FLD_EVENING);
			}
		} 
		catch (SQLException e)
		{
      		System.out.println("[Exam.getNoOfEveningExams()]: " + e.getMessage());
		}
		
		return evening;
	}
}