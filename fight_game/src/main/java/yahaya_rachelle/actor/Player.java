package yahaya_rachelle.actor;

public class Player {
    private Character character;

    private String pseudo;

    private Position playerPosition;

    public Player(Character character,String pseudo,Position defaultPosition){
        this.character = character;
        this.pseudo = pseudo;
        this.playerPosition = defaultPosition;
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
