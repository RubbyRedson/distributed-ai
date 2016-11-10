package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 * Created by victoraxelsson on 2016-11-09.
 */
public class Profiler extends Agent {

    @Override
    protected void setup() {
        //Ask TourGuide for personalized virtual tours
        //Ask the Curator about detailed information of items in the tour

        //This should maybe be tick instead? Or just ask on user input (whatever that is)
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                searchTours();
            }
        });
    }

    private void searchTours() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("virtual_tour");
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            AID[] tourGuides = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                tourGuides[i] = result[i].getName();
            }

            //Just take the first one, if there is any
            if (tourGuides != null && tourGuides.length > 0) {
                askForTour(tourGuides[0]);
            } else {
                System.out.println("I couldn't find any tour guides");
            }

        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void askForTour(AID tourGuide) {
        System.out.println(tourGuide);
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(tourGuide);
        msg.setLanguage("English");
        msg.setOntology("Weather-forecast-ontology");

        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                msg.setContent(String.valueOf(Interests.getRandom()));
                send(msg);
            }
        });
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }
}
