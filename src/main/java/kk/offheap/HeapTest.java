package kk.offheap;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class HeapTest {

	public static class Result {
		public Result(int index, int time) {
			this.count = index;
			this.time = time;
		}
		private int count;
		private int time;
		
		public int getCount() {
			return count;
		}
		public void setCount(int index) {
			this.count = index;
		}
		public int getTime() {
			return time;
		}
		public void setTime(int time) {
			this.time = time;
		}
	}
	
	public Result allocate(String type, int n) throws IOException {
		ArrayList<ByteBuffer> bufs = new ArrayList<ByteBuffer>(n);
		
		long start = System.currentTimeMillis();
		
		if (type.equalsIgnoreCase("onheap"))
		{
			for (int i = 0; i < n; i++) // 1200*1M to produce OOM with a 1G heap
			{
				ByteBuffer bb = ByteBuffer.allocate(1024*1024);
				bufs.add(bb);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		else if (type.equalsIgnoreCase("offheap"))
		{
			for (int i = 0; i < n; i++)
			{
				ByteBuffer bb = ByteBuffer.allocateDirect(1024*1024);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				bufs.add(bb);
			}
		}
		
		long end = System.currentTimeMillis();
		System.out.printf("Cost %d ms\n", end-start);
		
		for(ByteBuffer bb : bufs)
		{
			destroyDirectBuffer(bb);
		}
		bufs.clear();
		//System.gc();
		return new Result(n, (int) (end-start));
	}

	public void destroyDirectBuffer(Buffer buffer) {
	    if(buffer.isDirect()) {
	        try {
	            if(!buffer.getClass().getName().equals("java.nio.DirectByteBuffer")) {
	                Field attField = buffer.getClass().getDeclaredField("att");
	                attField.setAccessible(true);
	                buffer = (Buffer) attField.get(buffer);
	            }

	            // TODO Reuse cleanerMethod and cleanMethod
	            Method cleanerMethod = buffer.getClass().getMethod("cleaner");
	            cleanerMethod.setAccessible(true);
	            Object cleaner = cleanerMethod.invoke(buffer);
	            Method cleanMethod = cleaner.getClass().getMethod("clean");
	            cleanMethod.setAccessible(true);
	            cleanMethod.invoke(cleaner);
	        } catch(Exception e) {
	            throw new RuntimeException("Could not destroy direct buffer " + buffer, e);
	        }
	    }
	}
}
