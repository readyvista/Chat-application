package com.cauc.chat;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.MenuItem;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.derby.impl.sql.compile.StaticClassFieldReferenceNode;
import org.w3c.dom.UserDataHandler;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;

public class Server extends JFrame {

	private final JPanel contentPane;
	private final JTable tableOnlineUsers;
//    private ServerSocket serverSocket;
    private SSLServerSocket serverSocket;
    private final int port=9999;
    private final UserManage userManage=new UserManage();
    private final DefaultTableModel onlineUsersDtm=new DefaultTableModel();
    
    private JButton btnStart;
    private JTextPane textPaneMsgRecord;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    UserDatabase userDatabase;
    String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	// the database name
	String dbName = "USERDB";
	// define the Derby connection URL to use
	String connectionURL = "jdbc:derby:" + dbName + ";create=true";
	Connection conn;
    
	public SSLContext createSSLContext() throws Exception {
	    String keyStoreFile = "test.keys";
	    String passphrase = "654321";
	    KeyStore ks = KeyStore.getInstance("JCEKS");  
	    char[] password = passphrase.toCharArray();
	    ks.load(new FileInputStream(keyStoreFile), password);  //因为用的是你自己的证书，所以需要私钥
	    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	    kmf.init(ks, password);

	    SSLContext sslContext = SSLContext.getInstance("SSL");
	    sslContext.init(kmf.getKeyManagers(), null, null);  //出示证书，种子值随机，不需要获取受信任证书，所以第一参数需要指定，第二个和第三个参数为空即可。

	    //当要求客户端提供安全证书时，服务器端可创建TrustManagerFactory，
	    //并由它创建TrustManager，TrustManger根据与之关联的KeyStore中的信息，
	    //来决定是否相信客户提供的安全证书。
	    //String trustStoreFile = "client.keys";    
	    //KeyStore ts = KeyStore.getInstance("JKS");
	    //ts.load(new FileInputStream(trustStoreFile), password);
	    //TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
	    //tmf.init(ts);
	    //sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
	    
	    return sslContext;
	  }
	
	
    
    
    
    
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
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
	
	void errorPrint(Throwable e) {
		if (e instanceof SQLException) {
			SQLExceptionPrint((SQLException) e);
		} else {
			System.out.println("A non SQL error occured.");
			e.printStackTrace();
		}
	}
	
