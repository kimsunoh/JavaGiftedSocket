package lns.socket.client;

import lns.socket.server.ConnectedMessageClient;

public interface ClientMessageListener {
	public void onMessage(MessageSocketClient client, String message);
}
