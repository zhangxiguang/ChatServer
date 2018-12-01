package com.oracle.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.oracle.Chat.model.message;
import com.oracle.Chat.model.user;

public class server {

	private ServerSocket server;
	private static HashMap<String, ObjectOutputStream> allSocket; // 账户+输出流

	public server() {
		try {
			server = new ServerSocket(8888);
			while (true) {
				Socket client = server.accept();

				MessageReciverThread thread = new MessageReciverThread(client);
				thread.start();
				System.out.println(client.getInetAddress().getHostAddress());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		allSocket = new HashMap<>();
		new server();
	}

	class MessageReciverThread extends Thread {
		private Socket c;
		private ObjectInputStream in;
		private ObjectOutputStream out;

		public MessageReciverThread(Socket c) {
			super();
			this.c = c;
			try {
				in = new ObjectInputStream(c.getInputStream());
				out = new ObjectOutputStream(c.getOutputStream());
			} catch (Exception e) {
			}
		}

		@Override
		public void run() {
			try {

				while (true) {
					message Message = (message) in.readObject();
					if (Message.getType().equals("register")) {
//						System.out.println("register message" + Message);
						boolean result = datecontrol.register(Message.getFrom());

//						System.out.println(result);
						// 服务器再封装一个注册结果都Message对象发送给客户端
						message registerResult = new message();
						registerResult.setContent(result ? "success" : "false");
//						System.out.println(result);
						out.writeObject(registerResult);
						out.flush();
					} else if (Message.getType().equals("login")) {
						// 1.根据传过来的登陆信息查询数据库返回查询的结果
						System.out.println("login message" + Message);
						boolean loginresult = datecontrol.login(Message.getFrom());
						System.out.println(loginresult);

						// 2.将查询出来的这个用户对象直接封装成一个消息回给客户端
						// 除了要将登陆的用户资料查询返回给客户端之外还要讲所有注册的用户列表这个集合数据返回给客户端
						message loginResult = new message();
						loginResult.setContent(loginresult ? "success" : "false");
						if (loginResult.getContent().equals("success")) { // 登陆成功，将其加入hashmap
							Message.getFrom().setStatus(true);
							if(allSocket.containsKey(datecontrol.loginUser(Message.getFrom()).getZhanghu())) {
								loginResult.setContent("false");
								out.writeObject(loginResult);
								out.flush();
							}else {
								allSocket.put(datecontrol.loginUser(Message.getFrom()).getZhanghu(), out);

								user logineduser = datecontrol.loginUser(Message.getFrom());
								System.out.println(logineduser);
								List<user> alluesr = datecontrol.allusername();
								loginResult.setAllUser(alluesr);
								loginResult.setFrom(logineduser);
								out.writeObject(loginResult);
								out.flush();

								File files = new File("outlinemessage/onemessage");
								File[] allFiles = files.listFiles();
								for (int i = 0; i < allFiles.length; i++) {
									ObjectInputStream noappectmessage = new ObjectInputStream(
											new FileInputStream(allFiles[i]));
									message noAcceptmessage = (message) noappectmessage.readObject();
									noAcceptmessage.setType("sendmessage");
									if (Message.getFrom().getZhanghu().equals(noAcceptmessage.getTo().getZhanghu())) {
										out.writeObject(noAcceptmessage);
										out.flush();
										noappectmessage.close();
										System.out.println("未读信息已发出，删除文件");
										System.out.println(allFiles[i]);
										File delete = new File(allFiles[i].getPath());
										System.out.println(delete);
										if (delete.exists()) {
											delete.delete();
											System.out.println("我删除了");
										}
									}

								}
							}

						} else {
							out.writeObject(loginResult);
							out.flush();
						}
					} else if (Message.getType().equals("sendmessage")) {
						// 存储聊天记录
//						File onemessage = new File("allmessage/onemessage", Message.getFrom().getUsername() + ".txt");
//						onemessage.createNewFile();
//						ObjectOutputStream outOnemessage = new ObjectOutputStream(
//								new FileOutputStream(onemessage, true));
//						outOnemessage.writeObject(Message);

						System.out.println("sendmessage" + Message);
						Message.setTime(new Date().toString());
						
						if(allSocket.containsKey(Message.getTo().getZhanghu())) {
							allSocket.get(Message.getTo().getZhanghu()).writeObject(Message);
							allSocket.get(Message.getTo().getZhanghu()).flush();
						}else {
							System.out.println("联系人不在线，存储消息");
							File lineonemessage = new File("outlinemessage/onemessage",
									Message.getTo().getZhanghu() + ".txt");
							lineonemessage.createNewFile();
							ObjectOutputStream outlineonemessage = new ObjectOutputStream(
									new FileOutputStream(lineonemessage, true));
							outlineonemessage.writeObject(Message);
							outlineonemessage.flush();
						}
						
						
					} else if (Message.getType().equals("teammessage")) {
						System.out.println("teammessage" + Message);
						Message.setTime(new Date().toString());

						Set<String> set = allSocket.keySet();
						Iterator<String> it = set.iterator();
						while (it.hasNext()) { // 遍历所有用户，发送给所有在线用户
							String testname = it.next();
							if (!Message.getFrom().getZhanghu().equals(testname)) {
								System.out.println(allSocket.get(testname));
								allSocket.get(testname).writeObject(Message);
								allSocket.get(testname).flush();
							}
						}
					} else if (Message.getType().equals("modusermessage")) {
						user modeuesr = datecontrol.modUser(Message.getTo());
//							System.out.println(modeuesr);
						Message.setFrom(modeuesr);

						message updatemesssage = new message();
						updatemesssage.setType("updatemessage");
						Set<String> set = allSocket.keySet();
						Iterator<String> it = set.iterator();
						while (it.hasNext()) {
							String testname = it.next();
							if (Message.getFrom().getZhanghu().equals(testname)) {
								allSocket.get(testname).writeObject(Message);
								allSocket.get(testname).flush();
							} else {
								System.out.println("更他其他用户显示信息");
								allSocket.get(testname).writeObject(updatemesssage);
								allSocket.get(testname).flush();
							}
						}

//						out.writeObject(Message);
//						out.flush();
					} else if (Message.getType().equals("exitmessage")) {
						System.out.println(Message.getFrom().getZhanghu());
						allSocket.remove(Message.getFrom().getZhanghu());
						System.out.println(Message.getFrom().getZhanghu() + "已下线");

						System.out.println(allSocket.size()+"人在线");
						Set<String> set = allSocket.keySet();
						Iterator<String> it = set.iterator();
						while (it.hasNext()) {
							System.out.println(it.next().toString());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