	void SQLExceptionPrint(SQLException sqle) {
		while (sqle != null) {
			System.out.println("\n---SQLException Caught---\n");
			System.out.println("SQLState:   " + (sqle).getSQLState());
			System.out.println("Severity: " + (sqle).getErrorCode());
			System.out.println("Message:  " + (sqle).getMessage());
			sqle.printStackTrace();
			sqle = sqle.getNextException();
		}
	}

	
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
	/**
	 * Create the frame.
	 */
	public Server() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 561, 403);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JSplitPane splitPaneNorth = new JSplitPane();
		splitPaneNorth.setResizeWeight(0.5);
		contentPane.add(splitPaneNorth, BorderLayout.CENTER);

		JScrollPane scrollPaneMsgRecord = new JScrollPane();
		scrollPaneMsgRecord.setPreferredSize(new Dimension(100, 300));
		scrollPaneMsgRecord.setViewportBorder(new TitledBorder(null,
				"\u6D88\u606F\u8BB0\u5F55", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		splitPaneNorth.setLeftComponent(scrollPaneMsgRecord);

		textPaneMsgRecord = new JTextPane();
		textPaneMsgRecord.setPreferredSize(new Dimension(100, 100));
		scrollPaneMsgRecord.setViewportView(textPaneMsgRecord);

		JScrollPane scrollPaneOnlineUsers = new JScrollPane();
		scrollPaneOnlineUsers.setPreferredSize(new Dimension(100, 300));
		scrollPaneOnlineUsers.setViewportBorder(new TitledBorder(null,
				"\u5728\u7EBF\u7528\u6237", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		splitPaneNorth.setRightComponent(scrollPaneOnlineUsers);

		tableOnlineUsers = new JTable(onlineUsersDtm);
		onlineUsersDtm.addColumn("用户名");
		onlineUsersDtm.addColumn("IP");
		onlineUsersDtm.addColumn("端口");
		onlineUsersDtm.addColumn("登录时间");
		tableOnlineUsers.setPreferredSize(new Dimension(100, 270));
		tableOnlineUsers.setFillsViewportHeight(true); // 让JTable充满它的容器
		tableOnlineUsers.setPreferredSize(new Dimension(100, 270));
		scrollPaneOnlineUsers.setViewportView(tableOnlineUsers);
        tableOnlineUsers.addMouseListener(new java.awt.event.MouseAdapter(){
        	public void mouseClicked(MouseEvent e) {
        		int selectedRow=tableOnlineUsers.getSelectedRow();
//        		int selectedColumn=tableOnlineUsers.getSelectedColumn();
        		Object value=tableOnlineUsers.getValueAt(selectedRow, 0);
        		String username=value.toString();
        		System.out.println(username);
        		if (JOptionPane.showConfirmDialog(null, "是否强制将其退出?", "退出确认",
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
        			ForcedOfflineMessage forcedOfflineMessage=new ForcedOfflineMessage(username, false);
        				ObjectOutputStream o = userManage.getUserOOS(username);
        				try {
        					synchronized (o) {
        						o.writeObject(forcedOfflineMessage);
        						o.flush();
        						
        					}
        				} catch (Exception e1) {
        					e1.printStackTrace();
        				}
        				
        				
        		}
        			
        			String ip = userManage.getUserSocket(username).getInetAddress()
    						.getHostAddress();
    				final String msgRecord = dateFormat.format(new Date()) + " "
    						+ username + "(" + ip + ")" + "下线了!\r\n";
    				addMsgRecord(msgRecord, Color.green, 12, false, false);
    				// 在“在线用户列表”中删除下线用户
    				onlineUsersDtm.removeRow(selectedRow);
    				userManage.removeUser(username);
        		
        	}
        });
		
		try {
			/*
			 * * Load the Derby driver.* When the embedded Driver is used this
			 * action start the Derby engine.* Catch an error and suggest a
			 * CLASSPATH problem
			 */
			Class.forName(driver);  //加载JDBC驱动器
			System.out.println(driver + " loaded. ");
		} catch (java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
			System.out.println("\n    >>> Please check your CLASSPATH variable   <<<\n");
		}
		String createString = "create table USERTABLE " // 表名
				+ "(USERNAME varchar(20) primary key not null, " // 用户名
				+ "HASHEDPWD char(40) for bit data, " // 口令的HASH值
				+ "REGISTERTIME timestamp default CURRENT_TIMESTAMP,"// 注册时间
				+ "PHONENUMBER char(20),"  //电话号码
				+ "HASHEDSALT char(20)for bit data)";    //哈希后的种子值
		         

		try {
			DriverManager.setLogWriter(new PrintWriter(new File("aaa.txt"))); //设置数据库日志的输出流
			// Create (if needed) and connect to the database
			conn = DriverManager.getConnection(connectionURL);  //连接并打开数据库
			// Create a statement to issue simple commands.
			Statement s = conn.createStatement();   //statement用来执行sql语句，但是statement是基于connection创建出来的
			// Call utility method to check if table exists.
			// Create the table if needed
			if (!checkTable(conn)) {
				System.out.println(" . . . . creating table USERTABLE");
				s.execute(createString);
			}
			s.close();
			System.out.println("Database openned normally");
		} catch (SQLException e) {
			errorPrint(e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
		
		
		
		
		JPanel panelSouth = new JPanel();
		contentPane.add(panelSouth, BorderLayout.SOUTH);
		panelSouth.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		btnStart = new JButton("\u542F\u52A8");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					 SSLContext context=createSSLContext();
					    SSLServerSocketFactory factory=context.getServerSocketFactory();
					    serverSocket =(SSLServerSocket)factory.createServerSocket(port);
					    System.out.println("服务器启动");
//					    System.out.println(serverSocket.getUseClientMode()? "客户模式":"服务器模式");
//					    System.out.println(serverSocket.getNeedClientAuth()? "需要验证对方身份":"不需要验证对方身份");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("服务器启动成功！");
				//考点：创建并启动接受线程
				/*如果不用new Thread(),直接上while(true)代码块，那么这段代码是swing的Event Deal Threat，EDT，也就是
				 * swing的主线程上做的，
				 * 要不断响应用户操作。
				 * 如果在这里来了个while(true)，就会把EDT整个占满，所以正确的方法是
				开一个新线程去完成while(true)。结论：凡是耗时间的操作，都不要在EDT上做，去开一个新线程跑。*/
				new Thread() {
					public void run() {
						while(true) {
							try {//处理线程
								Socket socket=serverSocket.accept();
								UserHandler userHandler=new UserHandler(socket); 
								new Thread(userHandler).start(); 
								//启动为客户端服务的线程，每来一个客户端就会执行一个。
							}catch (Exception e) {
								// TODO: handle exception
							}
						}
					};
				}.start();
				//步骤：new Thread(){}.start,然后再在{}里打run函数然后再在run函数里加原来的while(true)语句块。
			    btnStart.setEnabled(false);
			}
		});
		panelSouth.add(btnStart);
		
		
		
	}
	
	private void addMsgRecord(final String msgRecord, Color msgColor,
			int fontSize, boolean isItalic, boolean isUnderline) {
		final SimpleAttributeSet attrset = new SimpleAttributeSet();
		StyleConstants.setForeground(attrset, msgColor);
		StyleConstants.setFontSize(attrset, fontSize);
		StyleConstants.setUnderline(attrset, isUnderline);
		StyleConstants.setItalic(attrset, isItalic);
		SwingUtilities.invokeLater(new Runnable() { //把任务交给swing线程，later的意思就是让系统在更新界面操作发生时实施操作
			@Override
			public void run() {
				Document docs = textPaneMsgRecord.getDocument();
				try {
					docs.insertString(docs.getLength(), msgRecord, attrset);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	class UserHandler implements Runnable{
		private Socket socket;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		public UserHandler(Socket socket) {
			try {
				this.socket=socket;
				ois=new ObjectInputStream(socket.getInputStream());
				oos=new ObjectOutputStream(socket.getOutputStream());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private boolean processUserLoginMessage(UserLoginMessage msg) {
			String userName=msg.getLoginName();
			String userPassword=msg.getPassword();
			System.out.println(userName);
			System.out.println(userPassword);
			if(checkUserPassword(userName, userPassword)) {
				System.out.println("???");
				return true;
			}
			else {
				return false;
			}
		}
		
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
								.prepareStatement("insert into USERTABLE values (?,?,?,?,?)");
						MessageDigest digest = MessageDigest.getInstance("SHA-1");
						MessageDigest digestSalt=MessageDigest.getInstance("SHA-1");
						digest.update(userPwd.getBytes());
						Date saltSeed=new Date();
						SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						String salt=dateFormat.format(saltSeed);
						digestSalt.update(salt.getBytes());
						byte[] hashedPwd = digest.digest();
						byte[] hashedSalt=digestSalt.digest();
						byte[] ultimatePwd=new byte[hashedPwd.length+hashedSalt.length];
						
						System.arraycopy(hashedPwd, 0, ultimatePwd, 0, hashedPwd.length);
						System.arraycopy(hashedSalt, 0, ultimatePwd, hashedPwd.length, hashedSalt.length);
						
						psInsert.setString(1, userName);
						psInsert.setBytes(2, ultimatePwd);
						psInsert.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
						psInsert.setString(4, phoneNumber);
						psInsert.setBytes(5, hashedSalt);
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
		
		private void processUserStateMessage(UserStateMessage msg) {
			String srcUser = msg.getSrcUser();
			if (msg.isUserOnline()) { // 用户上线消息
				if (userManage.hasUser(srcUser)) {
					// 这种情况对应着用户重复登录，应该发消息提示客户端，这里从略
					System.err.println("用户重复登录");
					return;
				}
				// 向新上线的用户转发当前在线用户列表
				String[] users = userManage.getAllUsers();
				try {
					for (String user : users) {
						UserStateMessage userStateMessage = new UserStateMessage(
								user, srcUser, true);
						synchronized (userStateMessage) {
							oos.writeObject(userStateMessage);
							oos.flush();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				// 向所有其它在线用户转发用户上线消息
				transferMsgToOtherUsers(msg);
				// 将用户信息加入到“在线用户”列表中
				onlineUsersDtm.addRow(new Object[] { srcUser,
						socket.getInetAddress().getHostAddress(),
						socket.getPort(),
						dateFormat.format(new Date()) });
				//tableOnlineUsers.add(tableOnlineUsers,new Object[] {srcUser});
				userManage.addUser(srcUser, socket, ois, oos);
				// 用绿色文字将用户名和用户上线时间添加到“消息记录”文本框中
				
				String ip = socket.getInetAddress().getHostAddress();
				final String msgRecord = dateFormat.format(new Date()) + " "
						+ srcUser + "(" + ip + ")" + "上线了!\r\n";
				addMsgRecord(msgRecord, Color.green, 12, false, false);
				
				
			} else { // 用户下线消息
				if (!userManage.hasUser(srcUser)) {
					// 这种情况对应着用户未发送上线消息就直接发送了下线消息，应该发消息提示客户端，这里从略
					System.err.println("用户未发送登录消息就发送了下线消息");
					return;
				}
				// 用绿色文字将用户名和用户下线时间添加到“消息记录”文本框中
				String ip = userManage.getUserSocket(srcUser).getInetAddress()
						.getHostAddress();
				final String msgRecord = dateFormat.format(new Date()) + " "
						+ srcUser + "(" + ip + ")" + "下线了!\r\n";
				addMsgRecord(msgRecord, Color.green, 12, false, false);
				// 在“在线用户列表”中删除下线用户
				userManage.removeUser(srcUser);
				for (int i = 0; i < onlineUsersDtm.getRowCount(); i++) {
					if (onlineUsersDtm.getValueAt(i, 0).equals(srcUser)) {
						onlineUsersDtm.removeRow(i);
					}
				}
				// 将用户下线消息转发给所有其它在线用户
				transferMsgToOtherUsers(msg);
			}
		}
		
		public boolean checkUserPassword(String userName, String userPwd) { //检查这个用户是不是注册过了
			try {
				if (!userName.isEmpty() && !userPwd.isEmpty()) {
					PreparedStatement psTest = conn.prepareStatement(
							"select HASHEDPWD,HASHEDSALT from USERTABLE where USERNAME=? ",
							ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					MessageDigest digest = MessageDigest.getInstance("SHA-1");
					digest.update(userPwd.getBytes());
					byte[] hashedPwd = digest.digest();
					String hashedPwd2compare=new HexBinaryAdapter().marshal(hashedPwd);
					Statement s = conn.createStatement();
					// Select all records in the USERTABLE table
					String savedSalt=new String();
					String ultimatePwd=new String();
					
					psTest.setString(1, userName);
					ResultSet passwords=psTest.executeQuery();
					
//					ResultSet users = s.executeQuery("select USERNAME, HASHEDPWD, REGISTERTIME, HASHEDSALT from USERTABLE order by REGISTERTIME");
					while(passwords.next()) {
						ultimatePwd=new HexBinaryAdapter().marshal(passwords.getBytes("HASHEDPWD"));
						savedSalt=new HexBinaryAdapter().marshal(passwords.getBytes("HASHEDSALT"));
					}
					hashedPwd2compare=hashedPwd2compare+savedSalt;
					System.out.println(hashedPwd2compare);
					if(hashedPwd2compare.equals(ultimatePwd)) {
						return true;
					}
					else {
						return false;
					}
				}
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return false;
			} catch (SQLException e1) {
				errorPrint(e1);
				return false;
			}
			return false;
		}
		
		
		public void showAllUsers() {
			String printLine = "  ______________当前所有注册用户______________";
			try {
				Statement s = conn.createStatement();
				// Select all records in the USERTABLE table
				ResultSet users = s
						.executeQuery("select USERNAME, HASHEDPWD, REGISTERTIME, HASHEDSALT from USERTABLE order by REGISTERTIME");

				// Loop through the ResultSet and print the data
				System.out.println(printLine);
				while (users.next()) {
					System.out.println("User-Name: " + users.getString("USERNAME")
							+ " Hashed-Pasword: "
							+ new HexBinaryAdapter().marshal(users.getBytes("HASHEDPWD"))
							+ " Regiester-Time " + users.getTimestamp("REGISTERTIME")
							+ "Hashed-Salt:"+new HexBinaryAdapter().marshal(users.getBytes("HASHEDSALT")));
				}
				System.out.println(printLine);
				// Close the resultSet
				s.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void processFileRequestMessage(FileRequestMessage msg) {
			String dstUser=((FileRequestMessage)msg).getDstUser();
		
			if(userManage.getUserSocket(dstUser)!=socket) {
				ObjectOutputStream o = userManage.getUserOOS(dstUser);
				try {
					synchronized (o) {
						o.writeObject(msg);
						o.flush();
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		private void processFileConfirmMessage(FileConfirmMessage msg) {
			String fileSender=((FileConfirmMessage)msg).getDstUser();
			System.out.println("......................");
			System.out.println(fileSender);
			if(userManage.getUserSocket(fileSender)!=socket) {
				ObjectOutputStream o = userManage.getUserOOS(fileSender);
				try {
					synchronized (o) {
						o.writeObject(msg);
						o.flush();
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		private void processChatMessage(ChatMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			String msgContent = msg.getMsgContent();
			if (userManage.hasUser(srcUser)) {
				// 用黑色文字将收到消息的时间、发送消息的用户名和消息内容添加到“消息记录”文本框中
				final String msgRecord = dateFormat.format(new Date()) + " "
						+ srcUser + "说: " + msgContent + "\r\n";
				addMsgRecord(msgRecord, Color.black, 12, false, false);
				if (msg.isPubChatMessage()) {
					// 将公聊消息转发给所有其它在线用户
					transferMsgToOtherUsers(msg);
				} else {
					// 将私聊消息转发给目标用户
					processDirectChatMessage(msg);
				}
			} else {
				// 这种情况对应着用户未发送上线消息就直接发送了聊天消息，应该发消息提示客户端，这里从略
				System.err.println("用启未发送上线消息就直接发送了聊天消息");
				return;
			}
		}
		
		
		private void transferMsgToOtherUsers(Message msg) {
			String[] users = userManage.getAllUsers();
			for (String user : users) {
				if (userManage.getUserSocket(user) != socket) {
					try {
						ObjectOutputStream o = userManage.getUserOOS(user);
						synchronized (o) {
							o.writeObject(msg);
							o.flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			}
		}
		
		
		
		
		
		private void processDirectChatMessage(ChatMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			String msgContent = msg.getMsgContent();
			System.out.println(msgContent);
			
			if (userManage.hasUser(srcUser)&&userManage.hasUser(dstUser)) {
				// 用黑色文字将收到消息的时间、发送消息的用户名和消息内容添加到“消息记录”文本框中
				final String msgRecord = dateFormat.format(new Date()) + " "
						+ srcUser + "对"+dstUser+"说: " + msgContent + "\r\n";
				addMsgRecord(msgRecord, Color.black, 12, false, false);
			
				// 将私聊消息转发给目标用户
					if(userManage.getUserSocket(dstUser)!=socket) {
						ObjectOutputStream o = userManage.getUserOOS(dstUser);
						try {
							synchronized (o) {
								o.writeObject(msg);
								o.flush();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					} 

			} else {
				if(userManage.hasUser(srcUser)&&!userManage.hasUser(dstUser))
				// 这种情况对应着用户未发送上线消息就直接发送了聊天消息，应该发消息提示客户端，这里从略
				System.err.println("用启未发送上线消息就直接发送了聊天消息");
				return;
			}
		}
		
		
		@Override
		public void run() {
			try {
				while (true) {
					Message msg = (Message) ois.readObject();
					if(msg instanceof UserLoginMessage) {
						UserLoginStateMessage userLoginStateMessage;
						if(processUserLoginMessage((UserLoginMessage)msg)) {
							System.out.println("???");
							userLoginStateMessage=new UserLoginStateMessage(true);
						}else {
							userLoginStateMessage=new UserLoginStateMessage(false);
						}
						oos.writeObject(userLoginStateMessage);
						oos.flush();
						showAllUsers();
					}
					else if(msg instanceof UserRegisterMessage) {
						String userName=((UserRegisterMessage) msg).GetUserName();
						String password=((UserRegisterMessage) msg).GetUserPassword();
						String phoneNumber=((UserRegisterMessage) msg).GetPhoneNumber();
						UserRegisterStateMessage userRegisterStateMessage;
						if(insertUser(userName, password, phoneNumber)) {
							userRegisterStateMessage=new UserRegisterStateMessage(true);
						}
						else {
							userRegisterStateMessage=new UserRegisterStateMessage(false);
						}
						showAllUsers();
						oos.writeObject(userRegisterStateMessage);
						oos.flush();
					}
					else if (msg instanceof UserStateMessage) {
						// 处理用户状态消息
						processUserStateMessage((UserStateMessage) msg);
					} else if (msg instanceof ChatMessage) {
						// 处理聊天消息
						processChatMessage((ChatMessage) msg);
					}else if(msg instanceof FileRequestMessage) {
						processFileRequestMessage((FileRequestMessage)msg);
						
					} else if(msg instanceof FileConfirmMessage) {
						processFileConfirmMessage((FileConfirmMessage)msg);
					}
					else { 
						// 这种情况对应着用户发来的消息格式 错误，应该发消息提示用户，这里从略
						System.err.println("用户发来的消息格式错误!");
					}
				}
			} catch (IOException e) {
				if (e.toString().endsWith("Connection reset")) {
					System.out.println("客户端退出");
					// 如果用户未发送下线消息就直接关闭了客户端，应该在这里补充代码，删除用户在线信息
				} else {
					e.printStackTrace();
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		
		
		
	}
	
	//User代表服务器上的一个在线用户
	class User{
		private Socket socket;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		private final LocalDate logonTime;
		
		public Socket getSocket() {
			return socket;
		}
		
		public ObjectInputStream getOis() {
			return ois;
		}
		
		public ObjectOutputStream getOos() {
			return oos;
		}
		
		public LocalDate getLogonTime() {
			return logonTime;
		}
		
		public User(Socket socket) {
			this.socket=socket;
			try {
				ois=new ObjectInputStream(socket.getInputStream());
				oos=new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logonTime=LocalDate.now();
		}
        public User(Socket socket,ObjectOutputStream oos,ObjectInputStream ois) {
        	this.ois=ois;
        	this.oos=oos;
        	this.socket=socket;
        	logonTime=LocalDate.now();
        	
        }
	}
    class UserManage{
    	//服务器用于管理在线用户的类
    	private Map<String, User> onLineUsers=new HashMap<String,User>();
    	//将待管理的用户名和User对象绑定在一起
    	public boolean hasUser(String userName) {
    		return onLineUsers.containsKey(userName);
    		//containsKey会检查key是否为空
    	}
    	//判断在线用户列表是否为空
    	public boolean isEmpty() {
    		return onLineUsers.isEmpty();
    	}
    	//获取一个用户的对象输出流
    	public ObjectOutputStream getUserOOS(String userName) {
    		if(hasUser(userName)) {
    			return onLineUsers.get(userName).getOos();
    		}
    		return null;
    	}
    	//获取一个用户的对象输入流
    	public ObjectInputStream getUserOIS(String userName) {
    		if(hasUser(userName)) {
    			return onLineUsers.get(userName).getOis();
    		}
    		return null;
    	}
    	//获取一个用户的socket对象
    	public Socket getUserSocket(String userName) {
    		if(hasUser(userName)) {
    			return onLineUsers.get(userName).getSocket();
    		}
    		return null;
    	}
    	//添加在线用户
    	public boolean addUser(String userName,Socket socket) {
    		if(userName!=null&&socket!=null) {//入口参数检查
    			onLineUsers.put(userName, new User(socket));
    			return true;
    		}
    		return false;
    	}
    	//第二种添加在线用户的方法
    	public boolean addUser(String userName,Socket socket,ObjectInputStream ois,ObjectOutputStream oos) {
    		if(userName!=null&socket!=null&ois!=null&oos!=null) {
    			onLineUsers.put(userName, new User(socket, oos, ois));
    			return true;
    		}
    		return false;
    	}
    	//删除在线用户
    	public boolean removeUser(String userName) {
    		if(hasUser(userName)) {
    			onLineUsers.remove(userName);
    			return true;
    		}
    		return false;
    	}
    	//获取所有在线用户用户名
    	public String[] getAllUsers() {
    		String[] Users=new String[onLineUsers.size()];
    		if(Users.length>0) {
    			int i=0;
    			for(Map.Entry<String, User>entry:onLineUsers.entrySet()) {
    				Users[i]=entry.getKey();
    			}
    		}
    		return Users;
    	}
    	
    	public int getOnlineUserCount() {
    		return onLineUsers.size();
    	}
    }
    
}
