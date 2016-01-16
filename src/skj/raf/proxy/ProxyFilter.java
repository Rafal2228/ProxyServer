package skj.raf.proxy;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class ProxyFilter {
	
	public enum ProxyStatus {
		ALLOW(),
		DENY(),
		UNSUPPORTED();
	}
	
	public static boolean configured = false;
	
	private static HashMap<String, ClientList> _clients = new HashMap<>();
	private static HashMap<String, AddressList> _addresses = new HashMap<>();
	private static ProxyStatus _default = ProxyStatus.ALLOW;
	
	public static void setDefault(boolean allow) {
		if(allow) _default = ProxyStatus.ALLOW;
		else _default = ProxyStatus.DENY;
	}
	
	// CLIENTS LIST METHODS
	
	public static void createClientList(String name) throws IllegalArgumentException {
		if(_clients.containsKey(name)) throw new IllegalArgumentException("Specified name: " + name + " of ClientList is taken");
		_clients.put(name, new ClientList(name));
	}
	
	public static void addClientToClientList(String list, String client) throws IllegalArgumentException {
		ClientList tmp = _clients.get(list);
		if(tmp != null) {
			tmp.add(client);
		} else {
			throw new IllegalArgumentException("Client List " + list + " not found");
		}
	}
	
	public static String getClientsLists() {
		String result = "";
		
		for(ClientList e : _clients.values()) {
			result += "ADDRESS LIST " + e.name + "\r\n";
		}
		
		return result;
	}
	
	public static String getClientList(String name) throws IllegalArgumentException {
		ClientList tmp = _clients.get(name);
		if(tmp != null) {
			return tmp.toString();
		} else {
			throw new IllegalArgumentException("Client List " + name + " not found");
		}
	}
	
	public static String removeClientList(String name) throws IllegalArgumentException {
		ClientList client = _clients.get(name);
		if(client != null) {
			_addresses.values().forEach(e -> e.removeClientList(client));
			_clients.remove(name, client);
			return "Removed " + name + " successfully";
		} else throw new IllegalArgumentException("Client list " + name + " not found, removal failed"); 
	}
	
	public static String removeClientFromClientList(String list, String pattern) throws IllegalArgumentException {
		ClientList client = _clients.get(list);
		if(client != null) {
			client.remove(pattern);
			return "Removed " + pattern + " from "+ list + " successfully";
		} else throw new IllegalArgumentException("Client list " + list + " not found, removal failed"); 
	}
	
	// ADDRESSES LIST METHODS
	
	public static void createAddressList(String name) throws IllegalArgumentException {
		if(_addresses.containsKey(name)) throw new IllegalArgumentException("Specified name: " + name + " of AddressList is taken");
		_addresses.put(name, new AddressList(name));
	}
	
	public static void addAddressToAddressList(String list, String addr) throws IllegalArgumentException {
		if(_addresses.containsKey(list)) {
			_addresses.get(list).addAddress(addr);
		} else {
			throw new IllegalArgumentException("Address List " + list + " not found on insert: " + addr);
		}
	}
	
	public static void addClientToAddressList(String addressListName, String client, boolean allow) throws IllegalArgumentException {
		AddressList addressList = _addresses.get(addressListName);
		if(addressList != null) {
			if(allow) addressList.allowClient(client);
			else addressList.denyClient(client);
		} else {
			throw new IllegalArgumentException("Address List " + addressListName + " not found");
		}
	}
	
	public static void addClientListToAddressList(String addressListName, String clientListName, boolean allow) throws IllegalArgumentException {
		ClientList clientList = _clients.get(clientListName);
		if(clientList != null){
			AddressList addressList = _addresses.get(addressListName);
			if(addressList != null) {
				if(allow) addressList.allowList(clientList);
				else addressList.denyList(clientList);
			} else {
				throw new IllegalArgumentException("Address List " + addressListName + " not found");
			}
		} else {
			throw new IllegalArgumentException("Client List " + clientListName + " not found");
		}
	}
	
	public static String getAddressesLists() {
		String result = "";
		
		for(AddressList e : _addresses.values()) {
			result += "ADDRESS LIST " + e.name + "\r\n";
		}
		
		return result;
	}
	
	public static String getAddressList(String name) throws IllegalArgumentException {
		AddressList tmp = _addresses.get(name);
		if(tmp != null) {
			return tmp.toString();
		} else {
			throw new IllegalArgumentException("Address List " + name + " not found");
		}
	}
	
	public static String removeAddressList(String name) throws IllegalArgumentException {
		AddressList tmp = _addresses.get(name);
		if(tmp != null) {
			_addresses.remove(name, tmp);
			return "Removed " + name + " successfully";
		} else {
			throw new IllegalArgumentException("Address List " + name + " not found");
		}
	}
	
	public static String removeClientFromAddressList(String list, String client) throws IllegalArgumentException {
		AddressList tmp = _addresses.get(list);
		if(tmp != null) {
			tmp.removeClient(client);
			return "Removed " + client + " from " + list + " successfully";
		} else {
			throw new IllegalArgumentException("Address List " + list + " not found");
		}
	}
	
	// FILTER
	
	public static ProxyStatus filter(InetSocketAddress addr, String host) {
		if(host.endsWith(":443") || host.startsWith("https://")) return ProxyStatus.UNSUPPORTED;
		
		int rank = 0;
		
		if(host.startsWith("http://")) host = host.substring(6);
		if(host.endsWith("/")) host = host.substring(0, host.length() - 2);
				
		for(AddressList a : _addresses.values()) {
			switch(a.canConnectTo(addr.getAddress().getHostAddress(), host)) {
				case ALLOW: rank++; break;
				case DENY: rank--; break;
				case DOESNT_HAVE: break;
			};
		}
				
		if(rank > 0) return ProxyStatus.ALLOW;
		if(rank < 0) return ProxyStatus.DENY;
		
		
		return _default;
	}
	
}
