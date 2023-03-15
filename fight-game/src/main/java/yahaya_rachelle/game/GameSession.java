package yahaya_rachelle.game;

import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable;
import yahaya_rachelle.configuration.Config.PlayerAction;
import yahaya_rachelle.data.SavedGames;
import yahaya_rachelle.scene.scene.GameSessionScene;
import yahaya_rachelle.scene.scene.GameSessionScene.PlayerManager;
import yahaya_rachelle.utils.GameCallback;
import yahaya_rachelle.utils.GameContainerCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.simple.parser.ParseException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import yahaya_rachelle.actor.Character;
import yahaya_rachelle.actor.Player;
import yahaya_rachelle.communication.communication.Communicator;
import yahaya_rachelle.communication.communication.Communicator.MessageManager;
import yahaya_rachelle.communication.message.Message;
import yahaya_rachelle.communication.message.PlayerActionMessage;
import yahaya_rachelle.communication.message.Message.MessageType;
import yahaya_rachelle.communication.communication.ClientManager;
import yahaya_rachelle.communication.communication.ServerManager;
import yahaya_rachelle.actor.Player.Position.Direction;

/**
 * représente une partie
 */
public class GameSession extends Configurable{
    public static final int X_SPEED = 11;
    public static final int JUMP_X_SPEED = 70;
    public static final int JUMP_HEIGHT = 230;
    public static final int HIT_DISTANCE = 80;

    private static String SAVED_GAMES_PATH = null;
    
    private static int LAST_SAVED_GAME_INDEX;
    
    private static File INDEX_FILE;

    public static final KeyCode SAVE_TOUCH = KeyCode.P;

    private Game linkedGame;

    private String saveGameFilePath;
    private String gameSessionCode;

    private GameSessionScene gameSessionScene;

    private GameCallback toCallOnEnd;

    private Player linkedPlayer;

    private double maxWidth;

    private int blockTime;
    private int countOfPlayers;

    private Communicator communicator;

    private HashMap<Socket,Player> otherPlayersMap;;

    private boolean lost;
    private boolean isBlockInDialog;

    private GameSession(Game linkedGame,GameCallback toCallOnEnd){
        this.linkedGame = linkedGame;
        this.toCallOnEnd = toCallOnEnd;
        this.countOfPlayers = 0;
        this.otherPlayersMap = new HashMap<Socket,Player>();
        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this.linkedGame);

        this.blockTime = configLongGetter.getValueOf(Config.App.CHARACTERS_SUPPER_ATTACK_BLOCK_TIME.key).intValue();
        this.gameSessionScene = new GameSessionScene(this);
        this.gameSessionScene.buildBefore();

        // placement du joueur sur la scène;

