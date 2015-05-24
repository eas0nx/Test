package kk.stream.engine;

import java.util.Date;
import java.util.Random;

import org.springframework.stereotype.Service;

import kk.stream.event.OrderEvent;
import kk.stream.listener.OrderListener;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.UpdateListener;

@Service
public class StreamEngine {

	private EPServiceProvider epService;

	public StreamEngine() {
		epService = EPServiceProviderManager.getDefaultProvider();
	}
	
	public void init() {
		String expression = "select current_timestamp as timestamp, avg(price) from kk.stream.event.OrderEvent.win:time_batch(10 sec, 5000L)";
		subscribe(expression, new OrderListener());
	}
	
	public void subscribe(String expression, UpdateListener listener) {
		EPStatement statement = epService.getEPAdministrator().createEPL(expression);
		statement.addListener(listener);
	}
	
	public void sendEvent(Object e) {
		epService.getEPRuntime().sendEvent(e);
	}
	
	public void sendEvents(int n) {
		@SuppressWarnings("deprecation")
		long time = new Date(2014, 9, 1).getTime();
		
		Random rd = new Random();
		// Send events
		for(int i = 0; i < n; i++)
		{
			OrderEvent e = new OrderEvent(time, "AirTicket", 500 + rd.nextInt(1000));
			sendEvent(e);
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			
			time += 100; // Simulate time passing for ext_timed_batch
		}
	}
}
