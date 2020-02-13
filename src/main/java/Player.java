import java.util.TreeMap;

public class Player {
	private double cash = 100000;
	private TreeMap<String, Double> capital = new TreeMap<String, Double>();
	
	
	public Player(){
	}
	
	public boolean buy(String Symbol, double price, double amount) {
		if(price * amount > cash) {
			return false;
		}
		
		if(capital.containsKey(Symbol) == true) {
			capital.put(Symbol, capital.get(Symbol) + amount);
			cash -= price * amount;
			return true;
		}else{
			capital.put(Symbol, amount);
			cash -= price * amount;
			return true;
		}
	}
	
	public boolean sell(String Symbol, double price, double amount) {
		if(capital.containsKey(Symbol) == true) {
			if(capital.get(Symbol) - amount < 0) {
				return false;
			}
	
			capital.put(Symbol, capital.get(Symbol) - amount);
			cash += amount * price;
			
			if(capital.get(Symbol) == 0) {
				capital.remove(Symbol);
			}
			return true;
		}else {
			return false;
		}
	}
	
	public void reset() {
		capital.clear();
		cash = 100000;
	}
	
	public double getNetWorth() {
		double worth = getCash();
		for(String i : capital.keySet()) {
			worth += Main.getPrice(i) * capital.get(i);
		}
		return worth;
	}
	
	public double getCash() {
		return cash;
	}
	
	public double getEquity(String symbol) {
		if(capital.containsKey(symbol) == true) {
			return capital.get(symbol);
		}else {
			return 0.00;
		}
	}
	
	public TreeMap<String, Double> getCapital(){
		return capital;
	}
}
