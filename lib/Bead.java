package lib;
/*
 * Bead Class
 */
public class Bead{
	private double x;
	private double y;
	private int dX;
	private int dY;
	private int a;
	private int distTolerance ;
	
	public Bead(double x, double y, int a, int distTolerance){
		this.x= x ;
		this.y= y ;
		this.a = a ;
		this.distTolerance = distTolerance ;
	}
	public String toString(){
		return "Bead @ {x="+String.format("%d", (int)x)+", y="+String.format("%d", (int)y)+","+" a="+a+"}";
	}
	public double getX(){
		return x;
	}
	public double getY(){
		return y ;
	}
	public int getA(){
		return a;
	}
	public int getDistTolerance(){
		return distTolerance ;
	}
}