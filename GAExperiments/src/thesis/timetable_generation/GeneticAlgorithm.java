package thesis.timetable_generation;

import helpers.DateHelper;
import helpers.FileHelper;
import helpers.SQLHelper;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;

public class GeneticAlgorithm
{	
	// an examIndex can represent more than one exam that has to be scheduled at the same time
	private int noOfExamIndexes; 
	private int noOfTimeslots;
	
	private int noOfDayExams;
	private int noOfEveningExams;
	private int noOfEveningTimeslots;
	private int noOfDayTimeslots;
	
	private double avgExamsInTimeslots;
	private double avgExamsInEveningTimeslots;
	
	private int noOfChromosomes;
	private double crossoverRate;
	private double mutationRate;
	
	private Integer[][] population;	
	private double[] fitness;
	private double[] accumulatedFitness;
	
	private static double[] averagePenalties;
	
	private static double[] averageClashes;
	private static double[] averageEvening;
	private static double[] averageSameDay;
	private static double[] averageTwoDays;
	private static double[] averageTwentyHr;
	private static double[] averageThreeDays;
	private static double[] averageMedium;
	private static double[] averageLarge;
	private static double[] averageSpreadOut;
	
	private static double finalPenalties = 0;
	
	private static double finalClashes = 0;
	private static double finalEvening = 0;
	private static double finalSameDay = 0;
	private static double finalTwoDays = 0;
	private static double finalTwentyHr = 0;
	private static double finalThreeDays = 0;
	private static double finalMedium = 0;
	private static double finalLarge = 0;
	private static double finalSpreadOut = 0;
	
	private static final int AVERAGE_RUNS = 10;
	private static final int NO_OF_EXPERIMENTS = 7;
	
	private ReadData datafile = null;
	private GAParameters gaParameters = null;
	
	private HashMap<ExamMap, ArrayList<String>> clashesMatrix = null;
	private HashMap<ExamMap, ArrayList<String>> examUniqueMatrix = null;
	private HashMap<Integer, Timeslot> timeslotMap = null;
	
	private ExamIndexMaps examIndexMaps = null;
	
	private ListMultimap<Integer, Integer> eveningIndexExamId = null;
	private ListMultimap<Integer, Integer> indexExamId = null;
	private Table<Integer, Integer, TemporalDifference> evalMatrix = null;
	
	private DateTime halfExamPeriod = null;
	
	long startTime;
	long endTime;
	
	Scanner kb = new Scanner(System.in);
	
	public GeneticAlgorithm(ReadData datafile) {	
		
		this.datafile = datafile;
	}
	
	public HashMap<Integer, Timeslot> getTimeslotMap() {
		return timeslotMap;
	}

	public HashMap<Integer, Integer> getExamIdIndex() {
		return examIndexMaps.getExamIDIndex();
	}
	
	public int getNoOfTimeslots() {
		return noOfTimeslots;
	}
	
	public void emptyPopulation() 
	{
		// init arrays for population fitness and accumulated fitness
		population = new Integer[noOfChromosomes][noOfExamIndexes];
		fitness = new double[noOfChromosomes];
		accumulatedFitness = new double[noOfChromosomes];
	}
	
	public void initGA(GAParameters params)
	{
		// set GA parameters used throughout most methods
		this.gaParameters = params;
		
		noOfChromosomes = gaParameters.getNoOfChromosomes();
		mutationRate = gaParameters.getMutationRate();
		crossoverRate = gaParameters.getCrossoverRate();
		
		emptyPopulation();
		
		// get student exams relationship from database
		Connection conn = SQLHelper.getConnection();
		constructClashesMatrix(conn, true);
		
    	FileHelper.saveGAParameters(gaParameters);		
    	timeslotMap = FileHelper.getTimeslotMap();
    	
		generateEvalMatrix(timeslotMap, true);
		calculateAverageExamsInTimeslot();
		
		halfExamPeriod = timeslotMap.get(noOfTimeslots / 2).getStartDateTime();
	}
	
	public void calculateAverageExamsInTimeslot()
	{
		this.avgExamsInEveningTimeslots = (noOfEveningExams / noOfEveningTimeslots) * 2.5;
		this.avgExamsInTimeslots =  (noOfDayExams / noOfDayTimeslots) * 2;
	}
	
	public void addMatrixEntry(HashMap<ExamMap, ArrayList<String>> matrix, String student, int... examIdentifier) 
	{
		if (matrix.get(ExamMap.getExamRel(examIdentifier)) == null) {
			ArrayList<String> students = new ArrayList<String>();
			students.add(student);
			matrix.put(ExamMap.getExamRel(examIdentifier), students);
		} else {
			matrix.get(ExamMap.getExamRel(examIdentifier)).add(student);
		}
	}
	
