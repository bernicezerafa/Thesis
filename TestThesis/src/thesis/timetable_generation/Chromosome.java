package thesis.timetable_generation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;

public class Chromosome implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer[] chromosome;
	private Constraint constraintViolations;
	private int totalPunishment;
	private double fitness;
	private Table<Integer, Integer, Boolean> clashesPunished;
	private Table<Integer, Integer, Boolean> samedayPunished;
	private Table<Integer, Integer, Boolean> twodaysPunished;
	private HashMap<IntTuple, Boolean> threedaysPunished;
	
	// how much times does a particular timeslot no. occur in the chromosome
	private Multiset<Integer> occurenceSet;
	
	public Chromosome(Integer[] chromosome) {
		
		this.chromosome = chromosome;
		clashesPunished = HashBasedTable.create();
		samedayPunished = HashBasedTable.create();
		twodaysPunished = HashBasedTable.create();
		threedaysPunished = new HashMap<IntTuple, Boolean>();
		constraintViolations = new Constraint();
		
		List<Integer> timeslotOccurences = Lists.newArrayList(chromosome);
		occurenceSet = HashMultiset.create(timeslotOccurences);
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
	
	public Table<Integer, Integer, Boolean> getClashesPunished() {
		return clashesPunished;
	}

	public void setClashesPunished(Table<Integer, Integer, Boolean> clashesPunished) {
		this.clashesPunished = clashesPunished;
	}

	public Table<Integer, Integer, Boolean> getSamedayPunished() {
		return samedayPunished;
	}

	public void setSamedayPunished(Table<Integer, Integer, Boolean> samedayPunished) {
		this.samedayPunished = samedayPunished;
	}

	public Table<Integer, Integer, Boolean> getTwodaysPunished() {
		return twodaysPunished;
	}

	public void setTwodaysPunished(Table<Integer, Integer, Boolean> twodaysPunished) {
		this.twodaysPunished = twodaysPunished;
	}
	
	public HashMap<IntTuple, Boolean> getThreedaysPunished() {
		return threedaysPunished;
	}

	public void setThreedaysPunished(HashMap<IntTuple, Boolean> threedaysPunished) {
		this.threedaysPunished = threedaysPunished;
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
}