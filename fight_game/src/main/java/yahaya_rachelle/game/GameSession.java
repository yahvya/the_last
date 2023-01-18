package yahaya_rachelle.game;

import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable;
import yahaya_rachelle.configuration.Config.PlayerAction;
import yahaya_rachelle.scene.scene.GameSessionScene;
import yahaya_rachelle.scene.scene.HomeScene;
import yahaya_rachelle.utils.GameCallback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import yahaya_rachelle.actor.Character;
import yahaya_rachelle.actor.Player;

public class GameSession extends Configurable{
    private Game linkedGame;

    private String saveGameFilePath;

    private GameSessionScene gameSessionScene;

    private GameCallback toCallOnEnd;

    private Player playerOne;

    public GameSession(Game linkedGame,Character character,String pseudo,GameCallback toCallOnEnd) throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.linkedGame = linkedGame;
        this.toCallOnEnd = toCallOnEnd;
        this.playerOne = new Player(character,pseudo);
    }

    /**
     * permet de lancer une partie à partir d'une sauvegarde
     * @param saveGameFilePath
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParseException
     * @throws FileNotFoundException
     */
    public GameSession(String saveGameFilePath,HomeScene homeScene,GameCallback toCallOnEnd) throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.saveGameFilePath = saveGameFilePath;
        this.toCallOnEnd = toCallOnEnd;

        this.setConfig();
    }

    /**
     * cherche un adversaire et lance le jeux
     */
    public void searchOpponent(GameCallback toCallAfterFind,GameCallback toCallOnFailure){

        Thread searchThread = new Thread(){
            @Override
            public void run(){
                try{
                    // recherche d'un adversaire

                    Platform.runLater(new Runnable(){
                        @Override 
                        public void run(){
                            // préviens du fait qu'il ait été trouvé
                            toCallAfterFind.action();

                            // lance la partie
                            startGame();
                        }
                    });
                }
                catch(Exception e){
                    
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run(){
                            // préviens d'une échec de recherche ou de création des joueurs
                            toCallOnFailure.action();
                        }
                    });
                }
            }
        };

        searchThread.start();
    }

    /**
     * finis le jeux et retourne à la page d'accueil
     */
    public void endGame(){
        this.toCallOnEnd.action();
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
        // placement des joueurs sur la scène;
        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this.linkedGame);

        this.playerOne.setPosition(new Player.Position(30,30,configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue(),configLongGetter.getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue() - 40 ) );
        this.gameSessionScene.addPlayer(this.playerOne);
        this.gameSessionScene.updatePlayer(this.playerOne,Config.PlayerAction.STATIC_POSITION,null);
    }  

    /**
     * gère les événements du clavier
     */
    private void manageKeyEvent(KeyEvent keyData){
        KeyCode code = keyData.getCode();

        GameCallback toDoAfter = () -> this.gameSessionScene.updatePlayer(this.playerOne,Config.PlayerAction.STATIC_POSITION,null);

        this
            .madeActionIf(code,KeyCode.SPACE,PlayerAction.ATTACK,toDoAfter)  
            .madeActionIf(code,KeyCode.RIGHT,PlayerAction.RUN,toDoAfter,() -> {
                Player.Position position = this.playerOne.getPosition();
                
                position
                    .setCurrentDirection(Player.Position.Direction.RIGHT)
                    .moveOnCurrentDirection(5);
            })  
            .madeActionIf(code,KeyCode.LEFT,PlayerAction.RUN,toDoAfter,() -> {
                Player.Position position = this.playerOne.getPosition();
                
                position
                    .setCurrentDirection(Player.Position.Direction.LEFT)
                    .moveOnCurrentDirection(5);
            }) ; 
    }

    /**
     * lance l'exécution d'unae action
     * @return this
     */
    public GameSession madeActionIf(KeyCode code,KeyCode toCheck,Config.PlayerAction action,GameCallback toDoAfter,GameCallback toDoBeforeIfMatch){
        if(code.compareTo(toCheck) == 0)
        {
            if(toDoBeforeIfMatch != null)
                toDoBeforeIfMatch.action();

            this.gameSessionScene.updatePlayer(this.playerOne,action,toDoAfter); 
        }

        return this;
    }

    /*
     * alias
     */
    public GameSession madeActionIf(KeyCode code,KeyCode toCheck,Config.PlayerAction action,GameCallback toDoAfter){
        return this.madeActionIf(code,toCheck,action,toDoAfter,null);
    }

    public Game getLinkedGame(){
        return this.linkedGame;
    }

    @Override
    protected String getConfigFilePath() {
        return this.saveGameFilePath;
    }
}
