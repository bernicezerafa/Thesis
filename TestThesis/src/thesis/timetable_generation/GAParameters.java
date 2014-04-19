package thesis.timetable_generation;

import java.io.Serializable;

@SuppressWarnings("serial")
public class GAParameters implements Serializable {
	
	private double crossoverRate;
	private double mutationRate;
	private int noOfGenerations;
	private int noOfChromosomes;
	private int crossoverType;
	private int fitnessFunction;
	private int elitistSelection;
	private int randomIntroduction;
	private int interpolatingRates;
	private int inverseSquarePressure;
	
	private int clashPunishment; 
	private int samedayPunishment; 
	private int twodaysPunishment; 
	private int threedaysPunishment;
	
	private double minCrossoverRate;
	private double maxMutationRate;
	private double stepValue;
	
	public static final class CrossoverType {
		
		public static final int ONEPOINT = 0;
		public static final int TWOPOINT = 1;
		public static final int UNIFORM = 2;
		public static final int INVERSION = 3;		
	}
	
	public static final class FitnessFunction {
		
		public static final int INVERSE_FUNCTION = 0;
		public static final int INVERSE_SQUARE_PRESSURE = 1;
	}
	
	public static final class ElitistSelection {
		
		public static final int NO_ELITIST_SELECTION = 0;
		public static final int ELITIST_SELECTION = 1;
	}
	
	public static final class RandomIntroduction {
		
		public static final int NO_RANDOM_INTRODUCTION = 0;
		public static final int RANDOM_INTRODUCTION = 1;
	}
	
	public static final class InterpolatingRates {
		
		public static final int NO_INTERPOLATING_RATES = 0;
		public static final int INTERPOLATING_RATES = 1;
	}
	
	public double getCrossoverRate() {
		return crossoverRate;
	}

	public void setCrossoverRate(double crossoverRate) {
		this.crossoverRate = crossoverRate;
	}

	public double getMutationRate() {
		return mutationRate;
	}

	public void setMutationRate(double mutationRate) {
		this.mutationRate = mutationRate;
	}

	public int getNoOfGenerations() {
		return noOfGenerations;
	}

	public void setNoOfGenerations(int noOfGenerations) {
		this.noOfGenerations = noOfGenerations;
	}

	public int getNoOfChromosomes() {
		return noOfChromosomes;
	}

	public void setNoOfChromosomes(int noOfChromosomes) {
		this.noOfChromosomes = noOfChromosomes;
	}

	public int getClashPunishment() {
		return clashPunishment;
	}

	public void setClashPunishment(int clashPunishment) {
		this.clashPunishment = clashPunishment;
	}

	public int getSameDayPunishment() {
		return samedayPunishment;
	}

	public void setSameDayPunishment(int samedayPunishment) {
		this.samedayPunishment = samedayPunishment;
	}

	public int getTwoDaysPunishment() {
		return twodaysPunishment;
	}

	public void setTwoDaysPunishment(int twodaysPunishment) {
		this.twodaysPunishment = twodaysPunishment;
	}

	public int getThreeDaysPunishment() {
		return threedaysPunishment;
	}

	public void setThreeDaysPunishment(int threedaysPunishment) {
		this.threedaysPunishment = threedaysPunishment;
	}

	public double getMinCrossoverRate() {
		return minCrossoverRate;
	}

	public void setMinCrossoverRate(double minCrossoverRate) {
		this.minCrossoverRate = minCrossoverRate;
	}

	public double getMaxMutationRate() {
		return maxMutationRate;
	}

	public void setMaxMutationRate(double maxMutationRate) {
		this.maxMutationRate = maxMutationRate;
	}

	public double getStepValue() {
		return stepValue;
	}

	public void setStepValue(double stepValue) {
		this.stepValue = stepValue;
	}
	
	public int getCrossoverType() {
		return crossoverType;
	}

	public void setCrossoverType(int crossoverType) {
		this.crossoverType = crossoverType;
	}

	public int getFitnessFunction() {
		return fitnessFunction;
	}

	public void setFitnessFunction(int fitnessFunction) {
		this.fitnessFunction = fitnessFunction;
	}

	public int getElitistSelection() {
		return elitistSelection;
	}

	public void setElitistSelection(int elitistSelection) {
		this.elitistSelection = elitistSelection;
	}

	public int getRandomIntroduction() {
		return randomIntroduction;
	}

	public void setRandomIntroduction(int randomIntroduction) {
		this.randomIntroduction = randomIntroduction;
	}

	public int getInterpolatingRates() {
		return interpolatingRates;
	}

	public void setInterpolatingRates(int interpolatingRates) {
		this.interpolatingRates = interpolatingRates;
	}

	public int getInverseSquarePressure() {
		return inverseSquarePressure;
	}

	public void setInverseSquarePressure(int inverseSquarePressure) {
		this.inverseSquarePressure = inverseSquarePressure;
	}
}
