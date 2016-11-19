package agents;

import behaviours.*;
import domain.Artifact;
import domain.OnDone;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class ArtistManager extends Agent {

    private static final String STATE_IDLING = "idleing";
    private static final String STATE_CREATE_ARTWORK = "createArtwork";
    private static final String STATE_INFORMING = "informing";
    private static final String STATE_CALCULATE_PRICE = "calcPrice";
    private static final String STATE_CALL_FOR_PROPOSALS = "callForProposals";
    private static final String STATE_EXIT_AUCTION = "exitAuction";

    private Artifact artifact;
    private int budget;
    private int currAuctionPrice;

    @Override
    protected void setup() {


        FSMBehaviour fsm = new FSMBehaviour(this);

        //Start by idling
        fsm.registerFirstState(new IdleBehaveiour(), STATE_IDLING);

        //Create a new artifact
        fsm.registerState(new CreateArtworkBehaviour(new OnDone<Artifact>() {
            @Override
            public void done(Artifact _artifact) {
                artifact = _artifact;
            }
        }), STATE_CREATE_ARTWORK);

        //Inform all that it's about to start an auction
        fsm.registerState(new InformAuctionParticipants(), STATE_INFORMING);

        //Find out the new auction price
        fsm.registerState(new CalculateAuctionPrice(artifact, budget, new OnDone<Integer>() {
            @Override
            public void done(Integer newAuctionPrice) {
                currAuctionPrice = newAuctionPrice;
            }
        }), STATE_CALCULATE_PRICE);

        //Deal with the bidding and figure out what agents should be part of the bidding
        fsm.registerState(new CallForProposals(), STATE_CALL_FOR_PROPOSALS);

        //Reset everything before the new round of auction
        fsm.registerState(new OneShotBehaviour(this){
            @Override
            public void action() {
                System.out.println("exiting");
                //Set new budget,
                artifact = null;
                currAuctionPrice = -1;
            }
        }, STATE_EXIT_AUCTION);

        //Flow of transitions
        fsm.registerDefaultTransition(STATE_IDLING, STATE_CREATE_ARTWORK);
        fsm.registerDefaultTransition(STATE_CREATE_ARTWORK, STATE_INFORMING);
        fsm.registerDefaultTransition(STATE_INFORMING, STATE_CALCULATE_PRICE);
        fsm.registerDefaultTransition(STATE_CALCULATE_PRICE, STATE_CALL_FOR_PROPOSALS);

        //If still no buyer
        fsm.registerTransition(STATE_CALL_FOR_PROPOSALS, STATE_CALCULATE_PRICE, 1);

        //If all is done
        fsm.registerTransition(STATE_CALL_FOR_PROPOSALS, STATE_EXIT_AUCTION, 2);

        //Restart the loop, go back to idling
        fsm.registerDefaultTransition(STATE_EXIT_AUCTION, STATE_IDLING);



        addBehaviour(fsm);


        System.out.println("Artist manager");
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }
}
