package backend.records;

import java.net.InetAddress;

public record ClientInfo(String name, InetAddress address, int port, int ID) {

}
