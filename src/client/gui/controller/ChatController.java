package client.gui.controller;

import client.gui.modal.ChatModal;
import client.gui.records.ClientInfo;
import common.packets.PacketType;
import client.gui.views.ChatView;
import common.packets.ProcessPacket;

import java.awt.event.*;

public class ChatController {

    ClientInfo clientInfo;
    ChatView chatView;
    ChatModal chatModal;
    boolean connState = true;
    Thread listen;

    public ChatController(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
        chatView = new ChatView(clientInfo);
        chatModal = new ChatModal(clientInfo);

        chatView.updateHistory("Attempting a connection to " + clientInfo.address() + ":" + clientInfo.port() + ", user: " + clientInfo.name());

        boolean connStatus = chatModal.openConnection();
        if (!connStatus) {
            connState = false;
            chatView.updateHistory("Failed to connect to server");
        } else {
            chatView.updateHistory("Connection established");
            chatView.addSendKeyListener(new SendButtonListener());
            chatView.addWindowCloseEvent(new WindowCloseButtonEvent());

            chatModal.send(PacketType.CONNECT, String.format("%s: Connection Successful", clientInfo.name()));
            listen();
        }
    }

    public void listen() {
        listen = new Thread("listen") {
            public void run() {
                System.out.println("Running listening");
                while (connState) {
                    String message = chatModal.receive();
                    String decodedData;

                    if (message.startsWith(PacketType.CONNECT.getValue())) {
                        decodedData = ProcessPacket.decodePacket(message, PacketType.CONNECT.getValue(), PacketType.ENDCHAR.getValue());
                        chatModal.token = Integer.parseInt(decodedData);
                        chatView.updateHistory("Successfully connected to server! " + chatModal.token);
                    }
                    else if (message.startsWith(PacketType.MESSAGE.getValue())) {
                        decodedData = ProcessPacket.decodePacket(message, PacketType.MESSAGE.getValue(), PacketType.ENDCHAR.getValue());
                        chatView.updateHistory(decodedData);
                    }
                    else if (message.startsWith(PacketType.PING.getValue())) {
                        chatModal.send(PacketType.PING, String.valueOf(chatModal.token));
                        System.out.println("Processed Ping");
                    }

                 }
            }
        };
        listen.start();
    }



    class SendButtonListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                String message = chatView.getInputText();
                if (!message.equals("")) {
                    String formattedMessage = chatView.formatsMessage(chatView.getInputText());
                    chatModal.send(PacketType.MESSAGE, formattedMessage);
                    chatView.clearInputField();
                }
            }
        }
    }

    class WindowCloseButtonEvent extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            System.out.println("Disconnected");
            chatModal.send(PacketType.DISCONNECT, String.valueOf(chatModal.token));
            connState = false;
            listen.interrupt();
        }
    }

}
