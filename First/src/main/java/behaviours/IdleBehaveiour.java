package behaviours;

import jade.core.behaviours.OneShotBehaviour;

import java.io.Serializable;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class IdleBehaveiour extends OneShotBehaviour  implements Serializable {

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
