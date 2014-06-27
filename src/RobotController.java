

// File:          RobotController.java
// Date:
// Description:
// Author:
// Modifications:

import com.cyberbotics.webots.controller.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A backup of a servo, i.e. a class containing the basic information about a servo
 * and make it easier to backup and restore a servo's state
 */
class BackupServo {
    private Servo servo;
    private double position;
    public BackupServo(Servo s){
        servo = s;
    }
    public void backup(){
        position = servo.getPosition();
    }
    public void restore(){
        servo.setPosition(position);
    }
}
public class RobotController extends Robot {
    
    /**
     * Handle to the robot's base, gripper and arm
     */
    private Base base = null;
    private Arm arm = null;
    private Gripper gripper = null;
    /**
     * Handle to all devices used by the robot, including GPSs, sensor and emitter
     */
    private GPS[] gps = null;
    private TouchSensor[] touchsensor = null;
    private Emitter emitter = null;
    
    /** attribute used for transitional informations*/
    private double[] instructions = null, objPos = null, robotPos = null;
    private Map<String, BackupServo> backup = null;
    private boolean first = true;
    
    /** Devices name*/
    String[] servoName = new String[] {
            "finger1", "finger2", "arm1", "arm2", "arm3", "arm4", "arm5",
            "wheel1", "wheel2", "wheel3", "wheel4"
        };
    /** simulation counter*/
    private int counter = 0;
        
    /**
     * Create the controller
     */
    public RobotController() {
        super();
        
        // initialization for working with simulation devices
        // Initialize the gripper
        gripper = new Gripper(
                getServo("finger1"),
                getServo("finger2"));
        // then the arm
        arm = new Arm(
                getServo("arm1"),
                getServo("arm2"),
                getServo("arm3"),
                getServo("arm4"),
                getServo("arm5")
                );
        // then the robot base
        base = new Base(
                getServo("wheel1"),
                getServo("wheel2"),
                getServo("wheel3"),
                getServo("wheel4")
                );
        // then the GPS
        gps = new GPS[4];
        for(int i=0; i<4; i++) {
            gps[i] = getGPS("gps"+Integer.toString(i));
            gps[i].enable(100);
        }
        // then the sensors
        touchsensor = new TouchSensor[]{
            getTouchSensor("finger_sensor1"), getTouchSensor("finger_sensor2")};
        touchsensor[0].enable(100);
        touchsensor[1].enable(100);
        // emitter for synchronization
        emitter = getEmitter("emitter");
        // prepare to backup
        backup = new HashMap<String, BackupServo>();
    }
    
    /**
     * Run the controller code. It will first create a backup, then loop
     * over the following instruction:
     * 1) check if any simulation is needed, if not, wait and check again later
     * 2) if there is any simulation needed, then read instruction
     * 3) if the a pickup is needed then grip the gripper and release whatever it has between its gripper on the backplate
     * 4) otherwise take an action
     * 5) write down into file the GPS, object and touch sensor result
     * 6) send a supervisor a signal that the simulation was done then go to step 1
     */
    public void run() {
        System.out.println("Controller launched");
        
        // Give supercontroller the privilege to run first
        createBackup();
        
        do {//System.out.println("gone further");
            if(needSimulation()){
                //restoreFromBackup();
                // read data from file
                readInstructions();
                if(instructions == null) continue;
                
                // prepare to record
                List<double[]> gpsPosition = new ArrayList<double[]>();
                List<double[]> sensor = new ArrayList<double[]>();
                
                gripper.release();
                
                if(needToPick()){
                    gripper.grip();
                    Util.passive_wait(this, 0.5);
                    arm.arm_set_height(Arm.ARM_BACK_PLATE_LOW);
                    Util.passive_wait(this, 1.5);
                    sensor.add(new double[]{-1});
                    double sens = Math.max(touchsensor[0].getValue(), touchsensor[1].getValue());
                    sensor.add(new double[]{sens});
                    gripper.release();
                    Util.passive_wait(this, 0.5);
                    for(int i=0; i<4; i++)
                        gpsPosition.add(new double[]{0.,0.,0.});
                }
                else{
                    //System.out.println(">>>> Positioning");
                    //arm.setPosition(null, adjustArm(1, instructions[1]), null, null, null);
                    arm.setPosition(
                            instructions[0],
                            instructions[1],
                            instructions[2],
                            instructions[3],
                            instructions[4]);
                    Util.passive_wait(this, 0.5);
                    for(int i=0; i<4; i++)
                        gpsPosition.add(gps[i].getValues());
                    double sens = Math.max(touchsensor[0].getValue(), touchsensor[1].getValue());
                    sensor.add(new double[]{sens});
                    sensor.add(new double[]{-1});
                }
                
                Util.writeFilePositionResult(Util.resultGPSFilePath, gpsPosition);
                Util.writeFilePositionResult(Util.resultSensorFilePath, sensor);
             
                //System.out.println("Sending message "+counter);
                sendMessage("DONE "+Integer.toString(counter++));
                Util.cleanFile(Util.commonFilePath);
            }
        } while (step(Util.TIME_STEP) != -1);
    }
    
    /**
     * Send a message over channel
     * @param message the message to be sent
     */
    public void sendMessage(String message){
        emitter.send(message.getBytes());
    }
    
    /**
     * Check whether any simulation is needed. Actually, a simulation is needed
     * when the instruction file is not empty
     * @return true if any simulation is needed
     */
    public boolean needSimulation() {
        try {
            File fl = new File(Util.commonFilePath);
            return (new BufferedReader(new FileReader(fl))).read() != -1;
        } catch (IOException ex) {
            Logger.getLogger(RobotController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    /**
     * Check whether a pickup is need or not. A pickup is needed, actually
     * when every part of the instruction is set to MAX_VALUE
     * @return true if any pickup is needed
     */
    public boolean needToPick(){
        for(int i=0; i<5; i++) 
            if(instructions[i]!=Double.MAX_VALUE)
                return false;
        return true;
    }
    
    /**
     * read the file instruction into an array of double[] - instructions
     */
    public void readInstructions(){
        List<Double> values = Util.listToDouble(Util.readFileResult(Util.commonFilePath));
        Util.cleanFile(Util.commonFilePath);
        instructions = null;
        if(values == null) return;
        instructions = new double[values.size()];
        int i=0;
        while(i<values.size()) {
            instructions[i] = values.get(i);
            i++;
        }
    }
    
    /**
     * Create a backup of the robot's servo
     */
    private void createBackup(){
        for(String name : servoName){
            BackupServo bn = new BackupServo(getServo(name));
            bn.backup();
            backup.put(name, bn);
        }
    }
    
    /**
     * Restore servo's state from backup
     */
    private void restoreFromBackup(){
        for(BackupServo bn : backup.values()){
            bn.restore();
        }
    }
    
    @Override
    public void finalize(){
        super.finalize();
        Util.deleteAllNeededFiles();
    }
    
    public static void main(String[] args) throws InterruptedException {
        Util.createAllNeededFiles();
        RobotController controller = new RobotController();
        controller.run();
    }
}
