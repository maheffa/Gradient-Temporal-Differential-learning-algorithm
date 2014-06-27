/**
 * Class containing all math calculus formula needed
 */
class TinyMath {
    
    /**
     * Calculate the distance between two coordinates
     * @param pos1 first coordinate
     * @param pos2 second coordinate
     * @return distance between @param pos1 and @param pos2
     */
    public static double distance(double[] pos1, double[] pos2){
        return Math.sqrt((pos1[0]-pos2[0])*(pos1[0]-pos2[0])
                +(pos1[1]-pos2[1])*(pos1[1]-pos2[1])
                +(pos1[2]-pos2[2])*(pos1[2]-pos2[2]));
    }
    
    /**
     * Vector subtraction
     * @param v0 vector to subtract
     * @param v1 subtracting vector
     * @return the result of @param v0 - @param v1
     */
    public static double[] subVector(double[] v0, double[] v1){
        if(v0.length!=v1.length) return null;
        double[] v = new double[v0.length];
        for(int i=0; i<v.length; i++) v[i] = v0[i] - v1[i];
        return v;
    }
    
    /**
     * Vector addition
     * @param v0 first vector
     * @param v1 second vector
     * @return the result of @param v0 + @param v1
     */
    public static double[] addVector(double[] v0, double[] v1){
        if(v0.length!=v1.length) return null;
        double[] v = new double[v0.length];
        for(int i=0; i<v.length; i++) v[i] = v0[i] + v1[i];
        return v;
    }
    
    /**
     * Dot product of two vector
     * @param v0 first vector
     * @param v1 second vector
     * @return result of @param v0 x @param v1
     */
    public static double multiplyVectorTransp(double[] v0, double[] v1){
        if(v0.length!=v1.length) return 0;
        double v = 0.0;
        for(int i=0; i<v0.length; i++) v += v0[i] * v1[i];
        return v;
    }
    
    /**
     * Multiply a vector with a coefficient
     * @param coeff a coefficient
     * @param v0 a vector
     * @return the result of @param coeff * @param v0
     */
    public static double[] multiplyVectorCoeff(double coeff, double[] v0){
        double[] v = new double[v0.length];
        for(int i=0; i<v.length; i++) v[i] = coeff*v0[i];
        return v;
    }
}