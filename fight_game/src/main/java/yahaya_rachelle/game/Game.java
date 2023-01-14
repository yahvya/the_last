package yahaya_rachelle.game;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable;
import yahaya_rachelle.data.GameDataManager;
import yahaya_rachelle.scene.scene.HomeScene;
import yahaya_rachelle.scene.scene.LoadingScene;

public class Game extends Configurable{

    private Stage window;

    private GameDataManager gameDataManager;

    public Game(Stage primaryStage){
        try{
            // lecture du fichier de configuration et lancement du chargement
            this.window = primaryStage;
            this.gameDataManager = new GameDataManager(this);

            this.setConfig();
            this.setWindowStyle();

            // affichage de la page de chargement du jeux
            LoadingScene loadingPage = new LoadingScene(this);

            loadingPage.putSceneInWindow();

            primaryStage.show();
            primaryStage.centerOnScreen();

            // chargement des données et action à exécuter après chargement
            this.gameDataManager.loadDatas(
                () -> {
                    loadingPage.destroyScene();
                    new HomeScene(this).putSceneInWindow();
                },
                () -> {
                    this.showLoadingError();
                }
            );
        }
        catch(Exception e){
            this.showLoadingError();
        }
    }
    
    private void showLoadingError(){
        Alert errorAlert = new Alert(AlertType.ERROR);

        errorAlert.setHeaderText("Echec de chargement des resources, veuillez relancer le jeux");

        errorAlert.show();
    }

    /**
     * définis l'apparence de base de la fenêtre
     */
    private void setWindowStyle(){
        ConfigGetter<String> configStringGetter = new ConfigGetter<String>(this);
        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this);

        this.window.setWidth(configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue() );
        this.window.setHeight(configLongGetter.getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue() );
        this.window.setTitle(configStringGetter.getValueOf(Config.App.GAME_NAME.key) );
        this.window.setResizable(false);
        this.window.getIcons().add(new Image(this.getClass().getResource(configStringGetter.getValueOf(Config.App.FAVICON_PATH.key) ).toString() ) );
    }
    
    public Stage getWindow(){
        return this.window;
    }

    public GameDataManager getGameDataManager() {
        return this.gameDataManager;
    }

    @Override
    protected String getConfigFilePath() {
        return "/config/app.json";
    }
}
