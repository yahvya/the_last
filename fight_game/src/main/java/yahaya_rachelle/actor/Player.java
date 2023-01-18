package yahaya_rachelle.actor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable;

public class Player extends Configurable{
    private Character character;

    private String pseudo;

    private Position playerPosition;

    private double width;
    private double height;

    public Player(Character character,String pseudo) throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.setConfig();
        this.character = character;
        this.pseudo = pseudo;
        
        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this);

        this.width = configLongGetter.getValueOf(Config.Player.PLAYER_WIDTH.key).doubleValue();
        this.height = configLongGetter.getValueOf(Config.Player.PLAYER_HEIGHT.key).doubleValue();
    }

    /**
     * définis la position du joueur, doit être appellé avant un getPosition
     * @param position
     */
    public void setPosition(Position playerPosition){
        this.playerPosition = playerPosition;
    }

    public Position getPosition(){
        return this.playerPosition;
    }

    public Character getCharacter() {
        return this.character;
    }

    public String getPseudo() {
        return this.pseudo;
    }

    public Position getPlayerPosition() {
        return this.playerPosition;
    }

    public double getWidth(){
        return this.width;
    }

    public double getHeight(){
        return this.height;
    }

    @Override
    protected String getConfigFilePath() {
        return "/config/player.json";
    }

    public static class Position{
        private int currentX;
        private int currentY;
        private Direction direction;

        public Position(int currentX,int currentY){
            this.currentX = currentX;
            this.currentY = currentY;         
            this.direction = Direction.RIGHT;   
        }

        public void setDirection(Direction direction){
            this.direction = direction;
        }

        public Direction getDirection(){
            return this.direction;
        }

        public int getCurrentX(){
            return this.currentX;
        }

        public int getCurrentY(){
            return this.currentY;
        }

        public static enum Direction{RIGHT,LEFT};
    }

}
