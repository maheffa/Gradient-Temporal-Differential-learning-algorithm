

import com.cyberbotics.webots.controller.Servo;

public class Base {
    private Servo[] wheels;
    private static double SPEED = 4.0, DISTANCE_TOLERANCE = 0.001, ANGLE_TOLERANCE = 0.001;
    
    public Base(Servo wheel1, Servo wheel2, Servo wheel3, Servo wheel4){
        wheels = new Servo[]{wheel1, wheel2, wheel3, wheel4};
    }
    
    public void setWheelVelocity(int index, double velocity){
        Servo wheel = wheels[index];
        if (velocity < 0.0)
            wheel.setPosition(Double.NEGATIVE_INFINITY);
        else
            wheel.setPosition(Double.POSITIVE_INFINITY);
        wheel.setVelocity(Math.abs(velocity));
    }
    
    public void setWheelSpeeds(double[] velocity){
        for(int i=0; i<4; i++)
            setWheelVelocity(i, velocity[i]);
    }
    
    void reset() {
        double[] speeds = new double[]{0.0, 0.0, 0.0, 0.0};
        setWheelSpeeds(speeds);
    }
    
    void forwards() {
        double[] speeds = new double[]{SPEED, SPEED, SPEED, SPEED};
        setWheelSpeeds(speeds);
    }
    
    void backwards() {
        double[] speeds = new double[]{-SPEED, -SPEED, -SPEED, -SPEED};
        setWheelSpeeds(speeds);
    }
    
    void turn_left() {
        double[] speeds = new double[]{-SPEED, SPEED, -SPEED, SPEED};
        setWheelSpeeds(speeds);
    }
    
    void turn_right() {
        double[] speeds = new double[]{SPEED, -SPEED, SPEED, -SPEED};
        setWheelSpeeds(speeds);
    }
    
    void strafe_left() {
        double[] speeds = new double[]{SPEED, -SPEED, -SPEED, SPEED};
        setWheelSpeeds(speeds);
    }
    
    void strafe_right() {
        double[] speeds = new double[]{-SPEED, SPEED, SPEED, -SPEED};
        setWheelSpeeds(speeds);
    }
    
    
}
