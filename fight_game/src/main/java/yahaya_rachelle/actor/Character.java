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
    private String directory;
    private String name;

    public Character(String configFilePath) throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.configFilePath = configFilePath;
        this.actionsMap = new HashMap<Config.PlayerAction,ArrayList<Image> >();

        File directoryObject = new File(this.configFilePath).getParentFile();
        
        this.directory = directoryObject.toString() + "/";
        
        this.setConfig();

        ConfigGetter<Long> configLongReader = new ConfigGetter<Long>(this);
        ConfigGetter<String> configStringReader = new ConfigGetter<String>(this);

        this.name = configStringReader.getValueOf(Config.Character.NAME.key);

        String[] childsList = new File(this.getClass().getResource(directoryObject.getPath().replace("\\","/") ).toURI() ).list();

        // chargement des images du personnage
        this.loadImagesFor(Config.PlayerAction.ATTACK,configLongReader.getValueOf(Config.Character.COUNT_OF_ATTACK_STATE.key).intValue(),childsList);
        this.loadImagesFor(Config.PlayerAction.DEATH,configLongReader.getValueOf(Config.Character.COUNT_OF_DEATH_STATE.key).intValue(),childsList);
        this.loadImagesFor(Config.PlayerAction.JUMP,configLongReader.getValueOf(Config.Character.COUNT_OF_JUMP_STATE.key).intValue(),childsList);
        this.loadImagesFor(Config.PlayerAction.RUN,configLongReader.getValueOf(Config.Character.COUNT_OF_RUN_STATE.key).intValue(),childsList);
        this.loadImagesFor(Config.PlayerAction.STATIC_POSITION,configLongReader.getValueOf(Config.Character.COUNT_OF_STATIC_STATE.key).intValue(),childsList);
        this.loadImagesFor(Config.PlayerAction.SUPER_ATTACK,configLongReader.getValueOf(Config.Character.COUNT_OF_SUPER_ATTACK_STATE.key).intValue(),childsList);
        this.loadImagesFor(Config.PlayerAction.TAKE_HIT,configLongReader.getValueOf(Config.Character.COUNT_OF_TAKE_HIT_STATE.key).intValue(),childsList);
    } 
    
    /**
     * charge les images de ce joueur
     * @param action
     * @param countOfImages
     */
    private void loadImagesFor(Config.PlayerAction action,int countOfImages,String[] filesList) throws NullPointerException,IllegalArgumentException{
        ArrayList<Image> imageList = new ArrayList<Image>();

        int filesListLength = filesList.length;

        // récupération des images
        for(; countOfImages > 0; countOfImages--)
        {
            for(int index = 0; index < filesListLength; index++)
            {
                if(filesList[index].startsWith(action.key) )
                {
                    imageList.add(new Image(this.directory + filesList[index]) );

                    break;
                }
            }
        }

        this.actionsMap.put(action,imageList);
    }

    /**
     * 
     * @param action
     * @return la liste des images décrivant l'action donnée
     */
    public ArrayList<Image> getActionSequence(Config.PlayerAction action){
        return this.actionsMap.get(action);
    }

    public String getName(){
        return this.name;
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
