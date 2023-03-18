package yahaya_rachelle.scene.scene;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable.ConfigGetter;
import yahaya_rachelle.game.Game;
import yahaya_rachelle.game.GameDataToSave;
import yahaya_rachelle.game.GameSession;
import yahaya_rachelle.scene.popup.CreatePlayer;
import yahaya_rachelle.scene.popup.GameStarter;
import yahaya_rachelle.scene.popup.SavedGameStarter;
import yahaya_rachelle.scene.popup.SavedGamesPopup;
import yahaya_rachelle.scene.popup.GameStarter.Action;
import yahaya_rachelle.scene.popup.GameStarter.ChoosedData;
import yahaya_rachelle.scene.popup.SavedGameStarter.SavedGameStarterPopupResult;
import yahaya_rachelle.scene.popup.SavedGamesPopup.SavedGamePopupResult;
import yahaya_rachelle.utils.GameCallback;

/**
 * représente la page d'accueil du jeux
 */
public class HomeScene extends GameScene{

    private boolean someActionIsPerforming;

    private MediaPlayer refusedActionSongPlayer;
    private MediaPlayer gameBackgroundSong;

    private AnchorPane container;

    public HomeScene(Game game) {
        super(game);

        this.someActionIsPerforming = false;
        // this.refusedActionSongPlayer = new MediaPlayer(this.gameDataManager.getAppSongs().getMedia(Config.AppSongs.REFUSED.key) );
        // this.refusedActionSongPlayer.setOnEndOfMedia(() -> {
        //     this.refusedActionSongPlayer.seek(Duration.ZERO);
        //     this.refusedActionSongPlayer.stop();
        // });
    }

    @Override 
    protected Scene buildPage() {

        this.container = new AnchorPane();

        this.addBackgroundImage();
        this.addMenu();
        this.addBackgroundSong();

        return new Scene(container);
    }

