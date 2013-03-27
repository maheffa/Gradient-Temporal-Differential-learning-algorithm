class TinyMath {
    
    public static double distance(double[] pos1, double[] pos2){
        return Math.sqrt((pos1[0]-pos2[0])*(pos1[0]-pos2[0])
                +(pos1[1]-pos2[1])*(pos1[1]-pos2[1])
                +(pos1[2]-pos2[2])*(pos1[2]-pos2[2]));
    }
    
    // angle from OA to OB
    public static double getAngle(double[] OA, double[] OB, double[] robot){
        double[] O = new double[]{0.,0.,0.};
        double dot = dotProd(OA, OB);
        double d1 = TinyMath.distance(O, OA);
        double d2 = TinyMath.distance(O, OB);
        return Math.acos(dot/d1/d2)*
                Math.signum(dotProd(robot, crossProd(OA, OB)));
    }
    
    public static double dotProd(double[] a, double[] b){
        return a[0]*b[0] + a[1]*b[1] + a[2]*b[2];
    }
    
    public static double[] crossProd(double[] a, double[] b){
        double i = a[1]*b[2] - a[2]*b[1];
        double j = -(a[0]*b[2] - a[2]*b[0]);
        double k = a[0]*b[1] - a[1]*b[0];
        return new double[]{i, j, k};
    }
    
    public static double[] addVector(double[] v0, double[] v1){
        if(v0.length!=v1.length) return null;
        double[] v = new double[v0.length];
        for(int i=0; i<v.length; i++) v[i] = v0[i] + v1[i];
        return v;
    }
    
    public static double multiplyVectorTransp(double[] v0, double[] v1){
        if(v0.length!=v1.length) return 0;
        double v = 0.0;
        for(int i=0; i<v0.length; i++) v += v0[i] * v1[i];
        return v;
    }
    
    public static double[] multiplyVectorCoeff(double coeff, double[] v0){
        double[] v = new double[v0.length];
        for(int i=0; i<v.length; i++) v[i] = coeff*v0[i];
        return v;
    }
}