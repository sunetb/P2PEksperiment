package dk.stbn.p2peksperiment;

import android.app.Activity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Requester implements Runnable{

    MessageUpdate phoneHome; //Access to MainActivity to pass messages to UI

    String remoteIP;

    String message;
    private boolean carryOn = true;

    public void setMessage(String usermessage){
        message = usermessage;
    }

    public Requester(String remoteIP, MessageUpdate main) {
        this.remoteIP = remoteIP;
        phoneHome = main;
        CommunicationHandler.getInstance().req = this;
    }

    public void endConversation(){

        carryOn = false;
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
                //Write message to outstream
                //message = CommunicationHandler.getInstance().generateRequest(messageFromResponder);
                System.out.println("requester før wait");
                synchronized (this){
                    wait();
                }
                System.out.println("requester efter wait");
                out.writeUTF(message);
                out.flush();
                phoneHome.updateUI("I said:______" + message, false);
                //Read message from Responder
                String messageFromResponder = instream.readUTF();
                phoneHome.updateUI("RESPONDER says:_" + messageFromResponder, false);
                //Simple wait
                //Util.waitABit();
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

    public void setPhoneHome(Activity act){
                this.phoneHome = (MessageUpdate) act;
    }

}
