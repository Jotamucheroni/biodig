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
            sample.SetSampleDimensions(80, 80, 80);
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
    
    public Modelo3D(int Tipo) {
        super(new BorderLayout());
        tipo = Tipo;
        //Funções---------------------------------------------------------------
        
        //Solo
        vtkImplicitFunction baseSolo     = fechaCilindro(new double[]{0, 2.1, 0}, 
                                                         new double[]{0, 1, 0}, 
                                                         4, 0.1),
                            espacoBioDig = fechaCilindro(new double[]{0, 2.1, 0}, 
                                                         new double[]{0, 1, 0}, 
                                                         1.3, 4);
        
        //Biomassa
        double espFundo = (tipo == INDIANO || tipo == BATELADA) ? 0.1 : 0;
        
        vtkImplicitFunction cilindroBiomassaNormal = geraCilindro(new double[]{0, 0, 0}, 
                                                                  new double[]{0, 1, 0},
                                                                  1, 2.4, 0.1, espFundo, 0),
                            cilindroAux = fechaCilindro(new double[]{0, 0, 0},
                                                        new double[]{0, 1, 0}, 
                                                        0.9, 4);
        
        //Revestimento Gasômetro
        vtkImplicitFunction cilindroRevGasometro = geraCilindro(new double[]{0, 1.8, 0}, 
                                                                new double[]{0, 1, 0},
                                                                1.3, 1.2, 0.1, 0.1, 0);
        
        //Gasômetro
        vtkImplicitFunction cilindroGasometro = geraCilindro(new double[]{0, 1.9, 0}, 
                                                             new double[]{0, 1, 0},
                                                             1.15, 1.2, 0.15, 0, 0.05);
        
        //Parede
        vtkImplicitFunction cilindroParede = fechaCilindro(new double[]{0, -0.6, 0},
                                                           new double[]{0, 1, 0}, 
                                                           0.9, 1);
        vtkPlane corteDir = new vtkPlane(),
                 corteEsq = new vtkPlane();
        
        if(tipo == INDIANO)
        {
            corteDir.SetOrigin(0.1, 0, 0);
            corteDir.SetNormal(1, 0, 0);

            corteEsq.SetOrigin(-0.1, 0, 0);
            corteEsq.SetNormal(-1, 0, 0);
        }
        
        //Tubo esquerdo e direito
        vtkImplicitFunction tuboEsqFuro = new vtkCylinder(),
                            tuboDirFuro = new vtkCylinder();
        
        double    xi         = Math.sin(Math.toRadians(30)),
                  yi         = Math.cos(Math.toRadians(30)),
                  Hesq       = 2.15 + 1.2 - 0.2 - 0.3 - 0.1 + 0.5,
                  compEsq    = Hesq / Math.cos(Math.toRadians(30)),
                  xEsq       = Math.cos(Math.toRadians(60)) * (compEsq/2),
                  yEsq       = Math.sin(Math.toRadians(60)) * (compEsq/2),
                  mEsq       = Math.tan(Math.toRadians(30)) * Hesq,
                  Hdir       = Hesq - 0.5,
                  compDir    = Hdir / Math.cos(Math.toRadians(30)),
                  xDir       = Math.cos(Math.toRadians(60)) * (compDir/2),
                  yDir       = Math.sin(Math.toRadians(60)) * (compDir/2),
                  mDir       = Math.tan(Math.toRadians(30)) * Hdir;
        
        vtkImplicitFunction tuboEsq = geraCilindro(new double[]{-0.9 - xEsq, -1.2 + 0.2 + 0.3 + 0.1 + yEsq, 0},
                                                   new double[]{-xi, yi, 0}, 
                                                   0.1, compEsq + 0.2, 0.03, 0, 0),
                            tuboDir = geraCilindro(new double[]{0.9 + xDir, -1.2 + 0.2 + 0.3 + 0.1 + yDir, 0},
                                                   new double[]{xi, yi, 0}, 
                                                   0.1, compDir + 0.2, 0.03, 0, 0);
        
        //Caixa de entrada
        vtkImplicitFunction caixaEnt = geraCilindro(new double[]{-0.9 - mEsq, 2.1 + 0.5, 0},
                                                    new double[]{0, 1, 0}, 
                                                    0.5, 1, 0.1, 0, 0);
        
        //Caixa de saída
        vtkImplicitFunction caixaSai = geraCilindro(new double[]{0.9 + mDir, 2.1 + 0.2, 0},
                                                    new double[]{0, 1, 0}, 
                                                    0.5, 0.4, 0.1, 0, 0);
        
        //Fundo da caixa de entrada
        vtkImplicitFunction caixaEntFundoNormal = fechaCilindro(new double[]{-0.9 - mEsq, 2.1 + 0.5, 0},
                                                                new double[]{0, 1, 0}, 
                                                                0.4, 0.01);
        //Calota superior
        double D = 2,
               hg = D / 4, 
               Rg = ( (D*D / 4) + hg*hg ) / (2*hg);
        
        vtkImplicitFunction calotaSupNormal = geraCalota(new double[]{0, 1.2 + hg - Rg, 0},
                                                         new double[]{0, 1, 0},
                                                         Rg, hg, 0.1);
        
        vtkImplicitFunction furoCalotaSup = fechaCilindro(new double[]{0, 1.2 + hg, 0},
                                                          new double[]{0, 1, 0},
                                                          0.6, 1);
        
        double hf = D / 8,
               Rf = ( (D*D / 4) + hf*hf ) / (2*hf);
        
        vtkImplicitFunction calotaInf = geraCalota(new double[]{0, -1.2 - hf + Rf, 0},
                                                   new double[]{0, -1, 0},
                                                   Rf, hf, 0.05);
        
        //Tampa de inspeção
        vtkImplicitFunction tampaInspNormal = geraCilindro(new double[]{0, 1.2 + hg, 0},
                                                           new double[]{0, 1, 0},
                                                           0.7, 1, 0.1, 0, 0.1);
        
        vtkSphere corteTampa = new vtkSphere();
        corteTampa.SetCenter(0, 1.2 + hg - Rg, 0);
        corteTampa.SetRadius(Rg);
        
        if(tipo == INDIANO || tipo == CHINES)
        {
           
           tuboEsqFuro = fechaCilindro(new double[]{-0.9 - xEsq, -1.2 + 0.2 + 0.3 + 0.1 + yEsq, 0},
                                       new double[]{-xi, yi, 0}, 
                                       0.1, compEsq + 0.3);
           
           if(tipo == INDIANO)
                tuboDirFuro = fechaCilindro(new double[]{0.9 + xDir, -1.2 + 0.2 + 0.3 + 0.1 + yDir, 0},
                                            new double[]{xi, yi, 0}, 
                                            0.1, compDir + 0.3);
        }
        
        //----------------------------------------------------------------------
        
        //Operações-------------------------------------------------------------
        
        //Solo
        vtkImplicitBoolean soloNormal = new vtkImplicitBoolean();
        soloNormal.SetOperationTypeToDifference();
        soloNormal.AddFunction(baseSolo);
        soloNormal.AddFunction(espacoBioDig);
        
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
        }
        
        //Parede
        vtkImplicitBoolean parede = new vtkImplicitBoolean();
        
        if(tipo == INDIANO)
        {
            parede.SetOperationTypeToIntersection();
            parede.AddFunction(cilindroParede);
            parede.AddFunction(corteDir);
            parede.AddFunction(corteEsq);
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
        
        if(tipo == CHINES)
        {
            tampaInsp.SetOperationTypeToDifference();
            tampaInsp.AddFunction(tampaInspNormal);
            tampaInsp.AddFunction(corteTampa);
            
            calotaSup.SetOperationTypeToDifference();
            calotaSup.AddFunction(calotaSupNormal);
            calotaSup.AddFunction(furoCalotaSup);
        }  

        //Actor-----------------------------------------------------------------
        
        //Solo
        Actor atorSolo = new Actor(solo, new double[]{-4.1, 4.1, 0, 2.3, -4.1, 4.1}, "Chocolate"),
              atorBiomassa = new Actor(cilindroBiomassa, new double[]{-1.1, 1.1, -1.3, 1.3, -1.1, 1.1}, "Snow"),
              atorRevGas = new Actor(),
              atorGas = new Actor(),
              atorParede = new Actor(),
              atorTuboEsq = new Actor(),
              atorTuboDir = new Actor(),
              atorCaixaEnt = new Actor(),
              atorCaixaSai = new Actor(),
              atorCaixaEntFundo = new Actor(),
              atorCalotaSup = new Actor(),
              atorCalotaInf = new Actor(),
              atorTampaInsp = new Actor();
        
        if(tipo == INDIANO || tipo == BATELADA)
        {
            atorRevGas.criaActor(cilindroRevGas, new double[]{-1.4, 1.4, 1.1, 2.5, -1.4, 1.4}, "Snow");
            atorGas.criaActor(cilindroGasometro, new double[]{-1.25, 1.25, 1.1, 2.6, -1.25, 1.25}, "Gray");
        }
        
        if(tipo == INDIANO || tipo == CHINES)
        {      
            if(tipo == INDIANO)
            {
                atorParede.criaActor(parede, new double[]{-1.1, 1.1, -1.3, 1.3, -1.1, 1.1}, "Snow");
                atorTuboDir.criaActor(tuboDir, new double[]{0.7, 1.1 + mDir, -1.2 + 0.2 + 0.2, 2.15 + 0.6, -0.3, 0.3}, "Snow");
                atorCaixaSai.criaActor(caixaSai, new double[]{0.9 + mDir - 0.6 , 0.9 + mDir + 0.6, 2, 2.1 + 0.5, -0.6, 0.6}, "Snow"); 
            }
            
            if(tipo == CHINES)
            {
                atorCalotaSup.criaActor(calotaSup, new double[]{-1.1, 1.1, 1.1, 1.2 + hg + 0.1, -1.1, 1.1}, "Snow");
                atorCalotaInf.criaActor(calotaInf, new double[]{-1.1, 1.1, -1.2 - hf - 0.1, -1.1, -1.1, 1.1}, "Snow");
                atorTampaInsp.criaActor(tampaInsp, new double[]{-0.8, 0.8, 1.2 + hg -0.6, 1.2 + hg + 0.6, -0.8, 0.8}, "Snow");
            }
            
            atorTuboEsq.criaActor(tuboEsq, new double[]{-1.1 - mEsq, -0.7, -1.2 + 0.2 + 0.2, 2.15 + 0.7, -0.3, 0.3}, "Snow");
            atorCaixaEnt.criaActor(caixaEnt, new double[]{-0.9 - mEsq - 0.6, -0.9 - mEsq + 0.6, 2, 2.1 + 1.1, -0.6, 0.6}, "Snow");
            atorCaixaEntFundo.criaActor(caixaEntFundo, new double[]{-0.9 - mEsq - 0.5, -0.9 - mEsq + 0.5, 2 + 0.3, 2.1 + 0.8, -0.5, 0.5}, "Snow");
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
                renWin.GetRenderer().AddActor(atorTuboDir.getActor());
                renWin.GetRenderer().AddActor(atorCaixaSai.getActor());
            }
            
            if(tipo == CHINES)
            {
                renWin.GetRenderer().AddActor(atorCalotaSup.getActor());
                renWin.GetRenderer().AddActor(atorCalotaInf.getActor());
                renWin.GetRenderer().AddActor(atorTampaInsp.getActor());
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
                    atorTuboDir.cortaActor();
                    atorCaixaSai.cortaActor();
                }
                
                if(tipo == CHINES)
                {
                    atorCalotaSup.cortaActor();
                    atorCalotaInf.cortaActor();
                    atorTampaInsp.cortaActor();
                }
            }
            
            cortado = !cortado;
            renWin.repaint();
        });
 
        add(renWin, BorderLayout.CENTER);
        add(exitButton, BorderLayout.SOUTH);
    }
 
    public static void iniciar(int tipo) {
        SwingUtilities.invokeLater( () ->
            {
                vtkNativeLibrary.LoadAllNativeLibraries();
                
                JFrame frame = new JFrame("Biodigestor 3D");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.getContentPane().setLayout(new BorderLayout());
                frame.getContentPane().add(new Modelo3D(tipo), BorderLayout.CENTER);
                frame.setSize(800, 800);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        );
    }
}
