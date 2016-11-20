package behaviours;

import agents.ArtistState;
import domain.Artifact;
import domain.ArtistArtifact;
import domain.Interest;
import domain.OnDone;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class CalculateAuctionPrice extends OneShotBehaviour {
    private OnDone<Integer> onDone;
    private ArtistState agentState;
    int currAuctionPrice;

    private static final int priceReduction = 100;
    private static final int initial = 1000;

    private int exitState;

    public CalculateAuctionPrice(ArtistState agentState, OnDone<Integer> onDone){
        this.agentState = agentState;
        this.onDone = onDone;
        exitState = 3;
    }

    @Override
    public void action() {
        currAuctionPrice = agentState.getCurrAuctionPrice();

        //There is no price yet
        if (agentState.getCurrAuctionPrice() <= 0) {
            currAuctionPrice = agentState.getAgentStrategy().setPrice(agentState.getArtifact(),
                    agentState.getCurrAuctionPrice());
        }else{

            if(currAuctionPrice - priceReduction > agentState.getArtifact().getProductionCost()){
                currAuctionPrice -=priceReduction;
            }else{
                //I cannot go lower than my production cost :(
                System.out.println("Sorry, I cannot go lower than my production cost");
                exitState = 4;
            }
        }

        onDone.done(currAuctionPrice);
    }

    @Override
    public int onEnd() {
        return exitState;
    }
}
