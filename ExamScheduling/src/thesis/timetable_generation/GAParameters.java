package thesis.timetable_generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

public class GAParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
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
	private int eveningPunishment;
	private int samedayPunishment; 
	private int twodaysPunishment; 
	private int threedaysPunishment;
	private int spreadOutPunishment;
	private int noOfStudentsPunishment;
	private int eveningMorningPunishment;
	
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

	public int getEveningPunishment() {
		return eveningPunishment;
	}

	public void setEveningPunishment(int eveningPunishment) {
		this.eveningPunishment = eveningPunishment;
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

	public int getSpreadOutPunishment() {
		return spreadOutPunishment;
	}

	public void setSpreadOutPunishment(int spreadOutPunishment) {
		this.spreadOutPunishment = spreadOutPunishment;
	}
	
	public int getNoOfStudentsPunishment() {
		return noOfStudentsPunishment;
	}

	public void setNoOfStudentPunishment(int noOfStudentsPunishment) {
		this.noOfStudentsPunishment = noOfStudentsPunishment;
	}
	
	public int getEveningMorningPunishment() {
		return eveningMorningPunishment;
	}

	public void setEveningMorningPunishment(int eveningMorningPunishment) {
		this.eveningMorningPunishment = eveningMorningPunishment;
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
	
	public void setGAParameters()
	{
		
		try {
			
			Properties properties = new Properties();
			properties.setProperty("crossoverRate", Double.toString(crossoverRate));
			properties.setProperty("mutationRate", Double.toString(mutationRate));
			properties.setProperty("noOfGenerations", Integer.toString(noOfGenerations));
			properties.setProperty("crossoverType", Integer.toString(crossoverType));
			properties.setProperty("fitnessFunction", Integer.toString(fitnessFunction));
			properties.setProperty("elitistSelection", Integer.toString(elitistSelection));
			properties.setProperty("randomIntroduction", Integer.toString(randomIntroduction));
			properties.setProperty("interpolatingRates", Integer.toString(interpolatingRates));
			properties.setProperty("inverseSquarePressure", Integer.toString(inverseSquarePressure));
			properties.setProperty("minCrossoverRate", Double.toString(minCrossoverRate));
			properties.setProperty("maxMutationRate", Double.toString(maxMutationRate));
			properties.setProperty("stepValue", Double.toString(stepValue));
			properties.setProperty("clashPunishment", Integer.toString(clashPunishment));
			properties.setProperty("eveningPunishment", Integer.toString(eveningPunishment));
			properties.setProperty("samedayPunishment", Integer.toString(samedayPunishment));
			properties.setProperty("twodaysPunishment", Integer.toString(twodaysPunishment));
			properties.setProperty("threedaysPunishment", Integer.toString(threedaysPunishment));
			properties.setProperty("spreadOutPunishment", Integer.toString(spreadOutPunishment));
			properties.setProperty("noOfStudentsPunishment", Integer.toString(noOfStudentsPunishment));
			properties.setProperty("eveningMorningPunishment", Integer.toString(eveningMorningPunishment));
			
			File file = new File("ga_parameters.properties");
			FileOutputStream fileOut = new FileOutputStream(file);
			properties.store(fileOut, "GA Parameters");
			fileOut.close();
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
