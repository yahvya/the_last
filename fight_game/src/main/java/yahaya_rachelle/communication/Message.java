package yahaya_rachelle.communication;

import java.io.Serializable;

import yahaya_rachelle.communication.Communicator.MessageType;

/**
 * représente un message de l'application
 */
public class Message implements Serializable{

    private MessageType messageType;

    private Object messageData;

    public Message(MessageType messageType,Object messageData){
        this.messageType = messageType;
        this.messageData = messageData;
    }

    public MessageType getMessageType(){
        return this.messageType;
    }

    public Object getMessageData(){
        return this.messageData;
    }
}
