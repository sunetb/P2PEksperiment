package dk.stbn.p2peksperiment;

import android.app.Activity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/* NOTE:
* This class is VERY primitive and that is on purpose.
* The idea is give you a starting point to work from.
*
* Having this class and the interface is just
* a suggestion for how to keep all communication
* in one place. And somewhat well-defined.
* You may want to do it differently.
* */

public class CommunicationHandler implements CommunicationInterface {

    private static CommunicationHandler instance;

    //Housekeeping references
    Responder resp;
    Requester req;
    Activity act;

    String nodeID = "_";

    //Shared data
    private CommunicationHandler(){}

    public static CommunicationHandler getInstance() {
        if(instance == null){
            instance = new CommunicationHandler();
        }
        return instance;
    }

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
        //Default
        return Util.getAnimal();
    }
}
