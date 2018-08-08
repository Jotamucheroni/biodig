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
 
    private vtkImplicitFunction geraCilindro(double[] centro, double[] eixo, double raio,
                                             double altura, double espParede, double espFundo,
                                             double espTopo)
    {
        //Cilindro externo
        vtkCylinder corpoCilindroExterno = new vtkCylinder();
        corpoCilindroExterno.SetCenter(centro);
        corpoCilindroExterno.SetAxis(eixo);
        corpoCilindroExterno.SetRadius(raio);
        
        vtkPlane tampaCilindroExterno = new vtkPlane(),
                 baseCilindroExterno = new vtkPlane();
        tampaCilindroExterno.SetNormal(eixo);
        baseCilindroExterno.SetNormal(-eixo[0], -eixo[1], -eixo[2]);
        double altura2 = altura/2;
        tampaCilindroExterno.SetOrigin(centro[0] + eixo[0] * altura2,
                                       centro[1] + eixo[1] * altura2,
                                       centro[2] + eixo[2] * altura2);
        baseCilindroExterno.SetOrigin(centro[0] - eixo[0] * altura2,
                                      centro[1] - eixo[1] * altura2,
                                      centro[2] - eixo[2] * altura2);
        
        vtkImplicitBoolean extCilindro = new vtkImplicitBoolean();
        extCilindro.SetOperationTypeToIntersection();
        extCilindro.AddFunction(corpoCilindroExterno);
        extCilindro.AddFunction(tampaCilindroExterno);
        extCilindro.AddFunction(baseCilindroExterno);
        
        //Cilindro interno
        vtkCylinder corpoCilindroInterno = new vtkCylinder();
        corpoCilindroInterno.SetCenter(centro);
        corpoCilindroInterno.SetAxis(eixo);
        corpoCilindroInterno.SetRadius(raio - espParede);
        
        vtkPlane tampaCilindroInterno = new vtkPlane(),
                 baseCilindroInterno = new vtkPlane();
        tampaCilindroInterno.SetNormal(eixo);
        baseCilindroInterno.SetNormal(-eixo[0], -eixo[1], -eixo[2]);
        tampaCilindroInterno.SetOrigin(centro[0] + eixo[0] * altura2 - espTopo,
                                       centro[1] + eixo[1] * altura2 - espTopo,
                                       centro[2] + eixo[2] * altura2 - espTopo);
        baseCilindroInterno.SetOrigin(centro[0] - eixo[0] * altura2 + espFundo,
                                      centro[1] - eixo[1] * altura2 + espFundo,
                                      centro[2] - eixo[2] * altura2 + espFundo);
        
        vtkImplicitBoolean intCilindro = new vtkImplicitBoolean();
        intCilindro.SetOperationTypeToIntersection();
        intCilindro.AddFunction(corpoCilindroInterno);
        intCilindro.AddFunction(tampaCilindroInterno);
        intCilindro.AddFunction(baseCilindroInterno);
        
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
        vtkImplicitFunction cilindroBiomassa = geraCilindro(new double[]{0, 0, 0}, new double[]{0, 1, 0},
                                              1, 2.4, 0.1, 0.2, 0);
        
        
        vtkCylinder cilindroAux = new vtkCylinder();
        cilindroAux.SetCenter(0, 0, 0);
        cilindroAux.SetRadius(0.9);
        
        vtkPlane baseAux = new vtkPlane();
        baseAux.SetOrigin(0, -1, 0);
        baseAux.SetNormal(0, -1, 0);
        
        vtkPlane tampaAux = new vtkPlane();
        tampaAux.SetOrigin(0, 1.2 + 0.2, 0);
        tampaAux.SetNormal(0, 1, 0);
        
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
        
        vtkImplicitBoolean intCilindro = new vtkImplicitBoolean();
        intCilindro.SetOperationTypeToIntersection();
        intCilindro.AddFunction(cilindroAux);
        intCilindro.AddFunction(baseAux);
        intCilindro.AddFunction(tampaAux);
        
        vtkImplicitBoolean corteCilindro = new vtkImplicitBoolean();
        corteCilindro.SetOperationTypeToIntersection();
        corteCilindro.AddFunction(cilindroBiomassa);
        corteCilindro.AddFunction(corte);
        
        //Revestimento Gasômetro
        
        vtkImplicitBoolean oCilindro2 = new vtkImplicitBoolean();
        oCilindro2.SetOperationTypeToDifference();
        oCilindro2.AddFunction(cilindroRevGasometro);
        oCilindro2.AddFunction(intCilindro);
        
        vtkImplicitBoolean corteRevGasCilindro = new vtkImplicitBoolean();
        corteRevGasCilindro.SetOperationTypeToIntersection();
        corteRevGasCilindro.AddFunction(oCilindro2);
        corteRevGasCilindro.AddFunction(corte);
        
        //Gasômetro     
        vtkImplicitBoolean corteGasCilindro = new vtkImplicitBoolean();
        corteGasCilindro.SetOperationTypeToIntersection();
        corteGasCilindro.AddFunction(cilindroGasometro);//oCilindro3);
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
        theRevGasCilindroSample.SetImplicitFunction(oCilindro2);
        theRevGasCilindroSample.SetModelBounds(-2.2, 2.7, -2.5, 2.5, -2.5, 2.5);
        theRevGasCilindroSample.SetSampleDimensions(100, 100, 100);
        theRevGasCilindroSample.ComputeNormalsOff();
        
        //Gasômetro
        vtkSampleFunction theGasCilindroSample = new vtkSampleFunction();
        theGasCilindroSample.SetImplicitFunction(cilindroGasometro); //oCilindro3);
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
            vtkImplicitFunction f = cortado ? cilindroBiomassa : corteCilindro,
                                g = cortado ? oCilindro2 : corteRevGasCilindro,
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
