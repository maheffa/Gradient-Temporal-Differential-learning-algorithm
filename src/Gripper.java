import com.cyberbotics.webots.controller.Servo;

/**
 * A class warehouse for the gripper. 
 */
public class Gripper {
    
    private Servo finger1, finger2;
    // Servo Minimum and Maximum position
    private static double MIN_POS = 0.0, MAX_POS = 0.5;
    
    /**
     * Create a Gripper
     * @param finger1 Servo of the left picker
     * @param finger2 Servo of the right picker
     */
    Gripper(Servo finger1, Servo finger2){
        this.finger1 = finger1;
        this.finger2 = finger2;
        finger1.setVelocity(0.1);
        finger2.setVelocity(0.1);
    }
    
    /**
     * make the gripper to grip. It will set each gripper's position to the minimum allowed
     */
    public void grip(){
        setPosition(MIN_POS);
    }
    
    /**
     * make the gripper to release. It will set each gripper's position to the maximum allowed
     */
    public void release(){
        setPosition(MAX_POS);
    }
    
    /**
     * Set gripper's position to some position
     * @param pos the position of the gripper which is desired
     */
    public void setPosition(double pos){
        finger1.setPosition(pos);
        finger2.setPosition(pos);
    }
 }
