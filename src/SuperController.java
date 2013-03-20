

import com.cyberbotics.webots.controller.Node;
import com.cyberbotics.webots.controller.Supervisor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class NodeField{
    public double[] translation = null;
    public double[] rotation = null;
}

public class SuperController extends Supervisor {
    
    public InstructionFitnessFunction fitness = null;
    private Map<String, NodeField> backup = null;
    private Map<String, List> results = null;
    private double[] instructions = null;
    private String[] objectDEFs = null;
    private Thread RLAlgorithm = null;
    
    public SuperController() {
        super();
        // array containing DEF of element to backup
        objectDEFs = new String[]{"ROBOT", "GrabMe"};
        // dictionnary for storing fields value
        backup = new HashMap<String, NodeField>();
        // launch the RL algorithm's thread
        RLAlgorithm = new GeneticAlgorithm(this);
        RLAlgorithm.start();
        // dicitonary for results
        // it should contain a pair <"Name of the device", it's value during each step>
        results = new HashMap<String, List>();
    }
    
    public void run(){
        
        // Backup startup configuration
        createBackup();
        int counter = 0;
        
        do {
            synchronized (this){
                try {
                    // wait untill the instruction array is ready
                    this.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(SuperController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            System.out.println("Received chromosome No "+counter+++" : "+Util.InstructionChromosome(instructions));
            // Restore startup configuration for new test
            restoreFromBackup();
            simulationPhysicsReset();
            
            // write instruction into file, then step along with robot controller
            simulateOnController();
            
            // simulate and record result into result dictionary
            List<double[]> toGrabPosition = new ArrayList<double[]>();
            List<double[]> robotPosition = new ArrayList<double[]>();
            
            System.out.println(">>>> Initializing");
            Util.passive_wait(this, 1.5);
            toGrabPosition.add(getPosition("GrabMe"));;
            robotPosition.add(getPosition("ROBOT"));

            System.out.println(">>>> Positioning");
            Util.passive_wait(this, 1.5);
            toGrabPosition.add(getPosition("GrabMe"));;
            robotPosition.add(getPosition("ROBOT"));

            System.out.println(">>>> Picking");
            Util.passive_wait(this, 1.0);
            toGrabPosition.add(getPosition("GrabMe"));;
            robotPosition.add(getPosition("ROBOT"));

            System.out.println(">>>> Bringing");
            Util.passive_wait(this, 1.0);
            toGrabPosition.add(getPosition("GrabMe"));;
            robotPosition.add(getPosition("ROBOT"));

            System.out.println(">>>> Puting");
            Util.passive_wait(this, 1.5);
            toGrabPosition.add(getPosition("GrabMe"));;
            robotPosition.add(getPosition("ROBOT"));

            synchronized (((GeneticAlgorithm)RLAlgorithm).fitnessFunction){
                ((GeneticAlgorithm)RLAlgorithm).fitnessFunction.notify();
            }
        } while (step(Util.TIME_STEP) != -1);
    }
    
    public void finalize(){
        super.finalize();
        RLAlgorithm.interrupt();
    }
    
    public void simulate(double[] instructions){
        this.instructions = instructions;
    }
    
    private void simulateOnController(){
        // set data file
        try {
            BufferedWriter bw = new BufferedWriter(
                    new FileWriter(Util.commonFilePath));
            for(double val : instructions){
                bw.write(Double.toString(val));
                bw.newLine();
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(SuperController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void createBackup(){
        for(String def : objectDEFs){
            Node node = getFromDef(def);
            NodeField nf = new NodeField();
            nf.translation = node.getField("translation").getSFVec3f().clone();
            nf.rotation = node.getField("rotation").getSFRotation().clone();
            backup.put(def, nf);
        }
    }
    
    private void restoreFromBackup(){
        for(String def : backup.keySet()){
            Node node = getFromDef(def);
            NodeField nf = backup.get(def);
            node.getField("translation").setSFVec3f(nf.translation.clone());
            node.getField("rotation").setSFRotation(nf.rotation.clone());
        }
    }
    
    private double[] getPosition(String def){
        return getFromDef(def).getField("translation").getSFVec3f().clone();
    }
    
    public static void main(String[] args) {
        SuperController controller = new SuperController();
        controller.run();
        
    }
}
