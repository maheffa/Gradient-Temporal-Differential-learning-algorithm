

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
    
    private Map<String, NodeField> backup = null;
    private Map<String, List<double[]>> results = null;
    private Double[] instructions = null;
    private String[] objectDEFs = null;
    private Agent RLAgent = null;
    private Receiver receiver = null;
    private int counter = 0;
    private double[] servoMin, servoMax, servoPosition;
    private int[] servoDivision = new int[]{1, 10, 10, 10, 2};
    
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
        RLAgent = new Agent(this);
        RLAgent.start();
        // dicitonary for results
        // it should contain a pair <"Name of the device", it's value during each step>
        results = new HashMap<String, List<double[]>>();
        readSetting();
        readPosition();
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
    
    public StateFeature applyAction(Action action){
        //System.out.println("Simulating");
        instructions = actionToInstruction(action);
        // Restore startup configuration for new test
        boolean pickSuccess = false;
        if(action == Action.PICK){
            simulatePickup(instructions);
            restoreFromBackup();
            simulationPhysicsReset();
            pickSuccess = waitMessage("GOOD");
        }
        else{
            simulateAction(instructions);
        }
        
        // get result
        double[] toGrabPosition = getPosition("GrabMe");
        List<double[]> gpsPosition = null;
        List<double[]> sensorValues = null;
        
        waitMessage("DONE "+Integer.toString(counter++));
        System.out.println("Got message "+(counter-1));
        gpsPosition = Util.readFilePositionResult(Util.resultGPSFilePath, 3);
        sensorValues = Util.readFilePositionResult(Util.resultSensorFilePath, 2);
        double[] sensorVal = sensorValues.get(0);
        
        // write down result to StateFeature
        
        StateFeature newState = new StateFeature(
                gpsPosition,
                toGrabPosition,
                sensorVal[0]==sensorVal[1]?(sensorVal[0]==1.0?true:false):false,
                pickSuccess?1:-1);
        
        return newState;
    }
    
    private Double[] actionToInstruction(Action a){
        int index = 0, sign = 0;
        Double[] dMovement = new Double[servoDivision.length];
        switch(a){
            case Servo0_UP: sign = +1;
            case Servo0_DOWN: sign = -1; index = 1; break;
            case Servo1_UP: sign = +1;
            case Servo1_DOWN: sign = -1; index = 2; break;
            case Servo2_UP: sign = +1;
            case Servo2_DOWN: sign = -1; index = 3; break;
            case Servo3_UP: sign = +1;
            case Servo3_DOWN: sign = -1; index = 4; break;
            case PICK: dMovement[0] = 2*Math.PI; break;
            case NONE: break;
        }
        if(index>0)
            dMovement[index] = sign*(servoMax[index]-servoMin[index])/((double)servoDivision[index]);
        return dMovement;
    }
    
    private void readSetting(){
        List<Double> servoSetting = Util.listToDouble(Util.readFileResult(Util.servoSettingFilePath));
        servoMin = new double[servoSetting.size()/2];
        servoMax = new double[servoSetting.size()/2];
        for(int i=0,j=0; i<servoSetting.size(); ){
            servoMin[j] = servoSetting.get(i++);
            servoMax[j++] = servoSetting.get(i++);
        }
    }
    
    private void readPosition(){
        List<Double> servoPos = Util.listToDouble(Util.readFileResult(Util.servoPositionPath));
        servoPosition = new double[servoPos.size()];
        for(int i=0; i<servoPosition.length; i++){
            servoPosition[i] = servoPos.get(i);
        }
    }
    
    public boolean isMaxServo(int index){
        return servoPosition[index+1]>=servoMax[index+1];
    }
    
    public boolean isMinServo(int index){
        return servoPosition[index+1]<=servoMin[index+1];
    }
    
    private boolean waitMessage(String message){
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
    
    private void simulateAction(Double[] instructions){
        Util.writeFileDouble(Util.commonFilePath, Arrays.asList(instructions));
        Util.passive_wait(this, 0.5);
    }
    
    private void simulatePickup(Double[] instructions){
        Double[] pick = new Double[instructions.length];
        pick[0] = 2*Math.PI;
        Util.writeFileDouble(Util.commonFilePath, Arrays.asList(instructions));
        Util.passive_wait(this, 2.75);
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
