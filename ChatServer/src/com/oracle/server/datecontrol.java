package com.oracle.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.oracle.Chat.model.user;

public class datecontrol implements Serializable {

	public datecontrol() {
		super();
	}

	public static boolean register(user user) { // 判断当前账户是否被注册
		try {
			File alluser = new File("userdata");
			System.out.println("找到文件啦");
			System.out.println(user);
			File[] allusername = alluser.listFiles();
			if (allusername.length == 0) {
				System.out.println("账户未被注册啦");
				File userfile = new File("userdata/" + user.getZhanghu() + ".txt");
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(userfile));
				out.writeObject(user);
				out.flush();
				return true;
			} else {
				for (int i = 0; i < allusername.length; i++) {
					if (user.getZhanghu()
							.equals(allusername[i].getName().substring(0, allusername[i].getName().length() - 4))) {
//						System.out.println("账户已经被注册啦");
						return false;
					} else {
//						System.out.println("账户未被注册啦");
						File userfile = new File("userdata/" + user.getZhanghu() + ".txt");
						ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(userfile));
						out.writeObject(user);
						out.flush();
						return true;
					}
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	public static boolean login(user user) { // 判断当前账户是否正确
		try {
			File alluser = new File("userdata");
			System.out.println(user);
			File[] allusername = alluser.listFiles();
			for (int i = 0; i < allusername.length; i++) {
				if (user.getZhanghu()
						.equals(allusername[i].getName().substring(0, allusername[i].getName().length() - 4))) {
					ObjectInputStream inlogin = new ObjectInputStream(new FileInputStream(allusername[i]));
					user loginuesr = (user) inlogin.readObject();
					System.out.println(user.getZhanghu());
					System.out.println(loginuesr.getZhanghu());
					if (user.getZhanghu().equals(loginuesr.getZhanghu())
							&& user.getPassword().equals(loginuesr.getPassword())) {

						return true;
					} else {
						return false;
					}
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static List<user> allusername() { // 遍历所有用户
		List<user> allusername = new ArrayList<>();
		File alluser = new File("userdata");
		File[] allusers = alluser.listFiles();
		for (int i = 0; i < allusers.length; i++) {
			try {
				ObjectInputStream in=new ObjectInputStream(new FileInputStream(allusers[i]));
				try {
					user allusernames=(user)in.readObject();
					allusername.add(allusernames);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return allusername;
	}

	public static user loginUser(user user) { // 根据登录账户提供账户的所有信息
		File alluser = new File("userdata");
		File[] allusers = alluser.listFiles();
		for (int i = 0; i < allusers.length; i++) {
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(new FileInputStream(allusers[i]));
				user loginuesr;
				try {
					loginuesr = (user) in.readObject();
					if (user.getZhanghu().equals(loginuesr.getZhanghu())) {
						return loginuesr;
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return user;
	}
	
	public static user modUser(user user) { // 修改用户信息
		File alluser = new File("userdata");
		File[] allusers = alluser.listFiles();
		for (int i = 0; i < allusers.length; i++) {
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(new FileInputStream(allusers[i]));
				user loginuesr;
				try {
					loginuesr = (user) in.readObject();
					if (user.getZhanghu().equals(loginuesr.getZhanghu())) {
						loginuesr.setUsername(user.getUsername());
						loginuesr.setPassword(user.getPassword());
						loginuesr.setTouxiangpath(user.getTouxiangpath());
						System.out.println(loginuesr);
						
						File userfile = new File("userdata/" + loginuesr.getZhanghu() + ".txt");
						ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(userfile));
						out.writeObject(user);
						out.flush();
						
						return loginuesr;
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return user;
	}
//	public static void main(String[] args) {
//		user u1=new user();
//		u1.setZhanghu("123");
//		register(u1);
//	}

}
