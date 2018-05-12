package com.cauc.chat;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.derby.impl.sql.compile.StaticClassFieldReferenceNode;

import com.cauc.chat.Client.ListeningHandler;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class Login_RegisterTest extends JFrame {
	UserDatabase userDatabase;
	private final int port = 9999;
	private String host="localhost";
	private SSLSocket socket;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	
	
	public Login_RegisterTest() {
		
		
		userDatabase=new UserDatabase();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 654, 455);
		contentPane = new JPanel();
		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel label_User = new JLabel("\u7528\u6237\u540D");
		label_User.setBounds(79, 111, 72, 18);
		contentPane.add(label_User);
		
		JLabel label_Password = new JLabel("\u5BC6\u7801");
		label_Password.setBounds(79, 154, 72, 18);
		contentPane.add(label_Password);
		
		userText = new JTextField();
		userText.setBounds(165, 108, 330, 21);
		contentPane.add(userText);
		userText.setColumns(10);
		
		
		loginButton = new JButton("\u767B\u9646");
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				String userName=userText.getText().trim();
				String userPwd=new String(password_Text.getPassword());
				String phoneNumber=phoneText.getText().trim();
				if(loginButton.getText()=="注册") {
					
//					if(userDatabase.checkUserPassword(userName, userPwd)==true) {
//						System.err.println("该账号已被注册！！");
//					}
						UserRegisterMessage userRegisterMessage=new UserRegisterMessage(userName, userPwd, phoneNumber);
						try {
							oos.writeObject(userRegisterMessage);
							oos.flush();
						}catch(IOException e0) {
							e0.printStackTrace();
						}
//						userDatabase.insertUser(userName, userPwd, phoneNumber);
//						userDatabase.showAllUsers();
						
					
					
					
				}
				
				if(userDatabase.checkUserPassword(userName, userPwd)==true) {
					System.err.println("恭喜你 登陆成功！");
				}
				else if(userDatabase.checkUserPassword(userName, userPwd)==false){
					System.err.println("账号或密码错误！");
				}
				
			}
		});
		loginButton.setBounds(261, 253, 113, 27);
		contentPane.add(loginButton);
		
		label_Phone = new JLabel("\u624B\u673A\u53F7\uFF1A");
		label_Phone.setBounds(79, 198, 72, 18);
		contentPane.add(label_Phone);
		label_Phone.setVisible(false);
		
		phoneText = new JTextField();
		phoneText.setBounds(165, 195, 330, 21);
		contentPane.add(phoneText);
		phoneText.setColumns(10);
		
		password_Text = new JPasswordField();
		password_Text.setBounds(165, 151, 330, 21);
		contentPane.add(password_Text);
		
		registerButton = new JButton("<html><u>点我注册</u></html>");
		registerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loginButton.setText("注册");
				phoneText.setVisible(true);
				label_Phone.setVisible(true);
			}
		});
		registerButton.setBorder(null);
		registerButton.setBounds(391, 254, 113, 27);
		contentPane.add(registerButton);
		phoneText.setVisible(false);
		
//		try {
//			socket = new SSLSocket("localhost", port);
//			// 将socket的输入流和输出流分别封装成对象输入流和对象输出流
//			oos = new ObjectOutputStream(socket
//					.getOutputStream());
//			ois = new ObjectInputStream(socket.getInputStream());
//		} catch (UnknownHostException e1) {
//			JOptionPane.showMessageDialog(null, "找不到服务器主机");
//			e1.printStackTrace();
//			System.exit(0);
//		} catch (IOException e1) {
//			JOptionPane.showMessageDialog(null,
//					"服务器I/O错误，服务器未启动？");
//			e1.printStackTrace();
//			System.exit(0);
//		}
		
		try {
			String passphrase = "654321";
			char[] password = passphrase.toCharArray();
			String trustStoreFile = "test.keys";    
			KeyStore ts = KeyStore.getInstance("JCEKS");
			ts.load(new FileInputStream(trustStoreFile), password);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ts); //客户端用的是别人的证书，当然没有私钥。
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);	//对于客户端来说，不需要向服务器出示证书，第一个参数为空；第二个是获取信任证书；第三个参数是随机种子	
			SSLSocketFactory factory=sslContext.getSocketFactory();
			socket=(SSLSocket)factory.createSocket(host,port);		
			String[] supportedSuites=socket.getSupportedCipherSuites();
