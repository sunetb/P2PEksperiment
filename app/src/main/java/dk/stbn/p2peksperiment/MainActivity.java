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
    Button startKlient, startServer, send;
    TextView ipInfo;
    EditText messageField;

    // Logging/status messages
    String info  = "LOG: \n";

    //Global data
    final int PORT = 4444;
    String IP_ADDRESS = "127.0.0.1"; //Default localhost - not really useful
    ArrayList<String> ips = new ArrayList();

    String theMessage;
    Thread serverTråd = new Thread(new MinServerTråd());
    Thread klientTråd = new Thread(new MinKlientTråd());

    //state
    boolean ip_submitted = false;
    boolean iAmServer = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //String phoneModel = Build.MODEL;

        startKlient = findViewById(R.id.button);
        startKlient.setOnClickListener(this);
        startServer = findViewById(R.id.button2);
        startServer.setOnClickListener(this);
        ipInfo = findViewById(R.id.ipinfo);
        //startKlient.setEnabled(false);
        send = findViewById(R.id.sendclient);
        send.setOnClickListener(this);
        messageField = findViewById(R.id.clientmessagefield);
        String localIP = null;
        try {
            localIP = getLocalIpAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        messageField.setText(localIP);




    }

    @Override
    public void onClick(View view) {

        if(view == startKlient) {
            // new Handler().postDelayed(new Runnable() {
            //   @Override
            // public void run() {
            iAmServer = false;
            klientTråd.start();
            //}
            //}, 50); //din kode køres om 50 milisekunder
            startKlient.setEnabled(false);
            startServer.setEnabled(false);
            info += "I AM CLIENT\n";
        }
        else if (view == startServer){
            iAmServer = true;
            serverTråd.start();
            startServer.setEnabled(false);
            startKlient.setEnabled(false);
            info += "I AM SERVER\n";
        }
        else if (view == send){
            theMessage = messageField.getText().toString();

                if (iAmServer) {
                    synchronized (serverTråd) {
                        update("onclick notify server");
                        serverTråd.notify();
                    }
                }
                else {
                    synchronized (klientTråd) {
                        update("onclick notify client");
                        klientTråd.notify();
                    }
                }
            }

    }

    class MinServerTråd implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    IP_ADDRESS = getLocalIpAddress();
                    update("SERVER: Automatic SERVER IP: " + IP_ADDRESS);
                    //Galaxy s10e IOT 10.90.17.158
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }



                try {

                    // update("SERVER: starting serversocket");
                    ServerSocket serverSocket = new ServerSocket(4444);

                    update("SERVER: start listening..");
                    Socket klientSocket = serverSocket.accept();
                    update("SERVER connection accepted");
                    DataInputStream instream = new DataInputStream(klientSocket.getInputStream());
                    DataOutputStream outstream = new DataOutputStream(klientSocket.getOutputStream());
                    //Scanner input = new Scanner(System.in);

                    boolean carryOn = true;
                    while(carryOn) {

                        String str = (String) instream.readUTF();
                        update("Client says: " + str);
                        //update("Type message (Enter sends message)");
                        wait();
                        String answer = theMessage;//do something interesting here
                        outstream.writeUTF(answer);
                        outstream.flush();
                        carryOn = !str.equalsIgnoreCase("bye");
                    }
                    serverSocket.close();
                    klientSocket.close();
                    instream.close();
                    //input.close();
                } catch (
                        IOException e) {
                    update("oops!!");
                    throw new RuntimeException(e);


                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                //update("SERVER (later): Automatic SERVER IP: " + IP_ADDRESS);
            }
        }
    }

private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }
    class MinKlientTråd  implements Runnable {
        @Override
        public void run() {

            try {
                //Scanner input = new Scanner(System.in);
                //update("Please write ip of server (Type 'c' to use hardcoded: 10.90.17.181) ");
                //String ip = input.nextLine();
                //if (ip.equalsIgnoreCase("c"))
                //    ip = "10.90.17.181";


                if(!ip_submitted){
                    update("Please submit an IP-address");
                  synchronized (this) {
                      try{
                          update("waiting for ip...");
                          wait();
                          update("after wait");
                          IP_ADDRESS = theMessage;
                          ip_submitted = true;
                          update("CLIENT: starting client socket ");
                      } catch (InterruptedException e) {
                          throw new RuntimeException(e);
                      }
                  }

                }
                Socket klientsocket = new Socket(IP_ADDRESS, 4444);//Fra emulator, indstillinger

                update("CLIENT: client connected ");

                DataInputStream instream = new DataInputStream(klientsocket.getInputStream());
                DataOutputStream out = new DataOutputStream(klientsocket.getOutputStream());
                update("CLIENT: made outputstream");
                boolean carryOn = true;
                while(carryOn) {

                    update("Type message (Enter sends the message)");
                  wait();
                   String besked = theMessage;
                    out.writeUTF(besked);
                    update("CLIENT: wrote to outputstream");

                    out.flush();
                    //update("CLIENT: flushed");
                    String messageFromServer = instream.readUTF();
                    update("Server says: " +messageFromServer);
                    carryOn = !messageFromServer.equalsIgnoreCase("bye");
                }
                //input.close();
                update("CLIENT: closed Scanner");
                instream.close();
                update("CLIENT: closed inputstream");
                out.close();
                update("CLIENT: closed outputstream");
                klientsocket.close();
                update("CLIENT: closed socket");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }//Run()
    } //class MinKlientTråd

    public void update (String besked){
        System.out.println(besked);

        //Run this code on UI-thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                info += besked + "\n";
                ipInfo.setText(info);
            }
        });

    }




}