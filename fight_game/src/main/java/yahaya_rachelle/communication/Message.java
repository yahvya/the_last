package yahaya_rachelle.communication;

import java.io.Serializable;
import java.net.Socket;

import yahaya_rachelle.communication.Communicator.MessageType;

/**
 * représente un message de l'application
 */
public class Message implements Serializable{

    private MessageType messageType;

    private Object messageData;

    private Socket source;

    public Message(MessageType messageType,Object messageData){
        this.messageType = messageType;
        this.messageData = messageData;
    }

    public Message setSource(Socket source){
        this.source = source;

        return this;
    }

    public MessageType getMessageType(){
        return this.messageType;
    }

    public Object getMessageData(){
        return this.messageData;
    }

    /**
     * 
     * @return retourne la source du message
     */
    public Socket getSource(){
        return this.source;
    }
}
