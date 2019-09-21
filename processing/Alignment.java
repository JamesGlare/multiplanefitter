package processing;

import gui.IntensityCorrectionTab;
import gui.SelectionImageCanvas;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.optimization.OptimizationException;
import org.jfree.data.xy.XYSeries;

import lib.GaussianFit;
import lib.GaussianUtils;
import lib.Point3D;
import lib.Procrustes;
import lib.Sample;
import lib.Utils;

import lib.Bead;

/**************************************
* Class Alignment
* Alignment procedure in a nut shell.
* (1) Get the N beads chosen by the user
* in the central subimage.
* (2) Find an estimate for the bead
* position in the other subimages.
* Fit 2D symmetric integrated gaussians
* in the region of interest determined by
* the estimated spot position and the length a (user input).
* (3) For each subimage:
* Store the position of the N non-central beads
* in a 2xN matrix 
* ( x1' x2' x3' x4' ... xN' )     ( x1 x2 ... xN )
* ( y1' y2' y3' y4' ... yN' ) = A*( y1 y2 ... yN ) + B
* the matrices A is a 2x2 matrix and B a 2x1 vector.
* The positions are measured wRt the respective subimage left upper corner.
* (4) The 6 parameters a1...a4, b1, b2 are determined
* by a procrustes analysis which automatically
* determines a geometrical transformation
* to map the central spots to the respective 
* subimage spots.
* (5) Then we know the transformation for each
* subimage and we may apply the inverse. (???)
**************************************/
public class Alignment {
	/*
	 * Alignment.
	 */
	private ArrayList<Bead> beads ;
	private ImagePlus zProject ;
	private SelectionImageCanvas canvas ;
	private List<Procrustes> procrusteses ;

