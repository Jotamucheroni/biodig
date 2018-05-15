package biodigestorindiano;

/**
 *
 * @author jota
 */
public class Indiano extends Biodigestor {
    
    final int QTD_REST_MI = 5, QTD_REST_I = 0, QTD_PARAM = 21;
    final double VB_PADRAO = 27.5, FREQ_PADRAO = 50, PRESS_PADRAO = 0.15, VUG_PADRAO = 4.06;
    final double P = (Math.PI) / 4;
    
    //Função principal a ser minimizada
    class FuncaoObjetivo implements Funcao{
        
        @Override
        public double f(double[] x){
            return P * x[0] * x[0] * x[1];
        }
    }
    
    //Restrições do tipo R(x) <= 0
    class RestricaoConsumo implements Funcao{
        
        @Override
        public double f(double[] x){
            return Vb - (P * x[0] * x[0] * x[1]);
        }
    }
    
    class RestricaoRelacaoDH1 implements Funcao{
        
        @Override
        public double f(double[] x){
            return x[0] - x[1];
        }
    }
    
    class RestricaoRelacaoDH2 implements Funcao{
        
        @Override
        public double f(double[] x){
            return  0.6 * x[1] - x[0];
        }
    }
    
    class RestricaoAlturaMaxima implements Funcao{
        
        @Override
        public double f(double[] x){
            return  x[1] - 6;
        }
    }
    
    class RestricaoAlturaMinima implements Funcao{
        
        @Override
        public double f(double[] x){
            return  3 - x[1];
        }
    }
    
    public Indiano(){
        inicializaFuncoes();
        inicializaParametros();
        this.Vb         = VB_PADRAO;
        this.freq       = FREQ_PADRAO;
        this.pressaoMax = PRESS_PADRAO;
        this.Vug        = VUG_PADRAO;
    }
    
    public Indiano(double Vb, double freq, double pressaoMax, double Vug){
        inicializaFuncoes();
        inicializaParametros();
        setVb(Vb * 1.1);
        setFreq(freq);
        setPressaoMax(pressaoMax);
        setVug(Vug);
    }
    
    @Override
    final void inicializaFuncoes(){
        varIni = new double[] {4, 5};
        sIni = new double[] {0.5, 0.5, 0.5, 0.5, 0.5};
        lambdaIni  = new double[] {};
        piIni = new double[] {0.5,0.5,0.5,0.5,0.5};
        
        objetivo = new FuncaoObjetivo();
        
        restMI = new Funcao[QTD_REST_MI];
        
        restMI[0] = new RestricaoConsumo();
        restMI[1] = new RestricaoRelacaoDH1();
        restMI[2] = new RestricaoRelacaoDH2();
        restMI[3] = new RestricaoAlturaMaxima();
        restMI[4] = new RestricaoAlturaMinima();
        
        restI = new Funcao[QTD_REST_I];
    }
    
    @Override
    final void inicializaParametros(){
        params = new Parametro[QTD_PARAM];
        String[] rotulos  = new String[] {"Diâmetro interno do biodigestor (Di):", "Altura do nível do substrato (H):", "Volume bruto do biodigestor (Vb):", "Altura da parede divisória (h):", "Volume da parede divisória (Vp):", 
                                          "Volume útil real do biodigestor (Vr):", "Altura da parede acima do nível do substrato (b):", "Diâmetro externo da parede inferior (De):", "Diâmetro da base (Db):", "Diâmetro do gasômetro (Dg):",
                                          "Altura ociosa do gasômetro (h1):", "Altura útil do gasômetro (h2):", "Volume ocioso do gasômetro (V1):", "Volume útil do gasômetro (V2):", "Volume do gasômetro (Vg):", "Espessura da parede do gasômetro (E):",
                                          "Peso do gasômetro (Pg):", "Diâmetro Interno da parede superior (Ds):", "Altura do fundo da caixa de entrada (a):", "Volume útil da caixa de entrada (v):", "Altura do posicionamento dos tubos de entrada e saída (e):"},
                 formatos = new String[] {"%.2f", "%.2f", "%.2f", "%.2f", "%.2f", "%.2f", "%.2f", "%.2f", "%.2f", "%.2f", "%.2f", "%.2f", "%.2f", "%.2f", "%.2f", "%.4f", "%.2f", "%.2f", "%.2f", "%.2f", "%.2f"},
                 unidades = new String[] {   "m",    "m",   "m³",    "m",   "m³",   "m³",    "m",    "m",    "m",    "m",    "m",    "m",   "m³",   "m³",   "m³",   "cm",  "kgf",    "m",    "m",   "m³",    "m"};
        
        for(int i = 0; i < QTD_PARAM; i++)
            params[i] = new Parametro(0, rotulos[i], formatos[i], unidades[i]);
    }
    
    @Override
    void determinaSolucao(double[] solucao){        
        double Vp, Vr, Vg, VB, V1, v, Di, H, Dg, Ds, Db, De, h, h1, h2,
               a, b, e2, p, esp, E, Pg, r, tensao = 750, n; //tensao = tensão de tração adimíssível do material da parede do gasômetro (kgf/cm²)
        
        Di = solucao[0];
        H = solucao[1];
        VB = (Math.PI) * Di * Di * H / 4;
        
        Dg = Di + 0.1; //diâmetro do gasômetro
        r = 0.5 * Dg * 100; //raio do gasometro 
        h2 = (4 * Vug) / (Math.PI * Dg * Dg);
        h2 *= 1.1; //reforço 10% para o gasometro comportar o volume de biogas
        h1 = pressaoMax; //altura ociosa
        V1 = (Math.PI * (Dg * Dg) * h1) / 4;
        Vg = V1 + Vug;
        p = pressaoMax / 10;
        Pg = (Math.PI * p *(Dg * Dg * 100 * 100)) / 4;//0.015 pressão máxima para o funcionamento normal dos aparelhos


        h = H - h2;
        esp = 0.24; //espessura de um tijolo revestido, referente à parede divisória
        Vp = h * Di * esp;
        Vr = VB - Vp;

        //Limitar-se-á a indicar estas medidas por julgarmos desnecessários maiores detalhes
        //Ortolani /\
        E = (p * r) / tensao;
        a = 0.5;
        b = 0.15;//altura da parede do biodigestor acima do nível do substrato
        e2 = 0.3;
        Ds = Dg + 0.1;
        n = freq;

        De = Di + 2 * esp; //parede de 1 tijolo e 0.24 = espessura
        Db = De + 0.2;

        v = (Vb / 1.1) / n; //v = V/n... n = dias. Nesse caso 1
        
        //Determinação do valor dos parâmetros
        params[0].setValor(Di);
        params[1].setValor(H);
        params[2].setValor(VB);
        params[3].setValor(h);
        params[4].setValor(Vp);
        params[5].setValor(Vr);
        params[6].setValor(b);
        params[7].setValor(De);
        params[8].setValor(Db);
        params[9].setValor(Dg);
        params[10].setValor(h1);
        params[11].setValor(h2);
        params[12].setValor(V1);
        params[13].setValor(Vug);
        params[14].setValor(Vg);
        params[15].setValor(E);
        params[16].setValor(Pg);
        params[17].setValor(Ds);
        params[18].setValor(a);
        params[19].setValor(v);
        params[20].setValor(e2);
    }
}
