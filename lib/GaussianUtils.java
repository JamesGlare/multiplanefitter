/*
 * Utilities for Gaussian fitting ImageJ plugins
 * Needs org.apache.commons.math and jfreechart
 * Includes the actual Gaussian functions
 */

package lib;

import gui.LogTab;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.special.Erf ;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


/**
 * GaussianUtils. 
 * @author Nico Stuurman
 */
public class GaussianUtils {

   public static final int INT = 0;
   public static final int BGR = 1;
   public static final int XC = 2;
   public static final int YC = 3;
   public static final int ZC = 4;
   public static final int S = 4;
   public static final int S1 = 4;
   public static final int S2 = 5;
   public static final int S3 = 6;

   /**
    * Create a frame with a plot of the data given in XYSeries
    */
   public static void plotData(String title, XYSeries data, String xTitle,
           String yTitle, int xLocation, int yLocation) {
      // JFreeChart code
      XYSeriesCollection dataset = new XYSeriesCollection();
      dataset.addSeries(data);
      JFreeChart chart = ChartFactory.createScatterPlot(title, // Title
                xTitle, // x-axis Label
                yTitle, // y-axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                false, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
            );
      XYPlot plot = (XYPlot) chart.getPlot();
      plot.setBackgroundPaint(Color.white);
      plot.setRangeGridlinePaint(Color.lightGray);
      XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
      renderer.setBaseShapesVisible(true);
      renderer.setSeriesPaint(0, Color.black);
      renderer.setSeriesFillPaint(0, Color.white);
      renderer.setSeriesLinesVisible(0, true);
      Shape circle = new Ellipse2D.Float(-2.0f, -2.0f, 4.0f, 4.0f);
      renderer.setSeriesShape(0, circle, false);
      renderer.setUseFillPaint(true);

      ChartFrame graphFrame = new ChartFrame(title, chart);
      graphFrame.getChartPanel().setMouseWheelEnabled(true);
      graphFrame.pack();
      graphFrame.setLocation(xLocation, yLocation);
      graphFrame.setVisible(true);
   }

   /**
    * Create a frame with a plot of the data given in XYSeries
    */
   public static void plotData2(String title, XYSeries data1, XYSeries data2, String xTitle,
           String yTitle, int xLocation, int yLocation) {
      // JFreeChart code
      XYSeriesCollection dataset = new XYSeriesCollection();
      dataset.addSeries(data1);
      dataset.addSeries(data2);
      JFreeChart chart = ChartFactory.createScatterPlot(title, // Title
                xTitle, // x-axis Label
                yTitle, // y-axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                false, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
            );
      XYPlot plot = (XYPlot) chart.getPlot();
      plot.setBackgroundPaint(Color.white);
      plot.setRangeGridlinePaint(Color.lightGray);
      XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
      renderer.setBaseShapesVisible(true);
      renderer.setSeriesPaint(0, Color.blue);
      renderer.setSeriesFillPaint(0, Color.white);
      renderer.setSeriesLinesVisible(0, true);
      renderer.setSeriesPaint(1, Color.red);
      renderer.setSeriesFillPaint(1, Color.white);
      renderer.setSeriesLinesVisible(1, true);
      Shape circle = new Ellipse2D.Float(-2.0f, -2.0f, 4.0f, 4.0f);   
      renderer.setSeriesShape(0, circle, false);
      Shape square = new Rectangle2D.Float(-2.0f, -2.0f, 4.0f, 4.0f);
      renderer.setSeriesShape(1, square, false);
      renderer.setUseFillPaint(true);

      ChartFrame graphFrame = new ChartFrame(title, chart);
      graphFrame.getChartPanel().setMouseWheelEnabled(true);
      graphFrame.pack();
      graphFrame.setLocation(xLocation, yLocation);
      graphFrame.setVisible(true);
   }

