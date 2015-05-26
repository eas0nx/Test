package kk.aggregator.lowprice;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Service
public class LowPriceCacheUpdater {

	private ExecutorService threadPool = Executors.newSingleThreadExecutor(
			new ThreadFactoryBuilder().setDaemon(false).setNameFormat("LowPriceCacheUpdater-%d").build());
	
	public void updateAsync(final Map<String, LowPriceEvent> prevBuffer, final Map<String, LowPriceEvent> currBuffer)
	{
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				doUpdate(prevBuffer, currBuffer);
			}
		});
	}
	
	private void doUpdate(Map<String, LowPriceEvent> prevBuffer, Map<String, LowPriceEvent> currBuffer) {
		System.out.printf("%s - buffer size: %d", new Date(), currBuffer.size());
		System.out.println();
		
		for (Map.Entry<String, LowPriceEvent> cur : currBuffer.entrySet()) {
			LowPriceEvent lpe = cur.getValue();
			
			LowPriceEvent other = prevBuffer.get(cur.getKey());
			if (other != null && other.getSalesPrice() < cur.getValue().getSalesPrice()) {
				lpe = other;
			}
			
			// TODO Update lpe to Redis cache
		}
	}
}
