package yahaya_rachelle.scene.popup;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable.ConfigGetter;
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

        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this.linkedScene.getGame() );

        double width = configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue();
        double height = configLongGetter.getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue();

        // création du conteneur
        VBox container = new VBox(30);

        container.setPrefSize(width / 2,height / 2);
        container.setBackground(new Background(new BackgroundImage(manager.getItems().getImage(Config.Items.PARCHMENT_TEXTURE.key),BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100,100,true,true,true, false) ) ) );
        container.setTranslateX(width / 4);
        container.setTranslateY(height / 4);
        container.setPadding(new Insets(10,10,10,10) );

        ObservableList<Node> children = container.getChildren();

        Label zoneTitle = new Label("Choisir le personnage");

        zoneTitle.setFont(manager.getFonts().getFont(Config.Fonts.BASIC.key,15) );

        children.addAll(zoneTitle,this.createCharactersChooserZone() );

        return container;
    }
    
    /**
     * 
     * @return la zone scrollable d'affichage des personnages
     */
    public ScrollPane createCharactersChooserZone(){
        ScrollPane scrollableZone = new ScrollPane();

        

        return scrollableZone;
    }
}