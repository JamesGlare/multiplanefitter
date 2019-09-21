package gui;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.Roi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.ImageIcon ;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.border.Border;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import lib.Comparer;
import lib.FindLocalMaxima;
import lib.GaussianFit;
import lib.Point3D;
import lib.Sample;
import lib.Utils;

import processing.Gaussian3DSpot;
import processing.SpotMother;
/**************************************
 * MainWindow Class
 * @author james
 * This class is the mainframe of the 
 * program. 
 **************************************/
public class MainWindow extends JFrame  implements PropertyChangeListener{

	/*
	 * Private Members
	 */
	private final JTabbedPane tabbedPane ;
	private final JPanel panel;
	private final JPanel panel1 ;
	private final JPanel panel2 ;
	
	private final Border buttonBorder ;
	
	private final MainTab mainTab ;
	private final ZCalibrationTab zCalibTab ;
	private final IntensityCorrectionTab intensityTab ;
	private final LogTab logTab ;
	private final JLabel mainTabLabel;
	private final JLabel zCalibLabel ;
	private final JLabel intensityTabLabel ;
	private final JLabel logTabLabel ;

	/* Panel2 Stuff*/ 
	final private JButton okButton ;
	final private JButton closeButton ;

	final private JProgressBar progressBar;
	final private JLabel progressLabel ;
	/* internal stuff */
	private SwingWorker<Void, Integer> currentSwingWorker;
	
	/*
	 * Constructor
	 * Creates the MainWindow displayed by the plugin.
	 * All Tabs displayed are self-contained classes of the 
	 * gui package. All data transfer from the gui to the main
	 * class MultiPlaneFitter_ is handled and managed by this MainWindow class
	 * (I know it's not the right way to do) through its internally stored
	 * references to the tab class instances.
	 * @see gui.MainTab
	 * @see gui.ZCalibrationTab
	 * @see gui.IntensityCorrectionTab
	 */
	public MainWindow()  throws HeadlessException {
		super("MultiPlaneFitter");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.currentSwingWorker = null ;
		
		/* Create all Instances */
		this.tabbedPane = new JTabbedPane() ;
		this.panel = new JPanel() ;
		this.panel1 = new JPanel() ;
		this.panel2 = new JPanel() ;
		this.buttonBorder = BorderFactory.createTitledBorder("Control") ;
		
		this.okButton = new JButton("Go") ;
		this.closeButton = new JButton("Cancel") ;

		// steal some icon
		this.progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0) ;
		progressBar.setStringPainted(true);

		this.progressLabel = new JLabel("Progress: ") ;
		
		/* Create Tabs */
		this.mainTab = new MainTab( this ) ;
		this.zCalibTab = new ZCalibrationTab(this);
		this.intensityTab = new IntensityCorrectionTab(this) ;
		this.logTab = new LogTab(this) ;
		this.mainTabLabel = new JLabel("General");
		this.zCalibLabel = new JLabel("Bead Calibration") ;
		this.intensityTabLabel = new JLabel("Intensity Adjustment") ;
		this.logTabLabel = new JLabel("Log") ;
		
		/* Add the instances to the frame */
		this.getContentPane().add(panel) ;
		this.panel.add(panel1) ;
		this.panel.add(panel2) ;
		this.panel1.add( tabbedPane ) ;
		//this.panel2.add(Box.createRigidArea(new Dimension(15,0)));
		this.panel2.add(okButton) ;
		this.panel2.add(Box.createRigidArea(new Dimension(15,0))); // spacing
		this.panel2.add(closeButton);
		this.panel2.add(Box.createRigidArea(new Dimension(40,0))); // spacing
		this.panel2.add(progressLabel ) ;
		this.panel2.add(Box.createRigidArea(new Dimension(10,0))); // spacing
		this.panel2.add(progressBar);
		
