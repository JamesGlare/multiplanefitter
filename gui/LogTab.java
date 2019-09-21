package gui;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

public class LogTab extends JComponent {

	/*
	 * Private members
	 */
	private JFrame frame ;
	private static JTextArea logArea ;
	private static JScrollPane scrollPane ;
	private JPanel panel ;
	private Border border ;
	private final static String newLine = "\n";
	private static SimpleDateFormat sdf ;
	private static final int maxRowCount = 500 ;
		/*
		 * Constructor
		 */
	public LogTab(JFrame frame_){
		frame = frame_ ;
		logArea = new JTextArea(20, 15);
		
		scrollPane = new JScrollPane(logArea); 
		sdf = new SimpleDateFormat("HH:mm"); // hours : minutes
		
		logArea.setEditable(false);
		panel = new JPanel() ;
		border = BorderFactory.createTitledBorder("MultiPlaneFitter Log") ;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)) ;
		this.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)) ;
		panel.add(scrollPane);
		panel.setBorder(border) ;
		
	}
	public static void writeLog(String toLog){
		if( logArea.getRows() > maxRowCount){
			clear();
			return ;
		}
		Calendar cal = Calendar.getInstance();
		String time = sdf.format(cal.getTime());
		logArea.append(time+" "+toLog+ newLine) ;
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}
	public static void clear(){
		logArea.setText("");
	}
}
