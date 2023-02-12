package client.gui.views;

import client.gui.records.ClientInfo;

import javax.swing.*;

import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


// custom package


public class LoginView extends JFrame {

    private JLabel lblPort;
    private JLabel lblPortDesc;
    private JLabel lblIpAddress;
    private JLabel lblAddressDesc;

    private JTextField txtName;
    private JTextField txtPort;
    private JTextField txtAddress;

    private JButton btnLogin;

    private JPanel contentPane;

    public ClientInfo getClientInfo() {
        return new ClientInfo(txtName.getText(), txtAddress.getText(),Integer.parseInt(txtPort.getText()));
    }

    public void addLoginSubmitListener(ActionListener listenForSubmitButton) {
        btnLogin.addActionListener(listenForSubmitButton);
    }

    public void createWindow() {
        setResizable(false);
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 380);
        setLocationRelativeTo(null);


        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        txtName = new JTextField();
        txtName.setBounds(67, 50, 165, 28);
        contentPane.add(txtName);
        txtName.setColumns(10);

        JLabel lblName = new JLabel("Name:");
        lblName.setBounds(127, 34, 45, 16);
        contentPane.add(lblName);

        txtAddress = new JTextField();
        txtAddress.setBounds(67, 116, 165, 28);
        contentPane.add(txtAddress);
        txtAddress.setColumns(10);

        lblIpAddress = new JLabel("IP Address:");
        lblIpAddress.setBounds(111, 96, 77, 16);
        contentPane.add(lblIpAddress);

        txtPort = new JTextField();
        txtPort.setColumns(10);
        txtPort.setBounds(67, 191, 165, 28);
        contentPane.add(txtPort);

        lblPort = new JLabel("Port:");
        lblPort.setBounds(133, 171, 34, 16);
        contentPane.add(lblPort);

        lblAddressDesc = new JLabel("(eg. 192.168.0.2)");
        lblAddressDesc.setBounds(94, 142, 112, 16);
        contentPane.add(lblAddressDesc);

        lblPortDesc = new JLabel("(eg. 8192)");
        lblPortDesc.setBounds(116, 218, 68, 16);
        contentPane.add(lblPortDesc);

        btnLogin = new JButton("Login");
        btnLogin.setBounds(91, 311, 117, 29);
        contentPane.add(btnLogin);

        setVisible(true);
    }

}
