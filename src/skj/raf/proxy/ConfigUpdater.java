package skj.raf.proxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.ParseException;

public class ConfigUpdater implements Runnable{

	private static final int BUFFER_SIZE = 32784;
	
	private DatagramPacket _packet;
	private DatagramSocket _server;
	private byte[] _buffer;
	private String _hash;
	
	public ConfigUpdater(int port) throws SocketException {
		_buffer = new byte[BUFFER_SIZE]; 
		_packet = new DatagramPacket(_buffer, _buffer.length);
		_server = new DatagramSocket(port);
		_hash = "";
		System.out.println("Starting config updater at " + _server.getLocalAddress().getHostAddress() + ":" + port);
	}
	
	public boolean setHash(String uuid) {
		if(_hash.equals("")) {
			_hash = uuid;
			_packet.setData(("HASH;" + uuid).getBytes());
			try {
				_server.send(_packet);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void run() {
		String command;
		while(true) {
			try {
				_packet.setData(_buffer);
				_server.receive(_packet);
				command = new String(_packet.getData(), 0, _packet.getLength());

				try {
					ConfigParser.parseConfigurator(command, this);
					_packet.setData("DONE".getBytes());
				} catch (ParseException e) {
					_packet.setData(e.getMessage().getBytes());
				} finally {
					_server.send(_packet);
				}
				
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
}
