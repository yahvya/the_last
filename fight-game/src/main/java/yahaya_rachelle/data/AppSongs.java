package yahaya_rachelle.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

/**
 * représente les musiques de l'application
 */
public class AppSongs extends MediaDatas {
    
    public AppSongs() throws FileNotFoundException, ParseException, IOException, URISyntaxException {
        super();
    }

    @Override
    protected String getConfigFilePath() {
        return "/config/app-songs.json";
    }
}