package main.java;

import com.sun.squawk.VM;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class ReadDistance extends PlugInComponent {
	public PluginRPort sensor;
	public PluginPPort LED;
	
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
			VM.println("init led");
			LED = new PluginPPort(this, "led");
		    }
	 
	 public void doFunction() throws InterruptedException {
		 int distance;
		 
		while(true){
			distance = sensor.readInt();
			if(distance >= 100) {
				LED.write("3|0"); LED.write("2|1"); LED.write("1|1");  
			} else if (distance > 50) {
				LED.write("3|1"); LED.write("2|0"); LED.write("1|1"); 
			} else {
				LED.write("3|1"); LED.write("2|1"); LED.write("1|0"); 
			}
		}
		
		
	 }
	
@Override
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