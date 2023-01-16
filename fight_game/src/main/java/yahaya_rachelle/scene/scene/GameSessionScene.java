package yahaya_rachelle.scene.scene;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import yahaya_rachelle.game.Game;

public class GameSessionScene extends GameScene{

    public GameSessionScene(Game game) {
        super(game);
    }

    @Override
    protected Scene buildPage() {
        AnchorPane container = new AnchorPane();

        return new Scene(container);
    }
    
}
