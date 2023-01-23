package yahaya_rachelle.scene.popup;

import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import yahaya_rachelle.actor.Character;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable.ConfigGetter;
import yahaya_rachelle.data.GameDataManager;
import yahaya_rachelle.scene.scene.GameScene;
import yahaya_rachelle.utils.GameContainerCallback;

public class GameStarter extends ScenePopup{

    private Action actionToDo;

    private TextField gameCode;

    private Spinner<Integer> countOfParticipants;

    private ScrollPane parent;

    public GameStarter(GameScene linkedScene, GameContainerCallback toDoOnConfirm) {
        super(linkedScene, toDoOnConfirm);
    }

    @Override
    protected Parent buildPopup() {
        GameDataManager manager = this.linkedScene.getGameDataManager();

        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this.linkedScene.getGame() );

        double width = configLongGetter.getValueOf(Config.App.WINDOW_WIDTH.key).doubleValue();
        double height = configLongGetter.getValueOf(Config.App.WINDOW_HEIGHT.key).doubleValue();

        this.actionToDo = Action.CREATE;
        this.gameCode = null;

        // création du conteneur
        VBox container = new VBox(30);

        final double divisionVal = 1.5;

        container.setPrefSize(width / divisionVal,height / divisionVal);
        container.setBackground(new Background(new BackgroundImage(manager.getItems().getImage(Config.Items.PARCHMENT_TEXTURE.key),BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT,BackgroundPosition.DEFAULT,new BackgroundSize(100,100,true,true,true, false) ) ) );
        container.setPadding(new Insets(10,10,10,10) );

        ObservableList<Node> children = container.getChildren();

        Label zoneTitle = new Label("Faites vos choix, vous creez une partie par defaut");

        zoneTitle.setFont(manager.getFonts().getFont(Config.Fonts.BASIC.key,15) );

        TextField pseudoChooser = new TextField();

        pseudoChooser.setPromptText("Choisir un pseudo");
        pseudoChooser.setMaxWidth(width / 4);
        pseudoChooser.setMinWidth(width / 4);

        this.parent = new ScrollPane(container);

        children.addAll(zoneTitle,pseudoChooser,this.createActionChooserZone(manager),this.createCharactersChooserZone(container,pseudoChooser),this.addExitButton(container) );

        this.parent.setTranslateX((width  - (width / divisionVal)) / 2);
        this.parent.setTranslateY((height  - (height / divisionVal)) / 2);
        this.parent.setPrefSize(width / divisionVal,height / divisionVal);
        this.parent.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.parent.setVbarPolicy(ScrollBarPolicy.NEVER);

