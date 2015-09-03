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
            byte[] buf;
            InetAddress address = InetAddress.getByName(hostAddress);

            Scanner sc = new Scanner(System.in);
            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");

                if (tokens[0].equals("reserve")) {
                    //TODO: implement checking of parameters for reserve, sending of command
                    //reserve <name> T|U
                    buf = tokens[0].getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
                    socket.send(packet);

                } else if (tokens[0].equals("bookSeat")) {
                    //TODO: implement checking of parameters for bookSeat, sending of command
                    //bookSeat <name> <seatNum> T|U
                    buf = tokens[0].getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
                    socket.send(packet);

                } else if (tokens[0].equals("search")) {
                    //TODO: implement checking of parameters for search, sending of command
                    //search <name> T|U
                    buf = tokens[0].getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
                    socket.send(packet);

                } else if (tokens[0].equals("delete")) {
                    //TODO: implement checking of parameters for delete, sending of command
                    //delete <name> T|U
                    buf = tokens[0].getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
                    socket.send(packet);

                } else {
                    System.out.println("ERROR: No such command");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
