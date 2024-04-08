package dk.stbn.p2peksperiment;

public interface RidesharingInterface {
    boolean assignID(String ip); //Called by the node itself

    //Called by other nodes
    String getId(String nodeId);

}
