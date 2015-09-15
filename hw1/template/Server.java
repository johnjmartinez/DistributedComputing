import java.io.*;
import java.lang.Integer;
import java.net.*;
import java.util.Set;
import java.util.HashSet;
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
            System.out.println("\t         assume the seat numbers are from 1 to N");
            System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(3) <udpPort>: the port number for UDP connection");

            System.exit(-1);
        }

        N = Integer.parseInt(args[0]);
        tcpPort = Integer.parseInt(args[1]);
        udpPort = Integer.parseInt(args[2]);

        // TODO: handle request from clients
        System.out.println("Starting server -- seats from 0 to "+ Integer.toString(N-1));

        //Start servers, including seating server
        Server myServer = new Server(N);
        TCPServer myTCP = new TCPServer(tcpPort, myServer.myseats);
        new Thread(myTCP).start();

        try {
            System.out.println(InetAddress.getLocalHost());

            // UDP Loop goes here
            while(true) {
                //receive packet and buf reused for transmit
                byte[] buf = new byte[256];
                DatagramSocket socket = new DatagramSocket(udpPort);
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                //Receive packet, tokenize.
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received (UDP): " + received);
                String[] tokens = received.split(" ");

                //Handle data packet as requested by client
                Integer commandReturn = null;
                String returnMessage = "ERROR occurred; try again";

                if (tokens[0].equals("reserve")) {
                    commandReturn = myServer.myseats.reserve(tokens[1]);
                    if (commandReturn == null) {
                        returnMessage = "Seat already booked against the name provided";
                    }
                    else {
                        returnMessage = "Seat assigned to you is " + Integer.toString(commandReturn);
                    }
                }
                else if (tokens[0].equals("bookSeat")) {
                    commandReturn = myServer.myseats.bookSeat(tokens[1], tokens[2]);
                    if (commandReturn == null) {
                        returnMessage = tokens[2] + " is not available";
                    }
                    else if (commandReturn.equals(Integer.MIN_VALUE)) {
                        returnMessage = "Seat already booked against the name provided";
                    }
                    else { //Seat assigned to you is <seat-number>
                        returnMessage = "Seat assigned to " + tokens[1] + " is "
                                + Integer.toString(commandReturn);                    }
                }
                else if (tokens[0].equals("search")) {
                    commandReturn = myServer.myseats.search(tokens[1]);
                    if (commandReturn == null) {
                        returnMessage = "No reservation found for " + tokens[1];
                    }
                    else {
                        returnMessage = Integer.toString(commandReturn);
                    }
                }
                else if (tokens[0].equals("delete")) {
                    commandReturn = myServer.myseats.delete(tokens[1]);
                    if (commandReturn == null) {
                        returnMessage = "No reservation found for " + tokens[1];
                    }
                    else {
                        returnMessage = Integer.toString(commandReturn);
                    }
                }
                else  {
                    System.out.println("Not valid request\t"+received);
                }

                //send response packet
                returnMessage += "\n";
                buf = returnMessage.getBytes();
                packet = new DatagramPacket(buf, buf.length, packet.getAddress() , packet.getPort());
                socket.send(packet);

                socket.close();
            } //END WHILE TRUE --- UDP LOOP
        } //END TRY
        catch (Exception e) {
            e.printStackTrace();
        }
    }//END MAIN
}// END SERVER CLASS

class TCPServer implements Runnable {
    // Main TCP Server, runs initial socket connection, then
    // looping of 'socket accepts' that spawns client threads.

    SeatingData seatingData;
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    Integer port;
    Thread runningThread = null;

    public TCPServer(Integer tcpport, SeatingData seatserver) {
        this.seatingData = seatserver; //aka -- myServer.myseats
        this.port = tcpport;
    }

