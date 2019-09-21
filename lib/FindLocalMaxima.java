package lib;

/**
 * Find local maxima in an Image (or ROI) using the algorithm described in
 * Neubeck and Van Gool. Efficient non-maximum suppression. 
 * Pattern Recognition (2006) vol. 3 pp. 850-855
 *
 * Jonas Ries brought this to my attention and send me C code implementing one of the
 * described algorithms
 *
 *
 *
 */


import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.ImageCalculator;
import ij.process.ImageProcessor;
import ij.plugin.filter.GaussianBlur;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import processing.Gaussian3DSpot;
import processing.Spot;
import processing.SpotMother;


/**
 *
 * @author nico
 */
public class FindLocalMaxima {

   private final static boolean useRealDataDetection = true ;
   public enum FilterType {
      NONE,
      GAUSSIAN1_5
   }

   /**
    * Static utility function to find local maxima in an Image
    * 
    * 
    * @param iPlus - ImagePlus object in which to look for local maxima
    * @param n - minimum distance to other local maximum
    * @param threshold - value below which a maximum will be rejected
    * @return Polygon with maxima 
    */
  /* public static Polygon FindMax(ImagePlus iPlus, Rectangle roi, int n,int p,int slice, int frame,  int threshold, int ruleNumber) {
 
	  Polygon maxima = new Polygon();
	  int k = slice;
      iPlus.setPosition(1, slice, frame) ;
      ImageProcessor3D iProc = new ImageProcessor3D(iPlus,frame, false, useRealDataDetection ) ;


      // divide the image up in blocks of size n and find local maxima
      int n2 = 2*n + 1;
      // calculate borders once
      int xRealEnd = roi.x +roi.width;
      int xEnd = xRealEnd - n;
      int yRealEnd = roi.y+roi.height;
      int yEnd = yRealEnd - n;
      for (int i=roi.x; i <= xEnd - n - 1; i+=n2) {
         for (int j=roi.y; j <= yEnd - n - 1; j+=n2) {
            int mi = i;
            int mj = j;
            for (int i2=i; i2 < i + n2 && i2 < xRealEnd; i2++) {
               for (int j2=j; j2 < j + n2 && j2 < yRealEnd; j2++) {
                  // revert getPixel to get after debugging
                  if (iProc.getPixel(i2, j2,k-1) > iProc.getPixel(mi, mj,k-1)) {
                     mi = i2;
                     mj = j2;
                  }
               }
            }
            // is the candidate really a local maximum?
            // check surroundings (except for the pixels that we already checked)
            boolean stop = false;
            // columns in block to the left
            if (mi - n < i && i>0) {
               for (int i2=mi-n; i2<i; i2++) {
                  for (int j2=mj-n; j2<=mj+n; j2++) {
                     if (iProc.getPixel(i2, j2,k-1) > iProc.getPixel(mi, mj,k-1)) {
                        stop = true;
                     }
                  }
               }
            }
            // columns in block to the right
            if (!stop && mi + n >= i + n2 ) {
               for (int i2=i+n2; i2<=mi+n; i2++) {
                   for (int j2=mj-n; j2<=mj+n; j2++) {
                     if (iProc.getPixel(i2, j2,k-1) > iProc.getPixel(mi, mj,k-1)) {
                        stop = true;
                     }
                  }
               }
            }
            // rows on top of the block
            if (!stop && mj - n < j && j > 0) {
               for (int j2 = mj - n; j2 < j; j2++) {
                  for (int i2 = mi - n; i2 <= mi + n; i2++) {
                     if (iProc.getPixel(i2, j2,k-1) > iProc.getPixel(mi, mj,k-1))
                        stop = true;
                  }
               }
            }
            // rows below the block
            if (!stop && mj + n >= j + n2) {
               for (int j2 = j + n2; j2 <= mj + n; j2++) {
                  for (int i2 = mi - n; i2 <= mi + n; i2++) {
                     if (iProc.getPixel(i2, j2,k-1) > iProc.getPixel(mi, mj,k-1))
                        stop = true;
                  }
               }
            }
            if (!stop){
            	boolean acceptRule = true ;
            	switch(ruleNumber){
            		case 1:
            			acceptRule =rule2(iProc,mi,mj,k-1,n,p,threshold);
            			break;
            		case 2:
            			acceptRule =rule3(iProc,mi,mj,k-1,n,p,threshold);
            			break;
            		case 3:
            			acceptRule =rule4(iProc,mi,mj,k-1,n,p,threshold);
            			break;
            		default:
            			acceptRule =rule3(iProc,mi,mj,k-1,n,p,threshold);
            			break;
            	}
            	if(threshold == 0 ||acceptRule)
            		maxima.addPoint(mi, mj);
            }
         }
      }
      return maxima;
   } */
   public void FindSpotsPreview(ImagePlus iPlus, Rectangle roi, List<Spot> list,int n, int p, int threshold, int ruleNumber) {
	      
	     // iPlus.setPosition(1,1,frame); // set frame to the beginning
	   	// Set realdata = true, such that it is harder to detect spots at the 
	   // z-borders 0 and 8, as detections there suck anyway - we want less of them.
	    ImageProcessor3D iProc3D = new ImageProcessor3D(iPlus, 1, false, useRealDataDetection) ;

	      // divide the image up in blocks of size n and find local maxima
	      int n2 = 2*n + 1;
	      int p2 = 2*p+1 ;
	      // calculate borders once
	      int xRealEnd = roi.x + roi.width;
	      int xEnd = xRealEnd - n;
	      int yRealEnd = roi.y + roi.height;
	      int yEnd = yRealEnd - n;
	      int zRealEnd = Utils.numFocalPlanes ;
	      int zEnd = zRealEnd -p;
	      /*
	      for (int i=roi.x; i <= xEnd - n - 1; i+=n2) {
	          for (int j=roi.y; j <= yEnd - n - 1; j+=n2) {
	        	  for(int k=0; k< Utils.numFocalPlanes; k+=1){
		             int mi = i;
		             int mj = j;
		             // set pixel values of the edges.
		             for (int i2=i; i2 < i + n2 && i2 < xRealEnd; i2++) {
		                for (int j2=j; j2 < j + n2 && j2 < yRealEnd; j2++) {
		                   // revert getPixel to get after debugging
		                   if (iProc3D.getPixel(i2, j2,k) > iProc3D.getPixel(mi, mj,k)) {
		                      mi = i2;
		                      mj = j2;
		                   }
		                }
		             }
		             // is the candidate really a local maximum?
		             // check surroundings (except for the pixels that we already checked)
		             boolean stop = false;
		             // columns in block to the left
		             if (mi - n < i && i>0) {
		                for (int i2=mi-n; i2<i; i2++) {
		                   for (int j2=mj-n; j2<=mj+n; j2++) {
		                      if (iProc3D.getPixel(i2, j2,k) > iProc3D.getPixel(mi, mj,k)) {
		                         stop = true;
		                      }
		                   }
		                }
		             }
		             // columns in block to the right
		             if (!stop && mi + n >= i + n2 ) {
		                for (int i2=i+n2; i2<=mi+n; i2++) {
		                    for (int j2=mj-n; j2<=mj+n; j2++) {
		                      if (iProc3D.getPixel(i2, j2,k) > iProc3D.getPixel(mi, mj,k)) {
		                         stop = true;
		                      }
		                   }
		                }
		             }
		             // rows on top of the block
		             if (!stop && mj - n < j && j > 0) {
		                for (int j2 = mj - n; j2 < j; j2++) {
		                   for (int i2 = mi - n; i2 <= mi + n; i2++) {
		                      if (iProc3D.getPixel(i2, j2,k) > iProc3D.getPixel(mi, mj,k))
		                         stop = true;
		                   }
		                }
		             }
		             // rows below the block
		             if (!stop && mj + n >= j + n2) {
		                for (int j2 = j + n2; j2 <= mj + n; j2++) {
		                   for (int i2 = mi - n; i2 <= mi + n; i2++) {
		                      if (iProc3D.getPixel(i2, j2,k) > iProc3D.getPixel(mi, mj,k))
		                         stop = true;
		                   }
		                }
		             }

		     */
		      for (int i=roi.x; i <= xEnd - n - 1; i+=n2) {
		         for (int j=roi.y; j <= yEnd - n - 1; j+=n2) {
		        	 for(int k=0; k< zRealEnd; k+=p2){
		        	 	int mi = i;
			            int mj = j;
			            int mk = k;
			            // search within the box.
			            for (int i2=i; i2 < i + n2 && i2 < xRealEnd; i2++) {
			               for (int j2=j; j2 < j + n2 && j2 < yRealEnd; j2++) {
			            	   for(int k2=k; k2< k+p2 && k2< zRealEnd; k2++){
				                  // revert getPixel to get after debugging
			            		   if ( iProc3D.getPixel(i2, j2, k2) > iProc3D.getPixel(mi, mj, mk) ) {
				                     mi = i2;
				                     mj = j2;
				                     mk = k2;
				                  }
			            	   }
			               }
			            }
			            // is the candidate really a local maximum?
			            // check surroundings (except for the pixels that we already checked)
			            boolean stop = false;
			            // columns in block to the left
			            if (mi - n < i && i>0) {
	
			               for (int i2=mi-n; i2<i; i2++) {
			                  for (int j2=mj-n; j2<=mj+n; j2++) {
			                	  for(int k2=mk-p; k2<=mk+p; k2++){
				            		   if (  iProc3D.getPixel(i2,j2,k2) > iProc3D.getPixel(mi,mj,mk) ) {
				                        stop = true;
				                     }
			                	}
			                }
			               }
			            }
			            // columns in block to the right
			            if (!stop && mi + n >= i + n2 ) {
	
			               for (int i2=i+n2; i2<=mi+n; i2++) {
			                   for (int j2=mj-n; j2<=mj+n; j2++) {
			                	   for(int k2=mk-p; k2<=mk+p;k2++){
				            		   if ( iProc3D.getPixel(i2, j2, k2) > iProc3D.getPixel(mi, mj, mk)) {
				                        stop = true;
				                     }
				                     
			                	   }
			                  }
			               }
			            }
			            // rows on top of the block
			            if (!stop && mj - n < j && j > 0) {
	
			               for (int j2 = mj - n; j2 < j; j2++) {
			                  for (int i2 = mi - n; i2 <= mi + n; i2++) {
			                	 for(int k2= mk -p; k2<=mk+p; k2++){
				            		   if ( iProc3D.getPixel(i2, j2, k2) > iProc3D.getPixel(mi, mj, mk))
				                        stop = true;
				                  }
			                 }
			               }
			            }
			            // rows below the block
			            if (!stop && mj + n >= j + n2) {
	
			               for (int j2 = j + n2; j2 <= mj + n; j2++) {
			                  for (int i2 = mi - n; i2 <= mi + n; i2++) {
			                	  for(int k2 = mk-p; k2<=mk+p; k2++){
				            		   if (iProc3D.getPixel(i2, j2, k2) > iProc3D.getPixel(mi, mj, mk))
				                        stop = true;
				                  }
			                  }
			               }
			            }
			            // rows and cols under the box
			            if(!stop && mk-p< k && k>0){
	
			            	for(int k2= mk-p; k2< k; k2++){
			            		for(int i2=mi-n; i2<=mi+n; i2++){
			            			for(int j2=mj-n; j2<=mj+n; j2++){
					            		   if(  iProc3D.getPixel(i2, j2, k2) > iProc3D.getPixel(mi, mj, mk))
			            					stop = true ;
			            			}
			            		}
			            	}
			            }
			            // rows and cols over the box
			            if(!stop &&  mk+p>=k+p2){
	
			            	for(int k2=k+p2; k2<=mk+p; k2++){
			            		for(int i2=mi-n; i2<=mi+n; i2++){
			            			for(int j2=mj-n; j2<=mj+n; j2++){
					            		   if(  iProc3D.getPixel(i2, j2, k2) > iProc3D.getPixel(mi, mj, mk))
			            					stop = true ;
			            			}
			            		}
			            	}
			            }
			            
			            if (!stop){
			            	boolean acceptRule = true ;
			            	switch(ruleNumber){
			            		case 1:
			            			acceptRule =rule2(iProc3D,mi,mj,mk,n,p,threshold);
			            			break;
			            		case 2:
			            			acceptRule =rule3(iProc3D,mi,mj,mk,n,p,threshold);
			            			break;
			            		case 3:
			            			acceptRule =rule4(iProc3D,mi,mj,mk,n,p,threshold);
			            			break;
			            		default:
			            			acceptRule =rule3(iProc3D,mi,mj,mk,n,p,threshold);
			            			break;
			            	}  
				            if(threshold == 0 
				            		|| acceptRule ){
				            	
				            	double x = mi-n+0.5; //  lowest z upperleft corner (still in pix), add 0.5 to compensate for the one 
								double y = mj-n+0.5; //  lowest z upperleft corner (still in pix)
								double z = mk-p +0.5;
	
								Point3D<Double> point = new Point3D<Double>(x,y,z);
				            	Gaussian3DSpot spot = new Gaussian3DSpot(point, null, false);
				            	spot.setImagePosition(mk+1, 1) ;
				            	list.add(spot) ;
				            }
		        	}
		      }
	        }
	      }
}
   
