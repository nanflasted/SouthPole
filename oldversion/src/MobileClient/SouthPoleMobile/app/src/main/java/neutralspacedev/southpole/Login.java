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
import GenericClient.LocalState;
import Utility.SPU;


//login screen
public class Login extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //add listeners to the sign in and log in buttons so we can get the username and password
        final Button button = (Button) findViewById(R.id.signInBut);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                login(false);
            }
        });

        final Button button2 = (Button) findViewById(R.id.signUpBut);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                login(true);
            }
        });
    }

    public void login(boolean newAccount){
        //on button click:
        //load in the username and password.
        LocalState.username = ((EditText)findViewById(R.id.userLogin)).getText().toString();
        LocalState.password = ((EditText)findViewById(R.id.passwordLogin)).getText().toString();

        //Try to login (bother the server)
        AsyncTask botherServer = null;

        if(newAccount)
           botherServer = new ContactServer().execute(SPU.Command.SIGNUP);
        else
           botherServer = new ContactServer().execute(SPU.Command.LOGIN);

        SPU.ServerResponse response = null;
        try {
           response = ((SPU.ServerResponse) botherServer.get());
        } catch (Exception e){
            Log.v("Fatal Error!","Login Protocol");
        }

        int duration = Toast.LENGTH_SHORT;

        if(response.equals(SPU.ServerResponse.LOGIN_OK)) {
            Context context = getApplicationContext();
            CharSequence text = "Login Successful!";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            //start game
            Intent myIntent = new Intent(Login.this, GamePlay.class);
            Login.this.startActivity(myIntent);
        } else {
            Context context = getApplicationContext();
            CharSequence text = "Unknown Error";
            switch (response){
                case LOGIN_FAIL:   text = "Bad username or password."; break;
                case ACCOUNT_CREATE_FAIL: text = "Try different username"; break;
                case SERVER_UNRESPONSIVE:   text = "Server is offline"; break;
                case ACCOUNT_CREATE_OK:  text = "Account Created"; break;
            }
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

    }

}
