package yahaya_rachelle.communication;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import yahaya_rachelle.utils.GameCallback;
import yahaya_rachelle.utils.GameContainerCallback;

/**
 * gère les échanges entre les différences instances du jeux durant une partie
 */
public class Communicator {
    private static final int PORT = 6666; 

    private static String[][] REPLACES_MAP = new String[][]{
        {"0","a"},
        {"1","m"},
        {"2","g"},
        {"3","k"},
        {"4","l"},
        {"5","h"},
        {"6","s"},
        {"7","e"},
        {"8","z"},
        {"9","#"},
        {"\\.","-"}
    };

    private ServerSocket server;

    private ArrayList<Socket> playersList;

    private ArrayList<ObjectOutputStream> outputList;

    public Communicator(){
        this.playersList = new ArrayList<Socket>();
        this.outputList = new ArrayList<ObjectOutputStream>();
    }

    /**
     * gère le processus de lancement du serveur et de partage des ips
     * gère certains messages et cherche dans la actionsMap les actions à réaliser sur les autres types de messages, si non spécifié alors message ignoré
     * @param code
     * @param countOfPlayersToWait
     * @param toDoOnJoin
     * @param toDoOnFailure
     * @param actionsMap
     */
    public void createEntryPoint(int countOfPlayersToWait,GameCallback toDoOnJoin,GameCallback toDoOnFailure,GameCallback toDoWhenAllJoin,HashMap<MessageType,GameContainerCallback> messagesLinkedActionsMap){
        try{
            this.server = new ServerSocket(Communicator.PORT);   

            // création et lancemnt du thread de gestion des sockets
            new Thread(){
                @Override
                public void run(){
                    boolean error = false; 

                    // on attend que les joueurs rejoignent
                    for(int playerCount = 0; playerCount < countOfPlayersToWait; playerCount++){
                        try{
                            // récupération du joueur
                            Socket player = server.accept();

                            String playerIp = player.getLocalAddress().getHostAddress();

                            // on vérifie que le joueur n'est pas déjà dans la partie
                            boolean alreadyExist = false;

                            for(Socket p : playersList){
                                if(p.getLocalAddress().getHostAddress() == playerIp)
                                    break;
                            }

                            if(alreadyExist)
                            {
                                playerCount--;

                                continue;
                            }

                            // sauvegarde du joueur
                            playersList.add(player); 
                            // création et sauvegarde de l'objet d'envoi au joueur
                            outputList.add(new ObjectOutputStream(player.getOutputStream() ) );
                            
                            toDoOnJoin.action();
                        }
                        catch(Exception e){
                            // envoie aux clients déjà connecté du cas d'erreurs

                            error = true;

                            break;
                        }
                    }

                    if(!error)
                        toDoWhenAllJoin.action();
                    else
                        toDoOnFailure.action();
                }
            }.start();
        }
        catch(Exception e){
            toDoOnFailure.action();
        }
    }

    /**
     * crée le socket client
     * rejoins la session 
     * gère la réception et la partage d'ip
     * @param code
     * @param toDoOnFailure
     * @param toDoWhenAllJoined
     */
    public void joinEntryPoint(String code,GameCallback toDoOnFailure,GameCallback toDoWhenAllJoined){
        try{  
            // connexion du joueur à la partie du code donné
            Socket player = new Socket(Communicator.readCode(code),Communicator.PORT);
        }
        catch(Exception e){
            toDoOnFailure.action();
        }
    }

    /**
     * ferme tous les sockets
     */
    public void closeAll(){
        try{
            this.server.close();
        }
        catch(Exception e){}
    }

     /**
     * envoie le message à la liste des participants, tente de renotifier une fois après 300 ms en cas de premier échec
     * @param message
     */
    public void propagateMessage(Message message){
        ArrayList<ObjectOutputStream> retryList = new ArrayList<ObjectOutputStream>();

        outputList.forEach((output) -> {
            try{
                // envoie du message
                output.writeObject(message);
            }
            catch(Exception e){
                // sauvegarde dans la liste des personnes à renotifier
                retryList.add(output);
            }
        });

        if(retryList.size() != 0){
            Timeline retryTimeline = new Timeline(new KeyFrame(Duration.ONE,(e) -> {
                retryList.forEach((output) -> {
                    try{
                        // envoie du message
                        output.writeObject(message);
                    }
                    catch(Exception exception){}
                });
            }) );

            retryTimeline.setCycleCount(1);
            retryTimeline.setDelay(Duration.millis(300) );
            retryTimeline.play();
        }
    }

    /**
     * crée un code à partir de l'ip transformé de l'utilisateur
     * @return le code
     * @throws UnknownHostException
     */
    public static String generateCode() throws UnknownHostException{
        String code =  InetAddress.getLocalHost().getHostAddress();

        // remplacement de chaque caractère dans le code par l'équivalent dans le tableau de remplacement
        for(String[] replaceMapItem : Communicator.REPLACES_MAP)
            code = code.replaceAll(replaceMapItem[0],replaceMapItem[1]);

        return code;
    }

    /**
     * 
     * @param code
     * @return le code remis sous forme d'ip
     */
    public static String readCode(String code){
        // remplacement de chaque caractère dans le code par l'équivalent dans le tableau de remplacement à l'inverse
        for(String[] replaceMapItem : Communicator.REPLACES_MAP)
            code = code.replaceAll(replaceMapItem[1],replaceMapItem[0]);

        return code;
    }

    public enum MessageType{RECEIVE_COUNT_OF_PLAYERS_IN_SESSION,RECEIVE_IP_LIST,ERROR};

    public interface Message extends Serializable{
        public MessageType getMessageType();
        public Object getMessageData();
    }
}
