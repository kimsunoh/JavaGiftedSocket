package lns.socket.server;

import java.io.DataOutputStream;
import java.net.Socket;

public class ConnectedMessageClient {
	Socket socket;
    private ConnectedMessageClient(){
    }

	public ConnectedMessageClient(Socket socket) {
		this.socket = socket;
	}

	public void emitMessage(String string) {
		
	}

	public void disconnect() {
		// TODO Auto-generated method stub
		
	}
}
