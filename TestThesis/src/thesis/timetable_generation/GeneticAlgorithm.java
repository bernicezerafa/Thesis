package thesis.timetable_generation;

import helpers.SQLHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.Days;

import com.dhtmlx.planner.DHXEv;
import com.dhtmlx.planner.DHXEvent;
import com.google.common.primitives.Ints;

import entities.StudyUnit;
import entities.TimetableEvent;

public class GeneticAlgorithm 
{
	private int noOfExams;

	private int noOfTimeSlots;
	private int noOfTimeSlotsPerDay = 3;
	
	private int noOfGenerations;
	private int noOfChromosomes;
	private int crossoverType;
	private double crossoverRate;
	private double mutationRate;
	private int inverseSqPress;
	private int elitistSelection;
	private int randomIntroduction;
	private int interpolatingRates;
	
	private static double minCrossoverRate = 0.2;
	private static double maxMutationRate = 0.8;
	
	private int[][] population;
	private int[][] studentsExams;
	private static int[][] clashesMatrix;

	private final int clashPunishment = 30; // hard constraint violation punishment
	private final int samedayPunishment = 10; // two exams in one day for any student punishment
	private final int twodaysPunishment = 3; // exams in two consecutive days punishment
	private final int threedaysPunishment = 1; // exams in three consecutive days punishment
	
	private double[] fitness;
	private double[] accumulatedFitness;
	
	private ReadData datafile = null;

	private Date startDate;
	
	private static HashMap<Integer, Integer> eventTimeslotMap = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> noOfDaysSummationResults = new HashMap<Integer, Integer>(); 
	ArrayList<Integer> functionResults = new ArrayList<Integer>();
	NavigableSet<Integer> summationResults = new TreeSet<Integer>();
	
	int daysAvailable; // period of time for exams
	int dayOfWeekStartDate;
	int startDayId;
	
	static boolean lastExperiment = false;
	long startTime;
	long endTime;
	
	Scanner kb = new Scanner(System.in);
	
