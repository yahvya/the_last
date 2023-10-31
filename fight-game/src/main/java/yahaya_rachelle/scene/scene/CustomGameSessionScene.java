package yahaya_rachelle.scene.scene;

import yahaya_rachelle.game.AiGameSession;
import yahaya_rachelle.game.GameSession;

/**
 * scène custom supprimant la sauvegarde
 */
public class CustomGameSessionScene extends GameSessionScene{

    public CustomGameSessionScene(GameSession gameSession) {
        super(gameSession);
    }
    
    // suppression de l'action d'enregistrement
    @Override
    public void initSaveDialog() {
        ((AiGameSession) this.gameSession).unlockSavingFrom(this);
    }
}
