
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class Demon extends Thread{
    
    protected double[] weight, eligibility, w;
    protected int t = 0, functionNumber, N;
    private Agent agent = null;
    private StateFeature lastState;
    private double beta = 0.01;
    
    public Demon(Agent agent, int functionNumber){
        w = new double[functionNumber];
        weight = new double[functionNumber];
        eligibility = new double[functionNumber];
        this.functionNumber = functionNumber;
        this.agent = agent;
    }
    
    public void resetEligibility(){
        Arrays.fill(eligibility, 0.);
    }
    
    public double update(StateFeature previous, StateFeature current){
        // TD algorithm update
        double v0 = value(previous, w);
        double v1 = value(current, w);
        lastState = current;
        double TDError = agent.getReward() + agent.getGamma()*v1 - v0;
        eligibility = TinyMath.addVector(
                weight,
                TinyMath.multiplyVectorCoeff(agent.getGamma(), eligibility));
        weight = TinyMath.addVector(
                    weight,
                    TinyMath.multiplyVectorCoeff(
                        agent.getAlpha(),
                        TinyMath.addVector(
                            TinyMath.multiplyVectorCoeff(TDError, eligibility),
                            TinyMath.multiplyVectorCoeff(
                                agent.getGamma()*(1-agent.getLambda())
                                *TinyMath.multiplyVectorTransp(eligibility, weight),
                                this.getFeature(current)
                            )
                        )
                    )
                );
        w = TinyMath.addVector(
                    w,
                    TinyMath.multiplyVectorCoeff(
                        beta,
                        TinyMath.addVector(
                            TinyMath.multiplyVectorCoeff(TDError, eligibility),
                            TinyMath.multiplyVectorCoeff(
                                TinyMath.multiplyVectorTransp(getFeature(previous), weight),
                                this.getFeature(previous)
                            )
                        )
                    )
                );
        return getValue();
    }
    
    public double getValue(){
        return value(lastState, weight);
    }
    
    public double value(StateFeature S , double[] w){
        double v = 0.;
        for(int i=0; i<functionNumber; i++){
            v += w[i]*function(S, i);
        }
        return v;
    }
    
    public double[] getFeature(StateFeature s){
        double[] v = new double[functionNumber];
        for(int i=0; i<functionNumber; i++) v[i] = function(s, i);
        return v;
    }
    
    public abstract double function(StateFeature S, int index);
}



class DemonPredictionProximity extends Demon{

    
    public DemonPredictionProximity(Agent agent){
        super(agent, 1);
    }
            
    @Override
    public double function(StateFeature S, int index) {
        switch(index){
            case 0 : return f0(S);
            default: return 0;
        }
    }
    
    private double f0(StateFeature S){
        return TinyMath.distance(S.getGps(3), S.getObject());
    }
}

class DemonControl extends Demon{

    private Demon[] predictionDemons;
    
    public DemonControl(Agent agent, Demon[] predictionDemons){
        super(agent, predictionDemons.length);
        this.predictionDemons = predictionDemons;
    }
    
    @Override
    public double function(StateFeature S, int index) {
        return predictionDemons[index].getValue();
    }
}