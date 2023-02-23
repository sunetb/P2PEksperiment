package dk.stbn.p2peksperiment;



import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String serverIP;
    Button startKlient, startServer;
    TextView ipInfo;

    String info  = "LOG: \n";

    final int PORT = 4444;

    String IP_ADDRESS = "127.0.0.1";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String phoneModel = Build.MODEL;


        startKlient = findViewById(R.id.button);
        startKlient.setOnClickListener(this);
        startServer = findViewById(R.id.button2);
        startServer.setOnClickListener(this);

        ipInfo = findViewById(R.id.ipinfo);
        startKlient.setEnabled(false);

        //TODO:
        // ny knap som lukker klienten og starter en ny
        //Både skriv og læs på klient og server - pakkes i metoder!! Kræver det Handler?





    }

    @Override
    public void onClick(View view) {

        if(view == startKlient) {
            // new Handler().postDelayed(new Runnable() {
            //   @Override
            // public void run() {
            Thread klientTråd = new Thread(new MinKlientTråd());
            klientTråd.start();
            //}
            //}, 50); //din kode køres om 50 milisekunder
            //startKlient.setEnabled(false);

        }
        else if (view == startServer){
            Thread serverTråd = new Thread(new MinServerTråd());
            serverTråd.start();
            startServer.setEnabled(false);
            startKlient.setEnabled(true);
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
                        String answer = "i'stupid"; //do something interesting here
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
        BufferedReader input;
        Socket socket;
        try {

            update("CLIENT: starting client socket on "+IP_ADDRESS);


            //socket = new Socket(serverIP, 5050);
            //socket = new Socket("localhost/127.0.0.1");
            //socket = new Socket("10.212.178.72", 5050);//fysisk s10e indstillinger

            //socket = new Socket("10.80.0.138", 5050);//fysisk s7 indstillinger
   /*         if (useAutoIP){
                update("CLIENT: Using Auto IP");
                IP_ADDRESS = serverIP;
            }*/
            //Test
            //IP_ADDRESS = "192.168.50.239"; //DTU DELL
            socket = new Socket(IP_ADDRESS, PORT);//Fra emulator, indstillinger

            update("CLIENT: client connected to "+ IP_ADDRESS);



             input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                update("CLIENT: Got inputstream");
                } catch (IOException e) {
            throw new RuntimeException(e);
        }


        update("CLIENT: Try reading");
        while (true) {

                try {
                    final String message = input.readLine();
                    if (message != null) {
                    //    MainActivity.this.runOnUiThread(new Runnable() {
                      //      @Override
                        //    public void run() {
                        update("CLIENT: SUCCESS!!! Server sent me this: " + message + " ");
                          //  }
                       // });
                    }
                    else {
                        update("messages was null");
                        break;
                    }

                } catch (
                        IOException e) {
                    update("CLIENT: oops ioexception!!");
                    throw new RuntimeException(e);
                }
            update("end loop");
            }
        update("CLIENT: Done reading");
        }//Run()
    }

    public void update (String besked){
        System.out.println(besked);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                info += besked + "\n";
                ipInfo.setText(info);
            }
        });

    }

    public void retry (){

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                startKlient.setEnabled(true);

            }
        });

    }
}