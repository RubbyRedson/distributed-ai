package agents;

import domain.Artifact;
import domain.Interest;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import messages.Message;
import messages.MessageType;

import java.util.*;

/**
 * Created by victoraxelsson on 2016-11-09.
 */
public class Curator extends Agent {

    private Map<String, Artifact> collection = new HashMap<>();
    DataStore store;

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

        store = new DataStore();

        /*
        MessageTemplate tourRequesttemplate = new MessageTemplate(new MessageTemplate.MatchExpression() {
            @Override
            public boolean match(ACLMessage msg) {
                System.out.println("Received the msg");
                System.out.println(msg);
                String content = msg.getContent();
                Message parsed = Message.fromString(content);
                return parsed != null && MessageType.TourRequestCurator.equals(parsed.getType());
            }
        });
        */


        MessageTemplate template = new MessageTemplate(new MessageTemplate.MatchExpression() {
            @Override
            public boolean match(ACLMessage msg) {
                String content = msg.getContent();
                Message parsed = Message.fromString(content);
                return parsed != null && MessageType.TourRequestCurator.equals(parsed.getType());
            }
        });

        MsgReceiver tourRequest = new MsgReceiver(this, template, MsgReceiver.INFINITE, store, "tourRequests"){
            @Override
            protected void handleMessage(ACLMessage msg) {
                String content = msg.getContent();
                Message parsed = Message.fromString(content);
                System.out.println("Curator received request from tour guide");
                String interest = parsed.getContent();
                reply(msg, replyTour(interest));
            }
        };

        addBehaviour(tourRequest);

        addBehaviour(new WakerBehaviour(this, 6000) {
            @Override
            protected void onWake() {
                System.out.println("Trying the store");
                System.out.println(store);
            }
        });

        //MsgReceiver t = new MsgReceiver(this, template, store, 1000, "msgRes");


        //Start listening for requests
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {


                /*
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
                */
            }

        });

    }

    private Message replyTour(String interest) {
        return new Message(MessageType.TourRequestReplyCurator, getTour(interest));
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
