package thesis.timetable_generation;

import helpers.FileHelper;
import helpers.SQLHelper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.dhtmlx.planner.DHXEv;
import com.dhtmlx.planner.DHXEvent;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.primitives.Ints;

import entities.StudyUnit;
import entities.TimetableEvent;

public class GeneticAlgorithm 
{
	private int noOfExams;

	private int noOfTimeslots;
	private int noOfChromosomes;
	
	private int clashPunishment;
	private int sameDayPunishment;
	private int twoDaysPunishment;
	private int threeDaysPunishment;
	
	private double crossoverRate;
	private double mutationRate;
	
	private int[][] population;
	private int[][] studentsExams;
	private Table<Integer, Integer, ArrayList<String>> clashesMatrix;

	private double[] fitness;
	private double[] accumulatedFitness;
	
	private ReadData datafile = null;

	private Date startDate = null;
	private Date endDate = null;
	
	private GAParameters gaParameters = null;
	
	long startTime;
	long endTime;
	
	Scanner kb = new Scanner(System.in);
	
	public GeneticAlgorithm() {
		
	}
	
	public GeneticAlgorithm(ReadData datafile, Date startDate, Date endDate) {
		this.datafile = datafile;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public int getNoOfTimeslots() {
		return noOfTimeslots;
	}
	
	public void initGA()
	{
		// set GA parameters used throughout most methods
		gaParameters = datafile.getGAParameters();
		noOfChromosomes = gaParameters.getNoOfChromosomes();
		
		clashPunishment = gaParameters.getClashPunishment();
		sameDayPunishment = gaParameters.getSameDayPunishment();
		twoDaysPunishment = gaParameters.getTwoDaysPunishment();
		threeDaysPunishment = gaParameters.getThreeDaysPunishment();
		
		mutationRate = gaParameters.getMutationRate();
		crossoverRate = gaParameters.getCrossoverRate();
		
		population = new int[noOfChromosomes][noOfExams];
		fitness = new double[noOfChromosomes];
		accumulatedFitness = new double[noOfChromosomes];
		
		// get student exams relationship from database
		Connection conn = SQLHelper.getConnection();
		studentsExams = datafile.getAllStudentExams(conn);
		noOfExams = datafile.getNoOfExams(conn);
		
		clashesMatrix = HashBasedTable.create();
		TreeMap<Integer, String> studentIDMap = datafile.getStudentIDMap();
    	TreeMap<Integer, Integer> indexStudentID = datafile.getIndexStudentID();
		
		// fill up matrix M[i][j] with each value representing students in common for exam i and exam j
		// symmetrical matrix
		for(int i=0; i < studentsExams.length; i++)
	  	{
        	String studentInCommon = studentIDMap.get(ReadData.getKeyByValue(indexStudentID, i));
			
		  	for(int j=0; j < studentsExams[i].length; j++)
		  	{ 
                for(int k=0; k < studentsExams[i].length; k++)
                {	
                    if(j != k)
                    {
                    	int exam1 = studentsExams[i][j];
                    	int exam2 = studentsExams[i][k];
                    	
                    	ArrayList<String> studentIds = clashesMatrix.get(exam1, exam2);
                    	
                    	if (studentIds == null)
                    	{
                    		studentIds = new ArrayList<String>();
	                    	studentIds.add(studentInCommon);
	                    	clashesMatrix.put(exam1, exam2, studentIds);
                    	}
                    	else
                    	{
                    		clashesMatrix.get(exam1, exam2).add(studentInCommon);
                    	}
                    }
                }
		  	}
	  	}
		
		saveClashesMatrix(clashesMatrix);
		saveGAParameters(gaParameters);
		
		TreeMap<Integer, Timeslot> timeslotMap = getTimeslotMap();
		noOfTimeslots = timeslotMap.size();
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
		
		GAParameters gaParameters = getGAParameters();
		// for the number of generations, calculate fitnesses and
		// produce offspring for next generation
		for (int i = 0; i < gaParameters.getNoOfGenerations(); i++)
		{	
			examineGenerationFitness(i);
			
			int interpolatingRates = gaParameters.getInterpolatingRates();
			
			if (interpolatingRates == GAParameters.InterpolatingRates.INTERPOLATING_RATES)
			{
				double minCrossoverRate = gaParameters.getMinCrossoverRate();
				double maxMutationRate = gaParameters.getMinCrossoverRate();
				double stepValue = gaParameters.getStepValue();
				
				if (crossoverRate > minCrossoverRate)
					crossoverRate -= stepValue;
					
				if (mutationRate < maxMutationRate)
					mutationRate += stepValue;
			}
			
			selectionAndReproduction();
		}
		
		endTime = System.currentTimeMillis();
		System.out.println("\nRun time: "+ ((endTime - startTime) / 1000) + " seconds"); 
		
		int[] timetable = population[getFitTimetable()];
		saveGeneratedTimetable(timetable);
		
		return timetable;
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
	
	// Get random position for crossover [0, noOfExams - 1]
	public int getRandomPosition()
	{
		Random rand = new Random(); 
		return rand.nextInt(noOfExams - 1);
	}

	// Get random gene for building chromosome [1, noOfTimeSlots]
	public int generateRandomGene()
	{
		Random rand = new Random(); 
		return rand.nextInt(noOfTimeslots);
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
	
	public Chromosome evaluateChromosome(int[] chrom, GAParameters gaParam, Table<Integer,Integer,ArrayList<String>> clashMatrix)
	{
		TreeMap<Integer, Timeslot> timeslotMap = getTimeslotMap();
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");
		
		if (clashMatrix == null)
			clashMatrix = clashesMatrix;
		
		if (gaParam == null)
			gaParam = gaParameters;
		
		Chromosome chromosome = new Chromosome(chrom);
		
		// for all exams 
		for (int j=0; j < chrom.length; j++)
		{
			// get timeslot and find its date and time in timetable
			int exam1_timeslot = chrom[j];
			
			Timeslot exam1Timeslot = timeslotMap.get(exam1_timeslot);  
			String exam1StartStr = exam1Timeslot.getStartDate();
			String exam1EndStr = exam1Timeslot.getEndDate();
			
			DateTime exam1Start = formatter.parseDateTime(exam1StartStr);
			DateTime exam1End = formatter.parseDateTime(exam1EndStr);
			
			for (int k=0; k < chrom.length; k++)
			{
				// if not the same exam
				if (j != k)
				{	
					ArrayList<String> studentsInExam1And2 = clashMatrix.get(j, k);
					
					// if there are students in common for both exams i and j 
					if (studentsInExam1And2 != null)
					{
						// comparing exam j in chromosome with exam k and checking if they
						// are scheduled in the same timeslot
						int exam2_timeslot = chrom[k];
						
						// get timeslot 2 date and time
						Timeslot exam2Timeslot = timeslotMap.get(exam2_timeslot);  
						String exam2StartStr = exam2Timeslot.getStartDate();
						String exam2EndStr = exam2Timeslot.getEndDate();
						
						DateTime exam2Start = formatter.parseDateTime(exam2StartStr);
						DateTime exam2End = formatter.parseDateTime(exam2EndStr);
						
						Interval interval = new Interval(exam1Start, exam1End);
						Interval interval2 = new Interval(exam2Start, exam2End);

						boolean overlaps = interval.overlaps(interval2);

						// if scheduled in the same timeslot or overlapping times
						// and clash not already noted for, increase clash punishment

						Table<Integer, Integer, Boolean> clashes = chromosome.getClashesPunished();
						
						if ((exam1_timeslot == exam2_timeslot || overlaps)  && clashes.get(j, k) == null)
						{	
							clashes.put(j, k, true); // set exam j & k to true - i.e. already checked clash
							chromosome.setClashesPunished(clashes);
							chromosome.setTotalPunishment(chromosome.getTotalPunishment() + clashPunishment);
							chromosome.setClashPunish(chromosome.getClashPunish() + 1);
						}
						
						// if not in same timeslot, check for soft constraints
						else
						{
							boolean before = exam1Start.isBefore(exam2Start);
							Days days = null;
							
							if (before) days = Days.daysBetween(exam1Start, exam2Start);
							else days = Days.daysBetween(exam2Start, exam1Start);
												
							// calculate days between exam 1 and exam 2
							int daysInBetween = days.getDays();
							
							// if those exams are scheduled on the same day, increase punishment
							Table<Integer, Integer, Boolean> sameday = chromosome.getSamedayPunished();
							Table<Integer, Integer, Boolean> twodays = chromosome.getTwodaysPunished();
							
							if (daysInBetween == 0 && sameday.get(j, k) == null)
							{
								sameday.put(j, k, true); // set exam j & k to true - i.e. already checked same day constraint
								chromosome.setSamedayPunished(sameday);
								chromosome.setTotalPunishment(chromosome.getTotalPunishment() + sameDayPunishment);
								chromosome.setSameDayPunish(chromosome.getSameDayPunish() + 1);
							}
							
							// if those exams are scheduled on two consecutive days
							else if (daysInBetween == 1 && twodays.get(j, k) == null)
							{
								twodays.put(j, k, true); // set exam j & k to true - i.e. already checked two consecutive day constraint
								chromosome.setTwodaysPunished(twodays);
								chromosome.setTotalPunishment(chromosome.getTotalPunishment() + twoDaysPunishment);
								chromosome.setTwoDayPunish(chromosome.getTwoDayPunish() + 1);
								
								// get time between exam 1 and 2
								Hours timeBetweenExam1And2 = Hours.hoursBetween(exam1End, exam2Start);
								Hours twentyHrs = Hours.EIGHT.multipliedBy(2).plus(4);
								
								// on two consecutive days - one in the evening, one in the next morning
								// more serious therefore punish more
								if (timeBetweenExam1And2.isLessThan(twentyHrs))
								{
									chromosome.setTotalPunishment(chromosome.getTotalPunishment() + 2);
								}
								
								// check for a third exam scheduled after these two
								for (int l = 0; l < chrom.length; l++)
								{
									// if k and l are not the same exam
									if (k != l)
									{
										// if there are students in common for both exams j and k 
										ArrayList<String> studentsInExam2And3 = clashMatrix.get(k, l);
										
										if (studentsInExam2And3 != null)
										{
											// check if students in exam 2 and 3 are also the same students in exam 1 and 2
											ArrayList<String> studentsInExam1And2And3 = new ArrayList<String>(studentsInExam2And3);
											studentsInExam1And2And3.retainAll(studentsInExam1And2);
											
											// if there are students who have exam 1 and 2 which are
											// already consecutive and exam 3 as well
											if (studentsInExam1And2And3.size() > 0)
											{
												// get 3rd timeslot date time
												int exam3_timeslot = chrom[l];
												String exam3StartStr = timeslotMap.get(exam3_timeslot).getStartDate();
												DateTime exam3Start = formatter.parseDateTime(exam3StartStr);
												
												boolean beforeThird = exam2Start.isBefore(exam3Start);
												Days days2 = null;
												
												if (beforeThird) days2 = Days.daysBetween(exam2Start, exam3Start);
												else days2 = Days.daysBetween(exam3Start, exam2Start);
		
												// get days between 2nd and 3rd exam
												daysInBetween = days2.getDays();
												
												// if days between third and second exam is 1 .. you have 3 consecutive exams
												// for the same student, therefore increase three day punish
												if (daysInBetween == 1)
												{
													chromosome.setTotalPunishment(chromosome.getTotalPunishment() + threeDaysPunishment);
													chromosome.setThreeDayPunish(chromosome.getThreeDayPunish() + 1);
												}			
											}
										} // end if students in exam 2 and 3
									} // end for loop l
								} // end if k != l
							} // end if two consecutive exams
						} // end if overlaps or finished checking soft constraints
					} // end if students in exams 1 and 2
				} // end if j != k
			} // end of for loop k 
		} // end of for loop j
		
		BigDecimal fitnessValue = null;
		int punishment = chromosome.getTotalPunishment();		
		
		int fitnessFunction = gaParam.getInverseSquarePressure();
			
		switch (fitnessFunction)
		{
			// inverse square pressure: 1.0 / 1.0 + (punishment * punishment)
			case GAParameters.FitnessFunction.INVERSE_SQUARE_PRESSURE: 
				 fitnessValue = new BigDecimal(1.0 / (double) (1.0 + Math.pow(punishment, 2)));
				 break;
				 
			// fitness function: 1.0 / (1.0 + punishment) because of division by 0.
			case GAParameters.FitnessFunction.INVERSE_FUNCTION:
				 fitnessValue = new BigDecimal(1.0 / (double) (1.0 + punishment));
			
			default: break;
		}
		
		fitnessValue.setScale(6, BigDecimal.ROUND_HALF_UP);
		chromosome.setFitness(fitnessValue.doubleValue());
		
		return chromosome;
	}
	
	// give punishments for each broken constraint for each chromosome
	// and finally fill up fitness array with fitness function objective
	// value for each chromosome
	
	// fitness function: 1 / (1 + punishment) therefore, fitness = 1, no broken constraints
	public void examineGenerationFitness(int generationNo)
	{
		int minConflicts = 0, minClashes = -1, minSameDay = -1, minTwoDays = -1, minThreeDays = -1;
		double maxFitness = 0, totalFitness = 0;
		
		/*System.out.print("\nGENERATION " + (generationNo + 1) + ":\n ");
		
		System.out.printf("%n %5s | %25s | %8s | %-310s | %8s %n", "Index", "Errors[HC][SC1][SC2][SC3]", "Fitness", "Chromosomes", "Acc. Fitness");
		
		for (int i=0; i < 380; i++){
			System.out.print("-");
		}
		
		System.out.print("\n"); */
		
		// for each chromosome / solution
		for (int i=0; i < population.length; i++)
		{ 
			// examine chromosome and return punishment + fitness information
			Chromosome chromosome = evaluateChromosome(population[i], null, null);	
			fitness[i] = chromosome.getFitness();
			totalFitness += fitness[i];
			
			int punishment = chromosome.getTotalPunishment();
			if (minConflicts == 0 || punishment < minConflicts) minConflicts = punishment;
			
			// minimum punishments for each constraint
			int clashPunish = chromosome.getClashPunish();
			if (minClashes == -1 || clashPunish < minClashes) minClashes = clashPunish;
			
			int samedayPunish = chromosome.getSameDayPunish();
			if (minSameDay == -1 || samedayPunish < minSameDay) minSameDay = samedayPunish;
			
			int twodayPunish = chromosome.getTwoDayPunish();
			if (minTwoDays == -1 || twodayPunish < minTwoDays) minTwoDays = twodayPunish;
			
			int threedayPunish = chromosome.getThreeDayPunish();
			if (minThreeDays == -1 || threedayPunish < minThreeDays) minThreeDays = threedayPunish;
			
			if (fitness[i] > maxFitness) maxFitness = fitness[i];
			
			if (i == 0) accumulatedFitness[i] = fitness[i];
			else accumulatedFitness[i] = new BigDecimal(fitness[i] + accumulatedFitness[i - 1]).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
			
			/*System.out.printf("%6s | %25s | %.6f | %-310s | %.6f %n", 
							(i+1) + ".",  
							punishment + " [" + clashPunish + "][" + samedayPunish + "][" + twodayPunish + "][" + threedayPunish + "]" ,
							fitness[i],
							Arrays.toString(population[i]).replace("[", "").replace("]", "") + "",
							accumulatedFitness[i]
			);*/
		
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
	
	// Select parents by Roulette Wheel Selection
	
	// If within crossover rate, perform one point crossover
	// else copy parents from current population
	
	// After, for each gene generate a random number
	// If within mutation rate, mutate bit with a random gene [1, noOfTimeSlots]
	public void selectionAndReproduction()
	{
		int[][] nextPopulation = new int[noOfChromosomes][noOfExams];
		int chromosomeNo = 0;
		
		// Elitist Selection - survive best parent from current generation
		int elitistSelection = gaParameters.getElitistSelection();
		
		if (elitistSelection == GAParameters.ElitistSelection.ELITIST_SELECTION)
		{
			int survivor = surviveBestParent();
			nextPopulation[chromosomeNo] = population[survivor];
			chromosomeNo += 1;
		}
		
		// Introduction of new chromosomes - 1/4 of population
		int randomIntroduction = gaParameters.getRandomIntroduction();
		
		if (randomIntroduction == GAParameters.RandomIntroduction.RANDOM_INTRODUCTION)
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
				int crossoverType = gaParameters.getCrossoverType();
				switch(crossoverType)
				{
					case GAParameters.CrossoverType.ONEPOINT: children = onePointCrossover(parent1, parent2); break;
					case GAParameters.CrossoverType.TWOPOINT: children = twoPointCrossover(parent1, parent2); break;
					case GAParameters.CrossoverType.UNIFORM: children = uniformCrossover(parent1, parent2); break;
					
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
	
	public void saveGAParameters(GAParameters gaParameters)
	{
		ObjectOutputStream outputStream = null;
		
		try
		{
			outputStream = FileHelper.getObjectWriter("GAParameters.data");
			outputStream.writeObject(gaParameters);
		}
		catch (IOException e)
		{
			System.out.println("[GeneticAlgorithm.saveGAParameters()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeOutputStream(outputStream);
		}
	}
	
	public static GAParameters getGAParameters()
	{
		ObjectInputStream inputStream = null;
		GAParameters gaParameters = null;
		
		try
		{
			inputStream = FileHelper.getObjectReader("GAParameters.data");
			gaParameters = (GAParameters) inputStream.readObject();
		}
		catch (Exception e)
		{
			System.out.println("[GeneticAlgorithm.getGAParameters()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeInputStream(inputStream);
		}
		return gaParameters;
	}
	
	public void saveGeneratedTimetable(int[] timetable)
	{
		ObjectOutputStream outputStream = null;
		
		try
		{
			outputStream = FileHelper.getObjectWriter("timetable.data");
			outputStream.writeObject(timetable);
		}
		catch (IOException e)
		{
			System.out.println("[GeneticAlgorithm.saveGeneratedTimetable()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeOutputStream(outputStream);
		}
	}
	
	public static int[] getTimetable()
	{
		ObjectInputStream inputStream = null;
		int[] timetable = null;
		
		try
		{
			inputStream = FileHelper.getObjectReader("timetable.data");
			timetable = (int[]) inputStream.readObject();
		}
		catch (Exception e)
		{
			System.out.println("[GeneticAlgorithm.getTimetable()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeInputStream(inputStream);
		}
		return timetable;
	}
	
	public void saveTimeslotMap(TreeMap<Integer, Timeslot> timeslotMap)
	{
		ObjectOutputStream outputStream = null;
		
		try
		{
			outputStream = FileHelper.getObjectWriter("timeslotMap.data");
			outputStream.writeObject(timeslotMap);
		}
		catch (IOException e)
		{
			System.out.println("[GeneticAlgorithm.saveTimeslotMap()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeOutputStream(outputStream);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Table<Integer, Integer, ArrayList<String>> getClashesMatrix()
	{
		ObjectInputStream inputStream = null;
		Table<Integer, Integer, ArrayList<String>> clashesMatrix = null;
		
		try
		{
			inputStream = FileHelper.getObjectReader("clashesMatrix.data");
			clashesMatrix = (Table<Integer, Integer, ArrayList<String>>) inputStream.readObject();
		}
		catch (Exception e)
		{
			System.out.println("[GeneticAlgorithm.getClashesMatrix()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeInputStream(inputStream);
		}
		return clashesMatrix;
	}
	
	public void saveClashesMatrix(Table<Integer, Integer, ArrayList<String>> clashesMatrix)
	{
		ObjectOutputStream outputStream = null;
		
		try
		{
			outputStream = FileHelper.getObjectWriter("clashesMatrix.data");
			outputStream.writeObject(clashesMatrix);
		}
		catch (IOException e)
		{
			System.out.println("[GeneticAlgorithm.saveClashesMatrix()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeOutputStream(outputStream);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static TreeMap<Integer, Timeslot> getTimeslotMap()
	{
		ObjectInputStream inputStream = null;
		TreeMap<Integer, Timeslot> timeslotMap = null;
		
		try
		{
			inputStream = FileHelper.getObjectReader("timeslotMap.data");
			timeslotMap = (TreeMap<Integer, Timeslot>) inputStream.readObject();
		}
		catch (Exception e)
		{
			System.out.println("[GeneticAlgorithm.getTimetable()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeInputStream(inputStream);
		}
		return timeslotMap;
	}

	public Timeslot buildTimeslot(HttpServletRequest request, String thisDay, int timeslotNo, int dayOfWeek)
	{
    	String tsName = null;
    	
    	if (dayOfWeek == Calendar.SATURDAY)
    		tsName = "ts_s" + timeslotNo;
    	else
    		tsName = "ts_w" + timeslotNo;
    		
    	String tsStarttime = request.getParameter(tsName + "_starttime");
		String tsEndtime = request.getParameter(tsName + "_endtime");
		
		String startTimeslotStr = thisDay + " " + tsStarttime;
		String endTimeslotStr = thisDay + " " + tsEndtime;
		
		Timeslot timeslot = new Timeslot(startTimeslotStr, endTimeslotStr);
		return timeslot;
	}

	public TreeMap<Integer, Timeslot> setTimeslotMap(HttpServletRequest request, Calendar c, InputParameters param)
	{
		TreeMap<Integer, Timeslot> timeslotMap = new TreeMap<Integer, Timeslot>();
		
		Days days = Days.daysBetween(new DateTime(startDate), new DateTime(endDate));
		int daysInBetween = days.getDays();
		
		int count = 0;
		for (int i=0; i < (daysInBetween + 1); i++)
		{
			c.add(Calendar.DATE, i);
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			
			String thisDay = sdf.format(c.getTime());
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			boolean includeSats = param.includeSaturdays();
			
			if (dayOfWeek == Calendar.SUNDAY || (dayOfWeek == Calendar.SATURDAY && !includeSats))
			{	
				c.setTime(startDate);
				continue;
			}
			
			int noOfTimeslots = 0;
			
			if (param.isSameWeekdays())
			{
				if (dayOfWeek == Calendar.SATURDAY)
					noOfTimeslots = param.getSaturdayTimeslots();
				else
					noOfTimeslots = param.getWeekdayTimeslots();
			}
			else
			{					
				switch (dayOfWeek)
				{
					case Calendar.MONDAY: noOfTimeslots = param.getMondayTimeslots(); break;
					case Calendar.TUESDAY: noOfTimeslots =  param.getTuesdayTimeslots(); break;
					case Calendar.WEDNESDAY: noOfTimeslots = param.getWednesdayTimeslots(); break;
					case Calendar.THURSDAY: noOfTimeslots = param.getThursdayTimeslots(); break;
					case Calendar.FRIDAY: noOfTimeslots = param.getFridayTimeslots(); break;
					case Calendar.SATURDAY: noOfTimeslots = param.getSaturdayTimeslots(); break;
				}
			}
				
			for (int j=0; j < noOfTimeslots; j++)
			{
				int timeslotNo = count;
				count++;
				
				Timeslot timeslot = buildTimeslot(request, thisDay, j+1, dayOfWeek);
				timeslotMap.put(timeslotNo, timeslot);
			}
			
			c.setTime(startDate);
		}
		
		return timeslotMap;
	}
	
	public void moveEvent()
	{
		// timeslotMap ha jkun mapped bit timeslot li eda fih mad data fit timetable
		// 30 -- 26/01/2013 09:00 - 26/01/2013 12:00
		
		// issa dan jista jcaqlaqli ezami fejn irid.. jista jkun li lanqas biss eda fil
		// hinijiet imnizzlin
		
		// trid ticcekja li l post fejn caqlaqta ma hemmx dati mit timeslot map joverlappjawa
		// jekk emm: iccekja li l ezamijiet li ghandek f dik it timeslot ma jicclashjawx ma
		// l ezami li caqlaqt
		
		
		
	}
	
	public void insertTimetableEvents(int[] bestTimetable)
	{	
		// exams - positions, timeslots - values
		ArrayList<StudyUnit> exams = datafile.getStudyUnits();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		TreeMap<Integer, Timeslot> timeslotMap = getTimeslotMap();
		
		// loop the timetable chosen by GA
		for (int i=0; i < bestTimetable.length; i++)
		{
			int timeslotNo = bestTimetable[i];
			int examNo = i;
			StudyUnit studyUnit = exams.get(examNo);
			
			Date start_date = null;
			Date end_date = null;
			
			try
			{
				start_date = sdf.parse(timeslotMap.get(timeslotNo).getStartDate());
				
				// get exam length in format 2.5 and convert to 2hours 30 minutes
				// add the result to start date to find out end time for exam
				double examLength = studyUnit.getExamLength();
				String examLengthStr = Double.toString(examLength);
				
				int hours = Integer.parseInt(examLengthStr.substring(0, examLengthStr.indexOf(".")));
				
				String string1 = "0." + examLengthStr.substring(examLengthStr.indexOf(".") + 1);
				double number = Double.parseDouble(string1);
				
				int minutes = (int) (number * 60);
				
				DateTime endTime = new DateTime(start_date);
				endTime = endTime.plusHours(hours);
				endTime = endTime.plusMinutes(minutes);
				
				end_date = endTime.toDate();
			}
			catch (ParseException e)
			{
				System.out.println("[GeneticAlgorithm.insertTimetableEvents()]: " + e.getMessage());
				e.printStackTrace();
			}
			
		    DHXEv event = new DHXEvent();
		    event.setStart_date(start_date);
		    event.setEnd_date(end_date);
		    event.setText(studyUnit.getUnitCode());
		    
		    PreparedStatement pstmt = null;
		    Connection conn = SQLHelper.getConnection();
		    
		    try
		    {
		    	TreeMap<Integer, Integer> indexExamID = ReadData.getIndexExamId();
		    	
		    	int studyUnitID = ReadData.getKeyByValue(indexExamID, examNo);
		    	pstmt = TimetableEvent.insertEvent(conn, event, studyUnitID);
		    	
		    	if (pstmt != null) {
	            	pstmt.executeUpdate();
	            }
		    }
		    catch (SQLException e)
		    {
				System.out.println("[GeneticAlgorithm.insertTimetableEvents()]: " + e.getMessage());
				e.printStackTrace();
		    }
		}
	}
}
