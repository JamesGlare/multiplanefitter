package lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import processing.Gaussian3DSpot;
import processing.Spot;

/*
 * Messily (because i was in a hurry) written
 * spot container class. 
 * Has #focalPlanes leaves containing
 * N further lists, which can store spots.
 * We can now store the spots with a key
 * corresponding to their position.
 * This makes it easy to group them, according
 * to their position on x,y and z.
 * 
 */
public class SpotTree implements Iterable<Spot> {
	/*
	 * Private Members
	 */
	private ArrayList<ArrayList<Spot>[] > tree ;
	int[] dimensions;
	int[] gridSize ;
	int lX ; // max of x
	int N; 
	int c ; //  internal counter
	int p = 2; // z-search depth
	/*						_______________________
	 * | <- gridSize[0] -> |     ^
	 * |                   |     !
	 * |				   |	gridsize[1]
	 *							 |
	 *							 V
	 *						------------------------
	 */	
	
	/*
	 * Constructor
	 */
	public SpotTree(int[] dimensions_, int[] grid_){
		dimensions = dimensions_ ;
		gridSize = grid_ ;
		lX = dimensions[0]/gridSize[0];
		N = lX*(dimensions[1]/gridSize[1]) ;
		
		tree = new ArrayList< ArrayList<Spot>[] >() ;
		this.initializeList();
	}
	protected int toC(int x, int y){
		return y*lX+ x;
	}
	protected int[] toCoord(int c){
		return new int[]{  c % lX ,c/lX} ;
	}
	protected void initializeList(){
		for(int i=0; i< Utils.numFocalPlanes; i++){
			ArrayList<Spot>[] leave = new ArrayList[N] ;
			for(int k=0; k<N; k++)
				leave[k] = new ArrayList<Spot>() ;
			tree.add(leave) ;
		}
	}
	public int[] key(Point3D<Double> spot){
		
		int z = (int) spot.getZ().doubleValue();
		int x = (int) (spot.getX()/gridSize[0]) ;
		int y = (int) (spot.getY()/gridSize[1]) ; //coarse grained position
		
		return new int[]{x,y,z} ;
	}
	/*
	 * Adds the spot to the tree.
	 * Checks for doubles along the z-Axis with a searching depth
	 * of p. Erases the other spots at the same position if they are
	 * fainter.
	 */
	public void add(Spot spot, boolean checkForDoubles){
		Point3D<Double> pos = spot.getPosition() ;
		int[] key = this.key(pos);
		
		if( !checkForDoubles) // just add
			this.tree.get(key[2])[toC(key[0], key[1])].add(spot) ;
		else {
			// ok, we have stuff to do...
			ArrayList<Spot> doubleList = this.getSpotsAtSamePosition(key[0], key[1], key[2]) ;
			double spotMaxInt = spot.getMaxInt(); // the maximum intensity of the spot we're adding
			double toCheckMaxInt = 0; // the maximum intensity of the spots stored along the z-axis
			for(Spot toCheck : doubleList){ // for every spot in the same position but different z list
				if( toCheck.getFrame() == spot.getFrame())
				toCheckMaxInt = Math.max(toCheckMaxInt, toCheck.getMaxInt() );
			}
			if(toCheckMaxInt < spotMaxInt){ // yes, the spot maybe added
				for(int i=0; i< p;i++){ // erase everything else 
					if(key[2]+i< Utils.numFocalPlanes){ // z+i < #focalPlanes
						this.clearSameFramers(key[0], key[1], key[2]+i, spot.getFrame()) ;
					} if(key[2]-i>=0 ){ // z-i >= 0
						this.clearSameFramers(key[0], key[1], key[2]-i, spot.getFrame()) ;
					}
				}
				this.tree.get(key[2])[toC(key[0], key[1])].add(spot) ;
			}
		}
	}
	
	public ArrayList<Spot> getAllSpotsAt(int x, int y, int z){
		return tree.get(z)[toC(x,y)] ;
	}
	public void clear(){
		for(int k=0; k< Utils.numFocalPlanes; k++){
			for(int i=0; i< N; i++)
				tree.get(k)[i].clear();
		}
	}
	
	protected ArrayList<Spot> getSpotsAtSamePosition(int x, int y, int z){
		ArrayList<Spot> list = new ArrayList<Spot>() ;
		for(int Z=0; Z<p ; Z++){
			if(z+Z< Utils.numFocalPlanes )
				list.addAll(tree.get(z+Z)[toC(x,y)]) ;
			if(z-Z>=0 )
				list.addAll(tree.get(z-Z)[toC(x,y)]) ;
		}
		return list ;
	}
	// Clear every spot on the same frame
	protected void clearSameFramers(int x, int y, int z, int frame){
		ArrayList<Spot> list = tree.get(z)[toC(x,y)] ;
		for(int i=0; i< list.size(); i++){
			Spot spot = list.get(i);
			if(spot.getFrame() == frame){

				list.remove(i) ;
			}
		}
	}
	/*
	 * Size of the tree.
	 */
	public int size(){
		return numberOnLeaves(0,0,0) ;
	}
	protected int numberOnLeaves(int z, int c, int h){
		int kount =0 ;
		for(int l=z; l<Utils.numFocalPlanes; l++)
			for(int i=0; i< N; i++){
				if( l>z || i>c )
					kount += this.tree.get(l)[i].size();
				else if( l==z && i==c)
					kount += this.tree.get(l)[i].size() -h ; // this one only 
			}
		return kount ;
	}
	@Override
	public Iterator<Spot> iterator() {
		
		return new MyIterator();
	}
	protected class MyIterator implements Iterator<Spot>{
		/*
		 * Private Members
		 */
		int z = 0; // leaves
		int c = 0; // lists
		int h = 0; // spots
		/*
		 * Constructor
		 */
		public MyIterator(){
			z=0;
			c=0;
			h=0;
		}
		@Override
		public boolean hasNext() {
			if(z < Utils.numFocalPlanes){
				if(numberOnLeaves(z,c,h)>0){
					return true ;
			} else{
				return false ;
			}
		} else
			return false ;
		}
		public String toString(){
			return "z="+z+" c="+c+" h="+h ;
		}
		@Override
		public Spot next() {
			if(! this.hasNext()) 
				throw new NoSuchElementException() ;
			
			if(  h < tree.get(z)[c].size()){
				Spot toReturn = tree.get(z)[c].get(h);
				h++ ;
				return toReturn ;
			}
			else if(c < N -1){
				h = 0;
				c++ ;
				return next() ;
			} else {
				c = 0;
				h = 0;
				z++;
				return next() ;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
}
