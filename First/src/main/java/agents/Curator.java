package agents;

import domain.Artifact;
import domain.Interest;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import messages.Message;
import messages.MessageType;

import java.util.*;

/**
 * Created by victoraxelsson on 2016-11-09.
 */
public class Curator extends Agent {

    private Map<String, Artifact> collection = new HashMap<>();

    private String getTour(String unparsed) {
        Interest interest = Interest.valueOf(unparsed);
        List<Artifact> tour = new ArrayList<>();

        for (Artifact a : Artifact.getCollection()) {
            if (a.getType().equals(interest)) tour.add(a);
        }

        return tour.toString();
    }

    @Override
    protected void setup() {

        setCollection();

        //Register the service in the DF
        registerCuratorService();

        //Start listening for requests
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {


                    // Message received. Process it
                    String content = msg.getContent();
                    Message parsed = Message.fromString(content);
                    if (parsed != null)
                        if (MessageType.TourRequestCurator.equals(parsed.getType())) {
                            System.out.println("Curator received request from tour guide");
                            String interest = parsed.getContent();
                            reply(msg, replyTour(interest));
                        } else if (MessageType.InfoRequest.equals(parsed.getType())) {
                            System.out.println("Curator received request from profiler");
                            String name = parsed.getContent();
                            reply(msg, replyInfo(name));
                        }
                } else {
                    block();
                }
            }
        });
    }

    private Message replyTour(String interest) {
        Message reply = new Message(MessageType.TourRequestReplyCurator, getTour(interest));
        return reply;
    }

    private Message replyInfo(String name) {
        return new Message(MessageType.InfoRequestReply, String.valueOf(collection.get(name)));
    }

    private void reply(ACLMessage receivedMsg, Message reply) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(receivedMsg.getSender());
        if (receivedMsg.getConversationId() != null && !receivedMsg.getConversationId().isEmpty())
            msg.setConversationId(receivedMsg.getConversationId());
        msg.setLanguage("English");
        msg.setOntology("Weather-forecast-ontology");
        msg.setContent(reply.toString());
        this.send(msg);
    }

    private void setCollection() {
        for (Artifact a : Artifact.getCollection()) this.collection.put(a.getName(), a);
    }

    private void registerCuratorService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("curator");
        serviceDescription.setName("Curator");
        dfd.addServices(serviceDescription);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }
}
