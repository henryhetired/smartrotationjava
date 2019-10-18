package husikenlab.SmartRotationProcessing;
import command_processor.command_listener;
import command_processor.config;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class command_server extends command_listener {
    private boolean initialized = false;
    private static SmartRotationProcessing processing_engine;
    private int[] angles;
    protected ReentrantLock anglelock = new ReentrantLock();
    public command_server(command_processor.config configin){
        this.config = configin;
        this.ServerPort = configin.port;
    }
    class command_worker implements Runnable{
        private String command;
        private String[] commandlist;
        private int port;
        public command_worker(int port,String commandin){
            this.command = commandin;
            this.commandlist = command.split(" ");
            this.port = port;
        }
        @Override
        public void run() {
            if (this.commandlist[1].startsWith("initialize")){
                anglelock.lock();
                if (!initialized) {
                    //start a smart rotation processing server that listens to command at specific port
                    processing_engine.init(commandlist[1]);
                    angles = new int[processing_engine.config.nAngles];
                    initialized = true;
                    printlock.lock();
                    System.out.println("Command finished:" + command);
                    printlock.unlock();
                }
                anglelock.unlock();
            }
            else if (this.commandlist[1].startsWith("evaluation")){
                if (initialized){
                    processing_engine.evaluation_step(commandlist[2], Integer.parseInt(commandlist[3]), Integer.parseInt(commandlist[4]));
                    processing_engine.de.get_strategy(Integer.parseInt(commandlist[3]),Integer.parseInt(commandlist[5]));
                    anglelock.lock();
                    angles = processing_engine.de.angles;
                    anglelock.unlock();
                }
            }
            else if (this.commandlist[1].startsWith("processangle")){
                if (initialized){
                    processing_engine.update_step(commandlist[2], Integer.parseInt(commandlist[3]), Integer.parseInt(commandlist[4]));
                    processing_engine.de.get_strategy(Integer.parseInt(commandlist[4]),Integer.parseInt(commandlist[5]));
                    anglelock.lock();
                    angles = processing_engine.de.angles;
                    anglelock.unlock();
                }
            }
            else if (this.commandlist[1].startsWith("getupdate")){
                if (angles!=null){
                    try {
                        send_angles(this.port);
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
            //TODO:Implement other potential functions
        }
    }
    private void send_angles(int port) throws IOException{
        //write the angles for optimal configuration to a socket
        anglelock.lock();
        String messageout = "angles=";
        for (int i=0;i<angles.length;i++){
            messageout+=Integer.toString(angles[i]*processing_engine.config.ang_reso)+",";
        }
        InetAddress add = InetAddress.getByName(config.ipadd);
        ServerSocket socket = new ServerSocket(port,10,add);
        Socket clientsocket;
        clientsocket = socket.accept();
        OutputStream os = clientsocket.getOutputStream();
        os.write(messageout.getBytes());
        os.close();

    }
}
