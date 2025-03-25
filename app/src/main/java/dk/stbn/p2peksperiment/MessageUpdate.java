package dk.stbn.p2peksperiment;

public interface MessageUpdate {
    //Allows communication from other objects to MainActivity
    void updateUI(String message, boolean isResponder);
}
