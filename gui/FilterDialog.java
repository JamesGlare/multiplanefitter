package gui;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.Roi;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.jfree.data.xy.XYSeries;

import processing.Gaussian3DSpot;
import processing.Spot;
import processing.SpotMother;

import lib.Comparer;
import lib.GaussianFit;
import lib.GaussianUtils;
import lib.Point3D;
import lib.Utils;

public class FilterDialog extends JDialog {

	/*
	 * Private members
	 */
	private final JLabel intensityLabel;
	private final JLabel sigmaXLabel;
	private final JLabel sigmaYLabel;
	private final JLabel sigmaZLabel;
	private final JLabel xLabel;
	private final JLabel yLabel;
	private final JLabel zLabel ;
	private final JLabel backgroundLabel;
	
	private final JFormattedTextField intensityMinField;
	private final JFormattedTextField sigmaXMinField;
	private final JFormattedTextField sigmaYMinField;
	private final JFormattedTextField sigmaZMinField;
	private final JFormattedTextField xMinField;
	private final JFormattedTextField yMinField;
	private final JFormattedTextField zMinField;
	private final JFormattedTextField backgroundMinField;
	
	private final JFormattedTextField intensityMaxField;
	private final JFormattedTextField sigmaXMaxField;
	private final JFormattedTextField sigmaYMaxField;
	private final JFormattedTextField sigmaZMaxField;
	private final JFormattedTextField xMaxField;
	private final JFormattedTextField yMaxField;
	private final JFormattedTextField zMaxField;
	private final JFormattedTextField backgroundMaxField;
	
	
	private final Border filterBorder;
	private final JButton closeButton;
	private final JButton simulateButton ;
	private final JButton valueRangeButton ;

	private JPanel panel;
	/*
	 * Internal stuff
	 */
	private MainWindow parent ;
	
