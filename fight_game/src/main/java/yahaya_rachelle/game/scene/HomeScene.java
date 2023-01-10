package yahaya_rachelle.game.scene;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import yahaya_rachelle.game.exception.KeyNotExist;
import yahaya_rachelle.game.game.Game;

public class HomeScene extends GameScene{

    public HomeScene(Game game)
    {
        super(game);
    }

    @Override
    protected Scene buildPage(SceneData sceneData) {
        StackPane container = new StackPane();

        ObservableList<Node> children =  container.getChildren();

        try
        {
            Canvas canvas = new Canvas(Game.GAME_WINDOW_WIDTH,Game.GAME_WINDOW_HEIGHT);

            GraphicsContext pen = canvas.getGraphicsContext2D();

            children.add(canvas);

            this.putBackgroundImage(pen);
            this.addLoadingText(children);
        }
        catch(KeyNotExist e){}

        return new Scene(container);
    }

    /**
     * ajoute // ajout de l'image d'accueil en fond
     * @param pen
     * @param list
     * @throws KeyNotExist
     */
    public void putBackgroundImage(GraphicsContext pen) throws KeyNotExist
    {
        pen.drawImage(new Image(this.game.getResource("app_images", "app-icon.png").toString() ),0,0,Game.GAME_WINDOW_WIDTH,Game.GAME_WINDOW_HEIGHT);
    }
   
    
    public void addLoadingText(ObservableList<Node> list) throws KeyNotExist
    {
        // lecture du fichier de configuration

        Label loadingText = new Label("Chargement des ressources");

        loadingText.setFont(Font.loadFont(this.game.getResource("fonts","special.ttf").toString(),23) );
        loadingText.setStyle("-fx-text-fill: #fff");

        list.add(loadingText);
    }
}
