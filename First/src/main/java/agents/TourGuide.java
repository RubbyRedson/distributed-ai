package agents;

import domain.Artifact;
import domain.Interests;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import messages.Message;
import messages.MessageType;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/*
[Tour guide agent]
Register virtual tour
Add behaviour to listen for requests from profiler agent (Matching interest)
Add behaviour that builds virtual tour for profiler (communicate with curator to get list of artifacts, Send receive)
domain.Interests: Flower, Portraits etc
*/
public class TourGuide extends Agent {

    List<AID> curators = new ArrayList<>();

    private void searchCurators() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("curator");
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            for (int i = 0; i < result.length; ++i) {
                if (!curators.contains(result[i].getName()))
                    curators.add(result[i].getName());
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void setup() {
        registerTourService();
        searchCurators();

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {

                    // Message received. Process it
                    String content = msg.getContent();
                    Message parsed = Message.fromString(content);
                    if (parsed != null)
                        if (MessageType.TourRequestGuide.equals(parsed.getType())) {
                            System.out.println("Tour guide received request from profiler");
                            queryForTour(msg.getSender(), parsed.getContent());
                        } else if (MessageType.TourRequestReplyCurator.equals(parsed.getType())) {
                            System.out.println("Tour guide received reply from curator");
                            replyProfiler(parsed);
                        }
                }
                else {
                    block();
                }
            }
        });

        //Ask the curator to build a virtual tour
    }

    private void queryForTour(AID sender, String content) {
        Message message = new Message(MessageType.TourRequestCurator, content);
        message.setInterstedParty(sender);

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(curators.get(0));
        msg.setLanguage("English");
        msg.setOntology("Weather-forecast-ontology");
        msg.setContent(message.toString());
        send(msg);
    }

    private void replyProfiler(Message parsed) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(parsed.getInterstedParty());
        msg.setLanguage("English");
        msg.setOntology("Weather-forecast-ontology");
        msg.setContent((new Message(MessageType.TourRequestReplyGuide, parsed.getContent())).toString());
        this.send(msg);
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
