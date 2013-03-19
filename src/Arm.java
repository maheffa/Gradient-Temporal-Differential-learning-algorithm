

import com.cyberbotics.webots.controller.Servo;

enum Height {
    ARM_FRONT_FLOOR, ARM_FRONT_PLATE, ARM_HANOI_PREPARE, ARM_FRONT_CARDBOARD_BOX,
    ARM_RESET, ARM_BACK_PLATE_HIGH, ARM_BACK_PLATE_LOW, ARM_MAX_HEIGHT
}

enum Orientation {
    ARM_BACK_LEFT, ARM_LEFT, ARM_FRONT_LEFT, ARM_FRONT, ARM_FRONT_RIGHT,
    ARM_RIGHT, ARM_BACK_RIGHT, ARM_MAX_SIDE
}

public class Arm {
    private int ARM1 = 0, ARM2 = 1, ARM3 = 2, ARM4 = 3, ARM5 = 4;
    private Servo[] arms;
    
    public Arm(Servo arm1, Servo arm2, Servo arm3, Servo arm4, Servo arm5){
        arms = new Servo[]{arm1, arm2, arm3, arm4, arm5};
    }
    
    public void arm_reset(){
        setPosition(0.0, 1.57, -2.64, 1.78, 0.0);
    }
    
    public void arm_set_height(Height height){
        switch (height) {
            case ARM_BACK_PLATE_LOW:
                setPosition(null, 0.92, 0.42, 1.78, 0.0);
                break;
        }
    }
    
    public void setPosition(Double hArm1, Double hArm2, Double hArm3, Double hArm4, Double hArm5){
        if(hArm1!=null) arms[ARM1].setPosition(hArm1);
        if(hArm2!=null) arms[ARM2].setPosition(hArm2);
        if(hArm3!=null) arms[ARM3].setPosition(hArm3);
        if(hArm4!=null) arms[ARM4].setPosition(hArm4);
        if(hArm5!=null) arms[ARM5].setPosition(hArm5);
    }
    
    public void arm_set_orientation(Orientation orientation){}
    
}
