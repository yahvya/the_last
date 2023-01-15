package yahaya_rachelle.actor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.parser.ParseException;

import javafx.scene.image.Image;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable;
import yahaya_rachelle.game.Game;

public class Character extends Configurable{
    private HashMap<Config.PlayerAction,ArrayList<Image> > actionsMap;

    private String configFilePath;

    public Character(String configFilePath) throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.configFilePath = configFilePath;
        
        this.setConfig();
    }   

    /**
     * 
     * @param action
     * @return la liste des images décrivant l'action donnée
     */
    public ArrayList<Image> getActionSequence(Config.PlayerAction action){
        return this.actionsMap.get(action);
    }

    @Override
    protected String getConfigFilePath() {
        return this.configFilePath;
    } 

    /**
     * 
     * @return liste de personnages
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParseException
     * @throws FileNotFoundException
     */
    public static ArrayList<Character> loadCharacters(Class<?> usableClass,String rootFolderPath,Game linkedGame)  throws NullPointerException, URISyntaxException, FileNotFoundException, ParseException, IOException{
        ArrayList<Character> list = new ArrayList<Character>();

        File directory = new File(usableClass.getResource(rootFolderPath).toURI() );

        ConfigGetter<String> configStringGetter = new ConfigGetter<String>(linkedGame);
    
        String toIgnore = configStringGetter.getValueOf(Config.App.CUSTOM_CHARACTERS_INDEX_FILENAME.key);
        String charactersConfigFileName = configStringGetter.getValueOf(Config.App.CHARACTERS_CONFIG_FILENAME.key);

        for(String subDirectory : directory.list() )
        {
            if(!subDirectory.equals(toIgnore) )
                list.add(new Character(String.join("/",rootFolderPath + subDirectory,charactersConfigFileName) ) );
        }

        return list;
    }   
}
