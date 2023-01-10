package yahaya_rachelle.game.scene;

import javafx.scene.Scene;
import yahaya_rachelle.game.game.Game;

public abstract class GameScene {

    protected Game game;

    protected Scene page;

    public GameScene(Game game)
    {
        this.game = game;
        this.page = null;
    }

    /**
     * permet d'afficher la page sur la fenêtre
     */
    public void putSceneInWindow(SceneData sceneData,boolean update)
    {
        this.game.getWindow().setScene(this.page == null || update ? this.buildPage(sceneData) : this.page);
    }

    /**
     * permet d'afficher la page sur la fenêtre
     */
    public void putSceneInWindow(SceneData sceneData)
    {
        this.putSceneInWindow(sceneData,false);
    }

    /**
     * permet de construire la scène
     */
    protected abstract Scene buildPage(SceneData sceneData);
}
