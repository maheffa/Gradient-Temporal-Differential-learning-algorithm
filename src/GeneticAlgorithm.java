
// JGAP library import, containing usefull GA functions
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgap.*;
import org.jgap.impl.*;

/**
 *
 * @author Mahefa
 */
public class GeneticAlgorithm extends Thread {
    
    private static int POPULATION_SIZE = 10;
    private static int MAX_EVOLUTION = 1000;
    
    private ArrayList<double[]> simulationResult;
    private Configuration conf = null;
    public FitnessFunction fitnessFunction = null;
    private Gene[] sampleGenes = null;
    private Chromosome sampleChromosome = null;
    private Genotype population = null;
    
    public GeneticAlgorithm(SuperController supercontroller){
        try {
            // Start with a DefaultConfiguration, which comes setup with the
            conf = new DefaultConfiguration();
            
            // Set the the fitness function (which is InstructionFitnessFunction)
            fitnessFunction =  new InstructionFitnessFunction(supercontroller);
            conf.setFitnessFunction(fitnessFunction);
            
            // Generate the gene sample
            sampleGenes = generateSampleGene(conf);
            
            // Create a sample chromosome from gene sample and configuration
            sampleChromosome = new Chromosome(conf, sampleGenes );
            conf.setSampleChromosome( sampleChromosome );
            
            // Set population size
            conf.setPopulationSize(GeneticAlgorithm.POPULATION_SIZE);
            
            // Create random population from the gene and chromosome sample
                population = Genotype.randomInitialGenotype( conf );
        } catch (InvalidConfigurationException ex) {
            Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    
    public synchronized void setResult(){
        
    }
    
    @Override
    public void run() {
        for(int i=0; i<GeneticAlgorithm.MAX_EVOLUTION; i++){
            if(Thread.interrupted()) return;
            population.evolve();
        }
    }
    
    private Gene[] generateSampleGene(Configuration conf){
        try {
             /*
              * create the chromosomes's gene sample
              * the chromosome is composed with 7 gene
              * the first two help with rotation and translation
              * the last five gene correspond to the angle of each 5 Servo
              */
            Gene[] sampleGene = new Gene[7];
            sampleGene[0] = new DoubleGene(conf, -50.0, 50.0 );
            sampleGene[1] = new DoubleGene(conf, -50.0, 50.0 );
            for(int i=2; i<7; i++)
                sampleGene[i] = new DoubleGene(conf, -Math.PI, Math.PI);
            return sampleGene;
        } catch (InvalidConfigurationException ex) {
            Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
