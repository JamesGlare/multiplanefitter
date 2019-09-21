package gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.EllipseRoi;
import ij.gui.ImageCanvas;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.StackWindow;

import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;

import lib.Point3D;
import lib.Sample;
import lib.Utils;

import processing.ZBeadCalibration;

/***************************************
 * SelectionImageCanvas.
 * My own canvas, so that I can play with stuff.
 * Maybe I'll write an interface so that I can
 * call on whatever function.
 * @author james
 ***************************************/
public class SelectionImageCanvas extends ImageCanvas{
	/*
	 * Private Members
	 */
	private static final long serialVersionUID = 1L;
	private MouseToClass toCall; // tab or whatever to be called
	private StackWindow win ;
	private Overlay overlay ;
	private List<Roi> roiList ;
	/*
	 * Constructor
	 */
	public SelectionImageCanvas(ImagePlus imp, MouseToClass toCall) {
		super(imp); // create real imagecanvas
		this.toCall = toCall ;
		win= new StackWindow(imp, this) ;
		win.setVisible(true) ;
		overlay= new Overlay() ;
		imp.setOverlay(overlay);
		roiList = new ArrayList<Roi>() ;
		this.drawSubImageGrid();
	}
	public void mouseClicked(java.awt.event.MouseEvent e){
		if( toCall != null){
			java.awt.Point cursorLoc =this.getCursorLoc();
			toCall.clicked((int)cursorLoc.getX(), (int)cursorLoc.getY(), imp.getCurrentSlice());
		}
	}
		
	/*
	 * Draws a circle at the specified position.
	 */
	public void addCircle(int x, int y, int a, int distTolerance, int[] coordOffset){
		//EllipseRoi ellipseRoi = new EllipseRoi((int)x-tail, (int)y-tail, (int)x+tail,(int)y+tail,1) ;
		//overlay.add(ellipseRoi);
		//roiList.add(ellipseRoi);

		// Now add this circle on every subimage
		int[] dimensions = win.getImagePlus().getDimensions();
		for(int i=0; i< Utils.numFocalPlanes; i++){
			int[] vec = Utils.sameSpotInSubImage(x, y, coordOffset[0], coordOffset[1],i, dimensions);
			// Now, refine the position of the subimage bead!!!
			Sample sample = Utils.sampleFromImage2D(win.getImagePlus(), vec[0]-distTolerance, vec[1]-distTolerance,distTolerance*2+1, win.getImagePlus().getCurrentSlice()) ;
			Point3D<Integer> vecRefined = sample.maxIntPixel();
			int upperLeftX = vec[0]-distTolerance+vecRefined.getX()-a ;
			int upperLeftY = vec[1]-distTolerance+vecRefined.getY()-a ;
			Roi roi = new Roi(upperLeftX, upperLeftY, 2*a+1, 2*a+1, 1) ;
			overlay.add(roi);
			roiList.add(roi);
		}
	}
	/*
	 * Adds rectangle @(x-a/2,y-a/2) with side length a.
	 * ! Doesnt add this rectangle on every subimage.
	 * x and y must be wholeImage coordinates!
	 */
	public void markSpot(double x, double y, int a){
		float upperLeftX = (float) (x-a/2);
		float upperRightX = (float) (x+a/2) ;
		float lowerLeftX = (float) (x-a/2);
		float lowerRightX= (float) (x+a/2) ;
		float upperLeftY = (float) (y-a/2);
		float upperRightY = (float) (y-a/2) ;
		float lowerLeftY = (float) (y+a/2);
		float lowerRightY= (float) (y+a/2) ;
		PolygonRoi roi = new PolygonRoi(new float[]{upperLeftX, upperRightX,  lowerRightX, lowerLeftX},
										new float[]{upperLeftY,upperRightY,lowerRightY,lowerLeftY} ,4,Roi.POLYGON);
		roi.setStrokeColor(Color.red) ;
		roi.setStrokeWidth(1);
		overlay.add(roi) ;
		/*float xCenter = (float) (x) ;
		float yCenter = (float)(y) ;
		PolygonRoi point = new PolygonRoi(new float[]{xCenter, xCenter, xCenter},
							new float[]{yCenter} ,1,Roi.Polygon);
		point.setStrokeColor(Color.orange) ;
		point.setStrokeWidth(2);
		overlay.add(point) ;*/
		this.imp.repaintWindow();
	}
	/*
	 * Draw lines on every subimageborder.
	 */
	protected void drawSubImageGrid(){
		int[] dimensions = win.getImagePlus().getDimensions();
		double[][][] subImageBorder =  Utils.bordersOf2DSubImages(dimensions);
		for(int i=0; i< (int)Math.sqrt(Utils.numFocalPlanes)-1; i++){
			// from top to bottom
			Line lineY = new Line(subImageBorder[i][1][0], 0,subImageBorder[i][1][0],dimensions[1]);
			// from left to right
			Line lineX = new Line(0, subImageBorder[i][1][0], dimensions[0], subImageBorder[i][1][0] );
			lineX.setStrokeColor(Color.blue);
			lineY.setStrokeColor(Color.blue) ;
			this.overlay.add(lineX);
			this.overlay.add(lineY);
		}
	}
	/*
	 * Getter Function
	 */
	public ImagePlus getImage(){
		return this.imp;
	}
	/*
	 * Removes all circles on every subimage from the image.
	 */
	public void deleteCircle(int index){
		for(int i=0; i< Utils.numFocalPlanes; i++){
			if(this.roiList.size()>=(Utils.numFocalPlanes)*(index+1)-i){
				this.overlay.remove(
					this.roiList.get(index*(Utils.numFocalPlanes)) ) ;	
				this.roiList.remove(index*(Utils.numFocalPlanes)) ;
			}
		}
		this.win.getImagePlus().updateAndDraw();
	}
}
