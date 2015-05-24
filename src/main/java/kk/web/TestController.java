package kk.web;

import java.io.IOException;

import kk.aggregator.lowprice.LowPriceEvent;
import kk.aggregator.lowprice.LowPriceEventLoop;
import kk.offheap.HeapTest;
import kk.offheap.HeapTest.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {
	
	private HeapTest heapTest;
	private LowPriceEventLoop lowPriceService;
	
	public TestController()
	{
		
	}
	
	@RequestMapping(value="/hello", method = RequestMethod.GET)
	public String hello() {
		return "Hello World!";
	}

	@RequestMapping(value="/lowprice/{key}", method = RequestMethod.GET)
	public String lowprice(@PathVariable String key, @RequestParam int price) {
		LowPriceEvent e = new LowPriceEvent();
		e.setKey(key);
		e.setSalesPrice(price);
		
		return lowPriceService.add(e) ? "OK" : "Error";
	}
	
	@RequestMapping(value="/allocate/{type}", method = RequestMethod.GET)
	public Object allocate(@PathVariable("type") String type, @RequestParam("count") int n) {
		try {
			return heapTest.allocate(type, n);
		} catch (Throwable e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	@Autowired
	public void setHeapTest(HeapTest heapTest) {
		this.heapTest = heapTest;
	}

	@Autowired
	public void setLowPriceService(LowPriceEventLoop lowPriceService) {
		this.lowPriceService = lowPriceService;
	}
}
