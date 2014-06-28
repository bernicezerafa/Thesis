package thesis.timetable_generation;

import helpers.DateHelper;
import helpers.FileHelper;
import helpers.SQLHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.dhtmlx.planner.DHXEv;
import com.dhtmlx.planner.DHXEvent;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;

import entities.ContemporaneousExams;
import entities.StudyUnit;
import entities.TimetableEvent;

public class GeneticAlgorithm
{
	private int noOfExams;
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
	
	private ReadData datafile = null;
	private GAParameters gaParameters = null;
	
	private HashMap<ExamMap, ArrayList<String>> clashesMatrix = null;
	private HashMap<Integer, Timeslot> timeslotMap = null;
	private HashMap<Integer, Integer> indexExamID = null;
	private HashMap<Integer, Integer> eveningIndexExamID = null;
	private Table<Integer, Integer, TemporalDifference> evalMatrix = null;
	
	private Date startDate = null;
	private Date endDate = null;
	private DateTime halfExamPeriod = null;
	
	private DateTimeFormatter formatter = null;
	
	long startTime;
	long endTime;
	
	Scanner kb = new Scanner(System.in);
	
	public GeneticAlgorithm() {
		
		readObjects();
		
		formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");
		halfExamPeriod = timeslotMap.get(noOfTimeslots / 2).getStartDateTime();
		
		InputParameters param = FileHelper.getInputParameters();
		this.datafile = new ReadData(param.getSemester());
	}
	
	public GeneticAlgorithm(Date startDate, Date endDate, int semester) {
		
		this.datafile = new ReadData(semester);
		this.startDate = startDate;
		this.endDate = endDate;
		
		formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");
	}
	
	public HashMap<Integer, Timeslot> getTimeslotMap() {
		return timeslotMap;
	}

	public HashMap<Integer, Integer> getIndexExamID() {
		return indexExamID;
	}

	public HashMap<Integer, Integer> getEveningIndexExamID() {
		return eveningIndexExamID;
	}
	
	public void readObjects() {
		
		timeslotMap = FileHelper.getTimeslotMap();
		clashesMatrix = FileHelper.getClashesMatrix();
		gaParameters = FileHelper.getGAParameters();
		indexExamID = FileHelper.getIndexExamId();	
		eveningIndexExamID = FileHelper.getEveningIndexExamId();
		evalMatrix = FileHelper.getEvalMatrix();
		
		InputParameters param = FileHelper.getInputParameters();
		noOfTimeslots = param.getNoOfTimeslots();
		noOfDayTimeslots = param.getNoOfDayTimeslots();
		noOfDayExams = param.getNoOfDayExams();
		noOfEveningExams = param.getNoOfEveningExams();
		noOfEveningTimeslots = param.getNoOfEveningTimeslots();
		noOfExams = noOfDayExams + noOfEveningExams;
		
		calculateAverageExamsInTimeslot();
	}
	
	public int getNoOfTimeslots() {
		return noOfTimeslots;
	}
	
