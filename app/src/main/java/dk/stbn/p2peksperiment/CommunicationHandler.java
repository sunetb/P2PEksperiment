package dk.stbn.p2peksperiment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CommunicationHandler implements CommunicationInterface {

    String nodeID;
    @Override
    public void assignID(String ip) {
        this.nodeID = hashIt(ip);
    }

    @Override
    public String getId(String nodeId) {
        return this.nodeID;
    }




    //From the internet
    static String hashIt (String data) {

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] stringAsBytes = new byte[0];
            stringAsBytes = data.getBytes(StandardCharsets.UTF_8);

        byte[] encodedhash = digest.digest(stringAsBytes);

        StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
        for (int i = 0; i < encodedhash.length; i++) {
            String hex = Integer.toHexString(0xff & encodedhash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
