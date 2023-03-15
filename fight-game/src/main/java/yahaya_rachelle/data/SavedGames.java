package yahaya_rachelle.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Scanner;

import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable.ConfigGetter;
import yahaya_rachelle.game.Game;
import yahaya_rachelle.game.GameDataToSave;

public class SavedGames {
    public static final String SAVED_GAMES_EXTENSION = ".game";

    private HashMap<String,GameDataToSave> savedGames;

    public SavedGames(Game linkedGame) throws FileNotFoundException, URISyntaxException{
        this.savedGames = new HashMap<String,GameDataToSave>();

        this.loadSavedGames(linkedGame);
    }

    /**
     * charge les parties sauvegardés
     * @throws FileNotFoundException
     * @throws URISyntaxException
     */
    private void loadSavedGames(Game linkedGame) throws FileNotFoundException, URISyntaxException{
        this.savedGames = new HashMap<String,GameDataToSave>();

        ConfigGetter<String> configStringGetter = new ConfigGetter<String>(linkedGame);

        String folderPath = this.getClass().getResource(configStringGetter.getValueOf(Config.App.SAVED_GAMES_PATH.key) ).toURI().toString(); 

        // récupération du nombre de parties sauvegardés
        Scanner fileReader = new Scanner(new File(URI.create(folderPath + configStringGetter.getValueOf(Config.App.SAVED_GAMES_INDEX_FILENAME.key) ) ) );   

        int countOfSavedGames = fileReader.nextInt();

        fileReader.close();

        for(int count = 0; count <= countOfSavedGames; count++){
            File gameFile = new File(URI.create(folderPath + count + SavedGames.SAVED_GAMES_EXTENSION) );

            if(!gameFile.exists() ) continue;

            GameDataToSave savedGame = GameDataToSave.getObjectFrom(gameFile);

            // alors échec de lecture du fichier
            if(savedGame == null) continue; 
                
            this.savedGames.put(savedGame.getSaveName(),savedGame);
        }
    }

    public HashMap<String,GameDataToSave> getSavedGames(){
        return this.savedGames;
    }
}
