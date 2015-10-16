import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Server {

    public static void main (String[] args) {

        Scanner sc = new Scanner(System.in);

        //PER ASSIGNMENT -- "The first line of the input to a server contains three natural numbers separated by a single whitespace"
        String cmd = sc.nextLine();
        String[] tokens = cmd.split(" ");
        int myID = Integer.parseInt(tokens[0])-1;     // NOT WORKING = sc.nextInt();
        int numServer = Integer.parseInt(tokens[1]);  // NOT WORKING = sc.nextInt();
        int numSeat = Integer.parseInt(tokens[2]);    // NOT WORKING = sc.nextInt();

        String[][] serversInfo = new String[numServer][2];

        //parse inputs to get the ips and ports of server
        for (int i = 0; i < numServer; i++) {
            cmd = sc.nextLine();
            tokens = cmd.split(":");

            serversInfo[i][0] = tokens[0];
            serversInfo[i][1] = tokens[1];
        }

        System.out.println("\nStarting ...");
        sc.close();

        //Start server listening, including seating object
        SeatingData seatsObj = new SeatingData(numSeat);
        TCPServer myTCP = new TCPServer(serversInfo, myID, seatsObj);
        new Thread(myTCP).start();
    }

}


class TCPServer implements Runnable {
    // Main TCP Server, runs initial socket connection, then
    // looping of 'socket accepts' that spawns client threads.
    final static int TIMEOUT = 100;//ms
    
    Integer ID, port;
    Integer[] CLK, Q;
    String[][] serversInfo;
    SeatingData seatsObj;
    ServerSocket serverSocket = null;
    Socket reqSckt = null;
    Thread runningThread = null;
    ExecutorService pool;

    public TCPServer(String [][] info, int id, SeatingData seatsObj) {
        this.seatsObj = seatsObj;
        this.port = Integer.parseInt(info[id][1]);
        this.serversInfo = info;
        this.ID = id;
        this.CLK = new Integer[info.length];
        this.Q = new Integer[info.length];
        for (int i=0; i<info.length; i++) {
        	Q[i]=Integer.MAX_VALUE;
            CLK[i]=0;
        }
        CLK[ID]++;
        pool = Executors.newFixedThreadPool(5);
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
            peerOut.println("server_msg synch "+ID.toString()+" "+CLK[ID].toString());

            while(!peerIn.ready()) {;}
            answer = peerIn.readLine();
            System.out.println(" "+answer);
            peerSckt.close();
        }
        catch (SocketTimeoutException t) {
            return false;
        }
        catch (Exception e) {
            return false;
        }

        String[] data = answer.split("::");
        CLK[ID] = Integer.max(CLK[ID], Integer.parseInt(data[1]));
        
        String[] tokens = data[0].split(" ");
        for (int i=0; i < tokens.length; i++) {
            if (tokens[i].equals("")) { continue; }
            String[] entry = tokens[i].split("=");
            seatsObj.bookSeat(entry[0], entry[1]);
        }

        return true;
    }
    
    @Override
    public void run() {

        synchronized(this) {
            this.runningThread = Thread.currentThread();
        }


    	//SYNCH FIRST
        System.out.println("Synching ...");
        for (int i = 0; i < serversInfo.length; i++) {
            if (i==ID) { continue; }
            else {
                if (synch(serversInfo[i][0], Integer.parseInt(serversInfo[i][1]))) {
                    break;
                }
            }
        }
        //START SERVER SOCKET
        System.out.println("Listening ...\n");
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
                pool.execute(new Handler(reqSckt, seatsObj, serversInfo, CLK, ID, Q));
                //new Thread(new Handler(reqSckt, seatsObj, serversInfo, CLK, ID, Q)).start();
            }
            catch (Exception e) {
                pool.shutdown();
            }
        }
    }
    
}

class Handler implements Runnable {
	
    Integer ID;
    Integer[] CLK, Q;
    String changeState; // either FALSE or [A|D]:seatNum:name
    String[][] serversInfo;
    SeatingData seatsObj;
    private final Socket sckt;

