package behaviours;

import agents.ArtistState;
import domain.*;
import jade.core.behaviours.OneShotBehaviour;

import java.util.Date;

import static jade.core.Runtime.getDate;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class CreateArtworkBehaviour extends OneShotBehaviour {

    private OnDone<ArtistArtifact> onDone;

    private int exitCondition;


    private ArtistState agentState;

    public CreateArtworkBehaviour(ArtistState agentState, OnDone<ArtistArtifact> onDone){
        this.onDone = onDone;
        this.agentState = agentState;
        exitCondition = 5;
    }

    @Override
    public void action() {
        agentState.setAgentStrategy();
        ArtistArtifact artifact = null;

        if(agentState.getAgentStrategy().createHighPrice(agentState.getBudget())){
             artifact = new ArtistArtifact(ArtistArtifact.HIGH_QUALITY_COST, true, Helper.getHelper().getRandomInterestEnum(), Helper.getHelper().getRandomName(), Helper.getHelper().getRandomName(), getDate());
        }else{
             artifact = new ArtistArtifact(ArtistArtifact.LOW_QUALITY_COST, false, Helper.getHelper().getRandomInterestEnum(), Helper.getHelper().getRandomName(), Helper.getHelper().getRandomName(), getDate());
        }

        if(artifact.getProductionCost() > agentState.getBudget()){
            System.out.println("The Artist manager is bankrupt :(");
            artifact = null;
            exitCondition = 6;
        }

        onDone.done(artifact);
    }


    @Override
    public int onEnd() {
        return exitCondition;
    }
}
