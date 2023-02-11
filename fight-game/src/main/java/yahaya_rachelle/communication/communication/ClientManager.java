package yahaya_rachelle.communication.communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import yahaya_rachelle.actor.Player;
import yahaya_rachelle.communication.message.IpMessage;
import yahaya_rachelle.communication.message.Message;
import yahaya_rachelle.communication.message.Message.MessageType;
import yahaya_rachelle.utils.GameCallback;

public class ClientManager extends Communicator{
    private GameCallback toCallOnGameStart;

    private Socket linkWithServerSocket;

    private int countOfPlayersToWait;

    public ClientManager(HashMap<MessageType, MessageManager> messagesLinkedActionsMap,Player internalPlayer) {
        super(messagesLinkedActionsMap,internalPlayer);
    }   

    @Override
    protected HashMap<MessageType,MessageManager> getInternalManagedMessages() {
        HashMap<MessageType,MessageManager> internalManagedMessages = new HashMap<MessageType,MessageManager>();

        // gestion de la réception du nombre de personnes à accepter
        internalManagedMessages.put(MessageType.RECEIVE_COUNT_OF_PLAYERS_TO_ACCEPT,(countOfPlayerMessage) -> this.acceptPlayersAndConfirm((int) countOfPlayerMessage.getMessageData() ) );

        // gestion du signal de partage des joueurs
        internalManagedMessages.put(MessageType.RECEIVE_SIGNAL_TO_SHARE_PLAYER,(nullMessage) -> this.shareMyPlayer() );

        // gestion de la réception des ips auquel se connecter
        internalManagedMessages.put(MessageType.RECEIVE_IP_LIST,(ipListMessage) -> this.connectToReceivedIpList(ipListMessage) );

        // gestion de la réception d'un joueur (redéfinition probable de la fonction défini par l'apppelant)
        internalManagedMessages.put(MessageType.RECEIVE_PLAYER,(playerMessage) -> {
            Player player = (Player) playerMessage.getMessageData();

            player.getCharacter().rebuildActionsMapSerializable();

            this.manageEntrantPlayer();

            // on appel la fonction défini à l'extérieur s'il y en a une
            MessageManager toDo = this.messagesLinkedActionsMap.get(MessageType.RECEIVE_PLAYER);
            
            if(toDo != null)
                toDo.manageMessage(playerMessage);
        });

        // gestion de lancement de la partie 
        internalManagedMessages.put(MessageType.START_GAME,(nullMessage) -> this.toCallOnGameStart.action() );

        return internalManagedMessages;
    }
    
    @Override
    synchronized protected Communicator addNewPlayerSocket(Socket otherPlayerSocket) throws IOException{
        this.countOfPlayersToWait++;

        return super.addNewPlayerSocket(otherPlayerSocket);
    }

    /**
     * rejoins la partie via le code
     * @param gameSessionCode
     * @param toCallOnFailure
     * @param toCallOnGameStart
     * @return this
     */
    public ClientManager joinEntryPoint(String gameSessionCode,GameCallback toCallOnFailure,GameCallback toCallOnGameStart){
        try{
            this.toCallOnGameStart = toCallOnGameStart;

            IpMessage serverSocketData = Communicator.readCode(gameSessionCode);

            if(!serverSocketData.getIsDefined() )
                throw new Exception();

            // création du serveur
            this.server = new ServerSocket(0);

            // connexion à la partie
            this.linkWithServerSocket = new Socket(serverSocketData.getIp(),serverSocketData.getPort() );
            // ajout du serveur dans la liste de propagation et début de son écoute
            this
                .addNewPlayerSocket(this.linkWithServerSocket)
                .startListening();
        }
        catch(Exception e){
            toCallOnFailure.action();
        }
        
        return this;
    }

    /**
     * lance le thread d'acceptation des joueurs et confirme les connexions une fois faites
     * préviens le serveur du lancement de l'acceptation
     * @param countOfPlayers
     * @return this
     */
    private ClientManager acceptPlayersAndConfirm(int countOfPlayers){
        try{
            new Thread(){
                @Override
                public void run(){
                    // acceptation des joueurs
                    for(int count = 0;count < countOfPlayers;count++){
                        try{
                            addNewPlayerSocket(server.accept() ).startListening();
                        }
                        catch(Exception e){}
                    }

                    try{
                        // envoi de la confirmation au serveur
                        sendMessageTo(linkWithServerSocket,new Message(MessageType.CONFIRM_CONNECT_TO_OTHERS,null) );
                    }
                    catch(Exception e){}
                }
            }.start();

            // confirmation du lancement de l'acceptation
                
            this.sendMessageTo(
                this.linkWithServerSocket,
                new Message(MessageType.CONFIRM_CAN_RECEIVE_CONNEXIONS,new IpMessage(this.server.getInetAddress().getHostAddress(),this.server.getLocalPort() ) ) 
            );
        }
        catch(Exception e){}

        return this;
    }

    /**
     * connecte le joueur à la liste d'ip donnée
     * @param ipListToConnectMessage
     * @return this
     */
    private ClientManager connectToReceivedIpList(Message ipListToConnectMessage){
        @SuppressWarnings("unchecked") ArrayList<IpMessage> ipList = (ArrayList<IpMessage>) ipListToConnectMessage.getMessageData();

        ipList.forEach(ipObject -> {
            try{
                if(ipObject.getIsDefined() )
                    this.addNewPlayerSocket(new Socket(ipObject.getIp(),ipObject.getPort() ) ).startListening();
            }
            catch(Exception e){}
        });
        
        return this;
    }

    /**
     * on gère l'arrivé de joueur
     * @return this
     */
    synchronized private ClientManager manageEntrantPlayer(){
        this.countOfPlayersToWait--;

        /* 
            si tous les joueurs sont reçu
            on prévient le serveur qu'on a reçu tous les joueurs attendu
        */
        if(this.countOfPlayersToWait == 0)
            this.sendMessageTo(this.linkWithServerSocket,new Message(MessageType.CONFIRM_RECEIVE_ALL_PLAYERS,null) );

        return this;
    }
}
