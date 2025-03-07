package dk.stbn.p2peksperiment;

public interface CommunicationInterface {
    //VERY primitive, almost a stub

    void assignID(String ip); //Called by the node itself

    //Called by other nodes
    String getId();

    String generateResponse (String request);

    String generateRequest (String response);

}
