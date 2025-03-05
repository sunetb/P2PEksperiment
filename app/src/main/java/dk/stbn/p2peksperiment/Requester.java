package dk.stbn.p2peksperiment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Requester implements Runnable{

    MessageUpdate phoneHome; //Access to MainActivity to pass messages to UI

    String remoteIP;
    private boolean carryOn = true;


    public Requester(String remoteIP, MessageUpdate main) {
        this.remoteIP = remoteIP;
        phoneHome = main;
    }

    public void endConversation(){
            carryOn = false;
        }

        @Override
        public void run() {

            try {
                phoneHome.updateUI("CLIENT: starting client socket ", false);
                Socket connectionToServer = new Socket(remoteIP, 4444);
                phoneHome.updateUI("CLIENT: client connected ", false);

                DataInputStream instream = new DataInputStream(connectionToServer.getInputStream());
                DataOutputStream out = new DataOutputStream(connectionToServer.getOutputStream());

                while (carryOn) {
                    //Write message to outstream
                    String message = Util.getAnimal();
                    out.writeUTF(message);
                    out.flush();
                    phoneHome.updateUI("I said:______" + message, false);
                    //Read message from server
                    String messageFromServer = instream.readUTF();
                    phoneHome.updateUI("Server says:_" + messageFromServer, false);
                    //Simple wait
                    Util.waitABit();
                }
                //Saying goodbye
                out.writeUTF("____Bye bye!!____");
                out.flush();
                //Closing down
                instream.close();
                phoneHome.updateUI("CLIENT: closed inputstream", false);
                out.close();
                phoneHome.updateUI("CLIENT: closed outputstream", false);
                connectionToServer.close();
                phoneHome.updateUI("CLIENT: closed socket", false);

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }//run()



}
