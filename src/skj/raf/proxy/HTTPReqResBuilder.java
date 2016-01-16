package skj.raf.proxy;

import java.util.Date;

public class HTTPReqResBuilder {

	public static byte[] unsupportedMethodResponse() {
		return ("HTTP/1.1 405 Method Not Allowed\r\n"
				+ "Date: " + new Date().toString() + "\r\n"
				+ "Cache-Control: no-cache\r\n"
				+ "Connection: close\r\n"
				+ "Content-Length: 0\r\n").getBytes();
	}
	
	public static byte[] accessDeniedResponse() {
		return ("HTTP/1.1 403 Forbidden\r\n"
				+ "Date: " + new Date().toString() + "\r\n"
				+ "Cache-Control: no-cache\r\n"
				+ "Connection: close\r\n"
				+ "Content-Length: 0\r\n").getBytes();
	}
}
