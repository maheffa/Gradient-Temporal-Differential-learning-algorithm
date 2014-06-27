
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public abstract class Demon extends Thread{
    
    private static int demIncrementation = 0;
    protected double[] weight, eligibility, w;
    protected int t = 0, functionNumber, N;
    private int demonNumber;
    private Agent agent = null;
    public DecimalFormat df = new DecimalFormat("0.000000000");
    public boolean verbose = false;
        
    public Demon(Agent agent, int functionNumber){
        w = new double[functionNumber];
        weight = new double[functionNumber];
        eligibility = new double[functionNumber];
        Arrays.fill(eligibility, 0);
        Arrays.fill(w, 0);
        for(int i=0; i<weight.length; i++){
            weight[i] = Math.random()*20 - 10;
        }
        this.functionNumber = functionNumber;
        this.agent = agent;
        demonNumber = demIncrementation++;
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.UK);
        otherSymbols.setDecimalSeparator(',');
        df.setDecimalFormatSymbols(otherSymbols);
    }
    
    public void resetEligibility(){
        Arrays.fill(eligibility, 0.);
    }
    
    @Override
    public boolean equals(Object o){
        if(o == this) return true;
        if(!(o instanceof Demon) || o==null) return false;
        Demon other = (Demon)o;
        return this.demonNumber == other.demonNumber;
    }
    
    @Override
    public int hashCode(){
        return demonNumber;
    }
    
    public void update(StateServo previous, StateServo current, double coeff){
        // TD algorithm update
        double TDError = getError(current, previous);
        if(verbose)System.out.println("before e "+Arrays.toString(eligibility)+" ==> "+coeff);
        eligibility = TinyMath.multiplyVectorCoeff(
                        coeff, 
                        TinyMath.addVector(
                            this.getFeatureVector(previous),
                            TinyMath.multiplyVectorCoeff(getGamma()*agent.getLambda(), eligibility)));
        if(verbose){
            System.out.println("after e "+Arrays.toString(eligibility));
            System.out.println("before we"+Arrays.toString(weight));
        }
        weight = TinyMath.addVector(
                    weight,
                    TinyMath.multiplyVectorCoeff(
                        agent.getAlpha(),
                        TinyMath.subVector(
                            TinyMath.multiplyVectorCoeff(TDError, eligibility),
                            TinyMath.multiplyVectorCoeff(
                                getGamma()*(1-agent.getLambda())*TinyMath.multiplyVectorTransp(eligibility, w),
                                this.getFeatureVector(current)
                            )
                        )
                    )
                );
        if(verbose){
            System.out.println("after we "+Arrays.toString(weight));
            System.out.println("before w"+Arrays.toString(w));
        }
        w = TinyMath.addVector(
                    w,
                    TinyMath.multiplyVectorCoeff(
                        agent.getBeta(),
                        TinyMath.subVector(
                            TinyMath.multiplyVectorCoeff(TDError, eligibility),
                            TinyMath.multiplyVectorCoeff(
                                TinyMath.multiplyVectorTransp(this.getFeatureVector(previous), w),
                                this.getFeatureVector(current)
                            )
                        )
                    )
                );
        if(verbose){
            System.out.println("after w"+Arrays.toString(w));
            System.out.println("before we"+Arrays.toString(weight));
        }
        /*weight = TinyMath.addVector(weight, 
                                    TinyMath.multiplyVectorCoeff(
                                        agent.getAlpha()*TDError, 
                                        this.getFeatureVector(current)));
        if(verbose) System.out.println("before we"+Arrays.toString(weight));*/
        StringBuilder str = new StringBuilder();
        str.append(df.format(TDError)+"; ");
        for(double v : weight) str.append(df.format(v)+"; ");
        if(this instanceof DemonPrediction){
            DemonPrediction this0 = (DemonPrediction)this;
            str.append(df.format((this0.getValueByK(current)))+"; ");
            str.append(df.format((this0.expectedValue(current)))+"; ");
        }
        str.append("-1; ");
        Util.writeGTDResultString(str.toString());
        /*for(double v : eligibility) str.append(df.format(v)+"; ");
        for(double v : w) str.append(df.format(v)+"; ");
        str.append("\n");*/
        
    }
    
    public double getValueFunction(StateServo S , double[] weight){
        double v = 0.;
        for(int i=0; i<functionNumber; i++){
            v += weight[i]*function(S, i);
        }
        return v;
    }
    
    public double getValue(StateServo s){
        return getValueFunction(s, weight);
    }
    
    private double[] getFeatureVector(StateServo s){
        double[] v = new double[functionNumber];
        for(int i=0; i<functionNumber; i++) v[i] = function(s, i);
        return v;
    }
    
    public double getError(StateServo current, StateServo previous){
        double v0 = getValueFunction(previous, weight);
        double v1 = getValueFunction(current, weight);
        if(verbose)System.out.println("TDError = "+getReward(current)+"(R) + "+getGamma()+"(gamma) * "+v1+"(v1) - "+v0+"(v0)");
        return getReward(current)+getGamma()*v1-v0;
    }
    
    public double[] getWeight(){
        return weight;
    }
    
    public abstract double function(StateServo S, int index);
    public abstract double getReward(StateServo s);
    public abstract double getGamma();
    
    public String toString(){
        return "[DEMON "+demonNumber+"] [weight] "+Arrays.toString(weight)+" [eligibility] "+Arrays.toString(eligibility)
                +" [w] "+Arrays.toString(w);
    }
}