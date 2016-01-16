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
	
	public boolean match(String pattern) {
		return _clients.stream().anyMatch(e -> e.contains(pattern));
	}
	
	public void remove(String pattern) {
		_clients.removeIf(e -> e.equals(pattern));
	}
	
	public boolean contains(String pattern) {
		return _clients.stream().anyMatch(e -> e.startsWith(pattern));
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
