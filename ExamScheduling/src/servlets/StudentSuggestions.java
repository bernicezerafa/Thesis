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

import entities.Student;
import entities.StudentExams;

public class StudentSuggestions extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public StudentSuggestions() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Connection conn = SQLHelper.getConnection();
		String query = request.getParameter("term");
		
		JSONObject jo = new JSONObject();
		
		if (query != null)
		{
			ArrayList<String> studentIds = Student.getStudentSuggestions(conn, query);		
			
			for (int i=0; i<studentIds.size(); i++)
			{
				jo.put(Integer.toString(i), studentIds.get(i));
			}
		}	
		
		String studentID = request.getParameter("studentID");
		
		if (studentID != null)
		{
			// check if student exists
			// if not, add student
			if (!Student.studentExists(conn, studentID.trim()))
			{
				Student student = new Student(studentID);
				student.insertStudent(conn);
				
				jo.put("insert", studentID);
			}
			// else get study units for student
			else
			{
				ArrayList<String> unitCodes = StudentExams.getStudentExams(conn, studentID);
				
				for (int i=0; i < unitCodes.size(); i++)
				{
					jo.put(Integer.toString(i), unitCodes.get(i));
				}
			}
		}
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jo.toJSONString());
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
}