package helpers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import thesis.timetable_generation.Chromosome;
import thesis.timetable_generation.ExamIndexMaps;
import thesis.timetable_generation.ExamMap;
import thesis.timetable_generation.GAParameters;
import thesis.timetable_generation.InputParameters;
import thesis.timetable_generation.TemporalDifference;
import thesis.timetable_generation.Timeslot;

import com.google.common.collect.Table;

public class FileHelper {

	public static String getFolderPath()
	{
		File currentFolder = new File(System.getProperty("user.home"));
		File workingFolder = new File(currentFolder, "TimetableStructures");
		File projectFolder = new File(workingFolder, "ExamScheduling");
		
		if (!workingFolder.exists()) {
	         workingFolder.mkdir();
	    }
		if (!projectFolder.exists()) {
			projectFolder.mkdir();
		}
		return projectFolder.getAbsolutePath();
	}
	
	// helper to save objects in a file. return objectoutputstream and write object in respective class
	public static ObjectOutputStream getObjectWriter(String fileName) throws IOException
	{
		ObjectOutputStream outputStream = null;		
		FileOutputStream fileOutputStream = new FileOutputStream(getFolderPath() + File.separator + fileName);
	    BufferedOutputStream buffer = new BufferedOutputStream(fileOutputStream);
		outputStream = new ObjectOutputStream(buffer);
		
		return outputStream;
	}
	
	// helper to close object output stream
	public static void closeOutputStream(ObjectOutputStream outputStream)
	{
		try 
		{ 
			outputStream.close(); 
		} 
		catch (IOException ignore) 
		{ 
			System.out.println("Could not close output stream");
		}
	}
	
	// helper to read objects from a file. return objectinputstream and read object in respective class
	public static ObjectInputStream getObjectReader(String fileName) throws IOException
	{
		ObjectInputStream inputStream = null;
		FileInputStream fileInputStream = new FileInputStream(getFolderPath() + File.separator + fileName);
		BufferedInputStream buffer = new BufferedInputStream(fileInputStream);
		inputStream = new ObjectInputStream(buffer);
		
		return inputStream;	
	}
	
	// helper to close object input stream
	public static void closeInputStream(ObjectInputStream inputStream)
	{
		try 
		{ 
			inputStream.close(); 
		} 
		catch (IOException ignore) 
		{ 
			System.out.println("Could not close input stream");
		}
	}
	
	public static void saveGAParameters(GAParameters gaParameters)
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
	
