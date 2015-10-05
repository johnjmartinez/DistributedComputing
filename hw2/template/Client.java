import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    String currAddr;
    String currPort;
    final static int TIMEOUT = 100; //ms

    public static void main (String[] args) {

        Scanner sc = new Scanner(System.in);
        int numServer = sc.nextInt();
        String[][] serversInfo = new String[numServer][2];

        for (int i = 0; i < numServer; i++) {
            // TODO: parse inputs to get the ips and ports of server
            String cmd = sc.nextLine();
            String[] tokens = cmd.split(":");

            serversInfo[i][0] = tokens[0]; //ip adr
            serversInfo[i][1] = tokens[1]; //port
        }

        while(sc.hasNextLine()) {
            String cmd = sc.nextLine();
            String[] tokens = cmd.split(" ");

            //reserve <name> -- First seat available, if any
            if (tokens[0].equals("reserve")) {
                if (tokens.length != 2) {
                    System.out.println("ERROR: Invalid command\t- "+cmd);
                    System.out.println(" syntax: reserve <name>\n");
                }
                else {
                    String answer = TCPreq(cmd, serversInfo);
                    System.out.println("Server(T): " + answer);
                }
            }
            //bookSeat <name> <seatNum> -- specific seat if available
            else if (tokens[0].equals("bookSeat")) {
                if ( tokens.length != 3 ) { //CAN'T GET THIS SHITE TO WORK -- || isInt(toke
                    System.out.println("ERROR: Invalid command\t- "+cmd);
                    System.out.println(" syntax: bookSeat <name> <seatNum>\n");
                }
                else {
                    String answer = TCPreq(cmd, serversInfo);
                    System.out.println("Server(T): " + answer);
                }
            }
            //search <name>
            else if (tokens[0].equals("search")) {
                if (tokens.length != 2) {
                    System.out.println("ERROR: Invalid command\t- "+cmd);
                    System.out.println(" syntax: search <name>\n");
                }
                else {
                    String answer = TCPreq(cmd, serversInfo);
                    System.out.println("Server(T): " + answer);
                }
            }
            //delete <name>
            else if (tokens[0].equals("delete")) {
                if (tokens.length != 2) {
                    System.out.println("ERROR: Invalid command\t- "+cmd);
                    System.out.println(" syntax: delete <name>\n");
                }
                else  {
                    String answer = TCPreq(cmd, serversInfo);
                    System.out.println("Server(T): " + answer);
                }
            }
            else {
                System.out.println("ERROR: No such command\t -"+cmd+"\n");
            }
        }
    }

    public static String TCPreq (String cmd, String [][] serversInfo) {

        String address;
        int port;
        Socket clientSocket;

        String answer = "Error in TCP request";
        int numSrvr = serversInfo.length;

        //ESTABLISH CONNECTION TO A LIVE SERVER
        for (int id = 0; id < numSrvr; id = (id+1) % numSrvr) {

            address = serversInfo[id][0];
            port = Integer.parseInt(serversInfo[id][1]);

            try {

                clientSocket = new Socket(InetAddress.getByName(address), port);
                PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                outToServer.println("incoming_request");
                answer = inFromServer.readLine();
                //clientSocket.close();
                }
             catch (Exception e) {
                e.printStackTrace();
             }

        }
        


        return answer+"\n";
    }
}

