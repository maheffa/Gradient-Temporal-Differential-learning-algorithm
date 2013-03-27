import java.text.DecimalFormat;

public class Matrix {
    
    public int height,width;
    public double[][] val;
    
    public Matrix(){
        height = 0;
        width = 0;
        val = null;
    }
    
    public Matrix(int n,int m){
        if(n>0&&m>0){
            val = new double[n][m];
            height = n;
            width = m;
        }
    }
    
    public Matrix(Matrix A){
        val = new double[A.height][A.width];
        height = A.height;
        width = A.width;
        for(int i=0;i<height;i++)
            for(int j=0;j<width;j++)
                val[i][j] = A.val[i][j];
    }
    
    public Matrix(double[][] A){
        height = A.length;
        width = A[0].length;
        val = new double[height][width];
        for(int i=0;i<height;i++)
            for(int j=0;j<width;j++)
                val[i][j]=A[i][j];
    }
    
    public Matrix(double[] A){
        height = 1;
        width = A.length;
        val = new double[height][width];
        for(int i=0;i<width;i++)
            val[0][i] = A[i];
    }
    
    // ***** basic operations *****
    
    public Matrix add(Matrix A){
        Matrix B = new Matrix(height,width);
        for(int i=0;i<height;i++)
            for(int j=0;j<width;j++)
                B.val[i][j] = val[i][j]+A.val[i][j];
        return B;
    }
    
    public Matrix multiply(double d){
        Matrix B = new Matrix(height,width);
        for(int i=0;i<height;i++)
            for(int j=0;j<width;j++)
                B.val[i][j] = d*val[i][j];
        return B;
    }
    
    public Matrix multiply(Matrix B){
        if(B==null) return null;
        if(width!=B.height) return null;
        Matrix A=this,C = new Matrix(A.height,B.width);
        for(int i=0;i<C.height;i++)
            for(int j=0;j<C.width;j++)
                for(int k=0;k<A.width;k++)
                    C.val[i][j] += A.val[i][k]*B.val[k][j];
        return C;
    }
    
    public Matrix transpose(){
        Matrix A = new Matrix(width,height);
        for(int i=0;i<height;i++)
            for(int j=0;j<width;j++)
                A.val[j][i] = val[i][j];
        return A;
    }
    
    // ***** more advanced operation *****
    
    public void swapLine(int i,int j){
        if(i>=height||j>=height||i<0||j<0||i==j) return;
        double c;
        for(int k=0;k<width;k++){
            c = val[i][k];
            val[i][k] = val[j][k];
            val[j][k] = c;
        }
    }
    
    public void multiplyLine(int i,double k){
        if(i>=height||i<0) return ;
        for(int j=0;j<width;j++)
            val[i][j] *= k;
    }
    
    public int maxLineGauss(int n){
        // line i>=n in the nth column containing the maximum element
        // needed before swapping in Gauss method
        if(n<0||n>=height||n>=width) return -1;
        int imax,i;
        double max;
        for(imax=n,i=n+1,max=Math.abs(val[n][n]);i<height;i++)
            if(max<Math.abs(val[i][n])){
                max=Math.abs(val[i][n]);
                imax=i;
            }
        return imax;
    }
    
    public double determinant(){
        int sign,i;
        double d = 0;
        Matrix B = new Matrix(this);
        Matrix A = new Matrix(this);
        sign=ForwardStepGauss(A,null);
        if (sign!=0){
            for (i=0,d=1;i<A.height;i++)
                d*=B.val[i][i];
        }
        else
            // what to do??? we're lost, determinant is null
            ;
        return sign*d;
    }
    
    public Matrix Solve(Matrix A, Matrix B){
        // A*X = B; return X;
        if(A.height!=A.width||A.height!=B.height) return null; // A isn't square or An<>Bn
        Matrix Aclone = new Matrix(A);
        Matrix Bclone = new Matrix(B);
        int p=ForwardStepGauss(Aclone,Bclone);
        if(p!=0)
            return BackwardStepGauss(Aclone,Bclone).transpose();
        else
            return null;
    }
    
