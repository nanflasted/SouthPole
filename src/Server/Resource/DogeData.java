package Server.Resource;

public class DogeData implements Purchasable{

	private double food;
	private double power;
	private double failureProb;
	private double price;
	private double weight;
	
	public DogeData()
	{
		
	}
	
	
	public double getFood()
	{
		return food;
	}
	
	public double getPower()
	{
		return power;
	}
	
	public double getFailure()
	{
		return failureProb;
	}
	
	public double getPrice()
	{
		return price;
	}
}
