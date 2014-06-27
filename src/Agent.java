
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The action class, containing an enumeration of all possible action
 */
enum Action {
    Servo0_UP, Servo0_DOWN,
    Servo1_UP, Servo1_DOWN,
    Servo2_UP, Servo2_DOWN,
    Servo3_UP, Servo3_DOWN,
    Servo4_UP, Servo4_DOWN,
    PICK, NONE;
    
    /**
     * @return the index of the object that the action refers to.
     * return MAX_VALUE if any pickup is need, -1 if nothing to do, and MIN_VALUE if something wrong happened
     */
    public int getIndex(){
        switch(this){
            case Servo0_UP: return 0;
            case Servo0_DOWN: return 0;
            case Servo1_UP: return 1;
            case Servo1_DOWN: return 1;
            case Servo2_UP: return 2;
            case Servo2_DOWN: return 2;
            case Servo3_UP: return 3;
            case Servo3_DOWN: return 3;
            case Servo4_UP: return 4;
            case Servo4_DOWN: return 4;
            case PICK: return Integer.MAX_VALUE;
            case NONE: return -1;
            default: return Integer.MIN_VALUE;
        }
    }    
    
    /**
     * @return the direction UP or DOWN of the action
     */
    public int getDirection(){
        if(this==PICK || this==NONE) {
            return 0;
        }
        else if(this==Servo0_UP || this==Servo1_UP || this==Servo2_UP || this==Servo3_UP || this==Servo4_UP) {
            return 1;
        }
        else {
            return -1;
        }
    }
    
    /**
     * @return true if the action refers to any Servo
     */
    public boolean isAction(){
        if(this==PICK||this==NONE) {
            return false;
        }
        else {
            return true;
        }
    }
    
    /**
     * @return array of all possible actions
     */
    public static Action[] toArrays(){
        return new Action[]{
            Servo0_UP, Servo0_DOWN,
            Servo1_UP, Servo1_DOWN,
            Servo2_UP, Servo2_DOWN,
            Servo3_UP, Servo3_DOWN,
            Servo4_UP, Servo4_DOWN,
            PICK, NONE};
    }
    
    @Override
    public String toString(){
        String res = "{";
        if(isAction()) {
            res += "SERVO "+getIndex() +" "+(getDirection()>0?"UP":"DOWN");
        }
        return res+"}";
    }
}


/**
 * A feature class containing data from simulation and is specific to some state
 * These data will be later used if any real evaluation of the state is needed
 */
class StateFeature {

    private List<double[]> gps_coords;
    private double[] object;
    private int success = 0;
    private double pickDistance = 0.1;
    private DecimalFormat df = new DecimalFormat("0.#####");
    
    /**
     * Create a feature class
     * @param gps a list of the all GPS position
     * @param obj the objects position
     * @param success the success value. 0 if nothing happened, -1 if failure and 1 if success. By success, we mean, the object was taken and placed in the right position
     */
    public StateFeature(List<double[]> gps, double[] obj, int success){
        gps_coords = gps;
        object = obj;
        this.success = success;
    }
    
    public int getGpsNumber(){
        return gps_coords.size();
    }
    
    public double[] getGps(int index){
        return gps_coords.get(index);
    }
    
    public double[] getObject(){
        return object;
    }
    
    public boolean isFinalState(){
        return TinyMath.distance(getGps(3), object) < pickDistance;
    }
    
    public int getSuccess(){
        return success;
    }
    
    @Override
    public boolean equals(Object o){
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof StateFeature)) {
            return false;
        }
        StateFeature other = (StateFeature)o;
        for(int i=0; i<gps_coords.size(); i++){
            double[] mygps = this.getGps(i);
            double[] othergps = other.getGps(i);
            for(int j=0; j<3; j++){
                if(Math.abs(mygps[j]-othergps[j])>Math.pow(10., -6)){
                    return false;
                }
            }
        }
        for(int i=0; i<3; i++){
            if(Math.abs(object[i]-other.object[i])>Math.pow(10., -6)) {
                return false;
            }
        }
        return this.success==other.success;
    }
    
    @Override
    public int hashCode(){
        int v = 0;
        for(double[] vv : gps_coords){
            for(double vvv : vv) {
                v = v*10 + (int)(100*vvv);
            }
        }
        return v;
    }
}

/**
 * A state of the robot
 */
class StateServo{
    /** The pseudo-state of the robot (see Report for more explanation)*/
    private Integer[] servoStatus = null;
    /** The robot feature, i.e. the simulation result of the current state*/
    private StateFeature feature = null;
    /** Handle to the supervisor*/
    private SuperController supercontroller = null;
    
