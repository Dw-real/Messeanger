//JavaObjServer.java ObjectStream 기반 채팅 Server

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

public class Server extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	JTextArea textArea;
	private JTextField txtPortNumber;
	private ServerSocket socket; // 서버소켓
	private Socket client_socket; // accept() 에서 생성된 client 소켓
	private Vector UserVec = new Vector(); // 연결된 사용자를 저장할 벡터
	private ImageIcon baseProfile = new ImageIcon("img/user.jpg");
	private String statusMessage = "상태메세지";
	private boolean sameroom = false;
	private Vector<Friend> friendVector = new Vector<Friend>();
	private Vector<ChatRoom> RoomVec = new Vector<ChatRoom>();
	private String userlist;
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Server frame = new Server();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Server() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 338, 440);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 300, 298);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(13, 318, 87, 26);
		contentPane.add(lblNewLabel);

		txtPortNumber = new JTextField();
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setText("30000");
		txtPortNumber.setBounds(112, 318, 199, 26);
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);

		JButton btnServerStart = new JButton("Server Start");
		btnServerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				AppendText("Chat Server Running..");
				btnServerStart.setText("Chat Server Running..");
				btnServerStart.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
				txtPortNumber.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(12, 356, 300, 35);
		contentPane.add(btnServerStart);
	}

	// 새로운 참가자 accept() 하고 user thread를 새로 생성한다.
	class AcceptServer extends Thread {
		@SuppressWarnings("unchecked")
		public void run() {
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
					AppendText("새로운 참가자 from " + client_socket);
					// User 당 하나씩 Thread 생성
					UserService new_user = new UserService(client_socket);
					UserVec.add(new_user); // 새로운 참가자 배열에 추가
					new_user.start(); // 만든 객체의 스레드 실행
					AppendText("현재 참가자 수 " + UserVec.size());
				} catch (IOException e) {
					AppendText("accept() error");
					// System.exit(0);
				}
			}
		}
	}

	public void AppendText(String str) {
		// textArea.append("사용자로부터 들어온 메세지 : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public void AppendObject(ChatMsg msg) {
		if (msg.getRoomId() == null) {
			textArea.append("code = " + msg.getCode() + "\n");
			textArea.append("id = " + msg.getId() + "\n");
			textArea.append("data = " + msg.getData() + "\n");
			textArea.setCaretPosition(textArea.getText().length());
		}
		else {
			textArea.append("code = " + msg.getCode() + "\n");
			textArea.append("id = " + msg.getId() + "\n");
			textArea.append("Roomid = " + msg.getRoomId() + "\n");
			textArea.append("userlist = " + msg.getUserlist() + "\n");
			textArea.append("data = "  + msg.getData() + "\n");
			textArea.setCaretPosition(textArea.getText().length());
		}
	}

	// User 당 생성되는 Thread
	// Read One 에서 대기 -> Write All
	class UserService extends Thread {
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;

		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		private Socket client_socket;
		private Vector user_vc;
		private Vector<ChatRoom> room_vc;
		public String roomid;
		public String UserName = "";
		public String UserStatus;

		public UserService(Socket client_socket) {
			// TODO Auto-generated constructor stub
			// 매개변수로 넘어온 자료 저장
			this.client_socket = client_socket;
			this.user_vc = UserVec;
			this.room_vc = RoomVec;
			try {
				oos = new ObjectOutputStream(client_socket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(client_socket.getInputStream());
			} catch (Exception e) {
				AppendText("userService error");
			}
		}

		public void Login() {
			AppendText("새로운 참가자 " + UserName + " 입장.");
			String msg = "[" + UserName + "]님이 입장 하였습니다.\n";
		}

		public void Logout() {
			String msg = "[" + UserName + "]님이 퇴장 하였습니다.\n";
			UserVec.removeElement(this); // Logout한 현재 객체를 벡터에서 지운다
			AppendText("사용자 " + "[" + UserName + "] 퇴장. 현재 참가자 수 " + UserVec.size());
		}

		// Windows 처럼 message 제외한 나머지 부분은 NULL 로 만들기 위한 함수
		public byte[] MakePacket(String msg) {
			byte[] packet = new byte[BUF_LEN];
			byte[] bb = null;
			int i;
			for (i = 0; i < BUF_LEN; i++)
				packet[i] = 0;
			try {
				bb = msg.getBytes("euc-kr");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (i = 0; i < bb.length; i++)
				packet[i] = bb[i];
			return packet;
		}

		
		public void WriteOneObject(Object ob) {
			try {
			    oos.writeObject(ob);
			} 
			catch (IOException e) {
				AppendText("oos.writeObject(ob) error");		
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;				
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout();
			}
		}
		
		// 모든 User들에게 Object를 방송. 채팅 message와 image object를 보낼 수 있다
		public void WriteAllObject(Object ob) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user.UserStatus == "O")
					user.WriteOneObject(ob);
			}
		}
		
		public void run() {
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				try {
					Object obcm = null;
					String msg = null;
					ChatMsg cm = null;
					String[] array = null;
					if (socket == null)
						break;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						AppendObject(cm);
						System.out.println(cm.getCode());
					} else
						continue;
					if (cm.getCode().matches("100")) {
						UserName = cm.getId();
						friendVector.add(new Friend(baseProfile, cm.getId(), statusMessage));
						UserStatus = "O"; // Online 상태
						Login();
					} else if (cm.getCode().matches("200")) {
						msg = String.format("[%s] %s", cm.getId(), cm.getData());
						AppendText(msg); // server 화면에 출력
						roomid = cm.getRoomId();
						for(int i=0; i<room_vc.size(); i++) {
							if(roomid.equals(room_vc.get(i).getRoomId())) {
								userlist = room_vc.get(i).getUserList();
								array = userlist.split(" ");
								for (int j = 0; j < array.length; j++) {
									for(int k = 0; k < user_vc.size(); k++) {
										UserService user = (UserService) user_vc.elementAt(k);
										if (array[j].equals(user.UserName)) {
											ChatMsg obcmr = new ChatMsg(cm.getId(), "200", roomid, userlist, msg);
											obcmr.setImg(cm.img);
											user.oos.writeObject(obcmr);
											break;
										}
									}	
								}
							}
						}
					} else if (cm.getCode().matches("300")) {
						roomid = cm.getRoomId();
						for(int i=0; i<room_vc.size(); i++) {
							if(roomid.equals(room_vc.get(i).getRoomId())) {
								userlist = room_vc.get(i).getUserList();
								array = userlist.split(" ");
								for (int j = 0; j < array.length; j++) {
									for(int k = 0; k < user_vc.size(); k++) {
										UserService user = (UserService) user_vc.elementAt(k);
										if (array[j].equals(user.UserName)) {
											System.out.println(cm.getRoomId());
											user.oos.writeObject(cm);
											break;
										}
									}	
								}
							}
						}
					} else if (cm.getCode().matches("400")) { 
						Logout();
						break;
					} else if (cm.getCode().matches("500")) { 
						for (int i=0; i<friendVector.size(); i++) {
							if (cm.getId().equals(friendVector.get(i).getID())) {
								friendVector.get(i).setIcon(cm.img);
								break;
							}
						}
						ChatMsg profile = new ChatMsg(UserName, "500", "프로필 사진 변경");
						profile.setImg(cm.img);
						WriteAllObject(profile);
					} else if (cm.getCode().matches("510")) { 
						for (int i=0; i<friendVector.size(); i++) {
							if (cm.getId().equals(friendVector.get(i).getID())) {
								friendVector.get(i).setStatusMessage(cm.getData());
								break;
							}
						}
						WriteAllObject(cm);
					} else if (cm.getCode().matches("600")) { // 친구 추가
						for (int i =0; i<friendVector.size(); i++) {
							if (friendVector.get(i).getID().equals(cm.getData())) {
								String info = cm.getData() + "/" + friendVector.get(i).getStatusMessage();
								ChatMsg friendMsg = new ChatMsg("", "600", info);
								if (!friendVector.get(i).getIcon().equals(baseProfile))
									friendMsg.setImg(friendVector.get(i).getIcon());
								WriteOneObject(friendMsg);
								break;
							}
						}
					} else if (cm.getCode().matches("700")) { 
						for (int i =0; i<user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if (user.UserName.equals(cm.getId())) {
								ChatMsg list = new ChatMsg("", "700", cm.getUserlist());
								WriteOneObject(list);
								break;
							}
						}
					} else if (cm.getCode().matches("800")) {
						roomid = cm.getRoomId();
						userlist = cm.getUserlist();
						array = userlist.split(" ");
						for(int i = 0; i < room_vc.size(); i++) {
							if(roomid.equals(room_vc.get(i).getRoomId())) {
								sameroom = true;
								break;
							}						
						}
						if(!sameroom)
						{
							ChatRoom room = new ChatRoom(roomid, userlist);
							for (int i = 0; i < user_vc.size(); i++) {
								for(int j = 0; j < array.length; j++) {
									UserService user = (UserService) user_vc.elementAt(i);
									
									if (array[j].equals(user.UserName)) {
										ChatMsg obcmr = new ChatMsg("", "810", roomid, userlist, "방 생성");
										user.WriteOneObject(obcmr);
										break;
									}
								}	
							}
							RoomVec.add(room);
							
						}					
						sameroom = false;
					}
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					System.out.println(e.getLocalizedMessage());
					try {
						ois.close();
						oos.close();
						client_socket.close();
						Logout(); // 에러가난 현재 객체를 벡터에서 지운다
						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝
			} // while
		} // run
	}
	class ChatRoom extends Thread {
		private String userlist;
		public String room_id = "";
		
		public ChatRoom(String room_id, String userlist) {
			this.room_id = room_id;
			this.userlist = userlist;
			
		}
		public String getRoomId() {
			return room_id;
		}
		public String getUserList() {
			return userlist;
		}
	}

}