package com.oracle.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	private static HashMap<String, ObjectOutputStream> allSocket;  //账户+输出流

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
		allSocket=new HashMap<>();
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
//						System.out.println("login message" + Message);
						boolean loginresult = datecontrol.login(Message.getFrom());
						System.out.println(loginresult);

						// 2.将查询出来的这个用户对象直接封装成一个消息回给客户端
						// 除了要将登陆的用户资料查询返回给客户端之外还要讲所有注册的用户列表这个集合数据返回给客户端
						message loginResult = new message();
						loginResult.setContent(loginresult ? "success" : "false");
						if (loginResult.getContent().equals("success")) {
							System.out.println(Message.getFrom());
							System.out.println(datecontrol.loginUser(Message.getFrom()).getUsername());
							allSocket.put(datecontrol.loginUser(Message.getFrom()).getZhanghu(), out);
							System.out.println(allSocket);
						}
						user logineduser = datecontrol.loginUser(Message.getFrom());
						List<user> alluesr = datecontrol.allusername();
						loginResult.setAllUser(alluesr);
						loginResult.setFrom(logineduser);
						out.writeObject(loginResult);
						out.flush();
						

					} else if (Message.getType().equals("sendmessage")) {
						System.out.println("sendmessage" + Message);
//						message sendmessage = (message) in.readObject();
						Message.setTime(new Date().toString());
//						user touesr=Message.getTo();
						System.out.println(Message.getTo().getZhanghu());
						System.out.println(allSocket.size());
						
						Set<String> set=allSocket.keySet();
						Iterator<String> it=set.iterator();
						while(it.hasNext()) {
							String testname=it.next();
							if(Message.getTo().getZhanghu().equals(testname)) {
								System.out.println("找到要联系的人啦");
								System.out.println(allSocket.get(testname));
								allSocket.get(testname).writeObject(Message);
								allSocket.get(testname).flush();
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
