package lns.socket.test;

import lns.ev3.model.Ev3;
import lns.socket.client.MessageSocketClient;
import lns.socket.server.MessageSocketServer;
import lns.socket.server.ServerMessageListener;

public class ServerTest {
	private static Ev3 masterEv3;
	private static int clientXp;
	private static int clientYp;
	private static MessageSocketServer server;
	
	public static void main(String[] args) {
		String ip = "192.168.0.12";
		server = new MessageSocketServer(ip);
		
		masterEv3 = new Ev3();
		masterEv3.ev3SetUp(0, 0, 100, 3, 4);
		
		server.start();
		
		server.onConnect(new ServerMessageListener() {
			public void onMessage(MessageSocketClient client, String message) {
				System.out.println(message);
				if(collisionPredict())
					client.emitMessage("turn");
				else
					client.emitMessage("null");

				//이동
				masterEv3.straightOneBlock();
			}

		});
		server.onDisconnect(new ServerMessageListener() {
			public void onMessage(MessageSocketClient client, String message) {
				//System.out.println(server.getClientsNumber() + "] disconnected client : " + client);
			}
		});
		server.onMessage(new ServerMessageListener() {
			public void onMessage(MessageSocketClient client, String message) {
				System.out.println("server received : " + message);
				//System.out.println(message);

				//좌표받기
				getClientCoordinates(message);
				//충돌여부확인
				//명령어 보내기
				if(collisionPredict())
					client.emitMessage("turn");
				else
					client.emitMessage("");

				//이동
				masterEv3.straightOneBlock();
			}
		});
	}
	
	public static boolean getClientCoordinates(String message){
		String[] strArray = message.split(",");
		
		if(strArray.length >= 2) {
			clientXp = Integer.parseInt(strArray[0]);
			clientYp = Integer.parseInt(strArray[1]);
			if ( strArray.length == 3 && strArray[2] == "clear")
				goMasterTargetSpot();
			
			return true;
		}
		return false;
	}
	
	private static void goMasterTargetSpot() {
		if( masterEv3.getTargetYp() > masterEv3.getYp() ) {
			if( masterEv3.getDirection() != 0)
				if( masterEv3.getDirection() == -1 )
					masterEv3.turnRight();
				else 
					masterEv3.turnLeft();
			while( masterEv3.getTargetYp() != masterEv3.getYp() )
				masterEv3.straightOneBlock();

		}
		
		if( masterEv3.getTargetXp() > masterEv3.getXp() ) {
			masterEv3.turnLeft();
			while( masterEv3.getTargetXp() != masterEv3.getXp() )
				masterEv3.straightOneBlock();
		}
	}

	private static boolean collisionPredict() {
		if ( getDistance() < Math.sqrt(5))
			return true;
		
		return false;
	}

	private static double getDistance() {
		return Math.sqrt(Math.pow(masterEv3.getXp()-clientXp, 2) + Math.pow(masterEv3.getYp()-clientYp, 2));
	}
}
