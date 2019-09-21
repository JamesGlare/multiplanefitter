package gui;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.StackWindow;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.plugin.filter.BackgroundSubtracter;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lib.GaussianUtils;
import lib.Utils;

import org.jfree.data.xy.XYSeries;

import processing.SpotMother;

/***************************************
 * MainTab Class.
 * @author james
 * This Tab includes all principal 
 * components of procedure management
 * like all kinds of buttons and
 * 'Save As..' textfields. 
 **************************************/
public class MainTab extends JComponent implements MouseToClass, ij.ImageListener {
	/*
	 * Private Members
	 */
	
	final private String stdOut ="localizations.jg" ;
	
	/* Parent Frame */
	private JFrame frame ;
	
	/* Box */
	final private JPanel panel1 ;
	final private Border ioBorder;
	
	/*IO */
	final private JFormattedTextField saveField ;
	final private JButton browseButton;
	final private JLabel saveAsLabel ;
	final private JFileChooser fileChooser ;
	final private JButton useActiveWindowButton;
	final private JLabel useActiveWindowLabel ;
	final private JLabel MainImageLabel ;
	final private JFormattedTextField stackFileField ;
	final private JFormattedTextField mainImageField;
	final private JButton thisOneButton ;
	/* Procedure Control */
	final private JPanel panel3 ;
	final private JLabel DoAutomatically ;
	final private JLabel doNow;
	final private Border procedureBorder ;
	final private JButton denoise ;
	final private JButton detect ;
	final private JButton filterButton;
	final private JCheckBox denoiseAutoCheck;
	final private JCheckBox detectAutoCheck ;
	final private JCheckBox filterCheckBox ;
	
	/* Information */
	final private JPanel panel2;
	final private JFormattedTextField focalPlaneDistanceField ;
	final private JLabel focalDistanceLabel;
	final private JLabel focalDistanceUnitLabel ;
		/* Information about setup */
	final private JLabel wavelengthLabel ;
	final private JLabel wavelengthUnitLabel ;
	final private JFormattedTextField wavelengthField ;
	final private JLabel pixelsizeLabel ;
	final private JLabel pixelsizeUnitLabel ;
	final private JFormattedTextField pixelsizeField ;

	final private JLabel NALabel ;
	final private JLabel NAUnitLabel ;
	final private JFormattedTextField NAField ;
	final private JLabel diffIndexLabel ;
	final private JLabel diffIndexUnitLabel ;
	final private JFormattedTextField diffIndexField ;
	
		/* Information about algorithm */
	final private JPanel panel21;
	final private Border detectBorder ;
	final private JLabel boxWidthLabel ;
	final private JFormattedTextField boxWidthField ;
	final private JLabel boxWidthUnitLabel ;
	final private JLabel zBoxWidthLabel ;
	
	final private JLabel integrateWidthLabel;
	final private JFormattedTextField integrateWidthField;
	final private JLabel integrateWidthUnitLabel;
	
	final private JLabel zIntegrateWidthLabel;
	final private JFormattedTextField zIntegrateWidthField;
	final private JLabel zIntegrateWidthUnitLabel;
	
	
	final private JFormattedTextField zBoxWidthField ;
	final private JLabel zBoxWidthUnitLabel ;
	
	final private JLabel noiseThresholdLabel ;
	final private JFormattedTextField noiseThresholdField ;
	final private JLabel noiseThresholdUnitLabel ;
	final private JRadioButton rule1Button ;
	final private JRadioButton rule2Button ;
	final private JRadioButton rule3Button ;
	final private ButtonGroup ruleButtonGroup ;
	
	final private JCheckBox symmetricGaussianCheck;// symmetric Gaussian ?
	final private JLabel maxIterationsLabel;
	final private JFormattedTextField maxIterationsField ;
	
