package client.gui.controller;

import client.gui.modal.ChatModal;
import client.gui.records.ClientInfo;
import client.gui.utils.PacketType;
import client.gui.views.ChatView;

import java.awt.event.*;

public class ChatController {

    ClientInfo clientInfo;
    ChatView chatView;
    ChatModal chatModal;
    boolean connState = true;
    Thread listen, run;

    public ChatController(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
        chatView = new ChatView(clientInfo);
        chatModal = new ChatModal(clientInfo);

        chatView.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Dissconnet from modal");
            }
        });

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
                System.out.println("Running listerning");
                while (connState) {
                    String message = chatModal.receive();

                    if (message.startsWith("/c/")) {
                        chatModal.id = Integer.parseInt(message.split("/c/|/e/")[1]);
                        chatView.updateHistory("Successfully connected to server! " + chatModal.id);
                    }
                    else if (message.startsWith("/m/")) {
                        chatView.updateHistory(message.split("/m/|/e/",0)[1]);
                    }
                    else if (message.startsWith("/i/")) {
                        chatModal.send(PacketType.PING, String.valueOf(chatModal.id));
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
                }
            }
        }
    }

    class WindowCloseButtonEvent extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            System.out.println("Disconnected");
            chatModal.send(PacketType.DISCONNECT, String.valueOf(chatModal.id));
            connState = false;
            listen.interrupt();
        }
    }

}
