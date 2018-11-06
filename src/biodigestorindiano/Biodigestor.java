package biodigestorindiano;

/**
 *
 * @author jota
 */
public abstract class Biodigestor {
    
    class Parametro{
        double valor;
        String rotulo, formato, unidade;
        
        public Parametro(){
            setProps(0, "", "", "");
        }
        
        public Parametro(double valor, String rotulo, String formato, String unidade){
            setProps(valor, rotulo, formato, unidade);
        }
        
        private void setProps(double valor, String rotulo, String formato, String unidade){
            this.valor = valor;
            this.rotulo = rotulo;
            this.formato = formato;
            this.unidade = unidade;
        }
        
        double getValor(){
            return valor;
        }
        
        String getRotulo(){
            return rotulo;
        }
        
        String getValorFormatado(){
            return String.format(formato, valor) + " " + unidade;
        }
        
        public void setValor(double valor){
            this.valor = valor;
        }
    }
    
    Funcao objetivo;
    Funcao[] restMI, restI;
    protected double Vb, freq, pressaoMax, Vug;
    
    public final void setVb(double Vb){
        this.Vb = (Vb >= 0) ? Vb : 0;
    }
    
    public final void setFreq(double freq){
        this.freq = (freq >= 1) ? freq : 1;
    }
    
    public final void setPressaoMax(double pressaoMax){
        this.pressaoMax = (pressaoMax >= 0) ? pressaoMax : 0;
    }
    
    public final void setVug(double Vug){
        this.Vug = (Vug >= 0) ? Vug : 0;
    }
    
    double[] varIni, sIni, lambdaIni, piIni;
    
    public double[] getVarIni(){
        return varIni;
    }
    
    public double[] getSIni(){
        return sIni;
    }
    
    public double[] getLambdaIni(){
        return lambdaIni;
    }
    
    public double[] getPiIni(){
        return piIni;
    }
    
    Parametro[] params;
    String nomeImagem;
    
    String getNomeImagem(){
        return nomeImagem;
    }
    
    abstract void inicializaFuncoes();
    abstract void inicializaParametros();
    abstract void determinaSolucao(double[] solucao);
    
    abstract String getNome();
}
