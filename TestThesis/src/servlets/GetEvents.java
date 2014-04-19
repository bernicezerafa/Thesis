package servlets;

import helpers.SQLHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import thesis.timetable_generation.GeneticAlgorithm;
import thesis.timetable_generation.ReadData;

import com.google.common.collect.Table;

import entities.TimetableEvent;

public class GetEvents extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetEvents() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    // getEventIds - true when return ArrayList need to be filled with event id's since modifications are being
    // 				 done on client side
    //			   - false when ArrayList need to be filled with exam id's to examine timetable
    public static ArrayList<Integer> getClashingExams(Connection conn, String eventID, boolean getEventIds) {
    	
    	int studyUnitID = TimetableEvent.getStudyUnitID(conn, eventID);
    	TreeMap<Integer, Integer> indexExamId = ReadData.getIndexExamId();
		int examIndex = indexExamId.get(studyUnitID);
					
		Table<Integer, Integer, ArrayList<String>> clashMatrix = GeneticAlgorithm.getClashesMatrix();
		ArrayList<Integer> clashingExams = new ArrayList<Integer>();
		
		for (int i=0; i < clashMatrix.size(); i++)
		{
			// if there is a clash between exam clicked and exam i
			if (clashMatrix.get(examIndex, i) != null)
			{
				// get exam ID by index of that exam
				int clashedExamID = ReadData.getKeyByValue(indexExamId, i);
				
				if (getEventIds)
				{
					int eventClashID = TimetableEvent.getEventID(conn, Integer.toString(clashedExamID));
					clashingExams.add(eventClashID);
				}
				else
				{
					clashingExams.add(clashedExamID);
				}
			}
		}
    	
		return clashingExams;
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Connection conn = null;
		
		try
		{
			conn = SQLHelper.getConnection();
			String unitCode = request.getParameter("unitCode");
			
			if (unitCode != null)
			{
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				
				String eventId = TimetableEvent.getEventId(conn, unitCode);
				
				try {
					out.println(eventId);
				} finally {
					out.close();
				}
			}
			else
			{
				String eventID = request.getParameter("eventID");
				
				boolean getEventIds = true;
				ArrayList<Integer> clashingExams = getClashingExams(conn, eventID, getEventIds);
				JSONObject json = new JSONObject();
				
				for (int i=0; i< clashingExams.size(); i++)
				{
					json.put(Integer.toString(i), clashingExams.get(i));
				}
				
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(json.toJSONString());
			}
		}
		finally
		{
			SQLHelper.closeConnection(conn);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
