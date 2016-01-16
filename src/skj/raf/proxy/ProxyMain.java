package skj.raf.proxy;

import java.io.IOException;

public class ProxyMain {

	public static void main(String[] args) {
		int serverPort = args.length > 0 ? Integer.parseInt(args[0]) : 9555;
		int updaterPort = args.length > 1 ? Integer.parseInt(args[1]) : 9556;
		
		try {
			ConfigParser.parseConfig("proxyConfig.ini");
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
		}
		
		try {
			new Thread(new ConfigUpdater(updaterPort)).start();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		if(ProxyFilter.configured)
			try {
				ProxyServer server = new ProxyServer(serverPort);
				
				server.startServer();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		else System.out.println("Wrong configuration");
	}
	
}
