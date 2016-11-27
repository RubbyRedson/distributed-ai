package behaviours;

import agents.ArtistState;
import domain.OnDone;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.Serializable;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class CallForProposals extends OneShotBehaviour implements Serializable {

    private ArtistState agentState;
    private int msgCounter;
    private int exitCondition;
    private AID auctionWinner;

    public CallForProposals(ArtistState agentState) {
        this.agentState = agentState;
    }

    @Override
    public void action() {
        //Finish the auction or not
        System.out.println("CFP");

        msgCounter = 0;
        exitCondition = 1;
        auctionWinner = null;

        ACLMessage message = getMessage();
        getAgent().send(message);
        startMessageLoop();

        System.out.println("Sending messages to all the particiapting agents and receiving their responses");
    }

    private void startMessageLoop() { //TODO doesn't seem to send cfp for the second round (no one proposed). works fine with 1 round though
        //Start listening for messages

        while (true) {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (msg.getInReplyTo() != null && msg.getInReplyTo().equalsIgnoreCase(getMessageId())) {
                    if (msg.getPerformative() == ACLMessage.PROPOSE) {
                        // We have a winner
                        exitCondition = 2;
                        auctionWinner = msg.getSender();
                        break;

                    } else if (msg.getPerformative() == ACLMessage.NOT_UNDERSTOOD) {
                        //continue,
                        //remove from list?
                    }

                    msgCounter++;
                }

                if (msgCounter >= agentState.getAuctioneers().size()) {
                    //Terminate the message loop, no one want this crappy piece of art
                    break;
                }
            }

        }
    }

    private String getMessageContent() {
        return agentState.getCurrAuctionPrice() + "";
    }

    private String getMessageId() {
        return "auction->" + agentState.getArtifact().getName();
    }

    private ACLMessage getMessage() {
        ACLMessage message = new ACLMessage(ACLMessage.CFP);
        message.setContent(getMessageContent());
        message.setLanguage("English");
        message.setOntology("auction");
        message.setSender(this.getAgent().getAID());
        message.setReplyWith(getMessageId());

        for (int i = 0; i < agentState.getAuctioneers().size(); i++) {
            message.addReceiver(agentState.getAuctioneers().get(i));
        }

        return message;
    }

    private ACLMessage getDenyMessage() {
        ACLMessage message = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
        message.setContent("lost:" + getMessageId());
        message.setLanguage("English");
        message.setOntology("auction");
        message.setSender(this.getAgent().getAID());

        for (int i = 0; i < agentState.getAuctioneers().size(); i++) {
            //We don't want to send this message to the winner
            if (!agentState.getAuctioneers().get(i).getName().equals(auctionWinner.getName())) {
                message.addReceiver(agentState.getAuctioneers().get(i));
            }
        }

        return message;
    }

    private ACLMessage getWinnerMessage() {
        ACLMessage message = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        message.setContent(agentState.getArtifact() + "\nPrice:" + agentState.getCurrAuctionPrice());
        message.setLanguage("English");
        message.setOntology("auction");
        message.setSender(this.getAgent().getAID());
        message.addReceiver(auctionWinner);

        agentState.setWinner(auctionWinner.getLocalName());

        return message;
    }

    @Override
    public int onEnd() {
        //Return 1 if we still need to negotiate the price
        //Return 2 if we are done with the auction and should create a new one

        if (exitCondition == 2) {
            //notify the losers
            ACLMessage denyMessage = getDenyMessage();
            getAgent().send(denyMessage);

            //notify the winner
            ACLMessage acceptMessage = getWinnerMessage();
            getAgent().send(acceptMessage);

            //end of auction
            ACLMessage endMessage = getEndMessage();
            getAgent().send(endMessage);
            agentState.onSoldArtifact();
        } else {

        }

        System.out.println("Exit Condition: " + exitCondition);


        return exitCondition;
    }

    private ACLMessage getEndMessage() {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setContent("Auction ended");
        message.setLanguage("English");
        message.setOntology("auction");
        message.setSender(this.getAgent().getAID());
        for (int i = 0; i < agentState.getAuctioneers().size(); i++) {
            message.addReceiver(agentState.getAuctioneers().get(i));
        }
        return message;
    }
}
