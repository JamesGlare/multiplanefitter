package plugins;


import gui.MainWindow;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;

/**************************************
 * MultiPlaneFitter
 * @author james
 * A program to deal with data obtained
 * from a Gustaffson-Dahan multifocal
 * microscope.
 **************************************/
public class MultiPlaneFitter_ implements PlugIn {

	/*
	 * Private Members
	 */
	private MainWindow gui ;
	/*
	 * (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg0) {
		/*
		 * Step 1: Create the gui.
		 */
		this.gui = new MainWindow() ;
	}

}
