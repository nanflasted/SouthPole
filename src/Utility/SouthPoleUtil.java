/**
 * 
 */
package Utility;

/**
 * @author NanflasTed
 * @author WJLIDDY
 *
 */

import java.io.*;
import java.sql.*;
import java.util.*;

public class SouthPoleUtil {

    public static final int WATER_BORDER_SIZE = 10;

    //CONVERT TO INT:Command.SIGNUP.ordinal();
    //CONVERT TO ENUM:Command.values()[(int)Command.SIGNUP.ordinal()];
    public static enum Command {
        LOGIN,
        SIGNUP,
        GETCOND,
        MOVEUP,
        MOVELEFT,
        MOVEDOWN,
        MOVERIGHT
    }

    public static enum ServerResponse {
        LOGIN_FAIL,
        LOGIN_OK,
        ACCOUNT_CREATE_FAIL,
        ACCOUNT_CREATE_OK
    }

    public static enum Tile {
        SNOW_LIGHT,
        SNOW_HEAVY,
        WATER,
        MOUNTAIN,
        GOAL
    }

    public static String dataISReadLine(DataInputStream stream) throws IOException {
        StringBuilder res = new StringBuilder();
        char temp;
        while ((temp = stream.readChar()) != '\n') {
            res.append(temp);
        }
        return res.toString();
    }

    public static Tile[][] generateWorld(int size) {

    	//System.out.println("\u001B[36m");
        Tile[][] world = new Tile[size][size];

        double distFromCenterToOcean = ((size / 2.0) - WATER_BORDER_SIZE);

        //generate the world.
        for (int x = 0; x != size; x++) {
            //System.out.println("");
            for (int y = 0; y != size; y++) {
                //100 % light snow at border, 100% heavy snow at center.
                double distFromCenter = Math.sqrt(Math.pow((x - (size/2)), 2) + Math.pow((y - (size/2)), 2));
                if (distFromCenter >= distFromCenterToOcean)
                    world[x][y] = Tile.WATER;
                else if ((distFromCenter / distFromCenterToOcean) > Math.random())
                    world[x][y] = Tile.SNOW_LIGHT;
                else
                    world[x][y] = Tile.SNOW_HEAVY;

                //add a mountain every once in a while
                if (Math.random() < .02 && !(world[x][y] == Tile.WATER)) {
                    world[x][y] = Tile.MOUNTAIN;
                }

                //debug draw
                switch (world[x][y]) {
                    case SNOW_LIGHT:
                        System.out.print("~ ");
                        break;
                    case SNOW_HEAVY:
                        System.out.print("* ");
                        break;
                    case WATER:
                        System.out.print("  ");
                        break;
                    case MOUNTAIN:
                        System.out.print("M ");
                        break;
                    case GOAL:
                        break;
                }

            }


        }

        return world;
    }

}