   public void FindSpots(ImagePlus iPlus, Rectangle roi, List<Spot> list,int n, int p, int aInt, int pInt, int frame , int threshold, boolean applyFilter, int ruleNumber, boolean symmetric) {
	      
	     // iPlus.setPosition(1,1,frame); // set frame to the beginning
	   	// Set realdata = true, such that it is harder to detect spots at the 
	   // z-borders 0 and 8, as detections there suck anyway - we want less of them.
	    ImageProcessor3D iProc3D = new ImageProcessor3D(iPlus, 1, applyFilter, useRealDataDetection) ;

	      // divide the image up in blocks of size n and find local maxima
	      int n2 = 2*n + 1;
	      int p2 = 2*p+1 ;
	      // calculate borders once
	      int xRealEnd = roi.x + roi.width;
	      int xEnd = xRealEnd - n;
	      int yRealEnd = roi.y + roi.height;
	      int yEnd = yRealEnd - n;
	      int zRealEnd = Utils.numFocalPlanes ;
	      int zEnd = zRealEnd -p;
	      /*
	      for (int i=roi.x; i <= xEnd - n - 1; i+=n2) {
	          for (int j=roi.y; j <= yEnd - n - 1; j+=n2) {
	        	  for(int k=0; k< Utils.numFocalPlanes; k+=1){
		             int mi = i;
		             int mj = j;
		             // set pixel values of the edges.
		             for (int i2=i; i2 < i + n2 && i2 < xRealEnd; i2++) {
		                for (int j2=j; j2 < j + n2 && j2 < yRealEnd; j2++) {
		                   // revert getPixel to get after debugging
		                   if (iProc3D.getPixel(i2, j2,k) > iProc3D.getPixel(mi, mj,k)) {
		                      mi = i2;
		                      mj = j2;
		                   }
		                }
		             }
		             // is the candidate really a local maximum?
		             // check surroundings (except for the pixels that we already checked)
		             boolean stop = false;
		             // columns in block to the left
		             if (mi - n < i && i>0) {
		                for (int i2=mi-n; i2<i; i2++) {
		                   for (int j2=mj-n; j2<=mj+n; j2++) {
		                      if (iProc3D.getPixel(i2, j2,k) > iProc3D.getPixel(mi, mj,k)) {
		                         stop = true;
		                      }
		                   }
		                }
		             }
		             // columns in block to the right
		             if (!stop && mi + n >= i + n2 ) {
		                for (int i2=i+n2; i2<=mi+n; i2++) {
		                    for (int j2=mj-n; j2<=mj+n; j2++) {
		                      if (iProc3D.getPixel(i2, j2,k) > iProc3D.getPixel(mi, mj,k)) {
		                         stop = true;
		                      }
		                   }
		                }
		             }
		             // rows on top of the block
		             if (!stop && mj - n < j && j > 0) {
		                for (int j2 = mj - n; j2 < j; j2++) {
		                   for (int i2 = mi - n; i2 <= mi + n; i2++) {
		                      if (iProc3D.getPixel(i2, j2,k) > iProc3D.getPixel(mi, mj,k))
		                         stop = true;
		                   }
		                }
		             }
		             // rows below the block
		             if (!stop && mj + n >= j + n2) {
		                for (int j2 = j + n2; j2 <= mj + n; j2++) {
		                   for (int i2 = mi - n; i2 <= mi + n; i2++) {
		                      if (iProc3D.getPixel(i2, j2,k) > iProc3D.getPixel(mi, mj,k))
		                         stop = true;
		                   }
		                }
		             }

		     */
		      for (int i=roi.x; i <= xEnd - n - 1; i+=n2) {
		         for (int j=roi.y; j <= yEnd - n - 1; j+=n2) {
		        	 for(int k=0; k< zRealEnd; k+=p2){
		        	 	int mi = i;
			            int mj = j;
			            int mk = k;
			            // search within the box.
			            for (int i2=i; i2 < i + n2 && i2 < xRealEnd; i2++) {
			               for (int j2=j; j2 < j + n2 && j2 < yRealEnd; j2++) {
			            	   for(int k2=k; k2< k+p2 && k2< zRealEnd; k2++){
				                  // revert getPixel to get after debugging
			            		   if ( iProc3D.getPixel(i2, j2, k2) > iProc3D.getPixel(mi, mj, mk) ) {
				                     mi = i2;
				                     mj = j2;
				                     mk = k2;
				                  }
			            	   }
			               }
			            }
			            // is the candidate really a local maximum?
			            // check surroundings (except for the pixels that we already checked)
			            boolean stop = false;
			            // columns in block to the left
			            if (mi - n < i && i>0) {
	
			               for (int i2=mi-n; i2<i; i2++) {
			                  for (int j2=mj-n; j2<=mj+n; j2++) {
			                	  for(int k2=mk-p; k2<=mk+p; k2++){
				            		   if (  iProc3D.getPixel(i2,j2,k2) > iProc3D.getPixel(mi,mj,mk) ) {
				                        stop = true;
				                     }
			                	}
			                }
			               }
			            }
			            // columns in block to the right
			            if (!stop && mi + n >= i + n2 ) {
	
			               for (int i2=i+n2; i2<=mi+n; i2++) {
			                   for (int j2=mj-n; j2<=mj+n; j2++) {
			                	   for(int k2=mk-p; k2<=mk+p;k2++){
				            		   if ( iProc3D.getPixel(i2, j2, k2) > iProc3D.getPixel(mi, mj, mk)) {
				                        stop = true;
				                     }
				                     
			                	   }
			                  }
			               }
			            }
			            // rows on top of the block
			            if (!stop && mj - n < j && j > 0) {
	
			               for (int j2 = mj - n; j2 < j; j2++) {
			                  for (int i2 = mi - n; i2 <= mi + n; i2++) {
			                	 for(int k2= mk -p; k2<=mk+p; k2++){
				            		   if ( iProc3D.getPixel(i2, j2, k2) > iProc3D.getPixel(mi, mj, mk))
				                        stop = true;
				                  }
			                 }
			               }
			            }
			            // rows below the block
			            if (!stop && mj + n >= j + n2) {
	
			               for (int j2 = j + n2; j2 <= mj + n; j2++) {
			                  for (int i2 = mi - n; i2 <= mi + n; i2++) {
			                	  for(int k2 = mk-p; k2<=mk+p; k2++){
				            		   if (iProc3D.getPixel(i2, j2, k2) > iProc3D.getPixel(mi, mj, mk))
				                        stop = true;
				                  }
			                  }
			               }
			            }
			            // rows and cols under the box
			            if(!stop && mk-p< k && k>0){
	
			            	for(int k2= mk-p; k2< k; k2++){
			            		for(int i2=mi-n; i2<=mi+n; i2++){
			            			for(int j2=mj-n; j2<=mj+n; j2++){
					            		   if(  iProc3D.getPixel(i2, j2, k2) > iProc3D.getPixel(mi, mj, mk))
			            					stop = true ;
			            			}
			            		}
			            	}
			            }
			            // rows and cols over the box
			            if(!stop &&  mk+p>=k+p2){
	
			            	for(int k2=k+p2; k2<=mk+p; k2++){
			            		for(int i2=mi-n; i2<=mi+n; i2++){
			            			for(int j2=mj-n; j2<=mj+n; j2++){
					            		   if(  iProc3D.getPixel(i2, j2, k2) > iProc3D.getPixel(mi, mj, mk))
			            					stop = true ;
			            			}
			            		}
			            	}
			            }
			            
			            if (!stop){
			            	boolean acceptRule = true ;
			            	switch(ruleNumber){
			            		case 1:
			            			acceptRule =rule2(iProc3D,mi,mj,mk,n,p,threshold);
			            			break;
			            		case 2:
			            			acceptRule =rule3(iProc3D,mi,mj,mk,n,p,threshold);
			            			break;
			            		case 3:
			            			acceptRule =rule4(iProc3D,mi,mj,mk,n,p,threshold);
			            			break;
			            		default:
			            			acceptRule =rule3(iProc3D,mi,mj,mk,n,p,threshold);
			            			break;
			            	}  
				            if(threshold == 0 
				            		|| acceptRule ){
				            	boolean realData = true ;
				            	Sample sample = Utils.sampleCuboidFromImage3D(iPlus, mi-aInt, mj-aInt, 2*aInt+1, mk-pInt, 2*pInt+1, 1, realData) ;
								int[] length = sample.getSize();
				            	// tell the spot where the box around it is located in space
								double x = mi-(length[0]-1)/2+0.5; //  lowest z upperleft corner (still in pix), add 0.5 to compensate for the one 
								double y = mj-(length[1]-1)/2+0.5; //  lowest z upperleft corner (still in pix)
								double z = mk-(length[2]-1)/2 +0.5; // lowest z upperleft corner (still in pix), add 0.5 to get later really in the center of the box.
								//in z a problem may easily occur: the box crashes into the wall -> that we have to account for..
								if( mk-pInt < 0)
									z = 0.5 ;
								else if (mk+pInt >= Utils.numFocalPlanes)
									z = mk-pInt+0.5 ;
								
								
								Point3D<Double> point = new Point3D<Double>(x,y,z);
				            	Gaussian3DSpot spot = new Gaussian3DSpot(point, sample, symmetric);
				            	spot.setImagePosition(mk+1, frame) ;
				            	list.add(spot) ;
				            }
		        	}
		      }
	        }
	      }
}
   
