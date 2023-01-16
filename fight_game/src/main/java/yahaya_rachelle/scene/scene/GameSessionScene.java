package yahaya_rachelle.scene.scene;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import yahaya_rachelle.data.GameDataManager;
import yahaya_rachelle.game.GameSession;

public class GameSessionScene extends GameScene{

    private GameSession gameSession;

    public GameSessionScene(GameSession gameSession) {
        super(gameSession.getLinkedGame() );

        this.gameSession = gameSession;
    }

    @Override
    protected Scene buildPage() {
        AnchorPane container = new AnchorPane();

        GameDataManager manager = this.getGameDataManager();
        
        container.setBackground(new Background(new BackgroundImage(manager.getScenes().getRandomScene().getSceneImage(),BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100,100,true,true,false, true)) ) );

        return new Scene(container);
    }
}
