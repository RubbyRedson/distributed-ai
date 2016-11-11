package messages;

/**
 * Created by Nick on 11/10/2016.
 */
public class Message {
    private MessageType type;
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


    @Override
    public String toString() {
        return type + "\r\n" + content;
    }

    public static Message fromString(String msg) {
        Message message;
        String[] separated = msg.split("\r\n");
        if (separated.length < 1) return null;

        MessageType type = MessageType.valueOf(separated[0]);
        String content = separated[1];
        message = new Message(type, content);
//        System.out.println("---------------------------------\n" +
//                "Parsed message:\n" + message + "\n---------------------------------");
        return message;
    }
}
