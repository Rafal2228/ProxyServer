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
	
	// ADDRESSES LIST METHODS
	
	public static void createAddressList(String name) throws IllegalArgumentException {
		if(_addresses.containsKey(name)) throw new IllegalArgumentException("Specified name: " + name + " of AddressList is taken");
		_addresses.put(name, new AddressList(name));
	}
	
	public static void addAddressToAddressList(String list, String addr) {
		if(_addresses.containsKey(list)) {
			_addresses.get(list).addAddress(addr);
		} else {
			throw new IllegalArgumentException("Address List " + list + " not found on insert: " + addr);
		}
	}
	
	public static void addClientToAddressList(String addressListName, String client, boolean allow) {
		AddressList addressList = _addresses.get(addressListName);
		if(addressList != null) {
			if(allow) addressList.allowClient(client);
			else addressList.denyClient(client);
		} else {
			throw new IllegalArgumentException("Address List " + addressListName + " not found");
		}
	}
	
	public static void addClientListToAddressList(String addressListName, String clientListName, boolean allow) {
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
