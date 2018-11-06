package biodigestorindiano;

/**
 *
 * @author jota
 */
public class Chines extends Biodigestor {
    
    final int QTD_REST_MI = 4, QTD_REST_I = 0, QTD_PARAM = 17;
    final double VB_PADRAO = 25, FREQ_PADRAO = 50, PRESS_PADRAO = 0.15, VUG_PADRAO = 4.06;
    final double P1 = (Math.PI) / 4, P2 = (49 * (Math.PI)) / 3072;
    
    //Função principal a ser minimizada
    class FuncaoObjetivo implements Funcao{
        
        @Override
        public double f(double[] x){
            return P1 * x[0] * x[0] * x[1] + P2 * x[0] * x[0] * x[0];
        }
    }
    
    //Restrições do tipo R(x) <= 0
    class RestricaoConsumo implements Funcao{
        
        @Override
        public double f(double[] x){
            return Vb - (P1 * x[0] * x[0] * x[1] + P2 * x[0] * x[0] * x[0]);
        }
    }
    
    class RestricaoRelacaoDH1 implements Funcao{
        
        @Override
        public double f(double[] x){
            return 0.5 * x[0] - x[1];
        }
    }
    
    class RestricaoRelacaoDH2 implements Funcao{
        
        @Override
        public double f(double[] x){
            return  -0.6 * x[0] + x[1];
        }
    }
    
    class RestricaoDiametroMinimo implements Funcao{
        
        @Override
        public double f(double[] x){
            return  -x[0];
        }
    }
    
    public Chines(){
        inicializaFuncoes();
        inicializaParametros();
        this.Vb         = VB_PADRAO;
        this.freq       = FREQ_PADRAO;
        this.pressaoMax = PRESS_PADRAO;
        this.Vug        = VUG_PADRAO;
    }
    
    public Chines(double Vb, double freq, double pressaoMax, double Vug){
        inicializaFuncoes();
        inicializaParametros();
        setVb(Vb);
        setFreq(freq);
        setPressaoMax(pressaoMax);
        setVug(Vug);
    }
    
    @Override
    final void inicializaFuncoes(){
        varIni = new double[] {4, 5};
        sIni = new double[] {0.5, 0.5, 0.5, 0.5};
        lambdaIni  = new double[] {};
        piIni = new double[] {0.5, 0.5, 0.5, 0.5};
        
        objetivo = new FuncaoObjetivo();
        
        restMI = new Funcao[QTD_REST_MI];
        
        restMI[0] = new RestricaoConsumo();
        restMI[1] = new RestricaoRelacaoDH1();
        restMI[2] = new RestricaoRelacaoDH2();
        restMI[3] = new RestricaoDiametroMinimo();
        
        restI = new Funcao[QTD_REST_I];
    }
    
    @Override
    final void inicializaParametros(){
        params = new Parametro[QTD_PARAM];
        String[] rotulos  = new String[] {"Diâmetro do biodigestor (D):", "Altura do nível do substrato (H):", "Altura da calota do fundo (hf):", "Volume da calota do fundo (Vf):", "Raio da calota do fundo (Rf):", 
                                          "Altura da calota do gasômetro (hg):", "Volume da calota do gasômetro (Vg):", "Raio da calota do gasômetro (Rg):", "Altura da caixa de saída (hs):", "Diâmetro da caixa de saída (Ds):",
                                          "Altura da caixa de entrada (he):", "Diametro da caixa de entrada (De):", "Volume de reabastecimento diário (v):", "Volume útil (Vd):", "Volume do corpo cilíndrico (Vc):", 
                                          "Pressão máxima de armazenamento do biogás (Pmax):", "Volume de biogás armazenado na pressão máxima (Vb):"},
                 formatos = new String[] {"%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f",    "%.4f", "%.4f"},
                 unidades = new String[] {   "m",    "m",    "m",   "m³",    "m",    "m",   "m³",    "m",    "m",    "m",    "m",    "m",   "m³",   "m³",   "m³", "kgf/cm²",   "m³"};
        
        for(int i = 0; i < QTD_PARAM; i++)
            params[i] = new Parametro(0, rotulos[i], formatos[i], unidades[i]);
    }
    
    @Override
    void determinaSolucao(double[] solucao){        
        double D, H, hf, Vf, Rf, hg, Vg, Rg, hs, Ds, he, De, v, Vd, Vc, Pmax, Vpmax, a = 0.2; //tomando o afundamento "a" como sendo 0.2 
        
        D = solucao[0];
        H = solucao[1];
        Vd = P1 * D * D * H + P2 * D * D * D;
        
        //Calota do fundo
        hf = D / 8;
        Vf = (Math.PI * hf / 6) * (3 * D * D / 4 + hf * hf);
        Rf = (D * D / 4 + hf * hf) / (2 * hf);
        
        //Calota do gasômetro
        hg = D / 4;
        Vg = (Math.PI * hg / 6) * (3 * D * D / 4 + hg * hg);
        Rg = (D * D / 4 + hg * hg) / (2 * hg);
        
        //Caixa de saída
        hs = hg + a + 0.2; 
        Ds = (D * D * H) / (3 * (hs - 0.1));
        Ds = Math.sqrt( (Ds > 0) ? Ds : 0 );
        
        //Caixa de entrada
        he = 0.5;
        v = Vb / freq;
        De = (4 * v) / (Math.PI * (he - 0.1));
        De = Math.sqrt( (De > 0) ? De : 0 );
        
        Vc = P1 * D * D * H;
        Pmax = H / 3 + (hs - 0.1);
        Pmax /= 10; //m.c.a -> kgf/cm²
        Vpmax = ( (P1 * D * D) * (H / 3) ) + Vg + (P1 * 0.36 * (a + 0.2) );
        
        //Determinação do valor dos parâmetros
        params[0].setValor(D);
        params[1].setValor(H);
        params[2].setValor(hf);
        params[3].setValor(Vf);
        params[4].setValor(Rf);
        params[5].setValor(hg);
        params[6].setValor(Vg);
        params[7].setValor(Rg);
        params[8].setValor(hs);
        params[9].setValor(Ds);
        params[10].setValor(he);
        params[11].setValor(De);
        params[12].setValor(v);
        params[13].setValor(Vd);
        params[14].setValor(Vc);
        params[15].setValor(Pmax);
        params[16].setValor(Vpmax);
        
        nomeImagem = "chines.png";
    }
    
    @Override
    final String getNome()
    {
        return "chinês";
    }
}
