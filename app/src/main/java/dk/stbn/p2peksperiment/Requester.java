package dk.stbn.p2peksperiment;

import android.app.Activity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Requester implements Runnable{

    MessageUpdate phoneHome; //Access to MainActivity to pass messages to UI

    String remoteIP; //The IP to connect to

    String message; //The user message to send
    private boolean carryOn = true;

    public Requester(String remoteIP, MessageUpdate main) {
        this.remoteIP = remoteIP;
        phoneHome = main;
        CommunicationHandler.getInstance().req = this; //in case of configuration change
    }
    public void setMessage(String usermessage){
        message = usermessage;
    }

    //For config change
    public void setPhoneHome(Activity act){
        this.phoneHome = (MessageUpdate) act;
    }
    @Override
    public void run() {

        try {
            phoneHome.updateUI("REQUESTER: starting requester socket ", false);
            Socket socket = new Socket(remoteIP, 4444);
            phoneHome.updateUI("REQUESTER: requester connected ", false);

            DataInputStream instream = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while (carryOn) {

                //message = CommunicationHandler.getInstance().generateRequest(messageFromResponder);

                //Waiting for the user to type
                synchronized (this){
                    wait();
                }
                //Write message to outstream
                out.writeUTF(message);
                out.flush();
                phoneHome.updateUI("I said:______" + message, false);

                //Wait for message from Responder (remote user)
                String messageFromResponder = instream.readUTF();
                phoneHome.updateUI("RESPONDER says:_" + messageFromResponder, false);
            }
            //Saying goodbye
            out.writeUTF("____Bye bye!!____");
            out.flush();
            //Closing down
            instream.close();
            phoneHome.updateUI("REQUESTER: closed inputstream", false);
            out.close();
            phoneHome.updateUI("REQUESTER: closed outputstream", false);
            socket.close();
            phoneHome.updateUI("REQUESTER: closed socket", false);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }//run()



}
