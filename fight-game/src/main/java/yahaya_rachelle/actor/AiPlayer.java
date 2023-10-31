package yahaya_rachelle.actor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;

import org.json.simple.parser.ParseException;

import yahaya_rachelle.communication.communication.AiCommunicator;
import yahaya_rachelle.game.GameSession;

/**
 * joueur intelligence artificielle
 */
public class AiPlayer extends Player implements Runnable{
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

    public AiPlayer(GameSession linkedGameSession)throws FileNotFoundException, ParseException, IOException, URISyntaxException {
        super(null,"Zvheer", linkedGameSession);

        // récupération d'un personnage aléatoire
        ArrayList<Character> characters = linkedGameSession.getLinkedGame().getGameDataManager().getCharacters();
        
        int countOfCharacters = characters.size();
        
        this.character = characters.get(new Random().nextInt(0,countOfCharacters - 1) );
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
        this.stop = false; 
        
        while(!stop){
            System.out.println("position x de l'opposant : " + opponent.getPosition().currentX);
            try{
                Thread.sleep(1000);
            }
            catch(Exception e){

            }
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
}
