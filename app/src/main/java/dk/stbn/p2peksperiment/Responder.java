package dk.stbn.p2peksperiment;

import android.app.Activity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class Responder implements Runnable {

    private MessageUpdate phoneHome; //Access to MainActivity to pass messages to UI
    int requesterNumber = 0; //rude
    RemoteRequester remoteUser; //Should be a list in order to handle more than one
    String message = "good morning";

    public Responder(MessageUpdate main) {
        CommunicationHandler.getInstance().resp = this;
        this.phoneHome = main;
    }

    public void setMessage(String usermessage){
        message = usermessage;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(4444);

            //Always be ready for next requester
            while (true) {
                phoneHome.updateUI("RESPONDER: start listening..", true);
                Socket socket = serverSocket.accept();//Accept is called when a requester connects
                phoneHome.updateUI("RESPONDER: connection accepted", true);
                requesterNumber++;
                remoteUser = new RemoteRequester(socket, requesterNumber);
                remoteUser.start();

            }//while listening for requesters

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }//run

    public void endConversation() {
        //TODO
    }
    public void setPhoneHome(Activity act){
        this.phoneHome = (MessageUpdate) act;
    }

//Inner class representing a remote node to contact tis node.
//Several nodes' requesters may contact this node's responder.
//The responder starts each remote requester in its own thread.

    class RemoteRequester extends Thread { //Belongs to the responder

        private final Socket socket; //The requester socket of the responder
        private int number; //This requester ID

        private boolean carryOn = true;
        public void endConversation(){
            carryOn = false;
        }
        public RemoteRequester(Socket socket, int number) {
            this.socket = socket;
            this.number = number;
        }
        public void run() {

            try {
                DataInputStream instream = new DataInputStream(socket.getInputStream());
                DataOutputStream outstream = new DataOutputStream(socket.getOutputStream());

                //Run conversation responder-side
                while (carryOn) {
                    //Recieving message from remote requester
                    String request = (String) instream.readUTF();
                    phoneHome.updateUI("Requester " + number + " says: " + request, true);
                    //Generating response
                    //response = CommunicationHandler.getInstance().generateResponse(request);
                    System.out.println("responder before wait");
                    synchronized (this){
                        wait();
                    }
                    System.out.println("responder after wait");
                    phoneHome.updateUI("Reply to requester " + number + ": " + message, true);
                    //Write message (answer) to client
                    outstream.writeUTF(message);
                    outstream.flush();
                    //Util.waitABit();//HINT You might want to remove this at some point
                }

                //Closing everything down
                socket.close();
                phoneHome.updateUI("RESPONDER: Remote requester " + number + " socket closed", true);
                instream.close();
                phoneHome.updateUI("RESPONDER: Remote requester " + number + " inputstream closed", true);
                outstream.close();
                phoneHome.updateUI("RESPONDER: Remote requester  " + number + "outputstream closed", true);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
     }//MyResponderThread
}
