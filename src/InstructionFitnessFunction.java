
import com.cyberbotics.webots.controller.Field;
import java.util.ArrayList;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.IChromosome;

class ExperimentData{
    
    private final String[] elementsDEF = {"Grabber", "GrabMe", "PLATE"};
    private Field[] translations = null;
    private SuperController superController = null;
    
    public ExperimentData(SuperController superController){
        this.superController = superController;
        translations = new Field[]{
            superController.getFromDef(this.elementsDEF[0]).getField("translation"),
            superController.getFromDef(this.elementsDEF[1]).getField("translation"),
            superController.getFromDef(this.elementsDEF[2]).getField("translation")
        };
    }
    
    public double[] getGrabberPosition(){
        return translations[0].getSFVec3f();
    }
    
    public double[] getThingsPosition(){
        return translations[1].getSFVec3f();
    }
    
    public double[] getPlacePosition(){
        return translations[2].getSFVec3f();
    }
}


public class InstructionFitnessFunction extends FitnessFunction {
    
    private SuperController supercontroller = null;
    private ExperimentData data = null;
    
    public InstructionFitnessFunction(SuperController supercontroller){
        super();
        this.supercontroller = supercontroller;
        data = new ExperimentData(supercontroller);
    }
    
    @Override
    protected double evaluate(IChromosome ic) {
        System.out.println("[GA] Evaluation of chromosome "+Util.printChromosome(ic));
        // To analyze the chromosome, we have to simulate it
        executeChromosomeOnSuperController(ic);
        
        // Read then the chromosome result (it will wait until something from resultServer is readen)
        obtainChromosomeResultFromSuperController();
        
        // Evaluate the result
        double result = evaluateResultFromSuperController();
        
        // Return the evaulation result
        return result;
        
    }
    
    private void executeChromosomeOnSuperController(IChromosome ic){
        // Wait until the current simulation finished
        while(supercontroller.isSimulating()) if(Thread.interrupted()) return;
        
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
    
    private void obtainChromosomeResultFromSuperController(){
        // TODO: loop until simulation is done
        
        // TODO: read the result
    }
    
    private double evaluateResultFromSuperController(){
        // TODO: evaluate the result to give the fitness value
        return Math.random()+1;
    }
    
    public static double evaluateSuperControllerResult(ArrayList<String> result){
        /*
         * TODO: think twice about what should the supercontroller return
         */
        // assume that the supercontroller return the grabber's hand position
        // the things to grab's position
        // the place where to put the things to grabs position
        // each 3 consecutive line will be the coorinate of something
        double[] grabberPosition = getPosition(result, 0);
        double[] thingsPosition = getPosition(result, 3);
        double[] placePosition = getPosition(result, 6);
        
        // let's say the closer the place and the things position
        // the better is the value of the fitness function
        return 100/(1+Util.distance(placePosition, thingsPosition));
    }
    
    private static double[] getPosition(ArrayList<String> result, int i){
        double[] position = new double[3];
        position[0] = Double.parseDouble(result.get(i++));
        position[1] = Double.parseDouble(result.get(i++));
        position[2] = Double.parseDouble(result.get(i++));
        return position;
    }
}
