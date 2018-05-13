package biodigestorindiano;
/**
 *
 * @author WELL1NGTON
 */
public class OtimizaBiodigestor {
    
    private double Vb = 25, mi = 0.01;
    private final int QTD_VAR = 2, QTD_REST_MI = 5, QTD_REST_I = 0,
                      total_variaveis = QTD_VAR + QTD_REST_MI * 2 + QTD_REST_I;
    
    private Funcao funcaoPrincipal = null;
    private Funcao[] restricoesMenorIgual = null, restricoesIgual = null;
    
    public OtimizaBiodigestor(){
        funcaoPrincipal = new FuncaoBiodigestorIndiano();
        
        restricoesMenorIgual = new Funcao[QTD_REST_MI];
        
        restricoesMenorIgual[0] = new restricao1_LE();
        restricoesMenorIgual[1] = new restricao2_LE();
        restricoesMenorIgual[2] = new restricao3_LE();
        restricoesMenorIgual[3] = new restricao4_LE();
        restricoesMenorIgual[4] = new restricao5_LE();
        
        restricoesIgual = new Funcao[QTD_REST_I];
    } 
    
    public void setVb(double valor){
        Vb = valor;
    }
    
    public void setMi(double valor){
        mi = valor;
    }
    
    //Função principal a ser minimizada
    class FuncaoBiodigestorIndiano implements Funcao{
        
        @Override
        public double f(double[] x){
            return (Math.PI) * x[0] * x[0] * x[1] / 4;
        }
    }
    
    //Restrições do tipo R(x) <= 0
    class restricao1_LE implements Funcao{
        
        @Override
        public double f(double[] x){
            return Vb - ((Math.PI) * x[0] * x[0] * x[1]) / 4;
        }
    }
    
    class restricao2_LE implements Funcao{
        
        @Override
        public double f(double[] x){
            return x[0] - x[1];
        }
    }
    
    class restricao3_LE implements Funcao{
        
        @Override
        public double f(double[] x){
            return  0.6 * x[1] - x[0];
        }
    }
    
    class restricao4_LE implements Funcao{
        
        @Override
        public double f(double[] x){
            return  x[1] - 6;
        }
    }
    
    class restricao5_LE implements Funcao{
        
        @Override
        public double f(double[] x){
            return  3 - x[1];
        }
    }
    
    public void gaussPivoParcialSemTrocas(double[][] m, double[] b, double[] x){
        int[] p = new int[x.length];
        int i, j, k, aux, maior, n = x.length;
        double d, soma;

        for(i = 0; i < n; i++)
            p[i] = i;
        
        for(k = 0;k < n-1; k++){
            maior = k;
            for(i = k + 1; i < n; i++)
                if( Math.abs( m[p[i]][k] ) > Math.abs( m[p[maior]][k] ) )
                    maior = i;
            aux = p[k];
            p[k] = p[maior];
            p[maior] = aux;
            
            for(i = k + 1; i < n; i++){
                d = m[p[i]][k] / m[p[k]][k];
                m[p[i]][k] = 0;
                
                for(j = k + 1; j < n; j++)
                    m[p[i]][j] = m[p[i]][j] - d * m[p[k]][j];
                b[p[i]] = b[p[i]] - d * b[p[k]];
            }
        }

        x[n - 1] = b[p[n - 1]] / m[p[n - 1]][n - 1];
        
        for(i = n - 2; i >= 0; i--){
            soma = 0;
            
            for(j = i + 1; j < n; j++)
                soma += m[p[i]][j] * x[j];
            x[i] = (b[p[i]] - soma) / m[p[i]][i];
        }
    }
    
    public double funcaoMaisC(Funcao func, double[] x, int pos, double c){
        double[] y = new double[x.length];
        
        System.arraycopy(x, 0, y, 0, x.length);
        y[pos] += c;
        
        return func.f(y);
    }
    
    public double funcaoMaisC(Funcao funcao, double[] x, int pos1, int pos2, double c1, double c2){
        double[] y = new double[x.length];
        
        System.arraycopy(x, 0, y, 0, x.length);
        y[pos1] += c1;
        y[pos2] += c2;
        
        return funcao.f(y);
    }
    
