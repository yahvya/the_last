package yahaya_rachelle.game.scene;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
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
        pen.drawImage(new Image(this.getClass().getResource(Game.DEFAULT_FAVICON_PATH).toString() ),0,0,Game.GAME_WINDOW_WIDTH,Game.GAME_WINDOW_HEIGHT);
    }
   
    /**
     * ajoute le cercle de chargement
     * @param list
     * @throws KeyNotExist
     */
    public void addLoadingAnimation(ObservableList<Node> list) throws KeyNotExist
    {
        final int circle_raduis = 15;
        final int rotationSpeed = 95;

        Circle loadingCircle = new Circle();

        loadingCircle.setFill(null);
        loadingCircle.setRadius(circle_raduis);
        loadingCircle.setStroke(Paint.valueOf(Game.DEFAUT_COLOR_ON_FAVICON) );
        loadingCircle.setStrokeWidth(5);
        loadingCircle.getStrokeDashArray().add(15d);
        loadingCircle.setTranslateX(Game.GAME_WINDOW_WIDTH / 2 - circle_raduis - 30);
        loadingCircle.setTranslateY(Game.GAME_WINDOW_HEIGHT / 2 - circle_raduis - 30);

        this.loadingAnimationTimeline = new Timeline(new KeyFrame(Duration.millis(rotationSpeed),e -> {
            double currentRotation = loadingCircle.getRotate();

            loadingCircle.setRotate(currentRotation + 30);
        }) );

        this.loadingAnimationTimeline.setCycleCount(Animation.INDEFINITE);
        this.loadingAnimationTimeline.play();
        
        list.add(loadingCircle);
    }

    /**
     * free the loading page resources
     */
    public void destroyScene()
    {   
        this.loadingAnimationTimeline.stop();
    }
}
