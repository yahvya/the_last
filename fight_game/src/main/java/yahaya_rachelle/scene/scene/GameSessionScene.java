package yahaya_rachelle.scene.scene;

import java.util.ArrayList;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.util.Duration;
import yahaya_rachelle.actor.Player;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.data.GameDataManager;
import yahaya_rachelle.game.GameSession;
import yahaya_rachelle.utils.GameCallback;

public class GameSessionScene extends GameScene{

    private GameSession gameSession;

    private Player playerOne;
    private Player playerTwo;

    private Timeline playerOneTimeLine;
    private Timeline playerTwoTimeLine;

    private ImageView playerOneView;
    private ImageView playerTwoView;

    private Config.PlayerAction playerOneCurrentAction;

    private Player.Position.Direction playerOneCurrentDirection;

    private int maxMsForAction = 700;

    public GameSessionScene(GameSession gameSession) {
        super(gameSession.getLinkedGame() );

        this.gameSession = gameSession;
    }

    @Override
    protected Scene buildPage() {
        AnchorPane container = new AnchorPane();

        this.playerOneView = new ImageView();
        this.playerTwoView = new ImageView();
        this.playerOneTimeLine = new Timeline();
        this.playerTwoTimeLine = new Timeline();

        this.playerOneView.setPreserveRatio(true);
        this.playerTwoView.setPreserveRatio(true);

        this.playerOneCurrentAction = null;

        GameDataManager manager = this.getGameDataManager();
        
        container.setBackground(new Background(new BackgroundImage(manager.getScenes().getRandomScene().getSceneImage(),BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100,100,true,true,false, true)) ) );

        container.getChildren().addAll(this.playerOneView,this.playerTwoView);

        return new Scene(container);
    }

    /**
     * met à jour la position et l'animation du joueur 1, à appeller après que setPlayerOne ait été appelé
     */
    public void updatePlayerOne(Config.PlayerAction action,GameCallback toDoOnEnd){
        Player.Position playerPosition = this.playerOne.getPosition();

        // mise à jour de la direction si nécessaire

        Player.Position.Direction currentDirection = playerPosition.getDirection();

        if(currentDirection != this.playerOneCurrentDirection){
            switch(currentDirection){
                case RIGHT:
                    this.playerOneView.setScaleX(1);
                ; break;

                case LEFT:
                    this.playerOneView.setScaleX(-1);
                ; break;
            }

            this.playerOneCurrentDirection = currentDirection;
        }

        if(this.playerOneCurrentAction == null)
            this.playerOneCurrentAction = action;
        else if(action == this.playerOneCurrentAction)
            return;

        // copie de la séquence d'images décrivant l'action
        ArrayList<Image> sequence = new ArrayList<Image>(this.playerOne.getCharacter().getActionSequence(action) );

        int sequenceSize = sequence.size();

        // mise à jour de la position du joueur

        this.playerOneView.setTranslateX(playerPosition.getCurrentX() );
        this.playerOneView.setTranslateY(playerPosition.getCurrentY() );

        // arrêt de l'animation précédente
        this.playerOneTimeLine.setOnFinished(null);;
        this.playerOneTimeLine.stop();

        // on joue l'animation de l'action, l'animation durera maxMsForAction et le temps sera partagé entre le nombre d'images
        this.playerOneTimeLine = new Timeline(new KeyFrame(Duration.millis(this.maxMsForAction / sequenceSize),(e) -> {
            try
            {
                // suppression et récupération de la première image de la séquence
                Image image = sequence.remove(0);

                this.playerOneView.setImage(image);

                // on place l'image récupéré à la liste / fin de la séquence
                sequence.add(image);
            }
            catch(Exception exception){}
        }) );

        // si une action est à éxécuter à la fin de l'animation
        if(toDoOnEnd != null)
            this.playerOneTimeLine.setOnFinished((e) ->  toDoOnEnd.action() );

        // si l'action n'est pas le sur place alors elle ne doit pas s'éxécuter à l'infini mais le nombre d'image qu'il y a
        if(action != Config.PlayerAction.STATIC_POSITION)
            this.playerOneTimeLine.setCycleCount(sequenceSize);
        else
            this.playerOneTimeLine.setCycleCount(Animation.INDEFINITE);
        
        this.playerOneTimeLine.play();
        this.playerOneCurrentAction = action;
    }

    /**
     * met à jour la position et l'animation du joueur 2, à appeller après que setPlayerTwo ait été appelé
     */
    public void updatePlayerTwo(){

    }

    /**
     * 
     * @return la valeur minimum du y utilisateur
     */
    public int getMinY(){
        return 100;   
    }

    /**
     * définis le joueur 1 et met à jour les dimensions de son objet vue
     * @param playerOne
     */
    public void setPlayerOne(Player playerOne){
        this.playerOne = playerOne;
        this.playerOneCurrentDirection = this.playerOne.getPosition().getDirection();
        this.playerOneView.setFitWidth(this.playerOne.getWidth() );
        this.playerOneView.setFitHeight(this.playerOne.getHeight() );
    }

    /**
     * définis le joueur 2 et met à jour les dimensions de son objet vue
     * @param playerTwo
     */
    public void setPlayerTwo(Player playerTwo){
        this.playerTwo = playerTwo;
        this.playerTwoView.setFitWidth(this.playerTwo.getWidth() );
        this.playerTwoView.setFitHeight(this.playerTwo.getHeight() );
    }
}
