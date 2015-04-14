package neutralspacedev.southpole;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import GenericClient.LocalState;
import Utility.SouthPoleUtil;


public class GamePlay extends Activity {

    ImageButton tileButtons[][];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        //Get Current Condition.
        AsyncTask botherServer = new ContactServer().execute(SouthPoleUtil.Command.GETCOND);
        boolean loginStatus = false;
        try {
            botherServer.get();
        } catch (Exception e) {
            Log.v("Error", "Error");
        }

        //get all the buttons and put in array.
        tileButtons = loadTileButtons();
        //load the buttons
        updateTileButtons();

    }

    private void updateTileButtons(){
        for(int x = 0; x != 5; x++){
            for(int y = 0; y != 5; y++){
                switch(LocalState.localEnv[x][y]){
                    case SNOW_LIGHT:
                        tileButtons[x][y].setBackgroundResource(R.drawable.snowlight);
                        break;
                    case SNOW_HEAVY:
                        tileButtons[x][y].setBackgroundResource(R.drawable.snowheavy);
                        break;
                    case WATER:
                        tileButtons[x][y].setBackgroundResource(R.drawable.water);
                        break;
                    case MOUNTAIN:
                        tileButtons[x][y].setBackgroundResource(R.drawable.mountain);
                        break;
                    case GOAL:
                        tileButtons[x][y].setBackgroundResource(R.drawable.abc_btn_radio_material);
                        break;
                }

            }
        }



    }

    private ImageButton[][] loadTileButtons(){
        ImageButton returnButs[][] = new ImageButton[5][5];

        returnButs[0][0] = ((ImageButton)findViewById(R.id.imageButton));
        returnButs[0][1] = ((ImageButton)findViewById(R.id.imageButton2));
        returnButs[0][2] = ((ImageButton)findViewById(R.id.imageButton3));
        returnButs[0][3] = ((ImageButton)findViewById(R.id.imageButton4));
        returnButs[0][4] = ((ImageButton)findViewById(R.id.imageButton5));

        returnButs[1][0] = ((ImageButton)findViewById(R.id.imageButton6));
        returnButs[1][1] = ((ImageButton)findViewById(R.id.imageButton7));
        returnButs[1][2] = ((ImageButton)findViewById(R.id.imageButton8));
        returnButs[1][3] = ((ImageButton)findViewById(R.id.imageButton9));
        returnButs[1][4] = ((ImageButton)findViewById(R.id.imageButton10));

        returnButs[2][0] = ((ImageButton)findViewById(R.id.imageButton11));
        returnButs[2][1] = ((ImageButton)findViewById(R.id.imageButton12));
        returnButs[2][2] = ((ImageButton)findViewById(R.id.imageButton13));
        returnButs[2][3] = ((ImageButton)findViewById(R.id.imageButton14));
        returnButs[2][4] = ((ImageButton)findViewById(R.id.imageButton15));

        returnButs[3][0] = ((ImageButton)findViewById(R.id.imageButton16));
        returnButs[3][1] = ((ImageButton)findViewById(R.id.imageButton17));
        returnButs[3][2] = ((ImageButton)findViewById(R.id.imageButton18));
        returnButs[3][3] = ((ImageButton)findViewById(R.id.imageButton19));
        returnButs[3][4] = ((ImageButton)findViewById(R.id.imageButton20));

        returnButs[4][0] = ((ImageButton)findViewById(R.id.imageButton21));
        returnButs[4][1] = ((ImageButton)findViewById(R.id.imageButton22));
        returnButs[4][2] = ((ImageButton)findViewById(R.id.imageButton23));
        returnButs[4][3] = ((ImageButton)findViewById(R.id.imageButton24));
        returnButs[4][4] = ((ImageButton)findViewById(R.id.imageButton25));

        return returnButs;
    }

}