	public GeneticAlgorithm(ReadData datafile, String startDate, int noOfWkDays, int noOfSaturdays) throws ParseException {
		this.datafile = datafile;
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date startDateParsed = sdf.parse(startDate);
		this.startDate = startDateParsed;
		
		Calendar c = Calendar.getInstance();
		c.setTime(startDateParsed);
		dayOfWeekStartDate = c.get(Calendar.DAY_OF_WEEK);
		
		daysAvailable = noOfWkDays + noOfSaturdays;
		noOfTimeSlots = (noOfTimeSlotsPerDay * (noOfWkDays + noOfSaturdays)); //+ (2 * noOfSaturdays);
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

	public int getCrossoverType() {
		return crossoverType;
	}

	public void setCrossoverType(int crossoverType) {
		this.crossoverType = crossoverType;
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

	public int getSamedayPunishment() {
		return samedayPunishment;
	}

	public int getTwodaysPunishment() {
		return twodaysPunishment;
	}

	public int getThreedaysPunishment() {
		return threedaysPunishment;
	}
	
	public int getInverseSqPress() {
		return inverseSqPress;
	}

	public void setInverseSqPress(int inverseSqPress) {
		this.inverseSqPress = inverseSqPress;
	}
	
	public static HashMap<Integer, Integer> getEventTimeslotMap() {
		return eventTimeslotMap;
	}
	
	public static int[][] getClashesMatrix() {
		return clashesMatrix;
	}

	public void initGA()
	{
		Connection conn = SQLHelper.getConnection();
		
		//studyUnits = datafile.getStudyUnitInformation(semester);
		studentsExams = datafile.getAllStudentExams(conn);
		noOfExams = datafile.getNoOfExams(conn);
		
		population = new int[noOfChromosomes][noOfExams];
		fitness = new double[noOfChromosomes];
		accumulatedFitness = new double[noOfChromosomes];
		
		clashesMatrix = new int[noOfExams][noOfExams];
		
		// fill up matrix M[i][j] with each value representing students in common for exam i and exam j
		// symmetrical matrix
		for(int i=0; i < studentsExams.length; i++)
	  	{
		  	for(int j=0; j < studentsExams[i].length; j++)
		  	{ 
                for(int k=0; k < studentsExams[i].length; k++)
                {	
                    if(j != k)
                    	clashesMatrix[studentsExams[i][j]][studentsExams[i][k]]++;
                }
		  	}
	  	}
		
		ObjectOutputStream oos = null;
		
		try
		{
			FileOutputStream fos = new FileOutputStream("C://Users//Bernice//Desktop//clashes_matrix.data");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(clashesMatrix);
		} 
		catch (IOException e)
		{
			System.out.println("Unable to write clashes matrix to file!\n" + e.getMessage());
		}
		finally
		{
			try { oos.close(); } catch (IOException ignore) { }
		}
	}
	
	public int[] runGA()
	{
		long startTime;
		long endTime;
		
		startTime = System.currentTimeMillis();

		// generate first population with random chromosomes
		for (int i = 0; i < noOfChromosomes; i++)
		{
			int[] chromosome = createChromosome();
			population[i] = chromosome;
		}
		
		// for the number of generations, calculate fitnesses and
		// produce offspring for next generation
		for (int i = 0; i < noOfGenerations; i++)
		{	
			try
			{
				examineGenerationFitness(i);
			}
			catch(ParseException e)
			{
				System.out.print(e.getMessage());
			}
			
			if (interpolatingRates == 1)
			{
				if (crossoverRate > minCrossoverRate)
					crossoverRate -= 0.02;
					
				if (mutationRate < maxMutationRate)
					mutationRate += 0.02;
			}
			
			selectionAndReproduction();
		}
		
		endTime = System.currentTimeMillis();
		System.out.println("\nRun time: "+ ((endTime - startTime) / 1000) + " seconds"); 
		
		int[] timetable = population[getFitTimetable()];
		return timetable;
	}
	
	public void generateTimeslotMaps()
	{
		startDayId = 0;
		
		switch (dayOfWeekStartDate)
		{
			case Calendar.MONDAY: startDayId = 1; break;
			case Calendar.TUESDAY: startDayId = 2; break;
			case Calendar.WEDNESDAY: startDayId = 3; break;
			case Calendar.THURSDAY: startDayId = 4; break;
			case Calendar.FRIDAY: startDayId = 5; break;
			case Calendar.SATURDAY: startDayId = 6; break;
		}
		
		int summationResult = 0;
		for (int i = startDayId; i < (daysAvailable + startDayId); i++)
		{			
			int summationFunction = 3 - (2 * (int) Math.abs(Math.cos((Math.PI * i) / 6)));
			summationResult += summationFunction;
			
			functionResults.add(summationFunction);
			summationResults.add(summationResult);
			noOfDaysSummationResults.put(i, summationResult);
		}		
	}
	
	// input timeslot number from chromosome
	public String getExamDate(int timeslotNo, boolean getTime)
	{		
		String examDate = "";
		
		Calendar c = Calendar.getInstance();
		Date startdate = startDate;
		c.setTime(startdate);
		
		String starttime = "";
		
		if (startDayId == Calendar.SATURDAY && timeslotNo == 0 ||
			startDayId != Calendar.SATURDAY && timeslotNo <= 2)
		{
			c.add(Calendar.DATE, timeslotNo);
		}
		else
		{
			int closestValue = summationResults.floor(timeslotNo);
			int n = (ReadData.getKeyByValue(noOfDaysSummationResults, closestValue) - startDayId) + 1;
			
			int remainder = Math.abs(timeslotNo - closestValue);
			
			if (getTime)
				System.out.println(n + " " + remainder);
			
			switch (remainder) 
			{
				case 1: c.add(Calendar.DATE, n);
						starttime = "09:00"; 
						break;
				case 2: c.add(Calendar.DATE, n);
						starttime = "13:00"; 
						break;
				case 0: c.add(Calendar.DATE, (n - 1));
						starttime = "17:00"; 
						break;
			}
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		examDate = sdf.format(c.getTime());
		
		if (getTime)
			examDate = examDate + " " + starttime;
		
		return examDate;
	}
	
	// get the maximum fitness among last generation to produce the timetable
	// to show to the user on screen
	public int getFitTimetable()
	{
		double fit = 0;
		int ind = 0;
		
		for (int i=0; i < noOfChromosomes; i++)
		{
			if (fitness[i] > fit)
			{
				fit = fitness[i];
				ind = i;
			}
		}
		
		return ind; // returns index of chromosome with max fitness
	}
	
	// Get random position for crossover [1, noOfExams - 1]
	public int getRandomPosition()
	{
		Random rand = new Random(); 
		return rand.nextInt(noOfExams - 1) + 1;
	}

	// Get random gene for building chromosome [1, noOfTimeSlots]
	public int generateRandomGene()
	{
		Random rand = new Random(); 
		return rand.nextInt(noOfTimeSlots);
	}
	
	// convert two ArrayLists of integers to a 2DArray where two_children[0] = first_child
	// produced by genetic operator and two_children[1] = second_child produced by
	// genetic operators (crossovers + mutation)
	public int[][] convertTo2DArray(ArrayList<Integer> child1, ArrayList<Integer> child2)
	{
		int[][] two_children = 
		{
			Ints.toArray(child1), Ints.toArray(child2)
		};
		
		return two_children;
	}
	
	// build up chromosome with random genes
	public int[] createChromosome()
	{
		int[] chromosome = new int[noOfExams];
				
		for (int i = 0; i < noOfExams; i++)
		{
			int gene = generateRandomGene();
			chromosome[i] = gene;
		}
		
		return chromosome;
	}

	// check if the clash between exam j and exam k was already punished for or not
	public boolean isAlreadyChecked(ArrayList<String> clashes, int j, int k)
	{
		for (int l = 0; l < clashes.size(); l++)
		{
			if (((Character.getNumericValue(clashes.get(l).charAt(0)) == j) && (Character.getNumericValue(clashes.get(l).charAt(1)) == k)) || 
			((Character.getNumericValue(clashes.get(l).charAt(0)) == k) && (Character.getNumericValue(clashes.get(l).charAt(1)) == j)))
			{
				return true;
			}
		}
		return false;
	}
	
	// give punishments for each broken constraint for each chromosome
	// and finally fill up fitness array with fitness function objective
	// value for each chromosome
	
	// fitness function: 1 / (1 + punishment) therefore, fitness = 1, no broken constraints
	public void examineGenerationFitness(int generationNo) throws ParseException
	{
		int punishment = 0, clashPunish = 0, samedayPunish = 0, twodayPunish = 0, threedayPunish = 0, minConflicts = 0;
		int minClashes = -1, minSameDay = -1, minTwoDays = -1, minThreeDays = -1;
		double maxFitness = 0, totalFitness = 0;
		
		System.out.print("\nGENERATION " + (generationNo + 1) + ":\n ");
		
		System.out.printf("%n %5s | %25s | %8s | %-310s | %8s %n", "Index", "Errors[HC][SC1][SC2][SC3]", "Fitness", "Chromosomes", "Acc. Fitness");
		
		for (int i=0; i < 380; i++){
			System.out.print("-");
		}
		
		System.out.print("\n");
		
		// for each chromosome / solution
		for (int i=0; i < population.length; i++)
		{ 
			punishment = 0;
			ArrayList<String> clashes = new ArrayList<String>();
			ArrayList<String> sameday = new ArrayList<String>();
			ArrayList<String> twodays = new ArrayList<String>();
			
			// for all exams 
			for (int j=0; j < population[i].length; j++)
			{
				int exam1_timeslot = population[i][j];
				String firstexamdate = getExamDate(exam1_timeslot, false);
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");	
				DateTime firstexamdatetime = new DateTime(sdf.parse(firstexamdate));
				
				for (int k=0; k < population[i].length; k++)
				{
					// comparing exam j in chromosome with exam k and checking if they
					// are scheduled in the same timeslot
					
					int exam2_timeslot = population[i][k];
					
					// if not the same exam
					if (j != k)
					{	
						// if scheduled in the same timeslot
						if (exam1_timeslot == exam2_timeslot)
						{
							// if there are more than 0 students in both exams and 
							// the clash was not already noted for, increase punishments
							if (clashesMatrix[j][k] > 0 && !isAlreadyChecked(clashes, j, k))
							{
								clashes.add(Integer.toString(j) + Integer.toString(k));
								punishment += clashPunishment;
								clashPunish++;
							}	
						}
						// if not in same timeslot, check for soft constraints
						else
						{
							int daysInBetween = 0;
							DateTime secondexamdatetime = null;
							
							// if the two exams are taken by at least one same student
							if (clashesMatrix[j][k] > 0)
							{
								// get no of days after startdate				
								
								String secondexamdate = getExamDate(exam2_timeslot, false);
								secondexamdatetime = new DateTime(sdf.parse(secondexamdate));
								
								boolean before = firstexamdatetime.isBefore(secondexamdatetime);
								Days days = null;
								
								if (before) days = Days.daysBetween(firstexamdatetime, secondexamdatetime);
								else days = Days.daysBetween(secondexamdatetime, firstexamdatetime);

								daysInBetween = days.getDays();
								
								// if those exams are scheduled on the same day, increase punishment
								if (firstexamdate.equals(secondexamdate) && !isAlreadyChecked(sameday, j, k))
								{
									sameday.add(Integer.toString(j) + Integer.toString(k));
									punishment += samedayPunishment;
									samedayPunish++;
								}
								// if those exams are scheduled on two consecutive days
								else if (daysInBetween == 1 && !isAlreadyChecked(twodays, j, k))
								{
									twodays.add(Integer.toString(j) + Integer.toString(k));
									punishment += twodaysPunishment;
									twodayPunish++;
									
									/*
									// on two consecutive days - one in the evening, one in the next morning
									// more serious therefore punish more
									if (Math.abs(exam1_timeslot - exam2_timeslot) == 1)
									{
										punishment += 2;
									}*/
									
									// check for a third exam scheduled after these two
									for (int l = 0; l < population[i].length; l++)
									{
										int exam3_timeslot = population[i][l];
										String thirdexamdate = getExamDate(exam3_timeslot, false);
										
										DateTime thirdexamdatetime = new DateTime(sdf.parse(thirdexamdate));
										
										boolean beforeThird = secondexamdatetime.isBefore(thirdexamdatetime);
										Days days2 = null;
										
										if (beforeThird) days2 = Days.daysBetween(secondexamdatetime, thirdexamdatetime);
										else days2 = Days.daysBetween(thirdexamdatetime, secondexamdatetime);

										daysInBetween = days2.getDays();
										
										// clashesMatrix[j][k] > 0 && clashesMatrix[k][l] > 0 
										if (clashesMatrix[k][l] > 0)
										{
											if (daysInBetween == 1)
											{
												punishment += threedaysPunishment;
												threedayPunish++;
											}
										}
									} // end for - l
								} // end if - two consecutive exams
							} // end if - check for clashes
						} // end if - not scheduled in same time slot
					} // end if - not the same exam
				} // end for - k
			}  // end for - j
			
			// calculate fitness for that particular chromosome from added up punishment value
			// fitness function: 1.0 / (1.0 + punishment) because of division by 0.
			// inverse square pressure: 1.0 / 1.0 + (punishment * punishment)
			BigDecimal fitnessValue = null;
			
			if (inverseSqPress == 0) // no inverse square pressure applied
				fitnessValue = new BigDecimal(1.0 / (double) (1.0 + punishment));
			else // else square punishment
				fitnessValue = new BigDecimal(1.0 / (double) (1.0 + Math.pow(punishment, 2)));
			
			fitnessValue.setScale(6, BigDecimal.ROUND_HALF_UP);
			fitness[i] = fitnessValue.doubleValue();
			
			// evaluate average fitness for generation, minimum number of punishments, maximum fitness
			totalFitness += fitness[i];
			if (minConflicts == 0 || punishment < minConflicts) minConflicts = punishment;
			
			// minimum punishments for each constraint
			if (minClashes == -1 || clashPunish < minClashes) minClashes = clashPunish;
			if (minSameDay == -1 || samedayPunish < minSameDay) minSameDay = samedayPunish;
			if (minTwoDays == -1 || twodayPunish < minTwoDays) minTwoDays = twodayPunish;
			if (minThreeDays == -1 || threedayPunish < minThreeDays) minThreeDays = threedayPunish;
			
			if (fitness[i] > maxFitness) maxFitness = fitness[i];
			
			if (i == 0) accumulatedFitness[i] = fitness[i];
			else accumulatedFitness[i] = new BigDecimal(fitness[i] + accumulatedFitness[i - 1]).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
			
			System.out.printf("%6s | %25s | %.6f | %-310s | %.6f %n", 
							(i+1) + ".",  
							punishment + " [" + clashPunish + "][" + samedayPunish + "][" + twodayPunish + "][" + threedayPunish + "]" ,
							fitness[i],
							Arrays.toString(population[i]).replace("[", "").replace("]", "") + "",
							accumulatedFitness[i]
			);
			
			// re-initialize counters for punishments for each chromosome
			clashPunish = 0;
			samedayPunish = 0;
			twodayPunish = 0;
			threedayPunish = 0;
		
		} // end for - i
		
		System.out.println("\nSummary of Generation " + (generationNo + 1) + ":");
		System.out.println("Average Fitness = " + totalFitness / noOfChromosomes + "\t\tMin Conficts = " + minConflicts+ "\t\tMax Fitness = " + maxFitness + "\n");
	}
	
	// select a parent by roulette wheel selection
	public int rouletteWheelSelection()
	{
		// generate random number [0, 1]
		double random = Math.random();
		// multiply by sum of fitnesses
		double multiplied = random * accumulatedFitness[noOfChromosomes - 1];
		
		for (int parent = 0; parent < noOfChromosomes; parent++)
		{
			if (accumulatedFitness[parent] >= multiplied)
			{
				return parent;
			}
		}
		return -1;
	}
	
	// survival of the best parent i.e. with max fitness value
	public int surviveBestParent()
	{
		double max = 0.0;
		int parentPosition = 0;
		
		for (int i = 0; i < noOfChromosomes; i++)
		{
			if (fitness[i] > max)
			{
				max = fitness[i];
				parentPosition = i;
			}
		}
		
		return parentPosition;
	}
	
	// - Select parents by roulette wheel selection
	
	// - If within crossover rate, perform one point crossover
	//   else copy parents from current population
	
	// - After, for each gene generate a random number
	// - If within mutation rate, mutate bit with a random gene [1, noOfTimeSlots]
	public void selectionAndReproduction()
	{
		int[][] nextPopulation = new int[noOfChromosomes][noOfExams];
		int chromosomeNo = 0;
		
		// Elitist Selection - survive best parent from current generation
		if (elitistSelection == 1)
		{
			int survivor = surviveBestParent();
			nextPopulation[chromosomeNo] = population[survivor];
			chromosomeNo += 1;
		}
		
		// Introduction of new chromosomes - 1/4 of population
		if (randomIntroduction == 1)
		{
			int count = 0;
			if (elitistSelection == 1) count = 1;
			
			while (count != Math.floor(noOfChromosomes / 4))
			{
				int[] chromosome = createChromosome();
				population[count] = chromosome;
				count++;
			}
		}
	
		while (chromosomeNo < noOfChromosomes)
		{
			// choose two parent by Roulette Wheel Selection
			int parent1 = rouletteWheelSelection();
			int parent2 = rouletteWheelSelection();
			
			while (parent2 == parent1)
			{
				parent2 = rouletteWheelSelection();
			}
			
			double random_crossover = Math.random(); // [0, 1]
			int[][] children = new int[2][noOfExams];
			
			// if within crossover rate, apply one point crossover
			// else leave parents as they are
			if (random_crossover < crossoverRate)
			{
				switch(crossoverType)
				{
					case 0: children = onePointCrossover(parent1, parent2); break;
					case 1:	children = twoPointCrossover(parent1, parent2); break;
					case 2: children = uniformCrossover(parent1, parent2); break;
					
					default: children = twoPointCrossover(parent1, parent2);
				}
			}
			else
			{
				children[0] = population[parent1];
				children[1] = population[parent2];
			}
			
			// try to mutate bits from both parents
			for (int i = 0; i < children.length; i++)
			{
				for (int j = 0; j < children[i].length; j++)
				{
					// random number between [0, 1]
					double random_mutation = Math.random();
					
					// if less than mutation rate, mutate bit
					if (random_mutation < mutationRate)
					{
						int newGene = generateRandomGene();
						children[i][j] = newGene;
					}
				}
			}
			
			nextPopulation[chromosomeNo] = children[0];
			
			if (chromosomeNo != (noOfChromosomes - 1))
				nextPopulation[chromosomeNo + 1] = children[1];
			else
				break;
			
			chromosomeNo += 2;
		}
		
		// copy next population to current population
		for (int i = 0; i < nextPopulation.length; i++)
		{
			for (int j = 0; j < nextPopulation[i].length; j++)
			{
				population[i][j] = nextPopulation[i][j];
			}
		}
	}
	
	// one point crossover method
	public int[][] onePointCrossover(int chromosome1, int chromosome2)
	{
		// random number for position 1-9
		int position = getRandomPosition();
		
		ArrayList<Integer> child1 = new ArrayList<Integer>();
		ArrayList<Integer> child2 = new ArrayList<Integer>();
		
		for (int i=0; i < noOfExams; i++)
		{
			if (i < position)
			{
				child1.add(population[chromosome1][i]);
				child2.add(population[chromosome2][i]);
			}
			else // i >= position
			{
				// swap bits
				child1.add(population[chromosome2][i]);
				child2.add(population[chromosome1][i]);	
			}
		}
		
		return convertTo2DArray(child1, child2);
	}

	// two point crossover method
	public int[][] twoPointCrossover(int chromosome1, int chromosome2)
	{
		int position1 = getRandomPosition();
		int position2 = getRandomPosition();
		
		if (position2 > position1)
		{
			ArrayList<Integer> child1 = new ArrayList<Integer>();
			ArrayList<Integer> child2 = new ArrayList<Integer>();
	
			for (int i = 0; i < noOfExams; i++)
			{
				if (i < position1 || i >= position2)
				{
					child1.add(population[chromosome1][i]);
					child2.add(population[chromosome2][i]);
				}
				else if (i >= position1 && i < position2)
				{
					child1.add(population[chromosome2][i]);
					child2.add(population[chromosome1][i]);
				}
			}
			
			return convertTo2DArray(child1, child2);
		}
		else
		{
			int[][] two_children = 
			{
				population[chromosome1], population[chromosome2]
			};
			
			return two_children;
		}
	}
	
	// two point crossover method
	public int[][] uniformCrossover(int chromosome1, int chromosome2)
	{
		int[] template = new int[noOfExams];
		
		for (int i=0; i < template.length; i++)
		{
			Random rand = new Random();
			int x = rand.nextInt(1);
			
			template[i] = x;
		}
		
		ArrayList<Integer> child1 = new ArrayList<Integer>();
		ArrayList<Integer> child2 = new ArrayList<Integer>();
		
		for (int i=0; i < template.length; i++)
		{
			if (template[i] == 1)
			{
				// swap bit from parent 1 to parent 2
				child1.add(population[chromosome2][i]);
				child2.add(population[chromosome1][i]);
			}
			else if (template[i] == 0)
			{
				// keep bits in the same chromosome
				child1.add(population[chromosome1][i]);
				child2.add(population[chromosome2][i]);
			}
		}
				
		int[][] two_children = 
		{
			Ints.toArray(child1), Ints.toArray(child2)
		};
		
		return two_children;
	}
	
	// Inversion is a genetic operator where a subset of one chromosome
	// has its order reversed
	public int[] inversion(int chromosome)
	{
		int position1 = getRandomPosition();
		int position2 = getRandomPosition();
		
		if (position2 > position1)
		{
			ArrayList<Integer> child = new ArrayList<Integer>();	
			ArrayList<Integer> substr = new ArrayList<Integer>();
			
			for (int i = 0; i < noOfExams; i++)
			{
				// if less than position1 or greater than position2, copy
				// chromosome bits as is
				if (i < position1 || i > position2)
				{
					child.add(population[chromosome][i]);
				}
				// else if between position1 and position2, add bits to another
				// ArrayList substring so that it can be reversed
				else if (i >= position1 && i < position2)
				{
					substr.add(population[chromosome][i]);
				}
				// else if position2, reverse substr ArrayList and copy bit to
				// child ArrayList
				else if (i == position2)
				{
					Collections.reverse(substr);
					for (int j=0; j < substr.size(); j++)
					{
						child.add(substr.get(j));
					}
					child.add(population[chromosome][i]);
				}
			}
			return Ints.toArray(child); // return chromosome with reversed subset
		}
		else return population[chromosome]; // else return chromosome as is
	}
	
	public void insertTimetableEvents(int[] bestTimetable)
	{	
		// exams - positions, timeslots - values
		ArrayList<StudyUnit> exams = datafile.getStudyUnits();
		
		// loop the timetable chosen by GA
		for (int i=0; i < bestTimetable.length; i++)
		{
			Date start_date = new Date();
			Date end_date = new Date();

			int examNo = i;
			
			try
			{
				String examDate = getExamDate(bestTimetable[i], true);
				
				SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy HH:mm");
				start_date = sdf2.parse(examDate);
			    
				StudyUnit studyUnit = exams.get(examNo);
				double examLength = studyUnit.getExamLength();
				
				Calendar cal = Calendar.getInstance(); // creates calendar
			    cal.setTime(start_date); // sets calendar time/date
			    
			    String examLengthStr = Double.toString(examLength);
			    
			    // add exam length to end date
			    String hours = examLengthStr.substring(0, examLengthStr.indexOf("."));
			    cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(hours)); 
			    
			    String minutes = examLengthStr.substring(examLengthStr.indexOf(".") + 1);
			    
			    switch (Integer.parseInt(minutes))
			    {
			    	case 0: break;
			    	case 5: cal.add(Calendar.MINUTE, 30); break; // if .5 add 30 minutes
			    	case 25: cal.add(Calendar.MINUTE, 15); break; // if .25 add 15 minutes
			    	case 75: cal.add(Calendar.MINUTE, 45); break; // if .75 add 45 minutes
			    }
			    
			    end_date = cal.getTime();
			    
			    DHXEv event = new DHXEvent();
			    event.setStart_date(start_date);
			    event.setEnd_date(end_date);
			    event.setText(studyUnit.getUnitCode());
			    
			    PreparedStatement pstmt = null;
			    ResultSet rs = null;
			    Connection conn = SQLHelper.getConnection();
			    
			    try
			    {
			    	HashMap<Integer, Integer> indexExamID = ReadData.getIndexExamID();
			    	
			    	int studyUnitID = ReadData.getKeyByValue(indexExamID, examNo);
			    	pstmt = TimetableEvent.insertEvent(conn, event, studyUnitID);
			    	
			    	if (pstmt != null) {
		            	pstmt.executeUpdate();
		                rs = pstmt.getGeneratedKeys();
		                
		                if (rs.next()) {
		                	int id = rs.getInt(1);
		                	eventTimeslotMap.put(id, bestTimetable[i]);
		                	event.setId(id);
		                }
		            }
			    }
			    catch (SQLException e)
			    {
					System.out.println("[GeneticAlgorithm.insertTimetableEvents()]: " + e.getMessage());
					e.printStackTrace();
			    }			    
			}
			catch (ParseException e)
			{
				System.out.println("[GeneticAlgorithm.insertTimetableEvents()]: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
