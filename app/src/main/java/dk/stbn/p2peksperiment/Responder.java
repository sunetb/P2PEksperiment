package dk.stbn.p2peksperiment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class Responder implements Runnable {

    private MessageUpdate phoneHome;
    int clientNumber = 0;


    public Responder(MessageUpdate main) {
        this.phoneHome = main;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(4444);

            //Always be ready for next client
            while (true) {
                phoneHome.updateUI("SERVER: start listening..", true);
                Socket clientSocket = serverSocket.accept();//Accept is called when a client connects
                phoneHome.updateUI("SERVER connection accepted", true);
                clientNumber++;
                new RemoteClient(clientSocket, clientNumber, phoneHome).start();

            }//while listening for clients

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }//run
}//MyServerThread

//Enabling several clients to one server, by running the communication with each client in its own thread.
//Maybe a better name then RemoteClient would be "ClientConnection", "ClientSocket" or similar
class RemoteClient extends Thread { //This belongs to the server

    MessageUpdate phoneHome;
    private final Socket client; //The client socket of the server
    private int number; //This client's ID

    private boolean carryOn = true;
    public void endConversation(){
        carryOn = false;
    }
    public RemoteClient (Socket clientSocket, int number, MessageUpdate main) {
        this.client = clientSocket;
        this.number = number;
        this.phoneHome = main;
    }
    public void run() {

        try {
            DataInputStream instream = new DataInputStream(client.getInputStream());
            DataOutputStream outstream = new DataOutputStream(client.getOutputStream());

            //Run conversation server-side
            while (carryOn) {
                //Recieving message from remote client
                String request = (String) instream.readUTF();
                phoneHome.updateUI("Client " + number + " says: " + request, true);
                //Generating response
                String response = generateResponse(request);
                phoneHome.updateUI("Reply to client " + number + ": " + response, true);
                //Write message (answer) to client
                outstream.writeUTF(response);
                outstream.flush();
                Util.waitABit();//HINT You might want to remove this at some point
            }

            //Closing everything down
            client.close();
            phoneHome.updateUI("SERVER: Remote client " + number + " socket closed", true);
            instream.close();
            phoneHome.updateUI("SERVER: Remote client " + number + " inputstream closed", true);
            outstream.close();
            phoneHome.updateUI("SERVER: Remote client  " + number + "outputstream closed", true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateResponse(String req){
        if (req.equals("____Bye bye!!____")){
            carryOn = false;
            return "See you";
        }
        //HINT: This is where you could react to "signals" or "requests" from the client
        // E.g. some if(req.equals(...))-statements
        String resp =  Util.getFood(); //HINT: This is where you could choose an appropriate response
        return resp;
    }
}
