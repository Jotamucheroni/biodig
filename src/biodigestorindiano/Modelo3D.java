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
 
    // -----------------------------------------------------------------
    public Modelo3D() {
        super(new BorderLayout());

        //Funções---------------------------------------------------------------
        
        //Biomassa
        vtkCylinder cilindro = new vtkCylinder();
        cilindro.SetCenter(0, 0, 0);
        cilindro.SetRadius(1);
        
        vtkCylinder cilindro2 = new vtkCylinder();
        cilindro2.SetCenter(0, 0, 0);
        cilindro2.SetRadius(0.9);
        
        vtkPlane base = new vtkPlane();
        base.SetOrigin(0, -1.2, 0);
        base.SetNormal(0, -1, 0);
        
        vtkPlane base2 = new vtkPlane();
        base2.SetOrigin(0, -1, 0);
        base2.SetNormal(0, -1, 0);
        
        vtkPlane tampa = new vtkPlane();
        tampa.SetOrigin(0, 1.2, 0);
        tampa.SetNormal(0, 1, 0);
        
        vtkPlane tampa2 = new vtkPlane();
        tampa2.SetOrigin(0, 1.2 + 0.2, 0);
        tampa2.SetNormal(0, 1, 0);
        
        vtkPlane corte = new vtkPlane();
        corte.SetOrigin(0, 0, 0);
        corte.SetNormal(0, 0, 1);
        
        //Revestimento Gasômetro
        vtkCylinder cilindro3 = new vtkCylinder();
        cilindro3.SetCenter(0, 0, 0);
        cilindro3.SetRadius(1.3);
        
        vtkCylinder cilindro4 = new vtkCylinder();
        cilindro4.SetCenter(0, 0, 0);
        cilindro4.SetRadius(1.2);
        
        vtkPlane base3 = new vtkPlane();
        base3.SetOrigin(0, 1.2, 0);
        base3.SetNormal(0, -1, 0);
        
        vtkPlane base4 = new vtkPlane();
        base4.SetOrigin(0, 1.3, 0);
        base4.SetNormal(0, -1, 0);
        
        vtkPlane tampa3 = new vtkPlane();
        tampa3.SetOrigin(0, 2.4, 0);
        tampa3.SetNormal(0, 1, 0);
        
        vtkPlane tampa4 = new vtkPlane();
        tampa4.SetOrigin(0, 2.4, 0);
        tampa4.SetNormal(0, 1, 0);
        
        //----------------------------------------------------------------------
        
        //Operações-------------------------------------------------------------
        
        //Biomassa
        vtkImplicitBoolean extCilindro = new vtkImplicitBoolean();
        extCilindro.SetOperationTypeToIntersection();
        extCilindro.AddFunction(cilindro);
        extCilindro.AddFunction(base);
        extCilindro.AddFunction(tampa);
        
        vtkImplicitBoolean intCilindro = new vtkImplicitBoolean();
        intCilindro.SetOperationTypeToIntersection();
        intCilindro.AddFunction(cilindro2);
        intCilindro.AddFunction(base2);
        intCilindro.AddFunction(tampa2);
        
        vtkImplicitBoolean oCilindro = new vtkImplicitBoolean();
        oCilindro.SetOperationTypeToDifference();
        oCilindro.AddFunction(extCilindro);
        oCilindro.AddFunction(intCilindro);
        
        vtkImplicitBoolean corteCilindro = new vtkImplicitBoolean();
        corteCilindro.SetOperationTypeToIntersection();
        corteCilindro.AddFunction(oCilindro);
        corteCilindro.AddFunction(corte);
        
        //Revestimento Gasômetro
        vtkImplicitBoolean extCilindro2 = new vtkImplicitBoolean();
        extCilindro2.SetOperationTypeToIntersection();
        extCilindro2.AddFunction(cilindro3);
        extCilindro2.AddFunction(base3);
        extCilindro2.AddFunction(tampa3);
        
        vtkImplicitBoolean intCilindro2 = new vtkImplicitBoolean();
        intCilindro2.SetOperationTypeToIntersection();
        intCilindro2.AddFunction(cilindro4);
        intCilindro2.AddFunction(base4);
        intCilindro2.AddFunction(tampa4);
        
        vtkImplicitBoolean oCilindro2 = new vtkImplicitBoolean();
        oCilindro2.SetOperationTypeToDifference();
        oCilindro2.AddFunction(extCilindro2);
        oCilindro2.AddFunction(intCilindro);
        oCilindro2.AddFunction(intCilindro2);
        
        vtkImplicitBoolean corteRevGasCilindro = new vtkImplicitBoolean();
        corteRevGasCilindro.SetOperationTypeToIntersection();
        corteRevGasCilindro.AddFunction(oCilindro2);
        corteRevGasCilindro.AddFunction(corte);
        
        //----------------------------------------------------------------------
        
        //Amostra---------------------------------------------------------------
        
        //Biomassa
        vtkSampleFunction theCilindroSample = new vtkSampleFunction();
        theCilindroSample.SetImplicitFunction(oCilindro);
        theCilindroSample.SetModelBounds(-1, 1.5, -1.25, 1.25, -1.25, 1.25);
        theCilindroSample.SetSampleDimensions(100, 100, 100);
        theCilindroSample.ComputeNormalsOff();
        
        //Revestimento Gasômetro
        vtkSampleFunction theRevGasCilindroSample = new vtkSampleFunction();
        theRevGasCilindroSample.SetImplicitFunction(oCilindro2);
        theRevGasCilindroSample.SetModelBounds(-2.2, 2.7, -2.5, 2.5, -2.5, 2.5);
        theRevGasCilindroSample.SetSampleDimensions(100, 100, 100);
        theRevGasCilindroSample.ComputeNormalsOff();
        
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
        
        //----------------------------------------------------------------------
        
        renWin = new vtkPanel();
        renWin.GetRenderer().AddActor(cilindroActor);
        renWin.GetRenderer().AddActor(revGasCilindroActor);
        
        // Add Java UI components
        exitButton = new JButton("Corte");
        exitButton.addActionListener( (e) -> {
            vtkImplicitFunction f = cortado ? oCilindro : corteCilindro,
                                g = cortado ? oCilindro2 : corteRevGasCilindro;
            
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
