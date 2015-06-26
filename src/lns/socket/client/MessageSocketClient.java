package lns.socket.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import lns.socket.E;
import lns.socket.server.MessageSocketServer;

public class MessageSocketClient  {
	private class MessageEmitThread extends Thread{
		DataOutputStream dos = null;
		String message = null;
		final boolean track = false;
		boolean stopRequested =false;
		public void run() {
			if (track) {log("1");}
			while(!stopRequested){
				if (track) {log("2");}
				if (emitQueue.peek() == null){
					continue;
				}
				if (track) {log("3");}
				if (!emitSocket.isConnected() || !emitSocket.isBound()){
					continue;
				}
				try {
					if (dos == null){
						dos = new DataOutputStream(emitSocket.getOutputStream());
					}
					if (track) {log("6");}
					if (message == null){
						message = (String)emitQueue.take();
					}
					if (track) {log("7");}
					if (message != null){
						log("emiting message : " + message);
						dos.writeUTF(message);
						dos.flush();
						//if (track) {log("emited to [" + emitSocket + "] : " + message);}
						log("emited to : " + message);
						message = null;
					}
					if (track) {log("8");}
				} catch (InterruptedException e) {
					// blockedQueue에서 take할때 실패 -> 재시도
					//e.printStackTrace();
					System.err.println("BlockedQueue InterruptedException");
				} catch (SocketException se){
					System.err.println("소켓이 닫히거나 사용이 불가합니다.");
					se.printStackTrace();
					disconnect();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					disconnect();
					break;
				}
			}
			
		}
	}
	private class MessageReceiveThread extends Thread {
		boolean stopRequested = false;
		public void run() {
			//try { receiveSocket.setTcpNoDelay(true); receiveSocket.setKeepAlive(true); receiveSocket.setSoTimeout(50);} catch (SocketException e) {}
			while (!stopRequested && receiveSocket != null){
				if (!receiveSocket.isConnected() || !receiveSocket.isBound() || receiveSocket.isOutputShutdown()){
					continue;
				}
				try {
					DataInputStream dis = new DataInputStream(receiveSocket.getInputStream());
					String str = dis.readUTF();
					onReceived(str);
					//dis.close();
				} catch (SocketTimeoutException ste){
					// 데이터를 다 읽었을 시 정상동작(readUTF 타임아웃)
				} catch (SocketException se){
					// TODO 소켓이 닫혀서 데이터가 없을 시 동작
					System.err.println("소켓이 닫혀있거나 연결이 없어졌습니다.");
					se.printStackTrace();
					disconnect();
					break;
				} catch (EOFException e) {
					// 데이터가 없을 시 정상동작
					disconnect();
					break;
				} catch (IOException e) {
					if (e instanceof UTFDataFormatException){
						System.err.println("스트림에서 온 데이터가 UTF-8 인코딩이 아닙니다.");
					} else {
						System.err.println("클라이언트에 대한 inputStream을 얻을 수 없습니다.");
					}
					e.printStackTrace();
					disconnect();
					break;
				}
			}
		}
	}
	public MessageSocketServer parentsServer = null;
	private final boolean _DEBUG = true;
	
	// Queue<String>
	private BlockingQueue emitQueue = new LinkedBlockingQueue(); // thread safe Queue  
	public Socket emitSocket = null;
	
	private MessageEmitThread met = null;
	private MessageReceiveThread mrt = null;
	
	private List onDisconnectHandler = new ArrayList();
	// List<ClientMessageListener>
	private List onMessageHandler = new ArrayList();
	
