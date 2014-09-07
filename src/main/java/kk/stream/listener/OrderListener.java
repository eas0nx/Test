package kk.stream.listener;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class OrderListener implements UpdateListener {
	
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        EventBean event = newEvents[0];
        System.out.println("newEvents=" + newEvents.length);
        System.out.println("timestamp=" + event.get("timestamp") + ";avg=" + event.get("avg(price)"));
    }
}
