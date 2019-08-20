package chattingProgram;

import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;

public class User {
	String nickName = null;
	String ip = null;
	Socket socket = null;
	int joinRoomNo = 0;
	
	public User (String nickName, Socket socket) {
		this.nickName = nickName;
		this.socket = socket;
	}
	
	public String getNickName() {
		return nickName;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public String getIp() {
		return ip;
	}
	
	public Integer getJoinRoomNo() {
		return joinRoomNo;
	}
	
	public void setJoinRoomNo(int i) {
		joinRoomNo = i;
	}
	
}