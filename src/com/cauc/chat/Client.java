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
	// �������û��б�ListModel��,����ά���������û��б�����ʾ������
	private final DefaultListModel<String> onlinUserDlm = new DefaultListModel<String>();
	// ���ڿ���ʱ����Ϣ��ʾ��ʽ
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

		btnLogon = new JButton("\u767B\u5F55"); // ����¼����ť
		btnLogon.addActionListener(new ActionListener() {
			
			
			
			
			
			
		
			
			
			public void actionPerformed(ActionEvent e) {
				if (btnLogon.getText().equals("��¼")) {
					localUserName = textFieldUserName.getText().trim();
					String localPassword=new String(passwordFieldPwd.getPassword());
					System.out.println(localPassword);
					if (localUserName.length() > 0) {
						// ��������˽���Socket���ӣ�����׳��쳣���򵯳��Ի���֪ͨ�û������˳�
						
						// ������������û�������Ϣ�����Լ����û������͸�������
						
						UserLoginMessage userLoginMessage=new UserLoginMessage(localUserName, localPassword);
						try {
							oos.writeObject(userLoginMessage);
							oos.flush();
							
						}catch(IOException e0) {
							e0.printStackTrace();
						}
					
						
						
					}
				} else if (btnLogon.getText().equals("�˳�")) {
					if (JOptionPane.showConfirmDialog(null, "�Ƿ��˳�?", "�˳�ȷ��",
							JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						// ������������û�������Ϣ
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

		btnSendMsg = new JButton("\u53D1\u9001\u6D88\u606F"); // ��������Ϣ����ť
		btnSendMsg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String msgContent = textFieldMsgToSend.getText();
				String dstUser="";
				int selectedIndex=listOnlineUsers.getSelectedIndex();
				dstUser=onlinUserDlm.getElementAt(selectedIndex);
				if (msgContent.length() > 0&&dstUser.equals("")) {
					// ����Ϣ�ı����е�������Ϊ������Ϣ���͸�������
					ChatMessage chatMessage = new ChatMessage(localUserName,
							"", msgContent);
					try {
						synchronized (oos) {
							oos.writeObject(chatMessage);
							oos.flush();
						}
						// �ڡ���Ϣ��¼���ı���������ɫ��ʾ���͵���Ϣ������ʱ��
						String msgRecord = dateFormat.format(new Date()) + "����˵:"
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
						// �ڡ���Ϣ��¼���ı���������ɫ��ʾ���͵���Ϣ������ʱ��
						String msgRecord = dateFormat.format(new Date()) + "��"+dstUser+"˵ "
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
				fileChooser.showOpenDialog(null);   //��ʾѡ���ļ�����
				File file=fileChooser.getSelectedFile();  //����File���󱣴�ѡ�е��ļ�
				System.out.println("�û���Ϊ"+dstUser);
				String fileName=file.getName();
				sendFileMessage(file, fileName, localUserName, dstUser,file.getAbsolutePath());
			}
		});
		panelSouth.add(btnSendFile);

		// �������ļ���ť��Ϊ������״̬
		btnSendFile.setEnabled(false);
		// ��������Ϣ��ť��Ϊ������״̬
		btnSendMsg.setEnabled(false);
		
		
		
		try {
			String passphrase = "654321";
			char[] password = passphrase.toCharArray();
			String trustStoreFile = "test.keys";    
			KeyStore ts = KeyStore.getInstance("JCEKS");
			ts.load(new FileInputStream(trustStoreFile), password);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ts); //�ͻ����õ��Ǳ��˵�֤�飬��Ȼû��˽Կ��
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);	//���ڿͻ�����˵������Ҫ���������ʾ֤�飬��һ������Ϊ�գ��ڶ����ǻ�ȡ����֤�飻�������������������	
			SSLSocketFactory factory=sslContext.getSocketFactory();
			socket=(SSLSocket)factory.createSocket(host,port);		
			String[] supportedSuites=socket.getSupportedCipherSuites();
//			Stream.of(supportedSuites).forEach(x -> System.out.println(x));		
			socket.setEnabledCipherSuites(supportedSuites);
			
			// ��socket����������������ֱ��װ�ɶ����������Ͷ��������
			oos = new ObjectOutputStream(socket
					.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (UnknownHostException e1) {
			JOptionPane.showMessageDialog(null, "�Ҳ�������������");
			e1.printStackTrace();
			System.exit(0);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null,
					"������I/O���󣬷�����δ������");
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
		
		
		try {//�����߳�
						
			ListeningHandler listeningHandler=new ListeningHandler(); 
			new Thread(listeningHandler).start(); 
		    //�����ͻ��˵ĺ�̨�����߳�
		    }catch (Exception e) {
			// TODO: handle exception
		}
				
		
	}

	// ����Ϣ��¼�ı��������һ����Ϣ��¼
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
						+ " �����û� "+dstUser+" �����ļ� "+fileName+" ,��СΪ "+getFormatFileSize(file.length())+" ,�ȴ�����\r\n";
				addMsgRecord(msgRecord, Color.orange, 12, false, false);
			}catch(IOException e) {
				e.printStackTrace();
			}
			
		}else {
			String msgRecord = dateFormat.format(new Date())
					+ " ��Ⱥ�����ļ� "+fileName+" ,��СΪ "+getFormatFileSize(file.length())+"\r\n";
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
	
	

	// ��̨�����߳�
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
								// �ڡ���Ϣ��¼���ı������ú�ɫ��ӡ�XXʱ���¼�ɹ�������Ϣ
								String msgRecord = dateFormat.format(new Date())
										+ " ��¼�ɹ�\r\n";
								addMsgRecord(msgRecord, Color.red, 12, false, false);
								// ��������������̨�����̡߳�,�����������������������Ϣ
								new Thread(new ListeningHandler()).start();
								// ������¼����ť��Ϊ���˳�����ť
								btnLogon.setText("�˳�");
								// �������ļ���ť��Ϊ����״̬
								btnSendFile.setEnabled(true);
								// ��������Ϣ��ť��Ϊ����״̬
								btnSendMsg.setEnabled(true);
							}
							else
							{
								JOptionPane.showMessageDialog(null, "��½ʧ�ܣ���������û��������룡");
							}
						}
					else 
						if (msg instanceof UserStateMessage) {
						// �����û�״̬��Ϣ
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
						// ����������Ϣ
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
							String msgRecord = dateFormat.format(new Date()) + " "+srcUser+" ͬ������ķ�������,��ʼ�����ļ�\r\n";
							addMsgRecord(msgRecord, Color.orange, 12, false, false);
							new Thread(new FileSendingHandler(inetAddress, filepath,port)).start();
						}
						else if(!confirmState) {
							String msgRecord = dateFormat.format(new Date()) + " "+srcUser+" �ܾ�������ļ���������\r\n";
							addMsgRecord(msgRecord, Color.red, 12, false, false);
						}
					}else if(msg instanceof FileRequestMessage) {
						String fileSender=((FileRequestMessage)msg).getSrcUser();
						
						String fileName=((FileRequestMessage)msg).getFileName();
						
		                
						if (JOptionPane.showConfirmDialog(null, fileSender+"���㷢���ļ� "+fileName, "����ȷ��",
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
						// ���������Ӧ���û���������Ϣ��ʽ ����Ӧ�÷���Ϣ��ʾ�û����������
						System.err.println("�û���������Ϣ��ʽ����!");
					} 
				}
			} catch (IOException e) {
				if (e.toString().endsWith("Connection reset")) {
					System.out.println("���������˳�");
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

		// �����û�״̬��Ϣ
		private void processUserStateMessage(UserStateMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			if (msg.isUserOnline()) {
				if (msg.isPubUserStateMessage()) { // ���û�������Ϣ
					// ����ɫ���ֽ��û������û�����ʱ����ӵ�����Ϣ��¼���ı�����
					final String msgRecord = dateFormat.format(new Date())
							+ " " + srcUser + "������!\r\n";
					addMsgRecord(msgRecord, Color.green, 12, false, false);
					// �ڡ������û����б������������ߵ��û���
					onlinUserDlm.addElement(srcUser);
					
				}
				if (dstUser.equals(localUserName)) { // �û�������Ϣ
					onlinUserDlm.addElement(srcUser);
				}
			} else if (msg.isUserOffline()) { // �û�������Ϣ
				if (onlinUserDlm.contains(srcUser)) {
					// ����ɫ���ֽ��û������û�����ʱ����ӵ�����Ϣ��¼���ı�����
					final String msgRecord = dateFormat.format(new Date())
							+ " " + srcUser + "������!\r\n";
					addMsgRecord(msgRecord, Color.green, 12, false, false);
					// �ڡ������û����б���ɾ�����ߵ��û���
					onlinUserDlm.removeElement(srcUser);
				}
			}
		}

		// ���������ת�����Ĺ�����Ϣ
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
					
					// �ú�ɫ���ֽ��յ���Ϣ��ʱ�䡢������Ϣ���û�������Ϣ������ӵ�����Ϣ��¼���ı�����
					final String msgRecord = dateFormat.format(new Date())
							+ " " + srcUser + "˵: " + msgContent + "\r\n";
					addMsgRecord(msgRecord, Color.black, 12, false, false);
				}else {
					final String msgRecord = dateFormat.format(new Date())
							+ " " + srcUser + "����˵: " + msgContent + "\r\n";
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
						System.out.println("��ʼ�����ļ�");
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
						addMsgRecord("�ļ� "+file.getName()+" ���ͳɹ�,����Ϊ�� "+getFormatFileSize(file.length()), Color.orange, 12, false, false);
						System.out.println("\r\n�ļ�������ϣ�");
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
			System.out.println("׼�������ļ�");
			
			
			try {
				
				System.out.println("��ʼ�����ļ�");
				dis=new DataInputStream(filesocket.getInputStream());
				String fileName=dis.readUTF();
				long fileLength=dis.readLong();
				System.out.println("��������������������");
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
				
				//�����ã�����ʵ����Ϻ����addMsgRecord
				addMsgRecord("�ļ� "+fileName+" ���ճɹ�,����Ϊ�� "+getFormatFileSize(fileLength), Color.orange, 12, false, false);
				System.out.println("�ļ� "+fileName+" ���ճɹ�,����Ϊ�� "+getFormatFileSize(fileLength));
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
