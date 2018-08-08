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
    private boolean cortado = false;
    
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
        
        //Biomassa
        vtkImplicitFunction cilindroBiomassa = geraCilindro(new double[]{0, 0, 0}, 
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
        
        //----------------------------------------------------------------------
        
        //Operações-------------------------------------------------------------
        
        //Biomassa       
        vtkImplicitBoolean corteBiomassaCilindro = new vtkImplicitBoolean();
        corteBiomassaCilindro.SetOperationTypeToIntersection();
        corteBiomassaCilindro.AddFunction(cilindroBiomassa);
        corteBiomassaCilindro.AddFunction(corte);
        
        //Revestimento Gasômetro
        vtkImplicitBoolean cilindroRevGas = new vtkImplicitBoolean();
        cilindroRevGas.SetOperationTypeToDifference();
        cilindroRevGas.AddFunction(cilindroRevGasometro);
        cilindroRevGas.AddFunction(cilindroAux); //intCilindro);
        
        vtkImplicitBoolean corteRevGasCilindro = new vtkImplicitBoolean();
        corteRevGasCilindro.SetOperationTypeToIntersection();
        corteRevGasCilindro.AddFunction(cilindroRevGas);
        corteRevGasCilindro.AddFunction(corte);
        
        //Gasômetro     
        vtkImplicitBoolean corteGasCilindro = new vtkImplicitBoolean();
        corteGasCilindro.SetOperationTypeToIntersection();
        corteGasCilindro.AddFunction(cilindroGasometro);
        corteGasCilindro.AddFunction(corte);
        
        //----------------------------------------------------------------------
        
        //Amostra---------------------------------------------------------------
        
        //Biomassa
        vtkSampleFunction theCilindroSample = new vtkSampleFunction();
        theCilindroSample.SetImplicitFunction(cilindroBiomassa);
        theCilindroSample.SetModelBounds(-1, 1.5, -1.25, 1.25, -1.25, 1.25);
        theCilindroSample.SetSampleDimensions(100, 100, 100);
        theCilindroSample.ComputeNormalsOff();
        
        //Revestimento Gasômetro
        vtkSampleFunction theRevGasCilindroSample = new vtkSampleFunction();
        theRevGasCilindroSample.SetImplicitFunction(cilindroRevGas);
        theRevGasCilindroSample.SetModelBounds(-2.2, 2.7, -2.5, 2.5, -2.5, 2.5);
        theRevGasCilindroSample.SetSampleDimensions(100, 100, 100);
        theRevGasCilindroSample.ComputeNormalsOff();
        
        //Gasômetro
        vtkSampleFunction theGasCilindroSample = new vtkSampleFunction();
        theGasCilindroSample.SetImplicitFunction(cilindroGasometro);
        theGasCilindroSample.SetModelBounds(-2.3, 2.8, -2.6, 2.6, -2.6, 2.6);
        theGasCilindroSample.SetSampleDimensions(100, 100, 100);
        theGasCilindroSample.ComputeNormalsOff();
        
        //----------------------------------------------------------------------
        
        //Contorno--------------------------------------------------------------
        
        //Biomassa
        vtkContourFilter theCilindroSurface = new vtkContourFilter();
        theCilindroSurface.SetInputConnection(theCilindroSample.GetOutputPort());
        theCilindroSurface.SetValue(0, 0.0);
        
        //Revestimento Gasômetro
        vtkContourFilter theRevGasCilindroSurface = new vtkContourFilter();
        theRevGasCilindroSurface.SetInputConnection(theRevGasCilindroSample.GetOutputPort());
        theRevGasCilindroSurface.SetValue(0, 0.0);
        
        //Gasômetro
        vtkContourFilter theGasCilindroSurface = new vtkContourFilter();
        theGasCilindroSurface.SetInputConnection(theGasCilindroSample.GetOutputPort());
        theGasCilindroSurface.SetValue(0, 0.0);
        
        //----------------------------------------------------------------------
        
        //Polígonos-------------------------------------------------------------
       
        //Biomassa
        vtkPolyDataMapper cilindroMapper = new vtkPolyDataMapper();
        cilindroMapper.SetInputConnection(theCilindroSurface.GetOutputPort());
        cilindroMapper.ScalarVisibilityOff();
        
        //Revestimento Gasômetro
        vtkPolyDataMapper revGasCilindroMapper = new vtkPolyDataMapper();
        revGasCilindroMapper.SetInputConnection(theRevGasCilindroSurface.GetOutputPort());
        revGasCilindroMapper.ScalarVisibilityOff();
        
        //Gasômetro
        vtkPolyDataMapper gasCilindroMapper = new vtkPolyDataMapper();
        gasCilindroMapper.SetInputConnection(theGasCilindroSurface.GetOutputPort());
        gasCilindroMapper.ScalarVisibilityOff();
        
        //----------------------------------------------------------------------
        
        //Ator------------------------------------------------------------------
        double[] cor = new double[3];
        
        //Biomassa
        vtkActor cilindroActor = new vtkActor();
        cilindroActor.SetMapper(cilindroMapper);
        new vtkNamedColors().GetColorRGB("Snow", cor);
        cilindroActor.GetProperty().SetColor(cor);
        
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
        renWin.GetRenderer().AddActor(cilindroActor);
        renWin.GetRenderer().AddActor(revGasCilindroActor);
        renWin.GetRenderer().AddActor(gasCilindroActor);
        
        // Add Java UI components
        exitButton = new JButton("Corte");
        exitButton.addActionListener((e) -> {
            vtkImplicitFunction f = cortado ? cilindroBiomassa : corteBiomassaCilindro,
                                g = cortado ? cilindroRevGas : corteRevGasCilindro,
                                h = cortado ? cilindroGasometro : corteGasCilindro;
            
            //Biomassa
            theCilindroSample.SetImplicitFunction(f);
            theCilindroSurface.SetInputConnection(theCilindroSample.GetOutputPort());
            cilindroMapper.SetInputConnection(theCilindroSurface.GetOutputPort());
            cilindroActor.SetMapper(cilindroMapper);
            
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
