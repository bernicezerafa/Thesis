package entities;

import helpers.DateHelper;
import helpers.FileHelper;
import helpers.SQLHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;

import thesis.timetable_generation.BestChromosome;
import thesis.timetable_generation.GeneticAlgorithm;
import thesis.timetable_generation.InputParameters;
import thesis.timetable_generation.Timeslot;

import com.dhtmlx.planner.DHXEv;

public class TimetableEvent {

	private int id;
	private String endDate;
	private String startDate;
	private int examId;
	
	public static final String TBL_EVENTS = "dbo.Timetable_Events";
	public static final String FLD_ID = "ID";
	public static final String FLD_UNITCODE = "UnitCode";
	public static final String FLD_STARTDATE = "StartDate";
	public static final String FLD_ENDDATE = "EndDate";
	public static final String FLD_EXAMID = "ExamID";
	
	public TimetableEvent(int id, String startDate, String endDate) {
		this.id = id;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	public int getExamId() {
		return examId;
	}

	public void setExamId(int examId) {
		this.examId = examId;
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
		
		if (examID != -1)
		{
			query.append(", ");
			query.append(FLD_EXAMID);
		}
		query.append(") VALUES (?,?,?");
		
		if (examID != -1)
			query.append(",?");
		
		query.append(")");
			
		pstmt = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, event.getText());
		pstmt.setString(2, start_date);
		pstmt.setString(3, end_date);
		
		if (examID != -1)
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
   		 
   		 SimpleDateFormat timeslotFormat = new SimpleDateFormat("dd/MM/yyy HH:mm");
   		 
   		 String timeslotStart = timeslotFormat.format(event.getStart_date());
   		 String timeslotEnd = timeslotFormat.format(event.getEnd_date());
  		 
   		 Timeslot timeslot = new Timeslot(timeslotStart, timeslotEnd);
   		 
   		 int examID = getStudyUnitID(conn, event.getId());
   		 moveEvent(examID, timeslot);
   		 
   		 // update extra event data for study units table
   		 query = new StringBuffer();
         
   		 query.append("UPDATE ");
   	 	 query.append(TBL_EVENTS);
   		 query.append(" SET ");
   		 query.append(FLD_UNITCODE);
   		 query.append(" = ?, ");
   		 query.append(FLD_STARTDATE);
   		 query.append(" = ?, ");
   		 query.append(FLD_ENDDATE);
   		 query.append(" = ? ");
   		 query.append(" WHERE ");
   		 query.append(FLD_ID);
		 query.append(" = ? ");
		 
		 pstmt = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
		 pstmt.setString(1, event.getText());
		 pstmt.setString(2, start_date);
		 pstmt.setString(3, end_date);
		 pstmt.setInt(4, event.getId());
   		 
		 return pstmt;
	}
	
	public void updateEventDates(Connection conn)
	{
		 StringBuffer query = null;
		 PreparedStatement pstmt = null;
		 
		 try
		 {
			 query = new StringBuffer();
	         
			 query.append("UPDATE ");
			 query.append(TBL_EVENTS);
			 query.append(" SET ");
	   		 query.append(FLD_STARTDATE);
	   		 query.append(" =?, ");
	   		 query.append(FLD_ENDDATE);
	   		 query.append(" =?, ");
	   		 query.append(FLD_EXAMID);
	   		 query.append(" =? ");
	   		 query.append(" WHERE ");
			 query.append(FLD_ID);
			 query.append(" =? ");
			 
			 pstmt = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
			 
			 pstmt.setString(1, startDate);
			 pstmt.setString(2, endDate);
			 pstmt.setInt(3, examId);
			 pstmt.setInt(4, id);
			 pstmt.executeUpdate();
		 }
		 catch (SQLException e)
		 {
			 System.out.println("[TimetableEvent.updateEventDates()]: " + e.getMessage());
		 }
	}
	
