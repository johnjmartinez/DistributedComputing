import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.HashMap;

public class Server {
    // Create Data structure out here that holds the array or data structure of reservations, init on main
    // Accesses to this must be Synchronized
    SeatingData myseats;

    //Constructor for server, needs only size for seating data
    public Server(Integer size) {
        this.myseats = new SeatingData(size);
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

    System.out.println("starting server");
    //Start servers, including seating server
    Server myServer = new Server(N);
    TCPServer myTCP = new TCPServer(tcpPort, myServer.myseats);
    new Thread(myTCP).start();

    // Main loop goes here, while (true)
        try {
            System.out.println(InetAddress.getLocalHost());

            while(true) {
                DatagramSocket socket = new DatagramSocket(udpPort);
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

class TCPServer implements Runnable {
    SeatingData seatingData;
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    Integer port;
    Thread runningThread = null;
    public TCPServer(Integer tcpport, SeatingData seatserver) {
        this.seatingData = seatserver;
        this.port = tcpport;
    }

    @Override
    public void run() {
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }

        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while(true) {
            try {
                this.clientSocket = this.serverSocket.accept();
            } catch (Exception e){
                e.printStackTrace();
            }
            new Thread(new HandleRequest(clientSocket, "Multithreaded Server")).start();
        }
    }
}

class HandleRequest implements Runnable{
    Socket clientSocket;
    String serverText = null;

    HandleRequest(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }
    @Override
    public void run() {

        try {
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            output.write(("Answering").getBytes());
            input.close();
            output.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}

class SeatingData {
    final Integer seat_allocation;
    HashMap<Integer, String> reservationSystem;

    public SeatingData(Integer seats) {
        this.seat_allocation = seats;
    }
}