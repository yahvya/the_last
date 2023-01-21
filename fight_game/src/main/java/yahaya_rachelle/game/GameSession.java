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

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import yahaya_rachelle.actor.Character;
import yahaya_rachelle.actor.Player;

public class GameSession extends Configurable{
    public static final int X_SPEED = 9;
    public static final int JUMP_HEIGHT = 230;

    private Game linkedGame;

    private String saveGameFilePath;

    private GameSessionScene gameSessionScene;

    private GameCallback toCallOnEnd;

    private Player linkedPlayer;

    private boolean canDoSuperAttack;
    private boolean isInJumpingSession;

    private int blockTime;

    public GameSession(Game linkedGame,Character character,String pseudo,GameCallback toCallOnEnd) throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.linkedGame = linkedGame;
        this.toCallOnEnd = toCallOnEnd;
        this.linkedPlayer = new Player(character,pseudo,this);
        this.canDoSuperAttack = true;
        this.isInJumpingSession = false;
        this.blockTime = new ConfigGetter<Long>(linkedGame).getValueOf(Config.App.CHARACTERS_SUPPER_ATTACK_BLOCK_TIME.key).intValue();
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
                    // recherche d'un adversaire à faire

                    Platform.runLater(new Runnable(){
                        @Override 
                        public void run(){
                            // préviens du fait qu'il ait été trouvé
                            if(toCallAfterFind != null)
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
                            if(toCallOnFailure != null)
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

        this.linkedPlayer.setPosition(new Player.Position(30,30,configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue(),configLongGetter.getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue() - 40 ) );
        this.gameSessionScene.addPlayer(this.linkedPlayer);
        this.gameSessionScene.updatePlayer(this.linkedPlayer,Config.PlayerAction.STATIC_POSITION,null);
    }  

    /**
     * gère les événements du clavier
     */
    private void manageKeyEvent(KeyEvent keyData){
        KeyCode code = keyData.getCode();

        GameCallback toDoAfter = () -> this.gameSessionScene.updatePlayer(this.linkedPlayer,Config.PlayerAction.STATIC_POSITION,null);

        this
            .madeActionIf(code,KeyCode.F,PlayerAction.ATTACK,toDoAfter)
            .madeActionIf(code,KeyCode.D,PlayerAction.SUPER_ATTACK,toDoAfter,() -> {
                // on bloque la super attaque pendant x temps
                this.canDoSuperAttack = false;

                // timeline pour débloquer la super attaque
                Timeline unlockTimeline = new Timeline(new KeyFrame(Duration.ONE,(e) -> this.canDoSuperAttack = true) );

                unlockTimeline.setCycleCount(1);
                unlockTimeline.setDelay(Duration.millis(this.blockTime) );
                unlockTimeline.play();
            },this.canDoSuperAttack)
            .madeActionIf(code,KeyCode.SPACE,PlayerAction.JUMP,() -> {
                // à la fin de la séquence de saut, on lance la séquence de descente
                this.gameSessionScene.updatePlayer(this.linkedPlayer,Config.PlayerAction.FALL,() -> {
                    // quand la décente est terminé on débloque les autres actions
                    this.isInJumpingSession = false;
                    toDoAfter.action();
                });
            },() -> this.isInJumpingSession = true)
            .madeActionIf(code,KeyCode.RIGHT,PlayerAction.RUN,toDoAfter,() -> {
                Player.Position position = this.linkedPlayer.getPosition();
                
                position
                    .setCurrentDirection(Player.Position.Direction.RIGHT)
                    .moveOnCurrentDirection(GameSession.X_SPEED);
            })  
            .madeActionIf(code,KeyCode.LEFT,PlayerAction.RUN,toDoAfter,() -> {
                Player.Position position = this.linkedPlayer.getPosition();
                
                position
                    .setCurrentDirection(Player.Position.Direction.LEFT)
                    .moveOnCurrentDirection(GameSession.X_SPEED);
            });
    }

    /**
     * lance l'exécution d'unae action
     * @return this
     */
    public GameSession madeActionIf(KeyCode code,KeyCode toCheck,Config.PlayerAction action,GameCallback toDoAfter,GameCallback toDoBeforeIfMatch,boolean conditionToCheck){
        
        // aucune action n'est possible durant
        if(this.isInJumpingSession)
            return this;

        if(code.compareTo(toCheck) == 0 && conditionToCheck)
        {
            if(toDoBeforeIfMatch != null)
                toDoBeforeIfMatch.action();

            this.gameSessionScene.updatePlayer(this.linkedPlayer,action,toDoAfter); 
        }

        return this;
    }

    /*
     * alias
     */
    public GameSession madeActionIf(KeyCode code,KeyCode toCheck,Config.PlayerAction action,GameCallback toDoAfter){
        return this.madeActionIf(code,toCheck,action,toDoAfter,null,true);
    }

     /*
     * alias
     */
    public GameSession madeActionIf(KeyCode code,KeyCode toCheck,Config.PlayerAction action,GameCallback toDoAfter,GameCallback toDoBeforeIfMatch){
        return this.madeActionIf(code,toCheck,action,toDoAfter,toDoBeforeIfMatch,true);
    }

    public Game getLinkedGame(){
        return this.linkedGame;
    }

    @Override
    protected String getConfigFilePath() {
        return this.saveGameFilePath;
    }
}
