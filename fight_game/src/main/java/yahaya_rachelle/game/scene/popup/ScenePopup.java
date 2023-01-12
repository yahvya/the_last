package yahaya_rachelle.game.scene.popup;

import javafx.scene.layout.Pane;
import yahaya_rachelle.game.scene.scene.GameScene;
import yahaya_rachelle.game.utils.GameContainerCallback;

public abstract class ScenePopup{
    protected GameScene linkedScene;

    protected GameContainerCallback toDoOnConfirm;

    private Pane popup;

    public ScenePopup(GameScene linkedScene,GameContainerCallback toDoOnConfirm){
        this.linkedScene = linkedScene;
        this.toDoOnConfirm = toDoOnConfirm;
        this.popup = this.buildPopup();
    }

    /**
     * 
     * @return la popul
     */
    public Pane getPopup(){
        return this.popup;
    }

    /**
     * construit la popup
     * @return le conteneur
     */
    protected abstract Pane buildPopup();
}
