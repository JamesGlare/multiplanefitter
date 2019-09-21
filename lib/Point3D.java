package lib;

public class Point3D<T extends Number> {

	private T x;
	private T y;
	private T z;
	
	public Point3D (T x_, T y_, T z_){
		x=x_;
		y=y_;
		z=z_;
	}
	public T getX() {
		return x;
	}

	public void setX(T x) {
		this.x = x;
	}

	public T getY() {
		return y;
	}

	public void setY(T y) {
		this.y = y;
	}

	public T getZ() {
		return z;
	}

	public void setZ(T z) {
		this.z = z;
	}
	public String toString(){
		return String.format("%.2f",x)+", "+" "+String.format("%.2f",y)+", "+String.format("%.2f",z) ;
	}

}
