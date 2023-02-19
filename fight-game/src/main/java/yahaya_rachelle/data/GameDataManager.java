package yahaya_rachelle.data;


import java.util.ArrayList;

import javafx.application.Platform;
import yahaya_rachelle.actor.Character;
import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable.ConfigGetter;
import yahaya_rachelle.game.Game;
import yahaya_rachelle.utils.GameCallback;

/**
 * gère les données du jeux
 */
public class GameDataManager {

    private AppSongs appSongs;
    
    private Fonts fonts;

    private GameSongs gameSongs;

    private Items items;

    private Scenes scenes;

    private ArrayList<Character> characters;

    private SavedGames savedGames;

    private Game linkedGame;

    public GameDataManager(Game linkedGame){
        this.linkedGame = linkedGame;
    }
    
    /**
     * charge les données du jeux
     */
    public void loadDatas(GameCallback toCallOnSuccess,GameCallback toCallOnFailure){
        GameDataManager manager = this;

        // lancement du thread de chargement des données
        new Thread(){
            @Override
            public void run(){ 
                try{
                    // chargement des données  
                    manager.fonts = new Fonts();
                    manager.appSongs = new AppSongs();
                    manager.gameSongs = new GameSongs();
                    manager.items = new Items();
                    manager.scenes = new Scenes();
                    manager.savedGames = new SavedGames(linkedGame);

                    // récupération et ajout des personnages du jeux
                    ConfigGetter<String> configStringGetter = new ConfigGetter<String>(manager.linkedGame);

                    manager.characters = Character.loadCharacters(manager.getClass(),configStringGetter.getValueOf(Config.App.CHARACTERS_PATH.key),manager.linkedGame);
                    manager.characters.addAll(Character.loadCharacters(manager.getClass(),configStringGetter.getValueOf(Config.App.CUSTOM_CHARACTERS_PATH.key),manager.linkedGame) );

                    // lancement de l'action callback de succès
                    Platform.runLater(() -> toCallOnSuccess.action() );
                }
                catch(Exception e){
                     // lancement de l'action callback d'échec
                     Platform.runLater(() -> toCallOnFailure.action() );
                }
            }
        }.start();
    }

    /**
     * ajoute un personnsage à la liste
     * @param character
     */
    public void addCharacter(Character character){
        this.characters.add(character);
    }

    public AppSongs getAppSongs() {
        return this.appSongs;
    }

    public Fonts getFonts() {
        return this.fonts;
    }

    public GameSongs getGameSongs() {
        return this.gameSongs;
    }

    public Items getItems() {
        return this.items;
    }

    public ArrayList<Character> getCharacters(){
        return this.characters;
    }

    public Game getLinkedGame(){
        return this.linkedGame;
    }

    public Scenes getScenes(){
        return this.scenes;
    }

    public SavedGames getSavedGames(){
        return this.savedGames;
    }
}
