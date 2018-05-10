package biodigestorindiano;

/**
 *
 * @author WELL1NGTON
 */
public class OtimizaBiodigestor {
    
    abstract public class Funcao{
        public abstract double f(double[] x);
    }
    
    private double Vb = 25;
    private double mi = 0.01;
    private final int QTD_VAR = 2;
    private final int QTD_REST_LE = 5;
    private final int QTD_REST_E = 0;
    private final int MAX = QTD_VAR+QTD_REST_LE*2+QTD_REST_E;
    private final int total_variaveis = QTD_VAR+QTD_REST_LE*2+QTD_REST_E;
    private final String endln = System.getProperty("line.separator");
    
    private Funcao funcao_Principal = null;
    private Funcao[] restricoes_LessEqual = null;
    private Funcao[] restricoes_Equal = null;
    private boolean prints = false;
    
    public void set_Prints(boolean test){
        prints = test;
    }
    
    public void set_Vb(double valor){
        Vb = valor;
    }
    
    public void set_mi(double valor){
        mi = valor;
    }
    
    public void set_funcao_Principal(){
        funcao_Principal = new funcao_Biodigestor_Indiano();
    }
    
    public void set_restricoes_LessEqual(){
        restricoes_LessEqual = new Funcao[QTD_REST_LE];
        
        restricoes_LessEqual[0] = new restricao1_LE();
        restricoes_LessEqual[1] = new restricao2_LE();
        restricoes_LessEqual[2] = new restricao3_LE();
        restricoes_LessEqual[3] = new restricao4_LE();
        restricoes_LessEqual[4] = new restricao5_LE();
    }
    
    public void set_restricoes_Equal(){
        restricoes_Equal = new Funcao[QTD_REST_E];
    }
    
    //Função principal a ser minimizada
    class funcao_Biodigestor_Indiano extends Funcao{
        
        @Override
        public double f(double[] x){
            return (Math.PI) * x[0] * x[0] * x[1] / 4;
        }
    }
    
    //Restrições do tipo R(x) <= 0
    class restricao1_LE extends Funcao{
        
        @Override
        public double f(double[] x){
            return Vb - ((Math.PI) * x[0] * x[0] * x[1]) / 4;
        }
    }
    
    class restricao2_LE extends Funcao{
        
        @Override
        public double f(double[] x){
            return x[0] - x[1];
        }
    }
    
    class restricao3_LE extends Funcao{
        
        @Override
        public double f(double[] x){
            return  0.6 * x[1] - x[0];
        }
    }
    
    class restricao4_LE extends Funcao{
        
        @Override
        public double f(double[] x){
            return  x[1] - 6;
        }
    }
    
    class restricao5_LE extends Funcao{
        
        @Override
        public double f(double[] x){
            return  3 - x[1];
        }
    }
    
    public boolean gaussPivoParcialSemTrocas(int n,double[][] m,double[] vt,double[] vs){
        int[] p = new int[MAX];
        int i, aux, maior, j, k;
        double d, soma;

        for(i = 0; i < n; i++)
            p[i] = i;
        for(k = 0;k < n-1; k++){
            maior = k;
            for(i = k+1; i < n; i++)
                if(Math.abs(m[p[i]][k]) > Math.abs(m[p[maior]][k]))
                    maior = i;
            aux = p[k];
            p[k] = p[maior];
            p[maior] = aux;
            for(i = k+1; i < n; i++){
                d = m[p[i]][k] / m[p[k]][k];
                m[p[i]][k] = 0;
                for(j = k+1; j < n; j++)
                    m[p[i]][j] = m[p[i]][j] - d * m[p[k]][j];
                vt[p[i]] = vt[p[i]] - d * vt[p[k]];
            }
        }

        vs[n-1] = vt[p[n-1]] / m[p[n-1]][n-1];
        for(i = n-2; i >= 0; i--){
            soma=0;
            for(j = i+1; j < n; j++)
                soma += m[p[i]][j] * vs[j];
            vs[i] = (vt[p[i]]-soma) / m[p[i]][i];
        }

        return true;
    }
    
    public double funcao_Mais_C(Funcao func, double[] x, int n, int pos, double c){
        double[] y = new double[n];
        
        System.arraycopy(x, 0, y, 0, n);
        //memcpy(y,x,sizeof(double)*n);
        y[pos] += c;
        return func.f(y);
    }
    
