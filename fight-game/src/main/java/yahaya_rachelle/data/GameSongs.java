package yahaya_rachelle.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

/**
 * représente les musiques des parties jeux
 */
public class GameSongs extends MediaDatas {

    public GameSongs() throws FileNotFoundException, ParseException, IOException, URISyntaxException {
        super();
    }

    @Override
    protected String getConfigFilePath() {
        return "/config/game-songs.json";
    }

    

}