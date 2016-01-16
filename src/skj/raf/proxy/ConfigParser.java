package skj.raf.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.UUID;

public class ConfigParser {

	private static final String SECRET = "12345";
	private static ArrayList<String> _workers = new ArrayList<>();
	
	public static void parseConfig(String path) throws IOException, ParseException {
		File file = new File(path);
		
		if(!file.isDirectory() && file.canRead()) {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String line;
			int lineNumber = 0;
			
			while((line = br.readLine()) != null) {
				lineNumber++;
				parseCommand(line, lineNumber);
			}
			
			ProxyFilter.configured = true;
			br.close();
		}
	}
	
	public static void parseCommand(String command, int lineNumber) throws ParseException{
		String[] arr = command.split(" ");
		switch(arr[0]) {
			case "DEFAULT": {
				parseDefault(arr, lineNumber);
			} break;
			case "CREATE": {
				parseCreate(arr, lineNumber);
			} break;
			case "ADD": {
				parseAdd(arr, lineNumber);
			} break;
			default: {
				throw new ParseException("Wrong command at " + lineNumber + " line", 0);
			}
		}
	}
	
	public static void parseConfigurator(String command, ConfigUpdater o) throws ParseException{
		String[] hashed = command.split(";");
		if(hashed.length > 1) {
			if(hashed[0].length() > 0 && _workers.contains(hashed[0])) {
				hashed[1] = hashed[1].replaceAll("\\r\\n", "");
				if(hashed[1].startsWith("GET")) {
					parseGet(hashed[1].split(" "), o);
				} else if(hashed[1].startsWith("REMOVE")){
					parseRemove(hashed[1].split(" "), o);
				} else {
					parseCommand(hashed[1], 0);
				}
			} else {
				String[] arr = hashed[1].split(" ");
				if(arr.length > 1) {
					arr[1] = arr[1].replaceAll("\\r\\n", "");
					if(arr.length > 1) {
						if(arr[0].equals("LOGIN") && arr[1].equals(SECRET)) {
							String uuid = UUID.randomUUID().toString();
							if(o.setHash(uuid)) _workers.add(uuid);
							else throw new ParseException("Problem with encoding", 0);
						} else {
							throw new ParseException("Wrong password", 0);
						}
					} else {
						throw new ParseException("Please login first", 0);
					}
				} else {
					throw new ParseException("Please login first", 0);
				}
			}
		}
	}
	
	private static void parseDefault(String[] arr, int lineNumber) throws ParseException{
		if(arr.length > 1) {
			ProxyFilter.setDefault(arr[1].equals("1"));
		} else {
			throw new ParseException("Wrong default section at " + lineNumber, 1);
		}
	}
	
	private static void parseCreate(String[] arr, int lineNumber) throws ParseException{
		if(arr.length > 1) {
			switch(arr[1]) {
				case "LIST_CLIENT" : {
					if(arr.length > 2) {
						ProxyFilter.createClientList(arr[2]);
					} else {
						throw new ParseException("Name for client list is not specified at " + lineNumber, 2);
					}
				} break;
				case "LIST_ADDRESS" : {
					if(arr.length > 2) {
						ProxyFilter.createAddressList(arr[2]);
					} else {
						throw new ParseException("Name for address list is not specified at " + lineNumber, 2);
					}
				} break;
				default: {
					throw new ParseException("Wrong create params at " + lineNumber, 2);
				}
			}
		} else {
			throw new ParseException("Wrong create section at " + lineNumber, 1);
		}
	}
	
