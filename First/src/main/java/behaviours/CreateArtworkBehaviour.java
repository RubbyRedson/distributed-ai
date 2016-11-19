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

    private static final int HIGH_QUALITY_COST = 3000;
    private static final int LOW_QUALITY_COST = 500;

    private ArtistState agentState;

    public CreateArtworkBehaviour(ArtistState agentState, OnDone<ArtistArtifact> onDone){
        this.onDone = onDone;
        this.agentState = agentState;
    }

    private boolean createHighQualityProduct(){
        return agentState.getBudget() > 1000;
    }

    @Override
    public void action() {

        ArtistArtifact artifact = null;
        if(createHighQualityProduct()){
             artifact = new ArtistArtifact(HIGH_QUALITY_COST, true, Helper.getHelper().getRandomInterestEnum(), Helper.getHelper().getRandomName(), Helper.getHelper().getRandomName(), getDate());
        }else{
             artifact = new ArtistArtifact(LOW_QUALITY_COST, false, Helper.getHelper().getRandomInterestEnum(), Helper.getHelper().getRandomName(), Helper.getHelper().getRandomName(), getDate());
        }

        onDone.done(artifact);
    }

}
