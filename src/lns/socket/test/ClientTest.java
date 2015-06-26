package lns.socket.test;

import lns.ev3.model.Ev3;
import lns.socket.client.ClientMessageListener;
import lns.socket.client.MessageSocketClient;

public class ClientTest {
	private static Ev3 slaveEv3;
	
	private static MessageSocketClient socket;
	
	public static void main(String[] args) {
		String ip = "192.168.0.12";
		socket = new MessageSocketClient(ip);
		slaveEv3 = new Ev3();
		slaveEv3.ev3SetUp(0, 4, 100, 3, 0);
		
		socket.connect();
		socket.emitMessage(slaveEv3.getXp()+","+slaveEv3.getYp());
 
		socket.onMessage(new ClientMessageListener(){
			public void onMessage(MessageSocketClient socket, String message){
				//System.out.println("client received : " + message);
				if(getMasterOrder(message))
					socket.emitMessage(slaveEv3.getXp()+","+slaveEv3.getYp());
				else
					socket.disconnect();
			}
		});

		socket.onDisconnect(new ClientMessageListener() {
			public void onMessage(MessageSocketClient client, String message) {
				System.out.println("client disconnected : " + message);
			}
		});
	}
	
	public static boolean getMasterOrder(String message){
	
		if (message == "turn") 
			turnSlave();
		else if (message == "run") {
			goSlaveTargetSpot();
			return true;
		}
		slaveEv3.straightOneBlock();
		
		return true;
	}

	private static void goSlaveTargetSpot() {
		
		if( slaveEv3.getTargetYp() > slaveEv3.getYp() ) {
			if( slaveEv3.getDirection() != 0)
				if( slaveEv3.getDirection() == -1 )
					slaveEv3.turnRight();
				else 
					slaveEv3.turnLeft();
			while( slaveEv3.getTargetYp() != slaveEv3.getYp() )
				slaveEv3.straightOneBlock();

		}
		
		if( slaveEv3.getTargetXp() < slaveEv3.getXp() ) {
			slaveEv3.turnRight();
			while( slaveEv3.getTargetXp() != slaveEv3.getXp() )
				slaveEv3.straightOneBlock();
		}
	}

	private static void turnSlave() {
		if(slaveEv3.getDirection() != 1)
			slaveEv3.turnRight();
		else
			slaveEv3.turnLeft();
	}
}
