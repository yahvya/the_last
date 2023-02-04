package yahaya_rachelle.communication.communication;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import yahaya_rachelle.actor.Player;
import yahaya_rachelle.communication.message.IpMessage;
import yahaya_rachelle.communication.message.Message;
import yahaya_rachelle.communication.message.Message.MessageType;
import yahaya_rachelle.utils.GameCallback;

/**
 * représente une communication en étant le premier serveur
 */
public class ServerManager extends Communicator{
    private int countOfParticipants;

    private GameCallback toDoWhenAllJoin;

    private int confirmCount;
    private int confirmCount2;

    private HashMap<Socket,IpMessage> clientServers;

    public ServerManager(HashMap<MessageType, MessageManager> messagesLinkedActionsMap,Player internalPlayer,int countOfParticipants) {
        super(messagesLinkedActionsMap,internalPlayer);

        this.countOfParticipants = countOfParticipants;
        this.confirmCount = 0;
        this.confirmCount2 = 0;
        this.clientServers = new HashMap<Socket,IpMessage>();
    }   

    @Override
    protected HashMap<MessageType,MessageManager> getInternalManagedMessages() {
        HashMap<MessageType,MessageManager> internalManagedMessages = new HashMap<MessageType,MessageManager>();
        
        // gestion du message de confirmation d'un joueur qui peut recevoir des connexions
        internalManagedMessages.put(MessageType.CONFIRM_CAN_RECEIVE_CONNEXIONS,(serverIpMessageData) -> {
            this.confirmCount++;

            synchronized(this.clientServers){
                // récupération des données du serveur joueur
                this.clientServers.put(serverIpMessageData.getSource(),(IpMessage) serverIpMessageData.getMessageData() );
            }

            // si tous les joueurs ont confirmé
            if(this.confirmCount == this.countOfParticipants){
                // arrêt d'écoute de cet évenement
                internalManagedMessages.remove(MessageType.CONFIRM_CAN_RECEIVE_CONNEXIONS);

                this.sendIpToPlayers();
            }
        });

        // gestion du message de confirmation de réception des connexions
        internalManagedMessages.put(MessageType.CONFIRM_CONNECT_TO_OTHERS,(nullMessage) -> {
            this.confirmCount2++;

            // si tous les joueurs ont confirmé
            if(this.confirmCount2 == this.countOfParticipants){
                // arrêt d'écoute de cet évenement
                internalManagedMessages.remove(MessageType.CONFIRM_CONNECT_TO_OTHERS);

                this.sendSharePlayersMessageToEach();
            }
        });

        // gestion de réception de confirmation (tous les joueurs reçu)
        internalManagedMessages.put(MessageType.CONFIRM_RECEIVE_ALL_PLAYERS,(nullMessage) -> {
            this.confirmCount--;

            // si tous on partagé leur joueur
            if(this.confirmCount == 0){
                // arrêt d'écoute de cet évenement
                internalManagedMessages.remove(MessageType.CONFIRM_RECEIVE_ALL_PLAYERS);

                this.sendAndStartGame();
            }
        });

        // gestion de la réception d'un joueur (redéfinition probable de la fonction défini par l'apppelant)
        internalManagedMessages.put(MessageType.RECEIVE_PLAYER,(playerMessage) -> {
            Player player = (Player) playerMessage.getMessageData();

            player.getCharacter().rebuildActionsMapSerializable();

            // on appel la fonction défini à l'extérieur s'il y en a une
            MessageManager toDo = this.messagesLinkedActionsMap.get(MessageType.RECEIVE_PLAYER);
            
            if(toDo != null)
                toDo.manageMessage(playerMessage);
        });

        return internalManagedMessages;
    }
    
    /**
     * gère le processus de lancement du serveur et de partage des ips
     * gère certains messages et cherche dans la actionsMap les actions à réaliser sur les autres types de messages, si non spécifié alors message ignoré
     * @param toDoOnJoin
     * @param toDoOnFailure
     * @param actionsMap
     * @return this
     */
    public ServerManager createEntryPoint(GameCallback toDoOnJoin,GameCallback toDoOnFailure,GameCallback toDoWhenAllJoin){
        try{
            this.server = new ServerSocket(0);
            this.toDoWhenAllJoin = toDoWhenAllJoin;

            // création du thread d'acceptation
            new Thread(){
                @Override
                public void run(){
                    try{
                        // acceptation des joueurs entrant
                        for(int participantNumber = 0; participantNumber < countOfParticipants; participantNumber++){
                            addNewPlayerSocket(server.accept() ).startListening();

                            // appel de l'action quand un joueur rejoins
                            toDoOnJoin.action();
                        }

                        shareCountOfParticipantsToPlayers();
                    }
                    catch(Exception e){
                        toDoOnFailure.action();
                    }
                }
            }.start();
        }
        catch(Exception e){
            toDoOnFailure.action();
        }
        
        return this;
    }

    /**
     * envoie à chaque joueur le nombre de personnes à accepter
     * @return this
     */
    private ServerManager shareCountOfParticipantsToPlayers(){
        try{
            int countOfParticipantToAccept = 0;

            // envoi du nombre de participant à accepter aux joueurs
            for(Socket otherPlayerSocket : this.otherPlayersSocket){
                this.sendMessageTo(otherPlayerSocket,new Message(MessageType.RECEIVE_COUNT_OF_PLAYERS_TO_ACCEPT,countOfParticipantToAccept) );

                countOfParticipantToAccept++;
            }
        }
        catch(Exception e){}

        return this;
    }

    /**
     * envoi à chaque joueur des ips auquelles se connecter 
     * @return this
     */
    private ServerManager sendIpToPlayers(){
        try{
            ArrayList<IpMessage> ipList = new ArrayList<IpMessage>();

            // récupération de toutes le ips dans la liste
            this.otherPlayersSocket.forEach(socket -> ipList.add(this.clientServers.get(socket) ) );

            // on vide la liste des ips qui ne sera plus utilisé
            this.clientServers.clear();

            // envoie de la liste d'ip concerné
            for(int index = 1; index <= countOfParticipants; index++)
                this.sendMessageTo(this.otherPlayersSocket.get(index - 1),new Message(MessageType.RECEIVE_IP_LIST,new ArrayList<IpMessage>(ipList.subList(index,countOfParticipants) ) ) );
        }
        catch(Exception e){}
        
        return this;
    }

    /**
     * envoi lemessage / signal pour démarrer le partage des joueurs et partage ce joueur aux autres 
     * @return this
     */
    private ServerManager sendSharePlayersMessageToEach(){
        this   
            .propagateMessage(new Message(MessageType.RECEIVE_SIGNAL_TO_SHARE_PLAYER,null) )
            .shareMyPlayer();

        return this;
    }

    /**
     * lance la partie et le signale aux autres
     * @return this
     */
    private ServerManager sendAndStartGame(){
        this.propagateMessage(new Message(MessageType.START_GAME,null) );
        this.toDoWhenAllJoin.action();

        return this;
    }
}