    @Override
    public void run() {
        //JJM --- not sure what's this for.
        synchronized(this) {
            this.runningThread = Thread.currentThread();
        }

        try {
            this.serverSocket = new ServerSocket(this.port);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //Main TCP Loop here
        while(true) {
            try {
                //Accept connection, spawn new thread
                this.clientSocket = this.serverSocket.accept();
                new Thread(new HandleTCPRequest(clientSocket, seatingData)).start();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }//END RUN()
}//END TCPSERVER CLASS

class HandleTCPRequest implements Runnable{
    // Handles each of the client sockets concurrently
    Socket clientSocket;
    SeatingData seatingData;
    //String serverText = null;// --- for?

    HandleTCPRequest(Socket clientSocket, SeatingData seatingData) {
        this.clientSocket = clientSocket;
        this.seatingData = seatingData; //aka -- myServer.myseats
    }

    @Override
    public void run() {

        try {
            //get streams
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            //DEBUG? -- output.write(("Answering").getBytes());

            //get request and tokenize
            String received = in.readLine();
            System.out.println("Received (TCP): " + received);
            String[] tokens = received.split(" ");

            //Handle data packet as requested by client
            Integer commandReturn = null;
            String returnMessage = "ERROR occurred; try again";

            if (tokens[0].equals("reserve")) {
                commandReturn = seatingData.reserve(tokens[1]);
                if (commandReturn == null) {
                    returnMessage = "Seat already booked against the name provided";
                }
                else {
                    returnMessage = "Seat assigned to you is " + Integer.toString(commandReturn);
                }
            }
            else if (tokens[0].equals("bookSeat")) {
                commandReturn = seatingData.bookSeat(tokens[1], tokens[2]);
                if (commandReturn == null) {
                    returnMessage = tokens[2] + " is not available";
                }
                else if (commandReturn.equals(Integer.MIN_VALUE)) {
                    returnMessage = "Seat already booked against the name provided";
                }
                else { //Seat assigned to you is <seat-number>
                    returnMessage = "Seat assigned to " + tokens[1] + " is "
                            + Integer.toString(commandReturn);
                }
            }
            else if (tokens[0].equals("search")) {
                commandReturn = seatingData.search(tokens[1]);
                if (commandReturn == null) {
                    returnMessage = "No reservation found for " + tokens[1];
                }
                else {
                    returnMessage = Integer.toString(commandReturn);
                }
            }
            else if (tokens[0].equals("delete")) {
                commandReturn = seatingData.delete(tokens[1]);
                if (commandReturn == null) {
                    returnMessage = "No reservation found for " + tokens[1];
                }
                else {
                    returnMessage = Integer.toString(commandReturn);
                }
            }
            else  {
                System.out.println("Not valid request\t"+received);
            }

            out.println(returnMessage+"\n");

            //CLOSE ALL STREAMS AND SOCKET
            out.close();
            in.close();
            clientSocket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }//END RUN
}//END HANDLER CLASS

class SeatingData {
    // Main seating data structure that allows for seat reservations.
    // Uses synchronized to ensure proper behavior with concurrent users.

    final Integer seat_allocation;
    ConcurrentHashMap<String, Integer> reservationSystem;

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
        }
        else {
            return null;
        }
    }

    public Integer bookSeat(String name, String seatnum) {
        seatnum = seatnum.replace("\n", "");
        Integer seat_num_int = -1;
        try {
            seat_num_int = Integer.parseInt(seatnum);
        }
        catch (NumberFormatException n) {
            System.out.println("seatnum not valid - "+seatnum);
            return null;
        }

        if (search(name) != null) {
            return Integer.MIN_VALUE;
        }
        else if (seat_num_int < 0 || seat_num_int >= seat_allocation ) {
            return null;
        }
        else { //search(name) == null
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
        }
        else {
            return null;
        }
    }

    public Integer delete(String name) {
        if (search(name) != null) {
            Integer seat_temp = reservationSystem.get(name);
            writeSeats(name, 0, true);
            return seat_temp;
        }
        else {
            return null;
        }
    }

    synchronized void writeSeats(String name, Integer seatnum, boolean delete) {
        if (!delete) {
            //System.out.println(seatnum + " " + name);
            reservationSystem.put(name, seatnum);
        }
        else {
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