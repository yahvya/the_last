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
import yahaya_rachelle.game.GameDataToSave;
import yahaya_rachelle.scene.scene.GameScene;
import yahaya_rachelle.utils.GameContainerCallback;

/**
 * popup de choix de parties sauvegardés
 */
public class SavedGamesPopup extends ScenePopup{

    // public static final int MAX_SHOWED_SAVED_GAMES = 6;
    public static final int MAX_SHOWED_SAVED_GAMES = 2;

    private SavedGames savedGamesManager;

    private int currentIndex;
    private int countOfSavedGames;
    private int lastShowedElementIndex;

    private AnchorPane pane;

    ObservableList<Node> showedGames;

    public SavedGamesPopup(GameScene linkedScene, GameContainerCallback toDoOnConfirm,SavedGames savedGamesManager) {
        super(linkedScene,toDoOnConfirm);

        this.savedGamesManager = savedGamesManager;
        this.currentIndex = 0;
        this.countOfSavedGames = this.savedGamesManager.getSavedGames().size();
        this.addGamesInPopup(new int[]{0,SavedGamesPopup.MAX_SHOWED_SAVED_GAMES});
        this.lastShowedElementIndex = SavedGamesPopup.MAX_SHOWED_SAVED_GAMES;
    }

    @Override
    protected Parent buildPopup(){
        // création de la popup
        this.pane = new AnchorPane();

        GameDataManager dataManager = this.linkedScene.getGameDataManager();

        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this.linkedScene.getGame() );

        final int width = 360;
        final int height = 390;

        this.pane.setBackground(new Background(new BackgroundImage(dataManager.getItems().getImage(Config.Items.PARCHMENT.key), BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100, 100, true, true, false,true) ) ) );
        this.pane.setMinWidth(width);
        this.pane.setMinHeight(height);

        // création du conteneur de la liste de parties
        VBox gamesNameContainer = new VBox(20);

        this.showedGames = gamesNameContainer.getChildren();

        gamesNameContainer.setTranslateX(30);
        gamesNameContainer.setTranslateY(60);
        gamesNameContainer.setMaxWidth(width - 80);

        // création des flèches de changement
        Polygon upArrow = this.createArrow();
        Polygon downArrow = this.createArrow();

        upArrow.setTranslateX(width - 60);
        upArrow.setTranslateY((height / 2) - 30);
        // gestion d'affichage "précédent"
        upArrow.setOnMouseClicked((e) -> {
            if(this.currentIndex != 0)
                this.addGamesInPopup(this.getIndexesToShow(Arrow.UP) );
        });

        downArrow.setRotate(180);
        downArrow.setTranslateX(width - 60);
        downArrow.setTranslateY((height / 2) + 30);
        downArrow.setOnMouseClicked((e) -> {
            if(this.lastShowedElementIndex != this.countOfSavedGames)
                this.addGamesInPopup(this.getIndexesToShow(Arrow.DOWN) );
        });
        
        this.pane.getChildren().addAll(gamesNameContainer,upArrow,downArrow);
        this.pane.setTranslateX((configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue() / 2) - (width / 2) );
        this.pane.setTranslateY((configLongGetter.getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue() / 2) - (height / 2) );

        return this.pane;
    }

    /**
     * calcule les indices min et max à afficher en fonction de la flèche appuyé
     * @return l'indice min et max sous forme d'un tableau à deux cases [min,max]
     */
    private int[] getIndexesToShow(Arrow direction){
        int[] indexes = new int[2];
        
        if(direction == Arrow.UP){
            indexes[0] = this.currentIndex - SavedGamesPopup.MAX_SHOWED_SAVED_GAMES;
            indexes[1] = this.currentIndex;
            this.currentIndex = indexes[0];
        }
        else{
            indexes[0] = this.currentIndex + SavedGamesPopup.MAX_SHOWED_SAVED_GAMES;
            this.currentIndex = indexes[0];
            indexes[1] = this.currentIndex + SavedGamesPopup.MAX_SHOWED_SAVED_GAMES > 
            this.countOfSavedGames ? this.countOfSavedGames : 
            this.currentIndex + SavedGamesPopup.MAX_SHOWED_SAVED_GAMES;
        }

        this.lastShowedElementIndex = indexes[1];

        return indexes;
    }

    /**
     * ajoute les parties dans la popup
     */
    private void addGamesInPopup(int[] limit){
        this.showedGames.clear();
        
        int count = -1;

        for(String savedGameName : this.savedGamesManager.getSavedGames().keySet() ){
            count++;

            if(count < limit[0]) 
                continue;

            if(count >= limit[1])
                break;

            this.showedGames.add(this.getCustomLabel(savedGameName) );
        }

    }

    /**
     * crée un Label en le mettant en forme
     * @param text
     * @return le label crée
     */
    private Label getCustomLabel(String text){
        Label custom = new Label(text);

        custom.setFont(this.linkedScene.getGameDataManager().getFonts().getFont(Config.Fonts.BASIC.key,22) );

        custom.setOnMouseEntered((e) -> custom.setTextFill(Paint.valueOf("brown") ) );
        custom.setOnMouseExited((e) -> custom.setTextFill(Paint.valueOf("black") ) );
        custom.setOnMouseClicked((e) -> this.toDoOnConfirm.action(new SavedGamePopupResult(this.pane,this.savedGamesManager.getSavedGames().get(custom.getText() ) ),false) );

        return custom;
    }

    /**
     * créer une flèche
     * @return la flèche
     */
    private Polygon createArrow(){
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

    /**
     * class représentant le résultat de sélection d'une partie sauvegardé
     */
    public class SavedGamePopupResult{
        private AnchorPane popup;
        private GameDataToSave savedGameData;

        public SavedGamePopupResult(AnchorPane popup,GameDataToSave savedGameData){
            this.popup = popup;
            this.savedGameData = savedGameData;
        }

        public AnchorPane getPopup(){
            return this.popup;
        }

        public GameDataToSave getSavedGameData(){
            return this.savedGameData;
        }
    }
    
    enum Arrow{UP,DOWN};
}
