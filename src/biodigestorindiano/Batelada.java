package biodigestorindiano;

/**
 *
 * @author jota
 */
public class Batelada extends Biodigestor{
    final int QTD_REST_MI = 5, QTD_REST_I = 0, QTD_PARAM = 16;
    final double FREQ_PADRAO = 65, PRESS_PADRAO = 0.2, VUT_PADRAO = 10, B_PADRAO = 15, 
                 PUF_PADRAO = 150, PEP_PADRAO = 105, ST_PADRAO = 85.5, STF_PADRAO = 8,
                 RENDMTO_PADRAO = 0.15;
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
            return  -x[1];
        }
    }
    
    protected double B, PBC, PUF, PEP, solidosTotais, solidosTotaisFinais, rendmto, Vut, N, A;
    
    private void calculaValoresIniciais(){
        double iniPEP = (PUF >= PEP) ? (PUF - PEP) : 0, CB, PBBio, PBB, Eu, Es, W;
        int unProdEfet = 0, diasFerm = 0;
        
        //Número de unidades na bateria (N):
        N = (int) Math.ceil( PUF / PBC);
        
        for(int i = 0; i < N; i++)
        {
            if(diasFerm >= iniPEP && diasFerm <= PUF)
                unProdEfet++;
            diasFerm += PBC;
        }
        
        CB = B * PBC;
        PBBio = rendmto / PEP;
        PBB = PBBio * PBC * unProdEfet;
        Eu = CB / PBB;
        Es = Eu * (solidosTotais / 100);
        W = Es / (solidosTotaisFinais / 100);
        A = W - Eu;
        W /= 1000;
        A /= 1000;
        
        setVb(W);
        setVug(Vut / unProdEfet);
    }
    
    public Batelada(){
        inicializaFuncoes();
        inicializaParametros();
        
        this.freq       = FREQ_PADRAO;
        this.pressaoMax = PRESS_PADRAO;
        
        this.B                      = B_PADRAO;
        this.PBC                    = FREQ_PADRAO;
        this.PUF                    = PUF_PADRAO;
        this.PEP                    = PEP_PADRAO;
        this.solidosTotais          = ST_PADRAO;
        this.solidosTotaisFinais    = STF_PADRAO;
        this.rendmto                = RENDMTO_PADRAO;
        this.Vut                    = VUT_PADRAO;
        
        calculaValoresIniciais();
    }
    
    public Batelada(double B, double PBC, double pressaoMax, double Vut, double PUF, 
                    double PEP, double solidosTotais, double solidosTotaisFinais, 
                    double rendmto){
        inicializaFuncoes();
        inicializaParametros();
        
        setFreq(PBC);
        setPressaoMax(pressaoMax);
        
        detB(B);
        detPBC(PBC);
        detPUF(PUF);
        detPEP(PEP);
        detSolidosTotais(solidosTotais);
        detSolidosTotaisFinais(solidosTotaisFinais);
        detRendimento(rendmto);
        detVut(Vut);
        
        calculaValoresIniciais();
    }
    
    double getB(){
        return B;
    }
    
    double getPBC(){
        return PBC;
    }
    
    double getPUF(){
        return PUF;
    }
    
    double getPEP(){
        return PEP;
    }
    
    double getSolidosTotais(){
        return solidosTotais;
    }
    
    double getSolidosTotaisFinais(){
        return solidosTotaisFinais;
    }
    
    double getRendimento(){
        return rendmto;
    }
    
    double getVut(){
        return Vut;
    }
    
    double getN(){
        return N;
    }
    
    double getA(){
        return A;
    }
    
    final void detB(double B){
        this.B = (B > 0) ? B : 1;
    }
    
    final void detPBC(double PBC){
        this.PBC = (PBC > 0) ? PBC : 1;
    }
    
    final void detPUF(double PUF){
        this.PUF = (PUF > 0) ? PUF : 1;
    }
    
    final void detPEP(double PEP){
        this.PEP = (PEP > 0) ? PEP : 1;
    }
    
    final void detSolidosTotais(double solidosTotais){
        this.solidosTotais = (solidosTotais > 0) ? solidosTotais : 1;
    }
    
    final void detSolidosTotaisFinais(double solidosTotaisFinais){
        this.solidosTotaisFinais = (solidosTotaisFinais > 0) ? solidosTotaisFinais : 1;
    }
    
    final void detRendimento(double rendmto){
        this.rendmto = (rendmto > 0) ? rendmto : 1;
    }
    
    final void detVut(double Vut){
        this.Vut = (Vut >= 0) ? Vut : 0;
    }
    
    void setB(double B){
        detB(B);
        calculaValoresIniciais();
    }
    
    void setPBC(double PBC){
        detPBC(PBC);
        calculaValoresIniciais();
    }
    
    void setPUF(double PUF){
        detPUF(PUF);
        calculaValoresIniciais();
    }
    
    void setPEP(double PEP){
        detPEP(PEP);
        calculaValoresIniciais();
    }
    
    void setSolidosTotais(double solidosTotais){
        detSolidosTotais(solidosTotais);
        calculaValoresIniciais();
    }
    
    void setSolidosTotaisFinais(double solidosTotaisFinais){
        detSolidosTotaisFinais(solidosTotaisFinais);
        calculaValoresIniciais();
    }
    
    void setRendimento(double rendmto){
        detRendimento(rendmto);
        calculaValoresIniciais();
    }
    
    void setVut(double Vut){
        detRendimento(Vut);
        calculaValoresIniciais();
    }
    
    @Override
    final void inicializaFuncoes(){
        varIni = new double[] {4, 5};
        sIni = new double[] {0.5, 0.5, 0.5, 0.5, 0.5};
        lambdaIni  = new double[] {};
        piIni = new double[] {0.5, 0.5, 0.5, 0.5, 0.5};
        
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
        String[] rotulos  = new String[] {"Diâmetro interno do biodigestor (Di):", "Altura do nível do substrato (H):", "Volume útil da unidade biodigestoras (Vd):", "Número de unidades da bateria (N): ", "Altura da parede acima do nível do substrato (b):", 
                                          "Altura do gasômetro acima da parede do biodigestor (c):", "Diâmetro do gasômetro (Dg):", "Altura ociosa do gasômetro (h1):", "Altura útil do gasômetro (h2):", "Altura do gasômetro(hg)", 
                                          "Altura livre para deslocamento do gasômetro (h3):", "Volume ocioso do gasômetro (V1):", "Volume útil do gasômetro (V2):", "Volume do gasômetro (Vg):", "Diâmetro Interno da parede superior (Ds):",
                                          "Água a ser adicionada ao substrato (A):"},
                 formatos = new String[] {"%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f", "%.4f"},
                 unidades = new String[] {   "m",    "m",   "m³",     "",    "m",    "m",    "m",    "m",    "m",    "m",    "m",   "m³",   "m³",   "m³",    "m",   "m³"};
        
        for(int i = 0; i < QTD_PARAM; i++)
            params[i] = new Parametro(0, rotulos[i], formatos[i], unidades[i]);
    }
    
    @Override
    void determinaSolucao(double[] solucao){        
        double Di, H, Vd, b, c, Dg, hg, h1, h2, h3, V1, V2, Vg, Ds;
        
        Di = solucao[0];
        H = solucao[1];
        Vd = (Math.PI) * Di * Di * H / 4;
        
        b = pressaoMax; //altura ociosa
        c = 0.1; //Suficiente para colocar a roldana
        Dg = Di + 0.1; //diâmetro do gasômetro
        h1 = b + c;
        h2 = (4 * Vug) / (Math.PI * Dg * Dg);
        hg = h1 + h2;
        h3 = h2 + 0.08; // h3 > h2
        V1 = (Math.PI * (Dg * Dg) * h1) / 4;
        V2 = Vug;
        Vg = V1 + V2;
        Ds = Dg + 0.1;
        
        //Determinação do valor dos parâmetros
        params[0].setValor(Di);
        params[1].setValor(H);
        params[2].setValor(Vd);
        params[3].setValor(N);
        params[4].setValor(b);
        params[5].setValor(c);
        params[6].setValor(Dg);
        params[7].setValor(h1);
        params[8].setValor(h2);
        params[9].setValor(hg);
        params[10].setValor(h3);
        params[11].setValor(V1);
        params[12].setValor(V2);
        params[13].setValor(Vg);
        params[14].setValor(Ds);
        params[15].setValor(A);
        
        nomeImagem = "batelada.png";
    }
    
    @Override
    final String getNome()
    {
        return "batelada";
    }
}
