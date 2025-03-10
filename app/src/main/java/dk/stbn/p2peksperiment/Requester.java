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
                phoneHome.updateUI("REQUESTER: starting requester socket ", false);
                Socket socket = new Socket(remoteIP, 4444);
                phoneHome.updateUI("REQUESTER: requester connected ", false);

                DataInputStream instream = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                //Hello part:
                String message = "ID";
                out.writeUTF(message);
                out.flush();
                String messageFromResponder = instream.readUTF();
                phoneHome.updateUI("RESPONDER says:_" + messageFromResponder, false);
                //Simple wait
                Util.waitABit();



                while (carryOn) {
                    //Write message to outstream
                    message = CommunicationHandler.getInstance().generateRequest(messageFromResponder);
                    out.writeUTF(message);
                    out.flush();
                    phoneHome.updateUI("I said:______" + message, false);
                    //Read message from server
                    messageFromResponder = instream.readUTF();
                    phoneHome.updateUI("RESPONDER says:_" + messageFromResponder, false);
                    //Simple wait
                    Util.waitABit();
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
            }

        }//run()



}
