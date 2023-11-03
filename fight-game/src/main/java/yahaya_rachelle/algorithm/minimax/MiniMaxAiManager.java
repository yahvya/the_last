package yahaya_rachelle.algorithm.minimax;

import javafx.scene.input.KeyCode;
import yahaya_rachelle.actor.AiPlayer;
import yahaya_rachelle.actor.Player;
import yahaya_rachelle.communication.communication.AiCommunicator;
import yahaya_rachelle.communication.communication.Communicator;
import yahaya_rachelle.communication.message.Message;
import yahaya_rachelle.communication.message.PlayerActionMessage;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.game.Game;
import yahaya_rachelle.game.GameSession;
import yahaya_rachelle.utils.GameCallback;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * algorithme de gestion min max
 */
public class MiniMaxAiManager implements AiPlayer.AiPlayerActionManager {
    /**
     * jeux lié
     */
    private final Game linkedGame;

    /**
     * liste des touches clavier (lié aux actions) possibles à faire pour l'ia
     */
    private final HashMap<KeyCode,Config.PlayerAction> actionsToCheck = new HashMap<KeyCode,Config.PlayerAction>(){{
        GameSession.ACTIONS_KEY_MAP.forEach(((playerAction, keyCode) -> this.put(keyCode,playerAction) ) );

        this.put(KeyCode.RIGHT,Config.PlayerAction.RUN);
        this.put(KeyCode.LEFT,Config.PlayerAction.RUN);
    }};

    /**
     *
     * @param linkedGame le jeux lié
     */
    public MiniMaxAiManager(Game linkedGame){
        this.linkedGame = linkedGame;
    }

    @Override
    public PlayerActionMessage getBestActionToDo(Player ai, Player opponent) {
        final int testDepth = 7;
        this.getBestActionToDoFromMinimax(ai,opponent,testDepth);
        return new PlayerActionMessage(KeyCode.LEFT, Config.PlayerAction.RUN);
    }

    /**
     * Récupère la meilleure action à faire basé sur minimax
     * @param max joueur ia
     * @param min opposant
     * @param depth profondeur de l'arbre à visiter
     * @return la meilleure action
     */
    protected Config.PlayerAction getBestActionToDoFromMinimax(Player max, Player min, int depth){
        // arbre des possibilités (racine nulle ignoré sur les opérations)
        Minimax.TreeNode resulTree = new Minimax.TreeNode(
            new Minimax.PlayerState(max),
            new Minimax.PlayerState(min),
            true,
                null
        );

        // construction de l'arbre minimax
        this.buildMinimaxTree(depth,resulTree); System.exit(0);
        // remonté des valeurs sur l'arbre / réduction

        resulTree = null;
        // remonté des valeurs
        return Config.PlayerAction.RUN;
    }

    /**
     * Récupère la meilleure action à faire basé sur minimax
     * @param depth profondeur de l'arbre à visiter
     * @param resultTree arbre contenant le résultat
     * @return this
     */
    protected void buildMinimaxTree(int depth,Minimax.TreeNode resultTree){
        boolean isMax = resultTree.getIsMax();

        this.actionsToCheck.forEach((key,action) -> {
            // copie de l'état (état à ne pas mettre à jour sur les objets de base)
            Minimax.PlayerState newAiState = resultTree.getAiState().copy();
            Minimax.PlayerState newOpponentState = resultTree.getOpponentState().copy();

            // simulation et mise à jour des états
            if(isMax)
                ActionSimulatorSession.simulate(this.linkedGame,key,newAiState,newOpponentState);
            else
                ActionSimulatorSession.simulate(this.linkedGame,key,newOpponentState,newAiState);

            Minimax.TreeNode actionNode = new Minimax.TreeNode(newAiState,newOpponentState,!isMax,key);

            // ajout de l'action dans l'arbre au niveau actuel
            resultTree.addNode(actionNode);

            // construction des possibilités
            if(depth - 1 != 0) this.buildMinimaxTree(depth - 1,actionNode);
        });
    }

    /**
     * session de jeux virtuelle de simulation
     */
    static class ActionSimulatorSession extends GameSession {
        protected ActionSimulatorSession(Game linkedGame) {
            super(linkedGame,null);

            this.lost = false;
            this.isBlockInDialog = false;
        }

