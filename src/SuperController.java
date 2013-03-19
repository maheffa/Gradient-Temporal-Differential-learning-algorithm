

import com.cyberbotics.webots.controller.Field;
import com.cyberbotics.webots.controller.Node;
import com.cyberbotics.webots.controller.Supervisor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class Status{
    public double[] translation;
    public double[] rotation;
}

class ServoStatus extends Status{
    public double angle;
}

class ExperimentData{
    
    private final String[] elementsDEF = {"Grabber", "GrabMe", "PLATE"};
    private Field[] translations = null;
    private SuperController superController = null;
    
    public ExperimentData(SuperController superController){
        this.superController = superController;
        translations = new Field[]{
            superController.getFromDef(this.elementsDEF[0]).getField("translation"),
            superController.getFromDef(this.elementsDEF[1]).getField("translation"),
            superController.getFromDef(this.elementsDEF[2]).getField("translation")
        };
    }
    
    public double[] getGrabberPosition(){
        return translations[0].getSFVec3f();
    }
    
    public double[] getThingsPosition(){
        return translations[1].getSFVec3f();
    }
    
    public double[] getPlacePosition(){
        return translations[2].getSFVec3f();
    }
}

public class SuperController extends Supervisor {
    
    private Map<String, Status> backup = null;
    private String[] devicesDEF = null;
    private ServerSocket superControllerServerSocket = null, resultServerSocket = null;
    private Socket RLClientSocket = null, controllerClientSocket = null;
    private PrintWriter outRL = null, outController = null;
    private BufferedReader inRL = null, inController = null;
    private ArrayList<String> instructionsFromRL = null;
    private ArrayList<String> instructionsToController = null;
    private ArrayList<String> resultFromController = null;
    private ArrayList<String> resultToRL = null;
    private Thread RLAlgorithm = null;
    private ExperimentData expData = null;
    
    
    public SuperController() {
        super();
        
        // array of string containing all DEF tag of devices on webot world
        devicesDEF = new String[]{
            "ROBOT", "ARM", "GrabMe", "SERVO0", "SERVO1", "SERVO2",
            "SERVO3", "SERVO4", "SERVO51",  "SERVO52"};
        
        // map for storing the initial configuration
        backup = new HashMap<String, Status>();
        
        // build experiment data
        expData = new ExperimentData(this);
        
        // serversocket for listening to the RL algorithm instruction
        try {
            superControllerServerSocket = 
                    new ServerSocket(Util.SUPERCONTROLLER_PORT, 0, InetAddress.getLocalHost());
            resultServerSocket = 
                    new ServerSocket(Util.RLANSWER_PORT, 0, InetAddress.getLocalHost());
        } catch (IOException e) {
            System.err.println("Could not listen on port: "+Util.SUPERCONTROLLER_PORT);
            System.exit(1);
        }
        
        // instruction and result list initialization
        instructionsFromRL = new ArrayList<String>();
        instructionsToController = new ArrayList<String>();
        resultFromController = new ArrayList<String>();
        resultToRL = new ArrayList<String>();
        
        // launch the RL algorithm's thread
        RLAlgorithm = new GeneticAlgorithm();
        RLAlgorithm.start();
    }
    
    public void run() throws IOException {
        
        // Backup startup configuration
        createBackup();
        
        do {
            // Restore startup configuration for new test
            restoreFromBackup();
            
            // Wait and accept connection from RL algorithm
            try {
                System.out.println("Waiting for RL algorithm to connect");
                RLClientSocket = superControllerServerSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            // Preapre to read and write into RL's socket
            outRL = new PrintWriter(RLClientSocket.getOutputStream(), true);
            inRL = new BufferedReader( new InputStreamReader(RLClientSocket.getInputStream()));
            
            // tell the RL algorithm we are ready to get instruction
            outRL.println("READY");
            System.out.println("RLAlgorithm instructions request");
            
            // read RL's instruction into
            String inputLine;
            while((inputLine = inRL.readLine()) != null){
                instructionsFromRL.add(inputLine);
            }
            System.out.println("Received instructions "+Util.printArrayList(instructionsFromRL));
            
            // format instructionsFromRL into instructionsToController
            for(String instruction : instructionsFromRL){
                instructionsToController.add(instruction);
            }
            
            // prepare to send instruction for controller socket
            controllerClientSocket = new Socket(InetAddress.getLocalHost(), Util.CONTROLLER_PORT);
            outController = new PrintWriter(controllerClientSocket.getOutputStream(), true);
            inController = new BufferedReader( new InputStreamReader(controllerClientSocket.getInputStream()));
            
            // ask if we can send instruction to controller
            inController.readLine();
            
            // send the instructions
            for(String instruction : instructionsToController){
                outController.println(instruction);
            }
            
            // prepare to send the result to RL algorithm
            resultToRL = getSimulationResult();
            
            // Put it into the server result so the client would now how did we do
            // Wait for the RL algorithm to request answer
            try {
                System.out.println("Waiting for RL algorithm to request answer");
                RLClientSocket = resultServerSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            outRL = new PrintWriter(RLClientSocket.getOutputStream(), true);
            for(String result : resultToRL){
                outRL.println(result);
            }
            
            // close PrintWriters
            outRL.close();
            outController.close();
            // close BufferReaders
            inRL.close();
            inController.close();
            // close Sockets
            RLClientSocket.close();
            controllerClientSocket.close();
            // clearing all list
            instructionsFromRL.clear();
            instructionsToController.clear();
            resultFromController.clear();
            resultToRL.clear();
            
        } while (step(Util.TIME_STEP) != -1);
    }
    
    public void finalize(){
        super.finalize();
        try {
            superControllerServerSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(SuperController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void createBackup(){
        System.out.println("Creating backup");
        for(String def : devicesDEF){
            backup.put(def, getStatus(def));
        }
    }
    
    private void restoreFromBackup(){
        System.out.println("Restoring from backup");
        for(String def : devicesDEF){
            setStatus(def, backup.get(def));
        }
    }
    
    private ArrayList<String> getSimulationResult(){
        ArrayList<String> result = new ArrayList<String>();
        for(double value : expData.getGrabberPosition()){
            result.add(Double.toString(value));
        }
        for(double value : expData.getThingsPosition()){
            result.add(Double.toString(value));
        }
        for(double value : expData.getPlacePosition()){
            result.add(Double.toString(value));
        }
        return result;
    }
    
    private void setStatus(String defName, Status status){
        Node node = getFromDef(defName);
        node.getField("translation").setSFVec3f(status.translation);
        node.getField("rotation").setSFRotation(status.rotation);
    }
    
    private Status getStatus(String defName){
        Node node = getFromDef(defName);
        Status s = new Status();
        s.translation = node.getField("translation").getSFVec3f();
        s.rotation = node.getField("rotation").getSFRotation();
        return s;
    }
    
    public static void main(String[] args) {
        SuperController controller = new SuperController();
        try {
            System.out.println("Launching supercontroller");
            controller.run();
        } catch (IOException ex) {
            System.err.println("Error while launching supercontroller");
            ex.printStackTrace();
        }
    }
}