	public void constructClashesMatrix(Connection conn, boolean init) {

		HashMap<String, ArrayList<Pair<Integer, Integer>>> studentsExams = datafile.getStudentExamRel(conn);
		examIndexMaps = FileHelper.getExamIndexMaps();
		indexExamId = examIndexMaps.getIndexExamID();
		
		// Table clashesMatrix entry at i, j -> students in common for exam i and exam j	
		clashesMatrix = new HashMap<ExamMap, ArrayList<String>>();
		examUniqueMatrix = new HashMap<ExamMap, ArrayList<String>>();
		HashMap<ExamMap, Boolean> isAdded = new HashMap<ExamMap, Boolean>();
		
	    for (Entry<String, ArrayList<Pair<Integer, Integer>>> entry : studentsExams.entrySet()) {
	   
	   		String studentId = entry.getKey();
	   		ArrayList<Pair<Integer, Integer>> studyUnits = entry.getValue();
				
			for (Pair<Integer, Integer> firstExamPair: studyUnits) {
				int firstExamIndex = firstExamPair.getLeft();
				int posInList = firstExamPair.getRight();
				
				addMatrixEntry(clashesMatrix, studentId, firstExamIndex);
				int firstExamID = indexExamId.get(firstExamIndex).get(posInList);
				addMatrixEntry(examUniqueMatrix, studentId, firstExamID);
				
				for (Pair<Integer, Integer> secondExamPair: studyUnits) {
					int secondExamIndex = secondExamPair.getLeft();
					int posInList2 = secondExamPair.getRight();
					
					if (firstExamIndex != secondExamIndex && firstExamIndex < secondExamIndex) {
						addMatrixEntry(clashesMatrix, studentId, firstExamIndex, secondExamIndex);
						int secondExamID = indexExamId.get(secondExamIndex).get(posInList2);
						addMatrixEntry(examUniqueMatrix, studentId, firstExamID, secondExamID);
						
						for (Pair<Integer, Integer> thirdExamPair: studyUnits) {
							int thirdExamIndex = thirdExamPair.getLeft();
							int posInList3 = thirdExamPair.getRight();
							ExamMap j_with_k_and_l = ExamMap.getExamRel(firstExamIndex, secondExamIndex, thirdExamIndex);
							
							if (firstExamIndex != thirdExamIndex && secondExamIndex != thirdExamIndex && 
								!j_with_k_and_l.isChecked(isAdded))
							{
								isAdded.put(j_with_k_and_l, true);
								addMatrixEntry(clashesMatrix, studentId, firstExamIndex, secondExamIndex, thirdExamIndex);
								int thirdExamID = indexExamId.get(thirdExamIndex).get(posInList3);
								addMatrixEntry(examUniqueMatrix, studentId, firstExamID, secondExamID, thirdExamID);
							}
						}
					}
				}
			}
	   	}
	    	
    	if (init) {
	    	eveningIndexExamId = examIndexMaps.getEveningIndexExamID();
	    	noOfExamIndexes = indexExamId.keySet().size();
	    	noOfEveningExams = eveningIndexExamId.size();
	    	noOfDayExams = noOfExamIndexes - noOfEveningExams;
    	}
	   
    	FileHelper.saveUniqueExamMatrix(examUniqueMatrix);
	    FileHelper.saveClashesMatrix(clashesMatrix);
	}
		
	// this matrix contains temporal difference between one exam and another in a scheduled
	// timetable
	public Table<Integer, Integer, TemporalDifference> generateEvalMatrix(HashMap<Integer, Timeslot> timeslotMap, boolean init)
	{
		evalMatrix = HashBasedTable.create();
		int noOfTimeslots = timeslotMap.size();
		int noOfEveningTimeslots = 0;
		int noOfDayTimeslots = 0;
		
		for (int i=0; i < noOfTimeslots; i++)
		{
			Timeslot exam1Timeslot = timeslotMap.get(i);			
			Interval interval = DateHelper.getInterval(exam1Timeslot);
			
			if (interval.getStart().getHourOfDay() >= 17)
				noOfEveningTimeslots++;
			
			for (int j=0; j < noOfTimeslots; j++)
			{
				Timeslot exam2Timeslot = timeslotMap.get(j);
				
				Interval interval2 = DateHelper.getInterval(exam2Timeslot);
				boolean overlaps = interval.overlaps(interval2);
				int daysInBetween = DateHelper.getDaysBetween(interval, interval2);
				
				boolean eveningMorning = false;
				
				// if timeslot 1 ends after 5PM and timeslot 2 starts before 12AM the next day
				if ((interval.getEnd().getHourOfDay() >= 17 && interval2.getStart().getHourOfDay() <= 12 && interval.getEnd().isBefore(interval2.getStart())) 
					|| (interval2.getEnd().getHourOfDay() >= 17 && interval.getStart().getHourOfDay() <= 12 && interval2.getEnd().isBefore(interval.getStart()))) {
					
					eveningMorning = true;
				}
				
				TemporalDifference tempDiff = new TemporalDifference(overlaps, daysInBetween, eveningMorning);
				evalMatrix.put(i, j, tempDiff);	
			}
		}
		noOfDayTimeslots = noOfTimeslots - noOfEveningTimeslots;
		
		InputParameters param = datafile.getInputParameters();
	
		this.noOfTimeslots = noOfTimeslots;
		this.noOfEveningTimeslots = noOfEveningTimeslots;
		this.noOfDayTimeslots = noOfDayTimeslots;
		
		param.setNoOfEveningTimeslots(noOfEveningTimeslots);
		param.setNoOfDayExams(noOfDayExams);
		param.setNoOfEveningExams(noOfEveningExams);
		param.setNoOfDayTimeslots(noOfDayTimeslots);
		param.setNoOfTimeslots(noOfTimeslots);
		
		FileHelper.saveEvalMatrix(evalMatrix);
		return evalMatrix;
	}
	
