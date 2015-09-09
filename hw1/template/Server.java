import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.HashMap;

public class Server {
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

        try {
            System.out.println(InetAddress.getLocalHost());

            while(true) {
                // UDP Loop goes here
                DatagramSocket socket = new DatagramSocket(udpPort);
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received: " + received);

                //Handle data packet
                if (received.equals("reserve")) {

                } else if (received.equals("bookSeat")) {

                } else if (received.equals("search")) {

                } else if (received.equals("delete")) {

                } else  {
                    System.out.println("Not valid request");
                }

                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class TCPServer implements Runnable {
    // Main TCP Server, runs initial socket connection, then
    // looping of socket accepts that spawns client threads.
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
            //Main TCP Loop here
            try {
                this.clientSocket = this.serverSocket.accept();
            } catch (Exception e){
                e.printStackTrace();
            }
            new Thread(new HandleRequest(clientSocket, "Multithreaded Server", seatingData)).start();
        }
    }
}

class HandleRequest implements Runnable{
    // Handles each of the client sockets concurrently
    Socket clientSocket;
    String serverText = null;
    SeatingData seatingData;

    HandleRequest(Socket clientSocket, String serverText, SeatingData seatingData) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
        this.seatingData = seatingData;
    }
    @Override
    public void run() {

        try {
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            //TODO: Maybe use Bufferedinputstream here?
            String received = ""; //TODO: get data from inputstream

            output.write(("Answering").getBytes());

            if (received.equals("reserve")) {
                
            } else if (received.equals("bookSeat")) {

            } else if (received.equals("search")) {

            } else if (received.equals("delete")) {

            } else  {
                System.out.println("Not valid request");
            }

            input.close();
            output.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}

class SeatingData {
    // Main seating datastructure that allows for seat reservations.
    // Uses synchronized to ensure proper behavior with concurrent users.

    final Integer seat_allocation;
    HashMap<Integer, String> reservationSystem;

    public SeatingData(Integer seats) {
        this.seat_allocation = seats;
    }

    synchronized public boolean reserve() {
        return false;
    }

    synchronized public boolean bookSeat() {
        return false;
    }

    synchronized public String search() {
        return "";
    }

    synchronized public boolean delete() {
        return false;
    }
}