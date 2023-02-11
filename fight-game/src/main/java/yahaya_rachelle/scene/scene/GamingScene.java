package yahaya_rachelle.scene.scene;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import yahaya_rachelle.game.Game;

/**
 * représente une page du jeux
 */
public class GamingScene extends GameScene{

    public GamingScene(Game game) {
        super(game);
    }

    @Override
    protected Scene buildPage() {
        AnchorPane container = new AnchorPane();
        
        return new Scene(container);
    }   
}
