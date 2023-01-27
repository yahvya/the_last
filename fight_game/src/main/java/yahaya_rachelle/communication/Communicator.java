package yahaya_rachelle.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import yahaya_rachelle.actor.Player;

/**
 * gestion des communications de l'application
 */
public abstract class Communicator {
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

    protected static final int PORT = 6666; 

    protected ArrayList<Socket> otherPlayersSocket;

    protected HashMap<Socket,ObjectOutputStream> otherPlayersSocketOutput;
    protected HashMap<Socket,ObjectInputStream> otherPlayersSocketInput;
    private HashMap<Socket,EntrantMessageThread> entrantMessageThreads;

    protected ServerSocket server;

    protected HashMap<MessageType,MessageManager> internalManagedMessages;
    protected HashMap<MessageType,MessageManager> messagesLinkedActionsMap;

    protected Player internalPlayer;

    /**
     * 
     * @return les messages géré à l'interne par la classe
     */
    protected abstract HashMap<MessageType,MessageManager> getInternalManagedMessages();

    public Communicator(HashMap<MessageType,MessageManager> messagesLinkedActionsMap,Player internalPlayer){
        this.messagesLinkedActionsMap = messagesLinkedActionsMap;
        this.internalPlayer = internalPlayer;
        this.server = null;
        this.internalManagedMessages = this.getInternalManagedMessages();
        this.otherPlayersSocket = new ArrayList<Socket>();
        this.otherPlayersSocketOutput = new HashMap<Socket,ObjectOutputStream>();
        this.otherPlayersSocketInput = new HashMap<Socket,ObjectInputStream>();
        this.entrantMessageThreads = new HashMap<Socket,EntrantMessageThread>();
    }

    /**
     * ferme toutes les ressources
     * @return this
     */
    public Communicator closeAll(){
        try{
            // fermeture du serveur
            if(this.server != null)
                this.server.close();

            // fermeture des sockets interne et des objets de sorties
            this.otherPlayersSocketOutput.forEach((socket,output) -> {
                try{
                    socket.close();
                    output.close();
                    this.otherPlayersSocketInput.get(socket).close();
                    
                    EntrantMessageThread thread =  this.entrantMessageThreads.get(socket);

                    if(thread != null)
                        thread.stopReading();
                }
                catch(Exception e){} 
            });
        }
        catch(Exception e){}

        return this;
    }

    /**
     * ajoute le socket joueur à la liste et crée ses objets output de sortie et input d'entrée
     * @param player
     * @return this
     * @throws IOException
     */
    protected Communicator addNewPlayerSocket(Socket playerSocket) throws IOException{
        this.otherPlayersSocket.add(playerSocket);
        this.otherPlayersSocketOutput.put(playerSocket,new ObjectOutputStream(playerSocket.getOutputStream() ) );
        this.otherPlayersSocketInput.put(playerSocket,new ObjectInputStream(playerSocket.getInputStream() ) );
        this.entrantMessageThreads.put(playerSocket,null);

        return this;
    }

    /**
     * partage son joueur aux autres
     * @return this
     */
    protected Communicator shareMyPlayer(){
        return this;
    }

    /**
     * gère les message entrant
     * @param messagesLinkedActionsMap
     * @param receivedMessage
     * @return this
     */
    synchronized protected Communicator manageEntrantMessage(Message receivedMessage){
        System.out.println("message recu -> type : " + receivedMessage.getMessageType() + " - message : " + receivedMessage.getMessageData() );

        MessageType messageType = receivedMessage.getMessageType();

        // on vérifie si le message doit être géré à l'interne
        MessageManager toDo = this.internalManagedMessages.get(messageType);
        
        // gestion interne du message
        if(toDo != null){
            toDo.manageMessage(receivedMessage.getMessageData() );
            return this;
        }

        toDo = this.messagesLinkedActionsMap.get(messageType);

        // gestion externe du message
        if(toDo != null)
            toDo.manageMessage(receivedMessage.getMessageData() );

        return this;
    }

    /**
     * lance le thread d'écoute des messages entrants 
     * @return this
     */
    protected Communicator startListening(){
        this.entrantMessageThreads.forEach((socket,inputObject) -> {
            // vérifie si l'objet n'a pas déjà un thread de lecture en cours
            if(inputObject == null){
                // crée le thread d'écoute
                EntrantMessageThread readingThread = new EntrantMessageThread(this.otherPlayersSocketInput.get(socket), this);

                readingThread.start();

                this.entrantMessageThreads.put(socket,readingThread);
            }
        });   

        return this;
    }

    /**
     * envoie le message à la liste des participants, tente de renotifier une fois après 300 ms en cas de premier échec
     * @param message
     * @return this
     */
    public Communicator propagateMessage(Message message){
        System.out.println("message à envoyer -> type : " + message.getMessageType() + " - message : " + message.getMessageData() );

        ArrayList<ObjectOutputStream> retryList = new ArrayList<ObjectOutputStream>();

        this.otherPlayersSocketOutput.forEach((socket,output) -> {
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
            // lancement du seconde tentative d'envoi après 300 ms
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

        return this;
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
        START_GAME // début de partie
    };

    /**
     * permet la gestion des messages reçuw
     */
    public interface MessageManager{
        public void manageMessage(Object messageData);
    }
}
