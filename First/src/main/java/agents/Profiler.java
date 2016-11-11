package agents;

import domain.Interests;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import messages.Message;
import messages.MessageType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by victoraxelsson on 2016-11-09.
 */
public class Profiler extends Agent {

    List<AID> curators = new ArrayList<>();
    List<AID> tourGuides = new ArrayList<>();
    private static int counter = 1;

    private void searchCurators() {
        System.out.println("Searching the curators");
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
        //Ask TourGuide for personalized virtual tours
        //Ask the Curator about detailed information of items in the tour

        //This should maybe be tick instead? Or just ask on user input (whatever that is)
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {


            }
        });

        SequentialBehaviour seq = new SequentialBehaviour();
        seq.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                searchCurators();
            }
        });
        seq.addSubBehaviour(new WakerBehaviour(this, 1000) {
            @Override
            protected void onWake() {
                searchTours();
            }
        });
        addBehaviour(seq);

        ParallelBehaviour update = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);

        update.addSubBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                searchCurators();
            }
        });

        update.addSubBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                searchTours();
            }
        });

        addBehaviour(update);


        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    // Message received. Process it
                    String content = msg.getContent();
                    Message parsed = Message.fromString(content);
                    if (parsed != null)
                        if (MessageType.TourRequestReplyGuide.equals(parsed.getType())) {
                            System.out.println("Profiler received tour guide reply: " + parsed.getContent());
                            queryForInfo(parsed.getContent());
                        } else if (MessageType.InfoRequestReply.equals(parsed.getType())) {
                            System.out.println("Profiler received info reply: " + parsed.getContent());
                        }
                } else {
                    block();
                }
            }
        });
    }

    private void queryForInfo(String content) {
        int startIndex = content.indexOf("name='") + "name='".length();
        int endIndex = content.indexOf('\'', startIndex);
        String name = content.substring(startIndex, endIndex);
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(curators.get(0));
        msg.setLanguage("English");
        msg.setOntology("Weather-forecast-ontology");
        msg.setContent((new Message(MessageType.InfoRequest, name)).toString());
        send(msg);
    }

    private void searchTours() {
        System.out.println("Searching tours");
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("virtual_tour");
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            for (int i = 0; i < result.length; ++i) {
                if (!tourGuides.contains(result[i].getName())) tourGuides.add(result[i].getName());
            }

            //Just take the first one, if there is any
            if (tourGuides != null && tourGuides.size() > 0) {
                askForTour(tourGuides.get(0));
            } else {
                System.out.println("I couldn't find any tour guides");
            }

        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void askForTour(AID tourGuide) {
//        System.out.println(tourGuide);
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(tourGuide);
        msg.setLanguage("English");
        msg.setOntology("Weather-forecast-ontology");
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                Message message = new Message(MessageType.TourRequestGuide,
                        String.valueOf(Interests.getRandom()));
                msg.setConversationId(String.valueOf(counter++));
                msg.setContent(message.toString());
                send(msg);
            }
        });
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }
}