		/* Set Layouts & Borders*/
		this.panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS)) ;
		this.panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)) ;
		this.panel2.setBorder(buttonBorder) ;
		
		/* Set preferred Sizes etc. */
		//this.panel2.setPreferredSize(this.panel2.getMaximumSize()) ;
		//this.panel1.setPreferredSize(this.panel1.getMaximumSize()) ;
		//this.intensityTabLabel.setForeground(Color.red) ;
		//this.zCalibLabel.setForeground(Color.red) ;
		
		/* Add Tabs to pane */ 
		this.tabbedPane.addTab(null,null, mainTab, "General Information") ;
		this.tabbedPane.addTab(null,null, zCalibTab, "General Information") ;
		this.tabbedPane.addTab(null,null, intensityTab, "General Information") ;
		this.tabbedPane.addTab(null,null, logTab, "Log") ;
		this.tabbedPane.setTabComponentAt(0, this.mainTabLabel) ;
		this.tabbedPane.setTabComponentAt(1, this.zCalibLabel) ;
		this.tabbedPane.setTabComponentAt(2, this.intensityTabLabel) ;
		this.tabbedPane.setTabComponentAt(3, this.logTabLabel) ;

		
		/* Add Listeners */
		this.closeButton.addActionListener(new CloseListener(this)) ;
		this.okButton.addActionListener(new GoListener(this)) ;
		this.okButton.setEnabled(false) ;
		/*Pack & Go*/
		this.pack() ;
		this.setVisible( true ) ;
		
		/* Initialize internval variables */
		
	}
	public void setGo(boolean go){
		if(go){
			okButton.setEnabled(true);
		} else {
			okButton.setEnabled(false) ;
		}
	}
	/*
	 * Get a tab instance. (I know private blabla)
	 */
	public JComponent getTab(String identifier){
		if(identifier == "mainTab") return this.mainTab;
		else if(identifier == "zCalibrationTab") return this.zCalibTab;
		else if(identifier == "intensityTab") return this.intensityTab;
		else if(identifier == "logTab") return this.logTab ;
		else return null;
	}
	public void updateProgress(int percent){
		this.progressBar.setValue(percent) ;
	}
	/*
	 * Setter Method for the main Image
	 */


	protected class CloseListener implements ActionListener{
		private JFrame parent ;
		public CloseListener(JFrame parent){
			this.parent = parent ;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			if( currentSwingWorker != null && (!currentSwingWorker.isDone() || !currentSwingWorker.isCancelled())){
				while(!currentSwingWorker.cancel(true)) ;
				unsetCurrentWorker();
				updateProgress(0) ;
			} else{
				
				parent.dispose() ;
			}
		}
		
	}
	/*
	 * Do the localization on the whole stack.
	 */
	public void doLocalization() throws NumberFormatException, Exception{
		int a = mainTab.getBoxWidth();
		int p = mainTab.getZBoxWidth();
		int aInt = mainTab.getFitBoxWidth();
		int pInt = mainTab.getZFitBoxWidth() ;
		int noiseThreshold = mainTab.getNoiseThreshold() ;
		int pixelSize = mainTab.getPixelSize() ;
		double dZ = mainTab.getDz() ;
		int ruleNumber = mainTab.getRuleNumber() ;
		ImagePlus mainImage = mainTab.getMainImage() ;
		boolean symmetric = mainTab.getSymmetric() ;
		int maxIterations = mainTab.getMaxIterations();
		 // we utilize the physical prediction of the standard deviation
		// but in units of pixel in order to avoid big numbers
		// the physical scale is later included during the writing process 
		GaussianFit.setPhysicalParameters(mainTab.getWavelength(), mainTab.getNA(), mainTab.getDiffIndex()) ;
		double[] sigmaEstimate = GaussianFit.estimateSigmaPhysics();
		sigmaEstimate[0] /= pixelSize ;
		sigmaEstimate[1] /= pixelSize ;
		sigmaEstimate[2] /= dZ;
		
		GaussianFit.changeSigmaEstimate(sigmaEstimate) ;// use the physical parameters as initial guess!
		GaussianFit.setMaxIterations(maxIterations);
		SpotMother spm = new SpotMother(mainImage,a,p, aInt, pInt, noiseThreshold, ruleNumber) ;
		
		setCurrentWorker(spm) ; // register the worker, so that it can be canceled.
		
		// set parameters that are relevant for the final run...
		// get the spot comparer out of the filterdialog 
		Comparer comparer = null;
		if( mainTab.getIfParameterFilter()){
			comparer =mainTab.getFilterDialog().getComparer();
		} 
		spm.setParams(mainTab.getOutFileName(), pixelSize, dZ, mainTab.getIfFilter(), mainTab.getIfParameterFilter(), this,comparer, symmetric) ;
		spm.addPropertyChangeListener(this) ; // make the progressbar listening to the spotmother
		this.setGo(false) ; // disable go button.
		spm.execute(); // GO!
	}

	/*
	 * The Actionlistener for the Go-Button.
	 * Here, the whole procedure starts.
	 */
	protected class GoListener implements ActionListener{
			MainWindow parent;
			
			public GoListener(MainWindow parent_){
				parent = parent_ ;
			}
		
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if( mainTab.getMainImage() == null){
				IJ.showMessage("Please set Imagefile, which you want to search through.");
				setGo(false) ;
				return ;
			}
			// (1) Do iT! 
			try{
				parent.doLocalization() ;
			
			} catch(NumberFormatException exc){
				IJ.showMessage("Please enter valid number string.\n"+exc.getLocalizedMessage()) ;
			} catch (Exception exk) {
				// TODO Auto-generated catch block
				IJ.showMessage("An error occurred!") ;
			}
			
		}
		
	}
	public void setCurrentWorker(SwingWorker<Void, Integer> sW ){
		this.currentSwingWorker = sW ;
	}
	public void unsetCurrentWorker(){
		this.currentSwingWorker = null ;
	}
	public void propertyChange(PropertyChangeEvent evt) {
	    if ("progress" == evt.getPropertyName()) {
	      int progress = (Integer) evt.getNewValue();
	      	this.updateProgress(progress) ;
	    }
	}

}
