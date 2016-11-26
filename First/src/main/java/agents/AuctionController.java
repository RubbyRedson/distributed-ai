package agents;

import behaviours.IdleBehaveiour;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.ProfileImpl;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.domain.mobility.CloneAction;
import jade.domain.mobility.MobileAgentDescription;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by victoraxelsson on 2016-11-26.
 */
public class AuctionController extends Agent {

    private AgentContainer home;
    private AgentContainer containerOne, containerTwo;

    private Map locations = new HashMap();
    private Vector agents = new Vector();

    private static final String STATE_IDLING = "idleing";
    private static final String STATE_CREATE_CONTAINERS = "createContainers";
    private static final String STATE_CLONE_AGENTS = "cloneAgents";
    private static final String STATE_WAIT_FOR_RESPONSE = "waitForResponse";
    private static final String STATE_DO_CLEANUP = "doCleanup";

    jade.core.Runtime runtime = jade.core.Runtime.instance();

    @Override
    protected void setup() {
        super.setup();

        setupContainers();


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
                cloneAgentsIntoContainers();
            }
        }, STATE_CLONE_AGENTS);

        fsm.registerState(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("wait for response");
            }
        }, STATE_WAIT_FOR_RESPONSE);

        fsm.registerLastState(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println("do cleanup");

                for(int i = 0; i < agents.size(); i++){
                    //killAgent((String)agents.get(i));
                }
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
        //fsm.registerDefaultTransition(STATE_DO_CLEANUP, STATE_IDLING);

        addBehaviour(fsm);
    }

    private void cloneAgentsIntoContainers() {
        cloneAgent("Container-2", "curator1");
        cloneAgent("Container-2", "curator2");
    }


    private void cloneAgent(String destName, String agentName){
        AID aid = new AID(agentName, AID.ISLOCALNAME);
        Location dest = (Location)locations.get(destName);
        MobileAgentDescription mad = new MobileAgentDescription();
        mad.setName(aid);
        mad.setDestination(dest);
        String newName = "Clone-"+agentName;
        CloneAction ca = new CloneAction();
        ca.setNewName(newName);
        ca.setMobileAgentDescription(mad);
        sendRequest(new Action(aid, ca));

        agents.add(newName);
    }

    private void createAgentsInHome() {
        //Agents for container one
        AgentController curator1 = getAgent(
                "curator1",
                Curator.class.getName(),
                home
        );
        AgentController curator2 = getAgent(
                "curator2",
                Curator.class.getName(),
                home
        );
        AgentController artistManager = getAgent(
                "artistManager",
                ArtistManager.class.getName(),
                home
        );
    }

    private AgentController getAgent(String name, String className, AgentContainer container){
        AgentController a = null;
        try {
            Object[] args = new Object[2];
            args[0] = getAID();
            a = container.createNewAgent(name, className, args);
            a.start();
            agents.add(name);
        }
        catch (Exception ex) {
            System.out.println("Problem creating new agent");
        }

        return a;
    }

    private void killAgent(String agentName){
        System.out.println("The killAgent don't work");
        AID aid = new AID(agentName, AID.ISLOCALNAME);
        KillAgent ka = new KillAgent();
        ka.setAgent(aid);
        sendRequest(new Action(aid, ka));
        agents.remove(agentName);
    }


    private void setupContainers(){
        //Register the language and ontology
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(MobilityOntology.getInstance());

        home = runtime.createAgentContainer(new ProfileImpl());
        containerOne = runtime.createAgentContainer(new ProfileImpl());
        containerTwo = runtime.createAgentContainer(new ProfileImpl());

        doWait(2000);

        try {
            getLocations();
        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        }

        createAgentsInHome();
    }


    private void getLocations() throws Codec.CodecException, OntologyException {
        sendRequest(new Action(getAMS(), new QueryPlatformLocationsAction()));

        //Receive response from AMS
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchSender(getAMS()),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        ACLMessage resp = blockingReceive(mt);
        ContentElement ce = getContentManager().extractContent(resp);
        Result result = (Result) ce;
        jade.util.leap.Iterator it = result.getItems().iterator();
        while (it.hasNext()) {
            Location loc = (Location)it.next();
            locations.put(loc.getName(), loc);
        }
    }

    void sendRequest(Action action) {
// ---------------------------------

        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.setLanguage(new SLCodec().getName());
        request.setOntology(MobilityOntology.getInstance().getName());
        try {
            getContentManager().fillContent(request, action);
            request.addReceiver(action.getActor());
            send(request);
        }
        catch (Exception ex) { ex.printStackTrace(); }
    }
}
