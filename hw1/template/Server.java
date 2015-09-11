import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
                System.out.println("Received (UDP): " + received);
                String[] tokens = received.split(" ");
                //Handle data packet

                Integer commandReturn;
                String returnMessage;

                if (tokens[0].equals("reserve")) {

                    commandReturn = myServer.myseats.reserve(tokens[1]);
                    if (commandReturn == null) {
                        returnMessage = "Seat already booked against the name provided";
                    } else {
                        returnMessage = "Seat assigned to you is " + Integer.toString(commandReturn);
                    }
                    buf = prepareMessage(returnMessage);

                    packet = new DatagramPacket(buf, buf.length, packet.getAddress() , packet.getPort());
                    socket.send(packet);

                } else if (tokens[0].equals("bookSeat")) {
                    commandReturn = myServer.myseats.bookSeat(tokens[1], tokens[2]);
                    if (commandReturn == null) {
                        returnMessage = tokens[2] + " is not available";
                    } else {
                        //Seat assigned to you is <seat-number>
                        returnMessage = "Seat assigned to you is " + Integer.toString(commandReturn);
                    }
                    buf = prepareMessage(returnMessage);

                    packet = new DatagramPacket(buf, buf.length, packet.getAddress() , packet.getPort());
                    socket.send(packet);

                } else if (tokens[0].equals("search")) {
                    commandReturn = myServer.myseats.search(tokens[1]);
                    if (commandReturn == null) {
                        returnMessage = "No reservation found for " + tokens[1];
                    } else {
                        returnMessage = Integer.toString(commandReturn);
                    }
                    buf = prepareMessage(returnMessage);

                    packet = new DatagramPacket(buf, buf.length, packet.getAddress() , packet.getPort());
                    socket.send(packet);

                } else if (tokens[0].equals("delete")) {
                    commandReturn = myServer.myseats.delete(tokens[1]);
                    if (commandReturn == null) {
                        returnMessage = "No reservation found for " + tokens[1];
                    } else {
                        returnMessage = Integer.toString(commandReturn);
                    }
                    buf = prepareMessage(returnMessage);

                    packet = new DatagramPacket(buf, buf.length, packet.getAddress() , packet.getPort());
                    socket.send(packet);
                } else  {
                    System.out.println("Not valid request");
                }

                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static byte[] prepareMessage(String message) {
        byte[] buf;
        String var = "";
        var = var.concat(message + "\n");
        buf = var.getBytes();

        return buf;
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
            new Thread(new HandleRequest(clientSocket, seatingData)).start();
        }
    }
}

class HandleRequest implements Runnable{
    // Handles each of the client sockets concurrently
    Socket clientSocket;
    String serverText = null;
    SeatingData seatingData;

    HandleRequest(Socket clientSocket, SeatingData seatingData) {
        this.clientSocket = clientSocket;
        this.seatingData = seatingData;
    }
    @Override
    public void run() {

        try {
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            BufferedReader inputReader = new BufferedReader(new InputStreamReader(input));
            String received = inputReader.readLine();
            System.out.println("Received (TCP): " + received);
            //output.write(("Answering").getBytes());

            String[] tokens = received.split(" ");
            if (tokens[0].equals("reserve")) {

            } else if (tokens[0].equals("bookSeat")) {

            } else if (tokens[0].equals("search")) {

            } else if (tokens[0].equals("delete")) {

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
    // Main seating data structure that allows for seat reservations.
    // Uses synchronized to ensure proper behavior with concurrent users.

    final Integer seat_allocation;
    ConcurrentHashMap<String, Integer> reservationSystem;
    //

    public SeatingData(Integer seats) {
        this.seat_allocation = seats;
        reservationSystem = new ConcurrentHashMap<String, Integer>();
    }

    public Integer reserve(String name) {
        if (search(name) == null) {
            //System.out.println("No name found, free to reserve");
            Integer seat = findOpenSeat();
            //System.out.println("Writing" + name + " " + seat);
            writeSeats(name, seat, false);
            return seat;
        } else {
            return null;
        }
    }

    public Integer bookSeat(String name, String seatnum) {
        seatnum = seatnum.replace("\n", "");
        Integer seat_num_int = Integer.parseInt(seatnum);
        if (search(name) == null) {
            //System.out.println("name not reserved yet");
            if (!seatFree(seat_num_int)) {
                //System.out.println("Seat is free");
                writeSeats(name, seat_num_int, false);
                return seat_num_int;
            }
        }
        return null;
    }

    public Integer search(String name) {
        if (reservationSystem.containsKey(name)) {
            return reservationSystem.get(name);
        } else {
            return null;
        }

    }

    public Integer delete(String name) {
        if (search(name) != null) {
            Integer seat_temp = reservationSystem.get(name);
            writeSeats(name, 0, true);
            return seat_temp;
        } else {
            return null;
        }
    }

    synchronized void writeSeats(String name, Integer seatnum, boolean delete) {
        if (!delete) {
            //System.out.println(seatnum + " " + name);
            reservationSystem.put(name, seatnum);
        } else {
            reservationSystem.remove(name);
        }
    }

    boolean seatFree(Integer seatcheck) {
        Set<Integer> res_seats = new HashSet<Integer>();
        for (Integer myseatnum: reservationSystem.values()) {
            res_seats.add(myseatnum);
        }
        return res_seats.contains(seatcheck);
    }

    Integer findOpenSeat() {
        Set<Integer> res_seats = new HashSet<Integer>();
        for (Integer myseatnum: reservationSystem.values()) {
            res_seats.add(myseatnum);
        }
        for (int i = 0; i < seat_allocation; i++) {
            if (!res_seats.contains(i)) {
                return i;
            }
        }
        return null;
    }
}