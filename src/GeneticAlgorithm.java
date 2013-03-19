
// JGAP library import, containing usefull GA functions
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgap.*;
import org.jgap.impl.*;

/**
 *
 * @author Mahefa
 */
public class GeneticAlgorithm extends Thread {
    
    private static int POPULATION_SIZE = 500;
    private static int MAX_EVOLUTION = 1000;
    
    private Configuration conf = null;
    private FitnessFunction fitnessFunction = null;
    private Gene[] sampleGenes = null;
    private Chromosome sampleChromosome = null;
    private Genotype population = null;
    
    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            System.out.println("[GA] Launching genetic algorithm");
            
            // Start with a DefaultConfiguration, which comes setup with the
            conf = new DefaultConfiguration();
            
            // Set the the fitness function (which is InstructionFitnessFunction)
            fitnessFunction =  new InstructionFitnessFunction();
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
            
            // Let the population to evolve
            for(int i=0; i<GeneticAlgorithm.MAX_EVOLUTION; i++){
                population.evolve();
            }
            
        } catch (Exception ex) {
            Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }

    private Gene[] generateSampleGene(Configuration conf){
        try {
            // create the chromosomes's gene sample
            // the chromosome is composed with 5 gene
            // each gene correspond to the angle of each SERVO
            Gene[] sampleGene = new Gene[5];
            for(int i=0; i<5; i++)
                sampleGene[i] = new DoubleGene(conf, -Math.PI, Math.PI);
            return sampleGene;
        } catch (InvalidConfigurationException ex) {
            Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
}
