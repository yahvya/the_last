package yahaya_rachelle;

import javafx.application.Application;
import javafx.stage.Stage;
import yahaya_rachelle.game.Game;

public class Launcher extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        new Game(primaryStage);   
    }
    
}
