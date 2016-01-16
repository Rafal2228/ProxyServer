package skj.raf.proxy;

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
	
	public boolean match(String pattern) {
		return _clients.stream().anyMatch(e -> e.contains(pattern));
	}
	
	public void remove(String pattern) {
		_clients.forEach(e -> {
			if(e.contains(pattern)) _clients.remove(e);
		});
	}
	
	public boolean contains(String pattern) {
		return _clients.stream().anyMatch(e -> e.startsWith(pattern));
	}
	
	public void displayToConsole() {
		System.out.println(name + " list has " + _clients.size() + " clients");
		_clients.forEach(e -> System.out.println(name + " - " + e));
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
