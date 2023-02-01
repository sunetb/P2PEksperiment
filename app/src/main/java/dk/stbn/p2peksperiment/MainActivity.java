package dk.stbn.p2peksperiment;

import androidx.appcompat.app.AppCompatActivity;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

String serverIP;
    Button startKlient;
    TextView ipInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startKlient = findViewById(R.id.button);
        startKlient.setOnClickListener(this);


        Thread serverTråd = new Thread(new MinServerTråd());
        serverTråd.start();






    }

    @Override
    public void onClick(View view) {
        // new Handler().postDelayed(new Runnable() {
        //   @Override
        // public void run() {
        Thread klientTråd = new Thread(new MinKlientTråd());
        klientTråd.start();
        //}
        //}, 50); //din kode køres om 50 milisekunder
        startKlient.setEnabled(false);
    }

    class MinServerTråd implements Runnable {
    @Override
    public void run() {
        Socket socket;
        try {
            serverIP = getLocalIpAddress();
            System.out.println("SERVER: SERVER IP: " + serverIP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            System.out.println("SERVER: starting serversocket");
            ServerSocket server = new ServerSocket(5050);

            System.out.println("SERVER: start listening..");
            socket = server.accept();
            System.out.println("SERVER connection accepted");
            PrintWriter output = new PrintWriter(socket.getOutputStream());
            output.write("BESKEDEN KOMMER HER");
            //Thread.sleep(50);
            // output.write("NY BESKED");
            output.flush();


        } catch (
                IOException e) {
            System.out.println("oops!!");
            throw new RuntimeException(e);


        }
        System.out.println("SERVER (later): SERVER IP: " + serverIP);
    }
}    private String getLocalIpAddress() throws UnknownHostException {
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
            System.out.println("CLIENT: starting client socket");


            socket = new Socket(serverIP, 5050);
            //socket = new Socket(""localhost/127.0.0.1");
            //s7 SERVER IP: 10.80.0.138
            //socket = new Socket("10.80.0.138", 5050);
            System.out.println("CLIENT: client connected..");

             input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                } catch (IOException e) {
            throw new RuntimeException(e);
        }


        System.out.println("CLIENT: Start reading");
        while (true) {

                try {
                    final String message = input.readLine();
                    if (message != null) {
                    //    MainActivity.this.runOnUiThread(new Runnable() {
                      //      @Override
                        //    public void run() {
                                System.out.println("CLIENT: Server sent me this: " + message + " ");
                          //  }
                       // });
                    }
                    else break;

                } catch (
                        IOException e) {
                    System.out.println("CLIENT: oops ioexception!!");
                    throw new RuntimeException(e);
                }
            System.out.println("loop");
            }
        System.out.println("CLIENT: Done reading");
        }//Run()
    }
}