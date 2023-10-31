package yahaya_rachelle.scene.popup;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.scene.scene.GameScene;
import yahaya_rachelle.utils.GameContainerCallback;
import yahaya_rachelle.actor.Character;

/**
 * lanceur de jeux ia choix du personnage
 */
public class AiGameStarter extends GameStarter{

    public AiGameStarter(GameScene linkedScene, GameContainerCallback toDoOnConfirm) {
        super(linkedScene, toDoOnConfirm);
    }
    
    @Override
    public Parent getPopup(){
        // suppression des élements graphiques non utilisés
        ScrollPane popup = (ScrollPane) super.getPopup();

        ObservableList<Node> children = ((VBox) popup.getContent()).getChildren();

        children.remove(2);

        return popup;
    }

    @Override
    protected void addNewCharacter(VBox container,ObservableList<Node> children,Character character,TextField pseudoChooser){
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

            ChoosedData data = new ChoosedData(this.parent,character,choosedPseudo,null);

            this.toDoOnConfirm.action(data,false);
        });

        characterContainer.getChildren().addAll(imageViewer,nameLabel); 

        children.add(characterContainer);
    }
}
