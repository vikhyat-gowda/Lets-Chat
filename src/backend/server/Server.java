package backend.server;

import backend.records.ClientInfo;
import backend.utils.UniqueIdentifier;
import backend.utils.PacketType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable{

    private final int port;
    private DatagramSocket socket;
    private boolean running = false;
    private Thread run;
    private Thread manage;
    private Thread receive;

    private final List<ClientInfo> connectedClients = new ArrayList<ClientInfo>();

    public Server(int port) {
        this.port = port;

        try {
            socket = new DatagramSocket(this.port);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
        run = new Thread(this, "Server");
        run.start();
    }

    @Override
    public void run() {
        running = true;
        System.out.println("Server Started on port " + port);
        manageClients();
        receive();
    }

    private void receive() {
        receive = new Thread("Receive") {
            public void run() {
                while (running) {
                    byte[] data = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(data, data.length);

                    try {
                        socket.receive(packet);
                    } catch (IOException e) {
                       e.printStackTrace();
                       return;
                    }
                    processPacket(packet);
                }
            }
        };
        receive.start();
    }

    private void processPacket(DatagramPacket packet) {

        String message = new String(packet.getData());
        if(message.startsWith("/c/")) {
            int id = UniqueIdentifier.getIdentifier();
            connectedClients.add(new ClientInfo(message.split("/c/|:|/e/", 0)[1], packet.getAddress(), packet.getPort(), id));
            System.out.printf("New Client %s connected", message.split("/c/|:|/e/", 0)[1]);
            send(PacketType.CONNECTION,Integer.toString(id) , packet.getAddress(), packet.getPort());
        }
        else if (message.startsWith("/m/")) {
            System.out.println(message.split("/m/|/e/")[1]);
            setToAll(message.split("/m/|/e/")[1]);
        }
        else if (message.startsWith("/d/")) {
            System.out.println(message.split("/d/|/e/")[1]);
            disconnect(Integer.parseInt(message.split("/d/|/e/")[1]), true);
        }
        else {
            System.out.println("unhandled " + message);
        }
    }

    private void setToAll(String message) {
        System.out.println("Indie sent to all");
        for(int i = 0; i < connectedClients.size(); i++) {
            ClientInfo clientInfo = connectedClients.get(i);
            send(PacketType.MESSAGE,message, clientInfo.address(), clientInfo.port());
        }
    }

    private void manageClients() {
    }


    private byte[] generatePacket(final PacketType packetType, final String message) {
        String s = "";

        switch (packetType) {
            case STATUS -> s = "/s/" + message + "/e/";
            case CONNECTION -> s = "/c/" + message + "/e/";
            case MESSAGE ->  s = "/m/" + message + "/e/";
        }
        return s.getBytes();
    }

    private void send(PacketType packetType, final String message, final InetAddress address, final int port) {
        Thread send = new Thread() {
            public void run() {

                byte[] data = generatePacket(packetType, message);

                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        send.start();
    }

    private void disconnect(int id, boolean status) {
        System.out.println("called disconneceted ");
        ClientInfo c = null;
        for (int i = 0; i < connectedClients.size(); i++) {
            if(connectedClients.get(i).ID() == id) {
                c = connectedClients.get(i);
                connectedClients.remove(i);
                break;
            }
        }
        String message = "";
        if(status){
            message = String.format("Client: %s (%d) @ %s port %d disconnected", c.name(), c.ID(), c.address().toString(), c.port());
        }else {
            String.format("Client: %s (%d) @ %s port %d timed out", c.name().split(":"), c.ID(), c.address().toString(), c.port());
        }
        System.out.println(message);
    }

}
