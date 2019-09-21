package gui;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.Opener;
import ij.io.SaveDialog;
import ij.plugin.GroupedZProjector;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;


import lib.Bead;
import lib.GaussianFit;
import lib.Point3D;
import lib.Sample;
import lib.Utils;

import processing.Alignment;
import processing.ZBeadCalibration;

public class ZCalibrationTab extends JComponent implements MouseToClass {

	/*
	 * Private Members
	 */
	private final JFrame frame ;
	private final JFormattedTextField zStepField ;
	private final JLabel zStepLabel ;
	private final JLabel zStepUnitLabel ;
	
	private final JFormattedTextField zCalibFileField;
	private final JLabel zCalibLabel;
	private final JButton zCalibBrowseButton ;
	private final JRadioButton fileRadioButton ;
	private final JRadioButton activeWinRadioButton ;
	private final JButton activeWindowButton ;
	private final JLabel activeWindowLabel ;
	private final JLabel activeWindowNameLabel ;
	final private ButtonGroup buttonGroup ;
	
	private final GridBagLayout layout1;
	private final GridBagConstraints c1 ;
	private final GridBagLayout layout2;
	private final GridBagConstraints c2 ;
	private final GridBagLayout layout21;
	private final GridBagConstraints c21 ;
	private final GridBagLayout layout3;
	private final GridBagConstraints c3 ;
	
	private final Border zData ;
	private final Border beadBorder;
	private final Border subImageBorder;
	private final Border alignBorder ;
	private final JPanel panel1 ; // zData
	private final JPanel panel2 ; // BeadList, DeduceDistortion & Calibrate Button & align subpanel
	private final JPanel panel21 ; // align panel
	private final JPanel panel3; // Subimage Offset
	private final JPanel panel13;
	
	final private JList<Bead> beadList ;
	final private JScrollPane scrollPane ;
	final private DefaultListModel<Bead> listModel ;
	final private ArrayList<Bead> beads ;
 	final private JButton goButton ;
 	final private JButton procrustesButton ;
 	final private JButton alignButton ;
 	final private JCheckBox adaptIntensity ;
 	final private JButton AlignFolderButton ;
 	final private JCheckBox saveIndividualImagesCheckBox ;
 	
 	final private JFormattedTextField offsetField;
 	final private JFormattedTextField aField ;
 	final private JLabel aLabel;
 	final private JLabel aUnitLabel ;
 	final private JLabel distToleranceLabel ;
 	final private JFormattedTextField distToleranceField ;
 	final private JLabel distToleranceUnitLabel ;
 	final private JLabel offsetLabel;
 	final private JLabel coordOffsetLabel;
 	final private JFormattedTextField dXField ;
 	final private JFormattedTextField dYField ;
 	final private JLabel coordOffsetUnitLabel ;
 	final private JLabel coordOffsetUnitLabel2 ;

 	
 	/* Internal data private members */
	
	private ZBeadCalibration zCalib;
	private Alignment align ;
	private SelectionImageCanvas zProjectCanvas;
	private SelectionImageCanvas realImgCanvas;
	private ImagePlus projection ;
	
	/* Constants */
	private final String aPredef = "3" ;
	private final String offsetPredef = "107";
	private double avgFocalPlaneDistance =0 ;
	private final String distTolerancePredef = "7" ;
	private final String zStepPredef = "60" ;
	
	private static final long serialVersionUID = 1L;
		
