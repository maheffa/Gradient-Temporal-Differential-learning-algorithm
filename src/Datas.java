
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class DataPerState {
    public double value = 0.0;
    public Map<Action, Double> qvalue = null;
}

public class Datas{
    private Map<StateFeature, DataPerState> tabular = null;
    
    public Datas(){
        tabular = new HashMap<StateFeature, DataPerState>();
    }
    
    public double getValue(StateFeature s){
        if(tabular.containsKey(s)) return tabular.get(s).value;
        else return 0.0;
    }
    
    public void setValue(StateFeature s, double v){
        if(tabular.containsKey(s)) tabular.get(s).value = v;
        else {
            DataPerState dps = new DataPerState();
            dps.value = v;
            tabular.put(s, dps);
        }
    }
    
    public double getQValue(StateFeature s, Action a){
        if(tabular.containsKey(s)) {
            Map<Action, Double> p = tabular.get(s).qvalue;
            if(p == null){
                return 0.0;
            }
            else{
                if(p.containsKey(a)) return p.get(a);
                else return 0.0;
            }
        }
        else return 0.0;
    }
    
    public void setQValue(StateFeature s, Action a, double v){
        if(tabular.containsKey(s)){
            Map<Action, Double> p = tabular.get(s).qvalue;
            if(p == null){
                p = new HashMap<Action, Double>();
                p.put(a, v);
                tabular.get(s).qvalue = p;
            }
            else{
                p.put(a, v);
            }
        }
        else{
            DataPerState dps = new DataPerState();
            Map<Action, Double> p = new HashMap<Action, Double>();
            p.put(a, v);
            dps.qvalue = p;
            tabular.put(s, dps);
        }
    }
    
    public Set<StateFeature> getKeys(){
        return tabular.keySet();
    }
    
}