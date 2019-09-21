package processing;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.PathIterator;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingWorker;

import net.imglib2.multithreading.SimpleMultiThreading;

import lib.Comparer;
import lib.FindLocalMaxima;
import lib.FindLocalMaxima.FilterType;
import lib.ImageProcessor3D;
import lib.Point3D;
import lib.Sample;
import lib.SpotTZList;
import lib.SpotTree;
import lib.Utils;
import gui.FilterDialog;
import gui.LogTab;
import gui.MainWindow;
import gui.SelectionImageCanvas;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.EllipseRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.io.SaveDialog;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.GaussianBlur;
import ij.process.ImageProcessor;

public class SpotMother extends SwingWorker<Void, Integer>{
	/*
	 * private members
	 */
	private ImagePlus imp;
	private Overlay ov ;
	
	private boolean saveToFile = true ;
	private int a = 3;
	private int aInt = 5;
	private int pInt = 2;
	private int p = 1 ;
	private int noise = 300 ;
	private FindLocalMaxima flm ;
	private String fileName ;
	private double pixSize;
	private double dZ;
	private boolean applyFilter;
	private boolean applyParameterFilter;
	private int ruleNumber =2 ;
	private boolean symmetric =true ;

	private MainWindow mw ;
	private Comparer comparer;
	
	/*
	 * Constructor
	 */

	public SpotMother(ImagePlus imp_, int boxWidth, int zBoxWidth, int fitboxWidth, int zFitBoxWidth, int noiseThreshold, int ruleNumber){
		imp = imp_; // instance that we gonna play with
		ov = new Overlay() ;
		imp.setOverlay(ov) ;

		a = boxWidth;
		p = zBoxWidth ;
		aInt = fitboxWidth;
		pInt = zFitBoxWidth;
		flm = new FindLocalMaxima() ;
		noise =  noiseThreshold ;
		this.ruleNumber = ruleNumber ;
		
	}
	/*
	 * Run only on current slice/frame
	 * only draw an overlay on top of detection.
	 */
	public int runOnCurrentImage(boolean applyFilter){

		ImagePlus ip =imp ; // dummy reference
		int frame = imp.getT();
		int slice = imp.getSlice();
		if( applyFilter){
			imp.setPosition(1, slice, frame) ;
			ip = SpotMother.filterImage(imp);
			frame = 1 ;
		} 
		ImagePlus substack = Utils.extractFrame(ip, frame) ;

		Rectangle rect =  imp.getProcessor().getRoi();
		//substack.setRoi(rect) ; 
		
		ArrayList<Spot> list = new ArrayList<Spot>() ;
		flm.FindSpotsPreview(substack,rect,list, a, p,  noise,  ruleNumber) ;
		for(Spot spot : list){
			Point3D<Double> point = spot.getPosition() ;
				int x = (int) (point.getX().doubleValue()-0.5);
				int y = (int) (point.getY().doubleValue()-0.5);
				int spotSlice = spot.getSlice();
				if(spotSlice == slice){
					Roi roi =new Roi(x, y, 2*a+1, 2*a+1, 1);
					roi.setStrokeColor(Color.green);
					ov.add(roi ) ;
				} else {
					Roi roi =new Roi(x, y, 2*a+1, 2*a+1, 1);
					roi.setStrokeColor(Color.blue);
					ov.add(roi ) ;
				}
		}
		/*Polygon poly = FindLocalMaxima.FindMax(substack,rect, a, p,slice ,frame,  noise,  ruleNumber) ;
		
        for (int i = 0; i < poly.npoints; i++) {
           int x = poly.xpoints[i];
           int y = poly.ypoints[i] ;
           Roi roi =new Roi(x-a, y-a, 2*a+1, 2*a+1, 1);
           roi.setStrokeColor(Color.green);
           ov.add(roi ) ;
        }*/
        imp.updateAndDraw() ;

		/*
		List<Spot> foundSpots = FindLocalMaxima.FindSpotsOnCurrentSlice(imp.duplicate(), a, p, imp.getT(), noise, FilterType.NONE) ;
		for(Spot spot : foundSpots){
			int x = (int)spot.getPosition().getX().doubleValue();
			int y = (int)spot.getPosition().getY().doubleValue();
			int z = (int)spot.getPosition().getZ().doubleValue();
			if(imp.getCurrentSlice()== z+1) // print only on the right z-slice
				ov.add(new Roi(x-a, y-a, 2*a, 2*a, 1));
		}
		imp.updateAndDraw();*/
        return list.size();
	}
	/*
	 * Set parameters later, that are only used for actual fitting of spots.
	 */
	public void setParams(String fN, double pS, double dZ, boolean applyFilter, boolean applyParameterFilter, MainWindow mw, Comparer comparer, boolean symmetric){
		this.fileName = fN;
		this.pixSize=pS;
		this.dZ = dZ ;
		this.applyFilter = applyFilter;
		this.applyParameterFilter = applyParameterFilter ;
		this.mw= mw ;
		this.comparer = comparer ;
		this.symmetric = symmetric ;
	}

