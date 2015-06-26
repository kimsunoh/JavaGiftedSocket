package lns.socket.test;

import java.net.UnknownHostException;

public class MainTest {
	public static void main(String[] args) throws UnknownHostException {
		String ip = "192.168.0.11";
		ServerTest.startServer(ip);
	}
}
