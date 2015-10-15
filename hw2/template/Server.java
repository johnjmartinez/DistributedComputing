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
        myTCP.start();
    }

}


class TCPServer implements Runnable {
    // Main TCP Server, runs initial socket connection, then
    // looping of 'socket accepts' that spawns client threads.
    final static int TIMEOUT = 100;//ms
    
    class Node {
    	String T;
    	Integer id, clk;
    	Node(String T, Integer id, Integer clk) {
    		this.T=T; this.id=id; this.clk=clk;
    	}    	
    }

    Integer ID, port;
    Integer[] CLK;
    Queue<Node> Q;

    String[][] serversInfo;
    SeatingData seatsObj;
    ServerSocket serverSocket;
    Socket reqSckt;

    public TCPServer(String [][] info, int id, SeatingData seatsObj) {
        this.seatsObj = seatsObj;
        this.port = Integer.parseInt(info[id][1]);
        this.serversInfo = info;
        this.ID = id;
        this.CLK = new int[info.length]; //init to 0 by default
        this.Q = new LinkedList<String>();
        this.CLK[ID]++;
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
            peerOut.println("server_msg synch S "+ID.toString()+" "+CLK[ID].toString());
            answer = peerIn.readLine();

            clientSckt.close();
        }
        catch (SocketTimeoutException t) {
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String[] data = answer.split("::");
        CLK[ID] = Integer.max(CLK[ID], Integer.parseInt(data[1]);
        
        String[] tokens = data[0].split(" ")
        for (int i=0; i < tokens.length; i++) {
            if (token[i].equals("")) { continue; }
            String[] entry = token[i].split("=");
            seatsObj.bookSeat(entry[0], entry[1]);
        }

        return true;
    }
    
    @Override
    void run() {
    	
    	//SYNCH FIRST
        for (int i = 0; i < serversInfo.length; i++) {
            if (i==id) { continue; }
            else {
                if (synch(serversInfo[i][0], Integer.parseInt(serversInfo[i][1]))) {
                    break;
                }
            }
        }
        //START SERVER SOCKET
        try {
            this.serverSocket = new ServerSocket(this.port);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //MAIN TCP LOOP -- LISTENING
        while(true) {
            try {
                //Accept connection, spawn new thread
                this.reqSckt = this.serverSocket.accept();
                new Thread(new Handler(reqSckt, seatsObj, serversInfo, CLK, ID, Q)).start();
            }
            catch (Exception e) {
            	
            }
        }
    }
    
}

class Handler implements Runnable {
	
    Integer ID;
    Integer[] CLK;
    Queue<Node> Q;
    String changeState; // either FALSE or [A|D]:seatNum:name
    String[][] serversInfo;
    SeatingData seatsObj;
    Socket sckt;

    public Handler(Socket sckt, SeatingData seatsObj, String [][] info, Integer[] clk, Integer id, Queue q ) {
        this.seatsObj = seatsObj;
        this.serversInfo = info;
        this.ID = id;
        this.CLK =clk;
        this.Q = q;
        this.sckt = sckt;
    }
	
	void run() {
	    String TAG = 'W';

	    try {
	        //get streams
	        BufferedReader in = new BufferedReader(new InputStreamReader(Sckt.getInputStream()));
	        PrintWriter out = new PrintWriter(Sckt.getOutputStream(), true);

	        //get request and tokenize
	        String received = in.readLine();
	        String[] tokens = received.split(" "); 
            System.out.println("Server: "+received);
	      
	        //CLIENT REQ
	        if (tokens[0].equals("client_req")) {
	          
	            out.println("ACK\n");
	            received = in.readLine();
	            System.out.println("Client: " + received);

	            tokens = received.split(" ");
	    		if (tokens[0].equals("search")) { TAG='R'; }
	            answerClient(tokens, TAG); 
	        }
	        //SERVER PEER MSG
	        else if (tokens[0].equals("server_msg")){
	        	answerPeer(tokens);
	        }
	        //CLOSE ALL STREAMS AND SOCKETS
	        out.close();
	        in.close();
	        clientSckt.close();
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	synchronized void answerClient(String[] tokens, String TAG) {
        //LAMPORT'S REQ_CS
        CLK[ID]++;
        Q.add(new Node(TAG, ID, CLK[ID]));
        broadcast("reqcs", TAG, ID, CLK[ID]);
      
        while (!Q.okCS(ID)) {;}
        //ENTER CS
        String returnMessage = seatMaster(received);
        out.println(returnMessage + "\n");

        //LAMPORT'S REL_CS -- synch others if write
        //use TAG to synch [A|D]:seatNum:name
        if (!TAG.equals('R') && !changeState.equals("FALSE")) {
            TAG = changeState;
        }
        broadcast("relcs", TAG, ID, CLK[ID]);
        Q.clear();	
	}
	
	synchronized void answerPeer(String[] tokens) {
        //CLK[rid] = (CLK[rid] >= rclk) ? CLK[rid] : rclk;
        //CLK[ID]  = (CLK[ID] >= rclk)  ? CLK[ID] : rclk;
        CLK[rid] = Integer.max(CLK[rid], rclk);
        CLK[ID] = Integer.max(CLK[ID], rclk);
        CLK[ID]++;
        
	    String msg  = tokens[1]; //MSG
	    String TAG  = tokens[2]; //TAG
	    Integer rid  = Integer.parseInt(tokens[3]); //RID
	    Integer rclk = Integer.parseInt(tokens[4]); //RCLK_VALUE
                      
        //SYNCH ANOTHER PEER
        if (tokens[1].equals("synch")) { 
            String rStr ="::"+CLK[rid].toString();
            out.println(seatingData.toString()+rStr); //update peer data + clk
        }
        //LAMPORTS RESPONSES
        else if (tokens[1].equals("reqcs")) { 
            Q.add(TAG, rid, CLK[rid])
            send("ACK", rid, CLK[rid]);
        }
        else if (tokens[1].equals("relcs")) { 
            Q.remove(rid);
        }
	}
	 
	 
    public String seatMaster (String received) {

        String[] tokens = received.split(" ");

        //Handle data packet as requested by client
        Integer commandReturn = null;
        String returnMessage = "ERROR occurred; try again";
        changeState = "FALSE";

        if (tokens[0].equals("reserve")) {
            commandReturn = seatingData.reserve(tokens[1]);
            if (commandReturn == null) {
                returnMessage = "Seat already booked against the name provided";
            }
            else {
                returnMessage = "Seat assigned to " + tokens[1] + " is " 
                		+ Integer.toString(commandReturn);
                changeState = "A:"+Integer.toString(commandReturn)+":"tokens[1];
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
            else { //Seat assigned to <name> is <seat-number>
                returnMessage = "Seat assigned to " + tokens[1] + " is "
                        + Integer.toString(commandReturn);
                changeState = "A:"+Integer.toString(commandReturn)+":"tokens[1];
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
                changeState = "D:"+Integer.toString(commandReturn)+":"tokens[1];
            }
        }
        else  {
            System.out.println("Not valid request\t"+received);
        }
        return returnMessage;
    }
	
}

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

    synchronized public String toString() {
        String out = "";
        List<Entry> entryList = new ArrayList<Entry>(reservationSystem.entrySet());
        for (Entry temp : entryList) {
            out += temp.toString();
            out += " ";
        }
        return out;
    }
}
