package biodigestorindiano;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import vtk.vtkNativeLibrary;
import vtk.vtkCylinder;
import vtk.vtkSphere;
import vtk.vtkPlane;
import vtk.vtkImplicitFunction;
import vtk.vtkImplicitBoolean;
import vtk.vtkSampleFunction;
import vtk.vtkContourFilter;
import vtk.vtkNamedColors;
import vtk.vtkPanel;
import vtk.vtkActor;
import vtk.vtkPolyDataMapper;

/**
 *
 * @author jota
 */
public class Modelo3D extends JPanel{
    final class Actor{
        private vtkImplicitFunction funcao, funcaoOriginal;
        private vtkImplicitBoolean funcaoCortada;
        private boolean cortado;
        private double xMin, xMax, yMin, yMax, zMin, zMax;
        private String nomeCor;
        private vtkActor actor;
        
        private vtkSampleFunction sample;
        private vtkContourFilter surface;
        private vtkPolyDataMapper mapper;
        
        public final void setFuncaoOriginal(vtkImplicitFunction funcao)
        {
            this.funcao = this.funcaoOriginal = funcao;
            
            cortado = false;
            
            vtkPlane corte = new vtkPlane();
            corte.SetOrigin(0, 0, 0);
            corte.SetNormal(0, 0, 1);
            
            funcaoCortada = new vtkImplicitBoolean();
            funcaoCortada.SetOperationTypeToIntersection();
            funcaoCortada.AddFunction(this.funcaoOriginal);
            funcaoCortada.AddFunction(corte);
        }
        
        public final void setMinMax(double[] vetMinMax)
        {
            xMin = vetMinMax[0];
            xMax = vetMinMax[1];
            yMin = vetMinMax[2];
            yMax = vetMinMax[3];
            zMin = vetMinMax[4];
            zMax = vetMinMax[5];
        }
        
        public void setNomeCor(String nomeCor)
        {
            this.nomeCor = nomeCor;
        }
        
        public void criaActor(vtkImplicitFunction funcao, double[] minMax, String nomeCor)
        {
            setFuncaoOriginal(funcao);
            setMinMax(minMax);
            setNomeCor(nomeCor);
          
            sample = new vtkSampleFunction();
            surface = new vtkContourFilter();
            mapper = new vtkPolyDataMapper();
            
            actor = new vtkActor();
            
            sample.SetImplicitFunction(funcaoOriginal);
            sample.SetModelBounds(xMin, xMax, yMin, yMax, zMin, zMax);
            sample.SetSampleDimensions(100, 100, 100);
            sample.ComputeNormalsOff();
            
            surface.SetInputConnection(sample.GetOutputPort());
            surface.SetValue(0, 0.0);
            
            mapper.SetInputConnection(surface.GetOutputPort());
            mapper.ScalarVisibilityOff();
        
            double[] cor = new double[3];
            
            actor.SetMapper(mapper);
            new vtkNamedColors().GetColorRGB(this.nomeCor, cor);
            actor.GetProperty().SetColor(cor);
        }
        
        public Actor()
        {
            funcaoOriginal = null; funcaoCortada = null;
            cortado = false;
            xMin = 0; xMax = 0; yMin = 0; yMax = 0; zMin = 0; zMax = 0;
            nomeCor = null;
            
            sample = null;
            surface = null;
            mapper = null;
            
            actor = null;
        }
        
        public Actor(vtkImplicitFunction funcao, double[] minMax, String nomeCor)
        {
            criaActor(funcao, minMax, nomeCor);
        }
        
        public void atualizaActor(vtkImplicitFunction funcao)
        {
            this.funcao = funcao;
            
            sample.SetImplicitFunction(this.funcao);
            surface.SetInputConnection(sample.GetOutputPort());
            mapper.SetInputConnection(surface.GetOutputPort());
            actor.SetMapper(mapper);
        }
        
        public void cortaActor()
        {
            if(cortado)
                atualizaActor(funcaoOriginal);
            else
                atualizaActor(funcaoCortada);
            
            cortado = !cortado;
        }
        
        public vtkImplicitFunction getFuncao()
        {
            return funcaoOriginal;
        }
        
