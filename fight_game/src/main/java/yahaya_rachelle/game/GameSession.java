package yahaya_rachelle.game;

import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable;
import yahaya_rachelle.configuration.Config.PlayerAction;
import yahaya_rachelle.scene.scene.GameSessionScene;
import yahaya_rachelle.scene.scene.HomeScene;
import yahaya_rachelle.utils.GameCallback;
import yahaya_rachelle.utils.GameContainerCallback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.json.simple.parser.ParseException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import yahaya_rachelle.actor.Character;
import yahaya_rachelle.actor.Player;
import yahaya_rachelle.communication.communication.ClientManager;
import yahaya_rachelle.communication.communication.Communicator;
import yahaya_rachelle.communication.communication.ServerManager;
import yahaya_rachelle.communication.communication.Communicator.MessageManager;
import yahaya_rachelle.communication.message.Message.MessageType;

/**
 * représente une partie
 */
public class GameSession extends Configurable{
    public static final int X_SPEED = 9;
    public static final int JUMP_X_SPEED = 70;
    public static final int JUMP_HEIGHT = 230;

    private Game linkedGame;

    private String saveGameFilePath;

    private GameSessionScene gameSessionScene;

    private GameCallback toCallOnEnd;

    private Player linkedPlayer;

    private boolean canDoSuperAttack;
    private boolean canMoveS;
    private boolean canDoAction;

    private double maxWidth;

    private int blockTime;

    private Communicator communicator;

