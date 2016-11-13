package agents;

import domain.Interest;
import gui.OnInput;
import gui.ProfilerApp;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import javafx.application.Application;
import messages.Message;
import messages.MessageType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by victoraxelsson on 2016-11-09.
 */
public class Profiler extends Agent implements OnInput{

    List<AID> curators = new ArrayList<>();
    List<AID> tourGuides = new ArrayList<>();
    private static int counter = 1;

    private ProfilerApp app;

    private static String CURATOR_TYPE = "curator";
    private static String TOUR_GUIDE_TYPE = "virtual_tour";


    @Override
    protected void setup() {
        //Ask TourGuide for personalized virtual tours
        //Ask the Curator about detailed information of items in the tour

        createSubscription(CURATOR_TYPE);
        createSubscription(TOUR_GUIDE_TYPE);

        //First search for curators then tours
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

        updateServiceProviders();

        //Start the message loop
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {

                    //We got a new curator or tour guide
                    if(msg.getSender().getName().equalsIgnoreCase(getDefaultDF().getName())){
                        updateServiceProviders();
                    }else{

                        //we got some other kind of message
                        deliverMessage(msg);
                    }
                }else {
                    block();
                }
            }
        });
        startGui();
    }



    private void startGui(){
        new Thread(){
            @Override
            public void run() {
                Application.launch(ProfilerApp.class);
            }
        }.start();


        addBehaviour(new TickerBehaviour(this, 100) {
            @Override
            protected void onTick() {
                if(ProfilerApp.getInstance() != null){
                    System.out.println("Application have loaded");
                    ProfilerApp.getInstance().setOnInput(Profiler.this);
                    stop();
                }else {
                    System.out.println("GUI have not loaded yet");
                }
            }
        });
    }

    private void deliverMessageToApp(String msg){
        if( ProfilerApp.getInstance() != null){
            ProfilerApp.getInstance().setLabel(msg);
        }
    }

    private void deliverMessage(ACLMessage msg){
        String content = msg.getContent();
        Message parsed = Message.fromString(content);

        if (parsed != null){
            if (MessageType.TourRequestReplyGuide.equals(parsed.getType())) {
                deliverMessageToApp("Here is the tour: \n" + parsed.getContent());
                System.out.println("Profiler received tour guide reply: " + parsed.getContent());
                deliverMessageToApp("If you want more info about some artifact: \nSYNPOSIS: info:<artifact name>");
            } else if (MessageType.InfoRequestReply.equals(parsed.getType())) {
                deliverMessageToApp("Here is more infor on the tour: \n" + parsed.getContent());
                System.out.println("Profiler received info reply: " + parsed.getContent());
            }
        }
    }

    private void createSubscription(String type){
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        dfd.addServices(sd);
        SearchConstraints sc = new SearchConstraints();
        sc.setMaxResults(new Long(1));

        send(DFService.createSubscriptionMessage(this, getDefaultDF(), dfd, sc));
    }

    private void updateServiceProviders(){
        ParallelBehaviour update = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
        update.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                searchCurators();
            }
        });
        update.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                searchTours();
            }
        });
        addBehaviour(update);
    }

    private void searchCurators() {
        System.out.println("Searching the curators");
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(CURATOR_TYPE);
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            List<AID> newCurators = new ArrayList<>();
            for (int i = 0; i < result.length; ++i) {
                newCurators.add(result[i].getName());
            }
            curators = newCurators;
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void queryForInfo(String artifactName) {
        /*
        int startIndex = content.indexOf("name='") + "name='".length();
        int endIndex = content.indexOf('\'', startIndex);
        String name = content.substring(startIndex, endIndex);
        */

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(curators.get(0));
        msg.setLanguage("English");
        msg.setContent((new Message(MessageType.InfoRequest, artifactName)).toString());
        send(msg);
    }

    private void searchTours() {
        System.out.println("Searching tours");
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(TOUR_GUIDE_TYPE);
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);

            List<AID> newTourGuides = new ArrayList<>();
            for (int i = 0; i < result.length; ++i) {
                newTourGuides.add(result[i].getName());
            }
            tourGuides = newTourGuides;

            //Just take the first one, if there is any
            if (tourGuides != null && tourGuides.size() > 0) {

                String names = "";
                for (int i = 0; i < tourGuides.size(); i++){
                    names += tourGuides.get(i).getLocalName() + ",";
                }
                deliverMessageToApp("I got some new tour guides. Which one do you want? " + names + "\n\nSYNOPSIS: tourguide:<name>");
            } else {
                System.out.println("I couldn't find any tour guides");
            }

        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void askForTour(AID tourGuide) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(tourGuide);
        msg.setLanguage("English");
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                Message message = new Message(MessageType.TourRequestGuide,
                        String.valueOf(Interest.getRandom()));
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

    private boolean isValidCommand(String msg){
        return msg.contains(":");
    }

    @Override
    public void onCommand(String msg) {

        if(isValidCommand(msg)){
            String[] parts = msg.split(":");

            switch (parts[0]){
                case "info":
                    queryForInfo(parts[1]);
                    break;
                case "tourguide":
                    AID guide = null;
                    for (int i = 0; i < tourGuides.size(); i++){
                        if(tourGuides.get(i).getLocalName().equalsIgnoreCase(parts[1])){
                            guide = tourGuides.get(i);
                            break;
                        }
                    }
                    if(guide != null){
                        askForTour(tourGuides.get(0));
                    }else{
                        deliverMessageToApp("There is no such tour guide");
                    }

                    break;
                default:
                    deliverMessageToApp("I could't find that command");
            }

        }else {
            deliverMessageToApp("That is not a valid message");
        }
    }
}
