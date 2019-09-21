package lib;

import org.apache.commons.math.optimization.fitting.*;

/**
 *
 * @author nico
 */
public class ParametricGaussianFunction implements ParametricRealFunction{
   private int width_;
   private int height_;
   private int depth_ ;
   private int mode_;
   private boolean backGround_ ;
   private boolean integrated_;
   private boolean symmetric_;
   
   public ParametricGaussianFunction(int mode, int width, int height, int depth, boolean backGround, boolean integrated, boolean symmetric) {
      width_ = width;
      height_ = height;
      depth_ = depth ;
      mode_ = mode;
      backGround_ = backGround ;
      integrated_ = integrated ;
      symmetric_ = symmetric ;
   }

   public double value(double d, double[] doubles)  {
      double value = 0;
      if (mode_ == 1)
         value =  GaussianUtils.gaussian1D(doubles, ((int) d) ) ;
      	 
      else if (mode_ == 2){
    	  if( integrated_){
    		  value = GaussianUtils.gaussianIntegrated2D(doubles, ((int) d) % width_, ((int) d) / width_) ;
    	  } else{
        	  value =  GaussianUtils.gaussian2DXY(doubles, ((int) d) % width_, ((int) d) / width_);
    	  }
   		}
      else if (mode_ == 3){ // mode three doesn't support integrated gaussians
    	  if(  symmetric_){
    		  int z = ((int) d)/(width_*height_) ; 
    		  int y = (((int)d)-z*width_*height_)/width_ ;
    		  int x = (int)d-z*width_*height_-y*width_ ;
    		  value =  GaussianUtils.gaussian3DXZ(doubles, x,y,z);
    	  } else{
    		  int z = ((int) d)/(width_*height_) ; 
    		  int y = (((int)d)-z*width_*height_)/width_ ;
    		  int x = (int)d-z*width_*height_-y*width_ ;
    		  value =  GaussianUtils.gaussian3DXYZ(doubles, x,y,z);
    	
    	  }
      }
      //value -= backGround_ ?   0 :doubles[1];
      return value;
   }

   public double[] gradient(double d, double[] doubles)  {
      double[] value = {0.0};
      if (mode_ == 1)
         value =  GaussianUtils.gaussian1DJ(doubles, ((int) d) );
      else if (mode_ == 2){
    	  if( integrated_){
    		  value = GaussianUtils.gaussianIntegratedJ2D(doubles, ((int) d) % width_, ((int) d) / width_);
    	  } else {
              value =  GaussianUtils.gaussianJ2DXY(doubles, ((int) d) % width_, ((int) d) / width_);
    	  }
      }else if (mode_ == 3){
    	  if(symmetric_){
    		  int z = ((int) d)/(width_*height_) ; 
      	  	int y = ((int)d-z*width_*height_)/width_ ;
      	  	int x = (int)d-z*width_*height_-y*height_ ;
            	value =  GaussianUtils.gaussianJ3DXZ(doubles, x,y,z);
    	  } else{
    		  int z = ((int) d)/(width_*height_) ; 
    	  	int y = ((int)d-z*width_*height_)/width_ ;
    	  	int x = (int)d-z*width_*height_-y*height_ ;
          	value =  GaussianUtils.gaussianJ3D(doubles, x,y,z);
    	  }
      }
      //value[1] = backGround_ ? value[1] : 0 ;
      return value;
   }

}
