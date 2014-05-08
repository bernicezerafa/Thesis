package thesis.timetable_generation;

import java.util.ArrayList;
import java.util.HashMap;

public class BestChromosome extends Chromosome {
	
	private static final long serialVersionUID = 1L;
	// constraint mapped with which exams are violating and which students
	private HashMap<Integer, HashMap<IntTuple, ArrayList<String>>> violationMap = null;
	
	public BestChromosome(Integer[] chromosome) {
		super(chromosome);
		violationMap = new HashMap<Integer, HashMap<IntTuple, ArrayList<String>>>();
	}
	
	public BestChromosome copyChromosome(BestChromosome otherChromosome) {
		
		BestChromosome chrom = new BestChromosome(otherChromosome.getChromosome());
		
		chrom.setConstraintViolations(otherChromosome.getConstraintViolations());
		chrom.setFitness(otherChromosome.getFitness());
		chrom.setViolationMap(otherChromosome.getViolationMap());
		chrom.setTotalPunishment(otherChromosome.getTotalPunishment());
		chrom.setOccurenceSet(otherChromosome.getOccurenceSet());
		chrom.setSamedayPunished(otherChromosome.getSamedayPunished());
		chrom.setTwodaysPunished(otherChromosome.getTwodaysPunished());
		chrom.setThreedaysPunished(otherChromosome.getThreedaysPunished());
		
		return chrom;
	}

	public HashMap<Integer, HashMap<IntTuple, ArrayList<String>>> getViolationMap() {
		return violationMap;
	}

	public void setViolationMap(HashMap<Integer, HashMap<IntTuple, ArrayList<String>>> violationMap) {
		this.violationMap = violationMap;
	}

	/*
	* IntTuple - array holding exams involved
	* violating type - one of the constraints constants defined
	* students violating - students affected by the constraint violation 
	*/
	public void saveViolationInfo(int violationType, IntTuple examsViolating, ArrayList<String> studentsViolating)
	{
		if (violationMap.get(violationType) == null)
		{
			// maps type of constraint with students violating the constraint
			HashMap<IntTuple, ArrayList<String>> violationStudentExams = new HashMap<IntTuple, ArrayList<String>>();
			violationStudentExams.put(examsViolating, studentsViolating);
			
			violationMap.put(violationType, violationStudentExams);
		}
		else
		{
			violationMap.get(violationType).put(examsViolating, studentsViolating);
		}
	}
}