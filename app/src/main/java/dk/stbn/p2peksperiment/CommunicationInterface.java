package dk.stbn.p2peksperiment;

public interface CommunicationInterface {
    //This is still almost a stub

    void assignID(String ip); //Called by the node itself

    //Called by other nodes
    String getId();

    String generateResponse (String request);

}
