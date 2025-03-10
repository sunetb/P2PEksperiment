package dk.stbn.p2peksperiment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CommunicationHandler implements CommunicationInterface {

    private static CommunicationHandler instance;

    String nodeID = "_";
    //One node consist of a requester and a responder.
    // Both of them can talk to strangers


    //Shared data; Initialised by setState
    private CommunicationHandler(){}

    public static CommunicationHandler getInstance() {
        if(instance == null){
            instance = new CommunicationHandler();
        }
        return instance;
    }

    //This is still almost a stub class
    @Override
    public void assignID(String ip) {
        this.nodeID = Util.hashIt(ip);
    }

    @Override
    public String getId() {
        return this.nodeID;
    }

    @Override
    public String generateResponse(String req){
        if (req.equals("____Bye bye!!____")){

            return "See you";
        }
        if (req.equals("ID")){
            return "Res NodeID: " + getId();
        }
       //Default
        return Util.getFood();
    }

    @Override
    public String generateRequest(String response){
        if (response.equals("ID")){
            return "Req NodeID: " + getId();
        }
        return Util.getAnimal();
    }








}
