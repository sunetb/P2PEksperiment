package dk.stbn.p2peksperiment;

import android.app.Activity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class Responder implements Runnable {

    private MessageUpdate phoneHome; //Access to MainActivity to pass messages to UI
    RemoteRequester remoteUser; //Should be a list in order to handle more than one
    String message = ""; //The user message to be sent
    boolean contact = false; //Is anyone on the line?

    public Responder(MessageUpdate main) {
        CommunicationHandler.getInstance().resp = this;
        this.phoneHome = main;
    }

    public void setMessage(String usermessage){
        message = usermessage;
    }
    public void setPhoneHome(Activity act){
        this.phoneHome = (MessageUpdate) act;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(4444);

            //Not working: always be ready for next requester
            while (true) {
                phoneHome.updateUI("RESPONDER: start listening..", true);
                Socket socket = serverSocket.accept();//Accept is called when a requester connects
                phoneHome.updateUI("RESPONDER: connection accepted", true);
                remoteUser = new RemoteRequester(socket);
                remoteUser.start();
                contact = true;
                phoneHome.remoteRequesterExists();
            }//while listening for requesters

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }//run

//Inner class representing a remote node to contact this node.
//Several nodes' requesters may contact this node's responder.
//The responder should start each remote requester in its own thread, but that is not supported.

    class RemoteRequester extends Thread { //Belongs to the responder

        private final Socket socket; //The requester socket of the responder
        private boolean carryOn = true;
        public RemoteRequester(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            try {
                DataInputStream instream = new DataInputStream(socket.getInputStream());
                DataOutputStream outstream = new DataOutputStream(socket.getOutputStream());

                //Run conversation responder-side
                while (carryOn) {
                    //Waiting for message from requester (remote user)
                    String request = (String) instream.readUTF();
                    phoneHome.updateUI("Requester says: " + request, true);

                    //response = CommunicationHandler.getInstance().generateResponse(request);

                    //Waiting for the user to type
                    synchronized (this){
                        wait();
                    }
                    phoneHome.updateUI("Reply to requester : " + message, true);

                    //Write message (answer) to client
                    outstream.writeUTF(message);
                    outstream.flush();
                }
                //Closing everything down
                socket.close();
                phoneHome.updateUI("RESPONDER: Remote requester socket closed", true);
                instream.close();
                phoneHome.updateUI("RESPONDER: Remote requester inputstream closed", true);
                outstream.close();
                phoneHome.updateUI("RESPONDER: Remote requester outputstream closed", true);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
     }//MyResponderThread
}
