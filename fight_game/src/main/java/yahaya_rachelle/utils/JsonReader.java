package yahaya_rachelle.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import org.json.simple.parser.ParseException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * permet de récupérer le contenu d'un fichier json
 */
public class JsonReader<ToCastIn> {

    private String key;

    private JSONObject jsonObject;

    public JsonReader(String key,JSONObject jsonObject){
        this.key = key;
        this.jsonObject = jsonObject;
    }

    /**
     * 
     * @return la valeur après cast
     */
    public ToCastIn getValue(){
        @SuppressWarnings("unchecked") ToCastIn value = (ToCastIn) this.jsonObject.get(this.key);

        return value;
    }

    public static JSONObject getJsonFrom(URI fileUri) throws ParseException,FileNotFoundException,IOException{
        return  (JSONObject) new JSONParser().parse(new FileReader(fileUri.getPath() ) );
    }
}