	/*
	 * Constructor
	 */
	public FilterDialog(MainWindow parent){
		super( parent) ; // mother class
		
		this.parent = parent ;
		
		intensityLabel = new JLabel("Intensity");
		sigmaXLabel = new JLabel("Sigma X");
		sigmaYLabel = new JLabel("Sigma Y");
		sigmaZLabel = new JLabel("Sigma Z");
		
		xLabel= new JLabel("X");
		yLabel = new JLabel("Y");
		zLabel = new JLabel("Z");
		backgroundLabel = new JLabel("Background");
		
		intensityMinField = new JFormattedTextField("");
		intensityMinField.setColumns(7);
		sigmaXMinField = new JFormattedTextField();
		sigmaXMinField.setColumns(7);
		sigmaYMinField = new JFormattedTextField();
		sigmaYMinField.setColumns(7);
		sigmaZMinField = new JFormattedTextField();
		sigmaZMinField.setColumns(7);
		xMinField = new JFormattedTextField();
		xMinField.setColumns(7);
		yMinField = new JFormattedTextField();
		yMinField.setColumns(7);
		zMinField = new JFormattedTextField();
		zMinField.setColumns(7);
		backgroundMinField = new JFormattedTextField();
		backgroundMinField.setColumns(7);
		
		intensityMaxField = new JFormattedTextField("");
		intensityMaxField.setColumns(7);
		sigmaXMaxField = new JFormattedTextField();
		sigmaXMaxField.setColumns(7) ;
		sigmaYMaxField = new JFormattedTextField();
		sigmaYMaxField.setColumns(7);
		sigmaZMaxField = new JFormattedTextField();
		sigmaZMaxField.setColumns(7);
		xMaxField = new JFormattedTextField();
		xMaxField.setColumns(7);
		yMaxField = new JFormattedTextField();
		yMaxField.setColumns(7);
		zMaxField = new JFormattedTextField();
		zMaxField.setColumns(7);
		backgroundMaxField = new JFormattedTextField();
		backgroundMaxField.setColumns(7);
		
		filterBorder =  BorderFactory.createTitledBorder("Enter admissible Gaussian Parameter Ranges") ;
		closeButton = new JButton("Close");
		simulateButton = new JButton("Test on Time Frame") ;
		simulateButton.setEnabled(false) ;
		valueRangeButton = new JButton("Get Range");
		
		panel = new JPanel() ;
		
		this.setTitle("Admissible Gaussian Parameter Ranges") ;
		// layout
		this.add(panel) ;
		GridBagLayout layout = new GridBagLayout( ) ; 
		GridBagConstraints c = new GridBagConstraints() ;
		panel.setLayout(layout);
		panel.setBorder(filterBorder ) ;
		
		// Add all the widgets
		c.gridx=0;
		c.gridy=0;
		
		// new line
		panel.add(intensityLabel,c);
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(intensityMinField,c) ;
		c.gridx++;
		panel.add(new JLabel("to "),c) ;
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(intensityMaxField,c) ;
		
		c.gridy++;
		panel.add(Box.createVerticalStrut(20));
		c.gridy++;
		
		c.gridy++;
		c.gridx=0;
		//new line
		panel.add(sigmaXLabel,c);
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(sigmaXMinField,c) ;
		c.gridx++;
		panel.add(new JLabel("to "),c) ;
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(sigmaXMaxField,c) ;
		
		c.gridy++;
		c.gridx=0;
		//newline
		panel.add(sigmaYLabel,c);
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(sigmaYMinField,c) ;
		c.gridx++;
		panel.add(new JLabel("to "),c) ;
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(sigmaYMaxField,c) ;
		
		c.gridy++;
		c.gridx=0;
		//newline
		panel.add(sigmaZLabel,c);
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(sigmaZMinField,c) ;
		c.gridx++;
		panel.add(new JLabel("to "),c) ;
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(sigmaZMaxField,c) ;
		
		c.gridy++;
		panel.add(Box.createVerticalStrut(20));
		c.gridy++;
		
		c.gridy++;
		c.gridx=0;		
		//newline
		panel.add(xLabel,c);
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(xMinField,c) ;
		c.gridx++;
		panel.add(new JLabel("to "),c) ;
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(xMaxField,c) ;
				
		c.gridy++;
		c.gridx=0;
		//newline
		panel.add(yLabel,c);
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(yMinField,c) ;
		c.gridx++;
		panel.add(new JLabel("to "),c) ;
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(yMaxField,c) ;
				
		c.gridy++;
		c.gridx=0;
		//newline
		panel.add(zLabel,c);
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(zMinField,c) ;
		c.gridx++;
		panel.add(new JLabel("to "),c) ;
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(zMaxField,c) ;
		
		c.gridy++;
		panel.add(Box.createVerticalStrut(20));
		c.gridy++;
		
		
		c.gridy++;
		c.gridx=0;
		//newline
		panel.add(backgroundLabel,c);
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(backgroundMinField,c) ;
		c.gridx++;
		panel.add(new JLabel("to "),c) ;
		c.gridx++;
		panel.add(Box.createHorizontalStrut(5));
		c.gridx++;
		panel.add(backgroundMaxField,c) ;
		
		c.gridy++;
		panel.add(Box.createVerticalStrut(20),c) ;
		c.gridy++;
		c.gridx=0;
		panel.add(valueRangeButton,c);
		c.gridx+=2;
		panel.add(simulateButton,c);
		c.gridx+=3;
		panel.add(closeButton,c) ;
		/*
		 * Add Listeners
		 */
		valueRangeButton.addActionListener(new DeduceRangeListener()) ;
		simulateButton.addActionListener(new TestOnFrameListener()) ;
		closeButton.addActionListener(new CloseListener()) ;
	}
	private ArrayList<Spot> extractFromFrame(){
		MainTab mainTab = (MainTab) parent.getTab("mainTab");
		ImagePlus imp = mainTab.getMainImage();
		
		int maxIterations = mainTab.getMaxIterations();
		GaussianFit.setMaxIterations(maxIterations) ;

		// (0) User correction...
		if(imp == null){
			IJ.showMessage("Please choose the image in which you want to do the localization in!") ;
			return new ArrayList<Spot>();
		}
		// (1) get The stack around the current image
		
		ImagePlus subStack= Utils.extractFrame(imp, imp.getT());
		subStack.setPosition(1, imp.getSlice(), 1) ;
		
		//(2) Localization and fitting.
		int a = mainTab.getBoxWidth();
		int p = mainTab.getZBoxWidth();
		int aInt = mainTab.getFitBoxWidth();
		int pInt = mainTab.getZFitBoxWidth() ;
		int noiseThreshold = mainTab.getNoiseThreshold() ;
		
		int pixelSize = mainTab.getPixelSize();
		double dZ = mainTab.getDz();
		int ruleNumber = mainTab.getRuleNumber() ;
		boolean applyFilter = mainTab.getIfFilter() ;
		boolean symmetric = mainTab.getSymmetric();
		Comparer comparer = null;
		
		 // we utilize the physical prediction of the standard deviation
		// but in units of pixel in order to avoid big numbers
		// the physical scale is later included during the writing process 
		GaussianFit.setPhysicalParameters(mainTab.getWavelength(), mainTab.getNA(), mainTab.getDiffIndex()) ;
		double[] sigmaEstimate = GaussianFit.estimateSigmaPhysics();
		sigmaEstimate[0] = sigmaEstimate[0]/pixelSize ;
		sigmaEstimate[1] = sigmaEstimate[1]/pixelSize ;
		sigmaEstimate[2] = sigmaEstimate[2]/dZ;
		
		GaussianFit.changeSigmaEstimate(sigmaEstimate) ;// use the physical parameters as initial guess!
		
		SpotMother spm =  new SpotMother(imp,a,p, aInt, pInt, noiseThreshold, ruleNumber) ;
		spm.setParams("", pixelSize, dZ, applyFilter, false, null, comparer, symmetric);
		
		ArrayList<Spot> list = new ArrayList<Spot>() ;
		// do the stepping
		spm.step(subStack, list, imp.getT()) ;
		return list ;
	}
	private void deduceRange(){
		
		MainTab mainTab = (MainTab) parent.getTab("mainTab");
		ArrayList<Spot> list = this.extractFromFrame() ;
		int pixelSize = mainTab.getPixelSize() ;
		double dZ = mainTab.getDz() ;
		/*
		 * (3) Now extract the ranges...
		 */
		double minIntensity = Double.MAX_VALUE;
		double maxIntensity =0;
		double minSigmaX = Double.MAX_VALUE;
		double maxSigmaX = 0;
		double minSigmaY = Double.MAX_VALUE;
		double maxSigmaY = 0;
		double minSigmaZ = Double.MAX_VALUE;
		double maxSigmaZ = 0;
		double minX =Double.MAX_VALUE;
		double maxX = 0;
		double minY =Double.MAX_VALUE;
		double maxY = 0;
		double minZ =Double.MAX_VALUE;
		double maxZ = 0;
		double minBackground = Double.MAX_VALUE ;
		double maxBackground = 0 ;
		
		
		XYSeries intensityPlot = new XYSeries("Intensity Distribution"); // plot the distribution	
		XYSeries[] sigmaPlots = new XYSeries[3]; // plot the distribution	
		sigmaPlots[0] =  new XYSeries("Sigma X");
		sigmaPlots[1] =  new XYSeries("Sigma Y");
		sigmaPlots[2] =  new XYSeries("Sigma Z");
		
		int i=0;
		
		for(Spot spot : list){
			
			Gaussian3DSpot gSpot = (Gaussian3DSpot) spot ;
			if(gSpot.wasRun()){
			double[] params = gSpot.getParams() ;
				i++;
				intensityPlot.add(i, params[0]);
				sigmaPlots[0].add(i, params[5]*pixelSize);
				sigmaPlots[1].add(i, params[6]*pixelSize);
				sigmaPlots[2].add(i, params[7]*dZ);
				
				minIntensity = Math.min(minIntensity, params[0] );
				maxIntensity =Math.max(maxIntensity, params[0]) ;
				minSigmaX = Math.min(minSigmaX, params[5] );
				maxSigmaX = Math.max(maxSigmaX, params[5] );
				minSigmaY = Math.min(minSigmaY, params[6] );
				maxSigmaY = Math.max(maxSigmaY, params[6] );
				minSigmaZ = Math.min(minSigmaZ, params[7] );
				maxSigmaZ = Math.max(maxSigmaZ, params[7] );
				minX = Math.min(minX, params[2] );
				maxX = Math.max(maxX, params[2]);
				minY = Math.min(minY, params[3] );
				maxY = Math.max(maxY, params[3]);
				minZ = Math.min(minZ, params[4] );
				maxZ = Math.max(maxZ, params[4]);
				minBackground = Math.min(minBackground, params[1]) ;
				maxBackground = Math.max(maxBackground, params[1]) ;
			}
		}
		GaussianUtils.plotData("Intensities" , intensityPlot, "Localization", "Intensity", 0, 0);
		GaussianUtils.plotDataN("Standard Deviations", sigmaPlots, "Localization", "Sigma", 0, 0, false, false) ;
		/*
		 * (4) Now, put the values in the field...
		 */
		double doubleEpsilon = 0.1;
		if( intensityMinField.getText().isEmpty() )
				//|| Double.valueOf(intensityMinField.getText())> minIntensity)
			intensityMinField.setText(String.format("%.2f", minIntensity-doubleEpsilon)) ;
		if( intensityMaxField.getText().isEmpty())
				//|| Double.valueOf(intensityMaxField.getText())< maxIntensity)
			intensityMaxField.setText(String.format("%.2f", maxIntensity+doubleEpsilon)) ;
		if( sigmaXMinField.getText().isEmpty())
				//|| Double.valueOf(sigmaXMinField.getText())> minSigmaX)
			sigmaXMinField.setText(String.format("%.2f", minSigmaX*pixelSize-doubleEpsilon)); 
		if( sigmaXMaxField.getText().isEmpty())
				//|| Double.valueOf(sigmaXMaxField.getText())< maxSigmaX)
			sigmaXMaxField.setText(String.format("%.2f", maxSigmaX*pixelSize+doubleEpsilon));
		if( sigmaYMinField.getText().isEmpty())
				//|| Double.valueOf(sigmaYMinField.getText())> minSigmaY)
			sigmaYMinField.setText(String.format("%.2f", minSigmaY*pixelSize-doubleEpsilon)); 
		if( sigmaYMaxField.getText().isEmpty())
				//|| Double.valueOf(sigmaYMaxField.getText())< maxSigmaY)
			sigmaYMaxField.setText(String.format("%.2f", maxSigmaY*pixelSize+doubleEpsilon));
		if( sigmaZMinField.getText().isEmpty())
				//|| Double.valueOf(sigmaZMinField.getText())> minSigmaZ)
			sigmaZMinField.setText(String.format("%.2f", minSigmaZ*dZ-doubleEpsilon)); 
		if( sigmaZMaxField.getText().isEmpty())
				//|| Double.valueOf(sigmaZMaxField.getText())< maxSigmaZ)
			sigmaZMaxField.setText(String.format("%.2f", maxSigmaZ*dZ+doubleEpsilon));
		if( xMinField.getText().isEmpty())
			xMinField.setText(String.format("%.2f", 0.0));//minX*pixelSize-doubleEpsilon)) ;
		if( xMaxField.getText().isEmpty())
			xMaxField.setText(String.format("%.2f", 500.0*pixelSize));//maxX*pixelSize+doubleEpsilon)) ;
		if( yMinField.getText().isEmpty())
			yMinField.setText(String.format("%.2f", 0.0));//minY*pixelSize-doubleEpsilon)) ;
		if( yMaxField.getText().isEmpty())
			yMaxField.setText(String.format("%.2f", 500.0*pixelSize));//maxY*pixelSize+doubleEpsilon)) ;
		if( zMinField.getText().isEmpty()
				|| Double.valueOf(zMinField.getText())> minZ)
			zMinField.setText(String.format("%.2f", -Utils.numFocalPlanes/2.0*dZ)) ;
		if( zMaxField.getText().isEmpty())
			zMaxField.setText(String.format("%.2f", Utils.numFocalPlanes*dZ)) ;
		if( backgroundMinField.getText().isEmpty())
				//|| Double.valueOf(backgroundMinField.getText())> minBackground)
			backgroundMinField.setText(String.format("%.2f", minBackground)); // minBackground
		if( backgroundMaxField.getText().isEmpty())
				//|| Double.valueOf(backgroundMaxField.getText())< maxBackground)
			backgroundMaxField.setText(String.format("%.2f", maxBackground+doubleEpsilon));
		
		simulateButton.setEnabled(true) ;
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE) ; // make it unclosable
	}
	/*
	 * Test the filter settings on this slice
	 */
	private void testOnFrame() {
		
		MainTab mainTab = (MainTab) parent.getTab("mainTab");
		
		int a = mainTab.getBoxWidth();
		int p = mainTab.getZBoxWidth() ;

		
		ImagePlus imp = mainTab.getMainImage();
		if(imp == null){ // user correction
			IJ.showMessage("Please choose the image in which you want to do the localization in!") ;
			return ;
		}
		Comparer comparer = this.getComparer() ;
		ArrayList<Spot> list = comparer.refineSpotList( extractFromFrame());
		
		ImagePlus subStack= Utils.extractFrame(imp, imp.getT());
		
		Overlay ov = new Overlay() ;
		subStack.setOverlay(ov);
		for(Spot spot : list){
			Gaussian3DSpot gSpot = (Gaussian3DSpot) spot ;
			Point3D<Double> pos = gSpot.getRefinedPosition() ;
			Roi roi = new Roi(pos.getX()-a, pos.getY()-a, 2*a, 2*a) ;
			roi.setStrokeColor(Color.cyan);
			ov.add(roi);
		}
		subStack.updateAndDraw();
		subStack.show();
	}
	protected class DeduceRangeListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			deduceRange() ;
		}
		
	}
	protected class TestOnFrameListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			testOnFrame();
		}
		
	}
	protected class CloseListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			setVisible(false) ;
		}
		
	}
	/*
	 * Produce up-to-date comparer.
	 */
	public Comparer getComparer(){
		MainTab mainTab = (MainTab) parent.getTab("mainTab");
		
		double pixelSize = mainTab.getPixelSize();
		double dZ = mainTab.getDz();
		return new Comparer(getNormedMin(pixelSize, dZ) , getNormedMax(pixelSize, dZ)) ;
	}
	/*
	 * All physical distances are divided by pixelSize and z-step respectively.
	 */
	public double[] getNormedMin( double pixelSize, double dZ){
		return new double[]{ Double.valueOf(intensityMinField.getText()),
						Double.valueOf(backgroundMinField.getText()),
						Double.valueOf(xMinField.getText())/pixelSize,
						Double.valueOf(yMinField.getText())/pixelSize,
						Double.valueOf(zMinField.getText())/dZ,
						Double.valueOf(sigmaXMinField.getText())/pixelSize,
						Double.valueOf(sigmaYMinField.getText())/pixelSize,
						Double.valueOf(sigmaZMinField.getText())/dZ } ;
	}
	public double[] getNormedMax( double pixelSize, double dZ){
		return new double[]{ Double.valueOf(intensityMaxField.getText()),
						Double.valueOf(backgroundMaxField.getText()),
						Double.valueOf(xMaxField.getText())/pixelSize,
						Double.valueOf(yMaxField.getText())/pixelSize,
						Double.valueOf(zMaxField.getText())/dZ,
						Double.valueOf(sigmaXMaxField.getText())/pixelSize,
						Double.valueOf(sigmaYMaxField.getText())/pixelSize,
						Double.valueOf(sigmaZMaxField.getText())/dZ } ;
	}
}
