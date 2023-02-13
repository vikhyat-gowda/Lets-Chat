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
    private final int MAX_ATTEMPTS = 5;

    private final List<ClientInfo> connectedClients = new ArrayList<ClientInfo>();
    private List<Integer> clientResponse = new ArrayList<>();

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
            sendToAll(PacketType.MESSAGE,message.split("/m/|/e/")[1]);
        }
        else if (message.startsWith("/d/")) {
            System.out.println(message.split("/d/|/e/")[1]);
            disconnect(Integer.parseInt(message.split("/d/|/e/")[1]), true);
        } else if (message.startsWith("/i/")) {
            clientResponse.add(Integer.parseInt(message.split("/i/|/e/")[1]));

        } else {
            System.out.println("unhandled " + message);
        }
    }

    private void sendToAll(PacketType packetType, String message) {
        for(int i = 0; i < connectedClients.size(); i++) {
            ClientInfo clientInfo = connectedClients.get(i);
            send(packetType,message, clientInfo.getAddress(), clientInfo.getPort());
        }
    }

    private void manageClients() {
        manage = new Thread("Manage") {
            public void run() {
                while (running) {
                    sendToAll(PacketType.PING, "server");
                    try {
                        Thread.sleep(5000);
                        System.out.println("Ping Started");
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < connectedClients.size(); i++) {
                        ClientInfo c = connectedClients.get(i);
                        if(!clientResponse.contains(connectedClients.get(i).getID())) {
                            if(c.getAttempt() >= MAX_ATTEMPTS) {
                                disconnect(c.getID(), false);
                            }
                            else {
                               c.incrementAttempt();
                            }
                        }else {
                            clientResponse.remove(Integer.valueOf(c.getID()));
                        }
                    }
                    System.out.println("Ping Ended");
                }
            }
        };
        manage.start();
    }


    private byte[] generatePacket(final PacketType packetType, final String message) {
        String s = "";

        switch (packetType) {
            case STATUS -> s = "/s/" + message + "/e/";
            case CONNECTION -> s = "/c/" + message + "/e/";
            case MESSAGE ->  s = "/m/" + message + "/e/";
            case PING -> s = "/i/" + message + "/e/";
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
        System.out.println("called disconnected ");
        ClientInfo c = null;
        for (int i = 0; i < connectedClients.size(); i++) {
            if(connectedClients.get(i).getID() == id) {
                c = connectedClients.get(i);
                connectedClients.remove(i);
                break;
            }
        }
        String message = "";
        if(status){
            message = String.format("Client: %s (%d) @ %s port %d disconnected", c.getName(), c.getID(), c.getAddress().toString(), c.getPort());
        }else {
            String.format("Client: %s (%d) @ %s port %d timed out", c.getName(), c.getID(), c.getAddress().toString(), c.getPort());
        }
        System.out.println(message);
    }

}
