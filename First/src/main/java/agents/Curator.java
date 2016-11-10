package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Created by victoraxelsson on 2016-11-09.
 */
public class Curator extends Agent {

    @Override
    protected void setup() {

        //Register the service in the DF


        //Start listening for requests
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    System.out.println("I got the message: " +  msg.getContent());
                    // Message received. Process it
                }
                else {
                    block();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }
}
