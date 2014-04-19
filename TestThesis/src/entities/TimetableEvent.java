package entities;

import helpers.SQLHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.dhtmlx.planner.DHXEv;

public class TimetableEvent {

	private int id;
	private Timestamp endDate;
	private Timestamp startDate;
	
	public static final String TBL_EVENTS = "dbo.Timetable_Events";
	public static final String FLD_ID = "ID";
	public static final String FLD_UNITCODE = "UnitCode";
	public static final String FLD_STARTDATE = "StartDate";
	public static final String FLD_ENDDATE = "EndDate";
	public static final String FLD_EXAMID = "ExamID";
	
	public TimetableEvent() {
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Timestamp getEndDate() {
		return endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	public Timestamp getStartDate() {
		return startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	// insert timetable event when new exam slot is added to the timetable
	public static PreparedStatement insertEvent(Connection conn, DHXEv event, int examID) throws SQLException
	{ 		
		StringBuffer query = null; 
		PreparedStatement pstmt = null;
		
		String start_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(event.getStart_date());
		String end_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(event.getEnd_date());
		
		// insert extra event data into study units table		
		query = new StringBuffer();
	    
		query.append("INSERT INTO ");
		query.append(TBL_EVENTS);
		query.append(" ( ");
		query.append(FLD_UNITCODE);
		query.append(", ");
		query.append(FLD_STARTDATE);
		query.append(", ");
		query.append(FLD_ENDDATE);
		query.append(", ");
		query.append(FLD_EXAMID);
		query.append(") VALUES (?,?,?,?)");
			
		pstmt = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, event.getText());
		pstmt.setString(2, start_date);
		pstmt.setString(3, end_date);
		pstmt.setInt(4, examID);
				
		return pstmt;
	}
	
	// update timetable event, when event is moved to another date or text is modified
	public static PreparedStatement updateEvent(Connection conn, DHXEv event) throws SQLException
	{ 
		 StringBuffer query = null; 
		 PreparedStatement pstmt = null;
		 
 		 String start_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(event.getStart_date());
   		 String end_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(event.getEnd_date());
   		 
   		 // update extra event data for study units table
		 query = new StringBuffer();
         
		 query.append("UPDATE ");
		 query.append(TBL_EVENTS);
		 query.append(" SET ");
		 query.append(FLD_UNITCODE);
		 query.append(" =?, ");
		 query.append(FLD_STARTDATE);
		 query.append(" =?, ");
		 query.append(FLD_ENDDATE);
		 query.append(" =? ");
		 query.append(" WHERE ");
		 query.append(FLD_ID);
		 query.append(" =? ");
		 
		 pstmt = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
		 pstmt.setString(1, event.getText());
		 pstmt.setString(2, start_date);
		 pstmt.setString(3, end_date);
		 pstmt.setInt(4, event.getId());
	
		 return pstmt;
	}
	
	// delete event from timetable
	public static PreparedStatement deleteEvent(Connection conn, DHXEv event) throws SQLException
	{ 
		 StringBuffer query = null; 
		 PreparedStatement pstmt = null;
		 
		 query = new StringBuffer();
         
		 query.append("DELETE FROM ");
		 query.append(TBL_EVENTS);
		 query.append(" WHERE ");
		 query.append(FLD_ID);
		 query.append(" =? ");
		 
		 pstmt = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
		 pstmt.setInt(1, event.getId());
		 
		 return pstmt;
	}
	
	public static String getEventId(Connection conn, String unitCode) {
		
		StringBuffer query = null;
		Statement stmt = null;
		String eventId = "";
		
		try {
   			
   	    	query = new StringBuffer();
   	    	
   	    	query.append("SELECT * \nFROM ");
   			query.append(TBL_EVENTS);
   			query.append("\nWHERE ");			
   			query.append(FLD_UNITCODE);
   			query.append(" = '");
   			query.append(unitCode);
   			query.append("'");
   			
   			stmt = conn.createStatement();
   	    	ResultSet rs = stmt.executeQuery(query.toString());

   	    	while (rs.next()) {
   	    		eventId = Integer.toString(rs.getInt(FLD_ID));
   	    	}
   	    	
   	    } catch (SQLException e) {
      		System.out.println("[TimetableEvent.getEventId(String unitCode)]: " + e.getMessage());
        
   	    } finally {
   	    	SQLHelper.closeConnection(conn);
        }
		
		return eventId;
	}
	
	public static int getStudyUnitID(Connection conn, String eventID) {

		StringBuffer query = null;
		Statement stmt = null;
		int examID = -1;
		
		try {
   			
   	    	query = new StringBuffer();
   	    	
   	    	query.append("SELECT ");
   	    	query.append(FLD_EXAMID);
   	    	query.append("\nFROM ");
   			query.append(TBL_EVENTS);
   			query.append("\nWHERE ");			
   			query.append(FLD_ID);
   			query.append(" = '");
   			query.append(eventID);
   			query.append("'");
   			
   			stmt = conn.createStatement();
   	    	ResultSet rs = stmt.executeQuery(query.toString());

   	    	while (rs.next()) {
   	    		examID = rs.getInt(FLD_EXAMID);
   	    	}
   	    	
   	    } catch (SQLException e) {
      		System.out.println("[TimetableEvent.getStudyUnitID(String eventID)]: " + e.getMessage());
   	    }
		
		return examID;
	}
	
	public static int getEventID(Connection conn, String examID) {

		StringBuffer query = null;
		Statement stmt = null;
		int eventID = -1;
		
		try {
   			
   	    	query = new StringBuffer();
   	    	
   	    	query.append("SELECT ");
   	    	query.append(FLD_ID);
   	    	query.append("\nFROM ");
   			query.append(TBL_EVENTS);
   			query.append("\nWHERE ");			
   			query.append(FLD_EXAMID);
   			query.append(" = '");
   			query.append(examID);
   			query.append("'");
   			
   			stmt = conn.createStatement();
   	    	ResultSet rs = stmt.executeQuery(query.toString());

   	    	while (rs.next()) {
   	    		eventID = rs.getInt(FLD_ID);
   	    	}
   	    	
   	    } catch (SQLException e) {
      		System.out.println("[TimetableEvent.getEventID(String examID)]: " + e.getMessage());
        
   	    }
		
		return eventID;
	}
	
	public static String getEventStart(Connection conn, String eventID)
	{
		StringBuffer query = null;
		Statement stmt = null;
		String startDate = null;
		
		try {
   			
   	    	query = new StringBuffer();
   	    	
   	    	query.append("SELECT ");
   	    	query.append(FLD_STARTDATE);
   	    	query.append("\nFROM ");
   			query.append(TBL_EVENTS);
   			query.append("\nWHERE ");			
   			query.append(FLD_ID);
   			query.append(" = '");
   			query.append(eventID);
   			query.append("'");
   			
   			stmt = conn.createStatement();
   	    	ResultSet rs = stmt.executeQuery(query.toString());

   	    	while (rs.next()) {
   	    		startDate = rs.getString(FLD_STARTDATE);
   	    	}
   	    	
   	    } catch (SQLException e) {
      		System.out.println("[TimetableEvent.getEventStart(String eventID)]: " + e.getMessage());
        
   	    }
		
		return startDate;
	}
}