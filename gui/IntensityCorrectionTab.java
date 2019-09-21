package gui;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Line;
import ij.process.ImageProcessor;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javassist.bytecode.Descriptor.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lib.Sample;
import lib.Utils;

public class IntensityCorrectionTab extends JComponent {
	/*
	 * Private Members
	 */
	private JFrame frame ;
	private Border intensitiesBorder ;
	private JLabel[] intensityLabels; 
	private JSlider[] intensityFactors ;
	private JLabel factorLabel;
	private JLabel[] percentageLabels ;
	private JLabel maxIntLabel ;
	private JFormattedTextField maxIntField ;
	private JPanel panel1;
	private JLabel constantOffsetLabel;
	private JFormattedTextField constantOffsetField ;
	private JLabel constantOffsetUnitLabel ;
	private JButton reverseButton ;
	private JButton applyButton;
	private JButton applyToStackButton ;
	private MyCanvas canvas;
	
	private GridBagConstraints c1;
	private GridBagLayout layout1;
	/* Internal parameters */
	private int maxIntensity = 65536; // 2^16
	/*
	 * Constructor
	 */
	public IntensityCorrectionTab(JFrame frame){
		super();
		this.frame = frame ;
		
		// initialize
		
		intensitiesBorder = BorderFactory.createTitledBorder("Intensities") ;
		panel1 = new JPanel();
		canvas = new MyCanvas() ;
		layout1 = new GridBagLayout();
		c1 = new GridBagConstraints() ;
		c1.gridx=0;
		c1.gridy=0;
		
		reverseButton = new JButton("Reverse Values") ;
		applyButton = new JButton("Apply to Slice") ;
		applyToStackButton = new JButton("Apply to Stack") ;
		factorLabel = new JLabel("Value added in % of Pixelintensity") ;
		maxIntLabel = new JLabel("Maximum") ;
		maxIntField = new JFormattedTextField("300");
		maxIntField.setColumns(10) ;
		constantOffsetLabel = new JLabel("To subtract before") ;
		constantOffsetField = new JFormattedTextField("107") ;
		constantOffsetField.setColumns(5) ;
		constantOffsetUnitLabel = new JLabel("[arb]") ;
		//Layout and adding
		panel1.setLayout(layout1);
		panel1.setBorder(intensitiesBorder);
		intensityLabels = new JLabel[Utils.numFocalPlanes];
		intensityFactors = new JSlider[Utils.numFocalPlanes] ;
		percentageLabels = new JLabel[Utils.numFocalPlanes] ;
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS)) ;
		this.add(panel1);
		c1.gridwidth = 3;
		panel1.add(factorLabel, c1) ;
		// new line
		c1.gridy++ ;
		c1.gridwidth=1;
		panel1.add(maxIntLabel,c1);
		c1.gridx++;
		c1.gridwidth=2;
		panel1.add(maxIntField,c1) ;
		c1.gridx=0 ;
		c1.gridwidth=1;
		c1.gridy++ ;
		for(int i = 0; i < Utils.numFocalPlanes; i++){
			//  add to panel
			intensityLabels[i] = new JLabel("Plane "+Utils.mappingOrder[i] +":") ;
			
			panel1.add(intensityLabels[i], c1);
			c1.gridx++;
			intensityFactors[i] = new JSlider() ;
			intensityFactors[i].setMinimum(-100);
			intensityFactors[i].setMaximum(300); // changes of up to +300 % magnitude !!
			intensityFactors[i].setValue(0);
			intensityFactors[i].addChangeListener(new IntensityChange(i)) ;
			percentageLabels[i] =  new JLabel("0 %") ;
			panel1.add(intensityFactors[i], c1) ;
			c1.gridx++ ;
			panel1.add(percentageLabels[i],c1) ;
			c1.gridy++;
			c1.gridx=0;
		}
		// new line
		c1.gridy++;
		panel1.add(constantOffsetLabel,c1) ;
		c1.gridx++;
		panel1.add(constantOffsetField,c1);
		c1.gridx++;
		panel1.add(constantOffsetUnitLabel,c1) ;
		c1.gridx=0;
		// now add all the other stuff.
		c1.gridx+=3;
		c1.gridy=2;
		panel1.add(Box.createRigidArea(new Dimension(20,0)),c1) ;
		c1.gridx++;
		c1.weightx=1;
		c1.weighty=1;
		c1.gridheight=Utils.numFocalPlanes+2 ;
		c1.fill = GridBagConstraints.BOTH;

		panel1.add(canvas,c1);
		c1.gridx++;
		c1.gridheight=Utils.numFocalPlanes/2-1 ;

		c1.fill = GridBagConstraints.NONE;
		panel1.add(reverseButton,c1);
		c1.gridy+=Utils.numFocalPlanes/3;
		panel1.add(applyButton,c1) ;
		c1.gridy+=Utils.numFocalPlanes/3;
		panel1.add(applyToStackButton,c1) ;
		/* Add the listeners */
		applyButton.addActionListener(new ApplyListener()) ;
		reverseButton.addActionListener(new ReverseListener()) ;
		applyToStackButton.addActionListener(new ApplyToStackListener()) ;
		this.maxIntField.getDocument().addDocumentListener(new MaxChangeListener() ) ;
		canvas.repaint(); // repaint once, since its then better aligned for some reason...
	}
	// Getter & Setter Offset Field
	public double getConstantOffset() throws NumberFormatException{
		return Double.valueOf(constantOffsetField.getText()) ;
	}
	public void setConstantOffset(double offset) {
		this.constantOffsetField.setText(String.valueOf(offset)) ;
	}
	/*
	 * Update intensity informatio
	 */
	public void updateIntensityInformation(int[] intensities){
		
		double avgCentralIntensity = intensities[Utils.numFocalPlanes/2] ;
		int maxIntensity = 0;
		for(int i=0; i< Utils.numFocalPlanes; i++){
				int maxVal =(int) (100* (avgCentralIntensity/ (intensities[i]) - 1));
				maxIntensity = Math.max(maxVal, maxIntensity) ;
		}

		maxIntField.setText(String.valueOf(maxIntensity)) ;
		
		for(int i=0; i< Utils.numFocalPlanes; i++){
			int val =(int) (100* (avgCentralIntensity/ intensities[i] -1));
			val = Math.min(val, intensityFactors[i].getMaximum()) ;
			val = Math.max(val, intensityFactors[i].getMinimum() ) ;
			this.intensityFactors[i].setValue( val );
		}
		this.canvas.repaint();
	}
	/*
	 * Getter function for the intensity selection
	 */
	public int[] getIntensityFactors(){
		int[] intensities = new int[Utils.numFocalPlanes] ;
		for(int i = 0; i< Utils.numFocalPlanes; i++){
			intensities[i] = this.intensityFactors[i].getValue();
		}
		return intensities ;
	}
	protected void apply(ImagePlus imp, double offset, int f){
		maxIntensity = (int)Math.pow(2, imp.getBitDepth()) -1 ;
		imp.setSlice(f);
		ImageProcessor ip = imp.getProcessor();
		for(int i=0; i< Utils.numFocalPlanes; i++){
			Sample subImage = Utils.sampleFromImage2D(imp, i, f) ;
			int c =0;
			double[][][] borders = Utils.bordersOf2DSubImages(imp.getDimensions());
			for(double j: subImage ){
				int[] coord = subImage.toCoord(c);
				int x = (int)( borders[i][0][0]+coord[0]);
				int y = (int)( borders[i][0][1]+coord[1]);
				 
				// HERE IS THE UPDATE EQUATION
				double newIntensity = intensityChange(j,  intensityFactors[i].getValue(), offset) ;
				
				newIntensity = Math.min(newIntensity, maxIntensity);
				newIntensity = Math.max(0, newIntensity) ;
				ip.putPixelValue(x,y, newIntensity) ;
				c++;
			}
		}
	}
	public static double intensityChange(double j, int factor, double offset){
		double newValue= (j-offset)*(1+
				 factor/100.0 ) ;
		return newValue<0? 0 :  newValue ;
	}
	protected class IntensityChange implements ChangeListener{
		int id ;
		public IntensityChange(int id_){
			id = id_ ;
		}
		@Override
		public void stateChanged(ChangeEvent e) {
			percentageLabels[id].setText(intensityFactors[id].getValue()+" %");
			canvas.repaint();
		}
		
	}
	protected class ApplyToStackListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// here we should put the main stack.
			ImagePlus imp = ((MainTab) ((MainWindow)frame).getTab("mainTab")).getZStack() ;
			double offset = getConstantOffset();//((ZCalibrationTab)((MainWindow)frame).getTab("zCalibrationTab")).getIntensityOffset();
			
			if(imp == null){
				IJ.showMessage("Please select z stack image in the 'General' Tab");
				return ;
			}
			//ImagePlus imp = WindowManager.getCurrentImage();
			// before you mess with intensities -> Check out maxintensity.
			for(int f=1; f<=imp.getStackSize(); f++){
				apply(imp,offset , f) ;
			}
			imp.updateAndDraw();
		}
		
	}
	protected class ApplyListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// here we should put the main stack.
			ImagePlus imp = ((MainTab) ((MainWindow)frame).getTab("mainTab")).getZStack() ;
			double offset = getConstantOffset();//((ZCalibrationTab)((MainWindow)frame).getTab("zCalibrationTab")).getIntensityOffset();

			if(imp == null){
				IJ.showMessage("Please select z stack image in the 'General' Tab");
				return ;
			}
			//ImagePlus imp = WindowManager.getCurrentImage();
			// before you mess with intensities -> Check out maxintensity.
			int f =imp.getCurrentSlice() ;
			apply(imp, offset, f) ;
			imp.updateAndDraw();
		}
		
	}
	protected class ReverseListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			for(int i=0; i< Utils.numFocalPlanes; i++){
				int curVal = intensityFactors[i].getValue();
				double temp = 1.0 / (curVal/100.0+1.0) ;
				intensityFactors[i].setValue( (int)((-1)*temp*curVal));
			}
		}
	}
	protected class MyCanvas extends Canvas{
		private int h,w;
		
		public MyCanvas(){
			
		}
		public void paint(Graphics g){
			h = getSize().height;
			w = getSize().width;
			int size = Math.min(h, w);

			// now subdivide the canvas as the image
			double[][][] coords = Utils.bordersOf2DSubImages(new int[]{size, size});

			for(int j=0; j< Utils.numFocalPlanes; j++){
				// now fill the subimages
				double intF =  (double)intensityFactors[j].getValue();
				int intensity = intF>0 ? (int) (255*(  (0.5 +intF/(2.0*intensityFactors[j].getMaximum())))) : (int) (255*(  (0.5 -intF/(2.0*intensityFactors[j].getMinimum()))))  ;
				g.setColor(new Color(intensity,intensity,intensity));
				int width = size/((int)Math.sqrt(Utils.numFocalPlanes));
				g.fillRect((int) coords[j][0][0], (int)coords[j][0][1],
							width, width );
			}
			for(int i = 0; i< (int)Math.sqrt(Utils.numFocalPlanes)-1; i++){
				// from top to bottom
				//int rows = (int) Math.sqrt(Utils.numFocalPlanes) ;
				//int subLength = (size)/rows ;
				g.setColor(new Color(0,0,200));
				g.drawLine((int)coords[i][1][0],0, (int)coords[i][1][0], size-1);
				g.drawLine(0, (int)coords[i][1][0], size-1, (int)coords[i][1][0]);
			}
			g.setColor(new Color(0,0,200));
			g.drawRect(0, 0, size-1, size-1);
		}
	}
	protected class MaxChangeListener implements DocumentListener{

		@Override
		public void insertUpdate(DocumentEvent e) {
			String maxString = maxIntField.getText() ;
			
			if( ! Utils.isNumeric(maxString)){
				return ;
			}
			int max = Integer.valueOf(maxString) ;
			for(int i=0; i< Utils.numFocalPlanes; i++){
				intensityFactors[i].setMaximum(max ) ;
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			this.insertUpdate(e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			this.insertUpdate(e) ;
		}
		
	}
}

