import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    public static void main (String[] args) {

        Scanner sc = new Scanner(System.in);
        int myID = sc.nextInt();
        int numServer = sc.nextInt();
        int numSeat = sc.nextInt();

        String[][] serversInfo = new String[numServer][2];
        SeatingData seatsObj = new SeatingData(numSeat);

        //parse inputs to get the ips and ports of server
        for (int i = 0; i < numServer; i++) {

            String cmd = sc.nextLine();
            String[] tokens = cmd.split(":");

            serversInfo[i][0] = tokens[0];
            serversInfo[i][1] = tokens[1];
        }

        //Start server listening, including seating object
        TCPServer myTCP = new TCPServer(serversInfo, myID, seatsObj);
        new Thread(myTCP).start();
    }

}

class TCPServer implements Runnable {

    // Main TCP Server, runs initial socket connection, then
    // looping of 'socket accepts' that spawns client threads.
    final static int TIMEOUT = 100;//ms

    int port, id;
    String[][] serversInfo;
    SeatingData seatsObj;
    ServerSocket serverSocket;
    Socket clientSckt;

    public TCPServer(String [][] info, int id, SeatingData seatsObj) {
        this.seatsObj = seatsObj;
        this.port = Integer.parseInt(info[id][1]);
        this.id = id;
        this.serversInfo = info;
    }

    public boolean synch (String addr, int port) {

        String answer = "";
        //ESTABLISH CONNECTION TO A LIVE SERVER
        try {
            Socket peerSckt = new Socket(InetAddress.getByName(addr), port);
            PrintWriter peerOut = new PrintWriter(peerSckt.getOutputStream(), true);
            BufferedReader peerIn =
                    new BufferedReader(new InputStreamReader(peerSckt.getInputStream()));

            //ESTABLISHING CONNECTION TO SERVER PEER
            peerSckt.setSoTimeout(TIMEOUT);
            peerOut.println("synch");
            answer = peerIn.readLine();

            clientSckt.close();
        }
        catch (SocketTimeoutException t) {
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String[] token = answer.split(" ");
        for (int i=0; i < token.length; i++) {
            if (token[i].equals("")) { continue; }
            String[] entry = token[i].split("=");
            seatsObj.bookSeat(entry[0], entry[1]);
        }

        return true;
    }

    @Override
    public void run() {

        for (int i = 0; i < serversInfo.length; i++) {
            if (i==id) { continue; }
            else {
                if (synch(serversInfo[i][0], Integer.parseInt(serversInfo[i][1]))) {
                    break;
                }
            }
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
                this.clientSckt = this.serverSocket.accept();
                new Thread(new HandleTCPRequest(clientSckt, seatsObj, serversInfo, id)).start();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }//END RUN()
}//END TCPSERVER CLASS

class HandleTCPRequest implements Runnable {
    //HANDLES DIFF TYPES OF REQS FROM CLIENTS OR SERVER PEERS
    final static int TIMEOUT = 100;//ms

    Socket clientSckt;
    SeatingData seatingData;
    String[][] srvInfo;
    int id;
    int[] clk;
    Queue<String> myQueue;

    HandleTCPRequest(Socket clientSckt, SeatingData seatingData, String[][] info, int id) {
        this.clientSckt = clientSckt;
        this.seatingData = seatingData;
        this.srvInfo = info;
        this.id = id;
        this.clk = new int[info.length]; //init to 0 by default
        myQueue = new LinkedList<String>();
    }

    @Override
    public void run() {

        try {
            //get streams
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSckt.getInputStream()));
            PrintWriter out = new PrintWriter(clientSckt.getOutputStream(), true);

            //get request and tokenize
            String received = in.readLine();
            String[] tokens = received.split(" ");
            clk[id]++;

            if (tokens[0].equals("client_req")) {
                out.println("ACK" + "\n");
                clk[id]++;

                received = in.readLine();
                clk[id]++;

                System.out.println("Client: " + received);
                clk[id]++;

                //LAMPORT'S -- read/writer version



                //ENTER CS
                String returnMessage = seatMaster(received);
                out.println(returnMessage + "\n");

                //LAMPORT'S REL -- synch others if write

            }
            else if (tokens[0].equals("synch")) { // SYNCH ANOTHER PEER
                clk[id]++;
                System.out.println("Server: " + received);
                out.println(seatingData.toString()+clk);
            }
            else { //LAMPORTS RESPONSES


            }

            //CLOSE ALL STREAMS AND SOCKETS
            out.close();
            in.close();
            clientSckt.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }//END RUN

    public String seatMaster (String received) {

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

        return returnMessage;
    }

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
        else if (seat_num_int < 1 || seat_num_int > seat_allocation ) {
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
        for (int i = 1; i <= seat_allocation; i++) {
            if (!res_seats.contains(i)) {
                return i;
            }
        }
        return null;
    }

    public String toString() {
        String out = "";
        List<Entry> entryList = new ArrayList<Entry>(reservationSystem.entrySet());
        for (Entry temp : entryList) {
            out += temp.toString();
            out += " ";
        }
        return out;
    }
}