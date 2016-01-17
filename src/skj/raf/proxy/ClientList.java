package skj.raf.proxy;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class ClientList {
	
	private ArrayList<String> _clients;
	public final String name;
	
	public ClientList(String name) {
		this.name = name;
		_clients = new ArrayList<>();
	}
	
	public void add(String pattern) {
		_clients.add(pattern);
	}
	
	public void addCollection(Collection<String> col) {
		col.forEach(e -> _clients.add(e));
	}
	
	private boolean matchPattern(String pattern, String client) {
		if(pattern.contains("/")) {
			String[] cidr = pattern.split("/");
			int c = Integer.parseInt(cidr[1]);
			int whichOctet = c / 8;
			int meaningBits = c % 8;
			String[] ip = cidr[0].split("\\.");
			if(whichOctet < 4) {
				int octet = Integer.parseInt(ip[whichOctet]);
				int min = 0;
				
				int power = 128;
				for(int z = 1; z < meaningBits; z++) {
					if((min + power/2) < octet) min += power/2;
					power /= 2;
				}
				
				power = 1;
				int max = min;
				for(int z = 7; z >= meaningBits; z--) {
					if(z == 7) max += 1;
					else {
						max += power * 2;
						power *= 2;
					}
				}
				
				String[] clientArr = client.split("\\.");
				
				for(int g = 0; g < whichOctet; g++){
					if(!ip[g].equals(clientArr[g])) return false;
				}
				
				int clientMeaning = Integer.parseInt(clientArr[meaningBits]);
				if(clientMeaning < min || clientMeaning > max) return false;
				
				return true;
			} else {
				return false;
			}
		}
		return pattern.equals(client);
	}
	
	public boolean match(String client) {
		return _clients.stream().anyMatch(e -> matchPattern(e, client));
	}
	
	public void remove(String pattern) {
		_clients.removeIf(e -> e.equals(pattern));
	}
	
	public boolean contains(String pattern) {
		return _clients.stream().anyMatch(e -> e.equals(pattern));
	}
	
	public void displayToConsole() {
		System.out.println(name + " list has " + _clients.size() + " clients");
		_clients.forEach(e -> System.out.println(name + " - " + e));
	}
	
	public void saveClients(FileWriter fr) throws IOException {
		for(String c : _clients) fr.write("ADD CLIENT " + c + " LIST_CLIENT "+ name + System.lineSeparator());
	}
	
	public void saveClientsForAddressList(FileWriter fr, boolean allow, String n) throws IOException {
		for(String c : _clients) fr.write("ADD CLIENT " + c + " LIST_ADDRESS "+ n + (allow ? " 1" : " 0") + System.lineSeparator());
	}
	
	public String getClients() {
		String result = "";
		for(String c : _clients) {
			result += "CLIENT " + c + "\r\n";
		}
		return result;
	}
	
	public int size() {
		return _clients.size();
	}
	
	@Override
	public String toString() {
		String result = "";
		result += "CLIENT LIST " + name + "\r\n";
		for(String c : _clients) {
			result += "CLIENT " + c + "\r\n";
		}
		return result;
	}
}
