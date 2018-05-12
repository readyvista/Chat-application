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
	    ks.load(new FileInputStream(keyStoreFile), password);  //��Ϊ�õ������Լ���֤�飬������Ҫ˽Կ
	    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	    kmf.init(ks, password);

	    SSLContext sslContext = SSLContext.getInstance("SSL");
	    sslContext.init(kmf.getKeyManagers(), null, null);  //��ʾ֤�飬����ֵ���������Ҫ��ȡ������֤�飬���Ե�һ������Ҫָ�����ڶ����͵���������Ϊ�ռ��ɡ�

	    //��Ҫ��ͻ����ṩ��ȫ֤��ʱ���������˿ɴ���TrustManagerFactory��
	    //����������TrustManager��TrustManger������֮������KeyStore�е���Ϣ��
	    //�������Ƿ����ſͻ��ṩ�İ�ȫ֤�顣
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
			if (theError.equals("42X05")) // Table does not exist��������
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
		onlineUsersDtm.addColumn("�û���");
		onlineUsersDtm.addColumn("IP");
		onlineUsersDtm.addColumn("�˿�");
		onlineUsersDtm.addColumn("��¼ʱ��");
		tableOnlineUsers.setPreferredSize(new Dimension(100, 270));
		tableOnlineUsers.setFillsViewportHeight(true); // ��JTable������������
		tableOnlineUsers.setPreferredSize(new Dimension(100, 270));
		scrollPaneOnlineUsers.setViewportView(tableOnlineUsers);
        tableOnlineUsers.addMouseListener(new java.awt.event.MouseAdapter(){
        	public void mouseClicked(MouseEvent e) {
        		int selectedRow=tableOnlineUsers.getSelectedRow();
//        		int selectedColumn=tableOnlineUsers.getSelectedColumn();
        		Object value=tableOnlineUsers.getValueAt(selectedRow, 0);
        		String username=value.toString();
        		System.out.println(username);
        		if (JOptionPane.showConfirmDialog(null, "�Ƿ�ǿ�ƽ����˳�?", "�˳�ȷ��",
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
    						+ username + "(" + ip + ")" + "������!\r\n";
    				addMsgRecord(msgRecord, Color.green, 12, false, false);
    				// �ڡ������û��б���ɾ�������û�
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
			Class.forName(driver);  //����JDBC������
			System.out.println(driver + " loaded. ");
		} catch (java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
			System.out.println("\n    >>> Please check your CLASSPATH variable   <<<\n");
		}
		String createString = "create table USERTABLE " // ����
				+ "(USERNAME varchar(20) primary key not null, " // �û���
				+ "HASHEDPWD char(40) for bit data, " // �����HASHֵ
				+ "REGISTERTIME timestamp default CURRENT_TIMESTAMP,"// ע��ʱ��
				+ "PHONENUMBER char(20),"  //�绰����
				+ "HASHEDSALT char(20)for bit data)";    //��ϣ�������ֵ
		         

		try {
			DriverManager.setLogWriter(new PrintWriter(new File("aaa.txt"))); //�������ݿ���־�������
			// Create (if needed) and connect to the database
			conn = DriverManager.getConnection(connectionURL);  //���Ӳ������ݿ�
			// Create a statement to issue simple commands.
			Statement s = conn.createStatement();   //statement����ִ��sql��䣬����statement�ǻ���connection����������
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
					    System.out.println("����������");
//					    System.out.println(serverSocket.getUseClientMode()? "�ͻ�ģʽ":"������ģʽ");
//					    System.out.println(serverSocket.getNeedClientAuth()? "��Ҫ��֤�Է����":"����Ҫ��֤�Է����");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("�����������ɹ���");
				//���㣺���������������߳�
				/*�������new Thread(),ֱ����while(true)����飬��ô��δ�����swing��Event Deal Threat��EDT��Ҳ����
				 * swing�����߳������ģ�
				 * Ҫ������Ӧ�û�������
				 * ������������˸�while(true)���ͻ��EDT����ռ����������ȷ�ķ�����
				��һ�����߳�ȥ���while(true)�����ۣ����Ǻ�ʱ��Ĳ���������Ҫ��EDT������ȥ��һ�����߳��ܡ�*/
				new Thread() {
					public void run() {
						while(true) {
							try {//�����߳�
								Socket socket=serverSocket.accept();
								UserHandler userHandler=new UserHandler(socket); 
								new Thread(userHandler).start(); 
								//����Ϊ�ͻ��˷�����̣߳�ÿ��һ���ͻ��˾ͻ�ִ��һ����
							}catch (Exception e) {
								// TODO: handle exception
							}
						}
					};
				}.start();
				//���裺new Thread(){}.start,Ȼ������{}���run����Ȼ������run�������ԭ����while(true)���顣
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
		SwingUtilities.invokeLater(new Runnable() { //�����񽻸�swing�̣߳�later����˼������ϵͳ�ڸ��½����������ʱʵʩ����
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
				if (!userName.isEmpty() && !userPwd.isEmpty()) {    //��ڲ�����飬�鿴�ַ����Ƿ�Ϊ��
					PreparedStatement psTest = conn.prepareStatement(  //��statement��һ�����࣬������ִ��Ԥ����sql���ġ�
							"select * from USERTABLE where USERNAME=?",  //����JDBC�Ĳ���
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
						psInsert.executeUpdate(); //����ִ�в�����������
						//prepareStatement���Է�ֹ�󲿷ֵ�Sqlע�빥����
						psInsert.close();
						System.out.println("�ɹ�ע�����û�" + userName);
						return true;
					}
				}
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				errorPrint(e);
			}
			System.out.println("�û�" + userName + "�Ѿ�����");
			return false;
		}
		
		private void processUserStateMessage(UserStateMessage msg) {
			String srcUser = msg.getSrcUser();
			if (msg.isUserOnline()) { // �û�������Ϣ
				if (userManage.hasUser(srcUser)) {
					// ���������Ӧ���û��ظ���¼��Ӧ�÷���Ϣ��ʾ�ͻ��ˣ��������
					System.err.println("�û��ظ���¼");
					return;
				}
				// �������ߵ��û�ת����ǰ�����û��б�
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
				// ���������������û�ת���û�������Ϣ
				transferMsgToOtherUsers(msg);
				// ���û���Ϣ���뵽�������û����б���
				onlineUsersDtm.addRow(new Object[] { srcUser,
						socket.getInetAddress().getHostAddress(),
						socket.getPort(),
						dateFormat.format(new Date()) });
				//tableOnlineUsers.add(tableOnlineUsers,new Object[] {srcUser});
				userManage.addUser(srcUser, socket, ois, oos);
				// ����ɫ���ֽ��û������û�����ʱ����ӵ�����Ϣ��¼���ı�����
				
				String ip = socket.getInetAddress().getHostAddress();
				final String msgRecord = dateFormat.format(new Date()) + " "
						+ srcUser + "(" + ip + ")" + "������!\r\n";
				addMsgRecord(msgRecord, Color.green, 12, false, false);
				
				
			} else { // �û�������Ϣ
				if (!userManage.hasUser(srcUser)) {
					// ���������Ӧ���û�δ����������Ϣ��ֱ�ӷ�����������Ϣ��Ӧ�÷���Ϣ��ʾ�ͻ��ˣ��������
					System.err.println("�û�δ���͵�¼��Ϣ�ͷ�����������Ϣ");
					return;
				}
				// ����ɫ���ֽ��û������û�����ʱ����ӵ�����Ϣ��¼���ı�����
				String ip = userManage.getUserSocket(srcUser).getInetAddress()
						.getHostAddress();
				final String msgRecord = dateFormat.format(new Date()) + " "
						+ srcUser + "(" + ip + ")" + "������!\r\n";
				addMsgRecord(msgRecord, Color.green, 12, false, false);
				// �ڡ������û��б���ɾ�������û�
				userManage.removeUser(srcUser);
				for (int i = 0; i < onlineUsersDtm.getRowCount(); i++) {
					if (onlineUsersDtm.getValueAt(i, 0).equals(srcUser)) {
						onlineUsersDtm.removeRow(i);
					}
				}
				// ���û�������Ϣת�����������������û�
				transferMsgToOtherUsers(msg);
			}
		}
		
		public boolean checkUserPassword(String userName, String userPwd) { //�������û��ǲ���ע�����
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
			String printLine = "  ______________��ǰ����ע���û�______________";
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
				// �ú�ɫ���ֽ��յ���Ϣ��ʱ�䡢������Ϣ���û�������Ϣ������ӵ�����Ϣ��¼���ı�����
				final String msgRecord = dateFormat.format(new Date()) + " "
						+ srcUser + "˵: " + msgContent + "\r\n";
				addMsgRecord(msgRecord, Color.black, 12, false, false);
				if (msg.isPubChatMessage()) {
					// ��������Ϣת�����������������û�
					transferMsgToOtherUsers(msg);
				} else {
					// ��˽����Ϣת����Ŀ���û�
					processDirectChatMessage(msg);
				}
			} else {
				// ���������Ӧ���û�δ����������Ϣ��ֱ�ӷ�����������Ϣ��Ӧ�÷���Ϣ��ʾ�ͻ��ˣ��������
				System.err.println("����δ����������Ϣ��ֱ�ӷ�����������Ϣ");
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
				// �ú�ɫ���ֽ��յ���Ϣ��ʱ�䡢������Ϣ���û�������Ϣ������ӵ�����Ϣ��¼���ı�����
				final String msgRecord = dateFormat.format(new Date()) + " "
						+ srcUser + "��"+dstUser+"˵: " + msgContent + "\r\n";
				addMsgRecord(msgRecord, Color.black, 12, false, false);
			
				// ��˽����Ϣת����Ŀ���û�
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
				// ���������Ӧ���û�δ����������Ϣ��ֱ�ӷ�����������Ϣ��Ӧ�÷���Ϣ��ʾ�ͻ��ˣ��������
				System.err.println("����δ����������Ϣ��ֱ�ӷ�����������Ϣ");
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
						// �����û�״̬��Ϣ
						processUserStateMessage((UserStateMessage) msg);
					} else if (msg instanceof ChatMessage) {
						// ����������Ϣ
						processChatMessage((ChatMessage) msg);
					}else if(msg instanceof FileRequestMessage) {
						processFileRequestMessage((FileRequestMessage)msg);
						
					} else if(msg instanceof FileConfirmMessage) {
						processFileConfirmMessage((FileConfirmMessage)msg);
					}
					else { 
						// ���������Ӧ���û���������Ϣ��ʽ ����Ӧ�÷���Ϣ��ʾ�û����������
						System.err.println("�û���������Ϣ��ʽ����!");
					}
				}
			} catch (IOException e) {
				if (e.toString().endsWith("Connection reset")) {
					System.out.println("�ͻ����˳�");
					// ����û�δ����������Ϣ��ֱ�ӹر��˿ͻ��ˣ�Ӧ�������ﲹ����룬ɾ���û�������Ϣ
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
	
	//User����������ϵ�һ�������û�
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
    	//���������ڹ��������û�����
    	private Map<String, User> onLineUsers=new HashMap<String,User>();
    	//����������û�����User�������һ��
    	public boolean hasUser(String userName) {
    		return onLineUsers.containsKey(userName);
    		//containsKey����key�Ƿ�Ϊ��
    	}
    	//�ж������û��б��Ƿ�Ϊ��
    	public boolean isEmpty() {
    		return onLineUsers.isEmpty();
    	}
    	//��ȡһ���û��Ķ��������
    	public ObjectOutputStream getUserOOS(String userName) {
    		if(hasUser(userName)) {
    			return onLineUsers.get(userName).getOos();
    		}
    		return null;
    	}
    	//��ȡһ���û��Ķ���������
    	public ObjectInputStream getUserOIS(String userName) {
    		if(hasUser(userName)) {
    			return onLineUsers.get(userName).getOis();
    		}
    		return null;
    	}
    	//��ȡһ���û���socket����
    	public Socket getUserSocket(String userName) {
    		if(hasUser(userName)) {
    			return onLineUsers.get(userName).getSocket();
    		}
    		return null;
    	}
    	//��������û�
    	public boolean addUser(String userName,Socket socket) {
    		if(userName!=null&&socket!=null) {//��ڲ������
    			onLineUsers.put(userName, new User(socket));
    			return true;
    		}
    		return false;
    	}
    	//�ڶ�����������û��ķ���
    	public boolean addUser(String userName,Socket socket,ObjectInputStream ois,ObjectOutputStream oos) {
    		if(userName!=null&socket!=null&ois!=null&oos!=null) {
    			onLineUsers.put(userName, new User(socket, oos, ois));
    			return true;
    		}
    		return false;
    	}
    	//ɾ�������û�
    	public boolean removeUser(String userName) {
    		if(hasUser(userName)) {
    			onLineUsers.remove(userName);
    			return true;
    		}
    		return false;
    	}
    	//��ȡ���������û��û���
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
