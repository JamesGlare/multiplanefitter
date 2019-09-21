package processing;

import java.util.concurrent.atomic.AtomicInteger;

import lib.Point3D;
import lib.Sample;

public abstract class Spot implements Runnable{

	protected int[] imgPosition ;
	protected Sample sample ;
	protected Point3D<Double> posEstimate ;
	protected AtomicInteger aI;
	protected double wavelength;
	protected double diffIndex ;
	protected double NA ;
	
	public Spot(Sample sample_, Point3D<Double> posEstimate_){
		sample= sample_ ;
		posEstimate = posEstimate_ ;
	}
	public void setAI(AtomicInteger aI_){
		aI = aI_ ;
	}
	public void run(){
		
	}
	public void setImagePosition(int slice, int frame){
		imgPosition[0] = slice ;
		imgPosition[1] = frame ; 
	}
	public int getFrame(){
		return imgPosition[1];
	}
	public int getSlice(){
		return imgPosition[0] ;
	}
	public double getMaxInt(){
		if( sample != null)
			return sample.maxInt() ;
		else
			return 0 ;
	}
	public abstract String getErrMsg() ;
	public abstract Point3D<Double> getRefinedPosition();
	public abstract Point3D<Double> getPosition() ;
}