        public double[] getMinMax()
        {
            return new double[]{xMin, xMax, yMin, yMax, zMin, zMax};
        }
        
        public String getNomeCor()
        {
            return nomeCor;
        }
        
        public vtkActor getActor()
        {
            return actor;
        }
    }
    
    private static final long serialVersionUID = 1L;
    private final vtkPanel renWin;
    private final JButton exitButton;
    private final int INDIANO = 0, CHINES = 1, BATELADA = 2;
    private boolean cortado = false;
    private int tipo;
    
    private void normalizaVetor(double[] vet)
    {
        double moduloVet = Math.sqrt(Math.pow(vet[0], 2) + Math.pow(vet[1], 2) +
                                      Math.pow(vet[2], 2));
       vet[0] /= moduloVet;
       vet[1] /= moduloVet;
       vet[2] /= moduloVet;
    }
    
    private vtkImplicitFunction fechaCilindro(double[] centro, double[] eixo, double raio,
                                              double altura)
    {
        double altura2 = altura/2;
        
        vtkCylinder corpoCilindro = new vtkCylinder();
        corpoCilindro.SetCenter(centro);
        corpoCilindro.SetAxis(eixo);
        corpoCilindro.SetRadius(raio);
        
        vtkPlane tampaCilindro = new vtkPlane(),
                 baseCilindro = new vtkPlane();
        tampaCilindro.SetNormal(eixo);
        baseCilindro.SetNormal(-eixo[0], -eixo[1], -eixo[2]);
        tampaCilindro.SetOrigin(centro[0] + eixo[0] * altura2,
                                centro[1] + eixo[1] * altura2,
                                centro[2] + eixo[2] * altura2);
        baseCilindro.SetOrigin(centro[0] - eixo[0] * altura2,
                               centro[1] - eixo[1] * altura2,
                               centro[2] - eixo[2] * altura2);
        
        vtkImplicitBoolean cilindro = new vtkImplicitBoolean();
        cilindro.SetOperationTypeToIntersection();
        cilindro.AddFunction(corpoCilindro);
        cilindro.AddFunction(tampaCilindro);
        cilindro.AddFunction(baseCilindro);
        
        return cilindro;
    }
    
    private vtkImplicitFunction geraCilindro(double[] centro, double[] eixo, double raio,
                                             double altura, double espParede, double espFundo,
                                             double espTopo)
    {
        normalizaVetor(eixo);
        
        //Cilindro externo
        vtkImplicitFunction extCilindro = fechaCilindro(centro, eixo, raio, altura);
        
        //Cilindro interno
        double fat = -espTopo/2 + espFundo/2;
        double[] intCentro = new double[]{centro[0] + eixo[0] * fat,
                                          centro[1] + eixo[1] * fat,
                                          centro[2] + eixo[2] * fat}; 
        vtkImplicitFunction intCilindro = fechaCilindro(intCentro,
                                                        eixo, raio - espParede,
                                                        altura - espTopo - espFundo);

        //Cilindro final
        vtkImplicitBoolean oCilindro = new vtkImplicitBoolean();
        oCilindro.SetOperationTypeToDifference();
        oCilindro.AddFunction(extCilindro);
        oCilindro.AddFunction(intCilindro);
        
        return oCilindro;
    }
    
    private vtkImplicitFunction geraCalota(double[] centro, double[] eixo, double raio,
                                           double altura, double espessura)
    {
        normalizaVetor(eixo);
        
        vtkSphere esferaExt = new vtkSphere();
        esferaExt.SetCenter(centro);
        esferaExt.SetRadius(raio);
        
        vtkSphere esferaInt = new vtkSphere();
        esferaInt.SetCenter(centro);
        esferaInt.SetRadius(raio - espessura);
        
        vtkImplicitBoolean casca = new vtkImplicitBoolean();
        casca.SetOperationTypeToDifference();
        casca.AddFunction(esferaExt);
        casca.AddFunction(esferaInt);
        
        vtkPlane corte = new vtkPlane();
        double dist = raio - altura;
        corte.SetOrigin(centro[0] + eixo[0] * dist, 
                        centro[1] + eixo[1] * dist, 
                        centro[2] + eixo[2] * dist);
        corte.SetNormal(-eixo[0], -eixo[1], -eixo[2]);
        
        vtkImplicitBoolean calota = new vtkImplicitBoolean();
        calota.SetOperationTypeToIntersection();
        calota.AddFunction(casca);
        calota.AddFunction(corte);
        
        return calota;
    }
    
