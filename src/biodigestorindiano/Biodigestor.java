package biodigestorindiano;

/**
 *
 * @author jota
 */
public abstract class Biodigestor {
    
    class Parametro{
        double valor;
        String rotulo, formato, unidade;
        
        String getRotulo(){
            return rotulo;
        }
        
        String getValorFormtado(){
            return String.format(formato, valor) + " " + unidade;
        }
    }
    
    Funcao objteivo;
    Funcao[] restMI, restI;
    
    double[] var;
    
    Parametro[] param;
    String nomeImagem;
}
