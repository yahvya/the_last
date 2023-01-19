package yahaya_rachelle.actor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable;
import yahaya_rachelle.game.GameSession;

public class Player extends Configurable{
    private Character character;

    private String pseudo;

    private Position position;

    private double width;
    private double height;
    private double currentLife;

    public Player(Character character,String pseudo,GameSession linkedGameSession) throws FileNotFoundException, ParseException, IOException, URISyntaxException{
        this.setConfig();
        this.character = character;
        this.pseudo = pseudo;
        
        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this);

        this.width = configLongGetter.getValueOf(Config.Player.PLAYER_WIDTH.key).doubleValue();
        this.height = configLongGetter.getValueOf(Config.Player.PLAYER_HEIGHT.key).doubleValue();

        configLongGetter = new ConfigGetter<Long>(linkedGameSession.getLinkedGame() );

        this.currentLife = configLongGetter.getValueOf(Config.App.PLAYERS_LIFE.key).doubleValue();
    }

    /**
     * définis la position du joueur, doit être appellé avant un getPosition
     * @param position
     */
    public void setPosition(Position position){
        this.position = position;
        this.position
            .setLinkedElementHeight(this.height)
            .setLinkedElementWidth(this.width)
            .updateAll();
    }

    public Position getPosition(){
        return this.position;
    }

    public Character getCharacter() {
        return this.character;
    }

    public String getPseudo() {
        return this.pseudo;
    }

    /**
     * prend un coup du personnage par son attaque basique
     * @param opponent
     * @return this
     */
    public Player receiveHitFromAttackOf(Player opponent){
        this.currentLife -= opponent.getCharacter().getForce();

        return this;
    }

    /**
     * prend un coup du personnsage par sa super attaque
     * @param opponent
     * @return this
     */
    public Player takeHitFromSuperAttackOf(Player opponent){
        this.currentLife -= opponent.getCharacter().getSuperForce();

        return this;
    }

    /**
     * 
     * @return si le joueur est mort
     */
    public boolean isDead(){
        return this.currentLife <= 0;
    }

    public Position getposition() {
        return this.position;
    }

    public double getWidth(){
        return this.width;
    }

    public double getHeight(){
        return this.height;
    }

    public double getCurrentLife(){
        return this.currentLife;
    }

    @Override
    protected String getConfigFilePath() {
        return "/config/player.json";
    }

    public static class Position{
        private double currentX;
        private double currentY;
        private double containerWidth;
        private double containerHeight;
        private double linkedElementWidth;
        private double linkedElementHeight;

        private Direction currentDirection;

        public Position(double currentX,double currentY,double containerWidth,double containerHeight){
            this.containerWidth = containerWidth;       
            this.containerHeight = containerHeight;       
            this.currentX = currentX;
            this.currentY = currentY;  
            this.currentDirection = Direction.RIGHT;   
        }

        /**
         * bouge la position dans la direction actuelle
         * @param direction
         * @return this
         */
        public Position moveOnCurrentDirection(double speed){
            switch(this.currentDirection)
            {
                case RIGHT:
                    if(this.currentX + this.linkedElementWidth + speed < this.containerWidth)
                        this.currentX += speed;
                    else
                        this.currentX = this.containerWidth - this.linkedElementWidth;
                break;
                case LEFT: 
                    if(this.currentX - speed >= 0)
                        this.currentX -= speed; 
                    else
                        this.currentX = 0;
                break;
            }

            return this;
        }

        /**
         * met à jour les valeurs après affectation des données linkedElement...
         */
        public Position updateAll(){
            this.currentY = this.containerHeight - this.linkedElementHeight;
            
            return this;
        }

        /**
         * 
         * @param linkedElementWidth
         * @return this
         */
        public Position setLinkedElementWidth(double linkedElementWidth){
            this.linkedElementWidth = linkedElementWidth;

            return this;
        }

        /**
         * 
         * @param linkedElementHeight
         * @return this
         */
        public Position setLinkedElementHeight(double linkedElementHeight){
            this.linkedElementHeight = linkedElementHeight;

            return this;
        }

        /**
         * 
         * @param currentDirection
         * @return this
         */
        public Position setCurrentDirection(Direction currentDirection){
            this.currentDirection = currentDirection;

            return this;
        }

        public Direction getCurrentDirection(){
            return this.currentDirection;
        }

        public Position setCurrentY(double currentY){
            this.currentY = currentY;

            return this;
        }

        public double getCurrentX(){
            return this.currentX;
        }

        public double getCurrentY(){
            return this.currentY;
        }

        public static enum Direction{RIGHT,LEFT};
    }
}
