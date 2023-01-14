package yahaya_rachelle.actor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.parser.ParseException;

import javafx.scene.image.Image;
import yahaya_rachelle.actor.Player.PlayerAction;
import yahaya_rachelle.configuration.Configurable;
import yahaya_rachelle.configuration.Config;

public class Character extends Configurable{
    private HashMap<PlayerAction,ArrayList<Image> > actionsMap;

    private String directory;

    public Character(String directory) throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.directory = directory;
        
        // this.setConfig();
    }   

    /**
     * 
     * @param action
     * @return la liste des images décrivant l'action donnée
     */
    public ArrayList<Image> getActionSequence(PlayerAction action){
        return this.actionsMap.get(action);
    }

    @Override
    protected String getConfigFilePath() {
        return this.directory + "config.json";
    } 

    /**
     * 
     * @return liste de personnages
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParseException
     * @throws FileNotFoundException
     */
    public static ArrayList<Character> loadCharacters(Class<?> usableClass,String rootFolderPath)  throws NullPointerException, URISyntaxException, FileNotFoundException, ParseException, IOException{
        ArrayList<Character> list = new ArrayList<Character>();

        File directory = new File(usableClass.getResource(rootFolderPath).toURI() );

        for(String subDirectory : directory.list() )
        {
            if(subDirectory != Config.App.CUSTOM_CHARACTERS_INDEX_FILENAME.key)
                list.add(new Character(rootFolderPath + subDirectory) );
        }

        return list;
    }   
}
