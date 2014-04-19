package thesis.timetable_generation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class Chromosome {
	
	private int[] chromosome;
	private int clashPunish;
	private int sameDayPunish;
	private int twoDayPunish;
	private int threeDayPunish;
	private int totalPunishment;
	private double fitness;
	private Table<Integer, Integer, Boolean> clashesPunished;
	private Table<Integer, Integer, Boolean> samedayPunished;
	private Table<Integer, Integer, Boolean> twodaysPunished;
	
	public Chromosome(int[] chromosome) {
		
		this.chromosome = chromosome;
		clashesPunished = HashBasedTable.create();
		samedayPunished = HashBasedTable.create();
		twodaysPunished = HashBasedTable.create();
	}
	
	public int[] getChromosome() {
		return chromosome;
	}
	
	public void setChromosome(int[] chromosome) {
		this.chromosome = chromosome;
	}
	
	public int getClashPunish() {
		return clashPunish;
	}
	
	public void setClashPunish(int clashPunish) {
		this.clashPunish = clashPunish;
	}
	
	public int getSameDayPunish() {
		return sameDayPunish;
	}
	
	public void setSameDayPunish(int sameDayPunish) {
		this.sameDayPunish = sameDayPunish;
	}
	
	public int getTwoDayPunish() {
		return twoDayPunish;
	}
	
	public void setTwoDayPunish(int twoDayPunish) {
		this.twoDayPunish = twoDayPunish;
	}
	
	public int getThreeDayPunish() {
		return threeDayPunish;
	}
	
	public void setThreeDayPunish(int threeDayPunish) {
		this.threeDayPunish = threeDayPunish;
	}
	
	public int getTotalPunishment() {
		return totalPunishment;
	}

	public void setTotalPunishment(int totalPunishment) {
		this.totalPunishment = totalPunishment;
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

}