    public double funcao_Mais_2_C(Funcao funcao, double[] x, int n, int pos1, int pos2, double c1, double c2){
        double[] y = new double[n];
        
        System.arraycopy(x, 0, y, 0, n);
        //memcpy(y,x,sizeof(double)*n);
        y[pos1] += c1;
        y[pos2] += c2;
        return funcao.f(y);
    }
    
    public double derivada_Parcial_1_ordem(Funcao funcao,double[] x, int n, int i, double epsilon){
        double erro, erro_ant, h, df_ant, df;
        int iteracoes_max = 10;

        erro = 99999;
        h = 0.000001;

        df = (funcao_Mais_C(funcao, x, n, i, h) - funcao_Mais_C(funcao, x, n ,i , -h)) / (2 * h);

        do{
            h /= 2;
            df_ant = df;
            df = ( funcao_Mais_C(funcao, x, n, i, h) - funcao_Mais_C(funcao, x, n, i, -h) ) / (2 * h);
            erro_ant = erro;
            erro = Math.abs(df - df_ant) / ( (1 > Math.abs(df)) ? (1) : (Math.abs(df)) );
        }while(erro > epsilon && erro_ant > erro && --iteracoes_max != 0);

        return df;
    }
    
    public double derivada_Parcial_2_ordem(Funcao funcao, double[] x, int n, int i, int j, double epsilon){
        double erro, erro_ant, h, df_ant, df;
        int iteracoes_max = 10;


        erro = 99999;
        h = 0.000001;
        df = (i==j) ? ( (funcao_Mais_C(funcao, x, n, i, 2 * h) - 2 * funcao.f(x) + funcao_Mais_C(funcao, x, n, i, -2 * h)) / (4 * h * h) ):
                      ( (funcao_Mais_2_C(funcao, x, n, i, j, h, h) - funcao_Mais_2_C(funcao, x, n, i, j, h, -h) - funcao_Mais_2_C(funcao, x, n, i, j, -h, h) + funcao_Mais_2_C(funcao, x, n, i, j, -h, -h)) / (4 * h * h) );
        do{
            h /= 2;
            df_ant = df;
            df = (i==j) ? ( (funcao_Mais_C(funcao, x, n, j, 2 * h) - 2 * funcao.f(x) + funcao_Mais_C(funcao, x, n, j, -2 * h)) / (4 * h * h) ):
                          ( (funcao_Mais_2_C(funcao, x, n, i, j, h, h) - funcao_Mais_2_C(funcao, x, n, i, j, h, -h) - funcao_Mais_2_C(funcao, x, n, i, j, -h, h) + funcao_Mais_2_C(funcao, x, n, i, j, -h, -h)) / (4 * h * h) );
            erro_ant = erro;
            erro = Math.abs(df-df_ant) / ( (1 > Math.abs(df)) ? (1) : (Math.abs(df)));
        }while(erro > epsilon && erro_ant > erro && --iteracoes_max != 0);

        return df;
    }
    
    public void calcula_Gradiente(Funcao funcao,double[] x, int n, double[] grad,double epsilon){
        for(int i=0; i<n;i++)
            grad[i] = derivada_Parcial_1_ordem(funcao,x,n,i,epsilon);
    }
    
