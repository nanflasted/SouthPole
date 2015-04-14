package neutralspacedev.southpole;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import GenericClient.Client;
import GenericClient.LocalState;
import Utility.SouthPoleUtil;


public class Login extends Activity {
    //move l8r

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        final Button button = (Button) findViewById(R.id.signInBut);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //load in the username and password.
                LocalState.username = ((EditText)findViewById(R.id.userLogin)).getText().toString();
                LocalState.password = ((EditText)findViewById(R.id.passwordLogin)).getText().toString();

                //Try to login
                AsyncTask botherServer = new ContactServer().execute(SouthPoleUtil.Command.LOGIN);
                boolean loginStatus = false;
                try {
                    loginStatus = SouthPoleUtil.ServerResponse.LOGIN_OK.equals((SouthPoleUtil.ServerResponse) botherServer.get());
                } catch (Exception e){
                    Log.v("Error","Error");
                }



                int duration = Toast.LENGTH_SHORT;

                if(loginStatus) {
                    Context context = getApplicationContext();
                    CharSequence text = "Login Successful!";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                    //start game
                    Intent myIntent = new Intent(Login.this, GamePlay.class);
                    Login.this.startActivity(myIntent);
                } else {
                    Context context = getApplicationContext();
                    CharSequence text = "Login Failed.";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

            }
        });
    }


}
