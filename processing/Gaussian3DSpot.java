package processing;


import gui.LogTab;

import org.apache.commons.math.MathException;
import org.apache.commons.math.optimization.OptimizationException;

import lib.GaussianFit;
import lib.Point3D;
import lib.Sample;

public class Gaussian3DSpot extends Spot {
	/*
	 * Private
	 */

	private double[] params ;
	private boolean wasRun ;	
	private String errMsg ="";
	private boolean symmetric =true ;
	/*
	 * Constructor
	 */
	public Gaussian3DSpot(Point3D<Double> posEstimate_, Sample sample_, boolean symmetric ){
		super(sample_, posEstimate_) ;
		params = new double[8] ;
		imgPosition = new int[2] ;
		imgPosition[0] = 0;
		imgPosition[1] = 0;
		wasRun = false ;
		this.symmetric = symmetric ;
	}
	/*
	 * ImagePosition
	 */
	public void setImagePosition(int slice, int frame){
		imgPosition[0] = slice ;
		imgPosition[1] = frame ; 
	}
	
	public void run(){
		// create gaussian fitter

		try{
			try{
				GaussianFit gf = new GaussianFit(3, sample,1,true, false, symmetric) ;
				//double[] test = gf.estimateParams();
				//System.out.println("I0:"+test[0]+" B:"+test[1]+" x"+test[2]+"y: "+test[3]+" z:"+test[4]+" sx: "+test[5]+" sy:"+test[6]+"sz: "+test[7] ) ;
				params = gf.doFit();
			}catch(MathException exc){
					errMsg+=exc.getLocalizedMessage()+"\n" ;
					return ; // dont set wasRun = true;
			}
		}catch(java.lang.OutOfMemoryError e){
			errMsg+=e.getLocalizedMessage()+"\n" ;
			return ; // dont set wasRun = true;
		}
		this.clean() ; // make the sigmas positive...
		wasRun = true ;
	}
	@Override
	public String getErrMsg(){
		return errMsg;
	}
	public String getPositionString(){
		
		return "Spot (zf="+imgPosition[0]+", t="+imgPosition[1]+") @"+this.getPosition().toString();
	}
	public String getRefinedPositionString(){
		
		return "Spot (zf="+imgPosition[0]+", t="+imgPosition[1]+") @ (r="+this.getRefinedPosition().toString()+", sx="
		+String.format("%.2f",Math.abs(params[5]))+", sy="+String.format("%.2f",Math.abs(params[6]))+", sz="+String.format("%.2f",Math.abs(params[7]))+")";
	}
	@Override
	public Point3D<Double> getRefinedPosition(){
		
		double x = posEstimate.getX()+params[2]  ; // fullImageCoordinates
		double y = posEstimate.getY()+params[3] ;
		double z = posEstimate.getZ()+params[4] ;
		return new Point3D<Double>(x,y,z) ;
	}
	@Override
	public Point3D<Double> getPosition() {
		return posEstimate;
	}
	public double getIntensity(){
		return params[0] ;
	}
	public double[] getSigmas(){
		return new double[]{ params[5], params[6], params[7]} ;
	}
	public boolean wasRun(){
		return wasRun ;
	}
	public double[] getParams(){
		return this.params ;
	}
	/*
	 * This function sets all sigmas to their absolute
	 * value, since the fitter sometimes assigns them negative values
	 * as their sign doesnt change the chi square sum, this effect should not 
	 * be taken into account.
	 */
	private void clean(){
		for(int i=5; i<=7; i++){
			params[i] = Math.abs(params[i]) ;
		}
		if(symmetric){
			params[6] = params[5] ; // symmetric...
		}
	}

}
