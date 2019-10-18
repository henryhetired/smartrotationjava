package husikenlab.SmartRotationProcessing;
import command_processor.config;

import java.io.IOException;

public class SmartRotationworkflow {
    public static void main(String args[]) throws IOException {
        config configuration = new config();
//        configuration.read("/home/henryhe/Documents/huiskenlab_commandprocessor/");
        configuration.read(args[0]);
        command_server cl = new command_server(configuration);
        Thread main = new Thread(cl);
        main.start();
        try {
            main.join();
        }
        catch (InterruptedException it){
            it.printStackTrace();
        }
        return;
    }
}
