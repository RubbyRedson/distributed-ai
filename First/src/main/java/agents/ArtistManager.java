package agents;

import behaviours.*;
import domain.ArtistArtifact;
import domain.OnArtifactDone;
import domain.OnPriceCalculation;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.domain.mobility.CloneAction;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import jade.wrapper.ControllerException;
import stategies.AuctioneerStrategy;
import stategies.SellHighQuality;
import stategies.SellLowQuality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class ArtistManager extends Agent implements ArtistState, OnArtifactDone, OnPriceCalculation {

    private static final String STATE_IDLING = "idleing";
    private static final String STATE_CREATE_ARTWORK = "createArtwork";
    private static final String STATE_INFORMING = "informing";
    private static final String STATE_CALCULATE_PRICE = "calcPrice";
    private static final String STATE_CALL_FOR_PROPOSALS = "callForProposals";
    private static final String STATE_EXIT_AUCTION = "exitAuction";

    private static final int INITIAL_BUDGET = 10000;

    private List<ArtistArtifact> allCreatedArtifacts;
    private List<AID> auctionneers;
    private ArtistArtifact artifact;
    private int budget;
    private int currAuctionPrice;
    private AuctioneerStrategy strategy;
    private Location destination;
    private static Location primeLocation;
    private DataStore store;
    private transient String containerName;
    private transient String winner;
    private transient Map<AID, Integer> receivedPrices;

    private void init() {
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(MobilityOntology.getInstance());
        try {
            containerName = getContainerController().getContainerName();
        } catch (ControllerException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void setup() {

        // Register language and ontology
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(MobilityOntology.getInstance());

        budget = INITIAL_BUDGET;
        store = new DataStore();
        currAuctionPrice = -1;

        allCreatedArtifacts = new ArrayList<>();
        destination = here();
        if (primeLocation == null) primeLocation = here();
        receivedPrices = new HashMap<>();

        init();

        addBehaviour(new CreateArtworkBehaviour(this, this));

        addCloneMsgReceiver();

        addClonePriceReceiver();
    }

    private void startAuction() {
        FSMBehaviour fsm = new FSMBehaviour(this);

        //Start by idling
        fsm.registerFirstState(new IdleBehaveiour(), STATE_IDLING);

        //Inform all that it's about to start an auction
        fsm.registerState(new InformAuctionParticipants(this), STATE_INFORMING);

        //Find out the new auction price
        fsm.registerState(new CalculateAuctionPrice(this, this), STATE_CALCULATE_PRICE);

        //Deal with the bidding and figure out what agents should be part of the bidding
        fsm.registerState(new CallForProposals(this), STATE_CALL_FOR_PROPOSALS);

        //Reset everything before the new round of auction
        fsm.registerState(new OneShotBehaviour(this) {
            @Override
            public void action() {

                System.out.println(containerName + ", Budget: " + budget + ", Artifact:" + artifact);

                //Set new budget,
                if (!here().equals(primeLocation)){
                    System.out.println("Acutioneers: ");
                    System.out.println(auctionneers);
                    for (AID aid : auctionneers) {
                        killAgent(aid);
                    }
                    doMove(primeLocation);
                }

                if (artifact != null) {
                    sendPriceToPrime(currAuctionPrice, winner);
//                    allCreatedArtifacts.add(artifact);
//                    budget -= artifact.getProductionCost();
                }


//                artifact = null;
//                currAuctionPrice = -1;
            }
        }, STATE_EXIT_AUCTION);

        //Flow of transitions
        fsm.registerDefaultTransition(STATE_IDLING, STATE_INFORMING);

        fsm.registerDefaultTransition(STATE_INFORMING, STATE_CALCULATE_PRICE);
        fsm.registerTransition(STATE_CALCULATE_PRICE, STATE_CALL_FOR_PROPOSALS, 3);
        fsm.registerTransition(STATE_CALCULATE_PRICE, STATE_EXIT_AUCTION, 4);

        //If still no buyer
        fsm.registerTransition(STATE_CALL_FOR_PROPOSALS, STATE_CALCULATE_PRICE, 1);

        //If all is done
        fsm.registerTransition(STATE_CALL_FOR_PROPOSALS, STATE_EXIT_AUCTION, 2);

        addBehaviour(new KillMessageReceiver(this, null, MsgReceiver.INFINITE, store, "onDeleteArtistManager"));

        addBehaviour(fsm);
    }

    public void sendPriceToPrime(int price, String winner) {
        System.out.println("sendPriceToPrime " + price + " " + winner);
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setContent("buyer:" + winner + ":myprice:" + price);
        message.setLanguage("English");
        message.setOntology("auction");
        message.setSender(getAID());
        message.addReceiver(new AID("artistManager", AID.ISLOCALNAME));
        this.send(message);

        for (AID aid : auctionneers) {
            killAgent(aid);
        }

        //Go kill yourself
        doDelete();
    }

    @Override
    protected void afterClone() {
        init();

        startAuction();
        System.out.println("My new container is: " + containerName);
    }

    private void addCloneMsgReceiver() {
        MessageTemplate cloneTemplate = new MessageTemplate(new MessageTemplate.MatchExpression() {
            @Override
            public boolean match(ACLMessage msg) {
                if (msg.getPerformative() == ACLMessage.REQUEST) {
                    ContentElement content = null;
                    try {
                        content = getContentManager().extractContent(msg);
                    } catch (Codec.CodecException e) {
                        e.printStackTrace();
                    } catch (OntologyException e) {
                        e.printStackTrace();
                    }
                    Concept concept = ((Action) content).getAction();
                    int i = 0;
                    return concept instanceof CloneAction;
                }
                return false;
            }
        });


        MsgReceiver cloneReceiver = new MsgReceiver(this, cloneTemplate, MsgReceiver.INFINITE, store, "onCloneArtistManager") {
            @Override
            protected void handleMessage(ACLMessage msg) {
                ContentElement content = null;
                try {
                    content = getContentManager().extractContent(msg);
                } catch (Codec.CodecException e) {
                    e.printStackTrace();
                } catch (OntologyException e) {
                    e.printStackTrace();
                }
                int i = 0;
                Concept concept = ((Action) content).getAction();


                CloneAction ca = (CloneAction) concept;
                String newName = ca.getNewName();
                Location l = ca.getMobileAgentDescription().getDestination();
                if (l != null) destination = l;

                doClone(destination, newName);

                addCloneMsgReceiver();
            }
        };

        addBehaviour(cloneReceiver);
    }

    private void addClonePriceReceiver() {
        MessageTemplate priceTemplate = new MessageTemplate(new MessageTemplate.MatchExpression() {
            @Override
            public boolean match(ACLMessage msg) {
                if (msg.getPerformative() == ACLMessage.INFORM) {
                    if (msg.getContent() != null) {
                        if (msg.getContent().contains("myprice:")) {
                            return true;
                        }
                    }
                }
                return false;
            }
        });


        MsgReceiver cloneReceiver = new MsgReceiver(this, priceTemplate, MsgReceiver.INFINITE, store, "onCloneArtistManager") {
            @Override
            protected void handleMessage(ACLMessage msg) {
                String[] parts = msg.getContent().split(":");
                String winner = parts[1];
                int price = Integer.parseInt(parts[3]);

                receivedPrices.put(new AID(winner, AID.ISLOCALNAME), price);
                System.out.println("Handled price \n" + receivedPrices.toString());

                if (receivedPrices.size() == 2){
                    int maxPrice = -1;
                    String maxName = "";
                    for (AID aid : receivedPrices.keySet()) {
                        if (receivedPrices.get(aid) > maxPrice) {
                            maxPrice = receivedPrices.get(aid);
                            maxName = aid.getName();
                        }
                    }
                    onAllPricesReceived(maxName, maxPrice);
                }

                addClonePriceReceiver();
            }
        };

        addBehaviour(cloneReceiver);
    }

    private void onAllPricesReceived(String cloneName, int price) {
        System.out.println("onAllPricesReceived " + receivedPrices.toString());
        receivedPrices.clear();
        allCreatedArtifacts.add(artifact);
        budget -= artifact.getProductionCost();

        if (price > -1) {
            budget += price;
            System.out.println("The new budget:" + budget);

            //Notify the local curator
            ACLMessage message = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
            message.setContent(getArtifact() + "\nPrice:" + price);
            message.setLanguage("English");
            message.setOntology("auction");
            message.setSender(getAID());

            String winner = "";

            if(cloneName.contains("curator1")){
                winner = "curator1";
            }else if(cloneName.contains("curator2")){
                winner = "curator2";
            }else {
                System.out.println("Something is wrong with the name");
            }

            message.addReceiver(new AID(winner, AID.ISLOCALNAME));

            send(message);
            notifyController(winner, price, true);
        } else {
            notifyController("", -1, false);
        }

        System.out.println("received prices after " + receivedPrices.toString());

    }

    private void notifyController(String winner, int price, boolean wasSold) {
        //Notify the local curator
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        if(wasSold){
            message.setContent("\n\n--auction_done--\n" + getArtifact() + "\nPrice:" + price + "\n" + "Winner: " + winner + "\n----\n");
        }else{
            message.setContent("\n\n--auction_failed--\n" + getArtifact());
        }
        message.setLanguage("English");
        message.setOntology("auction");
        message.setSender(getAID());
        message.addReceiver(new AID("ctrl", AID.ISLOCALNAME));
        send(message);

        //Create a new artwork so we don't sell the same one againaddBehaviour(new CreateArtworkBehaviour(this, this));
        addBehaviour(new CreateArtworkBehaviour(this, this));
    }


    @Override
    protected void takeDown() {
        super.takeDown();
    }

    @Override
    public ArtistArtifact getArtifact() {
        return artifact;
    }

    @Override
    public int getBudget() {
        return budget;
    }

    @Override
    public int getCurrAuctionPrice() {
        return currAuctionPrice;
    }

    @Override
    public void onSoldArtifact() {
        budget += currAuctionPrice;
    }

    @Override
    public List<AID> getAuctioneers() {
        return auctionneers;
    }

    @Override
    public void setAuctioneers(List<AID> auctioneers) {
        this.auctionneers = auctioneers;
    }

    @Override
    public AuctioneerStrategy getAgentStrategy() {
        return strategy;
    }

    @Override
    public void setAgentStrategy() {
        if (budget > ArtistArtifact.HIGH_QUALITY_COST) {
            System.out.println("Auctioneer chose sellHigh strategy");
            strategy = new SellHighQuality();
        } else {
            System.out.println("Auctioneer chose sellLow strategy");
            strategy = new SellLowQuality();
        }
    }

    @Override
    public void setWinner(String winner) {
        this.winner = winner;
    }

    @Override
    public String getContainerName() {
        return containerName;
    }

    @Override
    public void onDone(ArtistArtifact artifact) {
        this.artifact = artifact;
    }

    @Override
    public void onDone(int price) {
        this.currAuctionPrice = price;
    }

    private void killAgent(AID aid) {
        KillAgent ka = new KillAgent();
        ka.setAgent(aid);
        sendRequest(new Action(aid, ka));
    }

    void sendRequest(Action action) {
// ---------------------------------

        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.setLanguage(new SLCodec().getName());
        request.setOntology(MobilityOntology.getInstance().getName());
        try {
            getContentManager().fillContent(request, action);
            request.addReceiver(action.getActor());
            send(request);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
