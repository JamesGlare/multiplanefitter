package lib;
// usage of deprecated bockmist
import lib.Sample.MyIterator;
import gui.LogTab;
import ij.IJ;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math.optimization.fitting.CurveFitter;


/****************************************
 * GaussianFit
 * Inspired by the class GaussianFit
 * written by Nico Stuurman.
 * Uses Apache commons library.
 ****************************************/
public class GaussianFit {

	/*
	 * private members
	 */

	private int dimension ;
	private double[] params ; // (Mean, STD, Amplitude)
	private LevenbergMarquardtOptimizer lMO;
	
	private Sample sample ;
	public final static double hardcodeSigmaEstimate[] = {0.8,0.8,1.1} ; // its not a final
	private static double[] sigmaEstimate;
	private final boolean backGround ;
	private final boolean integrated ;
	private final boolean symmetric;
	/* Internal Fitting parameters */
	private int maxTime = 5000 ; // in milliseconds
	private static int maxIterations =300 ;
	
	private static double wavelength ;
	private static double NA;
	private static double diffIndex ;
	private static double pixelSize ;
	
	/*
	 * Constructor
	 * @param dimension_ : Dimension of gaussian fit: 1d,2d,3d
	 */
	public GaussianFit(int dimension_, Sample sample_, double pixelSize,  boolean backGround, boolean integrate, boolean symmetric){
		dimension=dimension_ ;
		sample = sample_ ;
		lMO = new LevenbergMarquardtOptimizer(); //(1.0e-6, 1e-6, 1.0e-6) ;
		lMO.setMaxIterations(maxIterations) ;
		this.backGround =  backGround ;
		this.integrated = integrate ;
		this.pixelSize = pixelSize ;
		this.symmetric = symmetric ;
		
		params = new double[dimension*2+2] ;
		/* Configure Fitter */
		if(sigmaEstimate == null)
			sigmaEstimate = this.hardcodeSigmaEstimate ;
	}
	/*
	 * Estimate parameters from a sample.
	 * @return a dimension*2+2 long array
	 */
	public double[] estimateParams() {
		
		double[] start_param = new double[ 2*dimension + 2 ];
		
		
		// now, estimate the background by averaging @ the border
		start_param[1] = backGround ? sample.sumAveragedBorder() : 0;
		//start_param[1] = 500 ;
		//Now, guess the mean
		Point3D<Integer> point = sample.maxIntPixel();
		double IMax = sample.maxInt();
		// First guess the amplitude with the max intensity
		start_param[0] = IMax;
		System.arraycopy(new double[]{point.getX()*pixelSize, point.getY()*pixelSize, point.getZ()*pixelSize},
									0, start_param, 2, dimension);	
		// now estimate the variance using a variance estimator
		int j = 0;
		
		for(int d = 0; d < dimension; d++){
			start_param[2+dimension+d] = sigmaEstimate[d] ; // = Sum_i[I_i*(x_i-<x>)^2]/ISum with i being pixel index.
		}
		return start_param;		
	}
	/*
	 * Change the sigma estimate for the gaussian fitting.
	 */
	public static void changeSigmaEstimate(double[] newEstimate){
		sigmaEstimate = newEstimate ;
	}
	/*
	 * Change the maxIterations of GaussianFitter
	 */
	public static void setMaxIterations(int maxIterations_){
		maxIterations = maxIterations_ ;
	}
	/*
	 * Estimate in one dimension the standard deviation.
	 * (Biased, low chi estimator) or (Unbiased estimator, worse chi square).
	 */
	public double[] estimateSigma(){
		Sample subSample = (Sample)sample.duplicate() ;
		subSample.add((-1)*subSample.sumAveragedBorder()) ; 
		double mean = subSample.centerOfMass().getX() ;
		double N = subSample.sumInt() ;
		double IXSquare = 0 ;
		for(MyIterator iter = (MyIterator) subSample.iterator(); iter.hasNext();){
			int x = subSample.toCoord(iter.getCurrentCouter())[0] ;
			double I = iter.next() ;
			IXSquare += I*x*x ;
		}
		double s = Math.sqrt( Math.abs((N-1)/N*(1/N * IXSquare - mean*mean )));
		return new double[]{s,s,s} ;
	}
	public static double[] estimateSigmaPhysics(){
		double sXY = 0.225* GaussianFit.wavelength/GaussianFit.NA ;
		double sZ = 0.78*GaussianFit.diffIndex*GaussianFit.wavelength/(GaussianFit.NA*GaussianFit.NA) ;
		return new double[]{sXY, sXY, sZ} ;
	}
	/*
	 * Set physical experimental parameters to estimate the width of the PSF in all dimensions.
	 */
	public static void setPhysicalParameters(double wavelength_, double NA_, double diffIndex_){
		wavelength = wavelength_;
		NA = NA_ ;
		diffIndex = diffIndex_ ;
	}
	/*
	 * Do Fit
	 */
	public double[] doFit() throws OptimizationException, 
						IllegalArgumentException{
		lMO.setMaxIterations(maxIterations) ;
		final CurveFitter cf =  new CurveFitter(this.lMO) ;
		// add the data to the fitter.
		// stretch the data to a dimensional sequence.
		double i=0;
		for(double I : sample){
			cf.addObservedPoint(i*this.pixelSize, ((short)I) & 0xffff );
			i++ ;
		}
		final int[] size = sample.getSize();
		final double[] estimate =this.estimateParams();
		final double[][] paramsContainer = new double[1][2*dimension+2] ;
		Thread thread = new Thread(){
			@Override
			public void run(){
				try {
					paramsContainer[0]= cf.fit(new ParametricGaussianFunction(dimension, size[0], size[1],size[2], 
																		backGround, integrated, symmetric), 
																		estimate);
				} catch (@SuppressWarnings("deprecation") OptimizationException e) {
					LogTab.writeLog(e.getLocalizedMessage());
				} catch (FunctionEvaluationException e) {
					LogTab.writeLog(e.getLocalizedMessage());
				} catch (IllegalArgumentException e) {
					LogTab.writeLog(e.getLocalizedMessage());
				}
			}
		};
		try {
			thread.start();
			thread.join(maxTime) ;
	
		} catch (InterruptedException e) {
			LogTab.writeLog("Fitter interrupted!\nMessage: "+e.getLocalizedMessage()) ;
		}
		
		//SimpleMultiThreading.startAndJoin(new Thread[]{thread}) ;
		return paramsContainer[0];
	}
	/* 
	 * params array to string
	 */
	public String showResults(double[] pO){
		String string ="";
		switch(dimension){
			case 1:
				string = "I0="+pO[0]+", B= "+pO[1] +" <x>="+pO[2]+", sx="+pO[3] ;
				break;
			case 2:
				string = "I0="+pO[0]+", B= "+pO[1] +" <x>="+pO[2]+", <y>="+pO[3]  +", sx="+pO[4]+", sy="+pO[5] ;
				break;
			case 3:
				string = "I0="+pO[0]+", B= "+pO[1] +" <x>="+pO[2]+", <y>="+pO[3]+", <z>="+pO[4]  +", sx="+pO[5]+", sy="+pO[6] +", sz="+pO[7];
				break;
		}
		return string;
	}

}
