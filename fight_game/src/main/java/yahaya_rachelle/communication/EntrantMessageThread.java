package yahaya_rachelle.communication;

import java.io.ObjectInputStream;

/**
 * gère la lecture des messages entrants
 */
public class EntrantMessageThread extends Thread{
    private boolean stopThis;

    private ObjectInputStream input;

    private Communicator linkedCommunicator;

    public EntrantMessageThread(ObjectInputStream input,Communicator linkedCommunicator){
        this.input = input;
        this.linkedCommunicator = linkedCommunicator;
        this.stopThis = false;
    }

    @Override
    public void run(){
        // lecture des messages entrant
        while(!this.stopThis){
            try{
                this.linkedCommunicator.manageEntrantMessage((Message) this.input.readObject() );
            }
            catch(Exception e){e.printStackTrace(); break;}
        }
    }

    /**
     * stoppe l'exécution du thread
     */
    public void stopReading(){
        this.stopThis = true;
    }
}
