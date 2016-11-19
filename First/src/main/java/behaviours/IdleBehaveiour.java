package behaviours;

import jade.core.behaviours.OneShotBehaviour;

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
