package servlets;

import helpers.FileHelper;
import helpers.SQLHelper;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import thesis.timetable_generation.BestChromosome;
import thesis.timetable_generation.Constraint;
import thesis.timetable_generation.IntTuple;
import thesis.timetable_generation.ReadData;
import thesis.timetable_generation.Timeslot;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

import entities.StudyUnit;
import entities.TimetableEvent;

public class GetTimetableState extends HttpServlet 
{	
	private static final long serialVersionUID = 1L;
    private Connection conn = null;
    private HashMap<Integer, Integer> indexExamID = null;
    private int modifiedNoOfStudents = 0;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetTimetableState()
    {
        super();
    }

    public int getEventID(int examPos)
    {
		int eventID = -1;
		if (examPos != -1)
		{
			int exam2ID = ReadData.getKeyByValue(indexExamID, examPos);
		    eventID = TimetableEvent.getEventID(conn, exam2ID);
		}
		
		return eventID;
    }
    
    @SuppressWarnings("unchecked")
	public JSONArray getExamViolationArray(Map<IntTuple, ArrayList<String>> entries, String type)
    {
    	JSONArray removedModifiedViolations = new JSONArray();
		modifiedNoOfStudents = 0;
    	
		for (Entry<IntTuple, ArrayList<String>> entry: entries.entrySet())
		{
			JSONObject thisViolation = new JSONObject();
			
			int[] examsArr = entry.getKey().getData();
			
			JSONArray exams = new JSONArray();
			for (int i=0; i < examsArr.length; i++)
			{
				JSONObject exam = new JSONObject();
				int eventId = getEventID(examsArr[i]);
				StudyUnit unit = StudyUnit.getStudyUnit(conn, eventId);		
				Timeslot timeslot = TimetableEvent.getEventTimeslot(conn, eventId);
				
				exam.put("unitCode", unit.getUnitCode());
				exam.put("evening", unit.isEvening());
				exam.put("eventStart", timeslot.getStartDate());
				exam.put("eventEnd", timeslot.getEndTime());
				exams.add(exam);
			}
			thisViolation.put("exams", exams);
			
			ArrayList<String> commonStudents = entry.getValue();
			
			JSONArray studentsAffected = new JSONArray();
			
			for (String studentId: commonStudents)
			{
				JSONObject student = new JSONObject();
				student.put("studentId", studentId);
				studentsAffected.add(student);
			}
			thisViolation.put("studentsAffected", studentsAffected);
			thisViolation.put("thisNoOfStudents", commonStudents.size());
			
			if (type.equals("added") || type.equals("common"))
			{
				modifiedNoOfStudents += commonStudents.size();
			}
			
			removedModifiedViolations.add(thisViolation);
		}
		return removedModifiedViolations;
    }
    
    @SuppressWarnings("unchecked")
	public JSONArray getViolationsArray(BestChromosome chromosome, Map<Integer, HashMap<IntTuple, ArrayList<String>>> violationMap)
    {
    	JSONArray removedOrAddedArr = new JSONArray();
		
		for (Entry<Integer, HashMap<IntTuple, ArrayList<String>>> removed: violationMap.entrySet())
		{
			JSONObject violationObj = new JSONObject();
			
			int violationKey = removed.getKey();
			HashMap<IntTuple, ArrayList<String>> violation = removed.getValue();
			
			JSONArray violationsOfType = new JSONArray();
			for (Entry<IntTuple, ArrayList<String>> entry: violation.entrySet())
			{
				JSONObject thisViolation = new JSONObject();
				
				int[] examsArr = entry.getKey().getData();
				
				JSONArray exams = new JSONArray();
				for (int i=0; i < examsArr.length; i++)
				{
					JSONObject exam = new JSONObject();
					int eventId = getEventID(examsArr[i]);
					StudyUnit unit = StudyUnit.getStudyUnit(conn, eventId);		
					Timeslot timeslot = TimetableEvent.getEventTimeslot(conn, eventId);
					
					exam.put("unitCode", unit.getUnitCode());
					exam.put("evening", unit.isEvening());
					exam.put("eventStart", timeslot.getStartDate());
					exam.put("eventEnd", timeslot.getEndTime());
					exams.add(exam);
				}
				thisViolation.put("exams", exams);
				
				ArrayList<String> commonStudents = entry.getValue();
				
				JSONArray studentsAffected = new JSONArray();
				
				for (String studentId: commonStudents)
				{
					JSONObject student = new JSONObject();
					student.put("studentId", studentId);
					studentsAffected.add(student);
				}
				thisViolation.put("studentsAffected", studentsAffected);
				thisViolation.put("thisNoOfStudents", commonStudents.size());
				
				violationsOfType.add(thisViolation);
			}
			
			violationObj.put("type", Constraint.getViolation(violationKey));
			violationObj.put("noOfStudents", Constraint.getNoOfStudentsAffected(chromosome.getConstraintViolations(), violationKey));
			violationObj.put("violations", violationsOfType);
			
			removedOrAddedArr.add(violationObj);
		}
		
    	return removedOrAddedArr;
    }
    
