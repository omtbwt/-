
import java.io.*;
import java.util.*;
import java.net.*;

public class Server {

	static ArrayList<ServerThread> list = new ArrayList<>();

	static int clientCount = 0;

	
	public static void main(String[] args) throws IOException {
		ServerSocket ssocket = new ServerSocket(5002);
		
		Socket s;
		
		while (true) {
			s = ssocket.accept();

			DataInputStream is = new DataInputStream(s.getInputStream());
			DataOutputStream os = new DataOutputStream(s.getOutputStream());

			ServerThread thread = new ServerThread(s, "client " + clientCount, is, os,clientCount);
			list.add(thread);
			thread.start();
			clientCount++;

		}
	}
}

class ServerThread extends Thread {
	Scanner scn = new Scanner(System.in);
	private String name;
	final DataInputStream is;
	final DataOutputStream os;
	Socket s;
	boolean active;
	
	static int x_p1 = 100, y_p1 = 100; //비행기의 좌표
	static int x_p2 = 500, y_p2 = 100;
	
	int num;//p1 p2 구분
	
	public ServerThread(Socket s, String name, DataInputStream is, DataOutputStream os, int num) {
		this.is = is;
		this.os = os;
		this.name = name;
		this.s = s;
		this.active = true;
		this.num = num;
	}

	@Override
	public void run() {
		try {
			this.os.writeInt(num);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		while (true) {
			try {
				String messageType = is.readUTF();

	            // 메시지 타입에 따라 처리
	            if (messageType.equals("X_p1")) {
	                // p1의 x좌표
	                x_p1 = is.readInt();
	                
	            } 
	            else if (messageType.equals("Y_p1")) {
	                // p1의 y좌표
	                y_p1 = is.readInt();

	            }
	            else if (messageType.equals("X_p2")) {
	                // p2의 x좌표
	                x_p2 = is.readInt();

	            }
	            else if (messageType.equals("Y_p2")) {
	                // p2의 y좌표
	                y_p2 = is.readInt();

	            }
	            else if (messageType.equals("M_p1")) {
	                
	            	for (ServerThread t : Server.list)
	            		if (t != this)
	            			t.os.writeUTF("M_p1");

	            }
	            else if (messageType.equals("M_p2")) {
	                
	            	for (ServerThread t : Server.list)
	            		if (t != this)
	            			t.os.writeUTF("M_p2");

	            }
	            
	            
				for (ServerThread t : Server.list) {
					//p1
					if (t != this) {
						t.os.writeUTF("X_p1");
						t.os.writeInt(x_p1);
						t.os.writeUTF("Y_p1");
						t.os.writeInt(y_p1);
						//p2
						t.os.writeUTF("X_p2");
						t.os.writeInt(x_p2);
						t.os.writeUTF("Y_p2");
						t.os.writeInt(y_p2);
						
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		try {
			this.is.close();
			this.os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
