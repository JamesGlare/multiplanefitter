package lib;

import processing.SpotMother;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class ImageProcessor3D {
	   
	   private ImageProcessor iProc ;
	   private ImagePlus imp;
	   private int frame ;
	   private boolean realData ; // switch, if we 
	   
	   public ImageProcessor3D(ImagePlus imp_, int frame_, boolean applyFilter_, boolean realData_){
		   /*iProcs =  new ImageProcessor[Utils.numFocalPlanes] ;
		   for(int i=0; i< Utils.numFocalPlanes; i++){
			   imp.setPositionWithoutUpdate(1, i+1, frame);
			   iProcs[i] = imp.getProcessor() ;
		   }*/
		   imp = imp_ ;
		   frame = frame_;
		   imp.setPositionWithoutUpdate(imp.getC(), imp.getSlice(), frame) ;
		   if(applyFilter_){
			   imp = SpotMother.filterImage(imp_) ; // loose all frames -> set frame to 1. 
		   	   frame =1;									
		   }
		   
		   realData = realData_ ;
		   iProc = imp.getProcessor(); //iProc is now bound to the imp instance.
	   }
	   public void setRealData(boolean realData_){
		   realData = realData_ ;
	   }
	   public double getPixel(int i, int j, int k){
		   
		   int slice = imp.getSlice(); // save z position
		   
		   if( k >= Utils.numFocalPlanes){
			   imp.setPositionWithoutUpdate(imp.getC(), 2*Utils.numFocalPlanes-k-1, frame ) ; // go back as much, as you crossed the upper z plane
			   double result = realData ? 0 : iProc.getPixel(i, j);
			   imp.setPositionWithoutUpdate(imp.getC(), slice, frame ) ;
			   return  result; //return stack.getVoxel(i, j, k) ;
		   } else if(k<0){ 
			   imp.setPositionWithoutUpdate(imp.getC(), Math.abs(k)+1, frame ) ; // go up as much as you crossed the lower z plane
			   double result = realData ? 0 : iProc.getPixel(i, j);
			   imp.setPositionWithoutUpdate(imp.getC(), slice, frame ) ;
			   return  result;//return stack.getVoxel(i, j, k) ;
		   } else{
			   imp.setPositionWithoutUpdate(imp.getC(), k+1, frame ) ; 
			   double result = iProc.getPixel(i, j);
			   imp.setPositionWithoutUpdate(imp.getC(), slice, frame ) ;
			   return  result;
		   }
	   }
	   /*public double getPixel(int i, int j, int k){
		   
		   int slice = imp.getSlice(); // save z position
		   
		   if( k >= Utils.numFocalPlanes){
			   imp.setPositionWithoutUpdate(imp.getC(), Utils.numFocalPlanes, frame ) ;
			   double result = realData ? 0 : iProc.getPixel(i, j);
			   imp.setPositionWithoutUpdate(imp.getC(), slice, frame ) ;
			   return  result; //return stack.getVoxel(i, j, k) ;
		   } else if(k<0){
			   imp.setPositionWithoutUpdate(imp.getC(),1, frame ) ;
			   double result = realData ? 0 : iProc.getPixel(i, j);
			   imp.setPositionWithoutUpdate(imp.getC(), slice, frame ) ;
			   return  result; //return stack.getVoxel(i, j, k) ;
		   }
		   else { 
			   imp.setPositionWithoutUpdate(imp.getC(), Math.abs(k)+1, frame ) ;
			   double result = iProc.getPixel(i, j);
			   imp.setPositionWithoutUpdate(imp.getC(), slice, frame ) ;
			   return  result;//return stack.getVoxel(i, j, k) ;
		   }
	   }*/
}