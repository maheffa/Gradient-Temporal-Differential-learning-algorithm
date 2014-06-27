

import com.cyberbotics.webots.controller.Servo;

/**
 * A class which encapsulate the work of the robot's base as one object
 */
public class Base {
    private Servo[] wheels;
    private double SPEED = 10.0;
    
    /**
     * A constructor taking the handle to the wheels as parameters
     */
    public Base(Servo wheel1, Servo wheel2, Servo wheel3, Servo wheel4){
        wheels = new Servo[]{wheel1, wheel2, wheel3, wheel4};
    }
    
    /**
     * Set an appropriate velocity to a specified wheel
     * @param index index of the wheel to set the velocity
     * @param velocity velocity to set
     */
    public void setWheelVelocity(int index, double velocity){
        Servo wheel = wheels[index];
        if (velocity < 0.0)
            wheel.setPosition(Double.NEGATIVE_INFINITY);
        else
            wheel.setPosition(Double.POSITIVE_INFINITY);
        wheel.setVelocity(Math.abs(velocity));
    }
    
    /**
     * Set the default velocity absolute value
     */
    public void setWheelSpeeds(double speed){
        this.SPEED = speed;
    }
    
    /**
     * Set the velocity of each wheel to correspond with the parameters
     * @param velocity array of double containing velocity for each wheel
     */
    public void setWheelSpeeds(double[] velocity){
        for(int i=0; i<4; i++)
            setWheelVelocity(i, velocity[i]);
    }
    
    /**
     * Set the velocity of each wheel to zero, resulting its movement break
     */
    public void reset() {
        double[] speeds = new double[]{0.0, 0.0, 0.0, 0.0};
        setWheelSpeeds(speeds);
    }
    
    /**
     * Set automatically the wheels velocity so the robot can go forward
     */
    public void forwards() {
        double[] speeds = new double[]{SPEED, SPEED, SPEED, SPEED};
        setWheelSpeeds(speeds);
    }
    
    /**
     * Set automatically the wheels velocity so the robot can go backward
     */
    public void backwards() {
        double[] speeds = new double[]{-SPEED, -SPEED, -SPEED, -SPEED};
        setWheelSpeeds(speeds);
    }
    
    /**
     * Set automatically the wheels velocity so the robot can turn left
     */
    public void turn_left() {
        double[] speeds = new double[]{-SPEED, SPEED, -SPEED, SPEED};
        setWheelSpeeds(speeds);
    }
    
    /**
     * Set automatically the wheels velocity so the robot can turn right
     */
    public void turn_right() {
        double[] speeds = new double[]{SPEED, -SPEED, SPEED, -SPEED};
        setWheelSpeeds(speeds);
    }
}