        this.maxWidth = configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue();
    }

    /**
     * lance une nouvelle partie
     * @param linkedGame
     * @param character
     * @param pseudo
     * @param toCallOnEnd
     * @throws FileNotFoundException
     * @throws ParseException
     * @throws IOException
     * @throws URISyntaxException
     */
    public GameSession(Game linkedGame,Character character,String pseudo,GameCallback toCallOnEnd) throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this(linkedGame,toCallOnEnd);
        this.linkedPlayer = new Player(character,pseudo,this);

        this.linkedPlayer.setPosition(new Player.Position(30,30,this.maxWidth,new ConfigGetter<Long>(this.linkedGame).getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue() - 40 ) );

        this.gameSessionScene
            .addPlayer(this.linkedPlayer)
            .updatePlayer(this.linkedPlayer,Config.PlayerAction.STATIC_POSITION,null);

        this.manageSavedGames();
    }

    public GameSession(Game linkedGame,GameDataToSave savedGameData,GameCallback toCallOnEnd) throws FileNotFoundException, URISyntaxException{
        this(linkedGame,toCallOnEnd);

        this.linkedPlayer = savedGameData.getSavedPlayer();

        this.linkedPlayer.setPosition(new Player.Position(30,30,this.maxWidth,new ConfigGetter<Long>(this.linkedGame).getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue() - 40 ) );

        this.gameSessionScene
            .addPlayer(this.linkedPlayer)
            .updatePlayer(this.linkedPlayer,Config.PlayerAction.STATIC_POSITION,null);
        
        this.manageSavedGames();
    }

    /**
     * génère le code de partage, cherche les adversaire et lance le jeux une fois trouvé
     * @param countOfParticipants
     * @param toCallWhenGetCode
     * @param toCallAfterFind
     * @param toCallOnFailure
     * @param toCallOnNewPlayer
     * @return this
     */
    public GameSession findOpponents(int countOfParticipants,GameContainerCallback toCallWhenGetCode,GameCallback toCallAfterFind,GameCallback toCallOnFailure,GameContainerCallback toCallOnNewPlayer){
        new Thread(){
            @Override
            public void run(){
                try{
                    ServerManager serverCommunicator = new ServerManager(createActionsMap(),linkedPlayer,countOfParticipants);

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

                    // sauvegarde du code de la partie
                    gameSessionCode = code;

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
        
        return this;
    }

    /**
     * tente de rejoindre une partie, patiente en attendant le début, lance le jeux une fois commencé
     * @param gameCode
     * @param toCallAfterStart
     * @param toCallOnFailure
     * @return this
     */
    public GameSession waitGameStart(String gameCode,GameCallback toCallAfterStart,GameCallback toCallOnFailure){
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
                                // sauvegarde du code
                                gameSessionCode = gameCode;
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
        
        return this;
    }

    /**
     * finis le jeux et retourne à la page d'accueil
     * @return this
     */
    public GameSession endGame(){
        try{
            this.communicator.closeAll();
        }
        catch(Exception e){}

        this.gameSessionScene.getPage().setOnKeyPressed(null);
        this.toCallOnEnd.action();

        return this;
    }

    public GameSession askSaveName(GameContainerCallback toCallOnChoose){
        this.gameSessionScene.askSaveName(toCallOnChoose);

        return this;
    }

    /**
     * sauvegarde cette partie
     * @return si la sauvegarde a réussi
     */
    public boolean saveGame(String saveName){
        try{
            String savePath = GameSession.SAVED_GAMES_PATH + Integer.toString(GameSession.LAST_SAVED_GAME_INDEX) + SavedGames.SAVED_GAMES_EXTENSION;

            GameDataToSave saver = new GameDataToSave(this.linkedPlayer,this.gameSessionCode,saveName,this.countOfPlayers);

            // tentative de sauvegarde du jeux
            if(saver.saveIn(URI.create(savePath) ) ){
                GameSession.LAST_SAVED_GAME_INDEX++;
                // modification du dernier index dans le fichier
                try{
                    FileWriter writer = new FileWriter(GameSession.INDEX_FILE);

                    writer.write(Integer.toString(GameSession.LAST_SAVED_GAME_INDEX) );
                    writer.close();
                }
                catch(Exception e){}

                // envoi du message de sauvegarde
                this.communicator.propagateMessage(new Message(MessageType.SAVE_GAME,null) );
                // fin de la partie
                this.endGame();

                return true;
            }
        }
        catch(Exception e){}

        this.isBlockInDialog = false;

        return false;
    }

    /**
     * annule l'action de sauvegatde
     */
    public void cancelSave(){
        this.isBlockInDialog = false;
    }

    /**
     * lance l'exécution d'unae action
     * @param code
     * @param toCheck
     * @param action
     * @param toDoAfter
     * @param toDoBeforeIfMatch
     * @param conditionToCheck
     * @param player
     * @param fromMessage
     * @param toAddOnEnd
     * @return this
     */
   synchronized private GameSession madeActionIf(KeyCode code,KeyCode toCheck,Config.PlayerAction action,GameCallback toDoAfter,GameCallback toDoBeforeIfMatch,boolean conditionToCheck,Player player,boolean fromMessage,GameCallback toAddOnEnd){
        // aucune action n'est possible durant le saut
        if(!player.getCanDoAction() )
            return this;

        if(code.compareTo(toCheck) == 0 && conditionToCheck)
        {   
            if(toDoBeforeIfMatch != null)
                toDoBeforeIfMatch.action();
                
            // envoi de l'action aux autres participants si l'exécution ne provient pas d'un message
            if(!fromMessage){
                this.communicator.propagateMessage(new Message(MessageType.RECEIVE_PLAYER_ACTION,new PlayerActionMessage(code,action) ) );

                // gestion de l'attaque de mon côté
                if(Player.playerHitActions.contains(action) ){
                    ArrayList<GameCallback> toDo = this.doIfAttackFrom(this.linkedPlayer,action,null,false); 

                    this.gameSessionScene.updatePlayer(player,action,() -> {
                        toDo.forEach(actionToDo -> actionToDo.action() );
                        if(toAddOnEnd != null)
                            toAddOnEnd.action();
                        
                        if(toDoAfter != null)
                            toDoAfter.action();
                    });

                    return this;
                }
            }

            // s'il y a une action supplémentaire à ajouter on défini une fonction qui appellera les deux action
            this.gameSessionScene.updatePlayer(player,action,toAddOnEnd != null ? () -> {
                toAddOnEnd.action();
                if(toDoAfter != null)
                    toDoAfter.action();
            } : toDoAfter); 
        }

        return this;
    }

    /**
     * alias
     * @return this
     */
    private GameSession madeActionIf(KeyCode code,KeyCode toCheck,Config.PlayerAction action,GameCallback toDoAfter,Player player,boolean fromMessage,GameCallback toAddOnEnd){
        return this.madeActionIf(code,toCheck,action,toDoAfter,null,true,player,fromMessage,toAddOnEnd);
    }

    /**
     * alias
     * @return this
     */
    private GameSession madeActionIf(KeyCode code,KeyCode toCheck,Config.PlayerAction action,GameCallback toDoAfter,GameCallback toDoBeforeIfMatch,Player player,boolean fromMessage,GameCallback toAddOnEnd){
        return this.madeActionIf(code,toCheck,action,toDoAfter,toDoBeforeIfMatch,true,player,fromMessage,toAddOnEnd);
    }

    /**
     * 
     * @return la map des actions liés aux types de message
     */
    private HashMap<MessageType,MessageManager> createActionsMap(){
        HashMap<MessageType,MessageManager> map = new HashMap<MessageType,MessageManager>();

        // ajout du joueur entrant dans la page
        map.put(MessageType.RECEIVE_PLAYER,(playerMessage) -> {
            Player player = (Player) playerMessage.getMessageData();

            this.otherPlayersMap.put(playerMessage.getSource(),player);

            this.countOfPlayers++;
            
            this.gameSessionScene
                .addPlayer(player)
                .updatePlayer(player,Config.PlayerAction.STATIC_POSITION,null);
        });
        // gestion d'une action utilisateur dans la page
        map.put(MessageType.RECEIVE_PLAYER_ACTION,(actionMessage) -> {
            // si le joueur n'est pas dans la map alors il a propablement été supprimé
            if(this.otherPlayersMap.get(actionMessage.getSource() ) != null)
                this.managePlayerEntrantAction(actionMessage);
        }); 
        // gestion de la sauvegarde de la partie lancé par un autre
        map.put(MessageType.SAVE_GAME,(saveMessage) -> {
            String sourcePlayerName = this.otherPlayersMap.get(saveMessage.getSource() ).getPseudo();

            Platform.runLater(() -> {
                // affichage préventif au joueur puis sauvegarde et arrêt automatique de la partie chez lui
                this.gameSessionScene.showWinStatusMessage("Partie en cours de sauvegarde lancé par " + sourcePlayerName,GameSessionScene.STATUS_SHOW_TIME,() -> {
                    this.askSaveName((choosedName,unused) -> {
                        // on arrête la partie même en cas d'échec de sauvegarde
                        if(!this.saveGame((String) choosedName) )
                            this.endGame();
                    });
                });
            });
        });

        return map;
    }

    /**
     * lance une partie
     * @return this
     */
    private GameSession startGame(){
        this.lost = false;
        this.isBlockInDialog = false;

        // ajout de la gestion des évenements clavier 
        this.gameSessionScene.getPage().setOnKeyPressed((keyData) -> this.manageKeyEvent(keyData.getCode(),this.linkedPlayer) );

        // affichage de la scène
        this.gameSessionScene.putSceneInWindow();

        return this;
    }  

    /**
     * gère les évenements du clavier
     * @param code
     * @param player
     * @param fromMessage
     * @param toAddOnEnd
     * @return this
     */
    private GameSession manageKeyEvent(KeyCode code,Player player,boolean fromMessage,GameCallback toAddOnEnd){

        if(code.compareTo(GameSession.SAVE_TOUCH) != 0){
            // on autorise l'accès à la gestion si l'action provient d'un message ou que le joueur n'est pas bloqué
            if(fromMessage || !this.isBlockInDialog){
                GameCallback toDoAfter = () -> this.gameSessionScene.updatePlayer(player,Config.PlayerAction.STATIC_POSITION,null);

                this
                    .madeActionIf(code,KeyCode.F,PlayerAction.ATTACK,toDoAfter,player,fromMessage,toAddOnEnd)
                    .madeActionIf(code,KeyCode.D,PlayerAction.SUPER_ATTACK,toDoAfter,() -> {
                        // on bloque la super attaque pendant x temps
                        player.setCanDoSuperAttack(false);

                        this.doAfterBlockTime(() -> player.setCanDoSuperAttack(true) );

                    },player.getCanDoSuperAttack(),player,fromMessage,toAddOnEnd)
                    .madeActionIf(code,KeyCode.SPACE,PlayerAction.JUMP,() -> {
                        // à la fin de la séquence de saut, on lance la séquence de descente
                        this.gameSessionScene.updatePlayer(player,Config.PlayerAction.FALL,() -> {
                            // quand la déscente est terminé on débloque les autres actions
                            player.setCanDoAction(true);
                            player.getPosition().moveOnCurrentDirection(GameSession.JUMP_X_SPEED);
                            toDoAfter.action();
                        });
                    },() -> player.setCanDoAction(false),player,fromMessage,toAddOnEnd)
                    .madeActionIf(code,KeyCode.M,PlayerAction.STATIC_POSITION,null,() -> {
                        // on bloque l'action pendant un certains pour la re activer
                        player.getPosition().moveOnCurrentDirection(this.maxWidth);
                        player.setCanMoveS(false);
                        this.doAfterBlockTime(() -> player.setCanMoveS(true) );
                    },player.getCanMoveS(),player,fromMessage,toAddOnEnd)
                    .madeActionIf(code,KeyCode.RIGHT,PlayerAction.RUN,toDoAfter,() -> {
                        player.getPosition()
                            .setCurrentDirection(Player.Position.Direction.RIGHT)
                            .moveOnCurrentDirection(GameSession.X_SPEED);
                    },player,fromMessage,toAddOnEnd)  
                    .madeActionIf(code,KeyCode.LEFT,PlayerAction.RUN,toDoAfter,() -> {                
                        player.getPosition()
                            .setCurrentDirection(Player.Position.Direction.LEFT)
                            .moveOnCurrentDirection(GameSession.X_SPEED);
                    },player,fromMessage,toAddOnEnd);
            }
        }
        else if(!this.isBlockInDialog){
            // affichage de dialogue de sauvegarde
            this.isBlockInDialog = true;
            this.gameSessionScene.initSaveDialog();
        }

        return this;
    }

    /**
     * alias
     * @param code
     * @param player
     * @return this
     */
    private GameSession manageKeyEvent(KeyCode code,Player player){
        return this.manageKeyEvent(code,player,false,null);
    }   

    /**
     * gère une attaque entrante si l'action en est une
     * @param attacker
     * @param attackAction
     * @param playerToIgnoreSocket
     * @param checkMe
     * @return une liste d'actions à faire après l'attaque si une / des personnes sonttouché
     */
    synchronized private ArrayList<GameCallback> doIfAttackFrom(Player attacker,PlayerAction attackAction,Socket playerToIgnoreSocket,boolean checkMe){
        ArrayList<GameCallback> toDo = new ArrayList<GameCallback>();

        // si l'action est un coup alors 
        if(Player.playerHitActions.contains(attackAction) ){
            ImageView messagePlayerView = this.gameSessionScene.getPlayerManager(attacker).getView();

            // vérification pour les autres joueurs
            for(Map.Entry<Socket,Player> playerEntry : this.otherPlayersMap.entrySet() ){
                // si c'est le même joueur que celui qui a fait l'action on ignore
                if(playerEntry.getKey() == playerToIgnoreSocket)
                    continue;

                GameCallback actionToDo = this.doAttackIfFrom(messagePlayerView,attacker,playerEntry.getValue(),attackAction);

                if(actionToDo != null)
                    toDo.add(actionToDo);
            }

            // vérification pour mon joueur
            if(checkMe){
                GameCallback actionToDo = this.doAttackIfFrom(messagePlayerView,attacker,this.linkedPlayer,attackAction);

                if(actionToDo != null)
                    toDo.add(actionToDo);
            }
        }

        return toDo;
    }
    
    /**
     * gère une attaque entrante si l'action en est une
     * @param messagePlayerView
     * @param attacker
     * @param toAttack
     * @param attackAction
     * @return une action à faire après l'attaque si une personne est touché
     */
    synchronized private GameCallback doAttackIfFrom(ImageView messagePlayerView,Player attacker,Player toAttack,PlayerAction attackAction){
        // vérification de colision
        PlayerManager manager = this.gameSessionScene.getPlayerManager(toAttack);

        ImageView currentPlayerView = manager.getView();
    
        // s'ils se touchent alors colision
        if(messagePlayerView.getBoundsInParent().intersects(currentPlayerView.getBoundsInParent() ) ){
            Player.Position toAttackPosition = toAttack.getPosition();

            Direction attackerCurrentDirection = attacker.getPosition().getCurrentDirection();

            // on tourne le joueur attaqué dans la direction où il se fait attaquer
            toAttackPosition.setCurrentDirection(attackerCurrentDirection == Direction.RIGHT ? Direction.LEFT : Direction.RIGHT);

            /* 
                on réduit la vie du joueur
                si la personne est morte après le coup on joue l'animation de mort et on le retire de la scène de jeux
                sinon on joue l'animation de coup reçu
            */
            if(!toAttack.receiveHitFrom(attacker,attackAction).isDead() ){
                // on recule le joueur en fonction de la direction pointé par celui qui l'attaque
                toAttackPosition.moveOnOppositeDirection(GameSession.HIT_DISTANCE);

                return () -> this.gameSessionScene.updatePlayer(toAttack,PlayerAction.TAKE_HIT,() -> this.gameSessionScene.updatePlayer(toAttack,PlayerAction.STATIC_POSITION,null) );
            }
            else {
                return () -> this.gameSessionScene.updatePlayer(toAttack,PlayerAction.DEATH,() -> {
                    this.gameSessionScene.removePlayer(toAttack);

                    // si le joueur mort n'est pas moi on ferme les ressources sinon on affiche le message de défaite
                    if(toAttack != this.linkedPlayer){
                        // recherche de la socket lié au joueur
                        for(Map.Entry<Socket,Player> entry : this.otherPlayersMap.entrySet() ){

                            if(entry.getValue() == toAttack){
                                Socket playerSocket = entry.getKey();
                                
                                // fermeture des resources et suppression de la liste des joueurs géré
                                this.communicator.close(playerSocket);
                                this.otherPlayersMap.remove(playerSocket);
                            
                                break;
                            }   
                        } 

                        int countOfRestantPlayers = this.otherPlayersMap.size();

                        /* 
                            si tous les autres joueurs ont perdu alors j'ai gagné
                            on affiche le message de victoire et on finis la partie après l'affichage
                            s'il reste un joueur et que j'ai perdu alors il a gagné de son côté (fin de partie)
                        */
                        if(countOfRestantPlayers == 0)
                            this.gameSessionScene.showWinStatusMessage("Vous avez gagne",GameSessionScene.STATUS_SHOW_TIME,() ->  this.endGame() );
                        else if(countOfRestantPlayers == 1 && this.lost)
                            this.endGame();
                    }
                    else {
                        this.lost = true;

                        // s'il reste un joueur quand je perd alors fin de partie
                        this.gameSessionScene.showWinStatusMessage("Vous avez perdu",GameSessionScene.STATUS_SHOW_TIME,this.otherPlayersMap.size() == 1 ? () -> this.endGame() : null);
                    }
                });
            }
        }

        return null;
    }

    /**
     * gère une action reçu d'un joueur
     * @param actionMessage
     * @return this
     */
    synchronized private GameSession managePlayerEntrantAction(Message actionMessage){
        PlayerActionMessage messageData = (PlayerActionMessage) actionMessage.getMessageData();

        Socket source = actionMessage.getSource();

        Player player = this.otherPlayersMap.get(source);

        PlayerAction action = messageData.getAction();

        // gestion de si c'est une attaque et mise à jour de la scène
        ArrayList<GameCallback> toDo = this.doIfAttackFrom(player,action,source,true);

        this.manageKeyEvent(messageData.getCode(),player,true,() -> toDo.forEach(actionToDo -> actionToDo.action() ) );

        return this;
    }

    private GameSession manageSavedGames() throws URISyntaxException, FileNotFoundException{
        // si null alors premier jeux crée on récupère le chemin de sauvegarde des parties et le dernier index de sauvegarde
        if(GameSession.SAVED_GAMES_PATH == null){
            ConfigGetter<String> configStringGetter = new ConfigGetter<String>(this.linkedGame);

            GameSession.SAVED_GAMES_PATH = this.getClass().getResource(configStringGetter.getValueOf(Config.App.SAVED_GAMES_PATH.key) ).toURI().toString();

            String indexFileName = configStringGetter.getValueOf(Config.App.SAVED_GAMES_INDEX_FILENAME.key);

            GameSession.INDEX_FILE = new File(URI.create(GameSession.SAVED_GAMES_PATH + indexFileName) );

            Scanner reader = new Scanner(GameSession.INDEX_FILE);

            try{
                GameSession.LAST_SAVED_GAME_INDEX = reader.nextInt();
                
                reader.close();
            }
            catch(Exception e){
                reader.close();

                throw e;
            }
        }

        return this;
    }

    /**
     * fais une certaine action après le temps de blocage
     * @param toDo
     * @return this
     */
    private GameSession doAfterBlockTime(GameCallback toDo){
        Timeline unlockTimeline = new Timeline(new KeyFrame(Duration.ONE,(e) -> toDo.action() ) );

        unlockTimeline.setCycleCount(1);
        unlockTimeline.setDelay(Duration.millis(this.blockTime) );
        unlockTimeline.play();
        
        return this;
    }

    public Player getLinkedPlayer(){
        return this.linkedPlayer;
    }

    public Game getLinkedGame(){
        return this.linkedGame;
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