import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main (String[] args) {
        String hostAddress;
        int tcpPort;
        int udpPort;

        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <hostAddress>: the address of the server");
            System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(3) <udpPort>: the port number for UDP connection");
            System.exit(-1);
        }

        hostAddress = args[0];
        tcpPort = Integer.parseInt(args[1]);
        udpPort = Integer.parseInt(args[2]);

        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine()) {
            String cmd = sc.nextLine();
            String[] tokens = cmd.split(" ");

             //reserve <name> T|U -- First seat available, if any
            if (tokens[0].equals("reserve")) {
                    if (tokens.length != 3) {
                        System.out.println("ERROR: Invalid command\t- "+cmd);
                        System.out.println(" syntax: reserve <name> <T|U>\n");
                    }
                    else if (tokens[2].toLowerCase().equals("u")) {
                        String response = UDPreq(cmd, hostAddress, udpPort);
                        System.out.println("Server(U): " + response);
                    }
                    else if (tokens[2].toLowerCase().equals("t")) {
                        String answer = TCPreq(cmd, hostAddress, tcpPort);
                        System.out.println("Server(T): " + answer);
                    }
                    else {
                        System.out.println("ERROR: Invalid command\t- "+cmd);
                        System.out.println(" syntax: reserve <name> <T|U>\n");
                    }
            }
            //bookSeat <name> <seatNum> T|U -- specific seat if available
            else if (tokens[0].equals("bookSeat")) {
                if ( tokens.length != 4 ) { //CAN'T GET THIS SHITE TO WORK -- || isInt(tokens[2]) ) {
                    System.out.println("ERROR: Invalid command\t- "+cmd);
                    System.out.println(" syntax: bookSeat <name> <seatNum> <T|U>\n");
                }
                else if (tokens[3].toLowerCase().equals("u")) {
                    String response = UDPreq(cmd, hostAddress, udpPort);
                    System.out.println("Server(U): " + response);
                }
                else if (tokens[3].toLowerCase().equals("t")) {
                    String answer = TCPreq(cmd, hostAddress, tcpPort);
                    System.out.println("Server(T): " + answer);
                }
                else {
                    System.out.println("ERROR: Invalid command\t- "+cmd);
                    System.out.println(" syntax: bookSeat <name> <seatNum> <T|U>\n");
                }
            }
            //search <name> T|U
            else if (tokens[0].equals("search")) {
                if (tokens.length != 3) {
                    System.out.println("ERROR: Invalid command\t- "+cmd);
                    System.out.println(" syntax: search <name> <T|U>\n");
                }
                else if (tokens[2].toLowerCase().equals("u")) {
                    String response = UDPreq(cmd, hostAddress, udpPort);
                    System.out.println("Server(U): " + response);
                }
                else if (tokens[2].toLowerCase().equals("t")) {
                    String answer = TCPreq(cmd, hostAddress, tcpPort);
                    System.out.println("Server(T): " + answer);
                }
                else {
                    System.out.println("ERROR: Invalid command\t- "+cmd);
                    System.out.println(" syntax: search <name> <T|U>\n");
                }
            }
            //delete <name> T|U
            else if (tokens[0].equals("delete")) {
                if (tokens.length != 3) {
                    System.out.println("ERROR: Invalid command\t- "+cmd);
                    System.out.println(" syntax: delete <name> <T|U>\n");
                }
                else if (tokens[2].toLowerCase().equals("u")) {
                    String response = UDPreq(cmd, hostAddress, udpPort);
                    System.out.println("Server(U): " + response);
                }
                else if (tokens[2].toLowerCase().equals("t")) {
                    String answer = TCPreq(cmd, hostAddress, tcpPort);
                    System.out.println("Server(T): " + answer);
                }
                else {
                    System.out.println("ERROR: Invalid command\t- "+cmd);
                    System.out.println(" syntax: delete <name> <T|U>\n");
                }
            }
            else {
                System.out.println("ERROR: No such command\t -"+cmd+"\n");
            }
        }//END WHILE SCANNER
    }//END MAIN

    public static String UDPreq (String cmd, String hostAddress, Integer port) {
        String response = "Error in UDP request";

        try {
            byte[] buf = cmd.getBytes();
            InetAddress address = InetAddress.getByName(hostAddress);
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);

            socket.send(packet);
            buf = new byte[256];
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            response = new String(packet.getData(), 0, packet.getLength());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    public static String TCPreq (String cmd, String hostAddress, Integer port) {
        String answer = "Error in TCP request";

        try {
            InetAddress address = InetAddress.getByName(hostAddress);
            Socket clientSocket = new Socket(address, port);
            PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            outToServer.println(cmd);
            answer = inFromServer.readLine();
            clientSocket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return answer+"\n";
    }

    public static boolean isInt (String token){
        return token.matches("^\\d+\\n?$"); //AHHH!!! WHY WON'T IT WORK?????
    }
}
