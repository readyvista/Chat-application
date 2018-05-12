package com.cauc.chat;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

public class TestSQLEjection extends JFrame{
	private JTextField textFieldUserName;
	private JTextField textFieldPassword;
	private SQLEnjectableUserDatabase userDatabase;
	public TestSQLEjection() {
		userDatabase = new SQLEnjectableUserDatabase();
		// 注册三个新用户
		userDatabase.insertUser("aaa", "aaa");
		userDatabase.insertUser("bbb", "bbb");
		userDatabase.insertUser("ccc", "ccc");
		// 显示所有已注册用户信息
		userDatabase.showAllUsers();
		
		setSize(400, 500);
		getContentPane().setLayout(null);
		
		JLabel label = new JLabel("\u7528\u6237\u540D\uFF1A");
		label.setBounds(76, 83, 54, 15);
		getContentPane().add(label);
		
		JLabel label_1 = new JLabel("\u53E3\u4EE4\uFF1A");
		label_1.setBounds(76, 134, 54, 15);
		getContentPane().add(label_1);
		
		textFieldUserName = new JTextField();
		textFieldUserName.setBounds(159, 83, 147, 21);
		getContentPane().add(textFieldUserName);
		textFieldUserName.setColumns(10);
		
		textFieldPassword = new JTextField();
		textFieldPassword.setBounds(159, 131, 147, 21);
		getContentPane().add(textFieldPassword);
		textFieldPassword.setColumns(10);
		
		JButton btnLogin = new JButton("\u767B\u5F55");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {				
				String userName = textFieldUserName.getText();
				String password = textFieldPassword.getText();				
				
				if (userDatabase.checkUserPassword(userName, password) == true) {
					JOptionPane.showMessageDialog(null, "登录成功");
				} else {
					JOptionPane.showMessageDialog(null, "登录失败");
				}				
			}
		});
		btnLogin.setBounds(159, 202, 93, 23);
		getContentPane().add(btnLogin);
		//pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
	}
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {			
			@Override
			public void run() {
				new TestSQLEjection();				
			}
		});
	}
}
