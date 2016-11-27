package queens;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;
import messages.Message;
import messages.MessageType;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Nick on 11/22/2016.
 */
public class Queen extends Agent {
    static List<AID> queens = new ArrayList<>();
    DataStore store;
    private int index;
    private int currentRow = -1;

    @Override
    protected void setup() {
        // do the setup
        registerService();
        queens.add(this.getAID());
        this.index = queens.indexOf(this.getAID());

        addMsgReceivers();

        addBehaviour(new WakerBehaviour(this, 3000) {
            @Override
            protected void onWake() {
                if (index == 0) {
                    start();
                }
            }
        });
    }

    private void start() {
        Board board = new Board(queens.size());
        int first = new SecureRandom().nextInt(queens.size());
        currentRow = first;
        board.occupy(index, first);
        System.out.println("Queen " + index + " is starting to find place. Board is \n" + board.printOut());
        try {
            moveNext(board);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addMsgReceivers() {
        store = new DataStore();
        MessageTemplate template = new MessageTemplate(new MessageTemplate.MatchExpression() {
            @Override
            public boolean match(ACLMessage msg) {
                return (msg.getPerformative() == ACLMessage.REQUEST && Objects.equals(msg.getOntology(), "queens"));
            }
        });

        MsgReceiver moveRequest = new MsgReceiver(this, template, MsgReceiver.INFINITE, store, "moveRequest"){
            @Override
            protected void handleMessage(ACLMessage msg) {
                try {
                    Board board = (Board) msg.getContentObject();
                    findPlace(board);
                } catch (UnreadableException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                addBehaviour(this);
            }
        };

        addBehaviour(moveRequest);
    }

    private void findPlace(Board board) throws IOException {
        System.out.println("Queen " + index + " is going to find place. Board is \n" + board.printOut());
        if (currentRow != -1) board.remove(index, currentRow);
        boolean placed = false;
        for (int row = currentRow == -1 ? 0 : currentRow; row < queens.size(); row++) {
            if (row != currentRow && board.checkSafe(index, row)) {
                board.occupy(index, row);
                currentRow = row;
                placed = true;
                break;
            }
        }

        if (placed) {
            if (board.checkFinished()) {
                System.out.println("All queens were placed. Board is \n" + board.printOut());
                //finished
            } else {
                //placed, but not finished -> move next queen
                if (index != queens.size() - 1)
                    moveNext(board);
                else
                    movePrevious(board);
            }
        } else {
            //not able to move, request previous to move
            currentRow = -1;
            if (index != 0)
                movePrevious(board);
            else
                moveNext(board);
        }
    }

    private void moveNext(Board board) throws IOException {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setOntology("queens");
        msg.setContentObject(board);
        msg.addReceiver(queens.get(index + 1));
        send(msg);
    }

    private void movePrevious(Board board) throws IOException {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setOntology("queens");
        msg.setContentObject(board);
        msg.addReceiver(queens.get(index - 1));
        send(msg);
    }


    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("queen");
        serviceDescription.setName(getLocalName());
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
