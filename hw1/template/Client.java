import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
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
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buf;
            InetAddress address = InetAddress.getByName(hostAddress);
            DatagramPacket packet;

            // Meant for TCP
            DataOutputStream outToServer;
            BufferedReader inFromServer;
            Socket clientSocket;

            Scanner sc = new Scanner(System.in);
            while(sc.hasNextLine()) {
                String cmd = sc.nextLine();
                String[] tokens = cmd.split(" ");

                if (tokens[0].equals("reserve")) {

                    //TODO: implement checking of parameters for reserve, sending of command
                    //reserve <name> T|U
                    //Code for UDP
                    if (tokens[2].equals("U")) {
                        buf = prepareMessage(tokens);
                        packet = new DatagramPacket(buf, buf.length, address, udpPort);
                        socket.send(packet);

                        buf = new byte[256];
                        packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        String received = new String(packet.getData(), 0, packet.getLength());
                        System.out.println("Server(U): " + received);

                    } else if (tokens[2].equals("T")) {
                        //Code for TCP
                        clientSocket = new Socket(hostAddress, tcpPort);
                        outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        outToServer.writeBytes(tcpMessage(tokens));
                        String answer = inFromServer.readLine();
                        System.out.println("Server(T):" + answer);
                        clientSocket.close();
                    } else {
                        System.out.println("ERROR: No such command");
                    }

                } else if (tokens[0].equals("bookSeat")) {
                    //TODO: implement checking of parameters for bookSeat, sending of command
                    //bookSeat <name> <seatNum> T|U
                    //Code for UDP
                    if (tokens[3].equals("U")) {
                        buf = prepareMessageLong(tokens);
                        packet = new DatagramPacket(buf, buf.length, address, udpPort);
                        socket.send(packet);

                        buf = new byte[256];
                        packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        String received = new String(packet.getData(), 0, packet.getLength());
                        System.out.println("Server(U): " + received);

                        //Code for TCP
                    } else if (tokens[2].equals("T")) {
                        clientSocket = new Socket(hostAddress, tcpPort);
                        outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        outToServer.writeBytes(tcpMessage(tokens));
                        String answer = inFromServer.readLine();
                        System.out.println("Server(T):" + answer);
                        clientSocket.close();
                    } else {
                        System.out.println("ERROR: No such command");
                    }

                } else if (tokens[0].equals("search")) {
                    //TODO: implement checking of parameters for search, sending of command
                    //search <name> T|U
                    //Code for UDP
                    if (tokens[2].equals("U")) {
                        buf = prepareMessage(tokens);
                        packet = new DatagramPacket(buf, buf.length, address, udpPort);
                        socket.send(packet);

                        buf = new byte[256];
                        packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        String received = new String(packet.getData(), 0, packet.getLength());
                        System.out.println("Server(U): " + received);

                        //Code for TCP
                    } else if (tokens[2].equals("T")) {
                        clientSocket = new Socket(hostAddress, tcpPort);
                        outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        outToServer.writeBytes(tcpMessage(tokens));
                        String answer = inFromServer.readLine();
                        System.out.println("Server(T):" + answer);
                        clientSocket.close();
                    } else {
                        System.out.println("ERROR: No such command");
                    }

                } else if (tokens[0].equals("delete")) {
                    //TODO: implement checking of parameters for delete, sending of command
                    //delete <name> T|U
                    //Code for UDP
                    if (tokens[2].equals("U")) {
                        buf = prepareMessage(tokens);
                        packet = new DatagramPacket(buf, buf.length, address, udpPort);
                        socket.send(packet);

                        buf = new byte[256];
                        packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        String received = new String(packet.getData(), 0, packet.getLength());
                        System.out.println("Server(U): " + received);

                        //Code for TCP
                    } else if (tokens[2].equals("T")) {
                        clientSocket = new Socket(hostAddress, tcpPort);
                        outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        outToServer.writeBytes(tcpMessage(tokens));
                        String answer = inFromServer.readLine();
                        System.out.println("Server(T):" + answer);
                        clientSocket.close();
                    } else {
                        System.out.println("ERROR: No such command");
                    }

                } else {
                    System.out.println("ERROR: No such command");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] prepareMessage(String[] tokens) {
        byte[] buf;
        String var = "";
        var = var.concat(tokens[0] + " ");
        var = var.concat(tokens[1] + "\n");
        buf = var.getBytes();

        return buf;
    }

    public static byte[] prepareMessageLong(String[] tokens) {
        byte[] buf;
        String var = "";
        var = var.concat(tokens[0] + " ");
        var = var.concat(tokens[1] + " ");
        var = var.concat(tokens[2] + "\n");
        buf = var.getBytes();

        return buf;
    }

    public static String tcpMessage(String[] tokens) {
        String var = "";
        var = var.concat(tokens[0] + " ");
        var = var.concat(tokens[1] + "\n");

        return var;
    }
}