        /**
         * simule l'action fourni avec l'ia comme maitre du terrain qui fais l'action
         * @param toSimulate la touche de l'action à simuler
         * @param actionMaker le joueur qui fait l'action (les valeurs de l'objet seront mise à jour après simulation pour conserver l'état de base une copie de l'objet)
         * @param opponentState l'adversaire joueur qui fait l'action (les valeurs de l'objet seront mise à jour après simulation pour conserver l'état de base une
         * @return this
         */
        protected ActionSimulatorSession internalSimulate(KeyCode toSimulate, Minimax.PlayerState actionMaker, Minimax.PlayerState opponentState){
            Player opponent = opponentState.getLinkedPlayer();

            this.linkedPlayer = actionMaker.getLinkedPlayer();
            this.otherPlayersMap.put(null,opponent);

            // aucune action ne sera reçue du joueur par le joueur faisant l'action
            AiCommunicator actionMakerCommunicator = new AiCommunicator(new HashMap<>(),this.linkedPlayer);
            // l'opposant recevra uniquement les attaques
            AiCommunicator opponentCommunicator = new AiCommunicator(this.createOpponentActionsMap(),opponent);

            // création des communicateurs
            actionMakerCommunicator.listenPlayer(opponent,opponentCommunicator);
            opponentCommunicator.listenPlayer(this.linkedPlayer,actionMakerCommunicator);

            // ajout des joueurs dans la scène
            this.gameSessionScene
                .addPlayer(this.linkedPlayer)
                .addPlayer(opponent);

            // exécution dans l'action dans la session de jeux virtuelle (provoque la mise à jour des données des joueurs (positions,vie,blocage d'action...) )
            this.communicator = actionMakerCommunicator;
            this.manageKeyEvent(toSimulate,this.linkedPlayer);

            return this;
        }

        /**
         *
         * @return la map de gestion de message opposant
         */
        protected HashMap<Message.MessageType, Communicator.MessageManager> createOpponentActionsMap(){
            HashMap<Message.MessageType, Communicator.MessageManager> actionsMap = new HashMap<Message.MessageType, Communicator.MessageManager>();

            actionsMap.put(Message.MessageType.RECEIVE_PLAYER_ACTION,(actionMessage) -> {
                PlayerActionMessage messageData = (PlayerActionMessage) actionMessage.getMessageData();

                Config.PlayerAction action = messageData.getAction();

                System.out.println("simulation d'une action faites par : " + this.linkedPlayer + " - action : " + action);

                if(Player.playerHitActions.contains(action) ){
                    // gestion de si c'est une attaque et mise à jour de la scène ainsi que des données de vie
                    GameCallback toDo = this.doAttackIfFrom(
                        this.gameSessionScene.getPlayerManager(this.linkedPlayer).getView(),
                        this.linkedPlayer,
                        this.otherPlayersMap.get(null),
                        action
                    );

                    if(toDo != null) toDo.action();
                }
            });

            return actionsMap;
        }

        /**
         * lance une simulation d'une action
         * @param linkedGame jeux lié
         * @param toSimulate la touche de l'action à simuler
         * @param actionMaker le joueur qui fait l'action (les valeurs de l'objet seront mise à jour après simulation pour conserver l'état de base une copie de l'objet)
         * @param opponentState l'adversaire joueur qui fait l'action (les valeurs de l'objet seront mise à jour après simulation pour conserver l'état de base une
         */
        public static void simulate(Game linkedGame,KeyCode toSimulate, Minimax.PlayerState actionMaker, Minimax.PlayerState opponentState){
            // lancement de la simulation
            ActionSimulatorSession simulator = new ActionSimulatorSession(linkedGame).internalSimulate(toSimulate,actionMaker,opponentState);

            // libération des ressources
            simulator.endGame(false);
        }

        @Override
        synchronized protected GameSession madeActionIf(KeyCode code,KeyCode toCheck,Config.PlayerAction action,GameCallback toDoAfter,GameCallback toDoBeforeIfMatch,boolean conditionToCheck,Player player,boolean fromMessage,GameCallback toAddOnEnd){

            // on joue l'action instantanément pour ne pas prendre le temps pris pour l'animation

            // aucune action n'est possible durant le saut
            if(!player.getCanDoAction() ) return this;

            if(code.compareTo(toCheck) == 0 && conditionToCheck)
            {
                if(toDoBeforeIfMatch != null)  toDoBeforeIfMatch.action();

                // envoi de l'action aux autres participants si l'exécution ne provient pas d'un message
                if(!fromMessage){
                    this.communicator.propagateMessage(new Message(Message.MessageType.RECEIVE_PLAYER_ACTION,new PlayerActionMessage(code,action) ) );

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
                } : toDoAfter,true);
            }

            return this;
        }
    }
}