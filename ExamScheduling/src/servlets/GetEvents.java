package servlets;

import helpers.FileHelper;
import helpers.SQLHelper;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import thesis.timetable_generation.Chromosome;
import thesis.timetable_generation.Constraint;
import thesis.timetable_generation.ExamMap;
import thesis.timetable_generation.ReadData;
import entities.TimetableEvent;

public class GetEvents extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Connection conn = null;
    private HashMap<Integer, Integer> indexExamId;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetEvents() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	public List<String> getColorShades(Color color, int noOfSameViolations) 
	{
	    List<String> colorBands = new ArrayList<>(noOfSameViolations);
	    for (int index = 0; index < noOfSameViolations; index++) 
	    {
	        colorBands.add(toHexString(darkenHSB(color, (float) index / (float) noOfSameViolations)));
	    }
	    return colorBands;
	}

	public static Color darkenHSB(Color color, float fraction)
	{
		if (fraction == 0F)
		{
			return color;
		}
		else
		{
			float hsbVals[] = Color.RGBtoHSB(color.getRed(),
											 color.getGreen(),
											 color.getBlue(), null);

			Color shadow = Color.getHSBColor(hsbVals[0], hsbVals[1], 0.8F * hsbVals[2]); //fraction * hsbVals[2]);
			return new Color(shadow.getRed(), shadow.getGreen(), shadow.getBlue(), shadow.getAlpha());
		}
	}
	
	public static Color darken(Color color, double fraction) 
	{
	    int red = (int) Math.round(Math.max(0, color.getRed() - 255 * fraction));
	    int green = (int) Math.round(Math.max(0, color.getGreen() - 255 * fraction));
	    int blue = (int) Math.round(Math.max(0, color.getBlue() - 255 * fraction));

	    int alpha = color.getAlpha();

	    return new Color(red, green, blue, alpha);
	}
	
	public static String toHexString(Color colour) 
	{
		String hexColour = Integer.toHexString(colour.getRGB() & 0xffffff);
		  
		if (hexColour.length() < 6) {
			hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
		}
		  
		return "#" + hexColour;
	}
    
	public int getColorIndex(SortedSet<Integer> setViolations, int noOfStudents)
	{
		int index = 0;
		for (Integer sortedStudents: setViolations)
		{
			if (sortedStudents == noOfStudents)
				return index;
			else index++;
		}
		return -1;
	}
	
    // getEventIds - true when return ArrayList need to be filled with event id's since modifications are being
    // 				 done on client side
    //			   - false when ArrayList need to be filled with exam id's to examine timetable
    public static ArrayList<Integer> getClashingExams(Connection conn, int eventID, boolean getEventIds) 
    {	
    	int studyUnitID = TimetableEvent.getStudyUnitID(conn, eventID);
    	HashMap<Integer, Integer> indexExamId = FileHelper.getIndexExamId();
		int examIndex = indexExamId.get(studyUnitID);
		
		HashMap<ExamMap, ArrayList<String>> clashMatrix = FileHelper.getClashesMatrix();
		
		ArrayList<Integer> clashingExams = new ArrayList<Integer>();
		
		for (int i=0; i < clashMatrix.size(); i++)
		{
			// if there is a clash between exam clicked and exam i
			if (clashMatrix.get(new ExamMap(examIndex, i)) != null)
			{
				// get exam ID by index of that exam
				int clashedExamID = ReadData.getKeyByValue(indexExamId, i);
				
				if (getEventIds)
				{
					int eventClashID = TimetableEvent.getEventID(conn, clashedExamID);
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
    
    public int getEventID(int examPos)
    {
		int eventID = -1;
		if (examPos != -1)
		{
			int exam2ID = ReadData.getKeyByValue(indexExamId, examPos);
		    eventID = TimetableEvent.getEventID(conn, exam2ID);
		}
		
		return eventID;
    }
    
    public static <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) 
    {
		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
		
		Collections.sort(sortedEntries, 
		    new Comparator<Entry<K,V>>() 
		    {
		        @Override
		        public int compare(Entry<K,V> e1, Entry<K,V> e2) 
		        {
		            return e2.getValue().compareTo(e1.getValue());
		        }
		    }
		);
		
		return sortedEntries;
	}
    
    @SuppressWarnings("unchecked")
	public JSONArray getViolationEvents()
    {
    	JSONArray violationColorMap = new JSONArray();
    	
    	conn = SQLHelper.getConnection();
    	indexExamId = FileHelper.getIndexExamId();
    	
    	Chromosome chromosome = FileHelper.getBestChromosome();
    	HashMap<ExamMap, ArrayList<String>>[] violationMap = chromosome.getViolationMap();
		
		for (int index = 0; index < violationMap.length; index++)
		{
			Integer violationKey = index;
			HashMap<ExamMap, ArrayList<String>> violationEntry = violationMap[index];
			
			if (violationEntry != null)
			{
				Color color = Constraint.getViolationColor(violationKey);
				
				SortedSet<Integer> setViolations = new TreeSet<Integer>();
				HashMap<ExamMap, Integer> unorderedViolations = new HashMap<ExamMap, Integer>();
				
				for (Entry<ExamMap, ArrayList<String>> examStudentsViolation: violationEntry.entrySet())
				{					
					ExamMap examMap = examStudentsViolation.getKey();
					int noOfStudents = examStudentsViolation.getValue().size();
					
					setViolations.add(noOfStudents);
					unorderedViolations.put(examMap, noOfStudents);
				}
				
				if (color != null)
				{
					//List<String> colorShades = getColorShades(color, setViolations.size());
					JSONArray differentShadesArr = new JSONArray();
					
					// list entry ExamMap, Integer sorted worst violations (i.e. greatest no. of students)
					List<Entry<ExamMap, Integer>> orderedViolations = entriesSortedByValues(unorderedViolations);
					
					for (Entry<ExamMap, Integer> violations: orderedViolations)
					{
						JSONObject jsonObject = new JSONObject();
						ExamMap examMap = violations.getKey();
						//Integer noOfStudents = violations.getValue();
						
						int[] examsArr = examMap.getData();
						JSONArray exams = new JSONArray();
						
						for (int i=0; i < examsArr.length; i++)
						{
							int eventId = getEventID(examsArr[i]);
							exams.add(eventId);
						}
						jsonObject.put("exams", exams);
						jsonObject.put("color", toHexString(color));
						
						/*
						if (violationKey != Constraint.EVENING_PUNISH && violationKey != Constraint.CLASH_PUNISH)
						{
							int colorIndex = getColorIndex(setViolations, noOfStudents);
							String shade = colorShades.get(colorIndex);
							jsonObject.put("color", shade);
						}
						else
						{
							jsonObject.put("color", toHexString(color));
						}*/
						
						differentShadesArr.add(jsonObject);
					}
					
					violationColorMap.add(differentShadesArr);
				}
			}
		}
		return violationColorMap;
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
			boolean evening = Boolean.parseBoolean(request.getParameter("evening"));
			String getEventColors = request.getParameter("getColors");
			
			if (getEventColors != null)
			{
				JSONArray eventColors = getViolationEvents();
			
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(eventColors.toJSONString());
			}
			else
			{
				if (unitCode != null)
				{
					response.setContentType("text/html;charset=UTF-8");
					PrintWriter out = response.getWriter();
					
					String eventId = TimetableEvent.getEventId(conn, unitCode, evening);
					
					try {
						out.println(eventId);
					} finally {
						out.close();
					}
				}
				else
				{
					int eventID = Integer.parseInt(request.getParameter("eventID"));
					
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