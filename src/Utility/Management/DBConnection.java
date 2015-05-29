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
	public String un = SPU.DBUN;
	public String pw = SPU.DBPW;
	public String url = SPU.DBURL;
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
	
	public DBConnection()
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
		}
		catch(SQLException e2)
		{
			System.err.println("SQL connection failure");
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
	
	public void close()
	{
		try
		{
			conn.close();
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
