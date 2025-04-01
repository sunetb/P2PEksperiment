package dk.stbn.p2peksperiment;


import android.app.Activity;
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
    *
    * A Responder has the role of waiting and listening
    * until a Requester makes a request.
    * Then request-response from there..
    * */

    //Trick to prevent crash on config-change

    //// UI-elements
   //Test-state
    private Button requesterButton, responderButton, reqSend, respSend;;
    private TextView responderInfoTv, requesterInfoTv, responderTitleTv;
    private EditText requesterMessage, responderMessage;

    // Global data
    private Responder responderThread;
    private Requester requesterThread;


    // UI related state
    private boolean ip_submitted = false;

    //Check if an instance is already running
    boolean requesterStarted;
    boolean responderStarted;
    boolean testState = true;
    boolean chatState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatState = !testState;
        if (chatState) setContentView(R.layout.chat_like_ui);
        else setContentView(R.layout.activity_main);
        requesterStarted = (CommunicationHandler.getInstance().req != null);
        responderStarted = (CommunicationHandler.getInstance().resp != null);
        System.out.println("Responder started? " + responderStarted);
        checkReferences();

        //UI boilerplate
        requesterButton = findViewById(R.id.button);
        responderButton = findViewById(R.id.sendrequester);
        requesterMessage = findViewById(R.id.requestermessagefield);
        responderTitleTv = findViewById(R.id.respondertitle);
        responderInfoTv = findViewById(R.id.responderoutput);
        requesterInfoTv = findViewById(R.id.requesteroutput);


        //Setting click-listeners on buttons
        requesterButton.setOnClickListener(this);
        responderButton.setOnClickListener(this);

        //Setting some UI state
        if (!requesterStarted) {
            String lastIP = getSavedIP();
            if (lastIP.equals("no"))
                requesterMessage.setHint("Submit IP-address");
            else
                requesterMessage.setText(lastIP);

            requesterButton.setEnabled(false); //deactivates the button


            //Getting the IP address of the device
            //1 Show it
            String thisIpAddress = Util.getLocalIpAddress(this);
            responderTitleTv.append("  "+thisIpAddress);
            //2: Use it for Node ID
            CommunicationHandler.getInstance().assignID(thisIpAddress);
            updateUI("- - - NODE ID:  \n" + CommunicationHandler.getInstance().getId() + "\n", true);
        }
        if (!responderStarted){
            //Starting the Responder thread
            responderThread = new Responder(this);
            new Thread(responderThread).start();
            updateUI("- - - RESPONDER STARTED AUTOMATICALLY - - -\n", true);
        }
    }


    @Override
    public void onClick(View view) {

        if (view == requesterButton) {
            if (!requesterStarted) { //Start scenario
                requesterStarted = true;
                String remoteIP = requesterMessage.getText().toString();
                requesterThread = new Requester(remoteIP, this);
                new Thread(requesterThread).start();
                updateUI("- - - REQUESTER STARTED - - - \n", false);
                requesterButton.setText("Stop");
            } else { //Stop scenario
                //requesterThread.endConversation();
                //responderThread.endConversation();
                requesterButton.setEnabled(false);
                responderButton.setEnabled(true);
                requesterStarted = false;
                ip_submitted = false;
                updateUI("- - - REQUESTER ENDED - - - \n", false);
            }
        } else if (view == responderButton) {
            if (!ip_submitted) {
                ip_submitted = true;
                saveIP(requesterMessage.getText().toString());
                requesterButton.setEnabled(true);
                requesterButton.setText("start");
                responderButton.setEnabled(false);
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

    @Override
    protected void onDestroy() {
        //avoid memory leak and delete static reference
        CommunicationHandler.getInstance().act = null;
        super.onDestroy();
    }

    //On configuration change, retrieve references for existing threads
    // and update references to activity
    private void checkReferences() {
        if (CommunicationHandler.getInstance().resp != null) {
            responderThread = CommunicationHandler.getInstance().resp;
            responderThread.setPhoneHome(this);
        }
        if (CommunicationHandler.getInstance().req != null){
            requesterThread = CommunicationHandler.getInstance().req;
            requesterThread.setPhoneHome(this);
        }
        CommunicationHandler.getInstance().act = this;
    }

}