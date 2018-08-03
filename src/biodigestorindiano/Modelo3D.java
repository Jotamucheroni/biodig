package biodigestorindiano;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import vtk.vtkNativeLibrary;
import vtk.vtkCylinder;
import vtk.vtkCone;
import vtk.vtkPlane;
import vtk.vtkSphere;
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
        vtkSphere bola1 = new vtkSphere();
        bola1.SetCenter(0, 0, 0);
        bola1.SetRadius(1.1);
        vtkSphere bola2 = new vtkSphere();
        bola2.SetCenter(0, 0, 0);
        bola2.SetRadius(0.2);
        vtkPlane base = new vtkPlane();
        base.SetOrigin(0, -1.2, 0);
        base.SetNormal(0, -1, 0);
        vtkPlane base2 = new vtkPlane();
        base2.SetOrigin(0, -1, 0);
        base2.SetNormal(0, -1, 0);
        vtkPlane tampa = new vtkPlane();
        tampa.SetOrigin(0, 1.2, 0);
        tampa.SetNormal(0, 1, 0);
        
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
        intCilindro.AddFunction(tampa);
        vtkImplicitBoolean oCilindro = new vtkImplicitBoolean();
        oCilindro.SetOperationTypeToDifference();
        oCilindro.AddFunction(extCilindro);
        oCilindro.AddFunction(intCilindro);
        
        //----------------------------------------------------------------------
        
        //Amostra---------------------------------------------------------------
        
        //Cilindro
        vtkSampleFunction theCilindroSample = new vtkSampleFunction();
        theCilindroSample.SetImplicitFunction(oCilindro);
        theCilindroSample.SetModelBounds(-1, 1.5, -1.25, 1.25, -1.25, 1.25);
        theCilindroSample.SetSampleDimensions(60, 60, 60);
        theCilindroSample.ComputeNormalsOff();
        
        //----------------------------------------------------------------------
        
        //Contorno--------------------------------------------------------------
        
        //Cilindro
        vtkContourFilter theCilindroSurface = new vtkContourFilter();
        theCilindroSurface.SetInputConnection(theCilindroSample.GetOutputPort());
        theCilindroSurface.SetValue(0, 0.0);
        
        //----------------------------------------------------------------------
        
        //Polígonos-------------------------------------------------------------
       
        //Cilindro
        vtkPolyDataMapper cilindroMapper = new vtkPolyDataMapper();
        cilindroMapper.SetInputConnection(theCilindroSurface.GetOutputPort());
        cilindroMapper.ScalarVisibilityOff();
        
        //----------------------------------------------------------------------
        
        //Ator------------------------------------------------------------------
        double[] cor = new double[3];
        
        //Cilindro
        vtkActor cilindroActor = new vtkActor();
        cilindroActor.SetMapper(cilindroMapper);
        new vtkNamedColors().GetColorRGB("Snow", cor);
        cilindroActor.GetProperty().SetColor(cor);
        
        //----------------------------------------------------------------------
        
        renWin = new vtkPanel();
        renWin.GetRenderer().AddActor(cilindroActor);
        
        // Add Java UI components
        exitButton = new JButton("Corte");
        //exitButton.addActionListener( (e) -> System.exit(0));
 
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
