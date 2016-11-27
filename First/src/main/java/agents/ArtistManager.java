package agents;

import behaviours.*;
import domain.ArtistArtifact;
import domain.OnArtifactDone;
import domain.OnDone;
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
import jade.domain.mobility.CloneAction;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import jade.wrapper.ControllerException;
import stategies.AuctioneerStrategy;
import stategies.SellHighQuality;
import stategies.SellLowQuality;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private static final int INITIAL_BUDGET = 2000;

    private List<ArtistArtifact> allCreatedArtifacts;
    private List<AID> auctionneers;
    private ArtistArtifact artifact;
    private int budget;
    private int currAuctionPrice;
    private AuctioneerStrategy strategy;
    private Location destination;
    private DataStore store;
    private transient String containerName;

    private void init(){
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

        store = new DataStore();
        budget = INITIAL_BUDGET;
        currAuctionPrice = -1;
        allCreatedArtifacts = new ArrayList<>();
        destination = here();

        init();

        addBehaviour(new CreateArtworkBehaviour(this, this));

        addCloneMsgReceiver();


    }

    private void startAuction(){
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
        fsm.registerLastState(new OneShotBehaviour(this){
            @Override
            public void action() {

                System.out.println(containerName + ", Budget: " + budget + ", Artifact:" + artifact);
                //Set new budget,

                if(artifact != null){
                    allCreatedArtifacts.add(artifact);
                    budget -= artifact.getProductionCost();
                }

                artifact = null;
                currAuctionPrice = -1;
            }
        }, STATE_EXIT_AUCTION);

        //Flow of transitions
        fsm.registerDefaultTransition(STATE_IDLING, STATE_INFORMING);

        //fsm.registerTransition(STATE_CREATE_ARTWORK, STATE_INFORMING, 5);
        //The artist manager is bankrupt
        //fsm.registerTransition(STATE_CREATE_ARTWORK, STATE_EXIT_AUCTION, 6);

        fsm.registerDefaultTransition(STATE_INFORMING, STATE_CALCULATE_PRICE);
        fsm.registerTransition(STATE_CALCULATE_PRICE, STATE_CALL_FOR_PROPOSALS, 3);
        fsm.registerTransition(STATE_CALCULATE_PRICE, STATE_EXIT_AUCTION, 4);

        //If still no buyer
        fsm.registerTransition(STATE_CALL_FOR_PROPOSALS, STATE_CALCULATE_PRICE, 1);

        //If all is done
        fsm.registerTransition(STATE_CALL_FOR_PROPOSALS, STATE_EXIT_AUCTION, 2);
        //fsm.registerDefaultTransition(STATE_EXIT_AUCTION, STATE_IDLING);

        addBehaviour(new KillMessageReceiver(this, null, MsgReceiver.INFINITE, store, "onDeleteArtistManager"));

        addBehaviour(fsm);
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
                if (msg.getPerformative() == ACLMessage.REQUEST ) {
                    ContentElement content = null;
                    try {
                        content = getContentManager().extractContent(msg);
                    } catch (Codec.CodecException e) {
                        e.printStackTrace();
                    } catch (OntologyException e) {
                        e.printStackTrace();
                    }
                    Concept concept = ((Action)content).getAction();
                    int i = 0;
                    return concept instanceof CloneAction;
                }
                return false;
            }
        });


        MsgReceiver cloneReceiver = new MsgReceiver(this, cloneTemplate, MsgReceiver.INFINITE, store, "onCloneArtistManager"){
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
                Concept concept = ((Action)content).getAction();


                CloneAction ca = (CloneAction)concept;
                String newName = ca.getNewName();
                Location l = ca.getMobileAgentDescription().getDestination();
                if (l != null) destination = l;

                doClone(destination, newName);

                addCloneMsgReceiver();
            }
        };

        addBehaviour(cloneReceiver);
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
    public void onDone(ArtistArtifact artifact) {
        this.artifact = artifact;
    }

    @Override
    public void onDone(int price) {
        this.currAuctionPrice = price;
    }
}
