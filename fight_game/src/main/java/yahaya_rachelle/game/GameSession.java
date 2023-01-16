package yahaya_rachelle.game;

import yahaya_rachelle.configuration.Configurable;
import yahaya_rachelle.scene.scene.GameSessionScene;
import yahaya_rachelle.utils.GameCallback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

import yahaya_rachelle.actor.Character;

public class GameSession extends Configurable{
    private Game linkedGame;

    private String saveGameFilePath;

    public GameSession(Game linkedGame,Character character,String pseudo){
        this.linkedGame = linkedGame;
    }

    /**
     * permet de lancer une partie à partir d'une sauvegarde
     * @param saveGameFilePath
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParseException
     * @throws FileNotFoundException
     */
    public GameSession(String saveGameFilePath) throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.saveGameFilePath = saveGameFilePath;

        this.setConfig();
    }

    /**
     * cherche un adversaire et lance le jeux
     */
    public void searchOpponent(GameCallback toCallAfterFind){
        // recherche
        toCallAfterFind.action();

        this.startGame();
    }

    /**
     * lance une partie
     */
    private void startGame(){
        GameSessionScene gameScene = new GameSessionScene(this.linkedGame);

        gameScene.putSceneInWindow();
    }   


    @Override
    protected String getConfigFilePath() {
        return this.saveGameFilePath;
    }
}
