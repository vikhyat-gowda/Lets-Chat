package backend;

import backend.server.Server;

public class Main {
    public static void main(String[] args) {

        if(args.length != 1) {
            System.out.println("Provide port number as command line argument: ");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new Server(port);
    }
}
