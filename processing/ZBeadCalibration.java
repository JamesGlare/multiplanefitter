package processing;


import java.awt.Color;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.jfree.data.xy.XYSeries;

import lib.Bead;
import lib.GaussianFit;
import lib.GaussianUtils;
import lib.Point3D;
import lib.Sample;
import lib.Utils;
import gui.LogTab;
import ij.ImagePlus;
import ij.gui.Plot;

/**************************************
 * ZBeadcalibration Class
 * @author james
 * The purpose of this class is to 
 * provide the means to derive the 
 * positions of the focal planes.
 * Due to the spectral aberration of wavelengths
 * different from the one used to create the 
 * grating, the z-distance betweeen the planes is unknown.
 * Description of procedure:
 * (1) Choose and display a z bead calibration
 * image.
 * (2) User chooses Beads by clicking on 
 * the image. An overlay affirms the choice.
 * (3) The corresponding beads are displayed
 * in the 8 other non-central subimages.
 * This is done by simple vector translation. 
 * The size of the subimages is determined by 
 * simple division of the total image edge length
 * by 3.
 * (4) Each image in the stack corresponds
 * to a different point in z (and time), the stack
 * is a time series of multifocal images which were taken
 * at a different height with KNOWN spacing dZ.
 * The intensity of the SUM OF ALL PIXELS INSIDE
 * A BOX OF CERTAIN LENGTH (to determine) AROUND
 * THE CHOOSEN BEADS for the corresponding peak 
 * on EACH subimage is plotted over the (slice number)*dZ.
 * This done for every chosen bead.
 * (5) For EACH Bead: The zPositions of 
 * maximum intensity  are plotted over
 * the SUBIMAGE number n.
 * Fit a linear function f(n)=a*zPos+b
 * through these points. This kind'a
 * averages the deltaZ between the subimage planes.
 * (6) Average the deltaZ over all Beads.
 **************************************/
public class ZBeadCalibration {
	/*
	 * Private Member
	 */
	private ImagePlus rawImp ; // zbead calibration image
	private ImagePlus zProject ;
	
	private final Color[] plotColors ={Color.black,Color.pink, 
									Color.blue, Color.cyan, 
									Color.green, Color.yellow, 
									Color.orange, Color.red, Color.magenta} ;
	private double[][][] gaussResults; // to store all the Intensities 
									  // extracted during the calibration.
	private double offset=0 ;
	private double zFocalDistance =0;
	
