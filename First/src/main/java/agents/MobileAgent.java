package agents;


import gui.MobileAgentGui;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.domain.mobility.CloneAction;
import jade.domain.mobility.MobilityOntology;
import jade.domain.mobility.MoveAction;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class MobileAgent extends GuiAgent {
// ----------------------------------------

    private AID controller;
    private Location destination;
    transient protected MobileAgentGui myGui;

    protected void setup() {
// ------------------------

        // Retrieve arguments passed during this agent creation
        Object[] args = getArguments();
        controller = (AID) args[0];
        destination = here();

        init();

        // Program the main behaviour of this agent
        addBehaviour(new ReceiveCommands(this));
    }

    void init() {
// -------------

        // Register language and ontology
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(MobilityOntology.getInstance());

        // Create and display the gui
        myGui = new MobileAgentGui(this);
        myGui.setVisible(true);
        myGui.setLocation(destination.getName());
    }

    protected void onGuiEvent(GuiEvent e) {
// ---------------------------------------
        //No interaction with the gui
    }

    protected void beforeMove() {
// -----------------------------

        System.out.println("Moving now to location : " + destination.getName());
        myGui.setVisible(false);
        myGui.dispose();
    }

    protected void afterMove() {
// ----------------------------

        init();
        myGui.setInfo("Arrived at location : " + destination.getName());
    }

    protected void beforeClone() {
// -----------------------------

        myGui.setInfo("Cloning myself to location : " + destination.getName());
    }

    protected void afterClone() {
// ----------------------------

        init();
    }


    /*
    * Receive all commands from the controller agent
    */
    class ReceiveCommands extends CyclicBehaviour {
// -----------------------------------------------

        ReceiveCommands(Agent a) { super(a); }

        public void action() {

            ACLMessage msg = receive(MessageTemplate.MatchSender(controller));

            if (msg == null) { block(); return; }

            if (msg.getPerformative() == ACLMessage.REQUEST){

                try {
                    ContentElement content = getContentManager().extractContent(msg);
                    Concept concept = ((Action)content).getAction();

                    if (concept instanceof CloneAction){

                        CloneAction ca = (CloneAction)concept;
                        String newName = ca.getNewName();
                        Location l = ca.getMobileAgentDescription().getDestination();
                        if (l != null) destination = l;
                        doClone(destination, newName);
                    }
                    else if (concept instanceof MoveAction){

                        MoveAction ma = (MoveAction)concept;
                        Location l = ma.getMobileAgentDescription().getDestination();
                        if (l != null) doMove(destination = l);
                    }
                    else if (concept instanceof KillAgent){

                        myGui.setVisible(false);
                        myGui.dispose();
                        doDelete();
                    }
                }
                catch (Exception ex) { ex.printStackTrace(); }
            }
            else { System.out.println("Unexpected msg from controller agent"); }
        }
    }

} // class MobileAgent