import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

public class Server {
    // Create Data structure out here that holds the array or data structure of reservations, init on main
    HashMap<Integer, String> reservationSystem;

    //Constructor for server
    public Server(Integer size) {
        //Is this the right data structure? not sure right now
        reservationSystem = new HashMap<Integer, String>();
    }

    public static void main (String[] args) {
    int N;
    int tcpPort;
    int udpPort;
    if (args.length != 3) {
        System.out.println("ERROR: Provide 3 arguments");
        System.out.println("\t(1) <N>: the total number of available seats");
        System.out.println("\t\t\tassume the seat numbers are from 1 to N");
        System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
        System.out.println("\t(3) <udpPort>: the port number for UDP connection");

        System.exit(-1);
    }
    N = Integer.parseInt(args[0]);
    tcpPort = Integer.parseInt(args[1]);
    udpPort = Integer.parseInt(args[2]);

    // TODO: handle request from clients
    // TODO: make sure ports are selected correct; can UDP and TCP happen at the same time?

    Server myServer = new Server(N);
    System.out.println("starting server");

    // Main loop goes here, while (true)
        try {

            while(true) {
                DatagramSocket socket = new DatagramSocket(4445);
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received: " + received);

                //Hand data packet
                if (received.equals("reserve")) {

                } else if (received.equals("bookSeat")) {

                } else if (received.equals("search")) {

                } else if (received.equals("delete")) {

                } else  {
                    System.out.println("Not valid request");
                }
                //Parse packet

                socket.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Utilizing boolean for ease of checking in main loop
    public boolean reserve() {
        return false;
    }

    // Utilizing boolean for ease of checking in main loop
    public boolean bookSeat() {
        return false;
    }

    // Using string in this instance, as this is a read (don't care)
    public String search() {
        return "";
    }

    // Utilizing boolean for ease of checking in main loop, as it is determininstic.
    public boolean delete() {
        return false;
    }
}
