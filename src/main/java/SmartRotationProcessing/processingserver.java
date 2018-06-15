package SmartRotationProcessing;

import java.net.*;
import java.io.*;
import java.util.Date;

//
//public class processingserver {
//    private Socket socket = null;
//    private ServerSocket server = null;
//    private DataInputStream in = null;
//    public processingserver(int port){
//        try{
//            InetAddress IP_address = InetAddress.getLocalHost();
//            System.out.println(IP_address);
//            server = new ServerSocket(port);
//            System.out.println("Processing server started");
//            System.out.println("Waiting for a client");
//            socket = server.accept();
//            System.out.println("Client accepted");
//
//            in = new DataInputStream(
//                    new BufferedInputStream(socket.getInputStream())
//            );
//            String line="";
//            while (!line.endsWith(".tif")){
//                try
//                {
//                    line = in.readUTF();
//                    System.out.println(line);
//                }
//                catch (IOException e){
//                    e.printStackTrace();
//                }
//            }
//            System.out.println("Closing connection");
//            socket.close();
//            in.close();
//        }
//        catch (IOException i){
//            i.printStackTrace();
//        }
//    }
//}
//
public class processingserver{

    /**
     * Runs the server.
     */
    public processingserver(int port) throws IOException {
        ServerSocket listener = new ServerSocket(port);
        try {
            while (true) {
                System.out.println("waiting for a client");
                Socket socket = listener.accept();

                try {
                    PrintWriter out =
                            new PrintWriter(socket.getOutputStream(), true);
                    out.println(new Date().toString());
                } finally {
                    socket.close();
                }
            }
        }
        finally {
            listener.close();
        }
    }
}
