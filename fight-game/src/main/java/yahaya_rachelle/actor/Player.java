package yahaya_rachelle.actor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.json.simple.parser.ParseException;

import yahaya_rachelle.configuration.Config;
import yahaya_rachelle.configuration.Configurable;
import yahaya_rachelle.configuration.Config.PlayerAction;
import yahaya_rachelle.game.GameSession;

/**
 * représente un joueur
 */
public class Player extends Configurable implements Serializable{

    public static final List<PlayerAction> playerHitActions = Arrays.asList(PlayerAction.ATTACK,PlayerAction.SUPER_ATTACK);

    protected Character character;

    protected String pseudo;

    protected Position position;

    protected double width;
    protected double height;
    protected double currentLife;

    protected boolean canDoSuperAttack;
    protected boolean canDoAction;
    protected boolean canMoveS;

    public Player(Character character,String pseudo,GameSession linkedGameSession) throws FileNotFoundException, ParseException, IOException, URISyntaxException{

        this.setConfig();
        this.character = character;
        this.pseudo = pseudo;
        this.canDoSuperAttack = true;
        this.canDoAction = true;
        this.canMoveS = true;
        
        ConfigGetter<Long> configLongGetter = new ConfigGetter<Long>(this);

        this.width = configLongGetter.getValueOf(Config.Player.PLAYER_WIDTH.key).doubleValue();
        this.height = configLongGetter.getValueOf(Config.Player.PLAYER_HEIGHT.key).doubleValue();

        configLongGetter = new ConfigGetter<Long>(linkedGameSession.getLinkedGame() );

        this.currentLife = configLongGetter.getValueOf(Config.App.PLAYERS_LIFE.key).doubleValue();
    }

    /**
     * constructeur de copie
     */
    protected Player(Character character,String pseudo){
        this.character = character;
        this.pseudo = pseudo;
        this.canDoSuperAttack = true;
        this.canDoAction = true;
        this.canMoveS = true;
    }

    /**
     * 
     * @param fromPlayer
     * @param attackType
     * @return this
     */
    synchronized public Player receiveHitFrom(Player fromPlayer,PlayerAction attackType){
        switch(attackType){
            case ATTACK: this.currentLife -= fromPlayer.getCharacter().getForce() ; break;
            case SUPER_ATTACK: this.currentLife -= fromPlayer.getCharacter().getSuperForce(); break;
            default:;
        }
        
        return this;
    }
   
    /**
     * 
     * @return si le joueur est mort
     */
    synchronized public boolean isDead(){
        return this.currentLife <= 0;
    }

    /**
     * définis la position du joueur, doit être appellé avant un getPosition
     * @param position
     */
    synchronized public void setPosition(Position position){
        this.position = position;
        this.position
            .setLinkedElementHeight(this.height)
            .setLinkedElementWidth(this.width)
            .updateAll();
    }

    synchronized public Player setCanDoAction(boolean canDoAction){
        this.canDoAction = canDoAction;

        return this;
    }

    synchronized public Player setCanDoSuperAttack(boolean canDoSuperAttack){
        this.canDoSuperAttack = canDoSuperAttack;
        
        return this;
    }

    synchronized public Player setCanMoveS(boolean canMoveS){
        this.canMoveS = canMoveS;
        
        return this;
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
     *
     * @return une copie de cet objet
     */
    synchronized public Player copy(){
        Player copiedPlayer = new Player(this.character.copy(),this.pseudo);

        copiedPlayer.width = this.width;
        copiedPlayer.height = this.height;
        copiedPlayer.currentLife = this.currentLife;
        copiedPlayer.position = this.position.copy();

        return copiedPlayer;
    }

    synchronized public boolean getCanDoSuperAttack(){
        return this.canDoSuperAttack;
    }

    synchronized public boolean getCanDoAction(){
        return this.canDoAction;
    }

   synchronized public boolean getCanMoveS(){
        return this.canMoveS;
    }

    @Override
    protected String getConfigFilePath() {
        return "/config/player.json";
    }

    /**
     * représente la position d'un joueur
     */
    public static class Position implements Serializable{
        protected double currentX;
        protected double currentY;
        protected double containerWidth;
        protected double containerHeight;
        protected double linkedElementWidth;
        protected double linkedElementHeight;

        protected Direction currentDirection;

        public Position(double currentX,double currentY,double containerWidth,double containerHeight){
            this.containerWidth = containerWidth;       
            this.containerHeight = containerHeight;       
            this.currentX = currentX;
            this.currentY = currentY;  
            this.currentDirection = Direction.RIGHT;
        }

        /**
         *
         * @return une copie de cet objet
         */
        synchronized public Position copy(){
            Position position = new Position(this.currentX,this.currentY,this.containerWidth,this.containerHeight);

            position.currentDirection = this.currentDirection;
            position.linkedElementHeight = this.linkedElementHeight;
            position.linkedElementWidth = this.linkedElementWidth;

            return position;
        }

        /**
         * bouge la position dans la direction actuelle
         * @param speed
         * @return this
         */
        synchronized public Position moveOnCurrentDirection(double speed){
            return this.moveOnDirection(speed,this.currentDirection);
        }

        /**
         * bouge dans la direction opposé
         * @param speed
         * @return this
         */
        synchronized public Position moveOnOppositeDirection(double speed){
            return this.moveOnDirection(speed,this.currentDirection == Direction.RIGHT ? Direction.LEFT : Direction.RIGHT);
        }

        /**
         * bouge dans la direction indiqué
         * @param speed
         * @param direction
         * @return this
         */
        synchronized protected Position moveOnDirection(double speed,Direction direction){
            
            switch(direction)
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
        synchronized public Position updateAll(){
            this.currentY = this.containerHeight - this.linkedElementHeight;
            
            return this;
        }

        /**
         * 
         * @param linkedElementWidth
         * @return this
         */
        synchronized public Position setLinkedElementWidth(double linkedElementWidth){
            this.linkedElementWidth = linkedElementWidth;

            return this;
        }

        /**
         * 
         * @param linkedElementHeight
         * @return this
         */
        synchronized public Position setLinkedElementHeight(double linkedElementHeight){
            this.linkedElementHeight = linkedElementHeight;

            return this;
        }

        /**
         * 
         * @param currentDirection
         * @return this
         */
        synchronized public Position setCurrentDirection(Direction currentDirection){
            this.currentDirection = currentDirection;

            return this;
        }

        synchronized public Direction getCurrentDirection(){
            return this.currentDirection;
        }

        synchronized public Position setCurrentY(double currentY){
            this.currentY = currentY;

            return this;
        }

        synchronized public double getCurrentX(){
            return this.currentX;
        }

        synchronized public double getCurrentY(){
            return this.currentY;
        }

        public static enum Direction{RIGHT,LEFT};
    }
}
