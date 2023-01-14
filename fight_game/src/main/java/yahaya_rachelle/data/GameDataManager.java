package yahaya_rachelle.data;


import javafx.application.Platform;
import yahaya_rachelle.utils.GameCallback;

public class GameDataManager {

    private AppSongs appSongs;
    
    private Fonts fonts;

    private GameSongs gameSongs;

    private Items items;
    
    
    /**
     * charge les données du jeux
     */
    public void loadDatas(GameCallback toCallOnSuccess,GameCallback toCallOnFailure){
        GameDataManager manager = this;

        // lancement du thread de chargement des données
        Thread loadingThread = new Thread(){
            @Override
            public void run()
            { 
                try
                {
                    // chargement des données  
                    manager.fonts = new Fonts();
                    manager.appSongs = new AppSongs();
                    manager.gameSongs = new GameSongs();
                    manager.items = new Items();

                    // lancement de l'action callback de succès
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            toCallOnSuccess.action();
                        }
                    });
                }
                catch(Exception e){
                     // lancement de l'action callback d'échec
                     Platform.runLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            toCallOnFailure.action();
                        }
                    });
                }
            }
        };

        loadingThread.start();
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

}
