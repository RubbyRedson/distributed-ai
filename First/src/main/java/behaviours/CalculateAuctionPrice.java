package behaviours;

import domain.Artifact;
import domain.Interest;
import domain.OnDone;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class CalculateAuctionPrice extends OneShotBehaviour {

    private Artifact artifact;
    private int budget;
    private OnDone<Integer> onDone;

    public CalculateAuctionPrice(Artifact artifact, int budget, OnDone<Integer> onDone){
        this.artifact = artifact;
        this.budget = budget;
        this.onDone = onDone;
    }

    @Override
    public void action() {
        System.out.println("Not implemented yet. I'm just giving it a hardcoded value");
        onDone.done(new Integer(1000));
    }


}
