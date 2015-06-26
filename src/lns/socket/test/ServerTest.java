package lns.socket.test;

import lns.socket.client.MessageSocketClient;
import lns.socket.server.ConnectedMessageClient;
import lns.socket.server.MessageSocketServer;
import lns.socket.server.ServerMessageListener;

public class ServerTest {
	public static void main(String[] args) {
		startServer("192.168.0.8");
	}
	public static void startServer(String ip) {
		final MessageSocketServer server = new MessageSocketServer(ip);
		server.start();
		server.onConnect(new ServerMessageListener() {
			public void onMessage(MessageSocketClient client, String message) {
				System.out.println(message);
			}
		});
		server.onDisconnect(new ServerMessageListener() {
			public void onMessage(MessageSocketClient client, String message) {
				System.out.println(server.getClientsNumber() + "] disconnected client : " + client);
			}
		});
		server.onMessage(new ServerMessageListener() {
			public void onMessage(MessageSocketClient client, String message) {
				System.out.println("server received : " + message);
				client.emitMessage("done with " + message);
			}
		});
	}

}
