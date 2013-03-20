

import com.cyberbotics.webots.controller.Node;
import com.cyberbotics.webots.controller.Servo;
import com.cyberbotics.webots.controller.Supervisor;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class BackupNode{
    private Node n;
    private boolean isObject;
    private double[] translation = null, rotation = null;
    public BackupNode(Node n, boolean isObject){
        this.isObject = isObject;
        this.n = n;
    }
    public void backup(){
        translation = n.getField("translation").getSFVec3f().clone();
        if(isObject)
            rotation = n.getField("rotation").getSFRotation().clone();
    }
    public void restore(){
        n.getField("translation").setSFVec3f(translation.clone());
        if(isObject)
            n.getField("rotation").setSFRotation(rotation.clone());
    }
}

public class SuperController extends Supervisor {
    
    private Map<String, BackupNode> backup = null;
    private String[] objectDEF = null, transformDEF = null, servoName = null;
    private Thread RLAlgorithm = null;
    private double[] instructions = null;
    
    
    public SuperController() {
        super();
        
        // array of string containing all DEF tag of devices on webot world
        objectDEF = new String[]{
            "ROBOT", "ARM", "GrabMe", "SERVO0", "SERVO1", "SERVO2",
            "SERVO3", "SERVO4", "SERVO51",  "SERVO52"};
        transformDEF = new String[]{
            "TR01", "TR02", "TR1", "TR2", "TR3", "TR4", 
            "TR51", "TR52", "TR61", "TR62"
        };
        // map for storing the initial configuration
        backup = new HashMap<String, BackupNode>();
        
        // launch the RL algorithm's thread
        RLAlgorithm = new GeneticAlgorithm(this);
        RLAlgorithm.start();
    }
    
    public void run(){
        
        // Backup startup configuration
        createBackup();
        
        do {
            if(needSimulation()){
                // Restore startup configuration for new test
                //restoreFromBackup();
                //simulationPhysicsReset();
                
                // write instruction into file
                // then call step so the controller can work with the data
                simulateOnController();
                
                // TODO: read current config
                
                // finalize simulation
                // as soon as the simulation done, the GA should check for the result automatically
                doneSimulation();
            }
        } while (step(Util.TIME_STEP) != -1);
    }
    
    public void finalize(){
        super.finalize();
        RLAlgorithm.interrupt();
    }
    
    private boolean needSimulation(){
        return instructions != null;
    }
    
    private void doneSimulation(){
        this.instructions = null;
        // TODO: clear the file to controller
        System.out.println("Done simulation");
    }
    
    public boolean isSimulating(){
        return instructions != null;
    }
    
    public void simulate(double[] instructions){
        this.instructions = instructions;
    }
    
    private void simulateOnController(){
        // set data file
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(Util.commonFilePath));
            for(double val : instructions){
                bw.write(Double.toString(val));
                bw.newLine();
            }
            bw.close();
        } catch (IOException ex) {
        }
        System.out.println("Got sim instructions : "+Arrays.toString(instructions));
        
        // step
        step(Util.TIME_STEP);
    }
    
    private void createBackup(){
        System.out.println("Creating backup");
        for(String def : objectDEF){
            BackupNode bn = new BackupNode(getFromDef(def), true);
            bn.backup();
            backup.put(def, bn);
        }
        for(String def : transformDEF){
            BackupNode bn = new BackupNode(getFromDef(def), false);
            bn.backup();
            backup.put(def, bn);
        }
    }
    
    private void restoreFromBackup(){
        System.out.println("Restoring from backup");
        for(BackupNode bn : backup.values()){
            bn.restore();
        }
    }
    
    
    public static void main(String[] args) {
        SuperController controller = new SuperController();
       System.out.println("Launching supercontroller");
            controller.run();
        
    }
}