        return parent;
    }

    /**
     * crée la zone de choix de l'action à faire (rejoindre une partie / créer une partie)
     * @return
     */
    private VBox createActionChooserZone(GameDataManager manager){
        VBox chooseZone = new VBox(20);

        String startDescription = "Voulez vous rejoindre ou creer une partie - Action choisie .";

        Label description = new Label(String.join(" ",startDescription,"Creer une partie") );

        description.setFont(manager.getFonts().getFont(Config.Fonts.BASIC.key, 13.5) );

        ObservableList<Node> children = chooseZone.getChildren();

        final int fixedTextWidth = 250;
        final int fixedTextHeight = 40;

        // création de la zone pour rejoindre une partie

        Button joinGameButton = this.getCustomButton("Rejoindre une partie");

        this.gameCode = new TextField();
        this.gameCode.setPromptText("Entrez le code de la partie");
        this.gameCode.setMaxSize(fixedTextHeight,fixedTextHeight);
        this.gameCode.setMinSize(fixedTextWidth,fixedTextHeight);

        joinGameButton.setMinSize(fixedTextWidth,fixedTextHeight);
        joinGameButton.setMaxSize(fixedTextWidth,fixedTextHeight);

        VBox joinBox = new VBox(20);

        joinBox.getChildren().addAll(joinGameButton,this.gameCode);

        // création de la zone pour créer une partie

        Button createGameButton = this.getCustomButton("Creer une partie");

        this.countOfParticipants = new Spinner<>(1,new ConfigGetter<Long>(manager.getLinkedGame() ).getValueOf(Config.App.GAME_MAX_PARTICIPANTS.key).intValue(),1);
        this.countOfParticipants.setPromptText("Entrez le nombre participants");

        this.countOfParticipants.setMaxSize(fixedTextHeight,fixedTextHeight);
        this.countOfParticipants.setMinSize(fixedTextWidth,fixedTextHeight);

        createGameButton.setMinSize(fixedTextWidth,fixedTextHeight);
        createGameButton.setMaxSize(fixedTextWidth,fixedTextHeight);

        VBox creationBox = new VBox(20);

        creationBox.getChildren().addAll(createGameButton,this.countOfParticipants);

        // gestion de l'action pour rejoindre une partie
        joinGameButton.setOnMouseClicked((e) -> {
            actionToDo = Action.JOIN;

            description.setText(String.join(" ",startDescription,"Rejoindre une partie") );
        });

        // gestion de l'action pour créer une partie
        createGameButton.setOnMouseClicked((e) -> {
            actionToDo = Action.CREATE;

            description.setText(String.join(" ",startDescription,"Creer une partie") );
        });

        HBox zones = new HBox(20);

        zones.getChildren().addAll(creationBox,joinBox);

        children.addAll(description,zones);

        return chooseZone;
    }   
    
    /**
     * 
     * @return la zone scrollable d'affichage des personnages
     */
    private ScrollPane createCharactersChooserZone(VBox parent,TextField pseudoChooser){
        ArrayList<Character> characters = this.linkedScene.getGameDataManager().getCharacters();

        HBox container = new HBox(10);

        ObservableList<Node> children = container.getChildren();

        characters.forEach(character -> this.addNewCharacter(parent,children,character,pseudoChooser) );        

        ScrollPane scrollableZone = new ScrollPane(container);

        scrollableZone.setPadding(new Insets(10,5,10,5) );
        scrollableZone.setMinHeight(130);
        scrollableZone.setVbarPolicy(ScrollBarPolicy.NEVER);

        return scrollableZone;
    }

    /**
     * crée le boutton d'annulation
     * @return
     */
    private Button addExitButton(VBox container){
        Button button = this.getCustomButton("Quitter");

        button.setOnMouseClicked((e) -> this.toDoOnConfirm.action(new ChoosedData(this.parent),true) );

        return button;
    }

    /**
     * ajoute un personnsage dans la zone de choix 
     * @param children
     * @param character
     */
    private void addNewCharacter(VBox container,ObservableList<Node> children,Character character,TextField pseudoChooser){
        VBox characterContainer = new VBox(10);

        Label nameLabel = new Label(character.getName() );

        nameLabel.setFont(this.linkedScene.getGameDataManager().getFonts().getFont(Config.Fonts.BASIC.key,12) );

        ImageView imageViewer = new ImageView(character.getActionSequence(Config.PlayerAction.STATIC_POSITION).get(0) );

        imageViewer.setFitWidth(70);
        imageViewer.setFitHeight(70);

        imageViewer.setOnMouseEntered((e) -> {
            imageViewer.setOpacity(0.7);
        });

        imageViewer.setOnMouseExited((e) -> {
            imageViewer.setOpacity(1);
        });

        // confirmation de choix au click
        imageViewer.setOnMouseClicked((e) -> {
            String choosedPseudo = pseudoChooser.getText();

            if(choosedPseudo.length() < 2 || (this.actionToDo == Action.JOIN && this.gameCode.getText().length() < 1) )
                return;

            ChoosedData data = new ChoosedData(this.parent,character,choosedPseudo,this.actionToDo);

            if(this.actionToDo == Action.JOIN)
                data.setGameCode(this.gameCode.getText() );
            else
                data.setCountOfParticipants(this.countOfParticipants.getValue() );

            this.toDoOnConfirm.action(data,false);
        });

        characterContainer.getChildren().addAll(imageViewer,nameLabel); 

        children.add(characterContainer);
    }

    private Button getCustomButton(String buttonText){
        Button button = new Button(buttonText);

        String color = "#C77F4F";
        String hoverColor = "#98572c";

        // design du boutton
        button.setBackground(Background.fill(Paint.valueOf(color)) );
        button.setFont(this.linkedScene.getGameDataManager().getFonts().getFont(Config.Fonts.BASIC.key,15) );
        button.setWrapText(true);
        button.setOnMouseExited((e) -> button.setBackground(Background.fill(Paint.valueOf(color) ) ) );
        button.setOnMouseEntered((e) -> button.setBackground(Background.fill(Paint.valueOf(hoverColor) ) ) );

        return button;
    }

    public enum Action{JOIN,CREATE};

    public class ChoosedData{
        private ScrollPane container;
        
        private Character choosedCharacter;
        
        private String choosedPseudo;

        private Action actionToDo;

        private String gameCode;

        private int countOfParticipants;

        public ChoosedData(ScrollPane container){
            this.container = container;
        }

        public ChoosedData(ScrollPane container,Character choosedCharacter,String choosedPseudo,Action actionToDo){
            this(container);
            this.choosedCharacter = choosedCharacter;
            this.choosedPseudo = choosedPseudo;
            this.actionToDo = actionToDo;
        }

        public void setGameCode(String gameCode){
            this.gameCode = gameCode;
        }

        public void setCountOfParticipants(int countOfParticipants){
            this.countOfParticipants = countOfParticipants;
        }

        public ScrollPane getContainer(){
            return this.container;
        }

        public Character getChoosedCharacter(){
            return this.choosedCharacter;
        }

        public String getChoosedPseudo(){
            return this.choosedPseudo;
        }

        public Action getActionToDo(){
            return this.actionToDo;
        }

        /**
         * à appeller uniquement si getActionToDo == Action.JOIN
         * @return le code de la partie à rejoindre
         */
        public String getGameCode(){
            return this.gameCode;
        }

        /**
         * à appeller uniquement si getActionToDo == Action.CREATE
         * @return le nombre de personne de la partie à créer
         */
        public int getCountOfParticipants(){
            return this.countOfParticipants;
        }
    }
}