	public void initGA()
	{
		// set GA parameters used throughout most methods
		if (gaParameters == null)
			gaParameters = datafile.getGAParameters();
		
		noOfChromosomes = gaParameters.getNoOfChromosomes();
		mutationRate = gaParameters.getMutationRate();
		crossoverRate = gaParameters.getCrossoverRate();
		
		// init arrays for population fitness and accumulated fitness
		population = new Integer[noOfChromosomes][noOfExams];
		fitness = new double[noOfChromosomes];
		accumulatedFitness = new double[noOfChromosomes];
		
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
	
	public void constructClashesMatrix(Connection conn, boolean init)
	{
		HashMap<String, ArrayList<Integer>> studentsExams = datafile.getStudentExamRel(conn);
		
		// Table clashesMatrix entry at i, j -> students in common for exam i and exam j	
		clashesMatrix = new HashMap<ExamMap, ArrayList<String>>();
	
    	for (Entry<String, ArrayList<Integer>> entry : studentsExams.entrySet())
		{
			String studentId = entry.getKey();
			ArrayList<Integer> studyUnits = entry.getValue();
			
			for (Integer firstExamID: studyUnits)
			{					
				if (clashesMatrix.get(ExamMap.getExamRel(firstExamID)) == null)
				{
					ArrayList<String> students = new ArrayList<String>();
					students.add(studentId);
					clashesMatrix.put(ExamMap.getExamRel(firstExamID), students);
					
					if (init)
						noOfExams++;
				}
				else
				{
					clashesMatrix.get(ExamMap.getExamRel(firstExamID)).add(studentId);
				}
				
				for (Integer secondExamID: studyUnits)
				{					
					if (clashesMatrix.get(ExamMap.getExamRel(firstExamID, secondExamID)) == null)
					{
						ArrayList<String> students = new ArrayList<String>();
						students.add(studentId);
						clashesMatrix.put(ExamMap.getExamRel(firstExamID, secondExamID), students);
					}
					else
					{
						clashesMatrix.get(ExamMap.getExamRel(firstExamID, secondExamID)).add(studentId);
					}
					
					for (Integer thirdExamID: studyUnits)
					{						
						if (clashesMatrix.get(ExamMap.getExamRel(firstExamID, secondExamID, thirdExamID)) == null)
						{
							ArrayList<String> students = new ArrayList<String>();
							students.add(studentId);
							clashesMatrix.put(ExamMap.getExamRel(firstExamID, secondExamID, thirdExamID), students);
						}
						else
						{
							clashesMatrix.get(ExamMap.getExamRel(firstExamID, secondExamID, thirdExamID)).add(studentId);
						}
					}
				}
			}
		}
    	
    	if (init)
    	{
	    	eveningIndexExamID = FileHelper.getEveningIndexExamId();
	    	noOfEveningExams = eveningIndexExamID.size();
	    	noOfDayExams = noOfExams - noOfEveningExams;
    	}
    	
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
				if (interval.getEnd().getHourOfDay() >= 17 && interval2.getStart().getHourOfDay() <= 12)
				{
					eveningMorning = true;
				}
				
				TemporalDifference tempDiff = new TemporalDifference(overlaps, daysInBetween, eveningMorning);
				evalMatrix.put(i, j, tempDiff);	
			}
		}
		noOfDayTimeslots = noOfTimeslots - noOfEveningTimeslots;
		
		InputParameters param = FileHelper.getInputParameters();
		if (init)
		{
			this.noOfTimeslots = noOfTimeslots;
			this.noOfEveningTimeslots = noOfEveningTimeslots;
			this.noOfDayTimeslots = noOfTimeslots;
			
			param.setNoOfEveningTimeslots(noOfEveningTimeslots);
			param.setNoOfDayExams(noOfDayExams);
			param.setNoOfEveningExams(noOfEveningExams);
			param.setNoOfDayTimeslots(noOfDayTimeslots);
			param.setNoOfTimeslots(noOfTimeslots);
			
			FileHelper.saveInputParameters(param);
		}
		else
		{
			this.noOfTimeslots = param.getNoOfTimeslots();
			this.noOfEveningTimeslots = param.getNoOfEveningTimeslots();
			this.noOfDayTimeslots = param.getNoOfDayTimeslots();
		}
		
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
		
		Chromosome bestGenerationChrom = new Chromosome(new Integer[noOfExams]);
		
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
				double maxMutationRate = gaParameters.getMinCrossoverRate();
				double stepValue = gaParameters.getStepValue();
				
				if (crossoverRate > minCrossoverRate)
					crossoverRate -= stepValue;
					
