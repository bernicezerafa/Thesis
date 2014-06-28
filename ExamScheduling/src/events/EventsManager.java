package events;

import helpers.SQLHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.dhtmlx.planner.DHXEv;
import com.dhtmlx.planner.DHXEvent;
import com.dhtmlx.planner.DHXEventsManager;
import com.dhtmlx.planner.DHXStatus;

import entities.StudentExams;
import entities.StudyUnit;
import entities.TimetableEvent;

public class EventsManager extends DHXEventsManager {

   	public EventsManager(HttpServletRequest request) {   		
   		super(request);
   	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Iterable getEvents() {
				
		Connection conn = null;
		DHXEventsManager.date_format = "yyyy-MM-dd HH:mm:ss";
   		List<DHXEvent> events = new ArrayList<DHXEvent>();
        
   		try {
   			
   			conn = SQLHelper.getConnection();
   	    	Statement statement = conn.createStatement();
   	    	
   	    	StringBuffer query = new StringBuffer();
   	    	String year = getRequest().getParameter("year");
   	    	String studentID = getRequest().getParameter("studentID");
   	    	
   	    	if (year == null && studentID == null)
   	    	{
   	    		//SELECT * FROM dbo.TIMETABLE_EVENTS
   	    		
   	    		query.append("SELECT * FROM ");
   	    		query.append(TimetableEvent.TBL_EVENTS);
   	    	}
   	    	else if (year != null)
   	    	{   	    		
   	    		query.append("SELECT t.* \nFROM ");
   				query.append(StudyUnit.TBL_STUDYUNITS);
   				query.append(" s JOIN ");
   				query.append(TimetableEvent.TBL_EVENTS);
   				query.append(" t ON s.");
   				query.append(StudyUnit.FLD_ID);
   				query.append(" = t.");
   				query.append(TimetableEvent.FLD_EXAMID);
   				query.append("\nWHERE s.");
   				
   				if (year.equalsIgnoreCase("evening"))
   				{
   	   	    		//SELECT t.*
   	   	    		//FROM dbo.STUDYUNITS s JOIN dbo.TIMETABLE_EVENTS t ON s.ID = t.ExamID
   	   	    		//WHERE s.Evening = 'true';
   					
   					query.append(StudyUnit.FLD_EVENING);
   					query.append(" = '");
   					query.append(true);
   					query.append("'");
   				}
   				else if (year.equalsIgnoreCase("fulltime"))
   				{
   	   	    		//SELECT t.*
   	   	    		//FROM dbo.STUDYUNITS s JOIN dbo.TIMETABLE_EVENTS t ON s.ID = t.ExamID
   	   	    		//WHERE s.Evening = 'false';
   					
   					query.append(StudyUnit.FLD_EVENING);
   					query.append(" = '");
   					query.append(false);
   					query.append("'");   					
   				}
   				else
   				{
   	   	    		//SELECT t.*
   	   	    		//FROM dbo.STUDYUNITS s JOIN dbo.TIMETABLE_EVENTS t ON s.ID = t.ExamID
   	   	    		//WHERE s.Year LIKE '%1%';
   					
   					query.append(StudyUnit.FLD_YEAR);
   					query.append(" LIKE '%");
   					query.append(year);
   					query.append("%'");
   				}
   	    	}
   	    	else if (studentID != null)
   	    	{
   	    		//SELECT t.*
   	   	    	//FROM dbo.TIMETABLE_EVENTS t JOIN dbo.STUDENT_EXAMS s 
   	   	    	//ON t.ExamID = s.ExamID
   	   	    	//WHERE s.StudentID = '306993M'
   	    		
   	    		query.append("SELECT t.* \nFROM ");
   	    		query.append(TimetableEvent.TBL_EVENTS);
   	    		query.append(" t JOIN ");
   	    		query.append(StudentExams.TBL_STUDENTEXAMS);
   	    		query.append(" s ON t.");
   	    		query.append(TimetableEvent.FLD_EXAMID);
   	    		query.append(" = s.");
   	    		query.append(StudentExams.FLD_EXAMID);
   	    		query.append("\nWHERE s.");
   	    		query.append(StudentExams.FLD_STUDENTID);
   	    		query.append(" LIKE '");
   	    		query.append(studentID);
   	    		query.append("%'");
   	    	}
   	    	
   	    	ResultSet rs = statement.executeQuery(query.toString());

   	    	while (rs.next()) {

   	    		DHXEvent event = new DHXEvent();
   	    		event.setId(Integer.parseInt(rs.getString(TimetableEvent.FLD_ID)));
   	    		event.setStart_date(rs.getString(TimetableEvent.FLD_STARTDATE));
	   	        event.setEnd_date(rs.getString(TimetableEvent.FLD_ENDDATE));
	   	        event.setText(rs.getString(TimetableEvent.FLD_UNITCODE));
	   	        
	   	        events.add(event);
   	    	}
   	    	
   	    } catch (SQLException e) {
      		System.out.println("[EventsManager.getEvents()]: " + e.getMessage());
        
   	    } finally {
   	    	SQLHelper.closeConnection(conn);
        }
        
   		DHXEventsManager.date_format = "MM/dd/yyyy HH:mm";
        return events;
   	}
	
   	@Override
   	public DHXStatus saveEvent(DHXEv event, DHXStatus status) {
   		Connection conn = SQLHelper.getConnection();
   		PreparedStatement pstmt = null;
   		ResultSet rs = null;
           		
   		try {
   			
   			if (status == DHXStatus.UPDATE) {
   				pstmt = TimetableEvent.updateEvent(conn, event);
   				
            } else if (status == DHXStatus.INSERT) {
            	pstmt = TimetableEvent.insertEvent(conn, event, -1);
            	
            } else if (status == DHXStatus.DELETE) {
            	pstmt = TimetableEvent.deleteEvent(conn, event);
            }

            if (pstmt != null) 
            {
            	pstmt.executeUpdate();
            	rs = pstmt.getGeneratedKeys();
                
	            if (rs.next()) 
	            {    
	            	int eventId = rs.getInt(1);
	                
	            	if (status == DHXStatus.INSERT)
	            		event.setId(eventId);
	            }
            }
            
      	} catch (SQLException e) {
      		System.out.println("[EventsManager.saveEvent() - " + status.name() + "]: " + e.getMessage());
      		e.printStackTrace();
      	
      	} finally {
      		
      		if (rs != null) SQLHelper.closeResultSet(rs);
      		if (pstmt != null) SQLHelper.closePreparedStatement(pstmt);
      		if (conn != null) SQLHelper.closeConnection(conn);
      	}

        return status;
   	}

   	@Override
   	public DHXEv createEvent(String id, DHXStatus status) {
   		return new DHXEvent();
   	}
}