    /** Minimum and maximum value each Servo could take*/
    private static Double[] minServo = new Double[]{0., -2*Math.PI/3,-Math.PI/2, -2*Math.PI/3., -2*Math.PI/3., 0.};
    private static Double[] maxServo = new Double[]{0., 0., Math.PI/2, 2*Math.PI/3, 2*Math.PI/3., 0.};
    /** The number of division between the minimum and maximum value a servo could take*/
    private static Integer[] division = new Integer[]{0, 10, 10, 10, 5, 0};
    
    
    /**
     * Create a new State, i.e. an fresh new initialized state
     * @param sup handle to the supervisor
     */
    public StateServo(SuperController sup){
        // initialPosition
        servoStatus = new Integer[6];//*** 6th gripper
        for(int i=0; i<6; i++) {
            servoStatus[i] = division[i]/2;
        }
        supercontroller = sup;
        feature = setPosition(servoStatus);
    }
    
    /**
     * Create a state with a defined position, and simulate it
     * @param sup handle to the supervisor
     * @param servoPositions pseudo-state to be simulated, which will create a full state
     */
    public StateServo(SuperController sup, Integer[] servoPositions){
        servoStatus = servoPositions;
        supercontroller = sup;
        feature = setPosition(servoStatus);
    }
    
    /**
     * Create a virtual state, i.e. with no future and no simulation data.
     * State created with this is used just for prediction and analysis
     * @param servoPositions the pseudo-state of the new state.
     */
    private StateServo(Integer[] servoPositions){
        servoStatus = servoPositions;
    }
    
    /**
     * Simulate a pseudo-state.
     * @param servoStatus the pseudo-state to be simulated
     * @return the simulation result containing GPS and Object's positions
     */
    public StateFeature setPosition(Integer[] servoStatus){
        Double[] realPosition = new Double[servoStatus.length-1];
        for(int i=0; i<servoStatus.length-1; i++){
            Double k = servoStatus[i].doubleValue()/(division[i].doubleValue()+1.);
            realPosition[i] = minServo[i] + k*(maxServo[i]-minServo[i]);
        }
        System.out.println("||| setting => "+Arrays.toString(realPosition));
        StateFeature result = supercontroller.simulate(realPosition, servoStatus[5]==1);
        synchronized(supercontroller){
            supercontroller.notify();
        }
        return result;
    }
    
    /**
     * @return an array of allowed action from the current state
     */
    public Action[] getPossibleAction(){
        if(feature.isFinalState()){
            /**
             * If the state is a final state, the only allowed action
             * is to Pick up the object
             */
            return new Action[]{Action.PICK};
        }
        else{
            ArrayList<Action> answ = new ArrayList<Action>();
            /**
             * Get all possible action
             */
            Action[] initial = Action.toArrays();
            for(int i=0; i<initial.length-2; i++){
                /**
                 * Then, for each state, verify if any maximum and minimum where reached
                 * If any, then do not allow some action which will lead to
                 * a position over those limits
                 */
                int j = i/2;
                if(servoStatus[j]<division[j]) {
                    answ.add(initial[i]);
                }
                i++;
                if(servoStatus[j]>0) {
                    answ.add(initial[i]);
                }
            }
            /**
             * Then return the allowed list of action
             */
            Action[] res = new Action[answ.size()];
            for(int i=0; i<res.length; i++) {
                res[i] = answ.get(i);
            }
            return res;
        }
    }
    
    /**
     * Apply an action to a state, resulting a creation of a new stae
     * @param a Action to take
     * @return State result from taking the action a
     */
    public StateServo applyAction(Action a){
        /**
         * Get the servo with which a refers
         * and the direction it takes
         */
        int index = a.getIndex();
        int dir = a.getDirection();
        /** clone the current pseudo-state*/
        Integer[] newServoStatus = servoStatus.clone();
        if(index == Integer.MAX_VALUE) {
            /**
             * If the index is MAX_VALUE, it means that the action is to pick the object
             * Set then the pseudo-state 6th value to 1
             */
            newServoStatus[5] = 1;
        }
        else {
            /**
             * Otherwise, increment or decrement the appropriate element of the pseudo state
             */
            newServoStatus[index] += dir;
        }
        /** and create a new state from the pseudo-state*/
        return new StateServo(supercontroller, newServoStatus);
    }
    
