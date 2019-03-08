package SmartRotationProcessing;

import java.io.IOException;

public class SmartRotationworkflow {
    public static void main(String args[]){
//        TCPserver runserver = new TCPserver();
//        runserver.init(Integer.parseInt(args[0]));
//        runserver.run();
//        System.out.println(System.getProperty("user.dir"));
        configwriter config = new configwriter();
        String workspace = "/mnt/fileserver/Henry-SPIM/smart_rotation/11222018/e5/data/workspace/";
        try{
            config.read(workspace);}
        catch (IOException e){
            e.printStackTrace();
        }
        decisionengine de = new decisionengine();
        de.init(config,workspace);
        de.get_strategy(0);
        System.out.println(de.angles[0]);
        System.out.println(de.angles[1]);
        System.out.println(de.angles[2]);
        System.out.println(de.angles[3]);

    }
}
