
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.IChromosome;

public class InstructionFitnessFunction extends FitnessFunction {
    
    ArrayList<String> resultFromSC = new ArrayList<String>();
    
    @Override
    protected double evaluate(IChromosome ic) {
        System.out.println("[GA] Evaluation of chromosome "+Util.printChromosome(ic));
        try {
            // To analyze the chromosome, we have to simulate it
            executeChromosomeOnSuperController(ic);
            
            // Read then the chromosome result (it will wait until something from resultServer is readen)
            obtainChromosomeResultFromSuperController();
                    
            // Evaluate the result
            double result = evaluateResultFromSuperController();
            
            // Return the evaulation result
            return result;
            
        } catch (IOException ex) {
            System.err.println("[GA] input output error");
            ex.printStackTrace();
        }
        return 1;
    }
    
    private void executeChromosomeOnSuperController(IChromosome ic) throws IOException{
        // TODO: Double check this method
        
        // Connect to supercontroller server and send instruction from chromosome
        System.out.println("[GA] Creating socket");
        Socket socket = new Socket(InetAddress.getLocalHost(), Util.RL_PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        // Wait until SuperController is ready
        in.readLine();
        
        // Send the chromosome information
        System.out.println("[GA] Sending chromosome information");
        for(String instruction : fromChromosomeToInstruction(ic)){
            out.println(instruction);
        }
        
        // Closing
        out.close();
        in.close();
        socket.close();
    }
    
    private void obtainChromosomeResultFromSuperController() throws IOException{
        // TODO: Double check this method
        
        // Connect to supercontroller server and send instruction from chromosome
        System.out.println("[GA] Creating socket");
        Socket socket = new Socket(InetAddress.getLocalHost(), Util.RESULT_PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        // Read the result from supercontroller
        System.out.println("[GA] Reading simulation result");
        String inputLine;
        while((inputLine = in.readLine()) != null){
            resultFromSC.add(inputLine);
        }
        
        // Closing
        in.close();
        socket.close();
    }
    
    private double evaluateResultFromSuperController() {
        System.out.println("[GA] Evaluation simulation result");
        return evaluateSuperControllerResult(resultFromSC);
    }
    
    public static ArrayList<String> fromChromosomeToInstruction(IChromosome ic){
        /*
         * TODO: toverify
         */
        ArrayList<String> instructions = new ArrayList<String>();
        for(Gene g : ic.getGenes()){
            instructions.add(Double.toString((double)g.getAllele()));
        }
        return instructions;
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
