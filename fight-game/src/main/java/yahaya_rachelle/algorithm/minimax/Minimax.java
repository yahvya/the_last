package yahaya_rachelle.algorithm.minimax;

import javafx.scene.input.KeyCode;
import yahaya_rachelle.actor.Player;

import java.util.ArrayList;

/**
 * gestion de l'algortihme minimax
 */
public class Minimax{
    /**
     * noeud arbre minimax
     */
    public static class TreeNode{
        /**
         * état ia lié au noeud
         */
        private PlayerState aiState;

        /**
         * état opposant lié au noeud
         */
        private PlayerState opponentState;

        /**
         * true = max ou min
         */
        private boolean isMax;

        private KeyCode code;

        /**
         * noeud de l'arbre
         */
        private ArrayList<TreeNode> childs;

        /**
         *
         * @param aiState état de l'ia joueur lié
         * @param opponentState état de l'opposant joueur lié
         * @param isMax si true = joueur max sinon min
         * @param code touche lié
         */
        TreeNode(PlayerState aiState,PlayerState opponentState,boolean isMax,KeyCode code){
            this.aiState = aiState;
            this.opponentState = opponentState;
            this.isMax = isMax;
            this.childs = new ArrayList<TreeNode>();
            this.code = code;
        }

        /**
         * ajout un noeud aux enfants
         * @param node le noeud à ajouter
         * @return this
         */
        public TreeNode addNode(TreeNode node){
            this.childs.add(node);

            return this;
        }

        /**
         *
         * @return l'état de l'ia
         */
        public PlayerState getAiState(){
            return this.aiState;
        }

        /**
         *
         * @return l'état de l'opposant
         */
        public PlayerState getOpponentState(){
            return this.opponentState;
        }

        /**
         *
         * @return true si noeud max sinon false pour min
         */
        public boolean getIsMax(){
            return this.isMax;
        }

        /**
         *
         * @return map des branches
         */
        public ArrayList<TreeNode> getChilds(){
            return this.childs;
        }
    }

    /**
     * état d'un joueur
     */
    public static class PlayerState{
        /**
         * copie du joueur donné en constructeur sans image
         */
        private Player player;

        PlayerState(Player player){
            this.player = player.copy();
            this.player.getCharacter().clearImageSequences();
        }

        /**
         *
         * @return une copie de l'état
         */
        PlayerState copy(){
            return new PlayerState(this.player);
        }

        /**
         *
         * @return le joueur lié
         */
        Player getLinkedPlayer(){
            return this.player;
        }
    }
}