	public Socket receiveSocket = null;  
	String serverIp = null;
	private MessageSocketClient(){
		
	}
	public MessageSocketClient(String ip) {
		serverIp = ip;
	}
	public MessageSocketClient(String ip, Socket clientEmitSocket, Socket clientReceiveSocket, MessageSocketServer server){
		serverIp = ip;
		parentsServer = server;
		this.emitSocket = clientEmitSocket;
		try { clientEmitSocket.setTcpNoDelay(true); clientEmitSocket.setKeepAlive(true); clientEmitSocket.setSoTimeout(50);} catch (SocketException e) {}
		this.receiveSocket = clientReceiveSocket;
		try { clientReceiveSocket.setTcpNoDelay(true); clientReceiveSocket.setKeepAlive(true); clientReceiveSocket.setSoTimeout(50);} catch (SocketException e) {}
		mrt = new MessageReceiveThread();
		mrt.start();
		met = new MessageEmitThread();
		met.start();
	}
	public void close() throws Exception {
		emitWithOperation(E.DISCONNECT, "client disconnet");
		while (emitQueue.peek() != null)
			;
		Thread.sleep(1000); // disconnect 보내기
		if (emitSocket != null){
			try {
				(new DataOutputStream(emitSocket.getOutputStream())).flush();
			} catch (IOException e1) {
				System.err.println("disconnect 메세지를 보내지 못했습니다.");
				e1.printStackTrace();
			}
		}
		met.stopRequested = true;
		met = null;
		mrt.stopRequested = true;
		mrt = null;
		try {
			log("shutdown socket");
			emitSocket.shutdownOutput();
			receiveSocket.shutdownInput();
			emitSocket.close();
			receiveSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		emitSocket = null;
		receiveSocket = null;
		onDisconnectRaised();
	}
	public void connect() {
		log("client connecting to " + serverIp + "...");
		try {
			emitSocket = new Socket(serverIp, MessageSocketServer.SERVER_RECEIVE_SOCKET);
			try { emitSocket.setTcpNoDelay(true); emitSocket.setKeepAlive(true);} catch (SocketException e) {}
			receiveSocket = new Socket(serverIp, MessageSocketServer.SERVER_EMIT_SOCKET);
			try { receiveSocket.setTcpNoDelay(true); receiveSocket.setKeepAlive(true); receiveSocket.setSoTimeout(50);} catch (SocketException e) {}
			log("client connected to " + serverIp + "...");
			mrt = new MessageReceiveThread();
			mrt.start();
			met = new MessageEmitThread();
			met.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void disconnect() {
		if (emitSocket == null || receiveSocket == null){
			//System.err.println((emitSocket == null ? "emitSocket" : "") + (receiveSocket == null ? "receiveSocket" : "") + "이 null입니다.");
			return;
		}
		if (!emitSocket.isConnected() && !receiveSocket.isConnected()){
			return;
		}
		try {
			this.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//log("client disconnected.");
	}
	public void emitMessage(final String message) {
		while(true){
			try {
				emitQueue.put(E.MESSAGE + E.EVENT_DELIMITER + message);
				break;
			} catch (InterruptedException e) {
				continue;
			}
		}
	}
	private void emitWithOperation(final String event, final String string){
		emitQueue.offer(event + E.EVENT_DELIMITER + string);
	}
	private void log(String string) {
		if (_DEBUG){
			System.out.println(string);
		}
	}
	public void onDisconnect(ClientMessageListener clientMessageListener) {
		onDisconnectHandler.add(clientMessageListener);
	}
	public void onDisconnectRaised() {
		if (parentsServer != null){
			parentsServer.ondisconnectRaised(this);
		}
		for (int i = 0; i < onDisconnectHandler.size(); i++) {
			ClientMessageListener listener = (ClientMessageListener) onDisconnectHandler.get(i);
			listener.onMessage(this, "disconnect");
		}
	}
	public void onMessage(ClientMessageListener clientMessageListener) {
		onMessageHandler.add(clientMessageListener);
	}
	private void onReceived(String str) {
		final String operation = str.split(E.EVENT_DELIMITER)[0];
		final String argument = str.substring(operation.length() + 1);
		if (operation.equals(E.MESSAGE)){
			onMessageReceived(argument);
		} else if (operation.equals(E.DISCONNECT)){
			//onDisconnectRaised();
			disconnect();
		}
	}
	/**
	 * 메세지가 왔을때 호출됨
	 * @param message
	 */
	private void onMessageReceived(String message) {
		for (int i = 0; i < onMessageHandler.size(); i++) {
			ClientMessageListener listener = (ClientMessageListener) onMessageHandler.get(i);
			listener.onMessage(this, message);
		}
	}

	public String toString() {
		if (emitSocket != null)
			return emitSocket.toString();
		else 
			return "(not connected) Client for " + serverIp;
	}
}
