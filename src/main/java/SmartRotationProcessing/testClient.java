package SmartRotationProcessing;

import java.io.*;
import java.net.*;

public class testClient {
    public void send(String message,String hostname,int port) throws   Exception{
        Socket clientSocket = new Socket(hostname,port);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeBytes(message + '\n');
        clientSocket.close();
    }
}
