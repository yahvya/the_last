package yahaya_rachelle.game.game;

import java.net.URL;
import java.util.HashMap;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import yahaya_rachelle.game.exception.KeyNotExist;
import yahaya_rachelle.game.scene.HomeScene;

public class Game extends Application {

    public static final int GAME_WINDOW_WIDTH = 800;
    public static final int GAME_WINDOW_HEIGHT = 450;

    public static final String GAME_NAME = "Rachelle - Yahaya | Nom du jeux";

    private HashMap<String,String> resourcesPathMap;

    private Stage window;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.window = primaryStage;
        this.resourcesPathMap = new HashMap<String,String>();

        this.fillPathMap();
        this.setWindowStyle();
        this.addBackgroundSound();

        HomeScene homePage = new HomeScene(this);

        homePage.putSceneInWindow(null);

        primaryStage.show();
        primaryStage.centerOnScreen();
    }
    
    /**
     * définis l'apparence de base de la fenêtre
     * @param primaryStage
     */
    public void setWindowStyle(){
        this.window.setWidth(Game.GAME_WINDOW_WIDTH);
        this.window.setHeight(Game.GAME_WINDOW_HEIGHT);
        this.window.setResizable(false);
        this.window.setTitle(Game.GAME_NAME);
        try
        {
            this.window.getIcons().add(new Image(this.getResource("app_images","app-icon.png").toString() ) );
        }
        catch(KeyNotExist e){}
    }

    /**
     * joue la musique de fond du jeux
     */
    public void addBackgroundSound() throws KeyNotExist
    {
        Media sound = new Media(this.getResource("app_songs","home.mp3").toString() );
        MediaPlayer soundPlayer = new MediaPlayer(sound);

        soundPlayer.play();
    }

    /**
     * rempli la hashmap avec les chemin des différentes resources
     */
    public void fillPathMap(){
        this.resourcesPathMap.put("characters","/characters/");
        this.resourcesPathMap.put("app_images","/images/app/");
        this.resourcesPathMap.put("app_items","/images/items/");
        this.resourcesPathMap.put("app_scenes","/images/scenes/");
        this.resourcesPathMap.put("app_songs","/songs/app/");
        this.resourcesPathMap.put("game_songs","/songs/game/");
        this.resourcesPathMap.put("fonts","/fonts/");
    }

    /**
     * 
     * @param key
     * @param resourcePath
     * @return l'URL d'une resource à partir de la map
     * @throws KeyNotExist
     */
    public URL getResource(String key,String resourcePath) throws KeyNotExist{
        if(!this.resourcesPathMap.containsKey(key) )
            throw new KeyNotExist();

        return this.getClass().getResource(this.resourcesPathMap.get(key) + resourcePath);
    }

    /**
     * 
     * @return la map des chemins des sources
     */
    public HashMap<String,String> getResourcesPathMap(){
        return this.resourcesPathMap;
    }

    /**
     * 
     * @return la fenêtre principale
     */
    public Stage getWindow()
    {
        return this.window;
    }
}
