package chattingProgram;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

//������ ServerSocket�� ����
//ServerSocket�� Socket�� ����
public class Server {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//���� ����
		ServerSocket serverSocket = null;
		Socket socket = null;
		RoomInfo roomInfo = new RoomInfo();
		
		roomInfo.addRoom("1�� ���Դϴ�.");
		roomInfo.addRoom("2�� ��������~");
		roomInfo.addRoom("3�� ���ε���!");
		
		try {
			serverSocket = new ServerSocket(6000); //port��ȣ 4098�� �������� ����
			socket = serverSocket.accept(); //���ϻ��� �޼ҵ� accept();
			Thread th1 = new SenderThread(socket);
			Thread th2 = new ReceiverThread(socket);
			th1.start();
			th2.start();
		} catch(Exception e) {
			System.out.println(e.getMessage());
		} finally { //���� ����� ������ ����
			try {
				serverSocket.close();
			} catch(Exception e) {
				
			}
		}
	}
}