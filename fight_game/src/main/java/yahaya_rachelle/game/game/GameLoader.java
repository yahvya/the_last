package yahaya_rachelle.game.game;

import java.util.HashMap;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
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
        this.fillGameItems();
        this.fillSongs();
    }

    /**
     * rempli la hashmap avec les chemin des différentes ressources
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
        HashMap<Key,String> fontsMap = new HashMap<Key,String>();

        try
        {
            fontsMap.put(Key.FONT_NORMAL,this.gameDataManager.getResource(Key.PATH_FONTS,"basic.ttf").toString() );
            fontsMap.put(Key.FONT_SPECIAL,this.gameDataManager.getResource(Key.PATH_FONTS,"special.ttf").toString() );
        }
        catch(KeyNotExist e){}

        this.gameDataManager.setFontsMap(fontsMap);
    }

    /**
     * rempli la map des images (items) de jeux
     */
    public void fillGameItems(){
        HashMap<Key,Image> gameItems = new HashMap<Key,Image>();

        try
        {
            gameItems.put(Key.ITEM_PARCHMENT,new Image(gameDataManager.getResource(Key.PATH_APP_ITEMS,"parchment.png").toString() ) );
            gameItems.put(Key.ITEM_PARCHMENT_D_TEXTURE,new Image(gameDataManager.getResource(Key.PATH_APP_ITEMS,"parchment-d-texture.png").toString() ) );
            gameItems.put(Key.ITEM_PARCHMENT_TEXTURE,new Image(gameDataManager.getResource(Key.PATH_APP_ITEMS,"parchment-texture.png").toString() ) );
        }
        catch(KeyNotExist e){}

        this.gameDataManager.setItemsMap(gameItems);
    }   

    /**
     * rempli la map des song
     */
    public void fillSongs()
    {
        HashMap<Key,Media> songsMap = new HashMap<Key,Media>();

        try
        {
            songsMap.put(Key.SONG_APP_HOME,new Media(this.gameDataManager.getResource(Key.PATH_APP_SONGS,"home.mp3").toString() ) );
            songsMap.put(Key.SONG_REFUSED_ACTION,new Media(this.gameDataManager.getResource(Key.PATH_APP_SONGS,"refused-action.mp3").toString() ) );
        }
        catch(KeyNotExist e){}

        this.gameDataManager.setSongsMap(songsMap);
    }   

    /**
     * représente la liste des clés des map de données du jeux
     */
    public enum Key{
        // clés des chemins
        PATH_CHARACTERS,PATH_APP_IMAGES,PATH_APP_ITEMS,PATH_APP_SCENES,PATH_APP_SONGS,PATH_GAME_SONGS,PATH_FONTS,
        // clé des polices
        FONT_SPECIAL,FONT_NORMAL,
        // clé des items
        ITEM_PARCHMENT,ITEM_PARCHMENT_TEXTURE,ITEM_PARCHMENT_D_TEXTURE,
        // clé des sons
        SONG_APP_HOME,SONG_REFUSED_ACTION
    };
}