	//SELECT t.* 
	//FROM dbo.TIMETABLE_EVENTS t JOIN dbo.STUDYUNITS s 
	//ON t.EXAMID = s.ID 
	//WHERE t.UNITCODE = 'CIS1021' AND s.EVENING = 'true';
	
	public static String getEventId(Connection conn, String unitCode, boolean evening) {
		
		StringBuffer query = null;
		Statement stmt = null;
		String eventId = "";
		
		try {
   			
   	    	query = new StringBuffer();
   	    	
   	    	query.append("SELECT t.*");
   	    	query.append("\nFROM ");
   			query.append(TBL_EVENTS);
   			query.append(" t JOIN ");
   			query.append(StudyUnit.TBL_STUDYUNITS);
   			query.append(" s ON t.");
   			query.append(FLD_EXAMID);
   			query.append(" = s.");
   			query.append(StudyUnit.FLD_ID);
   			query.append("\nWHERE t.");			
   			query.append(FLD_UNITCODE);
   			query.append(" = '");
   			query.append(unitCode);
   			query.append("' AND s.");
   			query.append(StudyUnit.FLD_EVENING);
   			query.append(" = '");
   			query.append(evening);
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
	
	// SELECT ExamID
	// FROM dbo.TimetableEvent
	// WHERE ID = '243'
	
	public static int getStudyUnitID(Connection conn, int eventID) {

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
	
	// SELECT ID
	// FROM dbo.TimetableEvent
	// WHERE ExamID = '123'
	
	public static int getEventID(Connection conn, int examID) {

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
	
	public static Timeslot getEventTimeslot(Connection conn, int eventID)
	{
		StringBuffer query = null;
		Statement stmt = null;
		Timeslot timeslot = null;
		
		try {
   			
   	    	query = new StringBuffer();
   	    	
   	    	query.append("SELECT ");
   	    	query.append(FLD_STARTDATE);
   	    	query.append(", ");
   	    	query.append(FLD_ENDDATE);
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
   	    		Timestamp startDate = rs.getTimestamp(FLD_STARTDATE);
   	    		Timestamp endDate = rs.getTimestamp(FLD_ENDDATE);
   	    		   	    		
   	    		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
   	    		timeslot = new Timeslot(sdf.format(startDate), sdf.format(endDate));
   	    	}
   	    	
   	    } catch (SQLException e) {
      		System.out.println("[TimetableEvent.getEventStart(String eventID)]: " + e.getMessage());
        
   	    }
		
		return timeslot;
	}
	
	// DELETE FROM dbo.Timetable_Event
	// WHERE ID = '23'
	
	public static PreparedStatement deleteEvent(Connection conn, DHXEv event) throws SQLException
	{ 
		 StringBuffer query = null; 
		 PreparedStatement pstmt = null;
		 
		 int examID = getStudyUnitID(conn, event.getId());
		 deleteExam(conn, examID);
		 
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
	
	// add new exam in timetable chromosome
	public static void addExam(Connection conn, int examID, Timeslot timeslotExam)
	{
		HashMap<Integer, Integer> indexExamID = FileHelper.getIndexExamId();
		HashMap<Integer, Timeslot> timeslotMap = FileHelper.getTimeslotMap();
		BestChromosome chrom = FileHelper.getBestChromosome();
		
		Integer[] timetableState = chrom.getChromosome();
		int examIndex = timetableState.length;
		timetableState = Arrays.copyOf(timetableState, timetableState.length + 1);
		
		InputParameters param = FileHelper.getInputParameters();

		if (StudyUnit.isEvening(conn, examID))
		{
			param.setNoOfEveningExams(param.getNoOfEveningExams() + 1);
			HashMap<Integer, Integer> eveningIndexExamId = FileHelper.getEveningIndexExamId();
			eveningIndexExamId.put(examID, examIndex);
			FileHelper.saveEveningIndexExamId(eveningIndexExamId);
		}
		else
		{
			param.setNoOfDayExams(param.getNoOfDayExams() + 1);
		}
		
		FileHelper.saveInputParameters(param);
		
		indexExamID.put(examID, examIndex);
		FileHelper.saveIndexExamId(indexExamID);
		
		Integer timeslotNo = DateHelper.getTimeslotNoByTimeslot(timeslotMap, timeslotExam);
		
		if (timeslotNo != null)
		{
			timetableState[examIndex] = timeslotNo;
		}
		// else add an entry in timeslotmap for this new timeslot and
		// re structure eval matrix for temp differences
		else
		{
			int newTimeslotNo = timeslotMap.size();
			timeslotMap.put(newTimeslotNo, timeslotExam);
			FileHelper.saveTimeslotMap(timeslotMap);
			
			GeneticAlgorithm ga = new GeneticAlgorithm();
			ga.generateEvalMatrix(timeslotMap, false);
			
			timetableState[examIndex] = newTimeslotNo; 
		}
		
		chrom.setChromosome(timetableState);
		FileHelper.saveBestChromosome(chrom);
	}
		
	// remove exam from timetable
	public static void deleteExam(Connection conn, int examID)
	{
		InputParameters param = FileHelper.getInputParameters();
		
		if (StudyUnit.isEvening(conn, examID))
		{
			param.setNoOfEveningExams(param.getNoOfEveningExams() - 1);
			HashMap<Integer, Integer> eveningIndexExamId = FileHelper.getEveningIndexExamId();
			eveningIndexExamId.remove(examID);
			FileHelper.saveEveningIndexExamId(eveningIndexExamId);
		}
		else
		{
			param.setNoOfDayExams(param.getNoOfDayExams() - 1);
		}
		
		FileHelper.saveInputParameters(param);
		
		GeneticAlgorithm ga = new GeneticAlgorithm();
		
		HashMap<Integer, Integer> indexExamId = ga.getIndexExamID();
		int examIndex = indexExamId.get(examID);
		BestChromosome chromosome = FileHelper.getBestChromosome();
		
		// set timetable entry for that index as -1
		Integer[] timetableState = chromosome.getChromosome();
		timetableState[examIndex] = -1;
		chromosome.setChromosome(timetableState);
		
		indexExamId.remove(examID);
		FileHelper.saveIndexExamId(indexExamId);
		
		BestChromosome chromosomeAfter = ga.evaluateChromosome(chromosome);
		FileHelper.saveBestChromosome(chromosomeAfter);
	}
	
	// move or change event duration
	public static void moveEvent(int examID, Timeslot timeslot)
	{
		GeneticAlgorithm ga = new GeneticAlgorithm();
		
		int examIndex = ga.getIndexExamID().get(examID);
		BestChromosome chromosome = FileHelper.getBestChromosome();
		FileHelper.saveChromosomeBefore(chromosome);
		
		Integer[] timetableState = chromosome.getChromosome();
		
		HashMap<Integer, Timeslot> timeslotMap = ga.getTimeslotMap();
		Integer timeslotNo = DateHelper.getTimeslotNoByTimeslot(timeslotMap, timeslot);
		
		// if the exam is being moved to a timeslot in timeslot settings,
		// set timtable at index to that timeslot number
		if (timeslotNo != null)
		{
			// if event was not moved
			if (timetableState[examIndex] == timeslotNo)
				return;
			else
				timetableState[examIndex] = timeslotNo;
		}
		// else add an entry in timeslotmap for this new timeslot and
		// re structure eval matrix for temp differences
		else
		{
			int newTimeslotNo = timeslotMap.size();
			timeslotMap.put(newTimeslotNo, timeslot);
			FileHelper.saveTimeslotMap(timeslotMap);
			ga.generateEvalMatrix(timeslotMap, false);
			
			timetableState[examIndex] = newTimeslotNo; 
		}
		
		chromosome.setChromosome(timetableState);
		
		BestChromosome chromosomeAfter = ga.evaluateChromosome(chromosome);
		FileHelper.saveBestChromosome(chromosomeAfter);
	}
}