    public Modelo3D(int Tipo, Biodigestor biodig) {
        super(new BorderLayout());
        tipo = Tipo;
        
        double espParede = 0.1, espTopoFundo = 0.1, espGas = 0.1,
               Rb = biodig.params[0].getValor() / 2 + espParede, Hb, altOciRev = 1.2, altOciGas = 1.3,
               Re = 0.5, Rs = 1, hs = 0.7,
               alturaSolo,
               larguraRevGas = Rb + espGas + 2 * 0.1,
               a = 0.2;
        
        //Parâmetros------------------------------------------------------------
        
        //----------------------------------------------------------------------
        if(tipo == CHINES)
        {
            double hg = Rb / 2; // (2 * Rb) / 4
            
            Hb = biodig.params[1].getValor();
            Re = biodig.params[11].getValor() / 2 + espParede;
            Rs = biodig.params[9].getValor() / 2 + espParede;
            hs = biodig.params[8].getValor() + espTopoFundo;
            alturaSolo = Hb / 2 + hg + a;
        }
        else
        {
            if(tipo == INDIANO)
            {
                double De = (4 * biodig.params[19].getValor() /*v*/) / (Math.PI * (0.5 - 0.1)),
                       h2 = biodig.params[11].getValor();
                De = Math.sqrt( (De > 0) ? De : 0 );
                
                Hb = biodig.params[3].getValor() + espTopoFundo;
                altOciRev = altOciGas = biodig.params[10].getValor() + h2; //h1 + h2
                Re = De / 2 + espParede;
                alturaSolo = Hb / 2 + h2;
            }
            else //Batelada
            {
                double h2 = biodig.params[8].getValor();
                
                Hb = biodig.params[1].getValor() - biodig.params[8].getValor(); //H - h2
                Hb += espTopoFundo;
                altOciGas = biodig.params[7].getValor() + h2; //h1 + h2
                altOciRev = altOciGas - 0.1;
                alturaSolo = Hb / 2 + h2;
            }
        }
        //Funções---------------------------------------------------------------
        
        //Solo
        vtkImplicitFunction baseSolo     = fechaCilindro(new double[]{0, alturaSolo, 0}, 
                                                         new double[]{0, 1, 0}, 
                                                         Rb + Re + 2.5, 0.1),
                            espacoBioDig = fechaCilindro(new double[]{0, alturaSolo, 0}, 
                                                         new double[]{0, 1, 0}, 
                                                         (tipo == CHINES) ? 0.3 + espParede : larguraRevGas, 4),
                            espacoCaixaSaiCh = fechaCilindro(new double[]{Rb - espParede + Rs, alturaSolo, 0}, 
                                                             new double[]{0, 1, 0}, 
                                                             Rs, 4);
        
        //Biomassa
        double espFundo = (tipo == INDIANO || tipo == BATELADA) ? espTopoFundo : 0;
        
        vtkImplicitFunction cilindroBiomassaNormal = geraCilindro(new double[]{0, 0, 0}, 
                                                                  new double[]{0, 1, 0},
                                                                  Rb, Hb, espParede, espFundo, 0),
                            cilindroAux = fechaCilindro(new double[]{0, Hb / 2, 0},
                                                        new double[]{0, 1, 0}, 
                                                        Rb - espParede, 2 * espTopoFundo);
        
        //Revestimento Gasômetro
        double centroRevGas = Hb / 2 + altOciRev / 2 + espTopoFundo / 2,
               alturaRevGas = altOciRev + espTopoFundo,
               centroGas    = Hb / 2 + altOciGas / 2 + espTopoFundo,
               alturaGas    = altOciGas;
               
        vtkImplicitFunction cilindroRevGasometro = geraCilindro(new double[]{0, centroRevGas, 0}, 
                                                                new double[]{0, 1, 0},
                                                                larguraRevGas,
                                                                alturaRevGas, espParede, espTopoFundo, 0);
        
        //Gasômetro
        vtkImplicitFunction cilindroGasometroNormal = geraCilindro(new double[]{0, centroGas, 0}, 
                                                                   new double[]{0, 1, 0},
                                                                   Rb - espParede + espGas + 0.1, 
                                                                   alturaGas, espGas, 0, espGas);
        
        //Parede
        double altParede = Hb;
        vtkImplicitFunction cilindroParede = fechaCilindro(new double[]{0, -Hb / 2 + espTopoFundo + altParede / 2, 0},
                                                           new double[]{0, 1, 0}, 
                                                           Rb - espParede, altParede);
        vtkPlane corteDir = new vtkPlane(),
                 corteEsq = new vtkPlane();
        
        if(tipo == INDIANO)
        {
            corteDir.SetOrigin(0.1, 0, 0);
            corteDir.SetNormal(1, 0, 0);

            corteEsq.SetOrigin(-0.1, 0, 0);
            corteEsq.SetNormal(-1, 0, 0);
        }
        
        //Guia
        vtkImplicitFunction guia = fechaCilindro(new double[]{0, Hb / 2 + espTopoFundo + 0.75 * alturaGas, 0},
                                                 new double[]{0, 1, 0}, 
                                                 0.05, 1.5 * alturaGas);
        
        //Tubo esquerdo e direito
        vtkImplicitFunction tuboEsqFuro = new vtkCylinder(),
                            tuboDirFuro = new vtkCylinder();
        
        double    xi         = Math.sin(Math.toRadians(30)),
                  yi         = Math.cos(Math.toRadians(30)),
                  /*Nível do substrato (topo do solo) + altura do cilindro / 2 (abaixo da origem) -
                    - espessura do fundo - e (ver Ortolani, 1991) - raio do cano + 
                    + a (ver Ortolani, 1991)
                  */
                  Hesq       = alturaSolo + 0.05 + Hb/2 - espTopoFundo - 0.3 - 0.1 + 0.5,
                  compEsq    = Hesq / Math.cos(Math.toRadians(30)),
                  xEsq       = Math.cos(Math.toRadians(60)) * (compEsq/2),
                  yEsq       = Math.sin(Math.toRadians(60)) * (compEsq/2),
                  mEsq       = Math.tan(Math.toRadians(30)) * Hesq,
                  Hdir       = Hesq - 0.5,
                  compDir    = Hdir / Math.cos(Math.toRadians(30)),
                  xDir       = Math.cos(Math.toRadians(60)) * (compDir/2),
                  yDir       = Math.sin(Math.toRadians(60)) * (compDir/2),
                  mDir       = Math.tan(Math.toRadians(30)) * Hdir;
        
        vtkImplicitFunction tuboEsq = geraCilindro(new double[]{-Rb + espParede - xEsq, -Hb/2 + espTopoFundo + 0.3 + 0.1 + yEsq, 0},
                                                   new double[]{-xi, yi, 0}, 
                                                   0.1, compEsq + 0.15, 0.03, 0, 0),
                            tuboDir = geraCilindro(new double[]{Rb - espParede + xDir, -Hb/2 + espTopoFundo + 0.3 + 0.1 + yDir, 0},
                                                   new double[]{xi, yi, 0}, 
                                                   0.1, compDir + 0.15, 0.03, 0, 0);
        
        //Caixa de entrada
        vtkImplicitFunction caixaEnt = geraCilindro(new double[]{-Rb + espParede - mEsq, alturaSolo + 0.5, 0},
                                                    new double[]{0, 1, 0}, 
                                                    Re, 1, espParede, 0, 0);
        
        //Caixa de saída
        vtkImplicitFunction caixaSai = geraCilindro(new double[]{Rb - espParede + mDir, alturaSolo + 0.2, 0},
                                                    new double[]{0, 1, 0}, 
                                                    0.5, 0.4, espParede, 0, 0);
        
        //Fundo da caixa de entrada
        vtkImplicitFunction caixaEntFundoNormal = fechaCilindro(new double[]{-Rb + espParede - mEsq, alturaSolo + 0.5, 0},
                                                                new double[]{0, 1, 0}, 
                                                                Re - espParede, 0.01);
        //Calota superior
        double D = 2 * Rb,
               hg = D / 4, 
               Rg = ( (D*D / 4) + hg*hg ) / (2*hg);
        
        vtkImplicitFunction calotaSupNormal = geraCalota(new double[]{0, Hb / 2 + hg - Rg, 0},
                                                         new double[]{0, 1, 0},
                                                         Rg, hg, 0.8 * espParede);
        
        vtkImplicitFunction furoCalotaSup = fechaCilindro(new double[]{0, Hb / 2 + hg, 0},
                                                          new double[]{0, 1, 0},
                                                          0.3, 1);
        
        double hf = D / 8,
               Rf = ( (D*D / 4) + hf*hf ) / (2*hf);
        
        vtkImplicitFunction calotaInf = geraCalota(new double[]{0, -Hb / 2 - hf + Rf, 0},
                                                   new double[]{0, -1, 0},
                                                   Rf, hf, 0.4 * espParede);
        
        //Tampa de inspeção
        vtkImplicitFunction tampaInspNormal = geraCilindro(new double[]{0, Hb / 2 + hg, 0},
                                                           new double[]{0, 1, 0},
                                                           0.3 + espParede, 2 * (0.2 + a + espTopoFundo),
                                                           espParede, 0, espTopoFundo);
        
        vtkSphere corteTampa = new vtkSphere();
        corteTampa.SetCenter(0, Hb / 2 + hg - Rg, 0);
        corteTampa.SetRadius(Rg);
        
        //Tubo de saída
        double altTubo = Hb / 3 + 0.2 + espTopoFundo;
        vtkImplicitFunction tuboSaiNormal = geraCilindro(new double[]{Rb + 0.3 / 2, Hb / 2 - altTubo / 2, 0},
                                                   new double[]{0, 1, 0},
                                                   0.3 / 2 + espParede, altTubo, espParede, espTopoFundo, 0);
        
        vtkImplicitFunction furoTuboSai = fechaCilindro(new double[]{Rb - espParede / 2, Hb / 2 - altTubo + espTopoFundo + 0.1, 0},
                                                   new double[]{1, 0, 0},
                                                   0.1, 2 * espParede);
        //Caixa de saída chinês
        vtkImplicitFunction caixaSaiChNormal = geraCilindro(new double[]{Rb - espParede + Rs, Hb / 2 + (hs + espTopoFundo) / 2, 0},
                                                   new double[]{0, 1, 0},
                                                   Rs, hs + espTopoFundo, espParede, espTopoFundo, 0);
        
        vtkImplicitFunction furoSaiCh = fechaCilindro(new double[]{Rb + 0.3 / 2, Hb / 2 + espTopoFundo / 2, 0},
                                                   new double[]{0, 1, 0},
                                                   0.15, espTopoFundo);
        
        if(tipo == INDIANO || tipo == CHINES)
        {
           
           tuboEsqFuro = fechaCilindro(new double[]{-Rb + espParede - xEsq, -Hb/2 + espTopoFundo + 0.3 + 0.1 + yEsq, 0},
                                       new double[]{-xi, yi, 0}, 
                                       0.1, compEsq + 0.15);
           
           if(tipo == INDIANO)
                tuboDirFuro = fechaCilindro(new double[]{Rb - espParede + xDir, -Hb/2 + espTopoFundo + 0.3 + 0.1 + yDir, 0},
                                            new double[]{xi, yi, 0}, 
                                            0.1, compDir + 0.15);
        }
        
        //----------------------------------------------------------------------
        
        //Operações-------------------------------------------------------------
        
        //Solo
        vtkImplicitBoolean soloNormal = new vtkImplicitBoolean();
        soloNormal.SetOperationTypeToDifference();
        soloNormal.AddFunction(baseSolo);
        soloNormal.AddFunction(espacoBioDig);
        
        if(tipo == CHINES)
            soloNormal.AddFunction(espacoCaixaSaiCh);
        
        vtkImplicitBoolean solo = new vtkImplicitBoolean();
        solo.SetOperationTypeToDifference();
        solo.AddFunction(soloNormal);
        
        if(tipo == INDIANO || tipo == CHINES)
        {
            solo.AddFunction(tuboEsqFuro);
            
            if(tipo == INDIANO)
                solo.AddFunction(tuboDirFuro);
        }
        
        //Biomassa
        vtkImplicitBoolean cilindroBiomassa = new vtkImplicitBoolean();
        cilindroBiomassa.SetOperationTypeToDifference();
        cilindroBiomassa.AddFunction(cilindroBiomassaNormal);
        
        if(tipo == INDIANO || tipo == CHINES)
        {
            cilindroBiomassa.AddFunction(tuboEsqFuro);
            
            if(tipo == INDIANO)
                cilindroBiomassa.AddFunction(tuboDirFuro);
            
            if(tipo == CHINES)
                cilindroBiomassa.AddFunction(furoTuboSai);
        }
        
        //Gasômetro
        vtkImplicitBoolean cilindroGasometro = new vtkImplicitBoolean();
        cilindroGasometro.SetOperationTypeToDifference();
        cilindroGasometro.AddFunction(cilindroGasometroNormal);
        
        //Parede
        vtkImplicitBoolean parede = new vtkImplicitBoolean();
        
        if(tipo == INDIANO)
        {
            parede.SetOperationTypeToIntersection();
            parede.AddFunction(cilindroParede);
            parede.AddFunction(corteDir);
            parede.AddFunction(corteEsq);
            
            cilindroGasometro.AddFunction(guia);
        }
        
        //Revestimento Gasômetro
        vtkImplicitBoolean cilindroRevGas = new vtkImplicitBoolean();
        
        //Fundo da caixa de entrada
        vtkImplicitBoolean caixaEntFundo = new vtkImplicitBoolean();
        
        if(tipo == INDIANO || tipo == BATELADA)
        {
            cilindroRevGas.SetOperationTypeToDifference();
            cilindroRevGas.AddFunction(cilindroRevGasometro);
            cilindroRevGas.AddFunction(cilindroAux);
        }
        
        if(tipo == INDIANO || tipo == CHINES)
        {
            caixaEntFundo.SetOperationTypeToDifference();
            caixaEntFundo.AddFunction(caixaEntFundoNormal);
            caixaEntFundo.AddFunction(tuboEsqFuro);
        }
        
        //Tampa de inspeção
        vtkImplicitBoolean tampaInsp = new vtkImplicitBoolean();
        
        //Calota superior
        vtkImplicitBoolean calotaSup = new vtkImplicitBoolean();
        
        //Tubo de saída
        vtkImplicitBoolean tuboSai = new vtkImplicitBoolean();
        
        //Caixa de saída chinês
        vtkImplicitBoolean caixaSaiCh = new vtkImplicitBoolean();
        
        if(tipo == CHINES)
        {
            tampaInsp.SetOperationTypeToDifference();
            tampaInsp.AddFunction(tampaInspNormal);
            tampaInsp.AddFunction(corteTampa);
            
            calotaSup.SetOperationTypeToDifference();
            calotaSup.AddFunction(calotaSupNormal);
            calotaSup.AddFunction(furoCalotaSup);
            
            tuboSai.SetOperationTypeToDifference();
            tuboSai.AddFunction(tuboSaiNormal);
            tuboSai.AddFunction(cilindroBiomassa);
            tuboSai.AddFunction(furoTuboSai);
            
            caixaSaiCh.SetOperationTypeToDifference();
            caixaSaiCh.AddFunction(caixaSaiChNormal);
            caixaSaiCh.AddFunction(calotaSup);
            caixaSaiCh.AddFunction(furoSaiCh);
        }  

        //Actor-----------------------------------------------------------------
        
        //Solo
        Actor atorSolo = new Actor(solo, new double[]{-Rb - Re - 3, Rb + Re + 3, 0, alturaSolo + 0.2, -Rb - Re - 3, Rb + Re + 3}, "Chocolate"),
              atorBiomassa = new Actor(cilindroBiomassa, new double[]{-Rb, Rb, -Hb / 2, Hb / 2, -Rb, Rb}, "Snow"),
              atorRevGas = new Actor(),
              atorGas = new Actor(),
              atorParede = new Actor(),
              atorGuia = new Actor(),
              atorTuboEsq = new Actor(),
              atorTuboDir = new Actor(),
              atorCaixaEnt = new Actor(),
              atorCaixaSai = new Actor(),
              atorCaixaEntFundo = new Actor(),
              atorCalotaSup = new Actor(),
              atorCalotaInf = new Actor(),
              atorTampaInsp = new Actor(),
              atorTuboSai = new Actor(),
              atorCaixaSaiCh = new Actor();
        
        if(tipo == INDIANO || tipo == BATELADA)
        {
            atorRevGas.criaActor(cilindroRevGas, new double[]{-larguraRevGas, larguraRevGas, centroRevGas - alturaRevGas / 2, centroRevGas + alturaRevGas / 2, -larguraRevGas, larguraRevGas}, "Snow");
            atorGas.criaActor(cilindroGasometro, new double[]{-larguraRevGas + espParede + 0.1, larguraRevGas - espParede - 0.1, centroGas - alturaGas / 2, centroGas + alturaGas / 2, -larguraRevGas + espParede + 0.1, larguraRevGas - espParede - 0.1}, "Gray");
        }
        
        if(tipo == INDIANO || tipo == CHINES)
        {      
            if(tipo == INDIANO)
            {
                atorParede.criaActor(parede, new double[]{-Rb, Rb, -Hb / 2, Hb / 2 + espTopoFundo, -Rb, Rb}, "Snow");
                atorGuia.criaActor(guia, new double[]{-0.05, 0.05, Hb / 2 + espTopoFundo, Hb / 2 + espTopoFundo + 1.5 * alturaGas + 0.1, -0.05, 0.05}, "Snow");
                atorTuboDir.criaActor(tuboDir, new double[]{Rb - espParede - 0.2, Rb + 0.1 + mDir, -Hb/2, alturaSolo + 0.05 + 0.2, -0.3, 0.3}, "Snow");
                atorCaixaSai.criaActor(caixaSai, new double[]{Rb - espParede + mDir - 0.6 , Rb - espParede + mDir + 0.6, alturaSolo - 0.1, alturaSolo + 0.5, -0.6, 0.6}, "Snow"); 
            }
            
            if(tipo == CHINES)
            {
                atorCalotaSup.criaActor(calotaSup, new double[]{-Rb, Rb, Hb / 2 - 0.1, Hb / 2 + hg + 0.1, -Rb, Rb}, "Snow");
                atorCalotaInf.criaActor(calotaInf, new double[]{-Rb, Rb, -Hb / 2 - hf - 0.1, -Hb / 2 + 0.1, -Rb, Rb}, "Snow");
                atorTampaInsp.criaActor(tampaInsp, new double[]{-0.3 - espParede, 0.3 + espParede, Hb / 2, Hb / 2 + hg + a + 0.2 + espTopoFundo, -0.3 - espParede, 0.3 + espParede}, "Snow");
                atorTuboSai.criaActor(tuboSai, new double[]{Rb - espParede, Rb + 0.3 + espParede, Hb / 2 - altTubo, Hb / 2, -0.3 - espParede, 0.3 + espParede}, "Snow");
                atorCaixaSaiCh.criaActor(caixaSaiCh, new double[]{Rb - espParede, Rb + 2 * Rs, Hb / 2 - 0.1, Hb / 2 + hs + espTopoFundo + 0.1, -Rs, Rs}, "Snow");
            }
            
            atorTuboEsq.criaActor(tuboEsq, new double[]{-Rb - 0.1 - mEsq, -Rb + espParede + 0.2, -Hb / 2, alturaSolo + 0.05 + 0.7, -0.3, 0.3}, "Snow");
            atorCaixaEnt.criaActor(caixaEnt, new double[]{-Rb + espParede - mEsq - Re - 0.1, -Rb + espParede - mEsq + Re + 0.1, alturaSolo - 0.1, alturaSolo + 1.1, -Re - 0.1, Re + 0.1}, "Snow");
            atorCaixaEntFundo.criaActor(caixaEntFundo, new double[]{-Rb + espParede - mEsq - Re, -Rb + espParede - mEsq + Re, alturaSolo - 0.1 + 0.3, alturaSolo + 0.8, -Re, Re}, "Snow");
        }
        
        //----------------------------------------------------------------------
        
        renWin = new vtkPanel();
        
        renWin.GetRenderer().AddActor(atorSolo.getActor());
        renWin.GetRenderer().AddActor(atorBiomassa.getActor());
        
        if(tipo == INDIANO || tipo == BATELADA)
        {
            renWin.GetRenderer().AddActor(atorRevGas.getActor());
            renWin.GetRenderer().AddActor(atorGas.getActor());
        }
        
        if(tipo == INDIANO || tipo == CHINES)
        {
            renWin.GetRenderer().AddActor(atorTuboEsq.getActor());
            renWin.GetRenderer().AddActor(atorCaixaEnt.getActor());
            renWin.GetRenderer().AddActor(atorCaixaEntFundo.getActor());
            
            if(tipo == INDIANO)
            {
                renWin.GetRenderer().AddActor(atorParede.getActor());
                renWin.GetRenderer().AddActor(atorGuia.getActor());
                renWin.GetRenderer().AddActor(atorTuboDir.getActor());
                renWin.GetRenderer().AddActor(atorCaixaSai.getActor());
            }
            
            if(tipo == CHINES)
            {
                renWin.GetRenderer().AddActor(atorCalotaSup.getActor());
                renWin.GetRenderer().AddActor(atorCalotaInf.getActor());
                renWin.GetRenderer().AddActor(atorTampaInsp.getActor());
                renWin.GetRenderer().AddActor(atorTuboSai.getActor());
                renWin.GetRenderer().AddActor(atorCaixaSaiCh.getActor());
            }
        }
        
        // Add Java UI components
        exitButton = new JButton("Corte");
        exitButton.addActionListener((e) -> {
            atorSolo.cortaActor();
            atorBiomassa.cortaActor();
            
            //Parede
            if(tipo == INDIANO || tipo == BATELADA)
            {
                atorRevGas.cortaActor();
                atorGas.cortaActor();
            }
            
            if(tipo == INDIANO || tipo == CHINES)
            {
                
                atorTuboEsq.cortaActor();
                atorCaixaEnt.cortaActor();
                atorCaixaEntFundo.cortaActor();
                
                if(tipo == INDIANO)
                {
                    atorParede.cortaActor();
                    atorGuia.cortaActor();
                    atorTuboDir.cortaActor();
                    atorCaixaSai.cortaActor();
                }
                
                if(tipo == CHINES)
                {
                    atorCalotaSup.cortaActor();
                    atorCalotaInf.cortaActor();
                    atorTampaInsp.cortaActor();
                    atorTuboSai.cortaActor();
                    atorCaixaSaiCh.cortaActor();
                }
            }
            
            cortado = !cortado;
            renWin.repaint();
        });
 
        renWin.GetRenderer().GetActiveCamera().SetPosition(0, 0, 25);
        renWin.repaint();
        
        add(renWin, BorderLayout.CENTER);
        add(exitButton, BorderLayout.SOUTH);
    }
 
    public static void iniciar(int tipo, Biodigestor biodig) {
        SwingUtilities.invokeLater( () ->
            {
                if(!vtkNativeLibrary.LoadAllNativeLibraries())
                {
                    javax.swing.JOptionPane.showMessageDialog(null, "Por favor, instale em seu sistemas as bibliotecas dinâmicas do VTK e configure a path","Erro ao tentar carregar bibliotecas gráficas",javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                JFrame frame = new JFrame("Biodigestor 3D");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.getContentPane().setLayout(new BorderLayout());
                frame.getContentPane().add(new Modelo3D(tipo, biodig), BorderLayout.CENTER);
                frame.setSize(800, 800);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        );
    }
}
