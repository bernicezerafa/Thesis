package thesis.timetable_generation;

import java.io.Serializable;

public class GAParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Double crossoverRate;
	private Double mutationRate;
	private Integer noOfGenerations;
	private Integer noOfChromosomes;
	private Integer crossoverType;
	private Integer fitnessFunction;
	private Integer elitistSelection;
	private Integer randomIntroduction;
	private Integer interpolatingRates;
	private Integer inverseSquarePressure;
	
	private Integer clashPunishment; 
	private Integer eveningPunishment;
	private Integer samedayPunishment; 
	private Integer twodaysPunishment; 
	private Integer threedaysPunishment;
	private Integer spreadOutPunishment;
	private Integer noOfStudentsPunishment;
	private Integer eveningMorningPunishment;
	
	private Double minCrossoverRate;
	private Double maxMutationRate;
	private Double crossoverStepValue;
	private Double mutationStepValue;
	
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
	
	public Double getCrossoverRate() {
		return crossoverRate;
	}

	public void setCrossoverRate(Double crossoverRate) {
		this.crossoverRate = crossoverRate;
	}

	public Double getMutationRate() {
		return mutationRate;
	}

	public void setMutationRate(Double mutationRate) {
		this.mutationRate = mutationRate;
	}

	public Integer getNoOfGenerations() {
		return noOfGenerations;
	}

	public void setNoOfGenerations(Integer noOfGenerations) {
		this.noOfGenerations = noOfGenerations;
	}

	public Integer getNoOfChromosomes() {
		return noOfChromosomes;
	}

	public void setNoOfChromosomes(Integer noOfChromosomes) {
		this.noOfChromosomes = noOfChromosomes;
	}

	public Integer getClashPunishment() {
		return clashPunishment;
	}

	public void setClashPunishment(Integer clashPunishment) {
		this.clashPunishment = clashPunishment;
	}

	public Integer getEveningPunishment() {
		return eveningPunishment;
	}

	public void setEveningPunishment(Integer eveningPunishment) {
		this.eveningPunishment = eveningPunishment;
	}
	
	public Integer getSameDayPunishment() {
		return samedayPunishment;
	}

	public void setSameDayPunishment(Integer samedayPunishment) {
		this.samedayPunishment = samedayPunishment;
	}

	public Integer getTwoDaysPunishment() {
		return twodaysPunishment;
	}

	public void setTwoDaysPunishment(Integer twodaysPunishment) {
		this.twodaysPunishment = twodaysPunishment;
	}

	public Integer getThreeDaysPunishment() {
		return threedaysPunishment;
	}

	public void setThreeDaysPunishment(Integer threedaysPunishment) {
		this.threedaysPunishment = threedaysPunishment;
	}

	public Integer getSpreadOutPunishment() {
		return spreadOutPunishment;
	}

	public void setSpreadOutPunishment(Integer spreadOutPunishment) {
		this.spreadOutPunishment = spreadOutPunishment;
	}
	
	public Integer getNoOfStudentsPunishment() {
		return noOfStudentsPunishment;
	}

	public void setNoOfStudentsPunishment(Integer noOfStudentsPunishment) {
		this.noOfStudentsPunishment = noOfStudentsPunishment;
	}
	
	public Integer getEveningMorningPunishment() {
		return eveningMorningPunishment;
	}

	public void setEveningMorningPunishment(Integer eveningMorningPunishment) {
		this.eveningMorningPunishment = eveningMorningPunishment;
	}

	public Double getMinCrossoverRate() {
		return minCrossoverRate;
	}

	public void setMinCrossoverRate(Double minCrossoverRate) {
		this.minCrossoverRate = minCrossoverRate;
	}

	public Double getMaxMutationRate() {
		return maxMutationRate;
	}

	public void setMaxMutationRate(Double maxMutationRate) {
		this.maxMutationRate = maxMutationRate;
	}
	
	public Double getCrossoverStepValue() {
		return crossoverStepValue;
	}

	public void setCrossoverStepValue(Double crossoverStepValue) {
		this.crossoverStepValue = crossoverStepValue;
	}

	public Double getMutationStepValue() {
		return mutationStepValue;
	}

	public void setMutationStepValue(Double mutationStepValue) {
		this.mutationStepValue = mutationStepValue;
	}
	
	public Integer getCrossoverType() {
		return crossoverType;
	}

	public void setCrossoverType(Integer crossoverType) {
		this.crossoverType = crossoverType;
	}

	public Integer getFitnessFunction() {
		return fitnessFunction;
	}

	public void setFitnessFunction(Integer fitnessFunction) {
		this.fitnessFunction = fitnessFunction;
	}

	public Integer getElitistSelection() {
		return elitistSelection;
	}

	public void setElitistSelection(Integer elitistSelection) {
		this.elitistSelection = elitistSelection;
	}

	public Integer getRandomIntroduction() {
		return randomIntroduction;
	}

	public void setRandomIntroduction(Integer randomIntroduction) {
		this.randomIntroduction = randomIntroduction;
	}

	public Integer getInterpolatingRates() {
		return interpolatingRates;
	}

	public void setInterpolatingRates(Integer interpolatingRates) {
		this.interpolatingRates = interpolatingRates;
	}

	public Integer getInverseSquarePressure() {
		return inverseSquarePressure;
	}

	public void setInverseSquarePressure(Integer inverseSquarePressure) {
		this.inverseSquarePressure = inverseSquarePressure;
	}
}
