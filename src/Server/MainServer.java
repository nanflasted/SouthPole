package ...

import ...

public class MainServer
{
	public Connection dbc;
	private Statement stmt;
	private ResultSet rsset;
	private ServerSocket listener;
	private Socket client;
	private int startPort, endPort;
	
	public MainServer(int sp, int ep) throws Exception
	{
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		dbc = DriverManager.getConnection("jdbc:sqlserver://127.0.0.1:1336, DatabaseName = SouthPole", "SouthPole", "southpole");
		for (int i = sp; i <= ep; i++)
		{
			new SubServer(i, dbc).start();
		}
		listener = new ServerSocket(1337);
		while (true)
		{
			client = listener.accept();
		}
	}
	
	public static void main(String[] args)
	{
		try
		{
			if (args.length!=2) 
			{
				throw new Exception("Wrong Number of Arguments: MainServer <Starting Port> <Ending Port>");
			}
			int sp = Integer.parseInt(args[0]);
			if (sp <= 1337)
			{
				throw new Exception("Reserved Port Number");
			}
			int ep = Integer.parseInt(args[1]);
			
			MainServer ms = new MainServer(sp,ep);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}