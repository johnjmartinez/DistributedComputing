import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    final static int TIMEOUT = 100;//ms

    public static void main (String[] args) {

        Scanner sc = new Scanner(System.in);
        int numServer = sc.nextInt();
        String[][] serversInfo = new String[numServer][2];

        for (int i = 0; i < numServer; i++) {
            // TODO: parse inputs to get the ips and ports of server
            String cmd = sc.nextLine();
            String[] tokens = cmd.split(":");

            serversInfo[i][0] = tokens[0]; //ip adr
            serversInfo[i][1] = tokens[1]; //currPort
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
                    String answer = TCPreq(cmd, serversInfo);
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
                    String answer = TCPreq(cmd, serversInfo);
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
                    String answer = TCPreq(cmd, serversInfo);
                    System.out.println("Server: " + answer);
                }
            }
            else {
                System.out.println("ERROR: No such command\t -"+cmd+"\n");
            }
        }
    }

    public static String TCPreq (String cmd, String [][] serversInfo) {

        String currAddr;
        int currPort;
        Socket clientSocket;

        String answer = "Error in TCP request";
        int numSrvr = serversInfo.length;

        //ESTABLISH CONNECTION TO A LIVE SERVER
        for (int id = 0; ; id = (id+1) % numSrvr) {

            currAddr = serversInfo[id][0];
            currPort = Integer.parseInt(serversInfo[id][1]);

            try {
                clientSocket = new Socket(InetAddress.getByName(currAddr), currPort);
                PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader inFromServer =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                //ESTABLISHING CONNECTION TO SERVER
                //TODO: check answer==ACK
                clientSocket.setSoTimeout(TIMEOUT);
                outToServer.println("client_req");
                answer = inFromServer.readLine();

                //SERVER IS ALIVE ... SEND REQ
                clientSocket.setSoTimeout(0);
                outToServer.println(cmd);
                answer = inFromServer.readLine();

                clientSocket.close();
            }
            //TODO: output dead server, add delay?
            catch (SocketTimeoutException t) {
                continue;
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            break; //IF I GET TO HERE I'M GOOD
        }

        return answer+"\n";
    }
}

