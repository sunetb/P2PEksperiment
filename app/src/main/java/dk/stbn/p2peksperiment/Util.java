package dk.stbn.p2peksperiment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

    static String readURL(String inputUrl){
        String value = "";
        URL url = null;
        try {
            url = new URL(inputUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            System.out.println(con);
            con.setRequestMethod("GET");
            InputStream i = con.getInputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(i));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
                value += inputLine + "\n";
            }
            in.close();
        } catch (ProtocolException ex) {
            throw new RuntimeException(ex);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return value;
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
