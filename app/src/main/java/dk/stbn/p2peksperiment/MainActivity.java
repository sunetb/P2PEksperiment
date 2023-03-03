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
    private Button startClient, startServer, serverSend, clientSend;
    private TextView serverInfoTv, clientInfoTv;
    private EditText serverMessageField, clientMessageField;

    // Logging/status messages
    private String serverinfo = "SERVER LOG:";
    private String clientinfo = "CLIENT LOG: ";
    //Global data
    private final int PORT = 4444;
    private String THIS_IP_ADDRESS = "";
    private String REMOTE_IP_ADDRESS = "";
    private ArrayList<String> ips = new ArrayList();

    private String theMessage = ""; //Where user input is stored
    private Thread serverThread = new Thread(new MyServerThread());
    private Thread clientThread = new Thread(new MyClientThread());

    //---Some state---
    private boolean ip_submitted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI boilerplate
        startClient = findViewById(R.id.button);
        startServer = findViewById(R.id.button2);

        serverInfoTv = findViewById(R.id.serveroutput);
        clientInfoTv = findViewById(R.id.clientoutput);
        serverSend = findViewById(R.id.sendserver);
        clientSend = findViewById(R.id.sendclient);

        startClient.setOnClickListener(this);
        startServer.setOnClickListener(this);
        serverSend.setOnClickListener(this);
        clientSend.setOnClickListener(this);
        serverSend.setEnabled(false); //Disable Send button until connection is established
        clientSend.setEnabled(false);
        serverMessageField = findViewById(R.id.servermessagefield);
        serverMessageField.setHint("Press START SERVER");
        clientMessageField = findViewById(R.id.clientmessagefield);
        clientMessageField.setHint("Press START CLIENT");
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

        //  - - Client - -
        if(view == startClient) {
            if(!ip_submitted){
                cUpdate("Enter server IP-address");
                clientMessageField.setHint("Enter server-IP-address");
                startClient.setEnabled(false);
                clientSend.setText("submit IP");
                clientSend.setEnabled(true);
                return;
            }

            clientMessageField.setHint("Write something");
            clientThread.start();
            startClient.setEnabled(false); //Don't start two clients
            clientinfo += "- - - CLIENT STARTED - - - \n";

        }
        else if(view == clientSend){
            theMessage = clientMessageField.getText().toString();

            if (!ip_submitted){
                ip_submitted = true;
                clientSend.setText("send");
                REMOTE_IP_ADDRESS = theMessage;
                startClient.setEnabled(true);
                clientMessageField.setHint("Now press START CLIENT");
                clientSend.setEnabled(true);
                return;
            }
            clientMessageField.setText("");
            clientMessageField.setHint("type message here...");
            synchronized (clientThread) {

                clientThread.notify();

            }
            clientSend.setEnabled(false);
        }

        //  - - Server - -

        else if (view == startServer){
            serverThread.start();
            serverinfo += "- - - SERVER STARTED - - -\n";

            //Obtain my local IP-address
            try {
                THIS_IP_ADDRESS = getLocalIpAddress();
                sUpdate("SERVER: Automatic SERVER IP: " + THIS_IP_ADDRESS);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            startServer.setEnabled(false); //Don't start two servers
            serverSend.setEnabled(false); //Client initiates conversation
            serverMessageField.setHint("Wait for client to initiate");
        }
        else if (view == serverSend){

            theMessage = serverMessageField.getText().toString();
            serverMessageField.setText("");
            serverMessageField.setHint("type message here...");




                synchronized (serverThread) {
                    //update("onclick notify server");
                    serverThread.notify();
                }




        }


    }//onclick

    class MyServerThread implements Runnable {
        @Override
        public void run() {
            //Alwway be ready for next client
            while (true) {

                try {
                    //sUpdate("SERVER: starting serversocket");
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
                        serverButtonEnable(true);
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
                } catch (IOException e) {
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
                    clientButtonEnable(true);
                    synchronized (clientThread) {
                    try{
                        //cUpdate("Waiting for your message...");
                        clientThread.wait();
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

        }//run()
    } //class MyClientThread

    //The below four methods are for updating UI-elements on the main thread
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
    void serverButtonEnable(boolean enabled){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                serverSend.setEnabled(enabled);
            }
        });
    }

    void clientButtonEnable(boolean enabled){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                clientSend.setEnabled(enabled);
            }
        });
    }


}