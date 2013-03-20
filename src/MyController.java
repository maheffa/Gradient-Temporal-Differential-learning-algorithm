

// File:          MyController.java
// Date:
// Description:
// Author:
// Modifications:

import com.cyberbotics.webots.controller.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    String[] servoName = new String[] {
            "finger1", "finger2", "arm1", "arm2", "arm3", "arm4", "arm5"
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
        // prepare to backup
        backup = new HashMap<String, BackupServo>();
    }
    
    public void run() {
        // Give supercontroller the privilege to run first
        createBackup();
        step(Util.TIME_STEP);
        do {
            if(needSimulation()){
                restoreFromBackup();
                // read data from file
                readInstructions();
                // TODO: adjust robot
                System.out.println("Simulation on "+Arrays.toString(instructions));
                // step to let the supervisor finish his part
                step(Util.TIME_STEP);
            }
            else {
                System.out.println("Nothing to do");
            }
        } while (step(Util.TIME_STEP) != -1);
    }
    
    private boolean needSimulation() {
        return (new File(Util.commonFilePath)).length() != 0;
    }
    
    private void readInstructions(){
        ArrayList<Double> values = new ArrayList<Double>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(Util.commonFilePath));
            String inputLine;
            while((inputLine = br.readLine()) != null){
                values.add(Double.parseDouble(inputLine));
            }
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
        MyController controller = new MyController();
        controller.run();
    }
}
