

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
    private ServerSocket srvRL = null, srvController = null, srvResult = null;
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
            srvRL = new ServerSocket(Util.RL_PORT, 0, InetAddress.getLocalHost());
            srvController = new ServerSocket(Util.CTRL_PORT, 0, InetAddress.getLocalHost());
            srvResult = new ServerSocket(Util.RESULT_PORT, 0, InetAddress.getLocalHost());
        } catch (IOException e) {
            e.printStackTrace();
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
    
    public void run(){
        
        // Backup startup configuration
        createBackup();
        
        do {
            try {
                // Restore startup configuration for new test
                restoreFromBackup();
                
                /*
                 * Work with GA to get the chromosome information
                 */
                // Wait and accept connection from RL algorithm
                System.out.println("WAINTING FOR RL");
                Socket RLSocket = waitForRL();
                // Read chromosome from RL algorithm through RLSocket
                System.out.println("READING FROM RL");
                readRL(RLSocket);
                // Close the socket, we don't use it anymore
                System.out.println("CLOSING RL");
                RLSocket.close();
                // After reading instrution from RL algorithm, prepare it to send to Controller
                prepareToServeController();
                
                /*
                 * Work with Controller to execute info in the chromosome
                 */
                // Wait and accept connection from RL algorithm
                step(Util.TIME_STEP);
                System.out.println("WAINTING FOR CTRL");
                Socket ControllerSocket = waitForController();
                // Read chromosome from RL algorithm through RLSocket
                System.out.println("SENDING TO CTRL");
                sendController(ControllerSocket);
                // Close the socket, we don't use it anymore
                System.out.println("CLOSING CTRL");
                ControllerSocket.close();
                // After the controller finish working, prepare to send result to GA
                prepareToServeRL();
                
                /*
                 * Work with GA and send them the result
                 */
                // Wait and accept connection from RL algorithm
                System.out.println("WAITING FOR RSLT");
                Socket ResultSocket = waitForResult();
                // Read chromosome from RL algorithm through RLSocket
                System.out.println("SENDING TO RSLT");
                sendResult(ResultSocket);
                // Close the socket, we don't use it anymore
                System.out.println("CLOSING RSLT");
                ResultSocket.close();
                // After reading instrution from RL algorithm, prepare it to send to Controller
            } catch (IOException ex) {
                Logger.getLogger(SuperController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        } while (step(Util.TIME_STEP) != -1);
    }
    
    // ***************************************************
    private Socket waitForRL() throws IOException{
        return srvRL.accept();
    }
    
    private void readRL(Socket socket) throws IOException{
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        // Tell RL we are ready
        out.println("READY");
        
        // read the instruction
        String inputLine;
        while((inputLine = in.readLine()) != null){
            instructionsFromRL.add(inputLine);
        }
        System.out.println("Received instructions "+Util.printArrayList(instructionsFromRL));
        
        in.close(); out.close();
    }
    
    private void prepareToServeController(){
        // format instructionsFromRL into instructionsToController
        for(String instruction : instructionsFromRL){
            instructionsToController.add(instruction);
        }
    }
    
    // ***************************************************
    private Socket waitForController() throws IOException{
        return srvController.accept();
    }
    
    private void sendController(Socket socket) throws IOException{
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        System.out.println("Send instruction to controller");
        // Tell Controller about our instruction
        for(String instruction : instructionsToController){
            out.println(instruction);
        }
        
        // wait until controller is done
        System.out.println("Waiting for controller's feedback");
        in.readLine();
        System.out.println("Done simulation");
        
        in.close(); out.close();
    }
    
    private void prepareToServeRL() {
        resultToRL = getSimulationResult();
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
    
    // ***************************************************
    private Socket waitForResult() throws IOException{
        return srvResult.accept();
    }
    
    private void sendResult(Socket socket) throws IOException{
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        // Tell Controller about our instruction
        for(String res : resultToRL){
            out.println(res);
        }
        
        out.close();
    }
    
    //****************************************************
    public void finalize(){
        super.finalize();
        try {
            srvController.close();
            srvRL.close();
            srvResult.close();
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
