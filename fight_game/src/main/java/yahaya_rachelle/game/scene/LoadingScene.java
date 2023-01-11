package yahaya_rachelle.game.scene;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import yahaya_rachelle.game.exception.KeyNotExist;
import yahaya_rachelle.game.game.Game;

public class LoadingScene extends GameScene{

    private Timeline loadingAnimationTimeline;

    public LoadingScene(Game game)
    {
        super(game);
    }

    @Override
    protected Scene buildPage() {
        StackPane container = new StackPane();

        ObservableList<Node> children =  container.getChildren();

        try
        {
            Canvas canvas = new Canvas(Game.GAME_WINDOW_WIDTH,Game.GAME_WINDOW_HEIGHT);

            GraphicsContext pen = canvas.getGraphicsContext2D();

            children.add(canvas);

            this.putBackgroundImage(pen);
            this.addLoadingAnimation(children);
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
        pen.drawImage(new Image(this.getClass().getResource(Game.DEFAULT_LOADING_POSTER).toString() ),0,0,Game.GAME_WINDOW_WIDTH,Game.GAME_WINDOW_HEIGHT);
    }
   
    /**
     * ajoute les animations de chargements
     * @param list
     * @throws KeyNotExist
     */
    public void addLoadingAnimation(ObservableList<Node> list) throws KeyNotExist
    {   
        Paint color = Paint.valueOf(Game.DEFAUT_COLOR_ON_FAVICON);

        final int circle_raduis = 15;
        final int rotationSpeed = 95;
        final int yTranslation = 110;

        Label loadingText = new Label("Chargement des ressources");

        Font font = Font.loadFont(this.getClass().getResource(Game.DEFAULT_LOADING_FONT).toString(),25);

        loadingText.setFont(font);
        loadingText.setTextFill(color);
        loadingText.setTranslateY(yTranslation);

        Circle loadingCircle = new Circle();

        loadingCircle.setFill(null);
        loadingCircle.setRadius(circle_raduis);
        loadingCircle.setStroke(color);
        loadingCircle.setStrokeWidth(5);
        loadingCircle.getStrokeDashArray().add(15d);
        loadingCircle.setTranslateY(yTranslation + 60);

        this.loadingAnimationTimeline = new Timeline(new KeyFrame(Duration.millis(rotationSpeed),e -> {

            double currentRotation = loadingCircle.getRotate();

            loadingCircle.setRotate(currentRotation + 30);
        }) );

        this.loadingAnimationTimeline.setCycleCount(Animation.INDEFINITE);
        this.loadingAnimationTimeline.play();

        list.add(loadingCircle);
        list.add(loadingText);
    }

    /**
     * free the loading page resources
     */
    public void destroyScene()
    {   
        this.loadingAnimationTimeline.stop();
    }
}
