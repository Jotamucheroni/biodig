package biodigestorindiano;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import vtk.vtkNativeLibrary;
import vtk.vtkCylinder;
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
    private static final long serialVersionUID = 1L;
    private final vtkPanel renWin;
    private final JButton exitButton;
    private final int INDIANO = 0, CHINES = 1, BATELADA = 2;
    private boolean cortado = false;
    private int tipo = INDIANO;
    
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
        double moduloEixo = Math.sqrt(Math.pow(eixo[0], 2) + Math.pow(eixo[1], 2) +
                                      Math.pow(eixo[2], 2));
        eixo[0] /= moduloEixo;
        eixo[1] /= moduloEixo;
        eixo[2] /= moduloEixo;
        
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
    
    // -----------------------------------------------------------------
    public Modelo3D() {
        super(new BorderLayout());

        //Funções---------------------------------------------------------------
        
        //Solo
        vtkImplicitFunction baseSolo     = fechaCilindro(new double[]{0, 2.1, 0}, 
                                                         new double[]{0, 1, 0}, 
                                                         4, 0.1),
                            espacoBioDig = fechaCilindro(new double[]{0, 2.1, 0}, 
                                                         new double[]{0, 1, 0}, 
                                                         1.3, 4);
        
        //Biomassa
        vtkImplicitFunction cilindroBiomassaNormal = geraCilindro(new double[]{0, 0, 0}, 
                                                                  new double[]{0, 1, 0},
                                                                  1, 2.4, 0.1, 0.2, 0),
                            cilindroAux = fechaCilindro(new double[]{0, 0, 0},
                                                        new double[]{0, 1, 0}, 
                                                        0.9, 4);
        vtkPlane corte = new vtkPlane();
        corte.SetOrigin(0, 0, 0);
        corte.SetNormal(0, 0, 1);
        
        //Revestimento Gasômetro
        vtkImplicitFunction cilindroRevGasometro = geraCilindro(new double[]{0, 1.8, 0}, 
                                                                new double[]{0, 1, 0},
                                                                1.3, 1.2, 0.1, 0.1, 0);
        
        //Gasômetro
        vtkImplicitFunction cilindroGasometro = geraCilindro(new double[]{0, 1.9, 0}, 
                                                             new double[]{0, 1, 0},
                                                             1.15, 1.2, 0.15, 0, 0.05);
        
        //Parede
        vtkImplicitFunction cilindroParede = fechaCilindro(new double[]{0, -0.5, 0},
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
        
        if(tipo == INDIANO)
        {
           
           tuboEsqFuro = fechaCilindro(new double[]{-0.9 - xEsq, -1.2 + 0.2 + 0.3 + 0.1 + yEsq, 0},
                                       new double[]{-xi, yi, 0}, 
                                       0.1, compEsq + 0.3);
           
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
        
        if(tipo == INDIANO)
        {
            solo.AddFunction(tuboEsqFuro);
            solo.AddFunction(tuboDirFuro);
        }
        
        vtkImplicitBoolean corteSolo = new vtkImplicitBoolean();
        corteSolo.SetOperationTypeToIntersection();
        corteSolo.AddFunction(solo);
        corteSolo.AddFunction(corte);
        
        //Biomassa
        vtkImplicitBoolean cilindroBiomassa = new vtkImplicitBoolean();
        cilindroBiomassa.SetOperationTypeToDifference();
        cilindroBiomassa.AddFunction(cilindroBiomassaNormal);
        
        if(tipo == INDIANO)
        {
            cilindroBiomassa.AddFunction(tuboEsqFuro);
            cilindroBiomassa.AddFunction(tuboDirFuro);
        }
        
        vtkImplicitBoolean corteBiomassaCilindro = new vtkImplicitBoolean();
        corteBiomassaCilindro.SetOperationTypeToIntersection();
        corteBiomassaCilindro.AddFunction(cilindroBiomassa);
        corteBiomassaCilindro.AddFunction(corte);
        
        //Parede
        vtkImplicitBoolean corteParede = new vtkImplicitBoolean(),
                           parede = new vtkImplicitBoolean();
        if(tipo == INDIANO)
        {
            parede.SetOperationTypeToIntersection();
            parede.AddFunction(cilindroParede);
            parede.AddFunction(corteDir);
            parede.AddFunction(corteEsq);
            
            corteParede.SetOperationTypeToIntersection();
            corteParede.AddFunction(parede);
            corteParede.AddFunction(corte);
        }
        
        //Revestimento Gasômetro
        vtkImplicitBoolean cilindroRevGas = new vtkImplicitBoolean();
        cilindroRevGas.SetOperationTypeToDifference();
        cilindroRevGas.AddFunction(cilindroRevGasometro);
        cilindroRevGas.AddFunction(cilindroAux);
        
        vtkImplicitBoolean corteRevGasCilindro = new vtkImplicitBoolean();
        corteRevGasCilindro.SetOperationTypeToIntersection();
        corteRevGasCilindro.AddFunction(cilindroRevGas);
        corteRevGasCilindro.AddFunction(corte);
        
        //Gasômetro     
        vtkImplicitBoolean corteGasCilindro = new vtkImplicitBoolean();
        corteGasCilindro.SetOperationTypeToIntersection();
        corteGasCilindro.AddFunction(cilindroGasometro);
        corteGasCilindro.AddFunction(corte);
        
        //Tubo esquerdo e direito
        vtkImplicitBoolean corteTuboEsq = new vtkImplicitBoolean();
        vtkImplicitBoolean corteTuboDir = new vtkImplicitBoolean();
        if(tipo == INDIANO)
        {
            corteTuboEsq.SetOperationTypeToIntersection();
            corteTuboEsq.AddFunction(tuboEsq);
            corteTuboEsq.AddFunction(corte);
        
            corteTuboDir.SetOperationTypeToIntersection();
            corteTuboDir.AddFunction(tuboDir);
            corteTuboDir.AddFunction(corte);
        }

        //----------------------------------------------------------------------
        
        //Amostra---------------------------------------------------------------
        
        //Solo
        vtkSampleFunction theSoloSample = new vtkSampleFunction();
        theSoloSample.SetImplicitFunction(solo);
        theSoloSample.SetModelBounds(-4.1, 4.1, 0, 2.3, -4.1, 4.1);
        theSoloSample.SetSampleDimensions(80, 80, 80);
        theSoloSample.ComputeNormalsOff();
        
        //Biomassa
        vtkSampleFunction theCilindroSample = new vtkSampleFunction();
        theCilindroSample.SetImplicitFunction(cilindroBiomassa);
        theCilindroSample.SetModelBounds(-1.1, 1.1, -1.3, 1.3, -1.1, 1.1);
        theCilindroSample.SetSampleDimensions(80, 80, 80);
        theCilindroSample.ComputeNormalsOff();
        
        //Parede
        vtkSampleFunction theParedeSample = new vtkSampleFunction();
        if(tipo == INDIANO)
        {
            theParedeSample.SetImplicitFunction(parede);
            theParedeSample.SetModelBounds(-1.1, 1.1, -1.3, 1.3, -1.1, 1.1);
            theParedeSample.SetSampleDimensions(80, 80, 80);
            theParedeSample.ComputeNormalsOff();
        }
        
        //Revestimento Gasômetro
        vtkSampleFunction theRevGasCilindroSample = new vtkSampleFunction();
        theRevGasCilindroSample.SetImplicitFunction(cilindroRevGas);
        theRevGasCilindroSample.SetModelBounds(-1.4, 1.4, 1.1, 2.5, -1.4, 1.4);
        theRevGasCilindroSample.SetSampleDimensions(80, 80, 80);
        theRevGasCilindroSample.ComputeNormalsOff();
        
        //Gasômetro
        vtkSampleFunction theGasCilindroSample = new vtkSampleFunction();
        theGasCilindroSample.SetImplicitFunction(cilindroGasometro);
        theGasCilindroSample.SetModelBounds(-1.25, 1.25, 1.1, 2.6, -1.25, 1.25);
        theGasCilindroSample.SetSampleDimensions(80, 80, 80);
        theGasCilindroSample.ComputeNormalsOff();
        
        //Tubo esquerdo e direito
        vtkSampleFunction theTuboEsqSample = new vtkSampleFunction();
        vtkSampleFunction theTuboDirSample = new vtkSampleFunction();
        
        if(tipo == INDIANO)
        {
            theTuboEsqSample.SetImplicitFunction(tuboEsq);
            theTuboEsqSample.SetModelBounds(-1.1 - mEsq, -0.7, -1.2 + 0.2 + 0.2, 2.15 + 0.7, -0.3, 0.3);
            theTuboEsqSample.SetSampleDimensions(80, 80, 80);
            theTuboEsqSample.ComputeNormalsOff();
            
            theTuboDirSample.SetImplicitFunction(tuboDir);
            theTuboDirSample.SetModelBounds(0.7, 1.1 + mDir, -1.2 + 0.2 + 0.2, 2.15 + 0.6, -0.3, 0.3);
            theTuboDirSample.SetSampleDimensions(80, 80, 80);
            theTuboDirSample.ComputeNormalsOff();
        }
        
        //----------------------------------------------------------------------
        
        //Contorno--------------------------------------------------------------
        
        //Solo
        vtkContourFilter theSoloSurface = new vtkContourFilter();
        theSoloSurface.SetInputConnection(theSoloSample.GetOutputPort());
        theSoloSurface.SetValue(0, 0.0);

        //Biomassa
        vtkContourFilter theCilindroSurface = new vtkContourFilter();
        theCilindroSurface.SetInputConnection(theCilindroSample.GetOutputPort());
        theCilindroSurface.SetValue(0, 0.0);
        
        //Parede
        vtkContourFilter theParedeSurface = new vtkContourFilter();
        theParedeSurface.SetInputConnection(theParedeSample.GetOutputPort());
        theParedeSurface.SetValue(0, 0.0);
        
        //Revestimento Gasômetro
        vtkContourFilter theRevGasCilindroSurface = new vtkContourFilter();
        theRevGasCilindroSurface.SetInputConnection(theRevGasCilindroSample.GetOutputPort());
        theRevGasCilindroSurface.SetValue(0, 0.0);
        
        //Gasômetro
        vtkContourFilter theGasCilindroSurface = new vtkContourFilter();
        theGasCilindroSurface.SetInputConnection(theGasCilindroSample.GetOutputPort());
        theGasCilindroSurface.SetValue(0, 0.0);
        
        //Tubo esquerdo e direito
        vtkContourFilter theTuboEsqSurface = new vtkContourFilter();
        vtkContourFilter theTuboDirSurface = new vtkContourFilter();
        
        if(tipo == INDIANO)
        {
            theTuboEsqSurface.SetInputConnection(theTuboEsqSample.GetOutputPort());
            theTuboEsqSurface.SetValue(0, 0.0);
            
            theTuboDirSurface.SetInputConnection(theTuboDirSample.GetOutputPort());
            theTuboDirSurface.SetValue(0, 0.0);
        }
        
        //----------------------------------------------------------------------
        
        //Polígonos-------------------------------------------------------------
       
        //Solo
        vtkPolyDataMapper soloMapper = new vtkPolyDataMapper();
        soloMapper.SetInputConnection(theSoloSurface.GetOutputPort());
        soloMapper.ScalarVisibilityOff();
        
        //Biomassa
        vtkPolyDataMapper cilindroMapper = new vtkPolyDataMapper();
        cilindroMapper.SetInputConnection(theCilindroSurface.GetOutputPort());
        cilindroMapper.ScalarVisibilityOff();
        
        //Parede
        vtkPolyDataMapper paredeMapper = new vtkPolyDataMapper();
        paredeMapper.SetInputConnection(theParedeSurface.GetOutputPort());
        paredeMapper.ScalarVisibilityOff();
        
        //Revestimento Gasômetro
        vtkPolyDataMapper revGasCilindroMapper = new vtkPolyDataMapper();
        revGasCilindroMapper.SetInputConnection(theRevGasCilindroSurface.GetOutputPort());
        revGasCilindroMapper.ScalarVisibilityOff();
        
        //Gasômetro
        vtkPolyDataMapper gasCilindroMapper = new vtkPolyDataMapper();
        gasCilindroMapper.SetInputConnection(theGasCilindroSurface.GetOutputPort());
        gasCilindroMapper.ScalarVisibilityOff();
        
        //Tubo esquerdo e direito
        vtkPolyDataMapper tuboEsqMapper = new vtkPolyDataMapper();
        vtkPolyDataMapper tuboDirMapper = new vtkPolyDataMapper();
        if(tipo == INDIANO)
        {
            tuboEsqMapper.SetInputConnection(theTuboEsqSurface.GetOutputPort());
            tuboEsqMapper.ScalarVisibilityOff();
            
            tuboDirMapper.SetInputConnection(theTuboDirSurface.GetOutputPort());
            tuboDirMapper.ScalarVisibilityOff();
        }
        
        //----------------------------------------------------------------------
        
        //Ator------------------------------------------------------------------
        double[] cor = new double[3];
        
        //Solo
        vtkActor soloActor = new vtkActor();
        soloActor.SetMapper(soloMapper);
        new vtkNamedColors().GetColorRGB("Chocolate", cor);
        soloActor.GetProperty().SetColor(cor);
        
        //Biomassa
        vtkActor cilindroActor = new vtkActor();
        cilindroActor.SetMapper(cilindroMapper);
        new vtkNamedColors().GetColorRGB("Snow", cor);
        cilindroActor.GetProperty().SetColor(cor);
        
        //Parede, tubo esquerdo e direito
        vtkActor paredeActor = new vtkActor();
        vtkActor tuboEsqActor = new vtkActor();
        vtkActor tuboDirActor = new vtkActor();
        
        if(tipo == INDIANO)
        {
            paredeActor.SetMapper(paredeMapper);
            paredeActor.GetProperty().SetColor(cor);
            
            tuboEsqActor.SetMapper(tuboEsqMapper);
            tuboEsqActor.GetProperty().SetColor(cor);
            
            tuboDirActor.SetMapper(tuboDirMapper);
            tuboDirActor.GetProperty().SetColor(cor);
        }
        
        
        //Revestimento Gasômetro
        vtkActor revGasCilindroActor = new vtkActor();
        revGasCilindroActor.SetMapper(revGasCilindroMapper);
        revGasCilindroActor.GetProperty().SetColor(cor);
        
        //Gasômetro
        vtkActor gasCilindroActor = new vtkActor();
        gasCilindroActor.SetMapper(gasCilindroMapper);
        new vtkNamedColors().GetColorRGB("Gray", cor);
        gasCilindroActor.GetProperty().SetColor(cor);
        
        //----------------------------------------------------------------------
        
        renWin = new vtkPanel();
        renWin.GetRenderer().AddActor(soloActor);
        renWin.GetRenderer().AddActor(cilindroActor);
        if(tipo == INDIANO)
        {
            renWin.GetRenderer().AddActor(paredeActor);
            renWin.GetRenderer().AddActor(tuboEsqActor);
            renWin.GetRenderer().AddActor(tuboDirActor);
        }
        renWin.GetRenderer().AddActor(revGasCilindroActor);
        renWin.GetRenderer().AddActor(gasCilindroActor);
        
        // Add Java UI components
        exitButton = new JButton("Corte");
        exitButton.addActionListener((e) -> {
            vtkImplicitFunction f = cortado ? cilindroBiomassa : corteBiomassaCilindro,
                                g = cortado ? cilindroRevGas : corteRevGasCilindro,
                                h = cortado ? cilindroGasometro : corteGasCilindro,
                                i = cortado ? parede : corteParede,
                                j = cortado ? solo : corteSolo,
                                k = cortado ? tuboEsq : corteTuboEsq,
                                l = cortado ? tuboDir : corteTuboDir;
            
            //Solo
            theSoloSample.SetImplicitFunction(j);
            theSoloSurface.SetInputConnection(theSoloSample.GetOutputPort());
            soloMapper.SetInputConnection(theSoloSurface.GetOutputPort());
            soloActor.SetMapper(soloMapper);

            //Biomassa
            theCilindroSample.SetImplicitFunction(f);
            theCilindroSurface.SetInputConnection(theCilindroSample.GetOutputPort());
            cilindroMapper.SetInputConnection(theCilindroSurface.GetOutputPort());
            cilindroActor.SetMapper(cilindroMapper);
            
            //Parede
            if(tipo == INDIANO)
            {
                theParedeSample.SetImplicitFunction(i);
                theParedeSurface.SetInputConnection(theParedeSample.GetOutputPort());
                paredeMapper.SetInputConnection(theParedeSurface.GetOutputPort());
                paredeActor.SetMapper(paredeMapper);
            }
            
            //Revestimento Gasômetro
            theRevGasCilindroSample.SetImplicitFunction(g);
            theRevGasCilindroSurface.SetInputConnection(theRevGasCilindroSample.GetOutputPort());
            revGasCilindroMapper.SetInputConnection(theRevGasCilindroSurface.GetOutputPort());
            revGasCilindroActor.SetMapper(revGasCilindroMapper);
            
            //Gasômetro
            theGasCilindroSample.SetImplicitFunction(h);
            theGasCilindroSurface.SetInputConnection(theGasCilindroSample.GetOutputPort());
            gasCilindroMapper.SetInputConnection(theGasCilindroSurface.GetOutputPort());
            gasCilindroActor.SetMapper(gasCilindroMapper);
            
            //Tubo esquerdo e direito
            if(tipo == INDIANO)
            {
                theTuboEsqSample.SetImplicitFunction(k);
                theTuboEsqSurface.SetInputConnection(theTuboEsqSample.GetOutputPort());
                tuboEsqMapper.SetInputConnection(theTuboEsqSurface.GetOutputPort());
                tuboEsqActor.SetMapper(tuboEsqMapper);
            
                theTuboDirSample.SetImplicitFunction(l);
                theTuboDirSurface.SetInputConnection(theTuboDirSample.GetOutputPort());
                tuboDirMapper.SetInputConnection(theTuboDirSurface.GetOutputPort());
                tuboDirActor.SetMapper(tuboDirMapper);
            }
            
            cortado = !cortado;
            renWin.repaint();
        });
 
        add(renWin, BorderLayout.CENTER);
        add(exitButton, BorderLayout.SOUTH);
    }
 
    public static void iniciar() {
        SwingUtilities.invokeLater( () ->
            {
                vtkNativeLibrary.LoadAllNativeLibraries();
                
                JFrame frame = new JFrame("SimpleVTK");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.getContentPane().setLayout(new BorderLayout());
                frame.getContentPane().add(new Modelo3D(), BorderLayout.CENTER);
                frame.setSize(800, 800);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        );
    }
}
