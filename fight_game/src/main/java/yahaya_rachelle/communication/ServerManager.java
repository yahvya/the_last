package yahaya_rachelle.communication;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import yahaya_rachelle.utils.GameCallback;

/**
 * représente une communication en étant le premier serveur
 */
public class ServerManager extends Communicator{
    private int countOfPlayersToWait;

    private ArrayList<String> ipListToReduce;

    private GameCallback toDoWhenAllJoin;

    public ServerManager(HashMap<MessageType, MessageManager> messagesLinkedActionsMap,int countOfPlayersToWait) {
        super(messagesLinkedActionsMap);
        this.countOfPlayersToWait = countOfPlayersToWait;
    }

    @Override
    protected HashMap<MessageType, MessageManager> getInternalManagedMessages() {
        HashMap<MessageType,MessageManager> internalManagedMessages = new HashMap<MessageType,MessageManager>();

        // gestion de la réception du message indiquant la possibilité de recevoir des connexions
        internalManagedMessages.put(MessageType.CONFIRM_CAN_RECEIVE_CONNEXIONS,(clientIp) -> {
            int size = this.ipListToReduce.size();

            // on supprime l'ip donné dans la liste
            for(int key = 0; key < size; key++){
                String containedIp = this.ipListToReduce.get(key);

                if(containedIp.equals((String) clientIp) ){
                    size--;
                    this.ipListToReduce.remove(key);
                    break;
                }
            }

            // si la taille est à 0 alors tout le monde à confirmé pouvoir recevoir les connexions
            if(size == 0)
                this.shareOthersIpToEachParticipants();
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
            this.server = new ServerSocket(Communicator.PORT); 
            this.toDoWhenAllJoin = toDoWhenAllJoin;  

            // création et lancemnt du thread de gestion des sockets
            new Thread(){
                @Override
                public void run(){
                    boolean error = false;

                    ipListToReduce = new ArrayList<String>();
                
                    // on attend que les joueurs rejoignent
                    for(int playerCount = 0; playerCount < countOfPlayersToWait; playerCount++){
                        try{
                            // récupération du joueur
                            Socket playerSocket = server.accept();

                            String playerIp = playerSocket.getLocalAddress().getHostAddress();

                            // on vérifie que le joueur n'est pas déjà dans la partie
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

                            // sauvegarde de l'ip dans la liste à réduire
                            ipListToReduce.add(playerIp);
                            // sauvegarde du joueur et création de son objet de sortie
                            addNewPlayerSocket(playerSocket);
                            // appel de l'action à exécuter quand un joueur rejoins
                            toDoOnJoin.action();
                        }
                        catch(Exception e){
                            // on préviens les joueurs déjà connecté de l'arrêt de la partie
                            error = true;

                            break;
                        }
                    }

                    if(!error){
                        // commence à écouter les messages entrants et partage le nombre de participants
                        startListening();
                        shareCountOfParticipantToOtherPlayers();
                    }
                    else toDoOnFailure.action();
                }
            }.start();
        }
        catch(Exception e){
            // exécution de l'action à faire en cas d'erreur
            toDoOnFailure.action();
        }

        return this;
    }
    
    /**
     * @param toDoWhenAllJoin
     * @return this
     */
    private ServerManager shareCountOfParticipantToOtherPlayers(){
        // envoi du nombre de participants à accepter
        this.propagateMessage(new Message(MessageType.RECEIVE_COUNT_OF_PLAYERS_TO_ACCEPT,countOfPlayersToWait - 1) );

        return this;
    }

    /**
     * partage la liste des ip de chaque participant aux autres
     * @return
     */
    private ServerManager shareOthersIpToEachParticipants(){
        this.otherPlayersSocketOutput.forEach((socket,outputObject) -> {
            ArrayList<String> othersIp = new ArrayList<String>();

            // on récupère la liste des 
            this.otherPlayersSocket.forEach(playerSocket -> {
                if(playerSocket != socket)
                    othersIp.add(socket.getLocalAddress().getHostAddress() );
            });

            // envoi de la liste d'ip des autres
            try{
                outputObject.writeObject(new Message(MessageType.RECEIVE_IP_LIST,othersIp) );
            }
            catch(Exception e){
                // on déconnecte ce joueur de la session
            }
        });

        return this;
    }
}
