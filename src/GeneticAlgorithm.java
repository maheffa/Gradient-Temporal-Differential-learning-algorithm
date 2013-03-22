
// JGAP library import, containing usefull GA functions
import java.util.ArrayList;
import java.util.List;
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
    private static int MAX_EVOLUTION = 2000;
    
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
            
            conf.addGeneticOperator(new MutationOperator(conf, 100));
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
        int populationsize = population.getConfiguration().getPopulationSize();
        System.out.println("[GA] Started");
        int c = 0;
        //while(populationsize > 2){
            //try {
                for(int i=0; i<GeneticAlgorithm.MAX_EVOLUTION; i++){
                    if(Thread.interrupted()) return;
                    population.evolve();
                    System.out.println("[GA] GENERATION NO "+c+""+i);
                }
                /*populationsize /= 2;
                List<IChromosome> newpopulation = population.getFittestChromosomes(populationsize);
                Configuration.reset();
                Configuration newconf = new DefaultConfiguration();
                newconf.setFitnessFunction(fitnessFunction);
                newconf.setSampleChromosome(sampleChromosome);
                newconf.setPopulationSize(populationsize);
                newconf.addGeneticOperator(new MutationOperator(conf, 100));
                IChromosome[] bestChromosomes = new IChromosome[populationsize];
                for(int i=0; i<populationsize; i++) bestChromosomes[i] = newpopulation.get(i);
                population = new Genotype(newconf, new Population(newconf, bestChromosomes));
            } catch (InvalidConfigurationException ex) {
                Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
        IChromosome fittest = population.getFittestChromosome();
        while(true){
            System.out.println("**********[FITTEEEEEEEEST CHROMOSOME]**********");
            ((InstructionFitnessFunction)fitnessFunction).evaluate(fittest);
            System.out.println("**********[FITTEEEEEEEEST CHROMOSOME]**********");
        }
    }
    
    private Gene[] generateSampleGene(Configuration conf){
        try {
             /*
              * 5 genes
              * 1st for forward or backward movement
              * 2nd for arm2 between -PI and 0
              * 3 - 5th for arm3 - arm5
              */
            Gene[] sampleGene = new Gene[5];
            sampleGene[0] = new IntegerGene(conf, 0, 100);
            sampleGene[1] = new IntegerGene(conf, 0, 200);
            sampleGene[2] = new IntegerGene(conf, 0, 500);
            sampleGene[3] = new IntegerGene(conf, 0, 500);
            sampleGene[4] = new IntegerGene(conf, 0, 5);
            return sampleGene;
        } catch (InvalidConfigurationException ex) {
            Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
