
import com.cyberbotics.webots.controller.Robot;
import java.util.ArrayList;
import org.jgap.Gene;
import org.jgap.IChromosome;





/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Mahefa
 */
public class Util {
    
    public static int TIME_STEP = 16;
    
    public static String commonFilePath = System.getProperty("java.io.tmpdir")+"webotControllerSupervisor";
    
    private Util(){
        
    }
    
    public static String printChromosome(IChromosome ic){
        StringBuilder chromosome = new StringBuilder();
        for(Gene g : ic.getGenes()){
            chromosome.append(Double.toString((double)g.getAllele())+" ");
        }
        return chromosome.toString();
    }
    
    public static String printArrayList(ArrayList<String> arr){
        StringBuilder str = new StringBuilder();
        for(String s : arr){
            str.append(s+" ");
        }
        return str.toString();
    }
    
    public static double bound(double v, double a, double b){
        return (v>b) ? b : (v<a) ? a : v;
    }
    
    public static double distance(double[] pos1, double[] pos2){
        return Math.sqrt((pos1[0]-pos2[0])*(pos1[0]-pos2[0])
                +(pos1[1]-pos2[1])*(pos1[1]-pos2[1])
                +(pos1[2]-pos2[2])*(pos1[2]-pos2[2]));
    }
    
    public static void passive_wait(Robot r, double sec) {
        double start_time = r.getTime();
        do {
            step(r);
        } while(start_time + sec > r.getTime());
    }
    
    public static void step(Robot r) {
        if (r.step(Util.TIME_STEP) == -1) {
            System.exit(0);
        }
    }
    
    
}
