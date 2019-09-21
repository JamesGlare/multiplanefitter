/**
 * Gaussian Fitting package
 * Implements MultiVariaRealFunction using the Gaussian functions defined in
 * GaussianUtils
 *
 * @author - Nico Stuurman, September 2010
 */

package lib;


import org.apache.commons.math.analysis.*;

/**
 *
 * @author nico
 */
public class MultiVariateGaussianFunction implements MultivariateRealFunction {

   int[] data_;
   int nx_;
   int ny_;
   int nz_ ;
   int count_ = 0;
   int mode_ = 1;



   /**
    * Gaussian fit can be run by estimating parameter c (width of Gaussian)
    * as 1 (circle), 2 (width varies in x and y), or 3 (ellipse) parameters
    *
    * @param dim
    */
   public MultiVariateGaussianFunction(int mode) {
      super();
      mode_ = mode;
   }

   public void setImage(short[] data, int width, int height, int depth) {
      data_ = new int[data.length];
      for (int i=0; i < data.length; i++) {
         data_[i] = (int) data [i] & 0xffff;
      }
      nx_ = width;
      ny_ = height;
      nz_ = depth;
   }

   public double value(double[] params) {
       double residual = 0.0;
       if (mode_ == 1) {
          for (int i = 0; i < nx_; i++) {
            		 residual += GaussianUtils.sqr(GaussianUtils.gaussian1D(params, i) - data_[ i]);
          }
       } else if (mode_ == 2) {
          for (int i = 0; i < nx_; i++) {
             for (int j = 0; j < ny_; j++) {
            		 residual += GaussianUtils.sqr(GaussianUtils.gaussian2DXY(params, i, j ) - data_[(j*nx_) + i]);
             }
          }
       } else if (mode_ == 3) {
          for (int i = 0; i < nx_; i++) {
             for (int j = 0; j < ny_; j++) {
            	 for(int k=0; k< nz_; k++){
            		 residual += GaussianUtils.sqr(GaussianUtils.gaussian3DXYZ(params, i, j,k) - data_[(k*nx_*ny_) + j*ny_+i]);
            	}
             }
          }
       }
       return residual;
   }
}
