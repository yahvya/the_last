package yahaya_rachelle.game;

import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

import yahaya_rachelle.actor.Player;

public class GameDataToSave implements Serializable{
    private Player savedPlayer;

    private String gameSessionCode;
    private String saveName;

    public GameDataToSave(Player savedPlayer,String gameSessionCode,String saveName){
        this.savedPlayer = savedPlayer;
        this.gameSessionCode = gameSessionCode;
        this.saveName = saveName;
    }

    /**
     * sauvegarde l'objet dans le fichier au chemin spécifié
     * @param savePath
     * @return si la sauvegarde à réussi
     */
    public boolean saveIn(URI savePath){
        try{
            FileOutputStream fileOutputStream = new FileOutputStream(new File(savePath) );

            ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream );

            // écriture de cet objet dans le fichier
            outputStream.writeObject(this);

            fileOutputStream.close();
            outputStream.close();

            return true;
        }
        catch(Exception e){}

        return false;
    }

    /**
     * récupère un objet de ce type dans le fichier au chemin spécifié
     * @param sourcePath
     * @return un objet de ce type si succès ou null en cas d'échec
     */
    public static GameDataToSave getObjectFrom(File sourceFile){
        try{
            FileInputStream fileInputStream = new FileInputStream(sourceFile);

            ObjectInputStream inputStream = new ObjectInputStream(fileInputStream);

            // lecture de l'objet sauvegardé
            GameDataToSave savedObject = (GameDataToSave) inputStream.readObject();

            fileInputStream.close();
            inputStream.close();

            return savedObject;
        }
        catch(Exception e){}

        return null;
    }

    public Player getSavedPlayer(){
        return this.savedPlayer;
    }

    public String getGameSessionCode(){
        return this.gameSessionCode;
    }

    public String getSaveName(){
        return this.saveName;
    }
}
