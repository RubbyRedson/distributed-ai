package behaviours;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;

/**
 * Created by Nick on 11/27/2016.
 */
public class KillMessageReceiver extends MsgReceiver {
    public KillMessageReceiver(Agent a, MessageTemplate mt, long deadline, DataStore s, Object msgKey) {
        super(a, new MessageTemplate((MessageTemplate.MatchExpression) msg -> {
            if (msg.getPerformative() == ACLMessage.REQUEST) {
                try {
                    ContentElement content = a.getContentManager().extractContent(msg);
                    if (content == null) return false;
                    Concept concept = ((Action)content).getAction();
                    if (concept instanceof KillAgent){
                        return true;
                    }
                }
                catch (Exception ex) { ex.printStackTrace(); }
            }
            return false;
        }), deadline, s, msgKey);
    }

    @Override
    protected void handleMessage(ACLMessage msg) {
        this.getAgent().doDelete();
    }
}