    public double derivadaPrimeira(Funcao funcao, double[] x, int i, double epsilon){
        double erro, erro_ant, h, df_ant, df;
        int iteracoes_max = 10;

        erro = 99999;
        h = 0.000001;

        df = (funcaoMaisC(funcao, x, i, h) - funcaoMaisC(funcao, x ,i , -h)) / (2 * h);

        do{
            h /= 2;
            df_ant = df;
            df = ( funcaoMaisC(funcao, x, i, h) - funcaoMaisC(funcao, x, i, -h) ) / (2 * h);
            erro_ant = erro;
            erro = Math.abs(df - df_ant) / ( (1 > Math.abs(df)) ? (1) : (Math.abs(df)) );
        }while(erro > epsilon && erro_ant > erro && --iteracoes_max != 0);

        return df;
    }
    
    public double derivadaSegunda(Funcao funcao, double[] x, int i, int j, double epsilon){
        double erro, erro_ant, h, df_ant, df;
        int iteracoes_max = 10;


        erro = 99999;
        h = 0.000001;
        df = (i==j) ? ( (funcaoMaisC(funcao, x, i, 2 * h) - 2 * funcao.f(x) + funcaoMaisC(funcao, x, i, -2 * h)) / (4 * h * h) ):
                      ( (funcaoMaisC(funcao, x, i, j, h, h) - funcaoMaisC(funcao, x, i, j, h, -h) - funcaoMaisC(funcao, x, i, j, -h, h) + funcaoMaisC(funcao, x, i, j, -h, -h)) / (4 * h * h) );
        do{
            h /= 2;
            df_ant = df;
            df = (i==j) ? ( (funcaoMaisC(funcao, x, j, 2 * h) - 2 * funcao.f(x) + funcaoMaisC(funcao, x, j, -2 * h)) / (4 * h * h) ):
                          ( (funcaoMaisC(funcao, x, i, j, h, h) - funcaoMaisC(funcao, x, i, j, h, -h) - funcaoMaisC(funcao, x, i, j, -h, h) + funcaoMaisC(funcao, x, i, j, -h, -h)) / (4 * h * h) );
            erro_ant = erro;
            erro = Math.abs(df-df_ant) / ( (1 > Math.abs(df)) ? (1) : (Math.abs(df)));
        }while(erro > epsilon && erro_ant > erro && --iteracoes_max != 0);

        return df;
    }
    
    public void gradiente(Funcao funcao, double[] x, double[] grad, double epsilon){
        int n = x.length, i;
        
        for(i = 0; i < n; i++)
            grad[i] = derivadaPrimeira(funcao, x, i, epsilon);
    }
    
    public void hessiana(Funcao funcao, double[] x, double[][] hessiana, double epsilon){
        int n = x.length, i, j;
        
        for(i = 0; i < n; i++)
        {
            for(j = 0; j < i; j++)
                hessiana[i][j] = hessiana[j][i] = derivadaSegunda(funcao, x, i, j, epsilon);
            hessiana[i][i] = derivadaSegunda(funcao, x, i, i, epsilon);
        }
                
    }
    
