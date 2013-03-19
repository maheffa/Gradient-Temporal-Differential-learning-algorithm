

import com.cyberbotics.webots.controller.Servo;

public class Gripper {
    
    private Servo finger1, finger2;
    private static double MIN_POS = 0.0, MAX_POS = 0.5, OFFSET_WHEN_LOCKED = 0.021;
    
    Gripper(Servo finger1, Servo finger2){
        this.finger1 = finger1;
        this.finger2 = finger2;
        finger1.setVelocity(0.1);
        finger2.setVelocity(0.1);
    }
    
    public void grip(){
        setPosition(MIN_POS);
    }
    
    public void release(){
        setPosition(MAX_POS);
    }
    
    public void setPosition(double pos){
        finger1.setPosition(pos);
        finger2.setPosition(pos);
    }
    
    public void set_gap(double gap){
        double v = Util.bound(0.5*(gap - OFFSET_WHEN_LOCKED), MIN_POS, MAX_POS);
        setPosition(v);
    }
}
