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
        send = findViewById(R.id.send);
        send.setOnClickListener(this);
        send.setEnabled(false);
        messageField = findViewById(R.id.besked);
        messageField.setText("192.168.10.106");




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
            send.setEnabled(true);
        }
        else if (view == startServer){
            iAmServer = true;
            serverTråd.start();
            startServer.setEnabled(false);
            startKlient.setEnabled(false);
            info += "I AM SERVER\n";
            send.setEnabled(false);
            messageField.setText("");
        }
        else if (view == send){
            theMessage = messageField.getText().toString();
            messageField.setText("");
            messageField.setHint("type message here...");
                if (iAmServer) {
                    synchronized (serverTråd) {
                        //update("onclick notify server");
                        serverTråd.notify();
                    }
                }
                else {
                    synchronized (klientTråd) {
                        //update("onclick notify client");
                        klientTråd.notify();
                        //update("forbi notify");
                    }
                }
            }
            if (ip_submitted)
                send.setEnabled(false);
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
                        able(true);
                        //update("Type message (Enter sends message)");
                        synchronized (serverTråd){
                            serverTråd.wait();
                        }
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


                if(!ip_submitted){
                    update("Please submit an IP-address");
                  synchronized (klientTråd) {
                      try{
                          update("waiting for ip...");
                          klientTråd.wait();
                          update("after wait");


                      } catch (InterruptedException e) {
                          throw new RuntimeException(e);
                      }
                  }

                }
                IP_ADDRESS = theMessage;
                ip_submitted = true;
                update("CLIENT: starting client socket ");
                Socket klientsocket = new Socket(IP_ADDRESS, 4444);

                update("CLIENT: client connected ");

                DataInputStream instream = new DataInputStream(klientsocket.getInputStream());
                update(""+instream);
                DataOutputStream out = new DataOutputStream(klientsocket.getOutputStream());
                //update("CLIENT: made outputstream");
                boolean carryOn = true;
                while(carryOn) {
                    able(true);
                   synchronized (klientTråd) {
                    try{
                        update("Venter på input fra DIG! (klient)");
                        klientTråd.wait();
                        //update("efter wait, ALMINDELIG LÆSNING");
                   } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                   }
                   String besked = theMessage;
                   out.writeUTF(besked);
                   //update("CLIENT: wrote to outputstream");
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
                e.printStackTrace();
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
                info = besked + "\n" + info ;
                ipInfo.setText(info);
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