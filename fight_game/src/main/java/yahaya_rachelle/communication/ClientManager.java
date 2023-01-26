package yahaya_rachelle.communication;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import yahaya_rachelle.utils.GameCallback;

/**
 * initie une communication en étant un client
 */
public class ClientManager extends Communicator{

    private Socket playerSocket;

    private String ip;

    public ClientManager(HashMap<MessageType, MessageManager> messagesLinkedActionsMap) {
        super(messagesLinkedActionsMap);
    }

    @Override
    protected HashMap<MessageType, MessageManager> getInternalManagedMessages() {
        HashMap<MessageType,MessageManager> internalManagedMessages = new HashMap<MessageType,MessageManager>();

        // gestion de la réception du message (nombre de joueurs à accepté en tant que serveur)
        internalManagedMessages.put(MessageType.RECEIVE_COUNT_OF_PLAYERS_TO_ACCEPT,(countOfPlayersToWait) -> startServerAccept((int)countOfPlayersToWait) );
        internalManagedMessages.put(MessageType.RECEIVE_IP_LIST,(ipList) -> connectToIpList((ArrayList<String>) ipList) );

        return internalManagedMessages;
    }

    @Override
    public Communicator closeAll(){
        super.closeAll();

        try{
            this.playerSocket.close();
        }
        catch(Exception e){}

        return this;
    }
    
    /**
     * crée le socket client
     * rejoins la session 
     * @param code
     * @param toDoOnFailure
     * @param toDoWhenAllJoined
     */
    public void joinEntryPoint(String code,GameCallback toDoOnFailure,GameCallback toDoWhenGameStart){
        try{  
            // connexion du joueur à la partie du code donné
            // this.server = new ServerSocket(Communicator.PORT);
            this.server = new ServerSocket(0);
            this.playerSocket = new Socket(Communicator.readCode(code),Communicator.PORT);
            this.ip = Communicator.readCode(code);
            // ajout du serveur dans la liste de propagation
            this.addNewPlayerSocket(this.playerSocket);
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
        new Thread(){
            @Override
            public void run(){
                try{
                    for(int playerCount = 0; playerCount < countOfPlayersToWait; playerCount++){
                        Socket playerSocket = server.accept();

                        String playerIp = playerSocket.getLocalAddress().getHostAddress();

                        // on vérifie que le joueur n'est pas déjà demandé
                        boolean alreadyExist = false;

                        for(Socket p : otherPlayersSocket){
                            if(p.getLocalAddress().getHostAddress() == playerIp){
                                alreadyExist = true;
                                break;
                            }
                        }

                        if(alreadyExist){
                            playerCount--;
                            continue;
                        }

                        // sauvegarde du joueur et création de son objet de sortie
                        addNewPlayerSocket(playerSocket);

                        System.out.println("nouveau lien");
                    }
                }
                catch(Exception e){
                    exitSession();
                }
            }
        }.start();

        // envoi du message de confirmation au serveur pour la réception de connexion, à ce stade unique membre de la liste
        this.propagateMessage(new Message(MessageType.CONFIRM_CAN_RECEIVE_CONNEXIONS,ip) );

        return this;
    }

    /**
     * lance la connexion à la liste des ip données
     * @param ipList
     * @return this
     */
    private ClientManager connectToIpList(ArrayList<String> ipList){
        ipList.forEach(ip -> {
            try{
                // connexion à l'ip et sauvegarde de la socket
                this.addNewPlayerSocket(new Socket(ip,Communicator.PORT) );

                System.out.println("je me connecte");
            }
            catch(Exception e){
                this.exitSession();
            }
        }); 

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