	final private Border infoBorder ;
	/* Layout */
	final private GridBagLayout layout1;
	final private GridBagConstraints c1;
	final private GridBagLayout layout12;
	final private GridBagConstraints c12;
	final private GridBagLayout layout2;
	final private GridBagConstraints c2;
	/*  */
	/* Internal Stuff */
	String focalPlaneDistance = "FOCALPLANEDISTANCE";
	SelectionImageCanvas zStackCanvas ;
	ImagePlus mainImage ;
	FilterDialog filterDialog ;
	private boolean detectListening ;
	private int lastFrame_ =1;
	private int lastSlice_ =1 ;
	private final int pixelSizePredef = 120 ;
	private final double focalDistancePredef = 400 ;
	
	
	/*
	 * Constructor
	 */
	public MainTab(JFrame frame){
		super() ; // Create JComponent
		
		this.frame = frame ;

		/* Create all Instances */
		this.layout1 = new GridBagLayout() ;
		this.c1 = new GridBagConstraints() ;
		this.layout12 = new GridBagLayout() ;
		this.c12 = new GridBagConstraints() ;
		this.layout2 = new GridBagLayout() ;
		this.c2 = new GridBagConstraints() ;
		
		this.saveField = new JFormattedTextField(this.stdOut) ;
		this.ioBorder = BorderFactory.createTitledBorder("File Management") ;
		this.browseButton = new JButton("Browse...") ;
		this.stackFileField = new JFormattedTextField("stackFile") ;
		this.useActiveWindowButton = new JButton("This one!") ;
		this.useActiveWindowLabel = new JLabel("File to be aligned: ") ;
		this.saveAsLabel = new JLabel("Save localizations to file: ") ;
		this.panel1 = new JPanel() ;
		fileChooser = new JFileChooser() ;
		this.MainImageLabel = new JLabel("3D Fluorescence Data: ") ;
		this.mainImageField = new JFormattedTextField("Main Image File") ;
		this.thisOneButton = new JButton("This one!") ;
		
		this.panel3 = new JPanel() ;
		this.procedureBorder = BorderFactory.createTitledBorder("Procedure") ;
		this.DoAutomatically = new JLabel("Apply automatically") ;
		this.doNow = new JLabel("Apply now & show") ;
		this.denoise = new JButton("Preview of DoF") ;
		this.denoiseAutoCheck = new JCheckBox() ;
		this.denoiseAutoCheck.setSelected(false) ;
		this.detect = new JButton("Preview Detection") ;
		this.detectAutoCheck = new JCheckBox() ;
		this.detectAutoCheck.setSelected(true) ;
		this.detectAutoCheck.setEnabled(false) ;
		
		this.filterCheckBox = new JCheckBox();
		this.filterButton = new JButton("Specifiy Bounds") ;
		
		this.panel2 = new JPanel() ;
		this.focalPlaneDistanceField = new JFormattedTextField() ;
		this.focalPlaneDistanceField.setText(String.valueOf(this.focalDistancePredef)) ;
		this.focalDistanceLabel = new JLabel("Avg. Distance of Focal Planes ") ;
		this.focalDistanceUnitLabel = new JLabel("[nm]") ;
		this.infoBorder = BorderFactory.createTitledBorder("Information") ;

		this.wavelengthLabel = new JLabel("Em. WaveLength") ;
		this.wavelengthField = new JFormattedTextField("600") ;
		this.wavelengthField.setColumns(5) ;
		this.wavelengthUnitLabel = new JLabel("[nm]") ;
		this.pixelsizeLabel= new JLabel("Pixelsize") ;
		this.pixelsizeField = new JFormattedTextField("") ;
		this.pixelsizeField.setText(String.valueOf(this.pixelSizePredef)) ;
		this.pixelsizeField.setColumns(5);
		this.pixelsizeUnitLabel = new JLabel("[nm]") ;
		this.NALabel = new JLabel("NA: ");
		this.NAField = new JFormattedTextField("1.4");
		this.NAField.setColumns(5);
		this.NAUnitLabel = new JLabel("[arb]");
		this.diffIndexLabel = new JLabel("Refractive Index ");
		this.diffIndexField = new JFormattedTextField("1.5");
		this.diffIndexField.setColumns(5) ;
		this.diffIndexUnitLabel = new JLabel("[arb]") ;
		
		this.panel21 = new JPanel() ;
		this.detectBorder = BorderFactory.createTitledBorder("Spot Detection") ;
		this.boxWidthLabel = new JLabel("Box Half Width") ;
		this.boxWidthField = new JFormattedTextField("4");
		this.boxWidthUnitLabel = new JLabel("[pix]") ;
		this.zBoxWidthLabel = new JLabel("z Box Half Width") ;
		this.zBoxWidthField = new JFormattedTextField("1");
		this.zBoxWidthUnitLabel = new JLabel("[pix]") ;
		
		this.integrateWidthLabel= new JLabel("Fitbox Half Width");
		this.integrateWidthField = new JFormattedTextField("5");
		this.integrateWidthUnitLabel = new JLabel("[pix]") ;
		
		this.zIntegrateWidthLabel= new JLabel("z Fitbox Half Width");
		this.zIntegrateWidthField = new JFormattedTextField("2");
		this.zIntegrateWidthUnitLabel = new JLabel("[pix]") ;
		
		this.rule1Button = new JRadioButton("4P 3 Plane ");
		this.rule2Button = new JRadioButton("4P 2 Planes");
		this.rule2Button.setSelected(true) ;
		this.rule3Button = new JRadioButton("Sum 2 Planes");
		this.ruleButtonGroup = new ButtonGroup() ;
		 // add all the buttons to the group !
		this.ruleButtonGroup.add(rule1Button);
		this.ruleButtonGroup.add(rule2Button);
		this.ruleButtonGroup.add(rule3Button);
		this.symmetricGaussianCheck =  new JCheckBox("Symmetric Gaussian ?");
		this.symmetricGaussianCheck.setSelected(true) ;
		
		this.noiseThresholdLabel = new JLabel("Noise Threshold") ;
		this.noiseThresholdField = new JFormattedTextField("250") ;
		this.noiseThresholdUnitLabel = new JLabel("[arb]") ;
		
		this.maxIterationsLabel = new JLabel("ML Iterations");
		this.maxIterationsField = new JFormattedTextField("1000") ;
		
		/* Add all Components and set the layouts !*/
		this.saveField.setColumns(10) ;
		this.stackFileField.setColumns(10);
		this.stackFileField.setEditable(false) ;
		this.mainImageField.setEditable(false);
		this.mainImageField.setColumns(10);
		this.panel1.setLayout(layout1);
		this.panel1.setBorder(ioBorder);
		
		this.panel3.setLayout(layout12);
		this.panel3.setBorder(procedureBorder) ;
		
		this.panel2.setLayout(layout2);
		this.panel2.setBorder(infoBorder);
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS)) ;
		
		this.add(panel1) ;
		this.add(panel2) ;
		/* Do THEN the subadding */ 
		c1.gridx = 0 ;
		c1.gridy = 0 ;
		c1.weightx= 1 ;
		c1.weighty= 1 ;
		this.panel1.add(useActiveWindowLabel,c1);
		++c1.gridx;
		this.panel1.add(stackFileField,c1) ;
		++c1.gridx;
		panel1.add(useActiveWindowButton,c1)  ;
		// new line
		++c1.gridy;
		c1.gridx = 0 ;
		this.panel1.add(MainImageLabel, c1);
		c1.gridx++ ;
		this.panel1.add(mainImageField, c1);
		c1.gridx++ ;
		this.panel1.add(thisOneButton, c1);
		// new line
		++c1.gridy;
		c1.gridx = 0 ;
		this.panel1.add(saveAsLabel,c1);
		++c1.gridx;
		this.panel1.add(saveField,c1);
		++c1.gridx ;
		this.panel1.add(browseButton,c1);
		c1.gridx=0;
		c1.gridy++ ;
		c1.gridwidth = 3; 
		c1.fill = GridBagConstraints.BOTH;
		this.panel1.add(panel3, c1) ;
		//Panel 12
		c12.gridx= 0;
		c12.gridy= 0;
		c12.fill = GridBagConstraints.BOTH ;
		this.panel3.add(DoAutomatically, c12) ;
		c12.gridx++ ;
		this.panel3.add(Box.createHorizontalStrut(30)) ;
		c12.gridx++ ;
		this.panel3.add(doNow, c12) ;
		//new line
		c12.gridx = 0;
		c12.gridy++ ;
		this.panel3.add(denoiseAutoCheck, c12 ) ;
		c12.gridx++ ;
		this.panel3.add(Box.createHorizontalStrut(30)) ;
		c12.gridx++ ;
		this.panel3.add(denoise, c12 ) ;
		//new line
		c12.gridx = 0;
		c12.gridy++ ;
		this.panel3.add(detectAutoCheck, c12 ) ;
		c12.gridx++ ;
		this.panel3.add(Box.createHorizontalStrut(30)) ;
		c12.gridx++ ;
		this.panel3.add(detect, c12 ) ;
		//new line
		c12.gridx = 0;
		c12.gridy++ ;
		this.panel3.add(filterCheckBox, c12 ) ;
		c12.gridx++ ;
		this.panel3.add(Box.createHorizontalStrut(30)) ;
		c12.gridx++ ;
		this.panel3.add(filterButton, c12 ) ;

		
		// Panel 2
		c2.gridx = 0 ;
		c2.gridy = 0 ;
		c2.weightx= 1 ;
		c2.weighty= 1 ;
		this.panel2.add(focalDistanceLabel,c2) ;
		c2.gridx++ ;
		this.panel2.add(focalPlaneDistanceField,c2);
		this.focalPlaneDistanceField.setColumns(5) ;
		c2.gridx++ ;
		this.panel2.add(focalDistanceUnitLabel,c2) ;
		// next line
		c2.gridy++ ;
		c2.gridx =0 ;
		this.panel2.add(pixelsizeLabel,c2) ;
		c2.gridx++ ;
		this.panel2.add(pixelsizeField,c2) ;
		c2.gridx++ ;
		this.panel2.add(pixelsizeUnitLabel,c2) ;
		
		// next line
		c2.gridx =0 ;
		c2.gridy++ ;
		this.panel2.add(wavelengthLabel,c2) ;
		c2.gridx++ ;
		this.panel2.add(wavelengthField,c2) ;
		c2.gridx++ ;
		this.panel2.add(wavelengthUnitLabel,c2) ;

		
		// next line
		c2.gridx = 0 ;
		c2.gridy++ ;
		this.panel2.add(NALabel,c2) ;
		c2.gridx++ ;
		this.panel2.add(NAField,c2) ;
		c2.gridx++ ;
		this.panel2.add(NAUnitLabel,c2) ;
		
		// next line
		c2.gridx = 0 ;
		c2.gridy++ ;
		this.panel2.add(diffIndexLabel,c2) ;
		c2.gridx++ ;
		this.panel2.add(diffIndexField,c2) ;
		c2.gridx++ ;
		this.panel2.add(diffIndexUnitLabel,c2) ;
				
		c2.gridy++;
		c2.gridx=0;
		c2.gridwidth=3;
		c2.fill = GridBagConstraints.BOTH;
		panel2.add(panel21,c2);
		
		// PANEL 21 - The one with the Spot Detection Border
		GridBagConstraints c21 = new GridBagConstraints() ;
		GridBagLayout layout21 = new GridBagLayout() ;
		panel21.setLayout(layout21) ;
		panel21.setBorder(detectBorder);
		c21.gridx =0;
		c21.gridy=0;
		c21.weightx= 1 ;
		c21.weighty= 1 ;
		c21.fill = GridBagConstraints.BOTH;
		// new line
		this.panel21.add(zBoxWidthLabel,c21);
		c21.gridx++ ;
		this.panel21.add(zBoxWidthField,c21);
		zBoxWidthField.setColumns(5) ;
		c21.gridx++ ;
		this.panel21.add(zBoxWidthUnitLabel,c21);

		c21.gridx=0 ;
		c21.gridy++ ;
		this.panel21.add(boxWidthLabel,c21) ;
		c21.gridx++ ;
		this.panel21.add(boxWidthField,c21) ;
		this.boxWidthField.setColumns(5) ;
		c21.gridx++ ;
		this.panel21.add(boxWidthUnitLabel,c21) ;
		
		// place holder
		c21.gridx= 0;
		c21.gridy++;
		this.panel21.add(Box.createVerticalStrut(5),c21) ;
		
		// new line
		c21.gridx= 0;
		c21.gridy++;
		this.panel21.add(zIntegrateWidthLabel, c21);
		c21.gridx++ ;
		this.panel21.add(zIntegrateWidthField,c21);
		this.zIntegrateWidthField.setColumns(5) ;
		c21.gridx++;
		this.panel21.add(zIntegrateWidthUnitLabel,c21) ;
		
		// new line
		c21.gridy++;
		c21.gridx=0;
		this.panel21.add(integrateWidthLabel, c21);
		c21.gridx++ ;
		this.panel21.add(integrateWidthField,c21);
		this.integrateWidthField.setColumns(5) ;
		c21.gridx++;
		this.panel21.add(integrateWidthUnitLabel,c21) ;
		
		// place holder
		c21.gridx= 0;
		c21.gridy++;
		this.panel21.add(Box.createVerticalStrut(5),c21) ;
				
		// new line
		c21.gridx=0 ;
		c21.gridy++ ;
		this.panel21.add(noiseThresholdLabel,c21) ;
		c21.gridx++ ;
		this.panel21.add(noiseThresholdField,c21) ;
		this.noiseThresholdField.setColumns(5) ;
		c21.gridx++ ;
		this.panel21.add(noiseThresholdUnitLabel,c21) ;
		// new line
		c21.gridx=0 ;
		c21.gridy++ ;
		this.panel21.add(rule1Button, c21);
		c21.gridx++;
		this.panel21.add(rule2Button, c21);
		c21.gridx++;
		this.panel21.add(rule3Button, c21);
		// new line
		c21.gridx=0 ;
		c21.gridy++ ;
		c21.gridwidth =1;
		this.panel21.add(symmetricGaussianCheck, c21);
		c21.gridx++;
		this.panel21.add(maxIterationsLabel, c21);
		c21.gridx++;
		this.panel21.add(maxIterationsField, c21);
		
		
		this.browseButton.addActionListener(new BrowseListener()) ;
		this.useActiveWindowButton.addActionListener(new UseActiveWindow(this)) ;
		this.denoise.addActionListener(new DenoiseListener()) ;
		this.detect.addActionListener(new DetectSpotsListener(this)) ;
		this.thisOneButton.addActionListener(new ThisOneListener()) ;
		this.boxWidthField.getDocument().addDocumentListener(new SpotFindParamListener());
		this.noiseThresholdField.getDocument().addDocumentListener(new SpotFindParamListener()) ;
		this.zBoxWidthField.getDocument().addDocumentListener(new SpotFindParamListener()) ;
		this.filterButton.addActionListener(new FilterDialogListener()) ;
		
		// set the filter dialog
		this.filterDialog= new FilterDialog((MainWindow) frame);
		this.filterDialog.pack();
		filterDialog.setVisible(false) ; // hide it		
	}
	/*
	 * Getter function for zStack Image.
	 */
	public ImagePlus getZStack(){
		if(this.zStackCanvas!= null){
			return this.zStackCanvas.getImage() ;
		} else{
			return null;
		}
	}
	public int getNoiseThreshold() throws NumberFormatException {
		return Integer.valueOf(this.noiseThresholdField.getText()) ;
	}
	public int getBoxWidth() throws NumberFormatException{
		return Integer.valueOf(this.boxWidthField.getText()) ;
	}
	public int getZBoxWidth() throws NumberFormatException{
		return Integer.valueOf(this.zBoxWidthField.getText()) ;
	}
	public int getPixelSize() throws NumberFormatException {
		return Integer.valueOf(pixelsizeField.getText()) ;
	}
	public double getDz() throws NumberFormatException{
		return Double.valueOf(focalPlaneDistanceField.getText()) ;
	}
	public String getOutFileName(){
		return saveField.getText();
	}
	public boolean getIfFilter(){
		return denoiseAutoCheck.isSelected() ;
	}
	public boolean getIfParameterFilter(){
		return this.filterCheckBox.isSelected() ;
	}
	public int getFitBoxWidth() throws NumberFormatException{
		return Integer.valueOf(integrateWidthField.getText()) ;
	}
	public int getZFitBoxWidth() throws NumberFormatException{
		return Integer.valueOf(zIntegrateWidthField.getText()) ;
	}

	public double getWavelength() throws NumberFormatException {
		return Double.valueOf(this.wavelengthField.getText()) ;
	}
	public double getNA() throws NumberFormatException{
		return Double.valueOf(this.NAField.getText()) ;
	}
	public double getDiffIndex() throws NumberFormatException{
		return Double.valueOf(this.diffIndexField.getText()) ;
	}
	public boolean getSymmetric(){
		return symmetricGaussianCheck.isSelected() ;
	}
	public int getMaxIterations() throws NumberFormatException{
		return Integer.valueOf(maxIterationsField.getText() ) ;
	}
	/*
	 * Returns an integer coding for the number of the rule the user chose!
	 */
	public int getRuleNumber() {
		if(rule1Button.isSelected())
			return 1;
		else if(rule2Button.isSelected())
			return 2;
		else 
			return 3;
	}
	/*
	 * To communicate with the other tabs and functions.
	 * To gather all the information needed to create a .3d file.
	 */
	public void updateInfo(String identifier, double value){
		if(identifier.equals(this.focalPlaneDistance)){
			this.focalPlaneDistanceField.setText(String.format("%.2f",value)) ;
		}
	}

	public ImagePlus getMainImage(){
		return mainImage ;
	}
	public FilterDialog getFilterDialog(){
		return filterDialog ;
	}
	protected class BrowseListener implements ActionListener{
		BrowseListener(){}
		public void actionPerformed( final ActionEvent arg0 ) {
			if(arg0.getSource() == browseButton){
				String rawTitle = saveField.getText();
				String ext = Utils.getExtension(rawTitle);
				if(ext != null){
					rawTitle = rawTitle.replace(ext, "") ;
				}
				SaveDialog saveDialog = new SaveDialog("Save localizations in xyz sigmaX sigmaY sigmaZ I f format", OpenDialog.getLastDirectory(),rawTitle  ,".jg") ;
				String outputFile = saveDialog.getDirectory()+saveDialog.getFileName() ;
				
				if(outputFile !=null){
					saveField.setText(outputFile) ;
				} 
			}
		}
	}
	protected class UseActiveWindow implements ActionListener{
		private MouseToClass parent ;
		
		public UseActiveWindow (MouseToClass parent_){
			parent = parent_ ;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if( null!= WindowManager.getCurrentImage()){
				zStackCanvas = new SelectionImageCanvas(WindowManager.getCurrentImage(), this.parent) ;
				stackFileField.setText(WindowManager.getCurrentImage().getTitle()) ;
			}
		}
		
	}
	/*
	 * Creates Filterdialog.
	 */
	protected class FilterDialogListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			filterDialog.setVisible(true) ;
		}
		
	}
	protected class ThisOneListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			/*
			 * PLAYZONE
			
				System.out.println(IJ.getFullVersion()  ) ;
				ImagePlus imp = IJ.createImage("stack1", "16-bit color-mode label white", 400, 400, 9);
				 imp.show();
				 ImageStack stack = imp.getStack();
			  (new ImagePlus("Stack2",stack)).show();
				

			   *  END PLAYZONE
			   */
			MainWindow mw = (MainWindow) frame;
			mainImage = WindowManager.getCurrentImage() ;
			if ( mainImage != null){
				mainImageField.setText(mainImage.getTitle()) ;
				
				mw.setGo(true) ;
				File file1 =new File(System.getProperty("user.dir")) ;
				File file2 = new File(mainImage.getTitle()) ;
				String fExt= Utils.getExtension(file2);
				String fName = mainImage.getTitle().replaceAll(fExt, "jg");
				String proposal = new File(file1,fName).toString() ;
				saveField.setText(proposal) ;
			} else{
				mw.setGo(false) ;
			}
		}
		
	}
	protected class SpotFindParamListener implements DocumentListener{

		@Override
		public void insertUpdate(DocumentEvent e) {
			ImagePlus ip = WindowManager.getCurrentImage();
		
			
			if(ip == null){
				return ;
			}
			
			if(! Utils.isNumeric(boxWidthField.getText())){
				//IJ.showMessage("Please enter only numeric characters.") ;
				return ;
			}
			if(! Utils.isNumeric(zBoxWidthField.getText())){
				//IJ.showMessage("Please enter only numeric characters.") ;
				return ;
			}
			if(! Utils.isNumeric(noiseThresholdField.getText())){
				//IJ.showMessage("Please enter only numeric characters.") ;
				return ;
			}
			
			if(detectListening){
			 			ip.setOverlay(null) ;
						ip.setHideOverlay(true) ;
						try{
							SpotMother spM = new SpotMother(ip,
									getBoxWidth(),
									getZBoxWidth(),
									getFitBoxWidth(),
									getZFitBoxWidth(),
									getNoiseThreshold(),
									getRuleNumber()
									) ;
							spM.runOnCurrentImage(getIfFilter()) ;	 
						} catch(NumberFormatException ex){
							LogTab.writeLog(ex.getLocalizedMessage()) ;
						}
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			insertUpdate(e) ; 
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			insertUpdate(e) ; 

		}
		
	}
	
	protected class DenoiseListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {

			ImagePlus ip = mainImage ;
			if(ip == null){
				IJ.showMessage("Error: Please set image in which you want to localize!") ;
				return ;
			}
			SpotMother spm = new SpotMother(ip,
					getBoxWidth(),
					getZBoxWidth(),
					getFitBoxWidth(),
					getZFitBoxWidth(),
					getNoiseThreshold(),
					getRuleNumber()) ;
			ImagePlus filtered =SpotMother.filterImage(ip);
			StackWindow win = new StackWindow(filtered) ;
			win.show();
		}
	}
	protected class DetectSpotsListener implements ActionListener{
		private ij.ImageListener parent ;
		public DetectSpotsListener(ij.ImageListener parent_){
			parent = parent_ ;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			ImagePlus ip = mainImage ;
			if(ip == null){
				IJ.showMessage("Error: Please set image in which you want to localize!") ;
				return ;
			}
			if( !detectListening){
				
				SpotMother spm = new SpotMother(ip,
											getBoxWidth(),
											getZBoxWidth(),
											getFitBoxWidth(),
											getZFitBoxWidth(),
											getNoiseThreshold(),
											getRuleNumber()) ;
				spm.runOnCurrentImage(getIfFilter()) ;			
				ImagePlus.addImageListener(parent) ;
				detectListening = true ;
				lastFrame_ = ip.getFrame();
				lastSlice_ = ip.getSlice();
			} else {
				detectListening = false ;
				ip.setOverlay(null) ;
				ip.setHideOverlay(true) ;
				ip.updateAndDraw() ;
			}
		}
		
	}
	// useless here
	@Override
	public void clicked(int x, int y, int f) {		
	}
	// ImageListeners
	@Override
	public void imageClosed(ImagePlus arg0) {
		detectListening = false ;
	}
	@Override
	public void imageOpened(ImagePlus arg0) {
		imageUpdated(arg0);
	}
	@Override
	public void imageUpdated(ImagePlus ip) {
		if(ip == null || !(ip== mainImage)){
			return ;
		}
		if(detectListening ){
				 int frame = 1;
				 int slice =1 ;
		         if (ip.getNFrames() > 1)
		            frame = ip.getT();
		         if( ip.getNSlices() >1 )
		            slice= ip.getSlice();
		         
		         if (lastFrame_ != frame || lastSlice_ != slice ) {
		 			ip.setOverlay(null) ;
					ip.setHideOverlay(true) ;
		            lastFrame_ = frame;
		            lastSlice_ = slice ;
					SpotMother spM = new SpotMother(ip,
									getBoxWidth(),
									getZBoxWidth(),
									getFitBoxWidth(),
									getZFitBoxWidth(),
									getNoiseThreshold(),
									getRuleNumber()) ;
					spM.runOnCurrentImage(getIfFilter()) ;	         
				}
		}
	}
}
