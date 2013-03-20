

// File:          MyController.java
// Date:
// Description:
// Author:
// Modifications:

import com.cyberbotics.webots.controller.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
    private double[] instructions = null;
    private Map<String, BackupServo> backup = null;
    private boolean first = true;
    String[] servoName = new String[] {
            "finger1", "finger2", "arm1", "arm2", "arm3", "arm4", "arm5",
            "wheel1", "wheel2", "wheel3", "wheel4"
        };
        
    public MyController() {
        super();
        
        // initialization for working with simulation devices
        gripper = new Gripper(
                getServo("finger1"),
                getServo("finger2")
                );
        arm = new Arm(
                getServo("arm1"),
                getServo("arm2"),
                getServo("arm3"),
                getServo("arm4"),
                getServo("arm5")
                );
        base = new Base(
                getServo("wheel1"),
                getServo("wheel2"),
                getServo("wheel3"),
                getServo("wheel4")
                );
        // prepare to backup
        backup = new HashMap<String, BackupServo>();
    }
    
    public void run() {
        // Give supercontroller the privilege to run first
        createBackup();
        int counter = 0;
        
        do {//System.out.println("gone further");
            if(needSimulation()){
                //restoreFromBackup();
                // read data from file
                readInstructions();
                if(instructions == null){
                    Util.passive_wait(this, 6.5);
                    continue;
                }
                
                System.out.println("SIMULATION "+counter+++" ON "+Util.InstructionChromosome(instructions));
                
                // rotate and initArm
                base.setWheelSpeeds(Math.abs(instructions[0]));
                if(instructions[0]>0) base.turn_left();
                else base.turn_right();
                arm.arm_reset();
                System.out.println(">>>> Initializing");
                Util.passive_wait(this, 1.5);
                
                // go straigth and positionArm
                base.setWheelSpeeds(Math.abs(instructions[1]));
                if(instructions[1]>0) base.forwards();
                else base.backwards();
                arm.setPosition(instructions[2], instructions[3], 
                        instructions[4], instructions[5], instructions[6]);
                System.out.println(">>>> Positioning");
                Util.passive_wait(this, 1.5);
                
                // stop and pick
                base.reset();
                gripper.grip();
                System.out.println(">>>> Picking");
                Util.passive_wait(this, 1.0);
                
                // put on plate
                arm.arm_set_height(Arm.ARM_BACK_PLATE_LOW);
                System.out.println(">>>> Bringing");
                Util.passive_wait(this, 1.0);
                gripper.release();
                System.out.println(">>>> Puting");
                Util.passive_wait(this, 1.5);
                //System.out.println("Done simulation ??");
            }
        } while (step(Util.TIME_STEP) != -1);
    }
    
    private boolean needSimulation() {
        try {
            File fl = new File(Util.commonFilePath);
            if(fl.exists())
                return (new BufferedReader(new FileReader(fl))).read() != -1;
            else
                return false;
        } catch (IOException ex) {
            Logger.getLogger(MyController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private void readInstructions(){
        List<Double> values;
        try {
            values = Util.listToDouble(Util.readFileResult(Util.commonFilePath));
            Util.cleanFile(Util.commonFilePath);
            instructions = null;
            if(values == null) return;
            instructions = new double[values.size()];
            int i=0;
            for(Double val : values){
                instructions[i++] = val;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void createBackup(){
        System.out.println("Creating backup");
        for(String name : servoName){
            BackupServo bn = new BackupServo(getServo(name));
            bn.backup();
            backup.put(name, bn);
        }
    }
    
    private void restoreFromBackup(){
        System.out.println("Restoring from backup");
        for(BackupServo bn : backup.values()){
            bn.restore();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        createAllNeededFiles();
        MyController controller = new MyController();
        controller.run();
    }
    
    public static void createAllNeededFiles(){
        try {
            File fl ;
            fl = new File(Util.commonFilePath);
            if(!fl.exists()) fl.createNewFile();
            fl = new File(Util.resultFilePath);
            if(!fl.exists()) fl.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(MyController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