    @SuppressWarnings("unchecked")
	public JSONObject compareViolations(BestChromosome chromosomeBefore, BestChromosome chromosomeAfter)
    {
    	HashMap<Integer, HashMap<IntTuple, ArrayList<String>>> violationMapBefore = chromosomeBefore.getViolationMap();
    	HashMap<Integer, HashMap<IntTuple, ArrayList<String>>> violationMapAfter = chromosomeAfter.getViolationMap();
    	
		MapDifference<Integer, HashMap<IntTuple, ArrayList<String>>> diff = Maps.difference(violationMapBefore, violationMapAfter);
		
		Map<Integer, HashMap<IntTuple, ArrayList<String>>> removedViolations = diff.entriesOnlyOnLeft();
		Map<Integer, HashMap<IntTuple, ArrayList<String>>> addedViolations = diff.entriesOnlyOnRight();
		Map<Integer, ValueDifference<HashMap<IntTuple, ArrayList<String>>>> modified = diff.entriesDiffering();
		Map<Integer, HashMap<IntTuple, ArrayList<String>>> commonViolations = diff.entriesInCommon();
		
		JSONObject comparisons = new JSONObject();
		
		// removed violations from chromosome before to chromosome after
		JSONArray removedViolationsArr = getViolationsArray(chromosomeBefore, removedViolations);
		comparisons.put("removedViolations", removedViolationsArr);
		
		// added violations from chromosome before to chromosome before
		JSONArray addedViolationsArr = getViolationsArray(chromosomeAfter, addedViolations);
		comparisons.put("addedViolations", addedViolationsArr);
		
		JSONArray commonViolationsArr = getViolationsArray(chromosomeBefore, commonViolations);
		comparisons.put("commonViolations", commonViolationsArr);
		
		// both violations present in both chromosomes but there may be added/removed exam violations
		// or added/dropped students
		JSONArray modifiedViolationsArr = new JSONArray();
		
		for (Entry<Integer, ValueDifference<HashMap<IntTuple, ArrayList<String>>>> modify: modified.entrySet())
		{
			JSONObject modifiedViolationObj = new JSONObject();
			
			int violationKey = modify.getKey();
			ValueDifference<HashMap<IntTuple, ArrayList<String>>> violation = modify.getValue();
			
			HashMap<IntTuple, ArrayList<String>> violationBefore = violation.leftValue();
			HashMap<IntTuple, ArrayList<String>> violationAfter = violation.rightValue();
			
			MapDifference<IntTuple, ArrayList<String>> violationDiff = Maps.difference(violationBefore, violationAfter);

			Map<IntTuple, ArrayList<String>> entriesBefore = violationDiff.entriesOnlyOnLeft();
			Map<IntTuple, ArrayList<String>> entriesAfter = violationDiff.entriesOnlyOnRight();
			Map<IntTuple, ArrayList<String>> entriesCommon = violationDiff.entriesInCommon();
			
			JSONArray removedModifiedViolations = getExamViolationArray(entriesBefore, "removed");
			JSONArray addedModifiedViolations = getExamViolationArray(entriesAfter, "added");
			JSONArray commonModifiedViolations = getExamViolationArray(entriesCommon, "common");
						
			modifiedViolationObj.put("type", Constraint.getViolation(violationKey));
			modifiedViolationObj.put("removedModified", removedModifiedViolations);
			modifiedViolationObj.put("addedModified", addedModifiedViolations);
			modifiedViolationObj.put("commonModified", commonModifiedViolations);
			
			JSONArray modifiedModifyViolationsArr = new JSONArray();
			Map<IntTuple, ValueDifference<ArrayList<String>>> modifiedViolations = violationDiff.entriesDiffering();

			for (Entry<IntTuple, ValueDifference<ArrayList<String>>> entry: modifiedViolations.entrySet())
			{
				modifiedNoOfStudents = 0;

				JSONObject thisViolation = new JSONObject();
				
				int[] examsArr = entry.getKey().getData();
				
				JSONArray exams = new JSONArray();
				for (int i=0; i < examsArr.length; i++)
				{
					JSONObject exam = new JSONObject();
					int eventId = getEventID(examsArr[i]);
					StudyUnit unit = StudyUnit.getStudyUnit(conn, eventId);		
					Timeslot timeslot = TimetableEvent.getEventTimeslot(conn, eventId);
					
					exam.put("unitCode", unit.getUnitCode());
					exam.put("evening", unit.isEvening());
					exam.put("eventStart", timeslot.getStartDate());
					exam.put("eventEnd", timeslot.getEndTime());
					exams.add(exam);
				}
				
				ArrayList<String> studentsBefore = entry.getValue().leftValue();
				ArrayList<String> studentsAfter = entry.getValue().rightValue();
				
				JSONArray studentsAffected = new JSONArray();
				
				for (String studentId: studentsBefore)
				{
					JSONObject student = new JSONObject();
					student.put("studentId", studentId);
					
					if (!studentsAfter.contains(studentId))
						student.put("state", "removed");
					else
						student.put("state", "common");
					
					studentsAffected.add(student);
				}
				
				for (String studentId: studentsAfter)
				{
					if (!studentsBefore.contains(studentId))
					{
						JSONObject student = new JSONObject();
						student.put("studentId", studentId);
						student.put("state", "added");
						studentsAffected.add(student);
					}
				}
				
				thisViolation.put("exams", exams);
				thisViolation.put("studentsAffected", studentsAffected);
				thisViolation.put("thisNoOfStudents", studentsAffected.size());
				
				modifiedNoOfStudents += studentsAffected.size();
				modifiedModifyViolationsArr.add(thisViolation);	
			}
			
			modifiedViolationObj.put("noOfStudents", modifiedNoOfStudents);
			modifiedViolationObj.put("modifiedModified", modifiedModifyViolationsArr);
			modifiedViolationsArr.add(modifiedViolationObj);
		}
		
		comparisons.put("modifiedViolations", modifiedViolationsArr);
		
		return comparisons;
    }
    
