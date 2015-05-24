package kk.aggregator.lowprice;

import java.util.HashMap;
import java.util.Map;

public class LowPriceBuffer extends HashMap<String, LowPriceEvent> {

	public LowPriceBuffer(int initialCapacity) {
		super(initialCapacity); // Note that load factor is not considered
	}

	/**
	 * Merge those keys present in both current and previous buffers.
	 * @param prevBuffer
	 * @deprecated
	 */
	public void merge(LowPriceBuffer prevBuffer) {
		if (prevBuffer == null) {
			return;
		}
		
		for (Map.Entry<String, LowPriceEvent> cur : entrySet()) {
			LowPriceEvent other = prevBuffer.get(cur.getKey());
			if (other != null) {
				if (other.getSalesPrice() < cur.getValue().getSalesPrice()) {
					// TODO Copy other info like flight if present
					cur.getValue().setSalesPrice(other.getSalesPrice());
				}
			}
		}
	}
}
