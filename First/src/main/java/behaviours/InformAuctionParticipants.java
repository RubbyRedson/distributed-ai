package behaviours;

import domain.OnDone;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class InformAuctionParticipants extends OneShotBehaviour {

    private static final String CURATOR = "curator";

    private OnDone<String> onDone;

    public InformAuctionParticipants(OnDone<String> onDone){
        this.onDone = onDone;
    }

    @Override
    public void action() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(CURATOR);
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this.getAgent(), template);
            List<AID> auctionParticipants = new ArrayList<>();
            for (int i = 0; i < result.length; ++i) {
                auctionParticipants.add(result[i].getName());
            }

            //Start informing all the curators that an auction is about to start
            for (int i = 0; i < auctionParticipants.size(); i++) {
                AID currId = auctionParticipants.get(i);

                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.setContent(getMessage());
                message.setLanguage("English");
                message.setOntology("auction");
                message.setSender(this.getAgent().getAID());
                message.addReceiver(currId);
                this.getAgent().send(message);
            }

        } catch (FIPAException e) {
            e.printStackTrace();
        }

        onDone.done("Done with informing");
    }

    public String getMessage() {
        return "A new action is about to start. Pick out your wallet";
    }
}
