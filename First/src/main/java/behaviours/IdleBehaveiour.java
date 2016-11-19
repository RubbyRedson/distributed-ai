package behaviours;

import com.sun.xml.internal.bind.v2.model.core.ID;
import domain.OnDone;
import gui.OnInput;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class IdleBehaveiour extends OneShotBehaviour {

    public IdleBehaveiour(){}

    @Override
    public void action() {
        System.out.println("Idling");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
