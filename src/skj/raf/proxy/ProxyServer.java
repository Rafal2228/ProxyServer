package skj.raf.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {
	
	ServerSocket _server;
	
	public ProxyServer(int localport) throws IOException {
		_server = new ServerSocket(localport);
	}
	
	public void startServer() throws IOException {
		System.out.println("Starting proxy server at " + _server.getInetAddress().getHostAddress() + ":" + _server.getLocalPort());

		while (true) {
			Socket client = null;
	        client = _server.accept();
	        
	        new Thread(new ClientHandler(client)).start();
		}
	}
}