   /**
    * Create a frame with a plot of the data given in XYSeries
    */
   public static void plotDataN(String title, XYSeries[] data, String xTitle,
                 String yTitle, int xLocation, int yLocation, boolean showShapes, Boolean logLog) {
      
      // JFreeChart code
      XYSeriesCollection dataset = new XYSeriesCollection();
      // calculate min and max to scale the graph
      double minX, minY, maxX, maxY;
      minX = data[0].getMinX();
      minY = data[0].getMinY();
      maxX = data[0].getMaxX();
      maxY = data[0].getMaxY();
      for (XYSeries d : data) {
         dataset.addSeries(d);
         if (d.getMinX() < minX)
            minX = d.getMinX();
         if (d.getMaxX() > maxX)
            maxX = d.getMaxX();
         if (d.getMinY() < minY)
            minY = d.getMinY();
         if (d.getMaxY() > maxY)
            maxY = d.getMaxY();
      }
      
      JFreeChart chart = ChartFactory.createScatterPlot(title, // Title
                xTitle, // x-axis Label
                yTitle, // y-axis Label
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Plot Orientation
                true, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
            );
      XYPlot plot = (XYPlot) chart.getPlot();
      plot.setBackgroundPaint(Color.white);
      plot.setRangeGridlinePaint(Color.lightGray);
      if (logLog) {
         LogAxis xAxis = new LogAxis(xTitle);
         xAxis.setTickUnit(new NumberTickUnit(1.0, new java.text.DecimalFormat(), 10));
         plot.setDomainAxis(xAxis);
         plot.setDomainGridlinePaint(Color.lightGray);
         plot.setDomainGridlineStroke(new BasicStroke(1.0f));
         plot.setDomainMinorGridlinePaint(Color.lightGray);
         plot.setDomainMinorGridlineStroke(new BasicStroke(0.2f));
         plot.setDomainMinorGridlinesVisible(true);
         LogAxis yAxis = new LogAxis(yTitle);
         yAxis.setTickUnit(new NumberTickUnit(1.0, new java.text.DecimalFormat(), 10));
         plot.setRangeAxis(yAxis);
         plot.setRangeGridlineStroke(new BasicStroke(1.0f));
         plot.setRangeMinorGridlinePaint(Color.lightGray);
         plot.setRangeMinorGridlineStroke(new BasicStroke(0.2f));
         plot.setRangeMinorGridlinesVisible(true);
      }
      
      
      XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
      renderer.setBaseShapesVisible(true);
      
      for (int i = 0; i < data.length; i++) {
         renderer.setSeriesFillPaint(i, Color.white);
         renderer.setSeriesLinesVisible(i, true);
      } 
      
      renderer.setSeriesPaint(0, Color.blue);
      Shape circle = new Ellipse2D.Float(-2.0f, -2.0f, 4.0f, 4.0f);   
      renderer.setSeriesShape(0, circle, false);
           
      if (data.length > 1) {
         renderer.setSeriesPaint(1, Color.red);
         Shape square = new Rectangle2D.Float(-2.0f, -2.0f, 4.0f, 4.0f);
         renderer.setSeriesShape(1, square, false);
      }
      if (data.length > 2) {
         renderer.setSeriesPaint(2, Color.darkGray);
         Shape rect = new Rectangle2D.Float(-2.0f, -1.0f, 4.0f, 2.0f);
         renderer.setSeriesShape(2, rect, false);
      }
      if (data.length > 3) {
         renderer.setSeriesPaint(3, Color.magenta);
         Shape rect = new Rectangle2D.Float(-1.0f, -2.0f, 2.0f, 4.0f);
         renderer.setSeriesShape(3, rect, false);
      }
      
      if (!showShapes) {
         for (int i = 0; i < data.length; i++) {
            renderer.setSeriesShapesVisible(i, false);
         }
      }
      
      renderer.setUseFillPaint(true);
     
      if (!logLog) {
         // Since the axis autoscale only on the first dataset, we need to scale ourselves
         NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
         yAxis.setAutoRangeIncludesZero(false);
         yAxis.setRangeWithMargins(minY, maxY);

         ValueAxis xAxis = (ValueAxis) plot.getDomainAxis();
         xAxis.setRangeWithMargins(minX, maxX);
      }
      
      ChartFrame graphFrame = new ChartFrame(title, chart);
      graphFrame.getChartPanel().setMouseWheelEnabled(true);
      graphFrame.pack();
      graphFrame.setLocation(xLocation, yLocation);
      graphFrame.setVisible(true);
   }


  
   public static double sqr(double val) {
      return val*val;
   }

   public static double cube(double val) {
      return val * val * val;
   }

 /**
    * Gaussian function of the form:
    * A *  exp(-((x-xc)^2+(y-yc)^2)/(2 sigy^2))+b
    * A = params[INT]  (amplitude)
    * b = params[BGR]  (background)
    * xc = params[XC]
    * yc = params[YC]
    * zc=params[ZC]
    * sig = params[S]
    */
   public static double gaussian(double[] params, int x, int y) {
      if (params.length < 5) {
                       // Problem, what do we do???
                       //MMScriptException e;
                       //e.message = "Params for Gaussian function has too few values"; //throw (e);
      }

      double exponent = (sqr(x - params[XC])  + sqr(y - params[YC])) / (2 * sqr(params[S]));
      double res = params[INT] * Math.exp(-exponent) + params[BGR];
      return res;
   }

