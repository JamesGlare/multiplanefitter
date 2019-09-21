package lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import processing.Spot;

public class SpotTZList implements Iterable<Spot> {

	/*
	 * Private Members
	 */
	private List<Spot>[][] spotList  ;
	private int frames ; // #frames
	private int p = 2 ; // z-search depth
	private double dist = 2.0 ; // distance on xy plane within which spots are considered equal
	/*
	 * Constructor
	 */
	public SpotTZList(int frames_){
		frames = frames_ ;
		// initialize list
		spotList = new List[Utils.numFocalPlanes][frames] ; //generic list
		
		for(int i=0; i< Utils.numFocalPlanes; i++){
			for(int j=0; j<frames; j++){
				spotList[i][j] = new ArrayList<Spot>() ;
			}
		}
		
	}
	
	public void add(Spot spot, boolean checkForDoubles){
		int frame = spot.getFrame();
		int slice = spot.getSlice() ;
		if( !checkForDoubles){
			this.spotList[slice-1][frame-1].add(spot) ;
		} else {
			if (clearWeakerFindings(spot)){
				this.spotList[slice-1][frame-1].add(spot) ;
			}
		}
	}
	public List<Spot> get(int i, int j){
		return spotList[i][j] ;
	}
	protected boolean clearWeakerFindings(Spot spot){
		int z = spot.getSlice() - 1 ;
		int f = spot.getFrame() - 1 ; 
		boolean okAbove = true ;
		boolean okBelow = true ;
		for( int Z = 0; Z< p; Z++){
			if(z+Z < Utils.numFocalPlanes){
				List<Spot> aboveSpots = spotList[z+Z][f] ;
				okAbove = compareToAndCleanList(spot, aboveSpots) ;
			} 
			if( z-Z >= 0){
				List<Spot> belowSpots = spotList[z-Z][f] ;
				okBelow = compareToAndCleanList(spot, belowSpots) ;
			}
		}
		return okAbove && okBelow ;
	}
	protected boolean compareToAndCleanList(Spot spot, List<Spot> list){
		double maxInt = spot.getMaxInt() ;
		boolean add = true ;
		for(int k=0; k< list.size(); k++){
			Point3D<Double> w = list.get(k).getPosition() ;
			Point3D<Double> q = spot.getPosition();
			double distXY = 	(w.getX()-q.getX())*(w.getX()-q.getX()) +
							(w.getY()-q.getY())*(w.getY()-q.getY()) ;
			distXY = Math.sqrt(distXY) ;
			if(distXY < dist){
				// now the two spots are apparently on the same xy position
				if(maxInt >= list.get(k).getMaxInt()){
					list.remove(k) ;
					k-- ; // so that we don't miss spots
				} else {
					add = false ;
				}
			}
		}
		return add ;
	}
	public void clear(){
		for(int i=0; i< Utils.numFocalPlanes; i++){
			for(int j=0; j< frames; j++){
				spotList[i][j].clear() ;
			}
		}
	}
	public int[] toCoords(int c){
		int z = c/frames ;
		int f = c % frames ;
		return new int[]{z,f} ;
	}
	public int size(){
		return numberOnLeaves(0,0,0) ;
	}
	@Override
	public Iterator<Spot> iterator() {
		return new MyIterator() ;
	}
	public int numberOnLeaves(int z, int c, int h){
		int kount =0 ;
		for(int l=z; l<Utils.numFocalPlanes; l++)
			for(int i=0; i< frames; i++){
				if( l>z || i>c )
					kount += this.spotList[l][i].size();
				else if( l==z && i==c)
					kount += this.spotList[l][i].size() -h ; // subtract this one only 
			}
		return kount ;
	}
	public int numberOnThisLeave(int z, int c, int h){
		int kount =0 ;
			for(int i=0; i< frames; i++){
				if(  i>c )
					kount += this.spotList[z][i].size();
				else if(  i==c)
					kount += this.spotList[z][i].size() -h ; // subtract this one only 
			}
		return kount ;
	}
	protected class MyIterator implements Iterator<Spot>{
		int z = 0 ;
		int c = 0;
		int h = 0; 
		
		public MyIterator(){
			z=0;
			c=0;
			h=0;
		}
		
		public int toCounter(int z, int f){
			return z*frames + f ;
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

		@Override
		public Spot next() {
			if(! this.hasNext()) 
				throw new NoSuchElementException() ;
			
			if(  h < spotList[z][c].size()){
				Spot toReturn = spotList[z][c].get(h);
				h++ ;
				return toReturn ;
			}
			else if(c < frames -1){
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
