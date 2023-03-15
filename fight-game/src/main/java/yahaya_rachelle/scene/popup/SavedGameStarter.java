package yahaya_rachelle.scene.popup;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable.ConfigGetter;
import yahaya_rachelle.scene.scene.GameScene;
import yahaya_rachelle.utils.GameContainerCallback;

/**
 * popup de choix de l'action à faire lors du lancement du partie sauvegardé
 */
public class SavedGameStarter extends ScenePopup{

    public SavedGameStarter(GameScene linkedScene, GameContainerCallback toDoOnConfirm) {
        super(linkedScene, toDoOnConfirm);
    }

    @Override
    protected Parent buildPopup() {
        VBox popup = new VBox(10);

        Label title = new Label("Lancer ou rejoindre la partie");

        title.setFont(this.linkedScene.getGameDataManager().getFonts().getFont(Config.Fonts.BASIC.key,13) );

        HBox container = new HBox(10);

        TextField codeInput = new TextField();

        codeInput.setPromptText("Code de la partie");

        Button joinButton = this.getCustomButton("Rejoindre");

        joinButton.setOnMouseClicked((e) -> {
            String code = codeInput.getText();

            if(code.length() > 1)
                this.toDoOnConfirm.action(new SavedGameStarterPopupResult(popup,code),false);
        });

        Button creationButton = this.getCustomButton("Creer");

        creationButton.setOnMouseClicked((e) -> this.toDoOnConfirm.action(new SavedGameStarterPopupResult(popup),false) );

        container.getChildren().addAll(codeInput,joinButton,creationButton);

        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this.linkedScene.getGame() );

        double width = configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue();
        double height = configLongGetter.getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue();
        double popupWidth = 400;
        double popupHeight = 160;

        popup.getChildren().addAll(title,container);
        popup.setPrefSize(popupWidth,popupHeight);
        popup.setPadding(new Insets(10,10,10,10) );
        popup.setBackground(new Background(new BackgroundImage(this.linkedScene.getGameDataManager().getItems().getImage(Config.Items.PARCHMENT_TEXTURE.key),BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100,100,true,true,true, false) ) ) );
        popup.setTranslateX((width - popupWidth) / 2);
        popup.setTranslateY((height - popupHeight) / 2);

        return popup;
    }

    /**
     * crée un boutton custom
     * @param buttonText
     * @return le boutton
     */
    private Button getCustomButton(String buttonText){
        Button button = new Button(buttonText);

        String color = "#C77F4F";
        String hoverColor = "#98572c";

        // design du boutton
        button.setBackground(Background.fill(Paint.valueOf(color)) );
        button.setFont(this.linkedScene.getGameDataManager().getFonts().getFont(Config.Fonts.BASIC.key,15) );
        button.setWrapText(true);
        button.setOnMouseExited((e) -> button.setBackground(Background.fill(Paint.valueOf(color) ) ) );
        button.setOnMouseEntered((e) -> button.setBackground(Background.fill(Paint.valueOf(hoverColor) ) ) );

        return button;
    }
   
    public class SavedGameStarterPopupResult{
        private VBox popup;

        private String code;

        private boolean choosedToJoin;

        public SavedGameStarterPopupResult(VBox popup){
            this.popup = popup;
            this.choosedToJoin = false;
        }

        public SavedGameStarterPopupResult(VBox popup,String code){
            this.popup = popup;
            this.choosedToJoin = true;
            this.code = code;
        }

        public VBox getPopup(){
            return this.popup;
        }

        public String getCode(){
            return this.code;
        }

        public boolean getChoosedToJoin(){
            return this.choosedToJoin;
        }
    }
}
