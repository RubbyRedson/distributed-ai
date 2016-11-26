package agents;

import behaviours.*;
import domain.ArtistArtifact;
import domain.OnDone;
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
import stategies.AuctioneerStrategy;
import stategies.SellHighQuality;
import stategies.SellLowQuality;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class ArtistManager extends Agent implements ArtistState {

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
    DataStore store;

    @Override
    protected void setup() {

        store = new DataStore();
        budget = INITIAL_BUDGET;
        currAuctionPrice = -1;
        allCreatedArtifacts = new ArrayList<>();
        destination = here();
        // Register language and ontology
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(MobilityOntology.getInstance());

        addCloneMsgReceiver();

        FSMBehaviour fsm = new FSMBehaviour(this);

        //Start by idling
        fsm.registerFirstState(new IdleBehaveiour(), STATE_IDLING);

        //Create a new artifact
        fsm.registerState(new CreateArtworkBehaviour(this, new OnDone<ArtistArtifact>() {
            @Override
            public void done(ArtistArtifact _artifact) {
                artifact = _artifact;
            }
        }), STATE_CREATE_ARTWORK);

        //Inform all that it's about to start an auction
        fsm.registerState(new InformAuctionParticipants(this, new OnDone<String>() {
            @Override
            public void done(String message) {

            }
        }), STATE_INFORMING);


        //Find out the new auction price
        fsm.registerState(new CalculateAuctionPrice(this, new OnDone<Integer>() {
            @Override
            public void done(Integer newAuctionPrice) {
                currAuctionPrice = newAuctionPrice;
            }
        }), STATE_CALCULATE_PRICE);

        //Deal with the bidding and figure out what agents should be part of the bidding
        fsm.registerState(new CallForProposals(this, new OnDone<String>() {
            @Override
            public void done(String message) {

            }
        }), STATE_CALL_FOR_PROPOSALS);

        //Reset everything before the new round of auction
        fsm.registerState(new OneShotBehaviour(this){
            @Override
            public void action() {
                System.out.println("Exiting the auction. Budget: " + budget);
                //Set new budget,

                if(artifact != null){
                    allCreatedArtifacts.add(artifact);
                    budget -= artifact.getProductionCost();
                }

                artifact = null;
                currAuctionPrice = -1;
            }
        }, STATE_EXIT_AUCTION);


        //clone into other container


        //Flow of transitions
        fsm.registerDefaultTransition(STATE_IDLING, STATE_CREATE_ARTWORK);
        fsm.registerTransition(STATE_CREATE_ARTWORK, STATE_INFORMING, 5);
        //The artist manager is bankrupt
        fsm.registerTransition(STATE_CREATE_ARTWORK, STATE_EXIT_AUCTION, 6);
        fsm.registerDefaultTransition(STATE_INFORMING, STATE_CALCULATE_PRICE);
        fsm.registerTransition(STATE_CALCULATE_PRICE, STATE_CALL_FOR_PROPOSALS, 3);
        fsm.registerTransition(STATE_CALCULATE_PRICE, STATE_EXIT_AUCTION, 4);

        //If still no buyer
        fsm.registerTransition(STATE_CALL_FOR_PROPOSALS, STATE_CALCULATE_PRICE, 1);

        //If all is done
        fsm.registerTransition(STATE_CALL_FOR_PROPOSALS, STATE_EXIT_AUCTION, 2);
        fsm.registerDefaultTransition(STATE_EXIT_AUCTION, STATE_IDLING);



        addBehaviour(fsm);
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
                    return concept instanceof CloneAction;
                }
                return false;
            }
        });


        MsgReceiver cloneReceiver = new MsgReceiver(this, cloneTemplate, MsgReceiver.INFINITE, store, "auctionEnd"){
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
}
