package yahaya_rachelle.configuration;

/**
 * regroupe les congigrations de l'application
 */ 
public abstract class Config {

    public enum App{
        WINDOW_HEIGHT("window_height"),
        WINDOW_WIDTH("window_width"),
        GAME_NAME("game_name"),
        FAVICON_PATH("favicon"),
        LOADING_POSTER_PATH("loading_poster"),
        LOADING_FONT_PATH("loading_font"),
        LOADING_ON_COLOR("loading_on_color"),
        CHARACTERS_PATH("characters_path"),
        CUSTOM_CHARACTERS_PATH("custom_characters_path"),
        CUSTOM_CHARACTERS_INDEX_FILENAME("custom_characters_index_file_name"),
        CHARACTERS_CONFIG_FILENAME("characters_config_file_name");

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

    public enum PlayerAction{
        ATTACK("attack_state"),
        SUPER_ATTACK("super_attack"),
        DEATH("death"),
        FALL("fall"),
        JUMP("jump"),
        STATIC_POSITION("static"),
        RUN("run"),
        TAKE_HIT("take_hit");
        public final String key;
        
        private PlayerAction(String key){
            this.key = key;
        }
    };

    public enum Character{
        NAME("name"),
        FORCE("force"),
        COUNT_OF_ATTACK_STATE("count_of_attack_state"),
        COUNT_OF_DEATH_STATE("count_of_death_state"),
        COUNT_OF_FALL_STATE("count_of_fall_state"),
        COUNT_OF_JUMP_STATE("count_of_jump_state"),
        COUNT_OF_RUN_STATE("count_of_run_state"),
        COUNT_OF_STATIC_STATE("count_of_static_state"),
        COUNT_OF_SUPER_ATTACK_STATE("count_of_super_attack_state"),
        COUNT_OF_TAKE_HIT_STATE("count_of_take_hit_state");

        public final String key;

        private Character(String key){
            this.key = key;
        }
    }

    public enum Scenes{
        SCENES("scenes"),
        IMAGE("image");

        public final String key;
        
        private Scenes(String key){
            this.key = key;
        }
    }
}
