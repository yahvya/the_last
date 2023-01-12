package yahaya_rachelle.game.scene.popup;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import yahaya_rachelle.game.game.Game;
import yahaya_rachelle.game.game.GameDataManager;
import yahaya_rachelle.game.game.GameLoader.Key;
import yahaya_rachelle.game.player.Player.PlayerAction;
import yahaya_rachelle.game.scene.scene.GameScene;
import yahaya_rachelle.game.utils.GameContainerCallback;

public class CreatePlayer extends ScenePopup{

    private HashMap<PlayerAction,ArrayList<Image> > actionsSequences;

    private double width;
    private double height;

    public CreatePlayer(GameScene linkedScene,GameContainerCallback toDoOnConfirm) {
        super(linkedScene,toDoOnConfirm);
    }

    @Override
    protected Pane buildPopup() {
        this.actionsSequences = new HashMap<PlayerAction,ArrayList<Image> >();
        this.width = Game.GAME_WINDOW_WIDTH;
        this.height = Game.GAME_WINDOW_HEIGHT;
        
        GameDataManager manager = this.linkedScene.getGameDataManager();

        VBox container = new VBox(20);

        container.setPadding(new Insets(10,20,10,20) );
        container.setPrefSize(width,height);
        container.setBackground(new Background(new BackgroundImage(manager.getItemsMap().get(Key.ITEM_PARCHMENT_TEXTURE),BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT,BackgroundPosition.DEFAULT,BackgroundSize.DEFAULT) ) );

        ObservableList<Node> children = container.getChildren();

        Font specialNormal = Font.loadFont(manager.getFontsMap().get(Key.FONT_NORMAL),14);

        Label title = new Label("Creer un personnage");
        Label message = new Label("Veuillez entrez les images");

        title.setFont(Font.loadFont(manager.getFontsMap().get(Key.FONT_SPECIAL),25) );
        message.setFont(Font.font(null,FontWeight.NORMAL,FontPosture.ITALIC, 13) );

        children.addAll(title,message);

        this.addAttackZone(specialNormal,children,manager);
        this.addSuperAttackZone(specialNormal,children,manager);
        // this.addRunZone(specialNormal,children,manager);
        // this.addDeathZone(specialNormal,children,manager);
        // this.addFallZone(specialNormal,children,manager);
        // this.addJumpZone(specialNormal,children,manager);
        // this.addStaticZone(specialNormal,children,manager);
        // this.addTakeHitZone(specialNormal,children,manager);
        this.addConfirmation(specialNormal,children,message,manager);

        ScrollPane scrollableZone = new ScrollPane(container);

        scrollableZone.setVbarPolicy(ScrollBarPolicy.NEVER);

        return new VBox(scrollableZone);
    }

    /**
     * crée une ligne d'ajout
     * le titre prend 15% de la taille
     * la zone d'affichage prend 55%
     * la zone de preview et le boutton d'ajout prend 25%
     * @return le conteneur final
     */
    public HBox createZone(Font font,String zoneTitle,ArrayList<Image> imageList,GameDataManager manager){
        HashMap<Key,String> fontsMap = manager.getFontsMap();

        HBox container = new HBox(10);

        ObservableList<Node> children = container.getChildren();

        // création du titre de la ligne
        Label zoneTitleLabel = new Label(zoneTitle);

        zoneTitleLabel.setFont(Font.loadFont(fontsMap.get(Key.FONT_NORMAL),14) );
        zoneTitleLabel.setMaxWidth((15.0 / 100.0) * this.width);
        zoneTitleLabel.setMinWidth((15.0 / 100.0) * this.width);
        zoneTitleLabel.setWrapText(true);

        children.add(zoneTitleLabel);

        // création de la zone d'affichage des images
        HBox imagesListContainer = new HBox(30);

        ScrollPane scrollableZone = new ScrollPane(imagesListContainer);

        double size = (55.0 / 100.0) * this.width;

        scrollableZone.setMinWidth(size);
        scrollableZone.setMaxWidth(size);
        scrollableZone.setPadding(new Insets(5,2,10,5) );
        scrollableZone.setVbarPolicy(ScrollBarPolicy.NEVER);

        // imagesListContainer.getChildren().addAll(new Label("bonjour"),new Label("bonjour"),new Label("bonjour"),new Label("bonjour"),new Label("bonjour"),new Label("bonjour"),new Label("bonjour"),new Label("bonjour"),new Label("bonjour"),new Label("bonjour") );

        children.add(scrollableZone);

        // ajout de la zone d'ajout et de preview

        HBox apZone = new HBox(5);

        Button addButton = this.getCustomButton("Ajout",font);

        size = (25.0 / 100.0) * this.width;

        StackPane previewContainer = new StackPane();

        ObservableList<Node> previewContainerChildren = previewContainer.getChildren();
        
        previewContainerChildren.add(new Label() );

        final double imageWidth = size / 2;

        previewContainer.setMinSize(imageWidth,90);
        previewContainer.setMaxSize(imageWidth,90);
        
        apZone.getChildren().addAll(previewContainer,addButton);
        apZone.setAlignment(Pos.CENTER_LEFT);
        apZone.setMinWidth(size);
        apZone.setMaxWidth(size);

        children.add(apZone);

        container.setMinHeight(90);
        container.setAlignment(Pos.CENTER_LEFT);

        imageList.add(new Image(this.getClass().getResource("/characters/1/death_1.png").toString() ) );
        imageList.add(new Image(this.getClass().getResource("/characters/1/death_2.png").toString() ) );
        imageList.add(new Image(this.getClass().getResource("/characters/1/death_3.png").toString() ) );
        imageList.add(new Image(this.getClass().getResource("/characters/1/death_4.png").toString() ) );

        Timeline previewTimeline = new Timeline(new KeyFrame(Duration.millis(100),(e) -> {
            previewContainerChildren.remove(0);
            
            Image image = imageList.remove(0);

            ImageView imageView = new ImageView(image);

            imageView.setFitWidth(imageWidth);
            imageView.setFitHeight(90);

            previewContainerChildren.add(imageView);
            
            imageList.add(image);
        }) );

        previewTimeline.setCycleCount(Animation.INDEFINITE);
        previewTimeline.play(); 

        return container;
    }

