package behaviours;

import domain.OnDone;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class CallForProposals extends OneShotBehaviour {

    OnDone<String> onDone;

    public CallForProposals( OnDone<String> onDone){
        this.onDone = onDone;
    }

    @Override
    public void action() {
        //Finish the auction or not
        System.out.println("Sending messages to all the particiapting agents and receiving their responses");
        onDone.done("mock done");
    }

    @Override
    public int onEnd() {
        //Return 1 if we still need to negotiate the price
        //Return 2 if we are done with the auction and should create a new one
        return 2;
    }
}
