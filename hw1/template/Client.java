import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Client {
    public static void main (String[] args) {
        String hostAddress;
        int tcpPort;
        int udpPort;

        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <hostAddress>: the address of the server");
            System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(3) <udpPort>: the port number for UDP connection");
            System.exit(-1);
        }

        hostAddress = args[0];
        tcpPort = Integer.parseInt(args[1]);
        udpPort = Integer.parseInt(args[2]);
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buf = new byte[256];
            InetAddress address = InetAddress.getByName(args[0]);
            //DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
            //socket.send(packet); //this is what you do

            Scanner sc = new Scanner(System.in);
            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");

                if (tokens[0].equals("reserve")) {
                    buf = tokens[0].getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
                    socket.send(packet);
                    socket.close();
                    // TODO: send appropriate command to the server and display the
                    // appropriate responses form the server
                } else if (tokens[0].equals("bookSeat")) {
                    // TODO: send appropriate command to the server and display the
                    // appropriate responses form the server
                } else if (tokens[0].equals("search")) {
                    // TODO: send appropriate command to the server and display the
                    // appropriate responses form the server
                } else if (tokens[0].equals("delete")) {
                    // TODO: send appropriate command to the server and display the
                    // appropriate responses form the server
                } else {
                    System.out.println("ERROR: No such command");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
