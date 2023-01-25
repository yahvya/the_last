package yahaya_rachelle.exception;

/**
 * exception de clé non existante
 */
public class KeyNotExist extends Exception{
    public KeyNotExist(){ 
        super("La clé n'existe pas");
    }
}
