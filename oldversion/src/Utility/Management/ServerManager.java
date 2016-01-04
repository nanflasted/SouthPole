package Utility.Management;

import javax.swing.*;

import Server.*;
import Server.Resource.*;

import java.awt.event.*;
import java.awt.GridLayout;
import java.sql.*;
import java.util.*;

/*
 * server configuration
 */

@SuppressWarnings("serial")
public class ServerManager extends JFrame implements ActionListener
{

	private boolean started = false;
	private JPanel mainPanel = new JPanel(new GridLayout(3,3));
	private JPanel functionalButtons = new JPanel(new GridLayout(1,3));
	private JButton reset = new JButton("Reset");
	private JButton init = new JButton("Initialize");
	private JButton start = new JButton("Start");
	private DBConnectionPool pool;
	private int startPort, endPort,size;
	
	private MainServer ms;
	private ArrayList<SubServer> subServerList = new ArrayList<SubServer>();
	
	//tbp=text box pair
	private class Tbp extends JPanel
	{
		private JTextField txt;
		
		public Tbp(String name,String def)
		{
			super(new GridLayout(1,2));
			this.add(new JLabel(name));
			this.add(txt = new JTextField(def));
			
		}
		
		public String getTxt()
		{
			return txt.getText();
		}
	}
	
	private Tbp sp = new Tbp("Starting Port:","1338");
	private Tbp ep = new Tbp("Ending Port:","1338");
	private Tbp hs = new Tbp("HandShake String:","connectpls");
	private Tbp dburl = new Tbp("Database URL:","jdbc:sqlserver://127.0.0.1:1336; DatabaseName = SouthPole");
	private Tbp dbun = new Tbp("Database username:","SouthPole");
	private Tbp dbpw = new Tbp("Database password:","southpole");
	private Tbp mapsize = new Tbp("Map Size:","1000");
	private Tbp ttl = new Tbp("Time to Live:","30");
	
	public ServerManager()
	{
		super("South Pole Server Launcher");
		this.setVisible(false);
		start.addActionListener(this);
		reset.addActionListener(this);
		init.addActionListener(this);
		functionalButtons.add(reset);
		functionalButtons.add(start);
		functionalButtons.add(init);
		this.getContentPane().add(functionalButtons, "South");
		
		mainPanel.add(sp);
		mainPanel.add(ep);
		mainPanel.add(hs);
		mainPanel.add(dburl);
		mainPanel.add(dbun);
		mainPanel.add(dbpw);
		mainPanel.add(mapsize);
		mainPanel.add(ttl);
		this.getContentPane().add(mainPanel, "Center");
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		JButton b = (JButton)e.getSource();
		if (b==reset)
		{
			doReset();
		}
		if (b==init)
		{
			doInit();
		}
		if (b==start)
		{
			if (!started)
			{
				doStart();
				started = true;
				b.setText("shut down");
			}
			else
			{
				doShutdown();
				started = false;
				b.setText("start");
			}
		}
	}
	
	public void doInit()
	{
		startPort = Integer.parseInt(sp.getTxt());
		endPort = Integer.parseInt(ep.getTxt());
		pool = new DBConnectionPool(dburl.getTxt(),dbun.getTxt(),dbpw.getTxt());
	}
	
	public void doReset()
	{
		try
		{
			pool = new DBConnectionPool(dburl.getTxt(),dbun.getTxt(),dbpw.getTxt());
			//Drop all tables
			DBConnection dbc = pool.getConnection();
			DatabaseMetaData metadata = dbc.getMD();
			ResultSet rsset = metadata.getTables(null,"dbo",null,null);
			ArrayList<String> tbls = new ArrayList<String>();
			while (rsset.next())
			{
				tbls.add(rsset.getString("TABLE_NAME"));
			}
			rsset.close();
			pool.freeConnection(dbc);
			while (!tbls.isEmpty())
			{
				dbc = pool.getConnection();
				PreparedStatement pst = dbc.getPS("DROP TABLE "+tbls.remove(0));
				pst.executeUpdate();
				pst.close();
				pool.freeConnection(dbc);
			}
			
			//Create needed tables
			//redir
			dbc = pool.getConnection();
			PreparedStatement pst = 
			dbc.getPS("CREATE TABLE redir "
					+ "("
					+ "username varchar(255), "
					+ "server int"
					+ ");");
			pst.executeUpdate();
			pool.freeConnection(dbc);			
			//userdata table
			dbc = pool.getConnection();
			pst = dbc.getPS("CREATE TABLE userdata"
					+ "("
					+ "username varchar(255),"
					+ "password varchar(255),"
					+ "class image,"
					+ "server int PRIMARY KEY);");
			pst.executeUpdate();
			pst.close();
			pool.freeConnection(dbc);
			//serverdata table
			startPort = Integer.parseInt(sp.getTxt());
			endPort = Integer.parseInt(ep.getTxt());
			size = Integer.parseInt(mapsize.getTxt());
			dbc = pool.getConnection();
			PreparedStatement pst2 = dbc.getPS("CREATE TABLE serverdata"
					+ "("
					+ "portNumber int PRIMARY KEY,"
					+ "map image);");
			pst2.executeUpdate();
			pst2.close();
			pool.freeConnection(dbc);
			
			//initialize maps
			for (int i = startPort; i <= endPort; i++)
			{
				MapData map = new MapData(size);
				MapManager.generateWorld(map, size);
				MapManager.spawnTowns(map);
				MapManager.create(map,i,pool);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void doStart()
	{
		MainServer ms = new MainServer(startPort,endPort,hs.getTxt(),pool,this);
		ms.start();
	}
	
	public void addSubServer(SubServer server)
	{
		subServerList.add(server);
	}
	public void doShutdown()
	{
		if (ms!=null) ms.forceStop();
		for (SubServer ss : subServerList)
		{
			ss.forceStop();
		}
		System.exit(0);
	}
	
	public static void main(String args[])
	{
		ServerManager mgr = new ServerManager();
		mgr.setSize(840,210);
		mgr.setVisible(true);
	}
}
