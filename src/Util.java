
import com.cyberbotics.webots.controller.Robot;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.*;

/**
 * Utility class
 */
public class Util {
    /** The time step, multiple of the basic time step specified on Webots */
    public static int TIME_STEP = 20*16;
    private static DecimalFormat df = new DecimalFormat("#.000");
    /** File path for sending information from the supervisor code to the controller*/
    public static final String commonFilePath =
            System.getProperty("java.io.tmpdir")+"webot000";
    /** File path for sending information about the servo's initial angle*/
    public static final String servoSettingFilePath =
            System.getProperty("java.io.tmpdir")+"webotServoSetting000";
    /** File path for sending information about the servo's angle*/
    public static final String servoPositionPath =
            System.getProperty("java.io.tmpdir")+"webotServoPosition000";
    /** File path for sending information about GPS position*/
    public static final String resultGPSFilePath = 
            System.getProperty("java.io.tmpdir")+"webotGPS000";
    /** File path for sending information about touch sensor detector*/
    public static final String resultSensorFilePath = 
            System.getProperty("java.io.tmpdir")+"webotSensor000";
    /** File path for output of statistical result*/
    public static final String resultGTDpaht = "D:\\resultGTD.csv";
    /** Arrays of all needed file path*/ 
    public static final String[] filePaths = {
        resultGTDpaht, commonFilePath, servoSettingFilePath, resultGPSFilePath, resultSensorFilePath, servoPositionPath};
    
    private Util(){
        
    }
    
    /**
     * Return a string representation of the list of string
     * @param arr string list
     * @return representation of arr
     */
    public static String printArrayList(List<String> arr){
        StringBuilder str = new StringBuilder();
        for(String s : arr){
            str.append(s+" ");
        }
        return str.toString();
    }
    
    /**
     * Make the robot controller R's code wait for a sec
     * @param r robot that will wait the simulation
     * @param sec the time the robot should wait for a simulation to finish
     */
    public static void passive_wait(Robot r, double sec) {
        double start_time = r.getTime();
        do {
            step(r);
        } while(start_time + sec > r.getTime());
    }
    
    /**
     * Let the robot make one step
     * @param r the robot's controller
     */
    public static void step(Robot r) {
        if (r.step(Util.TIME_STEP) == -1) {
            System.exit(0);
        }
    }
    
    /**
     * Create all needed file during the launch of the program to avoid file not found exception
     */
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
    
    /**
     * Delete all used temporary files
     */
    public static void deleteAllNeededFiles(){
        System.out.println("Deleting temporary files");
        File fl ;
        for(String path : filePaths){
            fl = new File(path);
            if(!fl.exists()) fl.delete();
        }
    }
    
    /**
     * Interprete a list of string as integer
     * @param str list of string
     * @return resulting list of integer
     */
    public static List<Integer> listToInteger(List<String> str){
        if(str == null) return null;
        List<Integer> rslt = new ArrayList<Integer>();
        for(String s : str){
            rslt.add(Integer.parseInt(s));
        }
        return rslt;
    }
    
    /**
     * Interprete a list of string as Boolean
     * @param str list of string
     * @return resulting list of Boolean
     */
    public static List<Boolean> listToBoolean(List<String> str){
        if(str == null) return null;
        List<Boolean> rslt = new ArrayList<Boolean>();
        for(String s : str){
            rslt.add(Boolean.parseBoolean(s));
        }
        return rslt;
    }
    
    /**
     * Interprete a list of string as Double
     * @param str list of string
     * @return resulting list of Double
     */
    public static List<Double> listToDouble(List<String> str){
        if(str == null) return null;
        List<Double> rslt = new ArrayList<Double>();
        for(String s : str){
            rslt.add(Double.parseDouble(s));
        }
        return rslt;
    }
    
    /**
     * Read the content of filePath
     * @param filePath the file to read
     * @return a list of line from filePath
     */
    public static List<String> readFileResult(String filePath){
        try {
            List<String> strVals = new ArrayList<String>();
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String inputLine;
            while((inputLine = br.readLine()) != null){
                strVals.add(inputLine);
            }
            br.close();
            if(strVals.size() == 0) {
                System.err.println("nothing has been read");
                return null;
            }
            return strVals;
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Write into file a list of integer
     * @param filePath the file into which we want to write
     * @param list the integer list we want to write down
     */
    public static void writeFileInt(String filePath, List<Integer> list){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            StringBuilder str = new StringBuilder();
            for(Integer d : list){
                bw.write(Integer.toString(d));
                bw.newLine();
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Writing down to filePath the content of each array of a list
     * @param filePath the file we want to write down our list of array
     * @param list the list of array itself
     */
    public static void writeFilePositionResult(String filePath, List<double[]> list){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            StringBuilder str = new StringBuilder();
            for(double[] el : list){
                //System.out.println("Writing down "+Arrays.toString(el));
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
    
    /**
     * Read a file as a list of array of size of n
     * @param filePath the file to read
     * @param n the arrays ' length
     * @return a list of array of double read from filePath
     */
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
                //System.out.println("reading up "+Arrays.toString(position));
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
    
    /**
     * write a list of double into file
     * @param filePath the file to write down the list
     * @param list the list to write down into the file
     */
    public static void writeFileDouble(String filePath, List<Double> list){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            StringBuilder str = new StringBuilder();
            //System.out.println("writing down "+filePath);
            for(Double d : list){
                bw.write(Double.toString(d));
                bw.newLine();
                //System.out.println(d);
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Write a string s into the statistical data file
     * @param s the string to write into statistical data file
     */
    public static void writeGTDResultString(String s){
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Util.resultGTDpaht, true)));
            out.print(s);
            out.close();
        } catch (IOException e) {
        }
    }
    
    /**
     * Empty a file
     * @param filePath the file to clean content
     */
    public static void cleanFile(String filePath){
        try {
            File f = new File(filePath);
            f.delete();
            f  = new File(filePath);
            f.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Clean all temporary file
     */
    public static void cleanAllFile(){
        for(String path : filePaths){
            cleanFile(path);
        }
    }
    
    /**
     * Convert a double[] array to Double[] array
     * @param input the double[] array
     * @return the corresponding Double[] array
     */
    public static Double[] doubleConverstion(double[] input){
        Double[] v = new Double[input.length];
        int i = 0;
        for(double d : input)
            v[i++] = Double.valueOf(d);
        return v;
    }
    
    /**
     * Convert a Double[] array to double[] array
     * @param input the Double[] array
     * @return the corresponding double[] array
     */
    public static double[] doubleConverstion(Double[] input){
        double[] v = new double[input.length];
        int i = 0;
        for(Double d : input)
            v[i++] = d.doubleValue();
        return v;
    }
    
    /**
     * Output the content of a map containing String as key and list of double as value
     * @param mapResult the map to be read
     */
    public static void readMapData(Map<String, List<double[]>> mapResult){
        for(String key : mapResult.keySet()){
            System.out.print("[Map] Key \""+key+"\" contains : ");
            for(double[] val : mapResult.get(key))
                System.out.print(Arrays.toString(val)+" ");
            System.out.println();
        }
    }
}