	private static void parseAdd(String[] arr, int lineNumber) throws ParseException{
		if(arr.length > 1) {
			switch(arr[1]) {
				case "CLIENT" : {
					if(arr.length > 2) {
						if(arr.length > 3) {
							switch(arr[3]) {
								case "LIST_CLIENT": {
									if(arr.length > 4) {
										ProxyFilter.addClientToClientList(arr[4], arr[2]);
									} else {
										throw new ParseException("Client list name not specified at " + lineNumber, 5);
									}
								} break;
								case "LIST_ADDRESS": {
									if(arr.length > 4) {
										if(arr.length > 5) {
											ProxyFilter.addClientToAddressList(arr[4], arr[2], arr[5].equals("1"));
										} else {
											throw new ParseException("Permision type not specified at " + lineNumber, 6);
										}
									} else {
										throw new ParseException("Address list name not specified at " + lineNumber, 5);
									}
								} break;
								default: {
									throw new ParseException("Wrong list type specified at " + lineNumber, 4);
								}
							}
						} else {
							throw new ParseException("List type not specified at " + lineNumber, 3);
						}
					} else {
						throw new ParseException("Client ip not specified at " + lineNumber, 2);
					}
				} break;
				case "LIST_CLIENT" : {
					if(arr.length > 2) {
						if(arr.length > 3) {
							switch(arr[3]) {
								case "LIST_ADDRESS": {
									if(arr.length > 4) {
										if(arr.length > 5) {
											ProxyFilter.addClientListToAddressList(arr[4], arr[2], arr[5].equals("1"));
										} else {
											throw new ParseException("Permision type not specified at " + lineNumber, 6);
										}
									} else {
										throw new ParseException("Address list name not specified at " + lineNumber, 5);
									}
								} break;
								default: {
									throw new ParseException("Wrong list type specified at " + lineNumber, 4);
								}
							}
						} else {
							throw new ParseException("List type not specified at " + lineNumber, 3);
						}
					} else {
						throw new ParseException("Client list name not specified at " + lineNumber, 2);
					}
				} break;
				case "ADDRESS" : {
					if(arr.length > 2) {
						if(arr.length > 3) {
							switch(arr[3]) {
								case "LIST_ADDRESS": {
									if(arr.length > 4) {
										ProxyFilter.addAddressToAddressList(arr[4], arr[2]);
									} else {
										throw new ParseException("Address list name not specified at " + lineNumber, 5);
									}
								} break;
								default: {
									throw new ParseException("Wrong list type specified at " + lineNumber, 4);
								}
							}
						} else {
							throw new ParseException("List type not specified at " + lineNumber, 3);
						}
					} else {
						throw new ParseException("Website name not specified at " + lineNumber, 2);
					}
				} break;
				default: {
					throw new ParseException("Wrong add params at " + lineNumber, 2);
				}
			}
		} else {
			throw new ParseException("Wrong add section at " + lineNumber, 1);
		}
	}

	private static void parseGet(String[] arr, ConfigUpdater o) throws ParseException {
		if(arr.length > 1) {
			switch(arr[1]) {
				case "LIST_CLIENT": {
					if(arr.length == 2) {
						o.sendList(ProxyFilter.getClientsLists());
					} else {
						o.sendList(ProxyFilter.getClientList(arr[2]));
					}
				} break;
				case "LIST_ADDRESS": {
					if(arr.length == 2) {
						o.sendList(ProxyFilter.getAddressesLists());
					} else {
						o.sendList(ProxyFilter.getAddressList(arr[2]));
					}
				} break;
				default : {
					throw new ParseException("Wrong get params", 2);
				}
			}
		} else {
			throw new ParseException("Wrong get section", 1);
		}
	}
	
	private static void parseRemove(String[] arr, ConfigUpdater o) throws ParseException {
		if(arr.length > 1) {
			switch(arr[1]) {
				case "CLIENT": {
					if(arr.length > 2) {
						if(arr.length > 3) {
							switch(arr[3]) {
								case "LIST_CLIENT": {
									if(arr.length > 4) {
										o.sendRemoval(ProxyFilter.removeClientFromClientList(arr[4], arr[2]));
									} else {
										throw new ParseException("Name for client list is not specified", 5);
									}
								} break;
								case "LIST_ADDRESS": {
									if(arr.length > 4) {
										o.sendRemoval(ProxyFilter.removeClientFromAddressList(arr[4], arr[2]));
									} else {
										throw new ParseException("Name for address list is not specified", 5);
									}
								} break;
								default: {
									throw new ParseException("Wrong list type", 5);
								}
							}
						} else {
							throw new ParseException("List type is not specified", 4);
						}
					} else {
						throw new ParseException("Name for client is not specified", 3);
					}
				} break;
				case "LIST_CLIENT": {
					if(arr.length > 2) {
						o.sendRemoval(ProxyFilter.removeClientList(arr[2]));
					} else {
						throw new ParseException("Name for client list is not specified", 3);
					}
				} break;
				case "LIST_ADDRESS": {
					if(arr.length > 2) {
						o.sendRemoval(ProxyFilter.removeAddressList(arr[2]));
					} else {
						throw new ParseException("Name for address list is not specified", 3);
					}
				} break;
				default: {
					throw new ParseException("Wrong remove params", 2);
				}
			}
		} else {
			throw new ParseException("Wrong remove section", 1);
		}
	}
}
