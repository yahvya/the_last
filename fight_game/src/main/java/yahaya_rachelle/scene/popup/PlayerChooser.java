package yahaya_rachelle.scene.popup;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.data.GameDataManager;
import yahaya_rachelle.scene.scene.GameScene;
import yahaya_rachelle.utils.GameContainerCallback;

public class PlayerChooser extends ScenePopup{

    public PlayerChooser(GameScene linkedScene, GameContainerCallback toDoOnConfirm) {
        super(linkedScene, toDoOnConfirm);
    }

    @Override
    protected Parent buildPopup() {
        GameDataManager manager = this.linkedScene.getGameDataManager();

        VBox container = new VBox(25);

        ObservableList<Node> children = container.getChildren();

        Font font = manager.getFonts().getFont(Config.Fonts.BASIC.key,14);

        Label title = new Label("Choisissez un personnage");

        title.setFont(font);
        
        HBox playersBox = new HBox(5);
        HBox buttonsBox = new HBox(15);

        children.addAll(title,playersBox,buttonsBox);

        return container;
    }
}
