package yahaya_rachelle.game.scene.scene;

import javafx.scene.Scene;
import yahaya_rachelle.game.game.Game;
import yahaya_rachelle.game.game.GameDataManager;

public abstract class GameScene {

    protected Game game;
    protected GameDataManager gameDataManager;

    protected Scene page;

    public GameScene(Game game)
    {
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
    public void putSceneInWindow(boolean update)
    {
        this.game.getWindow().setScene(this.page == null || update ? this.buildPage() : this.page);
    }

    /**
     * permet d'afficher la page sur la fenêtre
     */
    public void putSceneInWindow()
    {
        this.putSceneInWindow(false);
    }

    
    /**
     * 
     * @return le jeux lié
     */
    public Game getGame(){
        return this.game;
    }

    /**
     * permet de construire la scène
     */
    protected abstract Scene buildPage();
}
