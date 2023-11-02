package yahaya_rachelle.scene.scene;

import javafx.animation.Animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;
import yahaya_rachelle.actor.Player;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable.ConfigGetter;
import yahaya_rachelle.data.GameDataManager;
import yahaya_rachelle.data.Scenes;
import yahaya_rachelle.game.GameSession;
import yahaya_rachelle.utils.GameCallback;
import yahaya_rachelle.utils.GameContainerCallback;

/**
 * représente la page d'affichage d'une partie
 */
public class GameSessionScene extends GameScene{
    public static final int STATUS_SHOW_TIME = 3000;

    protected GameSession gameSession;

    protected ObservableList<Node> children;

    protected HashMap<Player,PlayerManager> playersMap;

    protected double playersMaxLife;

    protected ObservableList<Node> lifebarsList;

    protected Scenes.Scene backgroundScene;

    public GameSessionScene(GameSession gameSession) {
        super(gameSession.getLinkedGame() );

        this.gameSession = gameSession;
    }

    @Override
    protected Scene buildPage() {
        this.playersMap = new HashMap<Player,PlayerManager>();
        this.playersMaxLife = new ConfigGetter<Long>(this.game).getValueOf(Config.App.PLAYERS_LIFE.key).doubleValue();

        GameDataManager manager = this.getGameDataManager();

        this.backgroundScene = manager.getScenes().getRandomScene();

        AnchorPane container = new AnchorPane();

        HBox lifeContainer = new HBox(30);

        lifeContainer.setTranslateY(30);
        lifeContainer.setPadding(new Insets(0,20,0,20) );
        
        container.setBackground(new Background(new BackgroundImage(this.backgroundScene.getSceneImage(),BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100,100,true,true,false, true)) ) );

        this.children = container.getChildren();
        this.lifebarsList = lifeContainer.getChildren();
        this.children.add(lifeContainer);

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
        PlayerManager manager = new PlayerManager(player,this.playersMaxLife,this);

