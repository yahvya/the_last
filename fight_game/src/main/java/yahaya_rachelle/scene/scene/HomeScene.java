package yahaya_rachelle.scene.scene;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable.ConfigGetter;
import yahaya_rachelle.game.Game;
import yahaya_rachelle.scene.popup.CreatePlayer;
import yahaya_rachelle.scene.popup.PlayerChooser;
import yahaya_rachelle.scene.popup.PlayerChooser.ChoosedData;

public class HomeScene extends GameScene{

    private boolean someActionIsPerforming;

    private MediaPlayer refusedActionSongPlayer;
    private MediaPlayer gameBackgroundSong;

    public HomeScene(Game game) {
        super(game);

        this.someActionIsPerforming = false;
        this.refusedActionSongPlayer = new MediaPlayer(this.gameDataManager.getAppSongs().getMedia(Config.AppSongs.REFUSED.key) );
        this.refusedActionSongPlayer.setOnEndOfMedia(() -> {
            this.refusedActionSongPlayer.seek(Duration.ZERO);
            this.refusedActionSongPlayer.stop();
        });
    }

    @Override 
    protected Scene buildPage() {

        AnchorPane container = new AnchorPane();

        this.addBackgroundImage(container);
        this.addMenu(container);
        this.addBackgroundSong(container);

        return new Scene(container);
    }

    /**
     * ajoute l'image de fond
     * @param list
     */
    public void addBackgroundImage(AnchorPane container)
    {
        container.setBackground(new Background(new BackgroundImage(this.gameDataManager.getItems().getImage(Config.Items.HOME_BACKGROUND.key),BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100,100,true,false,false,true) ) ) );
    }

    /**
     * ajoute le menu
     */
    public void addMenu(AnchorPane container)
    {
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
        Label loadGame = new Label("Charger une partie");
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
        this.eventOnCreateNewPlayer(createPlayer,container);
        this.eventOnLoadGame(loadGame,container);
        this.eventOnStartGame(startGame,container);

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
    public void addBackgroundSong(AnchorPane container)
    {
        final double defaultVolumePercent = 30;

        double window_width = new ConfigGetter<Long>(this.game).getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue();

        Slider gameBackgroundSongVolumeController = new Slider(0,100,defaultVolumePercent);

        gameBackgroundSongVolumeController.setPrefSize(window_width / 4,30);
        gameBackgroundSongVolumeController.setTranslateX(((window_width / 2) + (window_width / 4) ) - 20);
        gameBackgroundSongVolumeController.setTranslateY(10);

        container.getChildren().add(gameBackgroundSongVolumeController);

        this.gameBackgroundSong = new MediaPlayer(this.gameDataManager.getAppSongs().getMedia(Config.AppSongs.HOME.key) );
        this.gameBackgroundSong.setOnEndOfMedia(() -> this.gameBackgroundSong.seek(Duration.ZERO) );
        this.gameBackgroundSong.setVolume(defaultVolumePercent / 100);
        this.gameBackgroundSong.play();

        gameBackgroundSongVolumeController.valueProperty().addListener((e) -> this.gameBackgroundSong.setVolume(gameBackgroundSongVolumeController.getValue() / 100) );
    }

    /**
     * place l'évenement pour la création d'un joueur
     * @param createPlayer
     */
    public void eventOnCreateNewPlayer(Label createPlayer,AnchorPane container){
        createPlayer.setOnMouseClicked((e) ->  {
            if(this.canDoAction() )
            {
                this.someActionIsPerforming = true;

                ObservableList<Node> children = container.getChildren();

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
    public void eventOnLoadGame(Label loadGame,AnchorPane container){
        loadGame.setOnMouseClicked((e) -> {
            if(this.canDoAction() )
            {
                this.someActionIsPerforming = true;

                System.out.println("on veut charger une partie");
            }
        });
    }

    /**
     * 
     * @param startGame
     */
    public void eventOnStartGame(Label startGame,AnchorPane container){
        startGame.setOnMouseClicked((e) -> {
            if(this.canDoAction() )
            {
                this.someActionIsPerforming = true;

                ObservableList<Node> children = container.getChildren();

                VBox chooser = (VBox) new PlayerChooser(this,(result,isCanceled) -> {
                    ChoosedData choiceResult = (ChoosedData) result;

                    children.remove(choiceResult.getContainer() );
                    this.someActionIsPerforming = false;

                    // si l'action non annulé alors on démarre une nouvelle partie
                    if(!isCanceled)
                    {
                        System.out.println(choiceResult.getChoosedCharacter() );
                    }
                }).getPopup();

                children.add(chooser);
            }
        });
    }

    /**
     * vérifie si une action peut être fait ou joue le son de refus
     * @return si une action peut être fait
     */
    private boolean canDoAction()
    {
        if(this.someActionIsPerforming)
        {
            this.refusedActionSongPlayer.play();

            return false;
        }

        return true;
    }
}
