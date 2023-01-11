package yahaya_rachelle.game.game;

import java.util.HashMap;

import javafx.scene.text.Font;
import yahaya_rachelle.game.exception.KeyNotExist;

public class GameLoader{

    GameDataManager gameDataManager;

    public GameLoader(GameDataManager gameDataManager)
    {
        this.gameDataManager = gameDataManager;
        this.loadGame();
    }

    /**
     * charge le jeux
     */
    public void loadGame()
    {
        this.fillPathMap();
        this.fillFonts();
    }

    /**
     * rempli la hashmap avec les chemin des différentes resources
     */
    public void fillPathMap(){
        HashMap<Key,String> resourcesPathMap = new HashMap<Key,String>();

        resourcesPathMap.put(Key.PATH_CHARACTERS,"/characters/");
        resourcesPathMap.put(Key.PATH_APP_IMAGES,"/images/app/");
        resourcesPathMap.put(Key.PATH_APP_ITEMS,"/images/items/");
        resourcesPathMap.put(Key.PATH_APP_SCENES,"/images/scenes/");
        resourcesPathMap.put(Key.PATH_APP_SONGS,"/songs/app/");
        resourcesPathMap.put(Key.PATH_GAME_SONGS,"/songs/game/");
        resourcesPathMap.put(Key.PATH_FONTS,"/fonts/");

        this.gameDataManager.setResourcesPathMap(resourcesPathMap);
    }

    /**
     * rempli la map avec les polices
     */
    public void fillFonts(){
        HashMap<Key,Font> fontsMap = new HashMap<Key,Font>();

        try
        {
            fontsMap.put(Key.FONT_NORMAL,new Font(this.gameDataManager.getResource(Key.PATH_FONTS,"basic.ttf").toString(),20) );
            fontsMap.put(Key.FONT_NORMAL,new Font(this.gameDataManager.getResource(Key.PATH_FONTS,"special.ttf").toString(),20) );
        }
        catch(KeyNotExist e){}

        this.gameDataManager.setFontsMap(fontsMap);
    }

    /**
     * rempli la map des images (items) de jeux
     */
    public void fillGameItems(){
        
    }   

    /**
     * représente la liste des clés des map de données du jeux
     */
    public enum Key{
        // clés des chemins
        PATH_CHARACTERS,PATH_APP_IMAGES,PATH_APP_ITEMS,PATH_APP_SCENES,PATH_APP_SONGS,PATH_GAME_SONGS,PATH_FONTS,
        // clé des polices
        FONT_SPECIAL,FONT_NORMAL
    };
}
