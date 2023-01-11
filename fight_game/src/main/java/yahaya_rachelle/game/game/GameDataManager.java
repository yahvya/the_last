package yahaya_rachelle.game.game;

import java.net.URL;
import java.util.HashMap;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.text.Font;
import yahaya_rachelle.game.exception.KeyNotExist;
import yahaya_rachelle.game.game.GameLoader.Key;

public class GameDataManager {
    private HashMap<Key,String> resourcesPathMap;
    private HashMap<Key,Font> fontsMap;
    private HashMap<Key,Media> songsMap;
    private HashMap<Key,Image> itemsMap;  

    /**
     * charge les donées du jeux
     */
    public void loadDatas(){
        new GameLoader(this);
    }

    /**
     * 
     * @param key
     * @param resourcePath
     * @return l'URL d'une resource à partir de la map
     * @throws KeyNotExist
     */
    public URL getResource(Key key,String resourcePath) throws KeyNotExist{
        if(!this.resourcesPathMap.containsKey(key) )
            throw new KeyNotExist();

        return this.getClass().getResource(this.resourcesPathMap.get(key) + resourcePath);
    }


    public HashMap<Key,String> getResourcesPathMap() {
        return this.resourcesPathMap;
    }

    public void setResourcesPathMap(HashMap<Key,String> resourcesPathMap) {
        this.resourcesPathMap = resourcesPathMap;
    }

    public HashMap<Key,Font> getFontsMap() {
        return this.fontsMap;
    }

    public void setFontsMap(HashMap<Key,Font> fontsMap) {
        this.fontsMap = fontsMap;
    }

    public HashMap<Key,Media> getSongsMap() {
        return this.songsMap;
    }

    public void setSongsMap(HashMap<Key,Media> songsMap) {
        this.songsMap = songsMap;
    }

    public HashMap<Key,Image> getItemsMap() {
        return this.itemsMap;
    }

    public void setItemsMap(HashMap<Key,Image> itemsMap) {
        this.itemsMap = itemsMap;
    }
    
}