   /**
    * Derivative (Jacobian) of the above function
    *
    * @param params - Parameters to be optimized
    * @param x - x position in the image
    * @param y - y position in the image
    * @return - array with the derivates for each of the parameters
    */
   public static double[] gaussianJ(double[] params, int x, int y) {
      double q = gaussian(params, x, y) - params[BGR];
      double dx = x - params[XC];
      double dy = y - params[YC];
      double[] result = {
         q/params[INT],
         1.0,
         dx * q/sqr(params[S]),
         dy * q/sqr(params[S]),
         (sqr(dx) + sqr(dy)) * q/cube(params[S])
      };
      return result;
   }


   /**
    * Gaussian function of the form:
    * f = A * e^(-((x-xc)^2/sigma_x^2 + (y-yc)^2/sigma_y^2)/2) + b
    * A = params[INT]  (total intensity)
    * b = params[BGR]  (background)
    * xc = params[XC]
    * yc = params[YC]
    * sig_x = params[S1]
    * sig_y = params[S2]
    */
   public static double gaussian2DXY(double[] params, int x, int y) {
      if (params.length < 6) {
                       // Problem, what do we do???
                       //MMScriptException e;
                       //e.message = "Params for Gaussian function has too few values"; //throw (e);
      }

      double exponent = ( (sqr(x - params[XC]))/(2*sqr(params[S1])))  +
              (sqr(y - params[YC]) / (2 * sqr(params[S2])));
      double res = params[INT] * Math.exp(-exponent) + params[BGR];
      return res;
   }

    /**
    * Derivative (Jacobian) of the above function
    *
     *
     * p = A,b,xc,yc,sigma_x,sigma_y
         f = A * e^(-((x-xc)^2/sigma_x^2 + (y-yc)^2/sigma_y^2)/2) + b
         J = {
          q/A,
          1,
          dx*q/sigma_x^2,
          dy*q/sigma_y^2,
          dx^2*q/sigma_x^3,
          dy^2*q/sigma_y^3
         }
    * @param params - Parameters to be optimized
    * @param x - x position in the image
    * @param y - y position in the image
    * @return - array with the derivates for each of the parameters
    */
   public static double[] gaussianJ2DXY(double[] params, int x, int y) {
      double q = gaussian2DXY(params, x, y) - params[BGR];
      double dx = x - params[XC];
      double dy = y - params[YC];
      double[] result = {
         q/params[INT],
         1.0,
         dx * q/sqr(params[S1]),
         dy * q/sqr(params[S2]),
         sqr(dx) * q /cube(params[S1]),
         sqr(dy) * q /cube(params[S2])
      };
      return result;
   }

   /*
    * watch out: params has here only 3 entries
    */
   public static double gaussian1D(double[] params, int x){
	   double exponent = sqr(x-params[XC])/(2*sqr(params[S-1]));
	   return params[INT]*Math.exp(-exponent)+params[BGR] ;
   }
   /**
    * Derivative (Jacobian) of the above function
    *
    * @param params - Parameters to be optimized
    * @param x - x position in the image
    * @return - array with the derivates for each of the parameters
    */
   public static double[] gaussian1DJ(double[] params, int x) {
      double q = gaussian1D(params, x) - params[BGR];
      double dx = x - params[XC];
      double[] result = {
         q/params[INT],
         1.0,
         dx * q/sqr(params[S-1]),
         (sqr(dx)) * q/cube(params[S-1])
      };
      return result;
   }

