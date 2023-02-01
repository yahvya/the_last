package yahaya_rachelle.communication.message;

import java.io.Serializable;

import javafx.scene.input.KeyCode;
import yahaya_rachelle.configuration.Config.PlayerAction;

public class PlayerActionMessage implements Serializable{
    private KeyCode code;

    private PlayerAction action;

    public PlayerActionMessage(KeyCode code,PlayerAction action){
        this.code = code;
        this.action = action;
    }

    public KeyCode getCode(){
        return this.code;
    }

    public PlayerAction getAction(){
        return this.action;
    }
}
