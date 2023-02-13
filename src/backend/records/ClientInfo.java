package backend.records;

import java.net.InetAddress;

public class ClientInfo {

    private final String name;
    private final InetAddress address;
    private final int port;
    private final int ID;
    private int attempt = 0;

    public ClientInfo(String name, InetAddress address, int port, int ID) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.ID = ID;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public String getName() {
        return name;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getID() {
        return ID;
    }

    public int getAttempt() {
        return attempt;
    }
    public void incrementAttempt() {
        attempt++;
    }
}