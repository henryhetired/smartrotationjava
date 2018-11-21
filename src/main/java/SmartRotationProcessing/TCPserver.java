package SmartRotationProcessing;

import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPserver {
    private static boolean keeprunning = true;
    private static ServerSocket server = null;
    private static Socket client = null;
    public String messagein = "start";
    public String messageout = "";
    private ArrayList<Integer> angles;
    private static DataInputStream is;
    private static PrintStream os;
    private static Queue<String> commands;
    private static ExecutorService myexecutor;
    private static boolean initialized = false;
    private static SmartRotationProcessing processing_engine;
    private final Object lock = new Object(); //lock to ensure the processing engine isn't trying to access an update when

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

    public void process_command(String command) {
        if (initialized) {
            //can be faster if there are multiple GPUs available for processing
            if (command.startsWith("initialize")) {
                String[] split_command = command.split("\\ ");
                processing_engine.init(split_command[1]);
                angles = new ArrayList<Integer>(processing_engine.config.nAngles);
            }
            if (command.startsWith("evaluation")) {
                String[] split_command = command.split("\\ ");
                if (processing_engine.initialized) {
                    processing_engine.evaluation_step(split_command[1], Integer.parseInt(split_command[2]), Integer.parseInt(split_command[3]), Integer.parseInt(split_command[4]));
                    processing_engine.de.get_strategy();
                    synchronized (lock) {
                        angles = processing_engine.de.current_angles;
                    }

                }
            }
            if (command.startsWith("processangle")) {
                String[] split_command = command.split("\\ ");
                if (processing_engine.initialized) {
                    processing_engine.update_step(split_command[1], Integer.parseInt(split_command[2]), Integer.parseInt(split_command[3]));
                    processing_engine.de.get_strategy();
                    synchronized (lock) {
                        angles = processing_engine.de.current_angles;
                    }
                }
            }
            if (command.startsWith("getupdate")){
                if (angles!=null){
                    send_angles();
                }
            }
        }

    }

    public void send_angles() {
        myexecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    messageout = "angles=";
                    for (int i = 0; i < angles.size(); i++) {
                        messageout += Integer.toString(angles.get(i)*processing_engine.config.ang_reso) + ",";
                    }
                }
                try {
                    os = new PrintStream(client.getOutputStream(),true);
                    os.println(messageout);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void command_handler() {
        myexecutor.execute(new Runnable() {
            @Override
            public void run() {
                String current_command;
                while (keeprunning) {
                    try {
                        current_command = commands.remove();
                        process_command(current_command);
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
                    if (messagein.startsWith("getupdate")){
                        send_angles();
                    }
                    else{
                    commands.add(messagein);
                    System.out.println("commands queued");}
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
