import java.text.DecimalFormat;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import org.json.JSONArray;
import org.json.JSONObject;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Main extends ListenerAdapter{
	
	private static TreeMap<String, JSONObject> prices = new TreeMap<String, JSONObject>();
	
	private static TreeMap<String, Player> players = new TreeMap<String, Player>();


	public static void main(String[] args) throws LoginException {	
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		String token = "Njc3MjM2NzYyMDgyNDEwNTM3.XkRUhQ.Q5YDojLnVNzh6Z7shWxsj4Venik";
		builder.setToken(token);
		builder.addEventListeners(new Main());
		builder.build();
    	
    	
		PriceBot getter = new PriceBot();
		Runnable refresh = new Runnable() {
		    public void run() {
		    	prices.clear();
		    	JSONArray data = getter.getLatest();
		    	for(int i = 0; i < data.length(); i++) {
		    		prices.put(data.getJSONObject(i).getString("symbol"), data.getJSONObject(i));
		    	}
		    	System.out.println("Prices Updated.");
		    }
		};
		
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
		exec.scheduleAtFixedRate(refresh , 0, 10, TimeUnit.MINUTES);
	}
	
	public static double getPrice(String symbol) {
		return prices.get(symbol).getJSONObject("quote").getJSONObject("USD").getDouble("price");
	}
	
	public static double get24Hour(String symbol) {
		return prices.get(symbol).getJSONObject("quote").getJSONObject("USD").getDouble("percent_change_24h");
	}
		
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getAuthor().getName().equals("Crypto Game"))
			return;
		
		if(players.containsKey(event.getAuthor().getId()) == false) {
			players.put(event.getAuthor().getId(), new Player());
		}
				
		Player currentPlayer = players.get(event.getAuthor().getId());
		//TODO change source
		
		EmbedBuilder eb = new EmbedBuilder();
		String[] command = event.getMessage().getContentRaw().split(" ");
		if(command[0].equals("!help")) {
			eb.setTitle("Help");
			eb.setDescription("Welcome to CryptoMarketGame. May the odds be in your favor.\n[Source](https://github.com/ethanent/discord-marketgame)");
			eb.setFooter("(c) 2020 Julian Carrier");
			eb.addField("Information Commands", "\n!help\n!price <symbol>\n!bal\n!shares", true);
			eb.addField("Activity Commands", "\n!buy <symbol> <count>\n!sell <symbol> <count>\n!reset", true);
			eb.setColor(0x3E606F);
			event.getChannel().sendMessage(eb.build()).queue();
		}
		
		if(command[0].equals("!price")) {
			
			if(command.length < 2) {
				eb.setTitle("ERROR");
				eb.setDescription("Missing arguments.\nUsage: !price <symbol>");
				eb.setColor(0xFF0000);
			}else if(prices.containsKey(command[1]) == false) {
				eb.setTitle("ERROR");
				eb.setDescription("Unknown Symbol!");
				eb.setColor(0xFF0000);
			}else {
				String symbol = command[1];
				eb.setTitle(symbol + "/USD");
				eb.addField("Price per coin", "$" + getPrice(symbol), false);
				eb.addField("24 hour change",  "" + get24Hour(symbol) + "% from yesterday", false);
				eb.addField(event.getAuthor().getName() + "'s equity", "$" + currentPlayer.getEquity(symbol) * getPrice(symbol) + 
						" (" + currentPlayer.getEquity(symbol) + " " + symbol + ")", false);
				eb.setColor(0x3E606F);
			}
			
			event.getChannel().sendMessage(eb.build()).queue();
		}
		
		if(command[0].equals("!bal")) {
			eb.setTitle(event.getAuthor().getName() + "'s Account");
			double worth = Math.round(currentPlayer.getNetWorth() * 100) / 100;
			double cash = Math.round(currentPlayer.getCash() * 100) / 100;
			eb.addField("Net Worth", "$" + worth, false);
			eb.addField("Cash", "$" + cash,true);
			eb.addField("Equity", "$" + (worth - cash),true);
			eb.setColor(0x3E606F);

			event.getChannel().sendMessage(eb.build()).queue();
		}
		
		if(command[0].equals("!shares")) {
			eb.setTitle(event.getAuthor().getName() + "'s Positions");
			TreeMap<String, Double> temp = currentPlayer.getCapital();
			double worth = currentPlayer.getNetWorth();
			eb.addField("Cash", "$" + formatDouble(currentPlayer.getCash()) + " (" + 
					getPortion(worth, currentPlayer.getCash()) + "% of portfolio)", false);
			
			for(String i : temp.keySet()) {
				eb.addField(temp.get(i) + " x " + i + " (" + formatDouble(get24Hour(i)) + "%)",
						"$" + formatDouble(getPrice(i) * temp.get(i)) + " (" + getPortion(worth, temp.get(i) * getPrice(i)) + "% of portfolio)", false);
			}
			
			event.getChannel().sendMessage(eb.build()).queue();
		}
		
		if(command[0].equals("!buy")) {
			if(command.length < 3) {
				eb.setTitle("ERROR");
				eb.setDescription("Missing arguments.\nUsage: !buy <symbol> <count>");
				eb.setColor(0xFF0000);
			}else if(prices.containsKey(command[1]) == false) {
				eb.setTitle("ERROR");
				eb.setDescription("Unknown Symbol!");
				eb.setColor(0xFF0000);
			}else {
				double amount = Double.parseDouble(command[2]);
				String symbol = command[1];
				boolean sucsess = currentPlayer.buy(symbol, getPrice(symbol), amount);
				
				if(sucsess) {
					eb.setTitle(":tada: " + amount + " x " + symbol + " Purchase Complete");
					eb.addField("Buy Price", "$" + formatDouble(getPrice(symbol)), false);
					eb.addField("Total Cost", "$" + formatDouble(getPrice(symbol) * amount), false);
					eb.setColor(0x46E8B2);
				}else {
					eb.setTitle("ERROR");
					eb.setDescription("You cannot afford to buy " + amount + " x " + symbol);
					eb.setColor(0xFF0000);
				}
			}
			event.getChannel().sendMessage(eb.build()).queue();
		}
		
		if(command[0].equals("!sell")) {
			if(command.length < 3) {
				eb.setTitle("ERROR");
				eb.setDescription("Missing arguments.\nUsage: !sell <symbol> <count>");
				eb.setColor(0xFF0000);
			}else if(prices.containsKey(command[1]) == false) {
				eb.setTitle("ERROR");
				eb.setDescription("Unknown Symbol!");
				eb.setColor(0xFF0000);
			}else {
				double amount = Double.parseDouble(command[2]);
				String symbol = command[1];
				boolean sucsess = currentPlayer.sell(symbol, getPrice(symbol), amount);
				
				if(sucsess) {
					eb.setTitle(":tada: " + amount + " x " + symbol + " Sale Complete");
					eb.addField("Sell Price", "$" + formatDouble(getPrice(symbol)), false);
					eb.addField("Total Recived", "$" + formatDouble(getPrice(symbol) * amount), false);
					eb.setColor(0x46E8B2);
				}else {
					eb.setTitle("ERROR");
					eb.setDescription("You do not own enough " + symbol + " to complete this sale. You currently own " +
							currentPlayer.getEquity(symbol) + " " + symbol + ".");
					eb.setColor(0xFF0000);
				}
			}
			event.getChannel().sendMessage(eb.build()).queue();
		}
		
		if(command[0].equals("!reset")) {
			currentPlayer.reset();
			eb.setTitle(":tada: Reset Complete");
			eb.setDescription("Your cash and equity have been destroyed.");
			eb.setColor(0x3E606F);
			event.getChannel().sendMessage(eb.build()).queue();
		}
	}
	
	private static String formatDouble(double num) {
		DecimalFormat formatter = new DecimalFormat("#0.00");
		return formatter.format(num);
	}
	
	private static String getPortion(double worth, double amount) {
		return formatDouble((amount / worth) * 100);
	}

}
