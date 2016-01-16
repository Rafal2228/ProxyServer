package skj.raf.proxy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

public class ClientHandler implements Runnable{

	private static final int BUFFER_SIZE = 32784;
	private static final long TIMEOUT = 5000;
	
	private Socket _client;
	boolean _running = true;
	
	public ClientHandler(Socket client) {
		_client = client;
	}
	
	private String parseHost(byte[] buffer, int total) throws IOException {
		String host = "";
		
		ByteArrayInputStream is = new ByteArrayInputStream(buffer);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while((line = br.readLine()) != null)
			if(line.contains("Host:"))host = line;
		
		if(host.length() > 5) host = host.substring(5);
    	if(host.contains(" ")) host = host.trim();
    	
		return host;
	}
	
	private boolean parseKeepAlive(byte[] buffer) throws IOException {
		boolean keep = true;
		
		ByteArrayInputStream is = new ByteArrayInputStream(buffer);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while((line = br.readLine()) != null)
			if(line.contains("Connection: close")) keep = false;
		
		return keep;
	}
	
	private InetSocketAddress parseAddress(String host) {
		if(host.contains(":")) {
    		String[] arr = host.split(":");
    		return new InetSocketAddress(arr[0], Integer.parseInt(arr[1]));
    	}
    	return new InetSocketAddress(host, 80);
	}
	
	@Override
	public void run() {
		try {
			byte[] request = new byte[BUFFER_SIZE];
        	int total = _client.getInputStream().read(request, 0, request.length);
        	String host = parseHost(request, total);
        	
        	try {
				_client.setSoTimeout((int)TIMEOUT / 10);
			} catch (SocketException e) {
				System.out.println(e.getMessage());
			}
        	
        	createGetterFromClient(request, total, host);
        } catch (IOException e) {
        	System.out.println(e.getMessage());
        }
	}
	
	private void createGetterFromClient(byte[] request, int total, String host) throws IOException{
		switch(ProxyFilter.filter(new InetSocketAddress(_client.getInetAddress(), _client.getPort()), host)) {
    	case ALLOW: {
    		
    		new Thread(new Runnable() {

				@Override
				public void run() {
	        		try {
	        			InetSocketAddress addr = parseAddress(host);
		        		Socket server = new Socket(addr.getHostName(), addr.getPort());
	        			OutputStream streamToServer = server.getOutputStream();
						InputStream streamFromClient = _client.getInputStream();
						int got = 0;
						boolean keep = true;
						
						String tmpHost = host;
						String oldHost = host;
						
						createGetterFromServer(server, oldHost);
						streamToServer.write(request, 0, total);
						streamToServer.flush();
						long last = new Date().getTime();
						
						while(keep && new Date().getTime() < (last + TIMEOUT)) {
		        			try {
		        				got = streamFromClient.read(request, 0, request.length);
							} catch (IOException e) {
							}
		        			
							if(got > 0) {
		        				keep = parseKeepAlive(request);
		        				if((tmpHost = parseHost(request, got)) != oldHost) {
		        					createGetterFromClient(request, got, tmpHost);
		        				} else {
		        					streamToServer.write(request, 0, got);
				        			streamToServer.flush();
				        			last = new Date().getTime();
		        				}
		        			} else {
		        				try {
									Thread.sleep((int)TIMEOUT / 100);
								} catch (InterruptedException e) {
								}
		        			}
		        		}
						
	        		} catch (IOException e) {
	        		}
				}
    			
    		}).start();
    	} break;
    	case DENY: {
    		System.out.println("DENIED " + host);
    		byte[] res = HTTPReqResBuilder.accessDeniedResponse();
    		
    		_client.getOutputStream().write(res, 0, res.length);
    		_client.getOutputStream().flush();
    		_client.close();
    	} break;
    	case UNSUPPORTED: {
    		System.out.println("UNSUPPORTED " + host);
    		byte[] res = HTTPReqResBuilder.unsupportedMethodResponse();

    		_client.getOutputStream().write(res, 0, res.length);
    		_client.getOutputStream().flush();
    		_client.close();
    	} break;
	}
	}
	
	private void createGetterFromServer(Socket server, String host) throws IOException{
		try {
			server.setSoTimeout((int)TIMEOUT / 10);
		} catch (SocketException e) {
			System.out.println(e.getMessage());
		}
		switch(ProxyFilter.filter(new InetSocketAddress(_client.getInetAddress(), _client.getPort()), host)) {
	    	case ALLOW: {
	    		System.out.println("ALLOW " + host);
	    		
				new Thread(new Runnable() {
		
					@Override
					public void run() {
						try {
							byte[] response = new byte[BUFFER_SIZE];
							InputStream streamFromServer = server.getInputStream();
							OutputStream streamToClient = _client.getOutputStream();
							int got = 0;
							boolean keep = true;
							long last = new Date().getTime();
							
							while(keep && new Date().getTime() < (last + TIMEOUT)) {
								try {
									got = streamFromServer.read(response, 0, response.length);
								} catch (IOException e) {
								}
								if(got > 0) {
									keep = parseKeepAlive(response);
									streamToClient.write(response, 0, got);
				        			streamToClient.flush();
				        			last = new Date().getTime();
								} else {
			        				try {
										Thread.sleep((int)TIMEOUT / 100);
									} catch (InterruptedException e) {
									}
			        			}
			        		}
		
						} catch (IOException e) {
						}
					}
					
				}).start();
	    	} break;
	    	case DENY: {
        		System.out.println("DENIED " + host);
        		byte[] res = HTTPReqResBuilder.accessDeniedResponse();
        		
        		_client.getOutputStream().write(res, 0, res.length);
        		_client.getOutputStream().flush();
        		_client.close();
        	} break;
        	case UNSUPPORTED: {
        		System.out.println("UNSUPPORTED " + host);
        		byte[] res = HTTPReqResBuilder.unsupportedMethodResponse();

        		_client.getOutputStream().write(res, 0, res.length);
        		_client.getOutputStream().flush();
        		_client.close();
        	} break;
		}
	}
	
}
