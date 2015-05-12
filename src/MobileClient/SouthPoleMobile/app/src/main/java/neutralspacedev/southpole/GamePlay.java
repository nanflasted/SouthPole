package neutralspacedev.southpole;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.view.ViewGroup.LayoutParams;

import GenericClient.LocalState;
import Utility.SPU;


public class GamePlay extends Activity {

    ImageButton tileButtons[][];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);


        //Get Current Condition.
        AsyncTask botherServer = new ContactServer().execute(SPU.Command.GETCOND);
        boolean loginStatus = false;
        try {
            botherServer.get();
        } catch (Exception e) {
            Log.v("Error", "Error");
        }

        //get all the buttons and put in array.
        tileButtons = initTileButtons(LocalState.viewSize);
        //load the buttons
        updateTileButtons();

    }

    private void updateTileButtons(){
        for(int x = 0; x != LocalState.viewSize; x++){
            for(int y = 0; y != LocalState.viewSize; y++){
                int bkgTile = -1;
                switch (LocalState.localEnv[x][y]) {
                    case SNOW_LIGHT:
                        bkgTile = R.drawable.snowlight;
                        break;
                    case SNOW_HEAVY:
                        bkgTile = R.drawable.snowheavy;
                        break;
                    case WATER:
                        bkgTile =R.drawable.water;
                        break;
                    case MOUNTAIN:
                        bkgTile =R.drawable.mountain;
                        break;
                    case GOAL:
                        bkgTile =R.drawable.abc_btn_radio_material;
                        break;
                }

                if(LocalState.viewSize/2 == x && x == y ){
                    Resources r = getResources();
                    Drawable[] layers = new Drawable[2];
                    layers[0] = r.getDrawable(bkgTile);
                    layers[1] = r.getDrawable(R.drawable.mush_outline);
                    LayerDrawable layerDrawable = new LayerDrawable(layers);
                    tileButtons[x][y].setBackground(layerDrawable.mutate());

                } else {
                    tileButtons[x][y].setBackgroundResource(bkgTile);
                }
            }
        }



    }

    private ImageButton[][] initTileButtons(int dim){
        //list of imageButtons to return
        ImageButton returnButs[][] = new ImageButton[dim][dim];

        //refernce to grid of buttons
        TableLayout layout = (TableLayout) findViewById(R.id.tileGrid);
        layout.removeAllViews();

        //for each y
        for(int y = 0; y != dim; y++) {
            //add the row
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
            layout.addView(row);
            //for each x
            for (int x = 0; x != dim; x++) {
                //add the button
                ImageButton tile = new ImageButton(this);
                row.addView(tile);
                returnButs[x][y] = tile;
            }
        }

        return returnButs;
    }

}
