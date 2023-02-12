package client.gui.views;

import client.gui.records.ClientInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ChatView extends JFrame {

    private JPanel contentPane;

    private String name, address;
    private int port;
    private JTextField txtMessage;
    private JTextArea history;
    private DefaultCaret caret;

    public ChatView(ClientInfo clientInfo) {
        setTitle("Chat Client");
        this.name = clientInfo.name();
        this.address = clientInfo.address();
        this.port = clientInfo.port();


        createWindow();
        String conn = name + "connected from " + address + ":" + port;
    }

    public void addSendKeyListener(KeyAdapter listenerForSendKey) {
        txtMessage.addKeyListener(listenerForSendKey);
    }

    public String getInputText() {
        return txtMessage.getText();
    }


    public void createWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(880, 550);
        setLocationRelativeTo(null);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{28, 815, 30, 7}; // SUM = 880
        gbl_contentPane.rowHeights = new int[]{35, 475, 40}; // SUM = 550
        gbl_contentPane.columnWeights = new double[]{1.0, 1.0};
        gbl_contentPane.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);

        history = new JTextArea();
        history.setEditable(false);
        JScrollPane scroll = new JScrollPane(history);
        caret = (DefaultCaret) history.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        GridBagConstraints scrollConstraints = new GridBagConstraints();
        scrollConstraints.insets = new Insets(0, 0, 5, 5);
        scrollConstraints.fill = GridBagConstraints.BOTH;
        scrollConstraints.gridx = 0;
        scrollConstraints.gridy = 0;
        scrollConstraints.gridwidth = 3;
        scrollConstraints.gridheight = 2;
        scrollConstraints.insets = new Insets(0, 5, 0, 0);
        contentPane.add(scroll, scrollConstraints);

        txtMessage = new JTextField();
//        txtMessage.addKeyListener(new KeyAdapter() {
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//                    formatsMessage(txtMessage.getText());
//                }
//            }
//        });
        GridBagConstraints gbc_txtMessage = new GridBagConstraints();
        gbc_txtMessage.insets = new Insets(0, 0, 0, 5);
        gbc_txtMessage.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtMessage.gridx = 0;
        gbc_txtMessage.gridy = 2;
        gbc_txtMessage.gridwidth = 2;
        contentPane.add(txtMessage, gbc_txtMessage);
        txtMessage.setColumns(10);

        JButton btnSend = new JButton("Send");
        GridBagConstraints gbc_btnSend = new GridBagConstraints();
        gbc_btnSend.insets = new Insets(0, 0, 0, 5);
        gbc_btnSend.gridx = 2;
        gbc_btnSend.gridy = 2;
        contentPane.add(btnSend, gbc_btnSend);

        setVisible(true);

        txtMessage.requestFocusInWindow();
    }

    public String formatsMessage(String message) {
        message = name + ": " + message;
        txtMessage.setText("");
        return message;
    }

    public void updateHistory(String message) {
        history.append(message + "\n\r");
        history.setCaretPosition(history.getDocument().getLength());
    }

}
