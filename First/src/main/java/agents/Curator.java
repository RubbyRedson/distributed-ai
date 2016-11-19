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
    private int budget = 1000;

    private String getTour(String unparsed) {
        Interest interest = Interest.valueOf(unparsed);
        StringBuilder tour = new StringBuilder();

        for (Artifact a : Artifact.getCollection()) {
            if (a.getType().equals(interest)) tour.append(a.getName()).append("\n");
        }

        return tour.toString();
    }

    @Override
    protected void setup() {

        setCollection();

        //Register the service in the DF
        registerCuratorService();

        store = new DataStore();

        //Tour requests
        //addTourRequestFromTourGuideMsgReceiver();

        //Info requests
        //addInfoRequestMsgReceiver();

        //Start auction
        addStartAuctionMsgReceiver();

        //Call for bids
        addCFPMsgReceiver();

        //Bid accepted
        addBidAcceptedMsgReceiver();

        //Bid rejected
        addBidRejectedMsgReceiver();

        //End auction
        addEndAuctionMsgReceiver();

    }

    private void addEndAuctionMsgReceiver() {
        MessageTemplate endAuctionTemplate = new MessageTemplate(new MessageTemplate.MatchExpression() {
            @Override
            public boolean match(ACLMessage msg) {
                if (msg.getPerformative() == ACLMessage.INFORM && Objects.equals(msg.getOntology(), "auction") &&
                        Objects.equals(msg.getContent(), "Auction ended")) {
                    return true;
                }
                return false;
            }
        });


        MsgReceiver endAuctionReceiver = new MsgReceiver(this, endAuctionTemplate, MsgReceiver.INFINITE, store, "auctionEnd"){
            @Override
            protected void handleMessage(ACLMessage msg) {
                System.out.println("Auction ended and that is acceptable");
                budget += 250; //TODO balancing?
            }
        };
        addBehaviour(endAuctionReceiver);

    }

    private void addBidRejectedMsgReceiver() {
        MessageTemplate rejectBidTemplate = new MessageTemplate(new MessageTemplate.MatchExpression() {
            @Override
            public boolean match(ACLMessage msg) {
                if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL && Objects.equals(msg.getOntology(), "auction")) {
                    return true;
                }
                return false;
            }
        });


        MsgReceiver rejectBidReceiver = new MsgReceiver(this, rejectBidTemplate, MsgReceiver.INFINITE, store, "rejectBid"){
            @Override
            protected void handleMessage(ACLMessage msg) {
                System.out.println("Foiled again! I was too late!"); //bid was rejected
            }
        };
        addBehaviour(rejectBidReceiver);
    }

    private void addBidAcceptedMsgReceiver() {
        MessageTemplate acceptBidTemplate = new MessageTemplate(new MessageTemplate.MatchExpression() {
            @Override
            public boolean match(ACLMessage msg) {
                if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL && Objects.equals(msg.getOntology(), "auction")) {
                    return true;
                }
                return false;
            }
        });


        MsgReceiver acceptBidReceiver = new MsgReceiver(this, acceptBidTemplate, MsgReceiver.INFINITE, store, "acceptBid"){
            @Override
            protected void handleMessage(ACLMessage msg) {
                int price = 250; //TODO parse it
                System.out.println("Honey! Look what I have bought! " + msg.getContent()); //dang stuff
                budget -= price;
            }
        };
        addBehaviour(acceptBidReceiver);
    }

    private void addCFPMsgReceiver() {
        MessageTemplate cfpTemplate = new MessageTemplate(new MessageTemplate.MatchExpression() {
            @Override
            public boolean match(ACLMessage msg) {
                if (msg.getPerformative() == ACLMessage.CFP && Objects.equals(msg.getOntology(), "auction")) {
                    return true;
                }
                return false;
            }
        });

        MsgReceiver cfpReceiver = new MsgReceiver(this, cfpTemplate, MsgReceiver.INFINITE, store, "cfp"){
            @Override
            protected void handleMessage(ACLMessage msg) {
                ACLMessage reply = msg.createReply();
                String currentPrice = msg.getContent();
                boolean propose = computeProposal(currentPrice);

                if (propose) {
                    reply.setLanguage("English");
                    reply.setOntology("auction");
                    reply.setSender(this.getAgent().getAID());
                    reply.setPerformative(ACLMessage.PROPOSE);
                    this.getAgent().send(reply);
                }
            }
        };
        addBehaviour(cfpReceiver);

    }

    private void addStartAuctionMsgReceiver() {

        MessageTemplate startAuctionTemplate = new MessageTemplate(new MessageTemplate.MatchExpression() {
            @Override
            public boolean match(ACLMessage msg) {
                if (msg.getPerformative() == ACLMessage.INFORM && Objects.equals(msg.getOntology(), "auction") &&
                        Objects.equals(msg.getContent(), "A new action is about to start. Pick out your wallet")) {
                    return true;
                }
                return false;
            }
        });

        MsgReceiver startAuctionReceiver = new MsgReceiver(this, startAuctionTemplate, MsgReceiver.INFINITE, store, "startAuction"){
            @Override
            protected void handleMessage(ACLMessage msg) {
                ACLMessage reply = msg.createReply();
                reply.setLanguage("English");
                reply.setOntology("auction");
                reply.setSender(this.getAgent().getAID());
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("Boy am I ready");
                this.getAgent().send(reply);
            }
        };
        addBehaviour(startAuctionReceiver);
    }

    private void addTourRequestFromTourGuideMsgReceiver() {

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
                addBehaviour(this);
            }
        };

        addBehaviour(tourRequest);
    }

    private void addInfoRequestMsgReceiver() {
        MessageTemplate templateInfoRequest = new MessageTemplate(new MessageTemplate.MatchExpression() {
            @Override
            public boolean match(ACLMessage msg) {
                String content = msg.getContent();
                Message parsed = Message.fromString(content);
                return parsed != null && MessageType.InfoRequest.equals(parsed.getType());
            }
        });

        MsgReceiver infoRequest = new MsgReceiver(this, templateInfoRequest, MsgReceiver.INFINITE, store, "infoRequests"){
            @Override
            protected void handleMessage(ACLMessage msg) {
                String content = msg.getContent();
                Message parsed = Message.fromString(content);
                System.out.println("Curator received request from profiler");
                String name = parsed.getContent();
                reply(msg, replyInfo(name));
                addBehaviour(this);
            }
        };

        addBehaviour(infoRequest);
    }

    private boolean computeProposal(String price) {
        //I love buying stuff
        try {
            int parsed = Integer.parseInt(price);
            if (parsed < budget)
                return true;
            else return false;
        } catch (NumberFormatException e) {
            System.out.println(price + " is not a valid price!");
            return false;
        }
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
        serviceDescription.setName(getLocalName());
        dfd.addServices(serviceDescription);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception e) {}
    }
}
