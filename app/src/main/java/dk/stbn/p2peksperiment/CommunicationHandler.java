package dk.stbn.p2peksperiment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CommunicationHandler implements RidesharingInterface{

    String nodeID;
    @Override
    public boolean assignID(String ip) {
        try {
            nodeID = hashIt(ip);
        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    @Override
    public String getId(String nodeId) {
        //save or something with the caller ID... at a later point
        return nodeID;
    }




    //I got this from the internet
    static String hashIt (String data) {

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] stringAsBytes = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            stringAsBytes = data.getBytes(StandardCharsets.UTF_8);
        }
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