    /**
     * ajout de la zone d'ajout des images d'attaque
     * @param font
     * @param children
     */
    public void addAttackZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image des attaques pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(PlayerAction.ATTACK,imageList);

        children.add(this.createZone(font,"Attaque",imageList,manager) );
    }

    /**
     * ajout de la zone d'ajout des images de super attaque
     * @param font
     * @param children
     */
    public void addSuperAttackZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image des supers attaques pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(PlayerAction.SUPER_ATTACK,imageList);

        children.add(this.createZone(font,"Super attaque",imageList,manager) );
    }

    /**
     * ajout de la zone d'ajout des images de course
     * @param font
     * @param children
     */
    public void addRunZone(Font font,ObservableList<Node> children,GameDataManager manager){

    }

    /**
     * ajout de la zone d'ajout des images de mort
     * @param font
     * @param children
     */
    public void addDeathZone(Font font,ObservableList<Node> children,GameDataManager manager){

    }

    /**
     * ajout de la zone d'ajout des images de saut
     * @param font
     * @param children
     */
    public void addJumpZone(Font font,ObservableList<Node> children,GameDataManager manager){

    }

    /**
     * ajout de la zone d'ajout des images de descende de saut
     * @param font
     * @param children
     */
    public void addFallZone(Font font,ObservableList<Node> children,GameDataManager manager){

    }

    /**
     * ajout de la zone d'ajout des images immobile
     * @param font
     * @param children
     */
    public void addStaticZone(Font font,ObservableList<Node> children,GameDataManager manager){

    }

    /**
     * ajout de la zone d'ajout des images de réception de dégâts
     * @param font
     * @param children
     */
    public void addTakeHitZone(Font font,ObservableList<Node> children,GameDataManager manager){

    }

    /**
     * ajoute et gère la confirmation de création
     * @param font
     * @param children
     * @param message
     * @param manager
     */
    public void addConfirmation(Font font,ObservableList<Node> children,Label message,GameDataManager manager){
        Button confirmationButton = this.getCustomButton("Ajouter mon personnage",font);

        confirmationButton.setOnMouseClicked((e) -> {
            this.tryToConfirmCreation(message);
        });

        children.add(confirmationButton);
    }

    /**
     * essaie de créer le personnage
     */
    public void tryToConfirmCreation(Label messageDisplayer){
        this.toDoOnConfirm.action(this.getPopup(),false);
    }
    
    /**
     * crée un button custom
     * @param title
     * @param font
     * @return le boutton
     */
    private Button getCustomButton(String title,Font font)
    {   
        Button button = new Button(title);

        String color = "#C77F4F";
        String hoverColor = "#98572c";

        // design du boutton
        button.setBackground(new Background(new BackgroundFill(Paint.valueOf(color),CornerRadii.EMPTY,Insets.EMPTY) ) );
        button.setFont(Font.font(font.getFamily(),15) );
        button.setWrapText(true);
        button.setOnMouseExited((e) -> {
            button.setBackground(new Background(new BackgroundFill(Paint.valueOf(color),CornerRadii.EMPTY,Insets.EMPTY) ) );
        });
        button.setOnMouseEntered((e) -> {
            button.setBackground(new Background(new BackgroundFill(Paint.valueOf(hoverColor),CornerRadii.EMPTY,Insets.EMPTY) ) );
        });

        return button;
    }
}
