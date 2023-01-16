package yahaya_rachelle.game;

import yahaya_rachelle.configuration.Configurable;
import yahaya_rachelle.scene.scene.GameSessionScene;
import yahaya_rachelle.scene.scene.HomeScene;
import yahaya_rachelle.utils.GameCallback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

import javafx.scene.input.KeyEvent;
import yahaya_rachelle.actor.Character;
import yahaya_rachelle.actor.Player;

public class GameSession extends Configurable{
    private Game linkedGame;

    private String saveGameFilePath;

    private HomeScene homeScene;
    private GameSessionScene gameSessionScene;

    private Player playerOne;
    private Player playerTwo;

    public GameSession(Game linkedGame,Character character,String pseudo,HomeScene homeScene){
        this.linkedGame = linkedGame;
        this.homeScene = homeScene;
    }

    /**
     * permet de lancer une partie à partir d'une sauvegarde
     * @param saveGameFilePath
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParseException
     * @throws FileNotFoundException
     */
    public GameSession(String saveGameFilePath,HomeScene homeScene) throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.saveGameFilePath = saveGameFilePath;
        this.homeScene = homeScene;
        
        this.setConfig();
    }

    /**
     * cherche un adversaire et lance le jeux
     */
    public void searchOpponent(GameCallback toCallAfterFind){
        // recherche d'un adversaire

        toCallAfterFind.action();

        this.startGame();
    }

    /**
     * finis le jeux et retourne à la page d'accueil
     */
    public void endGame(){
        this.homeScene.putSceneInWindow();
    }

    /**
     * sauvegarde cette partie
     * @param saveFilePath
     * @return si la sauvegarde a réussi
     */
    public boolean saveGameIn(String saveFilePath){
        return false;
    }

    /**
     * lance une partie
     */
    private void startGame(){
        // création de la scène
        this.gameSessionScene = new GameSessionScene(this);
        // ajout de la gestion des évenements clavier 
        this.gameSessionScene.getPage().setOnKeyPressed((keyData) -> this.manageKeyEvent(keyData) );
        // affichage de la scène
        this.gameSessionScene.putSceneInWindow();
        // placement des joueurs sur la scène
    }  

    /**
     * gère les événements du clavier
     */
    private void manageKeyEvent(KeyEvent keyData){

    }

    public Game getLinkedGame(){
        return this.linkedGame;
    }

    @Override
    protected String getConfigFilePath() {
        return this.saveGameFilePath;
    }
}