    /**
     * ajoute l'image de fond
     * @param list
     */
    public void addBackgroundImage(){
        this.container.setBackground(new Background(new BackgroundImage(this.gameDataManager.getItems().getImage(Config.Items.HOME_BACKGROUND.key),BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100,100,true,false,false,true) ) ) );
    }

    /**
     * ajoute le menu
     */
    public void addMenu(){
        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this.game);

        VBox menu = new VBox(20);

        final int width = 280;
        final int height = 350;
        
        menu.setMinWidth(width);
        menu.setMaxWidth(width);
        menu.setMinHeight(height);
        menu.setMaxHeight(height);
        menu.setTranslateX((configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue() / 2) - (width / 2) );
        menu.setTranslateY((configLongGetter.getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue() / 2) - (height / 2) );
        menu.setPadding(new Insets(35,20,20,35));
        // ajout du parchemin comme image de fond
        menu.setBackground(new Background(new BackgroundImage(this.gameDataManager.getItems().getImage(Config.Items.PARCHMENT.key), BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100, 100, true, true, true,false)) ) );

        // création des textes actions
        Label createPlayer = new Label("Creer un personnage");
        Label loadGame = new Label("Reprendre une partie");
        Label startGame = new Label("Lancer une partie");

        Font font = this.gameDataManager.getFonts().getFont(Config.Fonts.BASIC.key,25);

        // on coupe le texte s'il est trop grand
        createPlayer.setWrapText(true);
        loadGame.setWrapText(true);
        startGame.setWrapText(true);

        // changement de la police
        createPlayer.setFont(font);
        loadGame.setFont(font);
        startGame.setFont(font);

        // ajout des évenements sur les actions
        this.eventOnCreateNewPlayer(createPlayer);
        this.eventOnLoadGame(loadGame);
        this.eventOnStartGame(startGame);

        ObservableList<Node> children = menu.getChildren();
        
        children.addAll(createPlayer,loadGame,startGame);
        // ajout de l'évenement hower pour le changement de couleur
        children.forEach(item -> {
            item.setOnMouseEntered((e) -> {
                Label label = (Label) e.getSource();

                label.setTextFill(Paint.valueOf("brown") );
            });
        }); 

        children.forEach(item -> {
            item.setOnMouseExited((e) -> {
                Label label = (Label) e.getSource();

                label.setTextFill(Paint.valueOf("black") );
            });
        }); 

        Label gameName = new Label(new ConfigGetter<String>(this.game).getValueOf(Config.App.GAME_NAME.key) );

        gameName.setFont(this.gameDataManager.getFonts().getFont(Config.Fonts.SPECIAL.key,25) );
        gameName.setTranslateX(20);
        gameName.setTranslateY(10);
        gameName.setTextFill(Paint.valueOf("brown") );

        container.getChildren().addAll(menu,gameName);
    }

    /**
     * ajoute la musique de fond du jeux
     */
    public void addBackgroundSong()
    {
        final double defaultVolumePercent = 0;

        double window_width = new ConfigGetter<Long>(this.game).getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue();

        Slider gameBackgroundSongVolumeController = new Slider(0,100,defaultVolumePercent);

        gameBackgroundSongVolumeController.setPrefSize(window_width / 4,30);
        gameBackgroundSongVolumeController.setTranslateX(((window_width / 2) + (window_width / 4) ) - 20);
        gameBackgroundSongVolumeController.setTranslateY(10);

        this.container.getChildren().add(gameBackgroundSongVolumeController);

        try{

        this.gameBackgroundSong = new MediaPlayer(this.gameDataManager.getAppSongs().getMedia(Config.AppSongs.HOME.key) );
        this.gameBackgroundSong.setOnEndOfMedia(() -> this.gameBackgroundSong.seek(Duration.ZERO) );
        this.gameBackgroundSong.setVolume(defaultVolumePercent / 100);
        this.gameBackgroundSong.play();
        }
        catch(Exception e){
            System.out.println(e);
        }

        gameBackgroundSongVolumeController.valueProperty().addListener((e) -> this.gameBackgroundSong.setVolume(gameBackgroundSongVolumeController.getValue() / 100) );
    }

    /**
     * place l'évenement pour la création d'un joueur
     * @param createPlayer
     */
    public void eventOnCreateNewPlayer(Label createPlayer){
        createPlayer.setOnMouseClicked((e) ->  {
            if(this.canDoAction() )
            {
                this.someActionIsPerforming = true;

                ObservableList<Node> children = this.container.getChildren();

                // crée la zone de création d'un personnage
                ScrollPane creationBox = (ScrollPane) new CreatePlayer(this,(box,isCanceled) -> {
                    children.remove(box);
                    this.someActionIsPerforming = false;
                }).getPopup();

                children.add(creationBox);
            }
                
        });
    }

    /**
     * place l'évenement pour le chargement d'une partie
     * @param loadGame
     */
    public void eventOnLoadGame(Label loadGame){
        loadGame.setOnMouseClicked((e) -> {
            if(this.canDoAction() ){
                this.someActionIsPerforming = true;

                ObservableList<Node> children = this.container.getChildren();

                // ajout de la popup dans la page
                AnchorPane popup = (AnchorPane) new SavedGamesPopup(this,(result,isCanceled) -> {
                    SavedGamePopupResult popupResult = (SavedGamePopupResult) result;

                    children.remove(popupResult.getPopup() );

                    this.startNewGame(popupResult.getSavedGameData() );
                },this.gameDataManager.getSavedGames() ).getPopup();

                children.add(popup);
            }
        });
    }

    /**
     * 
     * @param startGame
     */
    public void eventOnStartGame(Label startGame){
        startGame.setOnMouseClicked((e) -> {
            if(this.canDoAction() )
            {
                this.someActionIsPerforming = true;

                ObservableList<Node> children = this.container.getChildren();

                ScrollPane chooser = (ScrollPane) new GameStarter(this,(result,isCanceled) -> {
                    ChoosedData choiceResult = (ChoosedData) result;

                    children.remove(choiceResult.getContainer() );

                    // si l'action non annulé alors on démarre une nouvelle partie
                    if(!isCanceled)
                        this.startNewGame(choiceResult);
                    else
                        this.someActionIsPerforming = false;
                }).getPopup();

                children.add(chooser);
            }
        });
    }

    /**
     * lance une nouvelle partie
     */
    public void startNewGame(ChoosedData choiceData){
        try{
            GameSession session;

            GameCallback toDoOnEnd = () -> {
                this.someActionIsPerforming = false;
                this.putSceneInWindow();
            };

            // création de l'objet de gestion d'une partie
            if(!choiceData.getRestart() )   
                session = new GameSession(this.game,choiceData.getChoosedCharacter(),choiceData.getChoosedPseudo(),toDoOnEnd);
            else
                session = new GameSession(this.game,choiceData.getSavedGameData(),toDoOnEnd);

            VBox waitingBox = new VBox(20);
            
            ObservableList<Node> children = waitingBox.getChildren();
            
            Label text = new Label();

            text.setFont(this.gameDataManager.getFonts().getFont(Config.Fonts.BASIC.key,17) );

            // ajout de l'animation de chargement
            Paint color = Paint.valueOf(new ConfigGetter<String>(this.game).getValueOf(Config.App.LOADING_ON_COLOR.key) );

            final int circleRadius = 10;
            final int rotationSpeed = 95;
            final int fixedWidth = 360;
            final int fixedHeight = 160;

            Circle loadingCircle = new Circle();

            loadingCircle.setFill(null);
            loadingCircle.setRadius(circleRadius);
            loadingCircle.setStroke(color);
            loadingCircle.setStrokeWidth(5);
            loadingCircle.getStrokeDashArray().add(10d);
            loadingCircle.setTranslateX((fixedWidth / 2) - (circleRadius * 3) );
            loadingCircle.setTranslateY(circleRadius * -1);

            Timeline loadingAnimationTimeline = new Timeline(new KeyFrame(Duration.millis(rotationSpeed),(e) -> {

                double currentRotation = loadingCircle.getRotate();

                loadingCircle.setRotate(currentRotation + 30);
            }) );

            loadingAnimationTimeline.setCycleCount(Animation.INDEFINITE);
            loadingAnimationTimeline.play();

            ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this.game);

            double width = configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue();
            double height = configLongGetter.getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue();

            waitingBox.setPadding(new Insets(10,15,10,15) );
            waitingBox.setBackground(new Background(new BackgroundImage(this.gameDataManager.getItems().getImage(Config.Items.PARCHMENT_TEXTURE.key),BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100,100,true,true,false,true) ) ) );
            waitingBox.setMaxSize(fixedWidth,fixedHeight);
            waitingBox.setMinSize(fixedWidth,fixedHeight);
            waitingBox.setTranslateX((width - fixedWidth) / 2);
            waitingBox.setTranslateY((height - fixedHeight) / 2);
            
            children.addAll(text,loadingCircle);

            GameCallback removeWaitingZone = () -> {
                Platform.runLater(() -> {
                    loadingAnimationTimeline.stop();
                    container.getChildren().remove(waitingBox);
                });
            };

            GameCallback removeAndFailure = () -> {
                Platform.runLater(() -> {
                    removeWaitingZone.action();
                    this.someActionIsPerforming = false;
                    this.showStartGameFailure();
                }); 
            };

            if(choiceData.getActionToDo() == Action.CREATE){
                // création d'une partie
                int countOfParticipants = choiceData.getCountOfParticipants();

                text.setText("En attente des joueurs");

                String countOfParticipantsStr = Integer.toString(countOfParticipants);

                Label gameCode = new Label("Creation du code de la partie ...");
                gameCode.setFont(this.gameDataManager.getFonts().getFont(Config.Fonts.BASIC.key,12.5) );
                gameCode.setWrapText(true);

                Label newPlayers = new Label(String.join(" sur ","0",countOfParticipantsStr) );
                
                newPlayers.setFont(this.gameDataManager.getFonts().getFont(Config.Fonts.BASIC.key,14) );

                children.add(1,gameCode);
                children.add(2,newPlayers);

                // lancement de la recherche d'adversaires
                session.findOpponents(
                    countOfParticipants,
                    // action quand le code de la partie à été généré
                    (code,noNeed) -> {
                        gameCode.setFont(Font.getDefault() );
                        gameCode.setText(String.join(" : ","Appuyez ici pour copier le code",(String) code) );

                        final Clipboard clipboard = Clipboard.getSystemClipboard();
                        final ClipboardContent content = new ClipboardContent();

                        Scene scene = this.page;

                        gameCode.setOnMouseEntered((e) -> scene.setCursor(Cursor.OPEN_HAND) );
                        gameCode.setOnMouseExited((e) -> scene.setCursor(Cursor.DEFAULT) );
                        gameCode.setOnMouseClicked((e) -> {
                            scene.setCursor(Cursor.CLOSED_HAND);
                            content.putString((String) code);
                            clipboard.setContent(content);
                        });
                    },
                    // action après avoir trouvé tous les participants
                    removeWaitingZone,
                    // action en cas d'échec de recherche
                    removeAndFailure,
                    // action appelé à chaque nouveau joueur trouvé
                    (playerIndex,noNeeed) -> newPlayers.setText(String.join(" sur ",Integer.toString((Integer) playerIndex),countOfParticipantsStr) )
                );
            }
            else{
                // rejoins une partie et attend qu'elle commence
                text.setText("Attente du debut de la partie");
                waitingBox.setMaxHeight(fixedHeight / 2);
                waitingBox.setMinHeight(fixedHeight / 2);

                session.waitGameStart(choiceData.getGameCode(),removeWaitingZone,removeAndFailure);
            }

            this.container.getChildren().add(waitingBox);
        }
        catch(Exception e){
            this.someActionIsPerforming = false;
            this.showStartGameFailure();
        }
    }

    /**
     * lance une partie sauvegardé
     * @param savedGameData
     */
    public void startNewGame(GameDataToSave savedGameData){
        VBox popup = (VBox) new SavedGameStarter(this,(result,isCanceled) -> {
            SavedGameStarterPopupResult popupResult = (SavedGameStarterPopupResult) result;

            this.container.getChildren().remove(popupResult.getPopup() );

            this.startNewGame(popupResult.getChoosedToJoin() ? new ChoosedData(popupResult.getCode(),savedGameData) : new ChoosedData(savedGameData) );
        }).getPopup();  

        this.container.getChildren().add(popup);
    }

    /**
     * affiche l'alerte d'échec de lancement
     */
    public void showStartGameFailure(){
        Alert errorAlert = new Alert(AlertType.ERROR);

        errorAlert.setHeaderText("Echec du lancement de la partie");
        errorAlert.show();
    }

    /**
     * vérifie si une action peut être fait ou joue le son de refus
     * @return si une action peut être fait
     */
    private boolean canDoAction(){
        if(this.someActionIsPerforming)
        {
            this.refusedActionSongPlayer.play();

            return false;
        }

        return true;
    }
}
