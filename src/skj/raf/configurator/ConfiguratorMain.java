package skj.raf.configurator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ConfiguratorMain {
	
	private static final int BUFFER_SIZE = 32784;
	
	public static void main(String[] args) {
		DatagramSocket socket;
		try {
			InetAddress localhost = InetAddress.getByName("localhost");
			socket = new DatagramSocket(9557, localhost);
			
			
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
				packet.setAddress(localhost);
				packet.setPort(9556);
				
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