	public Void doInBackground() throws Exception{
		/*
		 * (1) 	The first Step is to open a printstream with the file
		 * 		We have to account for the Exception here.
		 */
		// hide the image, because for some reason
					// the setPositionWithoutUpdate doesn't really work

		try{
			PrintStream ps = new PrintStream(new FileOutputStream(fileName));
			// Log the beginning of the writing process
			LogTab.writeLog("Start writing to file " +fileName) ;
			/*
			 * (2) 	The next Step is to open the frame-for loop
			 * 		and to detect all spots on a certain frame.
			 * 		We clear the spotList every frame, since
			 * 		otherwise we run into memory issues.
			 * 		The spotList was not designed for this, but ok.
			 */
			ArrayList<Spot> list = new ArrayList<Spot>() ;
			
			setProgress(0); // signal beginning of process
			int nFrames = imp.getNFrames();
			String delim = "\t" ;// delimiter of mohameds program
			

			for(int i=0; i< nFrames; i++ ){ // for EVERY Frame
				// clear the list
				list.clear();
				// now get the substack of this frame
				ImagePlus substack = Utils.extractFrame(imp, i+1) ;
				
				// Do the localization....
				this.step(substack, list, i+1) ;
				
				setProgress( (int) (i* 100.0/(nFrames-1)) ) ; // signal progress 
				/*
				 * (4)	Write localizations to file!
				 */
		
				for(Spot spot : list){
					Gaussian3DSpot gSpot = (Gaussian3DSpot) spot ;
					if( gSpot.wasRun()){
							boolean accept = true ;
							if(applyParameterFilter){
								accept = comparer.compareSpot(gSpot); // after filtering !!!
							}
							if(accept){
								Point3D<Double> pos  = gSpot.getRefinedPosition() ; // get ACTUAL REALIMAGEPOSITION!
								Roi roi = new Roi(pos.getX()-0.1, pos.getY()-0.1, 0.2,0.2) ;
								roi.setStrokeColor(Color.red) ;
								ov.add(roi) ; // mark localization

								double[] sigmas = gSpot.getSigmas();
								ps.println(String.format("%.2f", pixSize*pos.getX())+delim+String.format("%.2f", pixSize*pos.getY())+delim+String.format("%.2f", dZ*pos.getZ()) 
											+delim+String.format("%.2f", sigmas[0]*pixSize)+delim+String.format("%.2f", sigmas[1]*pixSize)+delim+String.format("%.2f", sigmas[2]*dZ)
											+delim+String.format("%.2f", gSpot.getIntensity())+delim+gSpot.getFrame());
							}
					}
				}
			}
			// close the printstream again.
			ps.close() ;
			// set progress again to zero
			setProgress(0) ;

		}catch(IOException exc){
			IJ.showMessage(exc.getMessage()) ;
		}	
		// statisfy Void
		return null ; 
	}
	
	
	 @Override
     protected void done() {
			mw.setGo(true) ; // make the go button accessible again
			mw.unsetCurrentWorker();
			LogTab.writeLog("Its Done! All data written to "+fileName);
			IJ.showMessage("Its Done! All data written to "+fileName) ;
	 }
	 public void step(ImagePlus substack,List<Spot> list, int frame){
		// now detect spots on frame i+1, fill the list
			Rectangle rect = imp.getProcessor().getRoi(); 
			//substack.setRoi(rect) ;
			flm.FindSpots(substack,rect,list, a, p, aInt, pInt,  frame ,noise, applyFilter, ruleNumber, symmetric) ;
						
			/*
			 * (3) 	We now fit the gaussians to the detections 
			 * 		on this frame using multithreading.
			 * 		To this end, we create a stack of threads.
			 */
			Thread[] threads = new Thread[list.size()];
			int k=0;
			for(Spot spot: list){
				Point3D<Double> p = spot.getPosition();
				
				threads[k] = new Thread(spot); // create new thread
				k++ ;
			}
			// Start AND Join all the threads here - 
			// there is probably a more efficient, less time consuming
			// way of handling this parallelism but ok... I don't care right now.
			SimpleMultiThreading.startAndJoin(threads) ;
			// Display all the collected error messages of the spots !
			for(Spot spot : list){
				String errMsg = spot.getErrMsg();
				if(errMsg != "")
					LogTab.writeLog(errMsg) ;
			}
	 }

