package yahaya_rachelle.game.scene;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
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
import yahaya_rachelle.game.exception.KeyNotExist;
import yahaya_rachelle.game.game.Game;
import yahaya_rachelle.game.game.GameLoader.Key;

public class HomeScene extends GameScene{

    private boolean someActionIsPerforming;

    private MediaPlayer refusedActionSongPlayer;
    private MediaPlayer gameBackgroundSong;

    public HomeScene(Game game) {
        super(game);

        this.someActionIsPerforming = false;
        this.refusedActionSongPlayer = new MediaPlayer(this.gameDataManager.getSongsMap().get(Key.SONG_REFUSED_ACTION) );
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
        try
        {
            container.setBackground(new Background(new BackgroundImage(new Image(this.gameDataManager.getResource(Key.PATH_APP_SCENES, "6.gif").toString() ),BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100,100,true,false,false,true) ) ) );
        } 
        catch(KeyNotExist e) {}
    }

    /**
     * ajoute le menu
     */
    public void addMenu(AnchorPane container)
    {
        VBox menu = new VBox(20);

        final int width = 280;
        final int height = 350;
        

        menu.setMinWidth(width);
        menu.setMaxWidth(width);
        menu.setMinHeight(height);
        menu.setMaxHeight(height);
        menu.setTranslateX(Game.GAME_WINDOW_WIDTH / 2 - (width / 2) );
        menu.setTranslateY(Game.GAME_WINDOW_HEIGHT / 2 - (height / 2) );
        menu.setPadding(new Insets(35,20,20,35));
        // ajout du parchemin comme image de fond
        menu.setBackground(new Background(new BackgroundImage(this.gameDataManager.getItemsMap().get(Key.ITEM_PARCHMENT), BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100, 100, true, true, true,false)) ) );

        // création des textes actions
        Label createPlayer = new Label("Creer un personnage");
        Label loadGame = new Label("Charger une partie");
        Label startGame = new Label("Lancer une partie");

        Font font = this.gameDataManager.getFontsMap().get(Key.FONT_NORMAL);

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

        Label gameName = new Label(Game.GAME_NAME);

        gameName.setFont(this.gameDataManager.getFontsMap().get(Key.FONT_SPECIAL) );
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

        Slider gameBackgroundSongVolumeController = new Slider(0,100,defaultVolumePercent);

        gameBackgroundSongVolumeController.setPrefSize(Game.GAME_WINDOW_WIDTH / 4,30);
        gameBackgroundSongVolumeController.setTranslateX(((Game.GAME_WINDOW_WIDTH / 2) + (Game.GAME_WINDOW_WIDTH / 4) ) - 20);
        gameBackgroundSongVolumeController.setTranslateY(10);

        container.getChildren().add(gameBackgroundSongVolumeController);

        this.gameBackgroundSong = new MediaPlayer(this.gameDataManager.getSongsMap().get(Key.SONG_APP_HOME) );
        this.gameBackgroundSong.setOnEndOfMedia(() -> this.gameBackgroundSong.seek(Duration.ZERO) );
        this.gameBackgroundSong.setVolume(defaultVolumePercent / 100);
        this.gameBackgroundSong.play();

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

                System.out.println("on veut crée un joueur");
            }
                
        });
    }

    /**
     * place l'évenement pour le chargement d'une partie
     * @param loadGame
     */
    public void eventOnLoadGame(Label loadGame){
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
    public void eventOnStartGame(Label startGame){
        startGame.setOnMouseClicked((e) -> {
            if(this.canDoAction() )
            {
                this.someActionIsPerforming = true;

                System.out.println("on veut commencer une partie");
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