    public GameSession(Game linkedGame,Character character,String pseudo,GameCallback toCallOnEnd) throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.linkedGame = linkedGame;
        this.toCallOnEnd = toCallOnEnd;
        this.linkedPlayer = new Player(character,pseudo,this);
        this.canDoSuperAttack = true;
        this.canMoveS = true;
        this.canDoAction = true;

        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this.linkedGame);

        this.blockTime = configLongGetter.getValueOf(Config.App.CHARACTERS_SUPPER_ATTACK_BLOCK_TIME.key).intValue();
        this.gameSessionScene = new GameSessionScene(this);
        this.gameSessionScene.buildBefore();
        // placement du joueur sur la scène;

        this.maxWidth = configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue();
        this.linkedPlayer.setPosition(new Player.Position(30,30,this.maxWidth,configLongGetter.getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue() - 40 ) );

        this.gameSessionScene
            .addPlayer(this.linkedPlayer)
            .updatePlayer(this.linkedPlayer,Config.PlayerAction.STATIC_POSITION,null);
    }

    
    /**
     * permet de lancer une partie à partir d'une sauvegarde
     * @param saveGameFilePath
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParseException
     * @throws FileNotFoundException
     */
    public GameSession(String saveGameFilePath,Game linkedGame,HomeScene homeScene,GameCallback toCallOnEnd) throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.saveGameFilePath = saveGameFilePath;
        this.toCallOnEnd = toCallOnEnd;

        this.setConfig();
    }

    /**
     * génère le code de partage, cherche les adversaire et lance le jeux une fois trouvé
     */
    public void findOpponents(int countOfParticipants,GameContainerCallback toCallWhenGetCode,GameCallback toCallAfterFind,GameCallback toCallOnFailure,GameContainerCallback toCallOnNewPlayer){
        new Thread(){
            @Override
            public void run(){
                try{
                    ServerManager serverCommunicator = new ServerManager(createActionsMap(),countOfParticipants,linkedPlayer);

                    IntegerHelper valueObject = new IntegerHelper();

                    // lance le serveur
                    serverCommunicator.createEntryPoint(
                        () -> {
                            int value = valueObject
                                .addOne()
                                .getValue();

                            Platform.runLater(() -> toCallOnNewPlayer.action(value,value == countOfParticipants) );
                        },
                        () -> {
                            if(toCallOnFailure != null)
                                toCallOnFailure.action();
                        },
                        () -> {
                            Platform.runLater(() -> {
                                // préviens du fait qu'il ait été trouvé
                                if(toCallAfterFind != null)
                                    toCallAfterFind.action();
        
                                // lance la partie
                                startGame();
                            });
                        }
                    );

                    communicator = serverCommunicator;

                    String code = serverCommunicator.generateCode();

                    Platform.runLater(() -> toCallWhenGetCode.action(code,false) );
                }
                catch(Exception e){
                    Platform.runLater(() -> {
                        // préviens d'une échec de recherche ou de création des joueurs
                        if(toCallOnFailure != null)
                            toCallOnFailure.action();
                    });
                }
            }
        }.start();
    }

    /**
     * tente de rejoindre une partie, patiente en attendant le début, lance le jeux une fois commencé
     */
    public void waitGameStart(String gameCode,GameCallback toCallAfterStart,GameCallback toCallOnFailure){
        // création et lancement du thread pour rejoindre la partie
        new Thread(){
            @Override
            public void run(){
                try{
                    ClientManager clientCommunicator = new ClientManager(createActionsMap(),linkedPlayer);

                    // on rejoins la partie
                    clientCommunicator.joinEntryPoint(
                        gameCode,
                        toCallOnFailure,
                        () -> {
                            Platform.runLater(() -> {
                                // lancement du jeux
                                toCallAfterStart.action();
                                startGame();
                            });
                        }
                    );

                    communicator = clientCommunicator;
                }
                catch(Exception e){
                    if(toCallOnFailure != null)
                        Platform.runLater(() -> toCallOnFailure.action() );
                }
            }
        }.start();
    }

    /**
     * finis le jeux et retourne à la page d'accueil
     */
    public void endGame(){
        this.toCallOnEnd.action();
    }

    /**
     * sauvegarde cette partie
     * @return si la sauvegarde a réussi
     */
    public boolean saveGame(){
        return false;
    }

    /**
     * lance l'exécution d'unae action
     * @return this
     */
    public GameSession madeActionIf(KeyCode code,KeyCode toCheck,Config.PlayerAction action,GameCallback toDoAfter,GameCallback toDoBeforeIfMatch,boolean conditionToCheck,Player player){
        // aucune action n'est possible durant le saut
        if(!this.canDoAction)
            return this;

        if(code.compareTo(toCheck) == 0 && conditionToCheck)
        {
            if(toDoBeforeIfMatch != null)
                toDoBeforeIfMatch.action();

            this.gameSessionScene.updatePlayer(player,action,toDoAfter); 
        }

        return this;
    }

    /*
     * alias
     */
    public GameSession madeActionIf(KeyCode code,KeyCode toCheck,Config.PlayerAction action,GameCallback toDoAfter,Player player){
        return this.madeActionIf(code,toCheck,action,toDoAfter,null,true,player);
    }

     /*
     * alias
     */
    public GameSession madeActionIf(KeyCode code,KeyCode toCheck,Config.PlayerAction action,GameCallback toDoAfter,GameCallback toDoBeforeIfMatch,Player player){
        return this.madeActionIf(code,toCheck,action,toDoAfter,toDoBeforeIfMatch,true,player);
    }

    /**
     * 
     * @return la map des actions liés aux types de message
     */
    private HashMap<MessageType,MessageManager> createActionsMap(){
        HashMap<MessageType,MessageManager> map = new HashMap<MessageType,MessageManager>();

        // ajout du joueur entrant dans la page (temporaire)
        map.put(MessageType.RECEIVE_PLAYER,(playerMessage) -> {
            Player player = (Player) playerMessage.getMessageData();
            
            this.gameSessionScene
                .addPlayer((Player) player)
                .updatePlayer(player,Config.PlayerAction.STATIC_POSITION,null);
        });

        return map;
    }

    /**
     * lance une partie
     */
    private void startGame(){
        // ajout de la gestion des évenements clavier 
        this.gameSessionScene.getPage().setOnKeyPressed((keyData) -> this.manageKeyEvent(keyData) );

        // affichage de la scène
        this.gameSessionScene.putSceneInWindow();
    }  

    /**
     * gère les événements du clavier
     */
    private void manageKeyEvent(KeyEvent keyData){
        KeyCode code = keyData.getCode();

        GameCallback toDoAfter = () -> this.gameSessionScene.updatePlayer(this.linkedPlayer,Config.PlayerAction.STATIC_POSITION,null);

        this
            .madeActionIf(code,KeyCode.F,PlayerAction.ATTACK,toDoAfter,this.linkedPlayer)
            .madeActionIf(code,KeyCode.D,PlayerAction.SUPER_ATTACK,toDoAfter,() -> {
                // on bloque la super attaque pendant x temps
                this.canDoSuperAttack = false;

                this.doAfterBlockTime(() -> this.canDoSuperAttack = true);

            },this.canDoSuperAttack,this.linkedPlayer)
            .madeActionIf(code,KeyCode.SPACE,PlayerAction.JUMP,() -> {
                // à la fin de la séquence de saut, on lance la séquence de descente
                this.gameSessionScene.updatePlayer(this.linkedPlayer,Config.PlayerAction.FALL,() -> {
                    // quand la déscente est terminé on débloque les autres actions
                    this.canDoAction = true;
                    this.linkedPlayer.getPosition().moveOnCurrentDirection(GameSession.JUMP_X_SPEED);
                    toDoAfter.action();
                });
            },() -> this.canDoAction = false,this.linkedPlayer)
            .madeActionIf(code,KeyCode.M,PlayerAction.STATIC_POSITION,null,() -> {
                    // on bloque l'action pendant un certains pour la re activer
                    this.linkedPlayer.getPosition().moveOnCurrentDirection(this.maxWidth);
                    this.canMoveS = false;
                    this.doAfterBlockTime(() -> this.canMoveS = true);
                },this.canMoveS,this.linkedPlayer)
            .madeActionIf(code,KeyCode.RIGHT,PlayerAction.RUN,toDoAfter,() -> {
                this.linkedPlayer.getPosition()
                    .setCurrentDirection(Player.Position.Direction.RIGHT)
                    .moveOnCurrentDirection(GameSession.X_SPEED);
            },this.linkedPlayer)  
            .madeActionIf(code,KeyCode.LEFT,PlayerAction.RUN,toDoAfter,() -> {                
                this.linkedPlayer.getPosition()
                    .setCurrentDirection(Player.Position.Direction.LEFT)
                    .moveOnCurrentDirection(GameSession.X_SPEED);
            },this.linkedPlayer);
    }

    /**
     * fais une certaine action après le temps de blocage
     * @param toDo
     * @return this
     */
    private GameSession doAfterBlockTime(GameCallback toDo){
        // timeline pour débloquer la super attaque
        Timeline unlockTimeline = new Timeline(new KeyFrame(Duration.ONE,(e) -> toDo.action() ) );

        unlockTimeline.setCycleCount(1);
        unlockTimeline.setDelay(Duration.millis(this.blockTime) );
        unlockTimeline.play();
        
        return this;
    }

    public Game getLinkedGame(){
        return this.linkedGame;
    }

    /**
     * @see fonction de test
     * @return void
     */
    public void testFunction(){
        new Thread(){
            @Override 
            public void run(){
                while(true){
                    System.out.println("Bonjour comment allez vous ?");
                }
            }
        }.start();
    }

    @Override
    protected String getConfigFilePath() {
        return this.saveGameFilePath;
    }

    /**
     * aide interne à l'incrémentation d'entier dans un lambda
     */
    class IntegerHelper{
        private int value;

        public IntegerHelper(){
            this.value = 0;
        }

        public IntegerHelper addOne(){
            this.value++;

            return this;
        }

        public int getValue(){
            return this.value;
        }
    }
}
