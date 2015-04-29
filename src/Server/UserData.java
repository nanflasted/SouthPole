package Server;

public class UserData implements java.io.Serializable{

	private String un;
	private int server;
	
	public UserData(String un, int server)
	{
		this.un = un;
		this.server = server;
	}
}
