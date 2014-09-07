package kk.stream.event;

public class OrderEvent {
	private long timestamp;
	private String itemName;
    private double price;

    public OrderEvent(long timestamp, String itemName, double price) {
    	this.timestamp = timestamp;
        this.itemName = itemName;
        this.price = price;
    }

    public long getTimestamp() {
		return timestamp;
	}

    public String getItemName() {
        return itemName;
    }

    public double getPrice() {
        return price;
    }
}