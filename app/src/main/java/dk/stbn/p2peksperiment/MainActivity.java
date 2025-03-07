package dk.stbn.p2peksperiment;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MessageUpdate {

    /*
    * Note:
    * "Responder" was "server"
    * "Requester" was "client"
    * */

    // UI-elements
    private Button startRequesterButton, submitIPButton;
    private TextView responderInfoTv, requesterInfoTv, responderTitleTv;
    private EditText ipInputField;

    // Global data
    private Responder responderThread;
    private Requester requesterThread;



    // UI related state
    private boolean ip_submitted = false;

    boolean requesterStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI boilerplate
        startRequesterButton = findViewById(R.id.button);
        responderInfoTv = findViewById(R.id.responderoutput);
        requesterInfoTv = findViewById(R.id.requesteroutput);
        responderTitleTv = findViewById(R.id.respondertitle);
        submitIPButton = findViewById(R.id.sendrequester);
        ipInputField = findViewById(R.id.requestermessagefield);

        //Setting click-listeners on buttons
        startRequesterButton.setOnClickListener(this);
        submitIPButton.setOnClickListener(this);

        //Setting some UI state
        String lastIP = getSavedIP();
        if (lastIP.equals("no"))
            ipInputField.setHint("Submit IP-address");
        else
            ipInputField.setText(lastIP);

        startRequesterButton.setEnabled(false); //deactivates the button

        //Getting the IP address of the device
        //1 Show it
        String thisIpAddress = Util.getLocalIpAddress(this);
        responderTitleTv.append("  "+thisIpAddress);
        //2: Us it for Node ID
        CommunicationHandler.getInstance().assignID(thisIpAddress);
        updateUI("- - - NODE ID:  \n" + CommunicationHandler.getInstance().getId() + "\n", true);

        //Starting the Responder thread
        responderThread = new Responder(this);
        new Thread(responderThread).start();
        updateUI("- - - RESPONDER STARTED AUTOMATICALLY - - -\n", true);

    }

    @Override
    public void onClick(View view) {

        if (view == startRequesterButton) {
            if (!requesterStarted) { //Start scenario
                requesterStarted = true;
                String remoteIP = ipInputField.getText().toString();
                requesterThread = new Requester(remoteIP, this);
                new Thread(requesterThread).start();
                updateUI("- - - REQUESTER STARTED - - - \n", false);
                startRequesterButton.setText("Stop");
            } else { //Stop scenario
                requesterThread.endConversation();
                responderThread.endConversation();
                startRequesterButton.setEnabled(false);
                submitIPButton.setEnabled(true);
                requesterStarted = false;
                ip_submitted = false;
                updateUI("- - - REQUESTER ENDED - - - \n", false);

            }
        } else if (view == submitIPButton) {
            if (!ip_submitted) {
                ip_submitted = true;
                saveIP(ipInputField.getText().toString());
                startRequesterButton.setEnabled(true);
                startRequesterButton.setText("restart");
                submitIPButton.setEnabled(false);
            }
        }

    }//END onclick

    //Thread-safe updating of UI elements
    @Override
    public void updateUI(String message, boolean fromResponder) {
        System.out.println(message);

        //Run this code on UI-thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (fromResponder) {
                    responderInfoTv.append(message + "\n");

                } else {
                    requesterInfoTv.append(message + "\n");
                }
            }
        });
    }

    //Convenience methods: save and restore latest IP
    private void saveIP(String ip){
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("lastIP",ip).apply();
    }
    private String getSavedIP(){
        return PreferenceManager.getDefaultSharedPreferences(this).getString("lastIP", "no");
    }

}