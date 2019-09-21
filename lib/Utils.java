package lib;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import ij.process.StackConverter;
/****************************************
 * Utils Library Class
 * @author james
 * Provides static functions used throughout 
 * the program.
 ****************************************/
public class Utils {

    public final static String jpeg = "jpeg";
    public final static String jpg = "jpg";
    public final static String gif = "gif";
    public final static String tiff = "tiff";
    public final static String tif = "tif";
    public final static String png = "png";
    public final static String cal ="cal" ;
    
    /*
     * Very important constants
     */
    // the total number of focal planes, a bit redundant, as
    // size(mappingOrder) = numFocalPlanes.
    // but perhaps only subset is to be used(?)
    // must an odd square number, e.g. 3*3=9
    public final static int numFocalPlanes = 9; 
    /*****************************************
     * this array relates an image index running
     * through all images (from upper left corner, to lower right corner, reading direction)
     * to the actual physical order in space. 
     * E.g. mappingOrder[secondImage in Stack] =  - third image in space
     *****************************************/
    public final static int[] mappingOrder = {-4,-3,-2,-1,0,1,2,3,4} ;
    /*
     * Get the extension of a file.
     */  
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    public static String getExtension(String fName) {
        String ext = null;
        String s = fName ;
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    public static boolean accept(File f) {
        if (f.isDirectory()) {
            return false;
        }

        String extension = Utils.getExtension(f);
        if (extension != null) {
            if ( 
                extension.equals(Utils.tiff) ||
                //extension.equals(Utils.gif) ||
                //extension.equals(Utils.jpeg) ||
                //extension.equals(Utils.jpg) ||
                //extension.equals(Utils.png)) {
            		extension.equals(Utils.tif) )
                    return true;
            } else {
                return false;
            }
        return false;
    }
    /*
     * Convert between central subimage coordinates and 
     * the coordinates of some other subimage.
     * @params x,y coordinates on whole image in central subimage. si
     * subimage number ranging from 0 to #subimages-1.
     */
    public static int[] sameSpotInSubImage(int x, int y, int dX, int dY, int si, int[] dimensions){
    	int[] vec = new int[2];
    	int rows = (int)Math.sqrt(numFocalPlanes); // = #cols
    	int[] coords = convertFromSubImageEnumeration(si);
    	int wX = dimensions[0]/rows ; // width of subimage
    	int wY = dimensions[1]/rows ; // height of subimage
    	int xCentral = rows/2 * wX ; // upper left corner of central subimage
    	int yCentral = rows/2 * wY ; // "" ""
    	vec[0] = coords[0]*wX + (x-xCentral) ; //  respective pixel in subimage si
    	vec[1] = coords[1]*wY + (y-yCentral) ;
    	// now add the offset.
    	vec[0]+= (coords[0]-rows/2)*dX ;
    	vec[1]+= (coords[1]-rows/2)*dY;
    	return vec ;
    }
    /*
     * Calculate an x and y integer coordinate from an subimage 
     * enumeration number, so that the upper left image always 
     * has the coordinate (0,0) and the lower right 
     * (Sqrt(numFocalPlanes), Sqrt(numFocalPlanes)). 
     */
    public static int[] convertFromSubImageEnumeration(int si){
    	int rows = (int)Math.sqrt(numFocalPlanes);
    	int j = si/rows ; // (i,j) picture coordinates in 2d pic array
    	int i = si-j*rows ; // both indices are 0-based
    	return new int[]{i,j} ;
    }
    /*
     * Convenience function needed to pull apart
     * a 2- or 3D array of whatever type.
     */
    public <T> ArrayList<T> straighten(T[][][] array, int rowLength, int columnLength){
    	ArrayList<T> result = new ArrayList<T> ();
    	for(int k=0; k< array[0][0].length; k++){
    		for(int j=0; j< columnLength; j++){
    			for(int i=0; i< rowLength; i++){
    				result.add(array[i][j][k]) ;
    			}
    		}
    	}
    	return result;
    }
    /*
     * Calculates using Utils.numOfFocalPlanes
     * the dimension of the subimages.
     * (n+1)*(n+1)=#SubImages with n: number of inner subimageborders in either vertical or horizontal direction.
     * => n = -1 +Sqrt(#Subimages)
     * @return double array with dimensions [#numfocalplanes][2][2]
     * so that you can get the left x and upper y border of any subimage
     * by using bordersOf2DSubImages (int dim) [ subimagenumber ][0][x or y]
     * with x -> 0 and y -> 1
     * and the right x and lower y border by using
     * by using bordersOf2DSubImages (int dim) [ subimagenumber ][1][x or y]
     */
    public static double[][][] bordersOf2DSubImages(int[] dimension){
    	int rows =  (int)Math.sqrt(numFocalPlanes) ; // hope and pray -should work!
    	double subImageLength = ((double)dimension[0])/rows ; // image is quadratic.
    	double[][][] borders = new double[Utils.numFocalPlanes][2][2] ;
    	for(int i=0; i< Utils.numFocalPlanes; i++){
    		int[] coords = convertFromSubImageEnumeration(i);
    		borders[i][0][0] = coords[0]*subImageLength ;
    		borders[i][0][1] = coords[1]*subImageLength ;
    		borders[i][1][0] = (coords[0]+1)*subImageLength ;
    		borders[i][1][1] = (coords[1]+1)*subImageLength ;
    	}
    	return borders ;
    }
    public static Sample sampleFromImage3D(ImagePlus imp, int x0, int y0, int f0, int x1, int y1, int f1){
		return null;
    	
    }
    public static boolean containsNonAlphaNumeric(String toTest){
    	Pattern p = Pattern.compile("[^a-zA-Z0-9]");
    	return p.matcher(toTest).find();
    }
    public static boolean isNumeric(String toTest){

    	   try  
    	   {  
    	      Integer.parseInt( toTest );  
    	      return true;  
    	   }  
    	   catch( NumberFormatException e )  
    	   {  
    	      return false;  
    	   }  
    }
    public static Sample sampleFromImage2D(ImagePlus imp, int x0, int y0,  int x1, int y1, int f){
    	
    	Sample toReturn = new Sample(x1-x0+1, y1-y0+1, 1);
    	
    	ImageProcessor ip = imp.getStack().getProcessor(f); // get the right image processor.
    	for(int i=x0; i<= x1;i++ ){
    		for(int j=y0; j<= y1; j++){
    			toReturn.setI(ip.getPixel(i, j), i-x0, j-y0, 0) ;
    		}
    	}
		return toReturn;
    	
    }
    /*
     * 3D Function
     */
    public static Sample sampleFromImage3D(ImagePlus imp, int x0, int y0, int z0, int a){
    	
    	Sample toReturn = new Sample(a, a, a);
    	
    	for(int i=x0; i< x0+a;i++ ){
    		for(int j=y0; j< y0+a; j++){
    			for(int k=z0-a+1; k<= z0; k++){ // go up from z0-a....
    				imp.setSlice(k+1) ; // z is zero based !!
    		    	ImageProcessor ip = imp.getProcessor(); // get the right image processor.
        			toReturn.setI(ip.getPixel(i, j), i-x0, j-y0, k) ;
    			}
    		}
    	}
		return toReturn;
    }
    public static Sample samplePillarFromImage3D(ImagePlus imp, int x0, int y0, int a, int frame){
    	return sampleCuboidFromImage3D(imp, x0, y0, frame, 0, Utils.numFocalPlanes, frame, true) ;
    }
    public static Sample sampleCuboidFromImage3D(ImagePlus imp, int x0, int y0, int a, int z0, int p, int frame, boolean realData){
    	
		ImageProcessor3D ip = new ImageProcessor3D(imp, frame, false, realData); // get the right image processor.
		 // HERE WE WANT TO SAMPLE REAL DATA and we don't want the 3dprocessor
								// to pretend at the z-edges that there is data beyond, instead everything is zero!
		int lowerZ = z0;
		int upperZ = z0+p ;
		
		if(lowerZ<0){
			lowerZ=0; // zero -based
		}
		if(upperZ > Utils.numFocalPlanes){
			upperZ =Utils.numFocalPlanes ; // 1-based
		} 
		
		
		Sample toReturn = new Sample(a, a, upperZ-lowerZ);
		
    	for(int k=lowerZ; k< upperZ; k++){ // go up from z0....
    		for(int j=y0; j< y0+a; j++){
				for(int i=x0; i< x0+a;i++ ){	
					
        			toReturn.setI(ip.getPixel(i, j, k), i-x0, j-y0, k-lowerZ) ;
    			}
    		}
    	}
		return toReturn;
    }
    /*
     * Convenience. Returns square of length a at (x0,y0).
     */
    public static Sample sampleFromImage2D(ImagePlus imp, int x0, int y0, int a, int f){
    	return sampleFromImage2D(imp, x0,y0, x0+a,y0+a, f) ;
    }
    /*
     * Gives a whole subimage. 
     */
    public static Sample sampleFromImage2D(ImagePlus imp, int si, int f){
    	double[][][] coords = bordersOf2DSubImages(imp.getDimensions()) ;
    	// acoount for rounding error.
    	
    	return sampleFromImage2D(imp, 
    			(int) coords[si][0][0], (int) coords[si][0][1],
    			(int) coords[si][1][0], (int) coords[si][1][1], f) ;
    }
    /*
     * si: 0 based
     * slice as always 1 based!!!
     */
    public static ImageProcessor cropToSubImage(ImagePlus toCrop, int slice, int si){
    	double[][][] borders = Utils.bordersOf2DSubImages(toCrop.getDimensions());
    	Roi cropRoi = new Roi(borders[si][0][0], borders[si][0][1], // upper left corner x,y
    						borders[si][1][0]-borders[si][0][0],	// width 
    						borders[si][1][1]-borders[si][0][1]); 	// height
    	ImageProcessor ip =(toCrop).getStack().getProcessor(slice);
    	ip.setRoi(cropRoi);
    	return ip.crop(); // creates new imageprocessor!
    }
    /*
     * Creates a Stack of #focalPlanes images out of a rawImg.
     */
    public static ImagePlus createSubImageStack(ImageProcessor ip,  boolean mirror, String title) {

		final int rows = (int)Math.sqrt(Utils.numFocalPlanes);
		final int width = ip.getWidth()/rows;
		final int height = ip.getHeight()/rows;

		ImageStack stack = new ImageStack( width, height );

		if (ip instanceof ShortProcessor) {
			short[] slice = new short[width*height];
			int i;
			int n = 0;
			
			for ( int yCell = 0; yCell < rows; ++yCell ) {
				if (mirror) {
					for ( int xCell = 0; xCell < 3; ++xCell ) {
						final int offsetY = yCell * height;
						final int offsetX = xCell * width;
						n++;
						i = 0;
						slice = new short[width*height];
						// load pixels loop
						for ( int y = offsetY; y < offsetY + height; ++y ) {
							for ( int x = offsetX; x < offsetX + width; ++x ) {
								slice[i] = (short) ((float)ip.getPixel( x, y ));
								i++;
							}
						}
						stack.addSlice("Slice " + n,slice);
					}
				}
				else {
					for ( int xCell = rows-1; xCell >= 0; --xCell ) {
						final int offsetY = yCell * height;
						final int offsetX = xCell * width;
						n++;
						i = 0;
						slice = new short[width*height];
						// load pixels loop
						for ( int y = offsetY; y < offsetY + height; ++y ) {
							for ( int x = offsetX; x < offsetX + width; ++x ) {
								slice[i] = (short) ((float)ip.getPixel( x, y ));
								i++;
							}
						}
						stack.addSlice("Slice " + n,slice);
					}					
				}
			}
		}
		else if (ip instanceof ByteProcessor) {
			byte[] slice = new byte[width*height];
			int i;
			int n = 0;
			
			for ( int yCell = 0; yCell < rows; ++yCell ) {
				if (mirror) {
					for ( int xCell = 0; xCell < 3; xCell++ ) {
						final int offsetY = yCell * height;
						final int offsetX = xCell * width;
						n++;
						i = 0;
						slice = new byte[width*height];
						// load pixels loop
						for ( int y = offsetY; y < offsetY + height; ++y ) {
							for ( int x = offsetX; x < offsetX + width; ++x ) {
								slice[i] = (byte) ((float)ip.getPixel( x, y ));
								i++;
							}
						}
						stack.addSlice("Slice " + n,slice);
					}
				}
				else {
					for ( int xCell = rows-1; xCell >= 0; --xCell ) {
						final int offsetY = yCell * height;
						final int offsetX = xCell * width;
						n++;
						i = 0;
						slice = new byte[width*height];
						// load pixels loop
						for ( int y = offsetY; y < offsetY + height; ++y ) {
							for ( int x = offsetX; x < offsetX + width; ++x ) {
								slice[i] = (byte) ((float)ip.getPixel( x, y ));
								i++;
							}
						}
						stack.addSlice("Slice " + n,slice);
					}					
				}
			}
		}
		
		ImagePlus returnStack = new ImagePlus(title,stack);
		// convert to 8-bit stack for readability with ij3d
		if (ip instanceof ShortProcessor) {
			new StackConverter(returnStack).convertToGray16();
		}
		return returnStack;
		
	}
    /*
     * Extracts the 3d stack corresponding to a single instant
     * in time from a hyperstack (x,y,z,t). The images are duplicated
     * and therefore the stack is independent from the hyperstack.
     * Frame is one based!
     */
    public static ImagePlus extractFrame(ImagePlus hyperstack, int frame){
    	ImageStack stack = hyperstack.getImageStack() ;

    	ImageStack toReturn = new ImageStack(stack.getWidth(), stack.getHeight()) ;
    	for(int i=1; i<= Utils.numFocalPlanes; i++){
    		toReturn.addSlice(stack.getProcessor((frame-1)*Utils.numFocalPlanes+i));
    	}
    	return new ImagePlus("Frame "+hyperstack.getT(), toReturn) ;
    }
}