package servlets;

import helpers.SQLHelper;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import entities.StudyUnit;

public class UpdateStudyUnit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateStudyUnit() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String getDetails = request.getParameter("get_details");
		String studyUnitSuggest = request.getParameter("studyunit_auto");
		
		String examCode = null;
		StudyUnit exam = null;
		String query = null;
		
		if (getDetails == null && studyUnitSuggest == null)
		{
			examCode = request.getParameter("exam-code");
			String examTitle = request.getParameter("exam-title");
			String department = request.getParameter("department");
			
			String examLengthStr = request.getParameter("exam-length");		
			float examLength = 0.0f;
			
			if (examLengthStr != "") {
				examLength = Float.parseFloat(examLengthStr);
			}
	
			String noOfStudentsStr = request.getParameter("no-of-students");
			short noOfStudents = 0;
		
			if (noOfStudentsStr != "") {
				noOfStudents = Short.parseShort(noOfStudentsStr);
			}
			
			String year = request.getParameter("exam-year");
			
			String semesterStr = request.getParameter("exam-semester");
			short semester = 0;
			
			if (semesterStr != "") {
				semester = Short.parseShort(semesterStr);
			}
			
			String creditsStr = request.getParameter("exam-credits");
			short credits = 0;
			
			if (creditsStr != "") {
				credits = Short.parseShort(creditsStr);
			}
			
			boolean evening = Boolean.parseBoolean(request.getParameter("evening"));
			String venue = request.getParameter("room");
			
			exam = new StudyUnit(examCode, examTitle, year, semester, examLength, noOfStudents, department, credits, evening, venue);
		}
		else if (getDetails != null)
		{
			// get details for lightbox
			examCode = request.getParameter("unit_code");
		}
		else if (studyUnitSuggest != null)
		{
			query = request.getParameter("query");
		}
		
		Connection conn = SQLHelper.getConnection();
		
		try {
			
			if (exam != null)
			{
				String startDate = request.getParameter("exam_period_start");
				
				if (StudyUnit.getStudyUnit(conn, examCode) == null)
				{
					exam.insertStudyUnit(conn);
				}
				else
				{
					exam.updateStudyUnit(conn);
				}
				
				response.sendRedirect("timetable.jsp?startdate=" + startDate);
			}
			else if (exam == null && query == null)
			{
				StudyUnit details = StudyUnit.getStudyUnit(conn, examCode);
								
				JSONObject jo = new JSONObject();
				jo.put("title", details.getTitle());
				jo.put("department", details.getDepartment());
				jo.put("examLength", details.getExamLength());
				jo.put("noOfStudents", details.getNoOfStudents());
				jo.put("semester", details.getSemester());
				jo.put("year", details.getYear());
				jo.put("credits", details.getCredits());
				jo.put("evening", details.isEvening());
				jo.put("room", details.getVenue());
				
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(jo.toJSONString());
			}
			else if (query != null)
			{
				ArrayList<String> studyUnitCodes = StudyUnit.getStudyUnitSuggestions(conn, query);				
				JSONObject jo = new JSONObject();
				
				for (int i=0; i<studyUnitCodes.size(); i++)
				{
					jo.put(Integer.toString(i), studyUnitCodes.get(i));
				}
				
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(jo.toJSONString());
			}
			
		} finally {
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