package client.gui.modal;

import client.gui.records.ClientInfo;
import client.gui.utils.PacketType;


import java.io.IOException;

import java.net.*;

public class ChatModal {

    private InetAddress ip;
    public DatagramSocket socket;
    public boolean connState;
    public int id = -1;
    ClientInfo clientInfo;

    public ChatModal(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public boolean openConnection() {
        try {
            socket = new DatagramSocket();
            ip = InetAddress.getByName(clientInfo.address());
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
            connState = false;
            return connState;
        }
        connState = true;
        return connState;
    }


    public String receive() {
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);

        try {
            if(connState)
                socket.receive(packet);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return new String(packet.getData());
    }

    private byte[] generatePacket(final PacketType packetType, final String message) {
        String s = "";

        switch (packetType) {
            case STATUS -> s = "/s/" + message + "/e/";
            case CONNECT -> s = "/c/" + message + "/e/";
            case MESSAGE ->  s = "/m/" + message + "/e/";
            case DISCONNECT -> s = "/d/" + message + "/e/";
            case PING -> s = "/i/" + message + "/e/";
        }
        return s.getBytes();
    }



    public void send(final PacketType packetType,final String message) {

        Thread send = new Thread("Send") {
            public void run() {
                byte[] data = generatePacket(packetType, message);
                DatagramPacket packet = new DatagramPacket(data, data.length, ip, clientInfo.port());
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        send.start();
    }

}
