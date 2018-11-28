package SmartRotationProcessing;

public class SmartRotationworkflow {
    public static void main(String args[]){
        TCPserver runserver = new TCPserver();
        runserver.init();
        runserver.run();
    }
}
