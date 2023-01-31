package yahaya_rachelle.communication.communication;

import java.net.InetAddress;
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
 * initie une communication en étant un client
 */
public class ClientManager extends Communicator{

    private Socket linkWithServerSocket;

    private String ip;

    private int countOfPlayersToReceive;

    private GameCallback toDoWhenGameStart;

    public ClientManager(HashMap<MessageType, MessageManager> messagesLinkedActionsMap,Player internalPlayer) {
        super(messagesLinkedActionsMap,internalPlayer);
        this.countOfPlayersToReceive = -1;
        this.toDoWhenGameStart = null;
    }

    @Override
    protected HashMap<MessageType, MessageManager> getInternalManagedMessages() {
        HashMap<MessageType,MessageManager> internalManagedMessages = new HashMap<MessageType,MessageManager>();

        // gestion de la réception du message (nombre de joueurs à accepté en tant que serveur)
        internalManagedMessages.put(MessageType.RECEIVE_COUNT_OF_PLAYERS_TO_ACCEPT,(countOfPlayersToWaitMessage) -> startServerAccept((int)countOfPlayersToWaitMessage.getMessageData() ) );
        internalManagedMessages.put(MessageType.RECEIVE_IP_LIST,(serversListMessage) -> connectToIpList((ArrayList<IpMessage>) serversListMessage.getMessageData() ) );
        internalManagedMessages.put(MessageType.RECEIVE_SIGNAL_TO_SHARE_PLAYER,(nullData) -> shareMyPlayer() );
        internalManagedMessages.put(MessageType.START_GAME,(nullData) -> {
            if(this.toDoWhenGameStart != null)
                this.toDoWhenGameStart.action();
        });

        // on remplace le message de gestion d'arrivé de joueur prédéfini si elle existe

        MessageManager defaultManager = this.messagesLinkedActionsMap.get(MessageType.RECEIVE_PLAYER);
        MessageManager finalManager;

        // alors une action avait été défini pour cet évenement
        if(defaultManager != null){
            finalManager = (playerMessage) -> {
                Player player = (Player) playerMessage.getMessageData();

                System.out.println("pseudo du joueur recu -> " + player.getPseudo());

                this.manageEntrantPlayer(player);

                // appel de l'action prédéfini
                defaultManager.manageMessage(playerMessage);
            };
        }
        else finalManager = (playerMessage) -> this.manageEntrantPlayer((Player) playerMessage.getMessageData() );

        internalManagedMessages.put(MessageType.RECEIVE_PLAYER,finalManager);

        return internalManagedMessages;
    }
    
    /**
     * crée le socket client
     * rejoins la session 
     * @param code
     * @param toDoOnFailure
     * @param toDoWhenGameStart
     */
    public void joinEntryPoint(String code,GameCallback toDoOnFailure,GameCallback toDoWhenGameStart){
        try{  
            IpMessage serverSocketData = Communicator.readCode(code);

            if(!serverSocketData.getIsDefined() )
                throw new Exception();

            // connexion du joueur à la partie du code donné
            this.toDoWhenGameStart = toDoWhenGameStart;
            this.server = new ServerSocket(0);
            this.linkWithServerSocket = new Socket(serverSocketData.getIp(),serverSocketData.getPort() );
            this.ip = this.linkWithServerSocket.getInetAddress().getHostAddress();
            // ajout du serveur dans la liste de propagation
            this.addNewPlayerSocket(this.linkWithServerSocket);
            this.startListening();
        }
        catch(Exception e){
            toDoOnFailure.action();
        }
    }

    /**
     * lance le thread d'acceptation des joueurs
     * @return this
     */
    private ClientManager startServerAccept(int countOfPlayersToWait){
        // le nombre joueurs à recevoir est le nombre d'autres joueurs + le serveur
        this.countOfPlayersToReceive = countOfPlayersToWait + 1;

        new Thread(){
            @Override
            public void run(){
                try{
                    // envoi du message de confirmation au serveur pour la réception de connexion, à ce stade unique membre de la liste
                    propagateMessage(new Message(MessageType.CONFIRM_CAN_RECEIVE_CONNEXIONS,new IpMessage(ip,server.getLocalPort() ) ) );
        
                    for(int playerCount = 0; playerCount < countOfPlayersToWait; playerCount++){
                        Socket playerSocket = server.accept();

                        String playerIp = playerSocket.getInetAddress().getHostAddress();

                        // on vérifie que le joueur n'est pas déjà demandé
                        // boolean alreadyExist = false;

                        // for(Socket p : otherPlayersSocket){
                        //     if(p.getInetAddress().getHostAddress() == playerIp){
                        //         alreadyExist = true;
                        //         break;
                        //     }
                        // }

                        // if(alreadyExist){
                        //     playerCount--;
                        //     continue;
                        // }

                        // enregistrement du joueur et on ajoute son thread de lecture
                        addNewPlayerSocket(playerSocket).startListening();
                    }

                    try{
                        // confirmation du fait d'avoir reçu toutes ses connexions attendus
                        otherPlayersSocketOutput
                            .get(linkWithServerSocket)
                            .writeObject(new Message(MessageType.CONFIRM_CONNECT_TO_OTHERS,ip) );
                    }
                    catch(Exception e){
                        // a gérer
                    }
                }
                catch(Exception e){
                    exitSession();
                }
            }
        }.start();

        return this;
    }

    /**
     * lance la connexion à la liste des ip données
     * @param ipList
     * @return this
     */
    private ClientManager connectToIpList(ArrayList<IpMessage> serverSocketDatas){

        serverSocketDatas.forEach(socketData -> {
            try{
                if(!socketData.getIsDefined() )
                    throw new Exception();

                // connexion à l'ip et sauvegarde de la socket
                this.addNewPlayerSocket(new Socket(socketData.getIp(),socketData.getPort() ) );
            }
            catch(Exception e){
                this.exitSession();
            }
        }); 

        return this;
    }

    /**
     * gère la réception interne d'un joueur partagé
     * @param playerData
     * @return this
     */
    synchronized private ClientManager manageEntrantPlayer(Player player){
        this.countOfPlayersToReceive--;

        // on reconstruit les images du personnage du joueur
        player.getCharacter().rebuildActionsMapSerializable();

        // alors tous les joueurs attendues sont reçu
        if(this.countOfPlayersToReceive == 0){
            try{
                // on envoi la confirmation de réception de tous les joueurs
                this.otherPlayersSocketOutput.get(this.linkWithServerSocket).writeObject(new Message(MessageType.CONFIRM_RECEIVE_ALL_PLAYERS,this.ip) );   
            }
            catch(Exception e){
                // a gérer
            }
        }

        return this;
    }

    /**
     * stoppe la participation à la partie
     * @return this
     */
    private ClientManager exitSession(){
        return this;
    }
}
