package thesis.timetable_generation;

import java.io.Serializable;
import java.util.HashMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/*
 * This class holds two structures for the exam index and the exam id and two
 * structures which hold the same data for evening exams.
 * 
 * Exams which need to be scheduled together have the same index in the first
 * four structures but are uniquely identified by the last two structures
 */
public class ExamIndexMaps implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private HashMap<Integer, Integer> examIDIndex;
	private ListMultimap<Integer, Integer> indexExamID;
	private HashMap<Integer, Integer> eveningExamIDIndex;
	private ListMultimap<Integer, Integer> eveningIndexExamID;
		
	public ExamIndexMaps() {
		
		// create index vs exam id data structures
		examIDIndex = new HashMap<Integer, Integer>();	
		indexExamID = ArrayListMultimap.create();
		
		// create evening data structures
		eveningExamIDIndex = new HashMap<Integer, Integer>();
		eveningIndexExamID = ArrayListMultimap.create();
	}
		
	public HashMap<Integer, Integer> getExamIDIndex() {
		return examIDIndex;
	}
	
	public void setExamIDIndex(HashMap<Integer, Integer> examIDIndex) {
		this.examIDIndex = examIDIndex;
	}
	
	public ListMultimap<Integer, Integer> getIndexExamID() {
		return indexExamID;
	}
	
	public void setIndexExamID(ListMultimap<Integer, Integer> indexExamID) {
		this.indexExamID = indexExamID;
	}
	
	public HashMap<Integer, Integer> getEveningExamIDIndex() {
		return eveningExamIDIndex;
	}
	
	public void setEveningExamIDIndex(HashMap<Integer, Integer> eveningExamIDIndex) {
		this.eveningExamIDIndex = eveningExamIDIndex;
	}
	
	public ListMultimap<Integer, Integer> getEveningIndexExamID() {
		return eveningIndexExamID;
	}

	public void setEveningIndexExamID(ListMultimap<Integer, Integer> eveningIndexExamID) {
		this.eveningIndexExamID = eveningIndexExamID;
	}
	
	public void addIndexExamEntry(Integer index, Integer examID) {
		
		indexExamID.put(index, examID);
		examIDIndex.put(examID, index);
	}
	
	public void addEveningIndexExamEntry(Integer index, Integer examID) {
		
		eveningIndexExamID.put(index, examID);
		eveningExamIDIndex.put(examID, index);
	}
	
	public void removeIndexExamEntry(Integer index, Integer examID) {
		
		indexExamID.remove(index, examID);
		examIDIndex.remove(examID);
	}
	
	public void removeEveningIndexExamEntry(Integer index, Integer examID) {
		
		eveningIndexExamID.remove(index, examID);
		eveningExamIDIndex.remove(examID);
	}
	
	public boolean isMappedToTimeslot(int index, int examID) {
		
		if (examIDIndex.get(examID) != null || indexExamID.get(index).contains(examID))
			return true;
		else return false;
	}
	
	public int getNoOfIndexes() {
		return indexExamID.size();
	}
}