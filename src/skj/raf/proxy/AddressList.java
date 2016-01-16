package skj.raf.proxy;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class AddressList {
	
	public enum ListStatus {
		ALLOW(),
		DENY(),
		DOESNT_HAVE();
	}
	
	private ArrayList<ClientList> _allow;
	private ArrayList<ClientList> _deny;
	private ArrayList<String> _addresses; // Host addresses
	public final String name;
	
	public AddressList(String name) {
		this.name = name;
		_allow = new ArrayList<>();
		_allow.add(new ClientList(name + ".allow"));
		
		_deny = new ArrayList<>();
		_deny.add(new ClientList(name + ".deny"));
		
		_addresses = new ArrayList<>();
	}
	
	public void denyClient(String client) {
		if(_allow.get(0).match(client)) _allow.get(0).remove(client);
		_deny.get(0).add(client);
	}
	
	public void denyList(ClientList list) {
		if(_allow.contains(list)) _allow.remove(list);
		_deny.add(list);
	}
	
	public void allowClient(String client) {
		if(_deny.get(0).match(client)) _deny.get(0).remove(client);
		_allow.get(0).add(client);
	}
	
	public void allowList(ClientList list) {
		if(_deny.contains(list)) _deny.remove(list);
		_allow.add(list);
	}
	
	public void addAddress(String addr) {
		_addresses.add(addr);
	}
	
	public void removeAddress(String addr) {
		_addresses.removeIf(e -> e.equals(addr));
	}
	
	public void removeClientList(ClientList client) {
		_allow.remove(client);
		_deny.remove(client);
	}

	public void removeClient(String client) {
		_allow.get(0).remove(client);
		_deny.get(0).remove(client);
	}
	
	public ListStatus canConnectTo(String client, String address) {
		boolean contains = _addresses.stream().anyMatch(e -> e.contains(address));
		
		if(contains) {
			if(_allow.stream().anyMatch(e -> e.contains(client))) return ListStatus.ALLOW;
			else if(_deny.stream().anyMatch(e -> e.contains(client))) return ListStatus.DENY;
		}
		
		return ListStatus.DOESNT_HAVE;
	}
	
	public void saveList(FileWriter fr) throws IOException {
		for(String e : _addresses) {
			fr.write("ADD ADDRESS " + e + " LIST_ADDRESS " + name + System.lineSeparator());
		}
		
		_allow.get(0).saveClientsForAddressList(fr, true, name);
		
		_deny.get(0).saveClientsForAddressList(fr, false, name);
		
		for(int i = 1; i < _allow.size(); i++) {
			fr.write("ADD LIST_CLIENT " + _allow.get(i).name + " LIST_ADDRESS " + name + " 1" + System.lineSeparator());
		}
		
		for(int i = 1; i < _deny.size(); i++) {
			fr.write("ADD LIST_CLIENT " + _deny.get(i).name + " LIST_ADDRESS " + name + " 1" + System.lineSeparator());
		}
	}
	
	@Override
	public String toString() {
		String result = "";
		result += "ADDRESS LIST " + name + "\r\n";
		for(String c : _addresses) {
			result += "ADDRESS " + c + "\r\n";
		}
		for(int i=0; i < _allow.size(); i++) {
			if(i == 0 && _allow.get(0).size() > 0) {
				result += "LOCAL ALLOW LIST\r\n";
				result += _allow.get(0).getClients();
			} else {
				result += "ALLOW CLIENT LIST " + _allow.get(i).name + "\r\n";
			}
		}
		for(int i=0; i < _deny.size(); i++) {
			if(i == 0 && _deny.get(0).size() > 0) {
				result += "LOCAL DENY LIST\r\n";
				result += _deny.get(0).getClients();
			} else {
				result += "DENY CLIENT LIST " + _deny.get(i).name + "\r\n";
			}
		}
		return result;
	}
}