    public Matrix invert(){
        Matrix I = this.unary();
        if(I==null) return null;
        return Solve(this,this.unary());
    }
    
    private int ForwardStepGauss(Matrix A, Matrix R){
        int xchg=0,i;
        for (i=0;i<A.height;i++){
            int l = A.maxLineGauss(i);
            xchg=l==i?xchg:xchg+1;
            A.swapLine(l, i);
            if(A.val[i][i]==0) return 0;
            if(R!=null)R.swapLine(l,i);
            for (int p=i+1;p<A.height;p++){
                double u=A.val[p][i]/A.val[i][i];
                for (int q=i;q<A.width;q++)
                    A.val[p][q]-=u*A.val[i][q];
                if(R!=null)
                    for (int r=0;r<R.width;r++)
                        R.val[p][r]-=u*R.val[i][r];
            }
        }
        return xchg%2==1?-1:1;
    }
    
    private Matrix BackwardStepGauss(Matrix A, Matrix B){
        Matrix X = new Matrix(B.width,B.height);
        for (int i=A.height-1;i>=0;i--)
            for(int j=0;j<B.width;j++){
                double x = B.val[i][j];
                int t=i;
                while(t<A.height-1){
                    x-=X.val[j][t+1]*A.val[i][t+1];
                    t++;
                }
                X.val[j][i]=x/A.val[i][i];
            }
        return X;
    }
    
    // ***** basic tools *****
    
    public boolean equal(Matrix M){
        if(height!=M.height||width!=M.width) return false;
        for(int i=0;i<height;i++)
            for(int j=0;j<width;j++)
                if(val[i][j]!=M.val[i][j])
                    return false;
        return true;
    }
    
    public boolean isEmpty(){
        return val!=null&&height>0&&width>0;
    }
    
    public Matrix unary(){
        if(height!=width) return null;
        Matrix I = new Matrix(this.height,this.width);
        for(int i=0;i<height;i++)
            I.val[i][i]=1;
        return I;
    }
    
    public void set(Matrix A){
        val = new double[A.height][A.width];
        height = A.height;
        width = A.width;
        for(int i=0;i<height;i++)
            for(int j=0;j<width;j++)
                val[i][j] = A.val[i][j];
    }
    
    public double min(){
        double min=val[0][0];
        for(int i=0;i<height;i++){
            double t=minLine(i);
            if(min>t)
                min=t;
        }
        return min;
    }
    
    public double max(){
        double max=val[0][0];
        for(int i=0;i<height;i++){
            double t=maxLine(i);
            if(max<t)
                max=t;
        }
        return max;
    }
    
    public double minLine(int i){
        double min=val[i][0];
        for(int j=0;j<width;j++)
            if(min>val[i][j])
                min=val[i][j];
        return min;
    }
    
    public double maxLine(int i){
        double max=val[i][0];
        for(int j=0;j<width;j++)
            if(max<val[i][j])
                max=val[i][j];
        return max;
    }
    
    public double minColumn(int j){
        double min=val[0][j];
        for(int i=0;i<height;i++)
            if(min>val[i][j])
                min=val[i][j];
        return min;
    }
    
    public double maxColumn(int j){
        double max=val[0][j];
        for(int i=0;i<height;i++)
            if(max<val[i][j])
                max=val[i][j];
        return max;
    }
    
    public double[][] toArray(){
        Matrix B = new Matrix(this);
        return B.val;
    }
    
    public double[] getLine(int i){
        double[] t = new double[width];
        for(int j=0;j<width;j++)
            t[j] = val[i][j];
        return t;
    }
    
    public double[] getColumn(int j){
        double[] t = new double[height];
        for(int i=0;i<height;i++)
            t[i] = val[i][j];
        return t;
    }
    
    public String size(){
        return "("+height+","+width+")";
    }
    
    public String toString(){
        if(height==0||width==0) return "<Matrix not initialized>";
        String str = new String("");
        DecimalFormat df = new DecimalFormat("#.#####");
        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++)
                str += df.format(val[i][j])+" ";
            str += "\n";
        }
        return str;
    }
}
