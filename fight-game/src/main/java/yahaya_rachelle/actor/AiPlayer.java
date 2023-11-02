package yahaya_rachelle.actor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;

import org.json.simple.parser.ParseException;

import javafx.scene.input.KeyCode;
import yahaya_rachelle.communication.communication.AiCommunicator;
import yahaya_rachelle.communication.message.Message;
import yahaya_rachelle.communication.message.PlayerActionMessage;
import yahaya_rachelle.communication.message.Message.MessageType;
import yahaya_rachelle.configuration.Config.PlayerAction;
import yahaya_rachelle.game.GameSession;

/**
 * joueur intelligence artificielle
 */
public class AiPlayer extends Player implements Runnable{
    /**
     * vitesse de jeux supposé d'un humain (réglage de la vitesse de frappe)
     * 100 à 250 ms
     * 100 -> les meilleurs
     */
    public static final int HUMAN_SPEED = 230;

    /**
     * vitesse de jeux supposé d'un humain de la course en restant appuyé (réglage de la vitesse de course)
     */
    public static final int HUMAN_RUN_SPEED = 40;

    /**
     * défini l'arrêt du thread
     */
    private boolean stop;

    /**
     * échangeur de l'ia
     */
    protected AiCommunicator communicator;

    /**
     * adversaire
     */
    protected Player opponent;

    /**
     * gestion des actions de l'ia
     */
    protected AiPlayerActionManager manager;

    /**
     *
     * @param linkedGameSession session de jeux
     * @param manager l'algorithme de calcul des actions à utilisé
     * @throws FileNotFoundException erreur de traitement interne
     * @throws ParseException erreur de traitement interne
     * @throws IOException erreur de traitement interne
     * @throws URISyntaxException erreur de traitement interne
     */
    public AiPlayer(GameSession linkedGameSession,AiPlayerActionManager manager)throws FileNotFoundException, ParseException, IOException, URISyntaxException {
        super(null,"Zvheer", linkedGameSession);

        // récupération d'un personnage aléatoire
        ArrayList<Character> characters = linkedGameSession.getLinkedGame().getGameDataManager().getCharacters();
        
        int countOfCharacters = characters.size();
        
        this.character = characters.get(new Random().nextInt(0,countOfCharacters - 1) );
        this.manager = manager;
    }

    /**
     * 
     * @param opponent l'opposant
     * @return this
     */
    synchronized public AiPlayer setOpponent(Player opponent){
        this.opponent = opponent;

        return this;
    }

    /**
     * 
     * @return l'adversaire
     */
    synchronized public Player getOpponent(){
        return this.opponent;
    }

    /**
     * 
     * @param communicator le communicateur
     * @return this
     */
    synchronized public AiPlayer setCommunicator(AiCommunicator communicator){
        this.communicator = communicator;

        return this;
    }

    /**
     * 
     * @return le communicateur interne
     */
    synchronized public AiCommunicator getCommunicator(){
        return this.communicator;
    }

    /**
     * lance l'ia
     */
    @Override
    public void run(){
        // attente de lancement du programme
        try{ Thread.sleep(1000); }catch (Exception e){}

        while(!this.stop && !this.isDead() ){
            try{
                // calcul du temps utilisé de calcul
                long calculationStart = System.currentTimeMillis();

                PlayerActionMessage toDo = this.manager.getBestActionToDo(this,this.opponent);

                long delay = System.currentTimeMillis() - calculationStart;

                // temps d'attente pour matcher la vitesse de jeux humaine
                if(delay < AiPlayer.HUMAN_SPEED){
                    // vitesse supérieur pour la course
                    if(toDo.getAction() == PlayerAction.RUN)
                        Thread.sleep(delay < AiPlayer.HUMAN_RUN_SPEED ? AiPlayer.HUMAN_RUN_SPEED - delay : 0);
                    else
                        Thread.sleep(AiPlayer.HUMAN_SPEED - delay);
                }

                if(toDo != null) this.communicator.propagateMessage(new Message(MessageType.RECEIVE_PLAYER_ACTION,toDo) );
            }
            catch(Exception e){}
        }
    }

    /**
     * stoppe le thread
     * @return this
     */
    synchronized public AiPlayer stop(){
        this.stop = true;

        return this;
    }

    /**
     * interface d'ia pouvant gérer l'action du joueur ia
     */
    public static interface AiPlayerActionManager{
        /**
         * cherche la meilleure action à faire pour le joueur ia
         * @param ai le joueur ia
         * @param opponent l'opposant de l'ia
         * @return la meilleure action à faire par rapport au contexte donnée
         */
        public PlayerActionMessage getBestActionToDo(Player ai,Player opponent);
    }
}
