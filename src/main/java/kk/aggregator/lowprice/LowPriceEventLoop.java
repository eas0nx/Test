package kk.aggregator.lowprice;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LowPriceEventLoop {

	private ArrayBlockingQueue<LowPriceEvent> eventQueue;
	private int queueCapacity;
	private int bufferCapacity;
	private int bufferPurgeInterval;
	
	private Thread processorThread;
	
	private long lastPurgeBufferTime;
	private Map<String, LowPriceEvent> currBuffer;
	private Map<String, LowPriceEvent> prevBuffer;

	private LowPriceCacheUpdater updater;
	
	@Autowired
	public LowPriceEventLoop(LowPriceCacheUpdater updater) {
		this.queueCapacity = 10_000;
		this.bufferCapacity = 10_000;
		this.bufferPurgeInterval = 60;
		
		this.updater = updater;
	}
	
	public int getQueueCapacity() {
		return queueCapacity;
	}

	@Value("${queue.capacity}")
	public void setQueueCapacity(int eventQueueCapacity) {
		this.queueCapacity = eventQueueCapacity;
	}

	public int getBufferCapacity() {
		return bufferCapacity;
	}

	@Value("${buffer.capacity}")
	public void setBufferCapacity(int bufferCapacity) {
		this.bufferCapacity = bufferCapacity;
	}

	public int getBufferPurgeInterval() {
		return bufferPurgeInterval;
	}

	@Value("${buffer.purgeInterval}")
	public void setBufferPurgeInterval(int bufferPurgeInterval) {
		this.bufferPurgeInterval = bufferPurgeInterval;
	}

	@PostConstruct
	public void init() {
		this.eventQueue = new ArrayBlockingQueue<LowPriceEvent>(queueCapacity);
		this.currBuffer = new HashMap<String, LowPriceEvent>(bufferCapacity);
		this.prevBuffer = Collections.emptyMap();
		
		lastPurgeBufferTime = System.currentTimeMillis();
		processorThread = new Thread(new EventProcessor(), "LowPriceEventProcessor");
		processorThread.start();
	}
	
	/**
	 * Try to insert the event to queue.
	 * @param e the event
	 * @return true if event queue is not full
	 */
	public boolean add(LowPriceEvent e) {
		return eventQueue.offer(e);
	}
	
	private class EventProcessor implements Runnable {

		@Override
		public void run() {
			int intervalMs = bufferPurgeInterval*1000;
			while (true)
			{
				try {
					long nextPurgeBufferTime = lastPurgeBufferTime + intervalMs;
					
					// Check whether it is time for purging buffer
					long now = System.currentTimeMillis();
					if (now >= nextPurgeBufferTime || isFull(currBuffer))
					{
						lastPurgeBufferTime = now;
						nextPurgeBufferTime = lastPurgeBufferTime + intervalMs;
						
						purgeBuffer();
					}
					
					long timeout = nextPurgeBufferTime - System.currentTimeMillis();
					// No wait if timeout <= 0
					LowPriceEvent e = eventQueue.poll(timeout, TimeUnit.MILLISECONDS);
					
					if (e != null) {
						process(e);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		private boolean isFull(Map<String, LowPriceEvent> buffer) {
			return buffer.size() >= bufferCapacity;
		}
		
		private void process(LowPriceEvent e) {
			LowPriceEvent old = currBuffer.get(e.getKey());
			// Put new price if old one does NOT exist or new price is cheaper
			if (old == null || e.getSalesPrice() < old.getSalesPrice())
			{
				currBuffer.put(e.getKey(), e);
			}
		}
		
		private void purgeBuffer() {
			// Send immutable buffer to cache updater
			updater.updateAsync(Collections.unmodifiableMap(prevBuffer), Collections.unmodifiableMap(currBuffer));
			
			// From now on, prevBuffer should NOT be modified
			prevBuffer = currBuffer;
			currBuffer = new HashMap<String, LowPriceEvent>(bufferCapacity);
		}
	}
}
