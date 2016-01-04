package GenericClient;

import Utility.SPU.Tile;

public class LocalState {
    //Will be overwritten by client.
	public static String username = "ANONYMOUS";
	public static String password = "PASSWORD";
    public static int viewSize = 11;
	public static Tile[][] localEnv = new Tile[viewSize][viewSize];

    public static void setViewSize(int v){
        viewSize = v;
        localEnv = new Tile[v][v];
    }
}
