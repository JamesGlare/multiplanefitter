package lib;

import gui.LogTab;

import java.util.ArrayList;

import processing.Gaussian3DSpot;
import processing.Spot;

public class Comparer{
	private double[] minParams;
	private double[] maxParams;
	private String[] paramNames = {"Intensity", "Background", "x","y", "z", "sigma X", "sigma y", "sigma z"};
	
	
	public Comparer(double[] minParams, double[] maxParams ){
		this.maxParams = maxParams;
		this.minParams = minParams ;
	}
	public ArrayList<Spot> refineSpotList(ArrayList<Spot> list){
		
		ArrayList<Spot> newList = new ArrayList<Spot>() ;
		
		for(Spot spot: list){
			if(compareSpot((Gaussian3DSpot) spot))
				newList.add(spot);
		}
		return newList ;
	}
	public boolean compareSpot(Gaussian3DSpot gSpot){
		boolean accept = true;
		double[] params = gSpot.getParams();
		String notAcceptMessage = "";

		for(int i=0; i< 8; i++){
			boolean temp = ((params[i]>= minParams[i] )&& (params[i]<= maxParams[i] )) ;
			accept = accept && temp;
			
			if(!temp) // log why not accept
				notAcceptMessage += "Fail @" + paramNames[i] + "("+params[i]+")" ;
		}
		if(!accept)
			LogTab.writeLog("Spot not accepted: "+gSpot.getPositionString() +" "+notAcceptMessage);
		return accept ;
	}
}