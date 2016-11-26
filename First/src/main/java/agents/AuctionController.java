package agents;

import behaviours.IdleBehaveiour;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Created by victoraxelsson on 2016-11-26.
 */
public class AuctionController extends Agent {


    private static final String STATE_IDLING = "idleing";
    private static final String STATE_CREATE_CONTAINERS = "createContainers";
    private static final String STATE_CLONE_AGENTS = "cloneAgents";
    private static final String STATE_WAIT_FOR_RESPONSE = "waitForResponse";
    private static final String STATE_DO_CLEANUP = "doCleanup";

    @Override
    protected void setup() {
        super.setup();


        FSMBehaviour fsm = new FSMBehaviour(this);

        //Start by idling
        fsm.registerFirstState(new IdleBehaveiour(), STATE_IDLING);

        fsm.registerState(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("create containers");
            }
        }, STATE_CREATE_CONTAINERS);

        fsm.registerState(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("clone the agents");
            }
        }, STATE_CLONE_AGENTS);

        fsm.registerState(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("wait for response");
            }
        }, STATE_WAIT_FOR_RESPONSE);

        fsm.registerState(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("do cleanup");
            }
        }, STATE_DO_CLEANUP);


        //idling
        //Create new container
        //Clone all the agents and move them
        //wait for response
        //Do cleanup

        fsm.registerDefaultTransition(STATE_IDLING, STATE_CREATE_CONTAINERS);
        fsm.registerDefaultTransition(STATE_CREATE_CONTAINERS, STATE_CLONE_AGENTS);
        fsm.registerDefaultTransition(STATE_CLONE_AGENTS, STATE_WAIT_FOR_RESPONSE);
        fsm.registerDefaultTransition(STATE_WAIT_FOR_RESPONSE, STATE_DO_CLEANUP);
        fsm.registerDefaultTransition(STATE_DO_CLEANUP, STATE_IDLING);

        addBehaviour(fsm);
    }
}