	/*
	 * Constructor
	 */
	public ZCalibrationTab(JFrame frame){
		this.frame = frame ;
		
		/* Create all instances */
		this.zStepField = new JFormattedTextField(this.zStepPredef) ;
		this.zStepUnitLabel= new JLabel("[nm]") ;
		this.zStepLabel = new JLabel("z-Step") ;
		this.panel1 = new JPanel() ;
		this.panel13 = new JPanel() ;
		this.panel21 = new JPanel() ;
		this.zData = BorderFactory.createTitledBorder("Bead Calibration ") ;
		
		this.panel2 = new JPanel();
		this.beadBorder = BorderFactory.createTitledBorder("Beads");
		this.listModel = new DefaultListModel<Bead>() ;
		this.beadList =  new JList<Bead>(this.listModel);
		this.scrollPane = new JScrollPane(beadList);
		this.beads = new ArrayList<Bead>() ;
		this.goButton = new JButton("Calibrate");
		this.procrustesButton = new JButton("Deduce Distortion") ;
		this.alignButton = new JButton("Align Stack") ;
		this.AlignFolderButton = new JButton("Align Folder") ;
		this.saveIndividualImagesCheckBox = new JCheckBox("Save As Stack ?") ;
		this.saveIndividualImagesCheckBox.setSelected(false) ;
		this.adaptIntensity = new JCheckBox("Apply intensity correction ?") ;
		this.adaptIntensity.setToolTipText("Apply intensity correction defined in the intensity tab") ; //explain
		
		this.alignBorder = BorderFactory.createTitledBorder("Align Stack") ;
		

		this.activeWindowButton = new JButton("Use this") ;
		this.activeWindowLabel = new JLabel("Use active Window: ");		
		this.activeWindowNameLabel = new JLabel("");
		
		this.fileRadioButton= new JRadioButton("") ;
		this.activeWinRadioButton = new JRadioButton() ;
		this.buttonGroup = new ButtonGroup() ;
		this.buttonGroup.add(fileRadioButton);
		this.buttonGroup.add(activeWinRadioButton);
		this.activeWinRadioButton.setSelected( true );
		
		this.panel3= new JPanel() ;
		this.zCalibFileField= new JFormattedTextField("Calibration File...");
		this.zCalibBrowseButton= new JButton("Browse ") ;
		this.zCalibLabel = new JLabel("Use Calibration file: ");
		this.subImageBorder = BorderFactory.createTitledBorder("Subimage Parameters") ;
		this.offsetField = new JFormattedTextField(this.offsetPredef) ;
		this.aField = new JFormattedTextField(this.aPredef) ;
		this.offsetField.setColumns(8);
		this.aField.setColumns(5) ;
		
		this.aLabel = new JLabel("Spot Integrate Window");
		this.aUnitLabel = new JLabel("[pix]") ;
		this.distToleranceLabel = new JLabel("Spot Searching Window") ;
		this.distToleranceUnitLabel = new JLabel("[pix]") ;
		this.distToleranceField = new JFormattedTextField(this.distTolerancePredef) ;
		this.distToleranceField.setColumns(5) ;
		this.offsetLabel = new JLabel("Background subtraction: ");
		this.coordOffsetLabel = new JLabel("X/Y Offset: ");
		this.dXField = new JFormattedTextField("0");
		this.dYField =  new JFormattedTextField("0");
		this.dXField.setColumns(5);
		this.dYField.setColumns(5);
		this.coordOffsetUnitLabel = new JLabel("[pix]");
		this.coordOffsetUnitLabel2 = new JLabel("[pix]");

		
		this.c1 = new GridBagConstraints() ;
		this.layout1 = new GridBagLayout() ;
		this.c2 = new GridBagConstraints() ;
		this.layout2 = new GridBagLayout() ;
		this.c21 = new GridBagConstraints() ;
		this.layout21 = new GridBagLayout() ;
		this.c3 = new GridBagConstraints() ;
		this.layout3 = new GridBagLayout() ;

		/* Add the Crap */
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS) );
		this.panel13.setLayout(new BoxLayout(panel13,BoxLayout.Y_AXIS)) ;
		panel1.setLayout(layout1);
		this.zStepField.setColumns(10);
		this.zCalibFileField.setColumns(15);
		// set the two fields/browse buttons disabled, as standard option
		// is to use already active window
		this.zCalibFileField.setEnabled(false);
		this.zCalibBrowseButton.setEnabled(false);
		// set the alignment button disabled at the beginning
		this.alignButton.setEnabled(false) ;
		this.AlignFolderButton.setEnabled(false) ;
		
		panel1.setBorder(zData) ;
		this.add(panel13);
		panel13.add(panel1);
		c1.gridx = 0 ;
		c1.gridy = 0 ;
		c1.weightx = 1 ;
		c1.weighty=1;
		c1.fill = GridBagConstraints.BOTH;
		// Create the GridBoxLayout of the window
		panel1.add(zStepLabel,c1);
		++c1.gridx;
		panel1.add(zStepField,c1) ;
		++c1.gridx ;
		panel1.add(zStepUnitLabel,c1) ;
		// New Line
		++c1.gridy;
		c1.gridx=0;
		this.panel1.add(activeWinRadioButton,c1);
		++c1.gridx;
		this.panel1.add(activeWindowLabel,c1);
		++c1.gridx;
		this.panel1.add(activeWindowNameLabel,c1);
		++c1.gridx;
		this.panel1.add(activeWindowButton,c1);

		// NEW LINE
		c1.gridx=0 ;
		++c1.gridy ;
		this.panel1.add(fileRadioButton, c1) ;		
		++c1.gridx;
		this.panel1.add(zCalibLabel,c1);
		++c1.gridx;
		this.panel1.add(zCalibFileField,c1);
		++c1.gridx;
		this.panel1.add(zCalibBrowseButton,c1) ;
		//panel2 
		this.add(panel2) ;
		this.panel2.setLayout(layout2);
		this.panel2.setBorder(beadBorder);
		c2.gridx=0;
		c2.gridy=0;
		c2.weightx=1;
		c2.weighty=1;
		c2.fill = GridBagConstraints.BOTH;
		c2.gridwidth=2;
		this.panel2.add(this.scrollPane,c2);
		++c2.gridy;
		c2.gridwidth=1;
		c2.weighty=0;
		this.panel2.add(goButton,c2) ;
		++c2.gridy;
		this.panel2.add(Box.createVerticalStrut(2),c2);
		++c2.gridy;
		this.panel2.add(procrustesButton,c2) ;
		++c2.gridy;
		// panel 21
		this.panel2.add(panel21,c2) ;
		panel21.setBorder(alignBorder) ;
		panel21.setLayout(this.layout21) ;
		c21.gridx= 0 ;
		c21.gridy=0 ;
		c21.gridwidth=2;
		this.panel21.add(this.adaptIntensity,c21) ;
		c21.gridy++ ;
		c21.fill = GridBagConstraints.BOTH;
		this.panel21.add(alignButton,c21) ;
		c21.gridy++ ;
		c21.gridwidth=1;
		this.panel21.add(saveIndividualImagesCheckBox,c21) ;
		c21.gridx++;
		this.panel21.add(AlignFolderButton,c21) ;

		//panel 3
		this.panel13.add(panel3) ;
		panel3.setLayout(layout3);
		panel3.setBorder(subImageBorder);
		c3.gridx=0;
		c3.gridy=0;
		panel3.add(aLabel, c3);
		++c3.gridx;
		panel3.add(Box.createRigidArea(new Dimension(5,0)), c3);
		++c3.gridx;
		panel3.add(aField,c3);
		++c3.gridx;
		panel3.add(aUnitLabel,c3);
		++c3.gridx;
		panel3.add(Box.createRigidArea(new Dimension(5,0)), c3);
		++c3.gridx;
		panel3.add(offsetLabel,c3);
		++c3.gridx;
		panel3.add(Box.createRigidArea(new Dimension(20,0)), c3);
		++c3.gridx;
		panel3.add(offsetField,c3);
		//new line
		c3.gridy++ ;
		c3.gridx=0 ;
		c3.gridwidth = 1;
		panel3.add(distToleranceLabel, c3) ;
		++c3.gridx;
		panel3.add(Box.createRigidArea(new Dimension(5,0)), c3);
		c3.gridx+=1;
		panel3.add(distToleranceField, c3) ;
		c3.gridx+=1;
		panel3.add(distToleranceUnitLabel, c3) ;
		// new line
		c3.gridx=0;
		c3.gridy++;
		panel3.add(coordOffsetLabel,c3);
		++c3.gridx;
		panel3.add(Box.createRigidArea(new Dimension(5,0)), c3);
		++c3.gridx;
		panel3.add(dXField,c3);
		++c3.gridx;
		panel3.add(coordOffsetUnitLabel, c3);
		++c3.gridx;
		panel3.add(Box.createRigidArea(new Dimension(5,0)), c3);
		++c3.gridx;
		panel3.add(dYField,c3);
		++c3.gridx;
		panel3.add(coordOffsetUnitLabel2, c3);
		// BeadList
		beadList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		beadList.setLayoutOrientation(JList.VERTICAL);
		
		/* Add Listeners */
		this.zCalibBrowseButton.addActionListener(new CalibrationBrowseListener()) ;
		this.addKeyListener(new DeleteListener());
		this.frame.addKeyListener(new DeleteListener());
		this.beadList.addKeyListener(new DeleteListener());
		this.goButton.addActionListener(new CalibrateListener()) ;
		this.procrustesButton.addActionListener(new DeduceDistortionListener()) ;
		this.activeWindowButton.addActionListener(new ActiveWindowListener());
		this.activeWinRadioButton.addItemListener(new RadioChangeListener());
		this.fileRadioButton.addItemListener(new RadioChangeListener()) ;
		/* Create other stuff*/
		this.alignButton.addActionListener(new AlignListener()) ;
		
		// Now instantiate the AlignFolderListener
		// which is a little more complex, since
		// it shows progress on the progressbar....
		AlignFolderListener afl = new AlignFolderListener(this, (MainWindow) frame);
		this.AlignFolderButton.addActionListener(afl) ;
	}
	/*
	 * Adds beads to list and internal list.
	 */
	public void addBead(double x, double y, int a, int distTolerance){
		Bead temp = new Bead(x,y, a, distTolerance);
		beads.add(temp) ;
		this.listModel.addElement(temp) ;
		zProjectCanvas.addCircle((int)x, (int)y, a, distTolerance, this.getCoordOffset()) ;
	}
	/*
	 * Get sampling window width.
	 */
	public String getSampleWindowSize() throws NumberFormatException{
		return this.aField.getText() ;
	}
	/*
	 * This function displays the chosen image
	 * and carries out a z projection.
	 */
	public void handleImage(ImagePlus imp){
		realImgCanvas = new SelectionImageCanvas(imp, null) ;
		GroupedZProjector projector = new GroupedZProjector();
		// set method=1 for max intensity projection.
		this.projection = projector.groupZProject(imp, 1, imp.getImageStackSize()) ;
		zCalib = new ZBeadCalibration( imp, projection ) ;
		zProjectCanvas = new SelectionImageCanvas(this.projection, this) ;
	}
	/*
	 * Find the point of max intensity and set bead there.
	 * (non-Javadoc)
	 * @see gui.MouseToClass#clicked(int, int)
	 */
	@Override
	public void clicked(int x, int y, int f) {
		if(Utils.containsNonAlphaNumeric(this.getSampleWindowSize())){
			IJ.showMessage("Please enter sample box size a [pix]!");
			return ;
		}
		int a = Integer.valueOf(this.getSampleWindowSize());
		int distTolerance = this.getDistTolerance() ;
		// Sample from image, then create overlay and list entry.
		Sample sample = Utils.sampleFromImage2D(projection, x-distTolerance, y-distTolerance, x+distTolerance, y+distTolerance, f); // sample within (2dX,2dY) Box
		// with the click being in the middle
		Point3D<Integer> point = sample.maxIntPixel(); // !! coord. WRT upper left corner of sample box
		int xPos = point.getX()+x-distTolerance;
		int yPos = point.getY()+y-distTolerance;
		this.addBead(xPos, yPos, a, distTolerance) ; // take care of coordinate translation
	}
	/*
	 * Save procrustes & intensity change proposal to File
	 */
	protected void saveTransformationToFile(){
		
		SaveDialog saveDialog = new SaveDialog("Save Transformation", "align_"+realImgCanvas.getImage().getTitle(),".cal") ;
		try{
			String outputFile = saveDialog.getDirectory()+saveDialog.getFileName() ;
	    	// crate file and overwrite if file already exists.
			PrintStream fitOutput = new PrintStream(new FileOutputStream(outputFile));
			// save procrustes data to file
			this.align.saveToFile(fitOutput) ;
			this.zCalib.saveToFile(fitOutput) ;
			fitOutput.close();
		} catch(IOException e){
			IJ.showMessage(e.getMessage()) ;
		}
	}
	protected void readFromFile(String inFile){
		try {
			FileReader fr = new FileReader(inFile);
					
			BufferedReader br = new BufferedReader(fr) ;
			
			align = new Alignment(new ArrayList<Bead>(), zProjectCanvas) ;
			this.align.readFromFile(br) ; // reconstruct from file.
			// now read the intensity information stored in the file.
			int[] intensities =new int[Utils.numFocalPlanes] ;
			double offset = 0 ;
			double zStep =0;
			try {
				for(int i=0; i< Utils.numFocalPlanes; i++){
				
					intensities[i] = Integer.valueOf(br.readLine()) ;
				
				}
			offset = Double.valueOf(br.readLine()) ;
			zStep = Double.valueOf(br.readLine()) ;
			} catch (NumberFormatException e1) {
				LogTab.writeLog(e1.getLocalizedMessage()) ;
			} catch (IOException e2) {
				LogTab.writeLog(e2.getLocalizedMessage()) ;
			}
			// update intensities and offset.
			IntensityCorrectionTab itab = ((IntensityCorrectionTab) ((MainWindow)frame).getTab("intensityTab"));
			itab.updateIntensityInformation(intensities) ;
			MainTab mTab = ((MainTab) ((MainWindow)frame).getTab("mainTab"));
			mTab.updateInfo("FOCALPLANEDISTANCE", zStep) ;
			itab.setConstantOffset(offset) ;
			alignButton.setEnabled(true) ; // now, the stack may be aligned
			AlignFolderButton.setEnabled(true) ;
		} catch (FileNotFoundException e) {
			IJ.showMessage(e.getMessage());
			alignButton.setEnabled(false) ; // now, the stack may not be aligned any more
			AlignFolderButton.setEnabled(false) ;
		}
	}
	public int getDistTolerance() throws NumberFormatException{
		return Integer.valueOf(this.distToleranceField.getText()) ;
	} 
	public int[] getCoordOffset() throws NumberFormatException{
		int x = Integer.valueOf(this.dXField.getText()) ;
		int y = Integer.valueOf(this.dYField.getText());
		return new int[]{x,y} ;
	}
	public double getIntensityOffset() throws NumberFormatException{
		return Double.valueOf(offsetField.getText()) ;
	}
	public double getZStep() throws NumberFormatException{
		return Double.valueOf(zStepField.getText()) ;
	}
	/*
	 * BrowseButton ActionListener
	 */
	protected class BrowseListener implements ActionListener{
		BrowseListener(){}
		public void actionPerformed( final ActionEvent arg0 ) { // currently unused
				JFileChooser fc = new JFileChooser();
				fc.setAcceptAllFileFilterUsed(false);
				fc.addChoosableFileFilter(new BeadFilter()) ;

				int returnVal = fc.showOpenDialog(frame ) ;
				if ( returnVal == JFileChooser.APPROVE_OPTION)
				{
					java.io.File file = fc.getSelectedFile() ;
					ImagePlus imp = new ImagePlus(file.getAbsolutePath()) ;
					activeWindowNameLabel.setText(imp.getTitle()) ;
					handleImage(imp); 
				}

		}
		protected class BeadFilter extends FileFilter {
			 
		    //Accept all directories and all gif, jpg, tiff, or png files.
		    public boolean accept(File f) {
		        if (f.isDirectory()) {
		            return true;
		        }
		 
		        String extension = Utils.getExtension(f);
		        if (extension != null) {
		            if (extension.equals(Utils.tif)) {
		                    return true;
		            } else {
		                return false;
		            }
		        }
		 
		        return false;
		    }
		    //The description of this filter
		    public String getDescription() {
		        return ".tif (Bead Image File)";
		    }
		}
	}
	/*
	 * Calibration Browse Listener
	 */
	protected class CalibrationBrowseListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			fc.setAcceptAllFileFilterUsed(false);
			fc.addChoosableFileFilter(new CalibrationFilter()) ;

			int returnVal = fc.showOpenDialog(frame ) ;
			if ( returnVal == JFileChooser.APPROVE_OPTION)
			{
				java.io.File file = fc.getSelectedFile() ;
				zCalibFileField.setText(file.getAbsolutePath()) ;	
				readFromFile(file.getAbsolutePath()) ;
			}
		}
		protected class CalibrationFilter extends FileFilter {		 
		    //Accept all directories and all gif, jpg, tiff, or png files.
		    public boolean accept(File f) {
		        if (f.isDirectory()) {
		            return true;
		        }
		 
		        String extension = Utils.getExtension(f);
		        if (extension != null) {
		            if (extension.equals(Utils.cal)) {
		                    return true;
		            } else {
		                return false;
		            }
		        }
		        return false;
		    }
		    //The description of this filter
		    public String getDescription() {
		        return ".cal Files";
		    }
		}
	}
	/*
	 * Key Listener
	 */
	protected class DeleteListener implements KeyListener{
		@Override
		public void keyTyped(KeyEvent e) {
			//IJ.showMessage("Hallo") ;
			int code=e.getKeyCode();
			if(code==KeyEvent.VK_DELETE ){
				if( beadList.isSelectionEmpty()){
					IJ.showMessage("No spot selected!");
					return ;
				}
				// some number that can never be reached
				int index = beadList.getSelectedIndex();
				listModel.remove(index) ; // remove from listModel
				beads.remove(index); // remove from beadlist
				zProjectCanvas.deleteCircle(index) ; // delete ALL respective circles 
				//spotsList.setModel(listModel) ; // update
			}
			}
		@Override
		public void keyPressed(KeyEvent e) {
			keyTyped(e);
		}
		@Override
		public void keyReleased(KeyEvent e) {
			//keyTyped(e);			
		}		
	}
	/*
	 * ActiveWindowListener: The ActionListener for the use active
	 * window button.
	 */
	protected class ActiveWindowListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			ImagePlus imp = WindowManager.getCurrentImage();
			activeWindowNameLabel.setText(imp.getTitle()) ;
			handleImage(imp); // create z Projection etc.
		}
	}


	/*
	 * Calibrate Listener
	 */
	protected class CalibrateListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			// Test if input is non-alphanumeric
			if(Utils.containsNonAlphaNumeric(zStepField.getText())){
				IJ.showMessage("Please enter z step in nanometers!");
				return;
			} 
			if ( Utils.containsNonAlphaNumeric(aField.getText())){
				IJ.showMessage("Please enter a, the size of the box from which will be sampled!");
				return ;
			} 

			// now we have to update the physical parameters of the experiment!!!
			try {
				double wavelength = ((MainTab) ((MainWindow)frame).getTab("mainTab")).getWavelength();
				double diffIndex = ((MainTab) ((MainWindow)frame).getTab("mainTab")).getDiffIndex();
				double NA = ((MainTab) ((MainWindow)frame).getTab("mainTab")).getNA();
				GaussianFit.setPhysicalParameters(wavelength, NA, diffIndex) ; //store values
				// do the calibration
				avgFocalPlaneDistance = zCalib.calibrate(getZStep(), 
						getIntensityOffset(), beads, getCoordOffset()) ;
				// let people know...
				IJ.showMessage("Average focal plane distance calculated and stored:"+ String.format("%.2f",avgFocalPlaneDistance)) ;
				LogTab.writeLog("Average focal plane distance calculated and stored:"+ String.format("%.2f",avgFocalPlaneDistance)) ;
				
				((MainTab) ((MainWindow)frame).getTab("mainTab")).updateInfo("FOCALPLANEDISTANCE", avgFocalPlaneDistance) ;
				IntensityCorrectionTab tab = ((IntensityCorrectionTab) ((MainWindow)frame).getTab("intensityTab"));
				tab.updateIntensityInformation(zCalib.getAveragedIntensities());
				tab.setConstantOffset(getIntensityOffset()) ; // make the two offsets equal !
				
			} catch(NumberFormatException exc){
				IJ.showMessage("Please enter valid numbers for the Wavelength, index of diffraction and/or numerical aperture!") ;
				return ;
			}
			
		}
	}
	/*
	 * DeduceDistortionListener
	 */
	protected class DeduceDistortionListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			if ( beads.isEmpty() || zProjectCanvas == null){
				IJ.showMessage("Please select beads and/or add beadcalibration image") ;
				return ;
			}
			align = new Alignment(beads, zProjectCanvas) ;
			align.align(getCoordOffset());
			
			alignButton.setEnabled(true) ; // now, the stack may be aligned
			AlignFolderButton.setEnabled(true) ;
			saveTransformationToFile(); // save the transformation
		}
	}
	protected class AlignListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			ImagePlus realImg = ((MainTab) ((MainWindow)frame).getTab("mainTab")).getZStack();
			if(realImg == null){
				IJ.showMessage("Error: No ImageStack chosen!\n " +
								"Please go to the General tab and click on 'This one'.") ;
				return ;
			}
			// adapt intensity of subimages according to choices made on intensitycorrection tab?
			int[] intensities ;
			double offset =0;
			IntensityCorrectionTab tab = ((IntensityCorrectionTab) ((MainWindow)frame).getTab("intensityTab"));
			if( adaptIntensity.isSelected()){
				intensities = tab.getIntensityFactors() ;
				offset = tab.getConstantOffset();
			} else {
				intensities = null ;
			}
			// go!
			ImagePlus aligned = align.alignWholeStack(realImg, offset, intensities ) ;
			aligned.show(); // show newly aligned image, so that user can save it.
		}
	}
	/*
	 * What to do, when AlignFolderButton is pressed ?
	 */
	protected class AlignFolderListener implements ActionListener {
		private JComponent parent ;
		private MainWindow mw;
		
		public AlignFolderListener(JComponent parent, MainWindow mw){
			this.parent = parent ;
			this.mw = mw ;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Worker worker = new Worker(!saveIndividualImagesCheckBox.isSelected()) ;
				worker.addPropertyChangeListener( mw);
				worker.execute() ;
				mw.setCurrentWorker(worker) ;

			} catch (Exception e1) {
				LogTab.writeLog(e1.getLocalizedMessage()) ;
			}
		}
		protected class Worker extends SwingWorker<Void, Integer>{
			private Boolean success ;
			private boolean saveIndividually = false;
			
			public Worker(boolean saveIndividually){
				this.success =  new Boolean(false) ;
				this.saveIndividually = saveIndividually ;
			}
			@Override
			protected Void doInBackground() throws Exception {
				/*
				 * (1) Open a certain Folder.
				 */
				JFileChooser fc = new JFileChooser();
				// open directories only
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnval = fc.showOpenDialog(parent) ;
				
				if(returnval == JFileChooser.APPROVE_OPTION){ //ok, valid path
					File folder = fc.getSelectedFile() ;
					LogTab.writeLog("Start aligning Folder... "+folder.getAbsolutePath()) ;
					/*
					 * (2) For every file in the folder...
					 */
					File[] files = folder.listFiles(
							new FilenameFilter(){
								@Override
								public boolean accept(File dir, String name) {
									return Utils.accept(new File(dir, name)); // joint path
								}
							}
							) ;
					// 2.1 get intensities
					int[] intensities ;
					double offset = 0;
					IntensityCorrectionTab tab= ((IntensityCorrectionTab) ((MainWindow)frame).getTab("intensityTab"));
					if( adaptIntensity.isSelected()){
						intensities = tab.getIntensityFactors() ;
						offset = tab.getConstantOffset() ;
						
					} else {
						intensities = null ;
					}
					Opener opener = new Opener() ;
					boolean success= (new File(folder.getParentFile(),"Aligned")).mkdir() ; // create new directory within the folder
					if(!success){
						IJ.showMessage("Creation of the directory to place the images in failed!") ;
						LogTab.writeLog("Creation of the directory to place the images in failed!") ;
						return null ;
					}
					// open the new folder
					File newFolder = new File(folder.getParentFile(), "Aligned") ;
					
					setProgress(0); // signal beginning of process
					int i =0; //counter
					LogTab.writeLog("Alignment procedure of folder "+folder+" started!") ;

					
					for(File file: files){ // for every image...
						String oldName = file.getName() ;
						ImagePlus imp = opener.openImage((new File(folder, oldName)).getAbsolutePath() ) ;
						/*
						 * (3) Align the image !
						 */
						ImagePlus aligned = align.alignWholeStack(imp, offset,intensities) ; // imp is now aligned and has to be saved
						String[] puzzle = oldName.split("\\."); // split at point
						// 3.1 create new name
						String newName = "blub.tif" ;
						
						/*
						 * (4) Save the image!
						 * We don't need to free the space, as the garbage collector will take care of it!
						 */	

						if(saveIndividually){
							
							for(int k=0; k< Utils.numFocalPlanes; k++){
								
								newName = puzzle[0]+"_Aligned_"+(k+1)+ "."+puzzle[1] ;
								aligned.setPositionWithoutUpdate(1, k+1, 1);
								ImageProcessor proc = aligned.getProcessor();
								
 								ImagePlus slice = new ImagePlus( newName, proc);
								
								IJ.saveAsTiff( slice,
										((new File(newFolder, newName)).getAbsolutePath()));
							}
						} else {
							newName = puzzle[0]+"_Aligned."+puzzle[1] ;
							IJ.saveAsTiff(aligned, ((new File(newFolder, newName)).getAbsolutePath())) ;
						}
						// keep track of the progress
						i++ ;
						double progress = i*100.0/files.length ;
						setProgress((int) progress) ;
						aligned.close() ;
						imp.close() ;
					}
					setProgress(0) ;
					this.success =  new Boolean(true) ;
					
				}			
				return null;
			}
			 @Override
		     protected void done() {
				 if(success.booleanValue()){
					LogTab.writeLog("Its Done! All Images are aligned in the new Folder ./Aligned/ !") ;
					IJ.showMessage("Its Done! All Images are aligned in the new Folder ./Aligned/ !"  ) ;

				 } else{
					 LogTab.writeLog("Alignment of folder aborted!");
					 IJ.showMessage("Alignment of folder aborted!") ;
				 }
				 mw.unsetCurrentWorker();
			 }
		}
	}
	/*
	 * ItemListener for the radio buttons
	 */
	protected class RadioChangeListener implements ItemListener{

		@Override
		public void itemStateChanged(ItemEvent e) {
			if(fileRadioButton== e.getSource()
					&& fileRadioButton.isSelected()){
				zCalibFileField.setEnabled(true);
				zCalibBrowseButton.setEnabled(true);
				activeWindowButton.setEnabled(false);
			} else if(activeWinRadioButton == e.getSource()
					&& activeWinRadioButton.isSelected()){
				zCalibFileField.setEnabled(false);
				zCalibBrowseButton.setEnabled(false);
				activeWindowButton.setEnabled(true);
			} 
		}
	}
}
