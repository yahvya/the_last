package yahaya_rachelle.scene.popup;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

import org.json.simple.JSONObject;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable.ConfigGetter;
import yahaya_rachelle.data.GameDataManager;
import yahaya_rachelle.scene.scene.GameScene;
import yahaya_rachelle.utils.GameContainerCallback;

public class CreatePlayer extends ScenePopup{

    private HashMap<Config.PlayerAction,ArrayList<Image> > actionsSequences;

    private TextField playerNameContainer;

    private double width;
    private double height;

    private ReentrantLock locker;

    public CreatePlayer(GameScene linkedScene,GameContainerCallback toDoOnConfirm) {
        super(linkedScene,toDoOnConfirm);
    }

    @Override
    protected Parent buildPopup() {
        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this.linkedScene.getGame() );

        this.actionsSequences = new HashMap<Config.PlayerAction,ArrayList<Image> >();
        this.width = configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue();
        this.height = configLongGetter.getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue();
        this.locker = new ReentrantLock();
        
        GameDataManager manager = this.linkedScene.getGameDataManager();

        VBox container = new VBox(20);

        container.setPadding(new Insets(10,20,10,20) );
        container.setBackground(new Background(new BackgroundImage(manager.getItems().getImage(Config.Items.PARCHMENT_TEXTURE.key),BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT,BackgroundPosition.DEFAULT,BackgroundSize.DEFAULT) ) );

        ObservableList<Node> children = container.getChildren();

        ScrollPane scrollableZone = new ScrollPane(container);

        scrollableZone.setVbarPolicy(ScrollBarPolicy.NEVER);
        scrollableZone.setMaxHeight(this.height - 30);

        Font normalFont = manager.getFonts().getFont(Config.Fonts.BASIC.key,14);

        Label title = new Label("Creer un personnage");
        Label message = new Label("Veuillez entrez les données du joueur, la puissance sera généré");

        title.setFont(manager.getFonts().getFont(Config.Fonts.SPECIAL.key,25) );
        title.setWrapText(true);
        message.setFont(Font.font(null,FontWeight.NORMAL,FontPosture.ITALIC, 13) );

        this.playerNameContainer = new TextField();

        double width = 300 > this.width / 2 ? this.width / 2 : 300;

        this.playerNameContainer.setPromptText("Entrez le nom du joueur");
        this.playerNameContainer.setMaxWidth(width);
        this.playerNameContainer.setMinWidth(width);

        children.addAll(title,message,this.playerNameContainer);

        this.addAttackZone(normalFont,children,manager);
        this.addSuperAttackZone(normalFont,children,manager);
        this.addRunZone(normalFont,children,manager);
        this.addDeathZone(normalFont,children,manager);
        this.addFallZone(normalFont,children,manager);
        this.addJumpZone(normalFont,children,manager);
        this.addStaticZone(normalFont,children,manager);
        this.addTakeHitZone(normalFont,children,manager);
        this.addConfirmation(normalFont,children,message,scrollableZone);

