
import com.cyberbotics.webots.controller.Servo;

public class Arm {
    public final static int ARM_BACK_PLATE_HIGH = 0, ARM_BACK_PLATE_LOW = 1;
    private int ARM1 = 0, ARM2 = 1, ARM3 = 2, ARM4 = 3, ARM5 = 4;
    private Servo[] arms;
    private double[] min = new double[]{-3., 0, -Math.PI/2., -Math.PI/2., 0.5};
    private double[] max = new double[]{3., Math.PI/2., Math.PI/2., Math.PI/2., 0.9};
        
    public Arm(Servo arm1, Servo arm2, Servo arm3, Servo arm4, Servo arm5){
        arms = new Servo[]{arm1, arm2, arm3, arm4, arm5};
    }
    
    public void arm_reset(){
        setPosition(0.0, 0.0, 0.0, 0.0, 0.7);
    }
    
    public Double[] getSettings(){
        // min \n max \n min \n max ...
        Double[] minmax = new Double[10];
        int i = 0, j = 0;
        for(Servo arm : arms){
            minmax[i++] = min[j];
            minmax[i++] = max[j++];
        }
        return minmax;
    }
    
    public Double[] getPositions(){
        Double[] positions = new Double[5];
        for(int i=0; i<5; i++){
            positions[i] = arms[i].getPosition();
        }
        return positions;
    }
    
    public void move(int armIndex, double dMovement){
        Servo arm = arms[armIndex];
        double pos = arm.getPosition();
        if(pos+dMovement>max[armIndex]) arm.setPosition(max[armIndex]);
        else if(pos+dMovement<min[armIndex]) arm.setPosition(min[armIndex]);
        else arm.setPosition(pos+dMovement);
    }
    
    public void setPosition(Double hArm1, Double hArm2, Double hArm3, Double hArm4, Double hArm5){
        if(hArm1!=null) arms[ARM1].setPosition(hArm1);
        if(hArm2!=null) arms[ARM2].setPosition(hArm2);
        if(hArm3!=null) arms[ARM3].setPosition(hArm3);
        if(hArm4!=null) arms[ARM4].setPosition(hArm4);
        if(hArm5!=null) arms[ARM5].setPosition(hArm5);
    }
    
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
}
