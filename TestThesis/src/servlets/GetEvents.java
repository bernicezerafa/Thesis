package servlets;

import helpers.SQLHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import thesis.timetable_generation.GeneticAlgorithm;
import thesis.timetable_generation.ReadData;
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
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
				ReadData rd = new ReadData(1);
				rd.mapExamIndexes(conn);
				
				int studyUnitID = TimetableEvent.getStudyUnitID(conn, eventID);
				int examIndex = ReadData.getIndexExamID().get(studyUnitID);
				
				FileInputStream fos = new FileInputStream("C://Users//Bernice//Desktop//clashes_matrix.data");
				ObjectInputStream ois = new ObjectInputStream(fos);
				
				int[][] clashMatrix = null;
				
				try
				{
					clashMatrix = (int[][]) ois.readObject();
				}
				catch (ClassNotFoundException e)
				{
					System.out.println("Unable to read clashes matrix from file!\n" + e.getMessage());
				}
				finally
				{
					try { ois.close(); } catch (IOException ignore) { }
				}
		
				ArrayList<Integer> clashingExams = new ArrayList<Integer>();
				
				JSONObject json = new JSONObject();
				
				for (int i=0; i < clashMatrix.length; i++)
				{
					// if there is a clash between exam clicked and exam i
					if (clashMatrix[examIndex][i] > 0)
					{
						// get exam ID by index of that exam
						String clashedExamID = Integer.toString(ReadData.getKeyByValue(ReadData.getIndexExamID(), i));
						int eventClashID = TimetableEvent.getEventID(conn, clashedExamID);
						
						clashingExams.add(eventClashID);
					}
				}
				
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
