package messages;

import jade.core.AID;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.StringACLCodec;

import java.io.StringReader;

/**
 * Created by Nick on 11/10/2016.
 */
public class Message {
    private MessageType type;
    private AID interstedParty;
    private String content;

    public Message(MessageType type, String content) {
        this.type = type;
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public AID getInterstedParty() {
        return interstedParty;
    }

    public void setInterstedParty(AID interstedParty) {
        this.interstedParty = interstedParty;
    }

    @Override
    public String toString() {
        return type + "\r\n" + content + "\r\n" + interstedParty;
    }

    public static Message fromString(String msg) {
        Message message;
        String[] separated = msg.split("\r\n");
        if (separated.length < 1) return null;

        MessageType type = MessageType.valueOf(separated[0]);
        String content = separated[1];
        message = new Message(type, content);
        if (separated.length >= 2 && separated[2] != null && !"null".equalsIgnoreCase(separated[2])) {
            System.out.println("Try to parse AID from " + separated[2]);
            StringACLCodec codec = new StringACLCodec(new StringReader(separated[2]), null);
            try {
                AID aid_rec = codec.decodeAID();
                message.setInterstedParty(aid_rec);
            } catch (ACLCodec.CodecException e) {
                System.out.println("Failed to parse AID from " + separated[2]);
            }
        }
        System.out.println("---------------------------------\n" +
                "Parsed message:\n" + message + "\n---------------------------------");
        return message;
    }
}
