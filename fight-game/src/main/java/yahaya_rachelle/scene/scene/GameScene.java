package yahaya_rachelle.scene.scene;

import javafx.scene.Scene;
import javafx.stage.Stage;
import yahaya_rachelle.data.GameDataManager;
import yahaya_rachelle.game.Game;

/**
 * représente une page du jeux
 */
public abstract class GameScene {

    protected Game game;
    protected GameDataManager gameDataManager;

    protected Scene page;

    public GameScene(Game game){
        this.game = game;
        this.gameDataManager = game.getGameDataManager();
        this.page = null;
    }

    public GameDataManager getGameDataManager(){
        return this.gameDataManager;
    }

    /**
     * permet d'afficher la page sur la fenêtre
     */
    public void putSceneInWindow(boolean update){
        if(this.page == null || update)
            this.page = this.buildPage();

        Stage window = this.game.getWindow();

        window.setScene(this.page);
        window.setX(window.getX() - 1);
        window.setX(window.getX() + 1);
    }

    /**
     * permet d'afficher la page sur la fenêtre
     */
    public void putSceneInWindow(){
        this.putSceneInWindow(false);
    }

    /**
     * construit la scène sans l'afficher
     */
    public void buildBefore(){
        this.page = this.buildPage();
    }
    
    /**
     * 
     * @return le jeux lié
     */
    public Game getGame(){
        return this.game;
    }

    public Scene getPage(){
        if(this.page == null)
            this.page = this.buildPage();

        return this.page;
    }

    /**
     * permet de construire la scène
     */
    protected abstract Scene buildPage();
}
