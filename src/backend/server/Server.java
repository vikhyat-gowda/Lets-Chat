package backend.server;

import backend.records.ClientInfo;
import backend.utils.UniqueIdentifier;
import common.packets.PacketType;
import common.packets.ProcessPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server implements Runnable{

    private final int port;
    private DatagramSocket socket;
    private boolean running = false;
    private final int MAX_ATTEMPTS = 5;
    private boolean raw = false;

    private final List<ClientInfo> connectedClients = new ArrayList<>();
    private final List<Integer> clientResponse = new ArrayList<>();

    public Server(int port) {
        this.port = port;

        try {
            socket = new DatagramSocket(this.port);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
        Thread run = new Thread(this, "Server");
        run.start();
    }

    @Override
    public void run() {
        running = true;
        System.out.println("Server Started on port " + port);
        manageClients();
        receive();
        Scanner scanner = new Scanner(System.in);

        while (running) {
            String text = scanner.nextLine();
            if(!text.startsWith("/")) {
                sendToAll(PacketType.MESSAGE, "Server: " + text);
                continue;
            }
            text = text.substring(1);
            if(text.equals("raw"))
                raw = !raw;
            else if (text.equals("list")) {
                System.out.println("================\nClients");
                for (int i =0; i < connectedClients.size(); i++) {
                    ClientInfo c = connectedClients.get(i);
                    System.out.printf("Name: %s Id: %d Address: %s Port: %d\n", c.getName(), c.getID(),c.getAddress().toString(), c.getPort());
                }
                System.out.println("================");
            }
            else if (text.startsWith("kick")) {
                System.out.println("here1");
                String name = text.split(" ")[1];
                boolean isNum = true;
                int id = -1;

                try {
                    System.out.println("here2");
                    System.out.println("text "+ name);
                    id = Integer.parseInt(name);
                }catch (NumberFormatException e) {
                    System.out.println("here3");
                    isNum = false;
                }

                if(isNum) {
                    System.out.println("here4");
                    boolean exist = false;
                    for (int i = 0; i < connectedClients.size(); i++) {
                        if(connectedClients.get(i).getID() == id){
                            exist = true;
                            break;
                        }
                    }
                    if(exist) {
                        System.out.println("here5");
                        disconnect(id, true);
                    }else {
                        System.out.println("Client ID "+ id + " doesn't exit");
                    }
                }
                else  {
                    for (int i = 0; i < connectedClients.size(); i++) {
                       ClientInfo c = connectedClients.get(i);
                       if(name.equals(c.getName())) {
                           disconnect(c.getID(), true);
                           break;
                       }
                    }
                }
            }
        }
    }

    private void receive() {
        Thread receive = new Thread("Receive") {
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
        String decodedData;

//        Log packets based on server command
        if (raw) System.out.println(message);

        if(message.startsWith(PacketType.CONNECT.getValue())) {
            int id = UniqueIdentifier.getIdentifier();
            decodedData = ProcessPacket.decodePacket(message, PacketType.CONNECT.getValue(),":",PacketType.ENDCHAR.getValue());
            connectedClients.add(new ClientInfo(decodedData, packet.getAddress(), packet.getPort(), id));
            send(PacketType.CONNECT,Integer.toString(id) , packet.getAddress(), packet.getPort());
            System.out.printf("New Client %s connected from %s:%d", decodedData, packet.getAddress().toString(), packet.getPort());
        }
        else if (message.startsWith(PacketType.MESSAGE.getValue())) {
            decodedData = ProcessPacket.decodePacket(message, PacketType.MESSAGE.getValue(), PacketType.ENDCHAR.getValue());
            sendToAll(PacketType.MESSAGE, decodedData);
        }
        else if (message.startsWith(PacketType.DISCONNECT.getValue())) {
            decodedData = ProcessPacket.decodePacket(message, PacketType.DISCONNECT.getValue(), PacketType.ENDCHAR.getValue());
            disconnect(Integer.parseInt(decodedData), true);
        } else if (message.startsWith(PacketType.PING.getValue())) {
            decodedData = ProcessPacket.decodePacket(message, PacketType.PING.getValue(), PacketType.ENDCHAR.getValue());
            clientResponse.add(Integer.parseInt(decodedData));
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
        Thread manage = new Thread("Manage") {
            public void run() {
                while (running) {
                    sendToAll(PacketType.PING, "server");
                    try {
                        Thread.sleep(5000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < connectedClients.size(); i++) {
                        ClientInfo c = connectedClients.get(i);
                        if (!clientResponse.contains(connectedClients.get(i).getID())) {
                            if (c.getAttempt() >= MAX_ATTEMPTS) {
                                disconnect(c.getID(), false);
                            } else {
                                c.incrementAttempt();
                            }
                        } else {
                            clientResponse.remove(Integer.valueOf(c.getID()));
                        }
                    }
                }
            }
        };
        manage.start();
    }


    private void send(PacketType packetType, final String message, final InetAddress address, final int port) {
        Thread send = new Thread() {
            public void run() {
                byte[] data = ProcessPacket.encodePacket(packetType, message);

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

        boolean exist = false;

        for (int i = 0; i < connectedClients.size(); i++) {
            if(connectedClients.get(i).getID() == id) {
                c = connectedClients.get(i);
                connectedClients.remove(i);
                exist = true;
                break;
            }
        }

        String message;

        if(!exist) return;
        if (status) {
            message = String.format("Client: %s (%d) @ %s port %d disconnected", c.getName(), c.getID(), c.getAddress().toString(), c.getPort());
        } else {
            message = String.format("Client: %s (%d) @ %s port %d timed out", c.getName(), c.getID(), c.getAddress().toString(), c.getPort());
        }

        System.out.println(message);
    }
}
