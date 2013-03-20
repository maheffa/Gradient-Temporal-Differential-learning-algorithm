
import com.cyberbotics.webots.controller.Field;
import com.cyberbotics.webots.controller.Node;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.IChromosome;

class ExperimentData{
    
    private Node grabber, grabme, plate;
    private SuperController superController = null;
    
    public ExperimentData(SuperController superController){
        this.superController = superController;
        grabber = superController.getFromDef("Grabber");
        grabme = superController.getFromDef("GrabMe");
        plate = superController.getFromDef("PLATE");
    }
    
    public double[] getGrabberPosition(){
        return grabber.getPosition();
    }
    
    public double[] getThingsPosition(){
        return grabme.getField("translation").getSFVec3f();
    }
    
    public double[] getPlacePosition(){
        return plate.getField("translation").getSFVec3f();
    }
}


public class InstructionFitnessFunction extends FitnessFunction {
    
    private SuperController supercontroller = null;
    private ExperimentData data = null;
    
    public InstructionFitnessFunction(SuperController supercontroller){
        super();
        this.supercontroller = supercontroller;
        data = new ExperimentData(supercontroller);
        try {
            File fl = new File(Util.resultFilePath);
            if(!fl.exists())
                fl.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(InstructionFitnessFunction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    protected double evaluate(IChromosome ic) {
        double result;
        do{
            System.out.println("[GA] Evaluate chromosome "+Util.InstructionChromosome(fromChromosomeToInstruction(ic)));
            // To analyze the chromosome, we have to simulate it
            executeChromosomeOnSuperController(ic);
            synchronized (this) {
                try {
                    //System.out.println("gonna wait");
                    this.wait();
                } catch (InterruptedException ex) {
                }
            }
            //System.out.println("notified");
                
            // Evaluate the result
            System.out.print("[GA] chromosome "
                    +Util.InstructionChromosome(fromChromosomeToInstruction(ic))
                    +" fitness = ");
            result = evaluateSuperControllerResult(supercontroller.results);
            System.out.println(result);
            //if(result == Double.MIN_NORMAL) System.out.println("REEVALUATION");
        } while(result==Double.MIN_NORMAL);
        
        System.out.println(result);
        if(supercontroller.results!=null)
            supercontroller.results.clear();
        // Return the evaulation result
        return result;
        
    }
    
    private void executeChromosomeOnSuperController(IChromosome ic){
        // Wait until the current simulation finished
        while(supercontroller.isSimulating()) if(Thread.interrupted()) ;

        // Interprete chromosome into instruction set and simulate it
        supercontroller.simulate(fromChromosomeToInstruction(ic));
    }
    
    private double[] fromChromosomeToInstruction(IChromosome ic){
        double[] instructions = new double[ic.size()];
        int i = 0;
        for(Gene g : ic.getGenes()){
            instructions[i++] = (double) g.getAllele();
        }
        return instructions;
    }
    
    public double evaluateSuperControllerResult(ArrayList<double[]> result){
        if(result == null) return Double.MIN_NORMAL;
        double d1 = Util.distance(result.get(0), result.get(1));
        double d2 = Util.distance(result.get(2), result.get(3));
        double d4 = Util.distance(result.get(4), result.get(5));
        double c = Math.abs(d1-d2);
        System.out.print("100.0/("+d4+"+1)+"+(+d1<d2?"-"+c+"/10":"10*"+c+" = "));
        return 1000/(100.0/(d4+1)+(d1<d2?-c/10:10*c));
    }
    
    private static double[] getPosition(ArrayList<String> result, int i){
        double[] position = new double[3];
        position[0] = Double.parseDouble(result.get(i++));
        position[1] = Double.parseDouble(result.get(i++));
        position[2] = Double.parseDouble(result.get(i++));
        return position;
    }
}
