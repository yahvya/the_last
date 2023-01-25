package yahaya_rachelle.scene.popup;

import javafx.scene.Parent;
import yahaya_rachelle.scene.scene.GameScene;
import yahaya_rachelle.utils.GameContainerCallback;

/**
 * représente toute chose pouvant être intégré dans une page
 */
public abstract class ScenePopup{
    protected GameScene linkedScene;

    protected GameContainerCallback toDoOnConfirm;

    private Parent popup;

    public ScenePopup(GameScene linkedScene,GameContainerCallback toDoOnConfirm){
        this.linkedScene = linkedScene;
        this.toDoOnConfirm = toDoOnConfirm;
        this.popup = this.buildPopup();
    }

    /**
     * 
     * @return la popup
     */
    public Parent getPopup(){
        return this.popup;
    }

    /**
     * construit la popup
     * @return le conteneur
     */
    protected abstract Parent buildPopup();
}
