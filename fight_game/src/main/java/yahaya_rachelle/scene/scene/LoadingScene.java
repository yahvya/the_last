package yahaya_rachelle.scene.scene;

import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable.ConfigGetter;
import yahaya_rachelle.exception.KeyNotExist;
import yahaya_rachelle.game.Game;
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

/**
 * représente la page de chargement des resources
 */
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

        try{
            ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this.game);

            Canvas canvas = new Canvas(configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue(),configLongGetter.getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue() );

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
    public void putBackgroundImage(GraphicsContext pen) throws KeyNotExist{
        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this.game);

        pen.drawImage(new Image(this.getClass().getResource(new ConfigGetter<String>(this.game).getValueOf(Config.App.LOADING_POSTER_PATH.key) ).toString() ),0,0,configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue(),configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue() );
    }
   
    /**
     * ajoute les animations de chargement
     * @param list
     * @throws KeyNotExist
     */
    public void addLoadingAnimation(ObservableList<Node> list) throws KeyNotExist
    {   
        ConfigGetter<String> configStringGetter = new ConfigGetter<String>(this.game);

        Paint color = Paint.valueOf(configStringGetter.getValueOf(Config.App.LOADING_ON_COLOR.key) );

        final int circle_raduis = 15;
        final int rotationSpeed = 95;
        final int yTranslation = 110;

        Label loadingText = new Label("Chargement des ressources");

        Font font = Font.loadFont(this.getClass().getResource(configStringGetter.getValueOf(Config.App.LOADING_FONT_PATH.key) ).toString(),25);

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

        this.loadingAnimationTimeline = new Timeline(new KeyFrame(Duration.millis(rotationSpeed),(e) -> {

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
    public void destroyScene(){   
        this.loadingAnimationTimeline.stop();
    }
}
