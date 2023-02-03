package yahaya_rachelle.communication.communication;

import java.io.ObjectInputStream;
import java.net.Socket;

import yahaya_rachelle.communication.message.Message;

/**
 * gère la lecture des messages entrants
 */
public class EntrantMessageThread extends Thread{
    private boolean stopThis;

    private ObjectInputStream input;

    private Communicator linkedCommunicator;

    private Socket sourceSocket;

    public EntrantMessageThread(Socket sourceSocket,ObjectInputStream input,Communicator linkedCommunicator){
        this.input = input;
        this.linkedCommunicator = linkedCommunicator;
        this.sourceSocket = sourceSocket;
        this.stopThis = false;
    }

    @Override
    public void run(){
        // lecture des messages entrant
        while(!this.stopThis){
            try{
                Message message = (Message) this.input.readObject();

                message.setSource(this.sourceSocket);

                this.linkedCommunicator.manageEntrantMessage(message);
            }
            catch(Exception e){
                this.stopThis = true;
                // à gérer (probablement envoyer un message simulant la mort du personnage)
            }
        }
    }

    /**
     * stoppe l'exécution du thread
     */
    public void stopReading(){
        this.stopThis = true;
    }
}