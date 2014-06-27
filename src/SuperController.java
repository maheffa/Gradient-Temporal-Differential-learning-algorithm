

import com.cyberbotics.webots.controller.Node;
import com.cyberbotics.webots.controller.Receiver;
import com.cyberbotics.webots.controller.Supervisor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Similar to RobotController's BackupServo class but much simpler
 * it just a pair translation-rotation, which is enough for restoring and
 * saving the robot's and the object's position in the Webots world
 */
class NodeField{
    public double[] translation = null;
    public double[] rotation = null;
}

/**
 * The supervisor class
 */
public class SuperController extends Supervisor {
    
    /** map for storing device-configuration setting*/
    private Map<String, NodeField> backup = null;
    /** result from simulation*/
    private Map<String, List<double[]>> results = null;
    /** instruction to simulate*/
    private Double[] instructions = null;
    /** objects to be backed-up DEF-tag name*/
    private String[] objectDEFs = null;
    /** handle to GTD agent*/
    private Agent RLAgent = null;
    /** receiver device for receiving information from controller*/
    private Receiver receiver = null;
    /** simulation counter*/
    private int counter = 0;
    /** servo pseudo-state as the agent understand it*/
    private Integer[] servoDivision = null, position = null;
    
    /**
     * Create a supervisor
     */
    public SuperController(){
        super();
        // array containing DEF of element to backup
        objectDEFs = new String[]{"ROBOT", "GrabMe"};
        // dictionnary for storing fields value
        backup = new HashMap<String, NodeField>();
        // receiver for synchronization
        receiver = getReceiver("receiver");
        receiver.enable(1);
        // launch the RL algorithm's thread
        RLAgent = new Agent(this);
        RLAgent.start();
        // dicitonary for results
        // it should contain a pair <"Name of the device", it's value during each step>
        results = new HashMap<String, List<double[]>>();
    }
    
    /**
     * run the supervisor code.
     * Actually, this code don't do anything but trying to keep the
     * supervisor instance alive in the memory. The actual simulation is done
     * by the method simulate. After a simulation, the GTD agent will notify this
     * class, so one loop inside the run() method will be executed
     * @throws InterruptedException 
     */
    public void run() throws InterruptedException{
        System.out.println("Controller launched");
       
        // Backup startup configuration
        createBackup();
        do{
            synchronized(this){
                /**
                 * Wait until a simulation is done
                 */
                wait();
            }
        } while(step(Util.TIME_STEP)!=-1);
    }
    
    /**
     * simulate a position of the arm, according the parameters content
     * @param instructions the instruction to be simulated, or the position of the arm to set
     * @return a result containing all devices value after the simulation
     */
    public synchronized StateFeature simulate(Double[] positions, boolean pick){
        int success = 0;
        double[] objectPosition0 = getPosition("GrabMe");
                
        if(pick) {
            /**
             * If the robot need to simulate a pickup,
             * then put every instruction to MAX_VALUE
             */
            instructions = new Double[5];
            for(int i=0; i<instructions.length; i++) instructions[i] = Double.MAX_VALUE;
        }
        else{
            /**
             * Otherwise just do as it was written
             */
            this.instructions = positions;
        }
        
        // write instruction into instruction file, then step along with robot controller
        System.out.println("[SIMULATE]");
        simulateOnController();
        
        if(pick){
            /**
             * The simulation should last 2 second if it's a pickup
             */
            Util.passive_wait(this, 2.0);
        }
        else{
            /**
             * Otherwise half a second
             */
            Util.passive_wait(this, 0.5);
        }        
        
        List<double[]> gpsPosition;
        List<double[]> sensorValues;
        
        waitMessage("DONE "+Integer.toString(counter++));
        //System.out.println("Got message "+(counter-1));
        gpsPosition = Util.readFilePositionResult(Util.resultGPSFilePath, 3);
        sensorValues = Util.readFilePositionResult(Util.resultSensorFilePath, 1);
        
        if(pick){
            /**
             * If we picked up the object, then verify if any of the touch sensor
             * was triggered, i.e. the object was touched and grabbed
             */
            if(sensorValues.get(0)[0]>0||sensorValues.get(1)[0]>0){
                /**
                 * If so then set success to 1 
                 */
                success = 1;
            }
            else {
                /**
                 * Otherwise to -1
                 */
                success = -1;
            }
        }
        else{
            /**
             * If we don't pick anything, it means that the object then shouldn't move
             */
            success = 0;
            double[] objectPosition1 = getPosition("GrabMe");
            for(int i=0; i<objectPosition0.length; i++) {
                if(TinyMath.distance(objectPosition0, objectPosition1) > 0.1){
                    /**
                     * If the object moved, it means that we touched it, and it might not
                     * be on the same position as it should be
                     * so we set that situation as a failure
                     */
                    success = -1;
                    break;
                }
            }
        }
        
        /**
         * Return a stateFeature according to collected and deducted data
         */
        return new StateFeature(
                gpsPosition, 
                objectPosition0, 
                success);
    }
    
    /**
     * Reset the environment from backup
     */
    public void reset(){
        restoreFromBackup();
        simulationPhysicsReset();
    }
    
    /**
     * Wait for a message from an Emitter. If the message obtained is not what 
     * we waited for, then it outputs an error to the console
     * @param message the message to be waited for
     */
    public boolean waitMessage(String message){
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
            return false;
        }
        return true;
    }
    
    /**
     * Simulate the instruction on controller.
     * Actually, it just write the instruction into instruction file. The simulator
     * , then, when he sees that the instruction file is not empty, will interprete
     * the instruction written into the file
     */
    public void simulateOnController(){
        // set data file
        List<Double> l = new ArrayList<Double>();
        for(double val : instructions)
            l.add(Double.valueOf(val));
        Util.writeFileDouble(Util.commonFilePath, l);
    }
    
    /**
     * Create the objectDEF-tagged devices backup
     */
    public void createBackup(){
        for(String def : objectDEFs){
            Node node = getFromDef(def);
            NodeField nf = new NodeField();
            nf.translation = node.getField("translation").getSFVec3f().clone();
            nf.rotation = node.getField("rotation").getSFRotation().clone();
            backup.put(def, nf);
        }
    }
    
    /**
     * Restore information backed-up by the createBackup() method
     */
    public void restoreFromBackup(){
        for(String def : backup.keySet()){
            Node node = getFromDef(def);
            NodeField nf = backup.get(def);
            node.getField("translation").setSFVec3f(nf.translation.clone());
            node.getField("rotation").setSFRotation(nf.rotation.clone());
        }
    }
    
    public double[] getPosition(String def){
        //System.out.println("[TAG] ["+def+"] = "+Arrays.toString(getFromDef(def).getField("translation").getSFVec3f()));
        return getFromDef(def).getField("translation").getSFVec3f().clone();
    }
    
    public static void main(String[] args) throws InterruptedException {
        SuperController controller = new SuperController();
        controller.run();
    }
}
