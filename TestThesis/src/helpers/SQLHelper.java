package helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLHelper
{
	/** The JDBC driver name */ 
	private static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver"; 
	/** The database URL */ 
	private static final String DATABASE_URL = "jdbc:sqlserver://localhost:1433;databaseName=FacultyICT;SelectMethod=cursor;"; 
	/** The database user-name */ 
	private static final String DATABASE_USERNAME = "sa"; 
	/** The database password */ 
	private static final String DATABASE_PASSWORD = "123456"; 
	
	public static Connection getConnection()
	{
		Connection conn = null;
		
		try
		{ 
			Class.forName(JDBC_DRIVER);
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("[SQLHelper.connectToDB()]: " + e.getMessage());
		}
		
		try
		{
			conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
		}
		catch (SQLException e)
		{
			System.out.println("[SQLHelper.connectToDB()]: " + e.getMessage());
		}
		
		return conn;
	}
	
	public static void closeResultSet(ResultSet rs)
	{
		try
		{
			rs.close();
		}
		catch (SQLException e)
		{
			System.out.println("[SQLHelper.closeResultSet()]: " + e.getMessage());
		}
	}
	
	public static void closeConnection(Connection conn)
	{
		try
		{
			conn.close();
		}
		catch (SQLException e)
		{
			System.out.println("[SQLHelper.closeConnection()]: " + e.getMessage());
		}
	}
	
	public static void closeStatement(Statement stmt)
	{
		try
		 {
			 if (stmt != null)
				 stmt.close();
		 }
		 catch (SQLException e)
		 {
			 System.out.println("[SQLHelper.closeStatement()]: " + e.getMessage());
		 }
	}
	
	public static void closePreparedStatement(PreparedStatement pstmt)
	{
		try
		 {
			 if (pstmt != null)
				 pstmt.close();
		 }
		 catch (SQLException e)
		 {
			 System.out.println("[SQLHelper.closePreparedStatement()]: " + e.getMessage());
		 }
	}
}
