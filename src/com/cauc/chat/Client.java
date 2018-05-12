package com.cauc.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.jws.soap.SOAPBinding.Style;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.transform.sax.SAXSource;

import org.apache.derby.tools.sysinfo;

import com.cauc.chat.Server.UserHandler;
import javax.swing.JProgressBar;

public class Client extends JFrame {
	private final int port = 9999;
	private String host="localhost";
	private SSLSocket socket;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	private String localUserName;
	// “在线用户列表ListModel”,用于维护“在线用户列表”中显示的内容
	private final DefaultListModel<String> onlinUserDlm = new DefaultListModel<String>();
	// 用于控制时间信息显示格式
	// private final SimpleDateFormat dateFormat = new
	// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	

	private final JPanel contentPane;
	private final JTextField textFieldUserName;
	private final JPasswordField passwordFieldPwd;
	private final JTextField textFieldMsgToSend;
	private final JTextPane textPaneMsgRecord;
	private final JList<String> listOnlineUsers;
	private final JButton btnLogon;
	private final JButton btnSendMsg;
	private final JButton btnSendFile;
	private JProgressBar fileTransferProgress;
	private JLabel progressLabel;
	
	
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Client frame = new Client();
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
	public Client() {
		
		
		setTitle("\u5BA2\u6237\u7AEF");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 631, 420);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panelNorth = new JPanel();
		panelNorth.setBorder(new EmptyBorder(0, 0, 5, 0));
		contentPane.add(panelNorth, BorderLayout.NORTH);
		panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.X_AXIS));

		JLabel lblUserName = new JLabel("\u7528\u6237\u540D\uFF1A");
		panelNorth.add(lblUserName);

		textFieldUserName = new JTextField();
		panelNorth.add(textFieldUserName);
		textFieldUserName.setColumns(10);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		panelNorth.add(horizontalStrut);

		JLabel lblPwd = new JLabel("\u53E3\u4EE4\uFF1A");
		panelNorth.add(lblPwd);

		passwordFieldPwd = new JPasswordField();
		passwordFieldPwd.setColumns(10);
		panelNorth.add(passwordFieldPwd);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		panelNorth.add(horizontalStrut_1);

		btnLogon = new JButton("\u767B\u5F55"); // “登录”按钮
		btnLogon.addActionListener(new ActionListener() {
			
			
			
			
			
			
		
			
			
			public void actionPerformed(ActionEvent e) {
				if (btnLogon.getText().equals("登录")) {
					localUserName = textFieldUserName.getText().trim();
					String localPassword=new String(passwordFieldPwd.getPassword());
					System.out.println(localPassword);
					if (localUserName.length() > 0) {
						// 与服务器端建立Socket连接，如果抛出异常，则弹出对话框通知用户，并退出
						
						// 向服务器发送用户上线信息，将自己的用户名发送给服务器
						
						UserLoginMessage userLoginMessage=new UserLoginMessage(localUserName, localPassword);
						try {
							oos.writeObject(userLoginMessage);
							oos.flush();
							
						}catch(IOException e0) {
							e0.printStackTrace();
						}
					
						
						
					}
				} else if (btnLogon.getText().equals("退出")) {
					if (JOptionPane.showConfirmDialog(null, "是否退出?", "退出确认",
							JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						// 向服务器发送用户下线消息
						UserStateMessage userStateMessage = new UserStateMessage(
								localUserName, "", false);
						try {
							synchronized (oos) {
								oos.writeObject(userStateMessage);
								oos.flush();
							}
							System.exit(0);
						} catch (IOException e1) {
							e1.printStackTrace();
							
						}
					}
				}

			}
		});
		panelNorth.add(btnLogon);
		
		JButton btnRegister = new JButton("\u6CE8\u518C");
		btnRegister.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				Login_RegisterTest login_RegisterTest=new Login_RegisterTest();
				login_RegisterTest.setVisible(true);
			}
		});
		panelNorth.add(btnRegister);

		JSplitPane splitPaneCenter = new JSplitPane();
		splitPaneCenter.setResizeWeight(1.0);
		contentPane.add(splitPaneCenter, BorderLayout.CENTER);

		JScrollPane scrollPaneMsgRecord = new JScrollPane();
		scrollPaneMsgRecord.setViewportBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "\u6D88\u606F\u8BB0\u5F55",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPaneCenter.setLeftComponent(scrollPaneMsgRecord);

		textPaneMsgRecord = new JTextPane();
		scrollPaneMsgRecord.setViewportView(textPaneMsgRecord);
		

		JScrollPane scrollPaneOnlineUsers = new JScrollPane();
		scrollPaneOnlineUsers.setViewportBorder(new TitledBorder(null,
				"\u5728\u7EBF\u7528\u6237", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		splitPaneCenter.setRightComponent(scrollPaneOnlineUsers);

		listOnlineUsers = new JList<String>(onlinUserDlm);
		scrollPaneOnlineUsers.setViewportView(listOnlineUsers);

		JPanel panelSouth = new JPanel();
		panelSouth.setBorder(new EmptyBorder(5, 0, 0, 0));
		contentPane.add(panelSouth, BorderLayout.SOUTH);
		panelSouth.setLayout(new BoxLayout(panelSouth, BoxLayout.X_AXIS));
		
		fileTransferProgress = new JProgressBar();
		panelSouth.add(fileTransferProgress);
		fileTransferProgress.setVisible(false);
		
		progressLabel = new JLabel("New label");
		panelSouth.add(progressLabel);
		progressLabel.setVisible(false);

		textFieldMsgToSend = new JTextField();
		panelSouth.add(textFieldMsgToSend);
		textFieldMsgToSend.setColumns(10);

		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		panelSouth.add(horizontalStrut_2);

		btnSendMsg = new JButton("\u53D1\u9001\u6D88\u606F"); // “发送消息”按钮
		btnSendMsg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String msgContent = textFieldMsgToSend.getText();
				String dstUser="";
				int selectedIndex=listOnlineUsers.getSelectedIndex();
				dstUser=onlinUserDlm.getElementAt(selectedIndex);
				if (msgContent.length() > 0&&dstUser.equals("")) {
					// 将消息文本框中的内容作为公聊消息发送给服务器
					ChatMessage chatMessage = new ChatMessage(localUserName,
							"", msgContent);
					try {
						synchronized (oos) {
							oos.writeObject(chatMessage);
							oos.flush();
						}
						// 在“消息记录”文本框中用蓝色显示发送的消息及发送时间
						String msgRecord = dateFormat.format(new Date()) + "向大家说:"
								+ msgContent + "\r\n";
						addMsgRecord(msgRecord, Color.blue, 12, false, false);
					} catch (IOException e1) {
						e1.printStackTrace();
					}	
				}else {

					ChatMessage chatMessage=new ChatMessage(localUserName, dstUser, msgContent);
					try {
						synchronized (oos) {
							oos.writeObject(chatMessage);
							oos.flush();
						}
						// 在“消息记录”文本框中用蓝色显示发送的消息及发送时间
						String msgRecord = dateFormat.format(new Date()) + "向"+dstUser+"说 "
								+ msgContent + "\r\n";
						addMsgRecord(msgRecord, Color.blue, 12, false, false);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		panelSouth.add(btnSendMsg);

		Component horizontalStrut_3 = Box.createHorizontalStrut(20);
		panelSouth.add(horizontalStrut_3);

		btnSendFile = new JButton("\u53D1\u9001\u6587\u4EF6");
		btnSendFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				JFileChooser fileChooser=new JFileChooser();
				String dstUser=null;
				int selectIndex=listOnlineUsers.getSelectedIndex();
				System.out.println(selectIndex);
				dstUser=onlinUserDlm.getElementAt(selectIndex);
				fileChooser.showOpenDialog(null);   //显示选择文件窗口
				File file=fileChooser.getSelectedFile();  //建立File对象保存选中的文件
				System.out.println("用户名为"+dstUser);
				String fileName=file.getName();
				sendFileMessage(file, fileName, localUserName, dstUser,file.getAbsolutePath());
			}
		});
		panelSouth.add(btnSendFile);

		// 将发送文件按钮设为不可用状态
		btnSendFile.setEnabled(false);
		// 将发送消息按钮设为不可用状态
		btnSendMsg.setEnabled(false);
		
		
		
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
		
		
		try {//处理线程
						
			ListeningHandler listeningHandler=new ListeningHandler(); 
			new Thread(listeningHandler).start(); 
		    //启动客户端的后台监听线程
		    }catch (Exception e) {
			// TODO: handle exception
		}
				
		
	}

	// 向消息记录文本框中添加一条消息记录
	private void addMsgRecord(final String msgRecord, Color msgColor,
			int fontSize, boolean isItalic, boolean isUnderline) {
		final SimpleAttributeSet attrset = new SimpleAttributeSet();
		StyleConstants.setForeground(attrset, msgColor);
		StyleConstants.setFontSize(attrset, fontSize);
		StyleConstants.setUnderline(attrset, isUnderline);
		StyleConstants.setItalic(attrset, isItalic);
		SwingUtilities.invokeLater(new Runnable() {
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
	
	
	private void sendFileMessage(File file,String fileName,String srcUser,String dstUser,String filepath) {
		FileRequestMessage fileRequestMessage=new FileRequestMessage(localUserName, fileName, filepath, dstUser);
		if(dstUser!=null) {
			try {
				oos.writeObject(fileRequestMessage);
				oos.flush();
				String msgRecord = dateFormat.format(new Date())
						+ " 你向用户 "+dstUser+" 发送文件 "+fileName+" ,大小为 "+getFormatFileSize(file.length())+" ,等待接收\r\n";
				addMsgRecord(msgRecord, Color.orange, 12, false, false);
			}catch(IOException e) {
				e.printStackTrace();
			}
			
		}else {
			String msgRecord = dateFormat.format(new Date())
					+ " 你群发了文件 "+fileName+" ,大小为 "+getFormatFileSize(file.length())+"\r\n";
			addMsgRecord(msgRecord, Color.orange, 12, false, false);
		}
	}
	
	
	
	private String getFormatFileSize(long size) {  
		DecimalFormat formater = new DecimalFormat("####.00");  
        if(size<1024){  
            return size+"bytes";  
        }else if(size<1024*1024){  
            float kbsize = size/1024f;    
            return formater.format(kbsize)+"KB";  
        }else if(size<1024*1024*1024){  
            float mbsize = size/1024f/1024f;    
            return formater.format(mbsize)+"MB";  
        }else if(size<1024*1024*1024*1024){  
            float gbsize = size/1024f/1024f/1024f;    
            return formater.format(gbsize)+"GB";  
        }else{  
            return "size: error";  
        } 
    }  
	
	

	// 后台监听线程
	class ListeningHandler implements Runnable {
		
		@Override
		public void run() {
			try {
				while (true) {
					Message msg = null;
					synchronized (ois) {
						msg = (Message) ois.readObject();
					}
					if(msg instanceof UserLoginStateMessage) {
//						System.out.println("........................................");
							if(((UserLoginStateMessage) msg).getUserState()) {
								UserStateMessage userStateMessage = new UserStateMessage(
										localUserName, "", true);
								try {
									oos.writeObject(userStateMessage);
									oos.flush();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								// 在“消息记录”文本框中用红色添加“XX时间登录成功”的信息
								String msgRecord = dateFormat.format(new Date())
										+ " 登录成功\r\n";
								addMsgRecord(msgRecord, Color.red, 12, false, false);
								// 创建并启动“后台监听线程”,监听并处理服务器传来的信息
								new Thread(new ListeningHandler()).start();
								// 将“登录”按钮设为“退出”按钮
								btnLogon.setText("退出");
								// 将发送文件按钮设为可用状态
								btnSendFile.setEnabled(true);
								// 将发送消息按钮设为可用状态
								btnSendMsg.setEnabled(true);
							}
							else
							{
								JOptionPane.showMessageDialog(null, "登陆失败，请检查你的用户名和密码！");
							}
						}
					else 
						if (msg instanceof UserStateMessage) {
						// 处理用户状态消息
						processUserStateMessage((UserStateMessage) msg);
					} else if(msg instanceof ForcedOfflineMessage){
						String username=((ForcedOfflineMessage)msg).getDstUser();
						System.out.println(username);
						UserStateMessage userStateMessage = new UserStateMessage(
								localUserName, "", false);
						try {
						     synchronized (oos) {
								oos.writeObject(userStateMessage);
								oos.flush();
							}
								
						} catch (IOException e1) {
								e1.printStackTrace();
						}
						
						
						
						
					} else if (msg instanceof ChatMessage) {
						// 处理聊天消息
						processChatMessage((ChatMessage) msg);
					} else if(msg instanceof FileConfirmMessage) {
						boolean confirmState;
						InetAddress inetAddress=((FileConfirmMessage)msg).getInetAddress();
						int port=((FileConfirmMessage)msg).getPort();
						String filepath=((FileConfirmMessage)msg).getPath2Send();
						String srcUser=((FileConfirmMessage)msg).getSrcUser();
						confirmState=((FileConfirmMessage)msg).getFileConfirmState();
						System.out.println(confirmState);
						
						srcUser=((FileConfirmMessage)msg).getSrcUser();
						if(confirmState) {
							String msgRecord = dateFormat.format(new Date()) + " "+srcUser+" 同意了你的发送请求,开始发送文件\r\n";
							addMsgRecord(msgRecord, Color.orange, 12, false, false);
							new Thread(new FileSendingHandler(inetAddress, filepath,port)).start();
						}
						else if(!confirmState) {
							String msgRecord = dateFormat.format(new Date()) + " "+srcUser+" 拒绝了你的文件发送请求\r\n";
							addMsgRecord(msgRecord, Color.red, 12, false, false);
						}
					}else if(msg instanceof FileRequestMessage) {
						String fileSender=((FileRequestMessage)msg).getSrcUser();
						
						String fileName=((FileRequestMessage)msg).getFileName();
						
		                
						if (JOptionPane.showConfirmDialog(null, fileSender+"向你发送文件 "+fileName, "接收确认",
								JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
							    processFileRequestMessage((FileRequestMessage)msg);
							} 
						else {
							FileConfirmMessage fileConfirmMessage=new FileConfirmMessage(false, localUserName, fileSender);
							try {
								synchronized (oos) {
									oos.writeObject(fileConfirmMessage);
									oos.flush();
								}
								
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
			             
					}
					else {
						// 这种情况对应着用户发来的消息格式 错误，应该发消息提示用户，这里从略
						System.err.println("用户发来的消息格式错误!");
					} 
				}
			} catch (IOException e) {
				if (e.toString().endsWith("Connection reset")) {
					System.out.println("服务器端退出");
				} else {
					e.printStackTrace();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
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

		// 处理用户状态消息
		private void processUserStateMessage(UserStateMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			if (msg.isUserOnline()) {
				if (msg.isPubUserStateMessage()) { // 新用户上线消息
					// 用绿色文字将用户名和用户上线时间添加到“消息记录”文本框中
					final String msgRecord = dateFormat.format(new Date())
							+ " " + srcUser + "上线了!\r\n";
					addMsgRecord(msgRecord, Color.green, 12, false, false);
					// 在“在线用户”列表中增加新上线的用户名
					onlinUserDlm.addElement(srcUser);
					
				}
				if (dstUser.equals(localUserName)) { // 用户在线消息
					onlinUserDlm.addElement(srcUser);
				}
			} else if (msg.isUserOffline()) { // 用户下线消息
				if (onlinUserDlm.contains(srcUser)) {
					// 用绿色文字将用户名和用户下线时间添加到“消息记录”文本框中
					final String msgRecord = dateFormat.format(new Date())
							+ " " + srcUser + "下线了!\r\n";
					addMsgRecord(msgRecord, Color.green, 12, false, false);
					// 在“在线用户”列表中删除下线的用户名
					onlinUserDlm.removeElement(srcUser);
				}
			}
		}

		// 处理服务器转发来的公聊消息
		private void processChatMessage(ChatMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			String msgContent = msg.getMsgContent();
			if (onlinUserDlm.contains(srcUser)) {
				if (msg.isPubChatMessage() || dstUser.equals(localUserName)) {
//					Pattern pattern=Pattern.compile("\\S*\\s");
//					Matcher matcher=pattern.matcher(msgContent);
//					String content=null;
//					while(matcher.find()) {
//						dstUser=matcher.group().substring(1).trim();
//						content=msgContent.replace(matcher.group(), "");
//					}
					
					// 用黑色文字将收到消息的时间、发送消息的用户名和消息内容添加到“消息记录”文本框中
					final String msgRecord = dateFormat.format(new Date())
							+ " " + srcUser + "说: " + msgContent + "\r\n";
					addMsgRecord(msgRecord, Color.black, 12, false, false);
				}else {
					final String msgRecord = dateFormat.format(new Date())
							+ " " + srcUser + "对你说: " + msgContent + "\r\n";
					addMsgRecord(msgRecord, Color.black, 12, false, false);
				}
			}
		}	
		
	}
	

	
	
	
	private void processFileRequestMessage(FileRequestMessage msg) {
		String dstUser=msg.getSrcUser();
		String path=msg.get2SendFilePath();
		String fileName=msg.getFileName();
		try {
			new Thread(new FileTransferHostHandler(localUserName, dstUser, path,fileName)).start();
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	class FileSendingHandler implements Runnable {
		Socket sendingsocket;
		String path;
		int port;
		public FileSendingHandler(InetAddress inetAddress,String path,int port) throws IOException {
			System.out.println(port);
			sendingsocket=new Socket(inetAddress, port);	
			this.path=path;
		}
		@Override
		public void run() {
			if(sendingsocket.isConnected()) {
				File file=new File(path);
				System.out.println(path);
				FileInputStream fis;
				DataOutputStream dos;
				if(file.exists()) {
					try {
						fis=new FileInputStream(file);
						dos=new DataOutputStream(sendingsocket.getOutputStream());
						
				        dos.writeUTF(file.getName());
				        dos.flush();
				        dos.writeLong(file.length());
				        dos.flush();
						System.out.println("开始传输文件");
						byte[] bytes=new byte[1024];
						int length=0;
						long progress=0;
						fileTransferProgress.setVisible(true);
						progressLabel.setVisible(true);
						while((length=fis.read(bytes,0,bytes.length))!=-1) {
//							dos.writeUTF(file.getName());
							dos.write(bytes,0,length);
							dos.flush();
			                progress += length;  
			                fileTransferProgress.setValue((int)(100*progress/file.length()));
			                int progressLevel=fileTransferProgress.getValue();
			                progressLabel.setText(progressLevel+"%");
			                System.out.print("| " + (100*progress/file.length()) + "% |");  
						}
						addMsgRecord("文件 "+file.getName()+" 发送成功,长度为： "+getFormatFileSize(file.length()), Color.orange, 12, false, false);
						System.out.println("\r\n文件传输完毕！");
						fileTransferProgress.setVisible(false);
						progressLabel.setVisible(false);
						fis.close();
						dos.close();
//						socket.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally {
						try {
							sendingsocket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
			}
			
		}
	}

	
	class FileTransferHostHandler implements Runnable{
		ServerSocket serverSocket=new ServerSocket(0, 3);
		
		Socket filesocket;
		String filepath;
		String fileName;
		private DataInputStream dis;
		private FileOutputStream fos;
		public FileTransferHostHandler(String localUserName,String fileSender,String path,String fileName) throws IOException {
			
//			InetAddress socketAddress=socket.getInetAddress();
			InetAddress inetAddress=serverSocket.getInetAddress();
			this.fileName=fileName;
			int port2package=serverSocket.getLocalPort();
			System.out.println(port2package);
//			InetAddress socketIPAddress=serversocket.getInetAddress();
//			int port2connect=socket.getPort();
			filepath=path;
			FileConfirmMessage fileConfirmMessage=new FileConfirmMessage(true, localUserName, fileSender,inetAddress,port2package,filepath);
			
			try {
				synchronized (oos) {
					oos.writeObject(fileConfirmMessage);
					oos.flush();
					System.out.println(".............");
					filesocket=serverSocket.accept();
					System.out.println("method runs successfully");
					
				}
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}	
			
		}
		@Override
		public void run() {
			System.out.println("准备接收文件");
			
			
			try {
				
				System.out.println("开始接收文件");
				dis=new DataInputStream(filesocket.getInputStream());
				String fileName=dis.readUTF();
				long fileLength=dis.readLong();
				System.out.println("…………………………");
				File directory = new File("E:\\ProjectCache");
				if(!directory.exists()) {
					directory.mkdir();
				}
				File file=new File(directory.getAbsolutePath()+File.separatorChar+fileName);
				fos=new FileOutputStream(file);
				fileTransferProgress.setVisible(true);
				long progress=0;
				progressLabel.setVisible(true);
				byte[] bytes=new byte[1024];
				int length=0;
				while((length=dis.read(bytes,0,bytes.length))!=-1) {
					fos.write(bytes, 0, length);
					fos.flush();
					progress += length;  
	                fileTransferProgress.setValue((int)(100*progress/fileLength));
	                int progressLevel=fileTransferProgress.getValue();
	                progressLabel.setText(progressLevel+"%");
				}
				
				//测试用，功能实现完毕后改用addMsgRecord
				addMsgRecord("文件 "+fileName+" 接收成功,长度为： "+getFormatFileSize(fileLength), Color.orange, 12, false, false);
				System.out.println("文件 "+fileName+" 接收成功,长度为： "+getFormatFileSize(fileLength));
				fileTransferProgress.setVisible(false);
				progressLabel.setVisible(false);
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				try {
					if(fos!=null) {
						fos.close();
						filesocket.close();
					}
					if(dis!=null) {
						dis.close();
						filesocket.close();
					}
				}catch(Exception e) {}
			}
		}
		
	}
}