        return scrollableZone;
    }

    /**
     * crée une ligne d'ajout
     * le titre prend 15% de la taille
     * la zone d'affichage prend 55%
     * la zone de preview et le boutton d'ajout prend 25%
     * @return le conteneur final
     */
    public HBox createZone(Font font,String zoneTitle,ArrayList<Image> imageList,GameDataManager manager){
        HBox container = new HBox(10);

        ObservableList<Node> children = container.getChildren();

        // création du titre de la ligne
        Label zoneTitleLabel = new Label(zoneTitle);

        zoneTitleLabel.setFont(manager.getFonts().getFont(Config.Fonts.BASIC.key,14) );
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

        children.add(scrollableZone);

        // ajout de la zone d'ajout et de preview

        HBox apZone = new HBox(5);

        Button addButton = this.getCustomButton("Ajout",font);

        size = (25.0 / 100.0) * this.width;

        StackPane previewContainer = new StackPane();

        final double imageWidth = size / 2;

        // afficheur de l'image
        ImageView imageView = new ImageView();

        imageView.setFitWidth(imageWidth);
        imageView.setFitHeight(90);
        
        previewContainer.getChildren().add(imageView);

        previewContainer.setMinSize(imageWidth,130);
        previewContainer.setMaxSize(imageWidth,130);
        
        apZone.getChildren().addAll(previewContainer,addButton);
        apZone.setAlignment(Pos.CENTER_LEFT);
        apZone.setMinWidth(size);
        apZone.setMaxWidth(size);

        children.add(apZone);

        container.setMinHeight(90);
        container.setAlignment(Pos.CENTER_LEFT);

        ArrayList<Image> previewImageList = new ArrayList<Image>();

        Timeline previewTimeline = new Timeline(new KeyFrame(Duration.millis(100),(e) -> {
            // on bloque l'accès à la liste d'images
            this.locker.lock();

            try
            {   
                Image image = previewImageList.remove(0);

                imageView.setImage(image);
                
                previewImageList.add(image);
            }
            catch(IndexOutOfBoundsException boundException){}

            // on remet l'accès
            this.locker.unlock();
        }) );

        previewTimeline.setCycleCount(Animation.INDEFINITE);

        // ajout de l'ajout d'images
        this.addNewImageZone(addButton,imageList,previewImageList,imagesListContainer,Config.PlayerAction.ATTACK,manager,imageWidth,previewTimeline);

        return container;
    }

    /**
     * ajoute la zone d'ajout d'images
     * @param addButton
     * @param imageList
     * @param previewList
     * @param imagesListContainer
     * @param actionType
     * @param manager
     * @param imageWidth
     * @param previewTimeline
     */
    public void addNewImageZone(Button addButton,ArrayList<Image> imageList,ArrayList<Image> previewList,HBox imagesListContainer,Config.PlayerAction actionType,GameDataManager manager,final double imageWidth,Timeline previewTimeline){
        addButton.setOnMouseClicked((e) -> {

            // on lance la timeline de preview à la première image
            if(imageList.size() == 0)
                previewTimeline.play();

            
            // on crée la boxe conteneur de l'image
            VBox addZone = new VBox(10);

            ImageView imageView = new ImageView();

            imageView.setFitWidth(90);
            imageView.setFitHeight(90);

            Button chooser = new Button("Choisir");

            addZone.setAlignment(Pos.CENTER_LEFT);
            addZone.getChildren().addAll(imageView,chooser);
            addZone.setMaxSize(imageWidth,100);
            addZone.setMinSize(imageWidth,100);
            
            imagesListContainer.getChildren().add(addZone);

            FileChooser fileChooser = new FileChooser();

            PreviewHelper helper = new PreviewHelper();

            // évenement d'ajout d'image
            chooser.setOnMouseClicked((clickEvent) -> {
                File choosedFile = fileChooser.showOpenDialog(this.linkedScene.getGame().getWindow() );

                if(choosedFile == null)
                    return;

                Image choosedImage = new Image(choosedFile.getAbsolutePath() );

                this.locker.lock();

                // alors l'image avait déjà été ajouté à la liste, on modifie
                if(!helper.imageIsSet() )
                {
                    imageList.add(choosedImage);
                    previewList.add(choosedImage);
                }
                else 
                {
                    Image previousImage = helper.getImage();

                    imageList.set(imageList.indexOf(previousImage),choosedImage);
                    previewList.set(previewList.indexOf(previousImage),choosedImage);
                }

                helper.setImage(choosedImage);

                imageView.setImage(choosedImage);

                this.locker.unlock();
            });
        });
    }

    /**
     * ajout de la zone d'ajout des images d'attaque
     * @param font
     * @param children
     */
    public void addAttackZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image des attaques pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(Config.PlayerAction.ATTACK,imageList);

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
        this.actionsSequences.put(Config.PlayerAction.SUPER_ATTACK,imageList);

        children.add(this.createZone(font,"Super attaque",imageList,manager) );
    }

    /**
     * ajout de la zone d'ajout des images de course
     * @param font
     * @param children
     */
    public void addRunZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image de course pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(Config.PlayerAction.RUN,imageList);

        children.add(this.createZone(font,"Course",imageList,manager) );
    }

    /**
     * ajout de la zone d'ajout des images de mort
     * @param font
     * @param children
     */
    public void addDeathZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image de la mort pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(Config.PlayerAction.DEATH,imageList);

        children.add(this.createZone(font,"Mort",imageList,manager) );
    }

    /**
     * ajout de la zone d'ajout des images de saut
     * @param font
     * @param children
     */
    public void addJumpZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image des sauts pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(Config.PlayerAction.JUMP,imageList);

        children.add(this.createZone(font,"Saut",imageList,manager) );
    }

    /**
     * ajout de la zone d'ajout des images de descende de saut
     * @param font
     * @param children
     */
    public void addFallZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image de descente pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(Config.PlayerAction.FALL,imageList);

        children.add(this.createZone(font,"Descente",imageList,manager) );
    }

    /**
     * ajout de la zone d'ajout des images immobile
     * @param font
     * @param children
     */
    public void addStaticZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image statiques pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(Config.PlayerAction.STATIC_POSITION,imageList);

        children.add(this.createZone(font,"Statique",imageList,manager) );
    }   

    /**
     * ajout de la zone d'ajout des images de réception de dégâts
     * @param font
     * @param children
     */
    public void addTakeHitZone(Font font,ObservableList<Node> children,GameDataManager manager){
        // création de la liste d'image des dégats pour la preview
        ArrayList<Image> imageList = new ArrayList<Image>();
        this.actionsSequences.put(Config.PlayerAction.TAKE_HIT,imageList);

        children.add(this.createZone(font,"Degats",imageList,manager) );
    }

    /**
     * ajoute et gère la confirmation de création
     * @param font
     * @param children
     * @param message
     * @param manager
     */
    public void addConfirmation(Font font,ObservableList<Node> children,Label message,ScrollPane scrollableZone){
        Button confirmationButton = this.getCustomButton("Ajouter mon personnage",font);

        confirmationButton.setOnMouseClicked((e) -> {
            this.tryToConfirmCreation(message,scrollableZone);
        });

        children.add(confirmationButton);
    }

    /**
     * essaie de créer le personnage
     */
    public void tryToConfirmCreation(Label messageDisplayer,ScrollPane scrollableZone){
        
        boolean isOk = true;
        final int minimumStep = 2;

        String playerName = this.playerNameContainer.getText();

        if(playerName.length() < 2)
        {       
            messageDisplayer.setText("Le nom du joueur doit avoir au moins deux caractères");

            scrollableZone.setVvalue(0);

            return;
        }

        // on vérifie que chaque action à au moins une image associé
        for(Map.Entry<Config.PlayerAction,ArrayList<Image> > entry : this.actionsSequences.entrySet() )
        {
            if(entry.getValue().size() < minimumStep)
            {
                isOk = false;

                break;
            }
        }   

        if(isOk)
        {
            try
            {
                ConfigGetter<String> configStringGetter = new ConfigGetter<String>(this.linkedScene.getGame() );

                // récupération du numéro de dossier du nouveau personnage
                File file = new File(this.getClass().getResource(configStringGetter.getValueOf(Config.App.CUSTOM_CHARACTERS_PATH.key) + "index.txt").toURI() );

                Scanner scanner = new Scanner(file);

                String folderId = Integer.toString(scanner.nextInt() + 1);

                scanner.close();

                FileWriter writer = new FileWriter(file);

                writer.write(folderId);

                writer.close();

                //création du dossier de destination
                File folder = new File(URI.create(String.join("",this.getClass().getResource(configStringGetter.getValueOf(Config.App.CUSTOM_CHARACTERS_PATH.key) ).toURI().toString(),folderId,"/") ) );

                HashMap<String,Object> configFileMap = new HashMap<String,Object>();

                folder.mkdirs();

                // remplissage du dossier personnsages par les images de séquences
                configFileMap.put(Config.Character.COUNT_OF_ATTACK_STATE.key,this.createActionFileGroup(this.actionsSequences.get(Config.PlayerAction.ATTACK),folderId,Config.PlayerAction.ATTACK.key) );
                configFileMap.put(Config.Character.COUNT_OF_DEATH_STATE.key,this.createActionFileGroup(this.actionsSequences.get(Config.PlayerAction.DEATH),folderId,Config.PlayerAction.DEATH.key) );
                configFileMap.put(Config.Character.COUNT_OF_FALL_STATE.key,this.createActionFileGroup(this.actionsSequences.get(Config.PlayerAction.FALL),folderId,Config.PlayerAction.FALL.key) );
                configFileMap.put(Config.Character.COUNT_OF_JUMP_STATE.key,this.createActionFileGroup(this.actionsSequences.get(Config.PlayerAction.JUMP),folderId,Config.PlayerAction.JUMP.key) );
                configFileMap.put(Config.Character.COUNT_OF_RUN_STATE.key,this.createActionFileGroup(this.actionsSequences.get(Config.PlayerAction.RUN),folderId,Config.PlayerAction.RUN.key) );
                configFileMap.put(Config.Character.COUNT_OF_STATIC_STATE.key,this.createActionFileGroup(this.actionsSequences.get(Config.PlayerAction.STATIC_POSITION),folderId,Config.PlayerAction.STATIC_POSITION.key) );
                configFileMap.put(Config.Character.COUNT_OF_SUPER_ATTACK_STATE.key,this.createActionFileGroup(this.actionsSequences.get(Config.PlayerAction.SUPER_ATTACK),folderId,Config.PlayerAction.SUPER_ATTACK.key) );
                configFileMap.put(Config.Character.COUNT_OF_TAKE_HIT_STATE.key,this.createActionFileGroup(this.actionsSequences.get(Config.PlayerAction.TAKE_HIT),folderId,Config.PlayerAction.TAKE_HIT.key) );

                configFileMap.put(Config.Character.NAME.key,playerName);
                configFileMap.put(Config.Character.FORCE.key,new Random().nextInt(50,100) );

                // création du fichier de configuration du personnage

                String characterConfigFilePath = String.join("",this.getClass().getResource(new ConfigGetter<String>(this.linkedScene.getGame() ).getValueOf(Config.App.CUSTOM_CHARACTERS_PATH.key) ).getPath(),folderId);

                characterConfigFilePath += "/" + new ConfigGetter<String>(this.linkedScene.getGame() ).getValueOf(Config.App.CHARACTERS_CONFIG_FILENAME.key);

                File characterConfigFile = new File(characterConfigFilePath);

                characterConfigFile.createNewFile();

                writer = new FileWriter(characterConfigFile);

                writer.write(JSONObject.toJSONString(configFileMap) );

                writer.close();

                Alert infoAlert = new Alert(AlertType.INFORMATION);

                infoAlert.setHeaderText("Veuillez relancer le jeux pour utiliser votre personnage");
                infoAlert.show();

                this.toDoOnConfirm.action(this.getPopup(),false);
            }
            catch(Exception e){
                messageDisplayer.setText("Une erreur s'est produite veuillez retenter");
            }
        }
        else
        {
            scrollableZone.setVvalue(0);

            ButtonType continueButton = new ButtonType("Continuer",ButtonData.YES);
            ButtonType quitButton = new ButtonType("Quitter",ButtonData.NO);

            Alert alert = new Alert(AlertType.CONFIRMATION,null,continueButton,quitButton);

            alert.setTitle("Erreur de création");
            alert.setHeaderText("Veuillez entrez toutes les images ou quitter la zone de création");
            
            messageDisplayer.setText("Veuillez entrez toutes les images ou quitter la zone de création");

            Optional<ButtonType> choosedButton = alert.showAndWait();

            if(choosedButton.get() == quitButton)
                this.toDoOnConfirm.action(this.getPopup(),true);
        }
    }

    /**
     * ajoute dans le dossier le groupe d'image selon l'action
     * @param manager
     * @param imageList
     * @param folderId
     * @param actionName
     */
    public int createActionFileGroup(ArrayList<Image> imageList,String folderId,String actionName){
        int id = 1;

        String customPath = String.join("",this.getClass().getResource(new ConfigGetter<String>(this.linkedScene.getGame() ).getValueOf(Config.App.CUSTOM_CHARACTERS_PATH.key) ).getPath(),folderId);

        if(customPath.startsWith("/") || customPath.startsWith("\\") )
            customPath = customPath.substring(1);

        for(Image image : imageList)
        {
            try
            {
                String url = image.getUrl();

                String[] parts = url.split("\\.");

                String extension = parts[parts.length - 1];

                Files.copy(
                    Path.of(url),
                    Path.of(
                        String.join("/",
                            customPath,
                            String.join(".",
                                String.join("_",actionName,Integer.toString(id) ),
                                extension
                            )
                        )
                    ) 
                );

                id++;
            }
            catch(Exception e){}
        }

        return imageList.size();
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
        button.setBackground(Background.fill(Paint.valueOf(color) ) );
        button.setFont(Font.font(font.getFamily(),15) );
        button.setWrapText(true);
        button.setOnMouseExited((e) -> button.setBackground(Background.fill(Paint.valueOf(color) ) ) );
        button.setOnMouseEntered((e) -> button.setBackground(Background.fill(Paint.valueOf(hoverColor) ) ) );

        return button;
    }

    class PreviewHelper
    {
        private Image image = null;

        /**
         * 
         * @return si l'image a été affecté
         */
        public boolean imageIsSet(){
            return this.image != null;
        }

        /**
         * 
         * @return l'image
         */
        public Image getImage(){
            return this.image;
        }

        /**
         * affecte l'image
         * @param image
         */
        public void setImage(Image image){
            this.image = image;
        }
    }
}
