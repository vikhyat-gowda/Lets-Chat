package client.gui.controller;

import client.gui.modal.LoginModal;
import client.gui.records.ClientInfo;
import client.gui.views.LoginView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainController {


    ClientInfo clientInfo;

    private final LoginView loginView = new LoginView();

    private final LoginModal loginModal = new LoginModal();


    public MainController() {
        loginView.createWindow();
        loginView.addLoginSubmitListener(new LoginFormSubmitListener());
    }

    private void switchWindowToChat() {
        new ChatController(clientInfo);
    }

    class LoginFormSubmitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            clientInfo = loginView.getClientInfo();
            boolean loginState = loginModal.loginUser(clientInfo);
            System.out.println("Login Form Submitted " + clientInfo);

            if(!loginState) {
                System.out.println("Invalid Cred");
                return;
            }

            // If login Successful
            loginView.dispose();

            switchWindowToChat();
        }
    }
}
