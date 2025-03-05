package dk.stbn.p2peksperiment;


import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MessageUpdate {

    // UI-elements
    private Button startClient, submitIP;
    private TextView serverInfoTv, clientInfoTv;
    private EditText ipInputField;

    // Logging/status messages
    private String serverinfo = "SERVER LOG:";
    private String clientinfo = "CLIENT LOG: ";

    // Global data
    private final int PORT = 4444;
    private String THIS_IP_ADDRESS = "";
    private String REMOTE_IP_ADDRESS = "";
    private Responder responderThread = new Responder(this);
    private Requester requesterThread;// = new Requester(this);

    // Some state
    private boolean ip_submitted = false;
   // private boolean carryOn = true; //Now only used for client part
    boolean clientStarted = false;

    int clientNumber = 0;

    CommunicationHandler ch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI boilerplate
        startClient = findViewById(R.id.button);
        serverInfoTv = findViewById(R.id.serveroutput);
        clientInfoTv = findViewById(R.id.clientoutput);
        submitIP = findViewById(R.id.sendclient);
        ipInputField = findViewById(R.id.clientmessagefield);

        //Setting click-listeners on buttons
        startClient.setOnClickListener(this);
        submitIP.setOnClickListener(this);

        //Setting some UI state
        String lastIP = getSavedIP();
        if (lastIP.equals("no"))
            ipInputField.setHint("Submit IP-address");
        else
            ipInputField.setText(lastIP);

        startClient.setEnabled(false); //deactivates the button

        //Getting the IP address of the device
        THIS_IP_ADDRESS = getLocalIpAddress();
        updateUI("This IP is " + THIS_IP_ADDRESS, true);

        //Starting the server thread
        new Thread(responderThread).start();
        serverinfo += "- - - SERVER STARTED - - -\n";

        ch = new CommunicationHandler();

    }

    @Override
    public void onClick(View view) {

        if (view == startClient) {
            if (!clientStarted) {
                clientStarted = true;
                requesterThread = new Requester(THIS_IP_ADDRESS, this);
                new Thread(requesterThread).start();
                clientinfo += "- - - CLIENT STARTED - - - \n";
                startClient.setText("Stop");
            } else {
                requesterThread.endConversation();
                startClient.setEnabled(false);
            }
        } else if (view == submitIP) {
            if (!ip_submitted) {
                ip_submitted = true;
                REMOTE_IP_ADDRESS = ipInputField.getText().toString();
                saveIP(REMOTE_IP_ADDRESS);
                startClient.setEnabled(true);
                submitIP.setEnabled(false);
            }
        }

    }//onclick

    //END UI-stuff


    //-------------------The server



    //-------------------The client


    // !!! Returns 0.0.0.0 on emulator
    //Modified from https://www.tutorialspoint.com/sending-and-receiving-data-with-sockets-in-android
    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        String address = null;
        try {
            address = InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return address;
    }



    //Thread-safe updating of UI elements

    public void updateUI(String message, boolean fromResponder) {
        System.out.println(message);

        //Run this code on UI-thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (fromResponder) {
                    serverinfo = message + "\n" + serverinfo;
                    serverInfoTv.setText(serverinfo);

                } else {
                    clientinfo = message + "\n" + clientinfo;
                    clientInfoTv.setText(clientinfo);
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