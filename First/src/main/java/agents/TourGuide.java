package agents;

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

import java.util.*;

/*
[Tour guide agent]
Register virtual tour
Add behaviour to listen for requests from profiler agent (Matching interest)
Add behaviour that builds virtual tour for profiler (communicate with curator to get list of artifacts, Send receive)
domain.Interest: Flower, Portraits etc
*/
public class TourGuide extends Agent {

    List<AID> curators = new ArrayList<>();
    Map<String, AID> requests = new HashMap<>();

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
                            requests.put(msg.getConversationId(), msg.getSender());
                            queryForTour(msg.getConversationId(), parsed.getContent());
                        } else if (MessageType.TourRequestReplyCurator.equals(parsed.getType())) {
                            System.out.println("Tour guide received reply from curator");
                            replyProfiler(msg.getConversationId(), parsed);
                        }
                }
                else {
                    block();
                }
            }
        });

        //Ask the curator to build a virtual tour
    }

    private void queryForTour(String conversationId, String content) {
        Message message = new Message(MessageType.TourRequestCurator, content);

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(curators.get(0));
        msg.setConversationId(conversationId);
        msg.setLanguage("English");
        msg.setOntology("Weather-forecast-ontology");
        msg.setContent(message.toString());
        send(msg);
    }

    private void replyProfiler(String conversationId, Message parsed) {
        if (requests.containsKey(conversationId)) {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(requests.get(conversationId));
            msg.setLanguage("English");
            msg.setOntology("Weather-forecast-ontology");
            msg.setContent((new Message(MessageType.TourRequestReplyGuide, parsed.getContent())).toString());
            this.send(msg);
            requests.remove(conversationId);
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
