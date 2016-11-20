package agents;

import behaviours.*;
import domain.Artifact;
import domain.ArtistArtifact;
import domain.OnDone;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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

    private static final int INITIAL_BUDGET = 3000;

    private List<ArtistArtifact> allCreatedArtifacts;
    private List<AID> auctionneers;
    private ArtistArtifact artifact;
    private int budget;
    private int currAuctionPrice;
    private AuctioneerStrategy strategy;
    private Random random = new SecureRandom();

    @Override
    protected void setup() {

        budget = INITIAL_BUDGET;
        currAuctionPrice = -1;
        allCreatedArtifacts = new ArrayList<>();

        FSMBehaviour fsm = new FSMBehaviour(this);

        //Start by idling
        fsm.registerFirstState(new IdleBehaveiour(), STATE_IDLING);

        //Create a new artifact
        fsm.registerState(new CreateArtworkBehaviour(this, new OnDone<ArtistArtifact>() {
            @Override
            public void done(ArtistArtifact _artifact) {
                artifact = _artifact;

                fsm.registerTransition(STATE_CREATE_ARTWORK, STATE_INFORMING, 5);

                //The artist manager is bankrupt
                fsm.registerTransition(STATE_CREATE_ARTWORK, STATE_EXIT_AUCTION, 6);
            }
        }), STATE_CREATE_ARTWORK);

        //Inform all that it's about to start an auction
        fsm.registerState(new InformAuctionParticipants(this, new OnDone<String>() {
            @Override
            public void done(String message) {
                fsm.registerDefaultTransition(STATE_INFORMING, STATE_CALCULATE_PRICE);
            }
        }), STATE_INFORMING);


        //Find out the new auction price
        fsm.registerState(new CalculateAuctionPrice(this, new OnDone<Integer>() {
            @Override
            public void done(Integer newAuctionPrice) {
                currAuctionPrice = newAuctionPrice;

                fsm.registerTransition(STATE_CALCULATE_PRICE, STATE_CALL_FOR_PROPOSALS, 3);
                fsm.registerTransition(STATE_CALCULATE_PRICE, STATE_EXIT_AUCTION, 4);

            }
        }), STATE_CALCULATE_PRICE);

        //Deal with the bidding and figure out what agents should be part of the bidding
        fsm.registerState(new CallForProposals(this, new OnDone<String>() {
            @Override
            public void done(String message) {

                //If still no buyer
                fsm.registerTransition(STATE_CALL_FOR_PROPOSALS, STATE_CALCULATE_PRICE, 1);

                //If all is done
                fsm.registerTransition(STATE_CALL_FOR_PROPOSALS, STATE_EXIT_AUCTION, 2);
            }
        }), STATE_CALL_FOR_PROPOSALS);

        //Reset everything before the new round of auction
        fsm.registerState(new OneShotBehaviour(this){
            @Override
            public void action() {
                //TODO notify every participant about end of auction with the message of type: INFORM and content: Auction ended
                System.out.println("exiting the auction. Budget: " + budget);
                //Set new budget,

                if(artifact != null){
                    allCreatedArtifacts.add(artifact);
                    budget -= artifact.getProductionCost();
                }

                artifact = null;
                currAuctionPrice = -1;

                fsm.registerDefaultTransition(STATE_EXIT_AUCTION, STATE_IDLING);
            }
        }, STATE_EXIT_AUCTION);

        //Flow of transitions
        fsm.registerDefaultTransition(STATE_IDLING, STATE_CREATE_ARTWORK);

        addBehaviour(fsm);
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
        if (random.nextFloat() > 0.5f) {
            System.out.println("Auctioneer chose sellHigh strategy");
            strategy = new SellHighQuality();
        } else {
            System.out.println("Auctioneer chose sellLow strategy");
            strategy = new SellLowQuality();
        }
    }
}
