package biodigestorindiano;
/**
 *
 * @author WELL1NGTON
 */
public class OtimizaBiodigestor {
    
    private static Biodigestor biodig = null;
    private static double  mi = 0.0001;
    private static int qtdVar, qtdRestMI, qtdRestI, totalVariaveis;
    
    public static void setBiodigesotor(Biodigestor modelo){
        biodig = modelo;
        qtdVar = biodig.varIni.length;
        qtdRestMI = biodig.restMI.length;
        qtdRestI = biodig.restI.length;
        totalVariaveis = qtdVar + qtdRestMI * 2 + qtdRestI;
    }
    
    public static void setMi(double valor){
        mi = valor;
    }
    
    public static void gaussPivoParcialSemTrocas(double[][] m, double[] b, double[] x){
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
    
    public static void calculamLU(double[][] m, double[][] mLU){
        int i, j, k, n = m.length;
        double soma;
        
        //Calcula a matriz LU (compacta)
        for( i = 0; i < n; i++)
        {
            for( j = i; j < n; j++)
            {
                soma = 0;

                for( k = 0; k < i; k++)
                    soma += mLU[i][k] * mLU[k][j];

                mLU[i][j] = m[i][j] - soma;
            }

            for( j = i+1; j < n; j++)
            {
                soma = 0;
            
                for( k = 0; k < i; k++)
                    soma += mLU[j][k] * mLU[k][i];
            
                if(mLU[i][i] == 0)
                    mLU[i][i] +=  0.0001;
                   
                mLU[j][i] = (m[j][i] - soma) / mLU[i][i];
            }
        }
    }
     
    public static void resolveLU(double[][] mLU, double[] b, double[] x){
        int i, j, n = mLU.length;
        double soma;
        double[] y = new double[n];
        
        //Solução de L.y = b
        for( i = 0; i < n; i++)
        {
            soma = 0;
          
            for( j = 0; j < i; j++)
                soma += mLU[i][j] * y[j];
          
            y[i] = b[i] - soma;
        }
        
        //Solução de U.x = y  
        for( i = n - 1; i >= 0; i--)
        {
            soma = 0;
            
            for( j = i + 1; j < n; j++)
                soma += mLU[i][j] * x[j];
            
            x[i] = (y[i] - soma) / mLU[i][i];
        }
    }
    
    public static double funcaoMaisC(Funcao func, double[] x, int pos, double c){
        double[] y = new double[x.length];
        
        System.arraycopy(x, 0, y, 0, x.length);
        y[pos] += c;
        
        return func.f(y);
    }
    
    public static double funcaoMaisC(Funcao funcao, double[] x, int pos1, int pos2, double c1, double c2){
        double[] y = new double[x.length];
        
        System.arraycopy(x, 0, y, 0, x.length);
        y[pos1] += c1;
        y[pos2] += c2;
        
        return funcao.f(y);
    }
    
    public static double derivadaPrimeira(Funcao funcao, double[] x, int i, double epsilon){
        double erro, erro_ant, h, df_ant, df;
        int maxIteracoes = 10;

        erro = 99999;
        h = 0.000001;

        df = (funcaoMaisC(funcao, x, i, h) - funcaoMaisC(funcao, x ,i , -h)) / (2 * h);

        do{
            h /= 2;
            df_ant = df;
            df = ( funcaoMaisC(funcao, x, i, h) - funcaoMaisC(funcao, x, i, -h) ) / (2 * h);
            erro_ant = erro;
            erro = Math.abs(df - df_ant) / ( (1 > Math.abs(df)) ? (1) : (Math.abs(df)) );
        }while(erro > epsilon && erro_ant > erro && --maxIteracoes != 0);

        return df;
    }
    
    public static double derivadaSegunda(Funcao funcao, double[] x, int i, int j, double epsilon){
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
    
    public static void gradiente(Funcao funcao, double[] x, double[] grad, double epsilon){
        int n = x.length, i;
        
        for(i = 0; i < n; i++)
            grad[i] = derivadaPrimeira(funcao, x, i, epsilon);
    }
    
    public static void hessiana(Funcao funcao, double[] x, double[][] hessiana, double epsilon){
        int n = x.length, i, j;
        
        for(i = 0; i < n; i++)
        {
            for(j = 0; j < i; j++)
                hessiana[i][j] = hessiana[j][i] = derivadaSegunda(funcao, x, i, j, epsilon);
            hessiana[i][i] = derivadaSegunda(funcao, x, i, i, epsilon);
        }
                
    }
    
    public static double funcaoPDBL(Funcao funcao, Funcao[] restricoesMI, Funcao[] restricoesI,
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
            soma += lambda[i]*(restricoesI[i]).f(x);
        total += soma;

        //f(x) - mi * soma(ln(S_j)) + soma(lambda_i * restricao_igualdade_i) + soma(pi_j * restricao_menor_igual_j + S_j)
        soma = 0;
        for(int j=0; j < npi; j++)
            soma += pi[j]*((restricoesMI[j]).f(x)+s[j]);
        total += soma;

        return total;
    }
    
    public static void xToVars(double[] x, double[] var, double[] s, double[] lambda, double[] pi){
        System.arraycopy(x, 0, var, 0, qtdVar);
        System.arraycopy(x, qtdVar, s, 0, qtdRestMI);
        System.arraycopy(x, qtdVar + qtdRestMI, lambda, 0, qtdRestI);
        System.arraycopy(x, qtdVar + qtdRestMI + qtdRestI, pi, 0, qtdRestMI);
    }
    
    public static void geraVetorX(double[] x, double[] var, double[] s, double[] lambda, double[] pi){
        System.arraycopy(var, 0, x, 0, qtdVar);
        System.arraycopy(s, 0, x, qtdVar, qtdRestMI);
        System.arraycopy(lambda, 0, x, qtdVar + qtdRestMI, qtdRestI);
        System.arraycopy(pi, 0, x, qtdVar + qtdRestMI + qtdRestI, qtdRestMI);
    }
    
    static class func implements Funcao{
        
        @Override
        public double f(double[] x){
            double[] var = new double[qtdVar];
            double[] s = new double[qtdRestMI];
            double[] lambda = new double[qtdRestI];
            double[] pi = new double[qtdRestMI];
            
            xToVars(x, var, s, lambda, pi);
       
            return funcaoPDBL(biodig.objetivo, biodig.restMI, biodig.restI, var, s, lambda, pi);
        }
    }    
    
    static double normaVet(double[] x){           //sqrt(soma(x_i))
        double soma = 0;
        for(int i=0; i < x.length; i++)
            soma += x[i] * x[i];
        return Math.sqrt(soma);
    }
    
    static void multVet(double[] x, int c){       // x = x*c;
        for(int i=0; i < x.length; i++)
            x[i] *= c;
    }

    static void somaVet(double[] v1,double[] v2){ // v1 = v1 + v2;
        for(int i = 0;i < v1.length; i++)
            v1[i] += v2[i];
    }
    
    static boolean verificaKKT(Funcao funcao,Funcao[] restricoes_LE, Funcao[] restricoes_E,
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
    
    static boolean temNan(double[] x){
        boolean test = false;
        double valor;
        
        for(int i = 0; i < x.length && test == false; i++){
            valor = x[i];
            if(Double.isNaN(valor))
                test = true;
        }
        
        return test;
    }   
        
    public static void executaOtimizacao(){
        if(biodig == null)
            return;
        
        double[] var = biodig.getVarIni();
        double[] s = biodig.getSIni();
        double[] lambda = biodig.getLambdaIni();
        double[] pi = biodig.getPiIni();
        double beta = 10;
        double alphap, alphad;
        double menor;
        double epsilon = 0.0001;
        func funcao = new func();
        
        double[][] Hessiana = new double[totalVariaveis][totalVariaveis], LU = new double[totalVariaveis][totalVariaveis];
        double[] grad = new double[totalVariaveis];
        double[] d = new double[totalVariaveis];
        double[] x = new double[totalVariaveis];
        
        final int maxRep = 7;
        int rep;
        
        //long inicio = System.currentTimeMillis();
        
        for(long k = 0; k < 10000000; k++){
            geraVetorX(x, var, s, lambda, pi);
            gradiente(funcao, x, grad, 0.00000001);
            
            rep = maxRep;
            
            do{
                rep++;
                
                if( rep > maxRep )
                {
                    rep = 1;
                    hessiana(funcao, x, Hessiana, 0.00000001);
                    calculamLU(Hessiana, LU);
                }
                
                multVet(grad, -1);
                resolveLU(LU, grad, d);
                
                //hessiana(funcao, x, Hessiana, 0.00000001);
                //gaussPivoParcialSemTrocas(Hessiana, grad, d);
               
                //atualizando alphap
                menor = 1;
                
                //s vai de x[2] até x[6]
                for(int i = qtdVar; i < qtdVar + qtdRestMI; i++)
                    if(d[i] < 0)
                        menor = ((x[i] / Math.abs(d[i])) < menor) ? (x[i] / Math.abs(d[i])) : (menor);

                alphap = 0.95 * menor;

                //atualizando alphad
                menor = 1;
                
                //pi vai de x[7] até x[11]
                for(int i = totalVariaveis - qtdRestMI; i < totalVariaveis; i++)
                    if(d[i] < 0)
                        menor = ((x[i]/Math.abs(d[i]))<menor)?(x[i]/Math.abs(d[i])):(menor);
                alphad = 0.95 * menor;

                //Atualiza o x
                for(int i = 0; i < totalVariaveis; i++)
                    x[i] += (i < qtdVar + qtdRestMI) ? (alphap * d[i]) : (alphad * d[i]);

                for(int i = qtdVar; i < totalVariaveis; i++)
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
                if( verificaKKT(biodig.objetivo, biodig.restMI, biodig.restI, var, epsilon) )
                    break;
            }
            else
                mi /= beta; //heurística
            
            if(mi < 0.000000001)
                mi = (mi * 2) * 10000000;
        }
        
        //long fim = System.currentTimeMillis();
        //System.out.println(fim - inicio);

        biodig.determinaSolucao(var);
    }

}
