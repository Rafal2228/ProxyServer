package skj.raf.configurator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ConfiguratorMain {
	
	private static final int BUFFER_SIZE = 32784;
	
	public static void main(String[] args) {
		String serverName = args.length > 0 ? args[0] : "localhost";
		int localPort = args.length > 1 ? Integer.parseInt(args[1]) : 9557;
		int remotePort = args.length > 2 ? Integer.parseInt(args[2]) : 9556;
		DatagramSocket socket;
		
		try {
			InetAddress serverAddr = InetAddress.getByName(serverName);
			socket = new DatagramSocket(localPort, serverAddr);
			
			
			Scanner scanner = new Scanner(System.in);
			DatagramPacket packet;
			byte[] buffer;
			boolean logged = false;
			String hash = "";
			String base = hash + ";";
			String tmp;
			
			while(true){
				tmp = base + scanner.nextLine();
				buffer = tmp.getBytes();
				packet = new DatagramPacket(buffer, buffer.length);
				packet.setAddress(serverAddr);
				packet.setPort(remotePort);
				
				try{
					socket.send(packet);
					buffer = new byte[BUFFER_SIZE];
					packet.setData(buffer);
					
					socket.receive(packet);
					String rec = new String(packet.getData(), 0, packet.getLength());
					
					if(!logged && rec.contains("HASH;")) {
						rec = rec.substring(5, rec.length());
						hash = rec;
						base = hash + ";";

						logged = true;
						buffer = new byte[BUFFER_SIZE];
						packet.setData(buffer);
						socket.receive(packet);
					}
					
					if(rec.startsWith("GET;")) {
						rec = rec.substring(4);
						System.out.println(rec);
						buffer = new byte[BUFFER_SIZE];
						packet.setData(buffer);
						socket.receive(packet);
					}
					
					if(rec.startsWith("REMOVE;")) {
						rec = rec.substring(7);
						System.out.println(rec);
						buffer = new byte[BUFFER_SIZE];
						packet.setData(buffer);
						socket.receive(packet);
					}
					
					if(rec.startsWith("SAVE;")) {
						rec = rec.substring(5);
						System.out.println(rec);
						buffer = new byte[BUFFER_SIZE];
						packet.setData(buffer);
						socket.receive(packet);
					}
					
					System.out.println(new String(packet.getData()));
				} catch(IOException e) {
					scanner.close();
					System.exit(1);
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
}