    public void calcula_Hessiana(Funcao funcao, double[] x, int n, double[][] hessiana, double epsilon){
        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++)
                hessiana[i][j] = derivada_Parcial_2_ordem(funcao,x,n,i,j,epsilon);
    }
    
    public double funcao_Primal_Dual_Barreira_Logaritmica(Funcao funcao, Funcao[] restricoes_LE, int num_RestLE,
                                                          Funcao[] restricoes_E,int num_RestE, double[] x, int nx, double[] s, int ns,
                                                          double[] lambda, int nlambda, double[] pi, int npi, double mi){
        double soma;
        double total;

        //f(x)
        total = funcao.f(x);

        //f(x) - mi * soma(ln(S_j))
        soma = 0;
        for(int j=0; j < ns; j++)
            soma += Math.log(s[j]);
        total -= mi*soma;

        //f(x) - mi * soma(ln(S_j)) + soma(lambda_i * restricao_igualdade_i)
        soma = 0;
        for(int i=0; i < nlambda; i++)
            soma += lambda[i]*(restricoes_E[i]).f(x);
        total += soma;

        //f(x) - mi * soma(ln(S_j)) + soma(lambda_i * restricao_igualdade_i) + soma(pi_j * restricao_menor_igual_j + S_j)
        soma = 0;
        for(int j=0; j < npi; j++)
            soma += pi[j]*((restricoes_LE[j]).f(x)+s[j]);
        total += soma;

        return total;
    }
    
    public void x_to_vars(double[] x, double[] var, double[] s, double[] lambda, double[] pi){
        System.arraycopy(x, 0, var, 0, QTD_VAR);
        System.arraycopy(x, QTD_VAR, s, 0, QTD_REST_LE);
        System.arraycopy(x, QTD_VAR+QTD_REST_LE, lambda, 0, QTD_REST_E);
        System.arraycopy(x, QTD_VAR+QTD_REST_LE+QTD_REST_E, pi, 0, QTD_REST_LE);
    }
    
    public void gera_vetor_x(double[] x, double[] var, double[] s, double[] lambda, double[] pi){
        System.arraycopy(var, 0, x, 0, QTD_VAR);
        System.arraycopy(s, 0, x, QTD_VAR, QTD_REST_LE);
        System.arraycopy(lambda, 0, x, QTD_VAR+QTD_REST_LE, QTD_REST_E);
        System.arraycopy(pi, 0, x, QTD_VAR+QTD_REST_LE+QTD_REST_E, QTD_REST_LE);
    }
    
    class func extends Funcao{
        
        @Override
        public double f(double[] x){
            double[] var = new double[QTD_VAR];
            double[] s = new double[QTD_REST_LE];
            double[] lambda = new double[QTD_REST_E];
            double[] pi = new double[QTD_REST_LE];
            
            x_to_vars(x,var,s,lambda,pi);
       
            return funcao_Primal_Dual_Barreira_Logaritmica(funcao_Principal,restricoes_LessEqual,QTD_REST_LE,restricoes_Equal,QTD_REST_E,var,QTD_VAR,s,QTD_REST_LE,lambda,QTD_REST_E,pi,QTD_REST_LE,mi);
        }
    }    
    
    double norma_Vet(double[] x){           //sqrt(soma(x_i))
        double soma = 0;
        for(int i=0; i < x.length; i++)
            soma += x[i]*x[i];
        return Math.sqrt(soma);
    }
    
    void mult_Vet(double[] x, int c){       // x = x*c;
        for(int i=0; i < x.length; i++)
            x[i] *= c;
    }

    void soma_Vet(double[] v1,double[] v2){ // v1 = v1 + v2;
        for(int i=0;i< v1.length;i++)
            v1[i] += v2[i];
    }
    
    void print_vet( double[] v){
        for(int i = 0; i < v.length; i++)
            System.out.printf("%.6f "+endln,v[i]);
        System.out.println();
    }
    
    void print_mat(double[][] mat,int n, int m){
        for(int i = 0; i < n; i++){
            for(int j = 0; j < m; j++)
                System.out.printf("%.6f ",mat[i][j]);
            System.out.println();
        }
        System.out.println();
    }
    
    boolean verifica_KKT(Funcao funcao,Funcao[] restricoes_LE,int num_RestLE,
                      Funcao[] restricoes_E,int num_RestE, double[] x, int nx,double[] s, int ns,
                      double[] lambda, int nlambda, double[] pi, int npi, double mi, double epsilon){
        boolean[] rest_LE_Ativa = new boolean[num_RestLE];
        boolean viavel = true;
        double result;
        double[] soma = new double[nx];

        for(int i=0; i < num_RestLE && viavel; i++){
            result = (restricoes_LE[i]).f(x);
            if(result > epsilon)
                viavel = false;
            rest_LE_Ativa[i] = Math.abs(result) < epsilon;
        }

        for(int i=0; i < num_RestE && viavel; i++){
            result = (restricoes_E[i]).f(x);
            if(Math.abs(result) > epsilon)
                viavel = false;
        }

        return viavel;
    }
    
    boolean tem_nan(double[] x){
        boolean test=false;
        Double valor;
        for(int i = 0; i < x.length && test == false; i++){
            valor = x[i];
            if(Double.isNaN(valor))
                test = true;
        }
        return test;
    }
    
    public OtimizaBiodigestor(){
        set_funcao_Principal();
        set_restricoes_LessEqual();
        set_restricoes_Equal();
    }    
        
    public double[] Executa_Otimizacao(){
        double[] var = new double[] {4,5};//QTD_VAR
        double[] s = new double[] {0.5,0.5,0.5,0.5,0.5};//QTD_REST_LE
        double[] lambda = new double[QTD_REST_E];
        double[] pi = new double[] {0.5,0.5,0.5,0.5,0.5};//QTD_REST_LE
        double beta = 10;
        double alphap,alphad;
        double menor;
        double epsilon = 0.0001;
        func funcao = new func();
        funcao_Biodigestor_Indiano funcao_Biodigestor = new funcao_Biodigestor_Indiano();
        long k,j;
        
        
        double[][] Hessiana = new double[total_variaveis][total_variaveis];
        double[] grad = new double[total_variaveis];
        double[] d = new double[total_variaveis];
        double[] x = new double[total_variaveis];
        
        for(k = 0; k < 10000000; k++){

            if(prints)
                System.out.println("k: " + k);

            gera_vetor_x(x,var,s,lambda,pi);
            j=0;
            do{
                calcula_Gradiente(funcao,x,total_variaveis,grad,0.00000001);
                calcula_Hessiana(funcao,x,total_variaveis,Hessiana,0.00000001);
                if(prints){
                    System.out.println("j: "+j++);
                    System.out.println("x: ");
                    print_vet(x);
                    System.out.println(endln+"grad: ");
                    print_vet(grad);
                }
                mult_Vet(grad,-1);
                if(prints){
                    System.out.println(endln+"grad*(-1): ");
                    print_vet(grad);
                    System.out.println(endln+"Hessiana: ");
                    print_mat(Hessiana,total_variaveis,total_variaveis);
                }
                gaussPivoParcialSemTrocas(total_variaveis,Hessiana,grad,d);
                if(prints){
                    System.out.println(endln+"d: ");
                    print_vet(d);
                }
               
                //atualizando alphap
                menor = 1;
                //s vai de x[2] até x[6]
                for(int i = QTD_VAR; i < QTD_VAR+QTD_REST_LE; i++)
                    if(d[i] < 0)
                        menor = ((x[i]/Math.abs(d[i]))<menor)?(x[i]/Math.abs(d[i])):(menor);

                alphap = 0.95*menor;
                //else
                //    alphap = 0;
                //atualizando alphad
                menor = 1;
                //pi vai de x[7] até x[11]
                for(int i = total_variaveis-QTD_REST_LE; i < total_variaveis; i++)
                    if(d[i] < 0)
                        menor = ((x[i]/Math.abs(d[i]))<menor)?(x[i]/Math.abs(d[i])):(menor);
                alphad = 0.95*menor;
                //else
                //    alphad = 0;
                //Atualiza o x

                for(int i=0; i < total_variaveis; i++)
                    x[i] += (i<QTD_VAR+QTD_REST_LE)?(alphap*d[i]):(alphad*d[i]);

                for(int i=QTD_VAR; i < total_variaveis; i++)
                    x[i] = (x[i]<epsilon)?(epsilon):(x[i]);
                    //x[i] = (x[i]<epsilon)?(epsilon):(x[i]);
                
                //System.out.println("teste");
                if(tem_nan(x)){
                    //System.out.println("deuruim");
                    gera_vetor_x(x,var,s,lambda,pi);
                    //javax.swing.JOptionPane.showMessageDialog(null,"teste","teste",javax.swing.JOptionPane.ERROR_MESSAGE);
                    break;
                    
                }
                    
                calcula_Gradiente(funcao,x,total_variaveis,grad,0.00000001);
                
                if(tem_nan(grad)){
                    gera_vetor_x(x,var,s,lambda,pi);
                    break;
                }
                
                if(prints){
                    System.out.println(endln + "novo x: ");
                    print_vet(x);
                    System.out.println(endln + "novo grad: ");
                    print_vet(grad);
                }
                //System.out.println("norma grad: "+norma_Vet(grad));
                //System.out.println("epsilon: "+epsilon);
                //System.out.println("teste: "+(norma_Vet(grad) > epsilon));
            }while(norma_Vet(grad) > epsilon);

            x_to_vars(x,var,s,lambda,pi);
            
            //java.util.Random gerador = new java.util.Random();
            //int numero = gerador.nextInt(10)+1;
            if(norma_Vet(grad) < epsilon){
                if(verifica_KKT(funcao_Biodigestor,restricoes_LessEqual,QTD_REST_LE,restricoes_Equal,QTD_REST_E,var,QTD_VAR,s,QTD_REST_LE,lambda,QTD_REST_E,pi,QTD_REST_LE,mi,epsilon))
                    break;
            }
            else
                mi /= beta; //heurística
            if(mi < 0.000000001)
                mi = (mi * 2) * 10000000;
        }
        if(prints){    
            System.out.println(endln+"result at k: "+k+endln+"with mi = "+mi+endln+"x* = ");
            print_vet(var);
            System.out.println(endln+"f(x*) = "+funcao_Biodigestor.f(x));
        }
        return var;
    }

}