	public Chromosome startGeneticAlgorithm()
	{
		long startTime;
		long endTime;
		
		startTime = System.currentTimeMillis();

		// generate first population with random chromosomes
		for (int i = 0; i < noOfChromosomes; i++)
		{
			Integer[] chromosome = createChromosome();
			population[i] = chromosome;
		}
		
		Chromosome bestGenerationChrom = new Chromosome(new Integer[noOfExamIndexes]);
		
		// for the number of generations, calculate fitnesses and
		// produce offspring for next generation
		for (int i = 0; i < gaParameters.getNoOfGenerations(); i++)
		{	
			Chromosome bestChromosome = examineGenerationFitness(i);
			// if the best chromosome of this generation is better than any timetable generated before
			if (bestChromosome.getFitness() > bestGenerationChrom.getFitness())
			{
				// replace best of generations with best chromosome now
				bestGenerationChrom = bestChromosome;
			}
			
			int interpolatingRates = gaParameters.getInterpolatingRates();
			if (interpolatingRates == GAParameters.InterpolatingRates.INTERPOLATING_RATES)
			{
				double minCrossoverRate = gaParameters.getMinCrossoverRate();
				double maxMutationRate = gaParameters.getMaxMutationRate();
				
				if (crossoverRate > minCrossoverRate)
					crossoverRate -= gaParameters.getCrossoverStepValue();
				
				if (mutationRate < maxMutationRate)
					mutationRate += gaParameters.getMutationStepValue();
			}
			selectionAndReproduction();
		}
		
		Constraint constraintsBest = bestGenerationChrom.getConstraintViolations();
		System.out.print("[" + constraintsBest.getClashPunish() + "]" +
						 "[" + constraintsBest.getEveningPunish() + "]" +
						 "[" + constraintsBest.getSameDayPunish() + "]" +
						 "[" + constraintsBest.getEveningMorningPunish() + "]" +
						 "[" + constraintsBest.getTwoDayPunish() + "]" +
						 "[" + constraintsBest.getThreeDayPunish() + "]" +
						 "[" + constraintsBest.getMediumClassPunish() + "]" +
						 "[" + constraintsBest.getLargeClassPunish() + "]" +
						 "[" + constraintsBest.getSpreadOutPunish() + "]\n");
				
		finalPenalties += bestGenerationChrom.getTotalPunishment();
		
		finalClashes += constraintsBest.getClashPunish();
		finalEvening += constraintsBest.getEveningPunish();
		finalSameDay += constraintsBest.getSameDayPunish();
		finalTwoDays += constraintsBest.getTwoDayPunish();
		finalTwentyHr += constraintsBest.getEveningMorningPunish();
		finalThreeDays += constraintsBest.getThreeDayPunish();
		finalMedium += constraintsBest.getMediumClassPunish();
		finalLarge += constraintsBest.getLargeClassPunish();
		finalSpreadOut += constraintsBest.getSpreadOutPunish();
		
		
		endTime = System.currentTimeMillis();
		System.out.println("\nRun time: "+ ((endTime - startTime) / 1000) + " seconds"); 
		
		// save best timetable
		FileHelper.saveBestChromosome(bestGenerationChrom);
		return bestGenerationChrom;
	}
	
	// Get random position for crossover [0, noOfExams - 1]
	public int getRandomPosition()
	{
		Random rand = new Random(); 
		return rand.nextInt(noOfExamIndexes - 1);
	}

	// Get random gene for building chromosome [0, noOfTimeSlots - 1]
	public int generateRandomGene()
	{
		Random rand = new Random(); 
		return rand.nextInt(noOfTimeslots);
	}
	
	// convert two ArrayLists of integers to a 2DArray where two_children[0] = first_child
	// produced by genetic operator and two_children[1] = second_child produced by
	// genetic operators (crossovers + mutation)
	public Integer[][] convertTo2DArray(ArrayList<Integer> child1, ArrayList<Integer> child2)
	{
		Integer[][] two_children = 
		{
			child1.toArray(new Integer[child1.size()]), child2.toArray(new Integer[child2.size()])
		};
		
		return two_children;
	}
	
