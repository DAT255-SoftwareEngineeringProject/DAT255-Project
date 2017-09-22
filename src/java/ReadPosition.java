package plugins;


import com.sun.squawk.VM;
import sics.plugin.PlugInComponent;
import sics.port.PluginRPort;

public class ReadDistance extends PlugInComponent {
	public PluginRPort sensor;
	
	public ReadDistance(String args[]) {
		super(args);
	}
	public ReadDistance(){}
	
	public static void main(String args[]) {
		VM.println("ReadDistance.main()\r\n");
		ReadDistance ap = new ReadDistance(args);
		ap.run();
	}
	
	 public void init() {
			// Initiate PluginPPort
			VM.println("init sensor");
			sensor = new PluginRPort(this, "se");
		    }
	 
	 public void doFunction() throws InterruptedException {
		 String distance = new String();
		 
		while(true){
			distance = sensor.readLine();
			VM.println(distance);
		}
		
		public void run() {
			init();
			try {
			    doFunction();
			} catch (InterruptedException e) {
			    VM.println("**************** Interrupted.");
			    return;
			}
		}
	 }
}