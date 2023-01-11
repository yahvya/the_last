package yahaya_rachelle.game.game;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import yahaya_rachelle.game.scene.LoadingScene;

public class Game extends Application {

    public static final int GAME_WINDOW_WIDTH = 800;
    public static final int GAME_WINDOW_HEIGHT = 450;

    public static final String GAME_NAME = "Rachelle - Yahaya | Nom du jeux";
    public static final String DEFAULT_FAVICON_PATH = "/images/app/favicon.png";
    public static final String DEFAULT_LOADING_POSTER = "/images/app/loading-poster.png";
    public static final String DEFAULT_LOADING_FONT = "/fonts/basic.ttf";
    public static final String DEFAUT_COLOR_ON_FAVICON = "#a0714bdb";

    private Stage window;

    private GameDataManager gameDataManager;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.window = primaryStage;
        this.gameDataManager = new GameDataManager();

        this.setWindowStyle();

        // affichage de la page de chargement du jeux
        LoadingScene loadingPage = new LoadingScene(this);

        loadingPage.putSceneInWindow();

        primaryStage.show();
        primaryStage.centerOnScreen();

        // chargement des données et action à exécuter après chargement
        this.gameDataManager.loadDatas(() -> {

            // loadingPage.destroyScene();
        });
    }
    
    /**
     * définis l'apparence de base de la fenêtre
     */
    private void setWindowStyle(){
        this.window.setWidth(Game.GAME_WINDOW_WIDTH);
        this.window.setHeight(Game.GAME_WINDOW_HEIGHT);
        this.window.setResizable(false);
        this.window.setTitle(Game.GAME_NAME);
        this.window.getIcons().add(new Image(this.getClass().getResource(Game.DEFAULT_FAVICON_PATH).toString() ) );
    }
    
    public Stage getWindow(){
        return this.window;
    }

    public GameDataManager getGameDataManager() {
        return this.gameDataManager;
    }
}
