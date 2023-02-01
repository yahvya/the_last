package yahaya_rachelle.scene.scene;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import yahaya_rachelle.actor.Player;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable.ConfigGetter;
import yahaya_rachelle.data.GameDataManager;
import yahaya_rachelle.game.GameSession;
import yahaya_rachelle.utils.GameCallback;

/**
 * représente la page d'affichage d'une partie
 */
public class GameSessionScene extends GameScene{
    private GameSession gameSession;

    private ObservableList<Node> children;

    private HashMap<Player,PlayerManager> playersMap;

    private double playersMaxLife;

    private ObservableList<Node> lifebarsList;

    public GameSessionScene(GameSession gameSession) {
        super(gameSession.getLinkedGame() );

        this.gameSession = gameSession;
    }

    @Override
    protected Scene buildPage() {
        this.playersMap = new HashMap<Player,PlayerManager>();
        this.playersMaxLife = new ConfigGetter<Long>(this.game).getValueOf(Config.App.PLAYERS_LIFE.key).doubleValue();

        AnchorPane container = new AnchorPane();

        HBox lifeContainer = new HBox(30);

        lifeContainer.setTranslateY(30);
        lifeContainer.setPadding(new Insets(0,20,0,20) );

        GameDataManager manager = this.getGameDataManager();
        
        container.setBackground(new Background(new BackgroundImage(manager.getScenes().getRandomScene().getSceneImage(),BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100,100,true,true,false, true)) ) );

        this.children = container.getChildren();
        this.lifebarsList = lifeContainer.getChildren();
        this.children.add(lifeContainer);

        // ajout du boutton de gestion
        this.lifebarsList.add(this.createGestionButton() );

        return new Scene(container);
    }

    /**
     * met à jour la position et l'animation du joueur donnée
     * @param player
     * @param action
     * @param toDoOnEnd
     */
   synchronized public GameSessionScene updatePlayer(Player player,Config.PlayerAction action,GameCallback toDoOnEnd){

        try{
            PlayerManager manager = this.playersMap.get(player);

            manager
                .updateDirection()
                .updateViewPosition()
                .updateLifebar()
                .newAction(action,toDoOnEnd);
        }
        catch(Exception e){}

        return this;
    }

    /**
     * ajoute un joueur à la scène
     * @param player
     * @return GameSessionScene
     */
    public GameSessionScene addPlayer(Player player){
        PlayerManager manager = new PlayerManager(player,this.playersMaxLife);

        this.playersMap.put(player,manager);
        this.children.add(manager.getView() );
        this.lifebarsList.add(manager.getLifebar() );
               
        return this;
    }

    /**
     * supprime un joueur à la scène
     * @param player
     * @return GameSessionScene
     */
    public GameSessionScene removePlayer(Player player){
        try{
            PlayerManager manager = this.playersMap.get(player);

            this.children.remove(manager.getView() );
            this.playersMap.remove(player);
            this.lifebarsList.remove(manager.getLifebar());
        }
        catch(Exception e){}

        return this;
    }

    /**
     * 
     * @param player
     * @return le gestionnaire du joueur
     */
    public PlayerManager getPlayerManager(Player player){
        return this.playersMap.get(player);
    }

    /**
     * 
     * @return le boutton de gestion des bouttons
     */
    private AnchorPane createGestionButton(){
        AnchorPane gameGestionButton = new AnchorPane();

        return gameGestionButton;
    }

    /**
     * gère les animations et placement du joueur dans la page de jeux
     */
    public class PlayerManager{
        private Player player;

        private Timeline timeline;
        
        private ImageView view;

        private double playersMaxLife;

        private AnchorPane lifebar;
        private AnchorPane toReduce;

        private Config.PlayerAction currentAction;
        
        private Player.Position.Direction currentDirection;

        private static final int MAX_MS_PER_ACTION = 700;
        private static final int LIFEBAR_WIDTH = 180;

        public PlayerManager(Player player,double playersMaxLife){
            this.player = player;
            this.playersMaxLife = playersMaxLife;
            this.lifebar = this.createLifeBar();
            this.timeline = new Timeline();
            this.view = new ImageView();
            this.currentAction = null;
            this.currentDirection = this.player.getPosition().getCurrentDirection();
            this.view.setFitWidth(this.player.getWidth() );
            this.view.setFitHeight(this.player.getHeight() );
        }

        /**
         * crée une barre de vie
         * @return la barre de vie
         */
        private AnchorPane createLifeBar() {
            AnchorPane lifeBar = new AnchorPane();
            AnchorPane redBar = new AnchorPane();
            this.toReduce = new AnchorPane();

            redBar.setBackground(Background.fill(Color.RED) );
            this.toReduce.setBackground(Background.fill(Color.GREEN) );

            // mise à jour des dimensions des anchor pane
            for(AnchorPane p : new AnchorPane[]{lifeBar,redBar,this.toReduce} )
                p.setPrefSize(PlayerManager.LIFEBAR_WIDTH, 20);

            lifeBar.getChildren().addAll(redBar,this.toReduce);

            return lifeBar;
        }

        /**
         * met à jour la barre de vie du joueur
         * @return this
         */
        public PlayerManager updateLifebar() {
            // récupération du taux de réduction et modification de la taille de la bare
            this.toReduce.setMaxWidth((PlayerManager.LIFEBAR_WIDTH / 100.0) * ((100.0 / this.playersMaxLife) * this.player.getCurrentLife() ) );

            return this;
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
            this.timeline.setOnFinished(null);
            this.timeline.stop();

            // on joue l'animation de l'action, l'animation durera maxMsForAction et le temps sera partagé entre le nombre d'images
            this.timeline = new Timeline(new KeyFrame(Duration.millis((action == Config.PlayerAction.JUMP || action == Config.PlayerAction.FALL ? PlayerManager.MAX_MS_PER_ACTION / 2 : PlayerManager.MAX_MS_PER_ACTION) / sequenceSize),(e) -> {
                try
                {   
                    if(action == Config.PlayerAction.JUMP || action == Config.PlayerAction.FALL){
                        double val  = GameSession.JUMP_HEIGHT / sequenceSize;

                        Player.Position position = this.player.getPosition();

                        switch(action){
                            case JUMP:
                                // modification du Y du joueur
                                double newAddY = position
                                    .setCurrentY(position.getCurrentY() - val)
                                    .getCurrentY();

                                this.view.setTranslateY(newAddY);
                            ; break;

                            case FALL:
                                // modification du Y du joueur
                                double newRemoveY = position
                                    .setCurrentY(position.getCurrentY() + val)
                                    .getCurrentY();

                                this.view.setTranslateY(newRemoveY);
                            ; break;

                            default:;
                        }
                    }

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
    
        public AnchorPane getLifebar() {
            return this.lifebar;
        }
    
    }   
}
