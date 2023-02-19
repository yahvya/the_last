package yahaya_rachelle.scene.popup;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable.ConfigGetter;
import yahaya_rachelle.data.GameDataManager;
import yahaya_rachelle.data.SavedGames;
import yahaya_rachelle.scene.scene.GameScene;
import yahaya_rachelle.utils.GameContainerCallback;

/**
 * popup de choix de parties sauvegardés
 */
public class SavedGamesPopup extends ScenePopup{

    public static final int MAX_SHOWED_SAVED_GAMES = 6;

    private SavedGames savedGamesManager;

    private int currentIndex;
    private int countOfSavedGames;

    ObservableList<Node> showedGames;

    public SavedGamesPopup(GameScene linkedScene, GameContainerCallback toDoOnConfirm,SavedGames savedGamesManager) {
        super(linkedScene,toDoOnConfirm);

        this.savedGamesManager = savedGamesManager;
        this.currentIndex = 0;
        this.countOfSavedGames = this.savedGamesManager.getSavedGames().size();
        this.addGamesInPopup();
    }

    /**
     * 
     * @return l'indice min et max des valeurs à afficher
     */
    private int[] getIndexesToShow(Arrow direction){
        int[] indexes = new int[2];

        if(direction == Arrow.UP){
            indexes[0] = this.currentIndex - SavedGamesPopup.MAX_SHOWED_SAVED_GAMES;
            indexes[1] = this.currentIndex - 1;
        }
        else{
            indexes[0] = this.currentIndex + SavedGamesPopup.MAX_SHOWED_SAVED_GAMES;
            this.currentIndex = indexes[0];
            indexes[1] = this.currentIndex + SavedGamesPopup.MAX_SHOWED_SAVED_GAMES > 
            this.countOfSavedGames ? ((this.countOfSavedGames - 1) - this.currentIndex) + this.currentIndex : 
            this.currentIndex + (SavedGamesPopup.MAX_SHOWED_SAVED_GAMES - 1);
        }

        this.currentIndex = indexes[0];

        return indexes;
    }

    @Override
    protected Parent buildPopup(){
        AnchorPane popup = new AnchorPane();

        GameDataManager dataManager = this.linkedScene.getGameDataManager();

        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this.linkedScene.getGame() );

        final int width = 360;
        final int height = 390;

        popup.setBackground(new Background(new BackgroundImage(dataManager.getItems().getImage(Config.Items.PARCHMENT.key), BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100, 100, true, true, false,true) ) ) );
        popup.setMinWidth(width);
        popup.setMinHeight(height);

        VBox gamesNameContainer = new VBox(20);

        this.showedGames = gamesNameContainer.getChildren();

        gamesNameContainer.setTranslateX(30);
        gamesNameContainer.setTranslateY(60);
        gamesNameContainer.setMaxWidth(width - 80);

        Polygon upArrow = this.getArrow();
        Polygon downArrow = this.getArrow();

        upArrow.setTranslateX(width - 60);
        upArrow.setTranslateY((height / 2) - 30);

        downArrow.setRotate(180);
        downArrow.setTranslateX(width - 60);
        downArrow.setTranslateY((height / 2) + 30);
        
        popup.getChildren().addAll(gamesNameContainer,upArrow,downArrow);
        popup.setTranslateX((configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue() / 2) - (width / 2) );
        popup.setTranslateY((configLongGetter.getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue() / 2) - (height / 2) );

        return popup;
    }

    /**
     * ajoute les parties dans la popup
     */
    private void addGamesInPopup(){
        int count = 0;

        for(String savedGameName : this.savedGamesManager.getSavedGames().keySet() ){
            if(count == SavedGamesPopup.MAX_SHOWED_SAVED_GAMES) 
                break;

            this.showedGames.add(this.getCustomLabel(savedGameName) );

            count++;
        }

    }

    private Label getCustomLabel(String text){
        Label custom = new Label(text);

        custom.setFont(this.linkedScene.getGameDataManager().getFonts().getFont(Config.Fonts.BASIC.key,22) );

        custom.setOnMouseEntered((e) -> custom.setTextFill(Paint.valueOf("brown") ) );
        custom.setOnMouseExited((e) -> custom.setTextFill(Paint.valueOf("black") ) );
        custom.setOnMouseClicked((e) -> this.toDoOnConfirm.action(this.savedGamesManager.getSavedGames().get(custom.getText() ),false) );

        return custom;
    }

    /**
     * créer une flèche
     * @return la flèche
     */
    private Polygon getArrow(){
        Polygon arrow = new Polygon();

        arrow.getPoints().addAll(new Double[]{
            20.0,0.0,
            0.0,30.0,
            40.0,30.0
        });

        Paint color = Paint.valueOf("#C77F4F");
        Paint hoverColor = Paint.valueOf("#98572c");

        arrow.setFill(color);

        arrow.setOnMouseEntered((e) -> arrow.setFill(hoverColor) );
        arrow.setOnMouseExited((e) -> arrow.setFill(color) );

        return arrow;
    }
    
    enum Arrow{UP,DOWN};
}
