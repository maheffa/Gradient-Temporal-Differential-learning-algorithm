

import com.cyberbotics.webots.controller.Node;
import com.cyberbotics.webots.controller.Servo;
import com.cyberbotics.webots.controller.Supervisor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class BackupNode{
    private Node n;
    private double[] translation = null, rotation = null;
    public BackupNode(Node n){
        this.n = n;
    }
    public void backup(){
        translation = n.getField("translation").getSFVec3f().clone();
        rotation = n.getField("rotation").getSFRotation().clone();
    }
    public void restore(){
        n.getField("translation").setSFVec3f(translation.clone());
        n.getField("rotation").setSFRotation(rotation.clone());
    }
}

class KeyObjects{
    private Node grabber, grabme, plate;
    SuperController sup;
    public KeyObjects(SuperController supercontroller){
        sup = supercontroller;
        renew();
    }
    public void renew(){
        grabber = sup.getFromDef("ROBOT");
        grabme = sup.getFromDef("GrabMe");
        plate = sup.getFromDef("Plate_PH");
    }
    public double[] getGrabberPosition(){
        return grabber.getField("translation").getSFVec3f();
    }
    public double[] getGrabmePosition(){
        return grabme.getField("translation").getSFVec3f();
    }
    public double[] getPlatePosition(){
        return plate.getField("centerOfMass").getSFVec3f();
    }
}

public class SuperController extends Supervisor {
    
    private Map<String, BackupNode> backup = null;
    private String[] objectDEF = null, transformDEF = null, servoName = null;
    private Thread RLAlgorithm = null;
    private double[] instructions = null;
    ArrayList<double[]> results = null;
    KeyObjects keyObject = null;
    private boolean simulating = false;
    public InstructionFitnessFunction fitness = null;
    
    public SuperController() {
        super();
        // array of string containing all DEF tag of devices on webot world
        objectDEF = new String[]{"ROBOT", "GrabMe"};
        // map for storing the initial configuration
        backup = new HashMap<String, BackupNode>();
        // launch the RL algorithm's thread
        RLAlgorithm = new GeneticAlgorithm(this);
        RLAlgorithm.start();
        // initialize key objects
        keyObject = new KeyObjects(this);
    }
    
    public void run(){
        
        // Backup startup configuration
        createBackup();
        int counter = 0;
        //Util.passive_wait(this, 6.5);
        do {
            //System.out.println("yep, further");
            while(instructions == null);
            simulating = true;
            results = new ArrayList<double[]>();
            //System.out.println("CHROMOSOME "+counter+++" = "+Util.InstructionChromosome(instructions));
            // Restore startup configuration for new test
            restoreFromBackup();
            simulationPhysicsReset();
            
            // write instruction into file
            // then call step so the controller can work with the data
            simulateOnController();
            // record data
            System.out.println(">>>> Initializing");
            Util.passive_wait(this, 1.5);
            keyObject.renew();
            results.add(keyObject.getGrabberPosition());
            results.add(keyObject.getGrabmePosition());
            System.out.println("grab-grabber distance = "
                    +Util.distance(results.get(0), results.get(1))
                    +" "+Arrays.toString(results.get(0))+":"
                    +Arrays.toString(results.get(1)));
            System.out.println(">>>> Positioning");
            Util.passive_wait(this, 1.5);
            keyObject.renew();
            results.add(keyObject.getGrabberPosition());
            results.add(keyObject.getGrabmePosition());
            System.out.println("grab-grabber distance = "
                    +Util.distance(results.get(2), results.get(3))
                    +" "+Arrays.toString(results.get(2))+":"
                    +Arrays.toString(results.get(3)));
            System.out.println(">>>> Picking");
            Util.passive_wait(this, 1.0);
            System.out.println(">>>> Bringing");
            Util.passive_wait(this, 1.0);
            System.out.println(">>>> Puting");
            Util.passive_wait(this, 1.5);
            keyObject.renew();
            results.add(keyObject.getPlatePosition());
            results.add(keyObject.getGrabmePosition());
            System.out.println("plate-grab distance = "
                    +Util.distance(results.get(4), results.get(5))
                    +" "+Arrays.toString(results.get(4))+":"
                    +Arrays.toString(results.get(5)));
            doneSimulation();
            
            synchronized (((GeneticAlgorithm)RLAlgorithm).fitnessFunction){
                ((GeneticAlgorithm)RLAlgorithm).fitnessFunction.notify();
            }
        } while (step(Util.TIME_STEP) != -1);
    }
    
    public void finalize(){
        super.finalize();
        RLAlgorithm.interrupt();
    }
    
    private boolean needSimulation(){
        //System.out.println("Need to simulate");
        return instructions != null;
    }
    
    private void doneSimulation(){
        instructions = null;
    }
    
    public boolean isSimulating(){
        return instructions != null;
    }
    
    public void simulate(double[] instructions){
        //System.out.println("Got to simulate");
        this.instructions = instructions;
    }
    
    private void simulateOnController(){
        // set data file
        try {
            File fl = new File(Util.commonFilePath);
            if(!fl.exists()) {
                System.out.println("created "+Util.commonFilePath);
                fl.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(fl));
            for(double val : instructions){
                bw.write(Double.toString(val));
                bw.newLine();
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(SuperController.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Got sim instructions : ");//+Arrays.toString(instructions));
    }
    
    private void createBackup(){
        System.out.println("Creating backup");
        for(String def : objectDEF){
            BackupNode bn = new BackupNode(getFromDef(def));
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
