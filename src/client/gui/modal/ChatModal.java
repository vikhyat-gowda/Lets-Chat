package client.gui.modal;

import client.gui.records.ClientInfo;
import common.packets.PacketType;
import common.packets.ProcessPacket;


import java.io.IOException;

import java.net.*;

public class ChatModal {

    private InetAddress ip;
    public DatagramSocket socket;
    public boolean connState;
    public int token = -1;
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
            return false;
        }
        connState = true;
        return true;
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


    public void send(final PacketType packetType,final String message) {

        Thread send = new Thread("Send") {
            public void run() {
                byte[] data = ProcessPacket.encodePacket(packetType, message);
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