   protected static boolean rule1(ImageProcessor3D iProc3D, int mi, int mj, int mk, int n, int p, int threshold){
	   double test = iProc3D.getPixel(mi, mj, mk); 
       test-=(iProc3D.getPixel(mi - n , mj - n, mk) + iProc3D.getPixel(mi -n, mj + n, mk) +
       		iProc3D.getPixel(mi + n, mj  - n, mk) + iProc3D.getPixel(mi + n, mj + n, mk)) / 12;
       test-= (iProc3D.getPixel(mi - n , mj - n, mk+p) + iProc3D.getPixel(mi -n, mj + n, mk+p) +
                  iProc3D.getPixel(mi + n, mj  - n, mk+p) + iProc3D.getPixel(mi + n, mj + n, mk+p)) / 12;
       test-=(iProc3D.getPixel(mi - n , mj - n, mk-p) + iProc3D.getPixel(mi -n, mj + n, mk-p) +
                  iProc3D.getPixel(mi + n, mj  - n, mk-p) + iProc3D.getPixel(mi + n, mj + n, mk-p)) / 12;
       return test > threshold ;
   }
   protected static boolean rule2(ImageProcessor3D iProc3D, int mi, int mj, int mk, int n, int p, int threshold){
	   // in plane
	   double test1 = iProc3D.getPixel(mi, mj, mk); 
       test1-=(iProc3D.getPixel(mi - n , mj - n, mk) + iProc3D.getPixel(mi -n, mj + n, mk) +
       		iProc3D.getPixel(mi + n, mj  - n, mk) + iProc3D.getPixel(mi + n, mj + n, mk)) / 4;
       // higher plane
       double test2 = iProc3D.getPixel(mi, mj, mk+p);
       test2-= (iProc3D.getPixel(mi - n , mj - n, mk+p) + iProc3D.getPixel(mi -n, mj + n, mk+p) +
                  iProc3D.getPixel(mi + n, mj  - n, mk+p) + iProc3D.getPixel(mi + n, mj + n, mk+p)) / 4;
       // lower plane
       double test3= iProc3D.getPixel(mi, mj, mk-p);
       test3-=(iProc3D.getPixel(mi - n , mj - n, mk-p) + iProc3D.getPixel(mi -n, mj + n, mk-p) +
                  iProc3D.getPixel(mi + n, mj  - n, mk-p) + iProc3D.getPixel(mi + n, mj + n, mk-p)) / 4;
       return test1 >threshold && test2>threshold && test3> threshold ;
   }
   protected static boolean rule3(ImageProcessor3D iProc3D, int mi, int mj, int mk, int n, int p, int threshold){
	   // in plane
	   double test1 = iProc3D.getPixel(mi, mj, mk); 
       double test12 =(iProc3D.getPixel(mi - n , mj - n, mk) + iProc3D.getPixel(mi -n, mj + n, mk) +
       		iProc3D.getPixel(mi + n, mj  - n, mk) + iProc3D.getPixel(mi + n, mj + n, mk)) / 4;
       boolean b1 = test1-test12>threshold ;

       // higher plane
       double test2 = iProc3D.getPixel(mi, mj, mk+p);
       double test22 = (iProc3D.getPixel(mi - n , mj - n, mk+p) + iProc3D.getPixel(mi -n, mj + n, mk+p) +
                  iProc3D.getPixel(mi + n, mj  - n, mk+p) + iProc3D.getPixel(mi + n, mj + n, mk+p)) / 4;
       boolean b2 = test2-test22>threshold ;

       // lower plane
       double test3= iProc3D.getPixel(mi, mj, mk-p);
       double test32=(iProc3D.getPixel(mi - n , mj - n, mk-p) + iProc3D.getPixel(mi -n, mj + n, mk-p) +
                  iProc3D.getPixel(mi + n, mj  - n, mk-p) + iProc3D.getPixel(mi + n, mj + n, mk-p)) / 4;
       boolean b3 = test3-test32>threshold ;
       
       return b1 && (b2 || b3) ; // at least one of them
   }
   protected static boolean rule4(ImageProcessor3D iProc3D, int mi, int mj, int mk, int n, int p, int threshold){
	   double border1 =0;
	   //int borderCount =0;
	   double volume1 = 0 ;
	   double border2 =0;
	   double volume2 = 0 ;
	   double border3 =0;
	   double volume3 = 0 ;
	   // sum every pixel on first plane
	   for(int i= mi-n; i<= mi+n; i++){
		   for(int j= mj-n; j<=mj+n; j++){
				   if( i== mi-n || i==mi+n){ // x border
					   border1 += iProc3D.getPixel(i, j, mk+1);
				   } else if(j==mj-n || j== mj+n){ // y border
					   border1 += iProc3D.getPixel(i, j, mk+1);
				   }  else { // not at the border -> within !
					   volume1 += iProc3D.getPixel(i, j, mk+1);
				   }
		   }
	   }
	// sum every pixel on second plane
	   for(int i= mi-n; i<= mi+n; i++){
		   for(int j= mj-n; j<=mj+n; j++){
				   if( i== mi-n || i==mi+n){ // x border
					   border2 += iProc3D.getPixel(i, j, mk);
				   } else if(j==mj-n || j== mj+n){
					   border2 += iProc3D.getPixel(i, j, mk);
				   }  else { // not at the border -> within !
					   volume2 += iProc3D.getPixel(i, j, mk);
				   }
		   }
	   }
	// sum every pixel on third plane
	   for(int i= mi-n; i<= mi+n; i++){
		   for(int j= mj-n; j<=mj+n; j++){
				   if( i== mi-n || i==mi+n){ // x border
					   border3 += iProc3D.getPixel(i, j, mk-1);
				   } else if(j==mj-n || j== mj+n){
					   border3 += iProc3D.getPixel(i, j, mk-1);
				   }  else { // not at the border -> within !
					   volume3 += iProc3D.getPixel(i, j, mk-1);
				   }
		   }
	   }
	   // normalize
	   border1 /= 8*n;
	   border2 /= 8*n;
	   border3 /= 8*n;
	   volume1/= (2*n-1)*(2*n-1);
	   volume2/= (2*n-1)*(2*n-1);
	   volume3/= (2*n-1)*(2*n-1);
	   return volume2-border2 > threshold && ((volume1-border1 > threshold) || (volume3-border3 > threshold)) ;
   }
  
   // Filters local maxima list using the ImageJ findMaxima Threshold algorithm
   public static Polygon noiseFilter(ImageProcessor iProc, Polygon inputPoints, int threshold)
   {
      Polygon outputPoints = new Polygon();

      for (int i=0; i < inputPoints.npoints; i++) {
         int x = inputPoints.xpoints[i];
         int y = inputPoints.ypoints[i];
         int value = iProc.getPixel(x, y) - threshold;
         if (    value > iProc.getPixel(x-1, y-1) ||
                 value > iProc.getPixel(x-1, y)  ||
                 value > iProc.getPixel(x-1, y+1)||
                 value > iProc.getPixel(x, y-1) ||
                 value > iProc.getPixel(x, y+1) ||
                 value > iProc.getPixel(x+1, y-1) ||
                 value > iProc.getPixel(x+1, y) ||
                 value > iProc.getPixel(x+1, y+1)
               )
            outputPoints.addPoint(x, y);
      }

      return outputPoints;
   }
   
  

}