	/**************************************
	 * ZBeadCalibration Constructor.
	 * Constructs an instance of ZBeadCalibration.
	 * @param imp The full stack of uncut, 
	 * uncalibrated images.
	 **************************************/
	public ZBeadCalibration(ImagePlus imp, ImagePlus zProject){
		this.rawImp = imp ;
		this.zProject = zProject ;
	}
	/***************************************
	 * Calibration of the nine focal planes.
	 * @return list of 9 z positions.
	 * @param zstep between the slices 
	 * (not the subimages) in nanometers.
	 ***************************************/
	public double calibrate(double zstep, double offset_, List<Bead> beadList, int[] coordOffset){
		// STEP 1: Get the summed Intensities over z for each bead.
		int stackSize = rawImp.getImageStackSize();
		double[][][] intensities = new double[beadList.size()][Utils.numFocalPlanes][stackSize] ;
		double[] z = new double[stackSize];
		int[] dimensions = rawImp.getDimensions();
		double[] maximums = new double[beadList.size()]; // store the max to scale graphs.
		offset = offset_ ; // store the offset to write it in the file later on.. 
		
		ImagePlus imp = rawImp.duplicate() ;

		for(int k=0; k< beadList.size(); k++){ // for every bead
			maximums[k]=0;
			for(int f=0; f<stackSize; f++){ // for every slice
				
				// Step 0.9: Subtract the offset - but only once!
				if( k== 0){
					imp.setSlice(f+1);
					imp.getProcessor().subtract(offset) ;
				}
				for(int j=0; j<Utils.numFocalPlanes; j++ ){ // on every subimage
					int xCenter = (int)beadList.get(k).getX() ;
					int yCenter = (int) beadList.get(k).getY();
					int[] vec = Utils.sameSpotInSubImage(xCenter, yCenter, coordOffset[0], coordOffset[1], j, dimensions) ;
					int width =beadList.get(k).getDistTolerance();
					// getProcessor is 1-based counter! -> f+1
					Sample sample = Utils.sampleFromImage2D(zProject, vec[0]-width, vec[1]-width, 2*width+1, 1) ;
					// Now realign the sample to the maximum intensity pixel ON THE ZPROJECTION
					Point3D<Integer> maxIntPixel = sample.maxIntPixel() ;
					int xRefined = maxIntPixel.getX() -width + vec[0] ;
					int yRefined = maxIntPixel.getY()-width + vec[1] ;
					
					int a = beadList.get(k).getA();
					// resample from the REAL Image with the integration width a
					Sample refinedSample = Utils.sampleFromImage2D(imp, xRefined-a, yRefined -a, 2*a+1, f+1) ;
					intensities[k][j][f]= refinedSample.sumInt()/((2*a+1)*(2*a+1));
					maximums[k]= Math.max(maximums[k], intensities[k][j][f]);
					//System.out.println(f+" " +j+" "+vec[0]+" "+vec[1]+ " "+sample.sumInt()) ;
				}
				z[f] = f*zstep ;
			}
		}
		
		// STEP 2: Fit a gaussian to each bead
		double[][] positions= new double[beadList.size()][Utils.numFocalPlanes] ;
		this.gaussResults = new double[beadList.size()][Utils.numFocalPlanes][] ;
		for(int k=0; k< beadList.size(); k++){
			for(int j=0; j< Utils.numFocalPlanes; j++){
				// Create new Sample etc.
				Sample sample = new Sample(intensities[k][j]) ;
				GaussianFit fit = new GaussianFit(1, sample, zstep,false, false, true);
				double[] sigmaEstimate = GaussianFit.estimateSigmaPhysics();
				sigmaEstimate[0] = sigmaEstimate[2]; // we're only interested in the z-estimate !
				GaussianFit.changeSigmaEstimate(sigmaEstimate) ;
				double[] paramsOut;
				try {
					paramsOut = fit.doFit();
					//System.out.println(sigmaEstimate +" "+paramsOut[3] ) ;
					// Check if value is reasonable, if not take max. intensity estimate
					positions[k][j] = paramsOut[2]  ;
					this.gaussResults[k][j] =  paramsOut  ;
				} catch (OptimizationException e) {
					LogTab.writeLog("Intensity Curve Gaussian Fitting Exception "+e.getLocalizedMessage()) ;
				} catch (IllegalArgumentException e) {
					LogTab.writeLog("Intensity Curve Gaussian Fitting Exception "+e.getLocalizedMessage()) ;
				}
				
			}
		}
		// STEP 3: Plot this for each Bead. So that the user can have a look at it (users usually wanna see stuff)
		XYSeries[][] series = new XYSeries[beadList.size()][2*Utils.numFocalPlanes];	
		
		for(int j=0; j< beadList.size(); j++){	
				//Plot plot = new Plot(j+": "+beadList.get(j).toString(), "z Coordinate [nm]", "Sum of Intensities per Area") ;
				//plot.setLimits(0, (stackSize-1)*zstep, 0, maximums[j]*1.1) ;
				for(int i=0; i< Utils.numFocalPlanes; i++){
						series[j][i] = new XYSeries("("+j+", "+i+")") ;
						series[j][i+Utils.numFocalPlanes] = new XYSeries("FIT: ("+j+", "+String.valueOf( i )+")") ;

						for(int k=0; k< intensities[j][i].length; k++){
							double val2 = intensities[j][i][k] ;
							double val1 =   GaussianUtils.gaussian1D(gaussResults[j][i],(int) z[k]) ;
							series[j][i].add(z[k],val1) ;
							series[j][i+Utils.numFocalPlanes].add(z[k], val2) ;
						}
						//plot.setColor(plotColors[i]);
						//plot.addPoints(z, intensities[j][i], Plot.LINE) ;
						//plot.draw();
				}
				GaussianUtils.plotDataN(j+": "+beadList.get(j).toString(),
						series[j], "z Coordinate [nm]", "Sum of Intensities per Area", 0, 0 , false, false);
				//plot.show();
			}

		// STEP 4: draw a line through all points.
		double avgFocalPlaneDistance = 0 ;
		double[][] temp =new double[beadList.size()][2]; // storage
		for(int k=0; k< beadList.size(); k++){
			SimpleRegression regress = new SimpleRegression();// create regression instance WITH intercept
			for(int j=0; j< Utils.numFocalPlanes; j++){
				regress.addData(j, positions[k][j]);
				
			}
			avgFocalPlaneDistance+=regress.getSlope();
			// store slope and intercept to plot the regressed line in step 5.
			temp[k][0] = regress.getSlope();
			temp[k][1] = regress.getIntercept();
		}
		avgFocalPlaneDistance /= beadList.size();
		// STEP 5: Output that line! (messy!)
		double[] plotX = new double[Utils.numFocalPlanes];
		double[] interpolation =new double[Utils.numFocalPlanes] ;
		for(int k=0; k< beadList.size(); k++){
			// initialize the plot data 
			for(int l=0; l< Utils.numFocalPlanes; l++){
				plotX[l]=Utils.mappingOrder[l];
				interpolation[l] = temp[k][0]*l+temp[k][1];
			}
			Plot plot = new Plot(k+": "+beadList.get(k).toString(), "Focalplane index", "z Coordinate of Gaussian Center") ;
			plot.setLimits( Utils.mappingOrder[0], Utils.mappingOrder[Utils.numFocalPlanes-1], 0, (stackSize)*zstep ) ;
			plot.setColor(Color.black);
			plot.addPoints(plotX ,positions[k], Plot.LINE) ;
			plot.draw();
			plot.setColor(Color.blue);
			plot.addPoints(plotX, interpolation, Plot.LINE) ;
			plot.draw();
			plot.show();
		}
		this.zFocalDistance = avgFocalPlaneDistance ;
		return avgFocalPlaneDistance;
	}
	/*
	 * Getter Method for intensities of beads in all slices.
	 */
	public int[] getAveragedIntensities(){
		int[] out = new int[Utils.numFocalPlanes];
		for(int i = 0; i< Utils.numFocalPlanes; i++){
			out[i]=0;
			if( gaussResults !=null )
				for(int j = 0; j < gaussResults.length; j++){ // #beads		
						out[i]+= (int) ( gaussResults[j][i][0]/ (gaussResults.length) );
				}
		}
		return out ;
	}
	public void saveToFile(PrintStream file){
		
		int[] avgInt = this.getAveragedIntensities() ;
		
		for(int i=0; i< Utils.numFocalPlanes; i++){
			file.println(avgInt[i]) ;
		}
		file.println(offset) ;
		file.println(zFocalDistance);
	}
}
