

import com.cyberbotics.webots.controller.Node;
import com.cyberbotics.webots.controller.Receiver;
import com.cyberbotics.webots.controller.Supervisor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class NodeField{
    public double[] translation = null;
    public double[] rotation = null;
}

public class SuperController extends Supervisor {
    
    public InstructionFitnessFunction fitness = null;
    private Map<String, NodeField> backup = null;
    private Map<String, List<double[]>> results = null;
    private double[] instructions = null;
    private String[] objectDEFs = null;
    private Thread RLAlgorithm = null;
    private Receiver receiver = null;
    private int counter = 0;
    
    public SuperController() {
        super();
        // array containing DEF of element to backup
        objectDEFs = new String[]{"ROBOT", "GrabMe"};
        // dictionnary for storing fields value
        backup = new HashMap<String, NodeField>();
        // receiver for synchronization
        receiver = getReceiver("receiver");
        receiver.enable(1);
        // launch the RL algorithm's thread
        RLAlgorithm = new GeneticAlgorithm(this);
        RLAlgorithm.start();
        // dicitonary for results
        // it should contain a pair <"Name of the device", it's value during each step>
        results = new HashMap<String, List<double[]>>();
    }
    
    public void run() throws InterruptedException{
        System.out.println("Controller launched");
       
        // Backup startup configuration
        createBackup();
        do{
            synchronized(this){
                //System.out.println("Waiting for new job");
                wait();
            }
        } while(step(Util.TIME_STEP)!=-1);
        
    }
    
    public synchronized Map<String, List<double[]>> simulate(double[] instructions){
        //System.out.println("Simulating");
        this.instructions = instructions;
        // Restore startup configuration for new test
        restoreFromBackup();
        simulationPhysicsReset();
        
        // write instruction into file, then step along with robot controller
        simulateOnController();
        
        // simulate and record result into result dictionary
        List<double[]> toGrabPosition = new ArrayList<double[]>();
        List<double[]> robotPosition = new ArrayList<double[]>();
        List<double[]> gpsPosition = null;
        List<double[]> sensorValues = null;
        
        System.out.println(">>>> Initializing");
        //step(Util.TIME_STEP);
        Util.passive_wait(this, 1.5);
        toGrabPosition.add(getPosition("GrabMe"));
        robotPosition.add(getPosition("ROBOT"));
        //System.out.println("[OBJECT] "+Arrays.toString(getPosition("GrabMe")));
        
        System.out.println(">>>> Positioning");
        //step(Util.TIME_STEP);
        Util.passive_wait(this, 1.5);
        toGrabPosition.add(getPosition("GrabMe"));;
        robotPosition.add(getPosition("ROBOT"));
        //System.out.println("[OBJECT] "+Arrays.toString(getPosition("GrabMe")));
        
        System.out.println(">>>> Picking");
        //step(Util.TIME_STEP);
        Util.passive_wait(this, 1.0);
        toGrabPosition.add(getPosition("GrabMe"));;
        robotPosition.add(getPosition("ROBOT"));
        //System.out.println("[OBJECT] "+Arrays.toString(getPosition("GrabMe")));
        
        System.out.println(">>>> Bringing");
        //step(Util.TIME_STEP);
        Util.passive_wait(this, 1.0);
        toGrabPosition.add(getPosition("GrabMe"));;
        robotPosition.add(getPosition("ROBOT"));
        //System.out.println("[OBJECT] "+Arrays.toString(getPosition("GrabMe")));
        
        System.out.println(">>>> Puting");
        //step(Util.TIME_STEP);
        Util.passive_wait(this, 1.5);
        toGrabPosition.add(getPosition("GrabMe"));;
        robotPosition.add(getPosition("ROBOT"));
        //System.out.println("[OBJECT] "+Arrays.toString(getPosition("GrabMe")));
        
        waitMessage("DONE "+Integer.toString(counter++));
        System.out.println("Got message "+(counter-1));
        gpsPosition = Util.readFilePositionResult(Util.resultGPSFilePath, 3);
        sensorValues = Util.readFilePositionResult(Util.resultSensorFilePath, 2);

        // write down result into hashmap
        results.put("ROBOT", robotPosition);
        results.put("OBJECT", toGrabPosition);
        results.put("GPS", gpsPosition);
        results.put("SENSORS", sensorValues);
        
        
        ////step(Util.TIME_STEP);
        //Util.readMapData(results);
        return results;
    }
    
    @Override
    public void finalize(){
        super.finalize();
        RLAlgorithm.interrupt();
        Util.deleteAllNeededFiles();
    }
    
    private void waitMessage(String message){
        byte[] input = null;
        boolean wait = true;
        do{
            wait = false;
            try{
                input = receiver.getData();
                receiver.nextPacket();
                
            } catch(NegativeArraySizeException ex){
                wait = true;
                Util.passive_wait(this, 0.001);
            }
        } while(wait);
        String str = new String(input);
        if(!str.equals(message)){
            System.err.println("Got wrong message: [message] "+str);
        }
    }
    
    private void simulateOnController(){
        // set data file
        List<Double> l = new ArrayList<Double>();
        double[] v = getPosition("GrabMe");
        // send the bottle position first
        for(double val : v){
            System.out.println("Down "+val);
            l.add(Double.valueOf(val));
        }
        // then the instruction
        System.out.println("CHROMOSOME "+Util.InstructionChromosome(instructions));
        for(double val : instructions)
            l.add(Double.valueOf(val));
        Util.writeFileDouble(Util.commonFilePath, l);
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
        //System.out.println("[TAG] ["+def+"] = "+Arrays.toString(getFromDef(def).getField("translation").getSFVec3f()));
        return getFromDef(def).getField("translation").getSFVec3f().clone();
    }
    
    public static void main(String[] args) throws InterruptedException {
        SuperController controller = new SuperController();
        controller.run();
    }
}
