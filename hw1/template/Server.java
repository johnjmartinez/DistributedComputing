import java.net.DatagramSocket;

public class Server {
    // Create Data structure out here that holds the array or data structure of reservations, init on main
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
    // Get ports
    // Make data structure
    // Main loop goes here, while (true)
        try {
            while(true) {
                DatagramSocket datasocket = new DatagramSocket(udpPort);
                //Hand data packet

                //Parse packet


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
