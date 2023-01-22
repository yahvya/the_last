package yahaya_rachelle.game;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class GameCommunicator {

    private ArrayList<ObjectOutputStream> outputList;
    private ObjectInputStream input;

    public GameCommunicator(){
        // je sais pas encore
    }

    /**
     * envoie le message à la liste des participants, tente de renotifier une fois après 300 ms en cas de premier échec
     * @param message
     */
    public void propagateMessage(Message message){
        ArrayList<ObjectOutputStream> retryList = new ArrayList<ObjectOutputStream>();

        outputList.forEach((output) -> {
            try{
                // envoie du message
                output.writeObject(message);
            }
            catch(Exception e){
                // sauvegarde dans la liste des personnes à renotifier
                retryList.add(output);
            }
        });

        if(retryList.size() != 0){
            // timeline pour débloquer la super attaque
            Timeline unlockTimeline = new Timeline(new KeyFrame(Duration.ONE,(e) -> {
                retryList.forEach((output) -> {
                    try{
                        // envoie du message
                        output.writeObject(message);
                    }
                    catch(Exception exception){}
                });
            }) );

            unlockTimeline.setCycleCount(1);
            unlockTimeline.setDelay(Duration.millis(300) );
            unlockTimeline.play();
        }
    }

    public enum MessageType{};

    public interface Message{
        public MessageType getMessageType();
        public Object getMessageData();
    }
}