	/*
	 * Constructor
	 */
	public Alignment(ArrayList<Bead> beads_, SelectionImageCanvas canvas_){
		beads = beads_ ; // list of beads in central image.
		canvas = canvas_ ;
		if ( canvas !=null && canvas.getImage() != null)
			zProject = canvas.getImage();
		else
			zProject = null ; // placeholder
		this.procrusteses= new ArrayList<Procrustes>(Utils.numFocalPlanes-1) ; // just declare...
	}
	/*
	 * Deduce distortion and align the z projection as a preview.
	 */
	public void align(int[] coordOffset){
		// this must not fail now -> has to be set manually, see setCanvas.
		zProject = canvas.getImage();
		
		// (1) get other beads & (2) Integrated Gaussian Fitting.
		int[] dimensions = zProject.getDimensions();
		// (1.1) create a stack 
		ImagePlus subImp = Utils.createSubImageStack(this.zProject.getProcessor(), true, "Alignment Preview" ) ;
		
		double[][][] positions= new double[Utils.numFocalPlanes][beads.size()][2] ;
		double[][][] borders = Utils.bordersOf2DSubImages(dimensions) ;
		
		// declare the matrix lists for each 
		ArrayList<RealMatrix> XStar = new ArrayList<RealMatrix>() ;
		RealMatrix X = MatrixUtils.createRealMatrix(2, beads.size()); // 2x#beads matrix
		//Overlay ov = new Overlay() ;
		//subImp.setOverlay(ov);
		// proceed with finding the spots in the subimages and fit with integrated gaussian
		int[] temp =  new int[2] ;
		for(int i = 0; i < Utils.numFocalPlanes; i++){ // subimage counter
			for(int j = 0; j < beads.size(); j++){ // bead counter

				temp = Utils.sameSpotInSubImage((int) beads.get(j).getX(), (int)beads.get(j).getY(),
												coordOffset[0], coordOffset[1],
														i, dimensions ) ; // whole picture coordinates !!
				int a = beads.get(j).getA(); // 0.5*width and 0.5*height of sampling window
				int distTolerance = beads.get(j).getDistTolerance() ;
				
				//int subX = (int)(beads.get(j).getX() - borders[Utils.numFocalPlanes/2][0][0]) ; // bead position in central subimage coordinates
				//int subY = (int)(beads.get(j).getY() - borders[Utils.numFocalPlanes/2][0][1]) ;
				
				int subX = (int)(temp[0]-borders[i][0][0]); // same position on subimage
				int subY = (int)(temp[1]-borders[i][0][1]); // to put in the gaussian fitting
				Sample refineSample = Utils.sampleFromImage2D(subImp, subX-distTolerance, subY-distTolerance,2*distTolerance +1, i+1); // sample around spots
				Point3D<Integer> refined = refineSample.maxIntPixel(); // refine position with maxIntensity search 
				// WHY I DO ANOTHER MAX INTENSITY SEARCH: If the distortion due to the optics is too strong, the algorithm might not converge.
				// make new refined sample...
				subX += refined.getX()- distTolerance; // Refined spot position in 
				subY += refined.getY()- distTolerance; // subimage coordinates.
				// !!! The Sampling of the gaussian is done ON the cut Images to avoid
				// having to recalculate the positions on the subimages !

				Sample subSample = Utils.sampleFromImage2D(subImp, (int)subX-a, (int)subY-a, 2*a+1, i+1); // take sample directly on cut subwindow!!
				//ov.add(new Roi((int)subX-a,(int)subY-a,2*a+1,2*a+1)) ;
				// Now fit the integrated gaussian.
				GaussianFit gFit = new GaussianFit(2, subSample, 1,true, false, false) ; // initialize the GaussianFit Instance
				gFit.changeSigmaEstimate(GaussianFit.hardcodeSigmaEstimate);
				double[] params;
				try {
					params = gFit.doFit();
					positions[i][j][0] = (int)subX-a + params[2]; // Remember: subX and subY are subimage coordinates (on already cut images)
					positions[i][j][1] = (int)subY-a + params[3];
				} catch (OptimizationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // do the fit with a background and an integrated gaussian
				//double x = temp[0]-a + (params[2]); // linear algebra to get point coordinate in whole image coordinates
				//double y = temp[1]-a + (params[3]);
				//this.canvas.markSpot(x, y, 2*a) ; // mark the new spot
			}
			// XStar must be filled with matrices of the bead positions in the non-central subimages
			if( i== Utils.numFocalPlanes / 2){ // central frame 9/2 = 4, 25/2 = 12 
				X = MatrixUtils.createRealMatrix(positions[i]) ;
			} else {
				// X must be filled with the bead positions of the central subimage
				XStar.add(  MatrixUtils.createRealMatrix(positions[i]) ) ;
			}
		}
		// STEP (4) get the procrustes results
		this.procrusteses = this.getProcrustes(XStar, X) ;

		/*
		 * STEP (5): Now, we have to apply this transformation
		 * and create a stack of #FocalPlanes images.
		 */
		//ImageStack stack = new ImageStack(width, width, Utils.numFocalPlanes) ;
		// (6) Fill the stack with #Utils.numOfFocalPlanes
		// ImagePluses so that 

		this.fillStack(subImp, procrusteses, 0,null); // no intensity change here, what so ever

		this.testPlot( positions,subImp,  coordOffset) ; // Plot the average movement of found bead positions

		subImp.show(); // preview the alignment
		
	}
	protected ArrayList<Procrustes> getProcrustes(List<RealMatrix> XStar,RealMatrix X){
		ArrayList<Procrustes> procrusteses =  new ArrayList<Procrustes>() ;
		for(int i=0; i< Utils.numFocalPlanes; i++){
			if(i!= Utils.numFocalPlanes / 2){
				int t = i< Utils.numFocalPlanes / 2 ? i: i-1 ;
				RealMatrix xStar = XStar.get(t);
				procrusteses.add(new Procrustes(  X, xStar,true,true)) ;
			}
		}
		return procrusteses ;
	}
	/*
	 * FillStack of subImp using the procrustes transformations.
	 */
	protected void fillStack(ImagePlus subImp, List<Procrustes> procrusteses, double offset,  int[] intensities){

		RealMatrix mg = this.meshgrid(subImp.getWidth(), subImp.getHeight()) ; // I know, its not a meshgrid... 
		
		for(int k=0; k < Utils.numFocalPlanes; k++){
					if(k != Utils.numFocalPlanes/2){
						int t = k< Utils.numFocalPlanes / 2 ? k: k-1 ;
					// now get the original coordinates
						Procrustes proc = procrusteses.get(t) ;
					// Equation we're implementing here OrigCoordinates = [( newCoordinates -translate)/scale]*T'
					// Z has to be a Nx2 vector consisting of every possible combination
					// of x and y.
						RealMatrix translate = this.createNRowMatrix(proc.getTranslation(), subImp.getWidth()*subImp.getHeight()) ;
						RealMatrix res = mg.multiply(proc.getR()).scalarMultiply(proc.getDilation()).add(translate);//mg.subtract(translate).multiply(proc.getR().transpose()).scalarMultiply(1.0/proc.getDilation()); // original coords as fct of new coords
						//RealMatrix res = mg.multiply(proc.getR()).scalarMultiply(proc.getDilation()).add(translate);
						ImageProcessor ip = subImp.getStack().getProcessor(k+1); // 1 based
						this.fill(ip,ip.duplicate(), res,mg,k,offset,  intensities) ; // write new pixel values in ip.
					} else{
						// But we may have to adapt the intensity  in the central fram!
						if(intensities != null){
							ImageProcessor ip = subImp.getStack().getProcessor(k+1); // 1 based
							for (int i=0; i< ip.getWidth(); i++){
								for(int j=0; j< ip.getHeight(); j++){
										// ADAPT Intensity automatically during the alignment !
										double oldValue = ip.getPixelValue(i, j);
										double newValue= IntensityCorrectionTab.intensityChange(oldValue, intensities[k], offset) ;
										ip.putPixelValue(i, j, newValue) ;
									}							
							}
						}
					}
		}
			
	}
	/*
	 * Convenience Function.
	 * @return (xN*yN)x2 matrix of coordinate combinations
	 */
	protected RealMatrix meshgrid(int xN, int yN){
		double[][] rawMesh = new double[xN*yN][2];
		for(int i=0; i< xN; i++){
			for(int j=0; j< yN; j++){
				rawMesh[yN*i+j][0] = i;
				rawMesh[yN*i+j][1] = j ;
			}
		}
		return MatrixUtils.createRealMatrix(rawMesh);
	}
	protected RealMatrix createNRowMatrix(RealMatrix row, int rows){
		RealMatrix res = MatrixUtils.createRealMatrix(rows, row.getRowDimension());
		for(int i=0; i< rows; i++)
			res.setRow(i, row.getColumn(0)) ;
		return res ;
	}
	/*
	 * We have to interpolate during the transformation
	 * as not always you can transfer simply the pixels
	 * from the old to the new picture.
	 */
	protected void fill(ImageProcessor ipNew, ImageProcessor ipOld, RealMatrix res, RealMatrix newCoord,  int si, double offset, int[] intensities){
		
		
		
		for(int i=0;i<res.getRowDimension(); i++){
				double interp = ipOld.getInterpolatedValue(Math.abs(res.getEntry(i, 0)), Math.abs(res.getEntry(i, 1))) ;
				if(intensities != null){
					// ADAPT Intensity automatically during the alignment !
					interp = IntensityCorrectionTab.intensityChange(interp, intensities[si], offset) ; 
				}
				ipNew.putPixelValue((int)newCoord.getEntry(i, 0),(int)newCoord.getEntry(i, 1), interp);
		}
	}
	/*
	 * Plots 1/N_beads*SUM_j | r_(i,j)- r_(i+1,j) |
	 * with r_(i,j) being the position of the jth bead on the ith subimage
	 */
	protected void testPlot(double[][][] positions, ImagePlus focalStack, int[] coordOffset){
		// only do a testPlot in case beads were selected
		if(beads.size() == 0){
			return ;
		}

		XYSeries series = new XYSeries("");
		double[][][] pos= new double[Utils.numFocalPlanes][beads.size()][2] ;
		
		double[] std = new double[beads.size()] ;
		double[] maxDist = new double[beads.size()] ;
		String stdAnnouncement = "STD/Maximum of bead position change with respect to central frame:\n" ;
		// we do it in two loops
		for(int j=0; j< beads.size(); j++){
			maxDist[j]=0; // initialize with zero
			for(int i=0; i< Utils.numFocalPlanes; i++){
				int a = beads.get(j).getDistTolerance(); // get roi width*height
				// now, we have to substract the coordinate offset, as the procrustes has probably killed this difference between the subimages!!
				// we added this offset in Utils.sameSpotInSubimage(...) look there for the math!
				int[] cI = Utils.convertFromSubImageEnumeration(i); // subimage coordinate index {(0,0),(1,0),(0,1) ... (2,2)} for 9 subimages
				int rows =(int) Math.sqrt(Utils.numFocalPlanes);
				int x = (int)positions[i][j][0]-a -(cI[0]-rows/2)*coordOffset[0]; 
				int y = (int)positions[i][j][1]-a -(cI[1]-rows/2)*coordOffset[1];

				Sample roiSample = Utils.sampleFromImage2D(focalStack,x,y,2*a+1, i+1 ) ;
				GaussianFit gFit = new GaussianFit(2, roiSample, 1,true, false,false ) ; // initialize the GaussianFit Instance

				double[] params;
				try {
					params = gFit.doFit();
					pos[i][j][0]= x+params[2];
					pos[i][j][1]= y+params[3];	
				} catch (OptimizationException e) {
					// TODO Auto-generated catch block
					System.out.println( e.getLocalizedMessage());
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					System.out.println(e.getLocalizedMessage()) ;
				}
			}
		}

		for(int i=0; i< Utils.numFocalPlanes-1; i++){
			double dist = 0;
			double centralDist = 0;
			for(int j=0; j< beads.size(); j++){
				centralDist = Math.sqrt(Math.pow(( pos[i][j][0]-pos[Utils.numFocalPlanes/2][j][0]),2)
						+ Math.pow(( pos[i][j][1]-pos[Utils.numFocalPlanes/2][j][1]),2)) ;
				std[j] += Math.pow(centralDist,2) ; // add to v
				maxDist[j] =  Math.max(centralDist, maxDist[j]); // take maximum distance to position on central subimage
				//maxDist[j]=Math.sqrt(maxDist[j]) ; // sqrt() -> distance

				dist += Math.sqrt(Math.pow(pos[i][j][0]-pos[i+1][j][0],2)
						+ Math.pow(pos[i][j][1]-pos[i+1][j][1],2) );
			}
			series.add(Utils.mappingOrder[i]+0.5, (1.0/beads.size())*dist ); 
		}
		// put string together
		for(int j=0; j< beads.size(); j++)
			stdAnnouncement += "Bead "+j+" :" + String.format("%.2f", Math.sqrt(1.0/(Utils.numFocalPlanes-1)*std[j]))
	  		+  " / "+String.format("%.2f",maxDist[j])+" [pix] \n";
		
		GaussianUtils.plotData("Net Position Change Of Spotcenters Between Aligned Focalplanes", series,"Focalplane Index", "Distance [pix]", 0, 0);
		IJ.showMessage(stdAnnouncement) ;
	}
	
	public ImagePlus alignWholeStack(ImagePlus toAlign,double offset,  int[] intensities){
		
		int rows = (int) Math.sqrt(Utils.numFocalPlanes);
		// create the hyperstack
		ImagePlus aligned = IJ.createHyperStack("Aligned "+toAlign.getTitle(),
				(int)(toAlign.getWidth()/rows),  (int)(toAlign.getHeight()/rows),
							1,  Utils.numFocalPlanes, toAlign.getImageStackSize(),16) ; 
		
			// first get all the substacks in place
			ImagePlus[] subStacks = new ImagePlus[toAlign.getImageStackSize()] ;
			
			for(int i=1; i<= toAlign.getImageStackSize(); i++){
				toAlign.setSlice(i) ;
				ImageProcessor ip= toAlign.getProcessor().duplicate();
				subStacks[i-1] = Utils.createSubImageStack(ip, true, "Slice "+i) ; // fill with the #focalPlanes-image stacks
				this.fillStack(subStacks[i-1], procrusteses, offset, intensities) ; // fill these in turn with actual pixel data
			}
			// now for every plane, add the whole time stack
			/*
			for(int j = 1; j <= Utils.numFocalPlanes; j++){ //z
				for(int i=1; i<=toAlign.getImageStackSize(); i++){ //t
					aligned.setPosition(1, j,i) ; 
					subStacks[i-1].setSlice(j) ;
					ImageProcessor ipAligned = subStacks[i-1].getProcessor();
					aligned.setProcessor(ipAligned) ;
				}
			}*/
			// create a stack with #focalPlanes times #frames slices, then sort the stack to a hyperstack
			ImageStack alignedStack = new ImageStack((int)(toAlign.getWidth()/rows),  (int)(toAlign.getHeight()/rows));
			
			for( int i=0; i<toAlign.getImageStackSize(); i++){ //z
				ImageStack subStack = subStacks[i].getStack() ;
				for(int j = 1; j <= Utils.numFocalPlanes; j++){ //t			{
					alignedStack.addSlice(subStack.getProcessor(j)) ;
				}
			}
			aligned.setStack(alignedStack) ;
			return aligned;
	}
	/*
	 * SaveToFile method
	 */
	public void saveToFile(PrintStream out){
		for(int i=0; i< Utils.numFocalPlanes-1; i++){ // for every subimage EXCEPT the central one
			for(int l=0; l< 4; l++){ // forevery rotation matrix element
				out.println(this.procrusteses.get(i).getR().getEntry(l/2, l%2)) ;
			}
			// savetranslation
			out.println(this.procrusteses.get(i).getTranslation().getEntry(0, 0));
			out.println(this.procrusteses.get(i).getTranslation().getEntry(1, 0));
			// save dilation
			out.println(this.procrusteses.get(i).getDilation()) ;
		}
	}
	/*
	 * ReadFromFile method
	 */
	public void readFromFile(BufferedReader in){
		try{
			double[][] entries = new double[2][2];
			double[] translation = new double[2] ;
			this.procrusteses.clear() ;
			
			for(int i=0; i< Utils.numFocalPlanes-1; i++){ // for every subimage
				for(int l=0; l< 4; l++){ // forevery rotation matrix element
					//System.out.println(in.readLine());
					entries[l/2][l%2] = Double.valueOf(in.readLine());
				}
				RealMatrix R = MatrixUtils.createRealMatrix(entries) ;
				// savetranslation
				translation[0] = Double.valueOf(in.readLine());
				translation[1] = Double.valueOf(in.readLine());
				RealMatrix T = MatrixUtils.createRowRealMatrix(translation).transpose(); // create row matrix == vector
				// save dilation
				double dilation = Double.valueOf(in.readLine());
				this.procrusteses.add( new Procrustes(R,T,dilation)) ; // create new procrustes instances from this data
			}
		} catch(IOException e){
			
		}
	}
}