   public static double gaussian3DXYZ(double[] params, int x, int y, int z){
	   double exponent = sqr(x-params[XC])/(2*sqr(params[S+1])) + sqr(y - params[YC])/(2*sqr(params[S+2]))
			   			+sqr(z-params[ZC])/(2*sqr(params[S+3]));
	   return params[INT]*Math.exp(-exponent) + params[BGR];
   }
   /**
    * Derivative (Jacobian) of the above function
    *
    * @param params - Parameters to be optimized
    * @param x - x position in the image
    * @param y - y position in the image
    * @return - array with the derivates for each of the parameters
    */
   public static double[] gaussianJ3D(double[] params, int x, int y, int z) {
      double q = gaussian3DXYZ(params, x, y,z) - params[BGR];
      double dx = x - params[XC];
      double dy = y - params[YC];
      double dz = z - params[ZC];
      double[] result = {
         q/params[INT],
         0,
         dx * q/sqr(params[S+1]),
         dy * q/sqr(params[S+2]),
         dz* q/sqr(params[S+3]),
         (sqr(dx)) * q/cube(params[S+1]),
         (sqr(dy)) * q/cube(params[S+2]),
        (sqr(dz)) * q/cube(params[S+3])
      };
      return result;
   }
   /*
    * Gaussian with sigmax=sigmaY
    */
   public static double gaussian3DXZ(double[] params, int x, int y, int z){
	   double exponent = sqr(x-params[XC])/(2*sqr(params[S+1])) + sqr(y - params[YC])/(2*sqr(params[S+1]))
	   			+sqr(z-params[ZC])/(2*sqr(params[S+3]));
	   return params[INT]*Math.exp(-exponent) + params[BGR];
   }
   public static double[] gaussianJ3DXZ(double[] params, int x, int y, int z) {
	      double q = gaussian3DXZ(params, x, y,z) - params[BGR];
	      double dx = x - params[XC];
	      double dy = y - params[YC];
	      double dz = z - params[ZC];
	      double[] result = {
	         q/params[INT],
	         1.0,
	         dx * q/sqr(params[S+1]),
	         dy * q/sqr(params[S+1]),
	         dz* q/sqr(params[S+3]),
	         (sqr(dx)+sqr(dy)) * q/cube(params[S+1]),
	         0.0,
	         (sqr(dz)) * q/cube(params[S+3])
	      };
	      return result;
	   }
   /*
    * Integrated Gaussian only works for pixels of size 1 (only imagespace).
    */
   public static double gaussianIntegrated2D(double[] params, int x, int y){

	   double val1=0;
	   double val2=0;
	try {
		val1 = (Erf.erf(((x-params[XC]+1))/(Math.sqrt(2)*params[S]))-Erf.erf((x-params[XC])/(Math.sqrt(2)*params[S])));
	
	    val2 = (Erf.erf(((y-params[YC]+1))/(Math.sqrt(2)*params[S]))-Erf.erf((y-params[YC])/(Math.sqrt(2)*params[S])));
	} catch (MathException e) {
		LogTab.writeLog(e.getLocalizedMessage());
	}
	   return params[INT]*0.25*val1*val2 +params[BGR];
   }
   /*
    * Structure of params
    * INT
    * BGR
    * XC
    * YC
    * S
    */
   public static double[] gaussianIntegratedJ2D(double[] params, int x, int y){
	   double q = gaussianIntegrated2D(params, x,y)  ;
	   double[] subparams =  new double[]{params[INT], params[BGR], params[XC], 
			   							  params[S]} ;
	   double difX = gaussian1D(subparams, x+1)-gaussian1D(params,x);
	   double tx =  gaussian1D(subparams, x+1);

	   subparams[2] = params[YC] ; // be careful with all these parameter arrays
	   double difY = gaussian1D(subparams, y+1) - gaussian1D(subparams, y) ;
	   double ty = gaussian1D(subparams, y+1) ;
	   double val1=0;
	   double val2=0;
	   try {
	    val1 = (Erf.erf(((x-params[XC]+1))/(Math.sqrt(2)*params[S]))-Erf.erf((x-params[XC])/(Math.sqrt(2)*params[S])));
	    val2 = (Erf.erf(((y-params[YC]+1))/(Math.sqrt(2)*params[S]))-Erf.erf((y-params[YC])/(Math.sqrt(2)*params[S])));
		} catch (MathException e) {
			LogTab.writeLog(e.getLocalizedMessage());
		}
	   double dx = x-params[XC];
	   double dy = y - params[YC] ;
	   
	   double[] result = {
			   // derivative WRT I
			   (q-params[BGR])/params[INT],
			   // derivative WRT B
			   q-params[BGR],
			   // derivative WRT x
			   params[INT]*0.5*difX*val2,
			   // derivative WRT y
			   params[INT]*0.5*difY*val1,
			   // derivative WRT mux
			   -params[INT]*0.5*difX*val2,
			   //derivative WRT muy
			   -params[INT]*0.5*difY*val1,
			   //derivative WRT s
			   params[INT]*0.5*( ((-dx/params[S])*difX+ 1/params[S]*tx)*val2 + val1*((-dy/params[S])*difY+ 1/params[S]*ty) )
	   			} ;
	   return result ;
   	}
}
