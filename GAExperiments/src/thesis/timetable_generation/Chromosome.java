package thesis.timetable_generation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

public class Chromosome implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer[] chromosome;
	private Constraint constraintViolations;
	private int totalPunishment;
	private double fitness;
	//private HashMap<Integer, HashMap<ExamMap, ArrayList<String>>> violationMap = null;
	private HashMap<ExamMap, ArrayList<String>>[] violationMap = null;
	
	// how much times does a particular timeslot no. occur in the chromosome
	private Multiset<Integer> occurenceSet;
	
	@SuppressWarnings("unchecked")
	public Chromosome(Integer[] chromosome) {
		
		this.chromosome = chromosome;
		constraintViolations = new Constraint();
		
		List<Integer> timeslotOccurences = Lists.newArrayList(chromosome);
		occurenceSet = HashMultiset.create(timeslotOccurences);
		violationMap = new HashMap[Constraint.noOfConstraints];
	}
	
	public Chromosome copyChromosome(Chromosome otherChromosome) {
		
		Chromosome chrom = new Chromosome(otherChromosome.getChromosome());
		
		chrom.setConstraintViolations(otherChromosome.getConstraintViolations());
		chrom.setFitness(otherChromosome.getFitness());
		chrom.setViolationMap(otherChromosome.getViolationMap());
		chrom.setTotalPunishment(otherChromosome.getTotalPunishment());
		chrom.setOccurenceSet(otherChromosome.getOccurenceSet());
		
		return chrom;
	}
	
	public Integer[] getChromosome() {
		return chromosome;
	}
	
	public void setChromosome(Integer[] chromosome) {
		this.chromosome = chromosome;
	}
	
	public Constraint getConstraintViolations() {
		return constraintViolations;
	}
	
	public void setConstraintViolations(Constraint constraintViolations) {
		this.constraintViolations = constraintViolations;
	}
	
	public double getFitness() {
		return fitness;
	}
	
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
	public Multiset<Integer> getOccurenceSet() {
		return occurenceSet;
	}

	public void setOccurenceSet(Multiset<Integer> occurenceSet) {
		this.occurenceSet = occurenceSet;
	}

	public int getTotalPunishment() {
		return totalPunishment;
	}

	public void setTotalPunishment(int totalPunishment) {
		this.totalPunishment = totalPunishment;
	}
	
	public HashMap<ExamMap, ArrayList<String>>[] getViolationMap() {
		return violationMap;
	}

	public void setViolationMap(HashMap<ExamMap, ArrayList<String>>[] violationMap) {
		this.violationMap = violationMap;
	}

	/*
	* ExamMap - array holding exams involved - examID's
	* violating type - one of the constraints constants defined
	* students violating - students affected by the constraint violation 
	*/
	public void saveViolationInfo(int violationType, ExamMap examsViolating, ArrayList<String> studentsViolating)
	{
		if (violationMap[violationType] == null)
		{
			// maps type of constraint with students violating the constraint
			HashMap<ExamMap, ArrayList<String>> violationStudentExams = new HashMap<ExamMap, ArrayList<String>>();
			violationStudentExams.put(examsViolating, studentsViolating);
			
			violationMap[violationType] = violationStudentExams;
		}
		else
		{
			violationMap[violationType].put(examsViolating, studentsViolating);
		}
	}
}