        this.playersMap.put(player,manager);
        this.children.add(manager.getView() );
        this.lifebarsList.add(manager.getLifebarContainer() );
               
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
            this.lifebarsList.remove(manager.getLifebarContainer() );
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
     * affiche un message sur l'écran durant un certains temps et appelle le callback après
     * @param message
     * @param showTimeInMs
     * @param toDoAfterShowTime
     * @return this
     */
    public GameSessionScene showWinStatusMessage(String message,int showTimeInMs,GameCallback toDoAfterShowTime){
        Label text = new Label(message);

        Font font = this.gameDataManager.getFonts().getFont(Config.Fonts.BASIC.key,40);

        text.setFont(font);
        text.setTextFill(Paint.valueOf(new ConfigGetter<String>(this.backgroundScene).getValueOf("upp-color") ) );

        final double charWidth = 20; 
        final double labelHeight = 15; 
        double labelWidth = charWidth * message.length();

        ConfigGetter<Long> longConfigGetter = new ConfigGetter<Long>(this.game);

        double width = longConfigGetter.getValueOf(Config.App.WINDOW_WIDTH.key);
        double height = longConfigGetter.getValueOf(Config.App.WINDOW_HEIGHT.key);

        children.add(text);

        text.setTranslateX((width - labelWidth) / 2);
        text.setTranslateY((height - labelHeight) / 2);
        

        // création de l'animation
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50),(e) -> text.setOpacity(text.getOpacity() - 0.2) ) );

        timeline.setCycleCount(4);
        timeline.setDelay(Duration.millis(showTimeInMs) );
        timeline.play();

        if(toDoAfterShowTime != null){
            timeline.setOnFinished((e) -> {
                children.remove(text);
                Platform.runLater(() -> toDoAfterShowTime.action() );   
            });
        }
        else timeline.setOnFinished((e) -> children.remove(text) );

        return this;
    }

    /**
     * demande le nom de la partie à sauvargdé
     * @param toCallOnChoose action à exécuté une fois le nom récupéré
     */
    public void askSaveName(GameContainerCallback toCallOnChoose){
        TextInputDialog textInput = new TextInputDialog();

        textInput.setContentText("Veuillez entrer un nom");
        
        try {
            toCallOnChoose.action(textInput.showAndWait().get(),true);
        } catch(Exception e) {}
    }   

    /**
     * propose la sauvegarde de la partie et appel les méthode de la GameSession lié en fonction de l'annulation ou non de l'action
     */
    public void initSaveDialog(){
        try{
            Alert choiceAlert = new Alert(AlertType.CONFIRMATION);

            choiceAlert.setContentText("Voulez vous vraiment sauvegarder la partie ?");

            // si la personne confirme alors on sauvegarde la partie
            if(choiceAlert.showAndWait().get() == ButtonType.OK)
                this.askSaveName((choosedName,unused) -> this.gameSession.saveGame((String) choosedName) );
            else    
                this.gameSession.cancelSave();
        }
        catch(Exception e){
            this.gameSession.cancelSave();
        }
    }

    /**
     * gère les animations et placement du joueur dans la page de jeux
     */
    public class PlayerManager{

        protected static final int MAX_MS_PER_ACTION = 700;
        protected static final int LIFEBAR_WIDTH = 120;

        protected Player player;

        protected Timeline currentTimeline;
        
        protected ImageView view;

        protected double playersMaxLife;

        protected VBox lifebarContainer;
        protected AnchorPane toReduce;

        protected Config.PlayerAction currentAction;
        
        protected Player.Position.Direction currentDirection;

        protected GameSessionScene linkedScene;

        public PlayerManager(Player player,double playersMaxLife,GameSessionScene linkedScene){
            this.player = player;
            this.playersMaxLife = playersMaxLife;
            this.linkedScene = linkedScene;
            this.lifebarContainer = this.createLifeBar();
            this.view = new ImageView();
            this.currentAction = null;
            this.currentTimeline = null;
            this.currentDirection = this.player.getPosition().getCurrentDirection();
            this.view.setFitWidth(this.player.getWidth() );
            this.view.setFitHeight(this.player.getHeight() );
        }

        /**
         * crée une barre de vie
         * @return la barre de vie
         */
        protected VBox createLifeBar() {
            AnchorPane lifeBar = new AnchorPane();
            AnchorPane redBar = new AnchorPane();
            this.toReduce = new AnchorPane();

            redBar.setBackground(Background.fill(Color.RED) );
            this.toReduce.setBackground(Background.fill(Color.GREEN) );

            // mise à jour des dimensions des anchor pane
            for(AnchorPane p : new AnchorPane[]{lifeBar,redBar,this.toReduce} )
                p.setPrefSize(PlayerManager.LIFEBAR_WIDTH,25);

            lifeBar.getChildren().addAll(redBar,this.toReduce);

            VBox container = new VBox(10);

            Label playerPseudo = new Label(this.player.getPseudo() );

            playerPseudo.setFont(this.linkedScene.gameDataManager.getFonts().getFont(Config.Fonts.BASIC.key,14) );
            playerPseudo.setTextFill(Paint.valueOf(new ConfigGetter<String>(this.linkedScene.backgroundScene).getValueOf("upp-color") ) );

            container.getChildren().addAll(lifeBar,playerPseudo);

            return container;
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
        synchronized public void newAction(Config.PlayerAction action,GameCallback toDoOnEnd){
            // actions pouvant être stoppé si elles sont en cours d'éxécution
            final List<Config.PlayerAction> stoppableActions = Arrays.asList(
                Config.PlayerAction.RUN,
                Config.PlayerAction.STATIC_POSITION
            );

            // actions dont on doit attendre la fin ou provoquer la fin
            final List<Config.PlayerAction> waitEndAction = Arrays.asList(
                Config.PlayerAction.TAKE_HIT,
                Config.PlayerAction.ATTACK,
                Config.PlayerAction.SUPER_ATTACK
            );

            // actions qui si elles sont en cours ignore le reste
            final List<Config.PlayerAction> ignoreActions = Arrays.asList(
                Config.PlayerAction.JUMP,
                Config.PlayerAction.DEATH,
                Config.PlayerAction.FALL
            );

            // cas spécial courir
            if(action == Config.PlayerAction.RUN && this.currentAction == action) return;
            // vérification du cas ignorer
            if(this.currentTimeline != null && this.currentAction != null && ignoreActions.contains(this.currentAction) && action != Config.PlayerAction.FALL ) return;
            // vérification du cas stoppable
            if(stoppableActions.contains(action) && this.currentAction != null && this.currentAction == action && this.currentTimeline != null) this.currentTimeline.stop();

            // construction de la timeline de l'action

            // copie de la séquence d'images décrivant l'action
            ArrayList<Image> sequence = new ArrayList<Image>(this.player.getCharacter().getActionSequence(action) );

            int sequenceSize = sequence.size();

            Timeline newTimeline = new Timeline(
                new KeyFrame(Duration.millis((action == Config.PlayerAction.JUMP || action == Config.PlayerAction.FALL ? PlayerManager.MAX_MS_PER_ACTION / 2. : PlayerManager.MAX_MS_PER_ACTION) / sequenceSize),
                (e) -> {
                    try
                    {
                        if(action == Config.PlayerAction.JUMP || action == Config.PlayerAction.FALL){
                            double val = (double) GameSession.JUMP_HEIGHT / sequenceSize;

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
                })
            );

            newTimeline.setOnFinished((e) -> {
                this.currentTimeline = null;
                // cas spécial mouvement composé
                if(action != Config.PlayerAction.JUMP) this.currentAction = null;
                if(toDoOnEnd != null) toDoOnEnd.action();
            } );

            if(action != Config.PlayerAction.STATIC_POSITION)
                newTimeline.setCycleCount(sequenceSize);
            else
                newTimeline.setCycleCount(Animation.INDEFINITE);

            // si première timeline alors on lance
            if(this.currentTimeline == null){
                this.currentAction = action;
                this.currentTimeline = newTimeline;
                this.currentTimeline.play();
            }
            else{
                if(waitEndAction.contains(this.currentAction) ){
                    newTimeline.stop();
                    this.currentTimeline.jumpTo("endAnimation");

                    if(Player.playerHitActions.contains(this.currentAction) && Player.playerHitActions.contains(action) ){
                        if(toDoOnEnd != null) Platform.runLater(() -> toDoOnEnd.action() );
                    }
                    else if(!Player.playerHitActions.contains(action) || !this.player.isDead() )
                        Platform.runLater(() -> this.newAction(action, toDoOnEnd) );
                }
                else{
                    // on écrase l'action actuelle
                    this.currentTimeline.stop();
                    this.currentTimeline = newTimeline;
                    this.currentAction = action;
                    this.currentTimeline.play();
                }
            }
        }

        public ImageView getView(){
            return this.view;
        }
    
        public VBox getLifebarContainer() {
            return this.lifebarContainer;
        }
    }   
}