//			Stream.of(supportedSuites).forEach(x -> System.out.println(x));		
			socket.setEnabledCipherSuites(supportedSuites);
			
			// 将socket的输入流和输出流分别封装成对象输入流和对象输出流
			oos = new ObjectOutputStream(socket
					.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (UnknownHostException e1) {
			JOptionPane.showMessageDialog(null, "找不到服务器主机");
			e1.printStackTrace();
			System.exit(0);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null,
					"服务器I/O错误，服务器未启动？");
			e1.printStackTrace();
			System.exit(0);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (CertificateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (KeyStoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (KeyManagementException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		
		
	}

	private JPanel contentPane;
//	String driver = "org.apache.derby.jdbc.EmbeddedDriver";
//	// the database name
//	String dbName = "USERDB";
//	// define the Derby connection URL to use
//	String connectionURL = "jdbc:derby:" + dbName + ";create=true";
//	Connection conn;
	private JTextField userText;
	private JPasswordField password_Text;
	private JLabel label_Phone;
	private JTextField phoneText;
	private JButton registerButton;
	private JButton loginButton;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login_RegisterTest frame = new Login_RegisterTest();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public class UserDatabase {

		// ## DEFINE VARIABLES SECTION ##
		// define the driver to use
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		// the database name
		String dbName = "USERDB";
		// define the Derby connection URL to use
		String connectionURL = "jdbc:derby:" + dbName + ";create=true";
		Connection conn;

		public UserDatabase() {
			// ## LOAD DRIVER SECTION ##
//			try {
//				/*
//				 * * Load the Derby driver.* When the embedded Driver is used this
//				 * action start the Derby engine.* Catch an error and suggest a
//				 * CLASSPATH problem
//				 */
//				Class.forName(driver);  //加载JDBC驱动器
//				System.out.println(driver + " loaded. ");
//			} catch (java.lang.ClassNotFoundException e) {
//				System.err.print("ClassNotFoundException: ");
//				System.err.println(e.getMessage());
//				System.out.println("\n    >>> Please check your CLASSPATH variable   <<<\n");
//			}
//			String createString = "create table USERTABLE " // 表名
//					+ "(USERNAME varchar(20) primary key not null, " // 用户名
//					+ "HASHEDPWD char(20) for bit data, " // 口令的HASH值
//					+ "REGISTERTIME timestamp default CURRENT_TIMESTAMP,"// 注册时间
//					+ "PHONENUMBER char(20))"; //电话号码
//			         
//
//			try {
//				DriverManager.setLogWriter(new PrintWriter(new File("aaa.txt"))); //设置数据库日志的输出流
//				// Create (if needed) and connect to the database
//				conn = DriverManager.getConnection(connectionURL);  //连接并打开数据库
//				// Create a statement to issue simple commands.
//				Statement s = conn.createStatement();   //statement用来执行sql语句，但是statement是基于connection创建出来的
//				// Call utility method to check if table exists.
//				// Create the table if needed
//				if (!checkTable(conn)) {
//					System.out.println(" . . . . creating table USERTABLE");
//					s.execute(createString);
//				}
//				s.close();
//				System.out.println("Database openned normally");
//			} catch (SQLException e) {
//				errorPrint(e);
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
		}

		// Insert a new user into the USERTABLE table
		public boolean insertUser(String userName, String userPwd,String phoneNumber) {
			try {
				if (!userName.isEmpty() && !userPwd.isEmpty()) {    //入口参数检查，查看字符串是否为空
					PreparedStatement psTest = conn.prepareStatement(  //是statement的一个子类，是用来执行预编译sql语句的。
							"select * from USERTABLE where USERNAME=?",  //？是JDBC的参数
							ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);  
					psTest.setString(1, userName);
					ResultSet rs = psTest.executeQuery();
					rs.last();
					int n = rs.getRow();
					psTest.close();
					if (n == 0) {
						PreparedStatement psInsert = conn
								.prepareStatement("insert into USERTABLE values (?,?,?,?)");
						MessageDigest digest = MessageDigest.getInstance("SHA-1");
						digest.update(userPwd.getBytes());
						byte[] hashedPwd = digest.digest();
						psInsert.setString(1, userName);
						psInsert.setBytes(2, hashedPwd);
						psInsert.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
						psInsert.setString(4, phoneNumber);
						psInsert.executeUpdate(); //真正执行插入操作的语句
						//prepareStatement可以防止大部分的Sql注入攻击，
						psInsert.close();
						System.out.println("成功注册新用户" + userName);
						return true;
					}
				}
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				errorPrint(e);
			}
			System.out.println("用户" + userName + "已经存在");
			return false;
		}
		
		public boolean deleteUser(String userName, String userPwd) {
			if (checkUserPassword(userName, userPwd) == true) {
				try {
					PreparedStatement psDelete = conn
							.prepareStatement("delete from USERTABLE where USERNAME=?");
					psDelete.setString(1, userName);
					int n = psDelete.executeUpdate();
					psDelete.close();
					if (n > 0) {
						System.out.println("成功删除用户" + userName);
						return true;
					} else {
						System.out.println("删除用户" + userName + "失败");
						return false;
					}
				} catch (SQLException e) {
					errorPrint(e);
				}
			}
			return false;
		}
	
		public boolean checkUserPassword(String userName, String userPwd) { //检查这个用户是不是注册过了
			try {
				if (!userName.isEmpty() && !userPwd.isEmpty()) {
					PreparedStatement psTest = conn.prepareStatement(
							"select * from USERTABLE where USERNAME=?",
							ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					MessageDigest digest = MessageDigest.getInstance("SHA-1");
					digest.update(userPwd.getBytes());
					byte[] hashedPwd = digest.digest();
					psTest.setString(1, userName);
					psTest.setBytes(2, hashedPwd);
					ResultSet rs = psTest.executeQuery(); //executeQuery会返回一个ResultSet类型的对象
					rs.last();
					int n = rs.getRow();
					psTest.close();   //结果集用完一定要关闭，防止资源泄露
					return n > 0 ? true : false;
				}
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				errorPrint(e);
			}
			return false;
		}
		
		public void showAllUsers() {
			String printLine = "  ______________当前所有注册用户______________";
			try {
				Statement s = conn.createStatement();
				// Select all records in the USERTABLE table
				ResultSet users = s
						.executeQuery("select USERNAME, HASHEDPWD, REGISTERTIME,HASHEDSALT from USERTABLE order by REGISTERTIME");

				// Loop through the ResultSet and print the data
				System.out.println(printLine);
				while (users.next()) {
					System.out.println("User-Name: " + users.getString("USERNAME")
							+ " Hashed-Pasword: "
							+ new HexBinaryAdapter().marshal(users.getBytes("HASHEDPWD"))
							+ " Regiester-Time " + users.getTimestamp("REGISTERTIME")
							+ " Hashed-Salt:"+new HexBinaryAdapter().marshal(users.getBytes("HASHEDSALT")));
				}
				System.out.println(printLine);
				// Close the resultSet
				s.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void shutdownDatabase() {
			/***
			 * In embedded mode, an application should shut down Derby. Shutdown
			 * throws the XJ015 exception to confirm success.
			 ***/
			if (driver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
				boolean gotSQLExc = false;
				try {
					conn.close();
					DriverManager.getConnection("jdbc:derby:;shutdown=true");
				} catch (SQLException se) {
					if (se.getSQLState().equals("XJ015")) {
						gotSQLExc = true;
					}
				}
				if (!gotSQLExc) {
					System.out.println("Database did not shut down normally");
				} else {
					System.out.println("Database shut down normally");
				}
			}
		}
		// 关闭数据库
		

		/*** Check for USER table ****/
		
		public boolean checkTable(Connection conTst) throws SQLException {
			try {
				Statement s = conTst.createStatement();
				s.execute("update USERTABLE set USERNAME= 'TEST', REGISTERTIME = CURRENT_TIMESTAMP where 1=3");
			} catch (SQLException sqle) {
				String theError = (sqle).getSQLState();
				// System.out.println("  Utils GOT:  " + theError);
				/** If table exists will get - WARNING 02000: No row was found **/
				if (theError.equals("42X05")) // Table does not exist，表不存在
				{
					return false;
				} else if (theError.equals("42X14") || theError.equals("42821")) {
					System.out
							.println("checkTable: Incorrect table definition. Drop table USERTABLE and rerun this program");
					throw sqle;
				} else {
					System.out.println("checkTable: Unhandled SQLException");
					throw sqle;
				}
			}
			return true;
		}
		
		// Exception reporting methods with special handling of SQLExceptions
			
   }
	
	static void errorPrint(Throwable e) {
		if (e instanceof SQLException) {
			SQLExceptionPrint((SQLException) e);
		} else {
			System.out.println("A non SQL error occured.");
			e.printStackTrace();
		}
	}
	
	static void SQLExceptionPrint(SQLException sqle) {
		while (sqle != null) {
			System.out.println("\n---SQLException Caught---\n");
			System.out.println("SQLState:   " + (sqle).getSQLState());
			System.out.println("Severity: " + (sqle).getErrorCode());
			System.out.println("Message:  " + (sqle).getMessage());
			sqle.printStackTrace();
			sqle = sqle.getNextException();
		}
	}
	
	class ListeningHandler implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					Message msg = null;
					synchronized (ois) {
						msg = (Message) ois.readObject();
					}
					if(msg instanceof UserRegisterStateMessage) {
						   if(((UserRegisterStateMessage) msg).GetUserRegisterStateMessage()) {
							   System.err.println("注册成功！");
								loginButton.setText("登陆");
								phoneText.setVisible(false);
								label_Phone.setVisible(false);
								password_Text.setText(null);
								userText.setText(null);
							   }
						   else {
							   JOptionPane.showMessageDialog(null, "注册失败，该用户已被注册");
						   }
						 }
					}
					
				}catch(IOException e){
					e.printStackTrace();
			} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
			}
	
		}
		
	
}