
import com.cyberbotics.webots.controller.Robot;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    public static final String[] filePaths = {
        resultFilePath, resultFilePath, resultGPSFilePath, resultSensorFilePath};
    
    private Util(){
        
    }
    
    public static String printChromosome(IChromosome ic){
        StringBuilder chromosome = new StringBuilder();
        for(Gene g : ic.getGenes()){
            chromosome.append(Double.toString((double)g.getAllele())+" ");
        }
        return chromosome.toString();
    }
    
    public static String printArrayList(List<String> arr){
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
    
    // angle FROM z axis
    public static double getZangle(double[] vect){
        double length = Util.distance(vect, new double[]{0.,0.,0.});
        double z = vect[2];
        double x = Math.sqrt(length*length-z*z);
        double alfa;
        if(z==0){
            alfa = x>0 ? Math.PI/2 : -Math.PI/2;
        }
        else {
            alfa = Math.atan(x/z);
            if (z<0) alfa += (alfa<0?1.:-1.)*Math.PI;
        }
        return alfa;
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
    
    public static void createAllNeededFiles(){
        try {
            File fl ;
            for(String path : filePaths){
                fl = new File(path);
                if(fl.exists()){ 
                    Util.cleanFile(path);
                }
                fl.createNewFile();
                System.out.println("[file] "+path);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Created temporary files");
    }
    
    public static void deleteAllNeededFiles(){
        System.out.println("Deleting temporary files");
        File fl ;
        for(String path : filePaths){
            fl = new File(path);
            if(!fl.exists()) fl.delete();
        }
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
        if(str == null) return null;
        List<Integer> rslt = new ArrayList<Integer>();
        for(String s : str){
            rslt.add(Integer.parseInt(s));
        }
        return rslt;
    }
    
    public static List<Boolean> listToBoolean(List<String> str){
        if(str == null) return null;
        List<Boolean> rslt = new ArrayList<Boolean>();
        for(String s : str){
            rslt.add(Boolean.parseBoolean(s));
        }
        return rslt;
    }
    
    public static List<Double> listToDouble(List<String> str){
        if(str == null) return null;
        List<Double> rslt = new ArrayList<Double>();
        for(String s : str){
            rslt.add(Double.parseDouble(s));
        }
        return rslt;
    }
    
    public static List<String> readFileResult(String filePath){
        try {
            List<String> strVals = new ArrayList<String>();
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
    
    public static void writeFileDouble(String filePath, List<Double> list){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            StringBuilder str = new StringBuilder();
            for(Double d : list){
                bw.write(Double.toString(d));
                bw.newLine();
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void writeFilePositionResult(String filePath, List<double[]> list){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            StringBuilder str = new StringBuilder();
            for(double[] el : list){
                for(double val : el){
                    bw.write(Double.toString(val));
                    bw.newLine();
                }
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static List<double[]> readFilePositionResult(String filePath, int n){
        try {
            List<double[]> posVals = new ArrayList<double[]>();
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            boolean haveToRead = true;
            while(haveToRead){
                int i = 0;
                String inputLine;
                double[] position = new double[n];
                while(i<n){
                    haveToRead = (inputLine = br.readLine()) != null;
                    if(haveToRead){
                        position[i++] = Double.parseDouble(inputLine);
                    }
                    else{
                        break;
                    }
                }
                posVals.add(position);
            }
            br.close();
            if(posVals.size() == 0) return null;
            return posVals;
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
    
    public static void cleanAllFile(){
        for(String path : filePaths){
            cleanFile(path);
        }
    }
    
    public static Double[] doubleConverstion(double[] input){
        Double[] v = new Double[input.length];
        int i = 0;
        for(double d : input)
            v[i++] = Double.valueOf(d);
        return v;
    }
    
    public static double[] doubleConverstion(Double[] input){
        double[] v = new double[input.length];
        int i = 0;
        for(Double d : input)
            v[i++] = d.doubleValue();
        return v;
    }
    
    public static void readMapData(Map<String, List<double[]>> mapResult){
        for(String key : mapResult.keySet()){
            System.out.print("[Map] Key \""+key+"\" contains : ");
            for(double[] val : mapResult.get(key))
                System.out.print(Arrays.toString(val)+" ");
            System.out.println();
        }
    }
    
    /*public static double[] readFileResult(boolean cleanAfterReading){
        List<String> strVal = new List<String>();
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
    }*/
}
