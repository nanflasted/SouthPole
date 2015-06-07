package Utility.Management;

import Utility.*;

import java.sql.*;
import java.util.*;

/* Wrapper Class for a Connection
 * @author NanflasTed
 * 
 */
public class DBConnection {

	public String driver = SPU.DRIVERNAME;
	private Connection conn;
	
	private Timer timer;
	private KillTask kill;

	private class KillTask extends TimerTask
	{
		
		Connection toKill;
		
		public KillTask(Connection toKill)
		{
			this.toKill = toKill;
		}
		public void run()
		{
			try
			{
				toKill.close();
			}
			catch(SQLException e)
			{
				System.err.println("Exception when closing connection using auto-disconnect");
				e.printStackTrace();
			}
		}
	}
	
	public DBConnection(String url, String un, String pw)
	{
		try
		{
			Class.forName(driver);
			conn = DriverManager.getConnection(url,un,pw);
			timer = new Timer();
			timer.schedule(kill = new KillTask(conn), SPU.DBTTL);
		}
		catch(ClassNotFoundException e1)
		{
			System.err.println("Driver not found");
			System.exit(1);
		}
		catch(SQLException e2)
		{
			System.err.println("SQL connection failure");
			e2.printStackTrace();
			System.exit(1);
		}
	}
	
	public void executeUpdate(String update) throws Exception
	{
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(update);
		kill.cancel();
		timer.schedule(kill = new KillTask(conn), SPU.DBTTL);
		stmt.close();
	}
	
	public ResultSet executeQuery(String query) throws Exception
	{
		Statement stmt = conn.createStatement();
		ResultSet rsset = stmt.executeQuery(query);
		kill.cancel();
		timer.schedule(kill = new KillTask(conn), SPU.DBTTL);
		stmt.close();
		return rsset;
	}
	
	public PreparedStatement getPS(String statement) throws Exception
	{
		PreparedStatement pst = conn.prepareStatement(statement);
		kill.cancel();
		timer.schedule(kill = new KillTask(conn), SPU.DBTTL);
		return pst;
	}
	
	public DatabaseMetaData getMD() throws SQLException
	{
		return conn.getMetaData();
	}
	
	public String getCatalogue() throws SQLException
	{
		return conn.getCatalog();
	}
			
	
	public void close()
	{
		try
		{
			conn.close();
			kill.cancel();
			timer.cancel();
		}
		catch(SQLException e)
		{
			System.err.println("Exception when closing connection using manual disconnect");
			e.printStackTrace();
		}
	}
	
	public boolean isClosed() throws Exception
	{
		return (conn==null)?true:conn.isClosed();
	}
}
