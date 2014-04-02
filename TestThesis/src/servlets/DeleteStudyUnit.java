package servlets;

import helpers.SQLHelper;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import entities.StudyUnit;

public class DeleteStudyUnit extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeleteStudyUnit() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String examCode = request.getParameter("unit_code");
		String startDate = request.getParameter("start_date");
		
		System.out.println(examCode);
		
		Connection conn = SQLHelper.getConnection();
		
		try {
			StudyUnit.deleteStudyUnit(conn, examCode);
			
		} finally {
			SQLHelper.closeConnection(conn);
		}
		
		response.sendRedirect("timetable.jsp?startdate=" + startDate);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

}
