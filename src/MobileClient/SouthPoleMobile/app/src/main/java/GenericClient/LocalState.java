package GenericClient;

import Utility.SPU.Tile;

public class LocalState {
	public static String username = "ANONYMOUS";
	public static String password = "PASSWORD";
    public static final int viewSize = 11;
	public static Tile[][] localEnv = new Tile[viewSize][viewSize];
}
