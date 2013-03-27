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
    private GPS[] gps = null;
    private TouchSensor[] touchsensor = null;
    private Emitter emitter = null;
    private double[] instructions = null, objPos = null, robotPos = null;
    private Map<String, BackupServo> backup = null;
    private boolean endEpisode = false;
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
        writeSetting();
        writePosition();
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
                
                if(endEpisode){
                    pick();
                    endEpisode = false;
                }
                else{
                    action();
                }
                
                for(GPS _gps : gps) gpsPosition.add(_gps.getValues());
                sensor.add(new double[]{touchsensor[0].getValue(), touchsensor[1].getValue()});
                
                // write result to file
                Util.writeFilePositionResult(Util.resultGPSFilePath, gpsPosition);
                Util.writeFilePositionResult(Util.resultSensorFilePath, sensor);
                
                //System.out.println("Sending message "+counter);
                sendMessage("DONE "+Integer.toString(counter++));
                Util.cleanAllFile();
            }
        } while (step(Util.TIME_STEP) != -1);
    }
    
    private void sendMessage(String message){
        emitter.send(message.getBytes());
    }
    
    private void writeSetting(){
        Util.writeFileDouble(Util.servoSettingFilePath, Arrays.asList(arm.getSettings()));
    }
    
    private void writePosition(){
        Util.writeFileDouble(Util.servoPositionPath, Arrays.asList(arm.getPositions()));
    }
    
    private void pick(){
        gripper.grip();
        Util.passive_wait(this, 0.5);
        arm.arm_set_height(1);
        Util.passive_wait(this, 2.0);
        if(touchsensor[0].getValue()==touchsensor[1].getValue()){
            if(touchsensor[0].getValue()==1.0){
                sendMessage("GOOD");
            }
            else sendMessage("BAD");
        }
        else sendMessage("BAD");
        gripper.release();
        Util.passive_wait(this, 0.25);
        arm.arm_reset();
    }
    
    private void action(){
        
        // rotate and initArm
        System.out.println(">>>> MOVING "+Arrays.toString(instructions));
        //step(Util.TIME_STEP);
        for(int i=1; i<5; i++)
            arm.move(i, instructions[i]);
        Util.passive_wait(this, 0.5);
        
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
        instructions = new double[values.size()];
        int i=0;
        while(i<values.size()) {
            instructions[i] = values.get(i);
            i++;
        }
        if(instructions[0] == 2*Math.PI) endEpisode = true;
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
