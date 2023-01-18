package yahaya_rachelle.scene.scene;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.scene.Node;
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

    private ObservableList<Node> children;

    private HashMap<Player,PlayerManager> playersMap;

    public GameSessionScene(GameSession gameSession) {
        super(gameSession.getLinkedGame() );

        this.gameSession = gameSession;
    }

    @Override
    protected Scene buildPage() {
        this.playersMap = new HashMap<Player,PlayerManager>();

        AnchorPane container = new AnchorPane();

        GameDataManager manager = this.getGameDataManager();
        
        container.setBackground(new Background(new BackgroundImage(manager.getScenes().getRandomScene().getSceneImage(),BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100,100,true,true,false, true)) ) );

        this.children = container.getChildren();

        return new Scene(container);
    }

    /**
     * met à jour la position et l'animation du joueur donnée
     * @param player
     * @param action
     * @param toDoOnEnd
     */
    public void updatePlayer(Player player,Config.PlayerAction action,GameCallback toDoOnEnd){
        try{
            PlayerManager manager = this.playersMap.get(player);

            manager
                .updateDirection()
                .updateViewPosition()
                .newAction(action,toDoOnEnd);
        }
        catch(Exception e){}
    }

    /**
     * ajoute un joueur à la scène
     * @param player
     * @return GameSessionScene
     */
    public GameSessionScene addPlayer(Player player){
        PlayerManager manager = new PlayerManager(player);

        this.playersMap.put(player,manager);
        this.children.add(manager.getView() );

        return this;
    }

    /**
     * ajoute un joueur à la scène
     * @param player
     * @return GameSessionScene
     */
    public GameSessionScene
     removePlayer(Player player){
        try{
            PlayerManager manager = this.playersMap.get(player);

            this.children.remove(manager.getView() );
            this.playersMap.remove(player);
        }
        catch(Exception e){}

        return this;
    }

    class PlayerManager{
        private Player player;

        private Timeline timeline;
        
        private ImageView view;

        private Config.PlayerAction currentAction;
        
        private Player.Position.Direction currentDirection;

        private static final int MAX_MS_PER_ACTION = 700;

        public PlayerManager(Player player){
            this.player = player;
            this.timeline = new Timeline();
            this.view = new ImageView();
            this.currentAction = null;
            this.currentDirection = this.player.getPosition().getCurrentDirection();
            this.view.setFitWidth(this.player.getWidth() );
            this.view.setFitHeight(this.player.getHeight() );
        }

        /**
         * met à jour la position de la vue
         * @return this
         */
        public PlayerManager updateViewPosition(){
            Player.Position playerPosition = this.player.getPosition();

            this.view.setTranslateX(playerPosition.getCurrentX() );
            this.view.setTranslateY(playerPosition.getCurrentY() );

            return this;
        }
    
        /**
         * met à jour le sens du joueur
         * @return this
         */
        public PlayerManager updateDirection(){
            Player.Position.Direction currentDirection = this.player.getPosition().getCurrentDirection();

            if(currentDirection != this.currentDirection){
                switch(currentDirection){
                    case RIGHT:
                        this.view.setScaleX(1);
                    ; break;

                    case LEFT:
                        this.view.setScaleX(-1);
                    ; break;
                }

                this.currentDirection = currentDirection;
            }
            
            return this;
        }
    
        /**
         * met à jour l'action actuel
         * @param action
         * @return this
         */
        public void newAction(Config.PlayerAction action,GameCallback toDoOnEnd){
            if(action == this.currentAction)
                return;

            if(this.currentAction == null)
                this.currentAction = action;

            // copie de la séquence d'images décrivant l'action
            ArrayList<Image> sequence = new ArrayList<Image>(this.player.getCharacter().getActionSequence(action) );

            int sequenceSize = sequence.size();

            // arrêt de l'animation précédente
            this.timeline.setOnFinished(null);;
            this.timeline.stop();

            // on joue l'animation de l'action, l'animation durera maxMsForAction et le temps sera partagé entre le nombre d'images
            this.timeline = new Timeline(new KeyFrame(Duration.millis(PlayerManager.MAX_MS_PER_ACTION / sequenceSize),(e) -> {
                try
                {
                    // suppression et récupération de la première image de la séquence
                    Image image = sequence.remove(0);

                    this.view.setImage(image);

                    // on place l'image récupéré à la liste / fin de la séquence
                    sequence.add(image);
                }
                catch(Exception exception){}
            }) );

            // si une action est à éxécuter à la fin de l'animation
            if(toDoOnEnd != null)
                this.timeline.setOnFinished((e) ->  toDoOnEnd.action() );

            // si l'action n'est pas le "sur place" alors elle ne doit pas s'éxécuter à l'infini mais le même nombre de fois qu'il y a d'images dans la séquence
            if(action != Config.PlayerAction.STATIC_POSITION)
                this.timeline.setCycleCount(sequenceSize);
            else
                this.timeline.setCycleCount(Animation.INDEFINITE);
            
            // on lance l'animation puis on modifie l'action actuelle
            this.timeline.play();
            this.currentAction = action;
        }

        public ImageView getView(){
            return this.view;
        }
    }   
}