    public double funcaoPDBL(Funcao funcao, Funcao[] restricoes_LE, Funcao[] restricoes_E,
                             double[] x, double[] s, double[] lambda, double[] pi){
        double soma;
        double total;
        final int ns = s.length, nlambda = lambda.length, npi = pi.length;
        
        //f(x)
        total = funcao.f(x);

        //f(x) - mi * soma(ln(S_j))
        soma = 0;
        for(int j = 0; j < ns; j++)
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
    
    public void xToVars(double[] x, double[] var, double[] s, double[] lambda, double[] pi){
        System.arraycopy(x, 0, var, 0, QTD_VAR);
        System.arraycopy(x, QTD_VAR, s, 0, QTD_REST_MI);
        System.arraycopy(x, QTD_VAR + QTD_REST_MI, lambda, 0, QTD_REST_I);
        System.arraycopy(x, QTD_VAR + QTD_REST_MI + QTD_REST_I, pi, 0, QTD_REST_MI);
    }
    
    public void geraVetorX(double[] x, double[] var, double[] s, double[] lambda, double[] pi){
        System.arraycopy(var, 0, x, 0, QTD_VAR);
        System.arraycopy(s, 0, x, QTD_VAR, QTD_REST_MI);
        System.arraycopy(lambda, 0, x, QTD_VAR + QTD_REST_MI, QTD_REST_I);
        System.arraycopy(pi, 0, x, QTD_VAR + QTD_REST_MI + QTD_REST_I, QTD_REST_MI);
    }
    
    class func implements Funcao{
        
        @Override
        public double f(double[] x){
            double[] var = new double[QTD_VAR];
            double[] s = new double[QTD_REST_MI];
            double[] lambda = new double[QTD_REST_I];
            double[] pi = new double[QTD_REST_MI];
            
            xToVars(x, var, s, lambda, pi);
       
            return funcaoPDBL(funcaoPrincipal, restricoesMenorIgual, restricoesIgual, var, s, lambda, pi);
        }
    }    
    
    double normaVet(double[] x){           //sqrt(soma(x_i))
        double soma = 0;
        for(int i=0; i < x.length; i++)
            soma += x[i] * x[i];
        return Math.sqrt(soma);
    }
    
    void multVet(double[] x, int c){       // x = x*c;
        for(int i=0; i < x.length; i++)
            x[i] *= c;
    }

    void somaVet(double[] v1,double[] v2){ // v1 = v1 + v2;
        for(int i = 0;i < v1.length; i++)
            v1[i] += v2[i];
    }
    
    boolean verificaKKT(Funcao funcao,Funcao[] restricoes_LE, Funcao[] restricoes_E,
                        double[] x, double epsilon){
        boolean viavel = true;
        double result;

        for(int i=0; i < restricoes_LE.length && viavel; i++){
            result = (restricoes_LE[i]).f(x);
            if(result > epsilon)
                viavel = false;
        }

        for(int i=0; i < restricoes_E.length && viavel; i++){
            result = (restricoes_E[i]).f(x);
            if(Math.abs(result) > epsilon)
                viavel = false;
        }

        return viavel;
    }
    
    boolean temNan(double[] x){
        boolean test = false;
        double valor;
        
        for(int i = 0; i < x.length && test == false; i++){
            valor = x[i];
            if(Double.isNaN(valor))
                test = true;
        }
        
        return test;
    }   
        
    public double[] executaOtimizacao(){
        double[] var = new double[] {4,5};//QTD_VAR
        double[] s = new double[] {0.5,0.5,0.5,0.5,0.5};//QTD_REST_LE
        double[] lambda = new double[QTD_REST_I];
        double[] pi = new double[] {0.5,0.5,0.5,0.5,0.5};//QTD_REST_LE
        double beta = 10;
        double alphap, alphad;
        double menor;
        double epsilon = 0.0001;
        func funcao = new func();
        FuncaoBiodigestorIndiano funcaoBiodigestor = new FuncaoBiodigestorIndiano();  
        
        double[][] Hessiana = new double[total_variaveis][total_variaveis];
        double[] grad = new double[total_variaveis];
        double[] d = new double[total_variaveis];
        double[] x = new double[total_variaveis];
        
        for(long k = 0; k < 10000000; k++){
            geraVetorX(x, var, s, lambda, pi);
            do{
                gradiente(funcao, x, grad, 0.00000001);
                hessiana(funcao, x, Hessiana, 0.00000001);
                multVet(grad, -1);
                gaussPivoParcialSemTrocas(Hessiana, grad, d);
               
                //atualizando alphap
                menor = 1;
                
                //s vai de x[2] até x[6]
                for(int i = QTD_VAR; i < QTD_VAR + QTD_REST_MI; i++)
                    if(d[i] < 0)
                        menor = ((x[i] / Math.abs(d[i])) < menor) ? (x[i] / Math.abs(d[i])) : (menor);

                alphap = 0.95 * menor;

                //atualizando alphad
                menor = 1;
                
                //pi vai de x[7] até x[11]
                for(int i = total_variaveis - QTD_REST_MI; i < total_variaveis; i++)
                    if(d[i] < 0)
                        menor = ((x[i]/Math.abs(d[i]))<menor)?(x[i]/Math.abs(d[i])):(menor);
                alphad = 0.95 * menor;

                //Atualiza o x
                for(int i = 0; i < total_variaveis; i++)
                    x[i] += (i < QTD_VAR + QTD_REST_MI) ? (alphap * d[i]) : (alphad * d[i]);

                for(int i = QTD_VAR; i < total_variaveis; i++)
                    x[i] = (x[i] < epsilon) ? (epsilon) : (x[i]);
                
                if(temNan(x)){
                    geraVetorX(x, var, s, lambda, pi);
                    break;                
                }
                    
                gradiente(funcao, x, grad, 0.00000001);
                
                if(temNan(grad)){
                    geraVetorX(x, var, s, lambda, pi);
                    break;
                }
            }while(normaVet(grad) > epsilon);

           xToVars(x, var, s, lambda, pi);

            if(normaVet(grad) < epsilon){
                if( verificaKKT(funcaoBiodigestor, restricoesMenorIgual, restricoesIgual, var, epsilon) )
                    break;
            }
            else
                mi /= beta; //heurística
            
            if(mi < 0.000000001)
                mi = (mi * 2) * 10000000;
        }

        return var;
    }

}
