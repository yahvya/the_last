package yahaya_rachelle.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

import javafx.scene.text.Font;
import yahaya_rachelle.configuration.Configurable;

public class Fonts extends Configurable{
    public Fonts() throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.setConfig();
    }

    /**
     * 
     * @param key
     * @param size
     * @return la police ou null
     */
    public Font getFont(String key,double size){
        try
        {
            return Font.loadFont(this.getClass().getResource(new ConfigGetter<String>(this).getValueOf(key) ).toString(),size);
        }
        catch(Exception e){
            return null;
        }
    }

    @Override
    protected String getConfigFilePath() {
        return "/config/fonts.json";
    }
}
