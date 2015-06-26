package lns.socket.test;

import lns.socket.client.ClientMessageListener;
import lns.socket.client.MessageSocketClient;

public class ClientTest {
	public static void main(String[] args) {
		connectServer("192.168.0.8");
	}
	
	public static void connectServer(String ip) {
		MessageSocketClient socket = new MessageSocketClient(ip);
		socket.connect();
		socket.emitMessage("emit1");
		//sleep();
		socket.emitMessage("emit2");
		//sleep();
		socket.emitMessage("emit3");
		socket.onMessage(new ClientMessageListener(){
			public void onMessage(MessageSocketClient socket, String message){
				System.out.println("client received : " + message);
				socket.disconnect();
			}
		});
		socket.onDisconnect(new ClientMessageListener() {
			public void onMessage(MessageSocketClient client, String message) {
				System.out.println("client disconnected : " + message);
			}
		});
	}

	private static void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
