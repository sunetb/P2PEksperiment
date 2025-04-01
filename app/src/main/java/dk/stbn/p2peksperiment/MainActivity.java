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
    *
    * A Responder has the role of waiting and listening
    * until a Requester makes a request.
    * Then request-response from there..
    * */

   //// UI-elements
    private Button startRequesterButton, reqSendButton, respSendButton;;
    private TextView responderChatlogTv, requesterChatlogTv, responderTitleTv;
    private EditText requesterMessageInput, responderMessageInput;

    // Global data
    private Responder responderThread;
    private Requester requesterThread;
    private String remoteIP;

    // UI related state
    private boolean ip_submitted = false;

    //Check if an instance is already running
    boolean requesterStarted;
    boolean responderStarted;

    //Signal that a remote requester has connection to our responder
    boolean remoteRequesterExists = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         //Choose which XML-file to use for UI
        setContentView(R.layout.chat_like_ui);

        //on configuration change, e.g. screen rotation
        requesterStarted = (CommunicationHandler.getInstance().req != null);
        responderStarted = (CommunicationHandler.getInstance().resp != null);
        ip_submitted = CommunicationHandler.getInstance().ip_Submitted;
        checkAndUpdateReferences();

        //UI elements
        startRequesterButton = findViewById(R.id.reqbutton);
        reqSendButton = findViewById(R.id.sendrequester);
        requesterMessageInput = findViewById(R.id.requestermessagefield);
        responderTitleTv = findViewById(R.id.respondertitle);
        responderChatlogTv = findViewById(R.id.responderoutput);
        requesterChatlogTv = findViewById(R.id.requesteroutput);
        respSendButton = findViewById(R.id.respsend);
        responderMessageInput = findViewById(R.id.respmessagefield);
        //Setting click-listeners on buttons
        startRequesterButton.setOnClickListener(this);
        reqSendButton.setOnClickListener(this);
        respSendButton.setOnClickListener(this);

        //Setting some initial UI state
        respSendButton.setEnabled(remoteRequesterExists);
        startRequesterButton.setEnabled(false);
        if (!requesterStarted) {
            reqSendButton.setEnabled(true);
            reqSendButton.setText("Submit");

            String lastIP = getSavedIP();
            if (lastIP.equals("no"))
                requesterMessageInput.setHint("Submit IP-address");
            else
                requesterMessageInput.setText(lastIP);

            //Getting the IP address of the device
            //1 Show it
            String thisIpAddress = Util.getLocalIpAddress(this);
            responderTitleTv.append("  "+thisIpAddress);
            //2: Use it for Node ID
            CommunicationHandler.getInstance().assignID(thisIpAddress);
            updateUI("- - - NODE ID:  \n" + CommunicationHandler.getInstance().getId() + "\n", true);
        }

        //Start listening
        if (!responderStarted){
            //Starting the Responder thread
            responderThread = new Responder(this);
            new Thread(responderThread).start();
            updateUI("- - - RESPONDER STARTED AUTOMATICALLY - - -\n", true);
        }
        else{
            updateUI("Welcome back!\n", true);
        }
   }

    @Override
    public void onClick(View view) {
        //Start scenario: Submit IP
        if (view == reqSendButton && !ip_submitted) {
                ip_submitted = true;
                remoteIP = requesterMessageInput.getText().toString();
                saveIP(remoteIP);
                startRequesterButton.setEnabled(true);
                startRequesterButton.setText("start");
                reqSendButton.setEnabled(false);
                requesterMessageInput.setText("");
                requesterMessageInput.setHint("Press Start");
                reqSendButton.setText("send");
                //Config changes
                CommunicationHandler.getInstance().ip_Submitted = true;

           } //Start scenario: Start requester
           else if (view == startRequesterButton) {
                reqSendButton.setEnabled(true);
                reqSendButton.setText("Send");
                startRequesterButton.setEnabled(false);
                if (!requesterStarted) { //Start scenario
                    requesterThread = new Requester(remoteIP, this);
                    new Thread(requesterThread).start();
                    requesterStarted = true;
                    updateUI("- - - REQUESTER STARTED - - - \n", false);
                    requesterMessageInput.setHint("Start typing your message");
                }
            }//Normal conversation from here
            else if (view == reqSendButton){
                    String reqMessage = requesterMessageInput.getText().toString();
                    requesterThread.setMessage(reqMessage);
                    synchronized (requesterThread) {
                        requesterThread.notify();
                    }
                    requesterMessageInput.setText("");
            }
            else if (view == respSendButton && remoteRequesterExists){
                    String resMessage = responderMessageInput.getText().toString();
                    responderThread.setMessage(resMessage);
                    synchronized (responderThread.remoteUser){
                        responderThread.remoteUser.notify();
                    }
                    responderMessageInput.setText("");

            }
        
    }//END onclick

    //Thread-safe updating of UI elements
    @Override
    public void updateUI(String message, boolean fromResponder) {
        //Just for logging
        String sender = (fromResponder) ? "Responder: " : "Requester: ";
        System.out.println(sender + message);

        //Run this code on UI-thread
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (fromResponder) {
                    responderChatlogTv.append(message + "\n");
                } else {
                    requesterChatlogTv.append(message + "\n");
                }
            }
        });
    }

    //We need to know if there is a remote user (requester) before we respond.
    @Override
    public void remoteRequesterExists() {
            remoteRequesterExists = true;
            CommunicationHandler.getInstance().remote = true;
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    respSendButton.setEnabled(true);
                    responderMessageInput.setHint("Start typing your message");
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


    //All the below can be ignored. Handling configuration change.
    // 1: Delete the Activity-reference on death
    // 2: Retrieve references for existing threads
    // and update reference to the new Activity
    //
    @Override
    protected void onDestroy() {
        //avoid memory leak and delete static reference
        CommunicationHandler.getInstance().act = null;
        super.onDestroy();
    }

    //Only called on Activity (re-)start
    private void checkAndUpdateReferences() {
        if (CommunicationHandler.getInstance().resp != null) {
            responderThread = CommunicationHandler.getInstance().resp;
            responderThread.setPhoneHome(this);
        }
        if (CommunicationHandler.getInstance().req != null){
            requesterThread = CommunicationHandler.getInstance().req;
            requesterThread.setPhoneHome(this);
        }
        CommunicationHandler.getInstance().act = this;
        remoteRequesterExists = CommunicationHandler.getInstance().remote;
    }

}