import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    final static int TIMEOUT = 100;//ms
    static String [][] serversInfo;

    public static void main (String[] args) {

        Scanner sc = new Scanner(System.in);
        //REFUSES TO WORK FOR ME, WHY??? --int numServer = sc.nextInt();
        String cmd = sc.nextLine();
        String[] tokens = cmd.split(" ");
        int numServer = Integer.parseInt(tokens[0]);

        serversInfo = new String[numServer][2];

        for (int i = 0; i < numServer; i++) {
            // TODO: parse inputs to get the ips and ports of server
            cmd = sc.nextLine();
            tokens = cmd.split(":");

            serversInfo[i][0] = tokens[0]; //ip adr
            serversInfo[i][1] = tokens[1]; //currPort
        }

        System.out.println("\nReady for a command ...");

        while(sc.hasNextLine()) {
            cmd = sc.nextLine();
            tokens = cmd.split(" ");

            //reserve <name> -- First seat available, if any
            if (tokens[0].equals("reserve")) {
                if (tokens.length != 2) {
                    System.out.println("ERROR: Invalid command\t- "+cmd);
                    System.out.println(" syntax: reserve <name>\n");
                }
                else {
                    String answer = TCPreq(cmd);
                    System.out.println("Server: " + answer);
                }
            }
            //bookSeat <name> <seatNum> -- specific seat if available
            else if (tokens[0].equals("bookSeat")) {
                if ( tokens.length != 3 ) {
                    System.out.println("ERROR: Invalid command\t- "+cmd);
                    System.out.println(" syntax: bookSeat <name> <seatNum>\n");
                }
                else {
                    String answer = TCPreq(cmd);
                    System.out.println("Server: " + answer);
                }
            }
            //search <name>
            else if (tokens[0].equals("search")) {
                if (tokens.length != 2) {
                    System.out.println("ERROR: Invalid command\t- "+cmd);
                    System.out.println(" syntax: search <name>\n");
                }
                else {
                    String answer = TCPreq(cmd);
                    System.out.println("Server: " + answer);
                }
            }
            //delete <name>
            else if (tokens[0].equals("delete")) {
                if (tokens.length != 2) {
                    System.out.println("ERROR: Invalid command\t- "+cmd);
                    System.out.println(" syntax: delete <name>\n");
                }
                else  {
                    String answer = TCPreq(cmd);
                    System.out.println("Server: " + answer);
                }
            }
            else {
                System.out.println("ERROR: No such command\t -"+cmd+"\n");
            }
        }
    }

    public static String TCPreq (String cmd) {

        String currAddr;
        int currPort;
        Socket clientSocket;

        String answer = "Error in TCP request";
        int numSrvr = serversInfo.length;

        //ESTABLISH CONNECTION TO A LIVE SERVER
        for (int id = 0; ; id = (id+1) % numSrvr) {

            currAddr = serversInfo[id][0];
            currPort = Integer.parseInt(serversInfo[id][1]);
            //DEBUG System.out.println("\n"+serversInfo[id][0]+" "+serversInfo[id][1]);

            try {
                clientSocket = new Socket(InetAddress.getByName(currAddr), currPort);
                clientSocket.setSoTimeout(TIMEOUT);
                PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader inFromServer =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                //ESTABLISHING CONNECTION TO SERVER
                //TODO: check answer==ACK
                outToServer.println("client_req");
                while(!inFromServer.ready()) {;}
                answer = inFromServer.readLine();

                //SERVER IS ALIVE ... SEND REQ
                //clientSocket.setSoTimeout(0);
                outToServer.println(cmd);
                while(!inFromServer.ready()) {;}
                answer = inFromServer.readLine();

                clientSocket.close();
            }
            //TODO: output dead server, add delay?
            catch (SocketTimeoutException t) {
                continue;
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            break; //IF I GET HERE I'M GOOD
        }

        return answer+"\n";
    }
}

