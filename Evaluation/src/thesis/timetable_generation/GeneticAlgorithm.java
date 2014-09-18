package thesis.timetable_generation;

import helpers.DateHelper;
import helpers.FileHelper;
import helpers.SQLHelper;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
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

import entities.Exam;

public class GeneticAlgorithm
{	
	// an examIndex can represent more than one exam that has to be scheduled at the same time
	private int noOfExamIndxes; 
	private int noOfTimeslots;
	
	private int noOfDayExams;
	private int noOfEveningExams;
	private int noOfEveningTimeslots;
	private int noOfDayTimeslots;
	
	private double avgExamsInTimeslots;
	private double avgExamsInEveningTimeslots;
	
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
	
	public GeneticAlgorithm(int semester) {
		
		this.datafile = new ReadData(semester);		
	}
	
	public HashMap<Integer, Timeslot> getTimeslotMap() {
		return timeslotMap;
	}
	
	public int getNoOfTimeslots() {
		return noOfTimeslots;
	}
	
	public Chromosome initGA()
	{		
		// get student exams relationship from database
		Connection conn = SQLHelper.getConnection();
		Chromosome chromosome = null;
		
		constructClashesMatrix(conn, true);
		Integer timetable[] = this.datafile.buildTimetable(noOfExamIndxes);
		
    	timeslotMap = FileHelper.getTimeslotMap();
    	gaParameters = FileHelper.getGAParameters();
    	
		generateEvalMatrix(timeslotMap, true);
		calculateAverageExamsInTimeslot();
		
		halfExamPeriod = timeslotMap.get(noOfTimeslots / 2).getStartDateTime();
		
		Chromosome timetableChromosome = new Chromosome(timetable);
		
		try {
			
			chromosome = evaluateChromosome(timetableChromosome);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return chromosome;
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
	    	noOfExamIndxes = indexExamId.keySet().size();
	    	noOfEveningExams = eveningIndexExamId.size();
	    	noOfDayExams = noOfExamIndxes - noOfEveningExams;
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
		FileHelper.saveInputParameters(param);
		
		FileHelper.saveEvalMatrix(evalMatrix);
		return evalMatrix;
		
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
						if (noOfStudents == Constraint.LARGE_SIZED_CLASS)
							noOfStudentsPunish += gaParameters.getNoOfStudentsPunishment();
						
						chromosome.setTotalPunishment(chromosome.getTotalPunishment() + noOfStudentsPunish);
						constraints.setNoOfStudentsPunish(constraints.getNoOfStudentsPunish() + noOfStudentsPunish);
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
	
	public static void main(String[] args)
	{
		Scanner kb = new Scanner(System.in);
		
		try
		{
			int semester = 2;
			Connection conn = SQLHelper.getConnection();
			
			GeneticAlgorithm ga = new GeneticAlgorithm(semester);
			Chromosome chromosome = ga.initGA();
			
			System.out.println("Fitness: " + chromosome.getFitness() + "\n");
			System.out.println("Punishment: " + chromosome.getTotalPunishment() + "\n");
			
			HashMap<ExamMap, ArrayList<String>>[] violationMap = chromosome.getViolationMap();
			
			for (int index = 0; index < violationMap.length; index++)
			{
				HashMap<ExamMap, ArrayList<String>> violationsOfTypeKey = violationMap[index];
				
				if (violationsOfTypeKey != null)
				{
					System.out.println("\n" + Constraint.getViolation(index) + ": \n");
					
					for (Entry<ExamMap, ArrayList<String>> examStudentsViolation: violationsOfTypeKey.entrySet())
					{
						//System.out.println("Exams: " + examStudentsViolation.getKey().toString());
						//System.out.println("Students: " + examStudentsViolation.getValue().toString() + "\n");
											
						ExamMap examMap = examStudentsViolation.getKey();
						
						for (int i=0; i< examMap.getData().length; i++)
						{
							int examID = examMap.getData()[i];
							System.out.print(Exam.getStudyUnitCode(conn, examID) + ", ");
						}
						
						if (index != Constraint.NO_OF_STUDENTS_PUNISH)
						{
							System.out.print(examStudentsViolation.getValue().toString() + " - ");
						}
						
						System.out.print(examStudentsViolation.getValue().size() + " \n");
					}
				}
			}
		}
		finally
		{
			kb.close();
		}
	}
}