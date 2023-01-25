package yahaya_rachelle.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

/**
 * représente les items du jeux
 */
public class Items extends ImageDatas {

    public Items() throws FileNotFoundException, ParseException, IOException, URISyntaxException {
        super();
    }

    @Override
    protected String getConfigFilePath() {
        return "/config/items.json"; 
    }

}