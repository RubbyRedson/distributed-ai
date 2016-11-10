package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/*
[Tour guide agent]
Register virtual tour
Add behaviour to listen for requests from profiler agent (Matching interest)
Add behaviour that builds virtual tour for profiler (communicate with curator to get list of artifacts, Send receive)
agents.Interests: Flower, Portraits etc
*/
public class TourGuide extends Agent {

    String[] interests;

    @Override
    protected void setup() {

        registerTourService();
        setInterestsFromArgs();

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    System.out.println("I got the message: " +  msg.getContent());
                    // Message received. Process it
                }
                else {
                    block();
                }
            }
        });

        //Ask the curator to build a virtual tour
    }

    private void setInterestsFromArgs(){
        Object[] args = getArguments();

        if(args != null && args.length > 0 ){
            interests = new String[args.length];
            for (int i = 0; i < args.length; i++){
                interests[i] = (String)args[i];
            }
        }else{
            System.out.println("There was no args");
        }
    }

    private void registerTourService(){
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("virtual_tour");
        serviceDescription.setName("VirtualTour");
        dfd.addServices(serviceDescription);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }
}
