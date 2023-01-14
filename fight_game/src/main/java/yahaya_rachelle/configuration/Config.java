package yahaya_rachelle.configuration;

public abstract class Config {


     /**
     * regroupe les clés du fichier de configuration de l'application
     */
    public enum App{
        WINDOW_HEIGHT("window_height"),
        WINDOW_WIDTH("window_width"),
        GAME_NAME("game_name"),
        FAVICON_PATH("favicon"),
        LOADING_POSTER_PATH("loading_poster"),
        LOADING_FONT_PATH("loading_font"),
        LOADING_ON_COLOR("loading_on_color"),
        CUSTOM_CHARACTERS_PATH("custom_characters_path");

        public final String key;

        private App(String key){
            this.key = key;
        }
    };

    public enum AppSongs {
        HOME("home"),
        REFUSED("refused");
        
        public final String key;

        private AppSongs(String key) {
            this.key = key;
        }

    }

    public enum Fonts {
        BASIC("basic"),
        SPECIAL("special");

        public final String key;

        private Fonts(String key) {
            this.key = key;
        }
    }

    public enum GameSongs {
        ;

        public final String key;

        private GameSongs(String key) {
            this.key = key;
        }
    }

    public enum Items {

        PARCHMENT("parchment"),
        PARCHMENT_TEXTURE("parchment_texture"),
        PARCHMENT_D_TEXTURE("parchment_d_texture"),
        HOME_BACKGROUND("home_background");

        public final String key;

        private Items(String key){
            this.key = key;
        }
    }

 


}
