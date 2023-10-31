package yahaya_rachelle.communication.communication;

import java.util.HashMap;

import yahaya_rachelle.actor.Player;
import yahaya_rachelle.communication.message.Message;
import yahaya_rachelle.communication.message.Message.MessageType;
import java.util.ArrayList;

/**
 * échangeur match ia
 */
public class AiCommunicator extends Communicator{
    /**
     * liste des joueurs écoutés
     */
    private ArrayList<Communicator> listenedPlayersCommunicators;

    public AiCommunicator(HashMap<MessageType, MessageManager> messagesLinkedActionsMap, Player internalPlayer) {
        super(messagesLinkedActionsMap, internalPlayer);
        this.listenedPlayersCommunicators = new ArrayList<Communicator>();
    }   

    /**
     * simule le message d'entrée de partage du joueur
     * @param toListen le joueur à écouter
     * @param communicator communicateur lié au joueur
     * @return this
     */
    public AiCommunicator listenPlayer(Player toListen,Communicator communicator){
        this.listenedPlayersCommunicators.add(communicator);
        this.manageEntrantMessage(new Message(MessageType.RECEIVE_PLAYER,toListen) );

        return this;
    }

    @Override
    public synchronized Communicator propagateMessage(Message message) {
        // propagation interne
        this.listenedPlayersCommunicators.forEach((communicator) -> communicator.manageEntrantMessage(message) );
        
        return this;
    }

    @Override
    protected HashMap<MessageType, MessageManager> getInternalManagedMessages() {
        return new HashMap<MessageType,MessageManager>();
    }
    
}
