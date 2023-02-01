package yahaya_rachelle.communication.message;

import java.io.Serializable;

/**
 * représente un ip et un port
 */
public class IpMessage implements Serializable{
    private String ip;

    private int port;

    private boolean isDefined;

    public IpMessage(String ip,int port){
        this.ip = ip;
        this.port = port;
        this.isDefined = true;
    }

    public IpMessage(){
        this.isDefined = false;
    }

    public int getPort(){
        return this.port;
    }

    public String getIp(){
        return this.ip;
    }

    public boolean getIsDefined(){
        return this.isDefined;
    }
}