	public static void saveInputParameters(InputParameters inputParameters)
	{
		ObjectOutputStream outputStream = null;
		
		try
		{
			outputStream = FileHelper.getObjectWriter("InputParameters.data");
			outputStream.writeObject(inputParameters);
		}
		catch (IOException e)
		{
			System.out.println("[GeneticAlgorithm.saveInputParameters()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeOutputStream(outputStream);
		}
	}
	
	public static InputParameters getInputParameters()
	{
		ObjectInputStream inputStream = null;
		InputParameters inputParameters = null;
		
		try
		{
			inputStream = FileHelper.getObjectReader("InputParameters.data");
			inputParameters = (InputParameters) inputStream.readObject();
		}
		catch (Exception e)
		{
			System.out.println("[GeneticAlgorithm.getInputParameters()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeInputStream(inputStream);
		}
		return inputParameters;
	}
	
	public static ExamIndexMaps getExamIndexMaps()
	{
		ObjectInputStream inputStream = null;
		ExamIndexMaps eveningIndexExamID = null;
		
		try
		{
			inputStream = FileHelper.getObjectReader("ExamIndexMaps.data");
			eveningIndexExamID = (ExamIndexMaps) inputStream.readObject();
		}
		catch (Exception e)
		{
			System.out.println("[ReadData.getExamIndexMaps()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeInputStream(inputStream);
		}
		
		return eveningIndexExamID;
	}
	
	public static void saveExamIndexMaps(ExamIndexMaps examIndexMaps)
	{
		ObjectOutputStream outputStream = null;
		
		try
		{
			outputStream = FileHelper.getObjectWriter("ExamIndexMaps.data");
			outputStream.writeObject(examIndexMaps);
		}
		catch (IOException e)
		{
			System.out.println("[ReadData.saveExamIndexMaps()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeOutputStream(outputStream);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap<ExamMap, ArrayList<String>> getClashesMatrix()
	{
		ObjectInputStream inputStream = null;
		HashMap<ExamMap, ArrayList<String>> clashesMatrix = null;
		
		try
		{
			inputStream = FileHelper.getObjectReader("clashesMatrix.data");
			clashesMatrix = (HashMap<ExamMap, ArrayList<String>>) inputStream.readObject();
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
	
	public static void saveClashesMatrix(HashMap<ExamMap, ArrayList<String>> clashesMatrix)
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
	public static HashMap<ExamMap, ArrayList<String>> getUniqueExamMatrix()
	{
		ObjectInputStream inputStream = null;
		HashMap<ExamMap, ArrayList<String>> uniqueExamMatrix = null;
		
		try
		{
			inputStream = FileHelper.getObjectReader("uniqueExamMatrix.data");
			uniqueExamMatrix = (HashMap<ExamMap, ArrayList<String>>) inputStream.readObject();
		}
		catch (Exception e)
		{
			System.out.println("[GeneticAlgorithm.getUniqueExamMatrix()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeInputStream(inputStream);
		}
		return uniqueExamMatrix;
	}
	
	public static void saveUniqueExamMatrix(HashMap<ExamMap, ArrayList<String>> uniqueExamMatrix)
	{
		ObjectOutputStream outputStream = null;
		
		try
		{
			outputStream = FileHelper.getObjectWriter("uniqueExamMatrix.data");
			outputStream.writeObject(uniqueExamMatrix);
		}
		catch (IOException e)
		{
			System.out.println("[GeneticAlgorithm.saveUniqueExamMatrix()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeOutputStream(outputStream);
		}
	}

	
	@SuppressWarnings("unchecked")
	public static HashMap<Integer[], ExamMap> getExamMapObj()
	{
		ObjectInputStream inputStream = null;
		HashMap<Integer[], ExamMap> clashesMatrix = null;
		
		try
		{
			inputStream = FileHelper.getObjectReader("examMapObj.data");
			clashesMatrix = (HashMap<Integer[], ExamMap>) inputStream.readObject();
		}
		catch (Exception e)
		{
			System.out.println("[GeneticAlgorithm.getExamMapObj()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeInputStream(inputStream);
		}
		return clashesMatrix;
	}
	
	public static void saveExamMapObj(HashMap<Integer[], ExamMap> examMapObj)
	{
		ObjectOutputStream outputStream = null;
		
		try
		{
			outputStream = FileHelper.getObjectWriter("examMapObj.data");
			outputStream.writeObject(examMapObj);
		}
		catch (IOException e)
		{
			System.out.println("[GeneticAlgorithm.saveExamMapObj()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeOutputStream(outputStream);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Table<Integer, Integer, TemporalDifference> getEvalMatrix()
	{
		ObjectInputStream inputStream = null;
		Table<Integer, Integer, TemporalDifference> evalMatrix = null;
		
		try
		{
			inputStream = FileHelper.getObjectReader("evalMatrix.data");
			evalMatrix = (Table<Integer, Integer, TemporalDifference>) inputStream.readObject();
		}
		catch (Exception e)
		{
			System.out.println("[GeneticAlgorithm.getEvalMatrix()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeInputStream(inputStream);
		}
		return evalMatrix;
	}
	
	public static void saveEvalMatrix(Table<Integer, Integer, TemporalDifference> evalMatrix)
	{
		ObjectOutputStream outputStream = null;
		
		try
		{
			outputStream = FileHelper.getObjectWriter("evalMatrix.data");
			outputStream.writeObject(evalMatrix);
		}
		catch (IOException e)
		{
			System.out.println("[GeneticAlgorithm.saveEvalMatrix()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeOutputStream(outputStream);
		}
	}
	
	public static void saveTimeslotMap(HashMap<Integer, Timeslot> timeslotMap)
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
	public static HashMap<Integer, Timeslot> getTimeslotMap()
	{
		ObjectInputStream inputStream = null;
		HashMap<Integer, Timeslot> timeslotMap = null;
		
		try
		{
			inputStream = FileHelper.getObjectReader("timeslotMap.data");
			timeslotMap = (HashMap<Integer, Timeslot>) inputStream.readObject();
		}
		catch (Exception e)
		{
			System.out.println("[GeneticAlgorithm.getTimeslotMap()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeInputStream(inputStream);
		}
		return timeslotMap;
	}
	
	public static void saveBestChromosome(Chromosome chromosome)
	{
		ObjectOutputStream outputStream = null;
		
		try
		{
			outputStream = FileHelper.getObjectWriter("bestChromosome.data");
			outputStream.writeObject(chromosome);
		}
		catch (IOException e)
		{
			System.out.println("[GeneticAlgorithm.saveBestChromosome()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeOutputStream(outputStream);
		}
	}
	
	public static Chromosome getBestChromosome()
	{
		ObjectInputStream inputStream = null;
		Chromosome bestChromosome = null;
		
		try
		{
			inputStream = FileHelper.getObjectReader("bestChromosome.data");
			bestChromosome = (Chromosome) inputStream.readObject();
		}
		catch (Exception e)
		{
			System.out.println("[GeneticAlgorithm.getBestChromosome()]: " + e.getMessage());
		}
		finally
		{
			FileHelper.closeInputStream(inputStream);
		}
		return bestChromosome;
	}	
}
