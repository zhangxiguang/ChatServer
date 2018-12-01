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
	private static HashMap<String, ObjectOutputStream> allSocket; // �˻�+�����

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
						// �������ٷ�װһ��ע������Message�����͸��ͻ���
						message registerResult = new message();
						registerResult.setContent(result ? "success" : "false");
//						System.out.println(result);
						out.writeObject(registerResult);
						out.flush();
					} else if (Message.getType().equals("login")) {
						// 1.���ݴ������ĵ�½��Ϣ��ѯ���ݿⷵ�ز�ѯ�Ľ��
						System.out.println("login message" + Message);
						boolean loginresult = datecontrol.login(Message.getFrom());
						System.out.println(loginresult);

						// 2.����ѯ����������û�����ֱ�ӷ�װ��һ����Ϣ�ظ��ͻ���
						// ����Ҫ����½���û����ϲ�ѯ���ظ��ͻ���֮�⻹Ҫ������ע����û��б�����������ݷ��ظ��ͻ���
						message loginResult = new message();
						loginResult.setContent(loginresult ? "success" : "false");
						if (loginResult.getContent().equals("success")) { // ��½�ɹ����������hashmap
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
										System.out.println("δ����Ϣ�ѷ�����ɾ���ļ�");
										System.out.println(allFiles[i]);
										File delete = new File(allFiles[i].getPath());
										System.out.println(delete);
										if (delete.exists()) {
											delete.delete();
											System.out.println("��ɾ����");
										}
									}

								}
							}

						} else {
							out.writeObject(loginResult);
							out.flush();
						}
					} else if (Message.getType().equals("sendmessage")) {
						// �洢�����¼
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
							System.out.println("��ϵ�˲����ߣ��洢��Ϣ");
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
						while (it.hasNext()) { // ���������û������͸����������û�
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
								System.out.println("���������û���ʾ��Ϣ");
								allSocket.get(testname).writeObject(updatemesssage);
								allSocket.get(testname).flush();
							}
						}

//						out.writeObject(Message);
//						out.flush();
					} else if (Message.getType().equals("exitmessage")) {
						System.out.println(Message.getFrom().getZhanghu());
						allSocket.remove(Message.getFrom().getZhanghu());
						System.out.println(Message.getFrom().getZhanghu() + "������");

						System.out.println(allSocket.size()+"������");
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
