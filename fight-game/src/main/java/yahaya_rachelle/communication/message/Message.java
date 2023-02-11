package yahaya_rachelle.communication.message;

import java.io.Serializable;
import java.net.Socket;

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

    /**
     * représente les types de messages pouvant être envoyé et reçu
     */
    public static enum MessageType{
        RECEIVE_COUNT_OF_PLAYERS_TO_ACCEPT, // réception du nombre de personnes à accepter
        CONFIRM_CAN_RECEIVE_CONNEXIONS, // confirmation du fait de pouvoir recevoir des connexion
        RECEIVE_IP_LIST, // réception de la list des ips auquels se connecter
        CONFIRM_CONNECT_TO_OTHERS, // confirmation de connexion aux autres
        RECEIVE_SIGNAL_TO_SHARE_PLAYER, // réception du signal d'envoi de son joueur aux autres
        RECEIVE_PLAYER, // réception d'un joueur,
        CONFIRM_RECEIVE_ALL_PLAYERS, // confirmation de réception de tous les joueurs,
        START_GAME, // début de partie,
        RECEIVE_PLAYER_ACTION, // réception d'une action d'un joueur,
        SAVE_GAME // préviens le joueur de lancer la sauvegarde chez lui
    };
}
