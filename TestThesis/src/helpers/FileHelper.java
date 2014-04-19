package helpers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileHelper {

	// helper to save objects in a file. return objectoutputstream and write object in respective class
	public static ObjectOutputStream getObjectWriter(String fileName) throws IOException
	{
		ObjectOutputStream outputStream = null;

		FileOutputStream fileOutputStream = new FileOutputStream(System.getProperty("user.home") + 
					 												 System.getProperty("file.separator") + 
																	 fileName);
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
		
		FileInputStream fileInputStream = new FileInputStream(System.getProperty("user.home") + 
					 												 System.getProperty("file.separator") + 
																	 fileName);
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
	
}