    /**
     * All steps are the same as applyAction, with the difference that it use
     * the private creator to create the next state, and, therefore, do not perform any simulation 
     * @param a
     * @return 
     */
    public StateServo applyActionVirtually(Action a){
        int index = a.getIndex();
        int dir = a.getDirection();
        Integer[] newServoStatus = servoStatus.clone();
        if(index == Integer.MAX_VALUE) {
            newServoStatus[5] = 1;
        }
        else {
            newServoStatus[index] += dir;
        }
        StateServo res = new StateServo(newServoStatus);
        res.supercontroller = this.supercontroller;
        return res;
    }
    
    public Integer getServo(int index){
        return servoStatus[index];
    }
    
    public StateFeature getFeature(){
        return feature;
    }
    
    @Override
    public boolean equals(Object o){
        if(o == null || !(o instanceof StateServo)) {
            return false;
        }
        if(o == this) {
            return true;
        }
        StateServo other = (StateServo)o;
        for(int i=0; i<servoStatus.length; i++) {
            if(servoStatus[i]!=other.servoStatus[i]) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode(){
        int hash = 0;
        for(int i=0; i<servoStatus.length; i++){
            hash = hash*10 + servoStatus[i];
        }
        return hash;
    }
 
    @Override
    public String toString(){
        return Arrays.toString(servoStatus);
    }
}

public class Agent extends Thread {
    
    /** Array to hold the prediction demons ' handle*/
    private DemonPrediction[] predictionDemons;
    /** Handle to the control demon*/
    private DemonControl controlDemon;
    /** parameters needed for the GTD algorithm*/
    private double learning_rate, discount_factor, learning_rate_second_weight,
            lambda, epsilon;
    /** Control panel ' s handle*/
    private ControlPanel panel = null;
    /** Handle to the supervisor*/
    private SuperController supercontroller = null;
    
    /**
     * Create a GTD(lambda) agent
     * @param supercontroller handle to the supervisor
     */
    public Agent(SuperController supercontroller){
        // create a list of all used demon
        predictionDemons = new DemonPrediction[]{
            new DemonPredictGripperProximity(this, 5),
            new DemonPredictArmProximity(this, 5),
            new DemonPredictStretch(this, 5)
        };
        // create the control demon
        controlDemon = new DemonControl(this, (DemonPrediction[])predictionDemons);
        // setting it to verbose, means that its evolution will be outputed in standart output
        controlDemon.verbose = true;
        // create a pannel
        panel = new ControlPanel(this);
        // and make it visible
        panel.setVisible(true);
        this.supercontroller = supercontroller;
    }
    
    /**
     * Run the agent.
     * Its main task is to execute the e-greedy algorithm, where the policy
     * is taken from the control demon's value of a state
     */
    @Override
    public void run(){
        // maximum allowed number of action in one episode
        int maxActionPerEpisode = 30;
        double marge_error = 0.001;
        while(true){
            // Start episode
            // List of Q-state-action value from each demon
            // It will be needed to compute the p rate of the eligibility vector
            Map<Demon, Map<Action, Double>> actionValues;
            // Create a new state
            StateServo preState = new StateServo(supercontroller);
            StateServo curState;
            // Reset the simulation world
            supercontroller.reset();
            // Reset elegibility vectors
            resetEligibility();
            Action action;
            int episodeAction = 0;
            do{
                // take the Q-state-action values
                // it will automatically contain all possible action
                actionValues = getPolicies(preState);
                // choose an action according from one demon's Q-State-Value, here the controlDemon
                action = getAction(actionValues.get(controlDemon));
                System.out.println("Taking action "+action);
                
                // apply the chosen action
                curState = preState.applyAction(action);
                
                System.out.println("Current state: "+curState+"; value = "+controlDemon.getValue(curState));
                controlDemon.showValue(curState);
                controlDemon.showError(curState, preState);
                
                // compute p
                double piV = 1./actionValues.get(controlDemon).size();
                for(Demon d : predictionDemons){
                    double BV = getDistribution(actionValues.get(d), action);
                    // update demons with p = piV/BV eligibility trace rate
                    d.update(preState, curState, piV/BV);
                }
                // update the control demon with p = 1 since its behavior policy is the same as its target policy
                controlDemon.update(preState, curState, 1);

                Util.writeGTDResultString("\n");
                
                synchronized(supercontroller){
                    // notify the supervisor that a simulation was done (while creating a new state)
                    supercontroller.notify();
                }
                
                if(preState.getFeature().isFinalState() 
                        || curState.getFeature().getSuccess()!=0){
                    /**
                     * If the previous state was a final state, or the current state is not a normal action
                     * break the episode and take a new one
                     */
                    break;
                }
                preState = curState;
                episodeAction++;
                if(episodeAction >= maxActionPerEpisode) break;
            } while(true);
        }
    }
    
    /**
     * Reset eligibilities
     */
    public void resetEligibility(){
        controlDemon.resetEligibility();
        for(Demon d : predictionDemons) {
            d.resetEligibility();
        }
    }
    
    /**
     * @param state the state to be analyzed
     * @return The Q-Action-Value related to the current state using current value function
     */
    public Map<Demon, Map<Action, Double>> getPolicies(StateServo state){
        // get all possible action
        Action[] actions = state.getPossibleAction();
        Map<Demon, Map<Action, Double>> actionValue = new HashMap<Demon, Map<Action, Double>>();
        // create a list of all demon
        List<Demon> dems = new ArrayList<Demon>(Arrays.asList(predictionDemons));
        dems.add(controlDemon);
        for (Iterator<Demon> it = dems.iterator(); it.hasNext();) {
            Demon d = it.next();
            actionValue.put(d, new EnumMap<Action, Double>(Action.class));
        }
        for(Action a : actions){
            // predict the next situation's state value using prediction demons
            StateServo s1 = state.applyActionVirtually(a);
            for(Demon d : dems) {
                actionValue.get(d).put(a, d.getValue(s1));
            }
        }
        return actionValue;
    }
    
    /**
     * Chose an action using e-greedy algorithm
     * @param actionValues The Q-Action-Value result from the analyze of a state 
     * @return An action according to e-greedy algorithm
     */
    public Action getAction(Map<Action, Double> actionValues){
        // Take a random value
        Random r = new Random();
        if(r.nextDouble() < this.getEpsilon()){
            /**
             * if it's less than epsilon, then take an action randomly
             */
            int index = r.nextInt(actionValues.size()), i = 0;
            for(Action a : actionValues.keySet()){
                if(i++ == index) {
                    return a;
                }
            }
            return null;
        }
        else{
            /**
             * Otherwise, take the one with the maximum predicted value
             */
            double max = -Double.MAX_VALUE;
            Action best = null;
            for(Action a : actionValues.keySet()){
                if(actionValues.get(a) >= max){
                    best = a;
                    max = actionValues.get(a);
                }
            }
            return best;
        }
    }
    
    /**
     * @param ad value of the next state associated with an action a
     * @param a an action a to be evaluated
     * @return the p/sum(p) value, i.e. the statistical probability of this action to be taken.
     */
    public double getDistribution(Map<Action, Double> ad, Action a){
        /**
         * The result of this function is mostly used for computing p
         * as the GTD algorithm proposed
         */
        double sum = 0.;
        for(Double d : ad.values()) {
            sum += d;
        }
        return sum ==0. ? 0 : ad.get(a)/sum;
    }
    
    public double getAlpha(){
        return learning_rate;
    }
    
    public double getLambda(){
        return lambda;
    }
    
    public double getEpsilon(){
        return epsilon;
    }
    
    public double getBeta(){
        return learning_rate_second_weight;
    }
    
    public synchronized void setEpsilon(double val){
        epsilon = val;
    }
    
    public synchronized void setAlpha(double val){
        learning_rate = val;
    }
    
    public synchronized void setLambda(double val){
        lambda = val;
    }
    
    public synchronized void setBeta(double val){
        learning_rate_second_weight = val;
    }
}

/**
 * A prediction demon class
 */
abstract class DemonPrediction extends Demon{
    
    /**
     * the desired prediction precision, the bigger this value is,
     * the more precise will be the attempt to predict
     * but there is no warranty of such precision success
     */
    double K = 100;
    
    public DemonPrediction(Agent agent, int servoNumber){
        super(agent, servoNumber);
        for(int i=0; i<weight.length; i++){
            // initialize the function-value parameter to 1
            // it will then adjust itself over episodes
            weight[i] = 1;
        }
    }
    
    /**
     * @return the precision error, i.e. the difference between the prediction and the estimation
     */
    @Override
    public double getError(StateServo cur, StateServo pre){
        double dx = getValue(cur) - K*expectedValue(cur);
        return -0.5*dx;
        
    }
    
    /**
     * Since the value a prediction demon try to approximate is K*time bigger than
     * the actual value (for the sake of precision), so when the actual value is needed
     * then the prediction is divided by K. Thereby, the error will be actually K time
     * smaller than the prediction's error.
     * @param s the state to be predicted
     * @return the prediction on state s
     */
    public double getValueByK(StateServo s){
        return getValue(s)/K;
    }
    
    /**
     * @return the i-th element of the pseudo-state array
     */
    @Override
    public double function(StateServo S, int index) {
        return S.getServo(index).doubleValue()+1;
    }
       
    /**
     * No reward is needed since the error is evaluated directly
     */
    @Override
    public double getReward(StateServo s) {
        throw new UnsupportedOperationException("Do not need any reward");
    }
    
    /**
     * @return the expected real value to be approximated on state s
     */
    public abstract double expectedValue(StateServo s);
}

/**
 * The class for prediction distance between the gripper and the object
 */
class DemonPredictGripperProximity extends DemonPrediction{
    public DemonPredictGripperProximity(Agent agent, int servoNumber){
        super(agent, servoNumber);
    }

    /**
     * @return the expected distance between object and gripper
     */
    @Override
    public double expectedValue(StateServo s) {
        // the gripper's position is given by the 4th GPS
        double[] gpsPosition = s.getFeature().getGps(3);
        double[] object = s.getFeature().getObject();
        // return the distance between the object and the 4th GPS coordinates
        return TinyMath.distance(gpsPosition, object);
    }    
    
    @Override
    public double getGamma(){
        return 0.5;
    }
}

/**
 * The class for prediction distance between the arm and the object
 * in other word, the sum of distance between arm and object
 * It might be useful in order to find out whether the robot, or the whole arm
 * itself is near the object. 
 */
class DemonPredictArmProximity extends DemonPrediction{
    
    public DemonPredictArmProximity(Agent agent, int servoNumber){
        super(agent, servoNumber);
    }

    @Override
    public double expectedValue(StateServo s) {
        double[] object = s.getFeature().getObject();
        double v = 0.;
        for(int i=0; i<s.getFeature().getGpsNumber(); i++) {
            /**
             * Sum up all the distance between GPS and object
             */
            v += TinyMath.distance(object, s.getFeature().getGps(i));
        }
        return v;
    }
    
    @Override
    public double getGamma(){
        return 0.9;
    }
}

/**
 * The class for prediction of the stretchiness of the arm.
 * It is useful depending on the situation. When object is far, a less stretched
 * arm is important, so the RL algorithm should make the parameter to this function positive
 * otherwise, if the object is near, a less stretched arm is needed, and the parameter
 * related to the value of this function should be then negative
 */
class DemonPredictStretch extends DemonPrediction{
    public DemonPredictStretch(Agent agent, int servoNumber){
        super(agent, servoNumber);
    }

    @Override
    public double expectedValue(StateServo s) {
        double v = 0;
        StateFeature feat = s.getFeature();
        for(int i=0; i+2<feat.getGpsNumber(); i++){
            /**
             * Sum up the distance between 2 interlaced GPS.
             * When to interlaced GPS is close, then the arm is surely more stretched
             */
            v += TinyMath.distance(feat.getGps(i), feat.getGps(i+2));
        }
        return v;
    }   
    
    @Override
    public double getGamma(){
        return 0.9;
    }
}

/**
 * Control demon, with same code as all demon, except that its value function
 * is the value predicted by prediction demons.
 */
class DemonControl extends Demon{
    private DemonPrediction[] predictionDemons;
    
    /**
     * Create a control demon
     * @param agent handle to the GTD agent
     * @param predictionDemons array of prediction demons used by this control demon
     */
    public DemonControl(Agent agent, DemonPrediction[] predictionDemons){
        super(agent, predictionDemons.length);
        this.predictionDemons = predictionDemons;
    }
    
    @Override
    public double function(StateServo S, int index) {
        return predictionDemons[index].getValueByK(S);
    }

    @Override
    public double getReward(StateServo s) {
        if(s.getFeature().getSuccess()!=0) {
            /**
             * If something happen, then return -100 or 100
             * if the thing that happened was bad or good
             */
            return 100*s.getFeature().getSuccess();
        }
        else if(s.getFeature().isFinalState()) {
            /**
             * Otherwise, if it's a final state, then reward 50 points
             */
            return 50;
        }
        // or get nothing
        return 0;
    }    
    
    @Override
    public double getGamma(){
        return 0.9;
    }
    
    public void showValue(StateServo s){
        StringBuilder str = new StringBuilder();
        str.append(df.format(getValue(s))).append(" = ");
        for(int i=0; i<functionNumber; i++){
            str.append(df.format(weight[i]))
                    .append("*")
                    .append(df.format(function(s, i)))
                    .append(i==functionNumber-1?"":" + ");
        }
        System.out.println(str);
    }
    
    public void showError(StateServo cur, StateServo pre){
        double v0 = getValueFunction(pre, weight);
        double v1 = getValueFunction(cur, weight);
        double r = getReward(cur);
        double error = r + getGamma()*v1-v0;
        System.out.println("Error = "+error+" = "+r+"(reward) + "+getGamma()+"(gamma)*"+v1+"(v1) - "+v0+"(v0)");
    }
}