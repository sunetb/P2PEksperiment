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
    private Button requesterButton, reqSend, respSend;;
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

    //Test-state with animals+food or Chat-state where threads wait for the user
    boolean testState = false;
    boolean chatState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatState = !testState;

        //Choose which XML-file to use for UI
        if (chatState) setContentView(R.layout.chat_like_ui);
        else setContentView(R.layout.activity_main);

        //Handling screen-rotation
        requesterStarted = (CommunicationHandler.getInstance().req != null);
        responderStarted = (CommunicationHandler.getInstance().resp != null);
        checkReferences();

        //UI boilerplate
        requesterButton = findViewById(R.id.reqbutton);
        reqSend = findViewById(R.id.sendrequester);
        requesterMessage = findViewById(R.id.requestermessagefield);
        responderTitleTv = findViewById(R.id.respondertitle);
        responderInfoTv = findViewById(R.id.responderoutput);
        requesterInfoTv = findViewById(R.id.requesteroutput);


        //Setting click-listeners on buttons
        requesterButton.setOnClickListener(this);
        reqSend.setOnClickListener(this);

        if (chatState){
            respSend = findViewById(R.id.respsend);
            respSend.setOnClickListener(this);
            responderMessage = findViewById(R.id.respmessagefield);
        }
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
                reqSend.setEnabled(true);
                requesterButton.setEnabled(false);
                if (!requesterStarted) { //Start scenario
                    requesterStarted = true;
                    String remoteIP = requesterMessage.getText().toString();
                    requesterThread = new Requester(remoteIP, this);
                    new Thread(requesterThread).start();
                    updateUI("- - - REQUESTER STARTED - - - \n", false);

                } else
                    if (testState) { //Stop scenario
                    //requesterThread.endConversation();
                    //responderThread.endConversation();
                    requesterStarted = false;
                    ip_submitted = false;
                    updateUI("- - - REQUESTER ENDED - - - \n", false);
                }
            } else if (view == reqSend) {
                if (!ip_submitted) {
                    ip_submitted = true;
                    saveIP(requesterMessage.getText().toString());
                    requesterButton.setEnabled(true);
                    requesterButton.setText("start");
                    reqSend.setEnabled(false);
                }
            }
            //Continuous conversation, chat-style
            if(requesterStarted && chatState){

                requesterButton.setEnabled(false);

                if (view == reqSend){
                    String reqMessage = requesterMessage.getText().toString();
                    synchronized (requesterThread) {
                        requesterThread.setMessage(reqMessage);
                        requesterThread.notify();
                        //updateUI(reqMessage, false);
                    }
                }
                else if (view == respSend){

                    String resMessage = responderMessage.getText().toString();
                    synchronized (responderThread.remoteUser){
                        responderThread.setMessage(resMessage);
                        responderThread.remoteUser.notify();
                        //updateUI(resMessage,true);
                        System.out.println("responder sync");
                    }
                }
            }
        
    }//END onclick

    //Thread-safe updating of UI elements
    @Override
    public void updateUI(String message, boolean fromResponder) {
        System.out.println(message);

        //Run this code on UI-thread
        this.runOnUiThread(new Runnable() {
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


    //All the below is to handle configuration change.
    // Retrieve references for existing threads
    // and update reference to activity
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

    @Override
    protected void onDestroy() {
        //avoid memory leak and delete static reference
        CommunicationHandler.getInstance().act = null;
        super.onDestroy();
    }

}