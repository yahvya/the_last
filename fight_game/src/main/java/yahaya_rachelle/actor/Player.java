package yahaya_rachelle.actor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable;
import yahaya_rachelle.configuration.Config.PlayerAction;

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

    @Override
    protected String getConfigFilePath() {
        return "/config/player.json";
    }

    public class Position{
        private int minX;
        private int maxX;
        private int minY;
        private int maxY;
        private int currentX;
        private int currentY;

        public Position(int minX,int minY,int maxX,int maxY,int currentX,int currentY){
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
            this.currentX = currentX;
            this.currentY = currentY;
        }  
        
        /**
         * tente de bouger la position à x
         * @param x
         */
        public void moveToX(int x){
            if(this.minX > x)
                this.currentX = this.minX;
            else if (this.maxX < x)
                this.currentX = this.maxX;
            else
                this.currentX = x;
        }

        /**
         * tente de bouger la position à y
         * @param y
         */
        public void moveToY(int y){
            if(this.minY > y)
                this.currentY = this.minY;
            else if (this.maxY < y)
                this.currentY = this.maxY;
            else
                this.currentY = y;
        }   


        /**
         * tente d'ajouter à x
         * @param toAdd
         */
        public void addToX(int toAdd){
            this.moveToX(this.currentX + toAdd);
        }

        /**
         * tente d'ajouter à Y
         * @param toAdd
         */
        public void addToY(int toAdd){
            this.moveToY(this.currentY + toAdd);
        }

        /**
         * tente de retirer à x
         * @param toRemove
         */
        public void removeToX(int toRemove){
            this.moveToY(this.currentX - toRemove);
        }

        /**
         * tente de retirer à y
         * @param toRemove
         */
        public void removeToY(int toRemove){
            this.moveToY(this.currentY - toRemove);
        }
    }

}