	// build up chromosome with random genes
	public Integer[] createChromosome()
	{
		Integer[] chromosome = new Integer[noOfExamIndexes];		
		for (int index = 0; index < noOfExamIndexes; index++)
		{
			int gene = generateRandomGene();		
			chromosome[index] = gene;
		}
		return chromosome;
	}
	
	// returns the exam causing the violation and the number of students in class
	public Pair<Integer, Integer> getLargeClass(Chromosome chromosome, int index) 
	{
		List<Integer> examsInThisIndex = indexExamId.get(index);
		Pair<Integer, Integer> examsPair;
		
		for (Integer examId: examsInThisIndex) {
			ArrayList<String> studentsInExam = examUniqueMatrix.get(ExamMap.getExamRel(examId));
			int noOfStudents = studentsInExam.size();
			
			if (noOfStudents >= Constraint.MEDIUM_SIZED_CLASS) {				
				
				if (noOfStudents >= Constraint.LARGE_SIZED_CLASS) {
					examsPair = new ImmutablePair<Integer, Integer>(examId, Constraint.LARGE_SIZED_CLASS);
					return examsPair;
				}
				
				examsPair = new ImmutablePair<Integer, Integer>(examId, Constraint.MEDIUM_SIZED_CLASS);
				return examsPair;
			}
		}
		return null;
	}
	
	// gets which exams should be evening from that index (exam ids)
	public void saveEveningExamViolations(Chromosome chromosome, int index)
	{
		HashMap<Integer, Integer> eveningExamIdIndex = examIndexMaps.getEveningExamIDIndex();
		List<Integer> examsInThisIndex = indexExamId.get(index);
		
		for (Integer examId: examsInThisIndex) {
			if (eveningExamIdIndex.get(examId) != null) {
				ExamMap examViolating = ExamMap.getExamRel(examId);
				ArrayList<String> studentsViolating = examUniqueMatrix.get(examViolating);
				chromosome.saveViolationInfo(Constraint.EVENING_PUNISH, examViolating, studentsViolating);
			}	
		}
	}
	
	public void saveViolation(Chromosome chromosome, int constraintType, int... indexes)
	{
		List<Integer> examsInFirst = indexExamId.get(indexes[0]);
		List<Integer> examsInSecond = indexExamId.get(indexes[1]);
		List<Integer> examsInThird = null;
		
		if (indexes.length == 3) 
			examsInThird = indexExamId.get(indexes[2]);
		
		for (Integer firstExamId: examsInFirst) {
			for (Integer secondExamId: examsInSecond) {
				
				ExamMap examMap = ExamMap.getExamRel(firstExamId, secondExamId);
				ArrayList<String> commonStudents = examUniqueMatrix.get(examMap);
				
				if (commonStudents != null) {
					chromosome.saveViolationInfo(constraintType, examMap, commonStudents);
				}
				
				if (examsInThird != null) {
					for (Integer thirdExamId: examsInThird) {
						
						ExamMap examMap2 = ExamMap.getExamRel(firstExamId, secondExamId, thirdExamId);
						ArrayList<String> commonStudents2 = examUniqueMatrix.get(examMap);
						
						if (commonStudents2 != null) {
							chromosome.saveViolationInfo(constraintType, examMap2, commonStudents);
						}
					}
				}
			}
		}
	}
	
