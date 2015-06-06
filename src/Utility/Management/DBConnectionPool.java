package Utility.Management;

import java.util.*;

/*
 * DBConnection pool
 * @author NanflasTed
 */
public class DBConnectionPool {
	public static final int MAXCONN = 30;
	public static final int MINCONN = 5;
	
	private Queue<DBConnection> pool;
	private int inUse;
	private int available;
	
	private String url;
	private String un,pw;
	
	public DBConnectionPool(String url, String un, String pw)
	{
		pool = new LinkedList<DBConnection>();
		this.url = url;
		this.un = un;
		this.pw = pw;
		for (int i = 0; i < MINCONN; i++)
		{
			pool.add(new DBConnection(url,un,pw));
		}
		inUse = 0;
		available = MINCONN;
	}
	
	public synchronized void freeConnection(DBConnection conn) throws Exception
	{
		conn.close();
		pool.add(new DBConnection(url,un,pw));
		inUse--;
		available++;
	}
	
	public synchronized DBConnection getConnection() throws Exception
	{
		if ((pool.isEmpty())&&(inUse+available>=MAXCONN))
		{
			return null;
		}
		DBConnection conn;
		conn = pool.poll();
		if (conn == null)
		{
			ad2Pool();
			conn = pool.poll();
		}
		while ((conn!=null)&&(conn.isClosed()))
		{
			available--;
			ad2Pool();
			conn = pool.poll();
		}
		inUse++;
		available--;
		return conn;
	}
	
	private synchronized void ad2Pool()
	{
		pool.add(new DBConnection(url,un,pw));
		available++;
	}
	
	public int currentConnectionsNumber()
	{
		return inUse;
	}
	
	public int currentAvailableNumber()
	{
		return available;
	}
}
