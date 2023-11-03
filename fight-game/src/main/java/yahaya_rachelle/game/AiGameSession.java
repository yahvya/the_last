package yahaya_rachelle.game;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import javafx.application.Platform;
import org.json.simple.parser.ParseException;

import yahaya_rachelle.actor.AiPlayer;
import yahaya_rachelle.actor.Character;
import yahaya_rachelle.actor.Player;
import yahaya_rachelle.algorithm.minimax.MiniMaxAiManager;
import yahaya_rachelle.communication.communication.AiCommunicator;
import yahaya_rachelle.communication.communication.Communicator.MessageManager;
import yahaya_rachelle.communication.message.Message;
import yahaya_rachelle.communication.message.Message.MessageType;
import yahaya_rachelle.communication.message.PlayerActionMessage;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Config.PlayerAction;
import yahaya_rachelle.scene.scene.CustomGameSessionScene;
import yahaya_rachelle.utils.GameCallback;

/**
 * session de jeux contre l'ia
 */
public class AiGameSession extends GameSession{
    /**
     * joueur ia lié
     */
    protected AiPlayer aiPlayer;

    public AiGameSession(Game linkedGame, Character character, String pseudo, GameCallback toCallOnEnd) throws FileNotFoundException, ParseException, IOException, URISyntaxException {
        super(linkedGame, character, pseudo, toCallOnEnd);

        // définition du communicateur ia
        HashMap<MessageType,MessageManager> actionsMap = this.createActionsMap();

        actionsMap.put(MessageType.RECEIVE_PLAYER_ACTION,(actionMessage) -> this.managePlayerEntrantAction(actionMessage) );

        // suppression des évenements non utiles
        actionsMap.remove(MessageType.SAVE_GAME);

        // redéfinition de la scène
        this.gameSessionScene = new CustomGameSessionScene(this);

        this.gameSessionScene.buildBefore();
        this.gameSessionScene
            .addPlayer(this.linkedPlayer)
            .updatePlayer(this.linkedPlayer,Config.PlayerAction.STATIC_POSITION,null);

        // création du personnage de l'ia
        this.aiPlayer = new AiPlayer(this,new MiniMaxAiManager(this.linkedGame) );

        this.aiPlayer.setPosition(
            new Player.Position(
                this.maxWidth - this.aiPlayer.getWidth(),
                30,
                this.maxWidth,
                new ConfigGetter<Long>(this.linkedGame).getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue() - 40 
            )
        );

        AiCommunicator linkedPlayerCommunicator = new AiCommunicator(actionsMap,this.linkedPlayer);
        AiCommunicator aiCommunicator = new AiCommunicator(this.createIaActionsMap(),this.aiPlayer);

        // simule l'écoute du personnage , provoque l'ajout du personnage de l'ia dans la scène de jeux du joueur
        linkedPlayerCommunicator.listenPlayer(this.aiPlayer,aiCommunicator);
        aiCommunicator.listenPlayer(this.linkedPlayer,linkedPlayerCommunicator);

        this.communicator = linkedPlayerCommunicator;
        this.aiPlayer.setCommunicator(aiCommunicator);

        // lancement du jeux
        this.gameSessionScene.putSceneInWindow();
        this.startGame();

        new Thread(this.aiPlayer).start();
    }

    /**
     * 
     * @return le gestion de message de l'ia
     */
    protected HashMap<MessageType,MessageManager> createIaActionsMap(){
        HashMap<MessageType,MessageManager> map = new HashMap<MessageType,MessageManager>();

        // ajout du joueur entrant dans la page
        map.put(MessageType.RECEIVE_PLAYER,(playerMessage) -> {
            Player player = (Player) playerMessage.getMessageData();

            this.aiPlayer.setOpponent(player);
        });
        
        map.put(MessageType.RECEIVE_PLAYER_ACTION,(actionMessage) -> {
            PlayerActionMessage messageData = (PlayerActionMessage) actionMessage.getMessageData();

            PlayerAction action = messageData.getAction();

            if(Player.playerHitActions.contains(action) ){
                // gestion de si c'est une attaque et mise à jour de la scène ainsi que des données de vie
                GameCallback toDo = this.doAttackIfFrom(
                    this.gameSessionScene.getPlayerManager(this.linkedPlayer).getView(),
                    this.linkedPlayer,
                    this.aiPlayer,
                    action
                );

                if(toDo != null) toDo.action();
            }
        });

        return map;
    }

    @Override
    protected synchronized GameSession managePlayerEntrantAction(Message actionMessage) {
        Platform.runLater(() -> super.managePlayerEntrantAction(actionMessage) );

        return this;
    }

    /**
     * dévérouille le blocage de sauvegarde
     * @param scene la scène qui fais la requête
     * @return this
     */
    public AiGameSession unlockSavingFrom(CustomGameSessionScene scene){
        if(scene == this.gameSessionScene) this.isBlockInDialog = false;

        return this;
    }

    @Override
    public GameSession endGame(boolean afterSave) {
        this.aiPlayer.stop();
        return super.endGame(afterSave);
    }
}
