package Server.Resource;

public class ItemData implements Purchasable {
	public double weight;
	public String name;
	private double price;
	//insert item definition here
	
	public double getPrice()
	{
		return price;
	}
	
	public double getWeight()
	{
		return weight;
	}
}
