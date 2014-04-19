package thesis.timetable_generation;

import helpers.FileHelper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.TreeMap;

import com.google.common.collect.Table;

public class FileOperations {

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
	
	public static void saveClashesMatrix(Table<Integer, Integer, ArrayList<String>> clashesMatrix)
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
	
	public static void saveTimeslotMap(TreeMap<Integer, Timeslot> timeslotMap)
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

	public static void saveGeneratedTimetable(int[] timetable)
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
}