				if (mutationRate < maxMutationRate)
					mutationRate += stepValue;
			}
			
			selectionAndReproduction();
		}
		
		System.out.println("Before Post " + bestGenerationChrom.getChromosome());
		System.out.println("Constraints Before " + bestGenerationChrom.getConstraintViolations().toString());
		
		Chromosome finalSchedule = scheduleContemporaneousExams(bestGenerationChrom);
		
		endTime = System.currentTimeMillis();
		System.out.println("\nRun time: "+ ((endTime - startTime) / 1000) + " seconds"); 
		
		// save best timetable
		FileHelper.saveBestChromosome(finalSchedule);
		return finalSchedule;
	}
	
	// after timetable is generated, schedule exams that have to be scheduled together
	public Chromosome scheduleContemporaneousExams(Chromosome bestGenerationChrom)
	{
		Connection conn = SQLHelper.getConnection();
		ArrayList<ExamMap> examsRel = ContemporaneousExams.getAllExamRelationships(conn);
		
		Chromosome bestSoFar = bestGenerationChrom;
		
		// for each exam relationship i.e. exams which have to be scheduled together
		for (ExamMap examRel: examsRel) 
		{	
			int[] examsTogether = examRel.getData(); // get these exams
			double maxFitness = 0.0;
			
			for (int i=0; i < noOfTimeslots; i++)
			{
				// get timetable of chromosome so far (create copy because of reference)
				Integer[] timetable = bestSoFar.getChromosome();
				Integer[] timetableCopy = Arrays.copyOf (timetable, timetable.length);
				
				// modify timetable positions at these exams from 0 to no of timeslot
				// and always keep best timeslot to schedule them in
				for (int j=0; j < examsTogether.length; j++)
				{
					timetableCopy[examsTogether[j]] = i;
				}
				
				Chromosome thisChromosome = null;
				try
				{
					thisChromosome = evaluateChromosome(new Chromosome(timetableCopy));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				double thisFitness = thisChromosome.getFitness();
				
				if (thisFitness > maxFitness)
				{
					maxFitness = thisFitness;
					bestSoFar = bestSoFar.copyChromosome(thisChromosome);
				}
			}
		}
		
		System.out.println("Final Timetable " + bestSoFar.getChromosome());
		System.out.println("Constraints " + bestSoFar.getConstraintViolations().toString());
		
		return bestSoFar;
	}
	
	// Get random position for crossover [0, noOfExams - 1]
	public int getRandomPosition()
	{
		Random rand = new Random(); 
		return rand.nextInt(noOfExams - 1);
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
		Integer[] chromosome = new Integer[noOfExams];
				
		for (int i = 0; i < noOfExams; i++)
		{
			int gene = generateRandomGene();
			chromosome[i] = gene;
		}
		
		return chromosome;
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
				boolean isEvening = eveningIndexExamID.containsValue(j);
				
				ExamMap exam_j = ExamMap.getExamRel(j);
				ArrayList<String> studentsInExam1 = clashesMatrix.get(exam_j);
				
				if ((isEvening && (occurences > avgExamsInEveningTimeslots)) || (!isEvening && occurences > avgExamsInTimeslots))
				{
					chromosome.setTotalPunishment(chromosome.getTotalPunishment() + gaParameters.getSpreadOutPunishment());
					constraints.setSpreadOutPunish(constraints.getSpreadOutPunish() + 1);
				}
				
				DateTime startTime = timeslotMap.get(exam1_timeslot).getStartDateTime();
				
				int numberOfStudents;
				
				if (studentsInExam1 == null) numberOfStudents = 0;
				else numberOfStudents = studentsInExam1.size();
				
				// if this exam has a large number of students and it is scheduled 
				// in the second half of the exam period
				if (numberOfStudents >= 30 && startTime.isAfter(halfExamPeriod))
				{
					int noOfStudentsPunish = gaParameters.getNoOfStudentsPunishment();
					
					// double the penalty if number of students > 60
					if (numberOfStudents >= 60)
						noOfStudentsPunish += gaParameters.getNoOfStudentsPunishment();
					
					chromosome.setTotalPunishment(chromosome.getTotalPunishment() + noOfStudentsPunish);
					constraints.setNoOfStudentsPunish(constraints.getNoOfStudentsPunish() + 1);								
					chromosome.saveViolationInfo(Constraint.NO_OF_STUDENTS_PUNISH, exam_j, studentsInExam1);
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
						constraints.setEveningPunish(constraints.getEveningPunish() + 1);
						chromosome.saveViolationInfo(Constraint.EVENING_PUNISH, exam_j, studentsInExam1);
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
									constraints.setClashPunish(constraints.getClashPunish() + studentsInExam1And2.size());
									chromosome.saveViolationInfo(Constraint.CLASH_PUNISH, j_with_k, studentsInExam1And2);
								}
								// if not in same timeslot, check for soft constraints
								else
								{
									// if those exams are scheduled on the same day, increase punishment
									int daysBetween = tempDiff.getDaysBetween();
									
									if (daysBetween == 0)
									{
										int punishment = gaParameters.getSameDayPunishment() + studentsInExam1And2.size();								
										chromosome.setTotalPunishment(chromosome.getTotalPunishment() + punishment);
										constraints.setSameDayPunish(constraints.getSameDayPunish() + studentsInExam1And2.size());
										chromosome.saveViolationInfo(Constraint.SAME_DAY_PUNISH, j_with_k, studentsInExam1And2);
									}
									// if those exams are scheduled on two consecutive days
									else if (daysBetween == 1)
									{
										int punishment = gaParameters.getTwoDaysPunishment() + studentsInExam1And2.size();
										chromosome.setTotalPunishment(chromosome.getTotalPunishment() + punishment);
										constraints.setTwoDayPunish(constraints.getTwoDayPunish() + studentsInExam1And2.size());								
										chromosome.saveViolationInfo(Constraint.TWO_DAYS_PUNISH, j_with_k, studentsInExam1And2);
										
										// get time between exam 1 and 2
										boolean eveningMorning = tempDiff.isEveningMorning();
										
										// on two consecutive days - one in the evening, one in the next morning
										// more serious therefore punish more
										if (eveningMorning)
										{
											int eveningMorningPunishment = gaParameters.getEveningMorningPunishment() + studentsInExam1And2.size();
											chromosome.setTotalPunishment(chromosome.getTotalPunishment() + eveningMorningPunishment);
											constraints.setEveningMorningPunish(constraints.getEveningMorningPunish() + studentsInExam1And2.size());
											chromosome.saveViolationInfo(Constraint.DAY_EVENING_PUNISH, j_with_k, studentsInExam1And2);	
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
															int punishmentthree = gaParameters.getThreeDaysPunishment() + studentsInExams1_2_3.size();
															chromosome.setTotalPunishment(chromosome.getTotalPunishment() + punishmentthree);
															constraints.setThreeDayPunish(constraints.getThreeDayPunish() + studentsInExams1_2_3.size());
															chromosome.saveViolationInfo(Constraint.THREE_DAYS_PUNISH, j_with_k_and_l, studentsInExams1_2_3);
			
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
	
	// fitness function: 1 / (1 + punishment) therefore, fitness = 1, no broken constraints
	public Chromosome examineGenerationFitness(int generationNo)
	{
		Chromosome bestChromosome = null;
		
		int minConflicts = 0, minClashes = -1, minSameDay = -1, minTwoDays = -1, minThreeDays = -1, 
			minEvening = -1, minSpreadOut = -1, minNoOfStudents = -1, minEveningMorning = -1;
		double maxFitness = 0, totalFitness = 0;
		
		System.out.print("\nGENERATION " + (generationNo + 1) + ":\n ");
		
		//System.out.printf("%n %5s | %45s | %22s | %-320s | %8s %n", "Index", "Errors[HC][HC2][SC1][SC2][SC3][S4]", "Fitness", "Chromosomes", "Acc. Fitness");
		
		//for (int i=0; i < 410; i++){
		//	System.out.print("-");
		//}
		
		//System.out.print("\n");
		
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
			if (minClashes == -1 || clashPunish < minClashes) minClashes = clashPunish;
			
			int samedayPunish = constraintViolations.getSameDayPunish();
			if (minSameDay == -1 || samedayPunish < minSameDay) minSameDay = samedayPunish;
			
			int eveningMorningPunish = constraintViolations.getEveningMorningPunish();
			if (minEveningMorning == -1 || eveningMorningPunish < minEveningMorning) minEveningMorning = eveningMorningPunish;
			
			int twodayPunish = constraintViolations.getTwoDayPunish();
			if (minTwoDays == -1 || twodayPunish < minTwoDays) minTwoDays = twodayPunish;
			
			int threedayPunish = constraintViolations.getThreeDayPunish();
			if (minThreeDays == -1 || threedayPunish < minThreeDays) minThreeDays = threedayPunish;
			
			int eveningPunish = constraintViolations.getEveningPunish();
			if (minEvening == -1 || eveningPunish < minEvening) minEvening = eveningPunish;
			
			int spreadOutPunish = constraintViolations.getSpreadOutPunish();
			if (minSpreadOut == -1 || spreadOutPunish < minSpreadOut) minSpreadOut = spreadOutPunish;
			
			int noOfStudentsPunish = constraintViolations.getNoOfStudentsPunish();
			if (minNoOfStudents == -1 || noOfStudentsPunish < minNoOfStudents) minNoOfStudents = noOfStudentsPunish;
			
			fitness[i] = chromosome.getFitness();
			totalFitness += fitness[i];
			
			if (fitness[i] > maxFitness) 
			{
				maxFitness = fitness[i];
				bestChromosome = chromosome;
			}
			
			if (i == 0) 
				accumulatedFitness[i] = fitness[i];
			else
				accumulatedFitness[i] = fitness[i] + accumulatedFitness[i - 1];
			
			/*System.out.printf("%6s | %45s | %.20f | %-320s | %.6f %n", 
							 (i+1) + ".",  
							 punishment + " [" + clashPunish + "][" + eveningPunish + "][" + samedayPunish + "][" + twodayPunish + "][" + eveningMorningPunish + "][" + threedayPunish + "][" + spreadOutPunish + "][" + noOfStudentsPunish + "]",
							 fitness[i],
							 Arrays.toString(population[i]).replace("[", "").replace("]", "") + "",
							 accumulatedFitness[i]
			);*/
		
		} // end for - i
		
		System.out.println("\nSummary of Generation " + (generationNo + 1) + ":");
		System.out.println("Average Fitness = " + totalFitness / noOfChromosomes + "\t\tMin Conficts = " + minConflicts+ "\t\tMax Fitness = " + maxFitness + "\n");
	
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
		Integer[][] nextPopulation = new Integer[noOfChromosomes][noOfExams];
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
				Integer[] chromosome = createChromosome();
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
			Integer[][] children = new Integer[2][noOfExams];
			
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
	public Integer[][] twoPointCrossover(Integer chromosome1, Integer chromosome2)
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
			return child.toArray(new Integer[child.size()]); // return chromosome with reversed subset
		}
		else return population[chromosome]; // else return chromosome as is
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
		
		DateTime startDateTime = formatter.parseDateTime(startTimeslotStr);
		
		Timeslot timeslot = new Timeslot(startTimeslotStr, endTimeslotStr);
		timeslot.setStartDateTime(startDateTime);
		return timeslot;
	}
	
	public HashMap<Integer, Timeslot> setTimeslotMap(HttpServletRequest request, Calendar c, InputParameters param)
	{
		HashMap<Integer, Timeslot> timeslotMap = new HashMap<Integer, Timeslot>();
		
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
			
			if (dayOfWeek == Calendar.SATURDAY)
				noOfTimeslots = param.getSaturdayTimeslots();
			else
				noOfTimeslots = param.getWeekdayTimeslots();
				
			for (int j=0; j < noOfTimeslots; j++)
			{
				int timeslotNo = count;
				count++;
				
				Timeslot timeslot = buildTimeslot(request, thisDay, j+1, dayOfWeek);
				timeslotMap.put(timeslotNo, timeslot);
			}
			
			c.setTime(startDate);
		}
		
		FileHelper.saveTimeslotMap(timeslotMap);
		return timeslotMap;
	}
	
	public void insertTimetableEvents(Integer[] bestTimetable)
	{	
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		ArrayList<StudyUnit> studyUnits = datafile.getStudyUnits();
		
		// loop the timetable chosen by GA
		for (int i=0; i < bestTimetable.length; i++)
		{
			int timeslotNo = bestTimetable[i];
			StudyUnit studyUnit = studyUnits.get(i);
			
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
		    	pstmt = TimetableEvent.insertEvent(conn, event, studyUnit.getExamID());
		    	
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