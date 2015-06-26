package lns.socket.server;

import lns.socket.client.MessageSocketClient;

public interface ServerMessageListener {

    void onMessage(MessageSocketClient client, String message);
}
