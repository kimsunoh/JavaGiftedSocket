package lns.socket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import lns.socket.E;
import lns.socket.client.ClientMessageListener;
import lns.socket.client.MessageSocketClient;

public class MessageSocketServer {
	
	public static final int SERVER_EMIT_SOCKET = 5003;
	public static final int SERVER_RECEIVE_SOCKET = 5002;
	private List onDisconnectHandler = new ArrayList(); 
	private class ServerThread extends Thread {
		boolean stopRequested;
		// List<Socket>
		List connectedReceiveSockets = new ArrayList();
		List connectedEmitSockets = new ArrayList();
		ServerThread(){
			stopRequested = false;
		}
		public void run() {
			System.out.println("server started : " + receiveServer.getInetAddress() + ":" + receiveServer.getLocalPort());
			if (isOpen()){
				ReceiveSocketAcceptThread rsat = new ReceiveSocketAcceptThread();
				EmitSocketAcceptThread esat = new EmitSocketAcceptThread();
				rsat.start();
				esat.start();
				while(!stopRequested){
					if (connectedReceiveSockets.size() > 0 && connectedEmitSockets.size() > 0){
						for (int i = 0; i < connectedReceiveSockets.size(); i++) {
							for (int j = 0; j < connectedEmitSockets.size(); j++) {
								Socket receiveSocket = (Socket) connectedReceiveSockets.get(i);
								Socket emitSocket = (Socket) connectedEmitSockets.get(i);
								if (receiveSocket.getInetAddress() != null
									&&	emitSocket.getInetAddress() != null
									&& receiveSocket.getInetAddress().toString().equals(emitSocket.getInetAddress().toString())){
									onClientConnected(receiveSocket, emitSocket);
									connectedReceiveSockets.remove(i);
									connectedEmitSockets.remove(j);
									i = 0;
									j = 0;
								}
							}
						}
					}
				}
			}
		}
		private class ReceiveSocketAcceptThread extends Thread {
			public void run() {
				try {
					receiveServer.setSoTimeout(100);
					while(!stopRequested){
						try{
							Socket aSocket = receiveServer.accept();
							connectedReceiveSockets.add(aSocket);
						} catch (SocketTimeoutException e){
							// do nothing
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		private class EmitSocketAcceptThread extends Thread {
			public void run() {
				try {
					emitServer.setSoTimeout(100);
					while(!stopRequested){
						try{
							Socket aSocket = emitServer.accept();
							connectedEmitSockets.add(aSocket);
						} catch (SocketTimeoutException e){
							// do nothing
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	// List<MessageSocketClient>
	List clients = new ArrayList();
	String hostIp = "";
	List onMessageHandler = new ArrayList();
	List onConnectHandler = new ArrayList();
	
	ServerSocket receiveServer = null;
	ServerSocket emitServer = null;
	
	ServerThread thread = null;

	public MessageSocketServer(String ip) {
		hostIp = ip;
	}
	
	private boolean isOpen() {
		return receiveServer != null && receiveServer.isBound() && emitServer != null && emitServer.isBound();
	}
	
	/**
	 * 새로운 클라이언트가 연결되었을때 serverThread가 호출함
	 * @param socket
	 * @param emitSocket 
	 */
	private void onClientConnected(Socket receiveSocket, Socket emitSocket){
		final MessageSocketClient aClient = new MessageSocketClient(hostIp, emitSocket, receiveSocket, this);
		clients.add(aClient);
		aClient.onMessage(new ClientMessageListener() {
			public void onMessage(MessageSocketClient client, String message) {
				onMessageReceived(aClient, message);
			}

			private void onMessageReceived(MessageSocketClient aClient,
					String message) {
				for (int i = 0; i < onMessageHandler.size(); i++) {
					ServerMessageListener listener = (ServerMessageListener) (onMessageHandler.get(i));
					listener.onMessage(aClient, message);
				}
			}
		});
		for (int i = 0; i < onConnectHandler.size(); i++) {
			ServerMessageListener listener = (ServerMessageListener) onConnectHandler.get(i);
			listener.onMessage(aClient, "connected : " + "[" + aClient.emitSocket + "]");
		}
	}
	
	/**
	 * 메세지 등록 이벤트
	 * @param serverMessageListener
	 */
	public void onMessage(ServerMessageListener serverMessageListener) {
		onMessageHandler.add(serverMessageListener);
	}
	
	/**
	 * 메세지 등록 이벤트
	 * @param serverMessageListener
	 */
	public void onConnect(ServerMessageListener serverMessageListener) {
		onConnectHandler.add(serverMessageListener);
	}
	
	public void start() {
		try {
			receiveServer = new ServerSocket(SERVER_RECEIVE_SOCKET, 8);
			emitServer = new ServerSocket(SERVER_EMIT_SOCKET, 8);
		} catch (IOException e) {
			System.err.println("소켓을 열수 없습니다. 서버가 이미 열려있거나 권한이 없을 수 있습니다.");
			e.printStackTrace();
		}
		if (isOpen()){
			thread = new ServerThread();
			thread.start();
		}
	}

	public void onDisconnect(final ServerMessageListener serverMessageListener) {
		onDisconnectHandler.add(serverMessageListener);
	}

	public void ondisconnectRaised(MessageSocketClient client) {
		for (int i = 0; i < onDisconnectHandler.size(); i++) {
			ServerMessageListener listener = (ServerMessageListener) onDisconnectHandler.get(i);
			listener.onMessage(client, E.DISCONNECT);
		}
		clients.remove(client);
	}

	public String getClientsNumber() {
		return "" + clients.size();
	}
}