    @SuppressWarnings("unchecked")
	public JSONArray getViolations(BestChromosome chromosome)
    {
    	JSONArray allViolations = new JSONArray();
		HashMap<Integer, HashMap<IntTuple, ArrayList<String>>> violationMap = chromosome.getViolationMap();
		Connection conn = SQLHelper.getConnection();
		
		for (Entry<Integer, HashMap<IntTuple, ArrayList<String>>> violationEntry: violationMap.entrySet())
		{
			Integer violationKey = violationEntry.getKey();
			HashMap<IntTuple, ArrayList<String>> violationsOfTypeKey = violationEntry.getValue();
			
			JSONObject jsonObject = new JSONObject();
			JSONArray violationsOfType = new JSONArray();
						
			for (Entry<IntTuple, ArrayList<String>> examStudentsViolation: violationsOfTypeKey.entrySet())
			{
				JSONObject violation = new JSONObject();
				
				IntTuple examMap = examStudentsViolation.getKey();
				ArrayList<String> commonStudents = examStudentsViolation.getValue();
				
				int[] examsArr = examMap.getData();
				
				JSONArray exams = new JSONArray();
				for (int i=0; i < examsArr.length; i++)
				{
					JSONObject exam = new JSONObject();
					int eventId = getEventID(examsArr[i]);
					StudyUnit unit = StudyUnit.getStudyUnit(conn, eventId);		
					Timeslot timeslot = TimetableEvent.getEventTimeslot(conn, eventId);
					
					exam.put("unitCode", unit.getUnitCode());
					exam.put("evening", unit.isEvening());
					exam.put("eventStart", timeslot.getStartDate());
					exam.put("eventEnd", timeslot.getEndTime());
					exams.add(exam);
				}
				violation.put("exams", exams);
				
				JSONArray studentsAffected = new JSONArray();
				for (String studentId: commonStudents)
				{
					JSONObject student = new JSONObject();
					student.put("studentId", studentId);
					studentsAffected.add(student);
				}
				
				violation.put("students", studentsAffected);
				violation.put("thisNoOfStudents", commonStudents.size());
				violationsOfType.add(violation);
			}
			jsonObject.put("type", Constraint.getViolation(violationKey));
			jsonObject.put("noOfStudents", Constraint.getNoOfStudentsAffected(chromosome.getConstraintViolations(), violationKey));
			jsonObject.put("violations", violationsOfType);
			
			allViolations.add(jsonObject);
		}
		return allViolations;
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String compareChanges = request.getParameter("compareChanges");
		
		BestChromosome bestChromosome = FileHelper.getBestChromosome();
		conn = SQLHelper.getConnection();
		indexExamID = FileHelper.getIndexExamId();
		
		JSONArray allViolations = getViolations(bestChromosome);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		if (compareChanges != null)
		{
			BestChromosome chromosomeBefore = FileHelper.getChromosomeBefore();
			
			JSONObject comparisons = compareViolations(chromosomeBefore, bestChromosome);
			response.getWriter().write(comparisons.toJSONString());	
		}
		else
		{
			response.getWriter().write(allViolations.toJSONString());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}
}
