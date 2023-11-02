package yahaya_rachelle.algorithm;

import javafx.scene.input.KeyCode;
import yahaya_rachelle.actor.AiPlayer;
import yahaya_rachelle.actor.Player;
import yahaya_rachelle.communication.message.PlayerActionMessage;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.game.Game;
import yahaya_rachelle.game.GameSession;
import yahaya_rachelle.utils.GameCallback;

/**
 * algorithme de gestion min max
 */
public class MiniMaxAiManager implements AiPlayer.AiPlayerActionManager {
    /**
     * jeux lié
     */
    private final Game linkedGame;

    /**
     *
     * @param linkedGame le jeux lié
     */
    public MiniMaxAiManager(Game linkedGame){
        this.linkedGame = linkedGame;
    }

    @Override
    public PlayerActionMessage getBestActionToDo(Player ai, Player opponent) {
        // test de fonction du retour de directive
        var aiX = ai.getPosition().getCurrentX();
        var opponentX = opponent.getPosition().getCurrentX();
        if(Math.abs(aiX - opponentX) < 40) return new PlayerActionMessage(GameSession.ACTIONS_KEY_MAP.get(Config.PlayerAction.ATTACK), Config.PlayerAction.ATTACK );
        else if(aiX < opponentX) return new PlayerActionMessage(KeyCode.RIGHT, Config.PlayerAction.RUN);
        else return new PlayerActionMessage(KeyCode.LEFT,Config.PlayerAction.RUN);
    }

    /**
     * session de jeux virtuelle de simulation
     */
    static class ActionSimulatorSession extends GameSession{
        protected ActionSimulatorSession(Game linkedGame, GameCallback toCallOnEnd) {
            super(linkedGame, toCallOnEnd);

            this.lost = false;
            this.isBlockInDialog = false;
        }

        /**
         * simule l'action fourni avec l'ia comme maitre du terrain qui fais l'action
         * @param toSimulate l'action à faire par l'ia
         * @param ai le joueur ia (les valeurs de l'objet seront mise à jour après simulation pour conserver l'état de base une copie de l'objet)
         * @param opponent l'adversaire joueur ia (les valeurs de l'objet seront mise à jour après simulation pour conserver l'état de base une copie de l'objet)
         * @return this
         */
        ActionSimulatorSession simulate(PlayerActionMessage toSimulate,Player ai,Player opponent){
            try {
                // intégration de l'ia comme joueur lié à la session et ajout de l'adversaire comme opposant
                this.linkedPlayer = ai;
                this.otherPlayersMap.put(null, opponent);

                // exécution dans l'action dans la session de jeux virtuelle (provoque la mise à jour des données des joueurs (positions,vie,blocage d'action...) )
                this.manageKeyEvent(toSimulate.getCode(),ai);
            }
            catch(Exception e){}

            return this;
        }
    }
}