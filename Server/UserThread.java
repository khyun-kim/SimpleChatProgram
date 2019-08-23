package chattingProgram;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

public class UserThread extends Thread {
	Socket socket;
	String ip;

	public UserThread(Socket socket, String ip) {
		this.socket = socket;
		this.ip = ip;
	}

	public void run() {
		UsersInfo userInstance = UsersInfo.getInstance();
		RoomsInfo roomInstance = RoomsInfo.getInstance();
		/*
		 * Room r1 = new Room("1번방인데수", "ㄱㄱㅎ"); roomInstance.addRoom(r1); Room r2 = new
		 * Room("2번방이지요", "kkh"); roomInstance.addRoom(r2);
		 */

		try {
			BufferedReader Breader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter Bwriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			PrintWriter Pwriter = new PrintWriter(socket.getOutputStream()); // 출력 스트림
			String nickStr = Breader.readLine();
			System.out.println("닉네임 수신 " + nickStr);

			// 유저 닉네임 저장
			if (nickStr.contains("NICKNAME%$%")) {
				System.out.println("유저닉네임저장 if 시작");
				nickStr = nickStr.substring(11, nickStr.length());
				System.out.println(nickStr);
				User userInfo = new User(nickStr, socket);
				userInstance.addUser(userInfo);
				System.out.println("유저닉네임저장 if 끝");
			} else {
				if (socket != null) {
					try {
						socket.close();
					} catch (Exception e) {
					}
				}
			}
			System.out.println("============================");
			System.out.println("============================");
			while (true) {
				String str = Breader.readLine(); // 입력된 데이타를 한 줄 단위로 읽음
				System.out.println("받은 메세지 : " + str); // 입력 확인용

				// 클라이언트 종료
				// if(str.equals("bye")) break;

				// 방정보 전송(형식 : "RNo방번호RNa방이름RPN인원수")
				if (str.equals("REQUEST_ROOMINFO")) {
					System.out.println("방정보 요청 메세지 수신 완료 / 수신 메세지 : " + str);
					for (int i = 0; i < roomInstance.getRoomAll(); i++) {
						System.out.println("방정보 보내는중 " + i + "개 준비중");
						Pwriter.println("NOTIFY_ADD_ROOM%$%" + i + "%$%" + roomInstance.getRoomInfo(i).roomNa + "%$%" + roomInstance.getRoomInfo(i).entryList.size() + "\r\n");
						System.out.println("NORIFY_ADD_ROOM%$%" + i + "%$%" + roomInstance.getRoomInfo(i).roomNa + "%$%" + roomInstance.getRoomInfo(i).entryList.size() + "\r\n");
						Pwriter.flush();
						Thread.sleep(50);
						System.out.println("방정보 보내는중 " + i + "개 완료");
					}
					System.out.println("방정보 전송 끝");
					str = null;
				}

				// 방 생성
				else if (str.contains("REQUEST_CREATE_ROOM%$%")) {
					System.out.println("방생성 요청 메세지 수신 완료 / 수신 메세지 : " + str);
					String[] createRoomStr = str.split("\\%\\$\\%"); // [0] : 요청 메세지, [1] : 방 제목, [2] : 요청 유저
					System.out.println("메세지 분리 확인용 - 0 : " + createRoomStr[0] + " 1 : " + createRoomStr[1] + " 2 : " + createRoomStr[2]);
					Room room = new Room(createRoomStr[1], createRoomStr[2]);
					roomInstance.addRoom(room);
					int addRoomNu = 0;
					for (int i = roomInstance.getRoomAll() - 1; i >= 0; i--) {
						System.out.println("생성한 방 번호 찾는 중 / 현재 탐색 시작할 방 번호 : " + i);
						if (roomInstance.getRoomInfo(i).roomNa == createRoomStr[1]) {
							addRoomNu = i;
							System.out.println("탐색 완료한 방 번호 : " + i);
							break;
						}
					}
					System.out.println("생성한 방 번호 : " + addRoomNu);
					Pwriter.println("SUCCESS_CREATE_ROOM%$%" + addRoomNu);
					Pwriter.flush();
					str = null;
					str = Breader.readLine();
					System.out.println("추가 메세지 확인 : " + str);
					if(str.equals("READY_FOR_JOIN")) {
						Pwriter.println("USERS%$%" + createRoomStr[2]);
						Pwriter.flush();
					}
					for (int i = 0; i < userInstance.getSizeInfo(); i++) { // 방 생성 후 갱신된 방 정보 재 전송
						System.out.println("생성 후 갱신된 방 정보 전송 준비중");
						PrintWriter NotifyAddRoom = new PrintWriter(userInstance.getUserInfo(i).socket.getOutputStream());
						NotifyAddRoom.println("NOTIFY_ADD_ROOM%$%" + addRoomNu + "%$%" + createRoomStr[1] + "%$%" + roomInstance.getRoomInfo(addRoomNu).entryList.size() +"\r\n");
						NotifyAddRoom.flush();
						System.out.println("갱신된 방 정보 전송 완료");
					}
					System.out.println("방 생성과 생성 후 후처리 완료");
					createRoomStr = null;
					str = null;
				}

				// 방 참여
				else if (str.contains("REQUEST_JOIN_ROOM%$%")) {
					System.out.println("방 참여 메세지 수신 완료 / 수신 메세지 : " + str);
					String[] joinRoomStr = str.split("\\%\\$\\%"); // [0] : 요청 메세지, [1] : 참여할 유저, [2] : 참여할 방 번호
					System.out.println("메세지 분리 확인용 - 0 : " + joinRoomStr[0] + " 1 : " + joinRoomStr[1] + " 2 : " + joinRoomStr[2]);
					roomInstance.getRoomInfo(Integer.parseInt(joinRoomStr[2])).setAddEntry(joinRoomStr[1]); // 방 정보에 참여 유저 넣기
					Pwriter.println("FINISH_JOIN");
					Pwriter.flush();
					str = null;
					str = Breader.readLine();
					System.out.println("참여 확인용" + str);
					if(str.equals("READY_FOR_JOIN")) { // 후 처리 전 메세지 받기
						// 참여자 목록 보내기
						String users = "USERS";
						for (int i = 0; i < roomInstance.getRoomInfo(Integer.parseInt(joinRoomStr[2])).entryList.size(); i++) {
							System.out.println("참여자 목록 가져오는 중 / 현재 진행도 : " + users);
							users += "%$%";
							users += roomInstance.getRoomInfo(Integer.parseInt(joinRoomStr[2])).entryList.get(i);
							System.out.println("참여자 목록 가져오는 중 / 현재 완료된 진행 : " + users);
						}
						users += "\r\n";
						Pwriter.println(users);
						Pwriter.flush();
					
						// 참여한 방에 참여자 정보 전달
						ArrayList<String> joinUsers = roomInstance.getRoomInfo(Integer.parseInt(joinRoomStr[2])).entryList; // 참여한 방의 유저 리스트
						System.out.println("참여한 방의 갱신된 유저 목록 : " + joinUsers);
						for (int i = 0; i < joinUsers.size(); i++) { // 채팅 전송
							System.out.println("참여한 방의 유저들에게 참여자 정보 전송중 / 현재 " + i + "번 유저에게 전송 대기 중");
							PrintWriter sendChat = new PrintWriter(userInstance.getUserSocket(joinUsers.get(i)).getOutputStream());
							sendChat.println(joinRoomStr[1] + "님이 참여하셨습니다.\r\n");
							sendChat.flush();
							System.out.println("참여한 방의 유저들에게 참여자 정보 전송중 / 현재 " + i + "번 유저까지 전송 완료 상태");
						}
						System.out.println("참여한 방의 유저에게 새로운 참여자 전송 완료");
						System.out.println("전체 유저에게 인원수가 변경된 방 알림");
						for (int i = 0; i < userInstance.getSizeInfo(); i++) {
							System.out.println(i + "번 유저에게 전송 준비 중");
							PrintWriter sendChangePeople = new PrintWriter(userInstance.getUserInfo(i).socket.getOutputStream());
							sendChangePeople.println("NOTIFY_CHAGE_ROOM%$%" + joinRoomStr[2] + "%$%" + roomInstance.getRoomInfo(Integer.parseInt(joinRoomStr[2])).entryList.size());
							sendChangePeople.flush();
							System.out.println(i + "번 유저까지 전송 완료");
						}
					}
					System.out.println("방 참여와 후처리 완료");
					joinRoomStr = null;
					str = null;
				}

				// 방 나가기
				else if (str.contains("REQUEST_OUT_ROOM%$%")) {
					System.out.println("방 나가기 메세지 요청 수신 완료 / 수신된 메세지 : " + str);
					String[] outRoomStr = str.split("\\%\\$\\%"); // [0] : 요청 메세지, [1] : 나간 유저, [2]: 나간 방 번호
					System.out.println("메세지 분리 확인용 - 0 : " + outRoomStr[0] + " 1 : " + outRoomStr[1] + " 2 : " + outRoomStr[2]);
					roomInstance.getRoomInfo(Integer.parseInt(outRoomStr[2])).setRemoveEntry(outRoomStr[1]); // 해당 방의 유저 목록에서 나간 유저 삭제
					// 나간 유저 정보 전달
					ArrayList<String> joinUsers = roomInstance.getRoomInfo(Integer.parseInt(outRoomStr[2])).entryList; // 참여한 방의 유저 리스트
					if (joinUsers.size() != 0) {
						System.out.println("해당 방에 남아있는 유저가 있다면 그 유저들에게 퇴장 메세지 전송");
						for (int i = 0; i < joinUsers.size(); i++) { // 채팅 전송
							System.out.println(i + "번 유저에게 전송 준비중");
							PrintWriter sendChat = new PrintWriter(userInstance.getUserSocket(joinUsers.get(i)).getOutputStream());
							sendChat.println(outRoomStr[1] + "님이 퇴장하셨습니다.");
							sendChat.flush();
							System.out.println(i + "번 유저까지 전송 완료");
						}
					}
					System.out.println("방 나가기 요청과 후처리 완료");
					outRoomStr = null;
					str = null;
				}

				// 채팅 전송
				else if (str.contains("SEND_CHAT%$%")) {
					System.out.println("채팅 전송 요청 메세지 / 받은 메세지 : " + str);
					String[] chatStr = str.split("\\%\\$\\%"); // [0] : 요청 메세지, [1] : 방 번호, [2] : 유저 닉네임, [3] : 채팅 내용
					ArrayList<String> joinUsers = roomInstance.getRoomInfo(Integer.parseInt(chatStr[1])).entryList; // 참여한 방의 유저 리스트
					for (int i = 0; i < joinUsers.size(); i++) { // 채팅 전송
						PrintWriter sendChat = new PrintWriter(userInstance.getUserSocket(joinUsers.get(i)).getOutputStream());
						sendChat.println(chatStr[2] + " : " + chatStr[3]);
						sendChat.flush();
					}
				}

				System.out.println("====================================");
			}
		} catch (Exception e) {
			System.out.println("userthread : " + e.getMessage());
		} finally { // 서버 종료시 소켓을 해제
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e) {
				}
			}
		}
	}
}
