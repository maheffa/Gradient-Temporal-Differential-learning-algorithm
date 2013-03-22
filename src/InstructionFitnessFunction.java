
import com.cyberbotics.webots.controller.Field;
import com.cyberbotics.webots.controller.Node;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.impl.IntegerGene;

public class InstructionFitnessFunction extends FitnessFunction {
    
    private SuperController supercontroller = null;
    Map<String, List<double[]>> mapResult = null;
    private int counter = 0;
    
    public InstructionFitnessFunction(SuperController supercontroller){
        super();
        this.supercontroller = supercontroller;
    }
    
    @Override
    public double evaluate(IChromosome ic) {
        double result;
        System.out.println("[GA "+counter+" - "+ic.getConfiguration().getGenerationNr()
                +" ] Evaluate chromosome "+Util.InstructionChromosome(fromChromosomeToInstruction(ic)));
        do{
            // To analyze the chromosome, we have to simulate it on the supervisor
            mapResult = supercontroller.simulate(fromChromosomeToInstruction(ic));
            synchronized(supercontroller){
                supercontroller.notify();
            }
             
            //System.out.println("Got result");
            //Util.readMapData(mapResult);
            // Evaluate the result
            System.out.print("[GA "+counter+++" ] chromosome "
                    +Util.InstructionChromosome(fromChromosomeToInstruction(ic))
                    +" fitness = ");
            result = evaluateSuperControllerResult(mapResult);
            System.out.println(result);
            if(result==Double.MIN_NORMAL) System.err.println("[GA] REEVALUATION");
            Util.cleanAllFile();
        } while(result==Double.MIN_NORMAL);
        
        // Return the evaulation result
        return result;
        
    }
    
    private double[] fromChromosomeToInstruction(IChromosome ic){
        double[] instructions = new double[ic.size()];
        int i = 0;
        double[] min = new double[]{-3., -Math.PI, -2., -Math.PI/2., 0.4};
        double[] max = new double[]{3., -Math.PI/6., 2., Math.PI/2., 1.2};
        for(Gene g : ic.getGenes()){
            int high = ((IntegerGene)g).getUpperBounds();
            double coeff = (max[i]-min[i])/((Integer)high).doubleValue();
            instructions[i] = coeff*(((Integer)g.getAllele()).doubleValue())+min[i];
            i++;
        }
        return instructions;
    }
    
    public double evaluateSuperControllerResult(Map<String, List<double[]>> mapResult){
        
        if(mapResult == null) return Double.MIN_NORMAL;
        double[] dOGPS;
        try {
            dOGPS = getDistanceBetweenObjectAndGPS(mapResult);
        } catch(IndexOutOfBoundsException  ex){
            return Double.MIN_NORMAL;
        }
        double d0 = Util.distance(
                mapResult.get("ROBOT").get(0), 
                mapResult.get("OBJECT").get(0));
        double sensor = mapResult.get("SENSORS").get(2)[0] == 1.0
                || mapResult.get("SENSORS").get(2)[1] == 1.0 ? 1.0 : 0.0;
        double[] dobj1 = mapResult.get("OBJECT").get(1);
        double[] dgps1 = mapResult.get("GPS").get(1);
        
        double result = 10/(d0+1);
        for(int i=0; i<dOGPS.length; i++)
            result += Math.pow(1/dOGPS[i], i+1);
        result += Math.pow(10/dOGPS[1], 3 ) * Math.pow(100/(dobj1[2]-dgps1[0]), 4);
        result *= sensor + 1;
        return result/Math.pow(10, 11);
    }
    
    private double[] getDistanceBetweenObjectAndGPS(Map<String, List<double[]>> mapResult)  throws IndexOutOfBoundsException {
        List<double[]> objectPosition = mapResult.get("OBJECT");
        List<double[]> gpsPosition = mapResult.get("GPS");
        /*if(objectPosition.size() <= gpsPosition.size()) {
            return null;
        }*/
        int size = objectPosition.size();
        double[] distance = new double[size];
        for(int i=0; i<size; i++){
            distance[i] = Util.distance(objectPosition.get(i), gpsPosition.get(i));
        }
        return distance;
    }
   
}