	/*
	 * Setter Methods
	 */
	public void setA(int a_){
		a = a_ ;
	}
	public void setP(int p_){
		p = p_ ;
	}
	public void setNoiseThreshold(int noise_ ){
		noise = noise_ ;
	}
	public static ImagePlus filterImage(ImagePlus toFilter){
		  GaussianBlur filter_ = new GaussianBlur();
		  ImageCalculator ic_ = new ImageCalculator();
		  int slice = toFilter.getSlice();
		  int frame = toFilter.getT();
		  ImageStack stack = new ImageStack(toFilter.getWidth(), toFilter.getHeight()) ;
		 for(int i=0; i< toFilter.getNSlices(); i++){ 
			 toFilter.setPositionWithoutUpdate(1, i+1,frame ) ;
			 ImageProcessor iProc = toFilter.getProcessor();
		     ImageProcessor iProcG1 = iProc.duplicate();
		     ImageProcessor iProcG5 = iProc.duplicate();
		     filter_.blur(iProcG1, 1);
		     filter_.blur(iProcG5, 5);
		     ImagePlus p1 = new ImagePlus("G1", iProcG1);
		     ImagePlus p5 = new ImagePlus("G5", iProcG5);
		     ic_.run("subtract", p1, p5);
		     stack.addSlice(p1.getProcessor()) ;
		 }
		 toFilter.setPositionWithoutUpdate(1, slice, frame); // leave image unchanged
	     return new ImagePlus("filtered_"+toFilter.getTitle(), stack);   
	}
	/*
	protected void writeToFile(String fileName, double pixSize, double dZ){
		//SaveDialog saveDialog = new SaveDialog("Save 3D Localizations","loc_"+imp.getTitle() ,".3d") ;
		//String fileName= saveDialog.getDirectory()+saveDialog.getFileName() ;
		ov.setStrokeColor(Color.red) ;
		String delim = "\t" ;

		try{
			PrintStream ps = new PrintStream(new FileOutputStream(fileName));
			for(Spot spot : spotList){
				Gaussian3DSpot gSpot = (Gaussian3DSpot) spot ;
				if( gSpot.wasRun()){
					Point3D<Double> pos  = gSpot.getRefinedPosition() ;
					Roi roi = new Roi(pos.getX()-0.1, pos.getY()-0.1, 0.2,0.2) ;
					ov.add(roi) ;
					ps.println(String.format("%.2f", pixSize*pos.getX())+delim+String.format("%.2f", pixSize*pos.getY())+delim+String.format("%.2f", dZ*pos.getZ()) 
						+delim+String.format("%.2f", gSpot.getIntensity())+delim+gSpot.getFrame());
				}
			}
			ps.close() ;
		} catch(IOException e){
			IJ.showMessage(e.getMessage()) ;
		}

	}*/

}
