package lib;

import java.util.Iterator;
import java.util.NoSuchElementException;

/***************************************
 * Only a simple container Class without
 * any ImageProcessing.
 * @author james
 ***************************************/
public class Sample implements Iterable<Double> {
	/*
	 * Private Member
	 */
	private double[][][] I; //Intensity 3D
	private int lX; // length X
	private int lY; // length Y
	private int lZ; // length Z
	
	/*
	 * Constructor
	 */
	public Sample(int lX_, int lY_, int lZ_){
		lX=lX_;
		lY=lY_ ;
		lZ=lZ_;
		I= new double[lX][lY][lZ];
	}
	public Sample(double[] I_){
		double[][][] temp = new double[I_.length][1][1] ;
		for(int i=0; i< I_.length; i++)
			temp[i][0][0] = I_[i];
		I = temp ;
		lX= I.length;
		lY=1;
		lZ=1;
	}
	public Sample(double[][] I_){
		double[][][] temp = new double[I_.length][I[0].length][1] ;
		for(int i=0; i< I_.length; i++){
			for(int j=0; j< I_[0].length; j++){
				temp[i][j][0] = I_[i][j];
			}
		}
			
		I = temp ;
		lX= I.length;
		lY=I[0].length;
		lZ=1;
	}
	public Sample( double[][][] I_){
		// just copy the shit
		lX=I_.length ;
		lY=I_[0].length ;
		lZ = I_[0][0].length ;
		I= I_ ;
	}
	/*
	 * Returns the sum of Integers
	 * of a certain region.
	 */
	public double sumInt(){
		int result = 0 ;
		for(double i:this)
			result+=i ;
		return result ;
	}
	/*
	 * Used as an estimator for the background.
	 */
	public double sumAveragedBorder(){
		double result =0 ;
		int circumference = 0 ;
		if( lZ>1){
			for(int i=0; i< lX; i++){
				for(int j=0; j< lY; j++){
					circumference+=2;
					result+=  I[i][j][lZ-1];
					result+=  I[i][j][0];
				}
			}
		}
		for(int j=0; j< lY; j++){ // upper and lower border
			for(int k=0; k< lZ; k++){
				circumference+=2;
				result += I[0][j][k];
				result+= I[lX-1][j][k];
			}
		}
		for(int i=0; i< lX; i++){ //right and left border
			for(int k=0; k< lZ; k++){
				circumference+=2;
				result+= I[i][0][k];
				result+=I[i][lY-1][k];
			}
		}
		return result/circumference ;
	}
	/*
	 * Returns CenterOfMass location.
	 */
	public Point3D<Double> centerOfMass(){
		double x_ =0;
		double y_ =0 ;
		double z_=0;
		for(int i=0; i< lX; i++){
			for(int j=0; j< lY; j++){
				for(int k=0; k< lZ; k++){
					x_ += I[i][j][k]*(i);
					y_+=I[i][j][k]*(j);
					z_+=I[i][j][k]*(k) ;
				}
			}
		}
		x_/= sumInt() ; // 
		y_ /=sumInt() ; // 
		z_ /=sumInt() ; // 

		return new Point3D<Double>(x_,y_,z_) ;
	}
	/*
	 * Returns sample coordinates of pixel with max. intensity
	 */
	public Point3D<Integer> maxIntPixel(){
		double max =0;
		int[] coord = new int[3];
		
		coord[0]=0;
		coord[1]=0;
		coord[2]=0;
		// go through this thing and get the coordinates of the maximum
		for(MyIterator iter = (MyIterator) this.iterator(); iter.hasNext();){
			double candidate = (Double) iter.next();
			if(candidate > max ){
				max = candidate ;
				coord = toCoord(iter.getCurrentCouter()-1) ;
			}
		}
		return new Point3D<Integer>(coord[0], coord[1], coord[2]) ;
	}
	public String toString(){
		String res="" ;
		for(MyIterator iter = (MyIterator) this.iterator(); iter.hasNext();){
			res+="I="+iter.next()+" @(" ;
			int coords[] = toCoord(iter.getCurrentCouter()-1) ;
			res+= coords[0]+", " ;
			res+= coords[1]+", ";
			res+= coords[2]+" )\n" ;
		}
		return res ;
	}
	/*
	 * Returns maximum Intensity.
	 */	
	public double maxInt(){
		Point3D<Integer> pM = this.maxIntPixel();
		return I[pM.getX()][pM.getY()][pM.getZ()] ;
	}
	public void add(double toAdd){
		for(MyIterator iter = (MyIterator) this.iterator(); iter.hasNext();){
			int[] coords = this.toCoord(iter.getCurrentCouter());
			I[coords[0]][coords[1]][coords[2]] = iter.next() + toAdd ;
		}
	}
	/*
	 * Duplicate method (creates deep copy).
	 */
	public Object duplicate(){
		int[] size = this.getSize() ;
		Sample dup = new Sample(size[0], size[1], size[2]) ;
		for(MyIterator iter = (MyIterator) this.iterator(); iter.hasNext();){
			int[] coords = this.toCoord(iter.getCurrentCouter()) ;
			double I = iter.next();
			dup.setI(I, coords[0], coords[1], coords[2]) ;
		}
		return dup ;
	}
	/*
	 * 0-based !
	 */
	public void setI(double I_, int i, int j, int k){
		I[i][j][k] = I_ ;
	}

	/*
	 * Getter for {lX,lY,lZ}
	 */
	public int[] getSize(){
		return new int[]{lX,lY,lZ} ;
	}
	
	@Override
	public Iterator<Double> iterator() {
		return new MyIterator(this) ;
	}
	/*
	 * Compute (x,y,z) from a single number.
	 */
	public int[] toCoord(int n){
		int z = (n) /( lX*lY) ; // to go to in x zero based
		int y = ((n -z*lX*lY)/ lX ); // where we are in y.
		int x = n -z*lX*lY-y*lX; // where we are in z.
		return new int[]{x,y,z};
	}
	/*
	 * Compute counter from coordinates. 
	 * 0-based coordinates, please!
	 */
	public int toCount(int x, int y, int z){
		return z*lX*lY+y*lX+x ;
	}
	/*
	 * Iterator Class
	 */
	protected class MyIterator implements Iterator<Double>{
		/*
		 * Private members
		 */
		private int counter ;
		private Sample parent ;
		private int[] size ;
		
		public MyIterator(Sample parent_){
			parent 	= parent_ ;
			size	= parent.getSize() ;
			counter = 0 ; //zero-based !!
		}
		
		@Override
		public boolean hasNext() {
			
			if( this.counter+1< size[0]*size[1]*size[2]){
				return true ;
			} else{
				return false;
			}
		}

		@Override
		public Double next() throws NoSuchElementException{
			if(! this.hasNext()) 
				throw new NoSuchElementException() ;
			int[] coord = toCoord(this.counter);
			this.counter++;
			return  parent.I[coord[0]][coord[1]][coord[2]];
		}
		public int getCurrentCouter(){
			return this.counter;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
}
