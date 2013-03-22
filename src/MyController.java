

// File:          MyController.java
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
public class MyController extends Robot {
    
    private Base base = null;
    private Arm arm = null;
    private Gripper gripper = null;
    private GPS gps = null;
    private TouchSensor[] touchsensor = null;
    private Emitter emitter = null;
    private double[] instructions = null, objPos = null;
    private Map<String, BackupServo> backup = null;
    private boolean first = true;
    String[] servoName = new String[] {
            "finger1", "finger2", "arm1", "arm2", "arm3", "arm4", "arm5",
            "wheel1", "wheel2", "wheel3", "wheel4"
        };
    private int counter = 0;
        
    public MyController() {
        super();
        
        // initialization for working with simulation devices
        // Initialize the gripper
        gripper = new Gripper(
                getServo("finger1"),
                getServo("finger2")
                );
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
        gps = getGPS("finger_gps");
        gps.enable(100);
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
                
                System.out.println("SIMULATION "+counter+" ON "+Util.InstructionChromosome(instructions));
                
                // rotate and initArm
                base.setWheelSpeeds(Math.abs(instructions[0]));
                if(instructions[0]>0) base.backwards();
                else base.forwards();
                arm.arm_reset();
                System.out.println(">>>> Initializing");
                //step(Util.TIME_STEP); 
                Util.passive_wait(this, 1.5);
                gpsPosition.add(gps.getValues()); //System.out.println("[GPS] "+Arrays.toString(gps.getValues()));
                sensor.add(new double[]{touchsensor[0].getValue(), touchsensor[1].getValue()});
                
                // go straigth and positionArm
                base.reset();
                arm.setPosition(null, instructions[1], 
                        instructions[2], instructions[3], instructions[4]);
                System.out.println(">>>> Positioning");
                //step(Util.TIME_STEP); 
                Util.passive_wait(this, 1.5);
                gpsPosition.add(gps.getValues()); //System.out.println("[GPS] "+Arrays.toString(gps.getValues()));
                sensor.add(new double[]{touchsensor[0].getValue(), touchsensor[1].getValue()});
                
                // stop and pick
                base.reset();
                arm.setPosition(null, null, null, adjustArm(), null);
                gripper.grip();
                System.out.println(">>>> Picking");
                //step(Util.TIME_STEP); 
                Util.passive_wait(this, 1.0);
                gpsPosition.add(gps.getValues()); //System.out.println("[GPS] "+Arrays.toString(gps.getValues()));
                sensor.add(new double[]{touchsensor[0].getValue(), touchsensor[1].getValue()});
                
                // put on plate
                arm.arm_set_height(Arm.ARM_BACK_PLATE_LOW);
                System.out.println(">>>> Bringing");
                //step(Util.TIME_STEP); 
                Util.passive_wait(this, 1.0);
                gpsPosition.add(gps.getValues()); //System.out.println("[GPS] "+Arrays.toString(gps.getValues()));
                sensor.add(new double[]{touchsensor[0].getValue(), touchsensor[1].getValue()});
                
                gripper.release();
                System.out.println(">>>> Puting");
                //step(Util.TIME_STEP);
                Util.passive_wait(this, 1.5);
                gpsPosition.add(gps.getValues()); //System.out.println("[GPS] "+Arrays.toString(gps.getValues()));
                sensor.add(new double[]{touchsensor[0].getValue(), touchsensor[1].getValue()});
                
                // write result to file
                Util.writeFilePositionResult(Util.resultGPSFilePath, gpsPosition);
                Util.writeFilePositionResult(Util.resultSensorFilePath, sensor);
             
                System.out.println("Sending message "+counter);
                sendMessage("DONE "+Integer.toString(counter++));
                Util.cleanAllFile();
            }
        } while (step(Util.TIME_STEP) != -1);
    }
    
    private void sendMessage(String message){
        emitter.send(message.getBytes());
    }
    
    private double adjustArm(){
        double[] gpsPos = gps.getValues();
        double[] OM = new double[3];
        System.out.println("[GPS] "+Arrays.toString(gpsPos));
        System.out.println("[OBJ] "+Arrays.toString(objPos));
        for(int i=0; i<3; i++) OM[i] = objPos[i]-gpsPos[i];
        double angle = Util.getZangle(OM);
        System.out.println("[angle] OM = "+angle+" - ARM = "+(instructions[1]+instructions[2]));
        double diffAngle = angle - (instructions[1]+instructions[2]);
        System.out.println("[angle] moving "+diffAngle);
        return diffAngle+Math.PI;
    }
    
    private boolean needSimulation() {
        try {
            File fl = new File(Util.commonFilePath);
            return (new BufferedReader(new FileReader(fl))).read() != -1;
        } catch (IOException ex) {
            Logger.getLogger(MyController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private void readInstructions(){
        List<Double> values = Util.listToDouble(Util.readFileResult(Util.commonFilePath));
        Util.cleanFile(Util.commonFilePath);
        instructions = null;
        if(values == null) return;
        instructions = new double[values.size()-3];
        objPos = new double[3];
        int i=0;
        while(i<3) {
            objPos[i] = values.get(i);
            i++;
        }
        while(i<values.size()) {
            instructions[(i)-3] = values.get(i);
            i++;
        }
    }
    
    private void createBackup(){
        for(String name : servoName){
            BackupServo bn = new BackupServo(getServo(name));
            bn.backup();
            backup.put(name, bn);
        }
    }
    
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
        MyController controller = new MyController();
        controller.run();
    }
}