    public Handler(Socket sckt, SeatingData seatsObj, String [][] info, Integer[] clk, Integer id, Integer[] q ) {
        this.seatsObj = seatsObj;
        this.serversInfo = info;
        this.ID = id;
        this.CLK =clk;
        this.Q = q;
        this.sckt = sckt;
    }
	
    @Override
    public void run() {
		
	    try {
	        //get streams
	        BufferedReader in = new BufferedReader(new InputStreamReader(sckt.getInputStream()));
            PrintWriter out = new PrintWriter(sckt.getOutputStream(), true);

	        //get request and tokenize
            while(!in.ready()) {;}
	        String received = in.readLine();
	        String[] tokens = received.split(" "); 
            System.out.println("Server: "+received);
	      
	        //CLIENT REQ
	        if (tokens[0].equals("client_req")) {
	            out.println("ACK\n");
                out.flush();
                while(!in.ready()) {;}
	            received = in.readLine();
	            System.out.println("Client: " + received);

	            reqCS();

                while (!okCS()) {Thread.yield();} //WAIT
                //ENTER CS
                String returnMessage = seatMaster(received);
                System.out.println("Server: "+returnMessage);
                out.println("~ " + returnMessage + "\n");
                out.flush();

                relCS();
	        }
	        //SERVER PEER MSG
	        else if (tokens[0].equals("server_msg")){
	        	answerPeer(tokens, out);
	        }
	        //CLOSE ALL STREAMS AND SOCKETS
	        sckt.close();
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }

	}
	
	synchronized void reqCS() {
        //LAMPORT'S REQ_CS
        CLK[ID]++;
        Q[ID] = CLK[ID];
        broadcast("reqcs", ID, CLK[ID]);
    }

    boolean okCS() {
        for (int x=0; x<serversInfo.length; x++) {
            if ((Q[ID] > Q[x]) || ((Q[ID] == Q[x]) && (ID > x))) {
                return false;
            }
            if ((Q[ID] > CLK[x]) || ((Q[ID] == CLK[x]) && (ID > x))) {
                return false;
            }
        }
        return true;
    }

    synchronized void relCS() {
        //LAMPORT'S REL_CS -- synch others if write
        //use changeState to synch [A|D]:seatNum:name (if not FALSE)
        Q[ID] = Integer.MAX_VALUE;	
        broadcast("relcs#"+changeState, ID, CLK[ID]);
	}
	
	synchronized void answerPeer(String[] tokens, PrintWriter resp) {
	    String msg   = tokens[1]; //MSG
	    Integer rid  = Integer.parseInt(tokens[2]); //RID
	    Integer rclk = Integer.parseInt(tokens[3]); //RCLK_VALUE
	    
	    CLK[rid] = Integer.max(CLK[rid], rclk);
        CLK[ID] = Integer.max(CLK[ID], rclk) + 1;
                      
        //SYNCH ANOTHER PEER
        if (msg.equals("synch")) {
            String rStr ="::"+CLK[rid].toString();
            resp.println(seatsObj.toString()+rStr); //update peer data + clk
        }
        //REQ_CS
        else if (msg.equals("reqcs")) {
            Q[rid] = CLK[rid];
            sendMsg("ACK", rid, ID, CLK[ID]);
        }
        //REL_CS -- SYNCH USING INFO RECEIVED
        else if (msg.contains("relcs")) { //relcs#***** (see above)
            Q[rid]=Integer.MAX_VALUE;
            String[] data = msg.split("#");
            if (!data[1].equals("FALSE")) { //changedState
            	String[] entry = data[1].split(":");
            	if (entry[0].equals("A")) {
            		seatsObj.bookSeat(entry[2], entry[1]);
            	}
            	else if (entry[0].equals("D")) {
            		seatsObj.delete(entry[1]);
            	}
            }
        }
	}

	void broadcast(String msg, Integer id, Integer clk) {
        for (int x = 0; x < serversInfo.length; x++) {
            if (x==id) { continue; }
            sendMsg(msg, x, id, clk);
        }     
	}
	
