
import com.cyberbotics.webots.controller.Robot;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
public class Util {
    
    public static int TIME_STEP = 20*16;
    public static DecimalFormat df = new DecimalFormat("#.000");
    public static final String commonFilePath =
            System.getProperty("java.io.tmpdir")+"webot000";
    public static final String resultFilePath =
            System.getProperty("java.io.tmpdir")+"webotRes000";
    public static final String resultGPSFilePath = 
            System.getProperty("java.io.tmpdir")+"webotGPS000";
    public static final String resultSensorFilePath = 
            System.getProperty("java.io.tmpdir")+"webotSensor000";
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
    
    public static void setWaitSupervisor(boolean val){
        System.setProperty("supervisor_wait", Boolean.toString(val));
    }
    
    public static boolean isWaitSupervisor(){
        return Boolean.parseBoolean(System.getProperty("supervisor_wait"));
    }
    
    public static String InstructionChromosome(double[] instruction){
        StringBuilder builder = new StringBuilder();
        builder.append("["+df.format(instruction[0]));
        for(int i=1; i<instruction.length; i++)
            builder.append(":"+df.format(instruction[i]));
        builder.append("]");
        return builder.toString();
    }
    
    public static void writeFileResult(double value){
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Util.resultFilePath, true)));
            out.println(Double.toString(value));
            out.close();
        } catch (IOException e) {
        }
    }
    
    public static List<Integer> listToInteger(List<String> str){
        List<Integer> rslt = new ArrayList<Integer>();
        for(String s : str){
            rslt.add(Integer.parseInt(s));
        }
        return rslt;
    }
    
    public static List<Boolean> listToBoolean(List<String> str){
        List<Boolean> rslt = new ArrayList<Boolean>();
        for(String s : str){
            rslt.add(Boolean.parseBoolean(s));
        }
        return rslt;
    }
    
    public static List<Double> listToDouble(List<String> str){
        List<Double> rslt = new ArrayList<Double>();
        for(String s : str){
            rslt.add(Double.parseDouble(s));
        }
        return rslt;
    }
    
    public static List<String> readFileResult(String filePath){
        try {
            ArrayList<String> strVals = new ArrayList<String>();
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String inputLine;
            while((inputLine = br.readLine()) != null){
                strVals.add(inputLine);
            }
            br.close();
            if(strVals.size() == 0) return null;
            return strVals;
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static void writeFileResult(String filePath, List<Class<?>> list){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            StringBuilder str = new StringBuilder();
            for(Class<?> el : list){
                str.append(el.toString());
            }
            bw.write(str.toString());
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void cleanFile(String filePath){
        try {
            File f = new File(Util.commonFilePath);
            f.delete();
            f  = new File(Util.commonFilePath);
            f.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static double[] readFileResult(boolean cleanAfterReading){
        ArrayList<String> strVal = new ArrayList<String>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(Util.resultFilePath));
            String inputLine;
            while((inputLine = br.readLine()) != null){
                strVal.add(inputLine);
            }
            br.close();
            if(strVal.size() == 0) return null;
            double[] val = new double[strVal.size()];
            int i=0;
            for(String s : strVal){
                val[i++] = Double.parseDouble(s);
            }
            if(cleanAfterReading){
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Util.resultFilePath, false)));
                out.print("");
                out.close();
            }
            return val;
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
