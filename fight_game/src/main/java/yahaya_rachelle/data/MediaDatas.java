package yahaya_rachelle.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

import javafx.scene.media.Media;
import yahaya_rachelle.configuration.Configurable;

public abstract class MediaDatas extends Configurable{

    public MediaDatas() throws FileNotFoundException, ParseException, IOException, URISyntaxException {
        this.setConfig();
    }
    
    /**
     * 
     * @return l'objet son ou null si une erreur s'est produite
     */
    public Media getMedia(String key){

        try{
            return new Media(this.getClass().getResource(new ConfigGetter<String>(this).getValueOf(key) ).toURI().toString() );
        }
        catch(Exception e){
            return null;
        }
    }
}
