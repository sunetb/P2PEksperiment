package dk.stbn.p2peksperiment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CommunicationHandler implements CommunicationInterface {

    //This is still almost a stub class
    String nodeID = "_";
    @Override
    public void assignID(String ip) {
        this.nodeID = Util.hashIt(ip);
    }

    @Override
    public String getId() {
        return this.nodeID;
    }






}
