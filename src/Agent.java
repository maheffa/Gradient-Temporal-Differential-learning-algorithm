
import java.util.ArrayList;
import java.util.List;

enum Action {
    Servo0_UP, Servo0_DOWN,
    Servo1_UP, Servo1_DOWN,
    Servo2_UP, Servo2_DOWN,
    Servo3_UP, Servo3_DOWN,
    PICK, NONE
}

class StateFeature {
    
    private List<double[]> gps_coords;
    private boolean touchsensor = false;
    private double[] object;
    private int success = 0;
    
    public StateFeature(List<double[]> gps, double[] obj, boolean sensor, int success){
        gps_coords = gps;
        touchsensor = sensor;
        object = obj;
        this.success = success;
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof StateFeature)) return false;
        StateFeature other = (StateFeature)o;
        for(int i=0; i<gps_coords.size(); i++){
            double[] mygps = gps_coords.get(i);
            double[] othergps = other.getGps(i);
            for(int j=0; j<3; j++){
                if(mygps[j]!=othergps[j]) return false;
            }
        }
        for(int i=0; i<3; i++){
            if(this.object[i]!=other.getObject()[i])
                return false;
        }
        return (this.touchsensor==other.touchsensor)&&(this.success==other.success);
    }
    
    @Override
    public int hashCode(){
        int hash = 1;
        for(int i=0; i<gps_coords.size(); i++){
            double[] _gps = gps_coords.get(i);
            for(int j=0; j<3; j++){
                hash += j*(int)(_gps[j]*1000);
            }
        }
        hash += 17*(touchsensor?5:7);
        hash += 51*success;
        return hash;
    }
    
    public double[] getGps(int index){
        return gps_coords.get(index);
    }
    
    public boolean getSensor(){
        return touchsensor;
    }
    
    public double[] getObject(){
        return object;
    }
    
    public boolean isFinalState(){
        return getSensor();
    }
    
    public boolean isSuccessful(){
        return success>0;
    }
}

public class Agent extends Thread {
    
    private Demon[] predictionDemons;
    private DemonControl controlDemon;
    private double learning_rate = 0.01, discount_factor = 0.1;
    private double lambda = 0.1, action_reward = -0.1, big_reward = 10.0, epsilon = 0.01;
    private double[] eligibility;
    private int t = 0;
    private ControlPanel panel = null;
    private SuperController supercontroller = null;
    private List<List<StateFeature>> episodes = null;
    private StateFeature previousState, currentState;
    public Datas data = null;
    
    public Agent(SuperController supercontroller){
        predictionDemons = new Demon[]{new DemonPredictionProximity(this)};
        controlDemon = new DemonControl(this, predictionDemons);
        panel = new ControlPanel(this);
        panel.setVisible(true);
        this.supercontroller = supercontroller;
        episodes = new ArrayList<List<StateFeature>>();
        data = new Datas();
    }
    
    @Override
    public void run(){
        previousState = initState();
        while(true){
            // Start episode
            resetEligibility();
            currentState = nextState(getActionPolicy(previousState));
            for(Demon d : predictionDemons){
                d.update(previousState, currentState);
            }
            data.setValue(currentState, controlDemon.update(previousState, currentState));
            supercontroller.notify();
        }
    }
    
    private void resetEligibility(){
        controlDemon.resetEligibility();
        for(Demon d : predictionDemons)
            d.resetEligibility();
    }
    
    public Action getActionPolicy(StateFeature state){
        // e-greedy, with epsilon rate exporation
        double coin = Math.random();
        Action[] actions = getLegalActions();
        if(coin<this.getEpsilon()){
            // explore
            int n = actions.length;
            int i = (int)(Math.random()*(double)n);
            return actions[i];
        }
        else{
            // exploit
            Action a = actions[0];
            double max = data.getQValue(state, a);
            for(Action act : actions){
                if(data.getQValue(state, act)>max){
                    max = data.getQValue(state, act);
                    a = act;
                }
            }
            return a;
        }
    }
    
    public Action[] getLegalActions(){
        if(currentState.isFinalState()){
            return new Action[]{Action.PICK};
        }
        else{
            ArrayList<Action> answ = new ArrayList<Action>();
            if(!supercontroller.isMaxServo(0)) answ.add(Action.Servo0_UP);
            if(!supercontroller.isMinServo(0)) answ.add(Action.Servo0_DOWN);
            if(!supercontroller.isMaxServo(1)) answ.add(Action.Servo1_UP);
            if(!supercontroller.isMinServo(1)) answ.add(Action.Servo1_DOWN);
            if(!supercontroller.isMaxServo(2)) answ.add(Action.Servo2_UP);
            if(!supercontroller.isMinServo(2)) answ.add(Action.Servo2_DOWN);
            if(!supercontroller.isMaxServo(3)) answ.add(Action.Servo3_UP);
            if(!supercontroller.isMinServo(3)) answ.add(Action.Servo3_DOWN);
            
            return (Action[])answ.toArray();
        }
    }
    
    public StateFeature initState(){
        return supercontroller.applyAction(Action.NONE);
    }
    
    public StateFeature nextState(Action a){
        return supercontroller.applyAction(a);
    }
    
    public double getAlpha(){
        return learning_rate;
    }
    
    public double getGamma(){
        return discount_factor;
    }
    
    public double getLambda(){
        return lambda;
    }
    
    public double getReward(){
        if(previousState.isFinalState()) 
            return big_reward*(currentState.isSuccessful()?1:-1); 
        else
            return action_reward;
    }
    
    public double getEpsilon(){
        return epsilon;
    }
    
    public synchronized void setEpsilon(double val){
        epsilon = val;
    }
    
    public synchronized void setAlpha(double val){
        learning_rate = val;
    }
    
    public synchronized void setGamma(double val){
        discount_factor = val;
    }
    
    public synchronized void setLambda(double val){
        lambda = val;
    }
    
    public synchronized void setReward(double val){
        action_reward = val;
    }
    
}