	void sendMsg(String msg, Integer dest_id, Integer source_id, Integer source_clk) {
		
		String fullMsg = msg+" "+source_id.toString()+" "+source_clk.toString();
		String addr = serversInfo[dest_id][0];
		Integer port = Integer.parseInt(serversInfo[dest_id][1]);
		
		new Thread(new SendMsg(addr, port, fullMsg)).start();
	}

    public String seatMaster (String received) {

        String[] tokens = received.split(" ");

        //Handle data packet as requested by client
        Integer commandReturn = null;
        String returnMessage = "ERROR occurred; try again";
        changeState = "FALSE";

        if (tokens[0].equals("reserve")) {
            commandReturn = seatsObj.reserve(tokens[1]);
            if (commandReturn == null) {
                returnMessage = "Seat already booked against the name provided";
            }
            else {
                returnMessage = "Seat assigned to " + tokens[1] + " is " 
                		+ Integer.toString(commandReturn);
                changeState = "A:"+Integer.toString(commandReturn)+":"+tokens[1];
            }
        }
        else if (tokens[0].equals("bookSeat")) {
            commandReturn = seatsObj.bookSeat(tokens[1], tokens[2]);
            if (commandReturn == null) {
                returnMessage = tokens[2] + " is not available";
            }
            else if (commandReturn.equals(Integer.MIN_VALUE)) {
                returnMessage = "Seat already booked against the name provided";
            }
            else { //Seat assigned to <name> is <seat-number>
                returnMessage = "Seat assigned to " + tokens[1] + " is "
                        + Integer.toString(commandReturn);
                changeState = "A:"+Integer.toString(commandReturn)+":"+tokens[1];
            }
        }
        else if (tokens[0].equals("search")) {
            commandReturn = seatsObj.search(tokens[1]);
            if (commandReturn == null) {
                returnMessage = "No reservation found for " + tokens[1];
            }
            else {
                returnMessage = Integer.toString(commandReturn);
            }
        }
        else if (tokens[0].equals("delete")) {
            commandReturn = seatsObj.delete(tokens[1]);
            if (commandReturn == null) {
                returnMessage = "No reservation found for " + tokens[1];                
            }
            else {
                returnMessage = Integer.toString(commandReturn);
                changeState = "D:"+Integer.toString(commandReturn)+":"+tokens[1];
            }
        }
        else  {
            System.out.println("Not valid request\t"+received);
        }
        return returnMessage;
    }
	
}


class SendMsg implements Runnable {
    final static int TIMEOUT = 100;//ms
    String msg, addr, answer;
    Integer port;
    Thread runningThread = null;
    
	public SendMsg(String addr, Integer port, String msg) {
		this.addr=addr; this.port=port; this.msg=msg;
	}
	
	@Override
	public void run() {

        synchronized(this) {
            this.runningThread = Thread.currentThread();
        }

        try {
            Socket peerSckt = new Socket(InetAddress.getByName(addr), port);
            peerSckt.setSoTimeout(TIMEOUT);

            PrintWriter peerOut = new PrintWriter(peerSckt.getOutputStream(), true);
            BufferedReader peerIn =
                    new BufferedReader(new InputStreamReader(peerSckt.getInputStream()));

            //ESTABLISHING CONNECTION TO SERVER PEER
            peerOut.println("server_msg " + msg);
            peerOut.flush();
            while(!peerIn.ready()) {;}
            answer = peerIn.readLine();
            peerSckt.close();
        }
        catch (SocketTimeoutException t) {
            System.out.println("Server at "+addr+":"+port.toString()+" TIMEOUT!");
        }
        catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Server at "+addr+":"+port.toString()+" ERROR:\t"+e);
        }

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
        for (Map.Entry<String, Integer> entry : reservationSystem.entrySet()) {
            out += entry.getKey() + "=" + entry.getValue()+" ";
        }
        return out;
    }
}

