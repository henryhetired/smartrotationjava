package SmartRotationProcessing;

import java.io.IOException;

public class SmartRotationworkflow {
    public static void main(String args[]){
        TCPserver runserver = new TCPserver();
        runserver.init(Integer.parseInt(args[0]));
        runserver.run();
//        System.out.println(System.getProperty("user.dir"));
    }
}