	public Chromosome evaluateChromosome(Chromosome chromosome) throws Exception
	{			
		Integer[] chrom = chromosome.getChromosome();
		chromosome = new Chromosome(chrom);
		
		HashMap<ExamMap, Boolean> isChecked = new HashMap<ExamMap, Boolean>();
		Multiset<Integer> occurenceSet = chromosome.getOccurenceSet();
		Constraint constraints = chromosome.getConstraintViolations();
		
		// for all exams 
		for (int j=0; j < chrom.length; j++)
		{
			// get timeslot and find its date and time in timetable
			int exam1_timeslot = chrom[j];
			
			// if exam still exists
			if (exam1_timeslot != -1)
			{
				// check occurences of this timeslot in the timetable i.e. how many exams scheduled in this timeslot
				int occurences = occurenceSet.count(exam1_timeslot);
				boolean isEvening = eveningIndexExamId.containsKey(j);
				
				if ((isEvening && (occurences > avgExamsInEveningTimeslots)) || (!isEvening && occurences > avgExamsInTimeslots))
				{
					chromosome.setTotalPunishment(chromosome.getTotalPunishment() + gaParameters.getSpreadOutPunishment());
					constraints.setSpreadOutPunish(constraints.getSpreadOutPunish() + gaParameters.getSpreadOutPunishment());
				}
				
				// if this exam has a large number of students and it is scheduled 
				// in the second half of the exam period
				DateTime startTime = timeslotMap.get(exam1_timeslot).getStartDateTime();
				Pair<Integer, Integer> largeClass = getLargeClass(chromosome, j);
				
				if (largeClass != null) {
					
					Integer examId = largeClass.getLeft();
					Integer noOfStudents = largeClass.getRight();
					
					if (noOfStudents >= Constraint.MEDIUM_SIZED_CLASS && startTime.isAfter(halfExamPeriod))
					{
						int noOfStudentsPunish = gaParameters.getNoOfStudentsPunishment();
						// double the penalty if number of students > 60
						if (noOfStudents == Constraint.LARGE_SIZED_CLASS) {
							noOfStudentsPunish += gaParameters.getNoOfStudentsPunishment();
							constraints.setLargeClassPunish(constraints.getLargeClassPunish() + noOfStudentsPunish);
						} else {
							constraints.setMediumClassPunish(constraints.getMediumClassPunish() + noOfStudentsPunish);
						}
						
						chromosome.setTotalPunishment(chromosome.getTotalPunishment() + noOfStudentsPunish);
						
						ExamMap examViolating = ExamMap.getExamRel(examId);
						ArrayList<String> studentsInExam = examUniqueMatrix.get(examViolating);
						chromosome.saveViolationInfo(Constraint.NO_OF_STUDENTS_PUNISH, examViolating, studentsInExam);
					}
				}
				
				// if it is an evening exam
				if (isEvening)
				{
					boolean saturday = (startTime.getDayOfWeek() == DateTimeConstants.SATURDAY);
					int hours = startTime.getHourOfDay();
					
					// if scheduled before 17:00 on a weekday
					if (!saturday && hours < 17)
					{
						chromosome.setTotalPunishment(chromosome.getTotalPunishment() + gaParameters.getEveningPunishment());
						constraints.setEveningPunish(constraints.getEveningPunish() + gaParameters.getEveningPunishment());
						saveEveningExamViolations(chromosome, j);
					}
				}
				
				for (int k=0; k < chrom.length; k++)
				{
					// if not the same exam and not already checked
					if (j != k && j < k)
					{	
						int exam2_timeslot = chrom[k];
						if (exam2_timeslot != -1)
						{
							ExamMap j_with_k = ExamMap.getExamRel(j, k);
							ArrayList<String> studentsInExam1And2 = clashesMatrix.get(j_with_k);
							
							// if there are students in common for both exams i and j 
							if (studentsInExam1And2 != null)
							{
								TemporalDifference tempDiff = evalMatrix.get(exam1_timeslot, exam2_timeslot);
								boolean overlaps = tempDiff.isOverlaps();
								
								// if scheduled in the same timeslot or overlapping times
								// and clash not already noted for, increase clash punishment
								if (exam1_timeslot == exam2_timeslot || overlaps)
								{	
									chromosome.setTotalPunishment(chromosome.getTotalPunishment() + gaParameters.getClashPunishment());
									constraints.setClashPunish(constraints.getClashPunish() + gaParameters.getClashPunishment());
									saveViolation(chromosome, Constraint.CLASH_PUNISH, j, k);
								}
								// if not in same timeslot, check for soft constraints
								else
								{
									// if those exams are scheduled on the same day, increase punishment
									int daysBetween = tempDiff.getDaysBetween();
									
									if (daysBetween == 0)
									{
										chromosome.setTotalPunishment(chromosome.getTotalPunishment() + gaParameters.getSameDayPunishment());
										constraints.setSameDayPunish(constraints.getSameDayPunish() + gaParameters.getSameDayPunishment());
										saveViolation(chromosome, Constraint.SAME_DAY_PUNISH, j, k);
									}
									// if those exams are scheduled on two consecutive days
									else if (daysBetween == 1)
									{
										chromosome.setTotalPunishment(chromosome.getTotalPunishment() + gaParameters.getTwoDaysPunishment());
										constraints.setTwoDayPunish(constraints.getTwoDayPunish() + gaParameters.getTwoDaysPunishment());																		
										saveViolation(chromosome, Constraint.TWO_DAYS_PUNISH, j, k);
										
										// get time between exam 1 and 2
										boolean eveningMorning = tempDiff.isEveningMorning();
										
										// on two consecutive days - one in the evening, one in the next morning
										// more serious therefore punish more
										if (eveningMorning)
										{
											chromosome.setTotalPunishment(chromosome.getTotalPunishment() + gaParameters.getEveningMorningPunishment());
											constraints.setEveningMorningPunish(constraints.getEveningMorningPunish() + gaParameters.getEveningMorningPunishment());
											saveViolation(chromosome, Constraint.DAY_EVENING_PUNISH, j, k);
										}
										
										// check for a third exam scheduled after these two
										for (int l = 0; l < chrom.length; l++)
										{
											// get 3rd timeslot date time
											int exam3_timeslot = chrom[l];
											
											if (exam3_timeslot != -1)
											{
												ExamMap j_with_k_and_l = ExamMap.getExamRel(j, k, l);
												
												// if j, k and l are three different exams
												if (j != l && k != l && !j_with_k_and_l.isChecked(isChecked))
												{
													isChecked.put(j_with_k_and_l, true);
													
													// if there are students in common for both exams j and k 
													ArrayList<String> studentsInExams1_2_3 = clashesMatrix.get(j_with_k_and_l);
													
													if (studentsInExams1_2_3 != null)
													{
														TemporalDifference tempDiff2 = evalMatrix.get(exam1_timeslot, exam3_timeslot);
														TemporalDifference tempDiff3 = evalMatrix.get(exam2_timeslot, exam3_timeslot);
														
														int daysInBetween1And3 = tempDiff2.getDaysBetween();
														int daysInBetween2And3 = tempDiff3.getDaysBetween();
																												
														// if either days between first and third exam is 1 or days between second and third exam 
														// is 1, you have 3 consecutive exams for the same student, therefore increase three day punish
														if ((daysInBetween1And3 == 1 && daysInBetween2And3 == 2) || (daysInBetween1And3 == 2 && daysInBetween2And3 == 1))
														{
															chromosome.setTotalPunishment(chromosome.getTotalPunishment() + gaParameters.getThreeDaysPunishment());
															constraints.setThreeDayPunish(constraints.getThreeDayPunish() + gaParameters.getThreeDaysPunishment());
															saveViolation(chromosome, Constraint.THREE_DAYS_PUNISH, j, k, l);
														}
													}
												}
											} 
										}
									} // exam j and k scheduled on two consecutive days 
								} // check soft constraints between j and k
							} // if students between exam j and k & clashes
						} // if exam k exists
					} // if j != k
				}  // end for k
			} // if exam j exists
		} // end for j
				
		chromosome.setConstraintViolations(constraints);
		
		double fitnessValue = 0.0;
		int punishment = chromosome.getTotalPunishment();		
		int fitnessFunction = gaParameters.getInverseSquarePressure();
			
		switch (fitnessFunction)
		{
			// inverse square pressure: 1.0 / 1.0 + (punishment * punishment)
			case GAParameters.FitnessFunction.INVERSE_SQUARE_PRESSURE: 
				 fitnessValue = 1.0 / (double) (1.0 + Math.pow(punishment, 2));
				 break;
				 
			// fitness function: 1.0 / (1.0 + punishment) because of division by 0.
			case GAParameters.FitnessFunction.INVERSE_FUNCTION:
				 fitnessValue = 1.0 / (double) (1.0 + punishment);
			
			default: break;
		}
		
		chromosome.setFitness(fitnessValue);
		
		return chromosome;
	}
		
