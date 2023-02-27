package dk.stbn.p2peksperiment;



import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // UI-elements
    private Button startClient, startServer, send;
    private TextView serverInfoTv, clientInfoTv;
    private EditText messageField;

    // Logging/status messages
    private String serverinfo = "SERVER LOG:";
    private String clientinfo = "CLIENT LOG: ";
    //Global data
    private final int PORT = 4444;
    private String THIS_IP_ADDRESS = ""; //Default localhost - not really useful
    private String REMOTE_IP_ADDRESS = "";
    private ArrayList<String> ips = new ArrayList();

    private String theMessage;
    private Thread serverThread = new Thread(new MyServerThread());
    private Thread klientThread = new Thread(new MyClientThread());

    //---Some state---
    private boolean ip_submitted = false;
    private boolean iAmServer = false; //TODO: only useful when testing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI boilerplate
        startClient = findViewById(R.id.button);
        startClient.setOnClickListener(this);
        startServer = findViewById(R.id.button2);
        startServer.setOnClickListener(this);
        serverInfoTv = findViewById(R.id.serveroutput);
        clientInfoTv = findViewById(R.id.clientoutput);
        send = findViewById(R.id.send);
        send.setOnClickListener(this);
        send.setEnabled(false); //Disable Send button until connection is established
        messageField = findViewById(R.id.besked);
        messageField.setText("192.168.10.106");

        //startClient.setEnabled(false);

        addHardcodedIPs();
    }

    private void addHardcodedIPs() {
        ips.add("192.168.10.106");//Sune home
        ips.add("192.168.10.187");//Sune home
        ips.add("192.168.10.118");//Sune home
        ips.add("10.90.17.158");//RucIOT

    }

    @Override
    public void onClick(View view) {

        if(view == startClient) {
            iAmServer = false;
            send.setEnabled(true);

            if(!ip_submitted){

                sUpdate("Enter server IP-address");
                messageField.setHint("Enter server-IP-address");
                startClient.setEnabled(false);
                send.setText("Remote IP");
                return;
            }
            klientThread.start();
            startClient.setEnabled(false); //Don't start two clients
            //startServer.setEnabled(false);
            serverinfo += "- - - CLIENT STARTED - - - \n";

        }
        else if (view == startServer){
            iAmServer = true;

            serverThread.start();
            serverinfo += "- - - SERVER STARTED - - -\n";

            //Obtain my local IP-address
            try {
                THIS_IP_ADDRESS = getLocalIpAddress();
                sUpdate("SERVER: Automatic SERVER IP: " + THIS_IP_ADDRESS);
                //Galaxy s10e IOT
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            startServer.setEnabled(false); //Don't start two servers
            //startKlient.setEnabled(false);
            send.setEnabled(false); //Client initiates conversation
            messageField.setText("");
        }
        else if (view == send){

            theMessage = messageField.getText().toString();
            messageField.setText("");
            messageField.setHint("type message here...");

            if (!ip_submitted && !iAmServer){
                ip_submitted = true;
                send.setText("send");
                REMOTE_IP_ADDRESS = theMessage;
                startClient.setEnabled(true);
                send.setEnabled(false);
            }

            if (iAmServer) {
                synchronized (serverThread) {
                    //update("onclick notify server");
                    serverThread.notify();
                }
            }
            else {
                synchronized (klientThread) {
                    //update("onclick notify client");
                    klientThread.notify();
                    //update("forbi notify");
                }
            }
            }


    }

    class MyServerThread implements Runnable {
        @Override
        public void run() {
            //Alwway be ready for next client
            while (true) {

                try {
                    // update("SERVER: starting serversocket");
                    ServerSocket serverSocket = new ServerSocket(4444);
                    sUpdate("SERVER: start listening..");
                    Socket klientSocket = serverSocket.accept();
                    sUpdate("SERVER connection accepted");

                    DataInputStream instream = new DataInputStream(klientSocket.getInputStream());
                    DataOutputStream outstream = new DataOutputStream(klientSocket.getOutputStream());

                    //Start conversation
                    boolean carryOn = true;
                    while(carryOn) {

                        String str = (String) instream.readUTF();
                        sUpdate("Client says: " + str);
                        able(true);
                        synchronized (serverThread){
                            serverThread.wait(); //Waiting for user input
                        }
                        String answer = theMessage;
                        outstream.writeUTF(answer);
                        outstream.flush();
                        carryOn = !str.equalsIgnoreCase("bye");
                    }
                    //Closing everything down
                    serverSocket.close();
                    sUpdate("SERVER: Server socket closed");
                    klientSocket.close();
                    sUpdate("SERVER: Client socket closed");
                    instream.close();
                    sUpdate("SERVER: inputstream closed");
                    outstream.close();
                    sUpdate("SERVER: outputstream closed");
                } catch (
                        IOException e) {
                    sUpdate("oops!!");
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

//Copied from https://www.tutorialspoint.com/sending-and-receiving-data-with-sockets-in-android
private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }
    class MyClientThread implements Runnable {
        @Override
        public void run() {

            try {
                cUpdate("CLIENT: starting client socket ");
                Socket connectionToServer = new Socket(REMOTE_IP_ADDRESS, 4444);
                cUpdate("CLIENT: client connected ");

                DataInputStream instream = new DataInputStream(connectionToServer.getInputStream());
                //cUpdate(""+instream);
                DataOutputStream out = new DataOutputStream(connectionToServer.getOutputStream());
                //cUpdate("CLIENT: made outputstream");
                boolean carryOn = true;
                while(carryOn) {
                    able(true);
                    synchronized (klientThread) {
                    try{
                        cUpdate("Waiting for your message...");
                        klientThread.wait();
                        //cUpdate("after wait");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    }
                    String besked = theMessage;
                    out.writeUTF(besked);
                    //cUpdate("CLIENT: wrote to outputstream");
                    out.flush();
                    //cUpdate("CLIENT: flushed");
                    String messageFromServer = instream.readUTF();
                    cUpdate("Server says: " +messageFromServer);

                    carryOn = !messageFromServer.equalsIgnoreCase("bye");
                }
                instream.close();
                cUpdate("CLIENT: closed inputstream");
                out.close();
                cUpdate("CLIENT: closed outputstream");
                connectionToServer.close();
                cUpdate("CLIENT: closed socket");

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }//Run()
    } //class MinKlientTråd

    public void sUpdate(String message){
        //Run this code on UI-thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                serverinfo = message + "\n" + serverinfo;
                serverInfoTv.setText(serverinfo);
            }
        });

    }

    public void cUpdate(String message){
        System.out.println(message);

        //Run this code on UI-thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                clientinfo = message + "\n" + clientinfo;
                clientInfoTv.setText(clientinfo);
            }
        });

    }


    void able(boolean enabled){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                send.setEnabled(enabled);
            }
        });
    }

}