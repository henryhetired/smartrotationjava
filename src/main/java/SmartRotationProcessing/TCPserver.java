package SmartRotationProcessing;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPserver {
    private static boolean keeprunning = true;
    private static ServerSocket server = null;
    private static Socket client = null;
    public static String messagein = "start";
    public static String messageout;
    private static DataInputStream is;
    private static Queue<String> commands;
    private static ExecutorService myexecutor;
    private static boolean initialized = false;
    private static SmartRotationProcessing processing_engine;

    //TCP/IP server class that listen to command for processing
    public static void init() {
        try {
            server = new ServerSocket(53705);
            System.out.println("Server Started");
        } catch (IOException e) {
            e.printStackTrace();
        }
        myexecutor = Executors.newCachedThreadPool();
        initialized = true;
        commands = new LinkedList<String>();
        processing_engine = new SmartRotationProcessing();
    }
    public static void process_command(String command){
        if (command.startsWith("evaluation")){
            String[] split_command = command.split("\\ ");

        }

    }
    void command_handler() {
        myexecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (keeprunning) {
                    try {
                        String current_command = commands.remove();
                        System.out.println(current_command);
                    } catch (NoSuchElementException e) {
                        continue;
                    }

                    try {
                        Thread.sleep(20);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("Stopped running");
                }
                return;
            }
        });
    }

    public void run() {
        command_handler();
        if (initialized) {
            while (!messagein.contains("disconnect")) {
                try {
                    client = server.accept();
                    System.out.println("client connected" + client.getInetAddress());
                    is = new DataInputStream(client.getInputStream());
                    BufferedReader d = new BufferedReader(new InputStreamReader(is));
                    messagein = d.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    commands.add(messagein);
                    System.out.println("commands queued");
                } catch (NullPointerException e) {
                    continue;
                }
            }
            keeprunning = false;
            myexecutor.shutdown();
            System.out.println("Server stopped");
            return;
        }
    }
}
