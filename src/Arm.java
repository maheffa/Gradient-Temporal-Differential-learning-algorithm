

import com.cyberbotics.webots.controller.Servo;
import java.util.ArrayList;
import java.util.List;

/**
 * The Arm class is a warehouse for the robot arm. It encompasses all
 * handle to robots arm, making it easier to controls as one object
 * rather than a separated  Servo
 */
public class Arm {
    public final static int ARM_BACK_PLATE_HIGH = 0, ARM_BACK_PLATE_LOW = 1;
    private int ARM1 = 0, ARM2 = 1, ARM3 = 2, ARM4 = 3, ARM5 = 4;
    private Servo[] arms;
    
    /**
     * A constructor which take all five Servo present on Youbot robot as parameter
     */
    public Arm(Servo arm1, Servo arm2, Servo arm3, Servo arm4, Servo arm5){
        arms = new Servo[]{arm1, arm2, arm3, arm4, arm5};
    }
    
    /**
     * Reposition the arm to a straight position
     */
    public void arm_reset(){
        setPosition(0.0, 0.0, 0.0, 0.0, 0.7);
    }
    
    /**
     * Reposition the object with a certain given angle for each Servo
     * @param hArm1 angle for first Servo
     * @param hArm2 angle for 2nd Servo
     * @param hArm3 angle for 3rd Servo
     * @param hArm4 angle for 4th Servo
     * @param hArm5 angle for 5th Servo
     */
    public void setPosition(Double hArm1, Double hArm2, Double hArm3, Double hArm4, Double hArm5){
        if(hArm1!=null) arms[ARM1].setPosition(hArm1);
        if(hArm2!=null) arms[ARM2].setPosition(hArm2);
        if(hArm3!=null) arms[ARM3].setPosition(hArm3);
        if(hArm4!=null) arms[ARM4].setPosition(hArm4);
        if(hArm5!=null) arms[ARM5].setPosition(hArm5);
    }
    
    /**
     * Set the robot's arm position to a certain predefined position
     * @param height take 0 or 1, 0 will position the arm high in the back plate,
     * 1 will position it low. Necessary when the object is needed to be placed
     * on the plate
     */
    public void arm_set_height(int height){
        switch(height){
            case ARM_BACK_PLATE_HIGH:
                setPosition(0.0, 0.678, 0.682, 1.74, 0.7);
                break;
            case ARM_BACK_PLATE_LOW:
                setPosition(0.0, 0.8, 0.2, 1.7, 0.7);
                break;
        }
    }
    
    public List<Double> getPositions(){
        List<Double> positions = new ArrayList<Double>();
        for(int i=0; i<arms.length; i++) {
            positions.add(arms[i].getPosition());
            System.out.println("added position "+arms[i].getPosition());
        }
        return positions;
    }
}