	// give punishments for each broken constraint for each chromosome
	// and finally fill up fitness array with fitness function objective
	// value for each chromosome
	Chromosome worseChromosome = null;
	
	// fitness function: 1 / (1 + punishment) therefore, fitness = 1, no broken constraints
	public Chromosome examineGenerationFitness(int generationNo)
	{
		Chromosome bestChromosome = null;
		worseChromosome = null;
		
		int minConflicts = 0; 
		int totalClashes = 0, totalSameDay = 0, totalTwoDays = 0, totalThreeDays = 0, 
			totalEvening = 0, totalMediumClass = 0, totalLargeClass = 0, totalEveningMorning = 0, totalSpreadOut = 0;
		double maxFitness = 0, totalFitness = 0, minFitness = 1.0;
		
		//System.out.print("\nGENERATION " + (generationNo + 1) + ":\n ");
		
		// for each chromosome / solution
		for (int i=0; i < population.length; i++)
		{ 
			// examine chromosome and return punishment + fitness information
			Chromosome chromosome = null;
			try
			{
				chromosome = evaluateChromosome(new Chromosome(population[i]));	
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			int punishment = chromosome.getTotalPunishment();
			if (minConflicts == 0 || punishment < minConflicts) minConflicts = punishment;
			
			Constraint constraintViolations = chromosome.getConstraintViolations();
			// minimum punishments for each constraint
			int clashPunish = constraintViolations.getClashPunish();
			totalClashes += clashPunish;
			
			int samedayPunish = constraintViolations.getSameDayPunish();
			totalSameDay += samedayPunish;

			int eveningMorningPunish = constraintViolations.getEveningMorningPunish();
			totalEveningMorning += eveningMorningPunish;
			
			int twodayPunish = constraintViolations.getTwoDayPunish();
			totalTwoDays += twodayPunish;
			
			int threedayPunish = constraintViolations.getThreeDayPunish();
			totalThreeDays += threedayPunish;
			
			int eveningPunish = constraintViolations.getEveningPunish();
			totalEvening += eveningPunish;
			
			int mediumClassPunish = constraintViolations.getMediumClassPunish();
			totalMediumClass += mediumClassPunish;
			
			int largeClassPunish = constraintViolations.getLargeClassPunish();
			totalLargeClass += largeClassPunish;
			
			int spreadOutPunish = constraintViolations.getSpreadOutPunish();
			totalSpreadOut += spreadOutPunish;
			
			fitness[i] = chromosome.getFitness();
			totalFitness += fitness[i];
			
			if (fitness[i] > maxFitness) 
			{
				maxFitness = fitness[i];
				bestChromosome = chromosome;
			}
			
			if (fitness[i] < minFitness)
			{
				minFitness = fitness[i];
				worseChromosome = chromosome;
			}
			
			if (i == 0) 
				accumulatedFitness[i] = fitness[i];
			else
				accumulatedFitness[i] = fitness[i] + accumulatedFitness[i - 1];
		
		} // end for - i
		
		averagePenalties[generationNo] += minConflicts;
		
		averageClashes[generationNo] += (totalClashes / noOfChromosomes);
		averageEvening[generationNo] += (totalEvening / noOfChromosomes);
		averageSameDay[generationNo] += (totalSameDay / noOfChromosomes);
		averageTwentyHr[generationNo] += (totalEveningMorning / noOfChromosomes);
		averageTwoDays[generationNo] += (totalTwoDays / noOfChromosomes);
		averageThreeDays[generationNo] += (totalThreeDays / noOfChromosomes);
		averageMedium[generationNo] += (totalMediumClass / noOfChromosomes);
		averageLarge[generationNo] += (totalLargeClass / noOfChromosomes);
		averageSpreadOut[generationNo] += (totalSpreadOut / noOfChromosomes);
		
		//System.out.println("\nSummary of Generation " + (generationNo + 1) + ":");
		//System.out.println("Average Fitness = " + totalFitness / noOfChromosomes + "\t\tMin Conficts = " + minConflicts+ "\t\tMax Fitness = " + maxFitness + "\n");
		
		return bestChromosome;
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
		Integer[][] nextPopulation = new Integer[noOfChromosomes][noOfExamIndexes];
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
			
			while (count != Math.floor(noOfChromosomes / 5))
			{
				Integer[] chromosome = createChromosome();
				population[count] = chromosome;
				count++;
			}
		}
	
		while (chromosomeNo < noOfChromosomes)
		{
			// choose two parents by Roulette Wheel Selection
			int parent1 = rouletteWheelSelection();
			int parent2 = rouletteWheelSelection();
			
			while (parent2 == parent1)
			{
				parent2 = rouletteWheelSelection();
			}
			
			double random_crossover = Math.random(); // [0, 1]
			Integer[][] children = new Integer[2][noOfExamIndexes];
			
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
	public Integer[][] onePointCrossover(Integer chromosome1, Integer chromosome2)
	{
		int position = getRandomPosition();
		
		ArrayList<Integer> child1 = new ArrayList<Integer>();
		ArrayList<Integer> child2 = new ArrayList<Integer>();
		
		for (int i=0; i < noOfExamIndexes; i++)
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
	public Integer[][] twoPointCrossover(Integer chromosome1, Integer chromosome2)
	{
		int position1 = getRandomPosition();
		int position2 = getRandomPosition();
		
		if (position2 > position1)
		{
			ArrayList<Integer> child1 = new ArrayList<Integer>();
			ArrayList<Integer> child2 = new ArrayList<Integer>();
	
			for (int i = 0; i < noOfExamIndexes; i++)
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
			Integer[][] two_children = 
			{
				population[chromosome1], population[chromosome2]
			};
			
			return two_children;
		}
	}
	
	// two point crossover method
	public Integer[][] uniformCrossover(int chromosome1, int chromosome2)
	{
		int[] template = new int[noOfExamIndexes];
		
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
				
		Integer[][] two_children = convertTo2DArray(child1, child2);
		return two_children;
	}
	
	// Inversion is a genetic operator where a subset of one chromosome
	// has its order reversed
	public Integer[] inversion(int chromosome)
	{
		int position1 = getRandomPosition();
		int position2 = getRandomPosition();
		
		if (position2 > position1)
		{
			ArrayList<Integer> child = new ArrayList<Integer>();	
			ArrayList<Integer> substr = new ArrayList<Integer>();
			
			for (int i = 0; i < noOfExamIndexes; i++)
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
			return child.toArray(new Integer[child.size()]); // return chromosome with reversed subset
		}
		else return population[chromosome]; // else return chromosome as is
	}
	
	public void calculateAveragePenalties(int noOfGenerations)
	{
		System.out.println("Average Penalties: ");
		for (int l = 0; l < noOfGenerations; l++) {			
			averagePenalties[l] = averagePenalties[l] / AVERAGE_RUNS;
			System.out.print(averagePenalties[l] + ",");
		}
		
		/*
		System.out.println("\n\nClashes: "); 
		for (int l = 0; l < noOfGenerations; l++) {
			averageClashes[l] = averageClashes[l] / AVERAGE_RUNS;
			System.out.print(averageClashes[l] + ",");
		}

		System.out.println("\n\nEvening: ");
		for (int l = 0; l < noOfGenerations; l++) {
			averageEvening[l] = averageEvening[l] / AVERAGE_RUNS;
			System.out.print(averageEvening[l] + ",");
		}

		System.out.println("\n\nAverage Same Day: ");
		for (int l = 0; l < noOfGenerations; l++) {
			averageSameDay[l] = averageSameDay[l] / AVERAGE_RUNS;
			System.out.print(averageSameDay[l] + ",");
		}
			
		System.out.println("\n\nAverage Evening Morning: ");
		for (int l = 0; l < noOfGenerations; l++) {
			averageTwentyHr[l] = averageTwentyHr[l] / AVERAGE_RUNS;
			System.out.print(averageTwentyHr[l] + ",");
		}		

		System.out.println("\n\nAverage Two Days: ");
		for (int l = 0; l < noOfGenerations; l++) {
			averageTwoDays[l] = averageTwoDays[l] / AVERAGE_RUNS;
			System.out.print(averageTwoDays[l] + ",");
		}
		
		System.out.println("\n\nAverage Three Days: ");
		for (int l = 0; l < noOfGenerations; l++) {
			averageThreeDays[l] = averageThreeDays[l] / AVERAGE_RUNS;
			System.out.print(averageThreeDays[l] + ",");
		}
		
		System.out.println("\n\nAverage Medium Class: ");
		for (int l = 0; l < noOfGenerations; l++) {
			averageMedium[l] = averageMedium[l] / AVERAGE_RUNS;
			System.out.print(averageMedium[l] + ",");
		}
		
		System.out.println("\n\nAverage Large Class: ");
		for (int l = 0; l < noOfGenerations; l++) {
			averageLarge[l] = averageLarge[l] / AVERAGE_RUNS;
			System.out.print(averageLarge[l] + ",");
		}
		
		System.out.println("\n\nSpread Out: ");
		for (int l = 0; l < noOfGenerations; l++) {
			averageSpreadOut[l] = averageSpreadOut[l] / AVERAGE_RUNS;
			System.out.print(averageSpreadOut[l] + ",");
		}*/
		System.out.println("\n");
	}
	
	public static void main(String[] args)
	{		
		ReadData datafile = new ReadData();
		
		for (int i=0; i < NO_OF_EXPERIMENTS; i++) {
			String experimentName = "ga" + (i + 1) + "_parameters";
			System.out.println("----------" + experimentName + "---------- \n\n");
			
			GAParameters params = datafile.getGAParameters(experimentName);
			GeneticAlgorithm ga = new GeneticAlgorithm(datafile);		
			ga.initGA(params);
			
			int noOfGenerations = ga.gaParameters.getNoOfGenerations();
			
			// init array for this experiment
			averagePenalties = new double[noOfGenerations];
			
			averageClashes = new double[noOfGenerations];
			averageEvening = new double[noOfGenerations];
			averageSameDay = new double[noOfGenerations];
			averageTwoDays = new double[noOfGenerations];
			averageTwentyHr = new double[noOfGenerations];
			averageThreeDays = new double[noOfGenerations];
			averageMedium = new double[noOfGenerations];
			averageLarge = new double[noOfGenerations];
			averageSpreadOut = new double[noOfGenerations];
			
			for (int j = 0; j < AVERAGE_RUNS; j++) {
				ga.startGeneticAlgorithm();
			}
			
			/*
			finalPenalties = finalPenalties / AVERAGE_RUNS;
			
			finalClashes = finalClashes / AVERAGE_RUNS;
			finalEvening = finalEvening / AVERAGE_RUNS;
			finalSameDay = finalSameDay / AVERAGE_RUNS;
			finalTwoDays = finalTwoDays / AVERAGE_RUNS;
			finalTwentyHr = finalTwentyHr / AVERAGE_RUNS;
			finalThreeDays = finalThreeDays / AVERAGE_RUNS;
			finalMedium = finalMedium / AVERAGE_RUNS;
			finalLarge = finalLarge / AVERAGE_RUNS;
			finalSpreadOut = finalSpreadOut / AVERAGE_RUNS;
			
			System.out.print("\n\n" 
					+ finalClashes + "\t" + finalEvening + "\t" + finalSameDay + "\t"
					+ finalTwentyHr + "\t" + finalTwoDays + "\t" + finalThreeDays + "\t" 
					+ finalMedium + "\t" + finalLarge + "\t" + finalSpreadOut
			);*/
			
			ga.calculateAveragePenalties(noOfGenerations);
		}
	}	
}